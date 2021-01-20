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

package com.percussion.sitemanage.service;

import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.sitemanage.dao.IPSiteDao;
import com.percussion.sitemanage.dao.impl.PSSitePublishDao;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.junit.experimental.categories.Category;

/**
 * Test the site publish dao.
 */
@Category(IntegrationTest.class)
public class PSSitePublishDaoTest extends ServletTestCase
{
    @Override
    public void setUp() throws Exception
    {
        PSSpringWebApplicationContextUtils.injectDependencies(this);
        super.setUp();
    } 
    
    public void testGetPublishingBase() throws Exception
    {
        String siteName = "Test";
        String publishingDir = sitePubDao.makePublishingDir(siteName);
        assertEquals("", sitePubDao.getPublishingBase(publishingDir, siteName));
        
        publishingDir = "/" + publishingDir;
        assertEquals("/", sitePubDao.getPublishingBase(publishingDir, siteName));
        
        publishingDir = "\\" + sitePubDao.makePublishingDir(siteName);
        assertEquals("\\", sitePubDao.getPublishingBase(publishingDir, siteName));
        
        publishingDir = "/tomcat/" + sitePubDao.makePublishingDir(siteName);
        assertEquals("/tomcat", sitePubDao.getPublishingBase(publishingDir, siteName));
        
        publishingDir = "/tomcat/server/" + sitePubDao.makePublishingDir(siteName);
        assertEquals("/tomcat/server", sitePubDao.getPublishingBase(publishingDir, siteName));
    }

    public void testGetPublishingRoot() throws Exception
    {
        String siteName = "Test";
        String publishingDir = sitePubDao.makePublishingDir(siteName);
        String publishingRoot = sitePubDao.getPublishingRoot("", siteName);
        assertEquals(publishingDir, publishingRoot);
        
        publishingRoot = sitePubDao.getPublishingRoot("/", siteName);
        assertEquals("/" + publishingDir, publishingRoot);
        
        publishingRoot = sitePubDao.getPublishingRoot("\\", siteName);
        assertEquals("\\" + publishingDir, publishingRoot);
        
        publishingRoot = sitePubDao.getPublishingRoot("/tomcat", siteName);
        assertEquals("/tomcat/" + publishingDir, publishingRoot);
        
        publishingRoot = sitePubDao.getPublishingRoot("/tomcat/server", siteName);
        assertEquals("/tomcat/server/" + publishingDir, publishingRoot);
    }
    
    public void testMakePublishingDir() throws Exception
    {
        String siteName = "Test";
        assertEquals(siteName + "apps/ROOT", sitePubDao.makePublishingDir(siteName));
    }
    
    public PSSitePublishDao getSitePubDao()
    {
        return sitePubDao;
    }

    public void setSitePubDao(PSSitePublishDao sitePubDao)
    {
        this.sitePubDao = sitePubDao;
    }

    public IPSiteDao getSiteDao()
    {
        return siteDao;
    }

    public void setSiteDao(IPSiteDao siteDao)
    {
        this.siteDao = siteDao;
    }
    
    private PSSitePublishDao sitePubDao;
    private IPSiteDao siteDao;
   
}
