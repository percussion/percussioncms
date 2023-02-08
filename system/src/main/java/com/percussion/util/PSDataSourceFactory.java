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
package com.percussion.util;


import com.percussion.utils.jdbc.PSJdbcUtils;
import org.apache.commons.dbcp.BasicDataSourceFactory;

import javax.sql.DataSource;
import java.util.Properties;


/**
 * Overrides {@link BasicDataSourceFactory#createDataSource(Properties)}
 * in order to provide datasources whose connections are established
 * using drivers loaded from external files.
 */
public class PSDataSourceFactory extends BasicDataSourceFactory
{
   /**
    * See {@link BasicDataSourceFactory#createDataSource(Properties)}.
    * 
    * @param properties the database connection properties used to
    * configure the datasource.  Must include the following:
    * url, database, username, password, driverLocation.
    * Never <code>null</code>.  
    * @return a new {@link PSDataSource} object.
    * 
    * @throws Exception if an error occurs.
    */
   public static DataSource createDataSource(Properties properties)
      throws Exception
   {
      String url = properties.getProperty(URL_PROP_NAME);
      if (url == null)
      {
         throw new IllegalArgumentException("properties must contain " +
                URL_PROP_NAME);
      }
      
      String username = properties.getProperty(USER_PROP_NAME);
      if (username == null)
      {
         throw new IllegalArgumentException("properties must contain " +
                USER_PROP_NAME);
      }
      
      String password = properties.getProperty(PWD_PROP_NAME);
      if (password == null)
      {
         throw new IllegalArgumentException("properties must contain " +
                PWD_PROP_NAME);
      }
      
      String driverClass = properties.getProperty(DRIVER_CLASS_PROP_NAME);
      if (driverClass == null)
      {
         throw new IllegalArgumentException("properties must contain " +
                DRIVER_CLASS_PROP_NAME);
      }
      
      String driverLocation = properties.getProperty(DRIVER_LOC_PROP_NAME);
      if (driverLocation == null)
      {
         throw new IllegalArgumentException("properties must contain " +
                DRIVER_LOC_PROP_NAME);
      }
            
      DataSource ds;
      
      if (PSJdbcUtils.isExternalDriver(PSJdbcUtils.getDriverFromUrl(url)))
      {
         ds = new PSDataSource(
               url,
               properties.getProperty(DB_PROP_NAME),
               username,
               password,
               driverClass,
               driverLocation);
      }
      else
      {
         ds = BasicDataSourceFactory.createDataSource(properties);
      }
    
      return ds;
   }
    
   /**
    * Property constants
    */
   public static final String URL_PROP_NAME = "url";
   public static final String DB_PROP_NAME = "database";
   public static final String USER_PROP_NAME = "username";
   public static final String PWD_PROP_NAME = "password";
   public static final String DRIVER_CLASS_PROP_NAME = "driverClassName";
   public static final String DRIVER_LOC_PROP_NAME = "driverLocation";
   
}
