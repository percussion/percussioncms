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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.tablefactory;

import com.percussion.security.PSEncryptionException;
import com.percussion.security.PSEncryptor;
import com.percussion.util.PSSqlHelper;
import com.percussion.utils.container.*;
import com.percussion.utils.container.jboss.PSJbossProperties;
import com.percussion.utils.io.PathUtils;
import com.percussion.utils.jdbc.*;
import com.percussion.utils.security.deprecated.PSLegacyEncrypter;
import com.percussion.utils.xml.PSInvalidXmlException;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.*;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

/**
 * Defines all information required to connect to a dbms and process a
 * PSJdbcTableSchema object against it.
 */
public class PSJdbcDbmsDef implements IPSJdbcDbmsDefConstants
{

   /**
    * Creates an instance of this class using the supplied properties.
    *
    * @param serverProps A set of properties that provides connection and basic
    * table information.  May not be <code>null</code>.  Properties expected:
    * <table>
    * <tr>
    * <th>Name</th>
    * <th>Description</th>
    * <th>Required?</th>
    * </tr>
    *
    * <tr>
    * <td>DB_BACKEND</td>
    * <td>Name of backend alias. Used to identify the set of mappings
    * to use from the dataTypeMap Xml file.  See DataTypeMap.dtd for more info.
    * See {@link #DB_BACKEND_PROPERTY}</td>
    * <td>No</td>
    * </tr>
    *
    * <tr>
    * <td>DB_DRIVER_NAME</td>
    * <td>Name of the Jdbc sub-protocol (e.g. inetdae7, oracle:thin). May be
    * used to identify the set of mappings to use from the dataTypeMap Xml file.
    * See {@link #DB_DRIVER_NAME_PROPERTY}
    * </td>
    * <td>Yes</td>
    * </tr>
    *
    * <tr>
    * <td>DB_DRIVER_CLASS_NAME</td>
    * <td>The class name of the jdbc driver. See {@link
    * #DB_DRIVER_CLASS_NAME_PROPERTY}</td>
    * <td>Yes</td>
    * </tr>
    *
    * <tr>
    * <td>DB_SERVER</td>
    * <td>The name of the database server to connect to. See {@link
    * #DB_SERVER_PROPERTY}</td>
    * <td>Yes</td>
    * </tr>
    *
    * <tr>
    * <td>DB_NAME</td>
    * <td>The name of the database.  May be empty. See {@link
    * #DB_NAME_PROPERTY}</td>
    * <td>No</td>
    * </tr>
    *
    * <tr>
    * <td>UID</td>
    * <td>The userid to use when connecting.  May be empty. See {@link
    * #UID_PROPERTY}</td>
    * <td>No</td>
    * </tr>
    *
    * <tr>
    * <td>PWD</td>
    * <td>The password to use when connecting, may be encrypted.  May be empty.
    * See {@link #PWD_PROPERTY}</td>
    * <td>No</td>
    * </tr>
    *
    * <tr>
    * <td>PWD_ENCRYPTED</td>
    * <td>Specifies if the supplied password is encrypted. If "Y", then it is,
    * if "N" it is not.  By default the password is assumed to not be encrypted,
    * so this option property only needs to be supplied if the password is
    * encrypted.  See {@link #PWD_ENCRYPTED_PROPERTY}</td>
    * <td>No</td>
    * </tr>
    *
    * <tr>
    * <td>DB_SCHEMA</td>
    * <td>The schema or origin to use.  May be empty. See {@link
    * #DB_SCHEMA_PROPERTY}</td>
    * <td>No</td>
    * </tr>
    * </table>
    *
    * @throws IllegalArgumentException if serverProps is <code>null</code> or if
    * a required property is missing.
    * @throws PSJdbcTableFactoryException if any other error occurs.
    */
   public PSJdbcDbmsDef(Properties serverProps) throws PSJdbcTableFactoryException
   {
      if (serverProps == null)
         throw new IllegalArgumentException("serverProps may not be null");

      m_backendDb = serverProps.getProperty(DB_BACKEND_PROPERTY);
      if (m_backendDb != null && m_backendDb.trim().length() == 0)
         m_backendDb = null;
      m_driver = getRequiredProperty(serverProps, DB_DRIVER_NAME_PROPERTY);
      m_class = getRequiredProperty(serverProps, DB_DRIVER_CLASS_NAME_PROPERTY);
      m_server = getRequiredProperty(serverProps, DB_SERVER_PROPERTY);
      m_database = serverProps.getProperty(DB_NAME_PROPERTY);
      m_schema = serverProps.getProperty(DB_SCHEMA_PROPERTY);
      m_uid = serverProps.getProperty(UID_PROPERTY);
      m_pw = serverProps.getProperty(PWD_PROPERTY);

      // handle possible encryption of password
      String isEncrypted = serverProps.getProperty(PWD_ENCRYPTED_PROPERTY);
      if (isEncrypted != null && isEncrypted.equalsIgnoreCase("Y"))
         try{

            m_pw = PSEncryptor.getInstance("AES",
                    PathUtils.getRxDir(null).getAbsolutePath().concat(PSEncryptor.SECURE_DIR)
            ).decrypt(m_pw);
         } catch (PSEncryptionException e) {
            m_pw = PSLegacyEncrypter.getInstance(
                    PathUtils.getRxDir(null).getAbsolutePath().concat(PSEncryptor.SECURE_DIR)
            ).decrypt(m_pw, PSLegacyEncrypter.getInstance(
                    PathUtils.getRxDir(null).getAbsolutePath().concat(PSEncryptor.SECURE_DIR)
            ).getPartOneKey(),null);
         }

   }

