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

package com.percussion.activity.service.impl;

import static com.percussion.itemmanagement.service.impl.PSWorkflowHelper.WF_STATE_ARCHIVE;
import static com.percussion.itemmanagement.service.impl.PSWorkflowHelper.WF_STATE_LIVE;
import static com.percussion.itemmanagement.service.impl.PSWorkflowHelper.WF_STATE_PENDING;
import static com.percussion.itemmanagement.service.impl.PSWorkflowHelper.WF_TAKE_DOWN_TRANSITION;
import static com.percussion.pagemanagement.service.IPSPageService.PAGE_CONTENT_TYPE;
import static java.util.Arrays.asList;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

import com.percussion.activity.data.PSActivityNode;
import com.percussion.activity.data.PSContentActivity;
import com.percussion.activity.service.IPSActivityService;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.itemmanagement.service.IPSItemWorkflowService.PSItemWorkflowServiceException;
import com.percussion.pagemanagement.dao.IPSResourceDefinitionGroupDao;
import com.percussion.pagemanagement.data.PSResourceDefinitionGroup;
import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSAssetResource;
import com.percussion.pathmanagement.service.IPSPathService.PSPathNotFoundServiceException;
import com.percussion.pathmanagement.service.IPSPathService.PSPathServiceException;
import com.percussion.pathmanagement.service.impl.PSPathUtils;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.content.data.PSItemSummary;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.IPSContentPropertyConstants;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.system.IPSSystemService;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.util.PSSiteManageBean;
import com.percussion.utils.date.PSDateRange;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.content.IPSContentWs;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;

/**
 * Utilities for content activity service.
 * 
 * @author yubingchen
 */
@PSSiteManageBean("activityService")
public class PSActivityService implements IPSActivityService
{
    private static Log ms_log = LogFactory.getLog(PSActivityService.class);
    
    IPSSiteManager siteMgr;
    IPSResourceDefinitionGroupDao resDao;
    PSItemDefManager itemDefMgr;
    IPSPublisherService pub;
    IPSContentMgr contentMgr;
    IPSSystemService sysSrv;
    IPSContentWs contentWs;
    IPSItemWorkflowService itemWfSrvc;
    IPSGuidManager guidMgr;
    IPSIdMapper idMapper;

    @Autowired
    public PSActivityService(IPSSiteManager siteMgr, IPSResourceDefinitionGroupDao resDao,
            PSItemDefManager itemDefMgr, IPSPublisherService pub, IPSContentMgr contentMgr, 
            IPSSystemService sysSrv, IPSContentWs contentWs, IPSItemWorkflowService itemWfSrvc,
            IPSGuidManager guidMgr, IPSIdMapper idMapper)
    {
        this.siteMgr = siteMgr;
        this.resDao = resDao;
        this.itemDefMgr = itemDefMgr;
        this.pub = pub;
        this.contentMgr = contentMgr;
        this.contentWs = contentWs;
        this.itemWfSrvc = itemWfSrvc;
        this.sysSrv = sysSrv;
        this.guidMgr = guidMgr;
        this.idMapper = idMapper;
    }
    
    public PSContentActivity createActivity(PSActivityNode node, Date beginDate, long timeout)
    {
        notNull(node);
        notNull(beginDate);
        
        StopWatch sw = new StopWatch();
        sw.start();
        
        // we may already be over the supplied timeout, no sense in continuing
        checkTimeout(sw.getTime(), timeout);
        
        String path = node.getPath();
        String siteName = node.getSiteName();
        Collection<Integer> ids = findPageIdsByPath(path);
        checkTimeout(sw.getTime(), timeout);
        
        Date endDate = new Date();

        int publishedItems = 0;
        if (!ids.isEmpty())
        {
            IPSSite site = siteMgr.findSite(siteName);
            if (site != null)
            {
                publishedItems = pub.findLastPublishedItemsBySite(site.getGUID(), ids);
            }
        }        
        checkTimeout(sw.getTime(), timeout);
        
        int newItems = ids.isEmpty()?0:sysSrv.findNewContentActivities(ids, beginDate, endDate, WF_STATE_LIVE);
        checkTimeout(sw.getTime(), timeout);

        int updatedItems = ids.isEmpty()?0:sysSrv.findNumberContentActivities(ids, beginDate, endDate, WF_STATE_LIVE, null);
        updatedItems -= newItems;
        checkTimeout(sw.getTime(), timeout);

        int archivedItems = ids.isEmpty() ? 0 : sysSrv.findNumberContentActivities(ids, beginDate, endDate,
                WF_STATE_ARCHIVE, WF_TAKE_DOWN_TRANSITION);
        checkTimeout(sw.getTime(), timeout);
        
        int pendingItems = (int)getPendingPageCount(path);
        
        PSContentActivity activity = new PSContentActivity(siteName, node.getPath(), node.getName(), publishedItems, 
                pendingItems, newItems, updatedItems, archivedItems);
        
        return activity;
    }

