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

package com.percussion.servlets;

import com.percussion.content.IPSMimeContentTypes;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.security.IPSSecurityErrors;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestParsingException;
import com.percussion.server.PSServer;
import com.percussion.server.PSUserSessionManager;
import com.percussion.tools.PSURIEncoder;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.utils.tools.IPSUtilsConstants;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import javax.security.auth.login.LoginException;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.percussion.auditlog.PSActionOutcome;
import com.percussion.auditlog.PSAuditLogService;
import com.percussion.auditlog.PSAuthenticationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * This servlet will process form based login/out calls. The servlet is mapped
 * to the "/Rhythmyx/login" and "/Rhythmyx/logout" request roots. The servlet
 * will look for pages named "login.jsp", "error.jsp", and "logout.jsp" in the
 * "<webapp root>/user" directory. Any or all of these pages may be defined. For
 * each found, that page will be used by Rhythmyx in place of the default
 * login/error and logout forms. If a custom login page is found, but a custom
 * error page is not found, then the custom login page will be used as the error
 * page.
 */
public class PSLoginServlet extends HttpServlet
{
   /**
    * Serial version id
    */
   private static final long serialVersionUID = 1L;
   private PSAuditLogService psAuditLogService=PSAuditLogService.getInstance();
   private PSAuthenticationEvent psAuthenticationEvent;


   /**
    * Handles requests to login and logout. Initial GET requests to "/login" are
    * returned an include of the correct login page (standard or custom if
    * defined). JAAS authentication will be performed for POST request from the
    * login page that provide credentials (the "j_username" and "j_password"
    * request params). Successful authentication will redirect to the originally
    * requested page as specified by the "RX_REDIRECT_URL" session attribute.
    * Authentication failures will return an include of either the custom error
    * page if defined, or else the appropriate login form again. Requests to
    * "/logout" will call {@link javax.servlet.http.HttpSession#invalidate()}
    * and redirect the user to the appropriate logout page ((standard or custom
    * if defined).
    *
    *
    * @see HttpServlet#service(HttpServletRequest, HttpServletResponse) for
    * other details.
    */
   protected void service(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException
   {
      // see if login or logout
      String url = request.getServletPath();
      // if login, logout first, then do login
      if (url.equals("/login"))
      {
         login(request, response);
      }
      else if (url.equals("/logout"))
      {
         logout(request, response);
      }
   }

   /**
    * Calculates the url to use when redirecting after successful form login and
    * appends it to the supplied login page url as a query string parameter.
    *
    * @param request The current request, may not be <code>null</code>.
    * @param loginPage The login page request url to which the result is
    * appended, may not be <code>null</code> or empty.
    *
    * @return The login page value with the redirect url query string parameter
    * appended, never <code>null</code> or empty.
    */
   public static String addRedirect(HttpServletRequest request,
                                    String loginPage)
   {
      if (request == null)
         throw new IllegalArgumentException("request may not be null");

      if (StringUtils.isBlank(loginPage))
      {
         throw new IllegalArgumentException(
                 "loginPage may not be null or empty");
      }

      String redirect = null;
      try
      {
         boolean isBehindProxy = PSServer.isRequestBehindProxy(request);
         if(isBehindProxy){
            redirect =PSServer.getProxyURL(request,false);
            if(redirect == ""){
               redirect = request.getRequestURL().toString();
            }
         }else{
            redirect = request.getRequestURL().toString();
         }
      }
      catch (NullPointerException ex)
      {
         // Default
         redirect = RHYTHMYX_INDEX_PAGE;
      }

      String sep = "?";
      // if the original request was for the login page, redirect to CX
      if (redirect.endsWith(loginPage))
      {
         redirect = RHYTHMYX_INDEX_PAGE;
      }
      else if (request.getQueryString() != null)
      {
         redirect += sep + request.getQueryString();
      }


      loginPage += sep + IPSHtmlParameters.SYS_REDIRECT + "=" +
              PSURIEncoder.escape(redirect);

      // loginPage += sep + IPSHtmlParameters.SYS_REDIRECT + "=" +PSURIEncoder.escape("/cm");
      return loginPage;
   }

   /**
    * Handles thSte logout request.
    *
    * @param request The current request, assumed not <code>null</code>.
    * @param response The current response, assumed not <code>null</code>.
    *
    * @throws IOException If there are any errors redirecting to the logout
    * page.
    * @throws ServletException If there are any other errors
    */
   private void logout(HttpServletRequest request, HttpServletResponse response)
           throws IOException, ServletException
   {
       try {
           psAuthenticationEvent = new PSAuthenticationEvent(PSActionOutcome.SUCCESS.name(), PSAuthenticationEvent.AuthenticationEventActions.logout, request, request.getRemoteUser());
           psAuditLogService.logAuthenticationEvent(psAuthenticationEvent);
       }catch (Exception e){
          log.error(e.getMessage());
          log.debug(e.getMessage(), e);
       }
      HttpSession session = request.getSession();
      if (session != null)
      {
         PSSecurityFilter.logout(request, (String) session.getAttribute(
                 IPSHtmlParameters.SYS_SESSIONID));
      }

      // return logout page
      response.setContentType(CONTENT_TYPE_HEADER_VAL);
      request.getRequestDispatcher(getLogoutPage()).include(request,
              response);

   }

   /**
    * Handles the login request.
    *
    * @param request The current request, assumed not <code>null</code>.
    * @param response The current response, assumed not <code>null</code>.
    *
    * @throws IOException If there are any errors including the login page or
    * redirecting to the originally requested page.
    * @throws ServletException If there are any other errors.
    */
   private void login(HttpServletRequest request, HttpServletResponse response)
           throws ServletException, IOException
   {
      // see if initial request for login page, or a post with credentials
      String uid = null;
      String pwd = null;
      String locale=null;

      // Checking for maximum users allowed in the system, if reached maximum, then don't allow more users
         if(!PSUserSessionManager.checkIfNewUserAllowed()){
            String errorText = "Maximum number of users are logged in,try again after some time!!";

            // add error param
            request = new HttpServletRequestWrapper(request) {
               @Override
               public String getParameter(String param)
               {
                  if (param.equals("j_error"))
                     return errorText;

                  return super.getParameter(param);
               }};

            response.setContentType(CONTENT_TYPE_HEADER_VAL);
            request.getRequestDispatcher(getErrorPage()).include(request,
                    response);
            return;
         }


      if (request.getMethod().equalsIgnoreCase("POST"))
      {
         PSRequest psreq = (PSRequest) PSRequestInfo
                 .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);

         if (psreq == null)
         {
            // this should never happen
            throw new RuntimeException(
                    "The request was not properly initialized by the security filter");
         }
         try
         {
            psreq.parseBody();
            uid = psreq.getParameter("j_username");
            pwd = psreq.getParameter("j_password");
            locale= psreq.getParameter("j_locale");

            if(locale!=null)
               request.getSession().setAttribute(PSI18nUtils.USER_SESSION_OBJECT_SYS_LANG, locale);
         }
         catch (PSRequestParsingException e)
         {
            throw new ServletException(e);
         }
      }

      String redirect = request.getParameter(IPSHtmlParameters.SYS_REDIRECT);
      if (isValidRedirectUri(request, redirect) ){
         request.getSession().setAttribute(REDIRECT_URL, redirect);
      }
      if (!StringUtils.isBlank(uid))
      {
         // handle authentication
         authenticate(request, response, uid, pwd);


      }
      else
      {
         // return login page
         response.setContentType(CONTENT_TYPE_HEADER_VAL);
         request.getRequestDispatcher(getLoginPage()).include(request,
                 response);
      }
   }

