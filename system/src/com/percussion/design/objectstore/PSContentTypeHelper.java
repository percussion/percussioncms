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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.design.objectstore;

import com.percussion.cms.PSApplicationBuilder;
import com.percussion.cms.objectstore.PSContentType;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.conn.PSServerException;
import com.percussion.design.objectstore.server.IPSLockerId;
import com.percussion.design.objectstore.server.PSServerXmlObjectStore;
import com.percussion.design.objectstore.server.PSXmlObjectStoreLockerId;
import com.percussion.security.PSAuthenticationRequiredException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.PSRequest;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.catalog.data.PSObjectSummary;
import com.percussion.services.content.data.PSContentTypeSummary;
import com.percussion.services.content.data.PSContentTypeSummaryChild;
import com.percussion.services.content.data.PSFieldDescription;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.IPSNodeDefinition;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.contentmgr.data.PSContentTemplateDesc;
import com.percussion.services.contentmgr.data.PSContentTypeWorkflow;
import com.percussion.services.contentmgr.data.PSNodeDefinition;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.util.PSCollection;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.webservices.IPSWebserviceErrors;
import com.percussion.webservices.PSWebserviceErrors;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Helper class to manage loading and saving content types from web services
 */
public class PSContentTypeHelper
{
   private static final Logger log = LogManager.getLogger(PSContentTypeHelper.class);
   /**
    * Private ctor to ensure static use
    */
   private PSContentTypeHelper()
   {
      
   }
   
   /**
    * Construct an item def from the supplied node def.  The item def will 
    * contain a content editor with the default settings.  Nothing is persisted
    * as a result of this call.
    * 
    * @param nodeDef The node def to use, may not be <code>null</code>.
    * 
    * @return The item def, never <code>null</code>.
    * 
    * @throws IOException If there is an error loading the default ce template
    * file.
    * @throws SAXException If the default ce template file is malformed.
    * @throws PSUnknownNodeTypeException If the default ce template file does
    * not conform to the expected format.
    */
   public static PSItemDefinition createContentType(IPSNodeDefinition nodeDef) 
      throws IOException, SAXException, PSUnknownNodeTypeException
   {
      InputStream in = null;
      try
      {
         // get template file
         in = PSContentTypeHelper.class.getResourceAsStream("sys_Default.xml");
         Document doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
         Element src = doc.getDocumentElement();
         PSContentEditor ce = new PSContentEditor(src, 
            null, null);
         String name = nodeDef.getInternalName();
         ce.setContentType(nodeDef.getGUID().longValue());
         ce.setName(name);
         ce.getRequestor().setRequestPage(name);
         String appName = PSContentType.createAppName(name);
         String url = PSContentType.createRequestUrl(name);
         
         // try to set a valid default workflow id
         IPSWorkflowService service = 
            PSWorkflowServiceLocator.getWorkflowService();
         List<PSObjectSummary> wfs = 
               service.findWorkflowSummariesByName(null);
         if (!wfs.isEmpty())
            ce.setWorkflowId((int)wfs.iterator().next().getGUID().longValue());
         
         PSContentType typeDef = new PSContentType(
               (int) nodeDef.getGUID().longValue(), name, name, 
               ce.getDescription(), url, false, 1);
         PSItemDefinition srcDef = new PSItemDefinition(appName, typeDef, 
            ce);
         
         return srcDef;
      }
      finally
      {
         if (in != null)
         {
            try
            {
               in.close();
            }
            catch (IOException e)
            {
            }
         }
      }      
   }
   
   /**
    * Convenience method that calls {@link #saveContentType(PSItemDefinition, 
    * descSet, int, IPSSaveNodeDefListener, boolean)}
    * 
    */
   public static void saveContentType(PSItemDefinition itemDef,
      Set<PSContentTemplateDesc> descSet, int version, boolean enable) 
   throws PSLockedException, PSServerException,
   PSAuthenticationRequiredException, PSNotFoundException, 
   PSNotLockedException, PSAuthorizationException, PSSystemValidationException,
   PSNonUniqueException, IOException, RepositoryException
   {
      saveContentType(itemDef, descSet, version, null, enable);
   }
   
