/******************************************************************************
 *
 * [ RxSilentInstallResponseFileChecking.java ]
 * 
 * COPYRIGHT (c) 1999 - 2014 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.installer.action;

import java.io.File;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.percussion.install.InstallUtil;
import com.percussion.install.PSLogger;
import com.percussion.install.RxInstallerProperties;
import com.percussion.installanywhere.RxIAAction;
import com.percussion.util.PSSqlHelper;
import com.percussion.utils.jdbc.PSDriverHelper;

public class RxSilentInstallResponseFileChecking extends RxIAAction {

   @Override
   public void execute() {

      // only do this for a silent install
      if (isSilentInstall()) {
         
         InstallUtil.setIsSilentInstall(true);
         
         // General property checking is done in this method
         
         // check if install directory is set
         String user_install_dir = getInstallValue("USER_INSTALL_DIR");
         if (user_install_dir.equals("")) {
            
            // init logger to directory where installer was started and abort install
            PSLogger.init(getInstallValue("INSTALLER_LAUNCH_DIR"));
            PSLogger.logError("Installation directory property USER_INSTALL_DIR is not set.");
            abortInstallation(100);
         
         } else {
            PSLogger.init(user_install_dir);
         }
         
         PSLogger.logInfo("Starting response file check");
   
         // check response file for required install property
         PSLogger.logInfo("Checking install type configuration property");
         String install_type = getInstallValue("install.type");
         if ((install_type.equals("new") || install_type.equals("upgrade")) == false) {
            PSLogger.logError("Invalid install type configuration property, install.type=" + install_type);
            abortInstallation(101);
         } else {
            PSLogger.logInfo("Found valid install type configuration property");
         }
         
         // check response file for required port properties
         String[] port_keys = { "port", "port.naming", "port.rmi", "port.jrmp", "port.pooled", "port.jms", "port.ajp" };
         if (checkPortProperties(port_keys) == false) {
            PSLogger.logError("Invalid server port properties");
            abortInstallation(101);
         }
         
         // check response file for specific repository properties settings
         String backend = getInstallValue("DB_BACKEND");
         if (backend.equalsIgnoreCase("derby")) {
            validateDerbySettings();
         } else if (backend.equalsIgnoreCase("mssql")) {
            validateMSSQLSettings();
         } else if (backend.equalsIgnoreCase("mysql")) { 
            validateMySQLSettings();
         } else if (backend.equals("")) {
            PSLogger.logError("Backend property, DB_BACKEND may not be empty");
            abortInstallation(100);
         } else {
            PSLogger.logError("Invalid backend property, DB_BACKEND=" + backend);
            abortInstallation(101);
         }
         
         // continue with the install
         PSLogger.logInfo("Finished checking response file");
      }
   }

   private void abortInstallation(int exit_code) {
      PSLogger.logError("Aborting installation with exit code: " + exit_code);
      installerProxy.abortInstallation(exit_code);
   }
   
   private boolean checkPortProperties(String[] keys) {

      // for now do basic syntax checking for ports values
      // in future could check for port conflicts, etc.
      boolean valid_ports = false;
      PSLogger.logInfo("Checking server port properties");
      if (checkForBlankPropertyValues(keys)) {
         valid_ports = true;
      }
      return valid_ports;
   }

   private void validateMSSQLSettings() {
      
      // check all properties for MS SQL
      String[] repository_keys = { "DB_DRIVER_NAME", "DB_DRIVER_CLASS_NAME", "DB_BACKEND", "DB_SERVER", 
            "DB_NAME", "DB_SCHEMA", "DSCONFIG_NAME", "UID", "PWD"};
      PSLogger.logInfo("Checking Microsoft SQL repository properties");
      if (checkForBlankPropertyValues(repository_keys) == false) {
         abortInstallation(100);
      }

      // connect to the database server
      PSLogger.logInfo("Connecting to the Microsoft SQL database server");
      Connection dbConnection = getConnection(getInstallValue("DB_DRIVER_NAME"), getInstallValue("DB_SERVER"), 
            getInstallValue("UID"), getInstallValue("PWD"));
      if (dbConnection == null) {
         PSLogger.logError("Failed to connect to the Microsoft SQL database server");
         abortInstallation(-1);
      } else {
         PSLogger.logInfo("Sucessfully connected to the Microsoft SQL database server");
      }
      
      // check if database exists
      String dbName = getInstallValue("DB_NAME");
      PSLogger.logInfo("Checking to see if database exists");
      if (checkIfDatabaseExists(dbConnection, dbName) == false) {
         PSLogger.logError("Failed to find database, DB_NAME=" + dbName);
         closeConnection(dbConnection);
         abortInstallation(-1);
      }
      
      // check if database schema exists, not needed for MySQL
      String dbSchema = getInstallValue("DB_SCHEMA");
      PSLogger.logInfo("Checking to see if schema exists");
      if (checkIfSchemaExists(dbConnection, dbSchema) == false) {
         PSLogger.logError("Failed to find database schema, DB_SCHEMA=" + dbSchema);
         closeConnection(dbConnection);
         abortInstallation(-1);
      }
      
      // check and see if user can create tables and views
      String dbDriverName = getInstallValue("DB_DRIVER_NAME");
      String dbUsername = getInstallValue("UID");
      PSLogger.logInfo("Checking if user can create tables and views");
      if (checkIfUserCanCreateTablesAndViews(dbConnection, dbName, "", dbDriverName, dbUsername) == false) {
         closeConnection(dbConnection);
         abortInstallation(-1);
      } else {
         PSLogger.logInfo("User can create tables and views");
      }
      
      // check database for Unicode support
      PSLogger.logInfo("Checking to see if database supports Unicode");
      if (checkDatabaseForUnicodeSupport(dbConnection, dbDriverName, dbName, dbSchema) == false) {
         closeConnection(dbConnection);
         abortInstallation(-1);
      } else {
         PSLogger.logInfo("Database has Unicode support");
      }
      
      // if it's a new install, check if repository is new
      if (getInstallValue("install.type").equals("new")) {
         if (checkIfNewRepository(dbConnection, dbName, dbSchema) == false) {
            closeConnection(dbConnection);
            abortInstallation(-1);
         }
      }
      
      // close connection
      closeConnection(dbConnection);
      
   }

   private void validateMySQLSettings() {
      
      // for MySQL, DB_SCHEMA is not set, also look for mysql.driver.location
      String[] repository_keys = { "DB_DRIVER_NAME", "DB_DRIVER_CLASS_NAME", "DB_BACKEND", "DB_SERVER", 
            "DB_NAME", "DSCONFIG_NAME", "mysql.driver.location", "UID", "PWD"};
      PSLogger.logInfo("Checking MySQL repository properties");
      if (checkForBlankPropertyValues(repository_keys) == false) {
         abortInstallation(100);
      }
      
      // check for MySQL driver file, not needed for Microsoft SQL
      String mysql_driver_file = getInstallValue("mysql.driver.location");
      PSLogger.logInfo("Looking for a valid MySQL driver file");
      if (checkDriverFile(mysql_driver_file, getInstallValue("DB_DRIVER_CLASS_NAME")) == false) {
         PSLogger.logError("Invalid MySQL driver information specified");
         abortInstallation(-1);
      } else {
         PSLogger.logInfo("Found valid MySQL driver file");
      }

      // valid driver, add it to the list
      InstallUtil.addJarFileUrl(mysql_driver_file);
      
      // connect to the database server
      PSLogger.logInfo("Connecting to the MySQL database server");
      Connection dbConnection = getConnection(getInstallValue("DB_DRIVER_NAME"), getInstallValue("DB_SERVER"), 
            getInstallValue("UID"), getInstallValue("PWD"));
      if (dbConnection == null) {
         PSLogger.logError("Failed to connect to the MySQL database server");
         abortInstallation(-1);
      } else {
         PSLogger.logInfo("Sucessfully connected to the MySQL database server");
      }
      
      // check if database exists
      String dbName = getInstallValue("DB_NAME");
      PSLogger.logInfo("Checking to see if database exists");
      if (checkIfDatabaseExists(dbConnection, dbName) == false) {
         PSLogger.logError("Failed to find database, DB_NAME=" + dbName);
         closeConnection(dbConnection);
         abortInstallation(-1);
      }
      
      // check and see if user can create tables and views
      String dbDriverName = getInstallValue("DB_DRIVER_NAME");
      String dbUsername = getInstallValue("UID");
      PSLogger.logInfo("Checking if user can create tables and views");
      if (checkIfUserCanCreateTablesAndViews(dbConnection, dbName, "", dbDriverName, dbUsername) == false) {
         closeConnection(dbConnection);
         abortInstallation(-1);
      } else {
         PSLogger.logInfo("User can create tables and views");
      }
      
      // check database for Unicode support
      PSLogger.logInfo("Checking to see if database supports Unicode");
      if (checkDatabaseForUnicodeSupport(dbConnection, dbDriverName, dbName, "") == false) {
         closeConnection(dbConnection);
         abortInstallation(-1);
      } else {
         PSLogger.logInfo("Database has Unicode support");
      }
      
      // if it's a new install, check if repository is new
      if (getInstallValue("install.type").equals("new")) {
         if (checkIfNewRepository(dbConnection, dbName, "") == false) {
            closeConnection(dbConnection);
            abortInstallation(-1);
         }
      }
      
      // close connection
      closeConnection(dbConnection);

   }

   private void validateDerbySettings() {
      
      // for Derby, DB_NAME is not set
      String[] repository_keys = { "DB_DRIVER_NAME", "DB_DRIVER_CLASS_NAME", "DB_BACKEND", "DB_SERVER", 
            "DB_SCHEMA", "DSCONFIG_NAME" };
      PSLogger.logInfo("Checking Derby repository properties");
      if (checkForBlankPropertyValues(repository_keys) == false) {
         PSLogger.logError("Invalid Derby repository properties");
         abortInstallation(-1);
      }
      
   }

   // basic check for properties
   private boolean checkForBlankPropertyValues(String[] keys) {

      boolean valid_properties = true;
      
      String value = null;
      for (String key: keys) {
         value = getInstallValue(key);
         if (value.equals("")) {
            PSLogger.logError("Reposonse file doesn't have " + key + " set");
            valid_properties = false;
            return false;
         } else {
            //PSLogger.logInfo("Checked: " + key + "=" + value);
         }
      }
      return valid_properties;
   }
   
   // Initial prototype method found in com.percussion.installer.model.RxDatabaseDriverLocationModel.validateDriver(String driverLocation)
   private boolean checkDriverFile(String driverLocation, String driverClass) {

      if (!((driverLocation == null) || (driverLocation.trim().length() == 0))) {
         
         try {
            
            // check if driver is a file
            File f = new File(driverLocation);
            if (!f.isFile()) {
               
               PSLogger.logError("File doesn't exist: " + driverLocation);
               return false;
               
            } else {
               
               // check if file contains the driver class
               try {
                  PSDriverHelper.getDriver(driverClass, driverLocation);
               } catch (ClassNotFoundException e) {
                  PSLogger.logError("Driver class not found: " + driverClass + " in " + driverLocation);
                  return false;
               } catch (MalformedURLException e) {
                  PSLogger.logError("MalformedURLException: " + e.getMessage());
                  return false;
               } catch (InstantiationException e) {
                  PSLogger.logError("InstantiationException: " + e.getMessage());
                  return false;
               } catch (IllegalAccessException e) {
                  PSLogger.logError("IllegalAccessException: " + e.getMessage());
                  return false;
               }
            }
            
         } catch (NullPointerException e) {
            PSLogger.logError("NullPointerException: " + e.getMessage());
            return false;
         } catch (SecurityException e) {
            PSLogger.logError("SecurityException: " + e.getMessage());
            return false;
         }
      } else {
         PSLogger.logError("Driver location may not be null or empty");
         return false;
      }
      return true;
   }
   
   // Initial prototype method found in com.percussion.installer.model.RxServerConnectionModel.queryExit()
   private Connection getConnection(String dbDriverName, String dbServer, String dbUsername, String dbPassword) {
      
      Connection con = null;
      try {
         con = InstallUtil.createConnection(dbDriverName, dbServer, dbUsername, dbPassword);
      } catch (Exception e) {
         PSLogger.logError("Failed to connected to database server: " + e.getMessage());
      }

      return con;
   }

   private void closeConnection(Connection con) {
      
      if (con != null) {
         try {
            con.close();
            PSLogger.logInfo("Closed connection to database server");
         } catch (SQLException e) {
            PSLogger.logError("SQLException when closing database connection: " + e.getMessage());
         }
      }
   }
   
   /**
    * Checks if "NEXTNUMBER" table exists in the repository specified by the
    * user. If exists then displays the following warning to the user:
    * 
    * An existing Rhythmyx repository has been detected in the database or
    * schema you specified to install. A new installation is not allowed to be 
    * performed on an existing Rhythmyx repository.  Please select a new database
    * or schema for installation.
    */
   // prototype method from com.percussion.installer.model.RxDatabaseModel.validateRepository(Connection conn)
   private boolean checkIfNewRepository(Connection conn, String database, String schema) {
      
      boolean exists = true;

      try {
         
         exists = InstallUtil.checkTableExists("NEXTNUMBER", conn, database, schema);
         
      } catch (SQLException e) {
         PSLogger.logError("SQLException occurred when checking if table exists: " + e.getMessage());
      }

      if (exists) {
         PSLogger.logError(RxInstallerProperties.getResources().getString("newRxRepositoryError"));
      }

      return !exists;
   }  

   // prototype method from com.percussion.installer.model.RxDatabaseModel.queryExit()
   private boolean checkDatabaseForUnicodeSupport(Connection conn, String driver, String database, String schema) {

      boolean supportsUnicode = false;
      
      try {

         // check to see if database is set to support unicode encoding
         if (!InstallUtil.checkForUnicode(conn, driver, database, schema)) {
            
            supportsUnicode = false;
            PSLogger.logError(RxInstallerProperties.getResources().getString("nonUnicodeRxRepositoryError"));

         } else {
            supportsUnicode = true;
         }
      } catch (SQLException e) {
         
         PSLogger.logError("SQLException when checking database for Unicode support: " + e.getMessage());
         supportsUnicode = false;

      }
      
      return supportsUnicode;
   }

   // prototype method from com.percussion.installer.model.RxDatabaseModel.validateUser(Connection conn)
   private boolean checkIfUserCanCreateTablesAndViews(Connection conn, String database, String schema, String driver, String username) {
      
      boolean validUser = true;
      String message = "Current database user cannot create views.";
      String tempTable = null;
      String tempView = null;

      try {
         
         // create a temporary table
         tempTable = PSSqlHelper.qualifyTableName("TEMP", database, schema, driver);
         String createTableStatement = "create table " + tempTable + " (tempColumn int)";
         PSLogger.logInfo("Creating test table :"+createTableStatement);
         InstallUtil.executeStatement(conn, createTableStatement);

         // create a temporary view
         tempView = PSSqlHelper.qualifyViewName("TEMP_VIEW", database, schema, driver);
         String createViewStatement = "create view " + tempView + " as select * from " + tempTable;

         
         try {
            PSLogger.logInfo("Creating test view :"+createViewStatement);
            InstallUtil.executeStatement(conn, createViewStatement);

         } catch (SQLException e) {
            
            PSLogger.logError(e);
            
            validUser = false;
            message = "The database user " + username + " does not have the "
                  + "privileges necessary to create views.  This user must be "
                  + "able to create views in order to successfully install.";
         }
         
      } catch (Exception e) {
         
         PSLogger.logError(e);
         
         validUser = false;
         message = "The database user " + username + " does not have the "
               + "privileges necessary to create tables.  This user must be "
               + "able to create tables in order to successfully install.";

      } finally {
         try {
            if (InstallUtil.checkTableExists("TEMP", conn, database, schema)) {
               // Drop the temporary table
               String dropTableStatement = "drop table " + tempTable;
               PSLogger.logInfo("dropping test table "+dropTableStatement);
               InstallUtil.executeStatement(conn, dropTableStatement);

               if (validUser) {
                  // Drop the temporary view
                  String dropViewStatement = "drop view " + tempView;
                  PSLogger.logInfo("dropping test view " + dropViewStatement);
                  InstallUtil.executeStatement(conn, dropViewStatement);
               }
            }
         } catch (SQLException e) {
            PSLogger.logError(e);
            validUser = false;
            message = "SQLException occurred when dropping temporary table or view: " + e.getMessage();

         }
      }

      if (!validUser) {
         PSLogger.logError(message);
      }

      return validUser;
   }

   
   // prototype method from com.percussion.installer.model.RxDatabaseModel.getSchemasAndDatabases(boolean isDB2, boolean isOracle)
   private boolean checkIfSchemaExists(Connection dbConnection, String schemaName) {

      boolean exists = false;
      DatabaseMetaData dbMetaData = null;
      ResultSet dbResultSet = null;
      
      try {
            
         dbMetaData = dbConnection.getMetaData();
         
         if (dbMetaData != null) {
            dbResultSet = dbMetaData.getSchemas();
            if (dbResultSet != null) {
               String schema = "";
               while (dbResultSet.next()) {
                  schema = dbResultSet.getString(1);
                  if (schema.equals(schemaName)) {
                     exists = true;
                     PSLogger.logInfo("Found schema match for: " + schema);
                  } else {
                     //PSLogger.logInfo("Found schema: " + schema);
                  }
               }
            } else {
               PSLogger.logInfo("Didn't find any schemas");
            }
         }
     
      } catch (SQLException e) {
         
         PSLogger.logError("SQL exception: " + e.getMessage());
         e.printStackTrace();
         
      } finally {
         if (dbResultSet != null) {
            try {
               dbResultSet.close();
            } catch (SQLException e) {
               PSLogger.logError("SQL exception closing result set: " + e.getMessage());
            }
            dbResultSet = null;
         }
      }
      return exists;
   }
   
   // prototype method from com.percussion.installer.model.RxDatabaseModel.getSchemasAndDatabases(boolean isDB2, boolean isOracle)
   private boolean checkIfDatabaseExists(Connection dbConnection, String databaseName) {

      boolean exists = false;
      DatabaseMetaData dbMetaData = null;
      ResultSet dbResultSet = null;

      try {

         dbMetaData = dbConnection.getMetaData();

         if (dbMetaData != null) {
            dbResultSet = dbMetaData.getCatalogs();
            if (dbResultSet != null) {
               String database = "";
               while (dbResultSet.next()) {
                  database = dbResultSet.getString(1);
                  if (database.equals(databaseName)) {
                     exists = true;
                     PSLogger.logInfo("Found database match for: " + database);
                  } else {
                     //PSLogger.logInfo("Found database: " + database);
                  }
               }
            } else {
               PSLogger.logInfo("Didn't find any databases");
            }
         }

      } catch (SQLException e) {
         
         PSLogger.logError("SQL exception: " + e.getMessage());
         e.printStackTrace();
         
      } finally {
         if (dbResultSet != null) {
            try {
               dbResultSet.close();
            } catch (SQLException e) {
               PSLogger.logError("SQLException closing result set: " + e.getMessage());
            }
            dbResultSet = null;
         }
      }
      return exists;
   }
}