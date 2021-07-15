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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.design.objectstore;

import com.percussion.utils.testing.IPSReflectionFilter;
import com.percussion.utils.testing.PSReflectionHelper;
import com.percussion.xml.PSXmlDocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// Test case
public class PSDisplayTextTest extends TestCase
{
   public PSDisplayTextTest(String name)
   {
      super(name);
   }

   public void testEquals() throws Exception
   {
      PSDisplayText AAA = new PSDisplayText("AAA");
      PSDisplayText AAA_2 = new PSDisplayText("AAA");
      PSDisplayText aaa = new PSDisplayText("aaa");
      PSDisplayText BBB = new PSDisplayText("BBB");

      assertTrue(AAA.equals(AAA_2));
      assertTrue(!AAA.equals(aaa));
      assertTrue(!AAA.equals(BBB));
      
      PSReflectionHelper.testEquals(AAA, AAA_2, new IPSReflectionFilter() {
         public boolean acceptMethod(String methodname)
         {
            return !methodname.contains("Id");
         }
      });
   }

   public void testXml() throws Exception
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(doc, "Test");

      // create test object
      PSDisplayText testTo = new PSDisplayText("text");
      Element elem = testTo.toXml(doc);
      root.appendChild(elem);

      // create a new object and populate it from our testTo element
      PSDisplayText testFrom = new PSDisplayText(elem, null, null);
      assertTrue(testTo.equals(testFrom));
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      
      suite.addTest(new PSDisplayTextTest("testEquals"));
      suite.addTest(new PSDisplayTextTest("testXml"));
      
      return suite;
   }
}