   /**
    * Convenience method that calls {@link #saveContentType(PSItemDefinition, 
    * int, IPSSaveNodeDefListener, List, boolean)}
    * 
    */
   public static void saveContentType(PSItemDefinition itemDef,
         Set<PSContentTemplateDesc> descSet, int version, 
         IPSSaveNodeDefListener listener, boolean enable) 
   throws PSLockedException, PSServerException, 
   PSAuthenticationRequiredException, PSNotFoundException, 
   PSNotLockedException, PSAuthorizationException, PSSystemValidationException,
   PSNonUniqueException, IOException, RepositoryException
   {
      List<IPSGuid> templateGuids = null;
      if (descSet != null)
      {
         templateGuids = new ArrayList<>();
         for (PSContentTemplateDesc temp : descSet)
         {
            templateGuids.add(temp.getTemplateId());
         }
      }
      saveContentType(itemDef, version, listener, templateGuids, enable);
   }
 
   /**
    * Saves the supplied item def by inserting or updating the respective
    * {@link IPSNodeDefinition} and {@link PSApplication} objects.
    * 
    * @param itemDef Specifies all information about the content type being
    * saved, may not be <code>null</code>.
    * @param version The version of the {@link IPSNodeDefinition} to update,
    * supply -1 for a new object.
    * @param listener Used to be informed of successful save of the node def,
    * may be <code>null</code>.
    * @param templateGuids the list of template guids that needs to be
    * associated with the content type. It is up to the user to provide the
    * accurate list of descriptors ( such as MSM ), may be <code>null</code>.
    * If it is null the template associations are not touched.
    * @param enable Set to <code>true</code> if the associated content editor
    * application should be enabled prior to being saved.  Set to
    * <code>false</code> if the application should be disabled.
    * 
    * @throws PSLockedException If the application lock cannot be obtained.
    * @throws PSServerException If there are any errors obtaining the lock.
    * @throws PSAuthorizationException If the user is not authorized to access
    * the application
    * @throws PSNotLockedException If the application cannot be locked.
    * @throws PSNotFoundException If an existing application cannot be found.
    * @throws PSAuthenticationRequiredException If the current session is not
    * authenticated.
    * @throws IOException If There is an error saving the application
    * @throws PSNonUniqueException If an application already exists with the
    * specified name.
    * @throws PSSystemValidationException If the supplied content editor does not pass
    * validation.
    * @throws RepositoryException If the content type definition cannot be
    * saved.
    */
   @SuppressWarnings(value = { "unchecked" })
   public static void saveContentType(PSItemDefinition itemDef, int version,
         IPSSaveNodeDefListener listener, List<IPSGuid> templateGuids,
         boolean enable) 
      throws PSLockedException, PSServerException, 
      PSAuthenticationRequiredException, PSNotFoundException, 
      PSNotLockedException, PSAuthorizationException, PSSystemValidationException,
      PSNonUniqueException, IOException, RepositoryException
   {
      if (itemDef == null)
         throw new IllegalArgumentException("itemDef may not be null");
      
      // validate the type ids match in the ce and item def.
      PSContentEditor ce = itemDef.getContentEditor();
      if (ce.getContentType() != itemDef.getTypeId())
         throw new IllegalArgumentException("The content type id in the item " +
               "def and its content editor must match");
      
      // try to load existing def
      IPSContentMgr mgr = PSContentMgrLocator.getContentMgr();
      IPSGuid guid = new PSGuid(PSTypeEnum.NODEDEF, itemDef.getTypeId());
      
      PSApplication ceApp;
      PSRequest req = (PSRequest) PSRequestInfo.getRequestInfo(
         PSRequestInfo.KEY_PSREQUEST);
      String userName = req.getUserSession().getRealAuthenticatedUserEntry();
      PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
      IPSLockerId lockId = new PSXmlObjectStoreLockerId(userName, false, 
         req.getUserSessionId());
      
      PSNodeDefinition nodeDef;
      String appName = null;
      boolean locked = false;
      boolean isNew = (version == -1);
      
      try
      {

         if (isNew)
         {
            // it's a create
            String name = itemDef.getName();
            validateUniqueName(name);

            appName = PSContentType.createAppName(itemDef.getName());
            
            nodeDef = new PSNodeDefinition();
            nodeDef.setGUID(guid);
         }
         else
         {
            // load the def
            nodeDef = findNodeDef(guid);
            
            // lock and load existing app
            appName = PSContentType.getAppName(nodeDef.getNewRequest());

         }

          os.getApplicationLock(lockId, appName, 3);
          locked = true;
          try {
              ceApp = os.getApplicationObject(lockId, appName,
                      req.getSecurityToken(), false);
          } catch (PSNotFoundException e )
          {
              ceApp = PSApplicationBuilder.createApplication(appName);
              ceApp.setApplicationType(PSApplicationType.CONTENT_EDITOR);
          }

         // update the def
         nodeDef.setInternalName(itemDef.getName());
         nodeDef.setDescription(itemDef.getDescription());
         nodeDef.setLabel(itemDef.getLabel());
         nodeDef.setHideFromMenu(itemDef.isHidden());
         nodeDef.setUpdateRequest(null);
         nodeDef.setObjectType(itemDef.getObjectType());
         if (!isNew)
            nodeDef.setVersion(version);
         
         // update the urls
         String ceUrl = PSContentType.createRequestUrl(itemDef.getName());
         nodeDef.setNewRequest(ceUrl);
         nodeDef.setQueryRequest(ceUrl);
         
         // update any template associations
         if ( templateGuids != null )
         {  
            if ( nodeDef.getCvDescriptors() != null )
               nodeDef.getCvDescriptors().clear();

            for (IPSGuid tguid : templateGuids)
            {
               nodeDef.addVariantGuid(tguid);
            }
         }
         
         //Update the workflow associations
         Set<IPSGuid> wfids = new HashSet<>();
         PSWorkflowInfo wfInfo = ce.getWorkflowInfo();
         if (wfInfo != null)
         {
            IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
            Iterator iter = wfInfo.getValues();
            while (iter.hasNext())
            {
               wfids.add(gmgr.makeGuid(((Integer) iter.next()).toString(),
                     PSTypeEnum.WORKFLOW));
            }
         }
         mergeWorkflowIds(nodeDef,wfids);
         
         // save the def
         List<IPSNodeDefinition> nodeDefs = new ArrayList<>(1);
         nodeDefs.add(nodeDef);

         // Do not save node definitions here.  Saving Application updates/saves contenttypes table already
         mgr.saveNodeDefinitions(nodeDefs);

         
         // set the ce dataset
         // update app names based on latest type name
         ce.setName(itemDef.getName());
         ce.getRequestor().setRequestPage(itemDef.getName());
         PSCollection datasets = ceApp.getDataSets();
         
         // remove any content editors
         Iterator dsIter = datasets.iterator();
         while (dsIter.hasNext())
         {
            PSDataSet ds = (PSDataSet) dsIter.next();
            if (ds instanceof PSContentEditor)
               datasets.remove(ds);
         }
         
         datasets.add(itemDef.getContentEditor());
         ceApp.setDataSets(datasets);
         ceApp.setEnabled(enable);
         ceApp.setName(PSContentType.createAppName(itemDef.getName()));
         ceApp.setRequestRoot(ceApp.getName());
                
         // save the app
         boolean didSaveApp = false;
         try
         {
            os.saveApplication(ceApp, lockId, req.getSecurityToken(), true);
            didSaveApp = true;
            if (listener != null)
                listener.nodeDefSaved();
         }
         finally
         {
            // if app save fails, delete the node def if new
            if (isNew && !didSaveApp)
            {
               try
               {
                  // app may have saved but not started
                  os.deleteApplication(ceApp.getName(), lockId, 
                     req.getSecurityToken());
               }
               catch (Exception e)
               {
                  // log it, but throw original problem
                  log.error(
                     "Failed delete new ce app after it failed to start. Error: {}", e.getMessage());
               }
               
               try
               {
                  List<IPSGuid> typeids = new ArrayList<>();
                  typeids.add(nodeDef.getGUID());
                  nodeDefs = mgr.loadNodeDefinitions(typeids);
                  mgr.deleteNodeDefinitions(nodeDefs);
               }
               catch(NoSuchNodeTypeException e)
               {
                   // This is ok, node already deleted.
               }
               catch(Exception e)
               {
                  // log it, but throw original problem
                  log.error(
                     "Failed delete new nodedef after app save failed. Error: {}", e.getMessage());
               }
            }
         }

      }
      finally
      {
         // release the lock 
         if (locked)
            os.releaseApplicationLock(lockId, appName);
      }
   }

