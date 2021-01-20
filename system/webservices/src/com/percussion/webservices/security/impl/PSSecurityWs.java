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
package com.percussion.webservices.security.impl;

import com.percussion.data.PSInternalRequestCallException;
import com.percussion.design.objectstore.PSRole;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.i18n.PSLocale;
import com.percussion.security.PSSecurityToken;
import com.percussion.security.PSThreadRequestUtils;
import com.percussion.server.*;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.security.*;
import com.percussion.services.security.data.PSCommunity;
import com.percussion.services.security.data.PSLogin;
import com.percussion.servlets.PSSecurityFilter;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSBaseBean;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.webservices.PSInvalidLocaleException;
import com.percussion.webservices.PSUserNotMemberOfCommunityException;
import com.percussion.webservices.security.IPSSecurityWs;
import com.percussion.webservices.system.IPSSystemWs;
import com.percussion.webservices.system.PSSystemWsLocator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.login.LoginException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The public security webservice implementations.
 */
@Transactional
@PSBaseBean("sys_securityWs")
public class PSSecurityWs extends PSSecurityBaseWs implements IPSSecurityWs
{
   // @see IPSSecurityWs#loadCommunities(String)
   public List<PSCommunity> loadCommunities(String name)
   {
      IPSBackEndRoleMgr service = PSRoleMgrLocator.getBackEndRoleManager();

      if (StringUtils.isBlank(name))
         name = "*";
      name = StringUtils.replaceChars(name, '*', '%');

      return service.findCommunitiesByName(name);
   }

   // @see IPSSecurityWs#loadRoles(String)
   public List<PSRole> loadRoles(String name)
   {
      IPSCmsObjectMgr service = PSCmsObjectMgrLocator.getObjectManager();

      if (StringUtils.isBlank(name))
         name = "*";
      name = StringUtils.replaceChars(name, '*', '%');

      List<PSRole> roles = service.findRolesByName(name);

      return roles;
   }

