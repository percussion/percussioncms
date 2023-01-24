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
package com.percussion.services.security.loginmods.impl;

import com.percussion.data.PSUserContextExtractor;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSSecurityProvider;
import com.percussion.security.PSSecurityProviderPool;
import com.percussion.security.PSUserEntry;
import com.percussion.services.security.PSJaasUtils;
import com.percussion.services.security.loginmods.IPSLoginMgr;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import java.util.List;

/**
 * Implementation class for login manager
 */
public class PSLoginMgr implements IPSLoginMgr
{
	private static final String LOGIN_DISABLED = "loginDisabled";
	
   public Subject login(String user, String password,
      CallbackHandler callbackHandler) throws LoginException
   {
	   
      if (callbackHandler == null)
         throw new IllegalArgumentException("callbackHandler may not be null");

      PSUserEntry userEntry = null;
      PSSecurityProvider[] providers = PSSecurityProviderPool.getAllProviders();
      StringBuilder errMsg = new StringBuilder();
      for (int i = 0; i < providers.length; i++)
      {
         try
         {
            userEntry = providers[i].authenticate(user, password, 
               callbackHandler);
            break;
         }
         catch (PSAuthenticationFailedException e)
         {
            // save
            errMsg.append(e.getLocalizedMessage());
            errMsg.append("; ");
         }
         catch (Exception e)
         {
            ms_log.debug("Login manager security provider error: ", e);
            throw new LoginException("security provider authentication error: " 
                  + e.getLocalizedMessage());
         }
      }
      
      if (userEntry != null)
      {
    	  List<String> attribs = PSUserContextExtractor.getSubjectAttributeValues(userEntry, LOGIN_DISABLED);
		  if(!attribs.isEmpty() && ((attribs.get(0)).equalsIgnoreCase("true") || (attribs.get(0)).equalsIgnoreCase("yes"))) {
			  
			  errMsg.append("The user " + userEntry.getName() + " is forbidden to login.");
			  throw new LoginException("The user " + userEntry.getName() + " is forbidden to login.");
		  }
         ms_log.debug(errMsg.toString());
      }
      else {
          // log all failures if none succeeded
          ms_log.info(errMsg.toString());
          return null;
      }
      
      return PSJaasUtils.userEntryToSubject(userEntry, password);
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.security.loginmods.IPSLoginMgr#logout(java.lang.String)
    */
   public boolean logout(String user) throws LoginException
   {
      if (user == null || user.trim().length() == 0)
      {
         throw new IllegalArgumentException("user may not be null or empty");
      }
      // Nothing to do...
      return true;
   }
   
   /**
    * Logger to use, never <code>null</code>.
    */
   private static final Logger ms_log = LogManager.getLogger(PSLoginMgr.class);
}
