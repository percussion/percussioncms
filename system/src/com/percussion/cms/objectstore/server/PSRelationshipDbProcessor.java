/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
package com.percussion.cms.objectstore.server;

import com.percussion.cms.IPSCmsErrors;
import com.percussion.cms.IPSConstants;
import com.percussion.cms.PSApplicationBuilder;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.PSRelationshipChangeEvent;
import com.percussion.cms.handlers.PSRelationshipCommandHandler;
import com.percussion.cms.objectstore.PSCmsObject;
import com.percussion.cms.objectstore.PSComponentSummaries;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSObjectPermissions;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.data.PSDataExtractionException;
import com.percussion.data.PSExecutionData;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.error.PSException;
import com.percussion.error.PSExceptionUtils;
import com.percussion.relationship.IPSExecutionContext;
import com.percussion.security.PSThreadRequestUtils;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSExecutionDataLight;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestContext;
import com.percussion.server.PSServer;
import com.percussion.server.cache.IPSCacheHandler;
import com.percussion.server.cache.PSAssemblerCacheHandler;
import com.percussion.server.cache.PSCacheManager;
import com.percussion.server.cache.PSFolderRelationshipCache;
import com.percussion.server.cache.PSItemSummaryCache;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.assembly.impl.nav.PSRelationshipSortOrderComparator;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.IPSItemEntry;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.notification.IPSNotificationService;
import com.percussion.services.notification.PSNotificationEvent;
import com.percussion.services.notification.PSNotificationEvent.EventType;
import com.percussion.services.notification.PSNotificationServiceLocator;
import com.percussion.services.relationship.IPSRelationshipService;
import com.percussion.services.relationship.PSRelationshipServiceLocator;
import com.percussion.util.IPSHtmlParameters;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * This class encapsulates all backend operations to create, modify, delete and
 * catalog relationships.
 */
public class PSRelationshipDbProcessor
{
   


   public PSRelationshipDbProcessor(boolean thread_context)
   {
      this.thread_context = thread_context;
   }
   
   /**
    * Create the backend processor for the supplied request.
    *
    * @param request the request to use, if <code>null</code> is provided, the
    *    internal rhythmyx user will be used to perform the requests.
    * @throws PSCmsException if the relationship support application is not
    *    found or not initialized yet.
    */
   public PSRelationshipDbProcessor(PSRequest request) throws PSCmsException
   {
      setRequest(request);
   }

   /**
    * Create the backend processor for the supplied context.
    *
    * @param context the request context to use, if <code>null</code> is
    *    provided, the internal rhythmyx user will be used to perform the
    *    requests.
    * @throws PSCmsException if the relationship support application is not
    *    found or not initialized yet.
    */
   public PSRelationshipDbProcessor(IPSRequestContext context)
         throws PSCmsException
   {
      setRequestContext(context);
   }

   /**
    * Catalogs all relationships based on the filter object supplied.
    * This is the same as the {@link #getRelationshipList(PSRelationshipFilter)}
    * except it returns relationships in a {@link PSRelationshipSet}.
    *
    * @param filter relationship filer, must not be <code>null</code>.
    *
    * @return A set of 0 or more relationship objects, in no particular order.
    *
    * @throws PSCmsException If any problems occur trying to get the 
    * relationships, such as a db failure.
    */
   public PSRelationshipSet getRelationships(PSRelationshipFilter filter)
         throws PSCmsException
   {
      List<PSRelationship> result = getRelationshipList(filter);
      PSRelationshipSet resultSet = new PSRelationshipSet(result.size());
      resultSet.addAll(result);
      return resultSet;
   }
   
   /**
    * Catalogs all relationships based on the filter object supplied.
    * This is the same as the {@link #getRelationshipList(PSRelationshipFilter)}
    * except it returns relationships in a {@link List}.
    *
    * @param filter relationship filer, must not be <code>null</code>.
    *
    * @return A set of 0 or more relationship objects, in no particular order.
    *
    * @throws PSCmsException If any problems occur trying to get the 
    *    relationships, such as a db failure.
    */
   public List<PSRelationship> getRelationshipList(PSRelationshipFilter filter)
         throws PSCmsException
   {
      if (filter == null)
         throw new IllegalArgumentException("filter must not be null");
      
      int doNotFilter = filter.isCommunityFilteringEnabled() ? 0 :
            PSRelationshipConfig.FILTER_TYPE_COMMUNITY;
      
      return getRelationshipList(filter, doNotFilter);
   }
   
