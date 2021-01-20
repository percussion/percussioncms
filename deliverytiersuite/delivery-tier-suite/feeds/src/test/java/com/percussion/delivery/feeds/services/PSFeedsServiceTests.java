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

package com.percussion.delivery.feeds.services;

import com.percussion.delivery.feeds.data.PSFeedDTO;
import com.percussion.delivery.feeds.services.rdbms.PSFeedDao;
import com.percussion.delivery.utils.security.PSHttpClient;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations =
{"classpath:test-beans.xml"})
public class PSFeedsServiceTests extends TestCase{

    @Autowired
    private PSFeedDao feedsDao;
    
    @Autowired
    private PSHttpClient httpClient;
  
    public void PSFeedsServiceTest(){
    //noop
    }
    
    @Test
    public void testExternalRSSFeed() throws UnsupportedEncodingException{
    	
    	PSFeedService svc = new PSFeedService(feedsDao,httpClient);

        PSFeedDTO feedDTO = new PSFeedDTO();
        feedDTO.setFeedsUrl("https://oakland.joinhandshake.com/external_feeds/1681/public.rss?token=RQNKyKoIGu6o4PcV2i5v9PS_WalEKDDfATa3Nj-CUFnK-bBCH3zmKQ");
        String xml = svc.readExternalFeed(feedDTO,"percId");
    	
    	Assert.hasText(xml);

        String url = "http://oakland.imodules.com/controls/cms_v2/components/rss/rss.aspx?sid=1001&gid=1001&calcid=2571&page_id=1209";
        PSFeedDTO feedDTO1 = new PSFeedDTO();
        feedDTO1.setFeedsUrl(URLEncoder.encode(url,"UTF8"));

        xml = svc.readExternalFeed(feedDTO1,"percId");
    	
    	Assert.hasText(xml);
    	
    	
    	
    }
    
    
    
}
