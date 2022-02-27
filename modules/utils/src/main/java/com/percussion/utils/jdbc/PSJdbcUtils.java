/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility class for JDBC operations
 */
public class PSJdbcUtils
{

   // NOTE: the following driver constants must match the names used in the
   // server configuration (config.xml).
   /**
    * Constant for "jtds:sqlserver".
    * Part of the JDBC URL required to connect to the MS SQL Server database
    * using JTds driver
    */
    public static final String JTDS_DRIVER = "jtds:sqlserver";

   /**
    * constant for the Oracle thin driver type. 
    */
   public static final String ORACLE_DRIVER = "oracle:thin";

   /**
    *  constant for the MySQL driver type.
    */
   public static final String MYSQL_DRIVER = "mysql";

   /**
    *  constant for the Embedded Apache Derby driver type.
    */
   public static final String DERBY_DRIVER = "derby";

    /**
    * constant for the DB2 driver type. 
    */
   public static final String DB2_DRIVER = "db2";

   /**
    * SPRINTA driver type for MS SQL Server database.
    */
   public static final String SPRINTA_DRIVER = "inetdae7";

   /**
    * Constant for "sqlserver".
    * Part of the JDBC URL required to connect to the MS SQL Server database
    * using Microsoft driver
    */
   public static final String MICROSOFT_DRIVER = "sqlserver";

   /**
    * constant for the ODBC driver type. 
    */
   public static final String ODBC = "odbc";
   
   /**
    * constant for the DB2 driver type. 
    */
   public static final String DB2 = DB2_DRIVER;
   
   /**
    * Constant for the oracle driver types. Only includes the primary part of
    * the sub-protocol (with trailing colon). Oracle sub-protocols contain 2
    * parts. E.g. oracle:thin and oracle:oci8. Unlike the other constants, you
    * compare this by using the <code>startsWith</code> method on the driver
    * being tested rather than equals. [The colon is included in the string to
    * help prevent false matches against other drivers that could possibly begin
    * with the same characters.]
    */
   public static final String ORACLE_PRIMARY = "oracle:";
   /** constant for the Oracle thin driver. */
   public static final String ORACLE = ORACLE_DRIVER;
   /** constant for the Oracle oci driver. */
   public static final String ORACLE_OCI = "oracle:oci";
   /** constant for the Oracle oci driver. */
   public static final String ORACLE_OCI7 = "oracle:oci7";
   /** constant for the Oracle oci driver. */
   public static final String ORACLE_OCI8 = "oracle:oci8";

   /** constant for the SPRINTA driver type. */
   public static final String SPRINTA = SPRINTA_DRIVER;

   /** constant for the Microsoft SQL driver type. */
   public static final String MSSQL = "sqlserver";

   /** constant for the Apache Derby driver type. */
   public static final String DERBY = DERBY_DRIVER;

   /** constant for the MYSQL driver type. */
   public static final String MYSQL = MYSQL_DRIVER;

   /**
    * Constant for "microsoft". Will need it for various hacks to make the
    * MS Sql Server JDBC driver work.
    */
   public static final String MICR0SOFT = "microsoft";
   /**
    * Constant for "jtds". Will need it for various hacks to make the
    * JTDS Sql Server JDBC driver work.
    */
   public static final String JTDS = "jtds";


   /**
    * Constant for sprinta db backend property,
    * see {@link PSJdbcDbmsDef#DB_BACKEND_PROPERTY}
    */
   public static final String SPRINTA_DB_BACKEND = "MSSQL";

   /**
    * Constant for db2 db backend property,
    * see {@link PSJdbcDbmsDef#DB_BACKEND_PROPERTY}
    */
   public static final String DB2_DB_BACKEND = "db2";

   /**
    * Constant for jtds db backend property,
    * see {@link PSJdbcDbmsDef#DB_BACKEND_PROPERTY}
    */
   public static final String JTDS_DB_BACKEND = "MSSQL";

   /**
    * Constant for oracle db backend property,
    * see {@link PSJdbcDbmsDef#DB_BACKEND_PROPERTY}
    */
   public static final String ORACLE_DB_BACKEND = "ORACLE";

   /**
    * Constant for Apache Derby db backend property,
    * see {@link PSJdbcDbmsDef#DB_BACKEND_PROPERTY}
    */
   public static final String DERBY_DB_BACKEND = "DERBY";

   /**
    * Constant for MYSQL db backend property,
    * see {@link PSJdbcDbmsDef#DB_BACKEND_PROPERTY}
    */
   public static final String MYSQL_DB_BACKEND = "MYSQL";

   /**
    * Constant for transaction isolation level read uncommitted
    */
   public static final String TRANSACTION_READ_UNCOMMITTED =
      "TRANSACTION_READ_UNCOMMITTED";
   /**
    * Constant numeric representation of transaction isolation level read
    * uncommitted
    */
   public static final String TRANSACTION_READ_UNCOMMITTED_VALUE = "1";
   
