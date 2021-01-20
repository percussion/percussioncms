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

package com.percussion.utils.service;

import static org.junit.Assert.assertEquals;

import com.percussion.share.dao.impl.PSLegacyExceptionUtils;
import com.percussion.utils.security.PSEncryptor;
import com.percussion.utils.security.deprecated.PSLegacyEncrypter;
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
        rxdeploydir = System.getProperty("rxdeploydir");
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
        String defaultKey = PSLegacyEncrypter.DEFAULT_KEY();

        String stringTobeEncrypted = "http://www.yahoo.com";

        PSUtilityService service = new PSUtilityService();

        String encryptedString = service.encryptString(stringTobeEncrypted, defaultKey);

        String decryptedString = service.decryptString(encryptedString, defaultKey);
        assertEquals(stringTobeEncrypted, decryptedString);

    }
}
