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
package com.percussion.hooks;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * This is the base class for all Rhythmyx specific servlet. It contains 
 * convenient methods that can be used for all derived classes.
 */
public class PSServletBase extends HttpServlet
{
   /**
    * Get the description of this servlet's information.
    *
    * @return the servlet information, never <code>null</code> or empty.
    */
   public String getServletInfo()
   {
      Object[] versionArgs =
      {
         getVersionResources().getString("versionString"),
         getVersionResources().getString("majorVersion"),
         getVersionResources().getString("minorVersion"),
         getVersionResources().getString("buildNumber"),
         getVersionResources().getString("buildId")
      };
      String version = PSConnectionFactory.formatMessage(
         IPSServletErrors.VERSION_STRING, versionArgs);

      Object[] args =
      {
         version
      };
      return PSConnectionFactory.formatMessage(
         IPSServletErrors.SERVLET_INFORMATION, args);
   }

   /**
    * Initialize the servlet. It loads the default properties file for log4j
    * if the log4j has not been configured. The location of the 
    * <code>log4j.properties</code> can be specified by 
    * <code>RxLogLocation</code> servlet parameter, if the parameter is empty 
    * or not specified, then load the property file from 
    * <code>com.percussion.hooks.servlet.log4j.properties</code>. 
    *
    * @param config a servlet configuration object, not <code>null</code>.
    * 
    * @throws ServletException if initialization failed.
    * @throws IllegalArgumentException if argument is <code>null</code>.
    */
   public void init(ServletConfig config)
      throws ServletException, IllegalArgumentException
   {
      super.init(config);
      
      try
      {
         // Register the HTTPS protocol handler
         PSUtils.registerSSLProtocolHandler();
         

      }
      catch (Exception e)
      {
         throw new ServletException(e);
      }

   }


   /**
    * Get the version resource bundle.
    *
    * @return the version resource bundle, never <code>null</code>.
    */
   protected ResourceBundle getVersionResources()
   {
      if (ms_version == null)
         ms_version = ResourceBundle.getBundle(
            "com.percussion.hooks.servlet.Version", Locale.getDefault());

      return ms_version;
   }

   /**
    * The version resource bundle, initialized in the first call to
    * {#link getVersionResources()}, never changed after that.
    */
   private static ResourceBundle ms_version = null;

}
