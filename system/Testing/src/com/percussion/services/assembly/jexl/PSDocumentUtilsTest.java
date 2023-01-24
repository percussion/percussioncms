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
