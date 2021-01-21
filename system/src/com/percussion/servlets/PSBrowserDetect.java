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

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.bitwalker.useragentutils.Browser;
import nl.bitwalker.useragentutils.UserAgent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * BrowserDetection Filter Detects which browser a user is running. If the
 * browser is IE and it is not version 8, they get a warning.
 * 
 * @author wesleyhirsch
 */
public class PSBrowserDetect implements Filter
{
   static Log log = LogFactory.getLog(PSBrowserDetect.class);

   private static final String cookieName = "unsupportedBrowserWarningSeen";

   private static final String warningFile = "unsupportedWarning.jsp";

   /*
    * (non-Javadoc) Constructor which simply calls super(); Nothing more needs
    * to be done.
    */
   public PSBrowserDetect()
   {
      super();
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.servlet.Filter#destroy()
    */
   public void destroy()
   {
      // Don't need to do anything

   }

   /**
    * BrowserDetection Filter Detects which browser a user is running. If the
    * browser is IE and it is not version 8, 9, 10, or 11 they get a warning.
    * 
    * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
    *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
    */
   public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
         ServletException
   {
      try
      {
         // log.debug("PSBrowserDetect: In doFilter()");
         // Ensure that we're actually handling an http request.
         if (request instanceof HttpServletRequest)
         {

            // First off, check whether we have a cookie which lets us bypass
            // this whole process.
            Cookie[] cookies = ((HttpServletRequest) request).getCookies();
            if (cookies != null)
            {
               for (int i = 0; i < cookies.length; i++)
               {
                  if (cookies[i].getName().equals(cookieName))
                  {
                     if (Boolean.parseBoolean(cookies[i].getValue()))
                     {
                        // log.debug("PSBrowserDetect: Have Cookie, skipping.");
                        // Optimization. If we have the cookie, they've already
                        // seen this. NOOP the function and get out as fast as
                        // possible.
                        chain.doFilter(request, response);
                        return;
                     }
                     else
                     {
                        // log.debug("PSBrowserDetect: Have Cookie, but false.  Skipping");
                        // This situation comes up if they're attempting to
                        // visit a page after having been tagged for warning.
                        // Because the warning page is a page itself, they need
                        // a cookie as a flag to noop this detection so we don't
                        // get an infinite loop.
                        chain.doFilter(request, response);
                        return;
                     }
                  }
               }
            }

            boolean showWarningMessage = false;
            Integer browserMajorVersion;
            Browser browserGroup;
            // Ok, since we know we have an HttpServletRequest or a sub-class,
            // lets go ahead and assume that and get the User-Agent string from
            // the headers.
            // Also, parse it and return the UserAgent object.
            // Skip nagios for SaaS
            String userAgentString = ((HttpServletRequest) request).getHeader("User-Agent");
            if (userAgentString != null && !userAgentString.equals("Jakarta Commons-HttpClient/3.1")
                  && !userAgentString.startsWith("Java/") && !userAgentString.contains("nagios") && !userAgentString.contains("LogicMonitor"))
            {
               UserAgent userAgent = UserAgent.parseUserAgentString(userAgentString);
               // log.debug("PSBrowserDetect: UserAgent: " +
               // userAgent.getBrowser());
               // log.debug("PSBrowserDetect: Support level for " +
               // userAgent.getBrowser() + " is " + supported);
               if (!(userAgentString.contains("rv:11") && userAgentString.contains("Windows NT 6.1")))
               {
                  browserMajorVersion = new Integer(userAgent.getBrowserVersion().getMajorVersion());
                  browserGroup = userAgent.getBrowser().getGroup();
                  // detect IE11 on windows,  uses different mechanism
                  
                  // If the browser is IE and its major version is not 8, 9, 10, or
                  // 11 show
                  // warning page, else redirects to an appropriate location.
                  showWarningMessage = ((browserGroup.equals(Browser.IE) && (browserMajorVersion < 11 )));

                  if (showWarningMessage)
                  {
                      log.info("PSBrowserDetect: Support level for " + userAgent.getBrowser() + " is not supported.");
                  }
               }
          }
            chain.doFilter(request, response);
            return;
         }
      }
      catch (Exception e)
      {
         log.debug("Failure parsing browser User-Agent " + ((HttpServletRequest) request).getHeader("User-Agent"));
      }
      chain.doFilter(request, response);
      return;
   }

   private void forwardToWarning(ServletRequest request, ServletResponse response) throws ServletException, IOException
   {
      // Creates and sets a cookie to be sent back. We need a cookie to
      // prevent infinite redirects.
      Cookie seenCookie = new Cookie(cookieName, "false");
      seenCookie.setPath("/"); // Sets /Rhythmyx otherwise.
      seenCookie.setMaxAge(60 * 5); // 5 Minutes so that it can time
                                    // out quickly if they try to
                                    // navigate away.
      seenCookie.setSecure(true);
      ((HttpServletResponse) response).addCookie(seenCookie); // Breaks
                                                              // infinite
                                                              // recursion.

      RequestDispatcher dispatcher = request.getRequestDispatcher(warningFile);
      dispatcher.forward(request, response);
      return; // We've got a broken browser. Kill the request.
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
    */
   public void init(FilterConfig config) throws ServletException
   {

   }

}
