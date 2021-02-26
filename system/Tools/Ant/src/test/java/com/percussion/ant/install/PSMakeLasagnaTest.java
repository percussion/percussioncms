/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
      newPWD = PSEncryptor.getInstance("AES",
              PathUtils.getRxDir().getAbsolutePath().concat(PSEncryptor.SECURE_DIR)
      ).decrypt(newPWD);
      assertTrue(newPWD.equals(originalPWD));
   }
   
}