   /**
    * The path to the mysql database driver, relative to the root directory.
    */
   public static final String MYSQL_DRIVER_LOCATION = "/jetty/base/lib/jdbc/mysql-connector.jar";
   public static final String MYSQL_DTS_DRIVER_LOCATION="/Deployment/Server/common/lib/mysql-connector.jar";
   public static final String MYSQL_STAGING_DTS_DRIVER_LOCATION="/Staging/Deployment/Server/common/lib/mysql-connector.jar";

   public static final String DEFAULT_JDBC_DRIVER_LOCATION = "/jetty/base/lib/jdbc";
   public static final String DEFAULT_DTS_DRIVER_LOCATION="/Deployment/Server/common/lib";
   public static final String DEFAULT_STAGING_DTS_DRIVER_LOCATION="/Staging/Deployment/Server/common/lib";
   /**
    * Additional connection url parameters required to use unicode (UTF-8) with mysql. 
    */
       public static String MYSQL_CONN_PARAMS = "?useUnicode=yes&characterEncoding=UTF-8&useSSL=true&requireSSL=false&verifyServerCertificate=false";
   
   /**
    * Parses the specified database URL and returns the driver for which this
    * is a valid URL.
    *
    * @param url database url, usually specified when connecting to the
    * database, may not be <code>null</code> or empty
    *
    * @return the driver for which the specified database url is a valid
    * url, never <code>null</code> or empty
    */
   static public String getDriverFromUrl(String url)
   {
      if ((url == null) || (url.trim().length() == 0))
      {
         throw new IllegalArgumentException(
            "Database URL may not be null or empty");
      }
   
      url = url.toLowerCase();
      if (!url.startsWith("jdbc:"))
         throw new IllegalArgumentException(
            "database url must start with jdbc:");
   
      int subIndex1 = url.indexOf(":", "jdbc:".length());
      if (subIndex1 == -1)
         throw new IllegalArgumentException(
            "invalid database url. Valid format is jdbc:subprotocol:subname");
   
      String sub1 = url.substring("jdbc:".length(), subIndex1);
   
      String sub2 = null;
      int subIndex2 = url.indexOf(":", subIndex1 + 1);
      if (subIndex2 != -1)
         sub2 = url.substring(subIndex1 + 1, subIndex2);
   
      String driver = null;
      if (sub2 != null)
         driver = (String)ms_jdbcUrlToDriverMap.get(sub1 + ":" + sub2);
   
      if (driver == null)
         driver = (String)ms_jdbcUrlToDriverMap.get(sub1);
   
      if ((driver == null) && (sub2 != null))
         driver = sub1 + ":" + sub2;
   
      if (driver == null)
         driver = sub1;
   
      return driver;
   }
   
   /**
    * Parses the specified database URL and returns the server for which this
    * is a valid URL.
    *
    * @param url database url, usually specified when connecting to the
    * database, may not be <code>null</code> or empty
    *
    * @return the server for which the specified database url is a valid
    * url, never <code>null</code> or empty
    */
   static public String getServerFromUrl(String url)
   {
      if ((url == null) || (url.trim().length() == 0))
      {
         throw new IllegalArgumentException(
            "Database URL may not be null or empty");
      }
   
      String tmpUrl = url.toLowerCase();
      if (!tmpUrl.startsWith("jdbc:"))
         throw new IllegalArgumentException(
            "database url must start with jdbc:");
   
      int subIndex1 = tmpUrl.indexOf(":", "jdbc:".length());
      if (subIndex1 == -1)
         throw new IllegalArgumentException(
            "invalid database url. Valid format is jdbc:subprotocol:subname");
   
      String sub1 = tmpUrl.substring("jdbc:".length(), subIndex1);
   
      String sub2 = null;
      int subIndex2 = tmpUrl.indexOf(":", subIndex1 + 1);
      if (subIndex2 != -1)
         sub2 = tmpUrl.substring(subIndex1 + 1, subIndex2);
   
      String driver = null;
      if (sub2 != null)
         driver = (String)ms_jdbcUrlToDriverMap.get(sub1 + ":" + sub2);
   
      if (driver == null)
         driver = (String)ms_jdbcUrlToDriverMap.get(sub1);
   
      if ((driver == null) && (sub2 != null))
         driver = sub1 + ":" + sub2;
   
      if (driver == null)
         driver = sub1;
   
      // sever is what follows the driver
      String server = StringUtils.substringAfter(url, driver + ':');
      
      return server;
   }   
   
