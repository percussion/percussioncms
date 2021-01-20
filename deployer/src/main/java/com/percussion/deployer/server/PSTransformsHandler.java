/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.deployer.server;

import com.percussion.deployer.PSMappingElement;
import com.percussion.deployer.error.PSDeployException;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSDeployableElement;
import com.percussion.deployer.objectstore.PSIdMap;
import com.percussion.deployer.objectstore.PSIdMapping;
import com.percussion.deployer.objectstore.PSImportPackage;
import com.percussion.security.PSSecurityToken;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The worker class that handles automatic id mapping during install.
 */
public class PSTransformsHandler 
{
   /**
    * Constructs the class and initializes the id map object for the specified
    * source.
    * 
    * @param tok the security token required to catalog elements on target
    * server, may not be <code>null</code>.
    * @param sourceName the source server name to which id map needs to be 
    * initialized, may not be <code>null</code> or empty.
    * @param packages the list of {@link PSImportPackage} objects for which
    * source id's will be mapped, may not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   @SuppressWarnings("unchecked")
   public PSTransformsHandler(PSSecurityToken tok, String sourceName,
         List<PSImportPackage> packages) 
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      
      if (StringUtils.isBlank(sourceName))
         throw new IllegalArgumentException("sourceName may not be blank");
      
      if (packages == null)
         throw new IllegalArgumentException("packages may not be null");
       
      m_idMap = new PSIdMap(sourceName);     
      m_tok = tok;
      m_packages = packages;
   }
   
   /**
    * Gets a shallow copy of the id map after id mapping is performed.  Only
    * valid mappings will be included in the map.
    * See {@link #getValidMappings(PSIdMap)} for details.
    * 
    * @return The transformed id map on the target for a source repository,
    * never <code>null</code>.
    * 
    * @throws PSDeployException If an error occurs during target guessing.
    */
   public PSIdMap getIdMap() throws PSDeployException
   {
      // transform id's
      guessAll();
      
      // gather valid mappings
      PSIdMap validMap = getValidMappings(m_idMap); 
      
      return validMap;
   }
   
   /**
    * Guess target for all package elements.
    *
    * @throws PSDeployException If an error occurs during target guessing.
    */
   private void guessAll()
      throws PSDeployException
   {
      List<PSIdMapping> allMappings = new ArrayList<PSIdMapping>();

      for (PSImportPackage pkg : m_packages)
      {
         Iterator<PSIdMapping> idMappings = getIDMappings(true, 
               pkg.getPackage());
         while (idMappings.hasNext())
         {
            allMappings.add(idMappings.next());
         }
      }

      // now guess them all at once
      List<PSIdMapping> unmapped = guessTarget(allMappings.iterator());

      // now re-guess any unmapped children
      guessTarget(unmapped.iterator());
   }
   
   /**
    * Checks whether the ids need to be mapped. If the source and target share 
    * the same repository, the ids don't need to be mapped.
    * 
    * @return <code>true</code> if the ids need to be mapped, otherwise <code>
    * false</code>
    */
   private boolean needToMapIds()
   {
      return m_idMap != null;
   }
   
   /**
    * Checks whether id map can be modified.  See {@link #needToMapIds()} for 
    * more info.
    * 
    * @throws IllegalStateException if id map can not be modifiable.
    */
   private void checkModifyIdMap()
   {
      if (!needToMapIds())
      {
         throw new IllegalStateException("Trying to get/add/save the id map," +
            "but the id map does not exist for the current source and target");
      }
   }

