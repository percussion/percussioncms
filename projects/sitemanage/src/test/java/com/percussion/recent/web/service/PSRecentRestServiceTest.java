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