    /**
     * @param time
     * @param timeout
     */
    private void checkTimeout(long time, long timeout)
    {
        if (time > timeout)
            throw new PSActivityServiceException("The requested data is taking too long to retrieve, sorry!");
    }

    /*
     * (non-Javadoc)
     * @see com.percussion.activity.service.IPSActivityService#findPageIdsByPath(java.lang.String)
     */
    public Collection<Integer> findPageIdsByPath(String path)
    {
        notEmpty(path);
        
        return  getContentIdsByPath(path, Collections.singletonList(PAGE_CONTENT_TYPE));
    }

    public Collection<Integer> findItemIdsByPath(String path, Collection<String> contentTypes)
    {
        notEmpty(path);
        
        return  getContentIdsByPath(path, contentTypes);
    }

    /*
     * (non-Javadoc)
     * @see com.percussion.activity.service.IPSActivityService#findNewContentActivities(java.util.List, java.util.List)
     */
    public List<Integer> findNewContentActivities(Collection<Integer> contentIds, List<Date> dates)
    {
        notNull(contentIds);
        if (dates == null || dates.size() < 2)
            throw new IllegalArgumentException("dates must contain more than 1 Date elements.");
        
        List<Integer> counts = new ArrayList<>(dates.size()-1);
        if (contentIds.isEmpty())
        {
            return Collections.nCopies(dates.size()-1, new Integer(0));
        }
        
        for (int i=1; i<dates.size(); i++)
        {
            Date beginDate = dates.get(i-1);
            Date endDate = dates.get(i);
            int count = sysSrv.findNewContentActivities(contentIds, beginDate, endDate, WF_STATE_LIVE);
            counts.add(count);
        }
        return counts;
    }

    /*
     * (non-Javadoc)
     * @see com.percussion.activity.service.IPSActivityService#findNumberContentActivities(java.util.List, java.util.List, java.lang.String, java.lang.String)
     */
    public List<Integer> findNumberContentActivities(Collection<Integer> contentIds, List<Date> dates, String stateName,
            String transitionName)
    {
        if (dates == null || dates.size() < 2)
            throw new IllegalArgumentException("dates must contain more than 1 Date elements.");
        
        List<Integer> counts = new ArrayList<>();
        if (contentIds.isEmpty())
        {
            return Collections.nCopies(dates.size()-1, new Integer(0));
        }

        for (int i=1; i<dates.size(); i++)
        {
            Date beginDate = dates.get(i-1);
            Date endDate = dates.get(i);
            int count = sysSrv.findNumberContentActivities(contentIds, beginDate, endDate, stateName, transitionName);
            counts.add(count);
        }
        return counts;
    }

    /*
     * (non-Javadoc)
     * @see com.percussion.activity.service.IPSActivityService#findNumberContentActivities(java.util.List, java.util.List, java.lang.String, java.lang.String)
     */
    public List<String> findPageIdsContentActivities(Collection<Integer> contentIds, Date beginDate, Date endDate, String stateName,
            String transitionName)
    {   
        if (beginDate == null || endDate == null)
            throw new IllegalArgumentException("date must not be empty");
        
        List<Long> pageIds = new ArrayList<>();
        List<String> pageStringIds = new ArrayList<>();
        
        if (contentIds.isEmpty())
        {
            return pageStringIds;
        }
            
        pageIds = sysSrv.findPageIdsContentActivities(contentIds, beginDate, endDate, stateName, transitionName);

        for(Long pageId:pageIds)
        {
            IPSGuid guid = guidMgr.makeGuid(pageId, PSTypeEnum.LEGACY_CONTENT);
            pageStringIds.add(idMapper.getString(guid));
        }
        
        return pageStringIds;
    }
    
    /*
     * (non-Javadoc)
     * @see com.percussion.activity.service.IPSActivityService#findPublishedItems(java.util.List, java.util.List)
     */
    public List<Integer> findPublishedItems(Collection<Integer> contentIds, List<Date> dates)
    {
        if (dates == null || dates.size() < 2)
            throw new IllegalArgumentException("dates must contain more than 1 Date elements.");
        
        List<Integer> counts = new ArrayList<>();
        if (contentIds.isEmpty())
        {
            return Collections.nCopies(dates.size()-1, new Integer(0));
        }
        
        for (int i=1; i<dates.size(); i++)
        {
            Date beginDate = dates.get(i-1);
            Date endDate = dates.get(i);
            int count = sysSrv.findPublishedItems(contentIds, beginDate, endDate, WF_STATE_LIVE, WF_STATE_ARCHIVE);
            counts.add(count);
        }
        return counts;
    }
    