   /**
    * Catalogs all relationships based on the filter object supplied.
    *
    * @param filter relationship filer, must not be <code>null</code>.
    * @param doNotApplyFilters mask to restrict the filtering of relationships
    * based on community or folder permissions (bitwise OR the FILTER_BY_xxx
    * constants defined in <code>PSRelationshipConfig</code> to restrict
    * filtering on both), should be set to <code>0</code> if filtering is to be
    * performed.
    *
    * @return A collection of 0 or more relationship objects, in no particular 
    *   order.
    *   
    * @throws PSCmsException 
    */
   private List<PSRelationship> getRelationshipList(
         PSRelationshipFilter filter, int doNotApplyFilters)
         throws PSCmsException
   {
      if ((!filter.isCommunityFilteringEnabled()) && doNotApplyFilters == 0)
         throw new IllegalArgumentException("Community filter is disabled, but doNotApplyFilters == 0");
      
      /*
       * The community filter is enabled by defaut. Disable community filtering
       * if so requested by this filter.
       */
      if (!filter.isCommunityFilteringEnabled())
         doNotApplyFilters |= PSRelationshipConfig.FILTER_TYPE_COMMUNITY;
    
      try
      {
         List<PSRelationship> result = null;
         
         QueryRelationshipCriteria params = new QueryRelationshipCriteria();

         PSFolderRelationshipCache folderCache = canProcessedByFolderCache(
               filter, params);
         
            String useAaRelationshipCacheString = StringUtils.defaultString((String)PSServer.getServerProps().get("useAaRelationshipCache"),"true");
         boolean testAaCache = useAaRelationshipCacheString.equals("test");
         boolean useAaRelationshipCache = useAaRelationshipCacheString.equals("true");
         List<PSRelationship> testResult = null;
         
         if (folderCache != null)
         { 
               result = getRelationshipsFromFolderCache(folderCache, params, filter);
         }
         else if (testAaCache || useAaRelationshipCache)
         {
            PSFolderRelationshipCache assemblyCache = canProcessedByAssemblyCache(
                  filter, params);
            if (assemblyCache != null)
            {
               result = getRelationshipsFromAaCache(assemblyCache, filter, params);
               
               if (testAaCache)
               {
                  testResult = result;
                  result = null;
               }
            }
         }
         
         if (result==null) // get relationships from backend repository
         {
            IPSRelationshipService svc = PSRelationshipServiceLocator
                  .getRelationshipService();

            result = svc.findByFilter(filter);
            setExtraDependentProperties(result);
         }
         
         if (testResult!=null)
         {
            if (result==null)
            {
               log.error("AA cache returned non null, service returned null for filter "+filter);
            }
            
            if (result !=null && result.size()!=testResult.size())
            {
               log.error("AA cache returned "+testResult.size()+" service returned "+result.size()+" items");
               log.error("Filter = "+filter);
               for (int i=0; i< result.size(); i++)
               {
                  log.error("item "+i+" : " + result.get(i));
               }
               for (int i=0; i< result.size(); i++)
               {
                  log.error("AA item "+i+" : " + testResult.get(i));
               }
               
               
            } else {
               PSRelationshipSortOrderComparator comparator = new PSRelationshipSortOrderComparator();
               if(result !=null) {
                  result.sort(comparator);
               }

               testResult.sort(comparator);
               if (!testResult.equals(result))
               {
                  
                  log.error("Test results do not match "+filter, new Throwable());
                  log.error("Cache Result ="+testResult);
                  log.error("Server Result ="+result);

                  if(result != null) {
                     for (int i = 0; i < result.size(); i++) {
                        if (!result.get(i).equals(testResult.get(i))) {
                           log.error("non matching item pos " + i);
                           if (result.get(i).getProperties() != testResult.get(i).getProperties()) {
                              log.error("server props= {} ", result.get(i).getProperties());
                              log.error("cache props=" + result.get(i).getProperties());
                           }
                        }
                     }
                  }
               }
            }
         }
         
         if (result != null && ! result.isEmpty())
         {
            // if the community id is -1, then there is no need to filter,
            // either it was not found or it is set to all communities
            int communityId = getCommunity();

            boolean doCommFilter = true;
            if ((doNotApplyFilters & PSRelationshipConfig.FILTER_TYPE_COMMUNITY)
                  == PSRelationshipConfig.FILTER_TYPE_COMMUNITY)
            {
               doCommFilter = false;
            }
            else
            {
               int commFilterFlags = 
                     PSRelationshipConfig.FILTER_TYPE_ITEM_COMMUNITY
                     | PSRelationshipConfig.FILTER_TYPE_FOLDER_COMMUNITY;
               if ((doNotApplyFilters & commFilterFlags) == commFilterFlags)
                  doCommFilter = false;
            }
            
            int filterFlags = 0;
            if (doCommFilter && communityId != -1)
            {
               if ((doNotApplyFilters
                     & PSRelationshipConfig.FILTER_TYPE_FOLDER_COMMUNITY)
                     == PSRelationshipConfig.FILTER_TYPE_FOLDER_COMMUNITY)
               {
                  filterFlags |= FILTER_ITEM;
               }
               if ((doNotApplyFilters 
                     & PSRelationshipConfig.FILTER_TYPE_ITEM_COMMUNITY)
                     == PSRelationshipConfig.FILTER_TYPE_ITEM_COMMUNITY)
               {
                  filterFlags |= FILTER_FOLDER;
               }
               
               if (filterFlags == 0)
                  filterFlags |= (FILTER_FOLDER | FILTER_ITEM);
               
               result = filterByCommunity(result, communityId, 
                     filterFlags);
            }

            // only skip if user is rxserver user
            if (!((doNotApplyFilters 
                  & PSRelationshipConfig.FILTER_TYPE_FOLDER_PERMISSIONS)
                  == PSRelationshipConfig.FILTER_TYPE_FOLDER_PERMISSIONS 
                  && PSThreadRequestUtils.isInternalUser()))
            {
               result = filterByFolderPermissions(result);
            }

            // TODO: fix this, note that when isNotEmpty check for category is removed lookup
            // for items and folders breaks :/
            // put in place for recycle bin.
            if (StringUtils.isNotEmpty(filter.getName()) && StringUtils.isNotEmpty(filter.getCategory())) {
               List<PSRelationship> filteredList = new ArrayList<>();
               for (PSRelationship rel : result) {
                  if (rel.getConfig().getName().equalsIgnoreCase(filter.getName())
                        && rel.getConfig().getCategory().equalsIgnoreCase(filter.getCategory())) {
                     filteredList.add(rel);
                  }
               }
               result = filteredList;
            }
         }
        
         return result;
      }
      catch (PSCmsException e)
      {
         throw e;
      }
      catch (PSException e)
      {
         throw new PSCmsException(e);
      }
   }
   


private List<PSRelationship> getRelationshipsFromAaCache(PSFolderRelationshipCache relCache, PSRelationshipFilter filter, QueryRelationshipCriteria params) throws PSCmsException
{
   List<PSRelationship> rels = new ArrayList<>();
   List<PSRelationship> result = null;

   int relId = filter.getRelationshipId();
   PSLocator owner = filter.getOwner();
 
   String slot = filter.getProperties().get("sys_slotid");
   // get the relationships according to the query criteria
   if (relId!=-1)
   {
      PSRelationship rel = null;
      try {
         rel = relCache.getRelationship(relId);
         if (rel != null)
            rels.add(rel);
      } catch (PSNotFoundException e) {
         log.warn("Relationship Not Found : {} : Error : {} " ,relId, PSExceptionUtils.getMessageForLog(e));
      }
   }
   else if (owner != null) 
   {
     
      rels = relCache.getAaChildren(owner,slot);
      
      if (params.m_dependentIds == null 
            && params.m_dependentContentTypeIds == null
            && params.m_dependentObjectType == -1)
         result = rels;
   }
   else if (params.m_dependentIds != null)
   {
      boolean publicRev = filter.getLimitToPublicOwnerRevision();
      boolean tip = filter.getLimitToTipOwnerRevision() || filter.getLimitToEditOrCurrentOwnerRevision();
      boolean current = filter.getLimitToEditOrCurrentOwnerRevision();
      
      if (! (publicRev || tip || current) )
         publicRev = tip = current = true;
      
      for (Integer depId : params.m_dependentIds)
         rels.addAll(relCache.getAAParents(publicRev,tip,current,slot,new  PSLocator(depId)));
      
      if (params.m_dependentContentTypeIds == null
            && params.m_dependentObjectType == -1)
         result = rels;
   }
   Collection<String> filterRelNames = filter.getNames();
   
   boolean crossSite = filter.getLimitToCrossSiteLinks();
   boolean filterNames = filterRelNames!=null && !filterRelNames.isEmpty();
   if (result!=null && (crossSite || filterNames))
   {
      rels.removeIf(rel -> (crossSite && (rel.getLegacySiteId() == null && rel.getLegacyFolderId() == null))
              || (filterNames && !filterRelNames.contains(rel.getConfig().getName())));

   } else if (result == null && rels != null) // need to filter the relationships
   {
      // apply the criteria to filter the relationships
      result = new ArrayList<>(rels.size());
      int ownerId;
      int dependentId;
      IPSItemEntry dependent; 
      for (PSRelationship rel : rels)
      {
         if (filterRelNames!=null && !filterRelNames.isEmpty() && !filterRelNames.contains(rel.getConfig().getName()))
            continue;
            
         ownerId = rel.getOwner().getId();
         dependentId = rel.getDependent().getId();
         dependent = relCache.getItemCache().getItem(dependentId);
         if (dependent==null)
            continue;
         // testing the relationship id
         if (params.m_relationshipId != -1
               && params.m_relationshipId != rel.getId())
            continue;
         
         // testing owner
         if (params.m_ownerId != -1 && params.m_ownerId != ownerId)
            continue;
         
         // testing dependent(s)
         if (params.m_dependentIds != null)
         {
            if (!params.m_dependentIds.contains(dependentId))
               continue;
         }
         
         if (params.m_dependentContentTypeIds != null
               && (!params.m_dependentContentTypeIds.contains(
                 (long) dependent.getContentTypeId())))
            continue;
         
         if (params.m_dependentObjectType != -1
               && params.m_dependentObjectType != dependent.getObjectType())
            continue;
         
         result.add(rel);
      }
   }

   return result;
   
   }
   
   /**
    * Set the additional dependent properties, such as the community id and
    * object type for the supplied relationship list.
    * 
    * @param rels the relationship list, assumed never <code>null</code>, but
    *   may be empty.
    * 
    * @throws PSCmsException if an error occurs.
    */
   private void setExtraDependentProperties(Collection<PSRelationship> rels)
         throws PSCmsException
   {
      PSFolderRelationshipCache folderCache = PSFolderRelationshipCache
            .getInstance();
      if (folderCache == null)
      {
         // get component summary for all dependent items  
         Collection<PSLocator> locators = new ArrayList<>(rels.size());
         for (PSRelationship rel : rels)
            locators.add(rel.getDependent());

         PSServerFolderProcessor folderProcessor = PSServerFolderProcessor.getInstance();
         PSComponentSummaries sums = folderProcessor.getComponentSummaries( locators.iterator(), null, false);

         // set the additional properties
         PSComponentSummary summary;
         for (PSRelationship rel : rels)
         {
            summary = sums.getComponentFromId(rel.getDependent().getId());
            if (summary != null)
            {
               rel.setDependentCommunityId(summary.getCommunityId());
               rel.setDependentObjectType(summary.getObjectType());
            }
         }
      }
      else // set the properties from item cache
      {
         IPSItemEntry dependent; 
         for (PSRelationship rel : rels)
         {
            dependent = folderCache.getItemCache().getItem(rel.getDependent().getId());
            if (dependent==null)
               continue;
            rel.setDependentCommunityId(dependent.getCommunityId());
            rel.setDependentObjectType(dependent.getObjectType());
         }
      }
   }
   
   /**
    * Gets a integer value of the specified parameter.
    * 
    * @param paramName the name of the parameter value, assumed not 
    *   <code>null</code>.
    * @param params the looked up parameters, assumed not <code>null</code>.
    * 
    * @return the integer value of the parameter. It may be <code>-1</code>
    *   if not found or invalid integer value.
    */
   private static int getParameterInt(String paramName, Map<String, Object>params)
   {
      String n = (String)params.get(paramName);
      if (n != null && n.trim().length() > 0)
      {
         try
         {
            return Integer.parseInt(n);
         }
         catch (NumberFormatException ne)
         {
            // ignore the bad value
         }
      }

      return -1;
   }
   
