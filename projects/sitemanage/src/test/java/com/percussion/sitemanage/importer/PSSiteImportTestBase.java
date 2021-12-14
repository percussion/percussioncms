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
