/******************************************************************************
 *
 * [ PSDeliveryTierDatabaseModel.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.model;

import com.percussion.install.InstallUtil;
import com.percussion.install.RxInstallerProperties;
import com.percussion.installanywhere.IPSProxyLocator;
import com.percussion.installanywhere.RxIAModel;
import com.percussion.installer.action.RxLogger;
import com.percussion.installer.action.RxUpdateUpgradeFlag;
import com.percussion.installer.model.PSDeliveryTierProtocolModel;
import com.percussion.installer.model.PSDeliveryTierServerConnectionModel;
import com.percussion.util.PSSqlHelper;
import com.percussion.utils.jdbc.PSJdbcUtils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;


/**
 * This model represents a panel/console that asks for the database name, schema
 * name, and datasource configuration name.
 */
public class PSDeliveryTierDatabaseModel extends RxIAModel
{
   /**
    * Constructs an {@link PSDeliveryTierDatabaseModel} object.
    *  
    * @param locator the proxy locator which will retrieve the proxy used to
    * interact with the InstallAnywhere runtime platform.
    */
   public PSDeliveryTierDatabaseModel(IPSProxyLocator locator)
   {
      super(locator);
      setPersistProperties(new String[]{
            PSDeliveryTierProtocolModel.DB_NAME,
            PSDeliveryTierProtocolModel.SCHEMA_NAME,
            PSDeliveryTierProtocolModel.DSCONFIG_NAME
      });
      setPropertyFileName("rxconfig/DTS/perc-datasources.properties");
   }
   
   @Override
   protected void initModel()
   {
      super.initModel();
      String protocol = PSDeliveryTierProtocolModel.fetchDriver();
      if (!((protocol == null) || (protocol.trim().length() == 0)))
      {
         m_isDB2 = protocol.equalsIgnoreCase(PSJdbcUtils.DB2);
         m_isOracle = protocol.equalsIgnoreCase(PSJdbcUtils.ORACLE);
      }
      getSchemasAndDatabases(isDB2(), isOracle());
   }
   
   /*************************************************************************
    * Worker functions
    *************************************************************************/
   
   /**
    * Populates the combo box for schema and database.
    *
    * @param isDB2 <code>true</code> if the database driver specified by
    * the user is "db2", <code>false</code> otherwise.
    *
    * @param isOracle <code>true</code> if the database driver specified by
    * the user is "oracle:thin", <code>false</code> otherwise.
    */
   private void getSchemasAndDatabases(boolean isDB2, boolean isOracle)
   {
      if (isEmbedded())
      {
         ms_databases = new String[0];
         ms_schemas = new String[0];
         return;
      }
      
      //    get database connection
      Connection conn = InstallUtil.createConnection(
            PSDeliveryTierProtocolModel.fetchDriver(),
            PSDeliveryTierServerConnectionModel.fetchDBServer(),
            PSDeliveryTierServerConnectionModel.fetchUser(),
            PSDeliveryTierServerConnectionModel.fetchPwd());
      
      if (conn == null)
         return;
      
      try
      {
         //get the schema names
         DatabaseMetaData dbMeta = null;
         ResultSet rs            = null;
         Statement stmt          = null;
         Vector<String> vSchemas         = new Vector<>();
         try
         {
            if (isDB2)
            {
               stmt = conn.createStatement();
               rs = stmt.executeQuery(DB2_SCHEMA_QUERY);
            }
            else
            {
               dbMeta = conn.getMetaData();
               if(dbMeta == null)
                  return;
               rs = dbMeta.getSchemas();
            }
            if (rs == null)
               return;
            while (rs.next())
            {
               String schema = rs.getString(1);
               vSchemas.add(schema);
               RxLogger.logInfo("Schema : " + schema);
            }
            ms_schemas = vSchemas.toArray(new String[0]);
         }
         catch(SQLException e)
         {
            RxLogger.logInfo("ERROR : " + e.getMessage());
            RxLogger.logInfo(e);
         }
         finally
         {
            if(rs != null)
            {
               try
               {
                  rs.close();
               }
               catch(SQLException e)
               {
               }
               rs = null;
            }
         }
         
         //get the databases except for DB2 and Oracle
         if (isDB2 || isOracle)
         {
            ms_databases = new String[0];
            return;
         }
         
         Vector<String> vDbs = new Vector<>();
         ResultSet rs1 = null;
         try
         {
            rs1 = dbMeta.getCatalogs();
            if (rs1 == null)
               return;
            while (rs1.next())
            {
               String sdb = rs1.getString(1);
               vDbs.add(sdb);
               RxLogger.logInfo("Database : " + sdb);
            }
            ms_databases = vDbs.toArray(new String[0]);
            
            if(isMySql())
            {
                ms_schemas = ms_databases;
                ms_databases = new String[0];
            }
         }
         catch(SQLException e)
         {
            RxLogger.logInfo("ERROR : " + e.getMessage());
            RxLogger.logInfo(e);
         }
         finally
         {
            if(rs1 != null)
            {
               try
               {
                  rs1.close();
               }
               catch(SQLException e)
               {
               }
               rs1 = null;
            }
         }
      }
      finally
      {
         if (conn != null)
         {
            try
            {
               conn.close();
            }
            catch (Exception ex)
            {
               //no-op
            }
            
         }
      }
   }
   
