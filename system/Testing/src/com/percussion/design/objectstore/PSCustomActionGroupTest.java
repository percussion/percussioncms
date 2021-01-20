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

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Unit test(s) for custom action group
 *  
 */
public class PSCustomActionGroupTest extends TestCase
{
   /**
    * Ctor
    * 
    * @param name The name of the test.
    */
   public PSCustomActionGroupTest(String name)
   {
      super(name);
   }

   /**
    * Case tests object->xml->object serialization.The custom action grou is
    * built as a Java object. Converted to XML document using toXml() method.
    * Then the object is restored from this xml document and compared with
    * original object using equals() method.
    * 
    * @throws PSUnknownNodeTypeException if construction of the object from XML
    *            document fails
    */
   public void testXml() throws PSUnknownNodeTypeException
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(doc, "Test");

      // create test object
      PSParam param1 = new PSParam("pssessionid", new PSUserContext(
            "/User/SessionId"));
      PSCollection parameters1 = new PSCollection(param1.getClass());
      parameters1.add(param1);

      PSCollection removeActions = new PSCollection(new String().getClass());
      removeActions.add("remove1");
      removeActions.add("remove2");
      removeActions.add("remove3");

      PSActionLink actionLink = new PSActionLink(new PSDisplayText(
            "displayText"));
      actionLink.setParameters(parameters1);
      PSActionLinkList actions = new PSActionLinkList(actionLink);
      PSLocation location = new PSLocation(PSLocation.PAGE_SUMMARY_VIEW,
            PSLocation.TYPE_ROW);
      Collection fieldRefs = new ArrayList();
      fieldRefs.add("testField");
      location.setFieldRefs(fieldRefs.iterator());
      PSCustomActionGroup testTo = new PSCustomActionGroup(location, actions);
      testTo.setRemoveActions(removeActions);
      Element elem = testTo.toXml(doc);
      root.appendChild(elem);

      // create a new object and populate it from our testTo element
      PSCustomActionGroup testFrom = new PSCustomActionGroup(elem, null, null);
      Document doc2 = PSXmlDocumentBuilder.createXmlDocument();
      Element root2 = PSXmlDocumentBuilder.createRoot(doc2, "Test");
      Element elem2 = testFrom.toXml(doc2);
      root2.appendChild(elem2);
      assertTrue(testTo.equals(testFrom));
   }

   /**
    * Collect all tests into a TestSuite and return it
    * 
    * @return
    */
   public static Test suite()
   {
      TestSuite suite = new TestSuite(PSCustomActionGroupTest.class);
      return suite;
   }
}