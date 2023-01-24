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
package com.percussion.delivery.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.percussion.delivery.data.PSDeliveryInfo;
import com.percussion.delivery.service.IPSDeliveryInfoService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;

public class PSDeliveryInfoServiceTest
{
    @Ignore
    @Test
    public void testAvailableServices() throws Exception
    {
        File tempConfigFile = createTempConfigFileBasedOn(this.getClass().getResourceAsStream("DeliveryServerConfigTest_AvailableServices.xml"));
        
        PSDeliveryInfo server;
        IPSDeliveryInfoService deliveryService = new PSDeliveryInfoService(tempConfigFile);
        
        server = deliveryService.findByService(PSDeliveryInfo.SERVICE_COMMENTS);
        assertTrue(server != null);

        server = deliveryService.findByService(PSDeliveryInfo.SERVICE_INDEXER);
        assertTrue(server != null);
        
        server = deliveryService.findByService(PSDeliveryInfo.SERVICE_FORMS);
        assertTrue(server != null);
    }
    
    @Ignore
    @Test
    public void testAvailableServicesServersInfo() throws Exception
    {
        File tempConfigFile = createTempConfigFileBasedOn(this.getClass().getResourceAsStream("DeliveryServerConfigTest_AvailableServices.xml"));
        
        PSDeliveryInfo server;
        IPSDeliveryInfoService deliveryService = new PSDeliveryInfoService(tempConfigFile);
        
        server = deliveryService.findByService(PSDeliveryInfo.SERVICE_COMMENTS);
        
        assertEquals(server.getUsername(), "admin2");

        server = deliveryService.findByService(PSDeliveryInfo.SERVICE_INDEXER);
        
        assertEquals(server.getUsername(), "admin2");
        
        server = deliveryService.findByService(PSDeliveryInfo.SERVICE_FORMS);
        assertEquals(server.getUsername(), "admin1");
    }
    
    private File createTempConfigFileBasedOn(InputStream baseConfigFile) throws Exception
    {
        // Copy mixed passwords to temp directory
        File tempConfigFile = File.createTempFile("deliveryServers", ".xml");
        OutputStream out = new FileOutputStream(tempConfigFile);
        InputStream in = baseConfigFile;
        
        IOUtils.copy(in, out);
        
        return tempConfigFile;
    }
}
