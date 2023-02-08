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
