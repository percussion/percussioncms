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
 * Unit test class for the <code>PSDbmsMapping</code> class.
 */
public class PSDbmsMappingTest
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
    public PSDbmsMappingTest()
   {
      super();
   }

   /**
    * Test constructing this object using parameters
    *
    * @throws Exception If there are any errors.
    */
   @Test
   public void testConstructor() throws Exception
   {
      PSDbmsInfo src = new PSDbmsInfo("rx-ds", "driver", "server", "db",
            "orig", "uid", "pwd", false);
      PSDbmsInfo tgt = new PSDbmsInfo("rx-dsTgt", "driverTgt", "serverTgt",
            "dbTgt", "origTgt", "uidTgt", "pwdTgt", false);

      // these should work fine
      assertTrue(testCtorValid(src, null));
      assertTrue(testCtorValid(src, tgt));

      // should be a problem
      assertTrue(!testCtorValid(null, src));
   }

   /**
    * Tests the equals methods
    *
    * @throws Exception if there are any errors.
    */
   @Test
   public void testEquals() throws Exception
   {
      PSDbmsInfo src1 = new PSDbmsInfo("rx-ds", "driver", "server", "db",
            "orig", "uid", "pwd", false);
      PSDbmsInfo src2 = new PSDbmsInfo("rx-ds", "driver", "server", "db",
            "orig", "uid", "pwd", false);

      PSDbmsMapping mapping1 = new PSDbmsMapping(new PSDatasourceMap(src1
            .getDatasource(), ""));
      PSDbmsMapping mapping2 = new PSDbmsMapping(new PSDatasourceMap(src2
            .getDatasource(), ""));

      assertTrue(mapping1.equals(mapping2));

      PSDbmsInfo mapping3 = new PSDbmsInfo("rx-ds", "driver3", "server", "db",
            "orig", "uid", "pwd", false);
      assertTrue(!mapping1.equals(mapping3));

      mapping1.setTargetInfo(src2.getDatasource());
      assertTrue(!mapping1.equals(mapping2));

      // test copy
      assertEquals(mapping1, new PSDbmsMapping(mapping1));
      assertEquals(mapping2, new PSDbmsMapping(mapping2));
   }

   /**
    * Tests all Xml functions, and uses equals as well.
    *
    * @throws Exception if there are any errors.
    */
   @Test
   public void testXml() throws Exception
   {
      // check object contains source only
      PSDbmsInfo src1 = new PSDbmsInfo("rx-ds", "driver", "server", "db",
            "orig", "uid", "pwd", false);
      PSDbmsMapping mapping1 = new PSDbmsMapping(new PSDatasourceMap(src1
            .getDatasource(), ""));
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element mapEl = mapping1.toXml(doc);

      PSDbmsMapping mapping2 = new PSDbmsMapping(mapEl);

      assertTrue(mapping1.equals(mapping2));

      // check object contains source and target
      PSDbmsInfo tgt1 = new PSDbmsInfo("rx-dsTgt", "driverTgt", "serverTgt",
            "dbTgt", "origTgt", "uidTgt", "pwdTgt", false);
      mapping1.setTargetInfo(tgt1.getDatasource());
      doc = PSXmlDocumentBuilder.createXmlDocument();
      mapEl = mapping1.toXml(doc);

      mapping2 = new PSDbmsMapping(mapEl);

      assertTrue(mapping1.equals(mapping2));
   }

   /**
    * Constructs a <code>PSDbmsMapping</code> object using the
    * supplied params and catches any exception.  For params,
    * see {@link PSDbmsMapping} ctor.
    *
    * @return <code>true</code> if no exceptions were caught, <code>false</code>
    * otherwise.
    */
   private boolean testCtorValid(PSDbmsInfo src, PSDbmsInfo tgt)
   {
      try
      {
         PSDbmsMapping mapping = new PSDbmsMapping(
               new PSDatasourceMap(src.getDatasource(), ""));
         if ( tgt != null )
            mapping.setTargetInfo(tgt.getDatasource());
      }
      catch (Exception ex)
      {
         return false;
      }

      return true;
   }


}