   /**
    * Gets the database name from a given JDBC URL. This should not be used
    * for Oracle JDBC driver, but for jTDS (SQLServer), SPRINTA (SQLServer),
    * MySQL and DB2.
    * <TABLE BORDER="1">
    * <TR><TH>Driver</TH><TH>Expected URL format</TH></TR>
    * <TR><TD>jTDS</TD><TD>jdbc:jtds:sqlserver://&lt;host>:&lt;port>[/db-name];user=u;password=p</TD></TR>
    * <TR><TD>jTDS</TD><TD>jdbc:jtds:sqlserver://&lt;host>:&lt;port>;database=&lt;db-name>;user=u;password=p</TD></TR>
    * <TR><TD>SPRINTA</TD><TD>jdbc:inetdae7:&lt;host>:&lt;post>?database=&lt;db-name></TD></TR>
    * <TR><TD>MySQL</TD><TD>jdbc:mysql://&lt;host>[,failoverhost...][:port][/database][?propertyName1][=propertyValue1][&propertyName2][=propertyValue2]...</TD></TR>
    * <TR><TD>DB2</TD><TD>jdbc:db2://&lt;host>:&lt;port>[/db-name]</TD></TR>
    * <TR><TD>Oracle</TD><TD>jdbc:oracle:thin:@&lt;host>:1521:&lt;sid></TD></TR>
    * </TABLE>

    * @param url the JDBC url in question, it may not be <code>null</code> or
    *    empty.
    * 
    * @return the database name defined in the URL. It may be <code>null</code>
    *    if the database name is not specified in the URL or the URL is for 
    *    Oracle driver.
    */
   public static String getDatabaseFromUrl(String url)
   {
      // Skip driver part
      String s = getServerFromUrl(url);
      
      // Skip first "//" if there is any
      if (s.length() > 2 && s.charAt(0) == '/' && s.charAt(1) == '/')
         s = s.substring(2);
      
      // Get the database name for jTDS, MySQL and DB2, which are in the format 
      // of ".../db-name;...", "../db-name?.." or ".../db-name"
      int i = s.indexOf("/");
      if (i > 0 && s.length() > i+1) // is not "<host>[:port]/"
      {
         s = s.substring(i+1);
         return getDbName(s);
      }

      // Get the database name for jTDS, SPRINTA, MySQL and DB2, which are in  
      // the format of "...database=<db-name>;...", "..database=<db-name>?.." 
      // or "...database=<db-name>"
      i = s.indexOf("database=");
      int pnameLength = "database=".length();
      if (i > 0 && s.length() > (i + pnameLength))
      {
         s = s.substring(i + pnameLength); // skip "database="
         return getDbName(s);
      }
      
      return null;
   }

   /**
    * Gets the database name from a given (partial) URL in the format of
    * "&lt;db-name>;...", "&lt;db-name>?...",  "&lt;db-name>&..."
    * or "&lt;db-name>"
    * 
    * @param partialUrl the partial URL in question, assumed not 
    *    <code>null</code> or empty.
    *    
    * @return the retrieved database name, never <code>null</code> or empty.
    */
   private static String getDbName(String partialUrl)
   {
      int i = partialUrl.indexOf(';');
      if ( i != -1)
         return partialUrl.substring(0, i);

      i = partialUrl.indexOf('?');
      if ( i != -1)
         return partialUrl.substring(0, i);
      
      i = partialUrl.indexOf('&');
      if ( i != -1)
         return partialUrl.substring(0, i);
      
      return partialUrl;
   }
   
   /**
    * Construct a jdbc url string from a given driver name and server name.
    * Correctly deals with cases where the name of the driver is not the
    * name of the second component in the jdbc url string.
    *
    * @param driverName Name of the driver, must never be <code>null</code>
    * @param serverNameOrConnUrl Name of the server or connection string,
    * must never be <code>null</code>
    * @return a string, never <code>null</code> or empty.
    */
   public static String getJdbcUrl(String driverName, String serverNameOrConnUrl)
   {
      if (driverName == null || driverName.trim().length() == 0)
      {
         throw new
            IllegalArgumentException("driverName may not be null or empty");
      }
      if (!driverName.equalsIgnoreCase("psxml") && 
          !driverName.equalsIgnoreCase("psfilesystem") &&
              (serverNameOrConnUrl == null || serverNameOrConnUrl.trim().length() == 0))
      {
         throw new
            IllegalArgumentException("serverName may not be null or empty");
      }
      StringBuilder rval = new StringBuilder(40);

      rval.append("jdbc:");

      rval.append(driverName);
      rval.append(':');
      rval.append(serverNameOrConnUrl);
      if (MYSQL_DRIVER.equalsIgnoreCase(driverName) && serverNameOrConnUrl.indexOf("?") == -1)
      {
         //TODO: Make this more robust so it is making sure the pramas we want to be there are there.
         rval.append(MYSQL_CONN_PARAMS);
      }else if(DERBY_DRIVER.equalsIgnoreCase(driverName) && serverNameOrConnUrl.indexOf(";create=true")== -1){
         rval.append(";create=true");
      }

      return rval.toString().replace(";;",";");
   }   
   