   /**
    * Creates a relationship filter from a supplied parameters.
    * 
    * @param params the parameters, never <code>null</code>.
    * 
    * @return the created filter, never <code>null</code>.
    */
   public static PSRelationshipFilter getFilterFromParameters(
         Map<String, Object> params)
   {
      if (params == null)
         throw new IllegalArgumentException("params must not be null.");
      
      PSRelationshipFilter filter = new PSRelationshipFilter();

      // get the standard parameters
      
      int rid = getParameterInt(IPSHtmlParameters.SYS_RELATIONSHIPID, params);
      if (rid != -1)
         filter.setRelationshipId(rid);

      String name = (String)params.get(IPSHtmlParameters.SYS_RELATIONSHIPTYPE);
      if (name != null && name.trim().length() > 0)
         filter.setName(name);

      int ownerId = getParameterInt(IPSHtmlParameters.SYS_CONTENTID, params);
      if (ownerId != -1)
      {
         int rev = getParameterInt(IPSHtmlParameters.SYS_REVISION, params);
         filter.setOwner(new PSLocator(ownerId, rev));
      }

      int dependentId = getParameterInt(IPSHtmlParameters.SYS_DEPENDENTID, params);
      if (dependentId != -1)
         filter.setDependent(new PSLocator(dependentId, -1));
      
      int slotId = getParameterInt(IPSHtmlParameters.SYS_SLOTID, params);
      if (slotId != -1)
         filter.setProperty(IPSHtmlParameters.SYS_SLOTID, slotId + "");
      
      String pname = (String) params.get("sys_propname");
      String pvalue = (String) params.get("sys_propvalue");
      if (pname != null && pvalue != null)
         filter.setProperty(pname, pvalue);
      
      // handle owner join
      String ownerJoin  = (String)params.get(SYS_JOINSELECTOR);
      if (ownerJoin != null && ownerJoin.equals(OWNER_JOINED))
      {
         int ctId = getParameterInt(IPSHtmlParameters.SYS_CONTENTTYPEID, params);
         if (ctId != -1)
            filter.setOwnerContentTypeId(ctId);

         int objType = getParameterInt(IPSHtmlParameters.SYS_OBJECTTYPE, params);
         if (objType != -1)
            filter.setOwnerObjectType(objType);
      }
      else
      {
         int ctId = getParameterInt(IPSHtmlParameters.SYS_CONTENTTYPEID, params);
         if (ctId != -1)
            filter.setDependentContentTypeId(ctId);

         int objType = getParameterInt(IPSHtmlParameters.SYS_OBJECTTYPE, params);
         if (objType != -1)
            filter.setDependentObjectType(objType);
      }
      
      return filter;
   }
   
   /**
    * Queries the relationships of requested type for the supplied owner. This
    * creates a new request with the internal Rhythmyx user to perform the
    * requested query.
    *
    * @param type the relationship configuration for which to query the
    *    relationships, not <code>null</code>.
    * @param owner the relationship owner for which to query the relationships,
    *    not <code>null</code>.
    *
    * @param doNotApplyFilters mask to restrict the filtering of relationships
    * based on community or folder permissions (bitwise OR the FILTER_BY_xxx
    * constants defined in <code>PSRelationshipConfig</code> to restrict
    * filtering on both), should be set to <code>0</code> if filtering is to be
    * performed.
    *
    * @return a set of relationships of the supplied type owned by the provided
    *    owner, never <code>null</code>, might be empty.
    * @throws PSCmsException if anything goes wrong.
    */
   public PSRelationshipSet queryRelationships(PSRelationshipConfig type,
      PSLocator owner, int doNotApplyFilters) throws PSCmsException
   {
      return queryRelationships(type, owner, false, doNotApplyFilters);
   }

   /**
    * Queries the relationships of requested type for the supplied locator.
    * This creates a new request with the internal Rhythmyx user to perform the
    * requested query.
    *
    * @param type the relationship configuration for which to query the
    *    relationships, not <code>null</code>.
    * @param locator the relationship locator for which to query the
    *    relationships, not <code>null</code>. Must be either the owner or
    *    dependent locator depending on the supplied flag.  If the revision is
    *    <code>-1</code> it will be ignored and all relationships of the 
    *    specified type for the specified id will be returned.
    *
    * @param lookupDependents <code>true</code> to lookup relationships for
    *    dependents, <code>false</code> to lookup relationships for owners.
    *
    * @param doNotApplyFilters mask to restrict the filtering of relationships
    * based on community or folder permissions (bitwise OR the FILTER_BY_xxx
    * constants defined in <code>PSRelationshipConfig</code> to restrict
    * filtering on both), should be set to <code>0</code> if filtering is to be
    * performed.
    *
    * @return a set of relationships of the supplied type with the provided
    *    locator as owner or dependent depending on the
    *    <code>lookupDependents</code> flag, never <code>null</code>, might be
    *    empty.
    *
    * @throws PSCmsException if anything goes wrong.
    */
   public PSRelationshipSet queryRelationships(PSRelationshipConfig type,
         PSLocator locator, boolean lookupDependents, int doNotApplyFilters)
         throws PSCmsException
   {
      if (type == null)
         throw new IllegalArgumentException("type cannot be null");

      if (locator == null)
         throw new IllegalArgumentException("locator cannot be null");

      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setName(type.getName());
      
      if (notFilterByCommunity(doNotApplyFilters))
      {
         filter.setCommunityFiltering(false);
      }
      
      boolean hasRevision = (locator.getRevision() != -1);
      if (lookupDependents)
      {
         if (type.useDependentRevision() && hasRevision)
            filter.setDependent(locator);
         else
            filter.setDependentId(locator.getId());
      }
      else
      {
         if (type.useOwnerRevision() && hasRevision)
         {
            filter.setOwner(locator);
            filter.limitToOwnerRevision(true);
         }
         else
            filter.setOwnerId(locator.getId());
      }
      List<PSRelationship> result = getRelationshipList(filter,
            doNotApplyFilters);
      PSRelationshipSet resultSet = new PSRelationshipSet(result.size());
      resultSet.addAll(result);
      return resultSet;
   }

   /**
    * This method looks at the do not apply filter constant supplied to see if
    * community filtering is not required. The check is evaluated as follows (in
    * the same order):
    * <ol>
    * <li><code>true</code> if not by community is <code>true</code> and
    * not by item community is <code>true</code> and not by folder community
    * is <code>true</code></li>
    * <li><code>true</code> if not by item community is <code>true</code>
    * and not by folder community is <code>true</code></li>
    * <li><code>true</code> if not by community is <code>true</code></li>
    * </ol>
    * 
    * @param doNotApplyFilters integer value expected to consist of none, one,
    * or more {@link PSRelationshipConfig} FILTER_TYPE flags.
    * @return <code>true</code> if community filtering is required based on
    * the above algorithm, <code>false</code> otheriwse.
    */
   private boolean notFilterByCommunity(int doNotApplyFilters)
   {
      boolean notByCom = 
         (doNotApplyFilters & PSRelationshipConfig.FILTER_TYPE_COMMUNITY) 
            == PSRelationshipConfig.FILTER_TYPE_COMMUNITY;
      boolean notByItemCom = 
         (doNotApplyFilters & PSRelationshipConfig.FILTER_TYPE_ITEM_COMMUNITY) 
            == PSRelationshipConfig.FILTER_TYPE_ITEM_COMMUNITY;
      boolean notByFolderCom = 
         (doNotApplyFilters & PSRelationshipConfig.FILTER_TYPE_FOLDER_COMMUNITY) 
            == PSRelationshipConfig.FILTER_TYPE_FOLDER_COMMUNITY;
      
      return  (notByCom && notByItemCom && notByFolderCom)
         || (notByItemCom && notByFolderCom) 
         || notByCom;

   }

   /**
    * Returns the singleton instance of the folder relationship cache according
    * to the supplied relationship name.
    * 
    * @param relationshipName
    *           the relationship name, assume not <code>null</code> or empty.
    * 
    * @return the folder relationship cache object, may be <code>null</code>
    *         if the cache is not initialized or the relationship name is not
    *         the name of the folder relationship.
    */
   private PSFolderRelationshipCache getFolderCache(String relationshipName)
   {
      PSFolderRelationshipCache cache = PSFolderRelationshipCache.getInstance();
      
      if (cache != null
            && (!relationshipName
                  .equalsIgnoreCase(PSRelationshipConfig.TYPE_FOLDER_CONTENT) || !relationshipName.equalsIgnoreCase(PSRelationshipConfig.TYPE_RECYCLED_CONTENT)))
      {
         return cache;
      }
      else
      {
         return null;
      }
   }
   
