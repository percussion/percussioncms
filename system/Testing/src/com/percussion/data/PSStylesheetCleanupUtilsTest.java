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
package com.percussion.data;

import com.percussion.utils.testing.IntegrationTest;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

/**
 *  Test class used to test methods in <code>PSStylesheetCleanupUtils</code>
 */
@Category(IntegrationTest.class)
public class PSStylesheetCleanupUtilsTest
{
   public PSStylesheetCleanupUtilsTest() {
   }

   /**
    * Test the namespaceCleanup routine, this method should, when successful,
    * remove non-XHTML namspace declarations and any elements or attributes that
    * use a namespace (i.e &lt;x:foo>&lt;/x:foo>)
    */
   @Test
   public void testNamespaceCleanup() throws Exception
   {
      try(ByteArrayInputStream bis = new ByteArrayInputStream(FILTER_XML.getBytes(StandardCharsets.UTF_8)))
      {
         Document doc = PSXmlDocumentBuilder.createXmlDocument(bis, false);
         PSStylesheetCleanupFilter filter = 
            PSStylesheetCleanupFilter.getInstance();
         filter.fromXml(doc.getDocumentElement());
         String actualResults = 
            PSStylesheetCleanupUtils.namespaceCleanup(SOURCE, filter);
         assertEquals(RESULT, actualResults);
      }
   }


   // Test Data
   private static final String SOURCE = "<jsp:include page=\"abc.jsp\"/><html xmlns=\"http://www.w3.org/1999/xhtml\">"
            + "<head><title>Test Data</title></head><body><p>"
            + "<div xmlns=\"\"><h1 xmlns:x=\"http://www.test.com/2004/test\" x:name=\"foo\">"
            + "</h1><br id=\"dummy\"><x:test>Hello!</x:test></div></body></html>";

   private static final String RESULT = "<html xmlns=\"http://www.w3.org/1999/xhtml\">"
            + "<head><title>Test Data</title></head><body><p>"
            + "<div><h1>"
            + "</h1><br id=\"dummy\">Hello!</div></body></html>";
   
   private static final String FILTER_XML = 
      "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" +
      "<stylesheetCleanupFilter>" +
         "<allowedNamespace name=\"\" declAllowed=\"true \" declValue=\"*xhtml*\">" +
            "<allowedElement name=\"*\"/>" +
            "<allowedAttribute name=\"*\"/>" +
         "</allowedNamespace>" +
         "<allowedNamespace name=\"xml\" declAllowed=\"false \">" +
            "<allowedAttribute name=\"lang\"/>" +
            "<allowedAttribute name=\"space\"/>" +
         "</allowedNamespace>" +
         "<!-- Uncomment the following to allow jsp tags -->" +
         "<!--" +
         "<allowedNamespace name=\"jsp\" declAllowed=\"false \">" +
            "<allowedElement name=\"*\"/>" +
            "<allowedAttribute name=\"*\"/>" +
         "</allowedNamespace>" +
         "-->" +
      "</stylesheetCleanupFilter>";

}