   /**
    * Get the appropriate db backend for the specified driver.
    * 
    * @param driver the driver type, may not be <code>null</code>.  Assumed to be
    * one of the following: {@link #SPRINTA}, {@link #DB2}, {@link #JTDS_DRIVER},
    * {@link #ORACLE}
    * 
    * @return the db backend, never <code>null</code> or empty.
    */
   public static String getDBBackendForDriver(String driver)
   {
      String strDBBackend;
      if (driver.equals(SPRINTA))
         strDBBackend = SPRINTA_DB_BACKEND;
      else if (driver.equals(DB2))
         strDBBackend = DB2_DB_BACKEND;
      else if (driver.equals(DERBY_DRIVER))
         strDBBackend = DERBY_DB_BACKEND;
      else if (driver.equals(MYSQL_DRIVER))
          strDBBackend = MYSQL_DB_BACKEND;
      else if (driver.equals(JTDS_DRIVER) || driver.equalsIgnoreCase(MICROSOFT_DRIVER) || driver.equalsIgnoreCase(MICROSOFT_DRIVER))
         strDBBackend = JTDS_DB_BACKEND;
      else
      {
         //Oracle is the only supported driver left
         strDBBackend = ORACLE_DB_BACKEND;
      }
      
      return strDBBackend;
   }
   
   /**
    * Returns List of all database drivers that can only be used for
    * database publishing and not as Rhythmyx repositories.
    * @return the list never <code>null</code>, may be empty.
    */
   public static List<String> getDbPubOnlyDrivers()
   {
      return new ArrayList<String>(ms_dbPubOnlyDrivers);
   }
   
   /**
    * Determines if the specified driver is external, which means
    * that it is not distributed with the server.
    * @return <code>true</code> if the driver is external,
    * <code>false</code> otherwise.
    */
   public static boolean isExternalDriver(String driver)
   {
      return ms_externalDrivers.contains(driver);
   }
   
   /**
    * Define a map from jdbc url to driver, never <code>null</code> or empty.
    * Initialized in the static initializer, never modified after that.
    */
   private static Map<String, String> ms_jdbcUrlToDriverMap = 
      new HashMap<String, String>();

   /**
    * Set of all database drivers that are not distributed
    * with the server.
    */
   private static final Set<String> ms_externalDrivers =
      new HashSet<String>();
   
   // Initialize the hashmap that maps jdbc url to driver as well as
   // the set of external drivers
   static
   {
      ms_jdbcUrlToDriverMap.put(ORACLE, ORACLE);
      ms_jdbcUrlToDriverMap.put(ORACLE_OCI, ORACLE_OCI);
      ms_jdbcUrlToDriverMap.put(ORACLE_OCI7, ORACLE_OCI7);
      ms_jdbcUrlToDriverMap.put(ORACLE_OCI8, ORACLE_OCI8);
      ms_jdbcUrlToDriverMap.put(SPRINTA, SPRINTA);
      ms_jdbcUrlToDriverMap.put(DB2, DB2);
      ms_jdbcUrlToDriverMap.put(MICROSOFT_DRIVER, MICROSOFT_DRIVER);
      ms_jdbcUrlToDriverMap.put(JTDS_DRIVER, JTDS_DRIVER);
      ms_jdbcUrlToDriverMap.put(DERBY_DRIVER, DERBY_DRIVER);
      ms_jdbcUrlToDriverMap.put(MYSQL_DRIVER, MYSQL_DRIVER);

      // weblogic drivers
      ms_jdbcUrlToDriverMap.put("weblogic:oracle", "weblogic:oracle");
      ms_jdbcUrlToDriverMap.put("weblogic:mssqlserver", "weblogic:mssqlserver");
      ms_jdbcUrlToDriverMap.put("weblogic:mssqlserver4", 
         "weblogic:mssqlserver4");

      // datadirect drivers
      ms_jdbcUrlToDriverMap.put("datadirect:db2", "datadirect:db2");
      ms_jdbcUrlToDriverMap.put("datadirect:oracle", "datadirect:oracle");
      ms_jdbcUrlToDriverMap.put("datadirect:sqlserver", "datadirect:sqlserver");
      
      // external drivers
      ms_externalDrivers.add(MYSQL_DRIVER);
   }
   
   /**
    * List of all database drivers that can only be used for
    * database publishing and not as a Rhythmyx repository.
    */
   private static final List<String> ms_dbPubOnlyDrivers = 
      new ArrayList<String>();
}

