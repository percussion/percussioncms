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

package com.percussion.utils.security;

import com.percussion.delivery.data.PSDeliveryInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PSContentSecurityPolicyUtilsTests {


    @Before
    public void setup(){

    }

    @After
    public void teardown(){

    }

    @Test
    @Ignore //FIXME Test is failing
    public void testEditCSP(){
        List<PSDeliveryInfo> psDeliveryInfoList = new ArrayList<>();
        String contentSecurityString = PSSecurityUtility.CONTENT_SECURITY_POLICY_DEFAULT;

        String edited = PSContentSecurityPolicyUtils.editContentSecurityPolicy(psDeliveryInfoList,contentSecurityString);

        assertNotNull(edited);

        assertEquals(contentSecurityString, edited);

    }
}
