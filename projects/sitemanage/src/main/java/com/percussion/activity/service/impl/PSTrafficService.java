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

import com.percussion.activity.data.PSContentTraffic;
import com.percussion.activity.data.PSContentTrafficRequest;
import com.percussion.activity.data.PSTrafficDetails;
import com.percussion.activity.data.PSTrafficDetailsRequest;
import com.percussion.activity.service.IPSActivityService;
import com.percussion.activity.service.IPSTrafficService;
import com.percussion.analytics.data.IPSAnalyticsQueryResult;
import com.percussion.analytics.error.IPSAnalyticsErrorMessageHandler;
import com.percussion.analytics.error.PSAnalyticsProviderException;
import com.percussion.analytics.service.IPSAnalyticsProviderQueryService;
import com.percussion.analytics.service.IPSAnalyticsProviderService;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.pathmanagement.service.IPSPathService;
import com.percussion.pathmanagement.service.impl.PSPathUtils;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.data.PSItemProperties;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.sitemanage.data.PSSiteSummary;
import com.percussion.sitemanage.service.IPSSiteDataService;
import com.percussion.utils.date.PSDateRange;
import org.apache.commons.lang.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static com.percussion.itemmanagement.service.impl.PSWorkflowHelper.WF_STATE_ARCHIVE;
import static com.percussion.itemmanagement.service.impl.PSWorkflowHelper.WF_STATE_LIVE;
import static com.percussion.itemmanagement.service.impl.PSWorkflowHelper.WF_TAKE_DOWN_TRANSITION;
import static com.percussion.itemmanagement.service.impl.PSWorkflowHelper.log;

/**
 * The traffic data service.  This service provides actual data.
 *  
 * @author luisteixeira
 */
public class PSTrafficService implements IPSTrafficService
{

    public PSTrafficService(IPSActivityService activityService, 
            IPSAnalyticsProviderQueryService analyticsService,
            IPSAnalyticsProviderService providerService,
            IPSSiteDataService siteDataService,
            IPSPathService pathService,
            IPSFolderHelper folderHelper, 
            IPSPageService pageService)
    {
        this.activityService = activityService;
        this.analyticsService = analyticsService;
        this.providerService = providerService;
        this.siteDataService = siteDataService;
        this.pathService = pathService;
        this.folderHelper = folderHelper;
        this.pageService = pageService;
    }
    
    public PSContentTraffic getContentTraffic(PSContentTrafficRequest request) throws PSTrafficServiceException {
        PSContentTraffic results = new PSContentTraffic();

        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        
        List<String> dates = new ArrayList<>();
        List<Integer> updateTotals = new ArrayList<>();
        List<Integer> newPages = new ArrayList<>();
        List<Integer> pageUpdates = new ArrayList<>();
        List<Integer> takeDowns = new ArrayList<>();
        List<Integer> livePages = new ArrayList<>();
        List<Integer> visits = new ArrayList<>();
        PSSiteSummary siteInfo = null;
        try
        {
            siteInfo = siteDataService.findByPath(request.getPath());
        }
        catch (Exception e)
        {
            throw new PSTrafficServiceException(notFoundError);
        }
        
        //Create PSDateRange
        PSDateRange range;
        try
        {
            range = createPSDateRange(request.getStartDate(), request.getEndDate(), request.getGranularity());
        }
        catch (ParseException e)
        {
            throw new PSTrafficServiceException(e.getMessage());
        }

        //Fill in dates
        for(Date date : range.getGranularityBreakdown())
        {
            dates.add(df.format(date));
        }
        
        //Fill in data    
        List<Date> dateList = range.getGranularityBreakdown();
        dateList.add(range.getEnd());
        List<String> dataReq = request.getTrafficRequested();
        Collection<Integer> pageIds = activityService.findPageIdsByPath(request.getPath());
        
        if(dataReq.contains(PSTrafficTypeEnum.LIVE_PAGES.toString()))
        {
          livePages.addAll(activityService.findPublishedItems(pageIds, dateList));
        }
        
        if(dataReq.contains(PSTrafficTypeEnum.NEW_PAGES.toString()))
        {
          newPages.addAll(activityService.findNewContentActivities(pageIds, dateList));
        }
        
        if(dataReq.contains(PSTrafficTypeEnum.UPDATED_PAGES.toString()))
        {
          List<Integer> activity = new ArrayList<>();
          activity.addAll(activityService.findNumberContentActivities(pageIds, 
                  dateList, WF_STATE_LIVE, null));
          for(int i = 0; i < activity.size(); i++)
          {
              pageUpdates.add(i, activity.get(i) - newPages.get(i));
          }
        }
        
        if(dataReq.contains(PSTrafficTypeEnum.TAKE_DOWNS.toString()))
        {
          takeDowns.addAll(activityService.findNumberContentActivities(pageIds, 
                  dateList, WF_STATE_ARCHIVE, WF_TAKE_DOWN_TRANSITION));
        }
            
        if(dataReq.contains(PSTrafficTypeEnum.VISITS.toString()))
        {
            try
            {
                visits.addAll(createAnalyticsActivity(range, dateList, 
                        siteInfo.getName(), request.getUsage()));
            }
            catch (PSAnalyticsProviderException e)
            {
               IPSAnalyticsErrorMessageHandler errorHandler = providerService.getErrorMessageHandler(); 
               throw new PSTrafficServiceException(errorHandler.getMessage(e), e);
            } catch (IPSGenericDao.LoadException e) {
                throw new PSTrafficServiceException(e.getMessage(),e);
            }
        }

        //Calculate Totals
        for (int i = 0; i < dates.size(); i++) {
            updateTotals.add(i, newPages.get(i) + pageUpdates.get(i) + 
                    takeDowns.get(i));
        }
        
        //Fill in results and remove added day used to create range.
        results.setStartDate(request.getStartDate());
        results.setEndDate(request.getEndDate());
        results.setDates(removeLast(dates));
        results.setPageUpdates(removeLast(pageUpdates));
        results.setNewPages(removeLast(newPages));
        results.setLivePages(removeLast(livePages));
        results.setTakeDowns(removeLast(takeDowns));
        results.setVisits(removeLast(visits));
        results.setUpdateTotals(removeLast(updateTotals));
        results.setSite(siteInfo.getName());
        results.setSiteId(siteInfo.getId());

        return results;
    }