   /**
    * Gets the relationships from the folder cache according to the supplied
    * parameters.
    * 
    * @param relCache the instance of the folder cache, assumed not 
    *    <code>null</code>.
    * @param params the parameters, assume not <code>null</code>.
    * 
    * @return a list of relationships, never <code>null</code> may be empty.
    * 
    * @throws PSCmsException if an error occurs.
    */
   private List<PSRelationship> getRelationshipsFromFolderCache(
         PSFolderRelationshipCache relCache,
         QueryRelationshipCriteria params, PSRelationshipFilter filter) throws PSCmsException
   {
      List<PSRelationship> rels = new ArrayList<>();
      List<PSRelationship> result = null;

      // owner must be folder content type if specified
      if (params.m_ownerContentTypeId != -1
            && params.m_ownerContentTypeId != PSFolder.FOLDER_CONTENT_TYPE_ID)
         return rels;

      // owner must be folder object type if specified
      if (params.m_ownerObjectType != -1
            && params.m_ownerObjectType != PSComponentSummary.TYPE_FOLDER)
         return rels;

      // get the relationships according to the query criteria
      if (params.m_relationshipId != -1)
      {
         PSRelationship rel = null;
         try {
            rel = relCache.getRelationship(params.m_relationshipId);
            if (rel != null)
               rels.add(rel);
         } catch (PSNotFoundException e) {
            log.warn("Relationship Not Found : {} : Error : {} " ,params.m_relationshipId, PSExceptionUtils.getMessageForLog(e));
         }

      }
      else if (params.m_ownerId != -1) 
      {
         rels = relCache.getChildren(new PSLocator(params.m_ownerId), filter);
         
         if (params.m_dependentIds == null 
               && params.m_dependentContentTypeIds == null
               && params.m_dependentObjectType == -1)
            result = rels;
      }
      else if (params.m_dependentIds != null)
      {
         for (Integer depId : params.m_dependentIds)
            rels.addAll(relCache.getParents(new PSLocator(depId)));
         
         if (params.m_dependentContentTypeIds == null
               && params.m_dependentObjectType == -1)
            result = rels;
      }

      if (result == null && rels!= null) // need to filter the relationships
      {
         // apply the criteria to filter the relationships
         result = new ArrayList<>(rels.size());
         int ownerId, dependentId;
         IPSItemEntry dependent; 
         for (PSRelationship rel : rels)
         {
            ownerId = rel.getOwner().getId();
            dependentId = rel.getDependent().getId();
            dependent = relCache.getItemCache().getItem(rel.getDependent().getId());
            if (dependent==null)
               continue;
   
            // testing the relationship id
            if (params.m_relationshipId != -1
                  && params.m_relationshipId != rel.getId())
               continue;
            
            // testing owner
            if (params.m_ownerId != -1 && params.m_ownerId != ownerId)
               continue;
            
            // testing dependent(s)
            if (params.m_dependentIds != null)
            {
               if (!params.m_dependentIds.contains(dependentId))
                  continue;
            }
            
            if (params.m_dependentContentTypeIds != null
                  && (!params.m_dependentContentTypeIds.contains(
                    (long) dependent.getContentTypeId())))
               continue;
            
            if (params.m_dependentObjectType != -1
                  && params.m_dependentObjectType != dependent.getObjectType())
               continue;
            
            result.add(rel);
         }
      }

      return result;
   }
   
   /**
    * Gets an item from the item cache for the supplied content id.
    * 
    * @param itemCache the Item Cache instance, assumed not 
    *    <code>null</code>.
    * @param id the content id of the item.
    * @param errorCode the error code if the item not exist.
    * 
    * @return the cached item, never <code>null</code>.
    * 
    * @throws PSCmsException if the item not exist in item cache.
    */
   private IPSItemEntry getItemFromCache(PSItemSummaryCache itemCache,
         int id, int errorCode) throws PSCmsException
   {
      IPSItemEntry item = itemCache.getItem(id);
      if (item == null)
      {
         Object[] args = new String[]{Integer.toString(id)};
         throw new PSCmsException(errorCode, args);
      }
      
      return item;
   }

   /**
    * Determines if the relationship query can be handled by folder 
    * relationship cache. It parses the request parameters and returns the
    * result to the caller in <code>criteria</code>.
    * <p>
    * Note: the sys_revision parameter will be ignored.
    *   
    * @param filter the relationship filter, assumed not <code>null</code>.
    * @param criteria the to be returned parameters, may be <code>null</code>.
    * 
    * @return the instance of the Folder Cache if the request can be processed 
    *    by folder cache; otherwise return <code>null</code>.
    */
   private PSFolderRelationshipCache canProcessedByFolderCache(PSRelationshipFilter filter,
         QueryRelationshipCriteria criteria)
   {
      PSFolderRelationshipCache folderCache = PSFolderRelationshipCache.getInstance();

      if (folderCache == null)
         return null;

      // Handle custom active assembly relationships but we cannot handle ones
      // with dependent revision.
      if (filter.getNames().size() != 1 || !((filter.getName().equalsIgnoreCase(PSRelationshipConfig.TYPE_FOLDER_CONTENT)) ||
              filter.getName().equalsIgnoreCase(PSRelationshipConfig.TYPE_RECYCLED_CONTENT)))
            return null; // not a folder relationship
     
      criteria.m_relationshipId = filter.getRelationshipId();

      if (filter.getOwner() != null)
         criteria.m_ownerId = filter.getOwner().getId();

      if (filter.getOwnerContentTypeId() != -1)
         criteria.m_ownerContentTypeId = filter.getOwnerContentTypeId();

      if (filter.getOwnerObjectType() != -1)
         criteria.m_ownerObjectType = filter.getOwnerObjectType();

      if (filter.getDependent() != null)
      {
         List<Integer> ids = new ArrayList<>();
         for (PSLocator loc : filter.getDependents())
            ids.add(loc.getId());
         criteria.m_dependentIds = ids;
      }

      // List<Long> m_dependentContentTypeIds
      if (filter.getDependentContentTypeId() != -1)
         criteria.m_dependentContentTypeIds = filter.getDependentContentTypeIds();

      if (filter.getDependentObjectType() != -1)
         criteria.m_dependentObjectType = filter.getDependentObjectType();

      if ((criteria.m_relationshipId == -1) && (criteria.m_ownerId == -1) && (criteria.m_dependentIds == null))
      {
         // unlikely query. Let database handle it if requested.
         return null;
      }

      return folderCache;
   }
   
   
   /**
    * Determines if the relationship query can be handled by folder 
    * relationship cache. It parses the request parameters and returns the
    * result to the caller in <code>criteria</code>.
    * <p>
    * Note: the sys_revision parameter will be ignored.
    *   
    * @param filter the relationship filter, assumed not <code>null</code>.
    * @param criteria the to be returned parameters, may be <code>null</code>.
    * 
    * @return the instance of the Folder Cache if the request can be processed 
    *    by folder cache; otherwise return <code>null</code>.
    */
   private PSFolderRelationshipCache canProcessedByAssemblyCache(PSRelationshipFilter filter,
         QueryRelationshipCriteria criteria)
   {
      PSFolderRelationshipCache assemblyCache = PSFolderRelationshipCache.getInstance();

      if (assemblyCache == null)
         return null;

      // Handle custom active assembly relationships but we cannot handle ones
      // with dependent revision.
  
      if (!(StringUtils.equals(filter.getName(),PSRelationshipConfig.TYPE_ACTIVE_ASSEMBLY) || StringUtils.equals(filter.getCategory(),PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY)))
         return null;
      
      
     
      // If Owner specified we only use this cache for three main item
      // revisions, older items
      // would be processed through db
      PSLocator owner = filter.getOwner();
      if (owner != null)
      {
         // we do not have all revisions so only use cache if limiting to owner
         // revision.
         if (!filter.getLimitToOwnerRevision())
         {
            log.debug("Limit to Owner Revison not set for relationship with owner ", new Throwable());
            return null;
         }

         int revision = owner.getRevision();
         IPSItemEntry item = assemblyCache.getItemCache().getItem(owner.getId());
         if (item==null)
         {
            log.debug("Cannot find owner from cache {}", owner.getId());
            return null;
         }
         // We check for folder as we do not want to query db for looking for active
         // assembly relationships for folder owner.
         if (item.getObjectType()!=PSCmsObject.TYPE_FOLDER && (item.getCurrentRevision() != revision && item.getTipRevision() != revision
               && item.getPublicRevision() != revision) )
            return null;

      }

      
    
      criteria.m_relationshipId = filter.getRelationshipId();

      if (filter.getOwner() != null)
         criteria.m_ownerId = filter.getOwner().getId();

      if (filter.getOwnerContentTypeId() != -1)
         criteria.m_ownerContentTypeId = filter.getOwnerContentTypeId();

      if (filter.getOwnerObjectType() != -1)
         criteria.m_ownerObjectType = filter.getOwnerObjectType();

      if (filter.getDependent() != null)
      {
         List<Integer> ids = new ArrayList<>();
         for (PSLocator loc : filter.getDependents())
            ids.add(loc.getId());
         criteria.m_dependentIds = ids;
         
         boolean publicRev = filter.getLimitToPublicOwnerRevision();
         boolean tip = filter.getLimitToTipOwnerRevision() || filter.getLimitToEditOrCurrentOwnerRevision();
         boolean current = filter.getLimitToEditOrCurrentOwnerRevision();
         // cache does not store all revisions of owner
         if (! (publicRev || tip || current) )
         {
            log.debug("Using dependent and not specifying owner limit will not use Aa Cache : {}", filter);
            return null;
         }
           
         
         
      }

      // List<Long> m_dependentContentTypeIds
      if (filter.getDependentContentTypeId() != -1)
         criteria.m_dependentContentTypeIds = filter.getDependentContentTypeIds();

      if (filter.getDependentObjectType() != -1)
         criteria.m_dependentObjectType = filter.getDependentObjectType();

      if ((criteria.m_relationshipId == -1) && (criteria.m_ownerId == -1) && (criteria.m_dependentIds == null))
      {
         // unlikely query. Let database handle it if requested.
         return null;
      }

      return assemblyCache;
   }
   
