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

package com.percussion.servlets;

import org.springframework.mock.web.MockHttpServletRequest;

import junit.framework.TestCase;

public class PSLoginServletTest extends TestCase
{

   MockHttpServletRequest request = new MockHttpServletRequest();
   
   public void testIsValidRedirectUri() throws Exception
   {
      request.setScheme("http");
      request.setServerPort(9992);
      request.setServerName("perc-test");
      request.setRequestURI("/stuff");
      assertEquals("http://perc-test:9992/stuff", request.getRequestURL().toString());
      assertTrue(PSLoginServlet.isValidRedirectUri(request, "http://perc-test:9992/logout"));
      assertFalse(PSLoginServlet.isValidRedirectUri(request, "http://badsite.com/login"));
      assertTrue(PSLoginServlet.isValidRedirectUri(request, "/login"));
   }

}
