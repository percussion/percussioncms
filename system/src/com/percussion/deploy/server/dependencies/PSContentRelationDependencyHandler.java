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
package com.percussion.deploy.server.dependencies;

import com.percussion.cms.IPSConstants;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.PSInlineLinkField;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.deploy.client.IPSDeployConstants;
import com.percussion.deploy.error.IPSDeploymentErrors;
import com.percussion.deploy.error.PSDeployException;
import com.percussion.deploy.objectstore.PSApplicationIDTypeMapping;
import com.percussion.deploy.objectstore.PSApplicationIDTypes;
import com.percussion.deploy.objectstore.PSDependency;
import com.percussion.deploy.objectstore.PSDependencyFile;
import com.percussion.deploy.objectstore.PSIdMap;
import com.percussion.deploy.objectstore.PSIdMapping;
import com.percussion.deploy.objectstore.PSTransactionSummary;
import com.percussion.deploy.server.IPSIdTypeHandler;
import com.percussion.deploy.server.PSAppTransformer;
import com.percussion.deploy.server.PSArchiveHandler;
import com.percussion.deploy.server.PSDbmsHelper;
import com.percussion.deploy.server.PSDependencyDef;
import com.percussion.deploy.server.PSDependencyMap;
import com.percussion.deploy.server.PSImportCtx;
import com.percussion.design.objectstore.IPSBackEndMapping;
import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSContentEditorPipe;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSProperty;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.security.PSSecurityToken;
import com.percussion.server.PSRequest;
import com.percussion.tablefactory.PSJdbcColumnData;
import com.percussion.tablefactory.PSJdbcRowData;
import com.percussion.tablefactory.PSJdbcSelectFilter;
import com.percussion.tablefactory.PSJdbcTableData;
import com.percussion.tablefactory.PSJdbcTableSchema;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSIteratorUtils;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * Class to handle packaging and deploying a content relation definition.
 */
