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
package com.percussion.pagemanagement.assembler;

import com.percussion.pagemanagement.data.PSAbstractRegion;
import com.percussion.utils.types.PSPair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
            if (widgetContents == null || widgetContents.isEmpty()) {
                log.debug("set widget contents to NULL or EMPTY.");
            }
            else {
                log.debug("set widget contents to " + widgetContents.size() + " items.");
            }
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
            if (widgetContents == null) {
                widgetContents = new ArrayList<>();
            }
            widgetContents.add(0, item);
        }
        if (log.isDebugEnabled())
        {
            if (widgetContents == null || widgetContents.isEmpty()) {
                log.debug("set widget contents to NULL or EMPTY.");
            }
            else {
                log.debug("set widget contents to " + widgetContents.size() + " items.");
            }
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
       if (widgetContents == null || widgetContents.isEmpty()) {
           return null;
       }
       
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

    private static final Logger log = LogManager.getLogger(PSWidgetAssemblyContext.class);

}
