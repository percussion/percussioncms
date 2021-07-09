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