   /**
    * Creates an instance of this class using the database name (dbName) and
    * driver type. Performs a JNDI lookup for "jdbc/dbName" or "dbName" if the
    * first name fails
    *
    * @param dbName name of the database to connect to, may not be
    *    <code>null</code> or empty
    * @param driverType type of the backend database, one of the constants
    *    specified in <code>PSSqlHelper</code> (e.g. oracle:thin, inetdae7,
    *    etc.), may be <code>null</code> or empty to derive it from a 
    *    connection.
    *
    * @throws NamingException if JNDI lookup for "jdbc/dbName" and "dbName" both
    * fail.
    * @throws IllegalArgumentException if dbName or driverType is
    *    <code>null</code> or empty.
    */
   public PSJdbcDbmsDef(String dbName, String driverType)
      throws NamingException
   {
      this(dbName, driverType, null);
   }

    public PSJdbcDbmsDef() {
    }

    /**
    * Loads the required repository properties from values found in the 
    * following files:
    * 
    * {@link PSJbossProperties#SPRING_CONFIG_FILE},
    * {@link PSJbossProperties#LOGIN_CONFIG_FILE}
    * 
    * @param rxRoot the Rhythmyx root, may not be <code>null</code> or empty.
    *
    * @return repository properties, which includes driver, driver class,
    * server, uid, encrypted password, password, database name, schema.  Never
    * <code>null</code>, may be empty.
    *
    * @throws RuntimeException if an error occurs loading the properties.
    * @throws IllegalArgumentException if rxRoot is <code>null</code> or empty.
    * @throws FileNotFoundException if one of the files cannot be found.
    * @throws PSInvalidXmlException if an error occurs loading datasource
    * resolver.
    * @throws SAXException if an error occurs loading secure credentials.
    * @throws IOException if an error occurs loading properties.
    * @throws PSMissingApplicationPolicyException if an error occurs loading
    * jndi datasources
    */
   public static Properties loadRxRepositoryProperties(String rxRoot)
      throws FileNotFoundException, PSInvalidXmlException, SAXException,
      IOException, PSMissingApplicationPolicyException
   {
      if (rxRoot == null || rxRoot.trim().length() == 0)
         throw new IllegalArgumentException("rxRoot may not be null or empty");
      
      String strDriver;
      String strClass;
      String strServer;
      String strId;
      String strPw;
      String strDb;
      String strSchema;
      String resolverName = null;
      boolean encrypted = false;

      IPSContainerUtils utils = PSContainerUtilsFactory.getInstance(Paths.get(rxRoot));

      IPSDatasourceConfig repositoryConfig = utils.getDatasourceResolver().getRepositoryDatasourceConfig();

      if (repositoryConfig == null)
      {
         throw new RuntimeException(
               "Unable to find connection configuration for repository datasource "+ utils.getDatasourceResolver().getRepositoryDatasource());
      }
   
         strDb = repositoryConfig.getDatabase();
         strSchema = repositoryConfig.getOrigin();
       String repositoryJndiDS = repositoryConfig.getDataSource();
      



      // Get the repository jndi datasource
      IPSJndiDatasource repositoryJndiDataSource = null;
      List<IPSJndiDatasource> datasources = 
         utils.getDatasources();
      String securityDomain;



      for (IPSJndiDatasource datasource : datasources)
      {
         if (datasource.getName().equalsIgnoreCase(repositoryJndiDS))
         {
            repositoryJndiDataSource = datasource;
            break;
         }
      }
      
      if (repositoryJndiDataSource == null)
      {
         throw new RuntimeException(
               "Unable to find jndi configuration for repository "
               + " datasource " + repositoryJndiDS);
      }
      
         strDriver = repositoryJndiDataSource.getDriverName();
         strClass = repositoryJndiDataSource.getDriverClassName();
         strServer = repositoryJndiDataSource.getServer();
         strId = repositoryJndiDataSource.getUserId();
         securityDomain = repositoryJndiDataSource.getSecurityDomain();
      
      if (securityDomain != null)
      {
         // Get the secure credentials
         File loginCfgFile = new File(rxRoot, PSJbossProperties.LOGIN_CONFIG_FILE);
         PSSecureCredentials creds = null;

         try(FileInputStream cfgIn = new FileInputStream(loginCfgFile))
         {

            Document cfgDoc = PSXmlDocumentBuilder.createXmlDocument(cfgIn, false);
            NodeList nodes = cfgDoc.getElementsByTagName(
                  PSSecureCredentials.APP_POLICY_NODE_NAME);
            int len = nodes.getLength();
            for (int i = 0; i < len && creds == null; i++)
            {
               Element node = (Element) nodes.item(i);
               if (PSSecureCredentials.isMatch(node, securityDomain))
                  creds = new PSSecureCredentials(node);
            }
         }
         
         if (creds == null)
         {
            try {
               strPw = PSEncryptor.getInstance("AES",
                       PathUtils.getRxDir(null).getAbsolutePath().concat(PSEncryptor.SECURE_DIR)
               ).encrypt(
                       repositoryJndiDataSource.getPassword());
               encrypted = true;
            } catch (PSEncryptionException e) {
               strPw = repositoryJndiDataSource.getPassword();
            }
         }
         else
            strPw = creds.getPassword();
      }
      else
      {
         try {
            strPw = PSEncryptor.getInstance("AES",
                    PathUtils.getRxDir(null).getAbsolutePath().concat(PSEncryptor.SECURE_DIR)
            ).encrypt(
                    repositoryJndiDataSource.getPassword());
            encrypted = true;
         } catch (PSEncryptionException e) {
            strPw = repositoryJndiDataSource.getPassword();
         }
      }
      
      Properties dbProps = new Properties();
            
      dbProps.setProperty(DB_BACKEND_PROPERTY,
            PSJdbcUtils.getDBBackendForDriver(strDriver));
      dbProps.setProperty(DB_DRIVER_NAME_PROPERTY, strDriver);
      dbProps.setProperty(DB_DRIVER_CLASS_NAME_PROPERTY, strClass);
      dbProps.setProperty(DB_SERVER_PROPERTY, strServer);
      dbProps.setProperty(UID_PROPERTY, strId);

      if(encrypted)
         dbProps.setProperty(PWD_ENCRYPTED_PROPERTY, "Y");
      else
         dbProps.setProperty(PWD_ENCRYPTED_PROPERTY, "N");

      dbProps.setProperty(PWD_PROPERTY, strPw);
      dbProps.setProperty(DB_NAME_PROPERTY, strDb);
      dbProps.setProperty(DB_SCHEMA_PROPERTY, strSchema);

      return dbProps;
   }

