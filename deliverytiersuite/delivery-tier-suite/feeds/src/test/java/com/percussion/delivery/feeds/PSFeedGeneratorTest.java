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
package com.percussion.delivery.feeds;

import com.percussion.delivery.feeds.data.IPSFeedDescriptor;
import com.percussion.delivery.feeds.data.PSFeedItem;
import junit.framework.TestCase;
import org.apache.commons.validator.routines.InetAddressValidator;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * @author erikserating
 *
 */
public class PSFeedGeneratorTest extends TestCase
{

    private final String PERC_FEEDS_PROPERTIES = "/src/main/java/webapp/WEB-INF/feeds.properties";
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
        
        System.out.println(feed);
        
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



        Properties props1 = new Properties();
        String path = System.getProperty("user.dir");
        InputStream in = new FileInputStream(path + PERC_FEEDS_PROPERTIES);
        props1.load(in);
        String feedsIp = props1.getProperty("rss.feeds.ip");
        if(feedsIp==null || feedsIp.isEmpty()){
            feedsIp=FEEDS_IP_DEFAULT;
        }else{
            feedsIp.trim();
        }

        InetAddressValidator ipValidator = new InetAddressValidator();
        boolean isValidIp = ipValidator.isValid(feedsIp);
        boolean isIPV4Address = false;
        boolean isIPV6Address = false;
        if(isValidIp){
            if(ipValidator.isValidInet4Address(feedsIp)){
                isIPV4Address = true;
            }else if(ipValidator.isValidInet6Address(feedsIp)){
                isIPV6Address = true;
            }else{
                feedsIp = FEEDS_IP_DEFAULT;
            }
        }else{
            feedsIp = FEEDS_IP_DEFAULT;
        }

        PSFeedGenerator generator = new PSFeedGenerator();
        String feed = null;
        feed= generator.makeFeedContent(desc,"www.google.com",items);

    }
    
}