   /**
    * Container class for the query criteria of the requested relationships. 
    */
   private class QueryRelationshipCriteria
   {
      protected int m_relationshipId = -1;
      protected int m_ownerId = -1;
      protected long m_ownerContentTypeId = -1;
      protected int m_ownerObjectType = -1;
      protected List<Integer> m_dependentIds = null;
      protected List<Long> m_dependentContentTypeIds = null;
      protected int m_dependentObjectType = -1;
   }
   
   /**
    * Filter the relationship set specified by the community id that is also
    * specified. If the dependent id's community is set to -1, then it is added
    * to the filtered result list, otherwise the dependent id's community is
    * compared to the one specified.
    *
    * Note: We always keep any "item", this is a workaround since there is no
    * difference between an item related to a folder and a folder related to
    * another folder. This is because we are just filtering folders for now. We
    * do this by checking the objectType within the relationship.
    * @param rels the relationship set to be filtered, assumed not
    * </code>null</code>, may be an empty set
    * @param communityId the id of the community to filter on
    * 
    * @param filterFlags Assumed 1 or more of the FILTER_xxx flags OR'd 
    * together.
    *
    * @return the relationships that containing only the valid
    * filtered {@link PSRelationship) objects, may be an empty list,
    * never <code>null</code>
    */
   private List<PSRelationship> filterByCommunity(
         Collection<PSRelationship> rels, int communityId, int filterFlags)
   {
      List<PSRelationship> filteredSet = new ArrayList<>(
            rels.size());
      for (PSRelationship rel : rels)
      {
         boolean filterFolders = (filterFlags & FILTER_FOLDER) > 0;
         boolean filterItems = (filterFlags & FILTER_ITEM) > 0;
         
         // if an item || all communities || match specified community
         // add it to the filtered set of relationships
         if (rel.getConfig().useCommunityFilter() 
            && rel.getDependentCommunityId() != -1
            && rel.getDependentCommunityId() != communityId)
         {
            if ((filterItems
                  && rel.getDependentObjectType() == PSCmsObject.TYPE_ITEM)
                  || (filterFolders
                  && rel.getDependentObjectType() == PSCmsObject.TYPE_FOLDER))
            {
               continue;
            }
         }
         filteredSet.add(rel);
      }
      return filteredSet;
   }

   /**
    * Filters the relationship set based on the user permissions. If the user's
    * permission is set to "No Access" then it is removed from the returned set.
    * The user must alteast have "Read Access" on owner and dependent objects
    * if a relationship is to be included in the returned set.
    *
    * @param rels the relationship set to be filtered, assumed not
    * </code>null</code>, may be an empty set
    *
    * @return the modified relationship set containing only the valid
    * filtered <code>PSRelationShip</code> objects, may be an empty list,
    * never <code>null</code>
    * 
    * @throws PSCmsException if an error occurs.
    */
   private List<PSRelationship> filterByFolderPermissions(
         List<PSRelationship> rels) throws PSCmsException
   {
      List<PSRelationship> filteredSet = new ArrayList<>(
            rels.size());
      for (PSRelationship rel : rels)
      {
         if(rel.getConfig().getName().equalsIgnoreCase(
            PSRelationshipConfig.TYPE_FOLDER_CONTENT))
         {
            if (!checkFolderPermission(rel, 
               PSObjectPermissions.ACCESS_READ, false))
            {
               continue;
            }
         }
         filteredSet.add(rel);
      }
      return filteredSet;
   }


   /**
    * Checks if the specified relationship if of type "Folder Content" and the
    * dependend object is of type "Folder". If <code>true</code> then obtains
    * the permissions on the parent and child folder. If either parent or child
    * folder does not have permission specified by <code>accessLevel</code>,
    * then returns <code>false</code> if <code>throwException</code> is
    * <code>false</code> otherwise throws <code>PSCmsException</code>.
    *
    * @param rel relationship specifiying the parent and child folder locators,
    * assumed not <code>null</code>
    *
    * @param accessLevel the level of access to check for on the parent and
    * child folder, should be non-negative
    *
    * @param throwException if <code>true</code> then an exception is thrown
    * if the user does not have the specified permission on the parent or child
    * folder, otherwise <code>false</code> is returned.
    *
    * @return <code>true</code> if the user has the specfied permission on the
    * parent and child folder, <code>false</code> otherwise. If the child is
    * not a folder, <code>true</code> is returned.
    *
    * @throws IllegalArgumentException if <code>accessLevel</code> is invalid
    * @throws PSCmsException if the user does not the specified permission
    * on parent or child folder and <code>throwException</code> is
    * <code>true</code>
    */
   private boolean checkFolderPermission(PSRelationship rel, int accessLevel,
      boolean throwException) throws PSCmsException
   {
      if (!thread_context && m_context == null)
         return true;

      if (rel.getDependentObjectType() != PSCmsObject.TYPE_FOLDER)
         return true;

      PSRelationshipConfig type = rel.getConfig();
      String configType = type.getName();
      if (!configType.equalsIgnoreCase(PSRelationshipConfig.TYPE_FOLDER_CONTENT))
         return true;

      String newLine = System.getProperty("line.separator", "\r\n");

      PSObjectPermissions perm = PSFolderSecurityManager.getPermissions(rel.getOwner().getId());
      if (!perm.hasAccess(accessLevel))
      {
         if (!throwException)
            return false;
         else
         {
            throw new PSCmsException(IPSCmsErrors.FOLDER_ERROR_MSG,
               "Error updating folder relationships." + newLine +
               rel.toString());
         }
      }

     
      perm = PSFolderSecurityManager.getPermissions(rel.getDependent().getId());
      if (!perm.hasAccess(accessLevel))
      {
         if (!throwException)
            return false;
         else
         {
            throw new PSCmsException(IPSCmsErrors.FOLDER_ERROR_MSG,
               "Error updating folder relationships." + newLine +
               rel.toString());
         }
      }

      return true;
   }

   /**
    * Creates a <code>PSRelationshipSet</code> and then calls
    * {@link #modifyRelationships(PSRelationshipSet)}.
    *
    * @param relationship the relationship to modify, not <code>null</code>.
    *    Provide a realtionship id of -1 to perform an insert or >= 0 to
    *    perform an update.
    * @throws PSCmsException if anything goes wrong processing the request.
    */
   public void modifyRelationship(PSRelationship relationship)
      throws PSCmsException
   {
      if (relationship == null)
         throw new IllegalArgumentException("relationship cannot be null");

      PSRelationshipSet set = new PSRelationshipSet();
      set.add(relationship);

      modifyRelationships(set);
   }

   /**
    * Creates a modify plan for the supplied relationships and then performs
    * inserts before updates.
    *
    * @param relationships a set of <code>PSRelationship</code> objects.
    *    Relationships with an id of -1 will be inserted, relationships with
    *    id's >= 0 will be updated. Not <code>null</code>, may be empty.
    * @throws PSCmsException if anything goes wrong processing the request.
    */
   public void modifyRelationships(PSRelationshipSet relationships)
      throws PSCmsException
   {
      if (relationships == null)
         throw new IllegalArgumentException("relationships cannot be null");
      if (!relationships.isEmpty())
         processModifyPlan(createModifyPlan(relationships));
   }

   /**
    * Deletes the supplied relationship.
    *
    * @param relationship the relationship to be deleted, not <code>null</code>.
    * @throws PSCmsException if anything goes wrong processing the request.
    */
   public void deleteRelationship(PSRelationship relationship)
      throws PSCmsException
   {
      if (relationship == null)
         throw new IllegalArgumentException("relationship cannot be null");

      PSRelationshipSet set = new PSRelationshipSet();
      set.add(relationship);

      deleteRelationships(set);
   }

   /**
    * Set a new request.
    *
    * @param request the request to use, if <code>null</code> is provided, the
    *    internal rhythmyx user will be used to perform the requests.
    */
   public void setRequest(PSRequest request)
   {
      if (request != null)
         m_context = new PSRequestContext(request);
      else
         m_context = null;
   }

