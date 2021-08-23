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

package com.percussion.utils.servlet;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang.StringUtils;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * Initialized by the servlet during its initialization, provides utility
 * methods such as determining configuration file paths relative to the servlet
 * root directory.  The {@link #initialize(ServletContext)} method must be 
 * called once before any other methods may be called.  This should be done by
 * the servlet during its initialization.
 */
@SuppressFBWarnings("REQUESTDISPATCHER_FILE_DISCLOSURE")
public class PSServletUtils
{
   /**
    * Constant for the name of the WEB-INF directory below the servlet root 
    * directory.
    */
   public static final String WEB_INF = "WEB-INF";

   /**
    * The name of the directory below the servlet WEB-INF directory that 
    * contains the system configuration files used by the servlet.
    */
   public static final String SYS_CONFIG_DIR = "config";

   /**
    * The name of the directory below the servlet WEB-INF directory that 
    * contains the user defined configuration files.
    */
   public static final String USER_CONFIG_DIR = "user";

   /**
    * The name of the Spring configuration file that specifies server beans that
    * may be implementation specific.
    */
   public static final String SERVER_BEANS_FILE_NAME = "server-beans.xml";

   /**
    * The name of the Spring configuration file that specified design beans that
    * are not modified for a specific implementation.
    */
   public static final String DEPLOYER_BEANS_FILE_NAME = "deployer-beans.xml";
   
   /**
    * The name of the Spring configuration file that specified design beans that
    * are not modified for a specific implementation.
    */
   public static final String DESIGN_BEANS_FILE_NAME = "design-beans.xml";

   /**
    * The name of the Spring configuration file that specifies cataloger beans
    */
   public static final String CATALOGER_BEANS_FILE_NAME = "cataloger-beans.xml";   
   
   /**
    * The name of the user dispatch servlet config file name, located in the 
    * same directory as the user beans config files 
    * (see {@link #getUserSpringConfigDir()}.
    */
   public static final String USER_DISPATCH_SERVLET_FILE_NAME = 
      "UserDispatcher-servlet.xml";

   /**
    * The name of the Spring configuration file that specified server beans that
    * are not modified for a specific implementation.
    */
   public static final String BEANS_FILE_NAME = "beans.xml";
   
   /**
    * The name of the spring configuration directory, located below the 
    * {@link #SYS_CONFIG_DIR}.
    */
   public static final String SPRING_CONFIG_DIR = "spring";
   
   /**
    * The name of the bean that defines the datasource resolver. 
    */
   public static final String DATASOURCE_RESOLVER_NAME = "sys_datasourceResolver";
   
   /**
    * The name of the bean that defines the datasource resolver in 6.0.  Used
    * by the Installer for 6.0 -> 6.X upgrades.
    */
   public static final String DATASOURCE_RESOLVER_NAME_60 = "datasourceResolver";
   
   /**
    * The name of the bean that defines the hibernate dialects. 
    */
   public static final String HIBERNATE_DIALECTS_NAME = "sys_hibernateDialects";
   
   /**
    * Constant for the role mgr bean name
    */ 
   public static final String ROLE_MGR_BEAN_NAME = "sys_roleMgr";      
   
   /**
    * Private ctor to enforce static use of the class.
    */
   private PSServletUtils()
   {

   }


   /**
    * Returns a file reference to the spring config directory. See 
    * {@link #SPRING_CONFIG_DIR} for more info.
    * 
    * @return The file, never <code>null</code>.
    * 
    * @throws IllegalStateException if {@link #initialize(ServletContext)} has
    * not been called.
    */
   public static File getSpringConfigDir()
   {
      if (m_servletContext == null)
         throw new IllegalStateException("initialize() must be called first");

      return new File(getConfigDir(), SPRING_CONFIG_DIR);
   }   

   /**
    * Returns a file reference to the user spring config directory. 
    * 
    * @return The file, never <code>null</code>.
    * 
    * @throws IllegalStateException if {@link #initialize(ServletContext)} has
    * not been called.
    */
   public static File getUserSpringConfigDir()
   {
      if (m_servletContext == null)
         throw new IllegalStateException("initialize() must be called first");

      return new File(getUserConfigDir(), SPRING_CONFIG_DIR);
   }   
   
   /**
    * Returns a path to the system spring config directory rooted relative to 
    * the servlet directory. See {@link #SPRING_CONFIG_DIR} for more info.
    * 
    * @return The path, never <code>null</code> or empty.
    */
   public static String getSpringConfigPath()
   {
      return "/" + WEB_INF + "/" + SYS_CONFIG_DIR + "/" + SPRING_CONFIG_DIR;
   }      

   /**
    * Returns a path to the system spring config directory rooted relative to 
    * the servlet directory. See {@link #SPRING_CONFIG_DIR} for more info.
    * 
    * @return The path, never <code>null</code> or empty.
    */
   public static String getUserSpringConfigPath()
   {
      return "/" + WEB_INF + "/" + SYS_CONFIG_DIR + "/" + USER_CONFIG_DIR + "/" 
         + SPRING_CONFIG_DIR;
   }      
   
   /**
    * Returns a file reference to the system config directory located below the 
    * servlet directory.  See {@link #SYS_CONFIG_DIR} for more info.
    * 
    * @return The file, never <code>null</code>.
    * 
    * @throws IllegalStateException if {@link #initialize(ServletContext)} has
    * not been called.
    */
   public static File getConfigDir()
   {
      if (m_servletContext == null)
         throw new IllegalStateException("initialize() must be called first");

      return new File(getWebInfDirectory(), SYS_CONFIG_DIR);
   }

   /**
    * Intializes this class with the servlet context.  Calling this method more
    * than once will simply replace the servlet context held by this class.
    * 
    * @param servletContext The servlet context to use, may not be 
    * <code>null</code>.
    */
   public static void initialize(ServletContext servletContext)
   {
      if (servletContext == null)
         throw new IllegalArgumentException("servletContext may not be null");
      
      m_servletContext = servletContext;
   }

   /**
    * Determines if the current environment is in servlet context or not.
    * 
    * @return <code>true</code> if the current environment is in servlet;
    * otherwise the current environment may be in the installer or unit test.
    */
   public static boolean isInitialized()
   {
      return m_servletContext != null;
   }
   
   /**
    * Returns a file reference to the user config directory located below the 
    * servlet directory.  See {@link #USER_CONFIG_DIR} for more info.
    * 
    * @return The file, never <code>null</code>.
    * 
    * @throws IllegalStateException if {@link #initialize(ServletContext)} has
    * not been called.
    */
   public static File getUserConfigDir()
   {
      if (m_servletContext == null)
         throw new IllegalStateException("initialize() must be called first");
      
      return new File(getConfigDir(), USER_CONFIG_DIR);
   }
   
   /**
    * Get the path to the WEB-INF directory of the web application in which this
    * servlet is running.  See {@link #WEB_INF}.
    * 
    * @return The path, never <code>null</code>.
    */   
   private static File getWebInfDirectory()
   {
      return new File(getServletDirectory(), WEB_INF);
   }
   
   /**
    * Get the path to the directory of the web application in which this
    * servlet is running.
    * 
    * @return The path, never <code>null</code>.
    */
   public static File getServletDirectory()
   {
      return new File(m_servletContext.getRealPath("/WEB-INF")).getParentFile();
   }
   
   /**
    * Get the request dispatcher to invoke the servlet on the given path. This
    * is used to invoke servlets directly, without establishing an http 
    * connection to the application server.
    *
    *     * @param path the path to the servlet, never <code>null</code> or empty
    * @return a request dispatcher, the semantics are those of the underlying
    * call to {@link ServletContext#getRequestDispatcher(String)}.
    */
   public static RequestDispatcher getDispatcher(String path)
   {
      if (StringUtils.isBlank(path))
      {
         throw new IllegalArgumentException("path may not be null or empty");
      }
      return m_servletContext.getRequestDispatcher(path);
   }
   
   /**
    * Call the servlet at path with the given request object and return the
    * result as a response object.
    * 
    * @param req the request, never <code>null</code>, will normally be a
    *           mock object.
    * @return the response, never <code>null</code>
    * @throws IOException 
    * @throws ServletException 
    */
   public static HttpServletResponse callServlet(HttpServletRequest req) throws ServletException, IOException
   {
      if (req == null)
      {
         throw new IllegalArgumentException("req may not be null");
      }
      if (StringUtils.isBlank(req.getServletPath()))
      {
         throw new IllegalArgumentException(
               "req.getServletPath() may not be null or empty");
      }
      String path =  req.getServletPath();
      RequestDispatcher disp = getDispatcher(path);
      if (disp == null)
      {
         throw new IllegalArgumentException(
               "path does not evaluate to a servlet: " + path);
      }
      HttpServletResponse response = new MockHttpServletResponse();
      disp.include(req, response);
      return response;
   }
   
   /**
    * Checks if a Throwable is Tomcat ClientAbortException.
    * A ClientAbortException is caused usually by a client (browser) that closes the socket to early.
    * This method is used to determine whether to ignore these exceptions for logging purposes.
    * http://tomcat.apache.org/tomcat-6.0-doc/api/org/apache/catalina/connector/ClientAbortException.html
    * @param t maybe <code>null</code>
    * @return <code>true</code> if the exception chain contains a ClientAbort Exception, false otherwise and on null.
    */
   public static boolean isClientAbortException(Throwable t) {
      while (t != null) {
         if (TOMCAT_ABORT_EXCEPTION_NAME.equals(t.getClass().getCanonicalName()) || 
               "ClientAbortException".equals(t.getClass().getName()) ||
               //For legacy code that doesn't chain exceptions properly
               (t.getMessage() != null && t.getMessage().contains(" ClientAbortException:")) )
            return true;
         t = t.getCause();
      }
      return false;
   }
   
   /**
    * The name of the exception Tomcat throws when a client aborts the connection.
    */
   private static final String TOMCAT_ABORT_EXCEPTION_NAME = "org.apache.catalina.connector.ClientAbortException";
   
   /**
    * The servlet context supplied to {@link #initialize(ServletContext)}, 
    * <code>null</code> until the first call to that method, never
    * <code>null</code> after that.
    */
   private static ServletContext m_servletContext = null;
}
