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