   /**
    * Creates an instance of this class using the database name (dbName) and
    * driver type. Performs a JNDI lookup for "jdbc/dbName" or "dbName" if the
    * first name fails.
    *
    * @param dbName name of the database to connect to, may not be
    *    <code>null</code> or empty
    * @param driverType type of the backend database, one of the constants
    *    specified in <code>PSSqlHelper</code> (e.g. oracle:thin, inetdae7,
    *    etc.), may be <code>null</code> or empty to derive it from a 
    *    connection.
    * @param environment the JNDI environment to use to lookup data sources,
    *    might be <code>null</code>. Uses the defaults for Tomcat 4.0 if
    *    nothing is provided.
    *
    * @throws NamingException if JNDI lookup for "jdbc/dbName" and "dbName" 
    * both fail.
    * @throws IllegalArgumentException if dbName or driverType is
    *    <code>null</code> or empty.
    */
   public PSJdbcDbmsDef(
      String dbName,
      String driverType,
      Properties environment)
      throws NamingException
   {
      this(dbName, null, driverType, null, environment);
   }

   /**
    * Creates an instance of this class using the database name (dbName) and
    * driver type. Performs a JNDI lookup for "jdbc/dbName" or "dbName" if the
    * first lookup fails. Must supply either dbName or resourceName.
    *
    * @param dbName name of the database to connect to, may be
    *    <code>null</code> or empty if resourceName is not null and non-empty.
    * @param resourceName name of the datasource resource to be used, may be
    *    <code>null</code> or empty if dbName is not null and non-empty.
    * @param driverType type of the backend database, one of the constants
    *    specified in <code>PSSqlHelper</code> (e.g. oracle:thin, inetdae7,
    *    etc.), may be <code>null</code> or empty to derive it from a 
    *    connection.
    * @param origin the origin or schema name to qualify tables. May be
    *    <code>null</code> or empty except when driverType = "Oracle" and 
    *    resourceName is not null and non-empty.
    * @param environment the JNDI environment to use to lookup data sources,
    *    might be <code>null</code>. Uses the defaults for Tomcat 4.0 if
    *    nothing is provided.
    *
    * @throws NamingException if JNDI lookup for "jdbc/dbName" and "dbName"
    * both fail.
    * @throws IllegalArgumentException if dbName or driverType is
    *    <code>null</code> or empty.
    */
   public PSJdbcDbmsDef(
      String dbName,
      String resourceName,
      String driverType,
      String origin,
      Properties environment)
      throws NamingException
   {
      boolean resourceNameUnspecified =
         (null == resourceName || resourceName.trim().length() == 0);
      boolean originUnspecified =
         (null == origin || origin.trim().length() == 0);

      if (resourceNameUnspecified
         && (dbName == null || dbName.trim().length() == 0))
         throw new IllegalArgumentException("One of the following must be " +
               "defined: Database name or Resource name.");

      if (resourceNameUnspecified)
         resourceName = dbName;
      
      init(getDataSource(resourceName, environment), driverType, origin);
      
      boolean isOracle = m_driver.startsWith("oracle:");

      if (isOracle && !resourceNameUnspecified && originUnspecified)
         throw new IllegalArgumentException("Origin is required when " +
            "using resourceName for Oracle.");      
   }

