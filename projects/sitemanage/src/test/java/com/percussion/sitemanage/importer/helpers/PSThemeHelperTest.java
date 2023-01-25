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
package com.percussion.sitemanage.importer.helpers;

import java.io.File;

import com.percussion.share.service.IPSDataService;
import junit.framework.TestCase;

import org.junit.Test;

import com.percussion.sitemanage.data.PSPageContent;
import com.percussion.sitemanage.data.PSSite;
import com.percussion.sitemanage.data.PSSiteImportCtx;
import com.percussion.sitemanage.importer.IPSSiteImportLogger;
import com.percussion.sitemanage.importer.IPSSiteImportLogger.PSLogObjectType;
import com.percussion.sitemanage.importer.PSSiteImportLogger;
import com.percussion.sitemanage.importer.helpers.impl.PSThemeHelper;
import com.percussion.theme.service.impl.PSThemeService;

/**
 * @author federicoromanelli
 *
 */
public class PSThemeHelperTest extends TestCase
{
    IPSSiteImportLogger logger = new PSSiteImportLogger(PSLogObjectType.SITE);
    
    public void setUp()
    {
        this.themeService = new PSThemeService();
        themeService.setDefaultThemeRootDirectory(RX_RESOURCES_ROOT);
        themeService.setThemesRootDirectory(WEB_RESOURCES_ROOT);
        themeService.setThemesRootRelativeUrl(WEB_RESOURCES_ROOT);
        themeHelper = new PSThemeHelper(themeService);
        themeHelper.setThemesRootDirectory(WEB_RESOURCES_ROOT);
    }

    public void tearDown() throws IPSDataService.DataServiceDeleteException, IPSDataService.DataServiceNotFoundException {
        // remove all themes created
        themeService.delete(SITE_NAME);
        themeService.delete(SITE_NAME_2_TRANSFORMED);
        themeService.delete(SITE_NAME_3_TRANSFORMED);
        themeService.delete(SITE_NAME_3_TRANSFORMED+"-1");
        themeService.delete(SITE_NAME_3_TRANSFORMED+"-2");
        themeService.delete(SITE_NAME_ROOLBACK);
    }
    
    public void testProcess() throws Exception
    {
        // Create basic context objects
        PSPageContent pageContent = new PSPageContent();
        PSSiteImportCtx importContext = new PSSiteImportCtx();
        PSSite site = new PSSite();
        site.setName(SITE_NAME);
        importContext.setSite(site);
        importContext.setLogger(logger);
        
        themeHelper.process(pageContent, importContext);
        // test the themeSummary object created and name of the new theme
        assertEquals(importContext.getThemeSummary().getName(), SITE_NAME);
        File imageFile = new File(WEB_RESOURCES_ROOT + "/" + importContext.getThemeSummary().getName(), SITE_NAME + ".png");
        File cssFile = new File(WEB_RESOURCES_ROOT + "/" + importContext.getThemeSummary().getName(), SITE_NAME + ".css");
        
        // test if basic files exist and have been renamed
        assertTrue(imageFile.exists());
        assertTrue(cssFile.exists());
    }
    
    public void testProcessDotName() throws Exception
    {
        // Create basic context objects
        PSPageContent pageContent = new PSPageContent();
        PSSiteImportCtx importContext = new PSSiteImportCtx();
        PSSite site = new PSSite();
        site.setName(SITE_NAME_2);
        importContext.setSite(site);
        importContext.setLogger(logger);
        
        themeHelper.process(pageContent, importContext);
        // test the themeSummary object created and name of the new theme was transformed
        assertEquals(importContext.getThemeSummary().getName(), SITE_NAME_2_TRANSFORMED);
        File imageFile = new File(WEB_RESOURCES_ROOT + "/" + importContext.getThemeSummary().getName(), SITE_NAME_2_TRANSFORMED + ".png");
        File cssFile = new File(WEB_RESOURCES_ROOT + "/" + importContext.getThemeSummary().getName(), SITE_NAME_2_TRANSFORMED + ".css");
        
        // test if basic files exist and have been renamed
        assertTrue(imageFile.exists());
        assertTrue(cssFile.exists());
    }