   @Override
   public boolean queryEnter()
   {      
      setPersistProperties(new String[]{
            PSDeliveryTierProtocolModel.DB_NAME,
            PSDeliveryTierProtocolModel.SCHEMA_NAME,
            PSDeliveryTierProtocolModel.DSCONFIG_NAME
      });
      
      //Don't show panel if embedded DB
      if (isEmbedded())
      {
         setDatabase(RxInstallerProperties.getResources().
               getString("embedded.db_name"));
         setSchema(RxInstallerProperties.getResources().
               getString("embedded.schema_name"));
         setDSConfig(RxInstallerProperties.getResources().
               getString("embedded.dsconfig_name"));
         super.saveToPropFile();
         return false;
      }
      
      return super.queryEnter();
   }
   
   @Override
   public boolean queryExit()
   {
      if (!super.queryExit())
         return false;
      
      // make sure datasource is not empty
      if (!validateDatasource())
         return false;
      
      // make sure database name is valid
      if (!validateDatabase())
         return false;
      
      String server;
      if (isMySql())
      {
          //MySql uses database name/schema as same name but connection uses schema as a database
          setDatabase(getSchema());
          setSchema("");
          
          server = PSDeliveryTierServerConnectionModel.fetchDBServer() + '/' + getDatabase() + PSJdbcUtils.MYSQL_CONN_PARAMS;
      }
      else
      {
          setDatabase(getDatabase());
          setSchema(getSchema());
          
          server = PSDeliveryTierServerConnectionModel.fetchDBServer();
      }
            
      setDSConfig(getDSConfig());
      
      Connection conn = null;
      try
      {
         conn = InstallUtil.createConnection(
               PSDeliveryTierProtocolModel.fetchDriver(),
               server,
               getDatabase(),
               PSDeliveryTierServerConnectionModel.fetchUser(),
               PSDeliveryTierServerConnectionModel.fetchPwd());
         
         // make sure database user can create views
         if (!validateUser(conn))
            return false;
         
         // check to see if database is set to support unicode encoding
         if (!InstallUtil.checkForUnicode(conn, PSDeliveryTierProtocolModel.fetchDriver(),
               getDatabase(), getSchema()))
         {
             ms_isUnicode = false;
             boolean isContinue = validationWarning("",
                     RxInstallerProperties.getResources().getString(
                     "nonUnicodeRxRepositoryError")
                     + "\n\nContinue with the current database?", "");
             if (!isContinue)
                 return false;
         }
         else
         {
             ms_isUnicode = true;
         }
         
         return RxUpdateUpgradeFlag.checkNewInstall() ?
               validateRepository(conn) : true;
      }
      catch (SQLException e)
      {
         RxLogger.logInfo(
               "RxDatabaseModel#queryExit() : " + e.getMessage());
         RxLogger.logInfo(e);
         
         validationError("", "Database unicode support validation failed." +
               "  Please see install.log for details.", "");
         return false;
      }
      finally
      {
         if (conn != null)
         {
            try
            {
               conn.close();
            }
            catch (SQLException e)
            {
            }
         }
      }
   }
   
