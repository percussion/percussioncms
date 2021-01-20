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
package com.percussion.utils.spring;

import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Dispatch servlet for WebDAV.  The <code>DispatcherServlet</code> class does 
 * not simply delegate all calls to the controller, and so the required HTTP 
 * call to OPTIONS would not return the correct values. This class overrides the 
 * {@link #service(HttpServletRequest, HttpServletResponse)}
 * method to by-pass some unnecessary processing and simply call the 
 * <code>doService()</code> method in the base class.
 */
public class PSPassThruDispatcherServlet extends DispatcherServlet
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   /**
    * Delegates directly to the <code>doService()</code> method in the base 
    * class.  See base class for more info. 
    */
   @Override
   protected void service(HttpServletRequest request, 
      HttpServletResponse response) throws ServletException, IOException
   {
      try
      {
         doService(request, response);
      }
      catch (Exception e)
      {
         throw new ServletException(e);
      }
   }
}