   /**
    * Set a new request context.
    *
    * @param context the request context to use, if <code>null</code> is
    *    provided, the internal rhythmyx user will be used to perform the
    *    requests.
    */
   public void setRequestContext(IPSRequestContext context)
   {
      m_context = context;
   }

   /**
    * Reset both owner and/or dependent revisions to <code>-1</code> if a
    * specified relationship is configured to ignore its corresponding
    * revisions.
    *
    * @param relationship The to be modified relationship object, assume it is
    *    not <code>null</code>.
    */
   private void resetRevision(PSRelationship relationship)
   {
      if (!relationship.getConfig().useOwnerRevision())
      {
         relationship.getOwner().setRevision(-1);
      }

      if (!relationship.getConfig().useDependentRevision())
      {
         relationship.getDependent().setRevision(-1);
      }
   }

   /**
    * Deletes all provided relationships.
    *
    * @param relationships a set of relationships to be deleted, not
    *    <code>null</code>, may be empty.
    * @throws PSCmsException if anything goes wrong processing the request.
    */
   @SuppressWarnings("unchecked") //rel iterator
   public void deleteRelationships(PSRelationshipSet relationships)
      throws PSCmsException
   {
      if (relationships == null)
         throw new IllegalArgumentException("relationships cannot be null");

      processModify(PSApplicationBuilder.REQUEST_TYPE_VALUE_DELETE, relationships);
      if (!relationships.isEmpty() && log.isDebugEnabled())
      {
         StringBuilder buf = new StringBuilder();
         for (PSRelationship r : (Iterable<PSRelationship>) relationships) {
            if (buf.length() > 0)
               buf.append("\n");
            buf.append(r.toString());
         }
         Exception e = new Exception(
               "The following trace is for tracking what code performed the deletions, no exception actually occurred.");
         log.debug("The following {}  relationship(s) have been deleted: {} \n",relationships.size(), buf.toString(), e);
      }
   }

   /**
    * Processes the supplied modify plan.
    *
    * @param plan the modify plan to be processed, assumed not <code>null</code>
    *    may be empty.
    * @throws PSCmsException if anything goes wrong processing the plan.
    */
   private void processModifyPlan(Map<String, PSRelationshipSet> plan)
         throws PSCmsException
   {
      Set<Map.Entry<String, PSRelationshipSet>> plans = plan.entrySet(); 
      for (Map.Entry<String, PSRelationshipSet> entry : plans)
      {
         processModify(entry.getKey(), entry.getValue());
      }
   }
   
   /**
    * Enum used when modifying relationships
    */
   private enum ActionEnum 
   {
      INSERT (PSRelationshipChangeEvent.ACTION_ADD),
      UPDATE (PSRelationshipChangeEvent.ACTION_MODIFY),
      DELETE (PSRelationshipChangeEvent.ACTION_REMOVE);

      /**
       * Default ctor.
       * 
       * @param ordinal the enum value.
       */
      ActionEnum(int ordinal)
      {
         m_ordinal = ordinal;
      }
      
      /**
       * @return the enum value of this object.
       */
      public int ordinalValue()
      {
         return m_ordinal;
      }
      
      /**
       * The enum value.
       */
      private int m_ordinal;
   }

   /**
    * Validates the relationship, to make sure both owner and dependent are existing items
    * @param rel the relationship in question.
    * @throws PSCmsException if either owner or dependent does not exist.
    */
   private void validateOwnerAndDependentExist(PSRelationship rel) throws PSCmsException
   {
      PSItemSummaryCache cache = PSItemSummaryCache.getInstance();
      if (cache != null)
      {
         getItemFromCache(cache, rel.getOwner().getId(),
               IPSCmsErrors.NON_EXITING_OWNER);
         getItemFromCache(cache, rel.getDependent().getId(),
               IPSCmsErrors.NON_EXITING_DEPENDENT);
      }
      else // hit the repository 
      {
         List<PSLocator> locators = new ArrayList<>();
         locators.add(rel.getOwner());
         locators.add(rel.getDependent());
         PSServerFolderProcessor folderProcessor = PSServerFolderProcessor.getInstance();
         folderProcessor.getComponentSummaries(
               locators.iterator(), null, false);
      }
   }
   
   /**
    * Processes the requested action for the supplied relationships.
    *
    * @param dbActionType the database action type, assumed one of
    *    <code>PSApplicationBuilder.REQUEST_TYPE_VALUE_INSERT</code> or
    *    <code>PSApplicationBuilder.REQUEST_TYPE_VALUE_UPDATE</code> or
    *    <code>PSApplicationBuilder.REQUEST_TYPE_VALUE_DELETE</code>.
    * @param relationships the relatonships to process, assumed not
    *    <code>null</code>, may be empty.
    * @throws PSCmsException if anything goes wrong performing the request.
    */
   private void processModify(String dbActionType,
      PSRelationshipSet relationships) throws PSCmsException
   {
      //Convert action to an ActionEnum to use in switch statement.
      ActionEnum action = ActionEnum.UPDATE; //default
      if(dbActionType.equals(PSApplicationBuilder.REQUEST_TYPE_VALUE_INSERT))
         action = ActionEnum.INSERT;
      else if(dbActionType.equals(PSApplicationBuilder.REQUEST_TYPE_VALUE_DELETE))
         action = ActionEnum.DELETE;

      try
      {
         if (!relationships.isEmpty())
         {
            PSRelationshipSet result = new PSRelationshipSet();
            IPSRelationshipService svc = PSRelationshipServiceLocator
               .getRelationshipService();
            for (PSRelationship relationship : (Iterable<PSRelationship>) relationships) {
               int execContext = -1;
               PSRelationship rel = relationship;
               checkFolderPermission(rel, PSObjectPermissions.ACCESS_WRITE, true);
               switch (action) {
                  case INSERT:
                     execContext = IPSExecutionContext.RS_PRE_CONSTRUCTION;
                     validateOwnerAndDependentExist(rel);
                     break;

                  case DELETE:
                     execContext = IPSExecutionContext.RS_PRE_DESTRUCTION;
                     resetRevision(rel);
                     break;

                  case UPDATE:
                     execContext = IPSExecutionContext.RS_PRE_UPDATE;
               }

               // run relationship effects BEFORE hit the repository
               PSRelationshipEffectProcessor effectProc = null;
               PSExecutionData data = new PSExecutionDataLight();
               data.setOriginatingRelationship(rel);
               PSRelationshipSet rels = new PSRelationshipSet();
               rels.add(rel);

               effectProc = new PSRelationshipEffectProcessor(rels, data,
                       execContext);
               effectProc.process();

               switch (action) {
                  case INSERT:
                  case UPDATE:
                     svc.saveRelationship(rel);
                     result.add(rel);
                     break;
                  case DELETE:
                     svc.deleteRelationshipByRid(rel.getId());
                     result.add(rel);
                     break;
               }
            }
            relationships.clear();
            relationships.addAll(result);
            updateCaches(action, relationships);
         }
      }
      catch (PSException e)
      {
         throw new PSCmsException(e);
      }
   }

   /**
    * Updates both folder and assembly caches for the modified relationships.
    * 
    * @param action the action has been performed for the given relationships,
    *   assumed not <code>null</code>.
    * @param relationships the modified relationships, assumed not 
    *   <code>null</code>.
    */
   private void updateCaches(ActionEnum action, PSRelationshipSet relationships)
   {
      // notify (Ehcache) listeners
      PSRelationshipChangeEvent event = new PSRelationshipChangeEvent(
            action.ordinalValue(), relationships);
      PSNotificationEvent notifyEvent = new PSNotificationEvent(
            EventType.RELATIONSHIP_CHANGED, event);
      IPSNotificationService srv = PSNotificationServiceLocator
         .getNotificationService();
      srv.notifyEvent(notifyEvent);

      // update the assembly cache
      PSCacheManager mgr = PSCacheManager.getInstance();
      IPSCacheHandler handler = mgr.getCacheHandler(
         PSAssemblerCacheHandler.HANDLER_TYPE);
         
      // if caching not enabled, handler will be null
      if (! (handler instanceof PSAssemblerCacheHandler))
         return;
      
      ((PSAssemblerCacheHandler)handler).relationshipChanged(event);

   }
   
