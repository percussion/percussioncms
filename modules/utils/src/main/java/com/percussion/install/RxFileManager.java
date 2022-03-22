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

package com.percussion.install;

//java

import com.percussion.util.PSOsTool;
import com.percussion.utils.container.PSContainerUtilsFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * RxFileManager is a class that manages the files in a Rhythmyx installation.
 */
public class RxFileManager
{
    /**
     * Holds a full directory name - before we do shortpath name on it. 
     */ 
    private static String ms_longRootDir = "";
    
     /**
      *  The absolute path of the root directory.  If it is empty then the
      *  paths returned will be relative.
      */
    private static String m_strRootDir = "";


     /**
      * Stores the location of the platform specific program files directory.
      * For example, "C:\Program Files" for windows.
      */
     private static String ms_programDir = null;

     /**
      * File used to test if DTS is installed. 
      */
     public static final String DTS_APP_CHECK_FILE="Deployment"+ File.separator + "Server"+ 
                                   File.separator + "bin" + File.separator + "tomcat6.exe";
     
     public static final String DTS_APP_CHECK_FILE7="Deployment"+ File.separator + "Server"+ 
           File.separator + "bin" + File.separator + "tomcat7.exe";
     
     /**
     * Configuration directory
     */
     public static final String CONFIG_DIR = "rxconfig";

     /**
     * Installation directory
     */
     public static final String INSTALLER_DIR = "Installer";

     /**
     * Server directory
     */
     public static final String SERVER_DIR = "Server";

     /**
     * ObjectStore directory
     */
     public static final String OBJECTSTORE_DIR = "ObjectStore";

     /**
     * Repository properties file name
     */
     public static final String REPOSITORY_FILE = "rxrepository.properties";

     /**
     * Server properties file name
     */
     public static final String SERVER_PROPERTIES_FILE = "server.properties";
     
     /**
      * Fts properties file name
      */
      public static final String RXFTS_PROPERTIES_FILE = "rxfts.properties";   

     /**
     * Installation properties file name
     */
     public static final String INSTALLATION_PROPERTIES_FILE =
        "installation.properties";
     

     /**
      * System installation file name
      */
     public static final String SYS_INSTALL_FILE = "cm1install.properties";
     
     /***
      * DTS system installation file name
      */
     public static final String DTS_SYS_INSTALL_PROP="dtsinstall.properties";     

     /***
      * SDK system installation file name
      */
     public static final String SDK_SYS_INSTALL_PROP="sdkinstall.properties";     

     
     /**
     * Rhythmyx directory name
     */
     public static final String RHYTHMYX_DIR_NAME = "Percussion";

     /**
      * Default DTS installation directory name
      */
     public static final String DTS_DIR_NAME = RHYTHMYX_DIR_NAME;
     
     /**
      * CM1 SDK directory name.
      */
     public static final String CM1_SDK_DIR_NAME = "PercussionSDK";

     /**
     * Server config file name.
     */
     public static final String SERVER_CONFIG_FILE_NAME = "config.xml";

    /**
     * Properties file for storing the version information of the Rhythmyx
     * being upgraded
     */
     public static final String PREVIOUS_VERSION_PROPS_FILE =
        "PreviousVersion.properties";

     /**
      * Key for storing the major version in "version.properties" file
      */
     public static final String MAJOR_VERSION_PROP_KEY = "majorVersion";

     /**
      * Key for storing the major version in "version.properties" file
      */
     public static final String MINOR_VERSION_PROP_KEY = "minorVersion";

     /**
      * Key for storing the major version in "version.properties" file
      */
     public static final String BUILD_NUMBER_PROP_KEY = "buildNumber";

     /**
      *  6.X deploy directory
      */
     public static final String DEPLOY_DIR = 
        "AppServer" + File.separator + "server" + File.separator +
        "rx" + File.separator + "deploy";

    /**
     *  Jetty CM deploy directory
     */
    public static final String JETTY_DEPLOY_DIR =
            "jetty" + File.separator + "base" + File.separator +
                    "webapps";

     /**
      *  The jar file to read the version properties from.
      */
     public static final String VERSION_JAR_FILE =
        "lib" + File.separator + "rxserver.jar";

