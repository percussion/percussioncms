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
package com.percussion.ant.install;

import com.percussion.security.PSEncryptionException;
import com.percussion.security.PSEncryptor;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.utils.io.PathUtils;
import com.percussion.utils.testing.UnitTest;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(UnitTest.class)
public class PSMakeLasagnaTest
{
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private String rxdeploydir;

   /**
    * Constant for the test rxrepository.properties file.
    */
   public static final String TEST_RXREPOSITORY_PROPS_FILE = 
      "/com/percussion/ant/install/test_rxrepository.properties";

    @Before
    public void setUp()
    {
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
   public void testExecute() throws IOException, PSEncryptionException {
       Path rxDir = PathUtils.getRxPath();
       File ret = rxDir.toFile();
       ret.deleteOnExit();

        InputStream is = PSMakeLasagnaTest.class.getResourceAsStream(TEST_RXREPOSITORY_PROPS_FILE);
        File temp =  new File(ret,"rxconfig/Installer/rxrepository.properties");
        temp.deleteOnExit();

        FileUtils.copyInputStreamToFile(is, temp);
        PSMakeLasagna ml;
       ml = new PSMakeLasagna();
       ml.setRoot(rxDir.toString());

      // load the original, un-encrypted password
      Properties repositoryProps = new Properties();
      repositoryProps.load(new FileInputStream(temp));
      String originalPWD = repositoryProps.getProperty(
            PSJdbcDbmsDef.PWD_PROPERTY);
      
      // this will encrypt the password and write it back
      ml.execute();
      
      // load the new, encrypted password
      repositoryProps = new Properties();
      repositoryProps.load(new FileInputStream(temp));
      String newPWD = repositoryProps.getProperty(
            PSJdbcDbmsDef.PWD_PROPERTY);
      
      // verify encryption was successful
      assertFalse(newPWD.equals(originalPWD));
      newPWD = PSEncryptor.decryptString(PathUtils.getRxDir(null).getAbsolutePath().concat(PSEncryptor.SECURE_DIR),newPWD);
      assertTrue(newPWD.equals(originalPWD));
   }
   
}
