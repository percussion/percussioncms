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

import com.percussion.utils.jdbc.PSDriverHelper;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;

/**
 * Datasource which creates a database connection using a driver
 * loaded from an external file.
 * 
 * @author peterfrontiero
 */
public class PSDataSource implements DataSource
{
   /**
    * Creates a datasource object.
    * 
    * @param url for the jdbc connection.  Never blank.
    * @param database never blank.
    * @param username never blank.
    * @param password never blank.
    * @param driverClass never blank.
    * @param driverLocation the absolute path to the file in which
    * the driver is contained.  Never blank.
    */
   public PSDataSource(String url, String database, String username,
         String password, String driverClass, String driverLocation)
   {
      if (StringUtils.isBlank(url))
      {
         throw new IllegalArgumentException("url may not be blank");
      }
      
      if (StringUtils.isBlank(username))
      {
         throw new IllegalArgumentException("username may not be blank");
      }
      
      if (StringUtils.isBlank(password))
      {
         throw new IllegalArgumentException("password may not be blank");
      }
      
      if (StringUtils.isBlank(driverClass))
      {
         throw new IllegalArgumentException("driverClass may not be blank");
      }
      
      if (StringUtils.isBlank(driverLocation))
      {
         throw new IllegalArgumentException("driverLocation may not be blank");
      }
      
      m_url = url;
      m_database = database;
      m_username = username;
      m_password = password;
      m_driverClass = driverClass;
      m_driverLocation = driverLocation;
   }

   public <T> T unwrap(java.lang.Class<T> t)
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }
   
   public boolean isWrapperFor(java.lang.Class<?> c)
   {
      return false;
   }
   
   /* (non-Javadoc)
    * @see javax.sql.DataSource#getConnection()
    */
   public Connection getConnection() throws SQLException
   {
      return createConnection(null, null);
   }

   /* (non-Javadoc)
    * @see javax.sql.DataSource#getConnection(java.lang.String, java.lang.String)
    */
   public Connection getConnection(String username, String password)
         throws SQLException
   {
      return createConnection(username, password);
   }

   /* (non-Javadoc)
    * @see javax.sql.DataSource#getLogWriter()
    */
   public PrintWriter getLogWriter() throws SQLException
   {
      return m_logWriter;
   }

   /* (non-Javadoc)
    * @see javax.sql.DataSource#getLoginTimeout()
    */
   public int getLoginTimeout() throws SQLException
   {
      return m_loginTimeout;
   }

   /* (non-Javadoc)
    * @see javax.sql.DataSource#setLogWriter(java.io.PrintWriter)
    */
   public void setLogWriter(PrintWriter out) throws SQLException
   {
      m_logWriter = out;      
   }

   /* (non-Javadoc)
    * @see javax.sql.DataSource#setLoginTimeout(int)
    */
   public void setLoginTimeout(int seconds) throws SQLException
   {
      m_loginTimeout = seconds;
   }

   /**
    * Creates a connection based on the current properties of
    * this datasource, overriding the username and password
    * properties.
    * 
    * @param user The username override.  May be <code>null</code>
    * to use the configured username.
    * @param pwd The password override.  May be <code>null</code>
    * to user the configured password.
    * @return a connection object, never <code>null</code>.
    * 
    * @throws SQLException if an error occurs.
    */
   private Connection createConnection(String user, String pwd)
      throws SQLException
   {
      String username = (user != null) ? user : m_username;
      String password = (pwd != null) ? pwd : m_password;
      
      Properties props = PSSqlHelper.makeConnectProperties(
            m_url,
            m_database,
            username,
            password);
      
      Connection conn = null;
      try
      {
         if (m_driver == null)
         {
            m_driver = PSDriverHelper.getDriver(m_driverClass, m_driverLocation);
         }
         
         conn = m_driver.connect(m_url, props);
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new SQLException(e.getMessage());
      }
      
      return conn;
   }
   
   /**
    * Database connection url.  Initialized in constructor, never
    * <code>null</code> after that.
    */
   private String m_url;
   
   /**
    * Database name.  Initialized in constructor, never
    * <code>null</code> after that.
    */
   private String m_database;
   
   /**
    * Database user name.  Initialized in constructor, never
    * <code>null</code> after that.
    */
   private String m_username;
   
   /**
    * Database driver class.  Initialized in constructor, never
    * <code>null</code> after that.
    */
   private String m_driverClass;
   
   /**
    * See constructor.
    */
   private String m_driverLocation;
   
   /**
    * The currently loaded driver used for creating connections.
    * Set in {@link #createConnection(String, String)}.
    */
   private Driver m_driver;
   
   /**
    * Database user password.  Initialized in constructor, never
    * <code>null</code> after that.
    */
   private String m_password;
      
   /** See {@link #getLoginTimeout()}, {@link #setLoginTimeout(int)}.
    */
   private int m_loginTimeout = 300;
   
   /**
    * See {@link #getLogWriter()}, {@link #setLogWriter(PrintWriter)}.
    */
   private PrintWriter m_logWriter = new PrintWriter(System.out);

   public Logger getParentLogger() throws SQLFeatureNotSupportedException
   {
      throw new SQLFeatureNotSupportedException("This method is not yet implemented");
   }   
   
}