    public void testProcessDotNameCollision() throws Exception
    {
        // Create folders in web_resources/themes to simulate collisions        
        File dirFile = new File(WEB_RESOURCES_ROOT + "/" + SITE_NAME_3_TRANSFORMED);
        File dirFile2 = new File(WEB_RESOURCES_ROOT + "/" + SITE_NAME_3_TRANSFORMED+ "-2");
        assertTrue(dirFile.mkdir());
        assertTrue(dirFile2.mkdir());

        // Create basic context objects
        PSPageContent pageContent = new PSPageContent();
        PSSiteImportCtx importContext = new PSSiteImportCtx();
        PSSite site = new PSSite();
        site.setName(SITE_NAME_3);
        importContext.setSite(site);
        importContext.setLogger(logger);
        
        themeHelper.process(pageContent, importContext);
        // test the themeSummary object created and name of the new theme was transformed and with collision change
        assertEquals(importContext.getThemeSummary().getName(), SITE_NAME_3_TRANSFORMED + "-1");
        File imageFile = new File(WEB_RESOURCES_ROOT + "/" + importContext.getThemeSummary().getName(), SITE_NAME_3_TRANSFORMED + ".png");
        File cssFile = new File(WEB_RESOURCES_ROOT + "/" + importContext.getThemeSummary().getName(), SITE_NAME_3_TRANSFORMED + ".css");
        
        // test if basic files exist and have been renamed
        assertTrue(imageFile.exists());
        assertTrue(cssFile.exists());
    }
    
    public void testRollback() throws Exception
    {
        // Create basic context objects
        PSPageContent pageContent = new PSPageContent();
        PSSiteImportCtx importContext = new PSSiteImportCtx();
        PSSite site = new PSSite();
        site.setName(SITE_NAME_ROOLBACK);
        importContext.setSite(site);
        importContext.setLogger(logger);
        
        themeHelper.process(pageContent, importContext);
        // test the themeSummary object created and name of the new theme
        assertEquals(importContext.getThemeSummary().getName(), SITE_NAME_ROOLBACK);
        File imageFile = new File(WEB_RESOURCES_ROOT + "/" + importContext.getThemeSummary().getName(), SITE_NAME_ROOLBACK + ".png");
        File cssFile = new File(WEB_RESOURCES_ROOT + "/" + importContext.getThemeSummary().getName(), SITE_NAME_ROOLBACK + ".css");
        
        // test if basic files exist and have been renamed
        assertTrue(imageFile.exists());
        assertTrue(cssFile.exists());
        
        themeHelper.rollback(pageContent, importContext);
        
        File dirRollbackFile = new File(WEB_RESOURCES_ROOT + "/" + importContext.getThemeSummary().getName());
        
        // test if basic files exist and have been renamed
        assertFalse(dirRollbackFile.exists());
    }    

    // Services
    private PSThemeService themeService;
    private PSThemeHelper themeHelper;
    
    // Site name constants
    private static String SITE_NAME = "siteName";
    private static String SITE_NAME_2 = "www.someDomain.com";
    private static String SITE_NAME_2_TRANSFORMED = "www-someDomain-com";
    private static String SITE_NAME_3 = "www.some-new-Domain.com";
    private static String SITE_NAME_3_TRANSFORMED = "www-some-new-Domain-com";    
    private static String SITE_NAME_ROOLBACK = "siteNameRollback";
    
    // Path constants
    private static String WEB_RESOURCES_ROOT = "src/test/resources/importer/data/web_resources/themes";
    private static String RX_RESOURCES_ROOT = "src/test/resources/importer/data/rx_resources/default_theme";
}
