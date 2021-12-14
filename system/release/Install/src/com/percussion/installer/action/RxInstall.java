/******************************************************************************
 *
 * [ RxInstall.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.action;



import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.launch.AntMain;

import com.percussion.install.InstallUtil;
import com.percussion.installanywhere.RxIAAction;
import com.percussion.installanywhere.RxIAUtils;
import com.percussion.installer.RxVariables;
import com.percussion.installer.model.PSDeliveryTierFeatureModel;
import com.percussion.installer.model.RxComponent;
import com.percussion.installer.model.RxDevToolsFeatureModel;
import com.percussion.installer.model.RxProductModel;
import com.percussion.util.IOTools;
import com.percussion.util.PSOsTool;
import com.zerog.ia.api.pub.IASys;


/**
 * This is the main processing action of the Rhythmyx installation process.
 * It gathers the necessary properties collected from the installer, then
 * launches the Ant install script with these properties specified.  The class
 * is specified as a build listener so that it can update the progress bar
 * based on feedback from the build process.
 */
public class RxInstall extends RxIAAction implements BuildListener
{
   @Override 
   public void execute()
   {
      setAdditionalProperties();
      
      buildCatLists();

      //Set on silent mode
      if(isSilentInstall())
      {
         setVariablesForSilentMode(INSTALL_CONF_LIST);
         setVariablesForSilentMode(SERVER_PROP_LIST);
         setVariablesForSilentMode(SELECT_PROP_LIST);
         setVariablesForSilentMode(Collections.singletonList(MYSQL_DRIVER_LOCATION_PROP));
         setVariablesForSilentMode(Collections.singletonList(DELIVERY_TARGET));
         setVariablesForSilentMode(Collections.singletonList(NIGHTLY_INSTALL));
         setVariablesForSilentMode(Collections.singletonList(DTS_INSTALL));
         setVariablesForSilentMode(Collections.singletonList(LICENCE_CODE));
         
         String mysqlDriverLoc = ms_propertiesMap.get(MYSQL_DRIVER_LOCATION_PROP);
         if (StringUtils.isNotBlank(mysqlDriverLoc))
         {
            InstallUtil.addJarFileUrl(mysqlDriverLoc);
         }
      }
      else
      {
         //Reg Vars
         registerVariable(RxVariables.INSTALL_DIR, 
               getInstallValue(RxVariables.INSTALL_DIR), 
               VarCategoryType.INSTALLATION_CONFIGURATION);
         setCatListVariables(INSTALL_CONF_LIST, 
               VarCategoryType.INSTALLATION_CONFIGURATION);
         setCatListVariables(SERVER_PROP_LIST, VarCategoryType.SERVER_PROPERTIES);
         setCatListVariables(SELECT_PROP_LIST, VarCategoryType.PRODUCT_SELECTION);
         
         String mysqlDriverLoc = ms_propertiesMap.get(MYSQL_DRIVER_LOCATION_PROP);
         if (StringUtils.isNotBlank(mysqlDriverLoc))
         {
            registerVariable(MYSQL_DRIVER_LOCATION_PROP, mysqlDriverLoc, VarCategoryType.REPOSITORY_CONFIGURATION);
         }
      }
      
      String tempDir = getInstallValue(RxVariables.INSTALLER_TEMP_DIR);
      String buildFile = tempDir + "/system/installResources/install.xml";
      String rootDir = getInstallValue(RxVariables.INSTALL_DIR);
      String logFile = rootDir + "/rxconfig/Installer/ant.log";
      
      String[] args = new String[6];
      args[0] = "-buildfile";
      args[1] = buildFile;
      args[2] = "-listener";
      args[3] = "com.percussion.installer.action.RxInstall";
      args[4] = "-logfile";
      args[5] = logFile;
      
      String strArgs = 
         "-buildfile " + buildFile +
         " -listener com.percussion.installer.action.RxInstall" +
         " -logfile " + logFile;
      
      Properties userProps = new Properties();
      userProps.put("RUN_LANGUAGE_TOOL", "true");
      
      String strProps = "";
      Set<String> props = ms_propertiesMap.keySet();
      Iterator<String> propsIter = props.iterator();
      while (propsIter.hasNext())
      {
         String prop = propsIter.next();
         String val = ms_propertiesMap.get(prop);
         userProps.put(prop, val);
         strProps += ' ' + prop + '=' + val;
      }
      
      //Launch the install process
      RxLogger.logInfo("Install args : " + strArgs);
      
      if (RxUpdateUpgradeFlag.checkUpgradeInstall())
      {
         //Mask admin user password when we print out to log on upgrade
         String adminUserPass = ms_propertiesMap.get(SERVER_ADMIN_PASSWORD);
         if (adminUserPass != null)
         {
            strProps = StringUtils.replace(strProps, adminUserPass, StringUtils.repeat("*", adminUserPass.length()));
         }
      }
      
      RxLogger.logInfo("Install properties : " + strProps);
      
      launchInstall(args, userProps);
      
      //Cleanup the temp directory (Windows only)
      if (PSOsTool.isWindowsPlatform())
      {
         RxLogger.logInfo("Cleaning up temporary directory for Windows "
               + "install");
         IOTools.deleteFile(new File(tempDir));
      }
   }

