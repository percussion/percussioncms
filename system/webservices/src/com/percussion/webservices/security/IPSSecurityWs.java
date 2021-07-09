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

import com.percussion.data.PSInternalRequestCallException;
import com.percussion.design.objectstore.PSRole;
import com.percussion.security.PSSecurityToken;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.security.data.PSCommunity;
import com.percussion.services.security.data.PSLogin;
import com.percussion.utils.guid.IPSGuid;

import java.io.IOException;
import java.util.List;

import javax.security.auth.login.LoginException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This interface defines all security related webservices.
 */
public interface IPSSecurityWs
{
   /**
    * Logon the user for the supplied credentials and the requested community
    * and locale.
    * 
    * @param request the servlet request, not <code>null</code>.
    * @param response the servlet response, not <code>null</code>.
    * @param user the name of the user to logon, not <code>null</code> or
    * empty.  Case-sensitivity is dependent on the login module implementation,
    * but the Rhythmyx server is not case-sensitive in regards to user names.
    * @param password the users password, not <code>null</code> or empty.  
    * Case-sensitivity is dependent on the login module implementation, but 
    * generally passwords are case-sensitive.
    * @param clientId the client id, may be <code>null</code> or empty. If a
    *    valid client id is provided, it will be used for design object locks
    *    instead of the session id. This allows a client to reuse the object 
    *    locks after a server crash if the same client id is supplied with the
    *    re-login. 
    * @param community the community name into which to logon the user,
    *    defaults to the last logged in community or the first community 
    *    by name if the user has not logged in before.
    * @param localeCode the locale into which to logon the user, defaults to 
    *    the last logged in locale or <code>us-en</code> if the user never
    *    logged in before.
    * @return the login information including the session, the roles, 
    *    communities and locales the user is a member of, 
    *    never <code>null</code>.
    * @throws IOException
    * @throws LoginException
    * @throws ServletException
    * @throws PSInternalRequestCallException
    */
   public PSLogin login(HttpServletRequest request, 
      HttpServletResponse response, String user, String password, 
      String clientId, String community, String localeCode) 
      throws IOException, LoginException, ServletException, 
         PSInternalRequestCallException;
   
   /**
    * Logout the user associated with the supplied session.
    * 
    * @param request the servlet request, not <code>null</code>.
    * @param session the session to be invalidated, not <code>null</code> or
    *    empty.
    */
   public void logout(HttpServletRequest request, String session);
   
   /**
    * Refresh the supplied session. If the supplied session is valid, the 
    * session timeout will be reset, otherwise an error is returned.
    * 
    * @param request the servlet request, not <code>null</code>.
    * @param session a valid session to be refreshed, not <code>null</code> or
    *    empty.
    * @throws LoginException if the supplied session is invalid.
    */
   public void refreshSession(HttpServletRequest request, String session) 
      throws LoginException;
   
   /**
    * Load all communities for the supplied name.
    * 
    * @param name the name of the community to load, may be <code>null</code>
    *    or empty in which case all communities will be loaded, 
    *    wildcards are accepted.
    * @return all found communities for the supplied name in read-only mode, 
    *    never <code>null</code>, may be empty, ascending alpha ordered by name.
    */
   public List<PSCommunity> loadCommunities(String name);
   
   /**
    * Load all roles for the supplied name.
    * 
    * @param name the name or the role to load, may be <code>null</code> 
    *    or empty, wildcards are accepted.
    * @return all found roles for the supplied name in read-only mode, 
    *    never <code>null</code>, may be empty, ascending alpha ordered by name.
    */
   public List<PSRole> loadRoles(String name);
   
   /**
    * Filter the supplied list of guids by the current user's runtime visibility
    * permissions to each.
    * 
    * @param ids The list to filter, may not be <code>null</code> or empty.
    * 
    * @return The filtered list, never <code>null</code>, may be empty.
    */
   public List<IPSGuid> filterByRuntimeVisibility(List<IPSGuid> ids);
   
   /**
    * Get the request context for the current user thread.
    * 
    * @return The request context, may be <code>null</code> if the request
    * context has not yet been initialized for the current thread.  See
    * {@link #reconnectSession(PSSecurityToken)} for more info.
    */
   public IPSRequestContext getRequestContext();
   
   /**
    * Restore the request context and user session referenced by the supplied
    * request context.
    * 
    * @param ctx The request context to restore, may not be <code>null</code>
    * and should have been obtained previously by calling
    * {@link #getRequestContext()}.
    */
   public void restoreRequestContext(IPSRequestContext ctx);
   
   /**
    * Login as the specified user. The request context and session for the
    * current thread will be updated to reflect the specified user. To restore
    * the user context in place prior to calling this method, first call
    * {@link #getRequestContext()} to get a handle to the current request
    * context, then call this method, and later call
    * {@link #restoreRequestContext(IPSRequestContext)} with the previously
    * obtained request context.
    * 
    * @param user the name of the user to logon, not <code>null</code> or
    * empty.  Case-sensitivity is dependent on the login module implementation,
    * but the Rhythmyx server is not case-sensitive in regards to user names.
    * @param password the users password, not <code>null</code> or empty.  
    * Case-sensitivity is dependent on the login module implementation, but 
    * generally passwords are case-sensitive.
    * @param community the name of the community to log the user into, may be
    * <code>null</code> or empty in which case it defaults to the last logged
    * in community or the first community by name if the user has not logged in
    * before.
    * @param localeCode the locale to set as the user's current locale, may be
    * <code>null</code> or empty in which case it defaults to the last logged
    * in locale or <code>us-en</code> if the user never logged in before.
    * 
    * @return The login information for the specified user, never 
    * <code>null</code>. 
    * 
    * @throws LoginException If the authentication fails.
    */
   public PSLogin login(String user, String password, String community, 
      String localeCode) 
      throws LoginException;
   
   /**
    * Get the security token that references the user session of the current 
    * thread.
    * 
    * @return The token, may be <code>null</code> if a session has not been
    * associated with the current thread.
    */
   public PSSecurityToken getSecurityToken();
   
   /**
    * Reconnect the user session specified by the supplied security token to the
    * current thread and initialize the request context. Generally used to
    * perform background processing in a new thread on behalf of the current
    * user by calling {@link #getSecurityToken()}, passing this object to the
    * new thread, and then calling this method in that new thread.
    * 
    * @param token The security token, may not be <code>null</code>.
    * 
    * @return <code>true</code> if the specified session was reconnected,
    * <code>false</code> if the specified session is no longer valid.
    */
   public boolean reconnectSession(PSSecurityToken token);
}

