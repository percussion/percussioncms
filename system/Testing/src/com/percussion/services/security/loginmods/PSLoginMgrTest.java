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
package com.percussion.services.security.loginmods;

import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.security.PSSecurityProviderPool;
import com.percussion.server.PSRequest;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.IOException;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;

import static org.junit.Assert.assertTrue;

/**
 * Test case for the {@link IPSLoginMgr}.
 */
@Category(IntegrationTest.class)
public class PSLoginMgrTest
{
   /**
    * Test login thru the backend table provider.
    *  
    * @throws Exception if the test fails
    */
   @Test
   public void testLogin() throws Exception
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument(
        PSLoginMgrTest.class.getResourceAsStream("/com/percussion/security/config.xml"), false);
      PSServerConfiguration config = new PSServerConfiguration(doc);
      PSSecurityProviderPool.init(config);
      CallbackHandler callbackHandler = new CallbackHandler() {

         public void handle(Callback[] callbacks) throws IOException, 
            UnsupportedCallbackException
         {
            if (callbacks.length > 0)
               throw new UnsupportedCallbackException(callbacks[0]);
         }};
         
      IPSLoginMgr mgr = PSLoginMgrLocator.getLoginManager();
      Subject sub = null;
      setRequestInfo("admin1");
      
      sub = mgr.login("admin1", "demo", callbackHandler);
      assertTrue(sub != null);
      assertTrue(!sub.getPublicCredentials().isEmpty());
      assertTrue(sub.getPublicCredentials().contains("admin1"));
      
      sub = mgr.login("admin1", "foo", callbackHandler);
      assertTrue(sub == null);
   }
   
   private void setRequestInfo(String userName) {
	   
	   PSRequest newRequest = null;
	   
	   try {
		   PSRequestInfo.resetRequestInfo();
	       newRequest = PSRequest.getContextForRequest(true, false);
	       PSRequestInfo.initRequestInfo((Map<String,Object>) null);
	       PSRequestInfo.setRequestInfo(PSRequestInfo.KEY_PSREQUEST, newRequest);
	       PSRequestInfo.setRequestInfo(PSRequestInfo.KEY_USER, userName);
	   } 
	   finally {
          if (newRequest!=null)
             newRequest.release();
          
         PSRequestInfo.resetRequestInfo();
      }
   }
}

