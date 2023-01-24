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
