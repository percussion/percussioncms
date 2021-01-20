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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.services.aaclient;

import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.assembly.data.PSAssemblyWorkItem;
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
   public void testGetSnippetBody() throws UnsupportedEncodingException
   {
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
