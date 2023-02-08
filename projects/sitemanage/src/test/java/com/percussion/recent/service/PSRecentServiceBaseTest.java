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

package com.percussion.recent.service;

import com.percussion.recent.data.PSRecent.RecentType;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.test.PSServletTestCase;

import java.util.List;

import com.percussion.utils.testing.IntegrationTest;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

@Category(IntegrationTest.class)
public class PSRecentServiceBaseTest extends PSServletTestCase
{
    private static final String TESTUSER1 = "testuser1";
    private static final String TESTUSER2 = "testuser2";
    private static final String TESTSITE1 = "testsite1";
    private static final int MAX_TEST_ADD = 100;
    private static final int MAX_ITEMS_SIZE = 40;
   
 
    IPSRecentServiceBase recentService;
  
    @Override
    protected void setUp() throws Exception
    { 
       
        //PSSpringWebApplicationContextUtils.injectDependencies(this);
        // bad need to work out how to get this autowired
        IPSRecentServiceBase bean = (IPSRecentServiceBase)PSSpringWebApplicationContextUtils.getWebApplicationContext().getBean("pSRecentServiceBase", IPSRecentServiceBase.class);
        setRecentService(bean);
        /*
        
        fixture = new PSSiteDataServletTestCaseFixture(request, response);
        fixture.setUp();
        */
        //FB:IJU_SETUP_NO_SUPER NC 1-16-16
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception
    {
       //fixture.tearDown();
    }

    public void testUpdateRecenItems()
    {
     
        
        for (int i=1;i<=MAX_TEST_ADD;i++)
        {
            recentService.addRecent(TESTUSER1, TESTSITE1, RecentType.ITEM, String.valueOf(i));
          
        }
        List<String> returnList = recentService.findRecent(TESTUSER1, TESTSITE1, RecentType.ITEM);
        assertNotNull(returnList);
        
        assertEquals(RecentType.ITEM.MaxSize(),returnList.size());
        
        for (int i=0; i<returnList.size(); i++)
        {
            String value = returnList.get(i);
            // Value should count down from last added value
            int orderValue = MAX_TEST_ADD-i;
           
            assertTrue(String.valueOf(orderValue).equals(value));
        }
        
    }
    
    public IPSRecentServiceBase getRecentService()
    {
        return recentService;
    }

    public void setRecentService(IPSRecentServiceBase recentService)
    {
        this.recentService = recentService;
    }

    
}
