/******************************************************************************
 *
 * [ RxVerifyConnectionModel.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.model;

import com.percussion.install.InstallUtil;
import com.percussion.install.RxFileManager;
import com.percussion.install.RxInstallerProperties;
import com.percussion.installanywhere.IPSProxyLocator;
import com.percussion.installanywhere.RxIAModel;
import com.percussion.installer.action.RxLogger;
import com.percussion.utils.security.PSEncrypter;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;


/**
 * This model represents a panel/console that verifies the database connection
 * using the values in the rxrepository.properties file.
 */
public class RxVerifyConnectionModel extends RxIAModel
{
   /**
    * Constructs an {@link RxVerifyConnectionModel} object.
    *  
    * @param locator the proxy locator which will retrieve the proxy used to
    * interact with the InstallAnywhere runtime platform.
    */
   public RxVerifyConnectionModel(IPSProxyLocator locator)
   {
      super(locator);
      setHideOnUpgrade(false);
   }
   
   @Override
   public boolean queryEnter()
   {
      // If embedded DB is not installed yet.
      if (RxProtocolModel.fetchDriver().equalsIgnoreCase(RxInstallerProperties.
            getResources().getString("embedded.name")))
      {
         return false;
      }
      
      if (!super.queryEnter())
         return false;
      
      if (!loadConnProperties())
      {
         m_errorMsg = RxInstallerProperties.getString("connpropswarn");
         return true;
      }
      
      // Test the connection and close it if a successful connection was
      // obtained
      Connection conn = null;
      try
      {
         String user = m_connProp.getProperty(REP_ID, "");
         String pwd = m_connProp.getProperty(REP_PW, "");
         if (!((pwd == null) || (pwd.trim().length() == 0)))
            pwd = PSEncrypter.decrypt(pwd, PSEncrypter.getPartOneKey());
         
         conn = InstallUtil.createConnection(
               m_driver,
               m_connProp.getProperty(REP_SERVER, ""),
               m_connProp.getProperty(REP_DATABASE, ""),
               user,
               pwd);
      }
      catch (Exception e)
      {
         m_errorMsg = RxInstallerProperties.getString("connwarn") + "\n\n" +
            e.getMessage();
         RxLogger.logInfo("exception : " + e.getMessage());         
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
            catch (SQLException e)
            {
            }
            conn = null;
            return false;
         }
      }
      return true;
   }
   
   @Override
   public String getTitle()
   {
      return RxInstallerProperties.getString("verifyconntitle");
   }
   
   /**
    * loads the connection properties from the rxrepository.properties file
    * 
    * @return <code>true</code> if properties were loaded successfully,
    * <code>false</code> otherwise.
    */
   private boolean loadConnProperties()
   {
      m_connProp.clear();
      m_driver = null;
      
      String strRootDir = getRootDir();
      String strRepPropFile = null;
      
      RxFileManager rxfm = new RxFileManager(strRootDir);
      strRepPropFile = rxfm.getRepositoryFile();
      
      File repPropFile = new File(strRepPropFile);
      if (!repPropFile.exists())
         return false;
      
      try
      {
         m_connProp = RxFileManager.loadProperties(strRepPropFile);
      }
      catch (IOException ioe)
      {
         RxLogger.logError("RxVerifyConnectionModel#loadConnProperties : " +
               ioe.getMessage());
         RxLogger.logError(ioe);
      }
            
      m_driver = m_connProp.getProperty(REP_DRIVER);
      if ((m_driver == null) || (m_driver.trim().length() == 0))
         return false;
      
      return true;
   }
   
   /**
    * This provides access to the message to be displayed if an error was
    * encountered during connection verification.
    * 
    * @return error message.
    */
   public String getErrorMsg()
   {
      return m_errorMsg;
   }
   
   /*************************************************************************
    * Static Variables
    *************************************************************************/
   
   /**
    * Database jdbc driver name property in rxrepository.properties.
    */
   private final static String REP_DRIVER = "DB_DRIVER_NAME";
   
   /**
    * Database server property in rxrepository.properties.
    */
   private final static String REP_SERVER = "DB_SERVER";
   
   /**
    * Database user login id property in rxrepository.properties.
    */
   private final static String REP_ID = "UID";
   
   /**
    * Database user login password property in rxrepository.properties.
    */
   private final static String REP_PW = "PWD";
   
   /**
    * Database name property in rxrepository.properties.
    */
   private final static String REP_DATABASE = "DB_NAME";
   /*************************************************************************
    * Private Variables
    *************************************************************************/
   
   /**
    * stores the connection properties in rxrepository.properties file
    */
   private Properties m_connProp = new Properties();
   
   /**
    * driver property in the rxrepository.properties file
    */
   private String m_driver = null;
   
   /**
    * the message to be displayed if an error was encountered during connection
    * verification
    */
   private String m_errorMsg = "";
}
