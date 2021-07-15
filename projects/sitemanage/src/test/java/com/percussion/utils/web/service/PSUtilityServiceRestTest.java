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
