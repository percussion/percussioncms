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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import com.percussion.pathmanagement.data.PSDeleteFolderCriteria;
import com.percussion.pathmanagement.data.PSItemByWfStateRequest;
import com.percussion.pathmanagement.data.PSMoveFolderItem;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.pathmanagement.data.PSRenameFolderItem;
import com.percussion.pathmanagement.service.impl.PSDispatchingPathService;
import com.percussion.pathmanagement.service.impl.PSDispatchingPathService.IPSPathMatcher.PathMatch;
import com.percussion.share.data.PSItemProperties;
import com.percussion.share.data.PSNoContent;
import com.percussion.share.service.exception.PSBeanValidationException;
import com.percussion.ui.service.IPSListViewHelper;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class PSDispatchingPathServicePathParsingTest
{
    PSDispatchingPathService.PathMatcher pathMatcher;
    PSDispatchingPathService.PathNormalizer pathNormalizer;
    Map<String, IPSPathService> pathRegistry;
    TestPathService pathServiceA;
    TestPathService pathServiceB;
    
    @Before
    public void setup () {
        pathRegistry = new HashMap<>();
        pathNormalizer = new PSDispatchingPathService.PathNormalizer();
        pathMatcher = new PSDispatchingPathService.PathMatcher(pathNormalizer, pathRegistry, null, null);
        pathServiceA = new TestPathService();
        pathServiceB = new TestPathService();
        pathRegistry.put("/a/", pathServiceA);
        pathRegistry.put("/b/", pathServiceB);
    }
    
    @Test
    public void shouldReturnProperFullPath() throws Exception
    {
        PathMatch pm = new PathMatch("/a/", "/b/",  "/a/b/", null, null, null);
        assertEquals("/a/b/c/d", pm.toFullPath("b/c/d"));
        assertEquals("/a/b/c/d", pm.toFullPath("/b/c/d"));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void shouldFailToReturnProperFullPathIfGivenRelativePathIsNull() throws Exception
    {
        PathMatch pm = new PathMatch("/a/", "/b/",  "/a/b/", null, null, null);
        assertEquals("/a/b/c/d", pm.toFullPath(null));
    }
    @Test
    public void shouldNormalizePath() throws Exception
    {
        assertNormalize("", "/");
        assertNormalize("aba/","/aba/");
        assertNormalize("/aba", "/aba/");
        assertNormalize("     /aba         ", "/aba/");
        assertNormalize("/ ", "/");
    }
    
    @Test(expected=IllegalArgumentException.class)
    @SuppressFBWarnings("NP_NULL_PARAM_DEREF_ALL_TARGETS_DANGEROUS")
    public void shouldFailOnNormalizeNullPath() throws Exception
    {
        assertNormalize(null, null);
    }
    
    void assertNormalize(String path, String expected) {
        String actual = pathNormalizer.normalizePath(path);
        assertEquals("Expected path to normalize: ", expected,actual);
    }
    
    @Test
    public void shouldMatchPath() throws Exception
    {
        assertPathMatch("/a/b/", "/b/", pathServiceA);
        assertPathMatch("/b/c/", "/c/", pathServiceB);
        assertPathMatch("/b/", "/", pathServiceB);
    }
    
    @Test
    public void shouldMatchPathUsingNormalizer() throws Exception
    {
        assertPathMatch(" /a/b   ", "/a/b/" , "/b/", pathServiceA);
        assertPathMatch(" b/c ", "/b/c/", "/c/", pathServiceB);
        assertPathMatch("b/c/", "/b/c/", "/c/", pathServiceB);
    }
    
    public void assertPathMatch(String fullPath, String relativePath, IPSPathService pathService) throws IPSPathService.PSPathNotFoundServiceException {
        assertPathMatch(fullPath, fullPath, relativePath, pathService);
    }
    
    public void assertPathMatch(String fullPath, String properFullPath, String relativePath, IPSPathService pathService) throws IPSPathService.PSPathNotFoundServiceException {
        PathMatch pm = pathMatcher.matchPath(fullPath);
        assertEquals("Full path: ", properFullPath, pm.fullPath);
        assertEquals("Relative path: ", relativePath, pm.relativePath);
        assertSame(pathService, pm.pathService);
    }
    
    
    public static class TestPathService implements IPSPathService {

        public PSPathItem find(String path) throws PSPathNotFoundServiceException, PSPathServiceException
        {
            throw new UnsupportedOperationException("find is not yet supported");
        }
        
        public List<String> getRolesAllowed()
        {
            return null;
        }
        
        public PSItemProperties findItemProperties(String path)
        {
            throw new UnsupportedOperationException("find item properties is not yet supported");
        }
        
        public List<PSPathItem> findChildren(String path) throws PSPathNotFoundServiceException, PSPathServiceException
        {
            throw new UnsupportedOperationException("findChildren is not yet supported");
        }

        public PSPathItem addFolder(String path) throws PSPathNotFoundServiceException, PSPathServiceException
        {
            throw new UnsupportedOperationException("addFolder is not yet supported");
        }
          
        public PSPathItem addNewFolder(String path) throws PSPathNotFoundServiceException, PSPathServiceException
        {
            throw new UnsupportedOperationException("addNewFolder is not yet supported");
        }
        
        public PSPathItem renameFolder(PSRenameFolderItem item) throws PSPathNotFoundServiceException, 
        PSPathServiceException, PSBeanValidationException
        {
            throw new UnsupportedOperationException("renameFolder is not yet supported");
        }
        
        public PSNoContent moveItem(PSMoveFolderItem request)
        {
            throw new UnsupportedOperationException("moveItem is not yet supported");
        }
        
        public int deleteFolder(PSDeleteFolderCriteria criteria) throws PSPathServiceException
        {
            throw new UnsupportedOperationException("deleteFolder is not yet supported");
        }
        
        public String validateFolderDelete(String path) throws PSPathNotFoundServiceException, PSPathServiceException
        {
            throw new UnsupportedOperationException("validateFolderDelete is not yet supported");
        }
        
        public List<PSItemProperties> findItemProperties(PSItemByWfStateRequest request)
        throws PSPathNotFoundServiceException, PSPathServiceException
        {
            throw new UnsupportedOperationException("findItemProperties(PSItemByWfStateRequest) is not yet supported");
        }
        
        public String findLastExistingPath(String path) throws PSPathServiceException
        {
            throw new UnsupportedOperationException("findLastExistingPath is not yet supported");
        }

        public IPSListViewHelper getListViewHelper()
        {
            return null;
        }
    }
}
