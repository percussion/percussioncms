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
package com.percussion.delivery.feeds;

import com.percussion.delivery.feeds.data.IPSFeedDescriptor;
import com.percussion.delivery.feeds.data.PSFeedItem;
import junit.framework.TestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author erikserating
 *
 */
public class PSFeedGeneratorTest extends TestCase
{

    private static final Logger log = LogManager.getLogger(PSFeedGeneratorTest.class);
    private final String PERC_FEEDS_PROPERTIES = "/feeds.properties";
    private final String FEEDS_IP_DEFAULT="127.0.0.1";
    public void testMakeFeedContent() throws Exception
    {
        PSFeedGenerator generator = new PSFeedGenerator();
        TestDescriptor desc = new TestDescriptor();
        List<PSFeedItem> items = new ArrayList<PSFeedItem>();
        
        desc.setName("Test Feed");
        desc.setTitle("Test Feed");
        desc.setSite("TestSite");
        desc.setLink("http://www.testme.com");
        desc.setFeedType("RSS2");
        desc.setDescription("This is a test feed description.");
        
        PSFeedItem item1 = new PSFeedItem();
        item1.setTitle("First Item");
        item1.setDescription("Item1 description");
        item1.setLink("http://www.google.com");
        item1.setPublishDate(new Date());
        items.add(item1);
        
        PSFeedItem item2 = new PSFeedItem();
        item2.setTitle("Second Item");
        item2.setDescription("Item2 description");
        item2.setLink("http://www.google.com");
        item2.setPublishDate(new Date());
        items.add(item2);
        
        PSFeedItem item3 = new PSFeedItem();
        item3.setTitle("Third Item");
        item3.setDescription("Item3 description");
        item3.setLink("http://www.google.com");
        item3.setPublishDate(new Date());
        items.add(item3);
        
        String feed = generator.makeFeedContent(desc, "www.google.com", items);
        
        log.info(feed);
        
    }  
    
    
    
    
    
    class TestDescriptor implements IPSFeedDescriptor
    {
        
        private String title;
        private String description;
        private String feedType;
        private String link;
        private String name;
        private String site;
        
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
            return feedType;
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
            return null;
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

        /**
         * @param title the title to set
         */
        public void setTitle(String title)
        {
            this.title = title;
        }

        /**
         * @param description the description to set
         */
        public void setDescription(String description)
        {
            this.description = description;
        }

        /**
         * @param feedType the feedType to set
         */
        public void setFeedType(String feedType)
        {
            this.feedType = feedType;
        }

        /**
         * @param link the link to set
         */
        public void setLink(String link)
        {
            this.link = link;
        }

        /**
         * @param name the name to set
         */
        public void setName(String name)
        {
            this.name = name;
        }

        /**
         * @param site the site to set
         */
        public void setSite(String site)
        {
            this.site = site;
        }
        
        
        
    }

    //TODO: This test doesn't actually validate ip4 vs ipv6
    public void testGenerateFeedHandlingIPV4IPV6() throws Exception{
        TestDescriptor desc = new TestDescriptor();
        desc.setName("Test Feed");
        desc.setTitle("Test Feed");
        desc.setSite("TestSite");
        desc.setLink("http://www.testme.com");
        desc.setFeedType("RSS2");
        desc.setDescription("This is a test feed description.");

        List<PSFeedItem> items = new ArrayList<PSFeedItem>();

        desc.setName("Test Feed");
        desc.setTitle("Test Feed");
        desc.setSite("TestSite");
        desc.setLink("http://www.testme.com");
        desc.setFeedType("RSS2");
        desc.setDescription("This is a test feed description.");

        PSFeedItem item1 = new PSFeedItem();
        item1.setTitle("First Item");
        item1.setDescription("Item1 description");
        item1.setLink("http://www.google.com");
        item1.setPublishDate(new Date());
        items.add(item1);

        PSFeedItem item2 = new PSFeedItem();
        item2.setTitle("Second Item");
        item2.setDescription("Item2 description");
        item2.setLink("http://www.google.com");
        item2.setPublishDate(new Date());
        items.add(item2);

        PSFeedItem item3 = new PSFeedItem();
        item3.setTitle("Third Item");
        item3.setDescription("Item3 description");
        item3.setLink("http://www.google.com");
        item3.setPublishDate(new Date());
        items.add(item3);



        PSFeedGenerator generator = new PSFeedGenerator();

        String feed = null;
        feed= generator.makeFeedContent(desc,"www.google.com",items);

    }
    
}