   /**
    * See {@link BuildListener#buildStarted(org.apache.tools.ant.BuildEvent)}
    * for details.
    */
   @SuppressWarnings("unused")
   public void buildStarted(BuildEvent arg0)
   {
      IASys.out.println("");
      IASys.out.println("Installing " + getInstallValue(
            RxVariables.PRODUCT_NAME) + "...");
   }

   /**
    * See {@link BuildListener#buildFinished(org.apache.tools.ant.BuildEvent)}
    * for details.
    */
   public void buildFinished(BuildEvent arg0)
   {
      Throwable t = arg0.getException();
      if (t != null)
         setInstallValue(RxVariables.RX_INSTALL_ERRORS_OCCURRED, "true");
      else
         setInstallValue(RxVariables.RX_INSTALL_ERRORS_OCCURRED, "false");
   }

   /**
    * See {@link BuildListener#targetStarted(org.apache.tools.ant.BuildEvent)}
    * for details.
    */
   public void targetStarted(BuildEvent arg0)
   {
      Project proj = arg0.getProject();
      String projName = proj.getName();
           
      if (shouldTrackProject(projName))
         resetProgressBar(proj);
   }

   /**
    * See {@link BuildListener#targetFinished(org.apache.tools.ant.BuildEvent)}
    * for details.
    */
   @SuppressWarnings("unused")
   public void targetFinished(BuildEvent arg0)
   {
   }

   /**
    * See {@link BuildListener#taskStarted(org.apache.tools.ant.BuildEvent)}
    * for details.
    */
   @SuppressWarnings("unused")
   public void taskStarted(BuildEvent arg0)
   {      
   }

   /**
    * See {@link BuildListener#taskFinished(org.apache.tools.ant.BuildEvent)}
    * for details.
    */
   public void taskFinished(BuildEvent arg0)
   {
      if (shouldTrackProject(arg0.getProject().getName()))
      {
         ms_currentTask++;
         double percent = (double) ms_currentTask/(double) ms_currentTasks;
      
         int multiplier;
         if (ms_selectedProds > 0)
            multiplier = 100/ms_selectedProds;
         else
            multiplier = 100;
         
         percent *= multiplier;
         if (percent <= multiplier)
         {
            ms_currentPercent = percent;
            setProgressPercentage((float) (ms_overallPercent +
                  ms_currentPercent));
         }
      }
   }

   /**
    * See {@link BuildListener#messageLogged(org.apache.tools.ant.BuildEvent)}
    * for details.
    */
   public void messageLogged(BuildEvent arg0)
   {
      setProgressStatusText(RxIAUtils.truncateMsg(arg0.getMessage()));
   }
   
   @Override
   public long getEstTimeToInstall()
   {
      return 3000;
   }
   
    /**
    * This method is used to set all additional user input properties required
    * by the installation.  By default it sets the installation type and
    * directory.
    */
   protected void setAdditionalProperties()
   {
      // if doing a repair, we need to act like an upgrade
      if (YES_VAL.equalsIgnoreCase(getInstallValue(RxVariables.RX_CM1_REPAIR)))
      {
            RxUpdateUpgradeFlag.upgradeInstall();
            RxLogger.logInfo("Performing repair, setting install type to upgrade");
      }
      
      ms_propertiesMap.put(INSTALL_TYPE_PROP,
            RxUpdateUpgradeFlag.checkNewInstall() ? "new" : "upgrade");
      ms_propertiesMap.put(INSTALL_DIR_PROP,
            getInstallValue(RxVariables.INSTALL_DIR));
   }
   
