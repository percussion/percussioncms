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

import static com.percussion.pagemanagement.assembler.PSMergedRegion.PSMergedRegionOwner.PAGE;
import static com.percussion.pagemanagement.assembler.PSMergedRegion.PSMergedRegionOwner.TEMPLATE;
import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notNull;

import com.percussion.pagemanagement.assembler.PSMergedRegion.PSMergedRegionOwner;
import com.percussion.pagemanagement.data.PSAbstractRegion;
import com.percussion.pagemanagement.data.PSRegionBranches;
import com.percussion.pagemanagement.data.PSRegionTree;
import com.percussion.pagemanagement.data.PSWidgetDefinition;
import com.percussion.pagemanagement.data.PSWidgetItem;
import com.percussion.pagemanagement.service.IPSWidgetService;
import com.percussion.pagemanagement.service.impl.PSWidgetUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * A Merged Region tree that inherits from the template regions
 * unless the template region is empty and there is a matching page region.
 * 
 * @see #isLeaf(PSMergedRegion)
 * @see #chooseTemplateOrPageRegion(PSAbstractRegion, PSAbstractRegion, PSMergedRegion)
 * @author adamgent
 *
 */
public class PSMergedRegionTree extends PSAbstractMergedRegionTree {
    
    private IPSWidgetService widgetService;
    
    public PSMergedRegionTree(IPSWidgetService widgetService)
    {
        notNull(widgetService, "widgetService");
        this.widgetService = widgetService;
    }

    public PSMergedRegionTree(IPSWidgetService widgetService, PSRegionTree templateRegionTree, PSRegionBranches pageRegionBranches)
    {
        this(widgetService);
        merge(templateRegionTree, pageRegionBranches);
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * This implementation chooses the page if and only if the templates region is has no
     * widgets or subregions.
     */
    @Override
    protected PSMergedRegionOwner chooseTemplateOrPageRegion(PSAbstractRegion template, PSAbstractRegion page, PSMergedRegion parent) {
        notNull(template, "template");
        notNull(page, "page");
        boolean templateHasRegionChildren = hasRegionChildren(template);
        boolean templateHasWidgets = hasTemplateWidgets(template);
        PSMergedRegionOwner rvalue;
        
        if ( templateHasRegionChildren || templateHasWidgets )  {
            log.debug("Page is trying to override a non leaf template region: " + template.getRegionId());
            rvalue = TEMPLATE;
        }
        else {
            rvalue = PAGE;
        }
        return rvalue;
    }
    

    /**
     * {@inheritDoc}
     * <p>
     * <strong>This implementation will expand if the region has children regardless if its a page or 
     * template.</strong>
     */
    @Override
    protected boolean isLeaf(PSMergedRegion mr) {
        notNull(mr, "mr");
        isTrue((mr.getOwner() == PAGE || mr.getOwner() == TEMPLATE), 
                "Does not support " + mr.getOwner());
        Boolean rvalue;
        boolean hasRegionChildren = hasRegionChildren(mr.getOriginalRegion());
        boolean hasWidgets = mr.getWidgetInstances() != null && ! mr.getWidgetInstances().isEmpty();
        if (hasWidgets && hasRegionChildren) {
            log.warn("Has child regions but also has widgets " +
            		"ignoring child regions: " + mr);
            rvalue = true;
        }
        else if (hasRegionChildren) {
            rvalue = false;
        }
        else if (hasWidgets) {
            rvalue = true;
        }
        else {
            /*
             * Empty Region
             */
            if (log.isDebugEnabled())
                log.debug("Empty region for template: " + mr);
            rvalue = true;
        }

        return rvalue;
    }
    
    
    /**
     * {@inheritDoc}
     * <p>
     * Will prefer the templates widgets over the pages widgets if there is a conflict.
     */
    @Override
    protected List<PSWidgetItem> getMergedWidgetItemsForRegion(PSMergedRegion mr) {
        String regionId = mr.getRegionId();
        List<PSWidgetItem> items = null;
        if (mr.getOwner() == PAGE && pageWidgetItems.containsKey(regionId)) {
            items = pageWidgetItems.get(regionId);
        }
        if (templateWidgetItems.containsKey(regionId)) {
            items = templateWidgetItems.get(regionId);
        }
        return items;
    }
    
    
    @Override
    protected List<PSWidgetInstance> loadWidgets(List<PSWidgetItem> widgetItems) {
        List<PSWidgetInstance> wis = new ArrayList<>();
        for(PSWidgetItem wi : widgetItems) {
            wis.add(loadWidget(wi));
        }
        return wis;
    }
    
    protected PSWidgetInstance loadWidget(PSWidgetItem widgetItem)
    {
        PSWidgetInstance pwi = new PSWidgetInstance();
        PSWidgetDefinition widget = widgetService.load(widgetItem.getDefinitionId());
        setDefaultValuesFromDef(widgetItem, widget);
        pwi.setItem(widgetItem);
        pwi.setDefinition(widget);
        // TODO ASSET
        return pwi;
    }
    
    
    /**
     * Gets the default values from the given definition and set the default
     * values to the given widget item if the properties do not exist in in
     * the widget item.
     * 
     * @param item the widget item, assumed not <code>null</code>.
     * @param def the widget definition, assumed not <code>null</code>.
     */
    private void setDefaultValuesFromDef(PSWidgetItem item, PSWidgetDefinition def)
    {
        PSWidgetUtils.setDefaultValuesFromDefinition(item, def);
    }
}