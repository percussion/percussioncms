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
