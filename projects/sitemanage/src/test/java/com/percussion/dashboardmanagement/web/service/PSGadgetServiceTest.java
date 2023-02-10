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
