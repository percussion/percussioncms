/******************************************************************************
 *
 * [ RxDB2ConnectionModel.java ]
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
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.tablefactory.RxJdbcTableFactory;
import com.percussion.tablefactory.install.RxLogTables;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.util.Properties;


/**
 * This is a model for the panel/console that prompts the user for the server
 * connection info on pre-5.7.1 upgrades which used the type 2 DB2 driver.  The
 * type 4 driver connection url syntax is different, and thus, requires this
 * panel during upgrade.
 */
public class RxDB2ConnectionModel extends RxIAModel
{
   /**
    * Constructs an {@link RxDB2ConnectionModel} object.
    * 
    * @param locator the proxy locator which will retrieve the proxy used to
    * interact with the InstallAnywhere runtime platform.
    */
   public RxDB2ConnectionModel(IPSProxyLocator locator)
   {
      super(locator);
      setHideOnUpgrade(false);
   }
   
   /**
    * Sets the database connection info properties to the appropriate defaults
    * or loads them from the properties files.
    */  
   @Override
   public void initModel()
   {
      super.initModel();
      String strRootDir = null;
      String strRepPropFile = null;
      String strServerPropFile = null;
      Properties repositoryProps = new Properties();
      Properties serverProps = new Properties();
      String strServer = "";
      
      try
      {
         strRootDir = getRootDir();
         
         if (!(strRootDir.endsWith(File.separator)))
            strRootDir += File.separator;
         
         strServerPropFile = strRootDir + m_serverPropertyFile;
         File serverPropFile = new File(strServerPropFile);
         if (serverPropFile.exists())
         {
            serverProps.load(new FileInputStream(strServerPropFile));
            
            // Convert to the new format
            strServer = serverProps.getProperty(
                  InstallUtil.SERVER_PROPERTY,
                  ""
            );
            strServer = "//" + InstallUtil.getMyHostName() + ":50000/"
            + strServer;
            
            m_userId = serverProps.getProperty(
                  InstallUtil.ID_PROPERTY,
                  ""
            );
            m_password = RxJdbcTableFactory.eatLasagna(m_userId,
                  serverProps.getProperty(
                        InstallUtil.PW_PROPERTY,
                        ""
                  ));
            m_driver = serverProps.getProperty(
                  InstallUtil.DRIVER_PROPERTY,
                  ""
            );
         }
         else
         {
            strRepPropFile = strRootDir + m_repositoryPropertyFile;
            File repPropFile = new File(strRepPropFile);
            if (repPropFile.exists())
            {
               repositoryProps.load(new FileInputStream(strRepPropFile));
               
               // Convert to the new format
               strServer = repositoryProps.getProperty(
                     PSJdbcDbmsDef.DB_SERVER_PROPERTY,
                     ""
               );
               strServer = "//" + InstallUtil.getMyHostName()
               + ":50000/" + strServer;
               
               m_userId = repositoryProps.getProperty(
                     PSJdbcDbmsDef.UID_PROPERTY,
                     ""
               );
               m_password = RxJdbcTableFactory.eatLasagna(m_userId,
                     repositoryProps.getProperty(
                           PSJdbcDbmsDef.PWD_PROPERTY,
                           ""
                     ));
               m_driver = repositoryProps.getProperty(
                     PSJdbcDbmsDef.DB_DRIVER_NAME_PROPERTY,
                     ""
               );
            }
            else
            {
               RxLogger.logError("RxDB2ConnectionModel#initPanel : "
                     + "Could not find properties file " + strServerPropFile
                     + " or " + strRepPropFile);
            }
         }
         
         setDBServer(strServer);
      }
      catch (Exception e)
      {
         RxLogger.logInfo("exception : " + e.getMessage());
      }
   }
   
