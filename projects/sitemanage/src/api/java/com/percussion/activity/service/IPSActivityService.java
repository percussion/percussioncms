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

package com.percussion.activity.service;

import com.percussion.activity.data.PSActivityNode;
import com.percussion.activity.data.PSContentActivity;
import com.percussion.utils.date.PSDateRange;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * This service provides various utility methods to get the activity of the content on a single site or all sites.
 */
public interface IPSActivityService 
{
    /**
     * Creates a content activity representation for the given activity node starting from the specified date.
     * @param node the activity node, must not be <code>null</code>.
     * @param beginDate the starting date for the content activity, must not be <code>null</code>.
     * @param timeout The number of milliseconds after which the operation should abort
     * @return {@link PSContentActivity} object, never <code>null</code>. 
     * 
     * @throws PSActivityServiceException if there is a timeout.
     */
    public PSContentActivity createActivity(PSActivityNode node, Date beginDate, long timeout);
    
    /**
     * Gets a site and path pairs for the specified path. The returned list is all sites if the path is
     * <code>PSPathUtils#SITES_FINDER_ROOT</code>.
     * @param path the path in question, must not be blank.
     * @param includeSite <code>true</code> to include an activity node for the site(s), <code>false</code> otherwise.
     * @return the site / path pairs, never <code>null</code>, but may be empty.
     */
    public List<PSActivityNode> createActivityNodesByPaths(String path, boolean includeSite);

    /**
     * Creates a date range from the supplied values.
     * 
     * @param start A date in the format yyyy-MM-dd. Never <code>null</code> or
     * empty.
     * @param end A date in the format yyyy-MM-dd. Never <code>null</code> or
     * empty.
     * @param granularity The string representation of one of the values of
     * {@link PSDateRange.Granularity}. Never <code>null</code> or empty.
     * @return Never <code>null</code>.
     */
    public PSDateRange createDateRange(String start, String end, String granularity);
    
    /**
     * Finds all page items under a specified folder path.
     * 
     * @param path the specified folder path, not <code>null</code> or empty.
     * 
     * @return a list of content IDs of the page items under the specified folder.
     */
    public Collection<Integer> findPageIdsByPath(String path);

    /**
     * Finds all page items under a specified folder path.
     * 
     * @param path the specified folder path, not <code>null</code> or empty.
     * @param contentTypes a list of content type names, it may be <code>null</code> or empty,
     * which is equivalent to all content types.
     * 
     * @return a list of content IDs of the page items under the specified folder.
     */
    public Collection<Integer> findItemIdsByPath(String path, Collection<String> contentTypes);
    
    /**
     * Finds the number of newly published items (among the specified items) for the specified date ranges.
     *  
     * @param contentIds a list of specified item IDs, not <code>null</code>. 
     * @param dates a list of specified date range, it must contain more than one Date element.
     * 
     * @return a list of newly published items in the same order of the specified date ranges.
     */
    public List<Integer> findNewContentActivities(Collection<Integer> contentIds, List<Date> dates);
    
    /**
     * Finds the number of content activities (among the specified items) for
     * the specified date ranges.
     * 
     * @param contentIds a list of specified item IDs, not <code>null</code>.
     * @param dates a list of specified date range, it must contain more than
     * one Date element.
     * @param stateName the workflow state name the items transition to, not
     * <code>null</code> or empty.
     * @param transitionName the transition name that is used to transition the
     * items to the above state, not <code>null</code> or empty.
     * 
     * @return a list of content activities in the same order of the specified date ranges.
     */
    public List<Integer> findNumberContentActivities(Collection<Integer> contentIds, List<Date> dates, String stateName,
            String transitionName);

    /**
     * Finds collection of page ids that have had content activities (among the
     * specified items) for the specified date ranges.
     * 
     * @param contentIds a list of specified item IDs, not <code>null</code>.
     * @param beginDate The starting date, inclusive. Never <code>null</code>.
     * @param endDate The ending date, exclusive. Never <code>null</code>.
     * @param stateName the workflow state name the items transition to, not
     * <code>null</code> or empty.
     * @param transitionName the transition name that is used to transition the
     * items to the above state, not <code>null</code> or empty.
     * 
     * @return a list of content activities in the specified date ranges. If
     * beginData is not <= to endDate, no data will be returned.
     */
    public List<String> findPageIdsContentActivities(Collection<Integer> contentIds, Date beginDate, Date endDate,
            String stateName, String transitionName);

    /**
     * Finds the number of published items (among the specified items) for the specified date ranges.
     *  
     * @param contentIds a list of specified item IDs, not <code>null</code>. 
     * @param dates a list of specified date range, it must contain more than one Date element.
     * 
     * @return a list of published items in the same order of the specified date ranges.
     */
    public List<Integer> findPublishedItems(Collection<Integer> contentIds, List<Date> dates);
    
    /**
     * Finds the published items (among the specified items).
     *  
     * @param contentIds a list of specified item IDs, not <code>null</code>. 
     * 
     * @return a collection of published item IDs, never <code>null</code>, may be empty.
     */
    public Collection<Long> findPublishedItems(Collection<Integer> contentIds);
    
    public static class PSActivityServiceException extends RuntimeException
    {
        /**
         * @param string
         */
        public PSActivityServiceException(String string)
        {
            super(string);
        }
        
    }
}
