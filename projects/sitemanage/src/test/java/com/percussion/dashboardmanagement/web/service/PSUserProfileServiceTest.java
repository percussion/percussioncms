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
