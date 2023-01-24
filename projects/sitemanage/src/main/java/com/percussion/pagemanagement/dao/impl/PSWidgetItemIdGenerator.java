/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.pagemanagement.dao.impl;

import com.percussion.cms.IPSConstants;
import com.percussion.pagemanagement.dao.IPSWidgetItemIdGenerator;
import com.percussion.pagemanagement.data.PSRegionWidgetAssociations;
import com.percussion.pagemanagement.data.PSRegionWidgets;
import com.percussion.pagemanagement.data.PSWidgetItem;
import com.percussion.util.PSSiteManageBean;
import com.percussion.utils.security.PSSecurityUtility;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.Validate.notNull;

@PSSiteManageBean("widgetItemIdGenerator")
public class PSWidgetItemIdGenerator implements IPSWidgetItemIdGenerator
{

    /**
     * {@inheritDoc}
     */
    public Long generateId(PSRegionWidgetAssociations widgets, PSWidgetItem item)
    {
        Long id = PSSecurityUtility.getSecureRandom().nextLong();
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
                        log.error("Widget ID ({}) is not unique. The widget is: {}" ,id, wi);
                    
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

    private static final Logger log = LogManager.getLogger(IPSConstants.CONTENTREPOSITORY_LOG);

}