   /**
    * Sets the feature properties in {@link #ms_propertiesMap} to the
    * appropriate value in order to indicate if the associated devtools
    * features have been selected for install.
    * 
    * @param model the <code>RxDevToolsFeatureModel</code> object which stores
    * the devtools feature selection information.
    */
   protected void setInstallDevToolsFeatures(RxDevToolsFeatureModel model)
   {
      RxComponent comp = model.getComponent(
            RxDevToolsFeatureModel.DEVELOPER_TOOLS_NAME);
      ms_propertiesMap.put(DEVTOOLS_CORE_PROP,
            comp != null && comp.isSelected() ? YES_VAL : NO_VAL);
     
      comp = model.getComponent(RxDevToolsFeatureModel.DOCUMENTATION_NAME);
      ms_propertiesMap.put(DEVTOOLS_DOC_PROP,
            comp != null && comp.isSelected() ? YES_VAL : NO_VAL);
   }

   /***
    * Sets the feature properties in {@link #ms_propertiesMap} to
    * the appropriate value in order to indicate which DTS features
    * have been selected for install.
    * 
    * @param model The <code>PSDeliveryTierFeatureModel</code> containing the current feature selection.
    */
   protected void setInstallDTSFeatures(PSDeliveryTierFeatureModel model){
      RxComponent comp = model.getComponent(
            PSDeliveryTierFeatureModel.DTS_FEATURE_NAME);
      ms_propertiesMap.put(DTS_PROP,
            comp != null && comp.isSelected() ? YES_VAL : NO_VAL);  
   }
   
   /**
    * Stores the project name, resets the current task information, updates the
    * overall percent completed.
    * 
    * @param proj the {@link Project} representing the Ant project, assumed
    * not <code>null</code>.
    */
   private void resetProgressBar(Project proj)
   {
      String projectName = proj.getName();
      if (!projectName.equals(ms_currentProject))
      {
         ms_currentProject = projectName;
         ms_currentTask = 0;
         ms_currentTasks = 0;
         ms_overallPercent += ms_currentPercent;
         
         ConcurrentHashMap targets = proj.getTargets();
         Set targetKeys = targets.keySet();
         Iterator targetKeyIter = targetKeys.iterator();
         while (targetKeyIter.hasNext())
         {
            Target tgt = (Target) targets.get(targetKeyIter.next());
            ms_currentTasks += tgt.getTasks().length;
         }
         
         System.out.println("Installing " + ms_currentProject + "...");
      }
   }
   
   /**
    * Launches Ant in process.
    * 
    * @param args the command line ant arguments, assumed not <code>null</code>.
    * @param props addition user properties to be supplied to Ant, assumed
    * not <code>null</code>.
    */
   private void launchInstall(String[] args, Properties props)
   {
      try
      {
         Class mainClass = Class.forName("com.percussion.ant.install.PSMain");
         AntMain main = (AntMain) mainClass.newInstance();
         main.startAnt(args, props, null);
      }
      catch(ClassNotFoundException e)
      {
         RxLogger.logError("RxInstall#launchInstall : " + e.getMessage());
      }
      catch(InstantiationException e)
      {
         RxLogger.logError("RxInstall#launchInstall : " + e.getMessage());
      }
      catch(IllegalAccessException e)
      {
         RxLogger.logError("RxInstall#launchInstall : " + e.getMessage());
      }
   }
   
   /**
    * Used to determine if the progress of a project should be tracked.  Queries
    * the set of products stored in {@link RxProductModel} in order to make the
    * determination.
    * 
    * @param projName name of the Ant project, assumed not <code>null</code>.
    * 
    * @return <code>true</code> if the project's progress should be tracked,
    * <code>false</code> otherwise.
    */
   private boolean shouldTrackProject(String projName)
   {
      return RxProductModel.getProductsMap().keySet().contains(projName);
   }
   
   /**
    * Method to register vars.
    * 
    * @param list - list of vars to process
    * @param catType - cat type to set
    */
   private void setCatListVariables(List<String> list, VarCategoryType catType)
   {
      for(String prop : list)
      {
         if(ms_propertiesMap.containsKey(prop))
         {
            registerVariable(prop, ms_propertiesMap.get(prop), catType);
         }
      }
   }
   
