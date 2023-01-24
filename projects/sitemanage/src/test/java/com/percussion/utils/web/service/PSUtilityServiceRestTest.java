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

package com.percussion.utils.web.service;


import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.percussion.share.data.PSMapWrapper;
import com.percussion.share.test.PSRestTestCase;
import com.percussion.utils.service.impl.PSUtilityRestService;

public class PSUtilityServiceRestTest extends PSRestTestCase<PSUtilityRestClient>
{

    private static PSUtilityRestClient utilityTestClient;

    
    protected PSUtilityRestClient getRestClient(String baseUrl)
    {
        return utilityTestClient = new PSUtilityRestClient(baseUrl);
    }
 
    @BeforeClass
    public static void setUp() throws Exception
    {
         
    }

    @Test
    public void encryptDecryptUrlTest()
    {
        String defaultKey = "D6ZX#23GGS$";

        String stringTobeEncrypted = "http://yahoo.com";

        Map<String, String> map = new HashMap<String, String>();
        map.put(PSUtilityRestService.KEY_KEY, defaultKey);
        map.put(PSUtilityRestService.STRING_KEY, stringTobeEncrypted);
        PSMapWrapper mapWrapper = new PSMapWrapper();
        mapWrapper.setEntries(map);

        PSMapWrapper mw = utilityTestClient.encryptString(mapWrapper);
        
        map.clear();
        map.put(PSUtilityRestService.KEY_KEY, defaultKey);
        map.put(PSUtilityRestService.STRING_KEY, mw.getEntries().get(PSUtilityRestService.STRING_KEY));
        mapWrapper = new PSMapWrapper();
        mapWrapper.setEntries(map);

        mw = utilityTestClient.decryptString(mapWrapper);

        assertEquals(stringTobeEncrypted, mw.getEntries().get(PSUtilityRestService.STRING_KEY));

    }
    
}