    /*
     * (non-Javadoc)
     * @see com.percussion.activity.service.IPSActivityService#findPublishedItems(java.util.Collection)
     */
    public Collection<Long> findPublishedItems(Collection<Integer> contentIds)
    {
        Collection<Long> ids = new ArrayList<>();
        
        if (!contentIds.isEmpty())
        {
            ids = sysSrv.findPublishedItems(contentIds, WF_STATE_LIVE, WF_STATE_ARCHIVE);
        }
        
        return ids;
    }

    /**
     * Returns the number of pages that are in pending state under a given path.
     * @param path must  not be <code>null</code>.
     * @return the number of pages, may be 0.
     */
    private long getPendingPageCount(String path)
    {
        IPSWorkflowService workflowService = PSWorkflowServiceLocator.getWorkflowService();
    	return getItemCount(path, Collections.singletonList(PAGE_CONTENT_TYPE),
    	        workflowService.getDefaultWorkflowName(), WF_STATE_PENDING);
    }
    
    /**
     * Finds the number of items under the given path that are in given workflow and state.
     * @param path the path of the specified item, never blank.
     * @param contentTypes must not be <code>null</code> or empty.
     * @param workflowName never blank.
     * @param stateName never blank
     * @return Number of items, may be zero.
     * @throws PSPathNotFoundServiceException, if the supplied path is not found
     * @throws PSPathServiceException If the item properties or workflow could not be found, or other system failure.
     */
    private long getItemCount(String path, List<String> contentTypes, String workflowName, String stateName) 
    throws PSPathNotFoundServiceException, PSPathServiceException
    {
        notEmpty(path);
        notEmpty(contentTypes);
        notEmpty(workflowName);
        notEmpty(stateName);
    	long itemCount = 0;
        // find the workflow, state id's
        int workflowId = -1;
        int stateId = -1;
        try
        {
            workflowId = itemWfSrvc.getWorkflowId(workflowName);
            stateId = itemWfSrvc.getStateId(workflowName, stateName);
        }
        catch (PSItemWorkflowServiceException e)
        {
            throw new PSPathServiceException(e);
        }
        String jcrCtypes = "rx:" + contentTypes.get(0);
        for (int i=1; i<contentTypes.size();i++) 
        {
        	jcrCtypes += ", rx:" + contentTypes.get(i);
        }
        // build the jcr query
        String jcrQuery = "select rx:sys_title from " + jcrCtypes
            + " where jcr:path like '" + path + "/%' and rx:sys_workflowid = " + workflowId;
        
        if (stateId != -1)
        {
           jcrQuery += " and rx:sys_contentstateid = " + stateId;
        }
        
        // get the properties for all items which satisfy the jcr query
        try
        {
            Query query = contentMgr.createQuery(jcrQuery, Query.SQL);
            QueryResult queryResult = contentMgr.executeQuery(query, -1, new HashMap<>(), null);
            itemCount = queryResult.getRows().getSize();
        }
        catch(Exception e)
        {
        	
        }
    	return itemCount;
    }
    
    public List<PSActivityNode> createActivityNodesByPaths(String path, boolean includeSite)
    {
        notEmpty(path);
        
        List<IPSSite> cm1Sites = new ArrayList<>();
        List<IPSSite> sites = siteMgr.findAllSites();
        for (IPSSite site : sites)
        {
            if (contentWs.getIdByPath(PSPathUtils.getFolderPath(site.getFolderRoot()) + "/.system") != null)
            {
                cm1Sites.add(site);
            }
        }
        
        if (!(PSPathUtils.SITES_FINDER_ROOT + "/").equals(path))
        {
            for (IPSSite site : cm1Sites)
            {
                String folderRoot = site.getFolderRoot();
                if (path.equals(folderRoot) || ("/" + path).equals(folderRoot))
                {
                    return createActivityNodesBySite(site, includeSite);
                }
                else if ((path+"/").startsWith(folderRoot+"/") || ("/" + path+"/").startsWith(folderRoot+"/"))
                {
                    return createActivityChildNodes(path, site.getName());
                }
            }
            throw new RuntimeException("Cannot find a site for path: " + path);
        }
     
        // get all sites
        List<PSActivityNode> result = new ArrayList<>();
        for (IPSSite site : cm1Sites)
        {
            String siteName = site.getName();
            PSActivityNode node = new PSActivityNode(siteName, siteName, site.getFolderRoot(), PAGE_CONTENT_TYPE);
            result.add(node);
        }
        return result;
    }