   /**
    * Gets the id mappings of the supplied package. Creates new mappings for the
    * the dependencies that supports id mappings and does not have a mapping in
    * the id map and adds them to map.
    * 
    * @param unMappedOnly if <code>true</code> it gets the mappings that are not
    * mapped to target.
    * @param depElement the package for which we have to find the mappings, may 
    * not be <code>null</code>
    * 
    * @return the list of mappings, never <code>null</code>, may be empty.
    * 
    * @throws IllegalArgumentException if any parameter is invalid.
    * @throws IllegalStateException if id map can not be modifiable.
    */
   @SuppressWarnings("unchecked")
   private Iterator<PSIdMapping> getIDMappings(boolean unMappedOnly, 
      PSDeployableElement depElement)
   {
      if (depElement == null)
         throw new IllegalArgumentException("depElement may not be null.");
      
      checkModifyIdMap();
      
      Set<PSIdMapping> mappings = new HashSet<PSIdMapping>();
      
      List<PSDependency> deps = new ArrayList<PSDependency>();
      getSupportedIdMapDependencies(depElement, deps);
      Iterator<PSDependency> idMapDeps = deps.iterator();
      while (idMapDeps.hasNext())
      {
         PSDependency dep = idMapDeps.next();
         String depId = dep.getDependencyId();
         String objType = dep.getObjectType();
         
         PSIdMapping mapping;
         if (!dep.supportsParentId())         
         {
            mapping = m_idMap.getMapping(depId, objType);
            if (mapping == null)
            {
               mapping = new PSIdMapping(
                  depId, dep.getDisplayName(), objType, true);
               m_idMap.addMapping(mapping);                  
            }
         }
         else
         {
            PSIdMapping parentMapping = m_idMap.getMapping(dep.getParentId(), 
               dep.getParentType());
            if (parentMapping == null) 
            {
               //this should not happen if we go in the order
               PSDependency parent = 
                  getParentDependency(dep, dep.getParentType(), depElement);
               if (parent == null)
               {
                  throw new IllegalStateException(
                     "could not find a parent for a child " + 
                     "that supports parent id");
               }
               parentMapping = new PSIdMapping(parent.getDependencyId(), 
                  parent.getDisplayName(), parent.getObjectType(), true);
               m_idMap.addMapping(parentMapping);         
               if (!unMappedOnly || !parentMapping.isMapped())              
                  mappings.add(parentMapping);            
            }
            mapping = m_idMap.getMapping(
               depId, objType, dep.getParentId(), dep.getParentType());               
            if (mapping == null)
            {
               mapping = new PSIdMapping(
                  depId, dep.getDisplayName(), objType, dep.getParentId(),                   
                  parentMapping.getSourceName(), dep.getParentType(), true);
               m_idMap.addMapping(mapping);               
            }                           
         }
            
         //Add always if user requested all mappings, otherwise adds only  the
         //the mappings that were not mapped.
         if (!unMappedOnly || !mapping.isMapped())
            mappings.add(mapping);
      }
          
      return mappings.iterator();
   }
   
   /**
    * Traverses the child dependencies of the parent recursively to find the 
    * immediate parent of the supplied dependency (<code>dep</code>) whose type
    * is supplied parent type.
    * 
    * @param dep the dependency for which to find the parent, assumed not to be
    * <code>null</code>
    * @param parentType the type of parent dependency, assumed not to be <code>
    * null</code> or empty.
    * @param root the root dependency to search for, assumed not to be 
    * <code>null</code>
    * 
    * @return the parent dependency, may be <code>null</code> if it could not
    * find the parent.
    * @throws IllegalStateException if the found parent does not support id 
    * mapping or reached the dependency in the tree but not found the parent.
    */
   @SuppressWarnings("unchecked")
   private PSDependency getParentDependency(PSDependency dep, String parentType,
      PSDependency root)
   {   
      /* Reached the actual dependency for which we are finding the parent, that 
       * means we didn't find a parent for the dependency matching the supplied 
       * parent type in the dependency tree, that should not happen so throw
       * exception.
       */
      if (dep == root) 
      {
         throw new IllegalStateException("required parent is not found");
      }
      if (root.getObjectType().equals(parentType) && 
         root.containsDependency(dep))
      {
         if (!root.supportsIDMapping())
         {
            throw new IllegalStateException(
               "Found parent that does not support id mapping");
         }
         return root;
      }
      
      PSDependency parent = null;
      Iterator childDeps = root.getDependencies();
      if (childDeps != null)
      {
         while (childDeps.hasNext() && parent != null)
         {
            parent = getParentDependency(dep, parentType, 
               (PSDependency)childDeps.next());            
         }
      }
      
      return parent; 
   }
   
