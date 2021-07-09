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
package com.percussion.webservices.security;

import com.percussion.security.PSSecurityToken;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequest;
import com.percussion.services.security.PSJaasUtils;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.request.PSRequestInfo;

import java.security.Principal;

import javax.security.auth.Subject;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Test for methods of the {@link IPSSecurityWs} class that are not exposed via
 * webservices and thus not covered by the web service unit tests.
 */
@Category(IntegrationTest.class)
public class PSSecurityWsTest extends ServletTestCase
{
   /**
    * Test several authentication related features.
    * 
    * @throws Exception If the test fails or there are any errors.
    */
   @Test
   public void testAuthentication() throws Exception
   {
      final IPSSecurityWs svc = PSSecurityWsLocator.getSecurityWebservice();
      // assert we are anonymous
      String user;
      validateAnonymous(svc.getRequestContext());
      
      // log in as editor1
      user = "editor1";
      svc.login(user, "demo", null, null);
            
      // validate all user thread and session info
      IPSRequestContext ctx = svc.getRequestContext();
      validateUser(user, ctx);
      
      // login as admin1
      user = "admin1";
      svc.login(user, "demo", null, null);
      validateUser(user, svc.getRequestContext());
      
      // restore context
      svc.restoreRequestContext(ctx);
      validateUser("editor1", ctx);
      
      final PSSecurityToken tok = svc.getSecurityToken();
      final Exception[] exArr = new Exception[] {null};
      
      // launch thread and test session reconnect
      Runnable test = new Runnable() {

         public void run()
         {
            try
            {
               validateAnonymous(svc.getRequestContext());
               svc.reconnectSession(tok);
               validateUser("editor1", svc.getRequestContext());
            }
            catch (Exception e)
            {
               exArr[0] = e;
            }
         }};
      
      Thread t = new Thread(test);
      t.setDaemon(true);
      t.start();
      t.join();
      if (exArr[0] != null)
         throw new RuntimeException("Runnable test failed", exArr[0]);
   }

   /**
    * Validates that the current user thread represents an anonymous user
    * 
    * @param ctx The request context to use, if <code>null</code>, assumes that
    * the current user thread has no request info or session associated with
    * it.
    */
   private void validateAnonymous(IPSRequestContext ctx)
   {
      if (ctx == null)
         return;
      
      String user = ctx.getUserName();
      assertTrue("Current session should not be authenticated", 
         StringUtils.isBlank(user));
      assertEquals(user, PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_USER));
      Subject sub = (Subject) PSRequestInfo.getRequestInfo(
         PSRequestInfo.SUBJECT);
      assertTrue(sub == null || PSJaasUtils.subjectToPrincipal(sub) == null);
   }

   /**
    * Validate the specified user is represented correctly by the supplied 
    * request context
    * 
    * @param user The user to check for, assumed not <code>null</code> or empty.
    * @param ctx The context to check, assumes a <code>null</code> value 
    * indicates no request context associated with the current thread.
    */
   private void validateUser(String user, IPSRequestContext ctx)
   {
      assertNotNull(ctx);
      assertEquals(user, ctx.getUserName());
      PSRequest req = PSRequest.getRequest(ctx);
      assertEquals(user, req.getUserSession().getRealAuthenticatedUserEntry());
      assertTrue(req == PSRequestInfo.getRequestInfo(
         PSRequestInfo.KEY_PSREQUEST));
      assertEquals(user, PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_USER));
      Subject sub = (Subject) PSRequestInfo.getRequestInfo(
         PSRequestInfo.SUBJECT);
      assertTrue(sub != null);
      Principal userPrincipal = PSJaasUtils.subjectToPrincipal(sub);
      assertTrue(userPrincipal != null && user.equals(userPrincipal.getName()));
      assertEquals(req.getUserSession().getSessionObject(
         IPSHtmlParameters.SYS_LANG), PSRequestInfo.getRequestInfo(
            PSRequestInfo.KEY_LOCALE));
   }

}

