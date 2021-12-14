/******************************************************************************
 *
 * [ PSDeliveryTierServerConnectionModel.java ]
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
import com.percussion.util.PSSqlHelper;
import com.percussion.util.PSStringTemplate;
import com.percussion.utils.jdbc.PSJdbcUtils;
import com.percussion.utils.security.PSEncrypter;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;


/**
 * This model represents the panel/console that only asks for the database
 * server, userid, and password.
 */
public class PSDeliveryTierServerConnectionModel extends RxIAModel
{  
   /**
    * Constructs an {@link PSDeliveryTierServerConnectionModel} object.
    *  
    * @param locator the proxy locator which will retrieve the proxy used to
    * interact with the InstallAnywhere runtime platform.
    */
   public PSDeliveryTierServerConnectionModel(IPSProxyLocator locator)
   {
      super(locator);
      setPersistProperties(new String[]{
            PSDeliveryTierProtocolModel.DB_SERVER_NAME,
            PSDeliveryTierProtocolModel.USER_ID,
            PSDeliveryTierProtocolModel.PWD
      });
      setPropertyFileName("rxconfig/DTS/perc-datasources.properties");
   }
   
   @Override
   protected void initModel()
   {
      super.initModel();
      setTemplateDatabaseServerConnString();
   }
   
   @Override
   public boolean queryEnter()
   { 
      setPersistProperties(new String[]
                                      {
            PSDeliveryTierProtocolModel.DB_SERVER_NAME,
            PSDeliveryTierProtocolModel.USER_ID,
            PSDeliveryTierProtocolModel.PWD
                                      });
      if (PSDeliveryTierProtocolModel.fetchDriver().equalsIgnoreCase(RxInstallerProperties.
            getResources().getString("embedded.name")))
      {
         setDBServer(RxInstallerProperties.getResources().
               getString("embedded.db_server_name"));
         setUSER(RxInstallerProperties.getResources().
               getString("embedded.user_id"));
         setPWD(RxInstallerProperties.getResources().
               getString("embedded.pwd"));
         super.saveToPropFile();
         //Don't show panel if embedded DB     
         return false;
      }
      
      setTemplateDatabaseServerConnString();
      return super.queryEnter();
   }
   