   /**
    * Determines if a redirect URI is valid and safe (XSS).
    * A redirection URI should be to the same host and a valid
    * URI.
    * @param request never null.
    * @param uri maybe null or invalid <code>false</code> will be returned.
    * @return true if a valid redirect uri.
    */
   protected static boolean isValidRedirectUri(HttpServletRequest request, String uri) {
      boolean rvalue = false;
      if (StringUtils.isBlank(uri)) return false;
      try
      {
         URI targetUri = new URI(uri);
         //See if its just a path
         if (targetUri.getHost() == null
                 && targetUri.getAuthority() == null
                 && targetUri.getScheme() == null &&
                 isNotBlank(targetUri.getPath())) {
            rvalue = true;
         }
         else {
            URI requestUri  = new URI(request.getRequestURL().toString());
            rvalue = ObjectUtils.equals(requestUri.getHost(), targetUri.getHost()) &&
                    ObjectUtils.equals(requestUri.getPort(), targetUri.getPort()) &&
                    ObjectUtils.equals(requestUri.getScheme(), targetUri.getScheme());
         }
      }
      catch (URISyntaxException e)
      {
         log.error("Bad redirect uri: {} , Error : {} ",  uri, e.getMessage());
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
         rvalue = false;
      }

      if(PSServer.isRequestBehindProxy(request)){
         rvalue=true;
      }
      if ( ! rvalue )
         log.error("Bad redirect uri: {}", uri);
      return rvalue;
   }




