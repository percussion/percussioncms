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

package com.percussion.recent.web.service;

import com.percussion.share.test.fixtures.PSRestFixtures;
import com.percussion.test.PSRestClientTestCase;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertTrue;

//TODO: Implement Me
@Category(IntegrationTest.class)
public class PSRecentRestServiceTest extends PSRestClientTestCase
{

    PSRestFixtures fixtures;
    /*   
    public PSRecentRestServiceTest(String testName)
    {
        super(testName);
        
    }
*/
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        //  need to update jackson/to get json unwrap working
        // fixtures = new PSRestFixtures(c,r);
        //fixtures.createSite();
    }

    @Test
    public void _testMyResource()
    {
        assertTrue(true);
        /*
        MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
        formData.add("value", "16777215-101-708");
        
        WebResource wr = r.path("services/recentmanagement/recent/item")
                .path("test1");

        ClientResponse response = getBuilder(wr)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(ClientResponse.class, formData);
        
        assertEquals(Status.NO_CONTENT.getStatusCode(), response.getStatus());
        */
    }

    @Test
    public void _testMyResource2()
    {
        assertTrue(true);
        /*
        MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
        formData.add("value", "16777215-101-708");
        
        
        WebResource wr = r.path("services/recentmanagement/recent/item")
                .path("16777215-101-708");

        ClientResponse response = getBuilder(wr)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(ClientResponse.class, formData);
        
        assertEquals(Status.NO_CONTENT.getStatusCode(), response.getStatus());

        */
    }
    
    @Test
    public void testToBeCompleted()
    {
        assertTrue(true);
    }
    
}
