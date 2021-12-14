/******************************************************************************
 *
 * [ RxServerPropertiesModel.java ]
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
import com.percussion.installer.RxVariables;
import com.percussion.installer.action.RxLogger;
import com.percussion.installer.action.RxUpdateUpgradeFlag;
import com.percussion.installer.rule.RxRhythmyxServiceInstallRule;
import com.percussion.util.PSOsTool;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;


/**
 * This model represents a panel/console that asks for the Rhythmyx Service Name
 * and Description along with the Server port.  It also allows the user to 
 * decide if additional ports need to be configured.
 */
public class RxServerPropertiesModel extends RxIAModel
{
   /**
    * Constructs an {@link RxServerPropertiesModel} object.
    *  
    * @param locator the proxy locator which will retrieve the proxy used to
    * interact with the InstallAnywhere runtime platform.
    */
   public RxServerPropertiesModel(IPSProxyLocator locator)
   {
      super(locator);
      setPersistProperties(new String[]{"bindPort", RHYTHMYX_SERVER_TYPE});
      setHideOnUpgrade(false);
      setPropertyFileName("rxconfig/Server/server.properties");
      setResourcePropertyFile("$RX_DIR$/config/server.properties");
   }
   
   @Override
   public boolean queryEnter()
   {
      if (!super.queryEnter())
         return false;
      if (!PSOsTool.isWindowsPlatform() &&
            RxUpdateUpgradeFlag.checkUpgradeInstall())
      {
         return false;
      }

      return evaluateTrueCondition();
   }
   
   @Override
   public boolean queryExit()
   {
      if (!super.queryExit())
         return(false);
      
      String strSvcName = getRhythmyxSvcName();
      if ((strSvcName == null) || (strSvcName.length() == 0))
      {
         validationError(RxInstallerProperties.getResources().
               getString("rhythmyxSvcNameErr"),
               null,
               null);
         return(false);
      }
      
      String strSvcDesc = getRhythmyxSvcDesc();
      if ((strSvcDesc == null) || (strSvcDesc.length() == 0))
      {
         validationError(RxInstallerProperties.getResources().
               getString("rhythmyxSvcDescErr"),
               null,
               null);
         return(false);
      }
      
      RxUniquePortsPopupDialog rxPortsMsg = new RxUniquePortsPopupDialog();
      rxPortsMsg.manageUniquePortsMessage(this);
      
      if (!validatePort(getBindPort()))
         return false;
      
      setRhythmyxSvcName(strSvcName);
      setRhythmyxSvcDesc(strSvcDesc);
      
      return true;
   }
   
   @Override
   public String getTitle()
   {
      return "Server Properties";
   }
   
   @Override
   protected void initModel()
   {
      setBindPort(getBindPort());
   }
   
   /*************************************************************************
    * Properties Accessors and Mutators
    *************************************************************************/
   
   /**
    * Returns the Rhythmyx Service Name
    * @return the Rhythmyx Service Name, never <code>null</code> or empty
    */
   public static String getRhythmyxSvcName()
   {
      return ms_rhythmyxServiceName;
   }
   
   /**
    * Sets the Rhythmyx Service Name
    * @param name the Rhythmyx Service Name
    */
   public void setRhythmyxSvcName(String name)
   {
      ms_rhythmyxServiceName = name;
      
      if (name != null && name.trim().length() > 0)
         setInstallValue(RxVariables.RX_SERVICE_NAME, name);
      
      propertyChanged(InstallUtil.RHYTHMYX_SVC_NAME);
   }
   
   /**
    * Returns the Rhythmyx Service Description
    * @return the Rhythmyx Service Description, never <code>null</code> or empty
    */
   public static String getRhythmyxSvcDesc()
   {
      return ms_rhythmyxServiceDesc;
   }
   
