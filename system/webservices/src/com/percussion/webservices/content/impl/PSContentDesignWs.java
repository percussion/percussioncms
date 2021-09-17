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
package com.percussion.webservices.content.impl;

import com.percussion.cms.IPSConstants;
import com.percussion.cms.handlers.PSContentEditorHandler;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSContentEditorMapper;
import com.percussion.design.objectstore.PSContentEditorPipe;
import com.percussion.design.objectstore.PSContentEditorSharedDef;
import com.percussion.design.objectstore.PSContentEditorSystemDef;
import com.percussion.design.objectstore.PSContentTypeHelper;
import com.percussion.design.objectstore.PSContentTypeHelper.IPSSaveNodeDefListener;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSSystemValidationException;
import com.percussion.design.objectstore.PSUnknownDocTypeException;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.design.objectstore.PSWorkflowInfo;
import com.percussion.design.objectstore.server.PSServerXmlObjectStore;
import com.percussion.i18n.PSLocale;
import com.percussion.i18n.rxlt.PSLocaleRxResourceCopyHandler;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.services.assembly.data.PSTemplateSlot;
import com.percussion.services.catalog.IPSCatalogIdentifier;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.catalog.data.PSObjectSummary;
import com.percussion.services.content.IPSContentService;
import com.percussion.services.content.PSContentException;
import com.percussion.services.content.PSContentServiceLocator;
import com.percussion.services.content.data.PSAutoTranslation;
import com.percussion.services.content.data.PSAutoTranslationPK;
import com.percussion.services.content.data.PSKeyword;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.IPSContentTypeMgr;
import com.percussion.services.contentmgr.IPSNodeDefinition;
import com.percussion.services.contentmgr.PSContentMgrConfig;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.contentmgr.PSContentMgrOption;
import com.percussion.services.contentmgr.data.PSContentTemplateDesc;
import com.percussion.services.contentmgr.data.PSContentTypeWorkflow;
import com.percussion.services.contentmgr.data.PSNodeDefinition;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.locking.IPSObjectLockService;
import com.percussion.services.locking.PSLockException;
import com.percussion.services.locking.PSObjectLockServiceLocator;
import com.percussion.services.locking.data.PSObjectLock;
import com.percussion.utils.exceptions.PSORMException;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.utils.string.PSStringUtils;
import com.percussion.webservices.IPSWebserviceErrors;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.PSLockErrorException;
import com.percussion.webservices.PSWebserviceErrors;
import com.percussion.webservices.PSWebserviceUtils;
import com.percussion.webservices.content.IPSContentDesignWs;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.percussion.webservices.PSWebserviceUtils.getItemSummary;
import static com.percussion.webservices.PSWebserviceUtils.isItemCheckedOutToUser;

/**
 * The private content design webservice implementations.
 */
