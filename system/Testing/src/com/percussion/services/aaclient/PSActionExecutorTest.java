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
package com.percussion.services.aaclient;

import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.assembly.data.PSAssemblyWorkItem;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import junit.framework.TestCase;

/**
 * @author Andriy Palamarchuk
 */
public class PSActionExecutorTest extends TestCase
{
   /**
    * Tests {@link PSActionExecutor#getSnippetBody(IPSAssemblyResult)}.
    */
   public void testGetSnippetBody() throws IOException {
      final PSActionExecutor executor = new PSActionExecutor();
      final PSAssemblyWorkItem result = new PSAssemblyWorkItem();

      // empty result
      result.setResultData("".getBytes());
      assertEquals("<br/>", executor.getSnippetBody(result));
      
      final String content = "Some Content";

      // no content
      result.setResultData(("ss<body></body>dd").getBytes());
      assertEquals("<br/>", executor.getSnippetBody(result));

      // normal situation
      result.setResultData(("ss<body>" + content + "</body>dd").getBytes());
      assertEquals(content + "<br/>", executor.getSnippetBody(result));

      // "body" tag in different case
      result.setResultData(("sS<BoDy>" + content + "</BODy>dd").getBytes());
      assertEquals(content + "<br/>", executor.getSnippetBody(result));
   }
}
