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
package com.percussion.assetmanagement.service.impl;

import static com.percussion.pagemanagement.assembler.PSWidgetContentFinderUtils.getLocalSharedAssetRelationships;
import static com.percussion.pagemanagement.assembler.PSWidgetContentFinderUtils.getMatchRelationship;
import static com.percussion.pagemanagement.data.PSRegionTreeUtils.getEmptyWidgetRegions;
import static com.percussion.share.spring.PSSpringWebApplicationContextUtils.getWebApplicationContext;
import static org.apache.commons.lang.Validate.notNull;

import com.percussion.assetmanagement.data.PSOrphanedAssetSummary;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSRegion;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.data.PSWidgetItem;
import com.percussion.share.service.IPSIdMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Santiago M. Murchio
 * 
 */
public class PSPreviewPageUtils
{
    /**
     * Gets those template widgets that are not occupied with template assets.
     * 
     * @param template {@link PSTemplate} representing the template. Must not be
     *            <code>null</code>.
     * 
     * @return {@link Set}<{@link PSWidgetItem}> never <code>null</code>, but
     *         may be empty.
     */
    public static Set<PSWidgetItem> getEmptyTemplateWidgets(PSTemplate template)
    {
        notNull(template);

        Set<PSWidgetItem> emptyTemplateWidgets = new HashSet<PSWidgetItem>();

        // Get a list of asset/template relationships, templateAssets
        Collection<PSRelationship> templateAssets = getLocalSharedAssetRelationships(template.getId());

        // Filter the widgets that are not in the templateAssets collection
        Map<String, PSRelationship> mapSlotIdToRelationship = getSlotIdToRelationshipMap(templateAssets);
        for (PSWidgetItem templateWidget : template.getWidgets())
        {
            if (!mapSlotIdToRelationship.containsKey(templateWidget.getId()))
            {
                emptyTemplateWidgets.add(templateWidget);
            }
        }

        return emptyTemplateWidgets;
    }

    /**
     * Gets those page widgets that are places in a page region, and that region
     * is valid in the template. If the template has widgets in a given region,
     * that region is not taken into account for return.
     * 
     * @param page {@link PSPage}, must not be <code>null</code>.
     * @param template {@link PSTemplate}, must not be <code>null</code>.
     * @return {@link Set}<{@link PSWidgetItem}> never <code>null</code> but may
     *         be empty.
     */
    public static Set<PSWidgetItem> getPageWidgets(PSPage page, PSTemplate template)
    {
        notNull(page);
        notNull(template);

        Set<PSWidgetItem> pageWidgets = new HashSet<PSWidgetItem>();

        if (template.getRegionTree() != null)
        {
            Set<PSRegion> emptyLeafRegions = getEmptyWidgetRegions(template.getRegionTree());
            Map<String, List<PSWidgetItem>> pageRegionWidgetsMap = page.getRegionBranches().getRegionWidgetsMap();

            for (PSRegion validTemplateRegion : emptyLeafRegions)
            {
                if (pageRegionWidgetsMap.containsKey(validTemplateRegion.getRegionId()))
                {
                    pageWidgets.addAll(pageRegionWidgetsMap.get(validTemplateRegion.getRegionId()));
                }
            }
        }

        return pageWidgets;
    }

    /**
     * Retrieves a list of relationships that are unused assets for the given page.
     * <p>
     * The current implementation will only retrieve:
     * <li>the page assets that do not have matching widgets in page's template</li>
     * <li>the page assets that have been overwritten by assets that belong to
     * the page's template</li>
     * <p>
     * The orphan assets are calculated against the current revision of the page.
     * 
     * @param page {@link PSPage} object representing the page for which we want
     *            to get the orphan assets.
     * @param template {@link PSTemplate} object representing the template used
     *            for the given page. Must not be <code>null</code>
     * @return a {@link Collection}<{@link PSRelationship}> with the unused
     *         assets. Never <code>null</code>, but may be empty.
     */
    public static Collection<PSRelationship> getOrphanedPageAssets(PSPage page, PSTemplate template)
    {
        return getPageAssets(page, template, null);
    }

