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

import com.percussion.utils.security.PSEncryptionException;
import com.percussion.utils.security.PSEncryptor;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations =
{"classpath:test-beans.xml"})
public class PSFeedsServiceTests{

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private String rxdeploydir;

    @Before
    public void setup() throws IOException {

        rxdeploydir = System.getProperty("rxdeploydir");
        System.setProperty("rxdeploydir", temporaryFolder.getRoot().getAbsolutePath());
    }

    @After
    public void teardown(){
        if(rxdeploydir != null)
            System.setProperty("rxdeploydir",rxdeploydir);
    }



    @Autowired
    private IPSFeedDao feedsDao;
    
    @Autowired
    private PSHttpClient httpClient;
  
    public void PSFeedsServiceTest(){
    //noop
    }
    
    @Test
    public void testExternalRSSFeed() throws UnsupportedEncodingException, PSEncryptionException {
    	
    	PSFeedService svc = new PSFeedService(feedsDao,httpClient);

        PSFeedDTO feedDTO = new PSFeedDTO();

        String url = PSEncryptor.getInstance().encrypt("https://www.nasa.gov/rss/dyn/breaking_news.rss");

        feedDTO.setFeedsUrl(url);
        String xml = svc.readExternalFeed(feedDTO);
    	System.out.println(xml);
    	assertTrue(xml != null);
    	assertTrue(xml.toLowerCase().contains("nasa"));
    	
    }
    
    
    
}