   /**
    * Initialize this object from a datasource.
    * 
    * @param dataSource The datasource to use, assumed not <code>null</code>.
    * @param driverType The JDBC driver name, may be <code>null</code> or empty
    * to derive it from the datsource.
    * @param origin The origin or schema to use, may be <code>null</code> or 
    * empty.
    * 
    * @throws NamingException If there is an error attempting to derive the
    * JDBC driver name.
    */
   protected void init(DataSource dataSource, String driverType, String origin)
      throws NamingException
   {
      m_database = null;
      m_backendDb = null;
      m_driver = driverType;
      m_schema = origin;

      m_dataSource = dataSource;


      if (driverType == null || driverType.trim().length() == 0)
      {
         // Create temporary connection to obtain the driverType and 
         // database info if it has not been supplied
         Connection conn = null;
         try
         {
            conn = m_dataSource.getConnection();
            DatabaseMetaData dmd = conn.getMetaData();
            m_driver = PSJdbcUtils.getDriverFromUrl(dmd.getURL());
         }
         catch (SQLException e)
         {
            if (conn == null)
            {
               throw new NamingException(
                  "Problem creating connection: " + e.getMessage());
            }
            else
            {
               throw new NamingException(
                  "Problem accessing metadata: " + e.getMessage());
            }
         }
         finally
         {
            try
            {
               if (conn != null)
                  conn.close();
            }
            catch (Throwable th)
            {
               // Catch and discard any exceptions here
            }
         }
      }

      // If it is still null then thrown an exception
      if (m_driver == null || m_driver.trim().length() == 0)
         throw new IllegalArgumentException("Database driver type must be " +
            "defined or derivable from the datasource and may not be empty.");

   }
   