   /**
    * @see IPSSecurityWs#login(HttpServletRequest, HttpServletResponse, 
    *    String, String, String, String, String)
    */
   @SuppressWarnings("unchecked")
   public PSLogin login(HttpServletRequest request,
      HttpServletResponse response, String user, String password,
      String clientId, String community, String localeCode) throws IOException,
      LoginException, ServletException, PSInternalRequestCallException
   {
      if (request == null)
         throw new IllegalArgumentException("request cannot be null");

      if (response == null)
         throw new IllegalArgumentException("response cannot be null");

      if (StringUtils.isBlank(user))
         throw new IllegalArgumentException("user cannot be null or empty");

      if (StringUtils.isBlank(password))
         throw new IllegalArgumentException("password cannot be null or empty");

      // authenticate for supplied credentials
      request = PSSecurityFilter
         .authenticate(request, response, user, password);

      // create the login object for return
      PSLogin login = new PSLogin();

      // get the rhythmyx session
      HttpSession session = request.getSession();
      String sessionId = (String) session
         .getAttribute(IPSHtmlParameters.SYS_SESSIONID);
      login.setSessionId(sessionId);
      PSUserSession rxSession = PSUserSessionManager.getUserSession(sessionId);
      login.setSessionTimeout(PSUserSessionManager.getUserSessionTimeout());

      // store the client id to the rhythmyx session if they provided one
      if (!StringUtils.isBlank(clientId))
         rxSession.setPrivateObject(PSUserSession.CLIENTID, clientId);

      // switch community and locale if specified
      boolean success = false;
      try
      {
         IPSSystemWs sysSvc = PSSystemWsLocator.getSystemWebservice();
         if (!StringUtils.isBlank(community))
            sysSvc.switchCommunity(community);
         if (!StringUtils.isBlank(localeCode))
            sysSvc.switchLocale(localeCode);
         success = true;
      }
      catch (PSUserNotMemberOfCommunityException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage(), e);
      }
      catch (PSInvalidLocaleException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage(), e);
      }
      finally
      {
         if (!success)
         {
            try
            {
               PSSecurityFilter.logout(request, sessionId);
            }
            catch (RuntimeException e)
            {
               // just in case
               LogFactory.getLog(getClass()).error(e);
            }
         }
      }

      // get the current rhythmyx community - we'll translate to a name below
      String communityId = (String) rxSession
         .getPrivateObject(IPSHtmlParameters.SYS_COMMUNITY);

      // get the current rhythmyx locale
      String locale = (String) rxSession
         .getPrivateObject(IPSHtmlParameters.SYS_LANG);
      if (locale == null)
         locale = PSI18nUtils.DEFAULT_LANG;
      login.setDefaultLocaleCode(locale);

      // get the authenticated users roles
      List<String> roleNames = rxSession.getUserRoles();
      List<PSRole> roles = loadRoles("*");
      Map<String, PSRole> nameToRole = new HashMap<String, PSRole>();
      for (PSRole r : roles)
         nameToRole.put(r.getName().toLowerCase(), r);
      
      for (String roleName : roleNames)
      {
         PSRole r = nameToRole.get(roleName.toLowerCase());
         if (r != null)
            login.addRole(r);
      }

      // get the authenticated users communities
      PSRequest rxRequest = (PSRequest) PSRequestInfo
         .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      List<IPSGuid> commIds = new ArrayList<IPSGuid>();
      for (String commid : rxSession.getUserCommunities(rxRequest))
      {
         commIds.add(new PSGuid(PSTypeEnum.COMMUNITY_DEF, Long
            .parseLong(commid)));
      }

      if (!commIds.isEmpty())
      {
         IPSBackEndRoleMgr roleMgr = PSRoleMgrLocator.getBackEndRoleManager();
         for (PSCommunity c : roleMgr.loadCommunities(commIds
            .toArray(new IPSGuid[commIds.size()])))
         {
            if (communityId != null
               && communityId.equals(String.valueOf(c.getId())))
            {
               login.setDefaultCommunity(c.getName());
            }
            login.addCommunity(c);
         }
      }

      // get the authenticated users locales
      IPSCmsObjectMgr cmsObjectMgr = PSCmsObjectMgrLocator.getObjectManager();
      for (PSLocale l : cmsObjectMgr.findLocaleByStatus(PSLocale.STATUS_ACTIVE))
         login.addLocale(l);

      return login;
   }

   // @see IPSSecurityWs#logout(HttpServletRequest, String)
   public void logout(HttpServletRequest request, String session)
   {
      if (request == null)
         throw new IllegalArgumentException("request cannot be null");

      if (StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");

      PSSecurityFilter.logout(request, session);
   }

   // @see IPSSecurityWs#refreshSession(HttpServletRequest, String)
   public void refreshSession(HttpServletRequest request, String session)
      throws LoginException
   {
      if (request == null)
         throw new IllegalArgumentException("request cannot be null");

      if (StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");

      PSSecurityFilter.authenticate(request, session);
   }

   // @see IPSSecurityWs#filterByRuntimeVisibility(List)
   public List<IPSGuid> filterByRuntimeVisibility(List<IPSGuid> ids)
   {
      if (ids == null || ids.isEmpty())
         throw new IllegalArgumentException("ids may not be null or empty");

      List<IPSGuid> filtered = new ArrayList<IPSGuid>();

      IPSAclService svc = PSAclServiceLocator.getAclService();

      for (IPSGuid guid : ids)
      {
         PSTypeEnum type = PSTypeEnum.valueOf(guid.getType());

         // filter if supports runtime
         if (PSAclUtils.typeSupportsRuntimeVisble(type))
         {
            if (svc.getUserAccessLevel(guid).hasRuntimeAccess())
               filtered.add(guid);
         }
         // pass through if support acl but not runtime
         else if (PSAclUtils.typeSupportsAcl(type))
         {
            filtered.add(guid);
         }
         else
         {
            // an error if supplied a non-design object that doesn't support acl
            throw new IllegalArgumentException(
               "id does not represent a design object: " + guid);
         }
      }

      return filtered;
   }

   public IPSRequestContext getRequestContext()
   {
      PSRequest req = PSSecurityFilter.getCurrentRequest();
      if (req != null)
         return new PSRequestContext(req);
      
      return null;
   }

   public PSSecurityToken getSecurityToken()
   {
      PSRequest req = PSSecurityFilter.getCurrentRequest();
      if (req != null && req.hasUserSession())
         return req.getSecurityToken();
      
      return null;
   }

   public PSLogin login(String user, String password, String community,
      String localeCode) throws LoginException
   {
      if (StringUtils.isBlank(user))
         throw new IllegalArgumentException("user cannot be null or empty");

      if (StringUtils.isBlank(password))
         throw new IllegalArgumentException("password cannot be null or empty");
      
      // Do not persist any request info on login if requests already set up
      if (PSRequestInfo.isInited())
         PSRequestInfo.resetRequestInfo();
      
         PSRequestInfo.initRequestInfo(new HashMap<String,Object>());
      
      
      try
      {
         return login(new MockHttpServletRequest(), new MockHttpServletResponse(),
            user, password, null, community, localeCode);
      }
      catch (PSInternalRequestCallException e)
      {
         throw new RuntimeException(e);
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
      catch (ServletException e)
      {
         throw new RuntimeException(e);
      }
   }

   public boolean reconnectSession(PSSecurityToken token)
   {
      if (token == null)
         throw new IllegalArgumentException("token may not be null");
      
      return PSThreadRequestUtils.initUserThreadRequestByToken(token);
   }

   public void restoreRequestContext(IPSRequestContext ctx)
   {
      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");
      
      PSRequest req = PSRequest.getRequest(ctx);
      PSSecurityFilter.setRequest(req);
   }
}
