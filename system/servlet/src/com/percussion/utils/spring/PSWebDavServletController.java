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