   /**
    * Obtains a connection to the database.
    *
    * @throws SQLException if database connection using the parameters
    * specified in the property file fails.
    * @throws PSJdbcTableFactoryException if database connection using the
    * datasource object fails.
    */
   public Connection getConnection()
      throws SQLException, PSJdbcTableFactoryException
   {
      Connection conn = null;
      if (m_dataSource != null)
      {
         conn = m_dataSource.getConnection();
         if (conn == null)
            throw new PSJdbcTableFactoryException(
               IPSTableFactoryErrors.SQL_CONNECTION_FAILED);
      }
      else
      {
         try
         {
            Class.forName(getDriverClassName());
         }
         catch (ClassNotFoundException cls)
         {
            throw new PSJdbcTableFactoryException(
               IPSTableFactoryErrors.SQL_CONNECTION_FAILED,
               cls.getLocalizedMessage(),cls);
         }
         catch (LinkageError link)
         {
            throw new PSJdbcTableFactoryException(
               IPSTableFactoryErrors.SQL_CONNECTION_FAILED,
               link.getLocalizedMessage(),link);
         }

         String connStr = PSSqlHelper.getJdbcUrl(getDriver(), getServer());
         Properties props = PSSqlHelper.makeConnectProperties(connStr,
            getDataBase(), getUserId(),getPassword());
         
         DriverManager.getDriver(connStr);

         // try 5 times to establish the connection before we give up
         SQLException sqe = null;
         String database = getDataBase();
         for (int i = 0; i < 5; i++)
         {
            try
            {
               conn = DriverManager.getConnection(connStr, props);
               if (conn != null)
               {
                  if (database != null)
                     conn.setCatalog(database);
                  break;
               }
            }
            catch (SQLException e)
            {
               // save, sleep and try again
               sqe = e;
            }

            try
            {
               Thread.sleep(i * 1000);
            }
            catch (InterruptedException e1)
            {
               // just ignore this
            }
         }

         // check if null
         if (null == conn)
         {
            // We get here if no drivers recognize the URL
            String message = connStr;
            if (sqe != null)
               message
                  += (" - " + PSJdbcTableFactoryException.formatSqlException(sqe));

            throw new PSJdbcTableFactoryException(
               IPSTableFactoryErrors.SQL_CONNECTION_FAILED,
               message);
         }
      }
      return conn;
   }

   /**
    * Lookup and return the correct type map for this database definition
    * @return a new data type map based on this database definition
    */
   public PSJdbcDataTypeMap getTypemap()
      throws PSJdbcTableFactoryException, IOException, SAXException
   {
      return new PSJdbcDataTypeMap(m_backendDb, m_driver, null);
   }

