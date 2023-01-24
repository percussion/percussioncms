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

package com.percussion.sitemanage.importer;

import com.percussion.pagemanagement.service.IPSPageCatalogService;
import com.percussion.pagemanagement.service.impl.PSPageCatalogService;
import com.percussion.queue.IPSPageImportQueue;
import com.percussion.queue.impl.IPSPerformPageImport;
import com.percussion.queue.impl.PSPageImportQueue;
import com.percussion.share.service.IPSSystemProperties;
import com.percussion.test.PSServletTestCase;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.experimental.categories.Category;

import java.util.Properties;

@Category(IntegrationTest.class)
public class PSSiteImportTestBase extends PSServletTestCase
{
    protected IPSPageImportQueue m_importQueue;
    protected IPSSystemProperties m_systemProps;
    protected IPSPerformPageImport m_systemPageImporter;
    
    protected IPSPageCatalogService m_pageCatalogService;
    
    protected PSMockSystemProps m_testProps = new PSMockSystemProps();
    
    protected void setUp() throws Exception
    {
        super.setUp();
        m_importQueue = (IPSPageImportQueue) getBean("pageImportQueue");
        m_systemProps = ((PSPageImportQueue) m_importQueue).getSystemProps();
        m_pageCatalogService = (IPSPageCatalogService) getBean("pageCatalogService");

        ((PSPageImportQueue)m_importQueue).setSystemProps(m_testProps);
        ((PSPageCatalogService)m_pageCatalogService).setSystemProps(m_testProps);
        
        m_testProps.setCatalogMax("0");
        m_testProps.setImportMax("0");
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        ((PSPageImportQueue)m_importQueue).setPageImporter(m_systemPageImporter);
        ((PSPageImportQueue) m_importQueue).setSystemProps(m_systemProps);
        
        ((PSPageCatalogService)m_pageCatalogService).setSystemProps(m_systemProps);
    }

    protected class PSMockSystemProps extends Properties implements IPSSystemProperties
    {
        public void setCatalogMax(String value)
        {
            setProperty(CATALOG_PAGE_MAX, value);
        }
        public void setImportMax(String value)
        {
            setProperty(IMPORT_PAGE_MAX, value);
        }
    }
}
