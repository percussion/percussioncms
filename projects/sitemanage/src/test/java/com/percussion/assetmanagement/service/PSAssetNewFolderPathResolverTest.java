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

package com.percussion.assetmanagement.service;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

import java.util.List;

import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.exception.PSValidationException;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.percussion.assetmanagement.service.impl.PSAssetNewFolderPathResolver;
import com.percussion.assetmanagement.service.impl.PSAssetNewFolderPathResolver.PSResolvedFolderPath;
import com.percussion.assetmanagement.service.impl.PSAssetNewFolderPathResolver.PSResolvedFolderPath.PSResolvedFolderPathType;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.share.data.IPSItemSummary;
import com.percussion.sitemanage.data.PSSiteSummary;
import com.percussion.sitemanage.service.IPSSiteTemplateService;

//import static java.util.Arrays.*;
//import static org.hamcrest.CoreMatchers.*;
//import static org.junit.matchers.JUnitMatchers.*;

/**
 * Scenario description: 
 * @author adamgent, Jan 7, 2010
 */
@RunWith(JMock.class)
public class PSAssetNewFolderPathResolverTest
{

    private Mockery context = new JUnit4Mockery();

    private PSAssetNewFolderPathResolver resolver;

    private IPSSiteTemplateService siteTemplateService;
    private IPSPageService pageService;
    
    private PSResolvedFolderPath resolved;
    private IPSItemSummary owner;
    private IPSItemSummary asset;
    private IPSItemSummary pageItem;
    private PSPage page;
    private IPSItemSummary template;
    private PSSiteSummary siteSummary;

    @Before
    public void setUp() throws Exception
    {
        
        siteTemplateService = context.mock(IPSSiteTemplateService.class);
        pageService = context.mock(IPSPageService.class);
        resolver = new PSAssetNewFolderPathResolver(pageService, siteTemplateService);
        pageItem = createItemSummary("p", IPSPageService.PAGE_CONTENT_TYPE, asList("//a/b"));
        template = createItemSummary("t", IPSTemplateService.TPL_CONTENT_TYPE, asList("//a/b"));
        /*
         * Asset in site A and site B
         */
        asset = createItemSummary("a", "asset", asList("//a/b", "//c/b"));
        page = new PSPage();
        page.setId("p");
        page.setFolderPath("//a/d");
        page.setTemplateId("t");
        
        siteSummary = new PSSiteSummary();
        siteSummary.setId("s");
        siteSummary.setFolderPath("//a");
        
    }

    @Test
    public void shouldResolveForPageUsingAssetsPath() throws IPSDataService.DataServiceLoadException, PSValidationException, IPSDataService.DataServiceNotFoundException {

        owner = pageItem;
        context.checking(new Expectations()
        {{
            one(pageService).find("p");
            will(returnValue(page));
            one(siteTemplateService).findSitesByTemplate("t");
            will(returnValue(asList(siteSummary)));
        }});
        
        resolved = resolver.resolveFolderPath(owner, asset);

        assertNotNull(resolved);
        assertEquals("//a/b", resolved.getFolderPath());
        assertEquals(PSResolvedFolderPathType.PAGE, resolved.getType());
        assertEquals(true, resolved.isAlreadyInFolder());
    }
    
    @Test
    public void shouldResolveForPageUsingSitePath() throws IPSDataService.DataServiceLoadException, PSValidationException, IPSDataService.DataServiceNotFoundException {

        pageItem = createItemSummary("blah", IPSPageService.PAGE_CONTENT_TYPE, asList("//SITE/blah"));
        owner = pageItem;
        siteSummary.setFolderPath("//SITE");
        context.checking(new Expectations()
        {{
            one(pageService).find("blah");
            will(returnValue(page));
            one(siteTemplateService).findSitesByTemplate("t");
            will(returnValue(asList(siteSummary)));
        }});
        
        resolved = resolver.resolveFolderPath(owner, asset);

        assertNotNull(resolved);
        assertEquals("//a/d", resolved.getFolderPath());
        assertEquals(PSResolvedFolderPathType.PAGE, resolved.getType());
        assertEquals(false, resolved.isAlreadyInFolder());
    }
    

    @Test
    public void shouldResolveForTemplateUsingAssetsPath() throws IPSDataService.DataServiceLoadException, PSValidationException, IPSDataService.DataServiceNotFoundException {

        owner = template;
        context.checking(new Expectations()
        {{
            one(siteTemplateService).findSitesByTemplate("t");
            will(returnValue(asList(siteSummary)));
        }});
        
        resolved = resolver.resolveFolderPath(owner, asset);

        assertNotNull(resolved);
        assertEquals("//a/b", resolved.getFolderPath());
        assertEquals(PSResolvedFolderPathType.TEMPLATE, resolved.getType());
        assertEquals(true, resolved.isAlreadyInFolder());
    }

    @Test
    public void shouldResolveForTemplateUsingSitePath() throws IPSDataService.DataServiceLoadException, PSValidationException, IPSDataService.DataServiceNotFoundException {

        owner = template;
        siteSummary.setFolderPath("//SITE");
        context.checking(new Expectations()
        {{
            one(siteTemplateService).findSitesByTemplate("t");
            will(returnValue(asList(siteSummary)));
        }});
        
        resolved = resolver.resolveFolderPath(owner, asset);

        assertNotNull(resolved);
        assertEquals("//SITE", resolved.getFolderPath());
        assertEquals(PSResolvedFolderPathType.TEMPLATE, resolved.getType());
        /*
         * It should be in the folder already
         */
        assertEquals(false, resolved.isAlreadyInFolder());
    }
    

    
    private IPSItemSummary createItemSummary(final String id, final String type, final List<String> paths) {
        final IPSItemSummary item = context.mock(IPSItemSummary.class, id);
        context.checking(new Expectations()
        {
            {
                allowing(item).getId();
                will(returnValue(id));
                
                allowing(item).getType();
                will(returnValue(type));
                
                allowing(item).getFolderPaths();
                will(returnValue(paths));
                
            }
        });
        
        return item;
    }
}


