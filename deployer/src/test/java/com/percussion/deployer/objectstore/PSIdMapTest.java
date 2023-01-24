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
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Unit test class for the <code>PSIdMap</code> class.
 */
public class PSIdMapTest extends TestCase
{
   /**
    * Construct this unit test
    *
    * @param name The name of this test.
    */
    public PSIdMapTest(String name)
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
      assertTrue(testCtorValid("sourceServer_1"));

      // should be a problem
      assertTrue(!testCtorValid(null));
      assertTrue(!testCtorValid(""));
   }

   /**
    * Tests the equals and copyFrom from methods
    *
    * @throws Exception if there are any errors.
    */
   public void testEquals() throws Exception
   {
      // testing equal
      PSIdMapping mapping1 =
         new PSIdMapping("sourceId_1", "sourceName_1", "type1");
      PSIdMap map1 = new PSIdMap("sourceServer1");
      map1.addMapping(mapping1);
      PSIdMapping mapping2 =
         new PSIdMapping("sourceId_1", "sourceName_1", "type1");
      PSIdMap map2 = new PSIdMap("sourceServer1");
      map2.addMapping(mapping2);
      assertTrue(map1.equals(map2));

      // testing NOT equal
      PSIdMapping mapping3 =
         new PSIdMapping("sourceId_3", "sourceName_3", "type1");
      map1.addMapping(mapping3);
      assertTrue(!map1.equals(map2));

      PSIdMap map3 = new PSIdMap("sourceServer3");
      map3.addMapping(mapping1);
      assertTrue(!map1.equals(map3));

      PSIdMap map1_3 = new PSIdMap("sourceServer1");
      map1_3.addMapping(mapping3);
      assertTrue(!map1_3.equals(map2));
   }

   /**
    * Tests all Xml functions, and uses <code>equals()</code> and
    * <code>isNewMappping()</code> and <code>isNewObject()</code> as well.
    *
    * @throws Exception if there are any errors.
    */
   public void testXml() throws Exception
   {
      // testing object without mapping list
      PSIdMap src = new PSIdMap("sourceServer");
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element srcEl = src.toXml(doc);
      PSIdMap tgt = new PSIdMap(srcEl);

      assertTrue(src.equals(tgt));

      // testing object with mapping list
      PSIdMapping mapping =
         new PSIdMapping("sourceId_1", "sourceName_1", "type_1");
      PSIdMapping mapping2 =
         new PSIdMapping("sourceId_2", "sourceName_2", "type_1");
      PSIdMapping mapping3 =
         new PSIdMapping("sourceId_3", "sourceName_3", "type_1");
      src.addMapping(mapping);
      src.addMapping(mapping2);
      src.addMapping(mapping3);
      doc = PSXmlDocumentBuilder.createXmlDocument();
      srcEl = src.toXml(doc);
      tgt = new PSIdMap(srcEl);

      assertTrue(src.equals(tgt));
   }

   private boolean testCtorValid(String sourceServer)
   {
      try
      {
         PSIdMap map = new PSIdMap(sourceServer);
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
      suite.addTest(new PSIdMapTest("testConstructor"));
      suite.addTest(new PSIdMapTest("testEquals"));
      suite.addTest(new PSIdMapTest("testXml"));
      return suite;
   }

}
