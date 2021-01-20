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
package com.percussion.dashboardmanagement.web.service;

import static org.junit.Assert.assertNotNull;

import com.percussion.dashboardmanagement.data.PSGadget;
import com.percussion.share.test.PSDataServiceRestClient;
import com.percussion.share.test.PSRestTestCase;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

@Ignore("SUT are not used")
public class PSGadgetServiceTest extends PSRestTestCase<PSGadgetServiceTest.GadgetRestClient> {
    
    protected GadgetRestClient restClient;

    @Test
    public void testSave() throws Exception {
//        PSGadget gadget = new PSGadget();
//        gadget.setName("Gadget ABC");
//        PSGadget actual = restClient.save(gadget);
//    	System.out.println("PSGadgetServiceTest.testSave().actual = " + actual.getName());
//        assertNotNull(actual);
//        assertEquals(gadget.getName(), actual.getName());
    }

    @Test
    public void testFind() throws Exception {
        PSGadget gadget = restClient.find("horoscope");
        assertNotNull(gadget);
    //    assertEquals(gadget.getName(), "horoscope");
    }
    
    @Test
    public void testFindAll() throws Exception {
        List<PSGadget> gadgets = restClient.findAll();
        assertNotNull(gadgets);
    }
    
    @Test
    public void testDelete() throws Exception {
        PSGadget gadget2 = new PSGadget();
        assertNotNull(gadget2);
//        restClient.delete("horoscope");
    }
    
    @Override
    protected GadgetRestClient getRestClient(String baseUrl) {
        restClient = new GadgetRestClient(baseUrl);
        return restClient;
    }
    
    
    public static class GadgetRestClient extends PSDataServiceRestClient<PSGadget> {
        public GadgetRestClient(String url) {
            super(PSGadget.class, url, "/Rhythmyx/services/dashboardmanagement/gadget/");
        }        
        public PSGadget find(String id) {
            return get(id);
        }  
        public List<PSGadget> findAll() {
            return getAll();
        }  
        public PSGadget getDefaultProfile() {
            return getObjectFromPath(getPath() + "default");
        }
    }

}