   /**
    * Method to set vars that are passed in when doing an install in silent mode.
    * Should only be called on a silent install.
    * 
    * @param list - List to process
    */
   private void setVariablesForSilentMode(List<String> list)
   {
      for (String prop : list)
      {
         String value = getInstallValue(prop);
         
         if (StringUtils.isNotBlank(value))
         {
            ms_propertiesMap.put(prop, value);
         }
      }
   }
   
   /**
    * Put properties in their correct Cat List
    */
   private void buildCatLists()
   {
      INSTALL_CONF_LIST.add(INSTALL_TYPE_PROP);
      
      SELECT_PROP_LIST.add(TRIM_RXPUBDOCS_PROP);
      
      SERVER_PROP_LIST.add(SERVICE_NAME_PROP);
      SERVER_PROP_LIST.add(SERVICE_DESC_PROP);
      SERVER_PROP_LIST.add(PORT_PROP);
      SERVER_PROP_LIST.add(PORT_NAMING_PROP);
      SERVER_PROP_LIST.add(PORT_RMI_PROP);
      SERVER_PROP_LIST.add(PORT_JRMP_PROP);
      SERVER_PROP_LIST.add(PORT_POOLED_PROP);
      SERVER_PROP_LIST.add(PORT_JMS_PROP);
      SERVER_PROP_LIST.add(PORT_AJP_PROP);
   }
   
   /**
    * Map containing the Ant installation property name/value pairs.
    */
   protected static Map<String,String> ms_propertiesMap = 
      new HashMap<>();
   
   /**
    * The name of the current Ant install project.
    */
   private static String ms_currentProject = "";
   
   /**
    * The current Ant task number used to track install progress.
    */
   private static int ms_currentTask = 0;
   
   /**
    * The estimated number of tasks for the current Ant project.
    */
   private static int ms_currentTasks = 0;
   
   /**
    * The number of Rhythmyx products which have been selected for install.
    */
   protected static int ms_selectedProds = 0;
   
   /**
    * The percent complete for the current Ant project.
    */
   private static double ms_currentPercent = 0.0;
   
   /**
    * The percent complete for the overall Ant install process.
    */
   private static double ms_overallPercent = 0.0;
            
   /**
    * Installation type property name.
    */
   protected static final String INSTALL_TYPE_PROP = "install.type";
   
   /**
    * Installation directory property name.
    */
   protected static final String INSTALL_DIR_PROP = "install.dir";
   
   /**
    * The repository will be installed based on the value of this property.
    * {@link #YES_VAL} to install, {@link #NO_VAL} to skip.
    */
   public static final String REPOSITORY_PROP = "repository";
   
   /**
    * The server will be installed based on the value of this property.
    * {@link #YES_VAL} to install, {@link #NO_VAL} to skip.
    */
   public static final String SERVER_PROP = "server";
   
   /**
    * The server core will be installed based on the value of this property.
    * {@link #YES_VAL} to install, {@link #NO_VAL} to skip.
    */
   protected static final String SERVER_CORE_PROP = "server.core";
   
   /**
    * The server documentation will be installed based on the value of this
    * property.  {@link #YES_VAL} to install, {@link #NO_VAL} to skip.
    */
   protected static final String SERVER_DOC_PROP = "server.doc";
   
   /**
    * The server remote development tools installer will be installed based on
    * the value of this property.  {@link #YES_VAL} to install, {@link #NO_VAL}
    * to skip.
    */
   protected static final String SERVER_DEVTOOLS_PROP = "server.devtools";
   
   /**
    * FastForward will be installed based on the value of this property.
    * {@link #YES_VAL} to install, {@link #NO_VAL} to skip.
    */
   public static final String FF_PROP = "fastforward";
   
   /**
    * The fastforward applications will be installed based on the value of this
    * property.  {@link #YES_VAL} to install, {@link #NO_VAL} to skip.
    */
   protected static final String FF_APPS_PROP = "fastforward.apps";
   
   /**
    * The fastforward sample content will be installed based on the value of
    * this property.  {@link #YES_VAL} to install, {@link #NO_VAL} to skip.
    */
   protected static final String FF_CONTENT_PROP = "fastforward.content";
   
   /***
    * The Delivery Tier Services will be installed hen this property is yes.
    */
   public static final String DTS_PROP = "install.dts";
   /**
    * The development tools will be installed based on the value of this
    * property.  {@link #YES_VAL} to install, {@link #NO_VAL} to skip.
    */
   public static final String DEVTOOLS_PROP = "devtools";
   