   /**
    * Checks the supplied dependency and its child dependencies recursively and 
    * gets the list of dependencies that supports id mapping.
    * 
    * @param dependency the dependency to check for, assumed not to be <code>
    * null</code>
    * @param idMapDeps the list of dependencies that support id maps, gets 
    * updated in this method. Assumed not <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   private void getSupportedIdMapDependencies(PSDependency dependency, 
      List<PSDependency> idMapDeps)
   {
      if (dependency.supportsIDMapping())
         idMapDeps.add(dependency);
      Iterator childDeps = dependency.getDependencies();
      if (childDeps != null)
      {
         while(childDeps.hasNext())
         {
            getSupportedIdMapDependencies((PSDependency)childDeps.next(), 
               idMapDeps);
         }
      }
   }   
   
   /**
    * Guess target for the supplied mappings.  First guesses by name and id, 
    * then if no match, guesses by name alone.  Does not guess for any
    * 
    * @param idMappings Mappings to guess, may or may not already have a target
    * set (will only guess unmapped mappings).  May not be <code>null</code>, 
    * may be empty.
    * 
    * @return A List of unmodified mappings that could not be mapped due to 
    * unmapped parents.  A dependency that supports parent ids can only be 
    * mapped if its parent id has already been mapped.  Never <code>null</code>, 
    * may be empty.  
    * 
    * @throws PSDeployException if there are any errors. 
    */
   private List<PSIdMapping> guessTarget(Iterator<PSIdMapping> idMappings)
      throws PSDeployException
   {
      List<PSIdMapping> unMatchedById = new ArrayList<PSIdMapping>();
      List<PSIdMapping> unMatchedParentList = new ArrayList<PSIdMapping>();
      while (idMappings.hasNext())
      {
         PSIdMapping mapping = idMappings.next();
         if (mapping != null && !mapping.isMapped())
         {
            //guess target by name and id
            guessTarget(mapping, true);
                        
            // save those needing parent, or subsequent guess by
            // name only
            if (!mapping.isMapped())
            {
               unMatchedParentList.add(mapping);
            }
            else if(mapping.isNewObject())
            {
               unMatchedById.add(mapping);
            }
         }
      }
         
      //now guess target by name alone
      Iterator<PSIdMapping> iter = unMatchedById.iterator();
      while (iter.hasNext())
      {
         guessTarget(iter.next(), false); 
      }
          
      return unMatchedParentList;
   }
   
   /**
    * Guesses target for the provided mapping. Gets the unmapped target elements
    * for the supplied mapping and tries to find the element matching source 
    * name and/or id case insensitively. If it finds a matching element, the 
    * target of mapping is set with that element, otherwise the source element 
    * is set as new object in the mapping. If the source element has a parent id
    * and if the parent id is not mapped, it simply returns without guessing 
    * target for the mapping.
    * 
    * @param mapping the mapping that is not mapped, may not be <code>null
    * </code>
    * @param mustMatchById supply <code>true</code> to must match by id also, 
    * otherwise <code>false</code>.
    * 
    * @throws IllegalArgumentException if mapping is <code>null</code>
    * @throws IllegalStateException if id map can not be modifiable.
    * @throws PSDeployException if exception happens cataloging.
    */
   private void guessTarget(PSIdMapping mapping, boolean mustMatchById) 
      throws PSDeployException
   {
      checkModifyIdMap();
      
      if (mapping == null)
         throw new IllegalArgumentException("mapping may not be null.");
         
      if (mapping.getParentType() != null && 
         !m_idMap.isMapped(mapping.getSourceParentId(),
               mapping.getParentType()))
      {
         return; //simply return because you don't know about parent.
      }
      
      PSMappingElement targetEl = guessTarget(
         mapping.getObjectType(), mapping.getSourceName(), 
         mapping.getSourceId(), mapping.getParentType(), 
         mapping.getSourceParentId(), mustMatchById);
         
      if (targetEl != null)
      {
         mapping.setIsNewObject(false);      
         mapping.setTarget(targetEl.getId(), targetEl.getName(), 
            targetEl.getParentId(), targetEl.getParentName());
      }      
      else
         mapping.setIsNewObject(true);
   }
   
