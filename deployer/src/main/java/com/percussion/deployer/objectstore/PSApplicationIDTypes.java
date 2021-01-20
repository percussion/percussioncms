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


package com.percussion.deployer.objectstore;

import com.percussion.deployer.objectstore.idtypes.PSApplicationIdContext;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * This class is used to contain {@link PSApplicationIDTypeMapping}s for server
 * objects that are applications or contain components used by applications,
 * such as the system and shared defs.  It serves to identify literal numeric
 * values specified within such objects as referencing specific types of
 * dependencies.
 * <p>
 * This class is not thread safe, so it should not be accessed by multiple
 * threads at the same time.
 */
public class PSApplicationIDTypes implements IPSDeployComponent
{
   /**
    * Construct this object supplying the dependency for which it will map id
    * types.
    *
    * @param dep The dependency, may not be <code>null</code> and for which
    * {@link PSDependency#supportIdTypes()} returns <code>true</code>.
    *
    * @throws IllegalArgumentException if <code>dep</code> is invalid.
    */
   public PSApplicationIDTypes(PSDependency dep)
   {
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      if (!dep.supportsIdTypes())
         throw new IllegalArgumentException("dep does not support id types");

      m_dep = dep;
   }

   /**
    * Create this object from its XML representation
    *
    * @param source The source element.  See {@link #toXml(Document)} for
    * the expected format.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>source</code> is
    * <code>null</code>.
    * @throws PSUnknownNodeTypeException <code>source</code> is malformed.
    */
   public PSApplicationIDTypes(Element source) throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      fromXml(source);
   }

   /**
    * Get the dependency used to construct this map.
    *
    * @return The dependency, never <code>null</code>.
    */
   public PSDependency getDependency()
   {
      return m_dep;
   }
   
   /**
    * Set the dependency. This method should be used with caution, setting
    * a new dependency that differs greatly from the one originally used to
    * construct this map, may result in bad things happening. This method was
    * added so we could change the dependency id to the correct id for a target
    * server, then using the modified id in <code>PSApplicationIDTypes</code>
    * so that we could save the dependency file to the target server with the 
    * correctly mapped key.
    * 
    * @param dep the dependency to be set. Must be of the same dependency type
    * and object type as was originally set upon this objects construction.
    * Cannot be <code>null</code>.
    */
   public void setDependency(PSDependency dep)
   {
      if(dep == null)
         throw new IllegalArgumentException("The dependency cannot be null.");
      // Validate to be sure that we are only setting the same
      // type of dependency as is already set.      
      if(dep.getDependencyType() != m_dep.getDependencyType()
         || !dep.getObjectType().equals(m_dep.getObjectType()))
      {
         throw new IllegalArgumentException(
           "The dependency to be set must match the "
              + "same dependency and object type as "
              + "the current dependency of this object.");
      }
      m_dep = dep;
   }

   /**
    * Get a list of resource names.
    *
    * @param    incompleteOnly   If <code>true</code>, the returned list
    * includes only resoures that contain mappings with a type set to
    * <code>PSApplicationIDTypeMapping.TYPE_UNDEFINED</code>, otherwise
    * the returned list includes all resources.
    *
    * @return An <code>iterator</code> over zero or more resource names as
    * <code>String</code> objects. Will be empty if there are no mappings
    * defined for the application referenced by this object.
    */
   public Iterator getResourceList(boolean incompleteOnly)
   {
      List resList = new ArrayList();
      if ( incompleteOnly)
      {
         Iterator resNameIterator = m_resourceMap.keySet().iterator();
         while (resNameIterator.hasNext())
         {
            String resName = (String) resNameIterator.next();
            if (! isComplete(resName))
               resList.add(resName);
         }
      }
      else
      {
         // return copy to allow removal during iterator traversal
         resList.addAll(m_resourceMap.keySet());
      }

      return resList.iterator();
   }

   /**
    * Get a list of element names for a given resource name.
    *
    * @param    resourceName    The name of a resource. It may not be empty or
    * <code>null</code>. It also needs to exist in the object, must be
    * one of the items from the list of {@link #getResourceList(boolean)
    * getResourceList(false)}.
    * @param    incompleteOnly    <code>true</code> to get only a list of
    * elements which contains incomplete
    * <code>PSApplicationIDTypeMapping</code>; <code>false</code> get the
    * complete list of elements.
    *
    * @return An <code>iterator</code> over zero or more element names as
    * <code>String</code>.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   public Iterator getElementList(String resourceName, boolean incompleteOnly)
   {
      if ( resourceName == null || resourceName.trim().length() == 0 )
         throw new IllegalArgumentException(
            "Resource name may not be null or empty");

      List elementList = new ArrayList();
      Map resElements = (Map) m_resourceMap.get(resourceName);

      if ( resElements == null )
         throw new IllegalArgumentException(
            "Resource name, " + resourceName + ", does not exist");

      if ( incompleteOnly ) // need to check the incompleteness
      {
         Iterator allElement = resElements.keySet().iterator();

         while (allElement.hasNext())
         {
            String currElement = (String) allElement.next();
            if ( hasInCompleteIdTypes((List)resElements.get(currElement)) )
               elementList.add(currElement);
         }
      }
      else
      {
         // create copy to allow removal during iterator traversal
         elementList.addAll(resElements.keySet());
      }

      return elementList.iterator();
   }

   /**
    * Get a list of ID Type mapping for a given resource and element names.
    *
    * @param    resourceName    The name of resource, which may not be empty
    * or <code>null</code>, it also must exist in the object.
    * @param    elementName    The name of element, which may not be empty
    * or <code>null</code>, it also need to be exist in the object.
    * @param    incompleteOnly    If <code>true</code>, only incomplete
    * mappings are returned.
    *
    * @return An iterator over zero or more
    * <code>PSApplicationIdTypeMapping</code> objects.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   public Iterator getIdTypeMappings(String resourceName, String elementName,
      boolean incompleteOnly)
   {
      if ( resourceName == null || resourceName.trim().length() == 0 )
         throw new IllegalArgumentException(
            "Resource name may not be null or empty");

      if ( elementName == null || elementName.trim().length() == 0 )
         throw new IllegalArgumentException(
            "Element name may not be null or empty");

      List idTypeList = getIdTypeMappingsList(resourceName, elementName);

      List mappingList = new ArrayList();
      if ( incompleteOnly )
      {
         Iterator idTypeIterator = idTypeList.iterator();

         while (idTypeIterator.hasNext())
         {
            PSApplicationIDTypeMapping idType =
              (PSApplicationIDTypeMapping) idTypeIterator.next();
            if (! idType.hasDefinedType() )
               mappingList.add(idType);
         }
      }
      else
      {
         // return copy to allow removal during iterator traversal
         mappingList.addAll(idTypeList);
      }

      return mappingList.iterator();
   }

   /**
    * Gets the mapping the matches the supplied context in the specified
    * resource and element.
    *
    * @param resourceName The name of the resource, not <code>null</code> or
    * empty.  May not exist in this map.
    * @param elementName The name of the element, not <code>null</code> or
    * empty.  May not exist in this map.
    * @param ctx The ctx of the mapping to locate for the supplied
    * <code>resourceName</code> and <code>elementName</code>, may not be
    * <code>null</code>.
    *
    * @return The matching mapping, or <code>null</code> if not found.
    *
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSApplicationIDTypeMapping getMapping(String resourceName,
      String elementName, PSApplicationIdContext ctx)
   {
      if ( resourceName == null || resourceName.trim().length() == 0 )
         throw new IllegalArgumentException(
            "Resource name may not be null or empty");

      if ( elementName == null || elementName.trim().length() == 0 )
         throw new IllegalArgumentException(
            "Element name may not be null or empty");

      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");

      PSApplicationIDTypeMapping mapping = null;
      List idTypeList = getIdTypeMappingsList(resourceName, elementName, false);
      Iterator mappings = idTypeList.iterator();
      while (mappings.hasNext() && mapping == null)
      {
         PSApplicationIDTypeMapping test =
            (PSApplicationIDTypeMapping)mappings.next();
         if (ctx.equals(test.getContext()))
            mapping = test;
      }

      return mapping;
   }

   /**
    * Determines if all ID Types have been completely mapped in the object.
    *
    * @return <code>true</code> if all IDs have been mapped to a type.
    */
   public boolean isComplete()
   {
      Iterator resList = m_resourceMap.keySet().iterator();
      while (resList.hasNext())
      {
         if ( ! isComplete((String)resList.next()) )
            return false;
      }
      return true;
   }

   /**
    * Determines if all IDs have been mapped to types for a given resource.
    *
    * @param    resourceName   The resource name, it should not a empty or
    * <code>null</code>
    *
    * @return <code>true</code> if all IDs have been mapped to types for the
    * given <code>resourceName</code>.
    *
    * @throws IllegalArgumentException if <code>resoureName</code> is
    * <code>null</code> or empty.
    */
   public boolean isComplete(String resourceName)
   {
      if ( resourceName == null || resourceName.trim().length() == 0 )
         throw new IllegalArgumentException(
            "resourceName may not be null or empty");

      // Get the whole list, but do the check here, so that we don't walk
      // through the whole list, less expensive
      Iterator elemList = getElementList(resourceName, false);

      while (elemList.hasNext())
      {
         if ( !isComplete(resourceName, (String)elemList.next()) )
            return false;
      }
      return true;
   }

   /**
    * Determines if all IDs have been mapped to types for a given resource and
    * element names.
    *
    * @param    resourceName  The resource name, it should not be empty or
    * <code>null</code>
    * @param    elementName   The element name, it should not be empty or
    * <code>null</code>
    *
    * @return <code>true</code> if all IDs have been mapped to types for the
    * given resource and element names.
    *
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public boolean isComplete(String resourceName, String elementName)
   {
      // Get the whole list, but do the check here, so that we don't walk
      // through the whole list, less expensive
      // NOTE: getIdTypeMappings() will check the parameters
      Iterator idTypeList = getIdTypeMappings(resourceName, elementName, false);

      while (idTypeList.hasNext())
      {
         PSApplicationIDTypeMapping idType =
            (PSApplicationIDTypeMapping) idTypeList.next();
         if (! idType.hasDefinedType())
            return false;
      }
      return true;
   }

   /**
    * Adds a list of mappings for the specified resource and element.
    * 
    * @param resourceName The name of the resource, may not be <code>null</code> 
    * or empty.
    * @param elementName The name of the element, may not be <code>null</code> 
    * or empty.
    * @param mappings An iterator over zero or more 
    * <code>PSApplicationIDTypeMapping</code> objects, may not be 
    * <code>null</code>.
    * 
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public void addMappings(String resourceName, String elementName, 
      Iterator mappings)
   {
      if (resourceName == null || resourceName.trim().length() == 0)
         throw new IllegalArgumentException(
            "resourceName may not be null or empty");
      
      if (elementName == null || elementName.trim().length() == 0)
         throw new IllegalArgumentException(
            "elementName may not be null or empty");
      
      if (mappings == null)
         throw new IllegalArgumentException("mappings may not be null");
      
      while (mappings.hasNext())
      {
         addMapping(resourceName, elementName, 
            (PSApplicationIDTypeMapping)mappings.next());
      }
   }
   
   /**
    * Adding a <code>PSApplicationIDTypeMapping</code> object for the
    * given resource and element names.
    *
    * @param    resourceName  The name of the resource. It should not be
    * <code>null</code> or empty.
    * @param    elementName  The name of the element. It should not be
    * <code>null</code> or empty.
    * @param    mapping  The ID Type Mapping object need to be added. It
    * should not be <code>null</code>.
    *
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public void addMapping(String resourceName, String elementName,
      PSApplicationIDTypeMapping mapping)
   {
      if ( resourceName == null || resourceName.trim().length() == 0 )
         throw new IllegalArgumentException(
            "Resource name may not be null or empty");

      if ( elementName == null || elementName.trim().length() == 0 )
         throw new IllegalArgumentException(
            "Element name may not be null or empty");

      if ( mapping == null )
         throw new IllegalArgumentException(
            "mapping may not be null");

      Map resElements = (Map) m_resourceMap.get(resourceName);
      if ( resElements == null ) // add a new resource/element name
      {
         List idtypeList = new ArrayList();
         idtypeList.add(mapping);
         Map elemMap = new HashMap();
         elemMap.put(elementName, idtypeList);
         m_resourceMap.put(resourceName, elemMap);
      }
      else // append to existing list
      {
         List idTypeList = (List) resElements.get(elementName);
         if ( idTypeList == null ) // add a new element in current resource
         {
            idTypeList = new ArrayList();
            idTypeList.add(mapping);
            resElements.put(elementName, idTypeList);
         }
         else // add to existing ID Type mapping list
         {
            // need to set change listeners
            setContextListeners(mapping, idTypeList);
            idTypeList.add(mapping);
         }
      }
   }

   /**
    * Remove the specified mapping if found.
    *
    * @param resource The resource containing the id, may not be
    * <code>null</code> or empty. It should exist in the current object.
    * @param element The element id within a resource, may not be
    * be <code>null</code> or empty. It should exist in the
    * <code>resource</code>
    * @param ctx The context of the mapping, may not be
    * <code>null</code> or empty.
    *
    * @return The removed mapping, or <code>null</code> if not found.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   public PSApplicationIDTypeMapping removeMapping(String resource,
      String element, PSApplicationIdContext ctx)
   {
      // NOTE: getIdTypeMappings() will check the parameters
      List idTypeList = getIdTypeMappingsList(resource, element);
      Iterator idTypeIterator = idTypeList.iterator();

      PSApplicationIDTypeMapping result = null;
      while (idTypeIterator.hasNext() && result == null)
      {
         PSApplicationIDTypeMapping currIDType =
                     (PSApplicationIDTypeMapping) idTypeIterator.next();
         if ( ctx.equals(currIDType.getContext()) )
         {
            idTypeList.remove(currIDType);
            // clear listeners
            clearContextListeners(currIDType, idTypeList);
            result = currIDType;
         }

         // clean up map entries
         if (idTypeList.isEmpty())
         {
            Map elementMap = (Map)m_resourceMap.get(resource);
            if (elementMap != null)
               elementMap.remove(element);
            if (elementMap.isEmpty())
               m_resourceMap.remove(resource);
         }
      }
      return result;
   }

   /**
    * Determines if the specified mapping exists.
    *
    * @param resource The resource containing the id, may not be
    * <code>null</code> or empty. This resouce id should exist in the current
    * object.
    * @param element The element within the resource containing the id, may not
    * be <code>null</code> or empty. This element should exist in the above
    * resource of the current object.
    * @param ctx The context of the mapping, may not be
    * <code>null</code> or empty.
    *
    * @return <code>true</code> if the specified mapping exists,
    * <code>false</code> otherwise.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   public boolean containsMapping(String resource,
      String element, PSApplicationIdContext ctx)
   {
      List idTypeList = getIdTypeMappingsList(resource, element);
      Iterator idTypeIterator = idTypeList.iterator();

      while (idTypeIterator.hasNext())
      {
         PSApplicationIDTypeMapping currIDType =
                     (PSApplicationIDTypeMapping) idTypeIterator.next();
         if ( ctx.equals(currIDType.getContext()) )
            return true;
      }

      return false;
   }

   /**
    * Determines if the current object is empty or not.
    *
    * @return <code>true</code> if there are no mappings defined for the
    * application referenced by this object, <code>false</code> otherwise.
    */
   public boolean isEmpty()
   {
      return m_resourceMap.isEmpty();
   }


   /**
    * Set choice filters to aid in selection of possible types for unmapped
    * ids, and then resets any invalid type mappings to undefined based on the 
    * filters.
    * 
    * @param filters A map of ids to their possible types.  Key is the id as a 
    * <code>String</code> object, value is a <code>List</code> of dependency
    * types as <code>String</code> objects, never <code>null</code>.  Supplied
    * map may not be <code>null</code>.  Must have at least one entry.
    */   
   public void setChoiceFilters(Map filters)
   {
      if (filters != null && filters.isEmpty())
         throw new IllegalArgumentException("filters may not be empty");
         
      m_choiceFilters = filters;
      
      // fixup mappings based on filters if provided
      if (m_choiceFilters == null)
         return;
         
      Iterator resources = m_resourceMap.values().iterator();
      while (resources.hasNext())
      {
         Map elementMap = (Map)resources.next();
         Iterator elements = elementMap.values().iterator();
         while (elements.hasNext())
         {
            List mappingList = (List)elements.next();
            Iterator mappings = mappingList.iterator();
            while (mappings.hasNext())
            {
               PSApplicationIDTypeMapping mapping = 
                  (PSApplicationIDTypeMapping)mappings.next();
                  
               // only care if not undefined and set to an actual type
               if (mapping.isComplete() && mapping.isIdType())
               {
                  String id = mapping.getValue();
                  List types = (List)m_choiceFilters.get(id);
                  
                  // if we have a filter and the current type isn't in it, set
                  // to undefined
                  if (types != null && !types.contains(mapping.getType()))
                     mapping.setType(PSApplicationIDTypeMapping.TYPE_UNDEFINED);
               }
            }
         }
      }      
   }

   /**
    * Get any choice filter info that has been set. See 
    * {@link #setChoiceFilters(Map)} for more info.
    * 
    * @return The filter map, may be <code>null</code> if one has not been set.
    * No guarantee is made that all ids in the filter map are included in the
    * mappings held by this object, or that all mappings have corresponding
    * ids in the filter map. Never empty.
    */
   public Map getChoiceFilters()
   {
      return m_choiceFilters;
   }
   
   /**
    * Get a set of all ids for which there are mappings.
    * 
    * @return A set of ids as <code>String</code> objects, never 
    * <code>null</code>, may be emtpy.
    */
   public Set getIds()
   {
      Set ids = new HashSet();
      
      Iterator resources = m_resourceMap.values().iterator();
      while (resources.hasNext())
      {
         Map elementMap = (Map)resources.next();
         Iterator elements = elementMap.values().iterator();
         while (elements.hasNext())
         {
            List mappingList = (List)elements.next();
            Iterator mappings = mappingList.iterator();
            while (mappings.hasNext())
            {
               PSApplicationIDTypeMapping mapping = 
                  (PSApplicationIDTypeMapping)mappings.next();
               ids.add(mapping.getValue());
            }
         }
      }
      
      return ids;
   }
   
   /**
    * Get a list of all mappings.
    * 
    * @param incompleteOnly If <code>true</code>, only incomplete mappings are 
    * returned, otherwise all mappings are returned.
    * 
    * @return An iterator over zero or more {@link PSApplicationIDTypeMapping}
    * objects, never <code>null</code>.
    */
   public Iterator getAllMappings(boolean incompleteOnly)
   {
      List allMappings = new ArrayList();
      
      Iterator resources = m_resourceMap.values().iterator();
      while (resources.hasNext())
      {
         Map elementMap = (Map)resources.next();
         Iterator elements = elementMap.values().iterator();
         while (elements.hasNext())
         {
            List mappingList = (List)elements.next();
            Iterator mappings = mappingList.iterator();
            while (mappings.hasNext())
            {
               PSApplicationIDTypeMapping mapping = 
                  (PSApplicationIDTypeMapping)mappings.next();
               if (!(incompleteOnly && mapping.isComplete()))
                  allMappings.add(mapping);
            }
         }
      }
      
      return allMappings.iterator();
   }

   /**
    * Serializes this object's state to its XML representation.  The format is:
    * <pre><code>
    * &lt;!ELEMENT PSXApplicationIDTypes ((PSXDeployableElement |
    *    PSXDeployableObject), PSXApplicationIDTypesResource*), choiceFilters?>
    * &lt;!ELEMENT PSXApplicationIDTypesResource
    *    (PSXApplicationIDTypesElement+)>
    * &lt;!ATTLIST PSXApplicationIDTypesResource
    *    resourceName CDATA #REQUIRED>
    * &lt;!ELEMENT PSXApplicationIDTypesElement (PSXApplicationIDTypeMapping+)>
    * &lt;!ATTLIST PSXApplicationIDTypesElement
    *    elementName CDATA #REQUIRED>
    * &lt;!ELEMENT choiceFilters (filter+)>
    * &lt;!ELEMENT filter (type*)>
    * &lt;!ATTLIST filter
    *    id CDATA #REQUIRED>
    * &lt;!ELEMENT type (#PCDATA)>
    * </code></pre>
    *
    * See {@link IPSDeployComponent#toXml(Document)} for more info.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      Element root = doc.createElement(XML_NODE_NAME);
      root.appendChild(m_dep.toXml(doc));

      Iterator resEntryList = m_resourceMap.entrySet().iterator();
      while (resEntryList.hasNext())
      {
         Map.Entry resEntry = (Map.Entry) resEntryList.next();
         Element resXml = toXmlResource((String)resEntry.getKey(),
            (Map)resEntry.getValue(), doc);

         root.appendChild(resXml);
      }
      
      if (m_choiceFilters != null)
         root.appendChild(toXmlFilters(m_choiceFilters, doc));
         
      return root;
   }

   /**
    * Restores this object's state from its XML representation.  See
    * {@link #toXml(Document)} for format of XML.  See
    * {@link IPSDeployComponent#fromXml(Element)} for more info on method
    * signature.
    */
   public void fromXml(Element sourceNode) throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");

      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

      Element depEl = tree.getNextElement(FIRST_FLAGS);
      String expected = "PSXDeployableElement | PSXDeployableObject";
      if (depEl == null)
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, expected);

      String nodeName = depEl.getNodeName();
      if (nodeName.equals(PSDeployableElement.XML_NODE_NAME))
         m_dep = new PSDeployableElement(depEl);
      else if (nodeName.equals(PSDeployableObject.XML_NODE_NAME))
         m_dep = new PSDeployableObject(depEl);
      else
      {
         Object[] args = { expected, nodeName};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      tree.setCurrent(sourceNode); // roll back to the root
      m_resourceMap.clear(); // initialize internal data.
      Element resEl = tree.getNextElement(XML_NODE_MAPPING_RESOURCE,
         FIRST_FLAGS);
      while (resEl != null)
      {
         String resName = PSDeployComponentUtils.getRequiredAttribute(resEl,
            XML_ATTR_RESOURCE_NAME);

         Map elemMap = getMappingElementsFromXml(tree);
         m_resourceMap.put(resName, elemMap);

         tree.setCurrent(resEl);       // get back to previous position

         resEl = tree.getNextElement(XML_NODE_MAPPING_RESOURCE, NEXT_FLAGS);
      }
      
      // set back to root
      tree.setCurrent(sourceNode);
      m_choiceFilters = null;
      Element filtersEl = tree.getNextElement(XML_NODE_CHOICE_FILTERS, 
         FIRST_FLAGS);
      if (filtersEl != null)
         m_choiceFilters = getFiltersFromXml(tree);
   }

   // see IPSDeployComponent interface
   public int hashCode()
   {
      return m_dep.hashCode() + m_resourceMap.hashCode() + 
         m_choiceFilters.hashCode();
   }

   // see IPSDeployComponent interface
   public boolean equals(Object obj)
   {
      boolean isEqual = true;

      if (!(obj instanceof PSApplicationIDTypes))
      {
         isEqual = false;  // not the same type of object
      }
      else if (obj != this) // compare to itself
      {
         PSApplicationIDTypes obj2 = (PSApplicationIDTypes) obj;

         if (!m_resourceMap.equals(obj2.m_resourceMap))
            isEqual = false;
         else if (!m_dep.equals(obj2.m_dep))
            isEqual = false;
         else if (m_choiceFilters == null ^ obj2.m_choiceFilters == null)
            isEqual = false;
         else if (m_choiceFilters != null && !m_choiceFilters.equals(
            obj2.m_choiceFilters))
         {
            isEqual = false;
         }
      }
      return isEqual;
   }

   // see IPSDeployComponent interface
   // This is a REAL shallow copy
   public void copyFrom(IPSDeployComponent obj)
   {
      if ( obj == null )
         throw new IllegalArgumentException("obj parameter should not be null");

      if (!(obj instanceof PSApplicationIDTypes))
         throw new IllegalArgumentException(
            "obj wrong type, expecting PSApplicationIDTypeMapping");

      PSApplicationIDTypes objSrc = (PSApplicationIDTypes) obj;

      m_dep = objSrc.m_dep;
      m_resourceMap.clear();
      m_resourceMap.putAll(objSrc.m_resourceMap);
      m_choiceFilters.clear();
      m_choiceFilters.putAll(objSrc.m_choiceFilters);
   }

   /**
    * Create XML <code>PSXApplicationIDTypesResource</code> Element for a given
    * resource, <code>resName</code> and <code>resElement</code>.
    *
    * See {@link #toXml(Document)} formate of
    * <code>PSXApplicationIDTypesResource</code> XML element.
    *
    * @param resName  Resource name. Assumed not <code>null</code> or empty.
    * @param resElement A list of element in <code>Map</code>. The map's key
    * is element name (<code>String</code>), the map's value is a
    * <code>List</code> of <code>PSApplicationIDTypeMapping</code> objects.
    * Assumed not <code>null</code>.
    * @param doc The Document for creating XML Element. Assumed not
    * <code>null</code>.
    *
    * @return the newly created XML Element
    */
   private Element toXmlResource(String resName, Map resElement, Document doc)
   {
      Element root = doc.createElement(XML_NODE_MAPPING_RESOURCE);
      root.setAttribute(XML_ATTR_RESOURCE_NAME, resName);

      Iterator elemEntryList = resElement.entrySet().iterator();
      while (elemEntryList.hasNext())
      {
         Map.Entry elemEntry = (Map.Entry) elemEntryList.next();
         Element elemXml = toXmlMappingElement((String)elemEntry.getKey(),
            (List)elemEntry.getValue(), doc);

         root.appendChild(elemXml);
      }
      return root;
   }

   /**
    * Create XML Element, <code>PSXApplicationIDTypesElement</code>, for a
    * given Mapping Element, <code>elementName</code> and
    * <code>mappingList</code>.
    *
    * See {@link #toXml(Document)} formate of
    * <code>PSXApplicationIDTypesElement</code> XML element.
    *
    * See {@link IPSDeployComponent#toXml(Document)} for more info.
    *
    * @param elementName  Element name. Assumed not <code>null</code> or empty.
    * @param mappingList A list of ID Type mappings. Assumed not
    * <code>null</code>.
    * @param doc The Document for creating XML Element. Assumed not
    * <code>null</code>.
    *
    * @return the newly created XML Element,  never <code>null</code>
    */
   private Element toXmlMappingElement(String elementName,
      List mappingList, Document doc)
   {
      Element root = doc.createElement(XML_NODE_MAPPING_ELEMENT);
      root.setAttribute(XML_ATTR_ELEMENT_NAME, elementName);

      Iterator idtypeList = mappingList.iterator();
      while (idtypeList.hasNext())
      {
         PSApplicationIDTypeMapping idtype =
            (PSApplicationIDTypeMapping) idtypeList.next();

         root.appendChild(idtype.toXml(doc));
      }
      return root;
   }
   
   /**
    * Creates the xml representation of the choice filters in the supplied map.
    * See {@link #toXml(Document)} for details on the <code>choiceFilters</code>
    * element.
    * 
    * @param filterMap Map of choice filters, assumed not <code>null</code>.
    * See {@link #setChoiceFilters(Map)} for details.
    * @param doc The document to use, assumed not <code>null</code>.
    * 
    * @return The element, never <code>null</code>.
    */
   private Element toXmlFilters(Map filterMap, Document doc)
   {
      Element root = doc.createElement(XML_NODE_CHOICE_FILTERS);
      
      Iterator filters = filterMap.entrySet().iterator();
      while (filters.hasNext())
      {
         Map.Entry filterEntry = (Map.Entry)filters.next();
         Element filter = PSXmlDocumentBuilder.addEmptyElement(doc, root, 
            XML_NODE_FILTER);
         filter.setAttribute(XML_ATTR_FILTER_ID, 
            filterEntry.getKey().toString());
         Object typeVal = filterEntry.getValue();
         if (typeVal instanceof List)
         {
            Iterator types = ((List)typeVal).iterator();
            while (types.hasNext())
            {
               filter.appendChild(PSXmlDocumentBuilder.addElement(doc, filter, 
                  XML_NODE_TYPE, types.next().toString()));
            }
         }
      }
      
      return root;
   }

   /**
    * Get a list of element from current <code>tree</code> location
    *
    * @param tree The XML tree which contains the element list. Assume
    * not <code>null</code>
    *
    * @return A list of element in a <code>Map</code>. It will not be
    * <code>null</code>
    *
    * @throws PSUnknownNodeTypeException <code>source</code> is malformed.
    */
   private Map getMappingElementsFromXml(PSXmlTreeWalker tree)
      throws PSUnknownNodeTypeException
   {
      // walk the element in current resource
      Element elemEl = tree.getNextElement(XML_NODE_MAPPING_ELEMENT,
         FIRST_FLAGS);
      // need to have at least one
      if (elemEl == null)
      {
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, XML_NODE_MAPPING_ELEMENT);
      }
      Map elemMap = new HashMap();

      while (elemEl != null)
      {
         String elemName = PSDeployComponentUtils.getRequiredAttribute(elemEl,
            XML_ATTR_ELEMENT_NAME);
         List idTypeList = getMappingListFromXml(tree);
         elemMap.put(elemName, idTypeList);
         tree.setCurrent(elemEl);
         elemEl = tree.getNextElement(XML_NODE_MAPPING_ELEMENT, NEXT_FLAGS);
      }
      return elemMap;
   }

   /**
    * Get a list of ID Type mapping from current <code>tree</code> location
    *
    * @param tree The XML tree which contains the mapping list. Assume not
    * <code>null</code>.
    *
    * @return A list of mappings in a <code>List</code>. It should contain
    * at least one item, will never be <code>null</code> or empty.
    *
    * @throws PSUnknownNodeTypeException <code>source</code> is malformed.
    */
   private List getMappingListFromXml(PSXmlTreeWalker tree)
      throws PSUnknownNodeTypeException
   {
      // walk the mapping list in current element
      Element mappingEl = tree.getNextElement(
         PSApplicationIDTypeMapping.XML_NODE_NAME, FIRST_FLAGS);
      // need to have at least one
      if (mappingEl == null)
      {
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL,
            PSApplicationIDTypeMapping.XML_NODE_NAME);
      }
      List mappingList = new ArrayList();

      while (mappingEl != null)
      {
         // need to set change listeners
         PSApplicationIDTypeMapping mapping = 
            new PSApplicationIDTypeMapping(mappingEl); 
         setContextListeners(mapping, mappingList);
         mappingList.add(mapping);
         mappingEl = tree.getNextElement(
            PSApplicationIDTypeMapping.XML_NODE_NAME, NEXT_FLAGS);
      }
      return mappingList;
   }
   
   /**
    * Restore the choice filters map from its XML representation.  See 
    * {@link #toXml(Document)} for details on the <code>choiceFilters</code>
    * element.
    * 
    * @param tree A tree walker assumed to be not <code>null</code> and to have
    * the choiceFilters element set as the current node.
    *  
    * @return The map, never <code>null</code> or empty.
    * 
    * @throws PSUnknownNodeTypeException if there xml format is invalid.
    */
   private Map getFiltersFromXml(PSXmlTreeWalker tree) 
      throws PSUnknownNodeTypeException
   {
      Map filterMap = new HashMap();
      
      // must have at least one filter
      Element filterEl = tree.getNextElement(XML_NODE_FILTER, FIRST_FLAGS);
      if (filterEl == null)
      {
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, XML_NODE_FILTER);
      }
      
      while (filterEl != null)
      {
         List typeList = new ArrayList();
         
         String id = PSXMLDomUtil.checkAttribute(filterEl, XML_ATTR_FILTER_ID, 
            true);
            
         Element typeEl = tree.getNextElement(XML_NODE_TYPE, FIRST_FLAGS);
         while (typeEl != null)
         {  
            typeList.add(tree.getElementData());
            typeEl = tree.getNextElement(XML_NODE_TYPE, NEXT_FLAGS);
         }
         
         filterMap.put(id, typeList);
         tree.setCurrent(filterEl);
         filterEl = tree.getNextElement(XML_NODE_FILTER, NEXT_FLAGS);
      }
      
      return filterMap;
   }

   /**
    * Convenience method that calls {@link #getIdTypeMappingsList(String,
    * String, boolean) getIdTypeMappingsList(resourceName, elementName, true}
    */
   private List getIdTypeMappingsList(String resourceName, String elementName)
   {
      return getIdTypeMappingsList(resourceName, elementName, true);
   }

   /**
    * Get a list of ID Type mapping for a given resource and element names.
    *
    * @param    resourceName    The name of resource, which may not be empty
    * or <code>null</code>, it also need to be exist in the object.
    * @param    elementName    The name of element, which may not be empty
    * or <code>null</code>, it also need to be exist in the object.
    * @param mustExist if <code>true</code>, the specified
    * <code>resourceName</code> and <code>elementName</code> must exist in this
    * map.  If either is not found, an <code>IllegalArgumentException</code> is
    * thrown.  If <code>false</code>, they may not exist, and if either does
    * not, an empty list is returned.
    *
    * @return An iterator over zero or more PSApplicationIdTypeMapping objects,
    * it will never be <code>null</code>, may be empty.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   private List getIdTypeMappingsList(String resourceName, String elementName,
      boolean mustExist)
   {
      if ( resourceName == null || resourceName.trim().length() == 0 )
         throw new IllegalArgumentException(
            "Resource name may not be null or empty");

      if ( elementName == null || elementName.trim().length() == 0 )
         throw new IllegalArgumentException(
            "Element name may not be null or empty");

      Map elementMap = (Map) m_resourceMap.get(resourceName);
      if ( elementMap == null )
      {
         if (mustExist)
            throw new IllegalArgumentException(
               "Resource name, " + resourceName + ", does not exist");
         else
            return new ArrayList();
      }

      List idTypeList = (List) elementMap.get(elementName);
      if ( idTypeList == null )
      {
         if (mustExist)
            throw new IllegalArgumentException(
               "Element name, " + elementName + ", does not exist");
         else
            return new ArrayList();
      }

      return idTypeList;
   }

   /**
    * Determings whether the ID Type mapping is in-complete or not for a
    * given list.
    *
    * @param    idTypeList    A list of <code>PSApplicationIDTypeMapping</code>
    * objects. Assume not <code>null</code>
    *
    * @return    <code>true</code> if one of the object
    * (<code>PSApplicationIDTypeMapping</code>) does not have a type; otherwise
    * <code>false</code>.
    */
   private boolean hasInCompleteIdTypes(List idTypeList)
   {
      Iterator itIDTypes = idTypeList.iterator();

      while (itIDTypes.hasNext())
      {
         PSApplicationIDTypeMapping idType =
            (PSApplicationIDTypeMapping) itIDTypes.next();
         if ( !idType.hasDefinedType() )
            return true;
      }
      return false;
   }
   
   /**
    * Walks the supplied mappings and with the context of each mapping, calls 
    * {@link PSApplicationIdContext#setListeners(PSApplicationIdContext)} on
    * the context of the supplied <code>mapping</code>.
    * 
    * @param mapping The mapping whose context has <code>setListeners()</code>, 
    * called on it, assumed not <code>null</code>. 
    * @param idTypeList The list of mappings to use, assumed not 
    * <code>null</code>.
    */
   private void setContextListeners(
      PSApplicationIDTypeMapping mapping,
      List idTypeList)
   {
      Iterator types = idTypeList.iterator();
      while (types.hasNext())
      {
         PSApplicationIDTypeMapping otherMapping = 
            (PSApplicationIDTypeMapping)types.next();
         mapping.getContext().setListeners(otherMapping.getContext());
      }
   }

   /**
    * Walks the supplied mappings and with the context of each mapping, calls 
    * {@link PSApplicationIdContext#clearListeners(PSApplicationIdContext)} on
    * the context of the supplied <code>mapping</code>.
    * 
    * @param mapping The mapping whose context has 
    * <code>clearListeners()</code>, called on it, assumed not 
    * <code>null</code>. 
    * @param idTypeList The list of mappings to use, assumed not 
    * <code>null</code>.
    */
   private void clearContextListeners(
      PSApplicationIDTypeMapping mapping,
      List idTypeList)
   {
      Iterator types = idTypeList.iterator();
      while (types.hasNext())
      {
         PSApplicationIDTypeMapping otherMapping = 
            (PSApplicationIDTypeMapping)types.next();
         mapping.getContext().clearListeners(otherMapping.getContext());
      }
   }

   /**
    * Root node name of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXApplicationIDTypes";

   /**
    * The resource node (2nd level node) name
    */
   private static final String XML_NODE_MAPPING_RESOURCE =
      "PSXApplicationIDTypesResource";

   /**
    * The element node (3nd level node) name
    */
   private static final String XML_NODE_MAPPING_ELEMENT =
      "PSXApplicationIDTypesElement";

   /**
    * The attribute name for resource node (2nd level node)
    */
   private static final String XML_ATTR_RESOURCE_NAME = "resourceName";

   /**
    * The attribbute name for element node (3nd level node)
    */
   private static final String XML_ATTR_ELEMENT_NAME = "elementName";

   /**
    * Map a list of resources to its element objects. The key is the name of
    * a resource (<code>String</code>); the value is a element object
    * (<code>Map</code>), which contains a list of element objects for the
    * resouce. Each element object (<code>Map</code>) uses its name as the key
    * (<code>String</code>) maps to a list of <code>PSIdTypeMapping</code>
    * objects (in <code>List</code>).
    *
    * The internal structure matches the XML described in
    * {@link #fromXml(Element)}.
    *
    * This Map object maybe empty, but will never be <code>null</code>. Each
    * element (<code>Map</code>) object will never be empty / <code>null</code>.
    * Each list of <code>PSIdTypeMapping</code> object (for a given element)
    * will never be empty or <code>null</code>.
    */
   private Map m_resourceMap = new HashMap();

   /**
    * The dependency for which this map defines literal id types.  Initialized
    * during construction, never <code>null</code> or modified after that.
    */
   private PSDependency m_dep;
   
   /**
    * Map of possible type choices for literal ids found in this map.  Initially
    * <code>null</code>, set by calls to {@link #setChoiceFilters(Object)}.
    */
   private Map m_choiceFilters = null;

   /**
    * flags to walk to a child node of a XML tree
    */
   private static final int FIRST_FLAGS =
      PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
      PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

   /**
    * flags to walk to a sibling node of a XML tree
    */
   private static final int NEXT_FLAGS =
      PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
      PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
   
   // private xml constants
   private static final String XML_NODE_CHOICE_FILTERS = "choiceFilters";
   private static final String XML_NODE_FILTER = "filter";
   private static final String XML_ATTR_FILTER_ID = "id";
   private static final String XML_NODE_TYPE= "type";
}
