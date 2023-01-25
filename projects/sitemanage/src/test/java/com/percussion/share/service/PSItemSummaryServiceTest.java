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
package com.percussion.share.service;

import static org.junit.Assert.*;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.percussion.fastforward.managednav.IPSManagedNavService;
import com.percussion.share.dao.impl.PSItemSummaryService;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.content.IPSContentDesignWs;
import com.percussion.webservices.content.IPSContentWs;


/**
 * Scenario description: 
 * ItemSummaryService 
 * @author adamgent, Oct 2, 2009
 */
@RunWith(JMock.class)
public class PSItemSummaryServiceTest
{

    Mockery context = new JUnit4Mockery();

    PSItemSummaryService sut;

    IPSContentWs collaborator;
    IPSIdMapper idMapper;
    IPSContentDesignWs contentDs;
    IPSGuid guid;
    TestIconPSItemSummaryService testIconService;
    IPSManagedNavService navService;

    @Before
    public void setUp() throws Exception
    {
        collaborator = context.mock(IPSContentWs.class);
        guid = context.mock(IPSGuid.class);
        idMapper = context.mock(IPSIdMapper.class);
        contentDs = context.mock(IPSContentDesignWs.class);
        navService = context.mock(IPSManagedNavService.class);
        sut = new PSItemSummaryService(collaborator,null,idMapper,navService);
        testIconService = new TestIconPSItemSummaryService();
    }

    @Test(expected=IllegalArgumentException.class)
    public void shouldFailForNullPath() throws Exception
    {

        assertNotNull(sut.pathToId(null));
        
    }
    
    
    @Test
    public void shouldFixIconPath() throws Exception
    {
        testIconService.setExpectedIconPath("../rx_resources/stuff/image.png");
        String id = "doesn'tmatter";
        String path = testIconService.getIcon(id);
        assertEquals("path should be corrected.", "/Rhythmyx/rx_resources/stuff/image.png", path);
    }
    
    @Test
    public void shouldNotFixIconPathIfSystemPathIsNull() throws Exception
    {
        testIconService.setExpectedIconPath(null);
        String id = "doesn'tmatter";
        String path = testIconService.getIcon(id);
        assertEquals("path should not be corrected as it null.", null, path);
    }
    
    
    
    public class TestIconPSItemSummaryService extends PSItemSummaryService {

        private String expectedIconPath;
        
        public TestIconPSItemSummaryService()
        {
            super(null, null, null, null);
        }

        
        @Override
        protected String getIcon(String id)
        {
            return super.getIcon(id);
        }


        @Override
        protected String getIconFromSystem(String id)
        {
            return getExpectedIconPath();
        }

        public String getExpectedIconPath()
        {
            return expectedIconPath;
        }

        public void setExpectedIconPath(String expectedIconPath)
        {
            this.expectedIconPath = expectedIconPath;
        }
    
    } 
    
    
    
}