   /**
    * Guesses the target element for the supplied source element by matching 
    * names case insensitively. If it finds more than one match, then tries to 
    * get the one matching id, if it didn't find any, gets the first one in the
    * list. If <code>mustMatchById</code> is <code>true</code> it always tries
    * to match by id also.
    * 
    * @param elementType the element type of the source, assumed not to be 
    * <code>null</code> or empty.
    * @param sourceName the name of the source element, assumed not to be 
    * <code>null</code> or empty.
    * @param sourceId the id of the source element, assumed not to be 
    * <code>null</code> or empty.
    * @param parentType the parent type of the source element type, may be 
    * <code>null</code> if source does not have a parent.
    * @param parentId the parent id of the source element, may be <code>null
    * </code> if source does not have a parent.
    * @param mustMatchById supply <code>true</code> to must match by id also, 
    * otherwise <code>false</code>.
    * 
    * @return the target element, may be <code>null</code> if it didn't find a
    * target by matching name case insensitively.
    * 
    * @throws PSDeployException if an error happens getting target elements.
    */
   private PSMappingElement guessTarget(String elementType, String sourceName,
      String sourceId, String parentType, String parentId, 
      boolean mustMatchById) throws PSDeployException
   {
      List<PSMappingElement> targetElements = getUnmappedTargetElements(
            elementType, parentType, parentId);      
               
      List<PSMappingElement> matchingElements = 
         new ArrayList<PSMappingElement>();
      //guess by name
      Iterator<PSMappingElement> elements = targetElements.iterator();
      while (elements.hasNext())
      {
         PSMappingElement target = elements.next();
         if (target.getName().equalsIgnoreCase(sourceName))
            matchingElements.add(target);
      }

      PSMappingElement target = null;      
      if (!matchingElements.isEmpty())
      {
         elements = matchingElements.iterator();

         while (elements.hasNext() && target == null)
         {
            PSMappingElement tg = elements.next();
            if (tg.getId().equals(sourceId))
               target = tg;  
         }
         if (target == null && !mustMatchById)
            target = matchingElements.get(0);
      }
      
      return target;
   }
   
   /**
    * Gets the unmapped target elements for the specified element (object) type
    * and the source parent id (if it has) from the current id mappings.
    * 
    * @param objectType the object/element type to get the elements, may not be
    * <code>null</code> or empty.
    * @param parentType the parent element type, may not be <code>null</code> or
    * empty if the supplied <code>objectType</code> has parent type, otherwise
    * it must be <code>null</code>
    * @param sourceParentId the parent id of the source, may not be <code>null
    * </code> or empty if the <code>parentType</code> is not <code>null</code>
    * 
    * @return the list of target (<code>PSMappingElement</code>) elements, never
    * <code>null</code>, may be empty when all target elements are mapped or no
    * existing target elements or supplied parent id (element) is being added to
    * server.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if an exception happens cataloging target 
    * elements.
    * @throws IllegalStateException if id map does not exist.
    */
   @SuppressWarnings("unchecked")
   private List<PSMappingElement> getUnmappedTargetElements(String objectType, 
      String parentType, String sourceParentId) 
      throws PSDeployException
   {
      checkModifyIdMap();
      
      if (objectType == null || objectType.trim().length() == 0)
      {
         throw new IllegalArgumentException(
            "objectType may not be null or empty.");
      }

      if (hasParentType(objectType))
      { 
         if (parentType == null || parentType.trim().length() == 0)
         {
            throw new IllegalArgumentException("parentId may not be null or "
               + "empty if objectType supports parent type");
         }
      }
      else
      {
         if (parentType != null)
         {
            throw new IllegalArgumentException("parentType must be null if " +
               "objectType does not support parent type");
         }
      }
      
      if (parentType != null)
      {
         if (sourceParentId == null || sourceParentId.trim().length() == 0)
         {
            throw new IllegalArgumentException("sourceParentId may not be null"
               + "or empty if objectType supports parent type.");
         }
      }
      else if (sourceParentId != null)
      {
         throw new IllegalArgumentException("sourceParentId must be null if " + 
            "objectType does not support parent type");      
      }
      
      List<PSMappingElement> targetElements = new ArrayList<PSMappingElement>();
            
      String targetParentId = null;
      if (sourceParentId != null)
      {
         PSIdMapping parentMapping = 
            m_idMap.getMapping(sourceParentId, parentType);      
         if (parentMapping == null || !parentMapping.isMapped())
         {
            throw new IllegalArgumentException(
               "sourceParentId must be mapped before mapping child");
         }
                        
         targetParentId = parentMapping.getTargetId();
         //The mapping have been mapped as new object, so this should return 
         //an empty list.
         if (targetParentId == null)
            return targetElements;
      }
 
      Set<String> usedElements = new HashSet<String>();       
      Iterator mappings = m_idMap.getMappings();
      while (mappings.hasNext())
      {
         PSIdMapping mapping = (PSIdMapping)mappings.next();
         if (mapping.getObjectType().equals(objectType) && 
            mapping.getTargetId() != null)
         {         
            usedElements.add(mapping.getTargetId() + 
            (mapping.getParentType() == null ? "" : 
            "-" + mapping.getTargetParentId())
            );         
         }
      }

      Iterator<PSMappingElement> iter = getElementsByType(objectType);
      while (iter.hasNext())
      {
         PSMappingElement element = iter.next();     
         String uniqueID = element.getId();
         if (element.hasParent())
         {
            uniqueID += "-" + element.getParentId();
         }
         
         if (!usedElements.contains(uniqueID) &&
               (targetParentId == null ||
                     targetParentId.equals(element.getParentId())))
         {
            targetElements.add(element);
         }
      }
      
      return targetElements;
   }
   
