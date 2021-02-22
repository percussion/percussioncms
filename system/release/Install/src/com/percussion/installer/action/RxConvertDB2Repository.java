/******************************************************************************
 *
 * [ RxConvertDB2Repository.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.action;

import com.percussion.install.InstallUtil;
import com.percussion.install.RxInstallerProperties;
import com.percussion.installanywhere.RxIAAction;
import com.percussion.installer.RxVariables;
import com.percussion.installer.model.RxDB2ConnectionModel;
import com.percussion.tablefactory.PSJdbcDbmsDef;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;


/**
 * This action converts DB2 type 2 driver repository and server properties files
 * into the type 4 driver format.
 * 
 * <p>The serverName property will be modified as follows:
 * 
 * <ul>
 * <li>
 * Old format: "DB_SERVER/serverName=Alias"
 * </li>
 * <li>
 * New format: "DB_SERVER/serverName=//server:50000/Alias"
 * </li>
 * </ul>
 * 
 * <p>This change is required as a result of the format of the connection url
 * used by the DB2 type 4 driver.
 *  
 * <p>The DB_DRIVER_CLASS_NAME/loggerClassName property will be modified as
 * follows:
 * 
 * <ul>
 * <li>
 * Old format: "COM.ibm.db2.jdbc.app.DB2Driver"
 * </li>
 * <li>
 * New format: "com.ibm.db2.jcc.DB2Driver"
 * </li>
 * </ul>
 * 
 * <p>This change is required as a result of the change in packages for the DB2
 * type 4 driver class.
 */
public class RxConvertDB2Repository extends RxIAAction
{
   @Override
   public void execute()
   {
      String strRootDir = null;
      String strRepPropFile = null;
      String strServerPropFile = null;
      Properties repProps = new Properties();
      Properties serverProps = new Properties();
      String strServer = "";
      
      try
      {
         strRootDir = getInstallValue(RxVariables.INSTALL_DIR);
         
         if (!(strRootDir.endsWith(File.separator)))
            strRootDir += File.separator;
         
         strServerPropFile = strRootDir + m_serverPropertyFile;
         File serverPropFile = new File(strServerPropFile);
         
         strRepPropFile = strRootDir + m_repositoryPropertyFile;
         File repPropFile = new File(strRepPropFile);
         
         if (serverPropFile.exists())
         {
            try(FileInputStream fs = new FileInputStream(strServerPropFile)) {
               serverProps.load(fs);
               strServer = serverProps.getProperty(
                       InstallUtil.SERVER_PROPERTY,
                       ""
               );
            }
         }
         else
         {
            if (repPropFile.exists())
            {
               try(FileInputStream fs = new FileInputStream(strRepPropFile)) {
                  repProps.load(fs);

                  strServer = repProps.getProperty(IPSJdbcDbmsConstants.DB_SERVER_PROPERTY,
                          ""
                  );
               }
            }
            else
            {
               RxLogger.logError("RxConvertDB2Repository#execute : "
                     + "Could not find properties file " + strServerPropFile
                     + " or " + strRepPropFile);
               return;
            }
         }
         
         // Perform conversion
         RxLogger.logInfo("Converting repository/server properties server "
               + "and driver class properties to type 4 driver format");
         
         RxLogger.logInfo("Current server format: " + strServer);
         
         strServer = RxDB2ConnectionModel.fetchDBServer();
         
         RxLogger.logInfo("New server format: " + strServer);
         
         String strDriverClass = RxInstallerProperties.getString("db2");
         RxLogger.logInfo("New driver class: " + strDriverClass);
         
         // Set the new properties and save
         if (serverPropFile.exists())
         {
            try(FileInputStream fs = new FileInputStream(strServerPropFile)) {
               serverProps.load(fs);
               serverProps.setProperty(InstallUtil.SERVER_PROPERTY, strServer);
               serverProps.setProperty(InstallUtil.CLASS_PROPERTY, strDriverClass);
               try (FileInputStream fs = new FileInputStream(strServerPropFile)) {
                  serverProps.store(fs, null);
               }
            }
         }
         
         if (repPropFile.exists())
         {
            try(FileInputStream fs = new FileInputStream(strRepPropFile)) {
               repProps.load(fs);
               repProps.setProperty(IPSJdbcDbmsDefConstants.DB_SERVER_PROPERTY, strServer);
               repProps.setProperty(IPSJdbcDbmsDefConstants.DB_DRIVER_CLASS_NAME_PROPERTY,
                       strDriverClass);
               try (FileInputStream fs = new FileInputStream(strRepPropFile)) {
                  repProps.store(fs, null);
               }
            }
         }
      }
      catch (Exception e)
      {
         RxLogger.logInfo("exception : " + e.getMessage());
      }
   }
   
   /*************************************************************************
    * Properties Accessors and Mutators
    *************************************************************************/
   
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
      m_repositoryPropertyFile = repositoryPropertyFile;
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
      m_serverPropertyFile = serverPropertyFile;
   }
   
   /*************************************************************************
    * Properties
    *************************************************************************/
   
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
}
