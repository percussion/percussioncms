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
package com.percussion.design.objectstore;

import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlDocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// Test case
public class PSContainerLocatorTest extends TestCase
{
   public PSContainerLocatorTest(String name)
   {
      super(name);
   }

   public void testEquals() throws Exception
   {
   }

   public void testXml() throws Exception
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(doc, "Test");

      // create test object
      PSBackEndCredential cred = new PSBackEndCredential("Cred1");
      cred.setDataSource("rxdefault");
      PSTableLocator table = new PSTableLocator(cred);
      PSTableRef tableRef = new PSTableRef("RXARTICLE", null);
      PSTableSet tableSet = new PSTableSet(table, tableRef);
      PSCollection tableSetCol = new PSCollection(tableSet.getClass());
      tableSetCol.add(tableSet);
      PSContainerLocator testTo = new PSContainerLocator(tableSetCol);
      Element elem = testTo.toXml(doc);

      // create a new object and populate it from our testTo element
      PSContainerLocator testFrom = new PSContainerLocator(elem, null, null);
      assertTrue(testTo.equals(testFrom));
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite();

      suite.addTest(new PSContainerLocatorTest("testXml"));

      return suite;
   }
}
