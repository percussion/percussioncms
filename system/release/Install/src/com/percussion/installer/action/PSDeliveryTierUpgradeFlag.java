/******************************************************************************
 *
 * [ PSDeliveryTierUpgradeFlag ]
 * 
 * COPYRIGHT (c) 1999 - 2012 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.installer.action;

import com.percussion.install.InstallUtil;
import com.percussion.install.OSEnum;
import com.percussion.install.RxFileManager;
import com.percussion.installanywhere.RxIAAction;
import com.percussion.installer.RxVariables;
import com.percussion.util.PSOsTool;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/***
 * Action that checks to see if this is an upgrade of the DTS
 * 
 * @author nate
 * 
 */
public class PSDeliveryTierUpgradeFlag extends RxIAAction
{
   /**
    * Constructs an {@link PSDeliveryTierUpgradeFlag}.
    */
   public PSDeliveryTierUpgradeFlag()
   {
      super();
   }
   
   
   /**
    * Will check for the dtsinstall.properties file for previous installations.
    * If the dtsinstall.properties is missing will check for previous CMS installations
    * with DTS.
    */
   @Override
   public void execute()
   {
      Properties props = null;
      
      setPropertyName(getInstallValue(InstallUtil.getVariableName(
            getClass().getName(), PROP_NAME_VAR)));
      
      try
      {
       //Insure that the system program directory is current.
         RxFileManager.setProgramDir(getInstallValue(RxVariables.PROGRAMS_DIR));

         String strInstallDirs = "";
         OSEnum os=OSEnum.Linux;
         
         if(PSOsTool.isWindowsPlatform()){
            os = OSEnum.Windows;
         }
         //Load the system installation props file if it exists.
         props = RxFileManager.getDTSSystemFileProperties(RxFileManager.DTS_SYS_INSTALL_PROP, 
                                                             RxFileManager.SYS_INSTALL_FILE, os); 
         if(props.size()==0)
            RxLogger.logInfo("No exisiting installs found in:" + RxFileManager.getSystemInstallationPropertiesAbsolute(os) );
       
         strInstallDirs = props.getProperty(RxFileManager.INSTALL_PROP, ""); 
        
         
         ms_upgDirs = getValidDTSInstallations(strInstallDirs);
         
         //Toggle the upgrade flag
         if (ms_upgDirs.size() > 0){
            ms_bUpgrade = true;
         }
         
         //Save the properties file removing any old or invalid entries.
         String newDirs = "";
         Iterator<?> rxDirIt = ms_upgDirs.iterator();
         while (rxDirIt.hasNext())
         {
            newDirs += (String)rxDirIt.next();
            newDirs += ";";
         }
         RxFileManager.setSystemInstallationPropertiesFile(RxFileManager.DTS_SYS_INSTALL_PROP);
         Properties newProps = RxFileManager.loadProperties(
               RxFileManager.getSystemInstallationPropertiesAbsolute(os));
         newProps.setProperty(RxFileManager.INSTALL_PROP, newDirs);
                 
         RxFileManager.saveProperties(
               newProps, RxFileManager.getSystemInstallationPropertiesAbsolute(os));
         
         //set the default directory
         if(!isSilentInstall() || StringUtils.isEmpty(getInstallValue(RxVariables.INSTALL_DIR)))
         {
            setInstallValue(RxVariables.INSTALL_DIR, RxFileManager.getDefaultInstallationDirectory());
         }
      }
      catch (Exception e)
      {
         RxLogger.logInfo("ERROR : " + e.getMessage());
         RxLogger.logInfo(e);
      }
   }

   /***
    * Takes a ; delimitted list of directories and returns a sorted tree set.
    * @param dirs
    * @return
    */
   public static SortedSet<String> getValidDTSInstallations(String dirs){


      SortedSet<String>ret = new TreeSet<>();

      if(dirs==null || dirs.equals("")){
         RxLogger.logInfo("No valid installations to detect!");
         return ret;
      }

      StringTokenizer tokens = new StringTokenizer(dirs, ";");
      while (tokens.hasMoreTokens())
      {
         String strDir = tokens.nextToken().trim();
         if (RxFileManager.isDTSDir(strDir)){
            RxLogger.logInfo("Adding " + strDir + " as an upgrade option.");
            ret.add(strDir);
         }
         else
            RxLogger.logInfo("Removing " + strDir + " as we no longer have a valid install there.");
      }

      return ret;
   }

   /**************************************************************************
    * Bean Properties
    *************************************************************************/
   /**
    *  The Upgrade property accessor.
    *
    *  @return <code>true</code> if this is an upgrade install,
    *  <code>false</code> otherwise.
    */
   public boolean getUpgrade()
   {
      return ms_bUpgrade;
   }
   
   /**
    *  @return <code>true</code> if this is an upgrade install,
    *  <code>false</code> otherwise.
    */
   public static boolean isUpgrade()
   {
      return ms_bUpgrade;
   }
   
   /*************************************************************************
    * Public functions
    *************************************************************************/
   
   /**
    * Determine if the type of Rx installation is new install.
    *
    * @return <code>true</code> if the type of Rx installation is new install,
    * <code>false</code> otherwise
    */
   public static boolean checkNewInstall()
   {
      return ms_bUpgrade ? false : true;
   }
   
   /**
    * Determine if the type of Rx installation is upgrade install.
    *
    * @return <code>true</code> if the type of Rx installation is upgrade
    * install, <code>false</code> otherwise
    */
   public static boolean checkUpgradeInstall()
   {
      return ms_bUpgrade ? true : false;
   }
   
   /**
    * Sets the type of Rx installation to new install.
    */
   public static void newInstall()
   {
      ms_bUpgrade = false;
   }
   
   /**
    * Sets the type of Rx installation to upgrade install.
    */
   public static void upgradeInstall()
   {
      ms_bUpgrade = true;
   }
   
   /**
    * Returns the sorted set of previous Rhythmyx installations on the system.
    *
    * @return a sorted set containing <code>String</code> objects representing
    * the previous Rhythmyx installations on the system.
    */
   public static Set<String> getUpgradeDirectories()
   {
      return ms_upgDirs;
   }
   
   /*************************************************************************
    * Property Accessors and Mutators
    *************************************************************************/
   
   /**
    * Accessor for the property name property.
    * 
    * @return the name of the property used to store the Rhythmyx install
    * directories.
    */
   public String getPropertyName()
   {
      return m_propertyName;
   }
   
   /**
    * Mutator for the property name property.
    * 
    * @param propertyName the name of the property used to store the Rhythmyx
    * install directories.
    */
   public void setPropertyName(String propertyName)
   {
      m_propertyName = propertyName;
   }  
   
   /**************************************************************************
    * Static Variables
    *************************************************************************/
   
   /**
    * Upgrade flag.
    */
   public static boolean ms_bUpgrade = false;
   
   /**
    * Sorted Set of previous Rhythmyx installations as <code>String</code>
    * objects, never <code>null</code>, may be empty,
    * modified in the <code>execute()</code> method.
    */
   public static SortedSet<String> ms_upgDirs = new TreeSet<>();
   
   /**
    * The variable name for the property name parameter passed in via the IDE.
    */
   private static String PROP_NAME_VAR = "propertyName";
   
   /**************************************************************************
    * Variables
    *************************************************************************/
   
   /**************************************************************************
    * Properties
    *************************************************************************/
   
   /**
    * The installation directory property name.
    */
   private String m_propertyName = RxFileManager.INSTALL_PROP;
   

}