   /**
    * Gets the mapping elements on the server for the specified type.  See
    * {@link PSMappingElement} for details.
    *
    * @param type the type of elements, assumed not be <code>null</code>.
    *
    * @return the list of <code>PSMappingElement</code>s, never <code>null
    * </code>, may be empty.
    *
    * @throws PSDeployException if any error happens cataloging.
    */
   @SuppressWarnings("unchecked")
   private Set<PSMappingElement> getMappingElements(String type)
      throws PSDeployException
   {
      Set<PSMappingElement> mapElems = new HashSet<PSMappingElement>();
      
      Iterator iter = PSDependencyManager.getInstance().getDependencies(m_tok,
            type);
   
      while (iter.hasNext())
      {
         PSDependency dep = (PSDependency) iter.next();
         PSMappingElement element = new PSMappingElement(dep.getObjectType(),
               dep.getDependencyId(), dep.getDisplayName());     
         mapElems.add(element);
      }
      
      return mapElems;
   }
   
   /**
    * Gets the list of elements on the server of the specified type. If the
    * elements are not yet cataloged for the supplied type, catalogs and caches
    * them. Uses case-sensitive comparison to check whether the supplied type is
    * already cataloged or not.  If the supplied type has parent type, then the
    * elements of the type will be all child elements of each element of parent
    * type.
    *
    * @param type the type of elements, may not be <code>null</code> or empty
    * and must be a valid element type.
    *
    * @return the list of <code>PSMappingElement</code>s, never <code>null
    * </code>, may be empty.
    *
    * @throws IllegalArgumentException if type is not valid.
    * @throws IllegalStateException if server is not connected.
    * @throws PSDeployException if any error happens cataloging.
    */
   @SuppressWarnings("unchecked")
   private Iterator<PSMappingElement> getElementsByType(String type)
      throws PSDeployException
   {
      if (type == null || type.trim().length() == 0)
         throw new IllegalArgumentException("type may not be null or empty");

      Set<PSDependencyDef> idTypes = getLiteralIDTypes();
      Iterator<PSDependencyDef> iter = idTypes.iterator();
      boolean isValid = false;
      while (iter.hasNext())
      {
         if (iter.next().getObjectType().equals(type))
         {
            isValid = true;
            break;
         }
      }

      if (!isValid)
         throw new IllegalArgumentException("type is not valid.");

      if (m_typeToMapElems.get(type) == null)
      {
         List<PSMappingElement> elements = new ArrayList<PSMappingElement>();
         if (hasParentType(type))
         {
            String parentType = getParentType(type);
            Iterator<PSMappingElement> parentIter = getElementsByType(
                  parentType);
            while (parentIter.hasNext())
            {
               PSMappingElement parentElement = parentIter.next();
               Iterator childElements = 
                  PSDependencyManager.getInstance().getDependencies(m_tok,
                        type, parentElement.getId());
               while (childElements.hasNext())
               {
                  PSDependency element = (PSDependency)childElements.next();
                  PSMappingElement childElement = new PSMappingElement(
                        element.getObjectType(), element.getDependencyId(),
                        element.getDisplayName());
                  childElement.setParent(parentElement.getType(),
                        parentElement.getId(),
                        parentElement.getName());
                  elements.add(childElement);
               }
            }
         }
         else
         {
            elements.addAll(getMappingElements(type));
         }
         
         if (!elements.isEmpty())
            Collections.sort(elements);

         m_typeToMapElems.put(type, elements);
      }
   
      return m_typeToMapElems.get(type).iterator();
   }
   