    /**
     *  The jar file to read the version properties from in 6.X
     */
    public static final String JETTY_VERSION_JAR_FILE_DIR =
            "rxconfig" + File.separator + "Installer";

    /**
      *  The jar file to read the version properties from in 6.X
      */
     public static final String VERSION_JAR_FILE_6X =
        DEPLOY_DIR + File.separator + "rxapp.ear" + File.separator +
        "rxapp.war" + File.separator + "WEB-INF" + File.separator +
        "lib" + File.separator + "rxserver.jar";
     
     /**
      *  The jar file to read the version properties from in a publisher install.
      */
     public static final String VERSION_JAR_FILE_PUB =
        "AppServer" + File.separator + "webapps" + File.separator +
        "RxServices" + File.separator + "WEB-INF" + File.separator +
        "lib" + File.separator + "rxclient.jar";
     
     /**
      *  The jar file to read the version properties from in a publisher install
      *  of 6.X.
      */
     public static final String VERSION_JAR_FILE_PUB_6X =
        DEPLOY_DIR + File.separator + "RxServices.war" + File.separator +
        "WEB-INF" + File.separator + "lib" + File.separator + "rxclient.jar";
     
     /**
      *  The RxServices war file in a publisher install.
      */
     public static final String RXSERVICES_WAR = 
        "InstallableApps" + File.separator + "AllInOne" + File.separator +
        "RxServices.war";
     
     /**
      *  The rxclient jar file in the RxServices war.
      */
     public static final String RXCLIENT_JAR =
        "WEB-INF/lib/rxclient.jar";
     
     /**
      * Key for storing the path of Rhythmyx installations in the system
      * in the "rxinstaller.properties" file.
      */
     public static final String INSTALL_PROP = "installer_directories";

     /**
      * Key for storing the path of Convera installations in the system
      * in the "rxinstaller.properties" file.
      */
     public static final String CONVERA_SETUP_PATH_PROP = "convera_setup_path";

     /**
      * Key for storing the path of Rhythmyx Publisher standalone installations
      * in the system in the "rxinstaller.properties" file.
      */
     public static final String PUB_INSTALL_PROP = "pub_installer_directories";
     
     /**
      * Key for storing the path of Rhythmyx Content Connector standalone
      * installations in the system in the "rxinstaller.properties" file.
      */
     public static final String ECC_INSTALL_PROP = "ecc_installer_directories";
     
     /**
      *  Version properties file in the rxclient.jar file
      */
     public static final String VERSION_FILE =
        "com/percussion/util/Version.properties";

     /**
      * Constant for string "Other...". This is displayed as the last item in
      * the combo box containing existing Rhythmyx installation directories.
      */
     public static final String OTHER_DIR = "Other...";
     
     /**
      *  Version properties file in the installation directory. 
      * Sometime in 4.5 cycle, installer was creating a lowercase file
      * But was dropped from 5.0 and above.
      * During upgrade cycle this file needs to be deleted.
      */
     public static final String LOWERCASE_VERSION_FILE =
        "version.properties";

     /**
      * The name of the cm war directory.  Used to determine if an installation is CM1
      * prior to war fusion
      */
     public static final String CM_WAR_DIR= "cm.war";
     
     /**
      * The name of the cm webapp directory.  Used to determine if an installation is CM1
      * after war fusion 
      */
     public static final String CM_WEBAPP_DIR="rxapp.ear/rxapp.war/cm";

    /**
     * The name of the cm webapp directory.  Used to determine if an installation is CM1
     * after war fusion
     */
    public static final String JETTY_CM_WEBAPP_DIR="Rhythmyx/cm";
   
     /***
      * The filename of the installation.properties file that
      * contains the location of product installations.  
      */
     private static String systemInstallationPropertiesFile=SYS_INSTALL_FILE;

   
   /**
    * Constructs an RxFileManager with no root directory
    */
   public RxFileManager()
   {
      m_strRootDir = "";
      
   }

  /**
   * Constructs an RxFileManager with a root directory defined
   */
   public RxFileManager(String strRootDir)
   {
      setRootDir(strRootDir);
   }

 /***************************************************************************
  * Utility functions.
  ***************************************************************************/
   
   /**
    * Returns the installation.properties file. 
    * @return a non-null string containing the properties file name
    */
   public static String getSystemInstallationPropertiesFile()
   {
      return systemInstallationPropertiesFile;
   }

