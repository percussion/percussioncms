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

package com.percussion.security;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSUserInfo;
import com.percussion.data.PSDataExtractionException;
import com.percussion.data.PSUserContextExtractor;
import com.percussion.design.objectstore.PSLiteral;
import com.percussion.design.objectstore.PSLiteralSet;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.server.*;
import com.percussion.services.security.PSJaasUtils;
import com.percussion.services.security.PSServletRequestWrapper;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.request.PSRequestInfo;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author stephenbolton
 *
 *  This Class wraps useful functions around PSRequestInfo which is in rxutils.jar and does not have access to com.percussion.services
 */
public class PSThreadRequestUtils
{

   /**
    * Logger for this service.
    */
   private static Log log = LogFactory.getLog(PSThreadRequestUtils.class);
   
   public static PSRequest getPSRequest()
   {
      return (PSRequest)PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
   }
   
   
   public static PSRequest initServerThreadRequest()
   {
      PSRequest req = PSRequest.getContextForRequest();
      if(PSRequestInfo.isInited())
      {
         log.debug("Thread request already initialized method should run only once per thread",new Throwable());
         PSRequestInfo.resetRequestInfo();
      }
      PSRequestInfo.initRequestInfo(new HashMap<String,Object>());
      PSRequestInfo.setRequestInfo(PSRequestInfo.KEY_PSREQUEST, req);
      PSRequestInfo.setRequestInfo(PSRequestInfo.KEY_USER, PSSecurityProvider.INTERNAL_USER_NAME);
      return req;
   }
   
   public static boolean initUserThreadRequestByToken(PSSecurityToken token)
   {
      if (token == null)
         throw new IllegalArgumentException("token may not be null");
      
      PSUserSession sess = PSUserSessionManager.getUserSession(
         token.getUserSessionId());
      
      if (sess == null)
         return false;
      
      if (!PSRequestInfo.isInited())
         PSRequestInfo.initRequestInfo(new HashMap<String, Object>());
      
      PSRequest psreq = (PSRequest) PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      if (psreq != null && psreq.hasUserSession() && 
         psreq.getUserSessionId().equals(sess.getId()))
      {
         // same session already setup on this thread, nothing to do
         return true;
      }
      
      psreq = new PSRequest(token);
      psreq.setServletRequest(new PSServletRequestWrapper(
         psreq.getServletRequest(), null));
      updateHttpSession(psreq.getServletRequest(), sess);
      updateRequestInfo(psreq);
      
      return true;
   }
   
   public static PSRequest initUserThreadRequest(Map<String,Object> threadContextMap)
   {
      final Map<String, Object> requestInfoMap = new HashMap<String,Object>(threadContextMap);
      PSRequest request = (PSRequest) requestInfoMap.get(PSRequestInfo.KEY_PSREQUEST);
      requestInfoMap.put(PSRequestInfo.KEY_PSREQUEST, request.cloneRequest());
      
      if(PSRequestInfo.isInited())
      {
         log.debug("Thread request already initialized method should run only once per thread",new Throwable());
         PSRequestInfo.resetRequestInfo();
      }
      
      PSRequestInfo.initRequestInfo(requestInfoMap);
      
      return request;
   }


   public static PSRequest changeToInternalRequest(boolean systemUser)
   {
      return changeToInternalRequest(null,systemUser);
   }
   
   public static PSRequest changeToInternalRequest(PSRequest req,boolean systemUser)
   {
      String currentUser = "";
      Map<String, Object> currentRequestMap = null;
      if (!PSRequestInfo.isInited())
      {
         currentRequestMap = new HashMap<String,Object>();
         currentRequestMap.put(PSRequestInfo.KEY_ORIG_MAP, req);
      }
      else
      {
         currentUser = (String)PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_USER);
         currentRequestMap = PSRequestInfo.getRequestInfoMap();
         PSRequestInfo.resetRequestInfo();
      }
      
      PSRequestInfo.initRequestInfo(new HashMap<String,Object>());
      
      if(systemUser)
         req = PSRequest.getContextForRequest();
      else
         req = req.cloneRequest();
      
      PSRequestInfo.setRequestInfo(PSRequestInfo.KEY_PSREQUEST, req);
      PSRequestInfo.setRequestInfo(PSRequestInfo.KEY_ORIG_MAP,currentRequestMap);
     
      log.debug("Changing to internal request for user "+currentUser + "on thread "+ Thread.currentThread().getName()+ " id "+Thread.currentThread().getId());
      if(systemUser  || currentUser==null)
         PSRequestInfo.setRequestInfo(PSRequestInfo.KEY_USER, PSSecurityProvider.INTERNAL_USER_NAME);

      if(currentUser==null)
         log.debug("No current user set on thread when changing to internal request");

