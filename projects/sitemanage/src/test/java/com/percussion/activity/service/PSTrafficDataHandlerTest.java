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
package com.percussion.activity.service;

import static org.junit.Assert.assertEquals;

import com.percussion.activity.data.PSContentTraffic;
import com.percussion.activity.data.PSContentTrafficRequest;
import com.percussion.activity.service.impl.PSTrafficDataHandler;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * @author luisteixeira
 */
public class PSTrafficDataHandlerTest
{
    @Test
    @SuppressWarnings("unchecked")
    public void testGetData() throws Exception
    {
        PSTrafficDataHandler handler = new PSTrafficDataHandler();
        handler.setFile("src/test/resources/activity/Traffic.xml");
        
        PSContentTrafficRequest properties = new PSContentTrafficRequest();
        properties.setStartDate("10/10/2010");
        properties.setEndDate("10/14/2010");
        properties.setGranularity("DAY");
        properties.setPath("//Sites/site1");
        properties.setUsage("uniquepageviews");
        List<String> trafficRequested = new ArrayList<String>();
        trafficRequested.add("VISITS");
        trafficRequested.add("NEW_PAGES");
        trafficRequested.add("UPDATED_PAGES");
        trafficRequested.add("TAKE_DOWNS");
        trafficRequested.add("LIVE_PAGES");
        properties.setTrafficRequested(trafficRequested);
                
        PSContentTraffic response = handler.getContentTraffic(properties);
        assertEquals(5, response.getDates().size());
        assertEquals(5, response.getLivePages().size());
        assertEquals(5, response.getNewPages().size());
        assertEquals(5, response.getPageUpdates().size());
        assertEquals(5, response.getTakeDowns().size());
        assertEquals(5, response.getVisits().size());
        assertEquals("10/14/2010", response.getEndDate());
    }

}