   /**
    * Sets the Rhythmyx Service Description
    * @param name the Rhythmyx Service Description
    */
   public void setRhythmyxSvcDesc(String name)
   {
      ms_rhythmyxServiceDesc = name;
      
      if (name != null && name.trim().length() > 0)
         setInstallValue(RxVariables.RX_SERVICE_DESC, name);
      
      propertyChanged(InstallUtil.RHYTHMYX_SVC_DESC);   
   }
      
   /**
    * Returns Rhythmyx Server Port.
    * @return the Rhythmyx Server Port, never <code>null</code> or empty
    */
   public String getBindPort()
   {
      return ms_rxServerPort;
   }
   
   /**
    * Sets Rhythmyx Server Port.
    * @param port the Rhythmyx Server Port
    */
   public void setBindPort(String port)
   {
      ms_rxServerPort = port;
      
      propertyChanged("bindPort");
   }
   
   /**
    * Returns Rhythmyx Server Port.
    * @return the Rhythmyx Server Port, never <code>null</code> or empty
    */
   public String getRxServerPort()
   {
      return getBindPort();
   }
   
   /**
    * Provides static access to the server port.
    * 
    * @return the Rhythmyx server port.
    */
   public static String fetchServerPort()
   {
      return ms_rxServerPort;
   }
   
   /*************************************************************************
    * Private functions
    *************************************************************************/
   
   /**
    * Returns <code>false</code> only if the Rhythmyx Service Name exists
    * in the installation.properties file and a service is installed with this
    * service name
    * @return <code>false<code> only if the Rhythmyx Service Name exists
    * in the installation.properties file and a service is installed with this
    * service name, <code>true</code> otherwise.
    */
   private boolean evaluateTrueCondition()
   {
      String installDir = getRootDir();
           
      if ((installDir == null) || (installDir.trim().length() == 0))
         return true;
      
      if (!installDir.endsWith(File.separator))
         installDir += File.separator;
      
      String strPropFile = installDir +
         "/rxconfig/Installer/installation.properties";
      File propFile = new File(strPropFile);
      if (!propFile.exists())
      {
         RxLogger.logInfo("file does not exist : " + strPropFile);
         return true;
      }
      try {
         Properties prop = new Properties();
         try (FileInputStream fs = new FileInputStream(propFile)) {
            prop.load(fs);
            String svcName = prop.getProperty(InstallUtil.RHYTHMYX_SVC_NAME);
            if ((svcName == null) || (svcName.trim().length() == 0))
               return true;

            //set the Service Name and Description
            setRhythmyxSvcName(svcName);
            String svcDesc = prop.getProperty(InstallUtil.RHYTHMYX_SVC_DESC);
            if (!((svcDesc == null) || (svcDesc.trim().length() == 0)))
               setRhythmyxSvcDesc(svcDesc);

         }
      }
      catch (Exception e)
      {
         RxLogger.logError(
               "RxServerPropertiesModel#evaluateTrueCondition : " +
               e.getMessage());
         RxLogger.logInfo(e);
         return true;
      }
      return true;
   }
      
   /**
    * Returns the Rhythmyx ServerType
    * @return the Rhythmyx Service Name, never <code>null</code> or empty
    */
   public static String getServerType()
   {
      return ms_serverType;
   }
   
   /*************************************************************************
    * Properties
    *************************************************************************/
   
   /**
    * key for storing the Rhythmyx Server Type in server.properties file
    */
   public static final String RHYTHMYX_SERVER_TYPE = "ServerType";
   
   /**
    * stores the Rhythmyx service name, never <code>null</code> or empty.
    */
   private static String ms_rhythmyxServiceName = "Percussion Service";
   
   /**
    * stores the Rhythmyx service name, never <code>null</code> or empty.
    */
   private static String ms_rhythmyxServiceDesc = "Percussion Service";
   
   /**
    * Rhythmyx Server Port, never <code>null</code> or empty.
    * Defaults to 9992.
    */
   private static String ms_rxServerPort = "9992";
   
   /**
    * stores the Rhythmyx server type, never <code>null</code> or empty
    */
   private static String ms_serverType = "System Master";
}