   /**
    * Panel validator to validate supplied connection information. If any
    * errors exist, this will disallow moving to next panel in the installer
    * wizard.
    * 
    * @return <code>true</code> if the validation passed, <code>false</code>
    * otherwise.
    */
   private boolean validateModelData()
   {
      String dbServer = getDBServer().trim();
      
      boolean bConnection = false;
      Connection conn = null;
      try
      {
         conn = RxLogTables.createConnection(m_driver, dbServer, null, m_userId,
               m_password);
         if (conn != null)
            bConnection = true;
         
         // if DB connection failed do not continue
         if (!bConnection)
         {
            String warnMsg = "Server: " + dbServer + " is incorrect";
            
            validationError(
                  RxInstallerProperties.getResources().getString("connwarn"), 
                  "Information entered in this panel is incorrect",
                  warnMsg);
            
            return false;
         }
         
         return true;
      }
      catch (Exception e)
      {
         RxLogger.logError("RxDB2ConnectionModel#validatePanelData :"
               + e.getMessage());
         
         validationError(
               RxInstallerProperties.getResources().getString("connwarn"), 
               "An error has occurred while validating panel data",
         "See the install.log for details");
         
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
      return super.queryEnter();
   }
   
   @Override
   public boolean queryExit()
   {
      if (!super.queryExit())
         return false;
      
      return validateModelData(); 
   }
   
   @Override
   public String getTitle()
   {
      // Return the empty string here to override the generic title of the 
      // parent class.  A specific title is not required for this model's
      // associated panel/console.
      return "";
   }
   
   /***********************************************************************
    * Public functions
    ***********************************************************************/
   
   /**
    * Returns the database server.
    *
    * @return the database server, never <code>null</code>, may be empty.
    */
   public static String fetchDBServer()
   {
      return ms_dbServer;
   }
   
   /*************************************************************************
    * Properties Accessors and Mutators
    *************************************************************************/
   
   /**
    * @return The connection url returned by {@link #fetchDBServer()}
    */
   public  String getDBServer()
   {
      return fetchDBServer();
   }
   
   /**
    * @param server The connection url for this database
    */
   public void setDBServer(String server)
   {
      ms_dbServer = server;
      propertyChanged("Server");
   }
   
   /**
    * Returns the repository property file name.
    * @return the repository property file name, never <code>null</code>
    * or empty.
    */
   public String getRepositoryPropertyFile()
   {
      return m_repositoryPropertyFile;
   }
   
   /**
    * Sets the repository property file name.
    * @param repositoryPropertyFile the repository property file name,
    * may not be <code>null</code> or empty
    */
   public void setRepositoryPropertyFile(String repositoryPropertyFile)
   {
      if ((repositoryPropertyFile == null) ||
            (repositoryPropertyFile.trim().length() == 0))
         throw new IllegalArgumentException(
         "repositoryPropertyFile may not be null or empty");
      this.m_repositoryPropertyFile = repositoryPropertyFile;
   }
   
   /**
    * Returns the server property file name.
    * @return the server property file name, never <code>null</code>
    * or empty.
    */
   public String getServerPropertyFile()
   {
      return m_serverPropertyFile;
   }
   
   /**
    * Sets the server property file name.
    * @param serverPropertyFile the server property file name,
    * may not be <code>null</code> or empty
    */
   public void setServerPropertyFile(String serverPropertyFile)
   {
      if ((serverPropertyFile == null) ||
            (serverPropertyFile.trim().length() == 0))
         throw new IllegalArgumentException(
         "serverPropertyFile may not be null or empty");
      this.m_serverPropertyFile = serverPropertyFile;
   }
   
   /*************************************************************************
    * Properties
    *************************************************************************/
   
   /**
    * Database server name. Never <code>null</code>, set in
    * the <code>queryExit</code> method
    */
   private static String ms_dbServer = "";
   
   /**
    * relative file path of the repository properties file,
    * never <code>null</code> or empty
    */
   private String m_repositoryPropertyFile =
      "rxconfig/Installer/rxrepository.properties";
   
   /**
    * relative file path of the server properties file,
    * never <code>null</code> or empty
    */
   private String m_serverPropertyFile =
      "rxconfig/Server/server.properties";
   
   /**
    * user id, never <code>null</code>
    */
   private String m_userId = "";
   
   /**
    * password, never <code>null</code>
    */
   private String m_password = "";
   
   /**
    * driver, never <code>null</code>
    */
   private String m_driver = "";
}