public class PSContentRelationDependencyHandler
   extends PSIdTypeDependencyHandler implements IPSIdTypeHandler
{

   /**
    * Construct a dependency handler.
    *
    * @param def The def for the type supported by this handler.  May not be
    * <code>null</code> and must be of the type supported by this class.  See
    * {@link #getType()} for more info.
    * @param dependencyMap The full dependency map.  May not be
    * <code>null</code>.
    *
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSContentRelationDependencyHandler(PSDependencyDef def,
      PSDependencyMap dependencyMap)
   {
      super(def, dependencyMap);
   }

   // see base class
   public Iterator getChildDependencies(PSSecurityToken tok, PSDependency dep)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      if (! dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");

      Set childDeps = new HashSet();

      PSDependencyHandler ctHandler = getDependencyHandler(
         PSContentDefDependencyHandler.DEPENDENCY_TYPE);

      String id = dep.getDependencyId();
      Iterator relationships = getRelationships(tok, id);
      while (relationships.hasNext())
      {
         PSRelationship relationship = (PSRelationship)relationships.next();
         PSLocator loc = relationship.getDependent();

         PSDependency child = ctHandler.getDependency(tok, String.valueOf(
            loc.getId()));
         if (child != null)
            childDeps.add(child);

         // get property deps
         Iterator props = relationship.getUserProperties().entrySet().iterator();
         while (props.hasNext())
         {
            Map.Entry prop = (Map.Entry)props.next();
            String type = (String) ms_propertyTypes.get(prop.getKey());
            String value = (String) prop.getValue();
            if (type != null && value != null && value.trim().length() > 0)
            {
               PSDependencyHandler handler = getDependencyHandler(type);
               if (type.equals(PSFolderDefDependencyHandler.DEPENDENCY_TYPE))
               {
                  PSFolderDefDependencyHandler folderHandler = 
                     (PSFolderDefDependencyHandler)handler;
                  value = folderHandler.getFolderPathFromId(tok, value);
               }
               
               if (value != null)
               {
                  PSDependency childDep = handler.getDependency(tok, value);
                  if (childDep != null)
                     childDeps.add(childDep);
               }
            }
         }
      }

      // get relationship dep
      PSDependencyHandler relHandler = getDependencyHandler(
         PSRelationshipDependencyHandler.DEPENDENCY_TYPE);
      PSPairDependencyId pairId = new PSPairDependencyId(id);
      PSDependency relDep = relHandler.getDependency(tok, pairId.getChildId());
      if (relDep != null)
         childDeps.add(relDep);

      // this will add all dependencies specified by id types
      childDeps.addAll(getIdTypeDependencies(tok, dep));

      return childDeps.iterator();
    }

   // see base class
   public Iterator getDependencies(PSSecurityToken tok) throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      // not supported
      return PSIteratorUtils.emptyIterator();
   }

   // see base class
   public PSDependency getDependency(PSSecurityToken tok, String id)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");

      PSDependency dep = null;

      // get all relationships of the child type and see if any is of the
      // AA category.  Return a dependency as soon as one is found.
      Iterator relationships = getRelationships(tok, id);
      if (relationships.hasNext())
      {
         PSPairDependencyId pairId = new PSPairDependencyId(id);
         dep = createDependency(m_def, id, pairId.getChildId());
      }

      return dep;
   }

   // see base class
   public boolean doesDependencyExist(PSSecurityToken tok, String id)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");

      return getDependency(tok, id) != null;
   }
   
   // see base class
   public boolean delegatesIdMapping()
   {
      return true;
   }
   
   // see base class
   public String getIdMappingType()
   {
     return PSContentDefDependencyHandler.DEPENDENCY_TYPE;
   }
   
   // see base class
   protected String getSourceForIdMapping(String id) throws PSDeployException 
   {
      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");
         
      PSPairDependencyId pairId = new PSPairDependencyId(id);
      
      return pairId.getParentId();
   }
   
   // see base class
   public String getTargetId(PSIdMapping mapping, String id) 
      throws PSDeployException
   {
      if (mapping == null)
         throw new IllegalArgumentException("mapping may not be null");
      
      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");
               
      PSPairDependencyId pairId = new PSPairDependencyId(id);
      String newParentId = super.getTargetId(mapping, pairId.getParentId());
      
      return PSPairDependencyId.getPairDependencyId(newParentId, 
         pairId.getChildId());      
   }

   /**
    * Provides the list of child dependency types this class can discover.
    * The child types supported by this handler are:
    * <ol>
    * <li>ContentDef</li>
    * <li>Folder</li>
    * <li>Relationship</li>
    * <li>Site</li>
    * <li>Slot</li>
    * <li>VariantDef</li>
    * </ol>
    *
    * @return An iterator over zero or more types as <code>String</code>
    * objects, never <code>null</code>, does not contain <code>null</code> or
    * empty entries.
    */
   public Iterator getChildTypes()
   {
      return ms_childTypes.iterator();
   }

   // see base class
   public String getType()
   {
      return DEPENDENCY_TYPE;
   }

   // see base class
   public Iterator getDependencyFiles(PSSecurityToken tok, PSDependency dep)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      if (!dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");

      List files = new ArrayList();
      PSFolderDefDependencyHandler folderHandler = 
         (PSFolderDefDependencyHandler)getDependencyHandler(
            PSFolderDefDependencyHandler.DEPENDENCY_TYPE);
      
      Iterator relationships = getRelationships(tok, dep.getDependencyId());
      while (relationships.hasNext())
      {
         PSRelationship rel = (PSRelationship)relationships.next();
         
         // switch folder id to path when saved in archive
         String folderId = rel.getProperty(IPSHtmlParameters.SYS_FOLDERID);
         if (folderId != null && folderId.trim().length() > 0)
         {
            String folderPath = folderHandler.getFolderPathFromId(tok, 
               folderId);
            rel.setProperty(IPSHtmlParameters.SYS_FOLDERID, folderPath);
         }
         
         Document doc = PSXmlDocumentBuilder.createXmlDocument();
         PSXmlDocumentBuilder.replaceRoot(doc, rel.toXml(doc));
         File file = createXmlFile(doc);
         files.add(new PSDependencyFile(PSDependencyFile.TYPE_COMPONENT_XML,
            file));
      }

      return files.iterator();
   }

   // see base class
   public void installDependencyFiles(PSSecurityToken tok,
      PSArchiveHandler archive, PSDependency dep, PSImportCtx ctx)
         throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if (archive == null)
         throw new IllegalArgumentException("archive may not be null");
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      if (!dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");
      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");

      try
      {
         PSRequest req = new PSRequest(tok);
         PSRelationshipProcessor proc = PSRelationshipProcessor.getInstance();
         PSPairDependencyId pairId = new PSPairDependencyId(
            dep.getDependencyId());
         String contentId = pairId.getParentId();
         String type = pairId.getChildId();

         // get target locators
         String tgtId = contentId;
         PSIdMapping idMapping = getIdMapping(ctx, contentId,
            PSContentDefDependencyHandler.DEPENDENCY_TYPE);
         if (idMapping != null)
            tgtId = idMapping.getTargetId();

         PSLocator tgtOwnerLocator = new PSLocator(tgtId);
         tgtOwnerLocator.setPersisted(true);

         // delete all current relationships
         proc.delete(type, tgtOwnerLocator, (List)null);

         // add log entry
         addTransactionLogEntry(dep, ctx, type,
            PSTransactionSummary.TYPE_CMS_OBJECT,
            PSTransactionSummary.ACTION_DELETED);

         PSRelationshipSet set = new PSRelationshipSet();
         Map relationshipMap = new HashMap();
         String category = null;
         
         Iterator files = archive.getFiles(dep);
         while (files.hasNext())
         {
            PSDependencyFile file = (PSDependencyFile)files.next();
            Document doc = createXmlDocument(archive.getFileData(file));
            PSRelationship rel = new PSRelationship(doc.getDocumentElement(),
               null, null);
            
            // set target owner id and revision
            tgtOwnerLocator = new PSLocator(tgtId, String.valueOf(
               rel.getOwner().getRevision()));
            tgtOwnerLocator.setPersisted(true);
            rel.setOwner(tgtOwnerLocator);

            // set target dependent id, no revision should be set for dependents
            String tgtDepId = String.valueOf(rel.getDependent().getId());
            PSIdMapping depIdMapping = getIdMapping(ctx, tgtDepId,
               PSContentDefDependencyHandler.DEPENDENCY_TYPE);
            if (depIdMapping != null)
               tgtDepId = depIdMapping.getTargetId();
            PSLocator tgtDependentLocator = new PSLocator(tgtDepId);
            tgtDependentLocator.setPersisted(true);
            rel.setDependent(tgtDependentLocator);

            // transform the child ids
            String newSlotId = transformIds(tok, ctx, rel);

            // save old id and object in the map for inline link processing
            if (newSlotId != null && PSInlineLinkField.isInlineSlot(newSlotId))
               relationshipMap.put(new Integer(rel.getId()), rel);            
            
            // make sure we insert - new id will be set on the object when saved
            rel.setId(-1);
            
            // add it to the set
            set.add(rel);
            
            // all relationships should have the same category, so save
            // it once
            if (category == null)
               category = rel.getConfig().getCategory();
         }

         // save the set
         proc.save(set);

         // add log entry
         addTransactionLogEntry(dep, ctx, type,
            PSTransactionSummary.TYPE_CMS_OBJECT,
            PSTransactionSummary.ACTION_CREATED);
            
         // now update inline links in all parent content item fields that may
         // have inline links if we are tranforming ids
         if (ctx.getCurrentIdMap() != null && !relationshipMap.isEmpty() &&
            PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY.equals(category))
         {
            PSRelationshipSet mods = updateInlineLinks(dep, ctx, tgtId, 
               relationshipMap);
            // resave any relationships with modified properties
            if (!mods.isEmpty())
            {
               proc.save(mods); 
            
               // add log entries
               addTransactionLogEntry(dep, ctx, type,
                  PSTransactionSummary.TYPE_CMS_OBJECT,
                  PSTransactionSummary.ACTION_MODIFIED);
            }
         }
      }
      catch (PSUnknownNodeTypeException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      }
      catch (PSCmsException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      } 
      catch (PSInternalRequestCallException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      }
   }

   // see IPSIdTypeHandler interface
   public PSApplicationIDTypes getIdTypes(PSSecurityToken tok, PSDependency dep) 
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      if (!dep.getObjectType().equals(getType()))
         throw new IllegalArgumentException("dep wrong type");
      
      PSApplicationIDTypes idTypes = new PSApplicationIDTypes(dep);      
      String id = dep.getDependencyId();
      
      // get property deps, build a set so we don't end up with dupes
      Set propSet = new HashSet();
      Iterator relationships = getRelationships(tok, id);
      while (relationships.hasNext())
      {
         PSRelationship relationship = (PSRelationship)relationships.next();
         propSet.addAll(getUnknownProperties(relationship));
      }
      
      List mappings = new ArrayList();
      String reqName = dep.getDisplayName();
      PSAppTransformer.checkProperties(mappings, propSet.iterator(), null);
      idTypes.addMappings(reqName, 
         IPSDeployConstants.ID_TYPE_ELEMENT_USER_PROPERTIES, 
         mappings.iterator());      
      
      return idTypes;
   }

   // see IPSIdTypeHandler interface
   public void transformIds(Object object, PSApplicationIDTypes idTypes, 
      PSIdMap idMap) throws PSDeployException
   {
      if (object == null)
         throw new IllegalArgumentException("object may not be null");
      
      if (idTypes == null)
         throw new IllegalArgumentException("idTypes may not be null");
      
      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");

      if (!(object instanceof PSRelationship))
      {
         throw new IllegalArgumentException("invalid object type");
      }

      PSRelationship rel = (PSRelationship) object;
      List propList = getUnknownProperties(rel);
      // walk id types and perform any transforms
      Iterator resources = idTypes.getResourceList(false);
      while (resources.hasNext())
      {
         String resource = (String)resources.next();
         Iterator elements = idTypes.getElementList(resource, false);
         while (elements.hasNext())
         {
            String element = (String)elements.next();
            Iterator mappings = idTypes.getIdTypeMappings(
                  resource, element, false);
            while (mappings.hasNext())
            {
               
               PSApplicationIDTypeMapping mapping = 
                  (PSApplicationIDTypeMapping)mappings.next();
               
               if (mapping.getType().equals(
                  PSApplicationIDTypeMapping.TYPE_NONE))
               {
                  continue;
               }                 
               else if (element.equals(
                  IPSDeployConstants.ID_TYPE_ELEMENT_USER_PROPERTIES))
               {
                  
                  PSAppTransformer.transformProperties(propList.iterator(), 
                     mapping, idMap);
               }
            }
         }
      }      
      
      // now set back on the relationship
      Iterator props = propList.iterator();
      while (props.hasNext())
      {
         PSProperty prop = (PSProperty) props.next();
         rel.setProperty(prop.getName(), (String)prop.getValue());
      }
   }
   
   // see base class
   public boolean shouldDeferInstallation()
   {
      // need to defer installation until after child items have been installed
      return true;
   }   
   /**
    * Gets the list of unknown properties from the supplied relationship.  
    * Known property names defined by {@link #ms_propertyTypes} are not included
    * in the results.
    * 
    * @param relationship The relationship whose properties are to be retrieved,
    * assumed not <code>null</code>.
    * 
    * @return The list of <code>PSProperty</code> objects, never 
    * <code>null</code>, may be empty.
    */
   private List getUnknownProperties(PSRelationship relationship)
   {
      List propList = new ArrayList();
      
      Iterator props = relationship.getUserProperties().entrySet().iterator();
      while (props.hasNext())
      {
         Map.Entry entry = (Map.Entry)props.next();
         // skip known property names
         if (ms_propertyTypes.containsKey(entry.getKey()))
            continue;
         
         PSProperty prop = new PSProperty((String) entry.getKey());
         prop.setValue(entry.getValue());
         propList.add(prop);
      }
      
      return propList;
   }
   
   /**
    * Transform child ids in the supplied relationship
    * 
    * @param tok The security token to use, assumed not <code>null</code>.
    * @param ctx The current install context to use, assumed not 
    * <code>null</code>.
    * @param rel The relationship to transform, assumed not <code>null</code>.
    * 
    * @return The new slot id if one is specified by the relationship
    * properties, <code>null</code> if not or if transforms are not required, 
    * never empty.
    * 
    * @throws PSDeployException if there are any errors.
    */
   private String transformIds(PSSecurityToken tok, PSImportCtx ctx, 
      PSRelationship rel) throws PSDeployException
   {
      String newSlotId = null;
      
      PSIdMap idMap = ctx.getCurrentIdMap();
      if (idMap == null)
         return newSlotId;

      // transform known ids
      Iterator props = rel.getUserProperties().entrySet().iterator();
      while (props.hasNext())
      {
         Map.Entry prop = (Map.Entry)props.next();
         String name = (String)prop.getKey();
         String type = (String) ms_propertyTypes.get(name);
         String value = (String) prop.getValue();
         if (type != null && value != null && value.trim().length() > 0)
         {
            String newId = null;
            // folders don't use id mapping
            if (type.equals(PSFolderDefDependencyHandler.DEPENDENCY_TYPE))
            {
               // convert path to id
               PSFolderDefDependencyHandler folderHandler = 
                  (PSFolderDefDependencyHandler)getDependencyHandler(type);
               newId = folderHandler.getFolderIdFromPath(tok, value);
               
               if (newId == null)
               {
                  Object[] args = {folderHandler.DEPENDENCY_TYPE, value};
                  throw new PSDeployException(
                     IPSDeploymentErrors.SERVER_OBJECT_NOT_FOUND, args);
               }
            }
            else
            {
               PSIdMapping mapping =
                  getIdMapping(ctx, value, type);
               if (mapping != null)
               {
                  newId = mapping.getTargetId();
                  // save new slot id
                  if (type.equals(PSSlotDependencyHandler.DEPENDENCY_TYPE))
                     newSlotId = newId;
               }     
            }
            
            if (newId != null)
               rel.setProperty(name, newId);
         }
      }      
      
      // transform id types
      if (ctx.getIdTypes() != null)
         transformIds(rel, ctx.getIdTypes(), idMap);      
      
      return newSlotId;
   }

   /**
    * Updates the inline link text with new ids in any item field for which 
    * inline slot relationships are being installed, and saves those item rows 
    * back to the server.  Also updates the inline inlinelinkfield property of 
    * any relationship that represents an inline link in a child item, and 
    * returns any such modified relationships so they may be resaved.
    * 
    * @param dep The dependency for which relationships are being installed,
    * assumed not <code>null</code>.
    * @param ctx The current installation context, assumed not 
    * <code>null</code>.
    * @param contentId The content id of the parent item for which relationships
    * are being installed, assumed not <code>null</code> or empty.
    * @param relationshipMap A map of inline relationships being installed for
    * which link text requires modification.  The key is the source server
    * relationship id as an <code>Integer</code>, value is the matching 
    * {@link PSRelationship} object that has already been saved to the local
    * server and thus has valid target server ids.  Assumed not 
    * <code>null</code>, or empty.
    * 
    * @return The set of modified relationships that require saving, never
    * <code>null</code>, may be empty.
    * 
    * @throws PSDeployException if there are any errors.
    */
   private PSRelationshipSet updateInlineLinks(PSDependency dep, 
      PSImportCtx ctx, String contentId, Map relationshipMap) 
         throws PSDeployException
   {
      try
      {
         // use internal user to bypass community filtering
         PSRequest adminReq = PSRequest.getContextForRequest();
         PSItemDefinition itemDef = PSItemDefManager.getInstance().getItemDef(
            new PSLocator(contentId), adminReq.getSecurityToken());
         PSContentEditorPipe pipe;
         pipe = (PSContentEditorPipe) itemDef.getContentEditor().getPipe();
         PSFieldSet fieldSet = pipe.getMapper().getFieldSet();
         PSRelationshipSet modifiedSet = new PSRelationshipSet();
         processFieldSet(dep, ctx, contentId, fieldSet, relationshipMap, 
            modifiedSet);
         
         return modifiedSet;  
      }
      catch (PSInvalidContentTypeException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      }  
   }

   /**
    * Recursively processes the supplied fieldset.  Walks the fields and for any
    * that support inline links, gets the corresponding row from the repository
    * and fixes up the inline text with correct ids.
    * 
    * @param dep The dependency for which relationships are being installed,
    * assumed not <code>null</code>.
    * @param ctx The current installation context, assumed not 
    * <code>null</code>.
    * @param contentid The content id of the parent item for which relationships
    * are being installed, assumed not <code>null</code> or empty.
    * @param fieldSet The fieldset being processed, assumed not 
    * <code>null</code>.
    * @param relationshipMap A map of inline relationships being installed for
    * which link text requires modification.  The key is the source server
    * relationship id as an <code>Integer</code>, value is the matching 
    * {@link PSRelationship} object that has already been saved to the local
    * server and thus has valid target server ids.  Assumed not 
    * <code>null</code>, or empty.
    * @param modifiedSet The set of modified relationships that require saving, 
    * populated by this method, assumed not <code>null</code>, may be empty.
    * 
    * @throws PSDeployException if there are any errors.
    */
   private void processFieldSet(PSDependency dep, PSImportCtx ctx, 
      String contentid, PSFieldSet fieldSet, Map relationshipMap, 
      PSRelationshipSet modifiedSet) 
         throws PSDeployException
   {
      // get list of inline fields, recurse into any field sets.  build map of
      // table to list of it's inline fields
      try
      {
         Map inlineFieldMap = new HashMap();
         Map fieldMap = new HashMap(); // map of fieldnames to be col names
         Iterator fields = fieldSet.getAll();
         while (fields.hasNext())
         {
            Object o = fields.next();
            if (o instanceof PSFieldSet)
            {
               processFieldSet(dep, ctx, contentid, (PSFieldSet)o, 
                  relationshipMap, modifiedSet);
            }
            else if (o instanceof PSField)
            {
               PSField field = (PSField)o;
               if (field.mayHaveInlineLinks())
               {
                  IPSBackEndMapping beMapping = field.getLocator();
                  if (beMapping instanceof PSBackEndColumn)
                  {
                     PSBackEndColumn becol = (PSBackEndColumn)beMapping;
                     String tableName = becol.getTable().getTable();
                     List fieldList = (List)inlineFieldMap.get(tableName);
                     if (fieldList == null)
                     {
                        fieldList = new ArrayList();
                        inlineFieldMap.put(tableName, fieldList);
                     }
                     fieldList.add(becol.getColumn().toLowerCase());
                     fieldMap.put(becol.getColumn(), field.getSubmitName());
                  }               
               }  
            }
         }
      
         // query each table and process each field in each row returned
         PSDbmsHelper dbmsHelper = PSDbmsHelper.getInstance();
         PSJdbcSelectFilter filter = new PSJdbcSelectFilter(
            IPSConstants.ITEM_PKEY_CONTENTID, PSJdbcSelectFilter.EQUALS, 
            contentid, Types.INTEGER);
            
         Iterator entries = inlineFieldMap.entrySet().iterator();
         while (entries.hasNext())
         {
            Entry entry = (Entry)entries.next();
         
            // query all matching rows from the table
            String tableName = (String)entry.getKey();
            List colList = (List)entry.getValue();
            PSJdbcTableSchema schema = dbmsHelper.catalogTable(tableName, 
               false);
            PSJdbcTableData data = dbmsHelper.catalogTableData(schema, null, 
               filter);            
            if (data == null)
               continue;
            
            List modRowList = new ArrayList();
            Iterator rows = data.getRows();
            while (rows.hasNext())
            {
               boolean modifiedRow = false;
               List modColList = new ArrayList();
                  
               // see if this is a child row
               boolean isChild = false;
               String sysId = null;                  
               PSJdbcRowData row = (PSJdbcRowData)rows.next();
               PSJdbcColumnData sysCol = row.getColumn(
                  IPSConstants.CHILD_ITEM_PKEY, true);                  
               if (sysCol != null)
               {
                  sysId = sysCol.getValue();
                  if (sysId != null && sysId.trim().length() > 0)
                     isChild = true;
               }   
                  
               // walk the fields and get the value for each if there
               Iterator cols = row.getColumns();
               while (cols.hasNext())
               {
                  PSJdbcColumnData colData = (PSJdbcColumnData)cols.next();
                  modColList.add(colData);
                  String colName = colData.getName();
                  if (!colList.contains(colName.toLowerCase()))
                     continue;

                  String text = colData.getValue();
                  if (null == text || text.trim().length() == 0)
                     continue;

                  // Assume the text is a valid XML document, already tidied
                  Document fieldDoc = PSXmlDocumentBuilder.createXmlDocument(
                     new InputSource((Reader) new StringReader(text)), 
                     false);
                     
                  // update all links and get back relationships matching
                  // modified links
                  PSRelationshipSet modifiedLinks = new PSRelationshipSet();
                  PSInlineLinkField.modifyField(
                     fieldDoc.getDocumentElement(), relationshipMap, 
                     modifiedLinks);
                     
                  // update the column if the links were modified and 
                  // remember if we changed the row
                  if (!modifiedLinks.isEmpty())
                  {
                     modifiedRow = true;
                     colData.setValue(PSXmlDocumentBuilder.toString(
                        fieldDoc));
                  }                     
                     
                  // update the inlinelinkfield prop of any changed 
                  // relationships if this is a child row
                  if (isChild)
                  {
                     Iterator mods = modifiedLinks.iterator();
                     while (mods.hasNext())
                     {
                        PSRelationship mod = (PSRelationship)mods.next();
                        String inlineRelText = mod.getProperty(
                           PSRelationshipConfig.RS_INLINERELATIONSHIP);
                        String fieldName = (String)fieldMap.get(colName);
                        if (inlineRelText != null && 
                           inlineRelText.trim().length() > 0 && 
                           fieldName.equalsIgnoreCase(
                              PSInlineLinkField.getFieldName(inlineRelText)))
                        {
                           mod.setProperty(
                              PSRelationshipConfig.RS_INLINERELATIONSHIP, 
                              PSInlineLinkField.makeInlineRelationshipId(
                                 fieldName, sysId));
                           modifiedSet.add(mod);
                        }
                     }
                  }                     
               }
                  
               // if we changed the row, add it to the modified row list
               if (modifiedRow)
                  modRowList.add(new PSJdbcRowData(modColList.iterator(), 
                     PSJdbcRowData.ACTION_UPDATE));
            }            
            
            if (modRowList.isEmpty())
               continue;
            
            // save any modified rows back
            PSJdbcTableData newData = new PSJdbcTableData(tableName,
               modRowList.iterator());
            schema.setAllowSchemaChanges(false); // don't change the table
            dbmsHelper.processTable(schema, newData);
            addTransactionLogEntry(dep, ctx, tableName, 
               PSTransactionSummary.TYPE_DATA, 
               PSTransactionSummary.ACTION_MODIFIED);
         }         
      }
      catch (Exception e)
      {
         if (e instanceof PSDeployException)
            throw (PSDeployException)e.fillInStackTrace();
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      } 
   }

   /**
    * Get all relationships of the specified type where the parent is specified
    * by the supplied id.
    *
    * @param tok The security token, assumed not <code>null</code>.
    * @param id The dependency id of the relationship def, assumed not
    * <code>null</code> or empty.
    *
    * @return An iterator over zero or more <code>PSRelationship</code> objects,
    * never <code>null</code>.
    *
    * @throws PSDeployException if there are any errors.
    */
   private Iterator getRelationships(PSSecurityToken tok, String id)
      throws PSDeployException
   {
      try
      {
         PSPairDependencyId pairId = new PSPairDependencyId(id);
         String type = pairId.getChildId();
         String contentId = pairId.getParentId();
         PSRequest req = new PSRequest(tok);

         // get all relationships of the child type
         PSRelationshipProcessor proc = PSRelationshipProcessor.getInstance();
         PSLocator locator = new PSLocator(contentId);
         locator.setPersisted(true);
         PSRelationshipSet relSet = proc.getDependents(type, locator);

         return relSet.iterator();
      }
      catch (PSCmsException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      }
   }

   /**
    * Constant for this handler's supported type
    */
   final static String DEPENDENCY_TYPE = 
      IPSDeployConstants.DEP_OBJECT_TYPE_CONTENT_RELATION;

   /**
    * List of child types supported by this handler, it will never be
    * <code>null</code>, entries are added by a static intializer.
    */
   private static List ms_childTypes = new ArrayList();
   
   /**
    * Map of property names to their associated child handler types, never
    * <code>null</code>, entries are added by a static intializer.  If a 
    * property name is known, but does not have a corresponding dependency type,
    * the value will be <code>null</code>.
    */
   private static Map ms_propertyTypes = new HashMap();
   
   static
   { 
      ms_childTypes.add(PSContentDefDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSFolderDefDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSRelationshipDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSSiteDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSSlotDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSTemplateDefDependencyHandler.DEPENDENCY_TYPE);
      ms_propertyTypes.put(IPSHtmlParameters.SYS_FOLDERID, 
         PSFolderDefDependencyHandler.DEPENDENCY_TYPE);
      ms_propertyTypes.put(IPSHtmlParameters.SYS_SITEID, 
         PSSiteDependencyHandler.DEPENDENCY_TYPE);
      ms_propertyTypes.put(IPSHtmlParameters.SYS_SLOTID, 
         PSSlotDependencyHandler.DEPENDENCY_TYPE);
      ms_propertyTypes.put(IPSHtmlParameters.SYS_VARIANTID, 
            PSTemplateDefDependencyHandler.DEPENDENCY_TYPE);
      ms_propertyTypes.put(IPSHtmlParameters.SYS_SORTRANK, 
         null);
   }
}