    public List<PSTrafficDetails> getTrafficDetails(PSTrafficDetailsRequest request) throws PSTrafficServiceException, PSDataServiceException, IPSPathService.PSPathServiceException {
        //Create PSDateRange
        PSDateRange range;
        try
        {
            range = createPSDateRange(request.getStartDate(), request.getEndDate(),
                    PSDateRange.Granularity.DAY.toString());
        }
        catch (ParseException e)
        {
            throw new PSTrafficServiceException(e.getMessage());
        }
        
        PSDateRange previousRange = new PSDateRange(range.getStart(), 
                PSDateRange.Granularity.DAY, range.getDaysInRange());
        
        //Get Page Activity List
        Collection<Integer> pageIds = activityService.findPageIdsByPath(request.getPath());
        List<String> activityIds = activityService.findPageIdsContentActivities(pageIds, 
                range.getStart(), range.getEnd(), WF_STATE_LIVE, null);
        
        //Get Item Properties
        List<PSTrafficDetails> itemPropList = new ArrayList<>();
        for(String pageId:activityIds)
        {try {
            PSPathItem pathItem = folderHelper.findItemById(pageId);
            String path = pathItem.getFolderPaths().get(0) + "/" + pathItem.getName();
            String finderPath = PSPathUtils.getFinderPath(path);
            PSItemProperties itemProp = pathService.findItemProperties(finderPath);
            itemProp.setPath(finderPath);
            PSPage page = null;

            page = pageService.findPageByPath(path);

            // CM-126: pageService.findPageByPath(path) may return null
            if (page != null) {
                itemProp.setSummary(page.getSummary());
            }

            itemPropList.add(createTrafficDetail(itemProp));
        } catch (PSNotFoundException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
        }
        }
        
        //Get Analytics data
        PSSiteSummary siteInfo = siteDataService.findByPath(request.getPath());
        List<IPSAnalyticsQueryResult> currentAnalytics = 
            new ArrayList<>();
        List<IPSAnalyticsQueryResult> previousAnalytics = 
            new ArrayList<>();
        
        try
        {
            currentAnalytics = analyticsService.getPageViewsByPathPrefix(
                    siteInfo.getName(), null, range);
            previousAnalytics = analyticsService.getPageViewsByPathPrefix(
                    siteInfo.getName(), null, previousRange);
        }
        catch (PSAnalyticsProviderException e)
        {
            throw new PSTrafficServiceException(e);
        }
        
        //Fill in Analytics data
        for(int j = 0; j < itemPropList.size(); j++)
        {
            int currentViews = findPageAnalyticsCount(currentAnalytics, 
                    itemPropList.get(j), siteInfo.getName(), request.getUsage());
            int previousViews = findPageAnalyticsCount(previousAnalytics, 
                    itemPropList.get(j), siteInfo.getName(), request.getUsage());
            itemPropList.get(j).setVisits(currentViews);
            itemPropList.get(j).setVisitsDelta(currentViews - previousViews);
        }
        
        return itemPropList;
    }
    