   /**
    * Performs the authentication.  If successful, the user is redirected to the
    * originally requested page, if it fails, then the appropriate error page is
    * included.
    *
    * @param request The current request, assumed not <code>null</code>.
    * @param response The current response, assumed not <code>null</code>.
    * @param uid The user id to use, assumed not <code>null</code> or empty.
    * @param pwd The password, may be <code>null</code> or empty.
    * @throws IOException
    * @throws ServletException
    */
   private void authenticate(HttpServletRequest request,
                             HttpServletResponse response, String uid, String pwd) throws IOException,
           ServletException
   {
      try
      {

         HttpSession sess = request.getSession(true);
//         event.setSessionid(sess.getId());

         String redirect = (String) sess.getAttribute(REDIRECT_URL);
         if (redirect == null)
            redirect = RHYTHMYX_INDEX_PAGE;

         request = PSSecurityFilter.authenticate(request, response, uid,
                 pwd);


         response.sendRedirect(redirect);

         sess.removeAttribute(REDIRECT_URL);
          psAuthenticationEvent=new PSAuthenticationEvent(PSActionOutcome.SUCCESS.name(), PSAuthenticationEvent.AuthenticationEventActions.login,request,uid);
          psAuditLogService.logAuthenticationEvent(psAuthenticationEvent);
      }
      catch (LoginException e)
      {
         psAuthenticationEvent=new PSAuthenticationEvent(PSActionOutcome.FAILURE.name(), PSAuthenticationEvent.AuthenticationEventActions.login,request,uid);
         psAuditLogService.logAuthenticationEvent(psAuthenticationEvent);
         Exception ex;

         if (e instanceof PSMissingRoleException)
         {
            ex = e;
         }
         else
         {
            // create error message
            ex =
                    new PSAuthenticationFailedException(
                            IPSSecurityErrors.GENERIC_AUTHENTICATION_FAILED, null);
         }

         final String errorText = ex.getLocalizedMessage();
         m_log.debug(errorText, e);

         // add error param
         request = new HttpServletRequestWrapper(request) {
            @Override
            public String getParameter(String param)
            {
               if (param.equals("j_error"))
                  return errorText;

               return super.getParameter(param);
            }};

         response.setContentType(CONTENT_TYPE_HEADER_VAL);
         request.getRequestDispatcher(getErrorPage()).include(request,
                 response);
      }
   }

   /**
    * Gets the appropriate page to include when authentication fails.
    *
    * @return The relative path to the error page, never <code>null</code> or
    * empty.
    */
   private String getErrorPage()
   {
      File errorPage = new File(getUserDirectory(), ERROR_PAGE);
      if (errorPage.exists())
         return "/" + USER_DIR + "/" + ERROR_PAGE;

      return getLoginPage();
   }

   /**
    * Gets the appropriate page to include when returning the login page.
    *
    * @return The relative path to the login page, never <code>null</code> or
    * empty.
    */
   private String getLoginPage()
   {
      File loginPage = new File(getUserDirectory(), LOGIN_PAGE);
      if (loginPage.exists())
         return "/" + USER_DIR + "/" + LOGIN_PAGE;

      return "/rxlogin.jsp";
   }

   /**
    * Gets the appropriate page to include when returning the logout page.
    *
    * @return The relative path to the logout page, never <code>null</code> or
    * empty.
    */
   private String getLogoutPage()
   {
      File logoutPage = new File(getUserDirectory(), LOGOUT_PAGE);
      if (logoutPage.exists())
         return USER_DIR + "/" + LOGOUT_PAGE;

      return "rxlogout.jsp";
   }

   /**
    * Get the absolute path to the user sub-directory of the web application in
    * which this servlet is running.
    *
    * @return The file, never <code>null</code>.
    */
   private File getUserDirectory()
   {
      return new File(getServletDirectory(), USER_DIR);
   }

   /**
    * Get the path to the directory of the web application in which this
    * servlet is running.
    *
    * @return The path, never <code>null</code>.
    */
   private File getServletDirectory()
   {
      return new File(getServletContext().getRealPath("/WEB-INF")).getParentFile();
   }

   /**
    * Default Rhythmyx page constant
    */
   private static final String RHYTHMYX_INDEX_PAGE = "index.jsp";

   /**
    * Constant for the "user" directory.
    */
   private static final String USER_DIR = "user";

   /**
    * Name of the user defined login page.
    */
   private static final String LOGIN_PAGE = "login.jsp";

   /**
    * Name of the user defined logout page.
    */
   private static final String LOGOUT_PAGE = "logout.jsp";

   /**
    * Name of the user defined error page.
    */
   private static final String ERROR_PAGE = "error.jsp";

   /**
    * This is used to record the original request that the user was attempting
    * when redirected to the login page while doing form based authentication.
    */
   public static final String REDIRECT_URL = "RX_REDIRECT_URL";

   /**
    * logger
    */
   private static final Logger log = LogManager.getLogger(PSLoginServlet.class);

   /**
    * The Content-Type header value to set when returning included pages,
    * currently text/html with the UTF-8 encoding.
    */
   private static final String CONTENT_TYPE_HEADER_VAL =
           IPSMimeContentTypes.MIME_TYPE_TEXT_HTML + ";charset=" +
                   IPSUtilsConstants.RX_STANDARD_ENC;

   /**
    * Static logger
    */
   private Logger m_log = LogManager.getLogger(PSLoginServlet.class);
}