   @Override
   public boolean queryExit()
   {
      if (!super.queryExit())
         return false;
      
      String dbServer = getDBServer().trim();
      String user     = getUSER().trim();
      String pwd      = getPWD().trim();
      
      boolean bConnection = false;
      Connection conn = null;
      try
      {
         conn = InstallUtil.createConnection(PSDeliveryTierProtocolModel.fetchDriver(), dbServer, user, pwd);
         if (conn != null)
            bConnection = true;
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
      // if DB connection failed do not continue
      if (!bConnection)
      {
         String warnMsg = "Server: " + dbServer + " or Login ID: " + user
         + " or Password is probably incorrect";
         
         validationError(
               RxInstallerProperties.getResources().getString("connwarn"), 
               "Information entered in this panel is incorrect",
               warnMsg);
         
         return false;
      }
      
      // check oracle version
      if (!isSupportedOracleVersion())
         return false;
      
      // set the static and persistent values
      setDBServer(dbServer);
      setUSER(user);
      setPWD(pwd);
      return true;
   }
   
   @Override
   public String getTitle()
   {
      return RxInstallerProperties.getString("reptitle");
   }
   
   /**
    * Checks if the oracle database is a supported version (9.2+).
    *
    * @return <code>true</code> if the database is not oracle or if the oracle 
    * database is version 9.2 or greater, <code>false</code> otherwise.
    */
   private boolean isSupportedOracleVersion()
   {
      String driver = PSDeliveryTierProtocolModel.fetchDriver();
      
      if (!driver.equalsIgnoreCase(PSJdbcUtils.ORACLE))
         return true;
      
      // check if the database is version 9.2 or greater
      Connection conn = null;
      int result = 0;
      
      try
      {
         // get database connection
         conn = InstallUtil.createConnection(
               PSDeliveryTierProtocolModel.fetchDriver(),
               PSDeliveryTierServerConnectionModel.fetchDBServer(),
               PSDeliveryTierServerConnectionModel.fetchUser(),
               PSDeliveryTierServerConnectionModel.fetchPwd());
         
         if (conn != null)
            result = PSSqlHelper.compareVersions(conn,
                  PSSqlHelper.MIN_VERSION_ORACLE);
      }
      catch (Exception e)
      {
         RxLogger.logInfo(
               "PSDeliveryTierServerConnectionModel#checkOracleVersion() : "
               + e.getMessage());
         RxLogger.logInfo(e);
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
      
      // display message if oracle database version is less than supported
      // minimum
      if (result == -1)
      {
         String message = RxInstallerProperties.getResources().getString(
         "oracleVersionNotSupported");
         
         validationError(
               "",
               message,
         "");
      }
      
      return result != -1;
   }  
   
   /**
    * Sets template DB server JDBC connection string and label.  The label is
    * based on the selected driver and the server url points at the current
    * system.  
    */
   protected void setTemplateDatabaseServerConnString()
   {
      try
      {
         String driver = PSDeliveryTierProtocolModel.fetchDriver();
         
         Map<String, String> vars = new HashMap<>();
         vars.put("HostName", InstallUtil.getMyHostName());
         
         //escape ':' in the driver name by underscore
         String label = 
            RxInstallerProperties.getString("databaseServerLabel."
                  + driver.replace(':','_')); 
         
         //escape ':' in the driver name by underscore
         String format = 
            RxInstallerProperties.getString("databaseServerFormat."
                  + driver.replace(':', '_')); 
         
         PSStringTemplate template = new PSStringTemplate(format);
         
         m_ServerLabelExample = label;
         
         String newServerTextStr = template.expand(vars);
         
         //Clear db server, user, password settings if there has been a change
         //in db server
         if (m_ServerTextStr != null)
         {
            if (m_ServerTextStr.compareTo(newServerTextStr) != 0)
               initDbUserPass();
         }
         
         m_ServerTextStr = newServerTextStr;
      }
      catch(Exception ex)
      {
         RxLogger.logError(ex.getLocalizedMessage());
         RxLogger.logError(ex);
         
         m_ServerLabelExample = "Database Server: ";
         m_ServerTextStr = "";
      }
   }
   
   /**
    * An example server connection url is provided based on the jdbc driver.
    * 
    * @return the example connection url for the selected driver.
    */
   public String getServerLabelExampleString()
   {
      return m_ServerLabelExample;
   }
   
   /**
    * The server textfield is populated with a default server connection url.
    * 
    * @return the default server connection url for the driver and system. 
    */
   public String getServerTextStr()
   {
      return m_ServerTextStr;
   }
   
   /*************************************************************************
    * public functions
    *************************************************************************/
   
   /**
    * Returns the database server.
    *
    * @return the database server, never <code>null</code> or empty if called
    * by a panel after this panel.
    */
   public static String fetchDBServer()
   {
      return ms_dbServer;
   }
   
   /**
    * Returns the database user.
    *
    * @return the database user, never <code>null</code> or empty if called
    * by a panel after this panel.
    */
   public static String fetchUser()
   {
      return ms_user;
   }
   
   /**
    * Returns the database user's password.
    *
    * @return the database user's password, never <code>null</code> if called
    * by a panel after this panel, may be empty
    */
   public static String fetchPwd()
   {
      return ms_pwd;
   }
   
   /**
    * See {@link #fetchDBServer()}.
    * 
    * @return the name of the database server.
    */
   public String getDBServer()
   {
      return fetchDBServer();
   }
   
   /**
    * See {@link #fetchPwd()}.
    * 
    * @return the database user login password.
    */
   public String getPWD()
   {
      return fetchPwd();
   }
   
   /**
    * See {@link #fetchUser()}.
    * 
    * @return the database user login id.
    */
   public String getUSER()
   {
      return fetchUser();
   }
   
   /**
    * Sets the database server name.
    * 
    * @param server db server name.
    */
   public void setDBServer(String server)
   {
      ms_dbServer = server;
      setValue(PSDeliveryTierProtocolModel.DB_SERVER_NAME, server);
      propertyChanged("RxDBServer");
   }
   
   /**
    * Sets the database user login password.  Stores the encrypted version of
    * the password as a persisted property.
    * 
    * @param pwd the db user login password.
    */
   public void setPWD(String pwd)
   {
      ms_pwd = pwd;
      setValue(PSDeliveryTierProtocolModel.PWD,
            PSEncrypter.encrypt(pwd, PSEncrypter.getPartOneKey()));
      propertyChanged("RxPWD");
   }
   
   /**
    * Sets the database user login id.
    * 
    * @param user the db user login id.
    */
   public void setUSER(String user)
   {
      ms_user = user;
      setValue(PSDeliveryTierProtocolModel.USER_ID, user);
      propertyChanged("RxUSER");
   }
   
   /**
    * Re-initializes the database server, user, and password to <code>""</code>.
    */
   protected void initDbUserPass()
   {
      ms_dbServer = "";
      ms_user = "";
      ms_pwd = "";
   }
   
   /**
    * See {@link #getServerLabelExampleString()}.
    */
   protected String m_ServerLabelExample = "Database Server: ";
   
   /**
    * See {@link #getServerTextStr()}.
    */
   protected String m_ServerTextStr = "";
   
   /*************************************************************************
    * Static Variables
    *************************************************************************/
   
   /**
    * Database server name. Never <code>null</code>, set in
    * the <code>queryExit</code> method
    */
   private static String ms_dbServer = "";
   
   /**
    * Database user name. Never <code>null</code>, set in
    * the <code>queryExit</code> method
    */
   private static String ms_user = "";
   
   /**
    * Database user's unencrypted password. Never <code>null</code>, set in
    * the <code>queryExit</code> method
    */
   private static String ms_pwd = "";
}





