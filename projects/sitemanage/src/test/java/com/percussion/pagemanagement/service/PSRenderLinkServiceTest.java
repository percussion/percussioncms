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

package com.percussion.pagemanagement.service;

import com.percussion.linkmanagement.service.PSPublicLinkContext;
import com.percussion.pagemanagement.assembler.PSAbstractAssemblyContext.EditType;
import com.percussion.pagemanagement.assembler.impl.PSAssemblyRenderLinkContext;
import com.percussion.pagemanagement.data.PSRenderLink;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.test.PSServletTestCase;
import com.percussion.theme.data.PSThemeSummary;
import com.percussion.theme.service.impl.PSThemeService;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Category(IntegrationTest.class)
public class PSRenderLinkServiceTest extends PSServletTestCase
{
    private PSSiteDataServletTestCaseFixture fixture;

    private File regionCssFile = null;
    
    @Override
    public void setUp() throws Exception
    {
        PSSpringWebApplicationContextUtils.injectDependencies(this);
        fixture = new PSSiteDataServletTestCaseFixture(request, response);
        fixture.setUp();
        fixture.pageCleaner.add(fixture.site1.getFolderPath() + "/Page1");
        //FB:IJU_SETUP_NO_SUPER NC 1-16-16
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception
    {
        fixture.tearDown();
        
        if (regionCssFile != null && regionCssFile.exists())
        {
            regionCssFile.delete();
            regionCssFile = null;
        }
    }
    
    public void testRenderLinkRegionCSS() throws IOException
    {
        PSTemplate template = fixture.getTemplateService().load(fixture.template1.getId());
        String themeName = template.getTheme();
        PSPublicLinkContext context = new PSPublicLinkContext(fixture.site1);
        PSRenderLink regionCss = renderLinkService.renderLinkThemeRegionCSS(context, template.getTheme(), false, EditType.PAGE);
        assertNotNull(regionCss.toString());
        
        PSThemeSummary summary = themeService.find(themeName);
        
        removeRegionCssFile(summary);
        regionCss = renderLinkService.renderLinkThemeRegionCSS(context, template.getTheme(), false, EditType.PAGE);
        assertTrue(StringUtils.isBlank(regionCss.toString()));
        
        copyThemeCssToRegionCssFile(summary);
        regionCss = renderLinkService.renderLinkThemeRegionCSS(context, template.getTheme(), false, EditType.PAGE);
        assertTrue(regionCss.toString().endsWith(PSThemeService.THEME_REGION_CSS_PATH));
        assertTrue(regionCss.toString().startsWith("/web_resources"));
        
        PSAssemblyRenderLinkContext assembContext = new PSAssemblyRenderLinkContext();
        assembContext.setSite(fixture.site1);
        regionCss = renderLinkService.renderLinkThemeRegionCSS(assembContext, template.getTheme(), false, EditType.PAGE);
        assertTrue(regionCss.toString().indexOf(PSThemeService.THEME_REGION_CSS_PATH + "?time=") != -1);
        assertTrue(regionCss.toString().startsWith("/Rhythmyx/web_resources"));
    }
    
    private void removeRegionCssFile(PSThemeSummary summary)
    {
        File regionCssFile = getRegionCssFile(summary);
        if (regionCssFile != null && regionCssFile.exists())
            regionCssFile.delete();
    }

    private void copyThemeCssToRegionCssFile(PSThemeSummary summary) throws IOException
    {
        removeRegionCssFile(summary);
        File regionFile = new File(getRegionCssFilePath(summary));
        File parentDir = regionFile.getParentFile();
        if (!parentDir.exists())
            parentDir.mkdirs();
        
        
        File cssFile = getThemeCssFile(summary);
        InputStream cssIn = null;
        OutputStream regionOut = null;
        try
        {
            cssIn = new FileInputStream(cssFile);
            regionOut = new FileOutputStream(regionFile);
            IOUtils.copy(cssIn, regionOut);
            regionCssFile = regionFile;
        }
        finally
        {
            IOUtils.closeQuietly(cssIn);
            IOUtils.closeQuietly(regionOut);
        }
    }
    
    private File getRegionCssFile(PSThemeSummary summary)
    {
        if (summary.getRegionCssFilePath() == null)
            return null;
        
        String path = getRegionCssFilePath(summary);
        File regionCssFile = new File(path);
        return regionCssFile;
    }

    private String getRegionCssFilePath(PSThemeSummary summary)
    {
        String path = themeService.getThemesRootDirectory() + "/"+ summary.getName() + "/" + PSThemeService.THEME_REGION_CSS_PATH;
        return path;
    }

    private File getThemeCssFile(PSThemeSummary summary)
    {
        String path = themeService.getThemesRootDirectory() + "/"+ summary.getCssFilePath();
        File cssFile = new File(path);
        return cssFile;
    }

    public void setRenderLinkService(IPSRenderLinkService renderService)
    {
        renderLinkService = renderService;
    }
    
    public void setThemeSrevice(PSThemeService themeSrv)
    {
        themeService = themeSrv;
    }
    
    private IPSRenderLinkService renderLinkService;
    
    private PSThemeService themeService;
}
