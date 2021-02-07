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
