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
