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
package com.percussion.i18n.rxlt;

import com.percussion.i18n.tmxdom.IPSTmxDocument;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.xml.PSXmlDocumentBuilder;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.junit.Assert.assertNotNull;

/**
 * Test framework for JSP scanner. This has to run against a rhythmyx 
 * installation. Setup rxdeploydir before running this test.
 * 
 * @author dougrand
 */
@Category({IntegrationTest.class})
public class PSJspHandlerTest
{
   /**
    * Run test
    */
   @Test
   public void testJspHandlerTest()
   {
      // Disable logging
      PSCommandLineProcessor.setIsLogEnabled(false);
      
      PSJspHandler jsph = new PSJspHandler();
      
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = doc.createElement("rxltconfig");
      root.setAttribute("rxroot", System.getProperty("rxdeploydir"));
      doc.appendChild(root);
      Element el = doc.createElement("section");
      root.appendChild(el);
      el.setAttribute("sectionid", "5");
      el.setAttribute("name", "JSP Files");
      el.setAttribute("process", "yes");
      
      IPSTmxDocument result = jsph.process(el);
      assertNotNull(result);
   }
}
