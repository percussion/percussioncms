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
package com.percussion.share.dao;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static com.percussion.share.dao.PSFolderPathUtils.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.percussion.share.data.IPSFolderPath;
import com.percussion.share.data.IPSItemSummary;


public class PSFolderPathUtilsTest
{
    private String a = "//a";
    private String ab = "//a/b";
    private String abc = "//a/b/c";
    private String abTrick = "//ab";
    private String b = "//b";
    private String actual;
    private String expected;
    private String ext = ".jpg";
    private IPSItemSummary mockItemSummary = new ItemSummary();
    private List<String> folderPaths = new ArrayList<String>();
    
    private void assertResults() {
        assertEquals("Strings should equal", expected, actual);
    }
    
    @Test
    public void testConcatPath() throws Exception
    {
        expected = abc;
        actual = concatPath("//a", "b", "c");
        assertResults();
    }

    @Test
    public void testReplaceInvalidCharacters() throws Exception
    {
        actual = replaceInvalidItemNameCharacters("stuff/called/poop?");
        expected = "stuff-called-poop-";
        assertResults();
        
        actual = replaceInvalidItemNameCharacters("stuff.jpg");
        expected = "stuff.jpg";
        assertResults();
    }
    
    @Test
    public void testGetName() throws Exception
    {
        expected = "a";
        actual = getName(a);
        assertResults();

        expected = "b";
        actual = getName(ab);
        assertResults();
        
        expected = "ab";
        actual = getName(abTrick);
        assertResults();
    }
    
    @Test
    public void testGetBaseName() throws Exception
    {
        expected = "a";
        actual = getBaseName(a + ext);
        assertResults();

        expected = "b";
        actual = getBaseName(ab + ext);
        assertResults();
        
        expected = "ab";
        actual = getBaseName(abTrick);
        assertResults();
    }
    

    @Test
    public void testAddEnumeration() throws Exception
    {
        expected = a + numberName(1);
        actual = addEnumeration(a, 1);
        assertResults();

        expected = ab + numberName(1) + ext;
        actual = addEnumeration(ab + ext, 1);
        assertResults();
        
        expected = "//a/b/c-1.txt";
        actual = addEnumeration(abc + ".txt", 1);
        assertResults();
        
    }
    
    
    @Test
    public void testMatchingDescedentPaths() throws Exception
    {
     
        List<String> actual = matchingDescedentPaths(a, asList(a,ab,abc,b,abTrick));
        assertEquals(asList(a,ab,abc), actual);
    }
    @Test
    public void testResolveFolderPath() throws Exception
    {
        folderPaths = asList(b,a,abc);
        expected = a;
        actual = resolveFolderPath(mockItemSummary, fp(a),fp(ab));
        assertResults();
        
        folderPaths = asList(b,abc);
        expected = abc;
        actual = resolveFolderPath(mockItemSummary, fp(a),fp(ab));
        assertResults();
    }
    
    private FolderPath fp(String fp) {
        return new FolderPath(fp);
    }
    
    @Test
    public void testIsDescedentPath() throws Exception
    {
        assertTrue(isDescedentPath(ab, a));
        assertFalse(isDescedentPath(abTrick, a));
        assertFalse(isDescedentPath(abTrick, ab));
        assertFalse(isDescedentPath(ab, abTrick));
    }
    @Test(expected=IllegalArgumentException.class)
    public void testValidatePath() throws Exception
    {
        validatePath("asdfasdf");
    }
    
    @Test
    public void testParentPath() throws Exception
    {
        expected = "//";
        actual = parentPath(a);
        assertResults();
        
        expected = a;
        actual = parentPath(ab);
        assertResults();
        
        expected = ab;
        actual = parentPath(abc);
        assertResults();
        
    }
    
    public class ItemSummary extends MockItemSummary {
    
        @Override
        public List<String> getFolderPaths()
        {
            return folderPaths;
        }
    
    }
    
    
    public class FolderPath implements IPSFolderPath {

        protected String folderPath;

        
        public FolderPath(String folderPath)
        {
            super();
            this.folderPath = folderPath;
        }

        public String getFolderPath()
        {
            return folderPath;
        }

        public void setFolderPath(String folderPath)
        {
            this.folderPath = folderPath;
        }
        
        
    }

}

