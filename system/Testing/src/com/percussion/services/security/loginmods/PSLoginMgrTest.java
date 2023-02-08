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

