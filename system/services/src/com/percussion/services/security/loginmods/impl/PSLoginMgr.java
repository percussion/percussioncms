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
package com.percussion.services.security.loginmods.impl;

import java.util.List;

import com.percussion.data.PSUserContextExtractor;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSSecurityProvider;
import com.percussion.security.PSSecurityProviderPool;
import com.percussion.security.PSUserAttributes;
import com.percussion.security.PSUserEntry;
import com.percussion.services.security.PSJaasUtils;
import com.percussion.services.security.loginmods.IPSLoginMgr;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
      StringBuffer errMsg = new StringBuffer();
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
   private static Log ms_log = LogFactory.getLog(PSLoginMgr.class);
}
