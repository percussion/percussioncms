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
package com.percussion.analytics.service;

import com.percussion.analytics.data.IPSAnalyticsQueryResult;
import com.percussion.analytics.error.PSAnalyticsProviderException;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.utils.date.PSDateRange;

import java.util.List;

/**
 * Service that queries against an analytics service provider generally returning the results
 * in an <code>IPSAnalyticsQueryResultSet</code>.
 * @author erikserating
 *
 */
public interface IPSAnalyticsProviderQueryService
{
   /**
    * Retrieves the new and returning visits and page views for a site and date within
    * the specified date range. The results will be filtered by sitename.
    * @param sitename the unique sitename to filter the results by. Cannot be <code>null</code>.
    * @param range the date range, cannot be <code>null</code>. The start and end date values in
    * the date range are inclusive. Granularity is ignored.
    * @return the list of results, never <code>null</code> may be empty. 
    * <pre>
    * The result set contains the following fields
    * <table border="1">
    *    <tr><th>Field name</th><th>Data type</th><th>Description</th></tr>
    *    <tr><td>date</td><td>Date</td><td>The date the data was captured</td></tr>
    *    <tr><td>newvisits</td><td>Integer</td><td>The number of visits from first-time vistors for this date</td></tr>
    *    <tr><td>visits</td><td>Integer</td><td>The total number of vistors for this date</td></tr>
    *    <tr><td>uniquepageviews</td><td>Integer</td><td>The number of unique page views for the page for this date</td></tr>
    *    <tr><td>pageviews</td><td>Integer</td><td>The total number of page views for the page for this date</td></tr>
    * </table>
    * </pre>
    * The data is sorted by Ascending site and then by Ascending date.
    * @throws PSAnalyticsProviderException if any connection or data processing error occurs.
    */
   public List<IPSAnalyticsQueryResult> getVisitsViewsBySite(
      String sitename, PSDateRange range) throws PSAnalyticsProviderException, IPSGenericDao.LoadException, PSValidationException;
   
   /**
    * Retrieves the page views and unique page views for each page path and date within
    * the specified date range. The results will be filtered by pathPrefix and sitename.
    * @param sitename the unique sitename to filter the results by. Cannot be <code>null</code>.
    * @param pathPrefix the path prefix used to filter the results. May be <code>null</code> in
    * which case no filtering will be done by path prefix.
    * @param range the date range, cannot be <code>null</code>. The start and end date values in
    * the date range are inclusive. Granularity is ignored.
    * @return the list of results, never <code>null</code> may be empty. 
    * <pre>
    * The result set contains the following fields
    * <table border="1">
    *    <tr><th>Field name</th><th>Data type</th><th>Description</th></tr>
    *    <tr><td>pagepath</td><td>String</td><td>The full page path</td></tr>
    *    <tr><td>date</td><td>Date</td><td>The date the data was captured</td></tr>
    *    <tr><td>uniquepageviews</td><td>Integer</td><td>The number of unique page views for the page for this date</td></tr>
    *    <tr><td>pageviews</td><td>Integer</td><td>The total number of page views for the page for this date</td></tr>
    * </table>
    * </pre>
    * The data is sorted by Ascending site, Ascending pagePath and then by Ascending date.
    * @throws PSAnalyticsProviderException if any connection or data processing error occurs.
    */
   public List<IPSAnalyticsQueryResult> getPageViewsByPathPrefix(
      String sitename, String pathPrefix, PSDateRange range) throws PSAnalyticsProviderException, IPSGenericDao.LoadException, PSValidationException;
   
   // Field name constants
   public static final String FIELD_DATE = "date";
   public static final String FIELD_NEW_VISITS = "newvisits";
   public static final String FIELD_VISITS = "visits";
   public static final String FIELD_UNIQUE_PAGEVIEWS = "uniquepageviews";
   public static final String FIELD_PAGEVIEWS = "pageviews";
   public static final String FIELD_PAGE_PATH = "pagepath";
}
