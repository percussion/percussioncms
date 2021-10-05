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
