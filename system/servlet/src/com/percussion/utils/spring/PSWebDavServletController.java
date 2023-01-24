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

import org.springframework.web.servlet.mvc.ServletWrappingController;

import javax.servlet.Servlet;

/**
 * This class allows for setting the supported HTTP methods as well as the
 * servlet class name. Since the end-users are writing bean configurations to
 * initialize the controller, we don't want to require them to have to specify
 * the same options and servlet class name every time, which is tedious and
 * error prone. Instead this class simply overrides the required methods to
 * hard-code the supported methods and <code>PSWebdavServlet</code> class
 * name. 
 */
public class PSWebDavServletController extends ServletWrappingController
{
   /**
    * Default ctor, sets the supported method names.
    */
   public PSWebDavServletController()
   {
      super();
      setSupportedMethods(SUPPORTED_METHODS);
   }
   
   /**
    * Sets the servlet class to <code>PSWebdavServlet</code>.  Called by the 
    * Spring Framework after the bean is instantiated and the properties are 
    * set, this method overides that method to hard-code the servletClass
    * property and then delegates to the base class.
    */
   public void afterPropertiesSet() throws Exception
   {
      setServletClass(Class.forName(SERVLET_CLASS_NAME).asSubclass(Servlet.class));
      super.afterPropertiesSet();
   }
   
   /**
    * Constant for the <code>PSWebdavServlet</code> class name.
    */
   private static final String SERVLET_CLASS_NAME = 
      "com.percussion.webdav.PSWebdavServlet";
   
   /**
    * Static list of supported methods returned by options method. 
    */
   public final static String[] SUPPORTED_METHODS =
   {
      "GET", "HEAD", "PUT", "DELETE", "OPTIONS", "PROPFIND", 
      "PROPPATCH", "COPY", "MOVE", "MKCOL", "LOCK", "UNLOCK"
   };
}

