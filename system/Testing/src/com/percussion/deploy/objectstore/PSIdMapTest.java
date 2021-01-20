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

package com.percussion.deploy.objectstore;

import com.percussion.utils.testing.UnitTest;
import com.percussion.xml.PSXmlDocumentBuilder;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.junit.Assert.assertTrue;

/**
 * Unit test class for the <code>PSIdMap</code> class.
 */
@Category(UnitTest.class)
public class PSIdMapTest
{
    public PSIdMapTest()
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
   @Test
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
   @Test
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

   /**
    * Constructs a <code>PSIdMap</code> object using the
    * supplied params and catches any exception.  For params,
    * see {@link PSIdMap(String} for more info
    *
    * @return <code>true</code> if no exceptions were caught,
    * <code>false</code> otherwise.
    */
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

}
