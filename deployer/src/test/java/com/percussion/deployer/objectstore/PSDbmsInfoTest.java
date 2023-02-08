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


package com.percussion.deployer.objectstore;

import com.percussion.xml.PSXmlDocumentBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test class for the <code>PSDbmsInfo</code> class.
 */
public class PSDbmsInfoTest
{

   @Rule
   public TemporaryFolder temporaryFolder = new TemporaryFolder();
   private String rxdeploydir;

   @Before
   public void setup() throws IOException {

      rxdeploydir = System.getProperty("rxdeploydir");
      System.setProperty("rxdeploydir", temporaryFolder.getRoot().getAbsolutePath());
   }

   @After
   public void teardown(){
      if(rxdeploydir != null)
         System.setProperty("rxdeploydir",rxdeploydir);
   }

   /**
    * Construct this unit test
    *
    */
   public PSDbmsInfoTest()
   {

   }
   
   /**
    * Test constructing this object using parameters
    * 
    * @throws Exception If there are any errors.
    */
   @Test
   public void testConstructor() throws Exception
   {
      // these should work fine
      assertTrue(testCtorValid("RhythmyxData", "inetdae7", "foo", null, null,
            null, null, false));
      assertTrue(testCtorValid("RhythmyxData", "inetdae7", "foo", "", "", "",
            "", true));

      // should be a problem
      assertTrue(!testCtorValid("RhythmyxData", null, "foo", "", "", "", "",
            true));
      assertTrue(!testCtorValid("RhythmyxData", "", "foo", "", "", "", "", true));
      assertTrue(!testCtorValid("RhythmyxData", "inetdae7", null, "", "", "",
            "", true));
      assertTrue(!testCtorValid("RhythmyxData", "inetdae7", "", "", "", "", "",
            true));
   }
   
   /**
    * Tests the equals and copy from methods
    * 
    * @throws Exception if there are any errors.
    */
   @Test
   public void testEquals() throws Exception
   {
      PSDbmsInfo info1 = new PSDbmsInfo("RhythmyxData", "inetdae7", "foo",
            "bar", "dbo", "sa", "demo", false);

      PSDbmsInfo info2 = new PSDbmsInfo("RhythmyxData", "inetdae7", "foo",
            "bar", "dbo", "sa", "demo", false);

      assertEquals(info1, info2);

      info2 = new PSDbmsInfo("RhythmyxData", "inetdae7", "foo", "bar", "dbo",
            "sa1", "demo", false);
      assertTrue(!info1.equals(info2));
      assertTrue(info1.isSameDb(info2));

      info2.copyFrom(info1);
      assertEquals(info1, info2);

      info2 = new PSDbmsInfo(info1.getDatasource(), info1.getDriver(), info1
            .getServer(), info1.getDatabase(), info1.getOrigin(), info1
            .getUserId(), info1.getPassword(true), true);
      assertEquals(info1, info2);


      info1 = new PSDbmsInfo("RhythmyxData", "inetdae7", "foo", null, null,
            null, null, false);
      info2 = new PSDbmsInfo("RhythmyxData", "inetdae7", "foo", "", "", "", "",
            false);
      assertEquals(info1, info2);
      info2 = new PSDbmsInfo("RhythmyxData", "inetdae7", "bar", "", "", "", "",
            false);
      assertTrue(!info1.isSameDb(info2));

      // test copy
      info1 = new PSDbmsInfo("RhythmyxData", "inetdae7", "foo", "bar", "dbo",
            "sa", "demo", false);
      assertEquals(info1, new PSDbmsInfo(info1));
      
   }
   
   /**
    * Tests all Xml functions, and uses equals as well.
    * 
    * @throws Exception if there are any errors.
    */
   @Test
   public void testXml() throws Exception
   {
      PSDbmsInfo src = new PSDbmsInfo("RhythmyxData", "inetdae7", "foo", "bar",
            "dbo", "sa", "demo", false);
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element srcEl = src.toXml(doc);
      PSDbmsInfo tgt = new PSDbmsInfo(srcEl);
      assertTrue(src.equals(tgt));
   }   
   
   /**
    * Constructs a <code>PSDbmsInfo</code> object using the supplied params and
    * catches any exception.  For params, see {@link PSDbmsInfo} ctor.
    * 
    * @return <code>true</code> if no exceptions were caught, <code>false</code>
    * otherwise.
    */
   private boolean testCtorValid(String datasrc, String driver, String server,
         String database, String origin, String uid, String pwd,
         boolean isPwdEncrypted)
   {
      try
      {
         PSDbmsInfo info = new PSDbmsInfo(datasrc, driver, server, database,
               origin, uid, pwd, isPwdEncrypted);
      }
      catch (Exception ex)
      {
         return false;
      }

      return true;
   }
   
}
