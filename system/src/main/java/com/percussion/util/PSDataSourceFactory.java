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
