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
package com.percussion.content;

import com.percussion.assetmanagement.data.PSAssetWidgetRelationship;
import com.percussion.assetmanagement.data.PSAssetWidgetRelationship.PSAssetResourceType;
import com.percussion.assetmanagement.web.service.PSAssetServiceRestClient;
import com.percussion.content.data.Widget;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSRegionBranches;
import com.percussion.pagemanagement.data.PSRegionWidgets;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.data.PSWidgetItem;
import com.percussion.pagemanagement.web.service.PSPageRestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Create and assign widgets to different regions on pages or templates.
 * 
 * @author rafaelsalis
 */
public class PSWidgetGenerator extends PSItemGenerator<PSPageRestClient>
{

    private PSAssetServiceRestClient assetClient;

    public PSWidgetGenerator(String baseUrl, String uid, String pw)
    {
        super(PSPageRestClient.class, baseUrl, uid, pw);
        assetClient = new PSAssetServiceRestClient(baseUrl);
        assetClient.login(uid, pw);
    }

    /**
     * Parse the regions where the widgets will be add to assign each one the
     * corresponding a widget to add.
     * 
     * @param widgets The list of widgets you want to add.
     * @return A list which associated the widgets with the respective regions
     *         where they will be added.
     */
    public Map<String, List<String>> parseRegionWidget(List<Widget> widgets)
    {
        Map<String, List<String>> regionToWidgets = new HashMap<String, List<String>>();
        for (Widget widget : widgets)
        {
            String regionName = widget.getRegionName();
            List<String> widgetNames = regionToWidgets.get(regionName);
            if (widgetNames == null)
            {
                widgetNames = new ArrayList<String>();
                regionToWidgets.put(regionName, widgetNames);
            }
            widgetNames.add(widget.getWidgetName());
        }

        return regionToWidgets;
    }

    /**
     * In view of the associations between widgets and regions, it adds.
     * 
     * @param regionToWidgets The regions of the page where the widgets should
     *            be will. <code>null</code> when the method call comes from the
     *            creation of a template.
     * @param resultWithRegion The regions of the template where the widgets
     *            will be added. <code>null</code> when the method call comes
     *            from the creation of a page.
     * @param regions The regions of the place to add widgets.
     */
    public void createAndAssignWidgets(Map<String, List<String>> regionToWidgets, PSTemplate resultWithRegion,
            PSRegionBranches regions)
    {
        for (String regionName : regionToWidgets.keySet())
        {
            PSRegionWidgets rw = new PSRegionWidgets();
            rw.setRegionId(regionName);
            List<PSWidgetItem> widgetItems = new ArrayList<PSWidgetItem>();
            for (String widgetName : regionToWidgets.get(regionName))
            {
                PSWidgetItem wi = new PSWidgetItem();
                widgetItems.add(wi);
                wi.setDefinitionId(widgetName);
            }

            if (resultWithRegion != null)
            {
                resultWithRegion.getRegionTree().setRegionWidgets(regionName, widgetItems);
            }
            else if (regions != null)
            {
                regions.setRegionWidgets(regionName, widgetItems);
            }

        }
    }

    /**
     * If the widget has an associated asset, get this property to associate the
     * widget.
     * 
     * @param widgets The list of widgets you want to add.
     * @param template The template where the widgets should be added.
     *            <code>null</code> when the method call comes from the creation
     *            of a page.
     * @param page The page where the widgets should be added. <code>null</code>
     *            when the method call comes from the creation of a template.
     */
    public void linkContent(List<Widget> widgets, PSTemplate template, PSPage page)
    {
        Map<String, Map<String, Integer>> regionNameToWidgetCount = new HashMap<String, Map<String, Integer>>();
        for (Widget widget : widgets)
        {
            long widgetInstanceId = getWidgetInstanceId(template, page, widget, regionNameToWidgetCount);
            if (widget.getContentSource().equalsIgnoreCase("local"))
                throw new UnsupportedOperationException("contentSource=local not supported yet");
            if (widget.getSourceAssetName() != null && widget.getSourceAssetName().trim().length() > 0)
            {
                String sharedAssetId = getAssetGuidFromPath("/Assets" + widget.getSourceAssetName());

                // todo: fix asset order
                String id = (page != null) ? page.getId() : template.getId();
                PSAssetWidgetRelationship awRel = new PSAssetWidgetRelationship(id, widgetInstanceId,
                        widget.getWidgetName(), sharedAssetId, 0);
                awRel.setResourceType(PSAssetResourceType.shared);
                assetClient.createAssetWidgetRelationship(awRel);
            }
        }
    }

    /**
     * 
     * @param template Contains the widget you are looking for.
     * @param page Contains the widget you are looking for.
     * @param widgetDef The widget type you wish to find in this page.
     * @param regionNameToWidgetCount Tracks how many instances of each widget
     *            type within each region. The key is the region name, the value
     *            is a map whose key is the widget def type and whose value is
     *            the count of that type within that region. The first time you
     *            call this method, pass in an empty one, then keep passing the
     *            same one for each successive call.
     * @return Always a valid widget instance id.
     */
    static long getWidgetInstanceId(PSTemplate template, PSPage page, Widget widgetDef,
            Map<String, Map<String, Integer>> regionNameToWidgetCount)
    {
        Map<String, Integer> widgetTypeToInstanceCount = regionNameToWidgetCount.get(widgetDef.getRegionName());
        if (widgetTypeToInstanceCount == null)
        {
            widgetTypeToInstanceCount = new HashMap<String, Integer>();
            regionNameToWidgetCount.put(widgetDef.getRegionName(), widgetTypeToInstanceCount);
        }
        Set<PSRegionWidgets> rws = (page != null) ? page.getRegionBranches().getRegionWidgetAssociations() : template
                .getRegionTree().getRegionWidgetAssociations();
        PSRegionWidgets matchingRw = null;
        for (PSRegionWidgets rw : rws)
        {
            if (rw.getRegionId().equalsIgnoreCase(widgetDef.getRegionName()))
            {
                matchingRw = rw;
                break;
            }
        }
        assert (matchingRw != null);

        List<PSWidgetItem> widgetItems = matchingRw.getWidgetItems();
        int count = 0;
        long instanceId = 0L;
        for (PSWidgetItem wi : widgetItems)
        {
            if (wi.getDefinitionId().equalsIgnoreCase(widgetDef.getWidgetName()))
            {
                Integer curCount = widgetTypeToInstanceCount.get(wi.getDefinitionId());
                if (curCount == null)
                {
                    curCount = new Integer(0);
                }
                if (count == curCount.intValue())
                {
                    widgetTypeToInstanceCount.put(wi.getDefinitionId(), new Integer(count + 1));
                    instanceId = Long.parseLong(wi.getId());
                }
                else
                    count++;
            }
        }
        assert (instanceId != 0L);
        return instanceId;
    }

}