   /**
    * Adds a workflow guid to the supplied node def, if does not exist.
    * 
    * @param nodeDef must not be <code>null</code>
    * @param guid must not be <code>null</code>
    */
   public void addWorkflowGuid(PSNodeDefinition nodeDef, IPSGuid guid)
   {
      if(nodeDef == null)
         throw new IllegalArgumentException("nodeDef must not be null");
         
      if (guid == null)
         throw new IllegalArgumentException("guid may not be null");

      IPSGuid ctGuid = nodeDef.getGUID();
      Set<PSContentTypeWorkflow> ctWfRels = nodeDef.getCtWfRels();
      if (ctWfRels == null)
      {
         ctWfRels = new HashSet<>();
      }
      IPSContentMgr cmgr = PSContentMgrLocator.getContentMgr();
      
      for (PSContentTypeWorkflow rel : ctWfRels)
      {
         if (guid.equals(rel.getWorkflowId()))
         {
            return;
         }
      }

      PSContentTypeWorkflow ctWf = null;
      // dont always create a new one, if an association exists, use it
      List<PSContentTypeWorkflow> ctWfs = null;
      
      try
      {
         ctWfs = cmgr.findContentTypeWorkflowAssociations(ctGuid);
         for (PSContentTypeWorkflow rel : ctWfs)
         {
            if (guid.equals(rel.getWorkflowId()))
            {
               ctWf = rel;
               break;
            }
         }
      }
      catch (RepositoryException e)
      {
      }
      if (ctWf != null)
         ctWfRels.add(ctWf);
      else
      {
         // association not found, so add a new one
         IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
         PSContentTypeWorkflow rel = new PSContentTypeWorkflow();
         rel.setId(gmgr.createGuid(PSTypeEnum.INTERNAL).longValue());
         rel.setContentTypeId(ctGuid);
         rel.setWorkflowId(guid);
         ctWfRels.add(rel);
      }
   }

