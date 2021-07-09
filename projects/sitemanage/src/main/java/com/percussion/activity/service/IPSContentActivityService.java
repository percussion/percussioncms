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

package com.percussion.activity.service;

import com.percussion.activity.data.PSContentActivity;
import com.percussion.activity.data.PSContentActivityRequest;
import com.percussion.activity.data.PSEffectiveness;
import com.percussion.activity.data.PSEffectivenessRequest;

import java.util.List;

/**
 * This service provides various methods to get the activity of the content on a single site or all sites.
 * @author BJoginipally
 *
 */
public interface IPSContentActivityService extends IPSTrafficService
{
    /**
     * Gets the content activity for the given path, and specified duration.
     * @param request the content activity request.  The path must not be blank, supply "/" for all sites.  The
     * duration type must not be <code>blank</code> and must be of PSDurationTypeEnum.  The duration must be greater
     * than 0.
     * @return List of {@link PSContentActivity} objects. If the path is root, then the returned list consists of 
     * an activity object for each site and two for assets. The first asset activity is for all the resource type
     * assets and the second one is for non-resource type assets. If the supplied path is for a site then the returned 
     * list consists of an activity object for each folder under the site and two activity objects for assets similar 
     * to the above.
     */
    public List<PSContentActivity> getContentActivity(PSContentActivityRequest request);
    
    /**
     * Gets the overall effectiveness for the given request.
     * @param request the effectiveness request.  The path must not be blank, supply "/" for all sites.  The
     * duration type must not be <code>blank</code> and must be of PSDurationTypeEnum.  The duration must be greater
     * than 0.  The threshold must be greater than or equal to 0.
     * @return list of effectiveness objects, never <code>null</code>, may be empty.  The entries will be sorted in 
     * descending order by effectiveness value, ascending order by name.
     */
    public List<PSEffectiveness> getEffectiveness(PSEffectivenessRequest request);
    
    /**
     * The type of the duration like days, weeks etc...
     */
    public enum PSDurationTypeEnum
    {
        days,
        weeks,
        months,
        years
    }
    
    /**
     * The usage metric to use when calculating effectiveness.
     */
    public enum PSUsageEnum
    {
        pageviews,
        unique_pageviews
    }
    
    /**
     * The default timeout after which content activity queries should abort
     */
    static final int DEFAULT_TIMEOUT = 30;
    
}
