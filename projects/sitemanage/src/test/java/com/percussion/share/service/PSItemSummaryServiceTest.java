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
