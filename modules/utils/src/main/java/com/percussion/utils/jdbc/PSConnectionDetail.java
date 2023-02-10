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
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.text.MessageFormat;
import java.util.Objects;

/**
 * Provides specific details about a connection's properties
 */
public class PSConnectionDetail
{
   /**
    * Construct this object from its member data.
    * 
    * @param dsName The name of the JNDI datasource used by the connection, may
    * not be <code>null</code> or empty.
    * @param driver The name of the JDBC driver used by the connection, may
    * not be <code>null</code> or empty.
    * @param database The name of the database used by the connection, may be
    * <code>null</code> or empty.
    * @param origin The origin or schema used by the connection, may be
    * <code>null</code> or empty.
    * @param jdbcUrl The jdbcUrl used by the connection, never <code>null</code> 
    * or empty.
    */
   public PSConnectionDetail(String dsName, String driver, String database, 
      String origin, String jdbcUrl)
   {
      if (StringUtils.isEmpty(dsName))
         throw new IllegalArgumentException("dsName may not be null or empty");
      
      if (StringUtils.isEmpty(driver))
         throw new IllegalArgumentException("driver may not be null or empty");

      if (StringUtils.isBlank(jdbcUrl))
         throw new IllegalArgumentException("jdbcUrl may not be null or empty");
      
      m_dsName = dsName;
      m_driverName = driver;
      m_database = database;
      m_origin = origin;
      m_jdbcUrl = jdbcUrl;
   }
   
   /**
    * Get the name of the JDBC driver used by the connection.
    *  
    * @return The driver name, never <code>null</code> or empty.
    */
   public String getDriver()
   {
      return m_driverName;
   }

   /**
    * Get the name of the database used by the connection.
    * 
    * @return the database name, may be <code>null</code> or empty.
    */
   public String getDatabase()
   {
      return m_database;      
   }
   
   /**
    * Get the origin or schema name used by the connection.
    * 
    * @return The origin or schema name, may be <code>null</code> or empty.
    */
   public String getOrigin()
   {
      return m_origin;
   }
   
   public String toString()
   {
      return m_dsName + "/" + m_driverName + "/" + m_database + "/" + m_origin; 
   }
   
   /**
    * Get the name of the JNDI datasource used by the connection.
    * 
    * @return The datasource name, never <code>null</code> or empty.
    */
   public String getDatasourceName()
   {
      return m_dsName;
   }
   
   /**
    * Get the JDBC url used by the connection.
    * 
    * @return the url, never <code>null</code> or empty.
    */
   public String getJdbcUrl()
   {
      return m_jdbcUrl;
   }
   
   /**
    * Get the connection details in a user readable format.
    * 
    * @return The detail string, never <code>null</code> or empty.
    */
   public String getDetailString()
   {
      String msg = "{0}, database: {1}, schema: {2}";
      String unspec = "<not specified>";
      String db = StringUtils.isBlank(m_database) ? unspec : m_database;
      String orig = StringUtils.isBlank(m_origin) ? unspec : m_origin;
      return MessageFormat.format(msg, m_jdbcUrl, db, orig);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSConnectionDetail)) return false;
      PSConnectionDetail that = (PSConnectionDetail) o;
      return Objects.equals(m_dsName, that.m_dsName) && Objects.equals(m_driverName, that.m_driverName) && Objects.equals(m_database, that.m_database) && Objects.equals(m_origin, that.m_origin) && Objects.equals(m_jdbcUrl, that.m_jdbcUrl);
   }

   @Override
   public int hashCode() {
      return Objects.hash(m_dsName, m_driverName, m_database, m_origin, m_jdbcUrl);
   }

   /**
    * The datasource name supplied during construction, immutable after that.
    */
   private String m_dsName;
   
   /**
    * The driver name supplied during construction, immutable after that.
    */
   private String m_driverName;
   
   /**
    * The database name supplied during construction, immutable after that.
    */
   private String m_database;
   
   /**
    * The origin supplied during construction, immutable after that.
    */
   private String m_origin;
   
   /**
    * The JDBC URL supplied during construction, immutable after that.
    */
   private String m_jdbcUrl;
}

