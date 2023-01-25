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
package com.percussion.design.objectstore;

import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlDocumentBuilder;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

// Test case
public class PSFieldSetTest extends TestCase
{
   public void testEquals() throws Exception
   {
      PSFieldSet fs1 = new PSFieldSet("aaa");
      PSFieldSet fs2 = new PSFieldSet("aaa");
      PSFieldSet fs3 = new PSFieldSet("bbb");
      
      assertTrue(fs1.equals(fs2));
      assertTrue(!fs1.equals(fs3));
      fs1.setUserSearchable(!fs1.isUserSearchable());
      assertTrue(!fs1.equals(fs2));
   }

   public void testXml() throws Exception
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();

      // create test object
      PSBackEndCredential cred = new PSBackEndCredential("credential_1");
      cred.setDataSource("rxdefault");
      PSTableLocator tl = new PSTableLocator(cred);
      PSTableRef tr = new PSTableRef("tableName_1", "tableAlias_1");
      PSTableSet ts = new PSTableSet(tl, tr);
      PSCollection tsCol = new PSCollection(ts.getClass());
      tsCol.add(ts);
      PSBackEndTable table = new PSBackEndTable("RXARTICLE");
      PSField f1 = new PSField("field_1", new PSBackEndColumn(table, "DISPLAYTITLE"));
      f1.setOccurrenceDimension(PSField.OCCURRENCE_DIMENSION_COUNT, null);
      f1.setOccurrenceCount(12, null);
      f1.setOccurrenceDimension(PSField.OCCURRENCE_DIMENSION_COUNT, new Integer(1));
      f1.setOccurrenceCount(12, new Integer(1));
      PSField f2 = new PSField("field_2", new PSBackEndColumn(table, "DISPLAYTITLE"));
      PSField f3 = new PSField("field_3", new PSBackEndColumn(table, "DISPLAYTITLE"));
      PSFieldSet fs1 = new PSFieldSet("fieldSet_1", f1);
      PSFieldSet fs2 = new PSFieldSet("fieldSet_2", f2);
      PSFieldSet fs3 = new PSFieldSet("fieldSet_3", f3);
      fs1.add(f1);
      fs1.add(f2);
      fs1.add(fs2);
      fs1.add(f3);
      fs1.add(fs3);
      PSFieldSet testTo = new PSFieldSet(fs1);
      Element elem = testTo.toXml(doc);
      //PSXmlDocumentBuilder.copyTree(doc, root, elem, true);

      // create a new object and populate it from our testTo element
      PSFieldSet testFrom = new PSFieldSet(elem, null, null);
      assertTrue(testTo.equals(testFrom));
      testFrom.setUserSearchable(!testFrom.isUserSearchable());
      testFrom = new PSFieldSet(testFrom.toXml(doc), null, null);
      assertTrue(testFrom.isUserSearchable() != testTo.isUserSearchable());
      assertTrue(!testTo.equals(testFrom));
   }

   /**
    * Tests that the XML serialization has a predictable ordering (ascending
    * alphabetical fieldname).
    */
   public void testXmlOrdering()
   {
      PSBackEndTable table = new PSBackEndTable("RXARTICLE");
      PSField f1 = new PSField("field_1", new PSBackEndColumn(table, "DISPLAYTITLE"));
      PSField f2 = new PSField("field_2", new PSBackEndColumn(table, "DISPLAYTITLE"));
      PSField f3 = new PSField("field_3", new PSBackEndColumn(table, "DISPLAYTITLE"));
      PSFieldSet fs1 = new PSFieldSet("fieldSet_1");
      fs1.add(f1);
      fs1.add(f3);
      fs1.add(f2);

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = fs1.toXml(doc);
      NodeList fields = root.getElementsByTagName(PSField.XML_NODE_NAME);
      assertEquals(3, fields.getLength());
      assertEquals("field_1", ((Element)fields.item(0)).getAttribute("name"));
      assertEquals("field_2", ((Element)fields.item(1)).getAttribute("name"));
      assertEquals("field_3", ((Element)fields.item(2)).getAttribute("name"));
    
   }
   
}
