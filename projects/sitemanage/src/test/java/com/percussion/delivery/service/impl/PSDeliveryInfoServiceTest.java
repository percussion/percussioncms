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
