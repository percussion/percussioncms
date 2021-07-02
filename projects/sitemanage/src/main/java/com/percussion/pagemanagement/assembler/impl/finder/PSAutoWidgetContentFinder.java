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

package com.percussion.pagemanagement.assembler.impl.finder;

import com.percussion.pagemanagement.assembler.PSWidgetInstance;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.impl.finder.PSAutoFinderUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
/**
 * The auto widget content finder allows a widget to be filled with items 
 * returned by a query.
 * 
 * <table>
 * <tr>
 * <th>Parameter</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>query</td>
 * <td>The JSR-170 query to be performed</td>
 * </tr>
 * <tr>
 * <td>max_results</td>
 * <td>Optional parameter. It is the maximum number of the returned result
 * from the find method if specified, zero or negative indicates no limit. 
 * It defaults to zero if not specified.</td>
 * </tr>
 * </table>
 *
 * @author YuBingChen
 */
public class PSAutoWidgetContentFinder extends PSWidgetContentFinder
{
    @Override
    protected Set<ContentItem> getContentItems(IPSAssemblyItem sourceItem, 
            PSWidgetInstance widget, Map<String, Object> params)
    {
    	Set<ContentItem> items = new HashSet<>();
    	try
    	{

    		items = super.filter(utils.getContentItems(sourceItem, -1, params, sourceItem.getTemplate().getGUID()),
					sourceItem.getFilter(), params);
    	}
    	catch(Exception e)
    	{
    		ms_logger.error("Error getting content items for the given finder. " +
    				"\nPlease make sure the query parameters are valid.", e);
    	}
    	return items;
    }

    /**
     * The utility object, used to fetch the content items.
     */
    private PSAutoFinderUtils utils = new PSAutoFinderUtils();
    
    /**
     * Log for this class.
     */
    private static final Logger ms_logger = LogManager.getLogger(PSAutoWidgetContentFinder.class);

}
