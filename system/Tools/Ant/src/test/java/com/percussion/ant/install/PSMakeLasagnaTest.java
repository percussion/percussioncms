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
package com.percussion.ant.install;

import com.percussion.server.PSServer;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.utils.security.PSEncryptionException;
import com.percussion.utils.security.PSEncryptor;
import com.percussion.utils.security.deprecated.PSLegacyEncrypter;
import com.percussion.utils.testing.UnitTest;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

@Category(UnitTest.class)
public class PSMakeLasagnaTest extends TestCase
{
   /**
    * Constant for the test rxrepository.properties file.
    */
   public static final String TEST_RXREPOSITORY_PROPS_FILE = 
      "/com/percussion/ant/install/test_rxrepository.properties";
   
   public void testExecute() throws IOException, PSEncryptionException {
       Path rxDir = Files.createTempDirectory("test");
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
      newPWD = PSEncryptor.getInstance().decrypt(newPWD);
      assertTrue(newPWD.equals(originalPWD));
   }
   
}