   /**
    * Returns the alias of the Back End database type, used to select the
    * appropriate datatype mappings.
    *
    * @return The alias, may be <code>null</code>, never empty.
    */
   public String getBackEndDB()
   {
      return m_backendDb;
   }

   /**
    * Returns the name of the Jdbc driver.
    *
    * @return The name, never <code>null</code> or empty.
    */
   public String getDriver()
   {
      return m_driver;
   }

   /**
    * Returns the class name of the Jdbc driver.
    *
    * @return The class name, never <code>null</code> or empty.
    */
   public String getDriverClassName()
   {
      return m_class;
   }

   /**
    * Returns the name of the server to connect to.
    *
    * @return The server name, might be <code>null</code> or empty if this was
    *    created through the JNDI lookup ctor.
    */
   public String getServer()
   {
      return m_server;
   }

   /**
    * Returns the name of the database to connect to.
    *
    * @return The database name, may be <code>null</code> or empty.
    */
   public String getDataBase()
   {
      return m_database;
   }

   /**
    * Returns the user id to use when connecting.
    *
    * @return The userid, may be <code>null</code> or empty.
    */
   public String getUserId()
   {
      return m_uid;
   }

   /**
    * Returns the password, unencrypted.
    *
    * @return The password, may be <code>null</code> or empty.
    */
   public String getPassword()
   {
      return m_pw;
   }

   /**
    * Returns the schema or origin to use when qualifying a table name.
    *
    * @return The schema, may be <code>null</code> or empty.
    */
   public String getSchema()
   {
      return m_schema;
   }

   /**
    * Gets a property by name, validating that it is a non-empty string
    *
    * @param props The properies object, assumed not <code>null</code>.
    * @param name The property to get, assumed not <code>null</code> or empty.
    *
    * @return The property value, never <code>null</code> or empty.
    *
    * @throws IllegalArgumentException with the appropriate message if the
    * property value is <code>null</code> or empty.
    */
   private String getRequiredProperty(Properties props, String name)
   {
      String value = props.getProperty(name);
      if (value == null || value.trim().length() == 0)
         throw new IllegalArgumentException(
            "Property " + name + " must be defined and may not be empty.");

      return value;
   }

   /**
    * compares this column to another object.
    *
    * @param obj the object to compare
    * @return <code>true</code> if the object is a PSJdbcDbmsDef with
    *    identical values. Comparison is case sensitive.  Otherwise returns
    *    <code>false</code>.
    */
   public boolean equals(Object obj)
   {
      boolean isMatch = true;
      if (obj == null || !(obj instanceof PSJdbcDbmsDef))
         isMatch = false;
      else
      {
         PSJdbcDbmsDef other = (PSJdbcDbmsDef) obj;
         if (this.m_backendDb != null ^ other.m_backendDb != null)
            isMatch = false;
         else if (!this.m_backendDb.equals(other.m_backendDb))
            isMatch = false;
         else if (!this.m_class.equals(other.m_class))
            isMatch = false;
         else if (!this.m_driver.equals(other.m_driver))
            isMatch = false;
         else if (this.m_server != null ^ other.m_server != null)
            isMatch = false;
         else if (
            this.m_server != null && !this.m_server.equals(other.m_server))
            isMatch = false;
         else if (this.m_database != null ^ other.m_database != null)
            isMatch = false;
         else if (
            this.m_database != null
               && !this.m_database.equals(other.m_database))
            isMatch = false;

         else if (this.m_uid != null ^ other.m_uid != null)
            isMatch = false;
         else if (this.m_uid != null && !this.m_uid.equals(other.m_uid))
            isMatch = false;
         else if (this.m_pw != null ^ other.m_pw != null)
            isMatch = false;
         else if (this.m_pw != null && !this.m_pw.equals(other.m_pw))
            isMatch = false;
         else if (this.m_schema != null ^ other.m_schema != null)
            isMatch = false;
         else if (
            this.m_schema != null && !this.m_schema.equals(other.m_schema))
            isMatch = false;
      }

      return isMatch;
   }

