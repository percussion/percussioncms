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
package com.percussion.services.assembly.jexl;

import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.assembly.data.PSAssemblyWorkItem;
import java.io.IOException;
import junit.framework.TestCase;

/**
 * @author Andriy Palamarchuk
 */
public class PSDocumentUtilsTest extends TestCase
{
   
   /**
    * Tests {@link PSDocumentUtils#extractBody(IPSAssemblyResult)} and
    * {@link PSDocumentUtils#extractBody(String)}.
    */
   public void testExtractBody() throws IOException
   {
      // empty result
      assertExtractedBody("", "");
      
      final String content = "Some Content";

      // no content
      // does not work as expected
//      assertExtractedBody("ss<body></body>dd", "");

      // normal situation
      assertExtractedBody("ss<body>" + content + "</body>dd", content);

      // "body" tag in different case
      assertExtractedBody("sS<BoDy>" + content + "</BODy>dd", content);
   }

   /**
    * Asserts that body extracted by
    * {@link PSDocumentUtils#extractBody(IPSAssemblyResult)} and
    * {@link PSDocumentUtils#extractBody(String)} from the <code>html</code>
    * parameter equals to <code>body</body>.
    * @param html the data to extract html body from.
    * Assumed not <code>null</code>.
    * @param body the expected extracted body content.
    * Assumed not <code>null</code>.
    */
   private void assertExtractedBody(final String html, final String body)
         throws IOException
   {
      final PSDocumentUtils utils = new PSDocumentUtils();

      assertEquals(body, utils.extractBody(html));

      m_result.setResultData(html.getBytes());
      assertEquals(body, utils.extractBody(m_result));
   }
   
   /**
    * Result variable used for testing to hold data.
    */
   final PSAssemblyWorkItem m_result = new PSAssemblyWorkItem();
   {
      m_result.setMimeType("text/html");
   }
}
