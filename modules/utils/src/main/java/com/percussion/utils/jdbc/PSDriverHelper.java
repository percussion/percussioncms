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

package com.percussion.utils.jdbc;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;

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