   /**
    * Creates a modify plan for the supplied relationships. Relationships
    * with an id of -1 will be put into the insert plan. Relationships with
    * an id >= 0 will be put into the update plan. Relationships with an
    * invalid id will be ignored.
    *
    * @param relationships a set of relationships for whicch to create the
    *    modify plan, assumed not <code>null</code>.
    * @return the modify plan, never <code>null</code> or empty. The map
    *    key is the db action type, while the value is a
    *    <code>PSRelationshipSet</code> which is never <code>null</code> but
    *    may be empty.
    * @throws PSCmsException if other error occurs.
    */
   private Map<String, PSRelationshipSet> createModifyPlan(
         PSRelationshipSet relationships) throws PSCmsException
   {
      PSRelationshipSet inserts = new PSRelationshipSet();
      PSRelationshipSet updates = new PSRelationshipSet();
      Map<String, PSRelationshipSet> plan = new HashMap<>(2);

      plan.put(PSApplicationBuilder.REQUEST_TYPE_VALUE_INSERT, inserts);
      plan.put(PSApplicationBuilder.REQUEST_TYPE_VALUE_UPDATE, updates);
      
      Collection<Integer> relsExisting = getExisiting(relationships);

      for (PSRelationship psRelationship : (Iterable<PSRelationship>) relationships) {
         PSRelationship relationship = psRelationship;
         resetRevision(relationship);

         if (!relsExisting.contains(relationship.getId())) {
            PSRelationshipProcessor relationshipProcessor = PSRelationshipProcessor.getInstance();
            //If there is a relationship Already Existing then just use that, don't recreate.
            PSRelationship existingRel = relationshipProcessor.checkIfRelationshipAlreadyExists(relationship);
            if (existingRel != null) {
               relationship.setId(existingRel.getId());
               relationship.setPersisted(true);
            } else if (relationship.getId() == -1) {
               int rid = PSRelationshipCommandHandler.getNextId();
               relationship.setId(rid);
            }

            if (!relationship.isPersisted()) {
               relationship.setPersisted(false);
               inserts.add(relationship);
            }

         } else {
            if (!relationship.isPersisted()) {
               relationship.setPersisted(true);
               log.debug("Oops! persisted-flag=FALSE for an existing relationship id: {} ", relationship.getId());
            }
            updates.add(relationship);
         }
      }

      return plan;
   }



   /**
    * Check if the supplied relationships by id exist in the system by returning
    * a set of relationship ids that exist. Executes an internal request passing
    * the relationship ids from the set and gets the relationshipids that exist
    * in the system.
    * 
    * @param relationships relattionship set to check the existence, assumed not
    *           <code>null</code>
    * @return set of relationshipids from the set that DO exist in the database,
    *         never <code>null</code> may be empty.
    */
   private Collection<Integer> getExisiting(PSRelationshipSet relationships)
   {
      Set<Integer> relIds = new HashSet<>(relationships.size());
      for (PSRelationship relationship : (Iterable<PSRelationship>) relationships) {
         PSRelationship element = (PSRelationship) relationship;
         if (element.getId() > 0)
            relIds.add(element.getId());
      }
      
      IPSRelationshipService svc = PSRelationshipServiceLocator
         .getRelationshipService();
      
      return svc.findPersistedRid(relIds);
   }

   /**
    * Get the community for the current request.
    *
    * @return the community of the user of the current request, may be
    *    <code>-1</code> if not found, which indicates all communities.
    */
   private int getCommunity()
   {
      //Assume the request context is available 
      IPSRequestContext req = PSThreadRequestUtils.getReqCtx();
      
      try
      {
         String userName =
            req.getUserContextInformation("User/Name", "").toString();
         if (userName.equals(IPSConstants.INTERNAL_USER_NAME))
         {
            //super user
            return -1;
         }
      }
      catch (PSDataExtractionException e)
      {
         // if happens assume not internal user
      }

      int communityId = 1;
      Object obj =
            req.getSessionPrivateObject(IPSHtmlParameters.SYS_COMMUNITY);
      if (obj != null)
      {
         try
         {
            communityId = Integer.parseInt(obj.toString());
         }
         catch (NumberFormatException ex)
         { /* ignore */
         }
      }
      return communityId;
   }

   /**
    * Get list of all parent locators of the object with specified locator and 
    * specified relationship type name. This method assumes that the dependent 
    * is revision insensitive. This method is used for folder relationship only,
    * but is NOT applicable to the relationship type, such as {@link 
    * PSRelationshipConfig#CATEGORY_ACTIVE_ASSEMBLY}.
    * <p>
    * @param object Locator of the object (item or folder) for which the 
    * owner locator list needs to be built. Must not be <code>null</code>.
    * @param relationshipTypeName Name of the relationship type to base the 
    * path on, must not be <code>null</code> or empty.
    * 
    * @return List of all ancestors of the specified item related via the 
    * relationship type specified. Never <code>null</code> may be empty.
    * 
    * @throws PSCmsException if the list cannot be built for an reason.
    */
   public List<PSLocator> getOwnerLocators(PSLocator object,
         String relationshipTypeName) throws PSCmsException
   {
      if(object == null)
         throw new IllegalArgumentException("object must not be null");
      
      if(relationshipTypeName == null || relationshipTypeName.length()<1)
         throw new IllegalArgumentException(
         "relationshipTypeName must not be null or empty");

      PSFolderRelationshipCache cache = getFolderCache(relationshipTypeName);
      if (cache != null
            && (relationshipTypeName
                  .equalsIgnoreCase(PSRelationshipConfig.TYPE_FOLDER_CONTENT) ||
              relationshipTypeName.equalsIgnoreCase(PSRelationshipConfig.TYPE_RECYCLED_CONTENT)))
      {
         return cache.getOwnerLocators(object, relationshipTypeName);
      }
      else
      {
         return getOwnerLocatorsFromRepository(object, relationshipTypeName);
      }
   }
   
   /**
    * Get list of all parent locators of the object with specified locator and 
    * specified relationship type name. This method assumes that the dependent 
    * is revision insensitive. The list contains immediate parent first, 
    * its parent next and so on.
    * 
    * @param object Locator of the object (item or folder) for which the 
    * owner locator list needs to be built. Must not be <code>null</code>.
    * @param relationshipTypeName Name of the relationship type to base the 
    * path on, must not be <code>null</code> or empty.
    * 
    * @return List of all ancestors of the specified item related via the 
    * relationship type specified. Never <code>null</code> may be empty.
    * 
    * @throws PSCmsException if the list cannot be built for any reason.
    */
   private List<PSLocator> getOwnerLocatorsFromRepository(PSLocator object,
         String relationshipTypeName) throws PSCmsException
   {
      List<PSRelationship> rels = getRelationshipsFromDependent(object,
            relationshipTypeName);
      List<PSLocator> owners = new ArrayList<>();
       
      for (PSRelationship rel : rels)
      {
         getAncestorLocatorsFromRepository(rel.getOwner(), owners,
               relationshipTypeName);
      }
      return owners;
   }
   
   /**
    * Get the content id from the supplied path list names.
    * Note: this is for folder relationship only. Other relationship is not 
    * supported. 
    * 
    * @param path
    *           fully qualified relationship path as explained in
    *           {@link com.percussion.cms.objectstore.IPSRelationshipProcessor#getSummaryByPath(String, String, String)},
    *           must not be <code>null</code> or empty.
    * 
    * @param relationshipTypeName
    *           name of the relatiosnhip type that links the parent and child
    *           items, must not be <code>null</code> or empty.
    * 
    * @return contentid of the dependent item, -1 if the path is empty or 
    *         <code>null</code>, or the specified relationship type does not 
    *         exist between the items or the items themselves do not exist in 
    *         the system.
    * 
    * @throws PSCmsException
    *            if an error occurs.
    */
   public int getIdByPath(
      String path,
      String relationshipTypeName)
      throws PSCmsException
   {
      if (!(relationshipTypeName
            .equalsIgnoreCase(PSRelationshipConfig.TYPE_FOLDER_CONTENT) ||
              relationshipTypeName.equalsIgnoreCase(PSRelationshipConfig.TYPE_RECYCLED_CONTENT)))
         throw new UnsupportedOperationException("Relationship, "
               + relationshipTypeName + ", is not supported by getIdByPath()");
      
      if (StringUtils.isBlank(path))
         return -1;
      
      if (path.equals("/"))
         return PSFolder.ROOT_ID; // root path
      
      final String start = "//";
      final String separator = "/";
      if (!path.startsWith(start))
         throw new IllegalArgumentException(
            "Path: " + path + " - must start with '" + start + "'.");
      path = path.substring(start.length() - 1);
      StringTokenizer tokenizer = new StringTokenizer(path, separator);
      // note the "paths" does not include the root name
      List<String> paths = new ArrayList<>();
      while (tokenizer.hasMoreTokens())
         paths.add(tokenizer.nextToken());
      if (paths.isEmpty() || paths.size() < 1)
      {
         throw new IllegalArgumentException(
               "path must have at least two token separated by '" + separator
                     + "'");
      }
      
      PSFolderRelationshipCache cache = PSFolderRelationshipCache.getInstance();
      if (cache != null)
      {
         return cache.getIdByPath(paths, relationshipTypeName);
      }
      else
      {
         int id = PSFolder.ROOT_ID;
         for (String name : paths)
         {
            id = getChildIdFromName(id, name);
            if (id == -1)
               return -1;
         }

         return id;
      }
   }

