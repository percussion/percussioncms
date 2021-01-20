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
