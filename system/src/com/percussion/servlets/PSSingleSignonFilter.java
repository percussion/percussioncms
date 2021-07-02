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
package com.percussion.servlets;

import com.percussion.server.PSServer;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.util.PSUrlUtils;
import com.percussion.utils.container.PSContainerUtilsFactory;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A filter designed to redirect the requests to the percussion cm server login
 * page if the user is not authenticated otherwise to the requested page.
 * 
 * @author bjoginipally
 * 
 */
public class PSSingleSignonFilter implements Filter
{
   /**
    * Finds the pssessionid cookie from the cookies and calls the
    * {@link #validateSession(String)}method to validate the session, if not
    * validated redirects to percussion cm server login page by passing in the
    * requested url as sys_redirect parameter.
    */
   public void doFilter(ServletRequest request, ServletResponse response,
         FilterChain chain) throws IOException, ServletException
   {
      if (request == null)
      {
         throw new IllegalArgumentException("request may not be null");
      }
      if (response == null)
      {
         throw new IllegalArgumentException("response may not be null");
      }
      if (chain == null)
      {
         throw new IllegalArgumentException("chain may not be null");
      }

      HttpServletRequest httpReq = null;
      HttpServletResponse httpResp = null;
      if (request instanceof HttpServletRequest)
      {
         httpReq = (HttpServletRequest) request;
         httpResp = (HttpServletResponse) response;
         Cookie[] cookies = httpReq.getCookies();
         String pssessid = null;
         if (cookies != null)
         {
            for (Cookie c : cookies)
            {
               if (c.getName().toLowerCase().equalsIgnoreCase("pssessionid"))
                  pssessid = c.getValue();
            }
         }
         boolean authenticated = false;
         if (pssessid != null)
         {
            authenticated = validateSession(pssessid, prepareBaseUrl(httpReq));
         }
         
         String gadgetUrlPrefix = 
            m_config.getInitParameter("gadgetUrlPrefix");
         boolean isShindigRequestor = httpReq.getHeader("x-shindig-dos") != null;
         boolean isGadgetRequest = gadgetUrlPrefix != null &&
            httpReq.getRequestURI().toLowerCase().startsWith(gadgetUrlPrefix) &&
            isShindigRequestor;

         if (!authenticated && !isGadgetRequest)
         {
            String forcedRedirectUrl = m_config.getInitParameter("forcedRedirectUrl");
            String redirectUrl = httpReq.getRequestURL().toString();
            if(forcedRedirectUrl != null && forcedRedirectUrl.length() > 0)
            {
               StringBuilder buff = new StringBuilder();
               URL temp = new URL(redirectUrl);
               buff.append(temp.getProtocol());
               buff.append("://");
               buff.append(temp.getHost());
               if(temp.getPort() != 80)
               {
                  buff.append(":");
                  buff.append(temp.getPort());
               }
               buff.append(forcedRedirectUrl);
               redirectUrl = buff.toString();
            }
            String redirect = "/"
                  + SSOSERVER_APPCONTEXT_ROOT
                  + "/login?sys_redirect="
                  + URLEncoder.encode(redirectUrl + "?"
                        + httpReq.getQueryString(), "UTF-8");
            httpResp.sendRedirect(redirect);
         }
      }
      chain.doFilter(httpReq, httpResp);
   }

   /**
    * Helper method to create the base url from the request. The returned URL
    * does not include the trailing /. Example http(s)://<Server>:<Port>. The
    * port part is included only if it is not a default port for the protocol.
    * 
    * @param httpReq assumed not <code>null</code>.
    * @return The base url fo the request, never <code>null</code>.
    */
   private String prepareBaseUrl(HttpServletRequest httpReq)
   {

      //  Not sure how this ever worked properly.  Need to refactor how we get local and remove base urls
      //  taking into account proxies;
      String baseUrl = httpReq.isSecure() ? "https://":"http://";
      baseUrl += PSServer.getHostName();
      // Check whether we need to add port
      boolean addPort = httpReq.isSecure() ? httpReq.getServerPort() != 443
            : httpReq.getServerPort() != 80;
      if (addPort)
      {
         baseUrl += ":" + httpReq.getServerPort();
      }
      return baseUrl;
   }

   /**
    * Helper method to make a http client request to Rhythmyx server to validate
    * the passed in session. Incase of an exception validating the session with
    * Rhythmyx, logs the error and returns <code>false</code>.
    * 
    * @param pssessid The session id to be validated, assumed not
    * <code>null</code>.
    * @return <code>true</code> if the session is valid otherwise
    * <code>false</code>.
    */
   private boolean validateSession(String pssessid, String baseUrl)
   {
      // make call to server
      boolean valid = false;
      HttpMethod method = null;
      try
      {
         HttpClient hc = new HttpClient();
         method = new GetMethod(baseUrl + "/" + SSOSERVER_APPCONTEXT_ROOT
               + "/__validateSession__?pssessionid=" + pssessid);
         int respCode = hc.executeMethod(method);
         if (respCode == 200)
            valid = true;
      }
      catch (Exception e)
      {
         ms_log.error(e);
      }
      finally
      {
         if (method != null)
            method.releaseConnection();
      }
      return valid;
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
    */
   public void init(FilterConfig config) throws ServletException
   {
      m_config = config;
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.servlet.Filter#destroy()
    */
   public void destroy()
   {
   // There is nothing to destroy at this point.
   }
   
   /**
    * A reference to the filter configuration. Initialized in {@link #init(FilterConfig)},
    * never <code>null</code> after that.
    */
   private FilterConfig m_config;

   /**
    * log to use, never <code>null</code>.
    */
   private static final Logger ms_log = LogManager.getLogger(PSSingleSignonFilter.class);

   /**
    * Context name of Rhythmyx server application context
    */
   private static final String SSOSERVER_APPCONTEXT_ROOT = "Rhythmyx";
}
