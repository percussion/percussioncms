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

