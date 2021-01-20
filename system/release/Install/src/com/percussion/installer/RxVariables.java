/******************************************************************************
 *
 * [ RxVariables.java ]
 * 
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.installer;


/**
 * This class contains constants for publicly available InstallAnywhere variable
 * names.  This includes standard and Rhythmyx InstallAnywhere variables.
 * 
 * @author peterfrontiero
 */
public class RxVariables
{
   /******************************************
    * Standard InstallAnywhere variable names.
    ******************************************/
   
   /**
    * The installation directory variable.
    */
   public static final String INSTALL_DIR = "USER_INSTALL_DIR";
   
   /**
    * The programs directory variable.
    */
   public static final String PROGRAMS_DIR = "PROGRAMS_DIR";
   
   /**
    * The path to a temp directory for use by an installer. This will be deleted
    * when its use is completed, assuming no items are in use. 
    */
   public static final String INSTALLER_TEMP_DIR = "INSTALLER_TEMP_DIR";
   
   /**
    * The installation product name variable.
    */
   public static final String PRODUCT_NAME = "PRODUCT_NAME"; 
   
   /**
    * The installer ui mode variable.  Valid values are: SILENT, CONSOLE, GUI,
    * SWING, AWT.
    */
   public static final String INSTALLER_UI = "INSTALLER_UI";
     
   /******************************************
    * Rhythmyx InstallAnywhere variable names.
    * ****************************************/
   
   /**
    * This variable is a comma-separated list of all install products chosen
    * for the installation. 
    */
   public static final String RX_INSTALL_PRODUCTS = "RX_INSTALL_PRODUCTS";
   
   /**
    * This variable is a comma-separated list of all server features chosen
    * for the installation. 
    */
   public static final String RX_SERVER_FEATURES = "RX_SERVER_FEATURES";
   
   /**
    * This variable is a comma-separated list of all development tools features
    * chosen for the installation. 
    */
   public static final String RX_DEVTOOLS_FEATURES = "RX_DEVTOOLS_FEATURES";
   
   /**
    * This variable is a comma-separated list of all fastforward features chosen
    * for the installation. 
    */
   public static final String RX_FASTFORWARD_FEATURES =
      "RX_FASTFORWARD_FEATURES";
   
   /**
    * This variable is a comma-seperated list of all DTS features
    * for this installation. 
    */
   public static final String PS_DTS_FEATURES = "PS_DTS_FEATURES";
   
   /**
    * This variable is a flag indicating whether or not the user has chosen to
    * configure the additional ports used by Rhythmyx.  The value will be
    * "true" or "false."
    */
   public static final String RX_CUSTOMIZE_PORTS = "RX_CUSTOMIZE_PORTS";
   
   /**
    * This variable is a flag indicating whether or not .cp files have been
    * found in the Rhythmyx root directory.  The value will be "true" or
    * "false."
    */
   public static final String RX_CP_FILES_FOUND = "RX_CP_FILES_FOUND";
   
   /**
    * In the event that .cp files are found in the Rhythmyx root directory, this
    * variable will store an appropriate warning message for the user. 
    */
   public static final String RX_CP_FILES_MSG = "RX_CP_FILES_MSG";
   
   /**
    * This variable is a flag indicating whether or not an existing Rhythmyx
    * repository does not support unicode.  The value will be "true" or "false."
    */
   public static final String RX_NON_UNICODE_DB = "RX_NON_UNICODE_DB";
   
   /**
    * In the event that an existing Rhythmyx repository does not support
    * unicode, this variable will store an appropriate warning message for the
    * user.
    */
   public static final String RX_NON_UNICODE_MSG = "RX_NON_UNICODE_MSG";
   
   /**
    * This variable is a flag indicating whether or not errors occurred during
    * the Ant install process.  The value will be "true" or "false."
    */
   public static final String RX_INSTALL_ERRORS_OCCURRED =
      "RX_INSTALL_ERRORS_OCCURRED";
   
   /**
    * This variable stores the name of the Rhythmyx Service.
    */
   public static final String RX_SERVICE_NAME = "RX_SERVICE_NAME";
   
   /**
    * This variable stores the description of the Rhythmyx Service.
    */
   public static final String RX_SERVICE_DESC = "RX_SERVICE_DESC";
   
   /**
    * This variable stores the name of the Rhythmyx Publisher Service.
    */
   public static final String RX_PUB_SERVICE_NAME = "RX_PUB_SERVICE_NAME";
   
   /**
    * This variable stores the description of the Rhythmyx Publisher Service.
    */
   public static final String RX_PUB_SERVICE_DESC = "RX_PUB_SERVICE_DESC";
   
   /**
    * This variable is a flag indicating if the current system contains enough
    * temporary disk space to install Rhythmyx.  This value will be "true" or
    * "false."
    */
   public static final String RX_TEMP_SPACE_OK = "RX_TEMP_SPACE_OK";
   
   /**
    * This variable stores the amount of temporary disk space (Mb) required to
    * install Rhythmyx.
    */
   public static final String RX_REQUIRED_TEMP_SPACE = "RX_REQUIRED_TEMP_SPACE";
   
   /**
    * This variable is set for CM1 SDK installs.
    */
   public static final String CM1_SDK_INSTALL = "CM1_SDK_INSTALL";
   
   /**
    * This variable is set for Percussion DTS installs
    */
   public static final String DTS_INSTALL = "DTS_INSTALL";
   
   /**
    * This variable indicates that the Tomcat DTS is running
    */
   public static final String RX_TOMCAT_RUNNING = "RX_TOMCAT_RUNNING";
   
   /**
    * This variable indicates that CM1 is running
    */
   public static final String RX_CM1_RUNNING = "RX_CM1_RUNNING";
   
   /**
    * This variable indicates that CM1 is being repaired
    */
   public static final String RX_CM1_REPAIR = "RX_CM1_REPAIR";
   
   /**
    * This variable indicates if there is an error in the status of the database
    */
   public static final String CHECK_DB_ERROR = "CHECK_DB_ERROR";
   
   /**
    * This variable indicates if there is an error in the status of the database
    */
   public static final String CHECK_DB_MESSAGE = "CHECK_DB_MESSAGE";
   
   /******************************************
    * CM1 InstallAnywhere variable names.
    * ****************************************/
   
   /**
    * This variable indicates which DTS server this is.
    * Production or Staging.
    * Default value is Production.
    */
   public static final String DTS_SERVER_TYPE = "production";
}
