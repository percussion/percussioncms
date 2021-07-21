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
package com.percussion.pagemanagement.dao.impl;

import com.percussion.pagemanagement.dao.IPSWidgetItemIdGenerator;
import com.percussion.pagemanagement.data.PSRegionWidgetAssociations;
import com.percussion.pagemanagement.data.PSRegionWidgets;
import com.percussion.pagemanagement.data.PSWidgetItem;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.Validate.notNull;


public class PSWidgetItemIdGenerator implements IPSWidgetItemIdGenerator
{

    /**
     * {@inheritDoc}
     */
    public Long generateId(PSRegionWidgetAssociations widgets, PSWidgetItem item)
    {
        Long id = Long.parseLong(RandomStringUtils.randomNumeric(10));
        log.debug("Generated widget item id: {} for widget: {}",id, item);
        return id;
    }

    /**
     * {@inheritDoc}
     */
    public void generateIds(PSRegionWidgetAssociations widgets)
    {
        Set<String> ids = getWidgetIds(widgets);
        
        Set<PSRegionWidgets> regionWidgets = widgets.getRegionWidgetAssociations();
        for (PSRegionWidgets ws : regionWidgets)
        {
            for (PSWidgetItem wi : ws.getWidgetItems())
            {
                if (isBlank(wi.getId()))
                {
                    Long id = generateId(widgets, wi);
                    // make sure the generated ID is unique
                    //FB: GC_UNRELATED_TYPES NC - 1-16-16
                    while (ids.contains(id.toString()))
                        id = generateId(widgets, wi);
                    
                    notNull(id);
                    wi.setId(id.toString());
                    
                    ids.add(id.toString());
                }
            }
        }
    }

    /**
     * Gets all non-blank widget IDs for the given widgets.
     * This will also validate the uniqueness of the IDs, log error for non-unique IDs.
     * 
     * @param widgets the widgets in question, assumed not <code>null</code>.
     * 
     * @return the widget IDs, never <code>null</code>, may be empty.
     */
    private Set<String> getWidgetIds(PSRegionWidgetAssociations widgets)
    {
        Set<String> ids = new HashSet<>();
        Set<PSRegionWidgets> regionWidgets = widgets.getRegionWidgetAssociations();
        for (PSRegionWidgets ws : regionWidgets)
        {
            for (PSWidgetItem wi : ws.getWidgetItems())
            {
                if (isNotBlank(wi.getId()))
                {
                    String id = wi.getId();
                    if (ids.contains(id))
                        log.error("Widget ID ({}) is not unique. The widget is: {}" ,id, wi.toString());
                    
                    ids.add(id); 
                }
            }
        }
        return ids;
    }
    
    /**
     * {@inheritDoc}
     */
    public void deleteIds(PSRegionWidgetAssociations widgets) 
	{
		Set<PSRegionWidgets> regionWidgets = widgets.getRegionWidgetAssociations();
        for (PSRegionWidgets ws : regionWidgets)
        {
            for (PSWidgetItem wi : ws.getWidgetItems())
            {
                    wi.setId(null);
            }
        }		
	}
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */

    private static final Logger log = LogManager.getLogger(PSWidgetItemIdGenerator.class);

}