   /**
    * Gets a child id from its parent folder id and the name of the child.
    * 
    * @param parentId the parent folder id.
    * @param childName the name of the child item/folder. Assumed not 
    *   <code>null</code>.
    *   
    * @return the child id. It may be <code>-1</code> if cannot find such 
    *   child item/folder.
    * 
    * @throws PSCmsException if an error occurs.
    */
   private int getChildIdFromName(int parentId, String childName)
         throws PSCmsException
   {
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setOwnerId(parentId);
      filter.setName(PSRelationshipConfig.TYPE_FOLDER_CONTENT);
      filter.setCommunityFiltering(false);

      List<PSRelationship> rels = getRelationshipList(filter,
            PSRelationshipConfig.FILTER_TYPE_COMMUNITY);

      for (PSRelationship rel : rels)
      {
         PSComponentSummary summary = cms.loadComponentSummary(rel
               .getDependent().getId());
         
         if (summary.getName().equalsIgnoreCase(childName))
            return rel.getDependent().getId();
      }
      
      return -1;
   }
   
   /**
    * Get object path built for the object parents by the supplied 
    * relationship type name. If the object happens to have multiple immediate 
    * parents, the method returns all paths. The path is build as described below:
    * <p>
    * "/sys_title_root/.../sys_title_second/sys_title_first"
    * <p>
    * If the object has no parents it returns an empty array NOT its title alone.
    * <p>
    * 
    * @param object Locator of the object (item or folder) for which the 
    * owner path needs to be built. Must not be <code>null</code>.
    * @param relationshipTypeName Name of the relationship type to base the 
    * path on and it must be {@link PSRelationshipConfig#TYPE_FOLDER_CONTENT).
    * 
    * @return String array of relationship owner paths as explained above, 
    * never <code>null</code> but may be empty.
    * 
    * @throws PSCmsException if the pah cannot be built for an reason.
    * @throws PSInternalRequestCallException if call to the internal request 
    * to get the paths' document fails for any reason.
    */
   public String[] getOwnerPaths(PSLocator object, String relationshipTypeName)
         throws PSCmsException, PSInternalRequestCallException
   {
      if (object == null)
         throw new IllegalArgumentException("object must not be null");

      if (!PSRelationshipConfig.TYPE_FOLDER_CONTENT
            .equalsIgnoreCase(relationshipTypeName) && !PSRelationshipConfig.TYPE_RECYCLED_CONTENT.equalsIgnoreCase(relationshipTypeName))
         throw new UnsupportedOperationException(
               "getOwnerPaths for relationship '" + relationshipTypeName
                     + "' is not supported.");

      
      PSFolderRelationshipCache cache = getFolderCache(relationshipTypeName);
      if (cache != null)
         return cache.getParentPaths(object, relationshipTypeName);
      else
         return getOwnerPathsFromRepository(object);
   }

   /**
    * Gets a list of relationships which are filtered by the supplied dependent
    * and relationship type name. Note, it is not filtered by community.
    * 
    * @param dependent the dependent, assumed not <code>null</code>.
    * @param relationshipTypeName the relationship type name, assumed not
    *   not <code>null</code>.
    *   
    * @return a list of relationships, never <code>null</code>
    * 
    * @throws PSCmsException
    */
   private List<PSRelationship> getRelationshipsFromDependent(
         PSLocator dependent, String relationshipTypeName)
         throws PSCmsException
   {
      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setDependent(dependent);
      filter.setName(relationshipTypeName);
      filter.setCommunityFiltering(false);
      
      return getRelationshipList(filter,
            PSRelationshipConfig.FILTER_TYPE_COMMUNITY);
   }
   
   /**
    * Get object path built for the object parents by the supplied 
    * relationship type name. If the object happens to have multiple immediate 
    * parents, the method returns all paths. The path is built as described below:
    * <p>
    * "/sys_title_root/.../sys_title_second/sys_title_first"
    * <p>
    * If the object has no parents it returns an empty array NOT its title alone.
    * <p>
    * This method basically executes a Rhythmyx query to get an XML document 
    * based on the following DTD.
    * <p>
    * <!ELEMENT Owner EMPTY>
    * <!ATTLIST  Owner sys_contentid CDATA #REQUIRED>
    * <!ATTLIST  Owner sys_title CDATA #REQUIRED>
    * <!ELEMENT Object (Owner* )>
    * <!ATTLIST  Object sys_contentid CDATA #REQUIRED>
    * <!ATTLIST  Object sys_relationshiptype CDATA #REQUIRED>
    * Then the paths are generated using the sys_title values for each owner object.
    * 
    * @param object Locator of the object (item or folder) for which the 
    * owner path needs to be built. Must not be <code>null</code>.
    * 
    * @return String array of relationship owner paths as explained above, 
    * never <code>null</code> but may be empty.
    * @throws PSCmsException if the pah cannot be built for any reason.
    * @throws PSInternalRequestCallException if call to the internal request 
    * to get the paths' document fails for any reason.
    */
   private String[] getOwnerPathsFromRepository(PSLocator object)
         throws PSCmsException, PSInternalRequestCallException
   {
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();

      List<PSRelationship> rels = getRelationshipsFromDependent(object,
            PSRelationshipConfig.TYPE_FOLDER_CONTENT);
      
      // create path from the locators of the direct parents
      String[] paths = new String[rels.size()];
      for (int i=0; i < paths.length; i++)
      {
         List<PSLocator> parents = new ArrayList<>();
         PSLocator parent = rels.get(i).getOwner();
         getAncestorLocatorsFromRepository(parent, parents,
               PSRelationshipConfig.TYPE_FOLDER_CONTENT);
         Collections.reverse(parents);
         
         // construct the folder path from the "parents" locators
         StringBuilder buffer = new StringBuilder();
         for (PSLocator loc : parents)
         {
            PSComponentSummary summary = cms.loadComponentSummary(loc.getId());
            buffer.append(PATH_SEP);
            buffer.append(summary.getName());
         }
         
         paths[i] = buffer.toString();
      }
      return paths;
   }

   /**
    * Generates a path to all ancestors of the given dependent. It calls itself
    * recursively until reach to the root ancestor, which does not have owner.
    * 
    * @param dependent the locator of a folder child, assumed not 
    *   <code>null</code>.
    * @param locators used to collect all owner/parent locators, assumed not 
    *   <code>null</code>. It adds the ancestors in reverse order, the direct
    *   parent, grand parent, grand grand parent, ...etc.
    * @param relationshipTypeName the name of the relationship config, assumed
    *   not <code>null</code> or empty.
    *   
    * @throws PSCmsException if an error occurs.
    */
   private void getAncestorLocatorsFromRepository(PSLocator dependent,
         List<PSLocator> locators, String relationshipTypeName)
         throws PSCmsException
   {
      locators.add(dependent);
      
      List<PSRelationship> rels = getRelationshipsFromDependent(dependent,
            relationshipTypeName);
      
      for (PSRelationship rel : rels)
      {
         PSLocator plocator = rel.getOwner();
         getAncestorLocatorsFromRepository(plocator, locators,
               relationshipTypeName);
      }
   }

   /**
    * The request context to use to make all internal requests, initialized in
    * constructor, never changed after that. May be <code>null</code>.
    */
   private IPSRequestContext m_context = null;

   /**
    * The HTML parameter used to select the relationship lookup resource
    * depending on how the <code>CONTENTSTATUS</code> table is joined to the 
    * relationship tables. This parameter must be <code>null</code> to use
    * the default which is 'dependentjoined', otherwise it will be set to 
    * 'ownerjoined' to use the correct resource for the lookups.
    */
   private static final String SYS_JOINSELECTOR = "sys_joinselector";
   
   /**
    * The value used to select the relationship lookup resource that joins the
    * <code>CONTENTSTATUS</code> table through the relationship owner id.
    */
   private static final String OWNER_JOINED = "ownerjoined";

   /**
    * One of the <code>FILTER_xxx</code> flags. Used to indicate that items 
    * should be filtered by their community. 
    */
   private static final int FILTER_ITEM = 0x1;

   /**
    * One of the <code>FILTER_xxx</code> flags. Used to indicate that folders 
    * should be filtered by their community. 
    */
   private static final int FILTER_FOLDER = 0x1<<1;
   
   /**
    * The separator used for folder path
    */
   private static final String PATH_SEP = "/";
   
   private static final PSRelationshipDbProcessor instance = new PSRelationshipDbProcessor(true);
   
   private boolean thread_context = false;
   
   /**
    * The logger for this class.
    */
   Logger log = LogManager.getLogger(PSRelationshipDbProcessor.class);

   public static PSRelationshipDbProcessor getInstance()
   {
      return instance;
   }

   
}
