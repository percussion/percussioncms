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
package com.percussion.webservices.content.impl;

import static com.percussion.webservices.PSWebserviceUtils.getRequest;

import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

import com.percussion.cms.IPSConstants;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.handlers.PSRelationshipCommandHandler;
import com.percussion.cms.objectstore.PSAaRelationship;
import com.percussion.cms.objectstore.PSComponentSummaries;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.cms.objectstore.PSDisplayFormat;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSInvalidChildTypeException;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.PSItemChild;
import com.percussion.cms.objectstore.PSItemChildEntry;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.cms.objectstore.PSObjectAclEntry;
import com.percussion.cms.objectstore.PSObjectPermissions;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.PSRelationshipProcessorProxy;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.cms.objectstore.server.PSServerItem;
import com.percussion.data.IPSInternalResultHandler;
import com.percussion.data.PSExecutionData;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.design.objectstore.PSContentTypeHelper;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.error.PSException;
import com.percussion.i18n.PSLocale;
import com.percussion.search.IPSSearchResultRow;
import com.percussion.search.PSWSSearchResponse;
import com.percussion.search.objectstore.PSWSSearchRequest;
import com.percussion.security.PSSecurityToken;
import com.percussion.security.PSUserEntry;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestContext;
import com.percussion.server.PSServer;
import com.percussion.server.PSUserSession;
import com.percussion.server.webservices.PSContentDataHandler;
import com.percussion.server.webservices.PSFolderHandler;
import com.percussion.server.webservices.PSSearchHandler;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.server.webservices.PSWebServicesRequestHandler;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.content.IPSContentService;
import com.percussion.services.content.PSContentServiceLocator;
import com.percussion.services.content.data.PSAutoTranslation;
import com.percussion.services.content.data.PSContentTypeSummary;
import com.percussion.services.content.data.PSFolderProperty;
import com.percussion.services.content.data.PSItemStatus;
import com.percussion.services.content.data.PSItemSummary;
import com.percussion.services.content.data.PSKeyword;
import com.percussion.services.content.data.PSRevisions;
import com.percussion.services.content.data.PSSearchSummary;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.filter.IPSFilterService;
import com.percussion.services.filter.IPSItemFilter;
import com.percussion.services.filter.PSFilterException;
import com.percussion.services.filter.PSFilterServiceLocator;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.services.security.IPSBackEndRoleMgr;
import com.percussion.services.security.PSRoleMgrLocator;
import com.percussion.services.security.PSSecurityException;
import com.percussion.services.security.data.PSCommunity;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.services.system.IPSSystemService;
import com.percussion.services.system.PSSystemException;
import com.percussion.services.system.PSSystemServiceLocator;
import com.percussion.services.system.data.PSContentStatusHistory;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.services.workflow.data.PSAssignmentTypeEnum;
import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSTransition;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.share.dao.PSFolderPathUtils;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.exceptions.PSORMException;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.thread.PSThreadUtils;
import com.percussion.webservices.IPSWebserviceErrors;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.PSInvalidStateException;
import com.percussion.webservices.PSUnknownChildException;
import com.percussion.webservices.PSUnknownContentTypeException;
import com.percussion.webservices.PSWebserviceErrors;
import com.percussion.webservices.PSWebserviceUtils;
import com.percussion.webservices.content.IPSContentDesignWs;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import com.percussion.webservices.system.IPSSystemDesignWs;
import com.percussion.webservices.system.PSSystemWsLocator;
import com.percussion.webservices.ui.IPSUiDesignWs;
import com.percussion.webservices.ui.PSUiWsLocator;
import com.percussion.workflow.PSWorkFlowUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * The public content webservice implementations.
 */
@Component("sys_contentWs")
@Transactional(noRollbackFor = Exception.class)
public class PSContentWs extends PSContentBaseWs implements IPSContentWs
{
   // @see IPSContentWs#loadContentTypes(String)
   public List<PSContentTypeSummary> loadContentTypes(String name)
   {
      return PSContentTypeHelper.loadContentTypeSummaries(name);
   }

   // @see IPSContentWs#loadKeywords(String)
   public List<PSKeyword> loadKeywords(String name)
   {
      IPSContentService service = PSContentServiceLocator.getContentService();

      if (StringUtils.isBlank(name))
         name = "*";
      name = StringUtils.replaceChars(name, '*', '%');

      return service.findKeywordsByLabel(name, "label");
   }

   // @see IPSContentWs#loadLocales(String, String)
   public List<PSLocale> loadLocales(String code, String name)
   {
      if (name != null)
         name = StringUtils.replaceChars(name, '*', '%');
      IPSCmsObjectMgr mgr = PSCmsObjectMgrLocator.getObjectManager();

      return mgr.findLocales(code, name);
   }

   /**
    * Converts the list of summaries to a map of ids to names.
    *
    * @param sums The list to convert, assumed not <code>null</code>.
    * @return The map, never <code>null</code>.
    */
   private Map<Long, String> getNameMap(List<? extends IPSCatalogSummary> sums)
   {
      Map<Long, String> map = new HashMap<>();

      for (IPSCatalogSummary summary : sums)
      {
         map.put(summary.getGUID().longValue(), summary.getName());
      }

      return map;
   }

   // @see IPSContentWs#loadTranslationSettings()
   public List<PSAutoTranslation> loadTranslationSettings()
   {
      IPSContentService svc = PSContentServiceLocator.getContentService();

      List<PSAutoTranslation> ats = svc.loadAutoTranslations();

      // fill in names
      IPSWorkflowService service = PSWorkflowServiceLocator
         .getWorkflowService();
      Map<Long, String> wfMap = getNameMap(service.findWorkflowsByName(null));
      IPSBackEndRoleMgr roleMgr = PSRoleMgrLocator.getBackEndRoleManager();
      Map<Long, String> commMap = getNameMap(roleMgr
         .findCommunitiesByName(null));

      IPSContentDesignWs contentDesignWs = PSContentWsLocator
         .getContentDesignWebservice();
      Map<Long, String> ctMap = getNameMap(contentDesignWs
         .findContentTypes(null));

      for (PSAutoTranslation at : ats)
      {
         String wfName = wfMap.get(at.getWorkflowId());
         if (!StringUtils.isBlank(wfName))
            at.setWorkflowName(wfName);

         String commName = commMap.get(at.getCommunityId());
         if (!StringUtils.isBlank(commName))
            at.setCommunityName(commName);

         String ctName = ctMap.get(at.getContentTypeId());
         if (!StringUtils.isBlank(ctName))
            at.setContentTypeName(ctName);
      }

      return ats;
   }

   @Deprecated
   public void checkinItems(List<IPSGuid> ids, String comment,
      @SuppressWarnings("unused") String user)
      throws PSErrorsException
   {
      checkinItems(ids, comment, false);
   }

   // @see IPSContentWs#checkinItems(List, String, String)
   public void checkinItems(List<IPSGuid> ids, String comment)
      throws PSErrorsException
   {
      checkinItems(ids, comment, false);
   }

   public void checkinItems(List<IPSGuid> ids, String comment, boolean ignoreRevisionCheck)
   {
      PSWebserviceUtils.validateLegacyGuids(ids);

      PSErrorsException results = new PSErrorsException();
      for (IPSGuid id : ids)
      {
         PSLegacyGuid guid = (PSLegacyGuid) id;
         int contentId = guid.getContentId();
         try
         {
            PSComponentSummary summary = PSWebserviceUtils
               .getItemSummary(contentId);

            // ignore if not checked out
            if (StringUtils.isBlank(summary.getCheckoutUserName()))
               continue;

            if(!ignoreRevisionCheck)
               PSWebserviceUtils.handleRevision(guid, summary);
            checkInItem(contentId, comment);
            results.addResult(guid);
         }
         catch (PSErrorException e)
         {
            results.addError(guid, e);
         }
      }

      if (results.hasErrors())
         throw results;
   }

   @Deprecated
   public void checkoutItems(List<IPSGuid> ids, String comment,
      @SuppressWarnings("unused") String user)
      throws PSErrorsException
   {
      checkoutItems(ids, comment);
   }

   // @see IPSContentWs#checkoutItems(List, String, String, String)
   public void checkoutItems(List<IPSGuid> ids, String comment)
      throws PSErrorsException
   {
      PSWebserviceUtils.validateLegacyGuids(ids);

      PSErrorsException results = new PSErrorsException();
      for (IPSGuid id : ids)
      {
         PSLegacyGuid guid = (PSLegacyGuid) id;
         int contentId = guid.getContentId();
         try
         {
            PSComponentSummary summary = PSWebserviceUtils
               .getItemSummary(contentId);

            // ignore if checked out by the user
            if (PSWebserviceUtils.isItemCheckedOutToUser(summary))
               continue;

            PSWebserviceUtils.handleRevision(guid, summary);
            checkOutItem(contentId, comment);
            results.addResult(guid);
         }
         catch (PSErrorException e)
         {
            results.addError(guid, e);
         }
      }

      if (results.hasErrors())
         throw results;
   }

   @Deprecated
   public List<PSCoreItem> createItems(String contentType, int count,
      @SuppressWarnings({"unused"})
      String session, @SuppressWarnings("unused")
      String user) throws PSUnknownContentTypeException,
      PSErrorException
   {
      return createItems(contentType, count);
   }

   public List<PSCoreItem> createItems(String contentType, int count)
      throws PSUnknownContentTypeException, PSErrorException
   {
      if (StringUtils.isBlank(contentType))
         throw new IllegalArgumentException(
            "contentType cannot be null or empty");

      if (count <= 0)
         throw new IllegalArgumentException("count must be > 0");

      try
      {
         PSItemDefinition itemDef = getItemDefinition(contentType);

         PSSecurityToken securityToken = getRequest().getSecurityToken();
         List<PSCoreItem> items = new ArrayList<PSCoreItem>();
         for (int i = 0; i < count; i++)
         {
            /*
             * Use server item and call load after the construction to load
             * the default values.
             */
            PSServerItem item = new PSServerItem(itemDef);
            item.load(null, securityToken);

            items.add(item);
         }

         return items;
      }
      catch (PSInvalidContentTypeException e)
      {
         throw new PSUnknownContentTypeException(e.getErrorCode(), e
            .getMessage(), ExceptionUtils.getFullStackTrace(new Exception()));
      }
      catch (PSCmsException e)
      {
         // this should never happen
         throwUnexpectedError(e);
         return null; // never happen here, used to turn off compiling error
      }
   }

   @Deprecated
   public void deleteItems(List<IPSGuid> ids, @SuppressWarnings("unused")
   String session, @SuppressWarnings("unused") String user)
   throws PSErrorsException
   {
      deleteItems(ids);
   }

   public void deleteItems(List<IPSGuid> ids)
      throws PSErrorsException
   {
      PSWebserviceUtils.validateLegacyGuids(ids);

      PSRequest request = getNewRequest();
      PSErrorsException errors = new PSErrorsException();
      for (IPSGuid id : ids)
      {
         try
         {
            PSLegacyGuid guid = handleRevision((PSLegacyGuid) id);

            List<String> itemIds = new ArrayList<String>();
            itemIds.add(String.valueOf(guid.getContentId()));

            PSContentDataHandler.purgeItems(request, itemIds);
         }
         catch (PSException e)
         {
            int code = IPSWebserviceErrors.DELETE_FAILED;
            PSErrorException error = new PSErrorException(code,
               PSWebserviceErrors.createErrorMessage(code, PSCoreItem.class
                  .getName(), id.getUUID(), e.getLocalizedMessage()),
               ExceptionUtils.getFullStackTrace(e));

            errors.addError(id, error);
         }
         catch (PSErrorException e)
         {
            int code = IPSWebserviceErrors.DELETE_FAILED;
            PSErrorException error = new PSErrorException(code,
               PSWebserviceErrors.createErrorMessage(code, PSCoreItem.class
                  .getName(), id.getUUID(), e.getLocalizedMessage()),
               ExceptionUtils.getFullStackTrace(e));

            errors.addError(id, error);
         }
      }

      if (errors.hasErrors())
         throw errors;
   }

   @Deprecated
   public List<PSSearchSummary> findItems(PSWSSearchRequest search,
      boolean loadOperations, @SuppressWarnings("unused")
      String session, @SuppressWarnings("unused")
      String user)
      throws PSErrorException
   {
      return findItems(search, loadOperations);
   }

   public List<PSSearchSummary> findItems(PSWSSearchRequest search,
      boolean loadOperations)
      throws PSErrorException
   {
      if (search == null)
         throw new IllegalArgumentException("search cannot be null");

      try
      {
         PSSearchHandler searchHandler = new PSSearchHandler();
         PSWSSearchResponse response = searchHandler.search(getNewRequest(),
            search);

         List<PSSearchSummary> results = new ArrayList<PSSearchSummary>();
         String user = PSWebserviceUtils.getUserName();
         for (IPSSearchResultRow row : response.getRowList())
         {
            PSSearchSummary searchSummary = new PSSearchSummary(row);
            results.add(searchSummary);

            if (loadOperations)
            {
               IPSCmsObjectMgr mgr = PSCmsObjectMgrLocator.getObjectManager();
               PSComponentSummary component = mgr
                  .loadComponentSummary(((PSLegacyGuid) searchSummary.getGUID())
                     .getContentId());
               searchSummary.setOperations(getAllowedOperations(component,
                  user));
            }
         }

         return results;
      }
      catch (PSException e)
      {
         throwUnexpectedError(e);
         return null; // turn off compiling error
      }
   }