   /**
    * Removes the workflow guid from the supplied node def.
    * 
    * @param nodeDef must not be <code>null</code>
    * @param guid must not be <code>null</code>
    */
   public void removeWorkflowGuid(PSNodeDefinition nodeDef, IPSGuid guid)
   {
      if(nodeDef == null)
         throw new IllegalArgumentException("nodeDef must not be null");
         
      if (guid == null)
      {
         throw new IllegalArgumentException("guid may not be null");
      }
      PSContentTypeWorkflow found = null;
      for (PSContentTypeWorkflow rel : nodeDef.getCtWfRels())
      {
         if (guid.equals(rel.getWorkflowId()))
         {
            found = rel;
            break;
         }
      }
      if (found != null)
      {
         nodeDef.getCtWfRels().remove(found);
      }
   }


   /**
    * Add the given workflow to the ctwf rels.Adds only if it is not found.
    * 
    * @param nodeDef The node def for which the workflows need to be merged.
    * Must not be <code>null</code>.
    * @param wfGuid set of workflow guids never <code>null</code>, may be
    * empty
    */
   private static void addWorkflowGuidsToCollection(PSNodeDefinition nodeDef,
         Set<IPSGuid> wfGuid)
   {
      PSContentTypeWorkflow ctwf = null;
      IPSGuid ctGuid = nodeDef.getGUID();
      IPSContentMgr cmgr = PSContentMgrLocator.getContentMgr();
      Map<IPSGuid, PSContentTypeWorkflow> curRels = 
         new HashMap<>();
      try
      {
         List<PSContentTypeWorkflow> ctWfs = cmgr
               .findContentTypeWorkflowAssociations(ctGuid);
         for (PSContentTypeWorkflow obj : ctWfs)
         {
            curRels.put(obj.getWorkflowId(), obj);
         }
      }
      catch (RepositoryException e)
      {
      }
      Set<PSContentTypeWorkflow> ctWfRels = nodeDef.getCtWfRels();
      if(ctWfRels == null)
      {
         ctWfRels = new HashSet<>();
      }
      for (IPSGuid guid : wfGuid)
      {
         ctwf = curRels.get(guid);
         if (ctwf != null)
         {
            ctWfRels.add(ctwf);
         }
         else
         {
            // association not found, so add a new one
            IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
            PSContentTypeWorkflow rel = new PSContentTypeWorkflow();
            rel.setId(gmgr.createGuid(PSTypeEnum.INTERNAL).longValue());
            rel.setContentTypeId(ctGuid);
            rel.setWorkflowId(guid);
            ctWfRels.add(rel);
            
         }
      }
   }