    private List<PSActivityNode> createActivityNodesBySite(IPSSite site, boolean includeSite)
    {
        List<PSActivityNode> result = new ArrayList<>();
        
        String siteName = site.getName();
        if (includeSite)
        {
            PSActivityNode node = new PSActivityNode(siteName, siteName, site.getFolderRoot(), PAGE_CONTENT_TYPE);
            result.add(node);
        }
        
        result.addAll(createActivityChildNodes(site.getFolderRoot(), siteName));
       
        return result;
    }
    
    private List<PSActivityNode> createActivityChildNodes(String path, String siteName)
    {
        List<PSActivityNode> result = new ArrayList<>();
        
        String folderPath = (!path.startsWith("//") ? '/' + path : path);
        List<PSItemSummary> sums = contentWs.findFolderChildren(folderPath, false);
        for (PSItemSummary sum : sums)
        {
            if (sum.getObjectType().equals(PSItemSummary.ObjectTypeEnum.FOLDER) && !sum.getName().equals(".system"))
            {
                String[] paths = contentWs.findItemPaths(sum.getGUID());
                PSActivityNode node = new PSActivityNode(siteName, sum.getName(), paths[0], PAGE_CONTENT_TYPE);
                result.add(node);
            }
        }
        return result;
    }
    
    /**
     * Gets all content types of the assets
     * 
     * @return content type names, never <code>null</code>.
     */
    public List<String> getResourceAssets()
    {
        List<String> result = new ArrayList<>();
        
        for (PSResourceDefinitionGroup resGrp : resDao.findAll())
        {
            for (PSAssetResource res : resGrp.getAssetResources())
            {
                if (res.isPrimary() && (!PAGE_CONTENT_TYPE.equals(res.getContentType())))
                {
                    result.add(res.getContentType());
                }
            }
        }
        return result;
    }
    
    /**
     * Gets all asset types which are not classified as resources.
     * 
     * @param resAssets collection of resource asset type names.  Never <code>null</code>, may be empty.
     * 
     * @return list of non resources asset type names;
     */
    public List<String> getNonResourceAssets(Collection<String> resAssets)
    {
        notNull(resAssets);
        
        List<String> result = new ArrayList<>();
        
        result.addAll(asList(itemDefMgr.getContentTypeNames(-1)));
        result.removeAll(resAssets);
        result.remove(PAGE_CONTENT_TYPE);
        
        return result;
    }

    public PSDateRange createDateRange(String start, String end, 
            String granularity)
    {
        DateFormat formatter; 
        formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = new Date();
        Date endDate = new Date();

        try
        {
            startDate = formatter.parse(start);
        }
        catch (ParseException e)
        {
            ms_log.error("Invalid start date: " + start, e);
        }
        try
        {
            endDate = formatter.parse(end);
        }
        catch (ParseException e)
        {
            ms_log.error("Invalid end date: " + end, e);
        }

        PSDateRange range = new PSDateRange(startDate,endDate,
                PSDateRange.Granularity.valueOf(granularity));

        return range;
    }
    
    @SuppressWarnings("unchecked")
    private Collection<Integer> getContentIdsByPath(String path, Collection<String> contentTypes)
    {
        List<Integer> result = new ArrayList<>();
        String query = createJCRQuery(path, contentTypes);
        try
        {
            Query q = contentMgr.createQuery(query, Query.SQL);
            QueryResult qresult = contentMgr.executeQuery(q, -1, null, null);
            RowIterator riter = qresult.getRows();

            while (riter.hasNext())
            {
               Row r = riter.nextRow();
               Value cid = r.getValue(IPSContentPropertyConstants.RX_SYS_CONTENTID);
               result.add((int) cid.getLong());
            }
            return result;
        }
        catch (Exception e)
        {
            ms_log.error("Caught error while query content IDs by path: '" + path + "'", e);
            return Collections.EMPTY_LIST;
        }
    }
    
    private String createJCRQuery(String path, Collection<String> contentTypes)
    {
        StringBuffer buffer = new StringBuffer();
        if (contentTypes == null || contentTypes.isEmpty())
        {
            buffer.append("nt:base");
        }
        else
        {
            for (String name : contentTypes)
            {
                if (buffer.length() == 0)
                {
                    buffer.append("rx:");
                    buffer.append(name);
                }
                else
                {
                    buffer.append(", rx:");
                    buffer.append(name);
                }
            }
        }
            
        return "select rx:sys_contentid from " + buffer.toString() + " where jcr:path like '" + path + "/%'";
    }
        
}