   @Override
   public String getTitle()
   {
      return RxInstallerProperties.getString("reptitle");
   }
   
   /**
    * Checks if "NEXTNUMBER" table exists in the repository specified by the
    * user. If exists then displays the following warning to the user:
    * <p>
    * An existing Rhythmyx repository has been detected in the database or
    * schema you specified to install. A new installation is not allowed to be 
    * performed on an existing Rhythmyx repository.  Please select a new database
    * or schema for installation.
    * </p>
    *
    * @param conn the connection to the database.  Assumed not <code>null</code>.
    * 
    * @return <code>true</code> if "NEXTNUMBER" table does not exist, otherwise
    * returns <code>false</code> and the user must change the database/schema.
    */
   private boolean validateRepository(Connection conn)
   {
      boolean exists = true;
      String err = RxInstallerProperties.getResources().getString(
      "newRxRepositoryError");
      
      try
      {
         exists = InstallUtil.checkTableExists(RX_TABLE_NAME, conn,
               ms_database, ms_schema);
      }
      catch (SQLException e)
      {
         RxLogger.logInfo("RxDatabaseModel#validateRepository() : "
               + e.getMessage());
         RxLogger.logInfo(e);
         err = "Repository validation failed.  Please see install.log for " +
            "details.";
      }
      
      if (exists)
         validationError("", err, "");
      
      return !exists;
   }
   
   /**
    * Checks to see if the datasource name is empty. If it is, then an error
    * dialog will be displayed.
    * 
    * @return <code>true</code> if the datasource is not empty,
    *         <code>false</code> otherwise.
    */
   private boolean validateDatasource()
   {
      boolean notempty = true;
      
      if (getDSConfig().trim().length() == 0)
      {
         notempty = false;
         String message = RxInstallerProperties.getResources().getString(
               "dsconfigwarn");
         
         validationError(
               "", 
               message,
         "");
      }
      
      return notempty;
   }
   
   /**
    * Checks to see if the database name contains '.' or begins with a number. 
    * If it does, then an error dialog will be displayed.  
    * 
    * @return <code>true</code> if the database name does not contain '.' and
    * does not begin with a number, <code>false</code> otherwise.
    */
   private boolean validateDatabase()
   {
      boolean valid = true;
      String message = "Database name invalid";
      
      String database = getDatabase();
      if (database.indexOf(".") != -1)
      {
         valid = false;
         message = RxInstallerProperties.getResources().getString(
         "dbcharwarn");
      }
      else if (database.trim().length() > 0)
      {
         char ch = database.charAt(0);
         String chStr = ch + "";
         if (StringUtils.isNumeric(chStr))
         {
            valid = false;
            message = RxInstallerProperties.getResources().getString(
            "dbnumwarn");
         }
      }
      
      if (!valid)
      {
         validationError(
               "", 
               message,
         "");
      }
      
      return valid;
   }
   
