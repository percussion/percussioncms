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

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.isNumeric;
import static org.apache.commons.lang.Validate.notNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 
 * The model for rendering a page.
 * 
 * @author adamgent
 * @see #getRegions()
 *
 */
public class PSPageAssemblyContext extends PSAbstractAssemblyContext
{
    /**
     * never <code>null</code>.
     * @see #getRegions()
     */
    private Map<String, List<PSRegionResult>> regions = new ConcurrentHashMap<>();

    private RenderResult renderResult = null;
    
    /**
     * The rendering results of a region.
     * Once a region is assembled the results are stored in this map.
     * <p>
     * Since the region has to be assembled first this method is only applicable for
     * template developers. Widget code should not call this method as the results are undefined
     * during widget assembly.
     * <p>
     * The region macro:
     * <pre>
     * #region('regionId' '' '' '' '')
     * </pre>
     * Is an example of template code that calls this method to get the results of a reigon.
     * 
     * @return a map where the key is the region id and the value is render results of the region.
     */
    public Map<String, List<PSRegionResult>> getRegions()
    {
        return regions;
    }

    public void setRegions(Map<String, List<PSRegionResult>> regions)
    {
        this.regions = regions;
    }
    
    /**
     * Gets the rendered page result. The caller must call this after {@link #setRegions(Map)},
     * or {@link #getRegions()} is not <code>null</code>.
     * 
     * @return the rendered result, never <code>null</code>.
     */
    public RenderResult getRenderResult()
    {
        notNull(regions);
        
        if (renderResult == null) {
            renderResult = new RenderResult();
        }
        
        return renderResult;
    }
    
    /**
     * The rendered result for all regions and widgets.
     * This is primarily used for exporting the rendered result to database.
     * In order to make sure the data can be exported, each data type must be returned
     * in a list. And all list must in the same order.
     * 
     * @author YuBingChen
     */
    public class RenderResult
    {
        private List<RenderedWidget> renderedWidgets;

        public RenderResult()
        {
            renderedWidgets = new ArrayList<>();
            for (String regionId : regions.keySet())
            {
                for (PSRegionResult r : regions.get(regionId))
                {
                    RenderedWidget w = new RenderedWidget(regionId, r);
                    renderedWidgets.add(w);
                }
            }            
        }
        
        /**
         * Gets the widget IDs of the page.
         * @return widget IDs, never <code>null</code>, may be empty.
         */
        public List<Long> getWidgetIds()
        {
            List<Long> result = new ArrayList<>();
            for (RenderedWidget w : renderedWidgets) {
                result.add(w.widgetId);
            }
            
            return result;
        }

        /**
         * Gets the widget names of the page.
         * @return widget names, never <code>null</code>, may be empty.
         */
        public List<String> getWidgetNames()
        {
            List<String> result = new ArrayList<>();
            for (RenderedWidget w : renderedWidgets) {
                result.add(w.widgetName);
            }
            
            return result;
        }

        /**
         * Gets the content of the rendered widget.
         * @return the contents, never <code>null</code>, may be empty.
         */
        public List<String> getWidgetContents()
        {
            List<String> result = new ArrayList<>();
            for (RenderedWidget w : renderedWidgets) {
                result.add(w.content);
            }
            
            return result;
        }

        /**
         * Gets the types of the widgets on the page.
         * @return widget types, never <code>null</code>, may be empty.
         */
        public List<String> getWidgetTypes()
        {
            List<String> result = new ArrayList<>();
            for (RenderedWidget w : renderedWidgets) {
                result.add(w.widgetType);
            }
            
            return result;
        }

        /**
         * Gets the region IDs of the rendered widgets.
         * @return region IDs, never <code>null</code>, may be empty.
         */
        public List<String> getRegionIds()
        {
            List<String> result = new ArrayList<>();
            for (RenderedWidget w : renderedWidgets) {
                result.add(w.regionId);
            }
            
            return result;
        }
        

        /**
         * Represent a single widget rendering result.
         */
        public class RenderedWidget
        {
            String regionId;
            Long widgetId;
            String widgetName;
            String widgetType;
            String content;
            
            public RenderedWidget(String regionId, PSRegionResult result)
            {
                this.regionId = regionId;
                String wId = result.getWidget().getItem().getId();
                widgetId = (isNotBlank(wId) && isNumeric(wId)) ? Long.parseLong(wId) : -1L;
                widgetName = result.getWidget().getItem().getName();
                widgetType = result.getWidget().getDefinition().getId();
                content = result.getResult();
            }
        }
    }
   
}
