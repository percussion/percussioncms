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
package com.percussion.share.test;

import java.io.InputStream;
import java.util.Properties;

import com.percussion.utils.testing.IntegrationTest;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public abstract class PSRestTestCase<REST_CLIENT extends PSObjectRestClient> {
    public static String baseUrl;
    protected REST_CLIENT restClient;
    
    @BeforeClass
    public static void loadProperties() throws Exception {
        if (baseUrl == null) {
            Properties cactusProps = new Properties();
            InputStream stream = PSRestTestCase.class.getResourceAsStream("/cactus.properties");
            if (stream == null) throw new RuntimeException("Cannot find cactus.properties");
            cactusProps.load(stream);
            baseUrl = cactusProps.getProperty("cactus.contextURL");
        }
    }
    
    protected abstract REST_CLIENT getRestClient(String baseUrl);
    
    @Before
    public void setupClient() throws Exception {
        restClient = getRestClient(baseUrl);
        setupClient(restClient);
    }
    
    public static void setupClient(PSObjectRestClient restClient) throws Exception {        
        setupClient(restClient, "admin1", EI_ADMIN_COMMUNITYID);
    }
    
    public static void setupClient(PSObjectRestClient restClient, String userName, int communityId) throws Exception {
        loadProperties();
        restClient.setUrl(baseUrl);
        restClient.getRequestHeaders().put("Accept", "text/xml");
        restClient.login(userName, "demo");
        restClient.switchCommunity(communityId);
    }
    
    public static int EI_ADMIN_COMMUNITYID = 1001;
    public static int EI_COMMUNITYID = 1002;
}
