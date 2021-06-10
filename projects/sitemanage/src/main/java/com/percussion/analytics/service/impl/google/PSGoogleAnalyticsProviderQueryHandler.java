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
package com.percussion.analytics.service.impl.google;

import com.google.api.services.analyticsreporting.v4.AnalyticsReporting;
import com.google.api.services.analyticsreporting.v4.model.ColumnHeader;
import com.google.api.services.analyticsreporting.v4.model.DateRangeValues;
import com.google.api.services.analyticsreporting.v4.model.Dimension;
import com.google.api.services.analyticsreporting.v4.model.GetReportsRequest;
import com.google.api.services.analyticsreporting.v4.model.GetReportsResponse;
import com.google.api.services.analyticsreporting.v4.model.Metric;
import com.google.api.services.analyticsreporting.v4.model.MetricHeaderEntry;
import com.google.api.services.analyticsreporting.v4.model.OrderBy;
import com.google.api.services.analyticsreporting.v4.model.Report;
import com.google.api.services.analyticsreporting.v4.model.ReportRequest;
import com.google.api.services.analyticsreporting.v4.model.ReportRow;
import com.percussion.analytics.data.IPSAnalyticsQueryResult;
import com.percussion.analytics.data.PSAnalyticsProviderConfig;
import com.percussion.analytics.data.impl.PSAnalyticsQueryResult;
import com.percussion.analytics.error.PSAnalyticsProviderException;
import com.percussion.analytics.error.PSAnalyticsProviderException.CAUSETYPE;
import com.percussion.analytics.service.IPSAnalyticsProviderService;
import com.percussion.analytics.service.impl.IPSAnalyticsProviderQueryHandler;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.share.validation.PSValidationErrorsBuilder;
import com.percussion.utils.date.PSDateRange;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

/**
 * Handler that does the actual query building and execution to the Google
 * Analytics service.
 * 
 * @author erikserating
 * 
 */