   /**
    * Overridden to fullfill the contract that if t1 and t2 are 2 different
    * instances of this class and t1.equals(t2), t1.hashCode() ==
    * t2.hashCode().
    *
    * @return The sum of all the hash codes of the composite objects.
    */
   public int hashCode()
   {
      int hash = 0;
      if (this.m_backendDb != null)
         hash += m_backendDb.hashCode();
      hash += m_class.hashCode();
      hash += m_driver.hashCode();
      if (this.m_server != null)
         hash += m_server.hashCode();
      if (this.m_database != null)
         hash += m_database.hashCode();
      if (this.m_uid != null)
         hash += m_uid.hashCode();
      if (this.m_pw != null)
         hash += m_pw.hashCode();
      if (this.m_schema != null)
         hash += m_schema.hashCode();
      return hash;
   }

   /**
    * Performs a JNDI name lookup based on the database name
    * to obtain DataSource object which will be used to
    * obtain database connections.
    *
    * Returns a <code> javax.sql.DataSource </code> object.
    *
    * @param dataSourceName name of the context for which the
    * JNDI name lookup will be done in the web servers properties file
    * (like server.xml for tomcat). The context name should be of
    * the type "jdbc/dataSourceName". This param may not be
    * <code>null</code> or empty.
    * @param environment the context environment properties to use to make the
    *    lookup, might be <code>null</code>. Uses the defaults (for Tomcat 4.0)
    *    if nothing is provided.
    * @return DataSource the requested JNDI data source, never 
    *    <code>null</code>.
    * @throws NamingException if it fails to obtain the
    * initial context for performing the JNDI name lookup.
    * @throws MissingResourceException if it fails to
    * obtain the datasource after performing the JNDI
    * name lookup.
    */
   private DataSource getDataSource(
      String dataSourceName,
      Properties environment)
      throws MissingResourceException, NamingException
   {
      if (dataSourceName == null || dataSourceName.trim().length() == 0)
         throw new IllegalArgumentException(
            "dataSourceName must be defined and may not be empty.");

      DataSource ds;
      if (environment == null)
      {
         Context initCtx = new InitialContext();
         Context envCtx = (Context) initCtx.lookup(DEFAULT_ENV);
         try
         {
            ds = lookupDatasource(envCtx, dataSourceName);
         }
         catch(NamingException e)
         {
            // For JBoss, the data sources are in java: instead, try that here
            envCtx = (Context) initCtx.lookup("java:");
            ds = lookupDatasource(envCtx, dataSourceName);
         }
      }
      else
      {
         Hashtable ht = new Hashtable();
         Iterator keys = environment.keySet().iterator();
         while (keys.hasNext())
         {
            String key = keys.next().toString();
            ht.put(key, environment.getProperty(key));
         }

         Context ctx = new InitialContext(ht);
         ds = lookupDatasource(ctx, dataSourceName);
      }

      if (ds == null)
         throw new MissingResourceException(
            "Failed to obtain datasource.",
            "javax.sql.DataSource",
            dataSourceName);

      return ds;
   }

   /**
    * Lookup datasource using jndi. Always start by looking it up prepending
    * the string "jdbc/". If this fails, then try looking it up without
    * the prepended string. This is done to maintain backward compatibility
    * for applications.
    * 
    * @param envCtx the JNDI naming context to be used for the lookup, 
    * assumed non-<code>null</code>
    * @param dataSourceName the datasource name being looked up, assumed 
    * non-<code>null</code>
    * @return the datasource, will never be <code>null</code>
    * @throws NamingException if the given datasource is not present or
    * is not accessible
    */
   private DataSource lookupDatasource(Context envCtx, String dataSourceName) 
   throws NamingException
   {
      DataSource rval;
      
      try
      {
         rval = (DataSource) envCtx.lookup("jdbc/" + dataSourceName);
      }
      catch (NamingException e)
      {
         rval = (DataSource) envCtx.lookup(dataSourceName);
      }
      return rval;
   }
   