   /**
    * Checks to see if the database user can create views.  If it cannot, then
    * an error dialog will be displayed.  
    * 
    * @param conn the database connection object, assumed not <code>null</code>.
    * 
    * @return <code>true</code> if the database user can create views,
    * <code>false</code> otherwise.
    */
   private boolean validateUser(Connection conn)
   {
      boolean valid = true;
      String message = "Current database user cannot create views.";
      String tempTable = null;
      String tempView = null;
      String database = "";
      String schema = "";
      
      try
      {
         database = getDatabase();
         schema = getSchema();
         String driver = PSDeliveryTierProtocolModel.fetchDriver();
         
         // First create a temp table
         tempTable = PSSqlHelper.qualifyTableName(
               TEMP_NAME, database, schema, driver);
         String createTblStmt = "create table " + tempTable +
            " (tempColumn int)";
         InstallUtil.executeStatement(conn, createTblStmt);
         
         // Now create a temp view
         tempView = PSSqlHelper.qualifyViewName(
               TEMP_VIEW_NAME, database, schema, driver);
         String createVwStmt = "create view " + tempView + " as select * from "
         + tempTable;
         
         try
         {
            InstallUtil.executeStatement(conn, createVwStmt);
         }
         catch (SQLException e)
         {
            valid = false;
            message = "The database user "
               + PSDeliveryTierServerConnectionModel.fetchUser() + " does not have the "
               + "privileges necessary to create views.  This user must be "
               + "able to create views in order to successfully install "
               + "Rhythmyx.";
         }
      }
      catch (Exception e)
      {
         valid = false;
         RxLogger.logInfo("RxDatabaseModel#validateUser() : "
               + e.getMessage());
         RxLogger.logInfo(e);
         message = "Database user validation failed.  Please see install.log "
            + "for details.";
      }
      finally
      {
         try
         {
            if (InstallUtil.checkTableExists(TEMP_NAME, conn, database, schema))
            {
               // Drop the temp table
               String dropTblStmt = "drop table " + tempTable;
               InstallUtil.executeStatement(conn, dropTblStmt);
               
               if (valid)
               {
                  // Drop the temp view
                  String dropVwStmt = "drop view " + tempView;
                  InstallUtil.executeStatement(conn, dropVwStmt);
               }
            }
         }
         catch (SQLException e)
         {
            RxLogger.logError("ERROR : " + e.getMessage());
            RxLogger.logError(e);
         }
      }
      
      if (!valid)
      {
         validationError(
               "", 
               message,
         "");
      }
      
      return valid;
   }
   
   /*************************************************************************
    * Helper Methods for helper members..
    *************************************************************************/
   
   /**
    * Queries the flag which detemines if the current backend is set to embedded.
    * 
    * @return <code>true</code> if the database is embedded, <code>false</code>
    * otherwise.
    */
   public static boolean isMySql() 
   {
      return PSDeliveryTierProtocolModel.fetchDriver().equalsIgnoreCase(
            RxInstallerProperties.getResources().getString("mysqlname"));
   }
   
   /**
    * Queries the flag which detemines if the current backend is set to embedded.
    * 
    * @return <code>true</code> if the database is embedded, <code>false</code>
    * otherwise.
    */
   public boolean isEmbedded() 
   {
      return PSDeliveryTierProtocolModel.fetchDriver().equalsIgnoreCase(
            RxInstallerProperties.getResources().getString("embedded.name"));
   }
   
   /**
    * @return <code>true</code> if the current database supports unicode, <code>false</code> otherwise.
    */
   public static boolean isUnicode()
   {
       return ms_isUnicode;
   }
   
   /**
    * Queries the flag which detemines if the current backend is db2.
    * 
    * @return <code>true</code> if the database is db2, <code>false</code>
    * otherwise.
    */
   public boolean isDB2() 
   {
      return m_isDB2;
   }
   
   /**
    * Queries the flag which detemines if the current backend is oracle.
    * 
    * @return <code>true</code> if the database is oracle, <code>false</code>
    * otherwise.
    */
   public boolean isOracle() 
   {
      return m_isOracle;
   }
   
   /**
    * Sets up the current schemas and databases choices.
    * 
    * @return the set of current schema choices.
    */
   public String[] getSchemas() 
   {
      getSchemasAndDatabases(isDB2(), isOracle());
      return ms_schemas;
   }
   
