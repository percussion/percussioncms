/******************************************************************************
 *
 * [ PSAAClientActionTestBase.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.content.ui.aa.actions.test;

import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestContext;
import com.percussion.servlets.PSSecurityFilter;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.junit.experimental.categories.Category;

import java.util.HashMap;
import java.util.Map;

/**
 * @author erikserating
 *
 */
@Category(IntegrationTest.class)
public class PSAAClientActionTestBase extends ServletTestCase
{
   /**
    * Login using the supplied credentials
    * 
    * @param uid The user id, assumed not <code>null</code> or empty.
    * @param pwd The password, assumed not <code>null</code> or empty.
    * 
    * @return The session id, never <code>null</code> or empty.
    * 
    * @throws Exception if the login fails.
    */
   protected String login(String uid, String pwd) throws Exception
   {
      // hack to get by re-logging in to same session see PSSecurityfilter)
      session.setAttribute("RX_LOGIN_ATTEMPTS", null);
      PSSecurityFilter.authenticate(request, response, uid, pwd);
      String sessionId = (String) session.getAttribute(
         IPSHtmlParameters.SYS_SESSIONID);
      return sessionId;
   }
   
   /**
    * Creates a new <code>IPSRequestContext</code> from the
    * request stored in thread local.
    * @return the request context, never <code>null</code>.
    */
   protected IPSRequestContext getRequestContext()
   {
      PSRequest req = (PSRequest) PSRequestInfo
      .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      return new PSRequestContext(req);
   }
   
   /**
    * Helper method to create a test JSON Array String
    * @param args the arguments
    * @return the array string, Never <code>null</code>.
    */
   protected static String makeJSONArrayString(String... args)
   {
      StringBuilder sb = new StringBuilder();
      sb.append("[");
      boolean isFirst = true;
      for(String arg : args)
      {
         if(!isFirst)
            sb.append(",");
         sb.append("\"");
         sb.append(arg);
         sb.append("\"");
         isFirst = false;         
      }
      sb.append("]");
      return sb.toString();
   }
   
   protected static final String pageJsonArray = 
      "[0,335,500,301,306,0,0,311,1,null,null,null,null]";
   protected static final String slotJsonArray = 
      "[1,335,505,301,306,0,0,311,1,518,null,null,null]";
   protected static final String snippetJsonArray = 
      "[2,372,503,301,null,0,0,311,0,518,1728,null,null]";
   protected static final String fieldJsonArray = 
      "[3,372,503,301,null,0,0,311,0,null,null,displaytitle,null]";
   
   protected static final Map<String,String> ms_testJsonArrays = 
      new HashMap<String,String>();
   static
   {
      ms_testJsonArrays.put("Page", pageJsonArray);
      ms_testJsonArrays.put("Slot", slotJsonArray);
      ms_testJsonArrays.put("Snippet", snippetJsonArray);
      ms_testJsonArrays.put("Field", fieldJsonArray);
   }
}