   public String[] findItemPaths(IPSGuid id) throws PSErrorException
   {
      PSWebserviceUtils.validateLegacyGuid(id);
      PSLocator locator = new PSLocator(((PSLegacyGuid) id).getContentId(), -1);
      try
      {
         String[] paths = getFolderProcessor().getItemPaths(locator);
         return paths;
      }
      catch (PSCmsException | PSNotFoundException e)
      {
         int code = IPSWebserviceErrors.FAILED_FIND_PATH_FROM_ID;
         PSErrorException error = new PSErrorException(code,
            PSWebserviceErrors.createErrorMessage(code, locator.getId(), e
               .getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
         ms_logger.error(error.getMessage(), e);

         throw error;
      }

   }

   public List<PSItemSummary> findItems(List<IPSGuid> ids, boolean loadOperations) throws PSErrorException
   {

      IPSCmsObjectMgr mgr = PSCmsObjectMgrLocator.getObjectManager();
      List<Integer> contentIds = new ArrayList<Integer>();
      for(IPSGuid id: ids)
      {
         PSWebserviceUtils.validateLegacyGuid(id);
         contentIds.add(((PSLegacyGuid) id).getContentId());
      }
      List<PSComponentSummary> components;
      try
      {
         components = mgr.loadComponentSummaries(contentIds);
      }
      catch (Exception e)
      {
         int code = IPSWebserviceErrors.LOAD_OBJECTS_ERROR;
         PSErrorException error = new PSErrorException(code,
            PSWebserviceErrors.createErrorMessage(code,  e
               .getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
         throw error;
      }
      PSComponentSummaries cs = new PSComponentSummaries();
      for(PSComponentSummary c : components)
      {
         cs.add(c);
      }
      return getItemSummaries(cs, loadOperations);

   }

   public List<Integer> findItemIdsByFolder(String folderPath) throws PSErrorException
   {
      List<Integer> results = null;

      PSServerFolderProcessor processor = getFolderProcessor();
      int folderId = getFolderIdFromPath(folderPath, false);

      if (folderId == -1)
         return results;

      PSLocator folderLoc = new PSLocator(folderId);
      results = new ArrayList<Integer>();
      try
      {
         Set<Integer> ids = processor.getChildIds(folderLoc, true);
         for (Integer id : ids)
         {
            if (!processor.isItemFolder(new PSLocator(id)))
               results.add(id);
         }

         return results;
      }
      catch (PSCmsException e)
      {
         int code = IPSWebserviceErrors.UNEXPECTED_ERROR;
         PSErrorException error =  new PSErrorException(code, PSWebserviceErrors.createErrorMessage(
               code, e.getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
         ms_logger.error(error.getMessage(), e);

         throw error;
      }
   }

   /**
    * Get the list of allowed operations for the supplied component and user.
    *
    * @param comp the component for which to get the allowed operations,
    *    not <code>null</code>.
    * @param user the user for which to get the allowed operations, not
    *    <code>null</code> or empty.
    * @return a list with all alloed operations, never <code>null</code>,
    *    may be empty.
    */
   @SuppressWarnings("unchecked")
   private List<PSItemSummary.OperationEnum> getAllowedOperations(
      PSComponentSummary comp, String user)
   {
      List<PSItemSummary.OperationEnum> operations = new ArrayList<PSItemSummary.OperationEnum>();

      if (comp.isFolder())
      {
         PSObjectPermissions permissions = comp.getPermissions();
         if (permissions.hasServerAdminAccess() || permissions.hasWriteAccess())
         {
            operations.add(PSItemSummary.OperationEnum.READ);
            operations.add(PSItemSummary.OperationEnum.WRITE);
         }
         else if (permissions.hasReadAccess())
         {
            operations.add(PSItemSummary.OperationEnum.READ);
         }
      }
      else
      {
         PSRequest req = getRequest();
         int assignmentType;
         try
         {
            IPSSystemService ssvc = PSSystemServiceLocator.getSystemService();
            IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
            IPSRequestContext ctx = new PSRequestContext(req);
            PSUserSession session = req.getSecurityToken().getUserSession();
            if (StringUtils.isBlank(user))
            {
               PSUserEntry[] entries = session.getAuthenticatedUserEntries();
               if (entries != null)
               {
                  PSUserEntry entry = entries[0];
                  if (entry != null)
                     user = entry.getName();
               }
            }
            List<PSAssignmentTypeEnum> types = ssvc.getContentAssignmentTypes(
                  Collections.singletonList(gmgr.makeGuid(comp
                        .getCurrentLocator())), user, ctx.getSubjectRoles(),
                  Integer.parseInt(session.getCommunityId(req, session
                        .getUserCurrentCommunity())));
            assignmentType = types.get(0).getValue();
         }
         catch (PSSystemException e)
         {
            ms_logger.error("Problems calculating assignment type" , e);
            assignmentType = PSAssignmentTypeEnum.NONE.getValue();
         }
         catch (NumberFormatException e)
         {
            ms_logger.error("Problems calculating assignment type" , e);
            assignmentType = PSAssignmentTypeEnum.NONE.getValue();
         }
         catch (PSInternalRequestCallException e)
         {
            ms_logger.error("Problems calculating assignment type" , e);
            assignmentType = PSAssignmentTypeEnum.NONE.getValue();
         }
         if (assignmentType == PSWorkFlowUtils.ASSIGNMENT_TYPE_ADMIN
            || assignmentType == PSWorkFlowUtils.ASSIGNMENT_TYPE_ASSIGNEE)
         {
            operations.add(PSItemSummary.OperationEnum.READ);

            if (StringUtils.isBlank(comp.getCheckoutUserName()))
            {
               // if the item has not been checked out
               operations.add(PSItemSummary.OperationEnum.TRANSITION);
               operations.add(PSItemSummary.OperationEnum.CHECKOUT);
            }
            else
            {
               if (PSWebserviceUtils.isItemCheckedOutToUser(comp))
               {
                  // if checked out by the same user
                  operations.add(PSItemSummary.OperationEnum.WRITE);
                  operations.add(PSItemSummary.OperationEnum.CHECKIN);
               }
               else if (assignmentType == PSWorkFlowUtils.ASSIGNMENT_TYPE_ADMIN)
               {
                  // if the user has admin access, allow force checkin
                  operations.add(PSItemSummary.OperationEnum.CHECKIN);
               }
            }
         }
         else if (assignmentType == PSWorkFlowUtils.ASSIGNMENT_TYPE_READER)
         {
            operations.add(PSItemSummary.OperationEnum.READ);
         }
      }

      if (operations.isEmpty())
         operations.add(PSItemSummary.OperationEnum.NONE);

      return operations;
   }

   @Deprecated
   public List<PSRevisions> findRevisions(List<IPSGuid> ids,
      @SuppressWarnings("unused") String session,
      @SuppressWarnings("unused") String user) throws PSErrorException
   {
      return findRevisions(ids);
   }

   public List<PSRevisions> findRevisions(List<IPSGuid> ids)
      throws PSErrorException
   {
      PSWebserviceUtils.validateLegacyGuids(ids);

      IPSSystemService sysSvc = PSSystemServiceLocator.getSystemService();

      List<PSRevisions> revisions = new ArrayList<PSRevisions>();
      for (IPSGuid id : ids)
      {
         PSLegacyGuid guid = (PSLegacyGuid) id;
         int contentId = guid.getContentId();

         List<PSContentStatusHistory> histList = sysSvc
            .findContentStatusHistory(guid);

         revisions.add(new PSRevisions(PSWebserviceUtils
            .getItemSummary(contentId), histList));
      }

      return revisions;
   }

   @Deprecated
   public List<String> getAssemblyUrls(List<IPSGuid> ids, String template,
      int context, String filter, String site, String folderPath,
      @SuppressWarnings("unused") String user)
      throws PSErrorException
   {
      return getAssemblyUrls(ids, template, context, filter, site, folderPath);
   }

   public List<String> getAssemblyUrls(List<IPSGuid> ids, String templateName,
      int context, String filterName, String site, String folderPath)
      throws PSErrorException
   {
      // validate parameters
      PSWebserviceUtils.validateLegacyGuids(ids);
      IPSAssemblyTemplate template = getTemplate(templateName);
      IPSItemFilter itemFilter = getItemFilter(filterName);
      IPSGuid folderId = null;
      if (StringUtils.isNotBlank(folderPath))
      {
         int id = getFolderIdFromPath(folderPath, true);
         folderId = new PSLegacyGuid(id, -1);
      }
      IPSGuid siteId = null;
      if (StringUtils.isNotBlank(site))
      {
         siteId = getSite(site).getGUID();
      }

      PSRequest req = getRequest();
      HttpServletRequest svletReq = req.getServletRequest();

      String host = svletReq.getServerName();
      String protocol = req.getProtocol();
      int port = svletReq.getServerPort();

      // construct the urls
      PSLegacyGuid itemId;
      String url;
      List<String> urls = new ArrayList<String>(ids.size());
      IPSPublisherService srv = PSPublisherServiceLocator.getPublisherService();
      for (IPSGuid id : ids)
      {
         itemId = getItemGuid((PSLegacyGuid) id, context);
         url = srv.constructAssemblyUrl(host, port, protocol, siteId, itemId,
            folderId, template, itemFilter, context, true);
         urls.add(url);
      }

      return urls;
   }

   /**
    * Gets the item GUID from the specified id and context.
    * If the specified id includes a revision (not <code>-1</code>), then
    * simple return the id; otherwise the revision of the assembly url will be
    * selected according to the specified context of the request. If the
    * specified context is <code>0</code> (preview context), then uses edit
    * revision if the item is checked out by the current user; otherwise
    * uses current revision. If the specified context is not <code>0</code>,
    * then uses public revision (if exists); otherwise uses current revision
    * (if public  revision does not exist)
    *
    * @param id the source id, which may not may not include revision,
    *    assumed not <code>null</code>.
    * @param context the context used to construct assembly URL. Assumed
    *    <code>0</code> for previewing.
    * @return the id described above, never <code>null</code>.
    *
    * @throws PSErrorException If the specified item does not exist.
    */
   private PSLegacyGuid getItemGuid(PSLegacyGuid id, int context)
      throws PSErrorException
   {
      if (id.getRevision() != -1)
         return id;

      if (context == 0) // previewing context
      {
         PSLocator locator = PSWebserviceUtils.getItemLocator(id);
         return new PSLegacyGuid(locator);
      }

      // handle non-previewing context
      PSComponentSummary summary = PSWebserviceUtils.getItemSummary(id
         .getContentId());
      return new PSLegacyGuid(id.getContentId(), summary
         .getPublicOrCurrentRevision());
   }

   /**
    * Gets a site with the specified site name.
    *
    * @param siteName the specified site name, assumed not <code>null</code> or
    *    empty.
    * @return the specified site, never <code>null</code>.
    * @throws IllegalArgumentException if cannot find the specified site.
    */
   private IPSSite getSite(String siteName)
   {
      IPSSiteManager sitemgr = PSSiteManagerLocator.getSiteManager();
      try
      {
         return sitemgr.loadSite(siteName);
      }
      catch (PSNotFoundException e)
      {
         e.printStackTrace();
         throw new IllegalArgumentException(
            "Failed to find a site with name = " + siteName
               + ", the underlying error was: " + e.getLocalizedMessage());
      }
   }

   /**
    * Gets a item filter with the specified name.
    *
    * @param filterName the name of the specified item filter, not
    *    <code>null</code> or empty.
    * @return the specified item filter, never <code>null</code>.
    * @throws IllegalArgumentException for invalid parameter or cannot find the
    *    specified item filter.
    */
   private IPSItemFilter getItemFilter(String filterName)
   {

      IPSFilterService srv = PSFilterServiceLocator.getFilterService();
      try
      {
         return srv.findFilterByName(filterName);
      }
      catch (PSFilterException e)
      {
         throw new IllegalArgumentException(
            "Cannot find item filter with name = " + filterName);
      }
   }

   /**
    * Gets a template with the specified name.
    *
    * @param templateName the name of the specified template, not
    *    <code>null</code> or empty.
    * @return the specified template, never <code>null</code>.
    * @throws IllegalArgumentException for invalid parameter or cannot find the
    *    specified template.
    */
   private IPSAssemblyTemplate getTemplate(String templateName)
   {
      if (StringUtils.isBlank(templateName))
         throw new IllegalArgumentException(
            "templateName may not be null or empty.");

      IPSAssemblyService asbSrv = PSAssemblyServiceLocator.getAssemblyService();
      try
      {
         return asbSrv.findTemplateByName(templateName);
      }
      catch (PSAssemblyException e)
      {
         throw new IllegalArgumentException(
            "Cannot find a template with name = " + templateName);
      }
   }

   @Deprecated
   public List<PSCoreItem> loadItems(List<IPSGuid> ids,
      boolean includeBinary, boolean includeChildren, boolean includeRelated,
      boolean includeFolderPath, @SuppressWarnings("unused")
      String session, @SuppressWarnings("unused") String user)
      throws PSErrorResultsException
   {
      return loadItems(ids, includeBinary, includeChildren, includeRelated,
         includeFolderPath);
   }

   public List<PSCoreItem> loadItems(List<IPSGuid> ids, boolean includeBinary,
      boolean includeChildren, boolean includeRelated,
      boolean includeFolderPath)
      throws PSErrorResultsException
   {
      return doLoadItems(ids, includeBinary, includeChildren, includeRelated,
         includeFolderPath, false, false, FOLDER_RELATE_TYPE);
   }

   public List<PSCoreItem> loadItems(List<IPSGuid> ids,
         boolean includeBinary, boolean includeChildren, boolean includeRelated,
         boolean includeFolderPath, boolean includeRelatedItem)
         throws PSErrorResultsException
   {
      return doLoadItems(ids, includeBinary, includeChildren, includeRelated,
            includeFolderPath, false, includeRelatedItem, FOLDER_RELATE_TYPE);
   }

   public List<PSCoreItem> loadItems(List<IPSGuid> ids,
                                     boolean includeBinary, boolean includeChildren, boolean includeRelated,
                                     boolean includeFolderPath, boolean includeRelatedItem, String relationshipTypeName)
           throws PSErrorResultsException
   {
      return doLoadItems(ids, includeBinary, includeChildren, includeRelated,
              includeFolderPath, false, includeRelatedItem, relationshipTypeName);
   }

   @Deprecated
   public List<PSCoreItem> newCopies(List<IPSGuid> ids, List<String> paths,
      String relationshipType, boolean enableRevisions,
      @SuppressWarnings("unused") String session,
      @SuppressWarnings("unused") String user)
      throws PSErrorResultsException, PSErrorException
   {
      return newCopies(ids, paths, relationshipType, enableRevisions);
   }

   @SuppressWarnings("unchecked")
   public List<PSCoreItem> newCopies(List<IPSGuid> ids, List<String> paths,
                                     String relationshipType, boolean enableRevisions)
           throws PSErrorResultsException, PSErrorException
   {
      // enableViewForceFuly ==true was created to fix the issue of ticket#6083
      return newCopies(ids,  paths, relationshipType, enableRevisions ,false);
   }

   @SuppressWarnings("unchecked")
   public List<PSCoreItem> newCopies(List<IPSGuid> ids, List<String> paths,
      String relationshipType, boolean enableRevisions , boolean enableViewForceFully)
      throws PSErrorResultsException, PSErrorException
   {
      PSWebserviceUtils.validateLegacyGuids(ids);

      paths = getMatchingPaths(paths, ids);

      if (!StringUtils.isBlank(relationshipType))
         validateRelationshipType(relationshipType,
            PSRelationshipConfig.CATEGORY_COPY);

      PSErrorResultsException results = new PSErrorResultsException();
      for (int i = 0; i < ids.size(); i++)
      {
         PSLegacyGuid id = new PSLegacyGuid(ids.get(i).longValue());
         String path = paths.get(i);

         try
         {
            PSItemDefinition def = getItemDefinition(getContentTypeId(id));

            PSLegacyGuid copyId = (PSLegacyGuid) createNewCopy(id,
               relationshipType, def.getInternalRequestResource());

            List<IPSGuid> copyIds = new ArrayList<IPSGuid>();
            copyIds.add(copyId);

            if (!StringUtils.isBlank(path))
            {
               addFolderTree(path);
               addFolderChildren(path, copyIds);
            }

            enableRevisions(copyId, enableRevisions);

            results.addResult(id, enableViewForceFully? loadNewlyItem(copyId) :loadItem(copyId));
         }
         catch (PSInvalidContentTypeException e)
         {
            int code = IPSWebserviceErrors.NEWCOPY_FAILED;
            PSErrorException error = new PSErrorException(code,
               PSWebserviceErrors.createErrorMessage(code, id.getUUID(), e
                  .getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
            results.addError(id, error);
         }
         catch (PSErrorException e)
         {
            int code = IPSWebserviceErrors.NEWCOPY_FAILED;
            PSErrorException error = new PSErrorException(code,
               PSWebserviceErrors.createErrorMessage(code, id.getUUID(), e
                  .getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
            results.addError(id, error);
         }
         catch (PSException e)
         {
            int code = IPSWebserviceErrors.NEWCOPY_FAILED;
            PSErrorException error = new PSErrorException(code,
               PSWebserviceErrors.createErrorMessage(code, id.getUUID(), e
                  .getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
            results.addError(id, error);
         }
         catch (PSORMException e)
         {
            int code = IPSWebserviceErrors.NEWCOPY_FAILED;
            PSErrorException error = new PSErrorException(code,
               PSWebserviceErrors.createErrorMessage(code, id.getUUID(), e
                  .getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
            results.addError(id, error);
         }
      }

      if (results.hasErrors())
         throw results;

      return results.getResults(ids);
   }


   /**
    * Gets a matching paths from the specified paths and ids.
    *
    * @param paths the source path. It must not be <code>null</code> or empty.
    *    It contains either one path or the same number of paths as the ids.
    * @param ids the ids; assumed not <code>null</code> or empty.
    * @return the same number of paths as the ids.
    * @throws IllegalArgumentException the paths is invalid.
    */
   private List<String> getMatchingPaths(List<String> paths, List<IPSGuid> ids)
   {
      if (paths == null || paths.isEmpty())
         throw new IllegalArgumentException("paths cannot be null or empty");

      // if there is only one path, then use it for all ids
      if (paths.size() == 1)
      {
         String path = paths.get(0);
         paths = new ArrayList<String>(ids.size());
         for (int i = 0; i < ids.size(); i++)
            paths.add(path);
      }

      if (ids.size() != paths.size())
         throw new IllegalArgumentException(
            "paths must be of the same size as ids");

      return paths;
   }

   @Deprecated
   public List<PSCoreItem> newPromotableVersions(List<IPSGuid> ids,
      List<String> paths, String relationshipType, boolean enableRevisions,
      @SuppressWarnings("unused") String session, @SuppressWarnings("unused")
      String user)
      throws PSErrorResultsException, PSErrorException
   {
      return newPromotableVersions(ids, paths, relationshipType,
         enableRevisions);
   }

   @SuppressWarnings("unchecked")
   public List<PSCoreItem> newPromotableVersions(List<IPSGuid> ids,
      List<String> paths, String relationshipType, boolean enableRevisions)
      throws PSErrorResultsException, PSErrorException
   {
      PSWebserviceUtils.validateLegacyGuids(ids);

      paths = getMatchingPaths(paths, ids);

      if (!StringUtils.isBlank(relationshipType))
         validateRelationshipType(relationshipType,
            PSRelationshipConfig.CATEGORY_PROMOTABLE);

      PSErrorResultsException results = new PSErrorResultsException();
      for (int i = 0; i < ids.size(); i++)
      {
         PSLegacyGuid id = new PSLegacyGuid(ids.get(i).longValue());
         String path = paths.get(i);

         try
         {
            PSItemDefinition def = getItemDefinition(getContentTypeId(id));

            PSLegacyGuid copyId = (PSLegacyGuid) createNewPromotableVersion(id,
               relationshipType, def.getInternalRequestResource());

            List<IPSGuid> copyIds = new ArrayList<IPSGuid>();
            copyIds.add(copyId);

            if (!StringUtils.isBlank(path))
            {
               addFolderTree(path);
               addFolderChildren(path, copyIds);
            }

            enableRevisions(copyId, enableRevisions);

            results.addResult(id, loadItem(copyId));
         }
         catch (PSInvalidContentTypeException e)
         {
            int code = IPSWebserviceErrors.NEWPROMOTABLEVERSION_FAILED;
            PSErrorException error = new PSErrorException(code,
               PSWebserviceErrors.createErrorMessage(code, id.getUUID(), e
                  .getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
            results.addError(id, error);
         }
         catch (PSErrorException e)
         {
            int code = IPSWebserviceErrors.NEWPROMOTABLEVERSION_FAILED;
            PSErrorException error = new PSErrorException(code,
               PSWebserviceErrors.createErrorMessage(code, id.getUUID(), e
                  .getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
            results.addError(id, error);
         }
         catch (PSException e)
         {
            int code = IPSWebserviceErrors.NEWPROMOTABLEVERSION_FAILED;
            PSErrorException error = new PSErrorException(code,
               PSWebserviceErrors.createErrorMessage(code, id.getUUID(), e
                  .getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
            results.addError(id, error);
         }
         catch (PSORMException e)
         {
            int code = IPSWebserviceErrors.NEWPROMOTABLEVERSION_FAILED;
            PSErrorException error = new PSErrorException(code,
               PSWebserviceErrors.createErrorMessage(code, id.getUUID(), e
                  .getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
            results.addError(id, error);
         }
      }

      if (results.hasErrors())
         throw results;

      return results.getResults(ids);
   }

   /**
    * Loads the specified item and resets its revisions.
    *
    * @param id the id of the item to load, assumed not <code>null</code>.
    * @return the loaded item with all its revisions set to -1, never
    *    <code>null</code>.
    * @throws PSErrorResultsException if the specified item could not be found.
    */
   private PSCoreItem loadItem(IPSGuid id)
      throws PSErrorResultsException
   {
      List<IPSGuid> ids = new ArrayList<IPSGuid>();
      ids.add(id);

      PSCoreItem item = doLoadItems(ids, false, false, false, false,
         false, false, FOLDER_RELATE_TYPE).get(0);
      item.resetRevisions();

      return item;
   }
    /**
     * Loads the specified item and resets its revisions.
     *
     * @param id the id of the item to load, assumed not <code>null</code>.
     * @return the loaded item with all its revisions set to -1, never
     *    <code>null</code>.
     * @throws PSErrorResultsException if the specified item could not be found.
     */
    private PSCoreItem loadNewlyItem(IPSGuid id)
            throws PSErrorResultsException
    {
        //created to fix ticket #6083
       List<IPSGuid> ids = new ArrayList<IPSGuid>();
       ids.add(id);

        PSCoreItem item = doLoadItems(ids, false, false, false, false,
                true, false, FOLDER_RELATE_TYPE).get(0);
        return item;
    }
   @Deprecated
   public List<PSCoreItem> newTranslations(List<IPSGuid> ids,
      List<PSAutoTranslation> translationSettings, String relationshipType,
      boolean enableRevisions, @SuppressWarnings("unused")
      String session, @SuppressWarnings("unused")
      String user)
      throws PSErrorResultsException, PSErrorException
   {
      return newTranslations(ids, translationSettings, relationshipType,
         enableRevisions);
   }

   @SuppressWarnings("unchecked")
   public List<PSCoreItem> newTranslations(List<IPSGuid> ids,
      List<PSAutoTranslation> translationSettings, String relationshipType,
      boolean enableRevisions)
      throws PSErrorResultsException, PSErrorException
   {
      final int errorCode = IPSWebserviceErrors.NEWTRANSLATION_FAILED;
      PSWebserviceUtils.validateLegacyGuids(ids);

      final List<PSComponentSummary> summaries = loadSummaries(ids);
      final boolean explicitLocales = translationSettings != null;
      final List<String> allLocaleCodes =
            getLocaleCodes(loadLocales(null, null));
      final List<String> localeCodes = explicitLocales
            ? getTranslationSettingsLocaleCodes(translationSettings)
            : allLocaleCodes;

      validateTranslationRequest(localeCodes, summaries, ids, explicitLocales,
            allLocaleCodes);

      if (!StringUtils.isBlank(relationshipType))
         validateRelationshipType(relationshipType,
            PSRelationshipConfig.CATEGORY_TRANSLATION);

      List<PSCoreItem> results = new ArrayList<PSCoreItem>();
      for (String localeCode : localeCodes)
      {
         PSErrorResultsException errorResults = new PSErrorResultsException();
         final List<IPSGuid> resultIds = new ArrayList<IPSGuid>();
         for (int i = 0; i < ids.size(); i++)
         {
            final IPSGuid originalId = ids.get(i);
            final PSComponentSummary summary = summaries.get(i);
            if (summary.getLocale().equals(localeCode))
            {
               assert !explicitLocales;
               // the request was to convert to all the locales,
               // except the item's one
               continue;
            }

            PSLegacyGuid id = new PSLegacyGuid(originalId.longValue());
            if (id.getRevision() == -1)
            {
               id = new PSLegacyGuid(id.getContentId(),
                     summary.getCurrentLocator().getRevision());
            }
            resultIds.add(id);

            try
            {
               PSItemDefinition def = getItemDefinition(getContentTypeId(id));

               PSLegacyGuid copyId = (PSLegacyGuid) createNewTranslation(id,
                     relationshipType, localeCode,
                     def.getInternalRequestResource());
               enableRevisions(copyId, enableRevisions);

               errorResults.addResult(id, loadItem(copyId));
            }
            catch (PSException e)
            {
               addToErrorResults(e, id, errorCode, errorResults);
            }
            catch (PSORMException e)
            {
               addToErrorResults(e, id, errorCode, errorResults);
            }
         }

         if (errorResults.hasErrors())
            throw errorResults;

         results.addAll(errorResults.getResults(resultIds));
      }

      return results;
   }

   /**
    * Adds the provided error to the error results.
    * @param e the exception to add. Assumed not <code>null</code>.
    * @param id the failed item id. Assumed not <code>null</code>.
    * @param code the error code to add.
    * @param errorResults the results to add to.
    * Assumed not <code>null</code>.
    */
   private void addToErrorResults(final Exception e,
         final PSLegacyGuid id, final int code,
         final PSErrorResultsException errorResults)
   {
      final PSErrorException error = new PSErrorException(code,
            PSWebserviceErrors.createErrorMessage(code, id.getUUID(),
               e.getLocalizedMessage()),
               ExceptionUtils.getFullStackTrace(e));
      errorResults.addError(id, error);
   }

   /**
    * Makes sure the translation request is valid.
    * Throws <code>IllegalArgumentException</code> if the data is invalid.
    * @param localeCodes the locales to validate. Assumed not <code>null</code>.
    * @param summaries the item summaries. Assumed not <code>null</code>.
    * @param ids the GUIDS of the ids the summaries.
    * Assumed not <code>null</code>, same length as the summaries list.
    * @param explicitLocales whether the locales were specified explicitely.
    * @param allLocaleCodes codes of all the system locales
    * @throws PSErrorException on an unexpected error while retrieving data.
    */
   private void validateTranslationRequest(final List<String> localeCodes,
         final List<PSComponentSummary> summaries,
         final List<IPSGuid> ids, final boolean explicitLocales,
         final List<String> allLocaleCodes)
      throws PSErrorException
   {
      assert summaries.size() == ids.size();
      if (localeCodes.isEmpty())
      {
         throw new IllegalArgumentException(
               "no locales to create translations for are available");
      }

      if (explicitLocales)
      {
         localesMustExist(localeCodes, allLocaleCodes);
         insureNoTranslationToSameLocale(localeCodes, summaries, ids);
      }

      insureNoTranslations(summaries, ids);
   }

   /**
    * Throws <code>IllegalArgumentException</code> if it finds any item
    * has a locale from the provided locale list.
    * @param localeCodes the locales to check.
    * Assumed not <code>null</code>.
    * @param summaries the items to check.
    * Assumed not <code>null</code>.
    * @param ids the item ids in the same order.
    * Assumed not <code>null</code>.
    */
   private void insureNoTranslationToSameLocale(final List<String> localeCodes,
         final List<PSComponentSummary> summaries, final List<IPSGuid> ids)
   {
      assert summaries.size() == ids.size();
      for (int i = 0; i < summaries.size(); i++)
      {
         final PSComponentSummary summary = summaries.get(i);
         if (localeCodes.contains(summary.getLocale()))
         {
            final IPSGuid id = ids.get(i);
            throw new IllegalArgumentException("Item " +
                  id.toString() + "(" + id.longValue()
                  + ") can't be translated to its own locale "
                  + summary.getLocale());
         }
      }
   }

   /**
    * Throws <code>IllegalArgumentException</code> if any of the provided items
    * are already translations.
    * @param summaries the items to check. Assumed not <code>null</code>.
    * @param ids the id values of the summaries in the same order.
    * Assumed not <code>null</code>.
    * @throws PSErrorException on data loading.
    */
   private void insureNoTranslations(final List<PSComponentSummary> summaries,
         final List<IPSGuid> ids) throws PSErrorException
   {
      assert summaries.size() == ids.size();

      final PSRelationshipProcessor processor =
            PSWebserviceUtils.getRelationshipProcessor();

      for (int i = 0; i < summaries.size(); i++)
      {
         final PSComponentSummary summary = summaries.get(i);
         final IPSGuid id = ids.get(i);
         final PSRelationshipFilter filter = new PSRelationshipFilter();
         filter.setDependent(summary.getCurrentLocator());
         filter.setCategory(PSRelationshipConfig.CATEGORY_TRANSLATION);
         final PSRelationshipSet owners;
         try
         {
            owners = processor.getRelationships(filter);
         }
         catch (PSCmsException e)
         {
            int code = IPSWebserviceErrors.NEWTRANSLATION_FAILED;
            throw new PSErrorException(code,
                  PSWebserviceErrors.createErrorMessage(code, id.getUUID(),
                        e.getLocalizedMessage()),
                        ExceptionUtils.getFullStackTrace(e));
         }
         if (owners != null && !owners.isEmpty())
         {
            throw new IllegalArgumentException(
               "Item " + id.toString() + "(" + id.longValue()
               + ") is already a translation of an existing item and "
               + "cannot be further translated");
         }
      }
   }

   /**
    * Throws <code>IllegalArgumentException</code> if any of the locales
    * does not exist.
    * @param localeCodes the locale codes to validate.
    * Assumed not <code>null</code>.
    * @param allLocaleCodes all existing locale codes.
    * Assumed not <code>null</code>.
    */
   private void localesMustExist(final List<String> localeCodes,
         final List<String> allLocaleCodes)
   {
      for (final String locale : localeCodes)
      {
         if (!allLocaleCodes.contains(locale))
         {
            throw new IllegalArgumentException(
                  "Invalid locale \"" + locale + "\" to translate to");
         }
      }
   }

   /**
    * Loads summaries for the provided ids.
    * @param ids the ids to load summaries for. Assumed not <code>null</code>.
    * @return the list of component summaries in the same order as their ids.
    * @throws PSErrorException if the specified item for any of the provided
    * ids does not exist.
    */
   private List<PSComponentSummary> loadSummaries(List<IPSGuid> ids)
         throws PSErrorException
   {
      final List<PSComponentSummary> summaries = new ArrayList<PSComponentSummary>();
      for (final IPSGuid originalId : ids)
      {
         summaries.add(PSWebserviceUtils.getItemSummary(originalId.getUUID()));
      }
      return summaries;
   }

   /**
    * Extracts locale codes from the provided translation settings.
    * @param translationSettings translation settings to extract locales from.
    * Assumed not <code>null</code>.
    * @return a list of locales extracted from the list of translation settings.
    * The locales are in the same position as the translation settings they are
    * extracted from.
    * Has the same number of entries as the translation settings list.
    * Not <code>null</code>.
    */
   private List<String> getTranslationSettingsLocaleCodes(
         final List<PSAutoTranslation> translationSettings)
   {
      final List<String> codes = new ArrayList<String>();
      for (final PSAutoTranslation translationSetting : translationSettings)
      {
         codes.add(translationSetting.getLocale());
      }
      assert codes.size() == translationSettings.size();
      return codes;
   }

   /**
    * Extracts locale codes from the provided locales.
    * @param locales locales to extract locale codes from.
    * Assumed not <code>null</code>.
    * @return a list of locale codes extracted from the list of locales.
    * The codes are in the same position as the locales they are
    * extracted from.
    * Has the same number of entries as the locales list.
    * Not <code>null</code>.
    */
   private List<String> getLocaleCodes(final List<PSLocale> locales)
   {
      final List<String> codes = new ArrayList<String>();
      for (final PSLocale locale : locales)
      {
         codes.add(locale.getLanguageString());
      }
      assert codes.size() == locales.size();
      return codes;
   }

   @Deprecated
   public List<PSItemStatus> prepareForEdit(List<IPSGuid> ids,
      @SuppressWarnings("unused") String user)
      throws PSErrorResultsException
   {
      return prepareForEdit(ids);
   }

   @SuppressWarnings("unchecked")
   public List<PSItemStatus> prepareForEdit(List<IPSGuid> ids)
      throws PSErrorResultsException
   {
      PSWebserviceUtils.validateLegacyGuids(ids);

      PSErrorResultsException results = new PSErrorResultsException();
      for (IPSGuid id : ids)
      {
         try
         {
            results.addResult(id, prepareForEdit(id));
         }
         catch (PSErrorException e)
         {
            results.addError(id, e);
         }
      }

      if (results.hasErrors())
         throw results;

      return results.getResults(ids);
   }


   /*
    * //see base interface method for details
    */
   public PSItemStatus prepareForEdit(IPSGuid itemId)
      throws PSErrorException
   {
      PSWebserviceUtils.validateLegacyGuid(itemId);
      int id = ((PSLegacyGuid) itemId).getContentId();

      PSComponentSummary summary = PSWebserviceUtils.getItemSummary(id);
      PSItemStatus itemStatus = new PSItemStatus(id);
      long fromStateId = summary.getContentStateId();
      itemStatus.setFromStateId(fromStateId);

      if (ms_logger.isDebugEnabled())
      {
         ms_logger.debug("prepareForEdit() id=" + id + ", workflowId="
               + summary.getWorkflowAppId() + ", currentStateId="
               + summary.getContentStateId());
      }

      transitionItemIfNeeded(itemStatus, summary);

      // handle checkout if needed
      if (PSWebserviceUtils.isItemCheckedOutToUser(summary))
      {
         itemStatus.setDidCheckout(false);
      }
      else
      {
         checkOutItem(summary.getContentId(), null);
         itemStatus.setDidCheckout(true);
      }

      if (ms_logger.isDebugEnabled())
      {
         ms_logger.debug("prepareForEdit() itemStatus=" + itemStatus.toString());
      }

      return itemStatus;
   }

   /**
    * Performs workflow transitions for the specified item from public state
    * (valid-flag is 'y') to quick-edit state (valid-flag is 'i') if the
    * specified item is in public state .
    *
    * @param itemStatus used to record the result of the possible transition,
    *    assumed not <code>null</code>.
    * @param summary the summary of the specified item, assumed not
    *    <code>null</code>.
    * @throws PSErrorException if an error occurs.
    */
   private void transitionItemIfNeeded(PSItemStatus itemStatus,
      PSComponentSummary summary) throws PSErrorException
   {
      PSWorkflow wf = PSWebserviceUtils.getWorkflow(summary.getWorkflowAppId());

      // get current state
      int stateId = summary.getContentStateId();
      PSState currState = PSWebserviceUtils.getStateById(wf, stateId);
      if (currState == null)
      {
         int errorCode = IPSWebserviceErrors.CANNOT_FIND_WORKFLOW_STATE_ID;
         PSErrorException error = new PSErrorException(errorCode,
            PSWebserviceErrors.createErrorMessage(errorCode, stateId, wf
               .getGUID().longValue(), wf.getName()), ExceptionUtils
               .getFullStackTrace(new Exception()));
         throw error;
      }

      if (ms_logger.isDebugEnabled())
      {
         ms_logger.debug("workflowId=" + summary.getWorkflowAppId()
               + ", stateId=" + stateId + ", isPublishable="
               + currState.isPublishable());
      }

      // perform transition if needed
      if (currState.isPublishable())
      {
         PSTransition trans = getTransitionToQuickEdit(currState, wf);
         PSWebserviceUtils.transitionItem(summary.getContentId(), trans
            .getTrigger(), null, null);

         PSState toState = PSWebserviceUtils.getStateById(wf, trans
            .getToState());

         itemStatus.setFromStateId(currState.getStateId());
         itemStatus.setFromState(currState.getName());
         itemStatus.setToStateId(toState.getStateId());
         itemStatus.setToState(toState.getName());
         itemStatus.setDidTransition(true);
      }
      else
      {
         itemStatus.setDidTransition(false);
      }
   }

   /**
    * Find a transition where its from-state is the specified public state
    * and its to-state is a quick-edit state (its valid-flag is 'i').
    *
    * @param pubState the public state, assumed not <code>null</code>.
    * @param wf the workflow contains all searchable states, assumed not
    *    <code>null</code>.
    * @return the quick-edit state, never <code>null</code>.
    * @throws PSErrorException if failed to find a quick-edit state.
    */
   private PSTransition getTransitionToQuickEdit(PSState pubState, PSWorkflow wf)
      throws PSErrorException
   {
      for (PSTransition t : pubState.getTransitions())
      {
         PSState toState = PSWebserviceUtils.getStateById(wf, t.getToState());
         if (toState.getContentValidValue().equalsIgnoreCase("i"))
         {
            return t;
         }
      }

      int errorCode = IPSWebserviceErrors.CANNOT_FIND_TRANS_TO_QE_STATE;
      PSErrorException error = new PSErrorException(errorCode,
         PSWebserviceErrors.createErrorMessage(errorCode,
            pubState.getStateId(), pubState.getName(),
            wf.getGUID().longValue(), wf.getName()), ExceptionUtils
            .getFullStackTrace(new Exception()));
      throw error;
   }

   /**
    * Check int the specified item.
    *
    * @param id the item id.
    * @param comment The comment, may be <code>null</code> or empty for no
    *    comment.
    * @throws PSErrorException if failed to check in the specified item.
    */
   private void checkInItem(int id, String comment) throws PSErrorException
   {
      checkInOutItem(id, -1, false, comment);
   }

   /**
    * Check out the current revision of the specified item.
    *
    * @param id the item id.
    * @param comment The comment, may be <code>null</code> or empty for no
    *    comment.
    * @throws PSErrorException if failed to check out of the specified item.
    */
   private void checkOutItem(int id, String comment) throws PSErrorException
   {
      checkInOutItem(id, -1, true, comment);
   }

   /**
    * Check in or out the specified item.
    *
    * @param id the item id.
    * @param revision the revision of the item. It may be <code>-1</code> if
    *    the revision of the item is ignored for this operation.
    * @param isCheckout <code>true</code> if performing check out; otherwise
    *    performing check in.
    * @param comment The comment, may be <code>null</code> or empty for no
    * comment.
    * @throws PSErrorException if failed to check in/out of the specified item.
    */
   private void checkInOutItem(int id, int revision, boolean isCheckout,
      String comment) throws PSErrorException
   {
      PSWebServicesRequestHandler ws = PSWebServicesRequestHandler
         .getInstance();

      PSRequest req = getRequest();

      // get request and reset parameter left overs from previous calls
      HashMap oldParams = req.getParameters();
      req.setParameters(new HashMap());

      try
      {
         req
            .setParameter(IPSHtmlParameters.SYS_CONTENTID, Integer.toString(id));
         if (revision != -1)
         {
            req.setParameter(IPSHtmlParameters.SYS_REVISION, Integer
               .toString(revision));
         }

         if (!StringUtils.isBlank(comment))
         {
            PSWorkFlowUtils.setTransitionCommentInHTMLParams(comment, req
               .getParameters());
         }

         if (isCheckout)
            ws.executeCheckInOut(req, IPSConstants.TRIGGER_CHECKOUT);
         else
            ws.executeCheckInOut(req, IPSConstants.TRIGGER_CHECKIN);
      }
      catch (PSException e)
      {
         int errorCode;
         if (isCheckout)
            errorCode = IPSWebserviceErrors.FAILED_CHECK_OUT_ITEM;
         else
            errorCode = IPSWebserviceErrors.FAILED_CHECK_IN_ITEM;
         String message = PSWebserviceErrors.createErrorMessage(errorCode, id, e.getLocalizedMessage());
         String stack = ExceptionUtils.getFullStackTrace(e);
         PSErrorException error = new PSErrorException(errorCode, message, stack, e);
         throw error;
      }
      finally
      {
         req.setParameters(oldParams);
      }
   }

   @SuppressWarnings("unused")
   @Deprecated
   public void promoteRevisions(List<IPSGuid> ids, String session, String user)
      throws PSErrorsException, PSErrorException
   {
      promoteRevisions(ids);
   }

   public void promoteRevisions(List<IPSGuid> ids)
      throws PSErrorsException
   {
      PSWebserviceUtils.validateLegacyGuids(ids);

      // validates revision of the ids
      for (IPSGuid id : ids)
      {
         PSLegacyGuid guid = (PSLegacyGuid) id;
         if (guid.getRevision() == -1)
         {
            throw new IllegalArgumentException("ids must specify revision");
         }
      }

      String method = "promoteRevisions";

      PSErrorsException results = new PSErrorsException();
      for (IPSGuid id : ids)
      {
         PSLegacyGuid guid = (PSLegacyGuid) id;
         int contentId = guid.getContentId();
         int revision = guid.getRevision();

         try
         {
            // ensure checked in
            PSComponentSummary sum = PSWebserviceUtils
               .getItemSummary(contentId);
            if (!StringUtils.isBlank(sum.getCheckoutUserName()))
            {
               int code = IPSWebserviceErrors.ITEM_NOT_CHECKED_IN;
               results.addError(id,
                  new PSErrorException(code, PSWebserviceErrors
                     .createErrorMessage(code, guid.longValue(), method),
                     ExceptionUtils.getFullStackTrace(new Exception())));
            }

            // ensure non-public
            PSWorkflow wf = PSWebserviceUtils.getWorkflow(sum
               .getWorkflowAppId());
            PSState currState = PSWebserviceUtils.getStateById(wf, sum
               .getContentStateId());
            if (currState.isPublishable())
            {
               int code = IPSWebserviceErrors.INAVLID_ACTION_FOR_STATE;
               results.addError(id, new PSErrorException(code,
                  PSWebserviceErrors.createErrorMessage(code, guid.longValue(),
                     wf.getName(), currState.getName()), ExceptionUtils
                     .getFullStackTrace(new Exception())));
            }

            // checkout the revision
            checkInOutItem(contentId, revision, true, null);
            results.addResult(id);
         }
         catch (PSErrorException e)
         {
            results.addError(id, e);
         }
      }

      if (results.hasErrors())
         throw results;
   }

   // @see IPSContentWs#releaseFromEdit(List, boolean, String, String)
   public void releaseFromEdit(List<PSItemStatus> status, boolean checkinOnly)
      throws PSErrorsException
   {
      PSErrorsException results = new PSErrorsException();
      for (PSItemStatus s : status)
      {
         try
         {
            releaseFromEdit(s, checkinOnly);
         }
         catch (PSErrorException e)
         {
            results.addError(new PSLegacyGuid(s.getId(), -1), e);
         }
      }

      if (results.hasErrors())
         throw results;
   }

   /*
    * //see base interface method for details
    */
   public void releaseFromEdit(PSItemStatus itemStatus, boolean checkinOnly)
      throws PSErrorException
   {
      notNull(itemStatus, "itemStatus may not be null");
      if (ms_logger.isDebugEnabled())
      {
         ms_logger.debug("releaseFromEdit() itemStatus=" + itemStatus.toString());
      }

      int id = itemStatus.getId();
      if (itemStatus.isDidCheckout())
      {
         checkInItem(id, null);
      }
      if (itemStatus.isDidTransition() && (!checkinOnly))
      {
         PSComponentSummary summary = PSWebserviceUtils
            .getItemSummary(id);

         if (ms_logger.isDebugEnabled())
         {
            ms_logger.debug("releaseFromEdit() id=" + id
                  + ", workflowId=" + summary.getWorkflowAppId()
                  + ", currentStateId=" + summary.getContentStateId());
         }

         if (!validateItemState(summary, itemStatus))
         {
            return;
         }

         PSWorkflow wf = PSWebserviceUtils.getWorkflow(summary
            .getWorkflowAppId());
         PSState toState = PSWebserviceUtils.getStateById(wf, itemStatus
            .getFromStateId());
         PSState fromState = PSWebserviceUtils.getStateById(wf, itemStatus
            .getToStateId());
         PSTransition trans = getTransition(fromState, toState, wf);

         if (ms_logger.isDebugEnabled())
         {
            ms_logger.debug("releaseFromEdit() transition="
                  + trans.toString());
         }

         PSWebserviceUtils.transitionItem(summary.getContentId(), trans
            .getTrigger(), null, null);
      }
   }

   /**
    * Determines if the current state of the specified item is the to-state of
    * the specified item-status.
    *
    * @param item the item in question, assumed not <code>null</code>.
    * @param itemStatus the item status in question, assumed not <code>null</code>.
    * <code>null</code>.
    *
    * @return <code>true</code> if current state of the item is the same as
    * the to-state of the item-status; otherwise return <code>false</code>.
    *
    * @throw PSErrorException if current state of the item is not the same
    * as the from state of the item-status.
    */
   private boolean validateItemState(PSComponentSummary item,
         PSItemStatus itemStatus)
   {
      if (item.getContentStateId() == itemStatus.getToStateId())
      {
         return true;
      }

      PSWorkflow wf = PSWebserviceUtils.getWorkflow(item.getWorkflowAppId());
      PSState toState = PSWebserviceUtils.getStateById(wf, itemStatus.getFromStateId());

      PSState currState = PSWebserviceUtils.getStateById(wf, item
            .getContentStateId());
      int errorCode = IPSWebserviceErrors.CURR_STATE_NOT_MATCH;
      PSErrorException error = new PSErrorException(errorCode,
            PSWebserviceErrors.createErrorMessage(errorCode, currState
                  .getStateId(), currState.getName(), toState.getStateId(),
                  toState.getName()), ExceptionUtils
                  .getFullStackTrace(new Exception()));
      throw error;
   }

   /**
    * Finds a transition contains the specified from-state and the specified
    * to-state. If cannot find such transition, then lookup a transition that
    * leads to a state with the same "content-valid" flag as the to-state.
    *
    * @param fromState the from-state, assumed not <code>null</code>.
    * @param toState the to-state, assumed not <code>null</code>.
    * @param wf the workflow that contains all transitions and states, assumed
    *            not <code>null</code>.
    * @return the specified transition, never <code>null</code>.
    * @throws PSErrorException if cannot find the specified transition.
    */
   private PSTransition getTransition(PSState fromState, PSState toState,
      PSWorkflow wf) throws PSErrorException
   {
      // find a transition: fromState -> toState
      long toStateId = toState.getStateId();
      for (PSTransition t : fromState.getTransitions())
      {
         if (t.getToState() == toStateId)
         {
            return t;
         }
      }

      // find a transition: to a state that has the same flag as the "toState"
      String contentFlag = toState.getContentValidValue();
      for (PSTransition t : fromState.getTransitions())
      {
         PSState state = PSWebserviceUtils.getStateById(wf, t.getToState());
         if (state.getContentValidValue().equals(contentFlag))
         {
            return t;
         }
      }

      int errorCode = IPSWebserviceErrors.CANNOT_FIND_TRANS_4_STATE_2_STATE;
      PSErrorException error = new PSErrorException(errorCode,
         PSWebserviceErrors.createErrorMessage(errorCode, fromState
            .getStateId(), fromState.getName(), toState.getStateId(), toState
            .getName(), wf.getGUID().longValue(), wf.getName()), ExceptionUtils
            .getFullStackTrace(new Exception()));
      throw error;
   }

   @Deprecated
   public List<IPSGuid> saveItems(List<PSCoreItem> items,
      boolean enableRevisions, boolean checkin, @SuppressWarnings("unused")
      String session, @SuppressWarnings("unused") String user)
      throws PSErrorResultsException
   {
      return saveItems(items, enableRevisions, checkin);
   }

   public List<IPSGuid> saveItems(List<PSCoreItem> items,
         boolean enableRevisions, boolean checkin)
         throws PSErrorResultsException
   {
      if (items == null || items.isEmpty())
         throw new IllegalArgumentException("items cannot be null or empty");

      return saveItems(items, enableRevisions, checkin, null);
   }

   public List<IPSGuid> saveItems(List<PSCoreItem> items,
      boolean enableRevisions, boolean checkin, IPSGuid folderId)
      throws PSErrorResultsException
   {
      return saveItems(items, enableRevisions, checkin, folderId, FOLDER_RELATE_TYPE);
   }

   public List<IPSGuid> saveItems(List<PSCoreItem> items,
                                  boolean enableRevisions, boolean checkin, IPSGuid folderId,
                                  String relationshipTypeName)
           throws PSErrorResultsException
   {
      if (items == null || items.isEmpty())
         throw new IllegalArgumentException("items cannot be null or empty");

      PSSearchHandler searchHandler = new PSSearchHandler();

      PSErrorResultsException results = new PSErrorResultsException();
      PSLegacyGuid guid = null;
      for (PSCoreItem item : items)
      {
         try
         {
            // create new request for each iteration
            PSRequest request = getNewRequest();

            if (folderId != null)
            {
               // add the folder id to the request
               request.setParameter(IPSHtmlParameters.SYS_FOLDERID,
                       String.valueOf(folderId.getUUID()));
            }

            if (StringUtils.isBlank(request.getParameter(IPSHtmlParameters.SYS_CONTENTTYPEID)))
            {
               // add the content type id if it's not already there
               request.setParameter(IPSHtmlParameters.SYS_CONTENTTYPEID, item.getContentTypeId());
            }

            guid = new PSLegacyGuid(item.getContentId(), item.getRevision());

            boolean isInsert = guid.getContentId() == -1;
            if (!isInsert && enableRevisions)
               guid = handleRevision(guid);

            PSServerItem serverItem = new PSServerItem(item.getItemDefinition());

            // load the existing item for updates
            if (!isInsert)
               serverItem.load(guid.getLocator(), request.getSecurityToken());

            // merge the new data into the current item
            serverItem.loadData(item);

            // process any keyfield searches
            searchHandler.executeKeyFieldSearch(request, serverItem);

            // insert or update the item
            serverItem.save(request);

            // refresh the guid from the saved item
            guid = new PSLegacyGuid(serverItem.getContentId(), -1);

            // process folders if supplied
            List<String> paths = item.getFolderPaths();
            if (paths != null)
            {
               processFoldersForItem(guid, paths, relationshipTypeName);
            }

            // enable revisions if requested
            enableRevisions(guid, enableRevisions);

            if (checkin)
               checkInItem(guid.getContentId(), null);

            results.addResult(guid, serverItem);
         }
         catch (PSException e)
         {
            int code = IPSWebserviceErrors.FAILED_SAVE_ITEM;
            PSErrorException error = new PSErrorException(code,
                    PSWebserviceErrors.createErrorMessage(code, guid.getUUID(), e
                            .getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e),e);
            results.addError(guid, error);
         }
         catch (PSORMException e)
         {
            int code = IPSWebserviceErrors.FAILED_SAVE_ITEM;
            PSErrorException error = new PSErrorException(code,
                    PSWebserviceErrors.createErrorMessage(code, guid.getUUID(), e
                            .getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e),e);
            results.addError(guid, error);
         }
         catch (PSErrorsException e)
         {
            int code = IPSWebserviceErrors.FAILED_SAVE_ITEM;
            PSErrorException error = new PSErrorException(code,
                    PSWebserviceErrors.createErrorMessage(code, guid.getUUID(), e
                            .getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e),e);
            results.addError(guid, error);
         }
         catch (PSErrorException e)
         {
            results.addError(guid, e);
         }
      }

      if (results.hasErrors())
         throw results;

      return results.getIds();
   }

   /**
    * Walks a list of paths for an item and adds the item to the paths.
    * If the item was in paths no longer in the list it is removed from
    * those paths.
    *
    * This method is Synchronised to prevent two threads from creating
    * the same directory if it does not exist.
    *
    * @param guid - guid of the item may be <code>null</code>
    * @param paths - list of paths may be <code>null</code> or empty.
    * @throws PSErrorException
    * @throws PSErrorResultsException
    * @throws PSErrorsException
    */
   private synchronized void processFoldersForItem(PSLegacyGuid guid,
         List<String> paths, String relationshipTypeName)
      throws PSErrorException, PSErrorResultsException, PSErrorsException
   {
      if (guid == null)
         return;

      if (paths == null || paths.isEmpty())
         return;

      List<IPSGuid> guids = new ArrayList<IPSGuid>();
      guids.add(guid);

      List<String> existingPaths = new ArrayList<String>();
      for (String existingPath : findFolderPaths(guid, relationshipTypeName))
         existingPaths.add(existingPath);

      // handle new paths
      for (String path : paths)
      {
         if (!existingPaths.contains(path))
         {
            addFolderTree(path);
            addFolderChildren(path, guids);
         }

         existingPaths.remove(path);
      }

      // handle removed paths
      for (String path : existingPaths)
         removeFolderChildren(path, guids, false);
   }

   /**
    * Enables the revisions for the supplied item is so requested.
    *
    * @param guid the id of the item for which to enable the revisions,
    *    assumed not <code>null</code>.
    * @param enable <code>true</code> to enable the revisions,
    *    <code>false</code> otherwise.
    * @throws PSORMException if it fails to enable the revisions for the
    *    identified item.
    */
   private void enableRevisions(PSLegacyGuid guid, boolean enable)
      throws PSORMException
   {
      if (enable)
      {
         List<Integer> ids = new ArrayList<Integer>();
         ids.add(guid.getContentId());

         IPSCmsObjectMgr mgr = PSCmsObjectMgrLocator.getObjectManager();
         List<PSComponentSummary> summaries = mgr.loadComponentSummaries(ids);
         for (PSComponentSummary summary : summaries)
            summary.setRevisionLock(true);

         mgr.saveComponentSummaries(summaries);
      }
   }

   /**
    * Get the content type id for the supplied item.
    *
    * @param guid the item id for which to get the content type id, assumed
    *    not <code>null</code>.
    * @return the content type id.
    * @throws IllegalArgumentException if the supplied id is invalid.
    */
   private long getContentTypeId(PSLegacyGuid guid)
   {
      List<Integer> ids = new ArrayList<Integer>();
      ids.add(guid.getContentId());

      IPSCmsObjectMgr mgr = PSCmsObjectMgrLocator.getObjectManager();
      List<PSComponentSummary> summaries = mgr.loadComponentSummaries(ids);
      if (summaries != null && summaries.size() > 0)
         return summaries.get(0).getContentTypeId();

      throw new IllegalArgumentException("an invalid content id was supplied");
   }

   @Deprecated
   public List<PSCoreItem> viewItems(List<IPSGuid> ids, boolean includeBinary,
      boolean includeChildren, boolean includeRelated,
      boolean includeFolderPath, @SuppressWarnings("unused") String session,
      @SuppressWarnings("unused") String user)
      throws PSErrorResultsException
   {
      return viewItems(ids, includeBinary, includeChildren, includeRelated,
         includeFolderPath);
   }

   public List<PSCoreItem> viewItems(List<IPSGuid> ids, boolean includeBinary,
      boolean includeChildren, boolean includeRelated,
      boolean includeFolderPath)
      throws PSErrorResultsException
   {
      return doLoadItems(ids, includeBinary, includeChildren, includeRelated,
         includeFolderPath, true, false, FOLDER_RELATE_TYPE);
   }

   /**
    * Convenience method for {@link #loadItems(List, boolean, boolean, boolean,
    * boolean)} or {@link #viewItems(List, boolean, boolean,
    * boolean, boolean)} depending on the extra flag.
    *
    * @param isView <code>true</code> to return item views, <code>false</code>
    *    to return loaded items.
    * @param includeRelatedItem if both <code>includeRelated</code> and this
    * is <code>true</code>, then load both the relationships as well as the
    * related items; otherwise the related items will not be loaded.
    */
   @SuppressWarnings("unchecked")
   private List<PSCoreItem> doLoadItems(List<IPSGuid> ids, boolean includeBinary,
      boolean includeChildren, boolean includeRelated, boolean includeFolderPath, boolean isView, boolean includeRelatedItem, String relationshipTypeName)
      throws PSErrorResultsException
   {
      PSWebserviceUtils.validateLegacyGuids(ids);

      PSRequest request = getNewRequest();

      PSErrorResultsException results = new PSErrorResultsException();
      for (IPSGuid id : ids)
      {
         try
         {
            PSLegacyGuid guid = (PSLegacyGuid) id;
            if (!isView)
               guid = handleRevision(guid);
            else if (guid.getRevision() == -1)
               guid = handleRevision(guid);

            PSItemDefinition def = getItemDefinition(getContentTypeId(guid));

            PSServerItem serverItem = new PSServerItem(def);
            serverItem.load(guid.getLocator(), request, includeBinary,
               includeChildren, includeRelated, includeRelatedItem);

            if (includeChildren)
            {
               // default child entry action to "update"
               Iterator<PSItemChild> children = serverItem.getAllChildren();
               while (children.hasNext())
               {
                  PSItemChild child = children.next();

                  Iterator<PSItemChildEntry> entries = child.getAllEntries();
                  while (entries.hasNext())
                  {
                     PSItemChildEntry entry = entries.next();
                     entry.setAction(PSItemChildEntry.CHILD_ACTION_UPDATE);
                  }
               }
            }

            if (includeFolderPath)
               serverItem.setFolderPaths(Arrays.asList(findFolderPaths(guid, relationshipTypeName)));

            results.addResult(id, serverItem);
         }
         catch (PSException e)
         {
            int code = isView ? IPSWebserviceErrors.FAILED_VIEW_ITEM
               : IPSWebserviceErrors.FAILED_LOAD_ITEM;
            PSErrorException error = new PSErrorException(code,
               PSWebserviceErrors.createErrorMessage(code, id.getUUID(), e
                  .getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
            results.addError(id, error);
         }
         catch (PSErrorException e)
         {
            int code = isView ? IPSWebserviceErrors.FAILED_VIEW_ITEM
               : IPSWebserviceErrors.FAILED_LOAD_ITEM;
            PSErrorException error = new PSErrorException(code,
               PSWebserviceErrors.createErrorMessage(code, id.getUUID(), e
                  .getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
            results.addError(id, error);
         }
      }

      if (results.hasErrors())
         throw results;

      return results.getResults(ids);
   }

   @Deprecated
   public List<PSItemChildEntry> createChildEntries(IPSGuid id, String name,
      int count, @SuppressWarnings("unused")
      String session, @SuppressWarnings("unused")
      String user) throws PSUnknownChildException,
      PSInvalidStateException, PSErrorException
   {
      return createChildEntries(id, name, count);
   }

   public List<PSItemChildEntry> createChildEntries(IPSGuid id, String name,
      int count) throws PSUnknownChildException,
      PSInvalidStateException, PSErrorException
   {
      PSWebserviceUtils.validateLegacyGuid(id);
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");
      if (count <= 0)
         throw new IllegalArgumentException("count must be greater than zero");

      List<PSItemChildEntry> results = new ArrayList<PSItemChildEntry>(count);

      PSLegacyGuid lguid = (PSLegacyGuid) id;

      // validate/update revision
      try
      {
         lguid = handleRevision(lguid);
      }
      catch (PSErrorException er)
      {
         throwUnexpectedError(er);
      }

      PSRequest req = getRequest();
      PSLocator loc = lguid.getLocator();
      PSServerItem parent;
      try
      {
         parent = PSServerItem.loadItem(loc, req, PSServerItem.TYPE_FIELDS);
      }
      catch (Exception e)
      {
         PSErrorException er = handleLoadFailed(id, e);
         throwUnexpectedError(er);
         return null; // never happen here, used to turn off compiling error
      }

      ensureCheckedOut(parent, "createChildEntries");

      PSItemChildEntry entry = null;
      try
      {
         // this way we get the field defaults
         entry = parent.createChildEntry(req, name);
         entry.setAction(PSItemChildEntry.CHILD_ACTION_INSERT);
      }
      catch (PSInvalidChildTypeException e)
      {
         handleMissingChildType(name);
      }
      catch (Exception e)
      {
         throwOperationError("Failed to create child entry: ", e);
      }

      for (int i = 0; i < count; i++)
      {
         if (i == 0)
            results.add(entry);
         else
            results.add((PSItemChildEntry) entry.clone());
      }

      return results;
   }

   /**
    * Just like {@link PSWebserviceUtils#handleRevision(PSLegacyGuid, PSComponentSummary)} except it loads the component summary, then calls
    * the above method.
    *
    * @throws PSErrorException if the component summary for the item cannot be
    *    loaded.
    */
   private PSLegacyGuid handleRevision(PSLegacyGuid lguid)
      throws PSErrorException
   {
      PSComponentSummary summary = PSWebserviceUtils.getItemSummary(lguid
         .getContentId());
      return PSWebserviceUtils.handleRevision(lguid, summary);
   }

   @Deprecated
   public void deleteChildEntries(IPSGuid id, String name,
      List<IPSGuid> childIds, @SuppressWarnings("unused")
      String session, @SuppressWarnings("unused")
      String user)
      throws PSUnknownChildException, PSInvalidStateException,
      PSErrorsException, PSErrorException
   {
      deleteChildEntries(id, name, childIds);
   }

   @SuppressWarnings("unchecked")
   public void deleteChildEntries(IPSGuid id, String name,
      List<IPSGuid> childIds)
      throws PSUnknownChildException, PSInvalidStateException,
      PSErrorsException, PSErrorException
   {
      if (id == null)
         throw new IllegalArgumentException("id may not be null");
      if (!(id instanceof PSLegacyGuid))
         throw new IllegalArgumentException(
            "id must be instance of legacy guid");
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");
      if (childIds == null || childIds.isEmpty())
         throw new IllegalArgumentException(
            "childIds may not be null or empty");

      PSLegacyGuid lguid = (PSLegacyGuid) id;
      PSErrorsException results = new PSErrorsException();

      // validate revision
      try
      {
         lguid = handleRevision(lguid);
      }
      catch (PSErrorException e)
      {
         results.addError(id, e);
         throw results;
      }

      PSRequest req = getRequest();
      PSLocator loc = lguid.getLocator();

      PSServerItem parent;
      try
      {
         parent = PSServerItem.loadItem(loc, req, PSServerItem.TYPE_FIELDS
            | PSServerItem.TYPE_CHILD);
      }
      catch (Exception e)
      {
         throwOperationError("Failed to load parent item ", e);
         return; // never happen here, used to turn off compiling error/warning
      }

      ensureCheckedOut(parent, "deleteChildEntries");

      PSItemChild child = parent.getChildByName(name);
      if (child == null)
      {
         handleMissingChildType(name);
      }

      int delChildId = child.getChildId();

      // walk existing children and mark them for delete if a match
      Set<Integer> childSet = new HashSet<Integer>();
      for (IPSGuid guid : childIds)
      {
         if (!(guid instanceof PSLegacyGuid))
            throw new IllegalArgumentException(
               "childIds must be instanceof PSLegacyGuid");

         PSLegacyGuid childGuid = (PSLegacyGuid) guid;
         int childId = childGuid.getChildId();
         if (childId != delChildId)
         {
            results.addError(guid, handleInvalidChildId(guid, name, id));
         }
         childSet.add(childGuid.getUUID());
      }

      Iterator<PSItemChildEntry> entries = child.getAllEntries();
      while (entries.hasNext())
      {
         PSItemChildEntry entry = entries.next();
         if (childSet.contains(entry.getChildRowId()))
            entry.setAction(PSItemChildEntry.CHILD_ACTION_DELETE);
         else
            entry.setAction(PSItemChildEntry.CHILD_ACTION_IGNORE);
      }

      // save parent back
      try
      {
         parent.save(req);
      }
      catch (PSCmsException e)
      {
         results.addError(id, handleItemSaveFailedError(id, e));
      }

      if (results.hasErrors())
         throw results;
   }

   @Deprecated
   public List<PSItemChildEntry> loadChildEntries(IPSGuid contentId,
      String name, boolean includeBinary, @SuppressWarnings("unused")
      String session, @SuppressWarnings("unused")
      String user)
      throws PSUnknownChildException, PSErrorResultsException, PSErrorException
   {
      return loadChildEntries(contentId, name, includeBinary);
   }

   @SuppressWarnings("unchecked")
   public List<PSItemChildEntry> loadChildEntries(IPSGuid id, String name,
      boolean includeBinary)
      throws PSUnknownChildException, PSErrorResultsException
   {
      if (id == null)
         throw new IllegalArgumentException("id may not be null");
      if (!(id instanceof PSLegacyGuid))
         throw new IllegalArgumentException(
            "id must be instance of legacy guid");
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");

      List<PSItemChildEntry> results = new ArrayList<PSItemChildEntry>();

      PSLegacyGuid lguid = (PSLegacyGuid) id;
      try
      {
         lguid = handleRevision(lguid);
      }
      catch (PSErrorException e)
      {
         PSErrorResultsException errorResults = new PSErrorResultsException();
         errorResults.addError(id, e);
         throw errorResults;
      }

      PSRequest req = getRequest();
      PSLocator loc = lguid.getLocator();
      PSServerItem parent;
      try
      {
         int flags = PSServerItem.TYPE_FIELDS | PSServerItem.TYPE_CHILD;
         if (includeBinary)
            flags |= PSServerItem.TYPE_BINARY;
         parent = PSServerItem.loadItem(loc, req, flags);
      }
      catch (Exception e)
      {
         PSErrorResultsException errorResults = new PSErrorResultsException();
         errorResults.addError(id, handleLoadFailed(id, e));
         throw errorResults;
      }

      PSItemChild child = parent.getChildByName(name);
      if (child == null)
      {
         handleMissingChildType(name);
      }

      Iterator<PSItemChildEntry> entries = child.getAllEntries();
      while (entries.hasNext())
      {
         PSItemChildEntry entry = entries.next();

         entry.setAction(PSItemChildEntry.CHILD_ACTION_UPDATE);
         results.add(entry);
      }

      return results;
   }

   @Deprecated
   public void reorderChildEntries(IPSGuid id, String name,
      List<IPSGuid> childIds, @SuppressWarnings("unused")
      String session, @SuppressWarnings("unused") String user)
      throws PSUnknownChildException, PSInvalidStateException,
      PSErrorsException, PSErrorException
   {
      reorderChildEntries(id, name, childIds);
   }

   @SuppressWarnings("unchecked")
   public void reorderChildEntries(IPSGuid id, String name,
      List<IPSGuid> childIds)
      throws PSUnknownChildException, PSInvalidStateException,
      PSErrorsException
   {
      if (id == null)
         throw new IllegalArgumentException("id may not be null");
      if (!(id instanceof PSLegacyGuid))
         throw new IllegalArgumentException(
            "id must be instance of legacy guid");
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");
      if (childIds == null || childIds.isEmpty())
         throw new IllegalArgumentException("childIds may not be null or empty");


      PSLegacyGuid lguid = (PSLegacyGuid) id;
      PSErrorsException results = new PSErrorsException();

      // validate revision
      try
      {
         lguid = handleRevision(lguid);
      }
      catch (PSErrorException e)
      {
         results.addError(id, e);
         throw results;
      }

      PSRequest req = getRequest();
      PSLocator loc = lguid.getLocator();

      PSServerItem parent;
      try
      {
         parent = PSServerItem.loadItem(loc, req, PSServerItem.TYPE_FIELDS
            | PSServerItem.TYPE_CHILD);
      }
      catch (Exception e)
      {
         results.addError(id, handleLoadFailed(id, e));
         throw results;
      }

      ensureCheckedOut(parent, "reorderChildEntries");

      PSItemChild child = parent.getChildByName(name);
      if (child == null)
      {
         handleMissingChildType(name);
      }

      if (!child.isSequenced())
      {
         throw new IllegalArgumentException(
            "Specified child does not support sequencing");
      }

      int delChildId = child.getChildId();

      // walk existing children and build a map
      Map<Integer, PSItemChildEntry> childMap = getChildEntryMap(child);

      // walk child guid list and reorder as specified
      int index = 0;
      for (IPSGuid guid : childIds)
      {
         if (!(guid instanceof PSLegacyGuid))
            throw new IllegalArgumentException(
               "childIds must be instanceof PSLegacyGuid");

         PSLegacyGuid childGuid = (PSLegacyGuid) guid;
         int childId = childGuid.getChildId();
         if (childId != delChildId)
         {
            // add error exception, continue and don't bump count
            results.addError(guid, handleInvalidChildId(guid, name, id));
         }

         PSItemChildEntry entry = childMap.get(childGuid.getUUID());
         if (entry == null)
         {
            // add error exception, continue and don't bump count
            results.addError(guid, handleMissingChildId(guid, name, id));
         }

         child.moveToPosition(entry, index);
         index++;
      }

      // save parent back
      try
      {
         parent.save(req);
      }
      catch (PSCmsException e)
      {
         results.addError(id, handleItemSaveFailedError(id, e));
      }

      if (results.hasErrors())
         throw results;
   }

   @Deprecated
   public void saveChildEntries(IPSGuid contentId, String name,
      List<PSItemChildEntry> entries, @SuppressWarnings("unused")
      String session, @SuppressWarnings("unused") String user)
      throws PSUnknownChildException, PSInvalidStateException,
      PSErrorsException, PSErrorException
   {
      saveChildEntries(contentId, name, entries);
   }

   public void saveChildEntries(IPSGuid id, String name,
      List<PSItemChildEntry> entries)
      throws PSUnknownChildException, PSInvalidStateException,
      PSErrorsException
   {
      if (id == null)
         throw new IllegalArgumentException("id may not be null");
      if (!(id instanceof PSLegacyGuid))
         throw new IllegalArgumentException(
            "id must be instance of legacy guid");
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");
      if (entries == null || entries.isEmpty())
         throw new IllegalArgumentException("entries may not be null or empty");

      PSLegacyGuid lguid = (PSLegacyGuid) id;
      PSErrorsException results = new PSErrorsException();

      // validate revision
      try
      {
         lguid = handleRevision(lguid);
      }
      catch (PSErrorException e)
      {
         results.addError(id, e);
         throw results;
      }

      PSRequest req = getRequest();
      PSLocator loc = lguid.getLocator();

      PSServerItem parent;
      try
      {
         parent = PSServerItem.loadItem(loc, req, PSServerItem.TYPE_FIELDS
            | PSServerItem.TYPE_CHILD);
      }
      catch (Exception e)
      {
         results.addError(id, handleLoadFailed(id, e));
         throw results;
      }

      ensureCheckedOut(parent, "saveChildEntries");

      PSItemChild child = parent.getChildByName(name);
      if (child == null)
      {
         handleMissingChildType(name);
      }

      // walk existing children and build a map
      Map<Integer, PSItemChildEntry> childMap = getChildEntryMap(child);

      // walk supplied items and process each
      for (PSItemChildEntry entry : entries)
      {
         PSErrorException err = null;
         int childRowId = entry.getChildRowId();
         PSLegacyGuid childGuid = new PSLegacyGuid(parent.getContentTypeId(),
            child.getChildId(), childRowId);

         String action = entry.getAction();
         if (action.equals(PSItemChildEntry.CHILD_ACTION_INSERT))
         {
            if (childRowId != -1)
            {
               // set already exists error
               err = handleExistingChildId(childGuid, name, id);
               continue;
            }

            child.addEntry(entry);
         }
         else if (action.equals(PSItemChildEntry.CHILD_ACTION_UPDATE)
            || action.equals(PSItemChildEntry.CHILD_ACTION_DELETE))
         {
            PSItemChildEntry current = childMap.get(childRowId);
            if (current == null)
            {
               err = handleMissingChildId(childGuid, name, id);
               continue;
            }

            if (action.equals(PSItemChildEntry.CHILD_ACTION_UPDATE))
               child.replaceEntry(entry);
            else
               current.setAction(PSItemChildEntry.CHILD_ACTION_DELETE);
         }

         if (err != null)
            results.addError(childGuid, err);
         else
            results.addResult(childGuid);
      }

      // save parent back
      try
      {
         parent.save(req);
      }
      catch (PSCmsException e)
      {
         results.addError(id, handleItemSaveFailedError(id, e));
      }

      if (results.hasErrors())
         throw results;
   }

   /**
    * Build a map of child entries found in the supplied child.
    *
    * @param child The child for which the map is returned, assumed not
    *    <code>null</code>.
    * @return The map with the entry's child row id as the key and the entry as
    *    the value, never <code>null</code>, may be empty.
    */
   private Map<Integer, PSItemChildEntry> getChildEntryMap(PSItemChild child)
   {
      Map<Integer, PSItemChildEntry> childMap = new HashMap<Integer, PSItemChildEntry>();

      Iterator<PSItemChildEntry> entries = child.getAllEntries();
      while (entries.hasNext())
      {
         PSItemChildEntry entry = entries.next();
         childMap.put(entry.getChildRowId(), entry);
      }
      return childMap;
   }

   /**
    * Determines if the current user has the supplied item checked out.
    * @param parent The item that should be checked out, assumed not
    *    <code>null</code>.
    * @param operation The name of the operation being performed, assumed not
    *    <code>null</code> or empty.
    *
    * @throws PSInvalidStateException if the item is not checked out by the
    *    specified user.
    */
   private void ensureCheckedOut(PSServerItem parent, String operation)
      throws PSInvalidStateException
   {
      if (!PSWebserviceUtils.getUserName().equals(parent.getCheckedOutByName()))
      {
         int code = IPSWebserviceErrors.ITEM_NOT_CHECKED_OUT;
         throw new PSInvalidStateException(code, PSWebserviceErrors
            .createErrorMessage(code, parent.getContentId(), operation),
            ExceptionUtils.getFullStackTrace(new Exception()));
      }
   }

   /**
    * Create an appropriate exception for a failed load of a server item.
    *
    * @param id The item guid, assumed not <code>null</code>.
    * @param e The exception being handled, assumed not <code>null</code>.
    * @return The error exception to return or throw, never <code>null</code>.
    */
   private PSErrorException handleLoadFailed(IPSGuid id, Exception e)
   {
      int code = IPSWebserviceErrors.LOAD_FAILED;
      PSDesignGuid dguid = new PSDesignGuid(id);
      return new PSErrorException(code, PSWebserviceErrors.createErrorMessage(
         code, PSTypeEnum.valueOf(id.getType()), dguid.getValue(), e
            .getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
   }

   /**
    * Create an appropriate exception for a failed save of a server item.
    *
    * @param id The item guid, assumed not <code>null</code>.
    * @param e The exception being handled, assumed not <code>null</code>.
    * @return The error exception to return or throw, never <code>null</code>.
    */
   private PSErrorException handleItemSaveFailedError(IPSGuid id, Exception e)
   {
      int code = IPSWebserviceErrors.SAVE_FAILED;
      PSDesignGuid dguid = new PSDesignGuid(id);
      return new PSErrorException(code, PSWebserviceErrors.createErrorMessage(
         code, PSTypeEnum.valueOf(id.getType()), dguid.getValue(), e
            .getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
   }

   /**
    * Create an appropriate exception for a missing child entry that is expected
    * to be found.
    *
    * @param guid The child item entry guid, assumed not <code>null</code>.
    * @param name The child item field name, assumed not <code>null</code> or
    *    empty.
    * @param id The parent item id, assumed not <code>null</code>.
    * @return The exception to return or throw, never <code>null</code>.
    */
   private PSErrorException handleMissingChildId(IPSGuid guid, String name,
      IPSGuid id)
   {
      int code = IPSWebserviceErrors.CHILD_ENTRY_NOT_FOUND;
      return handleChildError(code, guid, name, id);
   }

   /**
    * Create an appropriate exception for a child entry that has a child id
    * which does not match the specified child field.
    *
    * @param guid The child item entry guid, assumed not <code>null</code>.
    * @param name The child item field name, assumed not <code>null</code> or
    *    empty.
    * @param id The parent item id, assumed not <code>null</code>.
    * @return The exception to return or throw, never <code>null</code>.
    */
   private PSErrorException handleInvalidChildId(IPSGuid guid, String name,
      IPSGuid id)
   {
      int code = IPSWebserviceErrors.INVALID_CHILD_ID;
      return handleChildError(code, guid, name, id);
   }

   /**
    * Create an appropriate exception for an existing child entry that is not
    * expected to already exist.
    *
    * @param guid The child item entry guid, assumed not <code>null</code>.
    * @param name The child item field name, assumed not <code>null</code> or
    *    empty.
    * @param id The parent item id, assumed not <code>null</code>.
    * @return The exception to return or throw, never <code>null</code>.
    */
   private PSErrorException handleExistingChildId(IPSGuid guid, String name,
      IPSGuid id)
   {
      int code = IPSWebserviceErrors.CHILD_ENTRY_ALREADY_EXISTS;
      return handleChildError(code, guid, name, id);
   }

   /**
    * Create child error exception that expects the arguments
    * <code>{childId, fieldName, parentId}</code>.
    *
    * @param code The error code to use.
    * @param guid The child guid, assumed not <code>null</code>.
    * @param name The child field name, assumed not <code>null</code> or empty.
    * @param id The parent item guid, assumed not <code>null</code>.
    * @return The exception, never <code>null</code>.
    */
   private PSErrorException handleChildError(int code, IPSGuid guid,
      String name, IPSGuid id)
   {
      PSDesignGuid childGuid = new PSDesignGuid(guid);
      PSDesignGuid parentGuid = new PSDesignGuid(id);
      return new PSUnknownChildException(code, PSWebserviceErrors
         .createErrorMessage(code, childGuid.getValue(), name, parentGuid
            .getValue()), ExceptionUtils.getFullStackTrace(new Exception()));
   }

   /**
    * Create and throw an appropriate exception when a child field name is
    * specified that does not exist in the parent.
    *
    * @param child name or id of child, assumed not <code>null</code> or empty.
    * @throws PSUnknownChildException always.
    */
   private void handleMissingChildType(String child)
      throws PSUnknownChildException
   {
      int code = IPSWebserviceErrors.OBJECT_NOT_FOUND;
      throw new PSUnknownChildException(code, PSWebserviceErrors
         .createErrorMessage(code, PSTypeEnum.LEGACY_CHILD_CONTENT_TYPE.name(),
            child), ExceptionUtils.getFullStackTrace(new Exception()));
   }


   @Deprecated
   public List<PSAaRelationship> addContentRelations(IPSGuid id,
      List<IPSGuid> relatedIds, IPSGuid folderId, IPSGuid siteId,
      IPSGuid slotId, IPSGuid templateId, int index, @SuppressWarnings("unused")
      String user)
      throws PSErrorException
   {
      return addContentRelations(id, relatedIds, folderId, siteId, slotId,
         templateId, index);
   }

   @Deprecated
   public List<PSAaRelationship> addContentRelations(IPSGuid id,
      List<IPSGuid> relatedIds, String slot, String template, int index,
      String relationshipName, @SuppressWarnings("unused")
      String user) throws PSErrorException
   {
      return addContentRelations(id, relatedIds, slot, template,
         relationshipName, index);
   }

   @Deprecated
   public List<PSAaRelationship> addContentRelations(IPSGuid id,
      List<IPSGuid> relatedIds, String slot, String template, int index,
      @SuppressWarnings("unused")
      String user) throws PSErrorException
   {
      return addContentRelations(id, relatedIds, slot, template, index);
   }

   public List<PSAaRelationship> addContentRelations(IPSGuid ownerId,
      List<IPSGuid> relatedIds, String slotName, String templateName,
      String relationshipName, int index) throws PSErrorException
   {
      // validating parameters
      PSWebserviceUtils.validateLegacyGuid(ownerId);
      PSWebserviceUtils.validateLegacyGuids(relatedIds);
      if (StringUtils.isBlank(slotName))
         throw new IllegalArgumentException(
            "slotName may not be null or empty.");
      if (StringUtils.isBlank(templateName))
         throw new IllegalArgumentException(
            "templateName may not be null or empty.");

      IPSTemplateSlot slot = (IPSTemplateSlot) PSWebserviceUtils
         .getSlotOrTemplateFromName(slotName, true);

      String relName = slot.getRelationshipName();

      // validate the relationship name, it has to be AA category
      if (StringUtils.isBlank(relationshipName))
      {
         if (StringUtils.isBlank(relName))
         {
            throw new IllegalArgumentException(
               "relationshipName may not be null or empty" +
               "since it is not defined in the slot.");
         }
      }
      else // relationshipName is NOT blank
      {
         if (StringUtils.isBlank(relName))
         {
            relName = relationshipName;
         }
         else if (!relName.equalsIgnoreCase(relationshipName))
         {
            throw new IllegalArgumentException(
               "relationshipName must be the same as the one defined in slot.");
         }
      }

      IPSAssemblyTemplate template = (IPSAssemblyTemplate) PSWebserviceUtils
         .getSlotOrTemplateFromName(templateName, false);

      return addContentRelations(ownerId, relatedIds, slot, template, index,
               slot.getRelationshipName(), null, null);
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.webservices.content.IPSContentWs#addContentRelations(com.percussion.utils.guid.IPSGuid, java.util.List, java.lang.String, java.lang.String, int, java.lang.String)
    */
   public List<PSAaRelationship> addContentRelations(IPSGuid ownerId,
            List<IPSGuid> relatedIds, String slotName, String templateName,
            int index) throws PSErrorException
   {
      // validating parameters
      PSWebserviceUtils.validateLegacyGuid(ownerId);
      PSWebserviceUtils.validateLegacyGuids(relatedIds);
      if (StringUtils.isBlank(slotName))
         throw new IllegalArgumentException(
            "slotName may not be null or empty.");
      if (StringUtils.isBlank(templateName))
         throw new IllegalArgumentException(
            "templateName may not be null or empty.");

      IPSTemplateSlot slot = (IPSTemplateSlot) PSWebserviceUtils
         .getSlotOrTemplateFromName(slotName, true);

      IPSAssemblyTemplate template = (IPSAssemblyTemplate) PSWebserviceUtils
         .getSlotOrTemplateFromName(templateName, false);

      return addContentRelations(ownerId, relatedIds, slot, template, index,
               slot.getRelationshipName(), null, null);
   }

   public List<PSAaRelationship> addContentRelations(IPSGuid ownerId,
      List<IPSGuid> relatedIds, IPSGuid folderId, IPSGuid siteId,
      IPSGuid slotId, IPSGuid templateId, int index)
      throws PSErrorException
   {
      // validating parameters
      PSWebserviceUtils.validateLegacyGuid(ownerId);
      PSWebserviceUtils.validateLegacyGuids(relatedIds);
      if (folderId != null)
         PSWebserviceUtils.validateLegacyGuid(folderId);
      if (slotId == null)
         throw new IllegalArgumentException("slotId may not be null.");
      if (templateId == null)
         throw new IllegalArgumentException("templateId may not be null.");

      IPSTemplateSlot slot = PSWebserviceUtils.loadSlot(slotId);
      IPSAssemblyTemplate template =
         PSWebserviceUtils.loadUnmodifiableTemplate(templateId);

      return addContentRelations(ownerId, relatedIds, slot, template, index,
         slot.getRelationshipName(), (PSLegacyGuid)folderId, siteId);
   }

   /**
    * Creates the relationships from the specified parameters.
    *
    * @see #addContentRelations(IPSGuid, List, IPSGuid, IPSGuid, IPSGuid, IPSGuid, int)
    * @see #addContentRelations(IPSGuid, List, String, String, int)
    * @see #addContentRelations(IPSGuid, List, String, String, String, int)
    */
   private List<PSAaRelationship> addContentRelations(IPSGuid ownerId,
      List<IPSGuid> relatedIds, IPSTemplateSlot slot,
      IPSAssemblyTemplate template,  int index, String relationshipName,
      PSLegacyGuid folderId, IPSGuid siteId)
      throws PSErrorException
   {
      // validate the relationship name
      getRelationshipConfig(relationshipName,
         PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY);

      PSLocator owner = PSWebserviceUtils.validateItemCheckoutByUser(
         ((PSLegacyGuid) ownerId).getLocator());

      List<PSAaRelationship> rels = createAaRelationships(owner, relatedIds,
         index, relationshipName, slot, template, folderId, siteId);

      List<PSAaRelationship> existRels = loadSlotContentRelationships(owner, slot
         .getGUID());

      // sort all existing relationships according to their sortrank property
      Collections.sort(existRels, new RelationshipSorter());

      mergeAaRelationships(existRels, rels, index);

      PSWebserviceUtils.saveAaRelationships(existRels);

      return rels;
   }


   /**
    * Used to sort relationships based on the sort rank property.
    */
   private class RelationshipSorter implements Comparator<PSAaRelationship>
   {
      /* (non-Javadoc)
       * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
       */
      public int compare(PSAaRelationship rel1, PSAaRelationship rel2)
      {
         int sortrank1 = 0;
         try
         {
            sortrank1 = Integer.parseInt(rel1
               .getProperty(IPSHtmlParameters.SYS_SORTRANK));
         }
         catch (Exception e)
         {
            //ignore and keep 0
         }

         int sortrank2 = 0;
         try
         {
            sortrank2 = Integer.parseInt(rel2
               .getProperty(IPSHtmlParameters.SYS_SORTRANK));
         }
         catch (Exception e)
         {
            //ignore and keep 0
         }

         return (sortrank1 - sortrank2);
      }
   }

   /**
    * Creates AA relationships from the given owner, dependents, starting rank,
    * relationship name, slot and template.
    *
    * @param owner the owner of the created relationships; assumed not
    *    <code>null</code>.
    * @param dependentIds the dependent ids; assumed not <code>null</code> or
    *    empty.
    * @param index the starting rank number.
    * @param relationshipName the name of the created relationships; assumed
    *    not <code>null</code> or empty.
    * @param slot the slot of the created relationships; assumed not
    *    <code>null</code>.
    * @param template the template of the created relationships; assumed not
    *    <code>null</code>.
    *
    * @return the created relationships; never <code>null</code> or empty.
    *
    * @throws PSErrorException if an error occurs.
    */
   private List<PSAaRelationship> createAaRelationships(PSLocator owner,
      List<IPSGuid> dependentIds, int index, String relationshipName,
      IPSTemplateSlot slot, IPSAssemblyTemplate template, PSLegacyGuid folderId,
      IPSGuid siteId)
      throws PSErrorException
   {
      // get relationshipConfig, slot & template from names
      PSRelationshipConfig config = getRelationshipConfig(relationshipName,
         PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY);

      PSAaRelationship relationship;
      List<PSAaRelationship> relationships = new ArrayList<PSAaRelationship>();
      for (IPSGuid dependentId : dependentIds)
      {
         PSWebserviceUtils.validateLegacyGuid(dependentId);

         PSLocator dependent = PSWebserviceUtils.getHeadLocator(
            (PSLegacyGuid) dependentId, false, config);
         relationship = new PSAaRelationship(owner, dependent, slot, template);
         relationship.setId(PSWebserviceUtils.getNextRelationshipId());
         relationship.setSortRank(index++);

         if (folderId != null)
            relationship.setFolderId(folderId.getContentId());
         if (siteId != null)
            relationship.setSiteId(siteId);

         relationships.add(relationship);
      }

      return relationships;
   }

   /**
    * Merges the supplied source relationship collection into the target
    * relationship collection at the given index. First the
    * supplied source relationships are inserted at the specified index.
    * Then all target relationships sortrank property is normalized
    * starting with 1, continiously incremented by 1.
    *
    * @param target the relationship collection into which to insert the
    *    supplied source collection, assumed not <code>null</code>.
    * @param source the relationship collection which will be inserted into
    *    the supplied target collection at the provided index, assumed not
    *    <code>null</code>.
    * @param index the index where to insert the supplied source relationships
    *    into the target relationships. Supply -1 or a value greater than the
    *    target relationship size to append the source to the target.
    */
   private void mergeAaRelationships(List<PSAaRelationship> target,
      List<PSAaRelationship> source, int index)
   {
      // calculate the location index for new relationships
      if (index == -1 || index > target.size())
         index = target.size();

      // insert all new relationships into the existing relationships
      for (int i = 0; i < source.size(); i++)
         target.add(index++, source.get(i));

      // normalize the sortrank property as 0-based index
      for (int i = 0; i < target.size(); i++)
      {
         PSAaRelationship rel = target.get(i);
         rel.setProperty(IPSHtmlParameters.SYS_SORTRANK, String.valueOf(i));
      }
   }

   /**
    * Gets the relationship configuration from the specified relationship name
    * and the specified category.
    *
    * @param relationshipName the name of the relationship config, may not be
    *    <code>null</code> or empty.
    * @param category the specified relationship category. It may be
    *    <code>null</code> if the category does not to be validated.
    * @return specified relationship config, never <code>null</code>.
    */
   private PSRelationshipConfig getRelationshipConfig(String relationshipName,
      String category)
   {
      PSRelationshipConfig config = PSRelationshipCommandHandler
         .getRelationshipConfig(relationshipName);
      if (config == null)
         throw new IllegalArgumentException(
            "Cannot find relationship configuration with name: "
               + relationshipName);
      if (category != null && !config.getCategory().equalsIgnoreCase(category))
      {
         throw new IllegalArgumentException(
            "The category of the relationship configuration must be '"
               + category + "'.");
      }

      return config;
   }

   @Deprecated
   public void deleteContentRelations(List<IPSGuid> ids,
      @SuppressWarnings("unused") String user)
      throws PSErrorsException, PSErrorException
   {
      deleteContentRelations(ids);
   }

   public void deleteContentRelations(List<IPSGuid> ids)
      throws PSErrorsException, PSErrorException
   {
      if (ids == null || ids.isEmpty())
         throw new IllegalArgumentException("ids may not be null or empty.");

      PSWebserviceUtils.deleteRelationships(ids, true);
   }

   @Deprecated
   public List<PSItemSummary> findDependents(IPSGuid id,
      PSRelationshipFilter filter, boolean isLoadOperations,
      @SuppressWarnings("unused") String user)
      throws PSErrorException
   {
      return findDependents(id, filter, isLoadOperations);
   }

   public List<PSItemSummary> findDependents(IPSGuid id,
      PSRelationshipFilter filter, boolean isLoadOperations)
      throws PSErrorException
   {
      PSWebserviceUtils.validateLegacyGuid(id);

      filter = setFilterByAaCategorysIfNeeded(filter);
      filter.setOwner(((PSLegacyGuid) id).getLocator());

      try
      {
         PSComponentSummaries summs = PSWebserviceUtils
            .getRelationshipProcessor().getSummaries(filter, false);
         return getItemSummaries(summs, isLoadOperations);
      }
      catch (PSCmsException e)
      {
         e.printStackTrace();
         int code = IPSWebserviceErrors.FAILED_FIND_CHILD_ITEMS;
         PSErrorException error = new PSErrorException(code, PSWebserviceErrors
            .createErrorMessage(code, id.toString(), e.getLocalizedMessage()),
            ExceptionUtils.getFullStackTrace(e));
         throw error;
      }
   }

   /**
    * Converts the specified component summaries to item summary.
    *
    * @param summaries the to be converted objects, assumed not
    *    <code>null</code>.
    * @param isLoadOperations <code>true</code> if set the allowed operations
    *    for the returned item summaries; otherwise, the allowed operations
    *    of the returned item summaries will be <code>null</code>.
    * @return the converted object, never <code>null</code>, may be empty.
    */
   @SuppressWarnings("unchecked")
   private List<PSItemSummary> getItemSummaries(PSComponentSummaries summaries,
      boolean isLoadOperations)
   {
      List<PSItemSummary> result = new ArrayList<PSItemSummary>(summaries
         .size());
      String contentTypeName;
      Iterator summariesIt = summaries.iterator();
      while (summariesIt.hasNext())
      {
         PSThreadUtils.checkForInterrupt();
         PSComponentSummary comp = (PSComponentSummary) summariesIt.next();

         // get content type name from content type id
         try
         {
            PSItemDefManager mgr = PSItemDefManager.getInstance();
            contentTypeName = mgr.contentTypeIdToName(comp.getContentTypeId());
         }
         catch (PSInvalidContentTypeException e)
         {
            e.printStackTrace();
            throw new RuntimeException(e.getLocalizedMessage(), e);
         }

         PSItemSummary target = new PSItemSummary(comp.getContentId(), -1, comp
            .getName(), (int) comp.getContentTypeId(), contentTypeName, comp.isRevisionLock());

         if (isLoadOperations)
         {
            String user = PSWebserviceUtils.getUserName();
            target.setOperations(getAllowedOperations(comp, user));
         }

         result.add(target);
      }

      return result;
   }

   @Deprecated
   public List<PSItemSummary> findOwners(IPSGuid id,
      PSRelationshipFilter filter, boolean isLoadOperations,
      @SuppressWarnings("unused") String user)
      throws PSErrorException
   {
      return findOwners(id, filter, isLoadOperations);
   }

   public List<PSItemSummary> findOwners(IPSGuid id,
      PSRelationshipFilter filter, boolean isLoadOperations)
      throws PSErrorException
   {
      PSWebserviceUtils.validateLegacyGuid(id);

      filter = setFilterByAaCategorysIfNeeded(filter);
      filter.setDependent(((PSLegacyGuid) id).getLocator());

      try
      {
         PSComponentSummaries summs = PSWebserviceUtils
            .getRelationshipProcessor().getSummaries(filter, true);
         return getItemSummaries(summs, isLoadOperations);
      }
      catch (PSCmsException e)
      {
         e.printStackTrace();
         int code = IPSWebserviceErrors.FAILED_FIND_PARENT_ITEMS;
         PSErrorException error = new PSErrorException(code, PSWebserviceErrors
            .createErrorMessage(code, id.toString(), e.getLocalizedMessage()),
            ExceptionUtils.getFullStackTrace(e));
         throw error;
      }
   }

   /**
    * @see IPSContentWs#loadContentRelations(PSRelationshipFilter, boolean)
    */
   public List<PSAaRelationship> loadContentRelations(
      PSRelationshipFilter filter, boolean isLoadReferenceInfo)
      throws PSErrorException
   {
      if (filter == null)
         throw new IllegalArgumentException("filter may not be null.");

      filter = setFilterByAaCategorysIfNeeded(filter);
      List<PSRelationship> srcRels = PSWebserviceUtils
         .loadRelationships(filter);

      // use the map to locally cache the loaded slot/template
      LoadAARelationshipCache tmpCache = new LoadAARelationshipCache();

      // convert PSRelationship to PSAaRelationshipWs
      List<PSAaRelationship> result = new ArrayList<PSAaRelationship>();
      PSAaRelationship rel;

      for (PSRelationship srcRel : srcRels)
      {
         rel = new PSAaRelationship(srcRel);
         result.add(rel);

         if (!isLoadReferenceInfo)
            continue;

         //\/\/\/\/\/\/\/\/\/\
         // set transient data
         //\/\/\/\/\/\/\/\/\/\
         IPSTemplateSlot slot = tmpCache.getSlot(rel.getSlotId());
         rel.setSlot(slot);
         IPSAssemblyTemplate template = tmpCache.getTemplate(rel
            .getTemplateId());
         rel.setTemplate(template);

         // set folder properties
         int folderId = rel.getFolderId();
         if (folderId != -1)
         {
            PSLegacyGuid folderGuid = new PSLegacyGuid(folderId, -1);
            rel.setFolderName(tmpCache.getFolderName(folderGuid));
            rel.setFolderPath(tmpCache.getFolderPath(folderGuid));
         }
         // set site name
         IPSGuid siteId = rel.getSiteId();
         if (siteId != null)
            rel.setSiteName(tmpCache.getSite(siteId).getName());
      }


      // sort all existing relationships according to their sortrank property
      Collections.sort(result, new RelationshipSorter());

      return result;
   }

   /**
    * Turn on filtering by category if the specified filter is <code>null</code>
    * or has not set category or relationship names; otherwise do nothing.
    *
    * @param filter the filter in question, may be <code>null</code>.
    * @return the created or modified filter, never <code>null</code>.
    * @throws IllegalArgumentException if the category of the specified filter
    *   is specified, but is not Active Assembly, or if the relationship config
    *   name(s) is specified, but at least one of them is not Active Assembly
    *   category.
    */
   private PSRelationshipFilter setFilterByAaCategorysIfNeeded(
      PSRelationshipFilter filter)
   {
      if (filter == null)
      {
         filter = new PSRelationshipFilter();
         filter.setCommunityFiltering(false);
         filter.setCategory(PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY);
         return filter;
      }
      if ((!StringUtils.isBlank(filter.getCategory()))
         && (!filter.getCategory().equalsIgnoreCase(
            PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY)))
      {
         throw new IllegalArgumentException(
            "The category of Active Assembly filter must be: "
               + PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY);
      }

      // validate relationship name, must be active assembly category
      if (!filter.getNames().isEmpty())
      {
         for (String name : filter.getNames())
         {
            PSRelationshipConfig config = PSRelationshipCommandHandler
               .getRelationshipConfig(name);
            if (!config.getCategory().equals(
               PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY))
               throw new IllegalArgumentException(
                  "Invalid Active Assembly relationship name: '" + name + "'.");
         }
      }
      else
      {
         filter.setCategory(PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY);
      }

      return filter;
   }

   /**
    * Loads the specified folder path.
    *
    * @param folderId the id of the folder, assumed not <code>null</code>.
    * @return the folder path, never <code>null</code>, may be empty.
    * @throws PSErrorException if failed to load the specified folder path.
    */
   private String loadFolderPath(PSLegacyGuid folderId) throws PSErrorException
   {
      PSLocator locator = new PSLocator(folderId.getContentId(), -1);
      try
      {
         PSServerFolderProcessor processor = getFolderProcessor();

         PSWebserviceUtils.getRelationshipProcessor();
         String[] paths = processor.getItemPaths(locator);
         if (paths == null || paths.length == 0)
         {
            int code = IPSWebserviceErrors.NO_FOLDER_PATH_FOR_FOLDERID;
            PSErrorException error = new PSErrorException(code,
               PSWebserviceErrors
                  .createErrorMessage(code, folderId.longValue()),
               ExceptionUtils.getFullStackTrace(new Exception()));
            throw error;
         }
         return paths[0];
      }
      catch (PSCmsException | PSNotFoundException e)
      {
         // error occurred while finding folder path, may caused by a bad data.
         e.printStackTrace();
         int code = IPSWebserviceErrors.FAILED_LOAD_FOLDER_PATH;
         PSErrorException error = new PSErrorException(code, PSWebserviceErrors
            .createErrorMessage(code, folderId.toString(), e
               .getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
         throw error;
      }
   }

   /**
    *
    */
   private class LoadAARelationshipCache
   {
      /**
       * Gets the slot with the specified slot id.
       *
       * @param slotId the id of the slot, assumed not <code>null</code>.
       *
       * @return the specified slot, never <code>null</code>.
       *
       * @throws PSErrorException if the specified slot does not exit.
       */
      IPSTemplateSlot getSlot(IPSGuid slotId) throws PSErrorException
      {
         if (m_slotMap.get(slotId) == null)
            m_slotMap.put(slotId, PSWebserviceUtils.loadSlot(slotId));

         return m_slotMap.get(slotId);
      }

      /**
       * Gets the template with the specified template id.
       *
       * @param templateId the id of the template, assumed not
       *    <code>null</code>.
       * @return the specified template, never <code>null</code>.
       * @throws PSErrorException if the specified template does not exit.
       */
      IPSAssemblyTemplate getTemplate(IPSGuid templateId)
         throws PSErrorException
      {
         if (m_templateMap.get(templateId) == null)
         {
            // need to load template with slot since the slot will be accessed
            // by PSContentTypeVariant when creating PSAaRelationship
            m_templateMap.put(templateId,
               PSWebserviceUtils.loadUnmodifiableTemplate(templateId));
         }

         return m_templateMap.get(templateId);
      }


      /**
       * Gets the site with the specified site id.
       *
       * @param siteId the id of the specified site, assumed not
       *    <code>null</code>.
       * @return the specified site, never <code>null</code>.
       * @throws PSErrorException if the specified site does not exit.
       */
      IPSSite getSite(IPSGuid siteId) throws PSErrorException
      {
         if (m_siteMap.get(siteId) == null)
            m_siteMap.put(siteId, loadSite(siteId));

         return m_siteMap.get(siteId);
      }

      /**
       * Gets the name of the specified folder.
       *
       * @param folderId the id of the specified folder, assumed not
       *    <code>null</code>.
       * @return the name of the folder, never <code>null</code>.
       * @throws PSErrorException if the specified folder does not exit.
       */
      String getFolderName(IPSGuid folderId) throws PSErrorException
      {
         if (m_folderMap.get(folderId) == null)
         {
            PSComponentSummary summary = PSWebserviceUtils
               .getItemSummary(folderId.getUUID());
            m_folderMap.put(folderId, summary);
         }

         return m_folderMap.get(folderId).getName();
      }

      /**
       * Gets the name of the specified folder.
       *
       * @param folderId the id of the specified folder, assumed not
       *    <code>null</code>.
       * @return the name of the folder, never <code>null</code>.
       * @throws PSErrorException if the specified folder does not exit.
       */
      String getFolderPath(PSLegacyGuid folderId) throws PSErrorException
      {
         if (m_folderPathMap.get(folderId) == null)
         {
            m_folderPathMap.put(folderId, loadFolderPath(folderId));
         }

         return m_folderPathMap.get(folderId);
      }

      /**
       * Loads a site with the specified id.
       *
       * @param siteId the id of the site, assumed not <code>null</code>.
       * @return the specified site, never <code>null</code>.
       * @throws PSErrorException if the specified site does not exist.
       */
      private IPSSite loadSite(IPSGuid siteId) throws PSErrorException
      {
         IPSSiteManager sitemgr = PSSiteManagerLocator.getSiteManager();
         try
         {
            return sitemgr.loadSite(siteId);
         }
         catch (PSNotFoundException e)
         {
            // cannot find site, may caused by bad data.
            e.printStackTrace();
            int code = IPSWebserviceErrors.OBJECT_NOT_FOUND;
            PSDesignGuid guid = new PSDesignGuid(siteId);
            PSErrorException error = new PSErrorException(code,
               PSWebserviceErrors.createErrorMessage(code, IPSSite.class
                  .getName(), guid.longValue()), ExceptionUtils
                  .getFullStackTrace(e));
            throw error;
         }
      }

      /**
       * It maps the slot id to its instance.
       */
      private Map<IPSGuid, IPSTemplateSlot> m_slotMap = new HashMap<IPSGuid, IPSTemplateSlot>();

      /**
       * It maps the template id to its instance.
       */
      private Map<IPSGuid, IPSAssemblyTemplate> m_templateMap = new HashMap<IPSGuid, IPSAssemblyTemplate>();

      /**
       * It maps the site id to its instance.
       */
      private Map<IPSGuid, IPSSite> m_siteMap = new HashMap<IPSGuid, IPSSite>();

      /**
       * It maps the folder id to its summary.
       */
      private Map<IPSGuid, PSComponentSummary> m_folderMap = new HashMap<IPSGuid, PSComponentSummary>();

      /**
       * It maps the folder id to its summary.
       */
      private Map<IPSGuid, String> m_folderPathMap = new HashMap<IPSGuid, String>();
   }

   @Deprecated
   public void reorderContentRelations(List<IPSGuid> ids, int index,
      @SuppressWarnings("unused") String user)
      throws PSErrorException
   {
      reorderContentRelations(ids, index);
   }

   public void reorderContentRelations(List<IPSGuid> ids, int index)
      throws PSErrorException
   {
      if (ids == null || ids.isEmpty())
         throw new IllegalArgumentException("ids may not be null or empty.");

      List<PSAaRelationship> rels = new ArrayList<PSAaRelationship>();
      for (IPSGuid rid : ids)
      {
         rels.add(getAaRelationshipById(rid));
      }

      reArrangeContentRelations(rels, index);
   }

   @Deprecated
   public void reArrangeContentRelations(List<PSAaRelationship> rels,
      int index, @SuppressWarnings("unused") String user)
   throws PSErrorException
   {
      reArrangeContentRelations(rels, index);
   }

   public void reArrangeContentRelations(List<PSAaRelationship> rels,
      int index) throws PSErrorException
   {
      if (rels == null || rels.isEmpty())
         throw new IllegalArgumentException("rels cannot be null or empty");

      // validate slotid and owner for supplied relationships
      IPSGuid slotid = null;
      PSLocator owner = null;
      for (PSAaRelationship r : rels)
      {
         if (slotid == null)
            slotid = r.getSlotId();
         else if (!slotid.equals(r.getSlotId()))
            throw new IllegalArgumentException(
               "all relationhips must belong to the same slot");

         if (owner == null)
            owner = r.getOwner();
         else if (!owner.equals(r.getOwner()))
            throw new IllegalArgumentException(
               "all relationhips must belong to the same owner");
      }

      PSWebserviceUtils
         .validateItemCheckoutByUser(rels.get(0).getOwner());

      List<PSAaRelationship> existingRels = loadSlotContentRelationships(owner,
         slotid);

      // get a list of relationships which exist in the 'existingRels', but
      // not in the specified 'rels' relationships.
      List<PSAaRelationship> resultSet = new ArrayList<PSAaRelationship>();
      for (PSAaRelationship r : existingRels)
      {
         if (!rels.contains(r))
            resultSet.add(r);
      }

      mergeAaRelationships(resultSet, rels, index);

      PSWebserviceUtils.saveAaRelationships(resultSet);
   }

   /*
    * //see base interface method for details
    */
   public List<PSAaRelationship> loadSlotContentRelationships(IPSGuid ownerId,
         IPSGuid slotid) throws PSErrorException
   {
      notNull(ownerId);
      notNull(slotid);

      PSWebserviceUtils.validateLegacyGuid(ownerId);
      return loadSlotContentRelationships(
            ((PSLegacyGuid) ownerId).getLocator(), slotid);
   }

   /**
    * Gets a list of Active Assembly relationships which have the specified
    * owner and slot.
    *
    * @param owner the owner of the requested relationships, assumed not
    * <code>null</code>.
    * @param slotid the slot id of the specified slot, assumed not
    * <code>null</code>.
    *
    * @return the requested relationships in the order of , it may be empty,
    * but never <code>null</code>. It is in the order of
    * {@link IPSHtmlParameters#SYS_SORTRANK} property of the relationships.
    *
    * @throws PSErrorException if failed to load the relationships.
    */
   @SuppressWarnings("unchecked")
   private List<PSAaRelationship> loadSlotContentRelationships(PSLocator owner,
      IPSGuid slotid) throws PSErrorException
   {
      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setOwner(owner);
      filter.setProperty(IPSHtmlParameters.SYS_SLOTID, String.valueOf(slotid
         .longValue()));
      filter.setCategory(PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY);

      // Set to use the owner revision on the filter if it is true for all
      // Active Assembly relationships; otherwise, don't set the flag on the
      // filter.
      Iterator configs = PSRelationshipCommandHandler
         .getRelationshipConfigs(PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY);
      filter.limitToOwnerRevision(true);
      while (configs.hasNext())
      {
         PSRelationshipConfig config = (PSRelationshipConfig) configs.next();
         if (!config.useOwnerRevision())
         {
            filter.limitToOwnerRevision(false);
            break;
         }
      }
      List<PSAaRelationship> rels = loadContentRelations(filter, false);

      return rels;
   }

   /**
    * Retrieves the specified relationship with the specified relationship id.
    *
    * @param rid the id of the specified relationship, assumed not
    *    <code>null</code>.
    * @return the relationship with the specified id, never <code>null</code>.
    * @throws PSErrorException if an error occurred during retrieving the
    *    specified relationship.
    */
   private PSAaRelationship getAaRelationshipById(IPSGuid rid)
      throws PSErrorException
   {
      PSRelationshipFilter filter = new PSRelationshipFilter();
      PSRelationshipProcessor processor = PSWebserviceUtils
         .getRelationshipProcessor();

      List<PSRelationship> rels;
      filter.setRelationshipId(rid.getUUID());
      try
      {
         rels = processor.getRelationshipList(filter);
         if (rels.size() == 0)
         {
            int code = IPSWebserviceErrors.OBJECT_NOT_FOUND;
            PSErrorException error = new PSErrorException(code,
               PSWebserviceErrors.createErrorMessage(code, PSRelationship.class
                  .getName(), rid.longValue()), ExceptionUtils
                  .getFullStackTrace(new Exception()));
            throw error;
         }
         return new PSAaRelationship(rels.get(0));
      }
      catch (PSCmsException e)
      {
         e.printStackTrace();
         int errorCode = IPSWebserviceErrors.FAILED_LOAD_RELATIONSHIP;
         PSErrorException error = new PSErrorException(errorCode,
            PSWebserviceErrors.createErrorMessage(errorCode, rid.longValue(), e
               .getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));

         throw error;
      }
   }

   @Deprecated
   public void saveContentRelations(List<PSAaRelationship> relationships,
      @SuppressWarnings("unused") String user)
      throws PSErrorsException, PSErrorException
   {
      saveContentRelations(relationships);
   }

   public void saveContentRelations(List<PSAaRelationship> relationships)
      throws PSErrorsException, PSErrorException
   {
      if (relationships == null || relationships.isEmpty())
         throw new IllegalArgumentException(
            "relationships may not be null or empty.");

      List<PSRelationship> rels = new ArrayList<PSRelationship>(relationships
         .size());
      // validate all owner items have been checked out by the user
      for (PSAaRelationship rel : relationships)
      {
         PSLocator owner = PSWebserviceUtils.validateItemCheckoutByUser(rel
            .getOwner());
         // always reset the owner since the revision of the owner may be -1
         rel.setOwner(owner);
         rels.add(rel);
      }

      PSWebserviceUtils.saveRelationships(rels);
   }

   @Deprecated
   public PSFolder addFolder(String name, String path,
      @SuppressWarnings("unused") String user)
      throws PSErrorException
   {
      return addFolder(name, path);
   }

   public PSFolder addFolder(String name, String path)
   throws PSErrorException
   {
      return addFolder(name, path, true);
   }

   public PSFolder addFolder(String name, String path, boolean loadTransientData)
      throws PSErrorException
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name must not be null or empty.");
      if (StringUtils.isBlank(path))
         throw new IllegalArgumentException("path must not be null or empty.");

      int id = getFolderIdFromPath(path, true);
      return addFolder(name, id, id, loadTransientData);
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.webservices.content.IPSContentWs#addFolder(java.lang.String, java.lang.String, java.lang.String, boolean)
    */
   public PSFolder addFolder(String name, String parentPath, String srcPath, boolean loadTransientData)
   {
      notEmpty(name);
      notEmpty(parentPath);
      notEmpty(srcPath);

      int parentId = getFolderIdFromPath(parentPath, true);
      int srcId = getFolderIdFromPath(srcPath, true);

      return addFolder(name, parentId, srcId, loadTransientData);
   }

   /**
    * Just like {@link #addFolder(String, String, String)}, except
    * this specifies a parent id instead of parent path.
    * @param parentId the parent folder id.
    */
   private PSFolder addFolder(String name, int parentId, int srcId, boolean loadTransientData)
      throws PSErrorException
   {
      List<IPSGuid> ids = new ArrayList<IPSGuid>(1);
      ids.add(new PSLegacyGuid(srcId, -1));

      // clone the folder from the parent folder, except the name
      List<PSFolder> folders = null;
      try
      {
         folders = loadFolders(ids, loadTransientData);
      }
      catch (PSErrorResultsException e)
      {
         int code = IPSWebserviceErrors.FAILED_LOAD_FOLDER;
         throw new PSErrorException(code, PSWebserviceErrors
            .createErrorMessage(code, ids.get(0).toString(), e
               .getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
      }
      PSFolder folder = (PSFolder) folders.get(0).clone();
      folder.setName(name);
      if (folder.getDisplayFormatId() == -1)
      {
         // this should not happen.
         // set to the default display format (id=0) if it does
         ms_logger.warn("Cannot find display format property from Folder id='"
            + folders.get(0).getLocator() + "'.");
         folder.setDisplayFormatId(0);
      }

      // make sure the current user has the Admin access
      String user = PSWebserviceUtils.getUserName();
      int permissions = PSObjectAclEntry.ACCESS_ADMIN |
         PSObjectAclEntry.ACCESS_READ |
         PSObjectAclEntry.ACCESS_WRITE;

      PSObjectAclEntry entry = folder.getAcl().getAclEntry(user,
         PSObjectAclEntry.ACL_ENTRY_TYPE_USER);
      if (entry == null)
      {
         entry = new PSObjectAclEntry(PSObjectAclEntry.ACL_ENTRY_TYPE_USER,
            user, permissions);
         folder.getAcl().add(entry);
      }
      else
      {
         entry.setPermissions(permissions);
      }

      // save the new folder and create the Folder Relationship
      folder = saveFolder(folder);
      createFolderRelationship(folder, new PSLocator(parentId, 1));

      if (loadTransientData)
         setFolderTransientData(folder);

      return folder;
   }

   /**
    * Creates a folder relationship between the specified folder child
    * and parent.
    *
    * @param child the child folder, assumed not <code>null</code>.
    * @param parent the parent folder, assumed not <code>null</code>.
    * @throws PSErrorException if an error occurs.
    */
   private void createFolderRelationship(PSFolder child, PSLocator parent)
      throws PSErrorException
   {
      List<PSLocator> children = new ArrayList<PSLocator>();
      children.add(child.getLocator());
      try
      {
         getFolderProcessor().addChildren(children, parent);
      }
      catch (PSCmsException e)
      {
         e.printStackTrace();
         // remove the orphan data if failed to create Folder Relationship
         try
         {
            getFolderProcessor().delete(child);
         }
         catch (PSCmsException e1)
         {
            e1.printStackTrace();
         }
         int code = IPSWebserviceErrors.FAILED_SAVE_RELATIONSHIPS;
         PSErrorException error = new PSErrorException(code, PSWebserviceErrors
            .createErrorMessage(code, e.getLocalizedMessage()), ExceptionUtils
            .getFullStackTrace(e));
         throw error;
      }

   }

   // @see IPSContentWs#addFolderChildren(IPSGuid, List)
   public void addFolderChildren(IPSGuid parentId, List<IPSGuid> childIds)
      throws PSErrorException
   {
      PSWebserviceUtils.validateLegacyGuid(parentId);
      PSWebserviceUtils.validateLegacyGuids(childIds);

      // convert GUIDs to PSLocators
      PSLocator parent = new PSLocator(
         ((PSLegacyGuid) parentId).getContentId(), 1);
      List<PSLocator> children = new ArrayList<PSLocator>(childIds.size());
      for (IPSGuid id : childIds)
         children.add(new PSLocator(((PSLegacyGuid) id).getContentId(), -1));

      try
      {
         getFolderProcessor().addChildren(children, parent);
      }
      catch (PSCmsException e)
      {
         e.printStackTrace();
         int code = IPSWebserviceErrors.FAILED_ADD_FOLDER_CHILDREN;
         PSErrorException error = new PSErrorException(code, PSWebserviceErrors
            .createErrorMessage(code, childIds.toString(), parentId.toString(),
               e.getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
         throw error;
      }
   }

   // @see IPSContentWs#addFolderChildren(String, List, String)
   public void addFolderChildren(String parentPath, List<IPSGuid> childIds)
      throws PSErrorException
   {
      if (StringUtils.isBlank(parentPath))
         throw new IllegalArgumentException(
            "parentPath must not be null or empty.");
      PSWebserviceUtils.validateLegacyGuids(childIds);

      int parentId = getFolderIdFromPath(parentPath, true);
      addFolderChildren(new PSLegacyGuid(parentId, 1), childIds);
   }

   @Deprecated
   public List<PSFolder> addFolderTree(String path,
      @SuppressWarnings("unused") String user)
      throws PSErrorResultsException, PSErrorException
   {
      return addFolderTree(path);
   }

   public List<PSFolder> addFolderTree(String path)
   throws PSErrorResultsException, PSErrorException
   {
      return addFolderTree(path, true);
   }

   public List<PSFolder> addFolderTree(String path, boolean loadTransientData)
      throws PSErrorResultsException, PSErrorException
   {
      if (StringUtils.isBlank(path))
         throw new IllegalArgumentException("path must not be null or empty.");

      Stack<String> names = new Stack<String>();
      List<PSFolder> folders = new ArrayList<PSFolder>();
      int parentId = lookupParentId(path, names);
      PSErrorResultsException results = new PSErrorResultsException();

      // Do not allow folders with a space,  but still handle existing folders
      // that may already have spaces until we clean up.

      for (String invalidPathItemToTest : names)
      {
         if (PSFolderPathUtils.testHasInvalidChars(invalidPathItemToTest))
            throw new IllegalArgumentException("Cannot create a folder containing the following characters" + IPSConstants.INVALID_ITEM_NAME_CHARACTERS);
      }

      while (!names.empty())
      {
         try
         {
            PSFolder folder = addFolder(names.pop(), parentId, parentId, loadTransientData);
            folders.add(folder);
            parentId = folder.getLocator().getId();
            results.addResult(new PSLegacyGuid(folder.getLocator()), folder);
         }
         catch (PSErrorException e)
         {
            results.addError(new PSLegacyGuid(-1, -1), e);
            throw results;
         }
      }

      return folders;
   }

   /**
    * Search the 1st existing folder from the specified folder path. The lookup
    * process is from the longest path towards to the root.
    *
    * @param origPath the path used to search from, assumed not
    *    <code>null</code> or empty.
    * @param childNames the stack of non-existing folder names, which will be
    *    used to create child folder. Assumed not <code>null</code>.
    * @return the folder id of the 1st existing folder when performing
    *   backwords lookups.
    * @throws PSErrorException if an error occurs.
    */
   private int lookupParentId(String origPath, Stack<String> childNames)
      throws PSErrorException
   {
      String path = origPath;
      // remove the '/' at the end if any
      if (path.charAt(path.length() - 1) == '/')
         path = path.substring(0, path.length() - 1);

      int index;
      int parentId = -1;
      while (parentId == -1)
      {
         parentId = getFolderIdFromPath(path, false);
         if (parentId != -1)
            break;

         index = path.lastIndexOf("/");
         if (index == -1 || path.equals("/"))
            throw new IllegalArgumentException(
               "Cannot find existing folder from path: '" + origPath + "'.");

         childNames.push(path.substring(index + 1));
         path = path.substring(0, index);
      }

      return parentId;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.webservices.content.IPSContentWs#deleteFolders(java.util.List, boolean)
    */
   public void deleteFolders(List<IPSGuid> ids, boolean purgeItems)
   throws PSErrorsException
   {
      deleteFolders(ids, purgeItems, true);
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.webservices.content.IPSContentWs#deleteFolders(java.util.List, boolean, boolean)
    */
   public void deleteFolders(List<IPSGuid> ids, boolean purgeItems,
         boolean checkFolderPermission) throws PSErrorsException
   {
      PSWebserviceUtils.validateLegacyGuids(ids);

      PSErrorsException errors = new PSErrorsException();
      PSRequest request = getRequest();
      PSServerFolderProcessor processor = getFolderProcessor();
      for (IPSGuid id : ids)
      {
         try
         {
            int contentId = ((PSLegacyGuid) id).getContentId();
            if (purgeItems)
            {
               PSFolderHandler.purgeFolderAndChildItems(contentId, request);
            }
            else
            {
               PSKey[] keys = new PSKey[] { new PSLocator(contentId, 1) };
               processor.deleteFolders(keys, checkFolderPermission, RECYCLE_RELATE_TYPE);
            }
            errors.addResult(id);
         }
         catch (PSException e)
         {
            e.printStackTrace();
            int code = IPSWebserviceErrors.DELETE_FAILED;
            PSErrorException error = new PSErrorException(code,
               PSWebserviceErrors.createErrorMessage(code, PSFolder.class
                  .getName(), id.toString(), e.getLocalizedMessage()),
               ExceptionUtils.getFullStackTrace(e));
            errors.addError(id, error);
         }
      }

      if (errors.hasErrors())
         throw errors;
   }

   @Deprecated
   public List<PSItemSummary> findFolderChildren(IPSGuid id,
      boolean isLoadOperations, @SuppressWarnings("unused")
      String user) throws PSErrorException
   {
      return findFolderChildren(id, isLoadOperations);
   }

   public List<PSItemSummary> findFolderParents(IPSGuid id,
         boolean isLoadOperation)
   {
      PSWebserviceUtils.validateLegacyGuid(id);

      PSLocator parentId = new PSLocator(((PSLegacyGuid) id).getContentId(), 1);
      try
      {
         PSComponentSummary[] children = getFolderProcessor()
            .getParentSummaries(parentId);

         // convert to the returned format
         PSComponentSummaries summs = new PSComponentSummaries();
         for (PSComponentSummary comp : children)
            summs.add(comp);
         return getItemSummaries(summs, isLoadOperation);
      }
      catch (PSCmsException e)
      {
         e.printStackTrace();
         throw new RuntimeException("Failed to find folder parent for id="
               + id.toString(), e);
      }

   }
   public List<PSItemSummary> findFolderChildren(IPSGuid id,
      boolean isLoadOperation) throws PSErrorException
   {
      PSWebserviceUtils.validateLegacyGuid(id);

      PSLocator parentId = new PSLocator(((PSLegacyGuid) id).getContentId(), 1);
      try
      {
         PSComponentSummary[] children = getFolderProcessor()
            .getChildSummaries(parentId);

         // convert to the returned format
         PSComponentSummaries summs = new PSComponentSummaries();
         for (PSComponentSummary comp : children)
            summs.add(comp);
         return getItemSummaries(summs, isLoadOperation);
      }
      catch (PSCmsException e)
      {
         e.printStackTrace();
         int code = IPSWebserviceErrors.FAILED_FIND_FOLDER_CHILDREN;
         PSErrorException error = new PSErrorException(code, PSWebserviceErrors
            .createErrorMessage(code, id.toString(), e.getLocalizedMessage()),
            ExceptionUtils.getFullStackTrace(e));
         throw error;
      }
   }

   public List<PSItemSummary> findChildFolders(IPSGuid id) throws PSErrorException
   {
      PSWebserviceUtils.validateLegacyGuid(id);

      PSLocator parentId = new PSLocator(((PSLegacyGuid) id).getContentId(), 1);
      try
      {
         PSComponentSummaries summs = getFolderProcessor()
            .getChildFolderSummaries(parentId);

         // convert to the returned format
         return getItemSummaries(summs, false);
      }
      catch (PSCmsException e)
      {
         e.printStackTrace();
         int code = IPSWebserviceErrors.FAILED_FIND_FOLDER_CHILDREN;
         PSErrorException error = new PSErrorException(code, PSWebserviceErrors
            .createErrorMessage(code, id.toString(), e.getLocalizedMessage()),
            ExceptionUtils.getFullStackTrace(e));
         throw error;
      }
   }

   public List<PSItemSummary> findFolderChildren(String path,
      boolean isLoadOperations, @SuppressWarnings("unused")
      String user) throws PSErrorException
   {
      return findFolderChildren(path, isLoadOperations);
   }

   public List<PSItemSummary> findFolderChildren(String path,
      boolean isLoadOperation) throws PSErrorException
   {
      if (StringUtils.isBlank(path))
         throw new IllegalArgumentException("path must not be null or empty.");

      int parentId = getFolderIdFromPath(path, false);
      if (parentId == -1)
         return Collections.emptyList();

      return findFolderChildren(new PSLegacyGuid(parentId, 1), isLoadOperation);
   }

   /*
    * //see base interface method for details
    */
   public boolean isChildExistInFolder(String folderPath, String name)
   {
      String path;
      if (folderPath.charAt(folderPath.length()-1) == '/')
         path = folderPath + name;
      else
         path = folderPath + "/" + name;

      IPSGuid id = getIdByPath(path);
      return id != null;
   }

   /*
    * //see base interface method for details
    */
   public boolean isChildExistInFolder(IPSGuid folderId, String name)
   {
      String[] paths = findItemPaths(folderId);
      if (paths == null || paths.length == 0)
         return false;

      return isChildExistInFolder(paths[0], name);
   }

   // @see IPSContentWs#findFolderPaths(IPSGuid)
   @Override
   public String[] findFolderPaths(IPSGuid id) throws PSErrorException
   {
      PSWebserviceUtils.validateLegacyGuid(id);
      PSLocator locator = new PSLocator(((PSLegacyGuid) id).getContentId(), -1);
      try
      {
         String[] paths = getFolderProcessor().getFolderPaths(locator);
         return paths;
      }
      catch (PSCmsException e)
      {
         e.printStackTrace();
         int code = IPSWebserviceErrors.FAILED_FIND_PATH_FROM_ID;
         PSErrorException error = new PSErrorException(code,
            PSWebserviceErrors.createErrorMessage(code, locator.getId(), e
               .getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
         throw error;
      }
   }

   @Override
   public String[] findFolderPaths(IPSGuid id, String relationshipTypeName) throws PSErrorException
   {
      PSWebserviceUtils.validateLegacyGuid(id);
      PSLocator locator = new PSLocator(((PSLegacyGuid) id).getContentId(), -1);
      try
      {
         String[] paths = getFolderProcessor().getFolderPaths(locator, relationshipTypeName);
         return paths;
      }
      catch (PSCmsException e)
      {
         e.printStackTrace();
         int code = IPSWebserviceErrors.FAILED_FIND_PATH_FROM_ID;
         PSErrorException error = new PSErrorException(code,
                 PSWebserviceErrors.createErrorMessage(code, locator.getId(), e
                         .getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
         throw error;
      }
   }

   // @see IPSContentWs#findPathIds(String)
   public List<IPSGuid> findPathIds(String path) throws PSErrorException
   {
      if (StringUtils.isBlank(path))
         throw new IllegalArgumentException("path must be null or empty.");

      // remove the '/' at the end if any
      if (path.charAt(path.length() - 1) == '/')
         path = path.substring(0, path.length() - 1);

      List<IPSGuid> result = new ArrayList<IPSGuid>();
      while (path.length() > 0)
      {
         if (path.equals("/")) // exclude the root id
            break;

         int id = getFolderIdFromPath(path, true);
         result.add(new PSLegacyGuid(id, -1));

         int index = path.lastIndexOf("/");
         path = path.substring(0, index);
      }

      Collections.reverse(result);
      return result;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.webservices.content.IPSContentWs#loadFolder(com.percussion.utils.guid.IPSGuid)
    */
   public PSFolder loadFolder(IPSGuid id) throws PSErrorException
   {
      return loadFolder(id, true);
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.webservices.content.IPSContentWs#loadFolder(com.percussion.utils.guid.IPSGuid, boolean)
    */
   public PSFolder loadFolder(IPSGuid id, boolean loadTransientData)
         throws PSErrorException
   {
      PSServerFolderProcessor processor = getFolderProcessor();
      try
      {
         PSFolder folder = processor.openFolder(new PSLocator(
               ((PSLegacyGuid) id).getContentId(), -1));
         if (loadTransientData)
            setFolderTransientData(folder);

         return folder;
      }
      catch (PSCmsException e)
      {
         int code = IPSWebserviceErrors.FAILED_LOAD_FOLDER;
         PSErrorException error = new PSErrorException(code,
               PSWebserviceErrors.createErrorMessage(code, id.toString(), e
                     .getLocalizedMessage()), ExceptionUtils
                     .getFullStackTrace(e));

         throw error;
      }
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.webservices.content.IPSContentWs#loadFolders(java.util.List)
    */
   public List<PSFolder> loadFolders(List<IPSGuid> ids)
      throws PSErrorResultsException
   {
      return loadFolders(ids, true);
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.webservices.content.IPSContentWs#loadFolders(java.util.List, boolean)
    */
   @SuppressWarnings("unchecked")
   public List<PSFolder> loadFolders(List<IPSGuid> ids,
         boolean loadTransientData) throws PSErrorResultsException
   {
      PSWebserviceUtils.validateLegacyGuids(ids);

      PSErrorResultsException results = new PSErrorResultsException();
      for (IPSGuid id : ids)
      {
         try
         {
            PSFolder folder = loadFolder(id, loadTransientData);
            results.addResult(id, folder);
         }
         catch (PSErrorException e)
         {
            results.addError(id, e);
         }
      }

      if (results.hasErrors())
         throw results;

      return results.getResults(ids);
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.webservices.content.IPSContentWs#findDescendantFolders(com.percussion.utils.guid.IPSGuid)
    */
   public List<PSFolder> findDescendantFolders(IPSGuid id)
   {
      notNull(id);

      List<IPSGuid> ids = getDescendantFolderIds(id);
      try
      {
         if (ids.isEmpty())
            return Collections.EMPTY_LIST;

         return loadFolders(ids, false);
      }
      catch (PSErrorResultsException e)
      {
         throw new PSErrorException(
               "Failed to load some descendant folders for folder id = "
                     + id.toString(), e);
      }
   }

   /**
    * Gets all descendant folder IDs for the specified folder.
    * @param id the ID of the folder, assumed not <code>null</code>.
    * @return the list of descendant folder IDs, never <code>null</code>, but may be empty.
    */
   private List<IPSGuid> getDescendantFolderIds(IPSGuid id)
   {
      try
      {
         PSServerFolderProcessor processor = getFolderProcessor();
         PSLocator folderId = ((PSLegacyGuid)id).getLocator();
         PSLocator[] locators;

         locators = processor.getDescendentFolderLocators(folderId);
         List<IPSGuid> ids = new ArrayList<IPSGuid>();
         for (PSLocator locator : locators)
         {
            IPSGuid guid = new PSLegacyGuid(locator);
            ids.add(guid);
         }
         return ids;
      }
      catch (PSCmsException e)
      {
         throw new PSErrorException(
               "Failed to get all descendant folder IDs for folder id = "
                     + id.toString(), e);
      }
   }


   /**
    * Set transient data for the specified folder. The transient data will
    * not persisted with the folder when the folder is saved into the
    * repository, such as community name, display name, folder path, ...etc.
    *
    * @param folder the specified folder, assumed not <code>null</code>.
    * @throws PSErrorException if an error occurred when failed to load
    *    the transient data.
    */
   private void setFolderTransientData(PSFolder folder) throws PSErrorException
   {
      // set folder path and guid
      PSLegacyGuid folderId = new PSLegacyGuid(folder.getLocator().getId(), -1);
      folder.setFolderPath(loadFolderPath(folderId));
      folder.setGuid(folderId);

      IPSGuid id;
      // set community name
      if (folder.getCommunityId() != -1)
      {
         id = new PSGuid(PSTypeEnum.COMMUNITY_DEF, folder.getCommunityId());
         IPSBackEndRoleMgr roleMgr = PSRoleMgrLocator.getBackEndRoleManager();
         PSCommunity community;
         try
         {
            community = roleMgr.loadCommunity(id);
            folder.setCommunityName(community.getName());
         }
         catch (PSSecurityException e)
         {
            e.printStackTrace();
            int code = IPSWebserviceErrors.LOAD_FAILED;
            PSErrorException error = new PSErrorException(code,
               PSWebserviceErrors.createErrorMessage(code, PSCommunity.class
                  .getName(), id.longValue(), e.getLocalizedMessage()),
               ExceptionUtils.getFullStackTrace(e));
            throw error;

         }
      }

      // set display format name
      if (folder.getDisplayFormatId() != -1)
      {
         IPSUiDesignWs uiws = PSUiWsLocator.getUiDesignWebservice();
         id = new PSGuid(PSTypeEnum.DISPLAY_FORMAT, folder.getDisplayFormatId());
         List<IPSGuid> ids = new ArrayList<IPSGuid>();
         ids.add(id);

         try
         {
            List<PSDisplayFormat> dfs = uiws.loadDisplayFormats(ids, false,
               false, "", "");
            folder.setDisplayFormatName(dfs.get(0).getName());
         }
         catch (PSErrorResultsException e)
         {
            e.printStackTrace();
            int code = IPSWebserviceErrors.LOAD_FAILED;
            PSErrorException error = new PSErrorException(code,
               PSWebserviceErrors.createErrorMessage(code,
                  PSDisplayFormat.class.getName(), id.longValue(), e
                     .getLocalizedMessage()), ExceptionUtils
                  .getFullStackTrace(e));
            throw error;
         }
      }
   }

   /**
    * Gets a new instance of the folder processor, which is created from
    * the current request.
    *
    * @return the created folder processor, never <code>null</code>.
    */
   private PSServerFolderProcessor getFolderProcessor()
   {
      return PSServerFolderProcessor.getInstance();
   }

   // @see IPSContentWs#loadFolders(String[])
   @SuppressWarnings("unchecked")
   public List<PSFolder> loadFolders(String[] paths)
      throws PSErrorResultsException
   {
      if (paths == null || paths.length == 0)
         throw new IllegalArgumentException("paths may not be null or empty.");

      for (String path : paths)
         if (StringUtils.isBlank(path))
            throw new IllegalArgumentException(
               "folder paths entries cannot be null or empty.");

      PSServerFolderProcessor processor = getFolderProcessor();

      int nextFakeFolderId = Integer.MAX_VALUE;

      List<IPSGuid> ids = new ArrayList<IPSGuid>();
      PSErrorResultsException results = new PSErrorResultsException();
      for (String path : paths)
      {
         IPSGuid id = null;
         try
         {
            id = new PSLegacyGuid(getFolderIdFromPath(path, true), -1);

            PSFolder folder = processor.openFolder(new PSLocator(
               ((PSLegacyGuid) id).getContentId(), -1));
            setFolderTransientData(folder);

            results.addResult(id, folder);
         }
         catch (PSCmsException e)
         {
            // id is always valid if we get here
            int code = IPSWebserviceErrors.FAILED_LOAD_FOLDER;
            PSErrorException error = new PSErrorException(code,
               PSWebserviceErrors.createErrorMessage(code, id.toString(), e
                  .getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));

            results.addError(id, error);
         }
         catch (PSErrorException e)
         {
            /*
             * The id may not be valid if we get here, create a fake id for
             * the error results.
             */
            if (id == null)
               id = new PSLegacyGuid(nextFakeFolderId--, -1);
            results.addError(id, e);
         }

         ids.add(id);
      }

      if (results.hasErrors())
         throw results;

      return results.getResults(ids);
   }

   /**
    * Gets the folder id from the specified folder path.
    *
    * @param path the folder path, assumed not <code>null</code> or empty.
    * @param isRequired <code>true</code> if the specified path must be an
    *    existing folder; otherwise the specified path may not be an existing
    *    folder.
    * @return the folder id of the specified folder path. It may be
    *    <code>-1</code> if cannot find a folder with the path and the
    *    isRequired parameter is <code>false</code>.
    * @throws PSErrorException if an error occurs or cannot find the specified
    *    folder and the isRequired parameter is <code>null</code>.
    */
   private int getFolderIdFromPath(String path, boolean isRequired) throws PSErrorException {
      return getFolderIdFromPath(path, isRequired, FOLDER_RELATE_TYPE);
   }

   private int getFolderIdFromPath(String path, boolean isRequired, String relationshipTypeName)
      throws PSErrorException
   {
      PSRelationshipProcessor processor = PSWebserviceUtils
         .getRelationshipProcessor();

      try
      {
         int id = processor.getIdByPath(
            PSRelationshipProcessorProxy.RELATIONSHIP_COMPTYPE, path,
                 relationshipTypeName);

         if (id == -1 && isRequired)
         {
            int code = IPSWebserviceErrors.PATH_NOT_EXIST;
            PSErrorException error = new PSErrorException(code,
               PSWebserviceErrors.createErrorMessage(code, path),
               ExceptionUtils.getFullStackTrace(new Exception()));
            throw error;
         }
         return id;
      }
      catch (PSCmsException e)
      {
         e.printStackTrace();
         int code = IPSWebserviceErrors.FAILED_FIND_ID_FROM_PATH;
         PSErrorException error = new PSErrorException(code, PSWebserviceErrors
            .createErrorMessage(code, path, e.getLocalizedMessage()),
            ExceptionUtils.getFullStackTrace(e));
         throw error;
      }
   }

   /**
    * Gets the folder id based upon the folder property.
    *
    *
    *
    * @param property to search for. assumed not <code>null</code> or empty.

    */
   public List<PSFolder>  getFoldersByProperty(String property)  throws PSErrorException {


      IPSContentService service = PSContentServiceLocator.getContentService();

      List<PSFolderProperty>  propertiesList = service.getFolderProperties(property);

      List<IPSGuid> ids = new ArrayList<IPSGuid>();


      for(PSFolderProperty folderProperty: propertiesList){

         //set the GUID of the folder from the property
         PSGuid guid = new PSLegacyGuid(folderProperty.getContentID());

         ids.add(guid);
      }


      List<PSFolder> folders = new ArrayList<PSFolder>();

      try
      {
         if (ids != null && ids.size() > 0){
            //convert folder GUIDs to folders
            folders = loadFolders(ids);
         }

      }
      catch (PSErrorResultsException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

      return folders;
   }











   /**
    * @see IPSContentWs#moveFolderChildren(IPSGuid, IPSGuid, List)
    */
   public void moveFolderChildren(IPSGuid sourceId, IPSGuid targetId,
      List<IPSGuid> childIds) throws PSErrorException
   {
      moveFolderChildren(sourceId, targetId, childIds, true);
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.webservices.content.IPSContentWs#moveFolderChildren(com.percussion.utils.guid.IPSGuid, com.percussion.utils.guid.IPSGuid, java.util.List, boolean)
    */
   public void moveFolderChildren(IPSGuid sourceId, IPSGuid targetId,
         List<IPSGuid> childIds, boolean checkFolderPermission)
         throws PSErrorException
   {
      // validate ids
      PSWebserviceUtils.validateLegacyGuid(sourceId);
      PSWebserviceUtils.validateLegacyGuid(targetId);
      if (childIds != null && (!childIds.isEmpty()))
         PSWebserviceUtils.validateLegacyGuids(childIds);

      // get child locators
      List<PSComponentSummary> children = getChildSummaries(sourceId, childIds, false);
      validateMovedItemInTargetFolder(targetId, children);

      List<PSLocator> childLocators = new ArrayList<PSLocator>();
      for (PSComponentSummary child : children)
      {
         childLocators.add(child.getCurrentLocator());
      }

      PSLocator src = new PSLocator(((PSLegacyGuid) sourceId).getContentId(),
         -1);
      PSLocator tgt = new PSLocator(((PSLegacyGuid) targetId).getContentId(),
         -1);

      try
      {
         getFolderProcessor().moveFolderChildren(src, childLocators, tgt,
               false, checkFolderPermission);
      }
      catch (PSCmsException e)
      {
         throwUnexpectedError(e);
      }
   }

   /**
    * Make sure the specified folder does not contain any item with the same
    * name as the specified item list.
    *
    * @param folderId the ID of the folder in question, assumed not
    * <code>null</code>.
    * @param items the list of items, assumed not <code>null</code>, may be
    * empty.
    */
   private void validateMovedItemInTargetFolder(IPSGuid folderId,
         List<PSComponentSummary> items)
   {
      String[] paths = findItemPaths(folderId);
      if (paths == null || paths.length == 0)
      {
         throw new PSErrorException("Failed to move items to folder (id="
               + folderId.toString() + ") because the folder does not exist.");
      }
      String path = paths[0];

      for (PSComponentSummary child : items)
      {
         if (isChildExistInFolder(path, child.getName()))
         {
            String msg = "Cannot move item (id="
                  + child.getContentId()
                  + ", name="
                  + child.getName()
                  + ") to folder (path="
                  + path
                  + ", id="
                  + ((PSLegacyGuid) folderId).getContentId()
                  + ") because a child item with the same name already exists in the folder.";
            ms_logger.warn(msg);
            throw new PSErrorException(msg);
         }
      }
   }

   // @see IPSContentWs#moveFolderChildren(String, String, List, String, String)
   public void moveFolderChildren(String sourcePath, String targetPath,
      List<IPSGuid> childIds) throws PSErrorException
   {
      if (StringUtils.isBlank(sourcePath))
         throw new IllegalArgumentException(
            "sourcePath may not be null or empty.");

      if (StringUtils.isBlank(targetPath))
         throw new IllegalArgumentException(
            "targetPath may not be null or empty.");

      int srcId = getFolderIdFromPath(sourcePath, true);
      int tgtId = getFolderIdFromPath(targetPath, true);

      PSLegacyGuid source = new PSLegacyGuid(srcId, -1);
      PSLegacyGuid target = new PSLegacyGuid(tgtId, -1);

      moveFolderChildren(source, target, childIds);
   }

   // @see IPSContentWs#removeFolderChildren(IPSGuid, List, boolean)
   public void removeFolderChildren(IPSGuid parentId, List<IPSGuid> childIds,
      boolean purgeItems) throws PSErrorsException, PSErrorException
   {
      removeFolderChildren(parentId, childIds, purgeItems, FOLDER_RELATE_TYPE);
   }

   public void removeFolderChildren(IPSGuid parentId, List<IPSGuid> childIds,
                                    boolean purgeItems, String relationshipTypeName) throws PSErrorsException, PSErrorException
   {
      // validate ids
      PSWebserviceUtils.validateLegacyGuid(parentId);
      if (childIds != null && (!childIds.isEmpty()))
         PSWebserviceUtils.validateLegacyGuids(childIds);

      List<PSComponentSummary> children = getChildSummaries(parentId, childIds, purgeItems);
      PSErrorsException results = new PSErrorsException();

      // separate sub-folders and items
      List<PSKey> itemLocators = new ArrayList<PSKey>();
      List<IPSGuid> subFolders = new ArrayList<IPSGuid>();
      for (PSComponentSummary comp : children)
      {
         if (comp.isFolder())
         {
            PSLegacyGuid id = new PSLegacyGuid(comp.getContentId(), -1);
            subFolders.add(id);
         }
         else
         {
            itemLocators.add(new PSLocator(comp.getContentId(), -1));
         }
      }

      // remove all sub-folders
      if (!subFolders.isEmpty())
      {
         deleteFolders(subFolders, purgeItems);
      }

      // collecting the successful ids
      for (IPSGuid id : subFolders)
      {
         results.addResult(id);
      }

      PSRequest request = getRequest();

      // process items, one at a time
      for (PSKey k : itemLocators)
      {
         PSLocator locator = (PSLocator) k;
         PSLegacyGuid id = new PSLegacyGuid(locator);
         List<PSKey> singleKey = Collections.singletonList(k);
         try
         {
            // remove folder relationship between the parent and item children
            PSLocator parent = new PSLocator(((PSLegacyGuid) parentId)
                    .getContentId(), -1);
            getFolderProcessor().delete(parent, singleKey, relationshipTypeName);

            // purge the item if requeted
            if (purgeItems)
            {
               List<String> singleId = Collections.singletonList(String
                       .valueOf(locator.getId()));
               PSContentDataHandler.purgeItems(request, singleId);
            }
            results.addResult(id);
         }
         catch (PSCmsException e1) // failed to attach folder children
         {
            e1.printStackTrace();
            int errorCode = IPSWebserviceErrors.FAILED_DELETE_RELATIONSHIPS;
            PSErrorException error = new PSErrorException(errorCode,
                    PSWebserviceErrors.createErrorMessage(errorCode, e1
                            .getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e1));
            results.addError(id, error);
         }
         catch (PSException e) // failed to delete item
         {
            e.printStackTrace();
            int code = IPSWebserviceErrors.DELETE_FAILED;
            PSErrorException error = new PSErrorException(code,
                    PSWebserviceErrors.createErrorMessage(code, "Item", id
                            .toString(), e.getLocalizedMessage()), ExceptionUtils
                    .getFullStackTrace(e));
            results.addError(id, error);
         }
      }

      if (results.hasErrors())
         throw results;
   }

   /**
    * Get a new request based on the current user's session.
    *
    * @return The request, never <code>null</code>.
    *
    * @throws IllegalStateException if the current thread has not had a request
    * initialized.
    */
   private PSRequest getNewRequest()
   {
      return new PSRequest(getRequest().getSecurityToken());
   }

   /**
    * Gets the summaries for the specified parent id and its child ids.
    * Validates the child ids for the specified parent folder id if the child
    * ids is specified, not <code>null</code> or empty; otherwise get the
    * summaries for all children of the specified folder parent.
    *
    * @param parentId the specified folder id, assumed not <code>null</code>
    *    and is an instance of {@link PSLegacyGuid}.
    * @param childIds a list of {@link PSLegacyGuid} instances. It may be
    *    <code>null</code> or empty.
    * @param purgeItems <code>true</code> if items are being purged.
    *                   This indicates whether or not the recycle bin is purging items
    *                   or if they are being moved to recycle bin from folder.
    * @return a list of child summaries for the specified folder, never
    *    <code>null</code>, may be empty.
    * @throws PSErrorException if any of the specified child ids is not a child
    *    of the specified folder parent; or any error occurs.
    */
   private List<PSComponentSummary> getChildSummaries(IPSGuid parentId,
      List<IPSGuid> childIds, boolean purgeItems) throws PSErrorException
   {
      PSServerFolderProcessor processor = getFolderProcessor();

      PSLocator parent = new PSLocator(
         ((PSLegacyGuid) parentId).getContentId(), -1);
      Set<Integer> allChildIds = null;
      Set<Integer> tgtChildsIds = new HashSet<Integer>();
      try
      {
         String relType = purgeItems ? RECYCLE_RELATE_TYPE : FOLDER_RELATE_TYPE;
         allChildIds = processor.getChildIds(parent, false, relType);
         // take all child ids if not specified
         if (childIds == null || childIds.isEmpty())
         {
            tgtChildsIds = allChildIds;
         }
         else
         {
            // validating the specified child ids
            for (IPSGuid id : childIds)
            {
               PSLegacyGuid guid = (PSLegacyGuid) id;
               if (!allChildIds.contains(guid.getContentId()))
               {
                  int code = IPSWebserviceErrors.INVALID_FOLDER_CHILD;
                  PSErrorException error = new PSErrorException(code,
                     PSWebserviceErrors.createErrorMessage(code,
                        parent.getId(), guid.getContentId()), ExceptionUtils
                        .getFullStackTrace(new Exception()));
                  throw error;
               }
               tgtChildsIds.add(guid.getContentId());
            }
         }

         // get child summaries
         PSComponentSummary[] children = processor.getChildSummaries(parent, relType);
         List<PSComponentSummary> result = new ArrayList<PSComponentSummary>();
         for (PSComponentSummary comp : children)
         {
            if (tgtChildsIds.contains(comp.getContentId()))
            {
               result.add(comp);
            }
         }

         return result;
      }
      catch (PSCmsException e)
      {
         e.printStackTrace();
         int code = IPSWebserviceErrors.FAILED_FIND_CHILD_ITEMS;
         PSErrorException error = new PSErrorException(code, PSWebserviceErrors
            .createErrorMessage(code, parent.getId(), e.getLocalizedMessage()),
            ExceptionUtils.getFullStackTrace(e));
         throw error;
      }
   }

   // @see IPSContentWs#removeFolderChildren(String, List, boolean)
   public void removeFolderChildren(String parentPath, List<IPSGuid> childIds,
      boolean purgeItems) throws PSErrorsException, PSErrorException
   {
      if (StringUtils.isBlank(parentPath))
         throw new IllegalArgumentException(
            "parentPath may not be null or empty.");
      if (childIds != null && (!childIds.isEmpty()))
         PSWebserviceUtils.validateLegacyGuids(childIds);

      String relTypeName = purgeItems ? RECYCLE_RELATE_TYPE : FOLDER_RELATE_TYPE;
      int parentId = getFolderIdFromPath(parentPath, true, relTypeName);
      PSLegacyGuid parent = new PSLegacyGuid(parentId, -1);
      removeFolderChildren(parent, childIds, purgeItems, relTypeName);
   }

   /**
    * @see IPSContentWs#saveFolders(List)
    */
   @SuppressWarnings("unchecked")
   public List<IPSGuid> saveFolders(List<PSFolder> folders)
      throws PSErrorResultsException
   {
      if (folders == null || folders.isEmpty())
         throw new IllegalArgumentException(
            "folders must not be null or empty.");

      for (PSFolder folder : folders)
         if (folder.getLocator().getId() == -1)
            throw new IllegalArgumentException("all folders must exist");

      List<IPSGuid> originalIds = new ArrayList<IPSGuid>();
      PSErrorResultsException results = new PSErrorResultsException();
      for (PSFolder folder : folders)
      {
         try
         {
            IPSGuid originalId = new PSLegacyGuid(folder.getLocator());
            originalIds.add(originalId);

            PSFolder savedFolder = saveFolder(folder);
            IPSGuid newId = new PSLegacyGuid(savedFolder.getLocator());
            results.addResult(originalId, newId);
         }
         catch (PSErrorException e)
         {
            PSGuid id = new PSLegacyGuid(folder.getLocator());
            results.addError(id, e);
         }
      }

      if (results.hasErrors())
         throw results;

      return results.getResults(originalIds);
   }

   /**
    * Saves the specified folder to the repository.
    *
    * @param folder the to be saved folder, never <code>null</code>.
    *    It may or may not be a persisted folder.
    *
    * @return the saved folder which contains all persisted keys and data.
    *
    * @throws PSErrorException if failed to save the specified folder.
    */
   public PSFolder saveFolder(PSFolder folder) throws PSErrorException
   {
      notNull(folder);

      try
      {
         PSServerFolderProcessor processor = getFolderProcessor();

         PSFolder savedFolder;
         if (folder.getLocator().getId() == -1)
         {
            savedFolder = processor.save(folder);
         }
         else
         {
            // if has assigned id, but not persisted, then assumed it is
            // an existing folder, and need to merge the new data to
            // the existing folder.
            folder = mergeFolderData(folder);

            savedFolder = processor.save(folder);
         }
         return savedFolder;
      }
      catch (PSCmsException e)
      {
         e.printStackTrace();
         int code = IPSWebserviceErrors.SAVE_FAILED;
         PSErrorException error = new PSErrorException(code, PSWebserviceErrors
            .createErrorMessage(code, PSFolder.class.getName(), folder
               .getLocator().getId(), e.getLocalizedMessage()), ExceptionUtils
            .getFullStackTrace(e));
         throw error;
      }
   }

   /**
    * Loads the specified folder and merges the folder data from the specified
    * folder to the loaded folder object. This is typically used when updating
    * an existing folder.
    *
    * @param folder the to be updated existing folder, assumed not
    *    <code>null</code>.
    * @throws PSCmsException if failed to load the specified folder.
    */
   private PSFolder mergeFolderData(PSFolder folder) throws PSCmsException
   {
      PSServerFolderProcessor processor = getFolderProcessor();
      PSFolder[] origFolder = processor.openFolder(new PSKey[] { folder
         .getLocator() });

      origFolder[0].mergeFrom(folder);
      return origFolder[0];
   }

   /**
    * Get the item definition for the supplied parameters.
    *
    * @param contentType the name of the content type for which to get the
    *    item definition, assumed not <code>null</code> or empty.
    *
    * @return the requested item definition, never <code>null</code>.
    * @throws PSInvalidContentTypeException if the supplied content type is
    *    unknown.
    */
   private PSItemDefinition getItemDefinition(String contentType)
      throws PSInvalidContentTypeException
   {
      PSItemDefManager itemDefMgr = PSItemDefManager.getInstance();

      return itemDefMgr.getItemDef(contentType,
         getRequest().getSecurityToken());
   }

   /**
    * Get the item definition for the supplied parameters.
    *
    * @param contentTypeId the content type id for which to get the
    *    item definition.
    * @return the requested item definition, never <code>null</code>.
    * @throws PSInvalidContentTypeException if the supplied content type id is
    *    unknown.
    */
   private PSItemDefinition getItemDefinition(long contentTypeId)
      throws PSInvalidContentTypeException
   {
      PSItemDefManager itemDefMgr = PSItemDefManager.getInstance();

      return itemDefMgr.getItemDef(contentTypeId,
         getRequest().getSecurityToken());
   }

   /**
    * Validate that the supplied relationship type exists for the specified
    * category. Throws an <code>IllegalArgumentException</code> if the specified
    * relationship does not exist.
    *
    * @param type the relationship type to validate, assumed not
    *    <code>null</code> or empty.
    * @param category the catagory for which to validate, assumed not
    *    <code>null</code> or empty.
    * @throws PSErrorException for any unexpected error.
    */
   private void validateRelationshipType(String type, String category)
      throws PSErrorException
   {
      IPSSystemDesignWs service = PSSystemWsLocator.getSystemDesignWebservice();
      List<IPSCatalogSummary> relationships = service.findRelationshipTypes(
         type, category);

      if (relationships.size() == 0)
         throw new IllegalArgumentException("unknown relationship type \""
            + type + "\" for category \"" + category + "\".");
   }

   /**
    * Create a new copy for the identified item.
    *
    * @param id the id of the item to create a new copy for, not
    *    <code>null</code>.
    * @param type the relationship type, <code>null</code> or empty to use
    *    the default system new copy relationship.
    * @param resource the resource name to use to make the internal request,
    *    not <code>null</code> or empty.
    *
    * @return the id of the new copied item, never <code>null</code>.
    * @throws PSException for any error creating the new copy.
    */
   private IPSGuid createNewCopy(IPSGuid id, String type, String resource)
      throws PSException
   {
      if (StringUtils.isBlank(type))
         type = PSRelationshipConfig.TYPE_NEW_COPY;

      return createRelatedItem(id, type, resource, null);
   }

   /**
    * Create a new promotable version for the identified item.
    *
    * @param id the id of the item to create a new promotable version for, not
    *    <code>null</code>.
    * @param type the relationship type, <code>null</code> or empty to use
    *    the default system promotable relationship.
    * @param resource the resource name to use to make the internal request,
    *    not <code>null</code> or empty.
    * @return the id of the new promotable item, never <code>null</code>.
    * @throws PSException for any error creating the new promotable version.
    */
   private IPSGuid createNewPromotableVersion(IPSGuid id, String type,
      String resource) throws PSException
   {
      if (StringUtils.isBlank(type))
         type = PSRelationshipConfig.TYPE_PROMOTABLE_VERSION;

      return createRelatedItem(id, type, resource, null);
   }

   /**
    * Create a new translation for the identified item.
    *
    * @param id the id of the item to create a new translation for, not
    *    <code>null</code>.
    * @param type the relationship type, <code>null</code> or empty to use
    *    the default system translation relationship.
    * @param locale the locale code used for the new translation,
    *    not <code>null</code>.
    * @param resource the resource name to use to make the internal request,
    *    not <code>null</code> or empty.
    * @return the id of the new trnalation item, never <code>null</code>.
    * @throws PSException for any error creating the new translation.
    */
   private IPSGuid createNewTranslation(IPSGuid id, String type,
      final String locale, String resource)
      throws PSException
   {
      if (locale == null)
         throw new IllegalArgumentException("Locale cannot be null");

      if (StringUtils.isBlank(type))
         type = PSRelationshipConfig.TYPE_TRANSLATION;

      Map<String, String> params = new HashMap<String, String>();
      params.put(IPSHtmlParameters.SYS_LANG, locale);

      return createRelatedItem(id, type, resource, params);
   }

   /**
    * Create a new related item for the identified item and the specified
    * relationship type.
    *
    * @param id the id of the item to create a new related item for, not
    *    <code>null</code>.
    * @param type the relationship type, not <code>null</code> or empty,
    *    assumed to be a valid relationship type.
    * @param resource the resource name to use to make the internal request,
    *    not <code>null</code> or empty.
    * @param parameters additional service specific parameters, may be
    *    <code>null</code> or empty.
    * @return the id of the new related item, never <code>null</code>.
    * @throws PSException for any error creating the new related item.
    */
   private IPSGuid createRelatedItem(IPSGuid id, String type, String resource,
      Map<String, String> parameters) throws PSException
   {
      if (id == null)
         throw new IllegalArgumentException("id cannot be null");

      if (StringUtils.isBlank(type))
         throw new IllegalArgumentException("type cannot be null or empty");

      if (StringUtils.isBlank(resource))
         throw new IllegalArgumentException("resource cannot be null or empty");

      // create new request each time
      PSRequest request = getNewRequest();

      IPSInternalResultHandler rh = (IPSInternalResultHandler) PSServer
         .getInternalRequestHandler(resource);
      if (rh == null)
         throw new PSException(IPSServerErrors.CE_NEEDED_APP_NOT_RUNNING,
            resource);

      Map<String, String> params = null;
      if (parameters != null)
         params = parameters;
      else
         params = new HashMap<String, String>();
      params.put(IPSHtmlParameters.SYS_COMMAND,
         PSRelationshipCommandHandler.COMMAND_NAME);
      params.put(IPSHtmlParameters.SYS_CONTENTID, String
         .valueOf(((PSLegacyGuid) id).getContentId()));
      params.put(IPSHtmlParameters.SYS_REVISION, String
         .valueOf(((PSLegacyGuid) id).getRevision()));
      params.put(IPSHtmlParameters.SYS_RELATIONSHIPTYPE, type);
      request.setParameters((HashMap) params);
      PSExecutionData data = null;
      try
      {
         data = rh.makeInternalRequest(request);
      }
      finally
      {
         if(data != null)
            data.release();
      }

      return new PSLegacyGuid(Integer.valueOf(request
         .getParameter(IPSHtmlParameters.SYS_CONTENTID)), Integer
         .valueOf(request.getParameter(IPSHtmlParameters.SYS_REVISION)));
   }

   public IPSGuid getIdByPath(String path) throws PSErrorException
   {
      return getIdByPath(path, FOLDER_RELATE_TYPE);
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.webservices.content.IPSContentWs#getIdByPath(java.lang.String)
    */
   public IPSGuid getIdByPath(String path, String relationshipTypeName) throws PSErrorException
   {
      if (StringUtils.isBlank(path))
         throw new IllegalArgumentException("path must be null or empty.");
      PSRelationshipProcessor processor = PSWebserviceUtils
      .getRelationshipProcessor();
      IPSGuid guid = null;
      try
      {
         int id = processor.getIdByPath(
               PSRelationshipProcessorProxy.RELATIONSHIP_COMPTYPE, path,
               relationshipTypeName);
         if (id != -1)
         {
            guid = PSGuidManagerLocator.getGuidMgr().makeGuid(
                  new PSLocator(id, -1));
         }
      }
      catch (PSCmsException e)
      {
         e.printStackTrace();
         int code = IPSWebserviceErrors.FAILED_FIND_ID_FROM_PATH;
         PSErrorException error = new PSErrorException(code, PSWebserviceErrors
            .createErrorMessage(code, path, e.getLocalizedMessage()),
            ExceptionUtils.getFullStackTrace(e));
         throw error;
      }

      return guid;
   }

   /* (non-Javadoc)
    * @see com.percussion.webservices.content.IPSContentWs#loadDependentSlotContentRelationships(com.percussion.design.objectstore.PSLocator, com.percussion.utils.guid.IPSGuid)
    */
   public List<PSAaRelationship> loadDependentSlotContentRelationships(PSLocator dependent, IPSGuid slotid)
         throws PSErrorException
   {
      notNull(dependent);
      notNull(slotid);

      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setDependent(dependent);
      filter.setProperty(IPSHtmlParameters.SYS_SLOTID, String.valueOf(slotid.longValue()));
      filter.setCategory(PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY);

      List<PSAaRelationship> rels = loadContentRelations(filter, false);

      return rels;
   }

   /**
    * The logger for this class.
    */
   private static Logger ms_logger = Logger.getLogger(PSContentWs.class);

   private static final String FOLDER_RELATE_TYPE = PSRelationshipConfig.TYPE_FOLDER_CONTENT;
   private static final String RECYCLE_RELATE_TYPE = PSRelationshipConfig.TYPE_RECYCLED_CONTENT;

}
