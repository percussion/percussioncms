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
