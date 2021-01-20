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
package com.percussion.webservices.system.impl;

import com.percussion.cms.handlers.PSRelationshipCommandHandler;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.conn.PSServerException;
import com.percussion.design.objectstore.PSEntry;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipConfigSet;
import com.percussion.i18n.PSLocale;
import com.percussion.security.PSRoleEntry;
import com.percussion.security.PSUserEntry;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.server.PSUserSession;
import com.percussion.server.cache.PSCacheException;
import com.percussion.server.cache.PSCacheProxy;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.relationship.data.PSRelationshipConfigName;
import com.percussion.services.security.IPSBackEndRoleMgr;
import com.percussion.services.security.PSRoleMgrLocator;
import com.percussion.services.security.data.PSCommunity;
import com.percussion.services.system.IPSSystemService;
import com.percussion.services.system.PSSystemServiceLocator;
import com.percussion.services.system.data.PSContentStatusHistory;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSBaseBean;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.webservices.IPSWebserviceErrors;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.PSInvalidLocaleException;
import com.percussion.webservices.PSUserNotMemberOfCommunityException;
import com.percussion.webservices.PSWebserviceErrors;
import com.percussion.webservices.PSWebserviceUtils;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import com.percussion.webservices.system.IPSSystemDesignWs;
import com.percussion.webservices.system.IPSSystemWs;
import com.percussion.workflow.PSEntryNotFoundException;
import com.percussion.workflow.PSTransitionInfo;
import com.percussion.workflow.PSWorkFlowUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The public system webservice implementations. Note that service 
 * {@link #transitionItems(List, String, String)} is not transactional.
 */
@PSBaseBean("sys_systemWs")
public class PSSystemWs extends PSSystemBaseWs implements IPSSystemWs
{
   // @see IPSSystemWs#deleteRelationships(IPSGuid)
   @Transactional
   public void deleteRelationships(List<IPSGuid> ids) throws PSErrorsException,
      PSErrorException
   {
      if (ids == null || ids.isEmpty())
         throw new IllegalArgumentException("ids may not be null or empty.");

      PSWebserviceUtils.deleteRelationships(ids, false);
   }

   // @see IPSSystemWs#findDependents(IPSGuid, PSRelationshipFilter)
   @Transactional
   public List<IPSGuid> findDependents(IPSGuid id, PSRelationshipFilter filter)
      throws PSErrorException
   {
      return findItemIds(id, true, filter);
   }

   // @see IPSSystemWs#findOwners(IPSGuid, PSRelationshipFilter)
   @Transactional
   public List<IPSGuid> findOwners(IPSGuid id, PSRelationshipFilter filter)
      throws PSErrorException
   {
      return findItemIds(id, false, filter);
   }

   /**
    * Just like {@link IPSSystemDesignWs#findDependencies(List)} and 
    * {@link #findOwners(IPSGuid, PSRelationshipFilter)}, except the id is
    * either the owner id or dependent id according to the <code>isOwner</code>
    * parameter.
    *  
    * @param isOwnerId it is <code>true</code> if the specified <code>id</code> 
    *    parameter is an owner id, and this method will behave the way that 
    *    is described in {@link #findDependents(IPSGuid, PSRelationshipFilter)};
    *    otherwise the specified <code>id</code> parameter is a dependent id,
    *    and this method will behave the way that is described in 
    *    {@link #findOwners(IPSGuid, PSRelationshipFilter)}.
    *    
    * @see IPSSystemDesignWs#findDependencies(List)
    * @see #findOwners(IPSGuid, PSRelationshipFilter)
    */
   private List<IPSGuid> findItemIds(IPSGuid id, boolean isOwnerId,
      PSRelationshipFilter filter) throws PSErrorException
   {
      PSWebserviceUtils.validateLegacyGuid(id);
      if (filter == null)
         filter = new PSRelationshipFilter();
      if (isOwnerId)
         filter.setOwner(((PSLegacyGuid) id).getLocator());
      else
         filter.setDependent(((PSLegacyGuid) id).getLocator());

      List<PSRelationship> rels = PSWebserviceUtils.loadRelationships(filter);
      List<IPSGuid> ids = new ArrayList<IPSGuid>(rels.size());
      for (PSRelationship rel : rels)
      {
         if (isOwnerId)
            ids.add(new PSLegacyGuid(rel.getDependent()));
         else
            ids.add(new PSLegacyGuid(rel.getOwner()));
      }

      return ids;
   }

   // @see IPSSystemWs#loadAuditTrails(List<IPSGuid>)
   @Transactional
   public Map<IPSGuid, List<PSContentStatusHistory>> loadAuditTrails(
      List<IPSGuid> ids)
   {
      PSWebserviceUtils.validateLegacyGuids(ids);

      IPSSystemService sysSvc = PSSystemServiceLocator.getSystemService();

      Map<IPSGuid, List<PSContentStatusHistory>> result = new HashMap<IPSGuid, List<PSContentStatusHistory>>();
      for (IPSGuid id : ids)
      {
         List<PSContentStatusHistory> histList = sysSvc
            .findContentStatusHistory(id);
         if (histList.isEmpty())
            throw new IllegalArgumentException("No audit trails found for id: "
               + id);

         result.put(id, histList);
      }

      return result;
   }

   // @see IPSSystemWs#loadRelationships(PSRelationshipFilter)
   @Transactional
   public List<PSRelationship> loadRelationships(PSRelationshipFilter filter)
      throws PSErrorException
   {
      if (filter == null)
         throw new IllegalArgumentException("filter may not be null.");

      return PSWebserviceUtils.loadRelationships(filter);
   }

   // @see IPSSystemWs#loadRelationshipTypes(String, String)
   @Transactional
   public List<PSRelationshipConfig> loadRelationshipTypes(String name,
      String category) throws PSErrorException
   {
      List<PSRelationshipConfig> result = null;
      PSRelationshipConfigSet configSet = getRelationshipConfigSet();

      if (StringUtils.isBlank(category))
      {
         if (StringUtils.isBlank(name))
         {
            result = configSet.getConfigList();
         }
         else
         {
            result = new ArrayList<PSRelationshipConfig>();
            List<String> names = findRelationshipConfigNames(name);
            for (String n : names)
            {
               PSRelationshipConfig c = configSet.getConfig(n);
               if (c != null)
                  result.add(c);
            }
         }
      }
      else
      {
         validateCategory(category);
         List<PSRelationshipConfig> clist = configSet
            .getConfigListByCategory(category);
         if (StringUtils.isBlank(name))
         {
            result = clist;
         }
         else
         {
            result = new ArrayList<PSRelationshipConfig>();
            List<String> names = findRelationshipConfigNames(name);
            for (PSRelationshipConfig c : clist)
            {
               if (names.contains(c.getName()))
                  result.add(c);
            }
         }
      }
      return result;
   }

   /**
    * Validates a relationship category.
    * 
    * @param category the category in question, assumed not <code>null</code> or
    *    empty. 
    * @throws IllegalArgumentException if the validation failed.
    */
   private void validateCategory(String category)
   {
      boolean isValidated = false;
      for (PSEntry entry : PSRelationshipConfig.CATEGORY_ENUM)
      {
         if (entry.getValue().equalsIgnoreCase(category))
         {
            category = entry.getValue(); // normalize the category
            isValidated = true;
         }
      }
      if (!isValidated)
         throw new IllegalArgumentException(
            "Unknown relationship category name: " + category);
   }

   /**
    * Finds the relationship config names from the supplied pattern.
    * 
    * @param pattern the pattern of the lookup name, It may contain wildcard.
    *    it may be <code>null</code> or empty for all names.
    * @return the names that matches the pattern
    */
   private List<String> findRelationshipConfigNames(String pattern)
   {
      // convert wildcard character, from '*' to '%'
      if (StringUtils.isBlank(pattern))
         pattern = "*";
      pattern = StringUtils.replaceChars(pattern, '*', '%');
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      List<PSRelationshipConfigName> names = cms
         .findRelationshipConfigNames(pattern);

      List<String> result = new ArrayList<String>();
      for (PSRelationshipConfigName n : names)
         result.add(n.getName());
      return result;
   }

   // @see IPSSystemWs#loadWorkflows(String)
   @Transactional
   public List<PSWorkflow> loadWorkflows(String name)
   {
      if (!StringUtils.isBlank(name))
         name = name.replace('*', '%');

      IPSWorkflowService svc = PSWorkflowServiceLocator.getWorkflowService();
      return svc.findWorkflowsByName(name);
   }

   // @see IPSSystemWs#saveRelationships(List)
   @Transactional
   public void saveRelationships(List<PSRelationship> relationships)
      throws PSErrorsException
   {
      PSWebserviceUtils.saveRelationships(relationships);
   }

   // @see IPSSystemWs#switchCommunity(String)
   @Transactional
   public void switchCommunity(String name)
      throws PSUserNotMemberOfCommunityException
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");

      IPSBackEndRoleMgr roleMgr = PSRoleMgrLocator.getBackEndRoleManager();
      List<PSCommunity> comms = roleMgr.findCommunitiesByName(name);
      if (comms.isEmpty() || comms.size() > 1)
         throw new IllegalArgumentException("Invalid community name");

      PSCommunity comm = comms.get(0);
      String commid = String.valueOf(comm.getId());

      PSRequest psReq = (PSRequest) PSRequestInfo
         .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      if (psReq == null)
         throw new RuntimeException("PSRequest not initialized");

      PSUserSession pssess = psReq.getUserSession();
      Object curComm = pssess.getPrivateObject(IPSHtmlParameters.SYS_COMMUNITY);
      pssess.setPrivateObject(IPSHtmlParameters.SYS_COMMUNITY, commid);

      boolean success = false;
      try
      {
         PSServer.verifyCommunity(psReq);
         PSCacheProxy.flushSession(pssess.getId());
         success = true;
      }
      catch (PSServerException e)
      {
         // treat as contract violation
         if (e.getErrorCode() == IPSServerErrors.COMMUNITIES_AUTHENTICATION_FAILED_INVALID_COMMUNITY)
         {
            int code = IPSWebserviceErrors.USER_NOT_MEMBER_COMMUNITY;
            throw new PSUserNotMemberOfCommunityException(code,
               PSWebserviceErrors.createErrorMessage(code, name),
               ExceptionUtils.getFullStackTrace(e));
         }

         throw new RuntimeException("Failed to switch community: "
            + e.getLocalizedMessage(), e);
      }
      catch (PSCacheException e)
      {
         // unexpected
         throw new RuntimeException("Failed to switch community: "
            + e.getLocalizedMessage(), e);
      }
      finally
      {
         // undo the change if we did not succeed
         if (!success)
            pssess.setPrivateObject(IPSHtmlParameters.SYS_COMMUNITY, curComm);
      }
   }

   // @see IPSSystemWs#switchLocale(String)
   @Transactional
   public void switchLocale(String code) throws PSInvalidLocaleException
   {
      if (StringUtils.isBlank(code))
         throw new IllegalArgumentException("code may not be null or empty");

      // verify the locale
      IPSCmsObjectMgr mgr = PSCmsObjectMgrLocator.getObjectManager();
      PSLocale locale = mgr.findLocaleByLanguageString(code);
         
      // if the specifed locale is not active, piggy back the exception with
      // a set of valid locales. Clients can use this information if they need
      // such as in WB, where a new dialog is shown to choose a locale
      if (locale == null || locale.getStatus() != PSLocale.STATUS_ACTIVE)
      {
         StringBuffer msgSfx = new StringBuffer(1024);
         msgSfx.append(" Valid locales:<");
         List<PSLocale> locList = mgr.findAllLocales();
         Iterator<PSLocale> it = locList.iterator();
         while ( it.hasNext() )
         {
            PSLocale l = it.next();
            if ( l.getStatus() == PSLocale.STATUS_ACTIVE )
               msgSfx.append(l.getName()).append(" ");
         }
         if ( locList.size() == 0 )
            msgSfx.append("none");
         msgSfx.append(">");
         
         // throw invalid locale exception with a set of active locales
         int errcode = IPSWebserviceErrors.INVALID_LOCALE;
         throw new PSInvalidLocaleException(errcode, PSWebserviceErrors
            .createErrorMessage(errcode, code)+msgSfx.toString(), ExceptionUtils
            .getFullStackTrace(new Exception()));
      }

      PSRequest psReq = (PSRequest) PSRequestInfo
         .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      if (psReq == null)
         throw new RuntimeException("PSRequest not initialized");

      PSUserSession pssess = psReq.getUserSession();
      Object curLocale = pssess.getPrivateObject(IPSHtmlParameters.SYS_LANG);
      pssess.setPrivateObject(IPSHtmlParameters.SYS_LANG, code);

      boolean success = false;
      try
      {
         PSCacheProxy.flushSession(pssess.getId());
         success = true;
      }
      catch (PSCacheException e)
      {
         // unexpected
         throw new RuntimeException("Failed to switch locale: "
            + e.getLocalizedMessage(), e);
      }
      finally
      {
         // undo the change if we did not succeed
         if (!success)
            pssess.setPrivateObject(IPSHtmlParameters.SYS_LANG, curLocale);
      }
   }

   @Deprecated
   public List<String> transitionItems(List<IPSGuid> ids, String transition,
            String user) throws PSErrorsException, PSErrorException
   {
      return transitionItems(ids, transition);
   }
   
   
   public List<String> transitionItems(List<IPSGuid> ids, String transition)
      throws PSErrorsException, PSErrorException
   {
      return transitionItems(ids, transition, null, null);
   }

   @Deprecated
   public List<String> transitionItems(List<IPSGuid> ids, String transition,
      String comment, List<String> adhocUsers, String user)
      throws PSErrorsException, PSErrorException
   {
      return transitionItems(ids, transition, comment, adhocUsers);
   }

   public List<String> transitionItems(List<IPSGuid> ids, String transition,
      String comment, List<String> adhocUsers) 
      throws PSErrorsException, PSErrorException
   {
      if (StringUtils.isBlank(transition))
         throw new IllegalArgumentException(
            "transition must not be null or empty.");
      PSWebserviceUtils.validateLegacyGuids(ids);

      // validate all items exist, must be in checked in mode (or checkin first
      // if is checked out by the same user, and the revision of an id 
      // (in the ids) must be current revision if included
      List<PSComponentSummary> summaries = new ArrayList<PSComponentSummary>(
         ids.size());
      PSComponentSummary item;
      PSLegacyGuid id;
      for (IPSGuid guidId : ids)
      {
         id = (PSLegacyGuid) guidId;
         item = PSWebserviceUtils.getItemSummary(id.getContentId());
         // if the item is in checked out mode
         if (StringUtils.isNotBlank(item.getCheckoutUserName()))
         {
            if (PSWebserviceUtils.isItemCheckedOutToUser(item))
            {
               // checkin the item if the same user checked out
               IPSContentWs ctService = PSContentWsLocator
                  .getContentWebservice();
               List<IPSGuid> one_id = new ArrayList<IPSGuid>();
               one_id.add(id);
               ctService.checkinItems(one_id, "");
               // reload the summary in case current revision changed by checkin
               item = PSWebserviceUtils.getItemSummary(id.getContentId());
            }
            else
            {
               throw new IllegalArgumentException("Item (id="
                  + item.getContentId() + ") must not be in checked in mode.");
            }
         }
         if (id.getRevision() != -1
            && id.getRevision() != item.getCurrentLocator().getRevision())
            throw new IllegalArgumentException("The revision ("
               + id.getRevision() + ") of id (" + id.toString()
               + ") must be the current revision of the item.");
         summaries.add(item);
      }

      // perform transitions
      List<String> states = new ArrayList<String>(summaries.size());
      Map<Integer, PSWorkflow> wfMap = new HashMap<Integer, PSWorkflow>();
      for (PSComponentSummary summary : summaries)
      {
         try
         {
            PSWebserviceUtils.transitionItem(summary.getContentId(), 
               transition, comment, adhocUsers);
         }
         catch (PSErrorException e)
         {
            PSErrorsException error = new PSErrorsException();
            error.addError(new PSLegacyGuid(summary.getContentId(), -1), e);
            throw error;
         }

         // reload summary with the new state id
         item = PSWebserviceUtils.getItemSummary(summary.getContentId());

         // get the name of the new state
         PSWorkflow wf = wfMap.get(item.getWorkflowAppId());
         if (wf == null)
            wf = PSWebserviceUtils.getWorkflow(item.getWorkflowAppId());
         String stateName = PSWebserviceUtils.getStateById(wf,
            item.getContentStateId()).getName();

         states.add(stateName);
      }
      return states;
   }

   // @see IPSSystemWs#createRelationship(String, IPSGuid, IPSGuid)
   @Transactional
   public PSRelationship createRelationship(String name, IPSGuid ownerId,
      IPSGuid depenentId) throws PSErrorException
   {
      // validating the arguments
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name must not be null or empty.");
      PSWebserviceUtils.validateLegacyGuid(ownerId);
      PSWebserviceUtils.validateLegacyGuid(depenentId);

      // validating the config
      PSRelationshipConfig config = PSRelationshipCommandHandler
         .getRelationshipConfig(name);
      if (config == null)
         throw new IllegalArgumentException(
            "Cannot find a relationship type with name = '" + name + "'.");
      
      final boolean IS_OWNER_ID = true;
      PSLocator owner = PSWebserviceUtils.getHeadLocator(
         (PSLegacyGuid) ownerId, IS_OWNER_ID, config);
      PSLocator dependent = PSWebserviceUtils.getHeadLocator(
         (PSLegacyGuid) depenentId, !IS_OWNER_ID, config);

      PSRelationship relationship = new PSRelationship(PSWebserviceUtils
         .getNextRelationshipId(), owner, dependent, config);
      PSWebserviceUtils.saveRelationship(relationship);

      return relationship;
   }

   /* (non-Javadoc)
    * @see com.percussion.webservices.system.IPSSystemWs#getAllowedTransitions(
    *    List)
    */
   @Transactional
   @SuppressWarnings("unchecked")
   public Map<String, String> getAllowedTransitions(List<IPSGuid> ids)
   {
      PSWebserviceUtils.validateLegacyGuids(ids);

      Map<String, String> transitions = new HashMap<String, String>();
      PSRequest req = (PSRequest) PSRequestInfo
         .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      if (req == null)
         throw new IllegalStateException("PSRequest not initialized");

      PSUserSession sess = req.getUserSession();
      PSUserEntry[] entries = sess.getAuthenticatedUserEntries();
      if (entries.length == 0)
         throw new IllegalStateException("No authenticated user entry found");

      String userName = entries[0].getName();
      PSRoleEntry[] roleEntries = entries[0].getRoles();
      List<String> roles = new ArrayList<String>(roleEntries.length);
      for (PSRoleEntry roleEntry : roleEntries)
      {
         roles.add(roleEntry.getName());
      }

      int commId = -1;
      try
      {
         String usercomm = (String) sess
            .getPrivateObject(IPSHtmlParameters.SYS_COMMUNITY);
         if (usercomm != null)
            commId = Integer.parseInt(usercomm);
      }
      catch (Exception e)
      {
         // leave at -1
      }

      boolean first = true;
      try
      {
         for (IPSGuid id : ids)
         {
            PSLegacyGuid guid = (PSLegacyGuid) id;
            List<PSTransitionInfo> transInfos = PSWorkFlowUtils
               .getAllowedTransitions(guid.getContentId(), userName, roles,
                  commId);

            Map<String, String> transMap = new HashMap<String, String>();
            for (PSTransitionInfo transInfo : transInfos)
            {
               transMap.put(transInfo.getTrigger(), transInfo.getLabel());
            }

            if (first)
            {
               transitions.putAll(transMap);
               first = false;
            }
            else
            {
               transitions.keySet().retainAll(transMap.keySet());
            }
         }
      }
      catch (PSEntryNotFoundException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage(), e);
      }
      catch (Exception e)
      {
         throw new RuntimeException("Failed to get allowed transitions: "
            + e.getLocalizedMessage(), e);
      }

      return transitions;
   }
}
