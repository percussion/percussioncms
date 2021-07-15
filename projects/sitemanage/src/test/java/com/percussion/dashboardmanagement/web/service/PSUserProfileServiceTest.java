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
package com.percussion.dashboardmanagement.web.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.percussion.dashboardmanagement.data.PSUserProfile;
import com.percussion.share.test.PSDataServiceRestClient;
import com.percussion.share.test.PSRestTestCase;

import org.junit.Ignore;
import org.junit.Test;

@Ignore("SUT are not used")
public class PSUserProfileServiceTest extends PSRestTestCase<PSUserProfileServiceTest.UserProfileRestClient> {
    

    @Test
    public void testGet() throws Exception {
        PSUserProfile userProfile = restClient.get("test");
        assertNotNull(userProfile);
        assertEquals("test", userProfile.getUserName());
    }
    
    @Test
    public void testSave() throws Exception {
        PSUserProfile userProfile = new PSUserProfile();
        userProfile.setUserName("test2");
        PSUserProfile actual = restClient.save(userProfile);
        assertNotNull(actual);
        assertEquals(userProfile.getUserName(), actual.getUserName());
    }
    
    @Override
    protected UserProfileRestClient getRestClient(String baseUrl) {
        restClient = new UserProfileRestClient(baseUrl);
        return restClient;
    }
    
    
    public static class UserProfileRestClient extends PSDataServiceRestClient<PSUserProfile> {
        public UserProfileRestClient(String url) {
            super(PSUserProfile.class, url, "/Rhythmyx/services/dashboardmanagement/userprofile/");
        }
        
        @Override
        protected String getGetPath(String id) {
            return getPath() + "user/" + id;
        }  
        
    }

}