@Component("sys_contentDesignWs")
@Transactional
public class PSContentDesignWs extends PSContentBaseWs implements
   IPSContentDesignWs
{

   private static final String IDS_NOT_EMPTY_MSG = "ids cannot be null or empty";

   /**
    * Gets the GUID from the specified item. If the revision of the id is
    * undefined, <code>-1</code>, then the return revision of the GUID is the
    * Edit Revision if the item is checked out by the current user; otherwise
    * the revision of the GUID is the Current Revision.
    * <p>
    * Do nothing and return the specified ID if the revision of it is not
    * <code>-1</code>.
    * 
    * @param id the ID of the item.
    * 
    * @return the GUID of the item, never <code>null</code>.
    */
   public IPSGuid getItemGuid(IPSGuid id)
   {
      if (id == null)
         throw new IllegalArgumentException("id must not be null.");
      if (!(id instanceof PSLegacyGuid))
         throw new IllegalArgumentException("id must be an instance of PSLegacyGuid.");

      PSLegacyGuid guid = (PSLegacyGuid) id;
      if (guid.getRevision() >= 0)
         return id;
      
      try
      {
         PSLocator loc = PSWebserviceUtils.getItemLocator(guid);
         return new PSLegacyGuid(loc.getId(), loc.getRevision());
      }
      catch (PSErrorException e)
      {
         throw new RuntimeException("Failed to get locator for item id="
               + id.getUUID(), e);
      }
   }
   
   /*
    * //see base interface method for details
    */
   public String getItemEditUrl(IPSGuid itemId, String ctName, String viewName)
   {
      return getItemUrl(itemId, ctName, viewName, false);      
   }
   
   /*
    * //see base interface method for details
    */
   public String getItemViewUrl(IPSGuid itemId, String ctName, String viewName)      
   {
      return getItemUrl(itemId, ctName, viewName, true);
   }
   
   /**
    * Common method used to facilitate {@link #getItemEditUrl(IPSGuid, String, String)} and
    * {@link #getItemViewUrl(IPSGuid, String, String)}. See each of those methods doc for
    * further details.
    * @param itemId the ID of the item. It may be <code>null</code> if the 
    * returned URL will be used for creating an item.
    * @param ctName the name of the Content Type, never blank.
    * @param viewName the value of the "sys_view" for the specified 
    * Content Type, never blank.
    * @param readonly flag indicated which view type is requested.
    * 
    * @return the edit or view URL described above. It may be <code>null</code> if
    * the content type does not exist or it is not enabled.
    */
   private String getItemUrl(IPSGuid itemId, String ctName, String viewName, boolean readonly)
   {
      if (StringUtils.isBlank(ctName))
         throw new IllegalArgumentException("BlankContent");
      if (StringUtils.isBlank(viewName))
         throw new IllegalArgumentException("View name may not be blank.");
      
      List<IPSNodeDefinition> nodes = PSContentTypeHelper.loadNodeDefs(ctName);
      if (nodes.isEmpty())
         return null; // content type does not exist.
      
      IPSNodeDefinition node = nodes.get(0);
      PSContentEditor ctEditor = PSItemDefManager.getInstance()
            .getContentEditorDef(node.getGUID().longValue());
      if (ctEditor == null)
         return null; // content type is not enabled.
      
      if (viewName.startsWith(IPSConstants.SYS_HIDDEN_FIELDS_VIEW_NAME))
      {
          PSContentEditorHandler.addHiddenFieldsView(ctEditor, viewName);
      }
      
      StringBuilder buffer = new StringBuilder();
      buffer.append(node.getQueryRequest());
      buffer.append("?");
      if(readonly)
         buffer.append("sys_command=preview");
      else
         buffer.append("sys_command=edit");
      if (itemId != null)
      {
         PSLegacyGuid id = (PSLegacyGuid) getItemGuid(itemId);
         PSComponentSummary summary = getItemSummary(id.getContentId());
         if (!readonly && !isItemCheckedOutToUser(summary))
         {
            String msg = "'" + summary.getName() + "' must be checked "
               + "out by the current user.";
            PSErrorException e = new PSErrorException();
            e.setErrorMessage(msg);
            throw e;
         }
         if (!readonly)
         {
            id = new PSLegacyGuid(summary.getEditLocator());
         }
         buffer.append("&sys_contentid=").append(id.getContentId());
         buffer.append("&sys_revision=").append(id.getRevision());
      }
      buffer.append("&sys_view=").append(viewName);
      return buffer.toString();
   }
   
   // @see IPSContentDesignWs#createContentTypes(List<String>, String, String)
   public List<PSItemDefinition> createContentTypes(List<String> names,
      String session, String user) throws PSErrorException
   {
      if (names == null || names.isEmpty())
         throw new IllegalArgumentException("names cannot be null or empty");

      List<PSItemDefinition> itemDefs = new ArrayList<>();
      for (String name : names)
      {
         // validate name
         PSContentTypeHelper.validateUniqueName(name);

         IPSObjectLockService lockService = PSObjectLockServiceLocator
            .getLockingService();
         IPSContentTypeMgr contentTypeMgr = PSContentMgrLocator.getContentMgr();
         IPSNodeDefinition nodeDef = contentTypeMgr.createNodeDefinition();

         try
         {
            // lock the new guid, use null version
            lockService.createLock(nodeDef.getGUID(), session, user, null,
               false);

            nodeDef.setInternalName(name);
            nodeDef.setLabel(name);
            nodeDef.setObjectType(1);

            itemDefs.add(PSContentTypeHelper.createContentType(nodeDef));
         }
         catch (PSLockException |IOException | SAXException | PSUnknownNodeTypeException e)
         {
            throwOperationError("Failed to load default ce template", e);
         }
      }

      return itemDefs;
   }

   // @see IPSContentDesignWs#createKeywords(List<String>, String, String)
   public List<PSKeyword> createKeywords(List<String> names, String session,
      String user)
   {
      if (names == null || names.isEmpty())
         throw new IllegalArgumentException("names cannot be null or empty");

      List<PSKeyword> keywords = new ArrayList<>();
      for (String name : names)
      {
         if (StringUtils.isBlank(name))
            throw new IllegalArgumentException("name cannot be null or empty");

         IPSContentService service = PSContentServiceLocator
            .getContentService();

         List<PSKeyword> existing = service.findKeywordsByLabel(name, null);
         if (!existing.isEmpty())
         {
            PSWebserviceUtils.throwObjectExistException(name,
               PSTypeEnum.KEYWORD_DEF);
         }

         PSKeyword keyword = service.createKeyword(name, null);

         IPSObjectLockService lockService = PSObjectLockServiceLocator
            .getLockingService();
         try
         {
            lockService.createLock(keyword.getGUID(), session, user, null,
               false);
         }
         catch (PSLockException e)
         {
            // should never happen, ignore
         }

         keywords.add(keyword);
      }

      return keywords;
   }

   // @see IPSContentDesignWs#createLocale(String, String)
   public List<PSLocale> createLocales(List<String> codes, List<String> names,
      String session, String user) throws PSErrorException
   {
      if (codes == null || codes.isEmpty())
         throw new IllegalArgumentException("codes may not be null or empty");
      if (names == null || names.isEmpty())
         throw new IllegalArgumentException("names may not be null or empty");
      if (codes.size() != names.size())
         throw new IllegalArgumentException(
            "codes and labels must have the same number of elements");

      List<PSLocale> results = new ArrayList<>(codes.size());
      IPSCmsObjectMgr mgr = PSCmsObjectMgrLocator.getObjectManager();
      IPSObjectLockService lockService = PSObjectLockServiceLocator
         .getLockingService();

      Iterator<String> codesItr = codes.iterator();
      Iterator<String> namesItr = names.iterator();
      while (codesItr.hasNext())
      {
         String lang = codesItr.next();
         String name = namesItr.next();

         if (StringUtils.isBlank(lang))
            throw new IllegalArgumentException("code may not be null or empty");

         if (StringUtils.isBlank(name))
            throw new IllegalArgumentException("label may not be null or empty");

         // check for existing
         boolean existing = (mgr.findLocaleByLanguageString(lang) != null);
         if (existing)
         {
            int code = IPSWebserviceErrors.OBJECT_ALREADY_EXISTS;
            throw new IllegalArgumentException(PSWebserviceErrors
               .createErrorMessage(code, PSTypeEnum.LOCALE, lang));
         }

         PSLocale locale = mgr.createLocale(lang, name);

         // lock it
         try
         {
            lockService.createLock(new PSGuid(PSTypeEnum.LOCALE, locale
               .getLocaleId()), session, user, null, false);
         }
         catch (PSLockException e)
         {
            // should never happen
            throwOperationError("Failed to establish lock", e);
         }

         results.add(locale);
      }

      return results;
   }

   // @see IPSContentDesignWs#deleteContentTypes(List, boolean, String, String)
   public void deleteContentTypes(List<IPSGuid> ids,
      boolean ignoreDependencies, String session, String user)
      throws PSErrorsException
   {
      if (PSGuidUtils.isBlank(ids))
         throw new IllegalArgumentException(IDS_NOT_EMPTY_MSG);

      IPSObjectLockService lockService = PSObjectLockServiceLocator
         .getLockingService();

      PSErrorsException results = new PSErrorsException();

      for (IPSGuid id : ids)
      {
         try
         {
            if (PSWebserviceUtils.hasValidLockForDelete(id, session, user))
            {
               // check for deps if requested
               if (!ignoreDependencies)
               {
                  PSErrorException error = PSWebserviceUtils
                     .checkDependencies(id);
                  if (error != null)
                  {
                     results.addError(id, error);
                     continue;
                  }
               }

               // remove slot associations
              PSWebserviceUtils.removeSlotAssocations(id, session, user);

               Integer version = null;
               PSObjectLock lock = lockService.findLockByObjectId(id, session,
                  user);
               // remove template associations 
             if (lock == null)
               {
                  loadAssociatedTemplates(id, true, false, session, user);
               }
               List<IPSGuid> templateIds = Collections.emptyList();
               saveAssociatedTemplates(id, templateIds, lock == null, session,
                  user);

               // this needs to happen after the save in case the version is
               // incremented
               lock = lockService.findLockByObjectId(id, session, user);
               if (lock != null)
                  version = lock.getLockedVersion();
               PSContentTypeHelper.deleteContentType(id, version);

               results.addResult(id);
            }
            else
            {
               PSWebserviceUtils.handleMissingLockError(id,
                  PSItemDefinition.class, results);
            }
         }
         catch (Exception e)
         {
            int code = IPSWebserviceErrors.DELETE_FAILED;
            PSDesignGuid guid = new PSDesignGuid(id);
            PSErrorException error = new PSErrorException(code,
               PSWebserviceErrors.createErrorMessage(code,
                  PSItemDefinition.class.getName(), guid.getValue(), e
                     .getLocalizedMessage()), ExceptionUtils
                  .getFullStackTrace(e));
            results.addError(id, error);
         }
      }

      if (results.hasErrors())
         throw results;
   }

   // @see IPSContentDesignWs#deleteKeywords(List, boolean, String, String)
   public void deleteKeywords(List<IPSGuid> ids, boolean ignoreDependencies,
      String session, String user) throws PSErrorsException
   {
      if (PSGuidUtils.isBlank(ids))
         throw new IllegalArgumentException(IDS_NOT_EMPTY_MSG);

      IPSContentService service = PSContentServiceLocator.getContentService();

      PSErrorsException results = new PSErrorsException();
      for (IPSGuid id : ids)
      {
         if (PSWebserviceUtils.hasValidLockForDelete(id, session, user))
         {
            boolean exists = false;
            try
            {
               service.loadKeyword(id, null);
               exists = true;
            }
            catch (PSContentException e)
            {
               // ignore, just means that the keyword does not exist
            }

            if (exists)
            {
               // check for dependents if requested
               if (!ignoreDependencies)
               {
                  PSErrorException error = PSWebserviceUtils
                     .checkDependencies(id);
                  if (error != null)
                  {
                     results.addError(id, error);
                     continue;
                  }
               }

               service.deleteKeyword(id);
            }

            results.addResult(id);
         }
         else
         {
            PSWebserviceUtils.handleMissingLockError(id, PSKeyword.class,
               results);
         }
      }

      // release locks for all successfully deleted objects
      PSWebserviceUtils.releaseLocks(results.getResults(), session, user);

      if (results.hasErrors())
         throw results;
   }

   // @see IPSContentDesignWs#deleteLocales(List, boolean)
   public void deleteLocales(List<IPSGuid> ids, boolean ignoreDependencies,
      String session, String user) throws PSErrorsException
   {
      if (PSGuidUtils.isBlank(ids))
         throw new IllegalArgumentException(IDS_NOT_EMPTY_MSG);

      IPSCmsObjectMgr objMgr = PSCmsObjectMgrLocator.getObjectManager();

      PSErrorsException results = new PSErrorsException();
      for (IPSGuid id : ids)
      {
         try
         {
            if (PSWebserviceUtils.hasValidLockForDelete(id, session, user))
            {
               PSLocale locale = objMgr.loadLocale((int) id.longValue());

               if (locale != null)
               {
                  // check for deps if requested
                  if (!ignoreDependencies)
                  {
                     PSErrorException error = PSWebserviceUtils
                        .checkDependencies(id);
                     if (error != null)
                     {
                        results.addError(id, error);
                        continue;
                     }
                  }

                  objMgr.deleteLocale(locale);
               }

               results.addResult(id);
            }
            else
            {
               PSWebserviceUtils.handleMissingLockError(id, PSLocale.class,
                  results);
            }
         }
         catch (PSORMException e)
         {
            int code = IPSWebserviceErrors.DELETE_FAILED;
            PSDesignGuid guid = new PSDesignGuid(id);
            PSErrorException error = new PSErrorException(code,
               PSWebserviceErrors.createErrorMessage(code, PSTemplateSlot.class
                  .getName(), guid.getValue(), e.getLocalizedMessage()),
               ExceptionUtils.getFullStackTrace(e));
            results.addError(id, error);
         }
      }

      // release locks for all successfully deleted objects
      PSWebserviceUtils.releaseLocks(results.getResults(), session, user);

      if (results.hasErrors())
         throw results;
   }

   // @see IPSContentDesignWs#findContentTypes(String)
   public List<IPSCatalogSummary> findContentTypes(String name)
   {
      List<IPSCatalogSummary> sums = new ArrayList<>();

      IPSObjectLockService lockService = PSObjectLockServiceLocator
         .getLockingService();
      
      List<IPSNodeDefinition> nodeDefs = PSContentTypeHelper.loadNodeDefs(name);
      
      if (nodeDefs!=null && !nodeDefs.isEmpty())
      {
         
         List<PSObjectLock> locks = lockService.findLocksByObjectIds(nodeDefs.stream().map(IPSCatalogIdentifier::getGUID).collect(Collectors.toList()),null,null);
         Map<IPSGuid,PSObjectLock> lockMap = locks.stream().collect(Collectors.toMap(PSObjectLock::getObjectId, Function.identity()));
               
         for (IPSNodeDefinition nodeDef : nodeDefs)
         {
            
            PSObjectLock lock = lockMap.get(nodeDef.getGUID());
         PSObjectSummary sum = new PSObjectSummary(nodeDef.getGUID(), nodeDef
            .getInternalName(), nodeDef.getLabel(), nodeDef.getDescription());
         if (lock != null)
            sum.setLockedInfo(lock);
         sums.add(sum);
      }
      }
      return PSWebserviceUtils.toObjectSummaries(sums);
   }

   // @see IPSContentDesignWs#findKeywords(String)
   public List<IPSCatalogSummary> findKeywords(String name)
   {
      IPSContentWs service = PSContentWsLocator.getContentWebservice();
      List<PSKeyword> keywords = service.loadKeywords(name);
      return PSWebserviceUtils.toObjectSummaries(keywords);
   }

   // @see IPSContentDesignWs#findLocales(String, String)
   public List<IPSCatalogSummary> findLocales(String code, String name)
   {
      IPSContentWs service = PSContentWsLocator.getContentWebservice();
      List<PSLocale> locales = service.loadLocales(code, name);
      return PSWebserviceUtils.toObjectSummaries(locales);
   }

   /**
    * @see IPSContentDesignWs#loadAssociatedTemplates(IPSGuid, boolean, boolean, 
    *    String, String)
    */
   public List<PSContentTemplateDesc> loadAssociatedTemplates(
      IPSGuid contentTypeId, boolean lock, boolean overrideLock,
      String session, String user) throws PSErrorResultsException
   {
      if (lock && StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");

      if (lock && StringUtils.isBlank(user))
         throw new IllegalArgumentException("user cannot be null or empty");

      IPSContentMgr mgr = PSContentMgrLocator.getContentMgr();

      PSErrorResultsException results = new PSErrorResultsException();
      List<PSContentTemplateDesc> descList = new ArrayList<>();

      try
      {
         List<IPSNodeDefinition> nodeDefs;
         if (contentTypeId == null)
         {
            nodeDefs = mgr.findAllItemNodeDefinitions();
         }
         else
         {
            nodeDefs = mgr.loadNodeDefinitions(PSGuidUtils
               .toGuidList(contentTypeId));
         }

         for (IPSNodeDefinition def : nodeDefs)
         {
            PSNodeDefinition nodeDef = (PSNodeDefinition) def;
            descList.addAll(nodeDef.getCvDescriptors());
            results.addResult(def.getGUID(), nodeDef.getCvDescriptors());

            try
            {
               if (lock)
               {
                  IPSObjectLockService lockService = PSObjectLockServiceLocator
                     .getLockingService();
                  lockService.createLock(def.getGUID(), session, user, nodeDef
                     .getVersion(), overrideLock);
               }
            }
            catch (PSLockException e)
            {
               int code = IPSWebserviceErrors.CREATE_LOCK_FAILED;
               PSLockErrorException error = new PSLockErrorException(code,
                  PSWebserviceErrors.createErrorMessage(code, nodeDef
                     .getClass().getName(), nodeDef.getGUID().longValue(), e
                     .getLocalizedMessage()), ExceptionUtils
                     .getFullStackTrace(e), e.getLocker(), e.getRemainigTime());
               results.addError(nodeDef.getGUID(), error);
            }
         }
      }
      catch (RepositoryException e)
      {
         int code = IPSWebserviceErrors.LOAD_FAILED;
         PSDesignGuid guid = new PSDesignGuid(contentTypeId);
         PSErrorException error = new PSErrorException(code, PSWebserviceErrors
            .createErrorMessage(code, IPSNodeDefinition.class.getName(), guid
               .getValue(), e.getLocalizedMessage()), ExceptionUtils
            .getFullStackTrace(e));
         results.addError(contentTypeId, error);
      }

      if (results.hasErrors())
         throw results;

      return descList;
   }

   /**
    * @see IPSContentDesignWs#loadAssociatedWorkflows(IPSGuid, boolean, boolean)
    */
   public List<PSContentTypeWorkflow> loadAssociatedWorkflows(
         IPSGuid contentTypeId, boolean lock, boolean overrideLock)
      throws PSErrorResultsException
   {
      String session = getRequest().getUserSessionId();
      String user = PSWebserviceUtils.getUserName();

      if (lock && StringUtils.isBlank(user))
         throw new IllegalArgumentException("user cannot be null or empty");

      IPSContentMgr mgr = PSContentMgrLocator.getContentMgr();

      PSErrorResultsException results = new PSErrorResultsException();
      List<PSContentTypeWorkflow> ctWfList = 
         new ArrayList<>();

      try
      {
         List<IPSNodeDefinition> nodeDefs;
         if (contentTypeId == null)
         {
            nodeDefs = mgr.findAllItemNodeDefinitions();
         }
         else
         {
            nodeDefs = mgr.loadNodeDefinitions(PSGuidUtils
               .toGuidList(contentTypeId));
         }

         for (IPSNodeDefinition def : nodeDefs)
         {
            PSNodeDefinition nodeDef = (PSNodeDefinition) def;
            ctWfList.addAll(nodeDef.getCtWfRels());
            results.addResult(def.getGUID(), nodeDef.getCtWfRels());

            try
            {
               if (lock)
               {
                  IPSObjectLockService lockService = PSObjectLockServiceLocator
                     .getLockingService();
                  lockService.createLock(def.getGUID(), session, user, nodeDef
                     .getVersion(), overrideLock);
               }
            }
            catch (PSLockException e)
            {
               int code = IPSWebserviceErrors.CREATE_LOCK_FAILED;
               PSLockErrorException error = new PSLockErrorException(code,
                  PSWebserviceErrors.createErrorMessage(code, nodeDef
                     .getClass().getName(), nodeDef.getGUID().longValue(), e
                     .getLocalizedMessage()), ExceptionUtils
                     .getFullStackTrace(e), e.getLocker(), e.getRemainigTime());
               results.addError(nodeDef.getGUID(), error);
            }
         }
      }
      catch (RepositoryException e)
      {
         int code = IPSWebserviceErrors.LOAD_FAILED;
         PSDesignGuid guid = new PSDesignGuid(contentTypeId);
         PSErrorException error = new PSErrorException(code, PSWebserviceErrors
            .createErrorMessage(code, IPSNodeDefinition.class.getName(), guid
               .getValue(), e.getLocalizedMessage()), ExceptionUtils
            .getFullStackTrace(e));
         results.addError(contentTypeId, error);
      }

      if (results.hasErrors())
         throw results;

      return ctWfList;
   }

   /**
    * @see IPSContentDesignWs#loadContentEditorSharedDef(boolean, boolean, 
    *    String, String)
    */
   public PSContentEditorSharedDef loadContentEditorSharedDef(boolean lock,
      boolean overrideLock, String session, String user)
      throws PSLockErrorException, PSErrorException
   {
      try
      {
         PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();

         PSContentEditorSharedDef def = os.getContentEditorSharedDef();

         IPSGuid id = def.getGUID();

         if (lock)
         {
            try
            {
               IPSObjectLockService lockService = PSObjectLockServiceLocator
                  .getLockingService();

               lockService.createLock(id, session, user, null, overrideLock);
            }
            catch (PSLockException e)
            {
               int code = IPSWebserviceErrors.CREATE_LOCK_FAILED;
               throw new PSLockErrorException(code, PSWebserviceErrors
                  .createErrorMessage(code, def.getClass().getName(), id
                     .longValue(), e.getLocalizedMessage()), ExceptionUtils
                  .getFullStackTrace(e), e.getLocker(), e.getRemainigTime());
            }
         }

         return def;
      }
      catch (IOException | PSUnknownDocTypeException | PSUnknownNodeTypeException | SAXException e)
      {
         throwUnexpectedError(e);
      }
      // should never get here. this is used to turn off compiling error
      return null;
   }

   /**
    * @see IPSContentDesignWs#loadContentEditorSystemDef(boolean, boolean, 
    *    String, String)
    */
   public PSContentEditorSystemDef loadContentEditorSystemDef(boolean lock,
      boolean overrideLock, String session, String user)
      throws PSLockErrorException, PSErrorException
   {
      try
      {
         PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
         PSContentEditorSystemDef def = os.getContentEditorSystemDef();

         IPSGuid id = def.getGUID();
         if (lock)
         {
            try
            {
               IPSObjectLockService lockService = PSObjectLockServiceLocator
                  .getLockingService();

               lockService.createLock(id, session, user, null, overrideLock);
            }
            catch (PSLockException e)
            {
               int code = IPSWebserviceErrors.CREATE_LOCK_FAILED;
               throw new PSLockErrorException(code, PSWebserviceErrors
                  .createErrorMessage(code, def.getClass().getName(), id
                     .longValue(), e.getLocalizedMessage()), ExceptionUtils
                  .getFullStackTrace(e), e.getLocker(), e.getRemainigTime());
            }
         }

         return def;
      }
      catch (IOException | SAXException | PSUnknownNodeTypeException | PSUnknownDocTypeException e)
      {
         throwUnexpectedError(e);
      }

      // should never get here. this is used to turn off compiling error
      return null;
   }

   /**
    * @see IPSContentDesignWs#loadContentTypes(List, boolean, boolean, 
    *    String, String)
    */
   @SuppressWarnings("unchecked")
   public List<PSItemDefinition> loadContentTypes(List<IPSGuid> ids,
      boolean lock, boolean overrideLock, String session, String user)
      throws PSErrorResultsException
   {
      if (PSGuidUtils.isBlank(ids))
         throw new IllegalArgumentException(IDS_NOT_EMPTY_MSG);

      if (lock && StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");

      if (lock && StringUtils.isBlank(user))
         throw new IllegalArgumentException("user cannot be null or empty");

      Map<IPSGuid, PSItemDefinition> resultMap = new HashMap<>();
      PSErrorResultsException results = new PSErrorResultsException();

      // get list of node defs for locking later
      List<IPSNodeDefinition> defs = null;
      try
      {
         defs = PSContentTypeHelper.loadNodeDefs(ids);
      }
      catch (RuntimeException e)
      {
         // claims to throw exception only if no node def is found
         int code = IPSWebserviceErrors.OBJECT_NOT_FOUND;
         for (IPSGuid id : ids)
         {
            PSDesignGuid guid = new PSDesignGuid(id);
            PSErrorException error = new PSErrorException(code,
               PSWebserviceErrors.createErrorMessage(code,
                  PSItemDefinition.class.getName(), guid.getValue()),
               ExceptionUtils.getFullStackTrace(e));
            results.addError(id, error);
         }
      }
      //we cannot continue if there are already errors
      if (results.hasErrors())
         throw results;

      for (IPSGuid id : ids)
      {
         try
         {
            PSItemDefinition itemDef = PSContentTypeHelper.loadItemDef(id);
            resultMap.put(id, itemDef);
         }
         catch (Exception e)
         {
            int code = IPSWebserviceErrors.OBJECT_NOT_FOUND;
            PSDesignGuid guid = new PSDesignGuid(id);
            PSErrorException error = new PSErrorException(code,
               PSWebserviceErrors.createErrorMessage(code,
                  PSItemDefinition.class.getName(), guid.getValue()),
               ExceptionUtils.getFullStackTrace(e));
            results.addError(id, error);
         }
      }

      // walk list of requested ids, lock if required, add to results
      IPSObjectLockService lockService = PSObjectLockServiceLocator
         .getLockingService();
      if (defs != null) {
         for (IPSNodeDefinition def : defs)
         {
            PSNodeDefinition nodeDef = (PSNodeDefinition) def;
            PSItemDefinition itemDef = resultMap.get(nodeDef.getGUID());
            if (itemDef == null)
            {
               // must have been an error
               continue;
            }

            if (lock)
            {
               try
               {
                  lockService.createLock(nodeDef.getGUID(), session, user, nodeDef
                     .getVersion(), overrideLock);
                  results.addResult(nodeDef.getGUID(), itemDef);
               }
               catch (PSLockException e)
               {
                  int code = IPSWebserviceErrors.CREATE_LOCK_FAILED;
                  PSLockErrorException error = new PSLockErrorException(code,
                     PSWebserviceErrors.createErrorMessage(code, nodeDef
                        .getClass().getName(), nodeDef.getGUID().longValue(), e
                        .getLocalizedMessage()), ExceptionUtils
                        .getFullStackTrace(e), e.getLocker(), e.getRemainigTime());
                  results.addError(nodeDef.getGUID(), error);
               }
            }
            else
            {
               results.addResult(nodeDef.getGUID(), itemDef);
            }
         }
      }

      if (results.hasErrors())
         throw results;

      return results.getResults(ids);
   }

   /**
    * @see IPSContentDesignWs#loadKeywords(List, boolean, boolean, 
    *    String, String)
    */
   @SuppressWarnings("unchecked")
   public List<PSKeyword> loadKeywords(List<IPSGuid> ids, boolean lock,
      boolean overrideLock, String session, String user)
      throws PSErrorResultsException
   {
      if (PSGuidUtils.isBlank(ids))
         throw new IllegalArgumentException(IDS_NOT_EMPTY_MSG);

      if (lock && StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");

      if (lock && StringUtils.isBlank(user))
         throw new IllegalArgumentException("user cannot be null or empty");

      IPSContentService service = PSContentServiceLocator.getContentService();

      PSErrorResultsException results = new PSErrorResultsException();
      for (IPSGuid id : ids)
      {
         try
         {
            PSKeyword keyword = service.loadKeyword(id, "sequence");
            results.addResult(id, keyword);
         }
         catch (PSContentException e)
         {
            int code = IPSWebserviceErrors.OBJECT_NOT_FOUND;
            PSDesignGuid guid = new PSDesignGuid(id);
            PSErrorException error = new PSErrorException(code,
               PSWebserviceErrors.createErrorMessage(code, PSKeyword.class
                  .getName(), guid.getValue()), ExceptionUtils
                  .getFullStackTrace(e));
            results.addError(id, error);
         }
      }

      if (lock)
      {
         IPSObjectLockService lockService = PSObjectLockServiceLocator
            .getLockingService();
         lockService.createLocks(results, session, user, overrideLock);
      }

      if (results.hasErrors())
         throw results;

      return results.getResults(ids);
   }

   /**
    * @see IPSContentDesignWs#loadLocales(List, boolean, boolean, 
    *    String, String)
    */
   public List<PSLocale> loadLocales(List<IPSGuid> ids, boolean lock,
      boolean overrideLock, String session, String user)
      throws PSErrorResultsException
   {
      if (PSGuidUtils.isBlank(ids))
         throw new IllegalArgumentException(IDS_NOT_EMPTY_MSG);

      if (lock && StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");

      if (lock && StringUtils.isBlank(user))
         throw new IllegalArgumentException("user cannot be null or empty");

      IPSCmsObjectMgr objMgr = PSCmsObjectMgrLocator.getObjectManager();

      PSErrorResultsException results = new PSErrorResultsException();
      List<PSLocale> locales = new ArrayList<>();
      for (IPSGuid id : ids)
      {

         PSLocale locale = objMgr.loadLocale((int) id.longValue());
         if (locale == null)
         {
            int code = IPSWebserviceErrors.OBJECT_NOT_FOUND;
            PSDesignGuid guid = new PSDesignGuid(id);
            PSErrorException error = new PSErrorException(code,
               PSWebserviceErrors.createErrorMessage(code, PSLocale.class
                  .getName(), guid.getValue()), ExceptionUtils
                  .getFullStackTrace(new Exception()));
            results.addError(id, error);
         }
         else
         {
            results.addResult(id, locale);
            locales.add(locale);
         }
      }

      if (lock)
      {
         IPSObjectLockService lockService = PSObjectLockServiceLocator
            .getLockingService();
         lockService.createLocks(results, session, user, overrideLock);
      }

      if (results.hasErrors())
         throw results;

      return locales;
   }

   /**
    * @see IPSContentDesignWs#loadTranslationSettings(boolean, boolean, 
    *    String, String)
    */
   public List<PSAutoTranslation> loadTranslationSettings(boolean lock,
      boolean overrideLock, String session, String user)
      throws PSLockErrorException
   {
      if (lock && StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");

      if (lock && StringUtils.isBlank(user))
         throw new IllegalArgumentException("user cannot be null or empty");

      IPSContentService service = PSContentServiceLocator.getContentService();

      List<PSAutoTranslation> atList = service.loadAutoTranslations();

      if (lock)
      {
         IPSObjectLockService lockService = PSObjectLockServiceLocator
            .getLockingService();

         // use a single "dummy" guid for the whole set
         IPSGuid id = PSAutoTranslation.getAutoTranslationsGUID();
         try
         {
            lockService.createLock(id, session, user, null, overrideLock);
         }
         catch (PSLockException e)
         {
            throw new PSLockErrorException(e.getErrorCode(), e.getMessage(),
               ExceptionUtils.getFullStackTrace(e));
         }
      }

      return atList;
   }

   /**
    * @see IPSContentDesignWs#saveAssociatedTemplates(IPSGuid, List, boolean, 
    *    String, String)
    */
   public void saveAssociatedTemplates(IPSGuid contentTypeId,
      List<IPSGuid> templateIds, boolean release, String session, String user)
      throws PSErrorsException
   {
      final String TYPE = "Template Links";
      if (contentTypeId == null)
         throw new IllegalArgumentException("contentTypeId may not be null");

      if (PSGuidUtils.isBlank(templateIds))
         templateIds = new ArrayList<>();

      if (release && StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");

      if (release && StringUtils.isBlank(user))
         throw new IllegalArgumentException("user cannot be null or empty");

      IPSObjectLockService lockService = PSObjectLockServiceLocator
         .getLockingService();
      IPSContentMgr mgr = PSContentMgrLocator.getContentMgr();

      PSErrorsException results = new PSErrorsException();
      PSNodeDefinition nodeDef = null;

      try
      {
         PSObjectLock lock = lockService.findLockByObjectId(contentTypeId,
            session, user);
         if (lock != null)
         {
            List<IPSNodeDefinition> nodeDefs = mgr
               .loadNodeDefinitions(PSGuidUtils.toGuidList(contentTypeId));
            nodeDef = (PSNodeDefinition) nodeDefs.iterator().next();

            // set the locked version on what we'll save
            Integer version = lockService.getLockedVersion(contentTypeId);
            if (version != null)
               nodeDef.setVersion(version);

            // get current list
            Set<IPSGuid> curTemplates = nodeDef.getVariantGuids();

            // remove any existing not supplied
            Set<IPSGuid> missingTemplates = new HashSet<>(curTemplates);
            templateIds.forEach(missingTemplates::remove);

            PSErrorException error = null;
            if (!missingTemplates.isEmpty())
            {
               error = PSWebserviceUtils.checkAssociationDependencies(
                  contentTypeId, new ArrayList<>(missingTemplates));
            }

            if (error != null)
               results.addError(contentTypeId, error);
            else
            {
               for (IPSGuid guid : missingTemplates)
               {
                  nodeDef.removeVariantGuid(guid);
               }

               // create add list, ignoring any already associated
               templateIds.removeAll(curTemplates);
               for (IPSGuid guid : templateIds)
               {
                  nodeDef.addVariantGuid(guid);
               }

               // save
               mgr.saveNodeDefinitions(nodeDefs);
               if (version != null)
               {
                  List<IPSNodeDefinition> updatedDefs = mgr
                     .loadNodeDefinitions(Collections
                        .singletonList(contentTypeId));
                  assert (updatedDefs.size() == 1);

                  lockService.extendLock(contentTypeId, session, user,
                     ((PSNodeDefinition) updatedDefs.get(0)).getVersion());
               }

               // extend the lock 
               if (!release)
               {
                  nodeDefs = mgr.loadNodeDefinitions(PSGuidUtils
                     .toGuidList(contentTypeId));
                  nodeDef = (PSNodeDefinition) nodeDefs.iterator().next();

                  lockService.extendLock(contentTypeId, session, user, nodeDef
                     .getVersion());
               }
            }

         }
         else
         {
            lock = lockService.findLockByObjectId(contentTypeId, null, null);

            if (lock == null)
            {
               int code = IPSWebserviceErrors.OBJECT_NOT_LOCKED;
               PSDesignGuid guid = new PSDesignGuid(contentTypeId);
               PSErrorException error = new PSErrorException(code,
                  PSWebserviceErrors.createErrorMessage(code, TYPE, guid
                     .toString()), ExceptionUtils
                     .getFullStackTrace(new Exception()));
               results.addError(contentTypeId, error);
            }
            else
            {
               int code = IPSWebserviceErrors.OBJECT_NOT_LOCKED_FOR_REQUESTOR;
               PSDesignGuid guid = new PSDesignGuid(contentTypeId);
               PSErrorException error = new PSErrorException(code,
                  PSWebserviceErrors.createErrorMessage(code, TYPE, guid
                     .toString(), lock.getLocker(), lock.getRemainingTime()),
                  ExceptionUtils.getFullStackTrace(new Exception()));
               results.addError(contentTypeId, error);
            }
         }
      }
      catch (NoSuchNodeTypeException e)
      {
         int code = IPSWebserviceErrors.OBJECT_NOT_FOUND;
         PSDesignGuid guid = new PSDesignGuid(contentTypeId);
         PSErrorException error = new PSErrorException(code, PSWebserviceErrors
            .createErrorMessage(code, TYPE, guid.toString()), ExceptionUtils
            .getFullStackTrace(e));
         results.addError(contentTypeId, error);
      }
      catch (Exception e)
      {
         int code = IPSWebserviceErrors.SAVE_FAILED;
         PSDesignGuid guid = new PSDesignGuid(contentTypeId);
         PSErrorException error = new PSErrorException(code, PSWebserviceErrors
            .createErrorMessage(code, TYPE, guid.toString(), e
               .getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
         results.addError(contentTypeId, error);
      }

      if (release)
      {
         PSObjectLock lock = lockService.findLockByObjectId(contentTypeId,
            session, user);
         lockService.releaseLock(lock);
      }

      if (results.hasErrors())
         throw results;
   }

   /**
    * @see IPSContentDesignWs#saveAssociatedWorkflows(IPSGuid, List, boolean)
    */
   public void saveAssociatedWorkflows(IPSGuid contentTypeId,
      List<IPSGuid> workflowIds, boolean release)
      throws PSErrorsException
   {
      String session = getRequest().getUserSessionId();
      String user = PSWebserviceUtils.getUserName();
      final String TYPE = "Workflow Links";
      if (contentTypeId == null)
         throw new IllegalArgumentException("contentTypeId may not be null");

      if (PSGuidUtils.isBlank(workflowIds))
         workflowIds = new ArrayList<>();

      if (release && StringUtils.isBlank(user))
         throw new IllegalArgumentException("user cannot be null or empty");

      IPSObjectLockService lockService = PSObjectLockServiceLocator
         .getLockingService();
      IPSContentMgr mgr = PSContentMgrLocator.getContentMgr();

      PSErrorsException results = new PSErrorsException();
      PSNodeDefinition nodeDef = null;

      try
      {
         PSObjectLock lock = lockService.findLockByObjectId(contentTypeId,
            session, user);
         if (lock != null)
         {
            List<IPSNodeDefinition> nodeDefs = mgr
                  .loadNodeDefinitions(PSGuidUtils.toGuidList(contentTypeId));
            nodeDef = (PSNodeDefinition) nodeDefs.iterator().next();

            // set the locked version on what we'll save
            Integer version = lockService.getLockedVersion(contentTypeId);
            if (version != null)
               nodeDef.setVersion(version);
            //Merge workflows
            PSContentTypeHelper.mergeWorkflowIds(nodeDef,
                  new HashSet<>(workflowIds));
            // save
            mgr.saveNodeDefinitions(nodeDefs);
            // update the workflow info in the content editor.
            updateContentTypeWorkflowInfo(nodeDefs);
            if (version != null)
            {
               List<IPSNodeDefinition> updatedDefs = mgr
                     .loadNodeDefinitions(Collections
                           .singletonList(contentTypeId));
               assert (updatedDefs.size() == 1);

               lockService.extendLock(contentTypeId, session, user,
                     ((PSNodeDefinition) updatedDefs.get(0)).getVersion());
            }

            // extend the lock
            if (!release)
            {
               nodeDefs = mgr.loadNodeDefinitions(PSGuidUtils
                     .toGuidList(contentTypeId));
               nodeDef = (PSNodeDefinition) nodeDefs.iterator().next();

               lockService.extendLock(contentTypeId, session, user, nodeDef
                     .getVersion());
            }

         }
         else
         {
            lock = lockService.findLockByObjectId(contentTypeId, null, null);

            if (lock == null)
            {
               int code = IPSWebserviceErrors.OBJECT_NOT_LOCKED;
               PSDesignGuid guid = new PSDesignGuid(contentTypeId);
               PSErrorException error = new PSErrorException(code,
                  PSWebserviceErrors.createErrorMessage(code, TYPE, guid
                     .toString()), ExceptionUtils
                     .getFullStackTrace(new Exception()));
               results.addError(contentTypeId, error);
            }
            else
            {
               int code = IPSWebserviceErrors.OBJECT_NOT_LOCKED_FOR_REQUESTOR;
               PSDesignGuid guid = new PSDesignGuid(contentTypeId);
               PSErrorException error = new PSErrorException(code,
                  PSWebserviceErrors.createErrorMessage(code, TYPE, guid
                     .toString(), lock.getLocker(), lock.getRemainingTime()),
                  ExceptionUtils.getFullStackTrace(new Exception()));
               results.addError(contentTypeId, error);
            }
         }
      }
      catch (NoSuchNodeTypeException e)
      {
         int code = IPSWebserviceErrors.OBJECT_NOT_FOUND;
         PSDesignGuid guid = new PSDesignGuid(contentTypeId);
         PSErrorException error = new PSErrorException(code, PSWebserviceErrors
            .createErrorMessage(code, TYPE, guid.toString()), ExceptionUtils
            .getFullStackTrace(e));
         results.addError(contentTypeId, error);
      }
      catch (Exception e)
      {
         int code = IPSWebserviceErrors.SAVE_FAILED;
         PSDesignGuid guid = new PSDesignGuid(contentTypeId);
         PSErrorException error = new PSErrorException(code, PSWebserviceErrors
            .createErrorMessage(code, TYPE, guid.toString(), e
               .getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
         results.addError(contentTypeId, error);
      }

      if (release)
      {
         PSObjectLock lock = lockService.findLockByObjectId(contentTypeId,
            session, user);
         lockService.releaseLock(lock);
      }

      if (results.hasErrors())
         throw results;
   }
   
   /**
    * Helper method to update the workflow info object of content editor by
    * getting the workflows from node definition.
    * 
    * @param nodeDefs List of node defs assumed not null.
    * @throws PSInvalidContentTypeException
    */
   private void updateContentTypeWorkflowInfo(List<IPSNodeDefinition> nodeDefs)
      throws PSInvalidContentTypeException
   {
      PSItemDefManager mgr = PSItemDefManager.getInstance();
      for (IPSNodeDefinition nodeDef : nodeDefs)
      {
         PSItemDefinition ceDef = mgr.getItemDef(nodeDef.getGUID().longValue(),
               PSItemDefManager.COMMUNITY_ANY);
         PSContentEditor ceditor = ceDef.getContentEditor();
         Set<IPSGuid> wfguids = nodeDef.getWorkflowGuids();
         List<Integer> wfs = new ArrayList<>();
         for (IPSGuid wfguid : wfguids)
         {
            wfs.add(new Integer("" + wfguid.longValue()));
         }
         PSWorkflowInfo wfInfo = new PSWorkflowInfo(
               PSWorkflowInfo.TYPE_INCLUSIONARY, wfs);
         ceditor.setWorkflowInfo(wfInfo);
      }
   }
   
   /** 
    * @see IPSContentDesignWs#saveContentEditorSharedDef(
    *    PSContentEditorSharedDef, boolean, String, String)
    */
   public void saveContentEditorSharedDef(PSContentEditorSharedDef def,
      boolean release, String session, String user)
      throws PSLockErrorException, PSErrorException
   {
      if (def == null)
         throw new IllegalArgumentException("def cannot be null");

      if (release && StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");

      if (release && StringUtils.isBlank(user))
         throw new IllegalArgumentException("user cannot be null or empty");

      PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();

      IPSObjectLockService lockService = PSObjectLockServiceLocator
         .getLockingService();

      IPSGuid id = def.getGUID();
      try
      {
         if (lockService.isLockedFor(id, session, user))
         {
            // save the object and extend the lock
            String errMsg = validateOverrides(PSServer
               .getContentEditorSystemDef(), def);
            if (errMsg != null)
            {
               int code = IPSWebserviceErrors.UNABLE_SAVE_SHARED_DEF_VALIDATION;
               throw new PSErrorException(code, PSWebserviceErrors
                  .createErrorMessage(code, errMsg), ExceptionUtils
                  .getFullStackTrace(new Exception()));
            }
            os.saveContentEditorSharedDefFile(def);
            if (!release)
               lockService.extendLock(id, session, user, null);
         }
         else
         {
            PSObjectLock lock = lockService.findLockByObjectId(id, null, null);
            if (lock == null)
            {
               int code = IPSWebserviceErrors.OBJECT_NOT_LOCKED;
               throw new PSLockErrorException(code, PSWebserviceErrors
                  .createErrorMessage(code, def.getClass().getName(), id
                     .longValue()), ExceptionUtils
                  .getFullStackTrace(new Exception()));
            }

            int code = IPSWebserviceErrors.OBJECT_NOT_LOCKED_FOR_REQUESTOR;
            throw new PSLockErrorException(code, PSWebserviceErrors
               .createErrorMessage(code, def.getClass().getName(), id
                  .longValue(), lock.getLocker(), lock.getRemainingTime()),
               ExceptionUtils.getFullStackTrace(new Exception()));
         }
      }
      catch (PSLockException e)
      {
         int code = IPSWebserviceErrors.SAVE_FAILED;
         throw new PSLockErrorException(code, PSWebserviceErrors
            .createErrorMessage(code, def.getClass().getName(), id.longValue(),
               e.getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
      }
      catch (IOException e)
      {
         throwUnexpectedError(e);
      }

      if (release)
      {
         List<IPSGuid> ids = new ArrayList<>();
         ids.add(id);
         List<PSObjectLock> locks = lockService.findLocksByObjectIds(ids,
            session, user);
         lockService.releaseLocks(locks);
      }
   }

   /**
    * @see IPSContentDesignWs#saveContentEditorSystemDef(
    *    PSContentEditorSystemDef, boolean, String, String)
    */
   public void saveContentEditorSystemDef(PSContentEditorSystemDef def,
      boolean release, String session, String user)
      throws PSLockErrorException, PSErrorException
   {
      if (def == null)
         throw new IllegalArgumentException("def cannot be null");

      if (release && StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");

      if (release && StringUtils.isBlank(user))
         throw new IllegalArgumentException("user cannot be null or empty");

      PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();

      IPSObjectLockService lockService = PSObjectLockServiceLocator
         .getLockingService();

      IPSGuid id = def.getGUID();
      try
      {
         if (lockService.isLockedFor(id, session, user))
         {
            // save the object and extend the lock
            String errMsg = validateOverrides(def, PSServer
               .getContentEditorSharedDef());
            if (errMsg != null)
            {
               int code = IPSWebserviceErrors.SAVE_FAILED;
               throw new PSErrorException(code, PSWebserviceErrors
                  .createErrorMessage(code, def.getClass().getName(), id
                     .longValue(), errMsg), ExceptionUtils
                  .getFullStackTrace(new Exception()));
            }
            os.saveContentEditorSystemDef(def);
            if (!release)
               lockService.extendLock(id, session, user, null);
         }
         else
         {
            PSObjectLock lock = lockService.findLockByObjectId(id, null, null);
            if (lock == null)
            {
               int code = IPSWebserviceErrors.OBJECT_NOT_LOCKED;
               throw new PSLockErrorException(code, PSWebserviceErrors
                  .createErrorMessage(code, def.getClass().getName(), id
                     .longValue()), ExceptionUtils
                  .getFullStackTrace(new Exception()));
            }

            int code = IPSWebserviceErrors.OBJECT_NOT_LOCKED_FOR_REQUESTOR;
            throw new PSLockErrorException(code, PSWebserviceErrors
               .createErrorMessage(code, def.getClass().getName(), id
                  .longValue(), lock.getLocker(), lock.getRemainingTime()),
               ExceptionUtils.getFullStackTrace(new Exception()));
         }
      }
      catch (PSLockException e)
      {
         int code = IPSWebserviceErrors.SAVE_FAILED;
         throw new PSLockErrorException(code, PSWebserviceErrors
            .createErrorMessage(code, def.getClass().getName(), id.longValue(),
               e.getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
      }
      catch (IOException e)
      {
         throwUnexpectedError(e);
      }

      if (release)
      {
         List<IPSGuid> ids = new ArrayList<>();
         ids.add(id);
         List<PSObjectLock> locks = lockService.findLocksByObjectIds(ids,
            session, user);
         lockService.releaseLocks(locks);
      }
   }

   // @see IPSContentDesignWs#saveContentTypes(List, boolean, String, String)
   public void saveContentTypes(List<PSItemDefinition> contentTypes,
      boolean release, String session, String user) throws PSErrorsException
   {
      if (contentTypes == null || contentTypes.isEmpty())
         throw new IllegalArgumentException(
            "contentTypes may not be null or empty");
      if (release && StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");
      if (release && StringUtils.isBlank(user))
         throw new IllegalArgumentException("user cannot be null or empty");

      IPSObjectLockService lockService = PSObjectLockServiceLocator
         .getLockingService();

      PSErrorsException results = new PSErrorsException();
      PSItemDefManager.getInstance().deferUpdateNotifications();
      List<IPSGuid> ids = new ArrayList<>(contentTypes.size());

      try
      {
         for (PSItemDefinition def : contentTypes)
         {
            IPSGuid id = new PSGuid(PSTypeEnum.NODEDEF, def.getTypeId());
            try
            {
               PSObjectLock lock;
               lock = lockService.findLockByObjectId(id, session, user);
               if (lock != null)
               {
                  // get version from lock
                  int version = lock.getLockedVersion() == null ? -1 : lock
                     .getLockedVersion();

                  // save
                  PSSaveNodeDefListener listener = null;
                  if (!release)
                  {
                     listener = new PSSaveNodeDefListener(session, user, id,
                        lockService);
                  }

                  PSContentTypeHelper.saveContentType(def, null, version,
                     listener, true);

                  if (listener != null)
                  {
                     PSLockException le = listener.getLockException();
                     if (le != null)
                     {
                        // problem updating the lock
                        throw le;
                     }
                  }

                  // save id for release
                  ids.add(id);
                  results.addResult(id);
               }
               else
               {
                  lock = lockService.findLockByObjectId(id, null, null);

                  if (lock == null)
                  {
                     int code = IPSWebserviceErrors.OBJECT_NOT_LOCKED;
                     PSDesignGuid guid = new PSDesignGuid(id);
                     PSErrorException error = new PSErrorException(code,
                        PSWebserviceErrors.createErrorMessage(code,
                           PSItemDefinition.class.getName(), guid.getValue()),
                        ExceptionUtils.getFullStackTrace(new Exception()));
                     results.addError(id, error);
                  }
                  else
                  {
                     int code = IPSWebserviceErrors.OBJECT_NOT_LOCKED_FOR_REQUESTOR;
                     PSDesignGuid guid = new PSDesignGuid(id);
                     PSErrorException error = new PSErrorException(code,
                        PSWebserviceErrors.createErrorMessage(code,
                           PSItemDefinition.class.getName(), guid.getValue(),
                           lock.getLocker(), lock.getRemainingTime()),
                        ExceptionUtils.getFullStackTrace(new Exception()));
                     results.addError(id, error);
                  }
               }
            }
            catch (Exception e)
            {
               int code = IPSWebserviceErrors.SAVE_FAILED;
               PSDesignGuid guid = new PSDesignGuid(id);
               PSErrorException error = new PSErrorException(code,
                  PSWebserviceErrors.createErrorMessage(code,
                     PSItemDefinition.class.getName(), guid.getValue(), e
                        .getLocalizedMessage()), ExceptionUtils
                     .getFullStackTrace(e));
               results.addError(id, error);
            }
         }
      }
      finally
      {
         PSItemDefManager.getInstance().commitUpdateNotifications();
      }

      if (release && !ids.isEmpty())
      {
         List<PSObjectLock> locks = lockService.findLocksByObjectIds(ids,
            session, user);
         lockService.releaseLocks(locks);
      }

      if (results.hasErrors())
         throw results;
   }

   // @see IPSContentDesignWs#saveKeywords(List, boolean, String, String)
   public void saveKeywords(List<PSKeyword> keywords, boolean release,
      String session, String user) throws PSErrorsException
   {
      if (keywords == null || keywords.isEmpty())
         throw new IllegalArgumentException("keywords cannot be null or empty");

      if (release && StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");

      if (release && StringUtils.isBlank(user))
         throw new IllegalArgumentException("user cannot be null or empty");

      List<IPSGuid> ids = PSGuidUtils.toGuidList(keywords);

      IPSObjectLockService lockService = PSObjectLockServiceLocator
         .getLockingService();

      PSErrorsException results = new PSErrorsException();
      for (PSKeyword keyword : keywords)
      {
         IPSGuid id = keyword.getGUID();
         try
         {
            if (lockService.isLockedFor(id, session, user))
            {
               Integer version = lockService.getLockedVersion(id);

               // save the object and extend the lock
               saveKeyword(keyword, version);
               
               // reload the keyword to obtain the new version
               IPSContentService service = 
                  PSContentServiceLocator.getContentService();
               keyword = service.loadKeyword(id, null);
               
               if (!release)
               {
                  lockService.extendLock(id, session, user,
                        keyword.getVersion());
               }

               results.addResult(id);
            }
            else
            {
               PSWebserviceUtils.handleMissingLockError(id, PSKeyword.class,
                     results);
            }
         }
         catch (Exception e)
         {
            int code = IPSWebserviceErrors.SAVE_FAILED;
            PSDesignGuid guid = new PSDesignGuid(id);
            PSErrorException error = new PSErrorException(code,
               PSWebserviceErrors.createErrorMessage(code, PSKeyword.class
                  .getName(), guid.getValue(), e.getLocalizedMessage()),
               ExceptionUtils.getFullStackTrace(e));
            results.addError(id, error);
         }
      }

      if (release)
      {
         List<PSObjectLock> locks = lockService.findLocksByObjectIds(ids,
            session, user);
         lockService.releaseLocks(locks);
      }

      if (results.hasErrors())
         throw results;
   }

   // @see IPSContentDesignWs#saveLocales(List, boolean, String, String)
   public void saveLocales(List<PSLocale> locales, boolean release,
      String session, String user) throws PSErrorsException
   {
      if (locales == null || locales.isEmpty())
         throw new IllegalArgumentException("locales may not be null or empty");
      if (release && StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");
      if (release && StringUtils.isBlank(user))
         throw new IllegalArgumentException("user cannot be null or empty");

      IPSObjectLockService lockService = PSObjectLockServiceLocator
         .getLockingService();
      IPSCmsObjectMgr objMgr = PSCmsObjectMgrLocator.getObjectManager();

      PSErrorsException results = new PSErrorsException();
      List<IPSGuid> ids = new ArrayList<>(locales.size());
      for (PSLocale locale : locales)
      {
         IPSGuid id = locale.getGUID();
         try
         {
            boolean isNew = false;
            PSObjectLock lock;
            lock = lockService.findLockByObjectId(id, session, user);
            if (lock != null)
            {
               // set the correct version
               Integer version = lockService.getLockedVersion(id);
               if (version != null)
                  locale.setVersion(version);
               else
                  isNew = true;

               // save
               objMgr.saveLocale(locale);

               // copy resource files if new
               if (isNew)
               {
                  String rxroot = PSServer.getRxDir().getPath();
                  PSLocaleRxResourceCopyHandler resourceCopier = new PSLocaleRxResourceCopyHandler(
                     rxroot, locale.getLanguageString());
                  resourceCopier.processResourceCopy(false);
               }

               // extend the lock 
               if (!release)
               {
                  locale = objMgr.loadLocale((int) id.longValue());
                  if (locale == null)
                  {
                     // we just saved, this is a bug
                     throw new RuntimeException(
                        "Unable to locate locale for id: " + id.longValue());
                  }

                  lockService
                     .extendLock(id, session, user, locale.getVersion());
               }

               // save id for release
               ids.add(id);
               results.addResult(id);
            }
            else
            {
               lock = lockService.findLockByObjectId(id, null, null);

               if (lock == null)
               {
                  int code = IPSWebserviceErrors.OBJECT_NOT_LOCKED;
                  PSDesignGuid guid = new PSDesignGuid(id);
                  PSErrorException error = new PSErrorException(code,
                     PSWebserviceErrors.createErrorMessage(code, PSLocale.class
                        .getName(), guid.getValue()), ExceptionUtils
                        .getFullStackTrace(new Exception()));
                  results.addError(id, error);
               }
               else
               {
                  int code = IPSWebserviceErrors.OBJECT_NOT_LOCKED_FOR_REQUESTOR;
                  PSDesignGuid guid = new PSDesignGuid(id);
                  PSErrorException error = new PSErrorException(code,
                     PSWebserviceErrors.createErrorMessage(code, PSLocale.class
                        .getName(), guid.getValue(), lock.getLocker(), lock
                        .getRemainingTime()), ExceptionUtils
                        .getFullStackTrace(new Exception()));
                  results.addError(id, error);
               }
            }
         }
         catch (Exception e)
         {
            int code = IPSWebserviceErrors.SAVE_FAILED;
            PSDesignGuid guid = new PSDesignGuid(id);
            PSErrorException error = new PSErrorException(code,
               PSWebserviceErrors.createErrorMessage(code, PSLocale.class
                  .getName(), guid.getValue()), ExceptionUtils
                  .getFullStackTrace(e));
            results.addError(id, error);
         }
      }

      if (release && !ids.isEmpty())
      {
         List<PSObjectLock> locks = lockService.findLocksByObjectIds(ids,
            session, user);
         lockService.releaseLocks(locks);
      }

      if (results.hasErrors())
         throw results;
   }

   // @see IPSContentDesignWs#saveTranslationSettings(List, boolean)
   public void saveTranslationSettings(
      List<PSAutoTranslation> autoTranslations, boolean release,
      String session, String user) throws PSLockErrorException
   {
      if (autoTranslations == null)
         throw new IllegalArgumentException("autoTranslations may not be null");
      if (release && StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");
      if (release && StringUtils.isBlank(user))
         throw new IllegalArgumentException("user cannot be null or empty");

      IPSObjectLockService lockService = PSObjectLockServiceLocator
         .getLockingService();

      IPSContentService service = PSContentServiceLocator.getContentService();

      IPSGuid id = PSAutoTranslation.getAutoTranslationsGUID();

      try
      {
         PSObjectLock lock;
         lock = lockService.findLockByObjectId(id, session, user);
         if (lock != null)
         {
            // load the current set
            List<PSAutoTranslation> curList = service.loadAutoTranslations();
            Map<PSAutoTranslationPK, PSAutoTranslation> curMap = new HashMap<>();
            for (PSAutoTranslation at : curList)
            {
               curMap.put(at.getKey(), at);
            }

            // process adds, edits, and deletes
            Map<PSAutoTranslationPK, PSAutoTranslation> newMap = new HashMap<>();
            for (PSAutoTranslation at : autoTranslations)
            {
               PSAutoTranslation cur = curMap.get(at.getKey());
               if (cur != null)
               {
                  newMap.put(at.getKey(), at);

                  // set the correct version
                  at.setVersion(cur.getVersion());
               }

               // save
               service.saveAutoTranslation(at);
            }

            for (PSAutoTranslation at : curList)
            {
               if (!newMap.containsKey(at.getKey()))
               {
                  service.deleteAutoTranslation(at.getContentTypeId(), at
                     .getLocale());
               }
            }

            // extend the lock 
            if (!release)
            {
               lockService.extendLock(id, session, user, null);
            }
         }
         else
         {
            lock = lockService.findLockByObjectId(id, null, null);

            if (lock == null)
            {
               int code = IPSWebserviceErrors.OBJECT_NOT_LOCKED;
               PSDesignGuid guid = new PSDesignGuid(id);
               throw new PSLockErrorException(code, PSWebserviceErrors
                  .createErrorMessage(code, PSAutoTranslation.class.getName(),
                     guid.longValue()), ExceptionUtils
                  .getFullStackTrace(new Exception()));
            }
            else
            {
               int code = IPSWebserviceErrors.OBJECT_NOT_LOCKED_FOR_REQUESTOR;
               PSDesignGuid guid = new PSDesignGuid(id);
               throw new PSLockErrorException(code, PSWebserviceErrors
                  .createErrorMessage(code, PSAutoTranslation.class.getName(),
                     guid.longValue()), ExceptionUtils
                  .getFullStackTrace(new Exception()), lock.getLocker(), lock
                  .getRemainingTime());
            }
         }
      }
      catch (PSLockException e)
      {
         throw new PSLockErrorException(e.getErrorCode(), e.getMessage(),
            ExceptionUtils.getFullStackTrace(e));
      }

      if (release)
      {
         PSObjectLock lock = lockService.findLockByObjectId(id, session, user);
         lockService.releaseLock(lock);
      }
   }
   
   /*
    * //see base interface method for details
    */
   public List<Node> findNodesByIds(List<IPSGuid> ids, boolean isSummary)
   {
      // make sure the revision is not -1; otherwise set to current revision
      List<IPSGuid> idList = new ArrayList<>();
      for (IPSGuid id : ids)
      {
         if (((PSLegacyGuid)id).getRevision() == -1)
         {
            try
            {
               PSLocator loc = PSWebserviceUtils.getItemLocator((PSLegacyGuid)id);
               idList.add(new PSLegacyGuid(loc));
            }
            catch (Exception e)
            {
               continue;
            }
         }
         else
         {
            idList.add(id);
         }
      }
      // now, submit the query
      try
      {
         PSContentMgrConfig loadOption = new PSContentMgrConfig();
         if (isSummary)
         {
            loadOption.addOption(PSContentMgrOption.LOAD_MINIMAL);
            loadOption.addOption(PSContentMgrOption.LAZY_LOAD_CHILDREN);
         }

         
         return m_contentMgr.findItemsByGUID(idList, loadOption);
      }
      catch (Exception e)
      {
         throw new RuntimeException("Failed to find items by IDs", e);
      }
   }

   /**
    * Sets the Content Manager.
    * 
    * @param contentMgr the content manager, never <code>null</code>.
    */
   @Autowired
   public void setContentMgr(IPSContentMgr contentMgr)
   {
      m_contentMgr = contentMgr;
   }
   
   /**
    * Validates the supplied system and shared def against the currently active
    * content types to ensure there are not any invalid overrides or duplicate
    * fields.
    * 
    * @param sysDef The system def to validate against, may be <code>null</code>
    *    if there was an error when it was last initialized.
    * @param sharedDef The shared def to validate against, may be
    *    <code>null</code> if there was an error when it was last initialized.
    * @return An error message indicating the validation failures, or
    *    <code>null</code> if validation succeeded. If either def is
    *    <code>null</code> (indicating a system-wide problem that is likely
    *    already reported), this method simply returns <code>null</code>.
    * @throws PSErrorException If there are any unexpected errors.
    */
   private String validateOverrides(PSContentEditorSystemDef sysDef,
      PSContentEditorSharedDef sharedDef) throws PSErrorException
   {
      if (sysDef == null || sharedDef == null)
         return null;

      try
      {
         String errMsg = null;
         Map<String, List<String>> errorsMap = new HashMap<>();

         PSItemDefManager mgr = PSItemDefManager.getInstance();
         for (long id : mgr.getContentTypeIds(PSItemDefManager.COMMUNITY_ANY))
         {
            PSItemDefinition itemDef = PSContentTypeHelper
               .loadItemDef(new PSGuid(PSTypeEnum.NODEDEF, id));

            PSContentEditor ce = itemDef.getContentEditor();
            PSContentEditorPipe pipe = (PSContentEditorPipe) ce.getPipe();
            PSContentEditorMapper mapper = pipe.getMapper();

            try
            {
               // try to merge, will throw for invalid overrides
               PSContentEditorMapper merged = mapper.getMergedMapper(sysDef,
                  sharedDef, true);

               // validate no duplicate fields have been implicitly introduced
               merged.validateSharedFieldDuplication(sharedDef, merged
                  .getSharedFieldIncludes());
            }
            catch (PSSystemValidationException e)
            {
               String msg = e.getLocalizedMessage();
               List<String> ctypes = errorsMap.computeIfAbsent(msg, k -> new ArrayList<>());
               ctypes.add(itemDef.getLabel());
            }
         }

         if (!errorsMap.isEmpty())
         {
            StringBuilder bldr = new StringBuilder();
            for (Map.Entry<String, List<String>> entry : errorsMap.entrySet())
            {
               // append error
               if (bldr.length() > 0)
                  bldr.append("; ");
               bldr.append(PSWebserviceErrors.createErrorMessage(
                  IPSWebserviceErrors.FAILED_SYS_SHARED_DEF_VALIDATION, entry
                     .getKey(), PSStringUtils.listToString(entry.getValue(),
                     ", ")));
            }

            errMsg = bldr.toString();
         }

         return errMsg;
      }
      catch (Exception e)
      {
         throwOperationError("Failed to validate the shared def: ", e);
         return null; // used to turn off compiling error.
      }
   }

   /**
    * Listener implementation to handle extending node def lock and updating
    * the version after a successful node def save.  Allows updating of lock
    * if node def is saved but the content editor app save fails.
    */
   private class PSSaveNodeDefListener implements IPSSaveNodeDefListener
   {
      /**
       * Current user session, never <code>null</code> or empty.
       */
      private final String mi_session;

      /**
       * Current user name, never <code>null</code> or empty.
       */
      private final String mi_user;

      /**
       * Guid of the node def being saved, never <code>null</code>.
       */
      private final IPSGuid mi_id;

      /**
       * Lock service to use, not <code>null</code>.
       */
      private final IPSObjectLockService mi_service;

      /**
       * Stores a lock exception if one is thrown during {@link #nodeDefSaved()}
       */
      PSLockException mi_saveLockException = null;

      /**
       * Private Ctor for internal use only
       * 
       * @param session The user session, assumed not <code>null</code> or 
       * empty.
       * @param user The current user name, assumed not <code>null</code> or 
       * empty.
       * @param id The node def id, assumed not <code>null</code>.
       * @param service The lock service to use, assumed not <code>null</code>.
       */
      private PSSaveNodeDefListener(String session, String user, IPSGuid id,
         IPSObjectLockService service)
      {
         mi_session = session;
         mi_user = user;
         mi_id = id;
         this.mi_service = service;
      }

      /**
       * Updates the lock info for the node def.  See 
       * {@link IPSSaveNodeDefListener#nodeDefSaved()} for more info.
       */
      public void nodeDefSaved()
      {
         // get new version and extend the lock with it
         PSNodeDefinition nodeDef = PSContentTypeHelper.findNodeDef(mi_id);
         if (nodeDef == null)
         {
            // we just saved, this is a bug
            throw new RuntimeException("Unable to locate nodeDef for id: "
               + mi_id.longValue());
         }

         try
         {
            mi_service.extendLock(mi_id, mi_session, mi_user, nodeDef
               .getVersion());
         }
         catch (PSLockException e)
         {
            mi_saveLockException = e;
         }
      }

      /**
       * Get the lock exception if one was thrown during {@link #nodeDefSaved()}
       * 
       * @return The exception, <code>null</code> if none was thrown.
       */
      public PSLockException getLockException()
      {
         return mi_saveLockException;
      }
   }

   /**
    * Get the current user's request.
    * 
    * @return The request, never <code>null</code>.
    * 
    * @throws IllegalStateException if the current thread has not had a request
    * initialized.
    */
   private PSRequest getRequest()
   {
      PSRequest request = (PSRequest) PSRequestInfo
         .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      
      if (request == null)
         throw new IllegalStateException(
            "No request initialized for the current thread");
      
      return request;
   }
   
   /**
    * Saves the specified keyword.
    * 
    * @param keyword the object contains the new keyword, assumed not
    * <code>null</code>.
    * @param version version to restore on the keyword before saving it.
    */
   private void saveKeyword(PSKeyword keyword, Integer version)
   {
         IPSContentService service = 
            PSContentServiceLocator.getContentService();

         try
         {
            IPSGuid id = keyword.getGUID();
            
            // Load the existing keyword and copy data into it
            PSKeyword dbkeyword = service.loadKeyword(id, null);

            // Copy data object into "live" object
            dbkeyword.copy(keyword);
                       
            keyword = dbkeyword;
         }
         catch (PSContentException e)
         {
            // No problem, new instance
         }
       
         // Restore version
         keyword.setVersion(null);
         keyword.setVersion(version);

         service.saveKeyword(keyword);
   }
   
   /**
    * The content manager, set by {@link #setContentMgr(IPSContentMgr)}, never
    * <code>null</code> after that.
    */
   private IPSContentMgr m_contentMgr;
   
}