   /**
    * Given a Collection of workflow ids as IPSGuids, sync them with the
    * existing list of workflow associations for the given NodeDef
    * 
    * @param nodeDef The node def for which the workflows need to be merged.
    * Must not be <code>null</code>.
    * @param newWs set of string workflow ids never <code>null</code>, may be
    * empty
    */
   public static void mergeWorkflowIds(PSNodeDefinition nodeDef,
         Set<IPSGuid> newWfs)
   {
      if(nodeDef == null)
         throw new IllegalArgumentException("nodeDef must not be null");
         
      if (newWfs == null)
         throw new IllegalArgumentException("newWfs must not be null");
      Set<PSContentTypeWorkflow> ctWfRels = nodeDef.getCtWfRels();
      if(ctWfRels == null)
      {
         ctWfRels = new HashSet<>();
      }
      // if the current workflow set is empty
      if (ctWfRels.isEmpty())
      {
         addWorkflowGuidsToCollection(nodeDef,newWfs);
         return;
      }
      // get all existing workflow guids associated with this node
      Set<IPSGuid> curWfs = new HashSet<>();
      for (PSContentTypeWorkflow ctwf : ctWfRels)
         curWfs.add(ctwf.getWorkflowId());

      /**
       * 1. commons = intersection of curWfs, newWfs 2. removes =
       * curWfs - newWfs 3. delete removes from curWfs 4. delete
       * commons from newWfs
       */
      Collection commons = CollectionUtils.intersection(curWfs, newWfs);
      Collection removes = CollectionUtils.subtract(curWfs, newWfs);
      curWfs.removeAll(removes);
      newWfs.removeAll(commons);
      curWfs.addAll(newWfs);
      ctWfRels.clear();
      addWorkflowGuidsToCollection(nodeDef,curWfs);
   }


   /**
    * Validates that no content type with the supplied name exists.
    * 
    * @param name The name to check, may not be <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if a content type with the specified
    * name already exists.
    */
   public static void validateUniqueName(String name)
   {
      if (doesContentTypeExist(name))
      {
         int code = IPSWebserviceErrors.OBJECT_ALREADY_EXISTS;
         throw new IllegalArgumentException( 
            PSWebserviceErrors.createErrorMessage(code, PSTypeEnum.NODEDEF, 
               name));               
      }
   }
   
   /**
    * Load all content type summaries for the specified name.
    * 
    * @param name the content type name for which to load the content type
    * summaries, may be <code>null</code> or empty, wildcards are accepted.
    * All content type summaries will be loaded if <code>null</code> or empty.
    * 
    * @return a list with all loaded content type summaries, never
    * <code>null</code>, may be empty, oalpha ordered by name.
    */
   public static List<PSContentTypeSummary> loadContentTypeSummaries(
      String name)
   {
      List<PSContentTypeSummary> sums = new ArrayList<>();
      for (IPSNodeDefinition nodeDef : loadNodeDefs(name))
      {
         PSItemDefManager itemDefMgr = PSItemDefManager.getInstance();
         PSItemDefinition itemDef;
         try
         {
            itemDef = itemDefMgr.getItemDef(
               nodeDef.getGUID().longValue(), PSItemDefManager.COMMUNITY_ANY);
         }
         catch (PSInvalidContentTypeException e)
         {
            // ignore invalid content types
            continue;
         }
         PSContentTypeSummary sum = new PSContentTypeSummary();
         sum.setGuid(nodeDef.getGUID());
         sum.setName(nodeDef.getInternalName());
         sum.setDescription(itemDef.getDescription());
         for (PSField field : itemDef.getMappedParentFields())
         {
            sum.addField(new PSFieldDescription(field.getSubmitName(), 
               getFieldType(field).name(), field.isExportable()));
         }
         
         for (PSDisplayMapper mapper : itemDef.getChildMappers())
         {
            PSContentTypeSummaryChild child = new PSContentTypeSummaryChild(
               mapper.getFieldSetRef());
            Iterator<PSField> fields = itemDef.getChildFields(
               mapper.getId());
            while (fields.hasNext())
            {
               PSField field = fields.next();
               child.addField(new PSFieldDescription(field.getSubmitName(), 
                  getFieldType(field).name(), field.isExportable()));
            }
            sum.addChild(child);
         }

         sums.add(sum);
      }
      return sums;
   }

