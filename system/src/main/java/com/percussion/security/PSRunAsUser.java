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

package com.percussion.security;

import com.percussion.server.PSRequest;
import com.percussion.server.PSUserSession;
import com.percussion.services.security.IPSRoleMgr;
import com.percussion.services.security.PSJaasUtils;
import com.percussion.services.security.PSRoleMgrLocator;
import com.percussion.utils.request.PSRequestInfo;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.security.auth.Subject;
import java.util.Collections;
import java.util.Map;

/**
 * 
 * Executes code as if the given user was logged in.
 * Much of CM System uses the current request and thread local
 * information to determine security access.
 * <p>
 * This helper callback allows you to run code as a different user
 * with all the security details including roles loaded correctly.
 * <p>
 * Once {@link #run(String, Object)} is finished the request
 * and thread local will be restored to what the original caller
 * security was.
 * <p>
 * While this class is thread safe its recommend that you do not use
 * it with multiple threads as other parts of the system may not be. 
 * 
 * @author adamgent
 *
 * @param <CONTEXT>
 */
public abstract class PSRunAsUser<CONTEXT> 
{  
   
   private static final Logger log = LogManager.getLogger(PSRunAsUser.class);
   /**
    * Runs the callback {@link #run(Object, PSRequest)} as the given user.
    * @param userName not blank or null.
    * @param c a custom object that may needed for execution as the given user.
    * @throws Exception
    */
   public void run(final String userName, CONTEXT c) throws Exception
   {
      if (StringUtils.isBlank(userName)) {
         throw new IllegalArgumentException("userName is blank");
      }
     
      Map<String, Object> originalRequestInfoMap = null;
      PSRequest newRequest = null;
      String newUserName = null;
      PSUserSession sess = null;
      Subject s = null;
      try {
         PSThreadRequestUtils.changeToInternalRequest(true);
         s = getSubject(userName);
         PSJaasUtils.loadSubjectRoles(s, userName);
         

      } catch (Exception e)
      {
         log.debug("Cannot get user "+userName+ " to notify skipping",e);
         return;
      }
      finally {
         PSThreadRequestUtils.restoreOriginalRequest();
      }
      try {
         
         newRequest = PSRequest.getContextForRequest(true, false);
         originalRequestInfoMap = PSRequestInfo.copyRequestInfoMap();
         if (PSRequestInfo.isInited())
            PSRequestInfo.resetRequestInfo();
         
         PSJaasUtils.loadSubjectRoles(s, newUserName);
         newRequest = PSRequest.getContextForRequest(true, false);

         PSRequestInfo.initRequestInfo((Map<String,Object>) null);
         PSRequestInfo.setRequestInfo(PSRequestInfo.KEY_PSREQUEST, newRequest);
         PSRequestInfo.setRequestInfo(PSRequestInfo.KEY_USER, userName);
         PSRequestInfo.setRequestInfo(PSRequestInfo.SUBJECT, s);
         
         // Handle security information
         PSUserEntry entry = PSJaasUtils.subjectToUserEntry(s, userName, null);
         sess = newRequest.getUserSession();
         sess.clearAuthenticatedUserEntries();
         sess.addAuthenticatedUserEntry(entry);

         run(c, newRequest);
        
      }
      finally {
         // release only releases sessions for anonymous users so we must make it anonymous
         if (sess!=null)
            sess.clearAuthenticatedUserEntries();
         if (newRequest!=null)
            newRequest.release();
         
         PSRequestInfo.resetRequestInfo();
         PSRequestInfo.initRequestInfo(originalRequestInfoMap);
      }
   }
  /**
   * Gets the JAAS subject for the given user.
   * @param userName not null or empty.
   * @return not null
   * @throws PSSecurityCatalogException
   */
   private Subject getSubject(String userName) throws PSSecurityCatalogException 
   {
      IPSRoleMgr roleMgr = PSRoleMgrLocator.getRoleManager();
      return roleMgr.findUsers(Collections.<String>singletonList(userName)).get(0);
   }
   /**
    * This is the callback method that will be executed as the given user.
    * @param c maybe null.
    * @param request the new request as the given user, not null.
    * @throws Exception
    */
   protected abstract void run(CONTEXT c, PSRequest request) throws Exception;
}