      return req;
   }

   public static PSRequest restoreOriginalRequest()
   {

      log.debug("Restoring original request on thread "+ Thread.currentThread().getName()+ " id "+Thread.currentThread().getId());

      Map<String, Object> origRequestMap = (Map<String, Object>)PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_ORIG_MAP);
      PSRequest currentRequest = (PSRequest) PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      currentRequest.release();
      PSRequestInfo.resetRequestInfo();
    
      PSRequest oldRequest = null;
      if (origRequestMap!=null)
      {
         oldRequest = (PSRequest) origRequestMap.get(PSRequestInfo.KEY_PSREQUEST);
         PSRequestInfo.initRequestInfo(origRequestMap);
      }
      else
      {
         origRequestMap = new HashMap<String,Object>();
      }
      return oldRequest;
   }
   
   /**
    * Updates the http session from the supplied request with the information
    * from the supplied user session.
    * 
    * @param req The request to update, may not be <code>null</code>.
    * @param sess The user session to use, may not be <code>null</code>.
    */
   public static void updateHttpSession(HttpServletRequest req,
         PSUserSession sess)
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");
      if (sess == null)
         throw new IllegalArgumentException("sess may not be null");

      Subject subject = null;
      HttpSession httpSession = req.getSession(true);
      PSUserEntry[] userEntries = sess.getAuthenticatedUserEntries();
      if (userEntries.length > 0)
      {
         subject = PSJaasUtils.userEntryToSubject(userEntries[0], null);
         httpSession.setAttribute(PSRequestInfo.SUBJECT, subject);
      }
      
      // update the wrappers subject
      if (req instanceof PSServletRequestWrapper)
         ((PSServletRequestWrapper) req).setSubject(subject);
      
      if (PSRequestInfo.isInited())
         PSRequestInfo.setRequestInfo(PSRequestInfo.SUBJECT, subject);
      
      httpSession.setAttribute(IPSHtmlParameters.SYS_SESSIONID, sess.getId());
   }
   
   /**
    * Updates the {@link PSRequestInfo} object with the supplied request.
    * 
    * @param req The request, assumed not <code>null</code>.
    */
   private static void updateRequestInfo(PSRequest req)
   {
      PSRequestInfo.setRequestInfo(PSRequestInfo.KEY_PSREQUEST, req);
      
      if (req.hasUserSession())
      {
         PSUserSession sess = req.getUserSession();
         String locale = (String) sess.getPrivateObject(
            PSI18nUtils.USER_SESSION_OBJECT_SYS_LANG);
         if (locale != null)
            PSRequestInfo.setRequestInfo(PSRequestInfo.KEY_LOCALE, locale);
      }

      String username = req.getServletRequest().getRemoteUser();
      if (!StringUtils.isBlank(username))
         PSRequestInfo.setRequestInfo(PSRequestInfo.KEY_USER, username);      
   }
   
   
   public static IPSRequestContext getReqCtx()
   {
       IPSRequestContext requestContext = (IPSRequestContext)PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_PSREQUESTCONTEXT);
       if (requestContext==null)
       {
          PSRequest request = (PSRequest)PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
          if (request==null)
             throw new IllegalArgumentException("No PSRequest found in PSRequestInfo");
          requestContext = new PSRequestContext(request);
          PSRequestInfo.setRequestInfo(PSRequestInfo.KEY_PSREQUESTCONTEXT, new PSRequestContext(request));

       }
       return requestContext;
   }
   
   
   public static PSUserInfo getUserInfo() throws PSCmsException
   {
      PSUserInfo userInfo = (PSUserInfo) PSRequestInfo.getRequestInfo("USER_INFO");
      if (userInfo == null)
         userInfo = new PSUserInfo(getReqCtx());
         PSRequestInfo.setRequestInfo("USER_INFO", userInfo);
      return userInfo;
   }


   public static Integer getUserAccessLevel() throws PSAuthenticationRequiredException, PSAuthorizationException
   {
      Integer accessLevel = (Integer)PSRequestInfo.getRequestInfo("USER_ACCESS_LEVEL");
      if(accessLevel == null)
      {
          initServerInformation();
          PSRequest request = getPSRequest();
          com.percussion.security.PSSecurityToken tok = request.getSecurityToken();
          accessLevel = systemAclHandler.getUserAccessLevel(tok);
          PSRequestInfo.setRequestInfo("USER_ACCESS_LEVEL", new Integer(accessLevel));
      } else
      {
          accessLevel = accessLevel.intValue();
      }
      return accessLevel;
   }
   
   private static void initServerInformation()
   {
       if(systemAclHandler == null)
           synchronized(initLock)
           {
               if(systemAclHandler == null)
               {
                   PSServerConfiguration config = PSServer.getServerConfiguration();
                   com.percussion.design.objectstore.PSAcl acl = config.getAcl();
                   systemAclHandler = new PSAclHandler(acl);
               }
           }
   }

   public static boolean isInternalUser()
         throws PSDataExtractionException
      {
       
         boolean isInternalUser = false;

         String userCtx = "User/Name";
         PSLiteralSet userSet = (PSLiteralSet)PSUserContextExtractor.getUserContextInformation(userCtx,
               getPSRequest(), null);

         if (userSet != null)
         {
            Iterator userNames = userSet.iterator();

            String userName = null;
            while (userNames.hasNext() && !isInternalUser)
            {
               userName = ((PSLiteral)userNames.next()).getValueText();
               if (PSSecurityProvider.INTERNAL_USER_NAME.equals(userName))
               {
                  isInternalUser = true;
               }
            }
         }

         return isInternalUser;
      }
   private static PSAclHandler systemAclHandler = null;
   private static final Object initLock = new Object();
}
