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
package com.percussion.utils.servlet;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.junit.experimental.categories.Category;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;

/**
 * Test case for the {@link PSServletUtils} class.
 */
@Category(IntegrationTest.class)
public class PSServletUtilsTest extends ServletTestCase
{

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      PSServletUtils.initialize(config.getServletContext());
   }

   /**
    * Tests {@link PSServletUtils#getConfigDir()}.
    */
   public void testGetConfigDir()
   {
      File configDir = PSServletUtils.getConfigDir(); 
      assertTrue(configDir.exists());
      assertTrue(configDir.getPath().replace('\\', '/').endsWith(
         "deploy/rxapp.ear/rxapp.war/WEB-INF/config"));
   }

   /**
    * Tests {@link PSServletUtils#getUserConfigDir()}.
    */
   public void testGetUserConfigDir()
   {
      File configDir = PSServletUtils.getConfigDir(); 
      assertTrue(configDir.exists());
      assertTrue(configDir.getPath().replace('\\', '/').endsWith(
         "deploy/rxapp.ear/rxapp.war/WEB-INF/config"));
   }
   
   /**
    * Tests the request dispatcher and servlet code
    * @throws IOException 
    * @throws ServletException 
    */
   public void tobefixed_testCallServlet() throws ServletException, IOException
   {
      assertNotNull(PSServletUtils.getDispatcher("/rxwebdav"));
      MockHttpServletRequest req = new MockHttpServletRequest();
      req.setMethod("GET");
      req.setServletPath("/contentlist");
      req.setParameter("sys_deliverytype", "filesystem");
      req.setParameter("sys_assembly_context", "301");
      req.setParameter("sys_contentlist", "rffEiFullBinary");
      req.setParameter("sys_siteid", "301");
      req.setParameter("sys_authtype", "1");
      req.setParameter("sys_context", "1");
      req.setContextPath("/Rhythmyx");
      MockHttpServletResponse resp =
         (MockHttpServletResponse) PSServletUtils.callServlet(req);
      String content = resp.getContentAsString();
      assertNotNull(content);
      assertTrue(content.length() > 0);
      assertTrue("Could not find \"<contentitem\" in \"" + content + "\"",
            content.contains("<contentitem"));
   }
}

