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

import com.percussion.content.data.TemplateDef;
import com.percussion.content.data.Widget;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.data.PSTemplateSummary;
import com.percussion.pagemanagement.web.service.PSTemplateServiceClient;
import com.percussion.sitemanage.service.PSSiteTemplates;
import com.percussion.sitemanage.service.PSSiteTemplates.CreateTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PSTemplateGenerator extends PSGenerator<PSTemplateServiceClient>
{
    private PSSiteTemplateRestClient siteTemplateClient;

    private PSWidgetGenerator widgetGen;

    public PSTemplateGenerator(String baseUrl, String uid, String pw)
    {
        super(PSTemplateServiceClient.class, baseUrl, uid, pw);
        siteTemplateClient = new PSSiteTemplateRestClient(baseUrl);
        widgetGen = new PSWidgetGenerator(baseUrl, uid, pw);
        siteTemplateClient.login(uid, pw);
    }

    public PSTemplateSummary findTemplateByName(String name)
    {
        List<PSTemplateSummary> templateSums = getRestClient().findAll();
        PSTemplateSummary templateSum = null;
        for (PSTemplateSummary sum : templateSums)
        {
            if (sum.getName().equalsIgnoreCase(name))
            {
                templateSum = sum;
                break;
            }
        }
        return templateSum;
    }

    public PSTemplate createTemplate(TemplateDef def)
    {
        log.info("Creating template " + def.getName() + " in site " + def.getSiteName());
        String themeName = "percussion";

        // a blank template needs to be created first, then it is updated with
        // all its data
        PSSiteTemplates tpls = new PSSiteTemplates();
        List<CreateTemplate> ctpls = new ArrayList<CreateTemplate>();
        tpls.setCreateTemplates(ctpls);
        CreateTemplate ct = new CreateTemplate();
        ctpls.add(ct);
        ct.setName(def.getName());
        ct.setSiteIds(Collections.singletonList(def.getSiteName()));
        PSTemplateSummary tsum = findTemplateByName(def.getBaseTemplateName());
        ct.setSourceTemplateId(tsum.getId());
        List<PSTemplateSummary> sums = siteTemplateClient.save(tpls);

        PSTemplate template = new PSTemplate();
        template.setAdditionalHeadContent(def.getAdditionalHeadContent());
        template.setAfterBodyStartContent(def.getAfterBodyStart());
        template.setBeforeBodyCloseContent(def.getBeforeBodyClose());
        template.setName(def.getName());
        template.setId(sums.get(0).getId()); // must use id, same name results
                                             // in new template being created
        template.setSourceTemplateName(tsum.getName());
        template.setLabel(tsum.getLabel());
        template.setTheme(themeName);
        template.setReadOnly(false);

        List<Widget> widgets = def.getWidget();
        Map<String, List<String>> regionToWidgets = widgetGen.parseRegionWidget(widgets);

        PSTemplate resultWithRegion = getRestClient().save(template);
        widgetGen.createAndAssignWidgets(regionToWidgets, resultWithRegion, null);

        PSTemplate result = getRestClient().save(resultWithRegion);

        widgetGen.linkContent(widgets, result, null);

        log.info("Created template " + result.getId());
        return result;
    }
}