    /**
     * Retrieves a list of relationships that are used page assets for a given page.
     * 
     * @param page {@link PSPage} object representing the page for which we want
     *            to get the orphan assets.
     * @param template {@link PSTemplate} object representing the template used
     *            for the given page. Must not be <code>null</code>
     * @return a map that maps the widget ID (map key) to the used page asset (map value), never <code>null</code>.
     */
    public static Map<String, PSRelationship> getUsedPageAssets(PSPage page, PSTemplate template)
    {
        Map<String, PSRelationship> widgetToAsset = new HashMap<String, PSRelationship>();
        getPageAssets(page, template, widgetToAsset);
        return widgetToAsset;
    }

    /**
     * Gets the used or orphaned page assets for the supplied page and related template.
     * 
     * @param page {@link PSPage} object representing the page for which we want
     * to get the orphan assets.
     * @param template {@link PSTemplate} object representing the template used
     * for the given page. Must not be <code>null</code>
     * @param widgetToAsset the returned map that maps widget ID (map key) to the used page asset (map value).
     * It is used to collect the used page assets. This may be <code>null</code> if only need to return the orphaned assets.
     * 
     * @return the orphaned page assets. It may be <code>null</code> if only need to collect used assets. 
     */
    private static Collection<PSRelationship> getPageAssets(PSPage page, PSTemplate template, Map<String, PSRelationship> widgetToAsset)
    {
        notNull(page);
        notNull(template);

        List<PSRelationship> pageAssets = getLocalSharedAssetRelationships(page.getId());
        Collection<PSRelationship> orphanAssets = null;
        if (widgetToAsset == null)
        {
            // for getting orphaned assets, then we have to clone the original list, 
            // to avoid modify original list object, which is cached
            orphanAssets = new ArrayList<PSRelationship>(pageAssets);
        }

        // For each widget in pageWidgets use widgetFinderUtils to find the
        // matching relationship from pageAssets and remove it from pageAssets.
        Set<PSWidgetItem> widgets = getPageWidgets(page, template);
        widgets.addAll(getEmptyTemplateWidgets(template));
        
        for (PSWidgetItem widget : widgets)
        {
            PSRelationship matchingRelationship = getMatchRelationship(pageAssets, widget);
            if (matchingRelationship != null)
            {
                if (widgetToAsset != null)
                    widgetToAsset.put(widget.getId(), matchingRelationship);
                else
                    orphanAssets.remove(matchingRelationship);
            }
        }

        return orphanAssets;
    }


    /**
     * Retrieves the list of orphan assets for a given page, calling
     * {@link PSPreviewPageUtils#getOrphanedPageAssets(PSPage, PSTemplate)}, and
     * converts the result into objects of {@link PSOrphanedAssetSummary}.
     * 
     * @param page {@link PSPage} object representing the page for which we want
     *            to get the orphan assets.
     * @param template {@link PSTemplate} object representing the template used
     *            for the given page. Must not be <code>null</code>
     * @return a {@link Set}<{@link PSOrphanedAssetSummary}> with the unused
     *         assets. Never <code>null</code>, but may be empty.
     */
    public static Set<PSOrphanedAssetSummary> getOrphanedAssetsSummaries(PSPage page, PSTemplate template)
    {
        notNull(page);
        notNull(template);

        Set<PSOrphanedAssetSummary> unusedAssets = new HashSet<PSOrphanedAssetSummary>();
        Collection<PSRelationship> orphanAssets = getOrphanedPageAssets(page, template);
        for (PSRelationship relationship : orphanAssets)
        {
            String slotId = relationship.getProperties().get(PSRelationshipConfig.PDU_SLOTID);
            String dependantId = getIdMapper().getString(relationship.getDependent());
            String widgetName = relationship.getProperties().get(PSRelationshipConfig.PDU_WIDGET_NAME);
            unusedAssets.add(new PSOrphanedAssetSummary(dependantId, slotId, widgetName, relationship.getId()));
        }

        return unusedAssets;
    }    
    
    /**
     * @param templateAssets
     * @return
     */
    private static Map<String, PSRelationship> getSlotIdToRelationshipMap(Collection<PSRelationship> templateAssets)
    {
        Map<String, PSRelationship> map = new HashMap<String, PSRelationship>();
        for (PSRelationship relationship : templateAssets)
        {
            map.put(relationship.getProperty(PSRelationshipConfig.PDU_SLOTID), relationship);
        }
        return map;
    }

    private static IPSIdMapper getIdMapper()
    {
        if(idMapper == null)
        {
            idMapper = (IPSIdMapper) getWebApplicationContext().getBean("sys_idMapper");
        }
        return idMapper;
    }
    
    private static IPSIdMapper idMapper;


}
