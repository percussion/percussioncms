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
