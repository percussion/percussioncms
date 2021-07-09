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
package com.percussion.share.dao;

import static org.junit.Assert.*;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.percussion.services.contentmgr.IPSContentMgr;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


/**
 * Scenario description: Test behavior
 * @author adamgent, Oct 6, 2009
 */
@RunWith(JMock.class)
public class PSJcrNodeFinderTest
{

    Mockery context = new JUnit4Mockery();

    PSJcrNodeFinder nodeFinder;

    IPSContentMgr cm;

    @Before
    public void setUp() throws Exception
    {
        
        cm = context.mock(IPSContentMgr.class);
        nodeFinder = new PSJcrNodeFinder(cm, "ct", "sys_title");

    }

    @Test
    public void shouldGetQuery()
    {
        String actual = nodeFinder.getQuery("//folderpath", "my-id");
        String expected = "select rx:sys_contentid, rx:sys_folderid, jcr:path from ct where jcr:path like '//folderpath/%' and rx:sys_title = 'my-id'";
        assertEquals("Jcr query: ", expected, actual);
    }   
    
    @Test
    public void getQuery()
    {
        Map<String, String> whereFields = new TreeMap<String, String>();
        whereFields.put("field1", "value1");
        whereFields.put("field2", "value2");
        String actual = nodeFinder.getQuery("//folderpath", whereFields);
        String expected = "select rx:sys_contentid, rx:sys_folderid, jcr:path from ct where jcr:path like '//folderpath/%'" +
        		" and rx:field1 = 'value1' and rx:field2 = 'value2'";
        assertEquals("Jcr query: ", expected, actual);
        
        actual = nodeFinder.getQuery(null, whereFields);
        expected = "select rx:sys_contentid, rx:sys_folderid, jcr:path from ct where rx:field1 = 'value1'" +
                " and rx:field2 = 'value2'";
        assertEquals("Jcr query: ", expected, actual);
    }   
}