   /**
    * Gets the map of child to parent types on the server.
    * 
    * @return See {@link PSDependencyManager#getParentTypes()}.
    * 
    * @throws PSDeployException
    */
   @SuppressWarnings("unchecked")
   private Map getParentTypes() throws PSDeployException
   {
      return PSDependencyManager.getInstance().getParentTypes();
   }
   
   /**
    * Finds out whether supplied type has any parent type or not.
    *
    * @param type the type to check, may not be <code>null</code> or empty.
    *
    * @return <code>true</code> if has parent type, otherwise
    * <code>false</code>.
    *
    * @throws PSDeployException if an error happens getting parent types.
    */
   private boolean hasParentType(String type) throws PSDeployException
   {
      return getParentTypes().containsKey(type);
   }
   
   /**
    * Gets the parent type of supplied type.
    *
    * @param type the type, may not be <code>null</code> or empty.
    *
    * @return the parent type, may be <code>null</code> if it does not have any
    * parent type, never empty.
    *
    * @throws IllegalStateException if server is not connected.
    * @throws PSDeployException if an error happens getting parent types.
    */
   @SuppressWarnings("unchecked")
   private String getParentType(String type) throws PSDeployException
   {
      Map parentTypes = getParentTypes();
      return (String) parentTypes.get(type);
   }
   
   /**
    * Gets the cataloged result set of literal id types. If the literal id
    * types are not yet cataloged, catalogs and caches them.
    *
    * @return the literal id types, never <code>null</code>, it may have
    * empty results.
    */
   @SuppressWarnings("unchecked")
   private Set<PSDependencyDef> getLiteralIDTypes()
   {
      if (m_idTypes == null)
      {
         m_idTypes = new HashSet<PSDependencyDef>();
         PSDependencyManager mgr = PSDependencyManager.getInstance();
         Iterator idTypes = mgr.getObjectTypes();
         while (idTypes.hasNext())
         {
            PSDependencyDef def = (PSDependencyDef)idTypes.next();
            if (def.supportsIdMapping())
            {
               m_idTypes.add(def);
            }
         }
      }
      
      return m_idTypes;   
   }
   
   /**
    * Creates a shallow copy of the supplied id map, copying only those mappings
    * that have a target defined, or that are marked as new.
    *  
    * @param idMap The map to copy, assumed not <code>null</code>.
    * 
    * @return The copy, never <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   private PSIdMap getValidMappings(PSIdMap idMap)
   {
      PSIdMap copy = new PSIdMap(idMap.getSourceServer());
      Iterator mappings = idMap.getMappings();
      while (mappings.hasNext())
      {
         PSIdMapping mapping = (PSIdMapping)mappings.next();
         if (mapping.isMapped())
            copy.addMapping(mapping);
      }
      
      return copy;
   }
   
   /**
    * The id map on the target for a source repository, initialized in the 
    * constructor, never <code>null</code> after that.
    */
   private PSIdMap m_idMap;
   
   /**
    * The map of elements by type, with type as key (<code>String</code>) and
    * the <code>PSMappingElement</code> with results representing the
    * elements of that type.  Initialized to an empty map and entries get added
    * by calls to {@link #getElementsByType(String) }. Never <code>null</code>.
    */
   private Map<String, List<PSMappingElement>> m_typeToMapElems =
      new HashMap<String, List<PSMappingElement>>();
   
   /**
    * The security token to use if objectstore access is required.  Initialized
    * in the constructor, never <code>null</code> after that.
    */      
   private PSSecurityToken m_tok;
   
   /**
    * The list of {@link PSImportPackage} objects for which source id's will be
    * mapped.  Initialized in the constructor, never <code>null</code> after
    * that.
    */
   private List<PSImportPackage> m_packages;
   
   /**
    * The cataloged set of id types that are supported by the server,
    * <code>null</code> until first call to <code>getLiteralIDTypes()</code> or
    * if the server is not connected.
    */
   private Set<PSDependencyDef> m_idTypes = null;    
}
