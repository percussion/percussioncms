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
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Unit test class for the <code>PSIdMapping</code> class.
 */
public class PSIdMappingTest extends TestCase
{
   /**
    * Construct this unit test
    *
    * @param name The name of this test.
    */
    public PSIdMappingTest(String name)
   {
      super(name);
   }

   /**
    * Test constructing this object using parameters
    *
    * @throws Exception If there are any errors.
    */
   public void testConstructor() throws Exception
   {
      // these should work fine
      assertTrue(testCtorValid("sourceId_1", "sourceName_1", "type_1"));
      assertTrue(testCtorValid("sourceId_2", "sourceName_2", "type_2"));

      // should be a problem
      assertTrue(!testCtorValid(null, "foo", "type"));
      assertTrue(!testCtorValid("", "foo", "type"));
      assertTrue(!testCtorValid("id", null, "type"));
      assertTrue(!testCtorValid("id", "", "type"));
      assertTrue(!testCtorValid("id", "foo", null));
      assertTrue(!testCtorValid("id", "foo", ""));
   }



   /**
    * Tests the equals and copyFrom from methods
    *
    * @throws Exception if there are any errors.
    */
   public void testEquals() throws Exception
   {
      // testing with PSIdMapping(String, String, String)
      PSIdMapping mapping1 =
         new PSIdMapping("sourceId_1", "sourceName_1", "type1");
      PSIdMapping mapping2 =
         new PSIdMapping("sourceId_1", "sourceName_1", "type1");
      assertEquals(mapping1, mapping2);

      PSIdMapping mapping3 =
         new PSIdMapping("sourceId_3", "sourceName_3", "type1");
      assertTrue(!mapping1.equals(mapping3));

      mapping3.copyFrom(mapping1);
      assertTrue(mapping1.equals(mapping3));

      // testing with PSIdMapping(String, String, String, boolean)
      mapping1 =
         new PSIdMapping("sourceId_1", "sourceName_1", "type1", true);
      mapping2 =
         new PSIdMapping("sourceId_1", "sourceName_1", "type1", true);
      assertEquals(mapping1, mapping2);

      mapping3 =
         new PSIdMapping("sourceId_3", "sourceName_3", "type1", true);
      assertTrue(!mapping1.equals(mapping3));

      // test copy ctor
      mapping1.setTarget("targetId", "targetName", null, null);
      assertEquals(mapping1, new PSIdMapping(mapping1));
      mapping1 = new PSIdMapping("sourceId_1", "sourceName_1", "type1", 
         "parent1", "parentName", "parentType", false);
      assertEquals(mapping1, new PSIdMapping(mapping1));
   }

   /**
    * Tests all Xml functions, and uses <code>equals()</code> and
    * <code>isNewObject()</code> as well.
    *
    * @throws Exception if there are any errors.
    */
   public void testXml() throws Exception
   {
      //
      // testing object without targetId and targetName
      //
      PSIdMapping src =
         new PSIdMapping("sourceId_1", "sourceName_1", "type_1");

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element srcEl = src.toXml(doc);

      PSIdMapping tgt = new PSIdMapping(srcEl);

      assertTrue(src.equals(tgt));
      assertTrue(!src.isNewMapping());

      src.setIsNewObject(true);
      assertTrue(!src.equals(tgt));

      //
      // testing object with targetId and targetName
      //
      PSIdMapping src_2 =
         new PSIdMapping("sourceId_2", "sourceName_2", "type_1", false);
      src_2.setTarget("targetid_2", "targetName_2");

      doc = PSXmlDocumentBuilder.createXmlDocument();
      Element srcEl_2 = src_2.toXml(doc);

      PSIdMapping tgt_2 = new PSIdMapping(srcEl_2);

      assertTrue(src_2.equals(tgt_2));

      //
      // testing object with targetId and targetName, but "isNewObject" == true
      //
      PSIdMapping src_new =
         new PSIdMapping("sourceId_new", "sourceName_new", "type_new");
      src_new.setTarget("targetid_new", "targetName_new");

      doc = PSXmlDocumentBuilder.createXmlDocument();
      Element srcEl_new = src_new.toXml(doc);

      // NOTE: "tgt_new" is originated from "src_new", but it should not have
      //       targetId and targetName, and it equals "src_new_2"
      PSIdMapping tgt_new = new PSIdMapping(srcEl_new);
      tgt_new.setIsNewObject(true);
      
      PSIdMapping src_new_2 =
         new PSIdMapping("sourceId_new", "sourceName_new", "type_new");
      src_new_2.setIsNewObject(true);

      assertTrue(!src_new_2.equals(src_new));
      assertTrue(src_new_2.equals(tgt_new)); // both have NO target id and name

      assertTrue(src_new_2.isNewObject());
      assertTrue(tgt_new.isNewObject());
   }

   /**
    * Constructs a <code>PSIdMapping</code> object using the
    * supplied params and catches any exception.  For params,
    * see {@link PSIdMapping(String, String, String} for more info.
    *
    * @return <code>true</code> if no exceptions were caught,
    * <code>false</code> otherwise.
    */
   private boolean testCtorValid(String sourceId, String sourceName,
      String type)
   {
      try
      {
         PSIdMapping mapping = new PSIdMapping(sourceId, sourceName, type);
      }
      catch (Exception ex)
      {
         return false;
      }

      return true;
   }



   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSIdMappingTest("testConstructor"));
      suite.addTest(new PSIdMappingTest("testEquals"));
      suite.addTest(new PSIdMappingTest("testXml"));
      return suite;
   }

}
