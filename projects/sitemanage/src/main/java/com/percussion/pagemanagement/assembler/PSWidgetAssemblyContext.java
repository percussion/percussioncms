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
package com.percussion.pagemanagement.assembler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.pagemanagement.data.PSAbstractRegion;
import com.percussion.utils.types.PSPair;

/**
 * The model for rendering a widget.
 * @author adamgent
 *
 */
public class PSWidgetAssemblyContext extends PSAbstractAssemblyContext
{
    private PSWidgetInstance widget;

    private PSAbstractRegion region;

    private Map<String, Object> properties;

    private List<PSRenderAsset> widgetContents;
    
    public PSWidgetInstance getWidget()
    {
        return widget;
    }

    public void setWidget(PSWidgetInstance widgetInstance)
    {
        this.widget = widgetInstance;
    }

    public PSAbstractRegion getRegion()
    {
        return region;
    }

    public void setRegion(PSAbstractRegion region)
    {
        this.region = region;
    }

    public Map<String, Object> getProperties()
    {
        return properties;
    }

    public void setProperties(Map<String, Object> properties)
    {
        this.properties = properties;
    }

    /**
     * Sets the list of items that can be used while assembling the widgets.
     * 
     * @param widgetContents the new list of items, may be <code>null</code> or empty.
     */
    public void setWidgetContents(List<PSRenderAsset> widgetContents)
    {
        this.widgetContents = widgetContents;
        
        if (log.isDebugEnabled())
        {
            if (widgetContents == null || widgetContents.isEmpty())
                log.debug("set widget contents to NULL or EMPTY.");
            else
               log.debug("set widget contents to " + widgetContents.size() + " items.");
        }
        if (this.widgetContents != null) 
        {
            List<PSPair<String,String>> ownerAssetIds = new ArrayList<>();
            for(PSRenderAsset ai : widgetContents) 
            {
                PSPair<String,String> pair = new PSPair<>(ai.getOwnerId().toString(), ai.getId());
                ownerAssetIds.add(pair);
            }
            getWidget().setOwnerAssetIds(ownerAssetIds);
        }
    }

    /**
     * Gets a list of items that can be used while assembling the widget.
     * 
     * @return a list of items. It cannot be <code>null</code>, but may be
     * empty.
     */
    @SuppressWarnings("unchecked")
    public List<PSRenderAsset> getWidgetContents()
    {
        return widgetContents == null ? Collections.EMPTY_LIST : widgetContents;
    }
    
    /**
     * Sets the 1st element of the widget content list, which is returned
     * from {@link #getWidgetContents()}.
     * 
     * @param item the 1st element of the widget content list. It may be
     * <code>null</code>, in this case do nothing. 
     */
    public void setWidgetContent(PSRenderAsset item)
    {
        if (item == null)
        {
            log.debug("Attempt set null widget content, do nothing.");
        }
        else
        {
            if (widgetContents == null)
                widgetContents = new ArrayList<>();
            widgetContents.add(0, item);
        }
        if (log.isDebugEnabled())
        {
            if (widgetContents == null || widgetContents.isEmpty())
                log.debug("set widget contents to NULL or EMPTY.");
            else
                log.debug("set widget contents to " + widgetContents.size() + " items.");
        }
    }
    
    /**
     * A convenience method, it simply return the 1st element of the
     * widget content list, {@link #getWidgetContents()}.
     *  
     * @return the 1st element of the widget content list. It may be
     * <code>null</code> if the widget content list is <code>null</code> or
     * empty.
     */
    public PSRenderAsset getWidgetContent()
    {
       if (widgetContents == null || widgetContents.isEmpty())
          return null;
       
       return widgetContents.get(0);
    }
    
    @Override
    public PSWidgetAssemblyContext clone()
    {
        return (PSWidgetAssemblyContext) super.clone();
    }
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory.getLog(PSWidgetAssemblyContext.class);

}