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
package com.percussion.servlets;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * Sets the request character encoding.
 * The encoding can be specified by requestEncoding filter parameter.
 * If the filter parameter is not specified, the value UTF-8 is used.
 * This filter was created based on the Tomcat wiki article
 * <a href="http://wiki.apache.org/tomcat/Tomcat/UTF-8">describing how
 * to make Tomcat to work with UTF8</a>.
 *
 * @author Andriy Palamarchuk
 */
public class PSCharacterSetFilter implements Filter
{
   // see base
   public void doFilter(ServletRequest request, ServletResponse response,
         FilterChain next) throws IOException, ServletException
   {
      request.setCharacterEncoding(m_encoding);
      next.doFilter(request, response);
   }

   /**
    * Reads the filter configuration data.
    */
   public void init(FilterConfig config)
   {
      m_encoding = config.getInitParameter("requestEncoding");
      if (m_encoding == null)
      {
         m_encoding = "UTF-8";
      }
   }

   /**
    * Does nothing.
    */
   public void destroy()
   {
   }

   private String m_encoding;
}
