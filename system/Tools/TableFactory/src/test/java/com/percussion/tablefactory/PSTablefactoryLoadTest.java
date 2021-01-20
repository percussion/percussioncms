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
package com.percussion.tablefactory;

import com.percussion.utils.testing.UnitTest;
import org.apache.commons.io.FileUtils;
import org.apache.derby.drda.NetworkServerControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;

import static org.junit.Assert.assertTrue;

/**
 * Test adding data to db. Assumes a database with specific credentials,
 * change credentials or add an appropriate db to run the test.
 * 
 * @author dougrand
 */
@Category(UnitTest.class)
public class PSTablefactoryLoadTest
{
   @Rule
   public TemporaryFolder temporaryFolder = new TemporaryFolder();
   private String rxdeploydir;
   protected String baseDir;
   private  NetworkServerControl server;

   public PSTablefactoryLoadTest(){}

   @Before
   public void setup() throws IOException {

      rxdeploydir = System.getProperty("rxdeploydir");
      System.setProperty("rxdeploydir",temporaryFolder.getRoot().getAbsolutePath());

      baseDir = temporaryFolder.getRoot().getAbsolutePath();

       File ao_data = new File(baseDir,"ao_data.xml");
       ao_data.deleteOnExit();
       InputStream is = PSTablefactoryLoadTest.class.getResourceAsStream("/com/percussion/tablefactory/ao_data.xml");
       FileUtils.copyInputStreamToFile(is, ao_data);

      File ao_def = new File(baseDir,"ao_def.xml");
      ao_def.deleteOnExit();
      is = PSTablefactoryLoadTest.class.getResourceAsStream("/com/percussion/tablefactory/ao_def.xml");
      FileUtils.copyInputStreamToFile(is, ao_def);

      File db = new File(baseDir,"db.properties");
      db.deleteOnExit();
      is = PSTablefactoryLoadTest.class.getResourceAsStream("/com/percussion/tablefactory/db.properties");
      FileUtils.copyInputStreamToFile(is, db);

      File mc = new File(baseDir,"multichild.xml");
      mc.deleteOnExit();
      is = PSTablefactoryLoadTest.class.getResourceAsStream("/com/percussion/tablefactory/multichild.xml");
      FileUtils.copyInputStreamToFile(is, mc);

      File networkedDerbyDB = new File(baseDir,"derby-networked_rxrepository.properties");
      networkedDerbyDB.deleteOnExit();
      is = PSTablefactoryLoadTest.class.getResourceAsStream("/com/percussion/tablefactory/derby-networked_rxrepository.properties");
      FileUtils.copyInputStreamToFile(is, networkedDerbyDB);

      File mco = new File(baseDir,"multichild_out.xml");
      mco.deleteOnExit();
      is = PSTablefactoryLoadTest.class.getResourceAsStream("/com/percussion/tablefactory/multichild_out.xml");
      FileUtils.copyInputStreamToFile(is, mco);

      try {
         server = new NetworkServerControl
              (InetAddress.getByName("localhost"),1529);

         server.start(null);
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @After
   public void teardown(){
      try {
         //Reset the deploy dir property if it was set prior to test
         if(rxdeploydir != null)
            System.setProperty("rxdeploydir",rxdeploydir);

         server.shutdown();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
   @Test
   public void testAOCase() throws Exception
   {
      String args[] = new String[] {
            baseDir + "/db.properties",
            "dummy.xml",
            baseDir + "/ao_def.xml",
            baseDir + "/ao_data.xml",
            "-mld"
      };
      
      PSJdbcTableFactory.main(args);
   }

   @Test
   public void testNetworkedDerby(){
      try {
         String args[] = new String[]{
                 baseDir + "/derby-networked_rxrepository.properties",
                 "dummy.xml",
                 baseDir + "/ao_def.xml",
                 baseDir + "/ao_data.xml",
                 "-mld"
         };

         PSJdbcTableFactory.main(args);
         assertTrue(server!=null);
      }catch(Exception e){
         org.junit.Assert.fail(e.getMessage());
      }
   }

   @Test
   public void testMultiChildCase() throws Exception
   {
      String args[] = new String[] {
            baseDir + "/db.properties",
            "dummy.xml",
            baseDir + "/multichild.xml",
            "-mldp"
      };
      
      PSJdbcTableFactory.main(args);
   }   
}