   /**
    * Sets the installation.properties file for the instance.
    * @param A valid property file name to use for the property file.
    */
   public static void setSystemInstallationPropertiesFile(String arg)
   {
      if(arg==null || arg.trim().equals(""))
         throw new IllegalArgumentException("Installation Properties File name must not be null or empty.");
      
      systemInstallationPropertiesFile = arg.trim();
   }

   /***
    * Returns the absolute path to the installation properties file. 
    * @param os Specifies the OS that we are returning props for.
    * @return
    */
   public static String getSystemInstallationPropertiesAbsolute(OSEnum os){
      if(os == OSEnum.Windows)
         return getProgramDir() + File.separator + "Percussion" + File.separator + systemInstallationPropertiesFile ;
      else
         return getProgramDir() + File.separator +  systemInstallationPropertiesFile ;         
   }
   
   
  /**
   * Determines if the specified file is a valid Rhythmyx directory. If the
   * specified file contains "rxconfig" directory, then it is a valid Rhythmyx
   * directory.
   *
   * @param dir the file to test whether it is a valid Rhythmyx root
   * directory, may not be <code>null</code> or empty
   *
   * @return <code>true</code> if the specified file is a valid Rhythmyx root
   * directory, <code>false</code> otherwise
   */
   public static boolean isRhythmyxDir(String dir)
   {
      if ((dir == null) || (dir.trim().length() < 1))
         throw new IllegalArgumentException("dir may not be null or empty");

      if (!dir.endsWith(File.separator))
         dir += File.separator;

      dir = dir + CONFIG_DIR;

      File f = new File(dir);
      return (f.exists() && f.isDirectory()) ? true : false;
   }

   /**
    * Determines if the specified file is a valid CM1 directory. If the
    * specified file contains the "cm.war" deploy directory, then it is a valid CM1
    * directory.
    *
    * @param dir the file to test whether it is a valid CM1 root
    * directory, may not be <code>null</code> or empty
    *
    * @return <code>true</code> if the specified file is a valid CM1 root
    * directory, <code>false</code> otherwise
    */
    public static boolean isCM1Dir(String dir)
    {
       if ((dir == null) || (dir.trim().length() < 1))
          throw new IllegalArgumentException("dir may not be null or empty");

       if (!dir.endsWith(File.separator))
          dir += File.separator;

       Path p = Paths.get(dir);
       if(p.toAbsolutePath().equals(
               PSContainerUtilsFactory.getConfigurationContextInstance().getRootDir().toAbsolutePath())){
            return true;
       }

       return false;
    }
   
   /***
    * Determine if the specified directory is a valid DTS installation directory.  If the
    * <Root>/Deployment/Server/bin/tomcat6.exe file exists, then it is assumed to be a valid directory. 
    * 
    * @param dir  The directory to check for a DTS installation
    * @return <code>true</code> if the specified directory is a valid DTS root. <code>false</code> if it is not.
    */
   public static boolean isDTSDir(String dir){

      if ((dir == null) || (dir.trim().length() < 1))
         return false;

      if (!dir.endsWith(File.separator))
         dir += File.separator;

      File f1 = new File(dir, DTS_APP_CHECK_FILE);
      File f2 = new File(dir, DTS_APP_CHECK_FILE7);
      return (f1.exists() || f2.exists());

   }
   /**
    * Returns the location for installer configuration files.
    */
   public String getInstallerConfigLocation()
   {
      String strRet = CONFIG_DIR + File.separator + INSTALLER_DIR;
      if(m_strRootDir.length() > 0)
         strRet = m_strRootDir + File.separator + strRet;
      return  strRet;
   }

   
   /**
    * Returns the location for the server configuration files.
    */
   public String getServerConfigLocation()
   {
      String strRet = CONFIG_DIR + File.separator + SERVER_DIR;
      if(m_strRootDir.length() > 0)
          strRet = m_strRootDir + File.separator + strRet;
      return  strRet;
   }
   
   /**
    * Returns the location of the objectstore files.
    */
   public String getObjectStoreLocation()
   {
      String strRet = OBJECTSTORE_DIR;
      if(m_strRootDir.length() > 0)
          strRet = m_strRootDir + File.separator + strRet;
      return  strRet;
   }

