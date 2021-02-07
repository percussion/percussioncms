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
package com.percussion.delivery.feeds.services.rdbms;

import com.percussion.delivery.feeds.data.IPSFeedDescriptor;
import com.percussion.delivery.feeds.services.IPSConnectionInfo;
import com.percussion.delivery.feeds.services.IPSFeedDao;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * @author erikserating
 *
 */


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-beans.xml"})
public class PSFeedDaoTest extends TestCase
{
    @Autowired
    private IPSFeedDao dao;
            
    @Test
    public void testSaveLoadConnectionInfo() throws Exception
    {        
        //Test retrieve no info
        IPSConnectionInfo info = dao.getConnectionInfo();
        assertNull(info);
        
        //Test initial save and get
        dao.saveConnectionInfo("http://localhost:9980", "testuser", "demo", false);

        info = dao.getConnectionInfo();
        assertEquals("http://localhost:9980", info.getUrl());
        assertEquals("testuser", info.getUsername());
        assertEquals("demo", info.getPassword());
        assertEquals("false", info.getEncrypted());
        
        //Test updated save and get
        dao.saveConnectionInfo("http://devmachine:9980", "testuser1", "demo1", true);
        info = dao.getConnectionInfo();
        assertEquals("http://devmachine:9980", info.getUrl());
        assertEquals("testuser1", info.getUsername());
        assertEquals("demo1", info.getPassword());
        assertEquals("true", info.getEncrypted());
        
    }
    
    @Test
    public void testSaveLoadDescriptors() throws Exception
    {
        IPSFeedDescriptor desc1 = new TestDescriptor("test1", "site1", "test description", "http://www.testme.com", "Test 1", "{}", "RSS2");
        IPSFeedDescriptor desc2 = new TestDescriptor("test2", "site1", "test description2", "http://www.testme2.com", "Test 2", "{}", "RSS1");
        IPSFeedDescriptor desc3 = new TestDescriptor("test3", "site2", "test description3", "http://www.testme3.com", "Test 3", "{}", "ATOM");
        
        List<IPSFeedDescriptor> descs = new ArrayList<IPSFeedDescriptor>();
        descs.add(desc1);
        descs.add(desc2);
        descs.add(desc3);
        
        // Test saving descriptors
        dao.saveDescriptors(descs);
        IPSFeedDescriptor result = dao.find("test2", "site1");
        assertNotNull(result);
        assertEquals("test description2", result.getDescription());
        result = dao.find("test3", "site2");
        assertNotNull(result);
        assertEquals("test description3", result.getDescription());
        
        //Test findAll
        List<IPSFeedDescriptor> all = dao.findAll();
        assertEquals(3, all.size());
        
        //Test findBySite
        List<IPSFeedDescriptor> bysite1 = dao.findBySite("site1");
        List<IPSFeedDescriptor> bysite2 = dao.findBySite("site2");
        assertEquals(2, bysite1.size());
        assertEquals(1, bysite2.size());
        
        List<IPSFeedDescriptor> deletes = new ArrayList<IPSFeedDescriptor>();
        deletes.add(desc1);
        deletes.add(desc3);
        dao.deleteDescriptors(deletes);
        // Desc 2 should still exist
        result = dao.find("test2", "site1");
        assertNotNull(result);
        assertEquals("test description2", result.getDescription());
        // Desc 1 and 3 should not exist
        result = dao.find("test1", "site1");
        assertNull(result);
        result = dao.find("test3", "site2");
        assertNull(result);
        
        //Test updates and add
        List<IPSFeedDescriptor> updates = new ArrayList<IPSFeedDescriptor>();
        IPSFeedDescriptor desc1_a = new TestDescriptor("test4", "site1", "test description4", "http://www.testme4.com", "Test 4", "{}", "RSS2");
        IPSFeedDescriptor desc2_a = new TestDescriptor("test2", "site1", "test description5", "http://www.testme5.com", "Test 5", "{}", "RSS1");
        updates.add(desc1_a);
        updates.add(desc2_a);
        dao.saveDescriptors(updates);
        
        result = dao.find("test4", "site1");
        assertNotNull(result);
        assertEquals("test description4", result.getDescription());
        
        result = dao.find("test2", "site1");
        assertNotNull(result);
        assertEquals("test description5", result.getDescription());
        
    }
    
    
    class TestDescriptor implements IPSFeedDescriptor
    {

        private String name;
        private String site;
        private String description;
        private String link;
        private String title;
        private String query;
        private String type;
        
        
        
        
        
        /**
         * @param name
         * @param site
         * @param description
         * @param link
         * @param title
         * @param query
         * @param type
         */
        public TestDescriptor(String name, String site, String description, String link, String title, String query,
                              String type)
        {
            this.name = name;
            this.site = site;
            this.description = description;
            this.link = link;
            this.title = title;
            this.query = query;
            this.type = type;
        }

        /* (non-Javadoc)
         * @see com.percussion.feeds.data.IPSFeedDescriptor#getDescription()
         */
        public String getDescription()
        {
            return description;
        }

        /* (non-Javadoc)
         * @see com.percussion.feeds.data.IPSFeedDescriptor#getFeedType()
         */
        public String getType()
        {
            return type;
        }

        /* (non-Javadoc)
         * @see com.percussion.feeds.data.IPSFeedDescriptor#getLink()
         */
        public String getLink()
        {
            return link;
        }

        /* (non-Javadoc)
         * @see com.percussion.feeds.data.IPSFeedDescriptor#getName()
         */
        public String getName()
        {
            return name;
        }

        /* (non-Javadoc)
         * @see com.percussion.feeds.data.IPSFeedDescriptor#getQuery()
         */
        public String getQuery()
        {
            return query;
        }

        /* (non-Javadoc)
         * @see com.percussion.feeds.data.IPSFeedDescriptor#getSite()
         */
        public String getSite()
        {
            return site;
        }

        /* (non-Javadoc)
         * @see com.percussion.feeds.data.IPSFeedDescriptor#getTitle()
         */
        public String getTitle()
        {
            return title;
        }
        
    }
}