   /**
    * Deletes the node definition and associated application specified by the 
    * supplied guid. 
    * 
    * @param guid The guid of the node definition, may not be <code>null</code>.
    * @param version The version of the {@link IPSNodeDefinition} to delete.
    * 
    * @return <code>true</code> if a node definion was found to delete, 
    * <code>false</code> if not.
    * 
    * @throws RepositoryException If there is an error deleting the node 
    * definition. 
    * @throws PSLockedException If the application is locked by someone else.
    * @throws PSNotLockedException If the application cannot be locked.
    * @throws PSNotFoundException If the application file cannot be located.
    * @throws PSAuthorizationException If the user is not authorized to delete
    * the application.
    * @throws PSAuthenticationRequiredException If the current session is not
    * authenticated.
    * @throws PSServerException if there are any other errors.  
    */
   public static boolean deleteContentType(IPSGuid guid, Integer version)
      throws RepositoryException, PSAuthenticationRequiredException,
      PSAuthorizationException, PSNotFoundException, PSServerException,
      PSNotLockedException, PSLockedException
   {
      if (guid == null)
         throw new IllegalArgumentException("guid may not be null");
      
      PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
      boolean locked = false;
      IPSLockerId lockId = null;
      String appName = null;
      try
      {
         boolean deleted = false;
         
         // try to load existing def
         IPSContentMgr mgr = PSContentMgrLocator.getContentMgr();
         PSNodeDefinition nodeDef = findNodeDef(guid);
         if (nodeDef != null)
         {
            // set the version
            if (version != null)
               nodeDef.setVersion(version);
            
            // delete it
            List<IPSNodeDefinition> defs = new ArrayList<>();
            defs.add(nodeDef);
            mgr.deleteNodeDefinitions(defs);
            
            // now lock and delete the app
            appName = PSContentType.getAppName(nodeDef.getNewRequest());
            
            PSRequest req = (PSRequest) PSRequestInfo.getRequestInfo(
               PSRequestInfo.KEY_PSREQUEST);
            String userName = 
               req.getUserSession().getRealAuthenticatedUserEntry();

            lockId = new PSXmlObjectStoreLockerId(userName, false, 
               req.getUserSessionId());

            os.getApplicationLock(lockId, appName, 30);
            locked = true;
            os.deleteApplication(appName, lockId, 
               req.getSecurityToken());
            
            deleted = true;
         }
         
         return deleted;         
      }
      finally
      {
         // release the lock 
         if (locked)
            os.releaseApplicationLock(lockId, appName);
      }      
   }
   