   /**
    * Sets up the current schemas and databases choices.
    * 
    * @return the set of current database choices.
    */
   public String[] getDatabases() 
   {
      getSchemasAndDatabases(isDB2(), isOracle());
      return ms_databases;
   }
   
   /*************************************************************************
    * Accessors/mutators for private members   
    *************************************************************************/
   
   /**
    * The database name will be used for non-db2 and non-oracle backends.
    * 
    * @return the selected database name, empty for db2 and oracle backends.
    */
   public String getDatabase()
   {
      if (isDB2() || isOracle())
         return "";
      return ms_database;
   }
   
   /**
    * Sets the current database name selection.
    * 
    * @param str db name.
    */
   public  void setDatabase(String str)
   {
      ms_database = str;
      setValue(PSDeliveryTierProtocolModel.DB_NAME, str);
      propertyChanged("RxDatabase");
   }
   
   /**
    * @return the selected schema.
    */
   public  String getSchema()
   {
      return ms_schema;
   }
   
   /**
    * Sets the schema name.
    * 
    * @param str db schema.
    */
   public  void setSchema(String str)
   {
      ms_schema = str;
      setValue(PSDeliveryTierProtocolModel.SCHEMA_NAME, str);
      propertyChanged("RxSchema");
   }
   
   /**
    * The datasource configuration is composed of a set of values including
    * database server, user id, password, schema, and database name.
    * 
    * @return the datasource configuration name.
    */
   public String getDSConfig()
   {
      return ms_dSConfig;
   }
   
   /**
    * Sets the datasource configuration name.
    * 
    * @param str ds config name.
    */
   public void setDSConfig(String str)
   {
      ms_dSConfig = str;
      setValue(PSDeliveryTierProtocolModel.DSCONFIG_NAME, str);
      propertyChanged("RxDSConfig");
   }
   
   /**
    * Accesses persisted property value.
    * 
    * @param s the name of the property.
    * 
    * @return value for the specified property.
    */
   public Object getPropertyValue(String s)
   {
      return getValue(s);
   }
   
   /**
    * Static accessor to datasource configuration.
    * 
    * @return the name of the datasource configuration.
    */
   public static String findDSConfig()
   {
      return ms_dSConfig;
   }
   /*************************************************************************
    * Static Variables
    *************************************************************************/
   
   /**
    * Query to be executed for obtaining the schema names for DB2
    * DB2 does not return all the schema names using DatabaseMetaData's
    * <code>getSchemas</code> method
    */
   private static final String DB2_SCHEMA_QUERY =
      "SELECT SCHEMANAME FROM SYSCAT.SCHEMAAUTH";
   
   /**
    * Table to check for existence in the database
    */
   private static final String RX_TABLE_NAME = "NEXTNUMBER";
   
   /**
    * Temporary table name
    */
   private static final String TEMP_NAME = "TEMP";
   
   /**
    * Temporary view name
    */
   private static final String TEMP_VIEW_NAME = "TEMP_VIEW";
   
   /**
    * The database schema. Never <code>null</code>, set in the
    * <code>queryExit</code> method
    */
   private static String ms_schema = "";
   
   /**
    * The database name. Never <code>null</code>, set in the
    * <code>queryExit</code> method
    */
   private static String ms_database = "";
   
   /**
    * The datasource configuration name. Never <code>null</code>
    */
   private static String ms_dSConfig = "percDataSource";
   
   /*************************************************************************
    *  Private Helper Members
    *************************************************************************/
   
   /**
    * See {@link #isDB2()}.
    */
   private boolean m_isDB2;
   
   /**
    * See {@link #isOracle()}.
    */
   private boolean m_isOracle;
   
   /**
    * See {@link #getSchemas()}.
    */
   private static String[] ms_schemas;
   
   /**
    * See {@link #getDatabases()}.
    */
   private static String[] ms_databases;
   
   /**
    * See (@link #isUnicode()}.
    */
   private static boolean ms_isUnicode;
   
}