   /**
    * Returns the repository property file name.  No validation for the existence
    * of the file is made.
    */
   public String getRepositoryFile()
   {
      return getInstallerConfigLocation() + File.separator + REPOSITORY_FILE;
   }

   /**
    * Returns the server property file name.  No validation for the existence
    * of the file is made.
    */
   public String getServerPropertiesFile()
   {
      return getServerConfigLocation() + File.separator +
         SERVER_PROPERTIES_FILE;
   }
   
   /**
    * Returns the rxfts.properties file name.  No validation for the existence
    * of the file is made.
    */
   public String getFtsPropertiesFile()
   {
      return getInstallerConfigLocation() + File.separator +
         RXFTS_PROPERTIES_FILE;
   }   

   /**
    * Returns the server config file name.
    *
    */
   public String getServerConfigFile()
   {
      return getServerConfigLocation() + File.separator +
         SERVER_CONFIG_FILE_NAME;
   }

   /**
    * Returns the installation property file name. No validation of the
    * existence of the file is made.
    */
   public String getInstallationPropertyFile()
   {
      return getInstallerConfigLocation() + File.separator +
         INSTALLATION_PROPERTIES_FILE;
   }

   /**
    * For windows, see {@link #getWindowsSystemInstallationFile()}.
    * For Unix, see {@link #getUnixSystemInstallationFile()}.
    * 
    * @return the system installation file name.
    */
   public static String getSystemInstallationFile()
   {
      return PSOsTool.isWindowsPlatform() ?
         getWindowsSystemInstallationFile() : getUnixSystemInstallationFile();
   }

   /**
    * Returns the system installation file name for Unix systems.
    *
    * @return $(USER_HOME)/cm1install.properties
    */
   public static String getUnixSystemInstallationFile()
   {
      setProgramDir(System.getProperty("user.home"));
      return getSystemInstallationPropertiesAbsolute(OSEnum.Linux);
   }

   /**
    * Returns the system installation file name for Windows systems.
    *
    * @return $(PROGRAM_FILES)/Percussion/cm1install.properties
    */
   public static String getWindowsSystemInstallationFile()
   {
      return getSystemInstallationPropertiesAbsolute(OSEnum.Windows);
   }

   /**
    * Sets the platform specific program files directory.
    *
    * @param programDir the platform specific program files directory, may
    * not be <code>null</code> or empty
    */
   public static void setProgramDir(String programDir)
   {
      if ((programDir == null) || (programDir.trim().length() < 1))
         throw new IllegalArgumentException(
            "programDir may not be null or empty");

      programDir = programDir.trim();
      if (programDir.endsWith(File.separator))
         programDir = programDir.substring(0, programDir.length() - 1);
      ms_programDir = programDir;
   }

   /**
    * Returns the platform specific program files directory.
    *
    * @return the platform specific program files directory, never
    * not be <code>null</code> or empty
    *
    * @throws IllegalStateException if program files directory has not been
    * set through <code>setProgramDir()</code> method
    */
   public static String getProgramDir()
   {
      if (ms_programDir == null)
         throw new IllegalStateException("programs directory not set");

      return ms_programDir;
   }

   /**
    *  Returns the default installation directory for Rhythmyx
    */
   public static String getDefaultInstallationDirectory()
   {
      String strHomeDirName = getHomeDrive();
      if(strHomeDirName != null)
      {
          strHomeDirName += RHYTHMYX_DIR_NAME;
      }

      return(strHomeDirName);
   }
   
   /**
    *  Returns the default installation directory for the CM1 SDK.
    */
   public static String getDefaultSDKInstallationDirectory()
   {
      String strHomeDirName = getHomeDrive();
      if(strHomeDirName != null)
      {
          strHomeDirName += CM1_SDK_DIR_NAME;
      }

      return(strHomeDirName);
   }

   /**
    * Returns the home drive
    */
   public static String getHomeDrive()
   {
      File homeDir = new File(System.getProperty("user.home"));
      String strHomeDirName = homeDir.getAbsolutePath();

      //if we find a : only take the drive letter
      if(strHomeDirName.contains(":"))
      {
          strHomeDirName = strHomeDirName.substring(0,2);
      }

      if(!strHomeDirName.endsWith(File.separator))
          strHomeDirName += File.separator;


      return strHomeDirName;
  }



