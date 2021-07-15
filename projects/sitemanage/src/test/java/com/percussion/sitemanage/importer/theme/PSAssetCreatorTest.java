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
package com.percussion.sitemanage.importer.theme;

import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.assetmanagement.service.IPSAssetService;
import com.percussion.pagemanagement.service.PSSiteDataServletTestCaseFixture;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.test.PSServletTestCase;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.content.IPSContentWs;

import java.io.InputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author Ignacio Erro
 * 
 */
@Category(IntegrationTest.class)
public class PSAssetCreatorTest extends PSServletTestCase
{
    private static final String tempPrefix = "TemplateTest";

    private PSSiteDataServletTestCaseFixture fixture;

    private IPSAssetService assetService;

    private IPSContentWs contentWs;

    private PSAssetCreator assetCreator = new PSAssetCreator();

    @Override
    @Before
    public void setUp() throws Exception
    {
        PSSpringWebApplicationContextUtils.injectDependencies(this);
        fixture = new PSSiteDataServletTestCaseFixture(request, response);
        fixture.setUp("Admin", "demo", "Default");
        //FB:IJU_SETUP_NO_SUPER NC 1-16-16
        super.setUp();
    }

    @Override
    @After
    public void tearDown() throws Exception
    {
        fixture.tearDown();
        fixture.templateCleanUp(tempPrefix);
    }

    @Test
    public void testCreateAssetForImage()
    {
        String folderPath = "/Assets/uploads/www.percussion.com/image.JPG";

        try
        {
            InputStream in = getClass().getResourceAsStream("image.JPG");

            PSAsset newAsset = assetCreator.createAssetIfNeeded(in, folderPath);

            assertNotNull(newAsset);
            assertEquals("image.JPG", newAsset.getName());
        }
        catch (Exception e)
        {
            fail("Error creating the Asset.");
        }
    }
    
    @Test
    public void testCreateAssetForGifImage()
    {
        String folderPath = "/Assets/uploads/www.percussion.com/widgetIconPreviewPageOver.gif";

        try
        {
            InputStream in = getClass().getResourceAsStream("widgetIconPreviewPageOver.gif");

            PSAsset newAsset = assetCreator.createAssetIfNeeded(in, folderPath);

            assertNotNull(newAsset);
            assertEquals("widgetIconPreviewPageOver.gif", newAsset.getName());
        }
        catch (Exception e)
        {
            fail("Error creating the Asset.");
        }
    }
    
    @Test
    public void testCreateAssetForFlash()
    {
        String folderPath = "/Assets/uploads/www.percussion.com/flash.swf";

        try
        {
            InputStream in = getClass().getResourceAsStream("flash.swf");

            PSAsset newAsset = assetCreator.createAssetIfNeeded(in, folderPath);

            assertNotNull(newAsset);
            assertEquals("flash.swf", newAsset.getName());
        }
        catch (Exception e)
        {
            fail("Error creating the Asset.");
        }
    }
    
    @Test
    public void testCreateAssetForWord()
    {
        String folderPath = "/Assets/uploads/www.percussion.com/testWordDoc.doc";

        try
        {
            InputStream in = getClass().getResourceAsStream("testWordDoc.doc");

            PSAsset newAsset = assetCreator.createAssetIfNeeded(in, folderPath);

            assertNotNull(newAsset);
            assertEquals("testWordDoc.doc", newAsset.getName());
        }
        catch (Exception e)
        {
            fail("Error creating the Asset.");
        }
    }
    
    @Test
    public void testCreateAssetForPdf()
    {
        String folderPath = "/Assets/uploads/www.percussion.com/testPdf.pdf";

        try
        {
            InputStream in = getClass().getResourceAsStream("testPdf.pdf");

            PSAsset newAsset = assetCreator.createAssetIfNeeded(in, folderPath);

            assertNotNull(newAsset);
            assertEquals("testPdf.pdf", newAsset.getName());
        }
        catch (Exception e)
        {
            fail("Error creating the Asset.");
        }
    }
    
    public IPSAssetService getAssetService()
    {
        return assetService;
    }

    public void setAssetService(IPSAssetService assetService)
    {
        this.assetService = assetService;
    }

    public IPSContentWs getContentWs()
    {
        return contentWs;
    }

    public void setContentWs(IPSContentWs contentWs)
    {
        this.contentWs = contentWs;
    }
}
