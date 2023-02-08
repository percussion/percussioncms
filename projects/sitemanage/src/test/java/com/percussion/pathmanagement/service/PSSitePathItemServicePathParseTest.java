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
package com.percussion.pathmanagement.service;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.percussion.pathmanagement.service.IPSPathService.PSPathNotFoundServiceException;
import com.percussion.pathmanagement.service.impl.PSSitePathItemService;
import com.percussion.pathmanagement.service.impl.PSSitePathItemService.SiteIdAndFolderPath;

public class PSSitePathItemServicePathParseTest
{
    
    TestSitePathItemService ps;
    String siteFolderPath = "//Sites/Site1";
        
    @Before
    public void setup() {
        ps = new TestSitePathItemService();
    }
    
    
    @Test
    public void shouldExtractSiteIdAndPath() throws Exception
    {
        assertExtraction("/site1/b/c/", "site1", "//Sites/Site1/b/c/");
        assertExtraction("/site1/b/", "site1", "//Sites/Site1/b/");
        assertExtraction("/site1/", "site1", "//Sites/Site1/");
    }
    
    
    @Test
    public void shouldSayIfItHasOnlyTheSiteId() throws Exception
    {
        SiteIdAndFolderPath sfp =  ps.getSiteIdAndFolderPath("/site3/");
        assertTrue(sfp.isOnlySiteId());
    }
    
    @Test
    public void shouldSayIfItHasTheFolderPathWithTheSiteId() throws Exception
    {
        SiteIdAndFolderPath sfp =  ps.getSiteIdAndFolderPath("/site3/b/");
        assertFalse(sfp.isOnlySiteId());
    }
    
    @Test(expected=PSPathNotFoundServiceException.class)
    public void shouldFailOnRootPathAsThatIsHandledElseWhere() throws Exception
    {
        assertNull(ps.getSiteIdAndFolderPath("/"));
        
    }
    
    @Test(expected=PSPathNotFoundServiceException.class)
    public void shouldFailOnNoMatch() throws Exception
    {
        assertNull(ps.getSiteIdAndFolderPath("/asdfasd"));
        
    }
    
    public void assertExtraction(String path, String expectedSiteId, String expectedFolderPath) throws PSPathNotFoundServiceException {
        SiteIdAndFolderPath sfp = ps.getSiteIdAndFolderPath(path);
        assertEquals("Site Id", expectedSiteId,sfp.getSiteId());
        assertEquals("Folder path", expectedFolderPath, sfp.getFullFolderPath(siteFolderPath));
    }
    
    public static class TestSitePathItemService extends PSSitePathItemService {
        public TestSitePathItemService()
        {
            super(null, null, null, null, null, null, null, null, null, null, null, null);
        }

        @Override
        public SiteIdAndFolderPath getSiteIdAndFolderPath(String path) throws PSPathNotFoundServiceException {
            return super.getSiteIdAndFolderPath(path);
        }
        
    }

}