   /**
    * Encrypts the supplied String using the rot13 algorithm on each character.
    *
    * @param val The value to encrypt.  Assumed not <code>null</code> or empty.
    *
    * @return The encrypted string, never <code>null</code> or empty.
    */
   @Deprecated
   private static String rot13(String val)
   {
      StringBuffer buf = new StringBuffer(val);
      for (int i = 0; i < buf.length(); i++)
      {
         buf.setCharAt(i, rot13(buf.charAt(i)));
      }

      return buf.toString();
   }

   /**
    * Encrypts the supplied char using the rot13 algorithm
    * @param ch The char to encrypt.
    * @return The encrypted char.
    */
   @Deprecated
   private static char rot13(char ch)
   {
      char encrypted = ch;
      if (Character.isLetter(ch))
      {
         if (Character.isUpperCase(ch))
            encrypted =  (char) (((ch - 'A') + 13) % 26 + 'A');
         else
            encrypted = (char) (((ch - 'a') + 13) % 26 + 'a');
      }
      return encrypted;
   }
   
   
   public File getBinaryStorageLocation()
   {
      return m_binaryStorageLocation;
   }

   public void setBinaryStorageLocation(File binaryStorageLocation)
   {
      this.m_binaryStorageLocation = binaryStorageLocation;
   }

   /**
    * Returns the key value to use as part one with the Rhythmyx encyrption
    * algorithm.
    *
    * @return The key, never <code>null</code> or empty.
    */
   @Deprecated
   public static String getPartOneKey()
   {
      // get the encrypted constant and decrypt.
      return rot13(PART_ONE);
   }

   /**
    * The constant for the partone key for the Rx encryption algorithm.  The
    * constant is encrytped by the {@link #rot13(char)} method.
    */
   @Deprecated
   private static final String PART_ONE = PSLegacyEncrypter.getInstance(PathUtils.getRxDir(null).getAbsolutePath().concat(PSEncryptor.SECURE_DIR)).PART_ONE();

   /**
    * The alias for the backend database type.  Initialized in the ctor, may be
    * <code>null</code>, never empty or modified after that.
    */
   private String m_backendDb;

   /**
    * The name of the Jdbc driver.  Initialized in the ctor, never <code>null
    * </code>, empty, or modified after that.
    */
   private String m_driver;

   /**
    * The Jdbc driver class name.  Initialized in the ctor, never <code>null
    * </code>, empty, or modified after that.
    */
   private String m_class;

   /**
    * The name of the server. Initialized in the ctor. Is <code>null</code> if
    * constructed using JNDI. Never empty or modified after construction.
    */
   private String m_server;

   /**
    * The name of the database.  Initialized in the ctor, may be <code>null
    * </code> or empty, never modified after that.
    */
   private String m_database;

   /**
    * The name of the schema or origin.  Initialized in the ctor, may be <code>
    * null</code> or empty, never modified after that.
    */
   private String m_schema;

   /**
    * The user id to login with.  Initialized in the ctor, may be <code>null
    * </code> or empty, never modified after that.
    */
   private String m_uid;

   /**
    * The password to login with, unencrypted.  Initialized in the ctor, may be
    * <code>null</code> or empty, never modified after that.
    */
   private String m_pw;

   /**
    * The DataSource object for JNDI connection to the database.  Initialized
    * in the ctor.
    */
   private DataSource m_dataSource = null;
   



   /**
    * Defaut string for initial JNDI context lookup.
    */
   private static String DEFAULT_ENV = "java:comp/env";

   /**
    * Binary storage location file, if not <code>null</code> the BLOB content is stored under this folder
    * with a name generated from md5hash of content and same hash is saved part of table data xml.
    */
   private File m_binaryStorageLocation = null;

    public PSConnectionDetail getConnectionDetail() {
        return null;
    }

    public IPSConnectionInfo getConnectionInfo() {
        return null;
    }
}


