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

package com.percussion.utils.jdbc;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;

import org.apache.commons.lang.StringUtils;

/**
 * This class provides utilities for loading jdbc drivers.
 */
public class PSDriverHelper
{
   /**
    * Private noop ctor to enforce static use of this class
    */
   private PSDriverHelper()
   {

   }
   
   /**
    * Loads the jdbc driver for the specified class from the specified
    * file.
    * 
    * @param driverClass never blank.
    * @param driverPath absolute path to the file which contains the driver,
    * never blank.
    * 
    * @return The driver object, never <code>null</code>.
    * 
    * @throws MalformedURLException If an error occurs creating a url to
    * the file.
    * @throws ClassNotFoundException If the class does not exist in the
    * file. 
    * @throws IllegalAccessException If an error occurs loading the driver.
    * @throws InstantiationException If an error occurs loading the driver. 
    */
   @SuppressWarnings("unchecked")
   public static Driver getDriver(String driverClass, String driverPath)
   throws MalformedURLException, ClassNotFoundException,
         InstantiationException, IllegalAccessException
   {
      if (StringUtils.isBlank(driverClass))
      {
         throw new IllegalArgumentException("driverClass may not be blank");
      }
      
      if (StringUtils.isBlank(driverPath))
      {
         throw new IllegalArgumentException("driverPath may not be blank");
      }
      
      Driver driver = null;
      
      File jarfile = new File(driverPath);
      URL[] urls = new URL[]{jarfile.toURI().toURL()};
      ClassLoader loader = new URLClassLoader(urls);
      Class driverClazz = Class.forName(driverClass, true, loader);
      Object objDriver = driverClazz.newInstance();
      if (objDriver instanceof Driver)
      {
         driver = (Driver) objDriver;
      }
      else
      {
         throw new RuntimeException("driverClass is not a driver");
      }
    
      return driver;
   }
   
}
