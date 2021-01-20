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
package com.percussion.webservices.security.impl;

import com.percussion.design.objectstore.PSRole;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.catalog.PSCatalogException;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.catalog.data.PSObjectSummary;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.locking.IPSObjectLockService;
import com.percussion.services.locking.PSLockException;
import com.percussion.services.locking.PSObjectLockServiceLocator;
import com.percussion.services.locking.data.PSObjectLock;
import com.percussion.services.security.IPSAcl;
import com.percussion.services.security.IPSAclService;
import com.percussion.services.security.IPSBackEndRoleMgr;
import com.percussion.services.security.IPSRoleMgr;
import com.percussion.services.security.PSAclServiceLocator;
import com.percussion.services.security.PSRoleMgrLocator;
import com.percussion.services.security.PSSecurityException;
import com.percussion.services.security.data.PSAclEntryImpl;
import com.percussion.services.security.data.PSAclImpl;
import com.percussion.services.security.data.PSCommunity;
import com.percussion.services.security.data.PSCommunityVisibility;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.util.PSBaseBean;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.security.IPSTypedPrincipal.PrincipalTypes;
import com.percussion.utils.security.PSSecurityCatalogException;
import com.percussion.webservices.IPSWebserviceErrors;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.PSWebserviceErrors;
import com.percussion.webservices.PSWebserviceUtils;
import com.percussion.webservices.content.IPSContentDesignWs;
import com.percussion.webservices.content.PSContentWsLocator;
import com.percussion.webservices.security.IPSSecurityDesignWs;
import com.percussion.webservices.security.IPSSecurityWs;
import com.percussion.webservices.security.PSSecurityWsLocator;
import com.percussion.webservices.system.IPSSystemDesignWs;
import com.percussion.webservices.system.PSSystemWsLocator;
import com.percussion.webservices.ui.IPSUiDesignWs;
import com.percussion.webservices.ui.PSUiWsLocator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.Subject;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The private security design webservice implementations.
 */
