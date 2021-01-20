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
package com.percussion.content;

import com.percussion.assetmanagement.web.service.PSAssetServiceRestClient;
import com.percussion.content.data.CM1DataDef.SiteFolder.PageDef;
import com.percussion.content.data.Widget;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSRegion;
import com.percussion.pagemanagement.data.PSRegionBranches;
import com.percussion.pagemanagement.data.PSRegionCode;
import com.percussion.pagemanagement.data.PSRegionNode;
import com.percussion.pagemanagement.data.PSTemplateSummary;
import com.percussion.pagemanagement.web.service.PSPageRestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Creates a page as defined in a {@link PageDef} object in a specified site
 * folder. Also transitions the generated page to a specified state.
 * 
 * @author paulhoward
 */
public class PSPageGenerator extends PSItemGenerator<PSPageRestClient>
{
    private PSTemplateGenerator templateGen;

    private PSAssetServiceRestClient assetClient;

    private PSWidgetGenerator widgetGen;

    public PSPageGenerator(String baseUrl, String uid, String pw)
    {
        super(PSPageRestClient.class, baseUrl, uid, pw);
        templateGen = new PSTemplateGenerator(baseUrl, uid, pw);
        assetClient = new PSAssetServiceRestClient(baseUrl);
        widgetGen = new PSWidgetGenerator(baseUrl, uid, pw);
        assetClient.login(uid, pw);
    }

    /**
     * If browser title or link title is not specified in the page, the page
     * name is used for each.
     * 
     * @param def Not <code>null</code>. If link title is not specified, it
     *            defaults to name. If browser title is not specified, it
     *            defaults to link title.
     * @param path Must start w/ a '/' and is relative to /Sites. Trailing slash
     *            is optional.
     * @return The generated page, never <code>null</code>.
     */
    public PSPage createPage(PageDef def, String path)
    {
        log.info("Creating page '/Sites" + path + (path.endsWith("/") ? "" : "/") + def.getName() + "' ...");
        PSPage page = new PSPage();
        page.setName(def.getName());
        page.setDescription(def.getMetaPageDescription());
        page.setAdditionalHeadContent(def.getAdditionalHeadContent());
        page.setAfterBodyStartContent(def.getAfterBodyStart());
        page.setBeforeBodyCloseContent(def.getBeforeBodyClose());

        String linkTitle = def.getLinkTitle();
        if (linkTitle == null || linkTitle.trim().isEmpty())
            linkTitle = def.getName();
        page.setLinkTitle(linkTitle);

        String bTitle = def.getPageTitle();
        if (bTitle == null || bTitle.trim().isEmpty())
            bTitle = page.getLinkTitle();
        page.setTitle(bTitle);

        page.setFolderPath("//Sites" + path); // must be internal folder path
        
        PSTemplateSummary tsum = null; 
        if (def.getBlogPostTemplate() != null)//If a blog is being created
        {
            String templateBlogPage = def.getTemplateName().get(0) + "-" + def.getBlogPostTemplate();
            tsum = templateGen.findTemplateByName(templateBlogPage);
            // Try to find the duplicated template
            if (tsum == null)
                tsum = templateGen.findTemplateByName(def.getBlogPostTemplate());
            // If there is not a duplicated template, try to get the specified template
            if (tsum == null)
                throw new RuntimeException("templateName for PageDef not found: " + templateBlogPage);
        }
        else
        {
            tsum = templateGen.findTemplateByName(def.getTemplateName().get(0));
            if (tsum == null)
                throw new RuntimeException("templateName for PageDef not found: " + def.getTemplateName());
        }
        
        page.setTemplateId(tsum.getId());

        PSRegionBranches regions = new PSRegionBranches();
        page.setRegionBranches(regions);

        List<Widget> widgets = def.getWidget();
        Map<String, List<String>> regionToWidgets = widgetGen.parseRegionWidget(widgets);

        List<PSRegion> regionOverrides = new ArrayList<PSRegion>();
        regions.setRegions(regionOverrides);
        for (String regionName : regionToWidgets.keySet())
        {
            PSRegion r = new PSRegion();
            regionOverrides.add(r);
            r.setRegionId(regionName);
            List<PSRegionNode> children = new ArrayList<PSRegionNode>();
            r.setChildren(children);
            PSRegionCode n = new PSRegionCode();
            n.setTemplateCode("#region(\"" + regionName + "\", \"\", \"\", \"\", \"\")");
            children.add(n);
        }

        widgetGen.createAndAssignWidgets(regionToWidgets, null, regions);

        PSPage result = getRestClient().save(page);
        log.info("Created page " + result.getId());

        widgetGen.linkContent(widgets, null, result);

        transitionToState(result.getId(), def.getTargetStateName());
        return result;
    }
}
