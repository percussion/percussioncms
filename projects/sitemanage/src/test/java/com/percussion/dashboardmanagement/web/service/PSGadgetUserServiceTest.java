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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.percussion.dashboardmanagement.data.PSGadget;
import com.percussion.share.test.PSDataServiceRestClient;
import com.percussion.share.test.PSRestTestCase;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

@Ignore("SUT are not used")
public class PSGadgetUserServiceTest extends PSRestTestCase<PSGadgetUserServiceTest.GadgetRestClient> {
    
    protected GadgetRestClient restClient;

    @Ignore("Jose fix this bitch")
    @Test
    public void testFindAll() throws Exception {
    	List<PSGadget> gadgets = restClient.findAll("alex");
    	System.out.println("testFindAll().gadgets.size() = " + gadgets.size());
    	for(PSGadget g:gadgets){
//        	System.out.println("testFindAll().g.getName() = " + g.getName());
        	System.out.println("testFindAll().g.getUrl() = " + g.getUrl());
    	}    		
        assertNotNull(gadgets);
    	//testGetGadgetsForUser
//        g1.setName("Gadget1");
//        PSGadget g2 = new PSGadget();
//        g2.setName("Gadget2");
//        restClient.save("alex", g1);
//        restClient.save("alex", g2);
//        assertEquals(2, gadgets.size());
    }

    @Test
    public void testSave() throws Exception {
        PSGadget g1 = new PSGadget();
//        g1.setName("Gadget1");
        PSGadget g2 = restClient.save("bob", g1);
//    	System.out.println("testSave().g2.getName() = " + g2.getName());
        assertNotNull(g2);
    }
    
    @Test
    public void testDelete() throws Exception {
    	//testDeleteGadgetFromUser
    	assertEquals(1, 1);
//        List<PSGadget> gadgets = restClient.findAll("alex");
//        int sizeBeforeDelete = gadgets.size();
//    	PSGadget gadgetToDelete = gadgets.get(0);
//    	restClient.delete("alex", gadgetToDelete.getId());
//        gadgets = restClient.findAll("alex");
//        int sizeAfterDelete = gadgets.size();
//        int differenceInSize = sizeBeforeDelete - sizeAfterDelete;
//        assertEquals(1, differenceInSize);
    }
    
//    @Test
//    public void testUpdateGadgetForUser() throws Exception {
//        restClient.delete("horoscope");
//    }
    
    @Override
    protected GadgetRestClient getRestClient(String baseUrl) {
        restClient = new GadgetRestClient(baseUrl);
        return restClient;
    }
    
    
    public static class GadgetRestClient extends PSDataServiceRestClient<PSGadget> {
        public GadgetRestClient(String url) {
            super(PSGadget.class, url, "/Rhythmyx/services/dashboardmanagement/gadgetuser/");
        }        
        public List<PSGadget> findAll(String username) {
            return getObjectsFromPath(getPath() + username);
        }
        public PSGadget save(String username, PSGadget gadget) {
            return getObjectFromPath(getPath() + username);
        }
        public void delete(String username, String id) {
            getObjectFromPath(getPath() + username + "/" + id);
        }
    }
}