   /**
    * Loads the matching load defininitions
    * 
    * @param name The name to match, may be <code>null</code> or empty to load
    * all, "*" supported as a match any wildcard.
    * 
    * @return The list, never <code>null</code>, may be empty.
    */
   public static List<IPSNodeDefinition> loadNodeDefs(String name)
   {
      IPSContentMgr mgr = PSContentMgrLocator.getContentMgr();
      if (StringUtils.isBlank(name))
         name = "*";
      
      name = StringUtils.replaceChars(name, '*', '%');
      try
      {
         return mgr.findNodeDefinitionsByName(name);
      }
      catch (RepositoryException e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * Loads the matching load defininitions
    * 
    * @param ids The ids of the node defs to load, may not be <code>null</code>.
    * 
    * @return The list, never <code>null</code>, may be empty.
    */
   public static List<IPSNodeDefinition> loadNodeDefs(List<IPSGuid> ids)
   {
      if (ids == null)
         throw new IllegalArgumentException("ids may not be null");
      
      IPSContentMgr mgr = PSContentMgrLocator.getContentMgr();

      try
      {
         return mgr.loadNodeDefinitions(ids);
      }
      catch (RepositoryException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   /**
    * Constructs an item definition from the node def and first content editor 
    * resource found in the application referenced by that node def.
    *   
    * @param id The id of the content type, may not be <code>null</code>.
    * 
    * @return The item def, never <code>null</code>.
    * 
    * @throws PSInvalidContentTypeException If the id is invalid. 
    * @throws PSServerException If there are any errors obtaining the lock.
    * @throws PSAuthorizationException If the user is not authorized to access 
    * the application
    * @throws PSNotFoundException If an existing application cannot be found.
    * @throws PSAuthenticationRequiredException If the current session is not
    * authenticated.
    */
   @SuppressWarnings(value={"unchecked"})
   public static PSItemDefinition loadItemDef(IPSGuid id) 
      throws PSInvalidContentTypeException, PSAuthenticationRequiredException, 
      PSServerException, PSNotFoundException, PSAuthorizationException
   {
      // load the node def
      PSNodeDefinition nodeDef = findNodeDef(id);
      if (nodeDef == null)
      {
         throw new PSInvalidContentTypeException(id.toString());         
      }
      
      
      // load the local def from the file system
      String appName = PSContentType.getAppName(nodeDef.getQueryRequest());
      PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
      PSRequest req = (PSRequest) PSRequestInfo.getRequestInfo(
         PSRequestInfo.KEY_PSREQUEST);
      
      PSApplication ceApp = os.getApplicationObject(appName, req.getSecurityToken());
      
      // replace the content editor w/the saved local def
      PSContentEditor ce = null;
      Iterator datasets = ceApp.getDataSets().iterator();
      while (datasets.hasNext())
      {
         Object dataset = datasets.next();
         if (dataset instanceof PSContentEditor)
         {
            ce = (PSContentEditor) dataset;
            break;
         }  
      }
      
      if (ce == null)
      {
         // this would be the result of some other bug
         throw new RuntimeException("No local def found in application: " + 
            appName);
      }
      
      PSContentType contentType = new PSContentType((int) id.longValue(), 
         nodeDef.getInternalName(), nodeDef.getLabel(), 
         nodeDef.getDescription(), nodeDef.getQueryRequest(), 
         nodeDef.getHideFromMenu(), nodeDef.getObjectType());
      PSItemDefinition itemDef = new PSItemDefinition(appName, contentType, ce);
      
      return itemDef;
   }
   
   /**
    * Determine if a content type with the supplied name already exists.
    * 
    * @param name The name to check, may not be <code>null</code> or empty.
    *
    * @return <code>true</code> if it exists, <code>false</code> if not.
    */
   public static boolean doesContentTypeExist(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");
      
      IPSContentMgr mgr = PSContentMgrLocator.getContentMgr();
      try
      {
         mgr.findNodeDefinitionByName(name);
      }
      catch (NoSuchNodeTypeException e)
      {
         return false;
      }
      catch (RepositoryException e)
      {
         throw new RuntimeException(e);
      }
      
      return true;
   }
   
   
   /**
    * Locate a content type using the supplied guid.  
    * 
    * @param guid The guid to check for, may not be <code>null</code>.
    * 
    * @return The node def, or <code>null</code> if not found.
    */
   public static PSNodeDefinition findNodeDef(IPSGuid guid)
   {
      if (guid == null)
         throw new IllegalArgumentException("guid may not be null");
      
      IPSContentMgr mgr = PSContentMgrLocator.getContentMgr();
      try
      {
         List<IPSGuid> ids = new ArrayList<>();
         ids.add(guid);
         return (PSNodeDefinition) mgr.loadNodeDefinitions(ids).get(0);
      }
      catch (NoSuchNodeTypeException e)
      {
         return null;
      }
      catch (RepositoryException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   /**
    * Get the field type from the supplied field.
    * 
    * @param field The field to check, assumed not <code>null</code>.
    * 
    * @return The type, never <code>null</code>.
    */
   @SuppressWarnings("deprecation")
   private static PSFieldDescription.PSFieldTypeEnum getFieldType(PSField field)
   {
      // default to text
      PSFieldDescription.PSFieldTypeEnum type = 
         PSFieldDescription.PSFieldTypeEnum.TEXT;
      String dt = field.getDataType();
      if (dt.equals(PSField.DT_INTEGER) || dt.equals(PSField.DT_NUMBER) || 
         dt.equals(PSField.DT_FLOAT))
      {
         type = PSFieldDescription.PSFieldTypeEnum.NUMBER;
      }
      else if (dt.equals(PSField.DT_BINARY) || 
         dt.equals(PSField.DT_IMAGE))
      {
         type = PSFieldDescription.PSFieldTypeEnum.BINARY;
      }
      else if (dt.equals(PSField.DT_DATE) || dt.equals(
         PSField.DT_DATETIME) || dt.equals(PSField.DT_TIME))
      {
         type = PSFieldDescription.PSFieldTypeEnum.DATE;
      }
      return type;
   }
   
   /**
    * Used to be informed of a successful save of a node def during a call to
    * {@link PSContentTypeHelper#saveContentType(PSItemDefinition, Set, int, 
    * IPSSaveNodeDefListener)}
    */
   public interface IPSSaveNodeDefListener
   {
      /**
       * Called to inform listener of successful save.
       */
      public void nodeDefSaved();
   }

   /**
    * Gets the name of a shared field, which may be just a field name or in the
    * format of "shared-group-name"."field-name".
    * 
    * @param fieldName the shared field name in question, never <code>null</code>
    * or empty.
    * 
    * @return an array of 1 or 2 elements. The 1st element is always the actual
    * name of the field; the 2nd element (if there is one) is the name of the
    * shared group. It is never <code>null</code> or empty.
    */
   public static String[] getSharedFieldName(String fieldName)
   {
      String names[] = fieldName.split("\\.");
      if (names.length > 2)
         throw new IllegalArgumentException(
               "Shared field name cannot have more than 1 dot \".\".");

      if (names.length == 2)
      {
         return new String[] {names[1], names[0]};
      }
      return names;
   }
   
   /**
    * Gets the shared group that contains a specified shared field. 
    * 
    * @param fieldName the field name in question, may be <code>null</code> or
    * empty. The field name may be a simple field name or in the format of  
    * "shared-group-name"."field-name".  
    * 
    * @return the shared group of the shared field. It may be <code>null</code> 
    * if cannot find a shared field with the name.
    */
   @SuppressWarnings("unchecked")
   public static PSSharedFieldGroup getSharedGroup(String fieldName)
   {
      if (StringUtils.isBlank(fieldName))
         return null;
      
      // get the possible shared group and field name if specified
      String names[] = getSharedFieldName(fieldName);
      String grpName = names.length == 2 ? names[1] : null;
      fieldName = names[0];
      
      // load the shared group/fields
      PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
      PSContentEditorSharedDef sharedDef;
      try
      {
         sharedDef = os.getContentEditorSharedDef();
      }
      catch (Exception e)
      {
         throw new RuntimeException(
               "Failed to catalog shared field definition.");
      }
      
      PSDisplayMapping mapping = null;
      Iterator groups = sharedDef.getFieldGroups();
      while (groups.hasNext())
      {
         PSSharedFieldGroup shGroup = (PSSharedFieldGroup)groups.next();
         if (grpName != null && (!shGroup.getName().equalsIgnoreCase(grpName)))
            continue;
         
         if (shGroup.getFieldSet().findFieldByName(fieldName) != null)
         {
            if (mapping != null)
               throw new RuntimeException(
                     "The field name, \""
                           + fieldName
                           + "\", exists in more than one shared group. Must specify shared group as part of the field name.");
            
            return shGroup;
         }
      }

      return null;
   }
   
   
}

