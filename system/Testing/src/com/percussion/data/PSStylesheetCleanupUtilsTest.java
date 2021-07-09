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
package com.percussion.data;

import com.percussion.utils.testing.IntegrationTest;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.ByteArrayInputStream;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;

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
      ByteArrayInputStream bis = null;
      try
      {
         bis = new ByteArrayInputStream(FILTER_XML.getBytes("utf8"));
         Document doc = PSXmlDocumentBuilder.createXmlDocument(bis, false);
         PSStylesheetCleanupFilter filter = 
            PSStylesheetCleanupFilter.getInstance();
         filter.fromXml(doc.getDocumentElement());
         String actualResults = 
            PSStylesheetCleanupUtils.namespaceCleanup(SOURCE, filter);
         assertEquals(actualResults, RESULT);
      }
      finally
      {
         if(bis != null)
            bis.close();
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