@Transactional
@PSBaseBean("sys_securityDesignWs")
public class PSSecurityDesignWs extends PSSecurityBaseWs implements
   IPSSecurityDesignWs
{
   // @see IPSSecurityDesignWs#createCommunities(List, String, String)
   public List<PSCommunity> createCommunities(List<String> names,
      String session, String user)
   {
      if (names == null || names.isEmpty())
         throw new IllegalArgumentException("names cannot be null or empty");

      List<PSCommunity> communities = new ArrayList<PSCommunity>();
      for (String name : names)
      {
         if (StringUtils.isBlank(name))
            throw new IllegalArgumentException("name cannot be null or empty");

         IPSBackEndRoleMgr service = PSRoleMgrLocator.getBackEndRoleManager();

         if (!service.findCommunitiesByName(name).isEmpty())
         {
            PSWebserviceUtils.throwObjectExistException(name,
               PSTypeEnum.COMMUNITY_DEF);
         }

         PSCommunity community = service.createCommunity(name, null);

         IPSObjectLockService lockService = PSObjectLockServiceLocator
            .getLockingService();
         try
         {
            lockService.createLock(community.getGUID(), session, user, null,
               false);
         }
         catch (PSLockException e)
         {
            // should never happen, ignore
         }

         communities.add(community);
      }

      return communities;
   }

   // @see IPSSecurityDesignWs#deleteCommunities(List, boolean, String, String)
   public void deleteCommunities(List<IPSGuid> ids, boolean ignoreDependencies,
      String session, String user) throws PSErrorsException
   {
      if (PSGuidUtils.isBlank(ids))
         throw new IllegalArgumentException("ids cannot be null or empty");

      IPSBackEndRoleMgr service = PSRoleMgrLocator.getBackEndRoleManager();

      PSErrorsException results = new PSErrorsException();
      for (IPSGuid id : ids)
      {
         if (PSWebserviceUtils.hasValidLockForDelete(id, session, user))
         {
            boolean exists = false;
            try
            {
               service.loadCommunity(id);
               exists = true;
            }
            catch (PSSecurityException e)
            {
               // ignore, just means that the community does not exist
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

               service.deleteCommunity(id);
            }

            results.addResult(id);
         }
         else
         {
            PSWebserviceUtils.handleMissingLockError(id, PSCommunity.class,
               results);
         }
      }

      // release locks for all successfully deleted objects
      PSWebserviceUtils.releaseLocks(results.getResults(), session, user);

      if (results.hasErrors())
         throw results;
   }

   // @see IPSSecurityDesignWs#findCommunities(String)
   public List<IPSCatalogSummary> findCommunities(String name)
   {
      IPSBackEndRoleMgr service = PSRoleMgrLocator.getBackEndRoleManager();

      if (StringUtils.isBlank(name))
         name = "*";
      name = StringUtils.replaceChars(name, '*', '%');

      List<PSCommunity> communities = service.findCommunitiesByName(name);
      return PSWebserviceUtils.toObjectSummaries(communities);
   }

   // @see IPSSecurityDesignWs#findRoles(String)
   public List<IPSCatalogSummary> findRoles(String name)
   {
      IPSSecurityWs service = PSSecurityWsLocator.getSecurityWebservice();
      List<PSRole> roles = service.loadRoles(name);
      return PSWebserviceUtils.toObjectSummaries(roles);
   }

   // @see IPSSecurityDesignWs#isValidRhythmyxUser(String)
   public boolean isValidRhythmyxUser(String user)
      throws PSSecurityCatalogException
   {
      if (StringUtils.isBlank(user))
         throw new IllegalArgumentException("user cannot be null or empty");

      IPSRoleMgr service = PSRoleMgrLocator.getRoleManager();

      List<String> users = new ArrayList<String>();
      users.add(user);
      List<Subject> subjects = service.findUsers(users);

      return !subjects.isEmpty();
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSSecurityDesign#loadCommunities(List, boolean, boolean, 
    *    String, String)
    */
   @SuppressWarnings("unchecked")
   public List<PSCommunity> loadCommunities(List<IPSGuid> ids, boolean lock,
      boolean overrideLock, String session, String user) 
      throws PSErrorResultsException
   {
      if (PSGuidUtils.isBlank(ids))
         throw new IllegalArgumentException("ids cannot be null or empty");
      
      if (lock && StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");
   
      if (lock && StringUtils.isBlank(user))
         throw new IllegalArgumentException("user cannot be null or empty");

      IPSBackEndRoleMgr service = 
         PSRoleMgrLocator.getBackEndRoleManager();
      
      PSErrorResultsException results = new PSErrorResultsException();
      PSCommunity[] communities = service.loadCommunities(ids
            .toArray(new IPSGuid[ids.size()]));
   
      //check for error and handle it
      Exception e = new Exception();
      for (int i = 0, j=0; i < ids.size(); i++)
      {
         IPSGuid id = ids.get(i); 
         if (communities.length > 0 && j < communities.length)
         {
            if (id.equals(communities[j].getGUID()))
            {
               results.addResult(id, communities[j]);
               j++;
               continue;
            }
         }
         
         int code = IPSWebserviceErrors.OBJECT_NOT_FOUND;
         PSDesignGuid guid = new PSDesignGuid(id);
         PSErrorException error = new PSErrorException(code,
            PSWebserviceErrors.createErrorMessage(code, PSCommunity.class
               .getName(), guid.getValue()), ExceptionUtils
               .getFullStackTrace(e));
         results.addError(id, error);
      }
   
      if (lock)
      {
         IPSObjectLockService lockService = 
            PSObjectLockServiceLocator.getLockingService();
         lockService.createLocks(results, session, user, overrideLock);
      }
      
      if (results.hasErrors())
         throw results;
      
      return results.getResults(ids);
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSSecurityDesign#saveCommunities(List, boolean, String, String)
    */
   public void saveCommunities(List<PSCommunity> communities, boolean release, 
      String session, String user) throws PSErrorsException
   {
      if (communities == null || communities.isEmpty())
         throw new IllegalArgumentException(
            "communities cannot be null or empty");
      
      if (release && StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");
   
      if (release && StringUtils.isBlank(user))
         throw new IllegalArgumentException("user cannot be null or empty");
      
      IPSBackEndRoleMgr service = 
         PSRoleMgrLocator.getBackEndRoleManager();
      
      IPSObjectLockService lockService = 
         PSObjectLockServiceLocator.getLockingService();

      //todo: should check if the community is locked before renaming the acls
      
      // rename acls first
      Map<String, PSCommunity> renamedCommunities = 
         getRenamedCommunities(communities, session, user);
      try
      {
         renameAclEntries(renamedCommunities, session, user);
      }
      catch (PSErrorResultsException e)
      {
         // collect all error messages into one string
         StringBuffer message = new StringBuffer();
         for (Object value : e.getErrors().values())
         {
            if (value != null)
            {
               message.append(value.toString());
               message.append(", ");
            }
         }
         
         int code = IPSWebserviceErrors.FAILED_RENAMING_ACLS;
         PSErrorException error = new PSErrorException(code, 
            PSWebserviceErrors.createErrorMessage(code, 
               PSCommunity.class.getName(), message), 
            ExceptionUtils.getFullStackTrace(new Exception()));
   
         PSErrorsException ex = new PSErrorsException();
         for (PSCommunity community : communities)
            ex.addError(community.getGUID(), error);
         
         throw ex;
      }
      catch (RemoteException e)
      {
         int code = IPSWebserviceErrors.FAILED_RENAMING_ACLS;
         PSErrorException error = new PSErrorException(code, 
            PSWebserviceErrors.createErrorMessage(code, 
               PSCommunity.class.getName(), e.getLocalizedMessage()), 
            ExceptionUtils.getFullStackTrace(new Exception()));
   
         PSErrorsException ex = new PSErrorsException();
         for (PSCommunity community : communities)
            ex.addError(community.getGUID(), error);
         
         throw ex;
      }
   
      // then save the community
      PSErrorsException results = new PSErrorsException();
      List<PSObjectLock> locksToRelease = new ArrayList<PSObjectLock>();
      for (PSCommunity community : communities)
      {
         IPSGuid id = community.getGUID();
         PSObjectLock lock = lockService.findLockByObjectId(id, session, user);
         try
         {
            if (lock != null)
            {
               locksToRelease.add(lock);
               // set the correct version from the lock
               Integer version = lock.getLockedVersion();
               if (version != null)
                  community.setVersion(version);
               // save the object and extend the lock
               service.saveCommunity(community);
               if (!release)
               {
                  lockService.extendLock(id, session, user, 
                     community.getVersion());
               }
               
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
                        PSCommunity.class.getName(), guid.getValue()), 
                        ExceptionUtils.getFullStackTrace(new Exception()));
                  results.addError(id, error);
               }
               else
               {
                  int code = IPSWebserviceErrors.OBJECT_NOT_LOCKED_FOR_REQUESTOR;
                  PSDesignGuid guid = new PSDesignGuid(id);
                  PSErrorException error = new PSErrorException(code, 
                     PSWebserviceErrors.createErrorMessage(code, 
                        PSCommunity.class.getName(), guid.getValue(), 
                        lock.getLocker(), lock.getRemainingTime()), 
                        ExceptionUtils.getFullStackTrace(new Exception()));
                  results.addError(id, error);
               }
            }
         }
         catch (PSLockException e)
         {
            int code = IPSWebserviceErrors.SAVE_FAILED;
            PSDesignGuid guid = new PSDesignGuid(id);
            PSErrorException error = new PSErrorException(code, 
               PSWebserviceErrors.createErrorMessage(code, 
                  PSCommunity.class.getName(), guid.getValue(),
                  e.getLocalizedMessage()), 
                  ExceptionUtils.getFullStackTrace(e));
            results.addError(id, error);
         }
      }
      
      if (release)
      {
         lockService.releaseLocks(locksToRelease);
      }
      if (results.hasErrors())
         throw results;
   }
   
    /**
    * Get all communities that were renamed with the current request.
    * 
    * @param communities the communities to test for rename, not 
    *    <code>null</code>, may be empty.
    * @param session the session used for all requested, assuemd not 
    *    <code>null</code> or empty.
    * @param user the user making the request, assumed not <code>null</code>
    *    or empty.
    * @return a map with renamed communities, the key is the original community
    *    name, the value is the new community, never <code>null</code>, may
    *    be empty.
    */
   private Map<String, PSCommunity> getRenamedCommunities(
      List<PSCommunity> communities, String session, String user) 
   {
      Map<String, PSCommunity> renamedCommunities = 
         new HashMap<String, PSCommunity>();
      
      if (communities.isEmpty())
         return renamedCommunities;
      
      // load the current communities if available
      List<PSCommunity> currentCommunities = new ArrayList<PSCommunity>();
      try
      {
         List<IPSGuid> ids = new ArrayList<IPSGuid>();
         for (PSCommunity community : communities)
            ids.add(community.getGUID());
   
         currentCommunities.addAll(loadCommunities(ids, false, false, 
            session, user));
      }
      catch (PSErrorResultsException e)
      {
         // this is fine, we assume some communities did not exist yet
         for (Object obj : e.getResults().values())
         {
            if (obj instanceof PSCommunity)
               currentCommunities.add((PSCommunity) obj);
         }
      }
      
      // weed out renamed communities
      for (PSCommunity community : communities)
      {
         for (PSCommunity currentCommunity : currentCommunities)
         {
            if (community.getGUID().equals(currentCommunity.getGUID()))
            {
               if (!community.getName().equals(currentCommunity.getName()))
                  renamedCommunities.put(currentCommunity.getName(), community);
               
               break;
            }
         }
      }
      
      return renamedCommunities;
   }

   /**
    * Maps the <code>summaries</code> that are visible to each of the supplied
    * <code>communities</code>.
    * 
    * @param catSummaries Assumed not <code>null</code>.
    * 
    * @param communities Assumed not <code>null</code>.
    * 
    * @return Never <code>null</code>. Each object in
    * <code>communities</code> is placed in the result as a key. Each value is
    * a non-<code>null</code> collection from the supplied <code>summaries</code>
    * that are visible to that community.
    */
   private Map<PSCommunity, List<PSObjectSummary>> filterByCommunityVisibility(
         Map<IPSGuid,IPSCatalogSummary> catSummaries, Map<IPSGuid,PSCommunity> communities)
   {
      IPSAclService service = PSAclServiceLocator.getAclService();

      Map<IPSGuid,IPSAcl> objectAcls = new HashMap<IPSGuid,IPSAcl>();
      for (IPSGuid g : catSummaries.keySet()) {
         IPSAcl acl = service.loadAclForObject(g);

         objectAcls.put(g ,acl);
      }

      Map<PSCommunity, List<PSObjectSummary>> filteredSummaries =
         new HashMap<PSCommunity, List<PSObjectSummary>>();
      Map<String, PSCommunity> communitiesByName = 
         new HashMap<String, PSCommunity>();
      for (IPSGuid g : communities.keySet())
      {
         filteredSummaries.put(communities.get(g), new ArrayList<PSObjectSummary>());
      }

      //TODO: This code didn't seem to do anything at all so I removed it.

      return filteredSummaries;
   }

   /**
    * Rename the acl entries for the supplied communities.
    * 
    * @param communities the communities for which to rename the acl entries,
    * assumed not <code>null</code>, may be empty. The key holds the original
    * community name, the value the community with the new name.
    * @param session the session used for all requested, assumed not
    * <code>null</code> or empty.
    * @param user the user making the request, assumed not <code>null</code>
    * or empty.
    * @throws RemoteException for any unknown error.
    * @throws PSErrorResultsException for any error finding community
    * visibilities or loading/saving acls.
    */
   @SuppressWarnings("unused")  //RemoteException
   private void renameAclEntries(Map<String, PSCommunity> communities, 
      String session, String user) throws RemoteException, 
         PSErrorResultsException
   {
      if (communities.isEmpty())
         return;
      
      List<IPSGuid> commIds = new ArrayList<IPSGuid>();
      for (PSCommunity community : communities.values())
         commIds.add(community.getGUID());
      
      
      IPSSystemDesignWs systemService = 
         PSSystemWsLocator.getSystemDesignWebservice();
      IPSObjectLockService lockService = 
         PSObjectLockServiceLocator.getLockingService();
      
      // find acls that are already locked by the current user
      List<PSAclImpl> allAcls = systemService.loadAcls(null, false, false, 
         session, user);
      List<IPSGuid> aclIds = new ArrayList<IPSGuid>();
      for (PSAclImpl acl : allAcls)
         aclIds.add(acl.getGUID());

      List<PSObjectLock> existingLocks = lockService.findLocksByObjectIds(
         aclIds, session, user);
      
      
      //determine which acls actually need to be fixed
      List<IPSGuid> objRequireChangeIds= new ArrayList<IPSGuid>();
      List<IPSGuid> aclRequireChangeIds= new ArrayList<IPSGuid>();
      for (PSAclImpl acl : allAcls)
      {
         Iterator entries = acl.getEntries().iterator();
         boolean modified = false;
         while (entries.hasNext())
         {
            PSAclEntryImpl entry = (PSAclEntryImpl) entries.next();
            if (!entry.getType().equals(PrincipalTypes.COMMUNITY))
               continue;
            
            for (String name : communities.keySet())
            {
               if (entry.getName().equals(name))
               {
                  modified = true;
                  break;
               }
            }
         }
         if (modified)
         {
            objRequireChangeIds.add(acl.getObjectGuid());
            aclRequireChangeIds.add(acl.getGUID());
         }
      }
      
      
      try
      {
         List<PSAclImpl> toChangeAcls = new ArrayList<PSAclImpl>();
         // rename the acl entries without releasing the locks
         if (!objRequireChangeIds.isEmpty())
         {
            toChangeAcls = systemService.loadAcls(objRequireChangeIds, true,
                  false, session, user);
         }

         for (PSAclImpl acl : toChangeAcls)
         {
            Iterator entries = acl.getEntries().iterator();
            while (entries.hasNext())
            {
               PSAclEntryImpl entry = (PSAclEntryImpl) entries.next();
               if (!entry.getType().equals(PrincipalTypes.COMMUNITY))
                  continue;
               
               for (String name : communities.keySet())
               {
                  if (entry.getName().equals(name))
                  {
                     entry.setName(communities.get(name).getName());
                  }
               }
            }
         }
         if (!toChangeAcls.isEmpty())
            systemService.saveAcls(toChangeAcls, false, session, user);
      }
      finally
      {
         // release all acl locks which the current user did not own before
         for (PSObjectLock lock : existingLocks)
         {
            for (IPSGuid aclId : aclRequireChangeIds)
            {
               if (aclId.equals(lock.getGUID()))
               {
                  aclRequireChangeIds.remove(aclId);
                  break;
               }
            }
         }
         if (!aclRequireChangeIds.isEmpty())
            systemService.releaseLocks(aclRequireChangeIds, session, user);
      }
   }

   /* (non-Javadoc)
    * @see IPSSecurityDesignWs#getVisibilityByCommunity(List, PSTypeEnum, 
    *    String, String)
    */
   @SuppressWarnings("unchecked")
   public List<PSCommunityVisibility> getVisibilityByCommunity(
      List<IPSGuid> ids, PSTypeEnum type, String session, String user)
      throws PSErrorResultsException, RemoteException
   {
      if (ids == null || ids.isEmpty())
         throw new IllegalArgumentException("ids cannot be null or empty");
      // get all design object summaries for the specified type
      List<IPSCatalogSummary> summaries = null;
      try
      {
         summaries = getDesignObjectSummaries(type);
      }
      catch (Exception e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }
   
      PSErrorResultsException results = new PSErrorResultsException();
      List<PSCommunity> communities = loadCommunities(ids, false, false,
            session, user);

      Map<IPSGuid, PSCommunity> communityMap = new HashMap<IPSGuid,PSCommunity>();
      for(PSCommunity c : communities){
         communityMap.put(c.getGUID(),c);
      }

      Map<IPSGuid, IPSCatalogSummary> summaryMap = new HashMap<IPSGuid, IPSCatalogSummary>();
      for(IPSCatalogSummary s : summaries){
         summaryMap.put(s.getGUID(),s);
      }
      /*
       * value is a set of workflows that are visible to the keyed community
       */
      Map<PSCommunity, List<PSObjectSummary>> filtered = 
         filterByCommunityVisibility(summaryMap, communityMap);
      
      for (PSCommunity community : communities)
      {
         PSCommunityVisibility visibility = new PSCommunityVisibility(
               community.getGUID());
         visibility.addAllVisibleObjects(filtered.get(community));
         results.addResult(community.getGUID(), visibility);
      }
   
      if (results.hasErrors())
         throw results;
   
      return results.getResults(ids);
   }

   /**
    * Get all design object summaries for the specified type. Returns the 
    * summaries for all design object types as defined in 
    * {@link #getDesignObjectTypes()} if no type was specified.
    * 
    * @param type the object type for which to get the summaries, 
    *    <code>null</code> to get the summaries for all design object types.
    * @return the requested summaries, never <code>null</code>, may be empty.
    * @throws PSErrorException if an unsupported object type was supplied.
    * @throws PSAssemblyException if failed to load templates.
    */
   private List<IPSCatalogSummary> getDesignObjectSummaries(PSTypeEnum type) 
      throws PSErrorException, PSAssemblyException
   {
      List<PSTypeEnum> objectTypes = new ArrayList<PSTypeEnum>();
      if (type == null)
         objectTypes.addAll(getDesignObjectTypes());
      else
         objectTypes.add(type);
   
      IPSAssemblyService assemblyService = 
         PSAssemblyServiceLocator.getAssemblyService();
      IPSContentDesignWs contentService = 
         PSContentWsLocator.getContentDesignWebservice();
      IPSSystemDesignWs systemService = 
         PSSystemWsLocator.getSystemDesignWebservice();
      IPSUiDesignWs uiService = 
         PSUiWsLocator.getUiDesignWebservice();
      IPSSiteManager siteMgr = PSSiteManagerLocator.getSiteManager();
      List<IPSCatalogSummary> objects = new ArrayList<IPSCatalogSummary>();
      for (PSTypeEnum objectType : objectTypes)
      {
         switch (objectType)
         {
            case NODEDEF:
               objects.addAll(contentService.findContentTypes(null));
               break;
               
            case TEMPLATE:
               Set<IPSAssemblyTemplate> templates = assemblyService
                     .findAllTemplates();
               for (IPSAssemblyTemplate t : templates)
               {
                  objects.add((IPSCatalogSummary)t);
               }
               break;
               
            case WORKFLOW:
               objects.addAll(systemService.findWorkflows(null));
               break;
               
            case ACTION:
               objects.addAll(uiService.findActions(null, null, null));
               break;
               
            case DISPLAY_FORMAT:
               objects.addAll(uiService.findDisplayFormats(null, null));
               break;
               
            case SEARCH_DEF:
               objects.addAll(uiService.findSearches(null, null));
               break;
               
            case VIEW_DEF:
               objects.addAll(uiService.findViews(null, null));
               break;
               
            case SITE:
               List<IPSCatalogSummary> s = null;
               try
               {
                  s = siteMgr.getSummaries(objectType);   
               }
               catch (PSCatalogException e)
               {
                  s = new ArrayList<IPSCatalogSummary>();
               }
               objects.addAll(s);
               break;
               
            default:
               int code = IPSWebserviceErrors
                  .UNSUPPORTD_COMMUNITY_VISIBILITY_LOOKUP_TYPE;
               throw new PSErrorException(code, 
                  PSWebserviceErrors.createErrorMessage(code, 
                     objectType.toString()), 
                  ExceptionUtils.getFullStackTrace(new Exception()));
         }
      }
      
      return objects;
   }

   /**
    * Get the list of design object types which are supported by the 
    * <code>getVisibilityByCommunity</code> webservice.
    * 
    * @return the list of supported object types, never <code>null</code> or
    *    empty.
    */
   private static List<PSTypeEnum> getDesignObjectTypes()
   {
      List<PSTypeEnum> designObjectTypes = new ArrayList<PSTypeEnum>();
      designObjectTypes.add(PSTypeEnum.NODEDEF);
      designObjectTypes.add(PSTypeEnum.TEMPLATE);
      designObjectTypes.add(PSTypeEnum.WORKFLOW);
      designObjectTypes.add(PSTypeEnum.ACTION);
      designObjectTypes.add(PSTypeEnum.DISPLAY_FORMAT);
      designObjectTypes.add(PSTypeEnum.SEARCH_DEF);
      designObjectTypes.add(PSTypeEnum.VIEW_DEF);
      designObjectTypes.add(PSTypeEnum.SITE);
      
      return designObjectTypes;
   }
}
