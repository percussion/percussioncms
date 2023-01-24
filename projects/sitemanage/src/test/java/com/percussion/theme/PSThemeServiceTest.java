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
package com.percussion.theme;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.percussion.theme.data.PSTheme;
import com.percussion.theme.data.PSThemeSummary;
import com.percussion.theme.service.impl.PSThemeService;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class PSThemeServiceTest
{
    private PSThemeService themeService;
    
    @Before
    public void setup() {
        themeService = new PSThemeService();
        themeService.setThemesRootDirectory("src/test/resources/themes");
        themeService.setThemesRootRelativeUrl("/Rhythmyx/web_resources/themes");
        themeService.setThemesTempRootDirectory("src/test/resources/themes.tmp");
        themeService.setThemesTempRootRelativeUrl("/Rhythmyx/sys_resources/temp/themes");
   }
    
    @Test
    public void testFind() throws Exception
    {
        PSThemeSummary summary = themeService.find("test");
        assertNotNull(summary);
        assertNotNull(summary.getName());
        assertEquals("test",summary.getName());
        assertEquals("CSS filename", "test/perc_theme.css", summary.getCssFilePath());
        assertNotNull("Region CSS file", summary.getRegionCssFilePath());
        
        summary = themeService.find("more-than-one-css");
        assertNotNull(summary);
        assertNotNull(summary.getName());
        assertEquals("more-than-one-css",summary.getName());
        assertEquals("CSS filename", "more-than-one-css/more-than-one-css.css", summary.getCssFilePath());
        assertNotNull("Region CSS file", summary.getRegionCssFilePath());
    }
    
    @Test
    public void testFindAll() throws Exception
    {
        List<PSThemeSummary> sums =themeService.findAll();
        assertNotNull(sums);
        assertFalse(sums.isEmpty());
        assertTrue(sums.size() == 4);
        for (PSThemeSummary sum : sums)
        {
            if (sum.getName().equals("test"))
            {
                assertNotNull(sum.getThumbUrl());
                assertNotNull(sum.getCssFilePath());
                assertNotNull("Region CSS file", sum.getRegionCssFilePath());
            }
            else if (sum.getName().equals("more-than-one-css"))
            {
                assertNotNull(sum.getThumbUrl());
                assertNotNull(sum.getCssFilePath());
                assertNotNull("Region CSS file", sum.getRegionCssFilePath());
            }
            else if (sum.getName().equals("more-than-one-thumb-images"))
            {
                assertNotNull(sum.getThumbUrl());
                assertNotNull(sum.getCssFilePath());
                assertNotNull("Region CSS file", sum.getRegionCssFilePath());
            }
            else if (sum.getName().equals("no-thumb-image"))
            {
                assertNull(sum.getThumbUrl());
                assertNotNull(sum.getCssFilePath());
                assertNull("There is no Region CSS file", sum.getRegionCssFilePath());
            }
        }
    }
    
    @Test
    public void testLoad() throws Exception
    {
        PSTheme theme = themeService.load("test");
        assertNotNull("theme",theme);
        assertNotNull("css", theme.getCSS());
        
        theme = themeService.load("more-than-one-css");
        assertNotNull("theme",theme);
        assertNotNull("css", theme.getCSS());
    }

    @Test
    public void testCreate() throws Exception
    {
        PSThemeSummary newSum = null;
        try
        {
            PSThemeSummary sum = themeService.find("test");
            newSum = themeService.create("mynewtheme", sum.getName());
            assertNotNull(newSum);
            assertTrue(newSum.getCssFilePath().startsWith(newSum.getName()));
            assertNotNull("Region CSS file", newSum.getRegionCssFilePath());
            
            PSTheme theme = themeService.load(sum.getName());
            PSTheme newTheme = themeService.load(newSum.getName());
            assertEquals(newTheme.getCSS(), theme.getCSS());            
            
            try
            {
                PSThemeSummary newSum2 = themeService.create("shouldnotbecreated", "doesnotexist");
                fail("New theme '" + newSum2.getName() + "' should not have been created");
            }
            catch (Exception e)
            {
                // expected
            }
        }
        finally
        {
            if (newSum != null)
            {
                themeService.delete(newSum.getName());
            }
        }
    }
    
    @Test
    public void testDelete() throws Exception
    {
        PSThemeSummary sum = themeService.find("test");
        PSThemeSummary newSum = themeService.create("mynewtheme", sum.getName());
        themeService.delete(newSum.getName());
        try
        {
            newSum = themeService.find(newSum.getName());
            fail("Theme '" + newSum.getName() + "' should have been deleted");
        }
        catch (Exception e)
        {
            // expected
        }
        
        try
        {
            themeService.delete(newSum.getName());
        }
        catch (Exception e)
        {
            // issue CM-276
            fail("Theme '" + newSum.getName() + "' does not exist anymore, but the attemp to delete it again, should not throw any exception");
        }
    }
}
