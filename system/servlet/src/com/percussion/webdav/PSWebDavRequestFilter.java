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
package com.percussion.webdav;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This servlet picks out requests that must be handled by the webdav servlet
 * and invokes the webdav servlet instead of the default.
 * 
 * @author dougrand
 *
 */
public class PSWebDavRequestFilter implements Filter
{
   /**
    * log to use, never <code>null</code>.
    */
    private static final Logger ms_log = LogManager.getLogger(PSWebDavRequestFilter.class);
   /**
    * If one of these methods is matched, then the request will be forwarded.
    * Not every webdav method is included because most only make sense once 
    * we're in the tree.
    */
   private static Set<String> ms_webdavmethods = new HashSet<>();
   
   static {
      ms_webdavmethods.add("PROPFIND");
      ms_webdavmethods.add("PROPPATCH");
   }
   
   public void destroy()
   {
      /*
       * Nothing to do for destroy 
       */
   }

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
      
      // Redirect webdav methods to the webdav servlet, regardless of path
      HttpServletRequest httprequest = (HttpServletRequest) request;
      HttpServletResponse httpresponse = (HttpServletResponse) response;
      if (ms_webdavmethods.contains(httprequest.getMethod().toUpperCase()))
      {
         // If the request doesn't include the servlet, do a redirect
         if (!httprequest.getServletPath().startsWith("/rxwebdav"))
         {
            httpresponse.sendRedirect(httpresponse.encodeRedirectURL("/Rhythmyx/rxwebdav"));
            return;
         }
      }
      chain.doFilter(request, response);
   }

   public void init(FilterConfig arg0) throws ServletException
   {
      /*
       * Nothing to do for destroy 
       */
   }

}