public class PSGoogleAnalyticsProviderQueryHandler implements IPSAnalyticsProviderQueryHandler
{
    public PSGoogleAnalyticsProviderQueryHandler(IPSAnalyticsProviderService providerService)
    {
        this.providerService = providerService;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.percussion.analytics.service.IPSAnalyticsProviderQueryService#
     * getPageViewsByPathPrefix( java.lang.String, java.lang.String,
     * com.percussion.utils.date.PSDateRange)
     */
    public List<IPSAnalyticsQueryResult> getPageViewsByPathPrefix(String sitename, String pathPrefix, PSDateRange range) throws PSAnalyticsProviderException, IPSGenericDao.LoadException, PSValidationException {
        notEmpty(sitename);
        notNull(range);

        logPageViewsParameters(sitename, pathPrefix, range);

        range = PSGoogleAnalyticsProviderHelper.getInstance()
                .createValidPSDateRange(range);

        ReportRequest requestQuery = createQueryForPageViewsByPathPrefix(sitename, pathPrefix, range);

        Report entries = executeQuery(sitename, requestQuery);

        return getResultsForPageViewsByPathPrefix(sitename, entries);
    }

    private void logPageViewsParameters(String sitename, String pathPrefix, PSDateRange range)
    {
        log.debug("getPageViewsByPathPrefix: sitename = '{}', pathPrefix = '{}'" , sitename, pathPrefix);
        log.debug("Date begin: {}" , range.getStart());
        log.debug("Date end: {}" , range.getEnd());
        log.debug("Date getGranularity: {}" , range.getGranularity());
    }

    private void logResultsForPageViewsByPathPrefix(List<IPSAnalyticsQueryResult> results)
    {
        int i = 0;
        for (IPSAnalyticsQueryResult r : results)
        {
            i++;
            log.debug("[{}] ({}) {}",i,  FIELD_PAGE_PATH , r.getString(FIELD_PAGE_PATH));
            log.debug("[{}] ({}) {}", i,FIELD_PAGEVIEWS ,r.getInt(FIELD_PAGEVIEWS));
            log.debug("[{}] ({}) {}",i,FIELD_UNIQUE_PAGEVIEWS ,r.getInt(FIELD_UNIQUE_PAGEVIEWS));
            log.debug("[{}] ({}) {}",i, FIELD_DATE , r.getDate(FIELD_DATE).toString());
        }

        log.debug("PageViewsByPathPrefix result size: {}" , results.size());
    }

    private List<IPSAnalyticsQueryResult> getResultsForPageViewsByPathPrefix(String sitename, Report report) throws PSAnalyticsProviderException {
        List<IPSAnalyticsQueryResult> results = new ArrayList<>();
        ColumnHeader header = report.getColumnHeader();
        List<String> dimensionHeaders = header.getDimensions();
        List<MetricHeaderEntry> metricHeaders = header.getMetricHeader().getMetricHeaderEntries();
        List<ReportRow> rows = report.getData().getRows();

        if (rows != null) {
            for (ReportRow row: rows) {
                List<String> dimensions = row.getDimensions();
                List<DateRangeValues> metrics = row.getMetrics();
                PSAnalyticsQueryResult result = new PSAnalyticsQueryResult();
                boolean validSite=true; // Skip if this is not the right site

                for (int i = 0; i < dimensionHeaders.size() && i < dimensions.size(); i++) {
                    log.debug("{}:{}",dimensionHeaders.get(i) ,dimensions.get(i));

                    if(dimensionHeaders.get(i).equalsIgnoreCase("ga:date")){
                        result.put(FIELD_DATE, PSGoogleAnalyticsProviderHelper.getInstance().
                                parseDate(String.valueOf(dimensions.get(i))));
                    }
                    if(dimensionHeaders.get(i).equalsIgnoreCase("ga:pagePath")){
                        result.put(FIELD_PAGE_PATH, dimensions.get(i) !=null ? dimensions.get(i) : "");
                    }
                }
                if (!validSite) {
                    log.debug("Skipping for unwanted sites ");
                    continue; // Skip if this is not the right site
                }


                for (DateRangeValues values : metrics) {

                    for (int k = 0; k < values.getValues().size() && k < metricHeaders.size(); k++) {
                        log.debug("{}: {}",metricHeaders.get(k).getName(), values.getValues().get(k));
                        if (metricHeaders.get(k).getName().equalsIgnoreCase("ga:pageviews")) {
                            result.put(FIELD_PAGEVIEWS, Integer.parseInt(String.valueOf(values.getValues().get(k))));
                        } else if (metricHeaders.get(k).getName().equalsIgnoreCase("ga:uniquePageviews")) {
                            result.put(FIELD_UNIQUE_PAGEVIEWS, Integer.parseInt(String.valueOf(values.getValues().get(k))));
                        } else if (metricHeaders.get(k).getName().equalsIgnoreCase("ga:date")) {
                            result.put(FIELD_DATE, PSGoogleAnalyticsProviderHelper.getInstance().
                                    parseDate(String.valueOf(values.getValues().get(k))));
                        } else if (metricHeaders.get(k).getName().equalsIgnoreCase("ga:pagePath")) {
                            result.put(FIELD_PAGE_PATH, values.getValues().get(k) != null ? values.getValues().get(k) : "");
                        }

                    }
                }// metrics for loop
                results.add(result);
            }// for loop
        }



        logResultsForPageViewsByPathPrefix(results);

        return results;
    }

    /*
     * (non-Javadoc)
     * 
     * @seecom.percussion.analytics.service.IPSAnalyticsProviderQueryService#
     * getVisitsViewsBySite( java.lang.String,
     * com.percussion.utils.date.PSDateRange)
     */
    public List<IPSAnalyticsQueryResult> getVisitsViewsBySite(String sitename, PSDateRange range) throws PSAnalyticsProviderException, IPSGenericDao.LoadException, PSValidationException {
        notEmpty(sitename);
        notNull(range);

        logPageViewsParameters(sitename, null, range);
        ReportRequest requestQuery = createQueryForVisitsViews(range);
        Report entries = executeQuery(sitename, requestQuery);

        return getResultsForVisitsViewsySite(sitename, entries);
    }

    private List<IPSAnalyticsQueryResult> getResultsForVisitsViewsySite(String sitename, Report report) throws PSAnalyticsProviderException {
        List<IPSAnalyticsQueryResult> results = new ArrayList<>();
        ColumnHeader header = report.getColumnHeader();
        List<String> dimensionHeaders = header.getDimensions();
        List<MetricHeaderEntry> metricHeaders = header.getMetricHeader().getMetricHeaderEntries();
        List<ReportRow> rows = report.getData().getRows();

        if (rows != null) {
            for (ReportRow row: rows) {
                List<String> dimensions = row.getDimensions();
                List<DateRangeValues> metrics = row.getMetrics();
                PSAnalyticsQueryResult result = new PSAnalyticsQueryResult();
                boolean validSite=true; // Skip if this is not the right site


                for (int i = 0; i < dimensionHeaders.size() && i < dimensions.size(); i++) {
                    log.debug("{}:{}",dimensionHeaders.get(i) , dimensions.get(i));

                    if(dimensionHeaders.get(i).equalsIgnoreCase("ga:date")){
                        result.put(FIELD_DATE, PSGoogleAnalyticsProviderHelper.getInstance().
                                parseDate(String.valueOf(dimensions.get(i))));
                    }
                    if(dimensionHeaders.get(i).equalsIgnoreCase("ga:pagePath")){
                        result.put(FIELD_PAGE_PATH, dimensions.get(i) !=null ? dimensions.get(i) : "");
                    }
                }

                if (!validSite) {
                    log.debug("Skipping for unwanted sites");
                    continue; // Skip if this is not the right site
                }

                for (DateRangeValues values : metrics) {

                    for (int k = 0; k < values.getValues().size() && k < metricHeaders.size(); k++) {
                        log.debug("{}:{}",metricHeaders.get(k).getName() , values.getValues().get(k));
                        if (metricHeaders.get(k).getName().equalsIgnoreCase("ga:newVisits")) {
                            result.put(FIELD_NEW_VISITS, values.getValues().get(k));
                        } else if (metricHeaders.get(k).getName().equalsIgnoreCase("ga:visits")) {
                            result.put(FIELD_VISITS, values.getValues().get(k));
                        } else if (metricHeaders.get(k).getName().equalsIgnoreCase("ga:pageviews")) {
                            result.put(FIELD_PAGEVIEWS, Integer.parseInt(String.valueOf(values.getValues().get(k))));
                        } else if (metricHeaders.get(k).getName().equalsIgnoreCase("ga:uniquePageviews")) {
                            result.put(FIELD_UNIQUE_PAGEVIEWS, Integer.parseInt(String.valueOf(values.getValues().get(k))));
                        } else if (metricHeaders.get(k).getName().equalsIgnoreCase("ga:date")) {
                            result.put(FIELD_DATE, PSGoogleAnalyticsProviderHelper.getInstance()
                                    .parseDate(String.valueOf(values.getValues().get(k))));
                        } else if (metricHeaders.get(k).getName().equalsIgnoreCase("ga:pagePath")) {
                            result.put(FIELD_PAGE_PATH, values.getValues().get(k) != null ? values.getValues().get(k) : "");
                        }

                    }
                }// metrics for loop
                results.add(result);
            }// for loop
        }
        logVisitsViewsBySiteResults(results);
        return results;
    }

    private void logVisitsViewsBySiteResults(List<IPSAnalyticsQueryResult> results)
    {
        int i = 0;
        for (IPSAnalyticsQueryResult r : results)
        {
            i++;
            log.debug("[{}] ({}) {}" , i , FIELD_NEW_VISITS , r.getString(FIELD_NEW_VISITS));
            log.debug("[{}] ({}) {}" , i , FIELD_VISITS , r.getString(FIELD_VISITS));
            log.debug("[{}] ({}) {}" , i ,FIELD_PAGEVIEWS , r.getInt(FIELD_PAGEVIEWS));
            log.debug("[{}] ({}) {}" , i ,FIELD_UNIQUE_PAGEVIEWS ,r.getInt(FIELD_UNIQUE_PAGEVIEWS));
            log.debug("[{}] ({})" , i , r.getDate(FIELD_DATE));
        }
        log.debug("VisitsViewsBySite result size: {}" , results.size());
    }

    private ReportRequest createQueryForPageViewsByPathPrefix(String siteName, String pathPrefix, PSDateRange range)
            throws PSAnalyticsProviderException
    {
        ReportRequest request = PSGoogleAnalyticsProviderHelper.getInstance().
                createNewDataQuery(range);
        /*  -- removed these parameters
         new Dimension().setName("ga:customVarValue1")
          new OrderBy().setFieldName("ga:customVarValue1")
          */
        request.setDimensions(Arrays.asList(new Dimension().setName("ga:date")
                , new Dimension().setName("ga:pagePath")));
        request.setMetrics(Arrays.asList(
                new Metric().setExpression("ga:pageviews"),
                new Metric().setExpression("ga:uniquePageviews")));
        request.setOrderBys(Collections.singletonList(new OrderBy().setFieldName("ga:date")));

        String pagePathFilter = getPagePathFilter(siteName, pathPrefix);
        if (pagePathFilter != null)
        {
            //TODO: verify this filter work or not as compare to previous one
            request.setFiltersExpression(pagePathFilter);
        }
        return request;
    }

    /*private ReportRequest createQueryForPageViewsByPathPrefixTest(String siteName, String pathPrefix, PSDateRange range)
            throws PSAnalyticsProviderException
    {
        ReportRequest request = PSGoogleAnalyticsProviderHelper.createNewDataQuery(range);
        request.setDimensions(Arrays.asList(new Dimension().setName("ga:date"),  new Dimension().setName("ga:pagePath")));
        request.setMetrics(Arrays.asList(
                new Metric().setExpression("ga:pageviews"),
                new Metric().setExpression("ga:uniquePageviews")));
        request.setOrderBys(Arrays.asList( new OrderBy().setFieldName("ga:date")));
        //new OrderBy().setFieldName("ga:customVarValue1")
        String pagePathFilter = getPagePathFilter(siteName, pathPrefix);
        if (pagePathFilter != null)
        {
            // query.setFilters(pagePathFilter);
            log.debug("TODO verify this filter work or not as compare to previous one");
            request.setFiltersExpression(pagePathFilter);
        }
        return request;
    }*/

    private ReportRequest createQueryForVisitsViews(PSDateRange range) throws PSAnalyticsProviderException {
        range = PSGoogleAnalyticsProviderHelper.getInstance().
                createValidPSDateRange(range);
        ReportRequest request = PSGoogleAnalyticsProviderHelper.getInstance().
                createNewDataQuery(range);

        //Metric sessions = new Metric().setExpression("ga:pageviews").setAlias("sessions");
        //Dimension browser = new Dimension().setName("ga:browser");
        //OrderBy ordering = new OrderBy().setFieldName("ga:customVarValue1");
        //, new Dimension().setName("ga:customVarValue1")
        OrderBy ordering = new OrderBy().setFieldName("ga:date");

        request.setDimensions(Collections.singletonList(new Dimension().setName("ga:date")));

        // Set the cohort metrics
        request.setMetrics(Arrays.asList(
                new Metric().setExpression("ga:pageviews"),
                new Metric().setExpression("ga:uniquePageviews"),
                new Metric().setExpression("ga:visits"),
                new Metric().setExpression("ga:newVisits")));
        request.setOrderBys(Collections.singletonList(ordering));
        return request;
    }
    /*private ReportRequest createQueryForVisitsViewsTest(PSDateRange range) throws PSAnalyticsProviderException
    {
        range = createValidPSDateRange(range);
        ReportRequest request = createNewDataQuery(range);
        //Metric sessions = new Metric().setExpression("ga:pageviews").setAlias("sessions");
        //Dimension browser = new Dimension().setName("ga:browser");
        //OrderBy ordering = new OrderBy().setFieldName("ga:customVarValue1");
        OrderBy ordering2 = new OrderBy().setFieldName("ga:date");

        request.setDimensions(Arrays.asList(new Dimension().setName("ga:date"), new Dimension().setName("ga:browser")));
        // Set the cohort metrics
        request.setMetrics(Arrays.asList(
                new Metric().setExpression("ga:pageviews"),
                new Metric().setExpression("ga:uniquePageviews"),
                new Metric().setExpression("ga:visits"),
                new Metric().setExpression("ga:newVisits")));
        request.setOrderBys(Arrays.asList( ordering2));
        return request;
    }*/


    private String getPagePathFilter(String siteName, String pathPrefix)
    {
        if (StringUtils.isBlank(pathPrefix))
            return null;

        /*
         * Path prefix is the full URL so before setting the filter, we need to
         * remove "//Sites/{sitename} prefix". Example:
         * //Sites/schoolofdesign.edu/admissions/graduate will result in
         * /admissions/graduate. Sometimes URL will begin with "//", and others
         * with a single "/"
         */
        String pagePath = pathPrefix;

        if (!pathPrefix.startsWith("//"))
        {
            pagePath = "/" + pathPrefix;
        }

        pagePath = pagePath.replace("//Sites/" + siteName, "");

        String pagePathFilter = "ga:pagePath=~";
        // Google analytics doesn't support more than 128 characters for
        // ga:pagePath filters with regular expressions.
        if (pagePath.length() >= 126)
        {
            // If the path has more than 126 chars, trim it to use only the
            // last 126 (Two more chars are added later to the regex).
            pagePath = pagePath.substring(pagePath.length() - 126, pagePath.length());
        }
        else
        {
            // We know the path full path is being used so we specify ^ to
            // the regex to indicate that the matching paths should start
            // with the specified value.
            pagePathFilter += "^";
        }
        pagePathFilter += pagePath + "/*";

        return pagePathFilter;
    }

    /**
     * Execute the passed in query against Google Analytics service. This method
     * will set the values for Max Results and Start index. Multiple queries
     * will be made to Google until all results are collected.
     * 
     * @param sitename the site name is required so we can retrieve the proper
     *            profile to use. Cannot be <code>null</code> or empty.
     * @param requestQuery the Google DataQuery, the setMaxResults and setStartIndex
     *            values will be overwritten by this method. Cannot be
     *            <code>null</code>.
     * @return list of data entries, never <code>null</code>, may be empty.
     * @throws PSAnalyticsProviderException on any error that occurs while
     *             executing the query.
     */
    private Report executeQuery(String sitename, ReportRequest requestQuery) throws PSAnalyticsProviderException, IPSGenericDao.LoadException, PSValidationException {
        // Get user ID and password
        PSAnalyticsProviderConfig config = providerService.loadConfig(false);
        if (config == null)
        {
            throw new PSAnalyticsProviderException("Analytics has not been setup yet.", CAUSETYPE.ANALYTICS_NOT_CONFIG);
        }
        String uid = config.getUserid();
        String pwd = config.getPassword();

        String pid = getProfileId(sitename);
        return executeGoogleQuery(requestQuery, pid, uid, pwd);

    }

    private String getProfileId(String sitename) throws PSAnalyticsProviderException, IPSGenericDao.LoadException, PSValidationException {
        String profileId = providerService.getProfileId(sitename);
        if (profileId == null)
        {
            PSValidationErrorsBuilder builder = new PSValidationErrorsBuilder(this.getClass().getCanonicalName());
            String msg= "No profile set for site <" + sitename + ">.";
            builder.reject(CAUSETYPE.NO_PROFILE.toString(),msg ).throwIfInvalid();
        }

        return profileId;
    }

    private synchronized Report executeGoogleQuery(ReportRequest requestQuery , String pid, String uid, String pwd) throws PSAnalyticsProviderException {

        Report resultReport = null;
        try {
            AnalyticsReporting  analyticsReporting = PSGoogleAnalyticsProviderHelper.getInstance().initializeAnalyticsReporting(uid, pwd);
            requestQuery.setViewId(pid); // set pid mandatory
            ArrayList<ReportRequest> requests = new ArrayList<>();
            requests.add(requestQuery);
            GetReportsRequest getReport = new GetReportsRequest().setReportRequests(requests);
            // Call the batchGet method.
            GetReportsResponse response = analyticsReporting.reports().batchGet(getReport).execute();
            for (Report report: response.getReports()) {
                resultReport = report;

            }
        } catch (Exception e)
            {
                log.error(e);
                throw new PSAnalyticsProviderException(e.getMessage(), e);
            }

        return resultReport;
        }


    /**
     * Maximum data results to be returned from Google.
     */
    private static final int MAX_RESULTS = 10000;

    /**
     * Request sleep interval to prevent us from exceeding the Google request
     * quota of 10 queries per one second period.
     */
    private static final int REQUEST_SLEEP_INTERVAL = 115;

    /**
     * Analytics provider service, initialized in ctor, never <code>null</code>
     * after that.
     */
    private final IPSAnalyticsProviderService providerService;

    /**
     * Logger for this class
     */
    private static final Logger log = LogManager.getLogger(PSGoogleAnalyticsProviderQueryHandler.class);
}
