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

package com.percussion.utils.service;

import static org.junit.Assert.assertEquals;

import com.percussion.security.PSEncryptor;
import com.percussion.legacy.security.deprecated.PSLegacyEncrypter;
import com.percussion.utils.service.impl.PSUtilityService;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class PSUtilityserviceTest
{

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private String rxdeploydir;

    @Before
    public void setup(){
        this.rxdeploydir = System.getProperty("rxdeploydir");
        System.setProperty("rxdeploydir",temporaryFolder.getRoot().getAbsolutePath());
    }

    @After
    public void teardown(){
        //Reset the deploy dir property if it was set prior to test
        if(rxdeploydir != null)
            System.setProperty("rxdeploydir",rxdeploydir);
    }


    @Test
    public void encryptDecryptStringTest()
    {
        String defaultKey = PSLegacyEncrypter.getInstance(
                temporaryFolder.getRoot().getAbsolutePath().concat(PSEncryptor.SECURE_DIR)
        ).DEFAULT_KEY();

        String stringTobeEncrypted = "http://www.yahoo.com";

        PSUtilityService service = new PSUtilityService();

        String encryptedString = service.encryptString(stringTobeEncrypted, defaultKey);

        String decryptedString = service.decryptString(encryptedString, defaultKey);
        assertEquals(stringTobeEncrypted, decryptedString);

    }
}
