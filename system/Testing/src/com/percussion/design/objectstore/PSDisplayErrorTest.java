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

import com.percussion.xml.PSXmlDocumentBuilder;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PSDisplayErrorTest
{

   @Test
   public void testXml() throws Exception
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(doc, "DisplayError");
      root.setAttribute("errorCount", "2");
      Element gm = doc.createElement("GenericMessage");
      gm.appendChild(doc.createTextNode("test generic message"));
      root.appendChild(gm);
      Element dt1 = doc.createElement("Details");
      Element fe1 = doc.createElement("FieldError");
      fe1.setAttribute("submitName", "sys_title");
      fe1.setAttribute("displayName", "Content Title");
      fe1.appendChild(doc.createTextNode("Content title must not be duplicate"));
      dt1.appendChild(fe1);
      root.appendChild(dt1);
      
      Element dt2 = doc.createElement("Details");
      Element fe2 = doc.createElement("FieldError");
      fe2.setAttribute("submitName", "displaytitle");
      fe2.setAttribute("displayName", "Title");
      fe2.appendChild(doc.createTextNode("Title must not be empty"));
      dt2.appendChild(fe2);
      root.appendChild(dt2);

      Document newDoc = PSXmlDocumentBuilder.createXmlDocument();
      PSDisplayError testFrom = new PSDisplayError(root);
      Element testTo = testFrom.toXml(newDoc);

      assertEquals(PSXmlDocumentBuilder.toString(root),
            PSXmlDocumentBuilder.toString(testTo));
   }

}
