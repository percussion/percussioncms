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