    /**
     * Helper method to create PSdate range from sting dates and granularity.  The end date of the created range will be
     * the specified end date plus one. 
     * 
     * @param start date string in the following format "MM/dd/yyyy"
     * @param end date string in the following format "MM/dd/yyyy"
     * @param granularity
     * @return PSDateRange
     * @throws ParseException
     */
    private PSDateRange createPSDateRange(String start, String end, 
            String granularity) throws ParseException
    {
        DateFormat formatter; 
        formatter = new SimpleDateFormat("MM/dd/yyyy");
        
        Date startDate = formatter.parse(start);
        Date endDate = formatter.parse(addDay(end, formatter));

        return new PSDateRange(startDate,endDate,
                PSDateRange.Granularity.valueOf(granularity));
    }
    
    /**
     * Helper method to convert PSItemProperties to PSTrafficDetails
     * @param itemProp
     * @return PSTrafficDetails
     */
    private PSTrafficDetails createTrafficDetail(PSItemProperties itemProp)
    {
        PSTrafficDetails tDetail = new PSTrafficDetails();
        tDetail.setId(itemProp.getId());
        tDetail.setLastModifiedDate(itemProp.getLastModifiedDate());
        tDetail.setLastModifier(itemProp.getLastModifier());
        tDetail.setLastPublishedDate(itemProp.getLastPublishedDate());
        tDetail.setName(itemProp.getName());
        tDetail.setPath(itemProp.getPath());
        tDetail.setStatus(itemProp.getStatus());
        tDetail.setType(itemProp.getType());
        tDetail.setSummary(itemProp.getSummary());
        
        return tDetail;
    }

    
    /**
     * Helper method to loop through Analytics and find matches for a page.
     * 
     * @param visitResults - List of Analytics results
     * @param itemProp - item to match against Analytics list
     * @param siteName - Name of the site
     * @param usage - key used for Analytics
     * 
     * @return total of visits for page
     */
    private int findPageAnalyticsCount(List<IPSAnalyticsQueryResult> visitResults,  
            PSItemProperties itemProp, String siteName, String usage)
    {        
        int visitCount = 0;
        String itemPath = itemProp.getPath().toLowerCase().
            replaceFirst("/sites/" + siteName.toLowerCase(), "");
        //loop through Results
        for (IPSAnalyticsQueryResult visit:visitResults)
        {
            String analyticsPath = 
                visit.getString(IPSAnalyticsProviderQueryService.FIELD_PAGE_PATH);
            if(StringUtils.equalsIgnoreCase(analyticsPath, itemPath))
            {
                visitCount += visit.getInt(usage);
            }
        }
        return visitCount;
    }
    
    /**
     * Helper method to get analytics by list of dates.
     * 
     * @param dt - Date String
     * @param sdf - Date Formatter
     * @return date one day above dt param.
     * @throws ParseException 
     */
    private String addDay(String dt, DateFormat sdf) throws ParseException
    {
        Calendar c = Calendar.getInstance();
        c.setTime(sdf.parse(dt));
        c.add(Calendar.DATE, 1);  
        dt = sdf.format(c.getTime());
        
        return dt;
    }
    
    /**
     * Helper method to remove last index in List.
     * 
     * @param list
     * @return list with last index removed
     */
    private List removeLast(List list)
    {  
        list.remove(list.size() -1);
        return list;
    }
    
    /**
     * Helper method to get analytics by list of dates.
     * 
     * @param range - request date range
     * @param dates - array of date
     * @param siteName - name of site to use
     * @param usage - Analytics field key to use
     * @return list of totals for the date range
     * @throws PSAnalyticsProviderException
     */
    public List<Integer> createAnalyticsActivity(PSDateRange range, List<Date> dates, 
            String siteName, String usage) throws PSAnalyticsProviderException, IPSGenericDao.LoadException {
        List<Integer> counts = new ArrayList<>(dates.size() - 1);
        //Fill in array with 0
        for(int i=0;i < dates.size()-1;i++)
        {
            counts.add(0);
        }
        
        List<IPSAnalyticsQueryResult> visitResults = 
            analyticsService.getVisitsViewsBySite(siteName, range);
        
        //loop through Results
        for (IPSAnalyticsQueryResult visit:visitResults)
        {
            Date visitDate = visit.getDate(IPSAnalyticsProviderQueryService.FIELD_DATE);
            int visitCount = visit.getInt(usage);
            
            for(int i=0;i < dates.size() -1 ;i++ )
            {
                //Date match
                if (visitDate.equals(dates.get(i)) || 
                        (visitDate.after(dates.get(i)) && visitDate.before(dates.get(i+1))))
                {
                    int existingCount = counts.get(i);
                    counts.remove(i);
                    counts.add(i, existingCount + visitCount);
                    break;
                }
            }
        }
        return counts;
    }
    
    private IPSActivityService activityService;
    private IPSAnalyticsProviderQueryService analyticsService;
    private IPSAnalyticsProviderService providerService;
    private IPSSiteDataService siteDataService;
    private IPSPathService pathService;
    private IPSFolderHelper folderHelper;
    private String notFoundError = "Unable to retrieve analytics data. Please use the Google Setup gadget to select a profile for the desired site(s).";
    private IPSPageService pageService;
}