   /**
    * The development tools developer tools will be installed based on the value
    * of this property.  {@link #YES_VAL} to install, {@link #NO_VAL} to skip.
    */
   public static final String DEVTOOLS_CORE_PROP = "devtools.core";
   
   /**
    * The development tools documentation will be installed based on the value
    * of this property.  {@link #YES_VAL} to install, {@link #NO_VAL} to skip.
    */
   protected static final String DEVTOOLS_DOC_PROP = "devtools.doc";
   
   /**
    * The Rhythmyx server port will be set to the value of this property.
    */
   protected static final String PORT_PROP = "port";
   
   /**
    * The Rhythmyx server naming service port will be set to the value of this
    * property.
    */
   protected static final String PORT_NAMING_PROP = "port.naming";
   
   /**
    * The Rhythmyx server naming service rmi port will be set to the value of
    * this property.
    */
   protected static final String PORT_RMI_PROP = "port.rmi";
   
   /**
    * The Rhythmyx server invoker jrmp service port will be set to the value of
    * this property.
    */
   protected static final String PORT_JRMP_PROP = "port.jrmp";
   
   /**
    * The Rhythmyx server invoker pooled service port will be set to the value
    * of this property.
    */
   protected static final String PORT_POOLED_PROP = "port.pooled";
   
   /**
    * The Rhythmyx server uil2 service port will be set to the value of this
    * property.
    */
   protected static final String PORT_JMS_PROP = "port.jms";
   
   /**
    * The Rhythmyx server ajp13 service port will be set to the value of this
    * property.
    */
   protected static final String PORT_AJP_PROP = "port.ajp";
   
   /**
    * The Rhythmyx server type will be set to the value of this property.
    */
   public static final String SERVER_TYPE_PROP = "server.type";
   
   /**
    * The server admin user will be set to the value of this property.
    */
   public static final String SERVER_ADMIN_USER = "server.admin.user";
   
   /**
    * The server admin password will be set to the value of this property.
    */
   public static final String SERVER_ADMIN_PASSWORD = "server.admin.password";
   
   /**
    * The RxPubDocs table will be trimmed based on the value of this property:
    * {@link #YES_VAL}, {@link #NO_VAL}.
    * 
    */
   protected static final String TRIM_RXPUBDOCS_PROP = "trimRxPubDocs";
   
   /**
    * The RxPubDocs table will be trimmed to the value of this property.
    */
   protected static final String RXPUBDOCS_TRIM_DATE_PROP = "rxPubDocsTrimDate";
   
   /**
    * The Rhythmyx service name will be set to the value of this property.
    */
   protected static final String SERVICE_NAME_PROP = "service.name";
   
   /**
    * The Rhythmyx service description will be set to the value of this
    * property.
    */
   protected static final String SERVICE_DESC_PROP = "service.description";
   
   /**
    * MySQL driver location will be set to this property for MySQL installs
    */
   protected static final String MYSQL_DRIVER_LOCATION_PROP = "mysql.driver.location";
   
   /**
    * qa/dev delivery-servers.xml will be installed based on this property 
    */
   protected static final String DELIVERY_TARGET = "delivery.target";
   
   /**
    * checks if it is the nightly install
    */
   public static final String NIGHTLY_INSTALL = "nightly.install";
   
   /**
    * checks if it is the nightly install
    */
   public static final String DTS_INSTALL = "install.dts";
   
   /**
    * checks if dts server is staging or production
    */
   public static final String DTS_SEVER_TYPE_PROP = "dts.servertype";
   
   /**
    * installs and activates the server with the given license code
    */
   public static final String LICENCE_CODE = "license.code";
   
   /**
    * Yes property value.
    */
   public static final String YES_VAL = "yes";
   
   /**
    * No property value.
    */
   protected static final String NO_VAL = "no";
   
   /**
    * List of Install Configuration properties
    */
   protected static final List<String> INSTALL_CONF_LIST = 
      new ArrayList<>();
   
   
   /**
    * List of Product Selection properties
    */
   protected static final List<String> SELECT_PROP_LIST = 
      new ArrayList<>();
   
   /**
    * List of Server Properties
    */
   protected List<String> SERVER_PROP_LIST = 
       new ArrayList<>();
   
   
   
   
}
