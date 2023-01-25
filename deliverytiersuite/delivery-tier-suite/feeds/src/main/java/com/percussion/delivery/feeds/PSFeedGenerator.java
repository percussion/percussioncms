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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndContentImpl;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedOutput;

/**
 * @author erikserating
 *
 */
public class PSFeedGenerator
{
   public String makeFeedContent(IPSFeedDescriptor desc, String host, List<PSFeedItem> items) throws FeedException
   {
      
      SyndFeed feed = new SyndFeedImpl();
      feed.setFeedType(getFeedType(desc));
      feed.setTitle(desc.getTitle());
      feed.setDescription(desc.getDescription());
      feed.setLink(fixupHost(desc.getLink(), host));
      feed.setPublishedDate(new Date());
      
      // Process each item
      SyndEntry entry;
      SyndContent description;
      List<SyndEntry> entries = new ArrayList<>();
      for(PSFeedItem item : items)
      {
          entry = new SyndEntryImpl();
          entry.setTitle(item.getTitle());
          if(StringUtils.isNotBlank(item.getDescription()))
          {
              description = new SyndContentImpl();
              description.setType("text/html");
              description.setValue(item.getDescription());
              entry.setDescription(description);
          }
          entry.setLink(item.getLink());
          entry.setPublishedDate(item.getPublishDate());
          entries.add(entry);          
      }
      feed.setEntries(entries);
      
      
      SyndFeedOutput output = new SyndFeedOutput();
      return output.outputString(feed);
      
   }
   
    /**
     * Replaces the host name in the link with the supplied host
     * 
     * @param link
     * @param host
     * @return The link
     * 
     * @throws FeedException
     */
    private String fixupHost(String link, String host) throws FeedException
    {
        String curHost = getHost(link);
        return StringUtils.replace(link, curHost, host, 1);
    }
    

    public static String getHost(String link) throws FeedException
    {
        try
        {
            URI uri = new URI(link);
            String host = uri.getHost();
            int port = uri.getPort();
            if (port != -1)
                host += ":" + port;
            return host;
        }
        catch (URISyntaxException e)
        {
            String error = "Failed to parse host from feed descriptor link: " + link; 
            throw new FeedException(error);
        }
    }

/**
    * Helper method to return the proper rome feed type string for the feed type
    * set on the passed in descriptor.
    * @param desc assumed not <code>null</code>.
    * @return the feed type string, never <code>null</code> or empty.
    */
   private String getFeedType(IPSFeedDescriptor desc)
   {
       switch(desc.getType())
       {
           case "ATOM":
               return "atom_1.0";
           case "RSS1":
               return "rss_1.0";
           default:
               return "rss_2.0";
       }
   }
}
