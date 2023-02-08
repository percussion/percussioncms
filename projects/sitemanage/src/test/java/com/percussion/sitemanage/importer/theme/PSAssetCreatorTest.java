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
