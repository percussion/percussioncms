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

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.percussion.extension.PSExtensionRef;
import com.percussion.xml.PSXmlDocumentBuilder;

// Test case
public class PSFieldTranslationTest extends TestCase
{
   public void testXmlRoundTrip() throws Exception
   {
      // create test object
      PSExtensionRef exitRef = new PSExtensionRef("handler", "context", "exit");
      PSExtensionCall exitCall = new PSExtensionCall(exitRef, null);
      PSExtensionCallSet callSet = new PSExtensionCallSet();
      callSet.add(exitCall);
      callSet.add(exitCall);

      PSFieldTranslation testTo = new PSFieldTranslation(callSet);
      testTo.setErrorMessage(new PSDisplayText("one"));

      // create a new object and populate it from our testTo element
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = testTo.toXml(doc);
      PSFieldTranslation testFrom = new PSFieldTranslation(root, null, null);
      
      // make sure new object and test object match
      assertTrue(testTo.equals(testFrom));
      PSDisplayText displayTextOutput = testFrom.getErrorMessage();
      assertNotNull(displayTextOutput);
      assertEquals("one", displayTextOutput.getText());
   }

   public void testXml() throws Exception
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();     
      Element root = PSXmlDocumentBuilder.createRoot(doc, "Test");
      
      // try with bad root
      PSFieldTranslation ft;
      try
      {
         ft = new PSFieldTranslation(root, null, null);
         fail("Wrong root element name should thrown exception");
      }
      catch (PSUnknownNodeTypeException expected)
      {
         // expected
      }

      // correct root, but missing children
      PSXmlDocumentBuilder.removeElement(root);
      root = PSXmlDocumentBuilder.createRoot(doc, PSFieldTranslation.XML_NODE_NAME);
      try
      {
         ft = new PSFieldTranslation(root, null, null);
         fail("Missing required PSXExtensionCallSet should thrown exception");
      }
      catch (PSUnknownNodeTypeException expected)
      {
         // expected
      }
      
      // add required child <PSXExtensionCallSet>
      PSExtensionRef exitRef = new PSExtensionRef("handler", "context", "exit");
      PSExtensionCall exitCall = new PSExtensionCall(exitRef, null);
      PSExtensionCallSet callSetInput = new PSExtensionCallSet();
      callSetInput.add(exitCall);
      root.appendChild(callSetInput.toXml(doc));
      
      ft = new PSFieldTranslation(root, null, null);
      PSExtensionCallSet callSetOutput = ft.getTranslations();
      assertNotNull(callSetOutput);
      assertEquals(1, callSetOutput.size());
      
      // add optional child <ErrorLabel>, but without its required child
      Element errorElem = PSXmlDocumentBuilder.addEmptyElement(doc, root, 
            PSFieldTranslation.ERROR_LABEL_ELEM);
      try
      {
         ft = new PSFieldTranslation(root, null, null);
         fail("Missing required ErrorLabel/PSXDisplayText should thrown exception");
      }
      catch (PSUnknownNodeTypeException expected)
      {
         // expected
      }
     
      // add required child <PSXDisplayText> to <ErrorLabel>
      PSDisplayText displayTextInput = new PSDisplayText("test text");
      errorElem.appendChild(displayTextInput.toXml(doc));
      
      ft = new PSFieldTranslation(root, null, null);
      PSDisplayText displayTextOutput = ft.getErrorMessage();
      assertNotNull(displayTextOutput);
      assertEquals("test text", displayTextOutput.getText());
      
   }
}