   /***
    * Retrieve the DTS System file properties.  Will first attempt
    * to use dtsinstall.properties and will fail back to cm1install.properties
    * 
    * @param file The File
    * @param os The Operating System
    * @return
    * @throws IOException
    */
   public static Properties getDTSSystemFileProperties(String dts_file,String cm1_file, OSEnum os) throws IOException{
      
      RxFileManager.setSystemInstallationPropertiesFile(dts_file);
      Properties props= RxFileManager.loadProperties(
            RxFileManager.getSystemInstallationPropertiesAbsolute(os)
            );
   
      //If a DTS install.properties file wasn't found look for a CM1 properties file.
      if(props.size()==0){
          RxFileManager.setSystemInstallationPropertiesFile(cm1_file);
          props = RxFileManager.loadProperties(
                RxFileManager.getSystemInstallationPropertiesAbsolute(os)
                );
                          
      }
      return props;
   }
   

   
   /**
    * Loads the specified properties file.
    *
    * @param propFilePath path of the properties file to load, may not be
    * <code>null</code> or empty
    *
    * @return the properties object representing the specified properties file,
    * never <code>null</code>, may be empty if the specified properties file
    * does not exist or does not contain any property
    *
    * @throws IllegalArgumentException if <code>propFilePath</code> is
    * <code>null</code> or empty
    *
    * @throws IOException if an I/O error occurs
    */
   public static Properties loadProperties(String propFilePath)
      throws IOException
   {
          if ((propFilePath == null) || (propFilePath.trim().length() < 1))
             throw new IllegalArgumentException(
                "propFilePath may not be null or empty");

          Properties props = new Properties();

         File propFile = new File(propFilePath.trim());
         if (propFile.exists() && propFile.isFile())
         {
            try( FileInputStream fis = new FileInputStream(propFile)){
                props.load(fis);
            }
        }
      return props;
   }

   /**
    * Saves the specified properties file.
    *
    * @param props the properties object to save, may not be <code>null</code>
    *
    * @param propFilePath path of the file where the properties object should
    * be saved, may not be <code>null</code> or empty
    *
    * @throws IllegalArgumentException if <code>props</code> or
    * <code>propFilePath</code> is <code>null</code> or if
    * <code>propFilePath</code> is empty
    *
    * @throws IOException if an I/O error occurs
    */
   public static void saveProperties(Properties props, String propFilePath)
      throws IOException
   {
      if (props == null)
         throw new IllegalArgumentException("props may not be null");

      if ((propFilePath == null) || (propFilePath.trim().length() < 1))
         throw new IllegalArgumentException(
            "propFilePath may not be null or empty");

         File propFile = new File(propFilePath);
         if (!propFile.exists())
         {
            propFile.getParentFile().mkdirs();
            propFile.createNewFile();
         }

        try(FileOutputStream fos = new FileOutputStream(propFile)){
             props.store(fos, null);
          }
   }
   
   /**
    * Getter for a root dir.
    * @return root dir, never null or empty after directory is chosen.  
    */
   public static String getRootDir()
   {
      return m_strRootDir;
   }

   /**
    * Setter for root dir.
    * @param strRootDir may be <code>null</code>, however since this is a 
    * shared static property only a non null and non empty is taken.
    */
   public static void setRootDir(String strRootDir)
   {
      if (strRootDir != null && strRootDir.trim().length() > 0)
      {
         strRootDir = strRootDir.trim();
         if (strRootDir.endsWith(File.separator))
         {
            int lastCharIndex = strRootDir.length() - 1;
            m_strRootDir = strRootDir.substring(0, lastCharIndex);
         }
         else
         {
            m_strRootDir = strRootDir;
         }
      }
   }
   
   /**
    * Getter for long directory path name, one that user actually chose.
    * @return longRootDir, never <code>null</code>.
    */
   public static String getLongRootDir() 
   {
      return ms_longRootDir;
   }

   /**
    * Setter for long Root directory path before we do short name on it.
    * @param longRootDir never <code>null</code>.
    */
   public static void setLongRootDir(String longRootDir) 
   {
      if (longRootDir == null)
         throw new IllegalArgumentException("longRootDir may not be null");
      
      ms_longRootDir = longRootDir;
   }
}
