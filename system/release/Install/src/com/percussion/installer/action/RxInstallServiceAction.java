/******************************************************************************
 *
 * [ RxInstallServiceAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.action;

import com.percussion.install.InstallUtil;
import com.percussion.installanywhere.RxIAAction;
import com.percussion.installer.RxVariables;
import com.percussion.util.PSProperties;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;


/**
 * Installs Rhythmyx as a service.
 */
@Deprecated
public class RxInstallServiceAction extends RxIAAction
{
   @Override
   public void execute()
   {
       throw new NotImplementedException("No longer use JNI registry methods need to replace");
   }
   
   /*************************************************************************
    * Worker functions
    *************************************************************************/
   /**
    * Helper method to filter out any duplicates but preserving the order.
    * 
    * @param arlList the list of Strings to filter, assumed not
    * <code>null</code>.
    */
   private static void removeDuplicatesWithOrder(ArrayList<String> arlList)
   {
      Set<String> set = new HashSet<>();
      java.util.List<String> newList = new ArrayList<>();
      for (java.util.Iterator iter = arlList.iterator(); iter.hasNext();)
      {
         String tmpStr = (String) iter.next();
         // set.add will fail if it already exists in the list
         if (set.add(tmpStr.trim()))
            newList.add(tmpStr);
      }
      arlList.clear();
      arlList.addAll(newList);
   }
   
   /**
    * Returns the install directory from the registry settings based on the
    * service name.
    *  
    * @param svcName name for the service, assumed not <code>null</code>.
    * 
    * @return the installation directory corresponding to the specified service. 
    */
   private String getInstallationPathFromRegistry(String svcName)
   {
       throw new NotImplementedException("No longer use JNI registry methods need to replace");
   }
   /**
    * This method will first extract the install directory from the registry
    * based on the service name. If the service exists it returns the install
    * directory for the service.
    * If the service does not exist it returns the new install directory that 
    * was selected during this installation
    * @param svcName the service name, assumed not <code>null</code>.
    * @return install directory
    */
   private String getInstallDir(String svcName)
   {
      String imgPathDir = getInstallationPathFromRegistry(svcName);
      String installDir = getInstallValue(RxVariables.INSTALL_DIR);
      
      if (imgPathDir != null && imgPathDir.compareTo(installDir) != 0)
      {
         RxLogger.logInfo("************************************************");
         RxLogger.logInfo(
         "Failed to verify install directory for this installation");
         RxLogger.logInfo(
               "    Registry settings for the specified service: "
               + svcName
               + " does not");
         RxLogger.logInfo(
               "    agree with the selected install dir:" + installDir);
         RxLogger.logInfo("************************************************");
      }
      return installDir;
   }
   
   /**
    * Stores registry classpath and vm options.
    */
   private class RegVars
   {
      /**
       * The classpath for the registry variable.
       */
      String classPath;
      
      /**
       * The vm options for the registry variable.
       */
      String[] vmOpts;
      
      /**
       * Constructor.
       * 
       * @param cp the classpath.
       * @param vm the vm options.
       */
      RegVars(String cp, String[] vm)
      {
         classPath = cp;
         vmOpts = vm;
      }
      
      /**
       * Accessor for the registry variable classpath.
       * 
       * @return the classpath.
       */
      String getClassPath()
      {
         return classPath;
      }
      
      /**
       * Accessor for the registry variable vm options.
       * 
       * @return the vm options.
       */
      String[] getVmOpts()
      {
         return vmOpts;
      }
      
   }
   
   /**
    * Based on the service name, extract CLASSPATH and VMOpts if they exist 
    * NOTE: From 55 and UP, there will be no use for these RegKeys. They are 
    * generated merged ( if necessary )and written to storage for the launcher
    * to use them
    * @param svcName the service name, assumed not <code>null</code>.
    * 
    * @return {@link RegVars} object representing the extracted classpath and vm
    * options for the service.
    */
   private RegVars getCPandVMOptsFromRegistry(String svcName)
   {
       throw new NotImplementedException("No longer use JNI registry methods need to replace");

   }
   
   /**
    * Algorithm:
    * 1. get oldclasspath from registry and add UDB path (if necessary)
    * 2. Remove any dups in this list
    * 3. See if the .cp file exists, if it does add its paths to the end of the 
    *    current list. If this file does not exist, piece of cake : write to this
    *    file all the paths accumulated thus far
    * @param svcName the service name.
    */
   private void purifyAndCreateClassPathFile(String svcName)
   {
      String installDir     = getInstallDir(svcName);
      RegVars regvars       = getCPandVMOptsFromRegistry(svcName);
      String[] oldClassPath = regvars.getClassPath().split("\\;");
      
      // generate array lists for the old class path from the registry and
      // for the classpath builtin to the exe
      ArrayList<String> oldClassPathArr =
         new ArrayList<>((List<String>) Arrays.asList(oldClassPath));
      
      /*
       * Remove any duplicate entries in the oldClassPath Array
       */
      removeDuplicatesWithOrder(oldClassPathArr);
      ArrayList<String> exeClassPathArr =
         new ArrayList<>((List<String>) Arrays.asList(ms_ClassPath));
      ArrayList<String> excludeClassPathArr =
         new ArrayList<>((List<String>) Arrays.asList(ms_validExcludes));
      
      /*
       * Remove any jars from the oldClassPath Array that exist in the exe's CP
       * Remove any valid excludes that we know are not needed going forward
       */
      ListIterator oldIter = oldClassPathArr.listIterator();
      for (; oldIter.hasNext();)
      {
         Object elem = oldIter.next();
         if (exeClassPathArr.contains(elem)
               || excludeClassPathArr.contains(elem))
            oldIter.remove();
      }
      
      // 1.4 is smart to figure out forward vs backward slash ;)
      // if the CP file exists merge entries from the purified registry CP..
      File cpFile = new File(installDir + "/RhythmyxServer.cp");
      ArrayList<String> cpFileArr = new ArrayList<>();
      cpFileArr.ensureCapacity(20);
      
      if ( cpFile.exists() )
      {
         try
         {
            java.io.BufferedReader in =
               new java.io.BufferedReader(
                     new java.io.FileReader(installDir + "/RhythmyxServer.cp"));
            String str;
            while ((str = in.readLine()) != null)
            {
               cpFileArr.add(str);
            }
            in.close();
         }
         catch (java.io.IOException e)  
         {
            RxLogger.logInfo("Could not read file:"+cpFile.getName());
            RxLogger.logInfo(e.getMessage());
         }
      }
      cpFileArr.addAll(oldClassPathArr);
      removeDuplicatesWithOrder(cpFileArr);
      
      try {
         java.io.BufferedWriter out = 
            new java.io.BufferedWriter(
                  new java.io.FileWriter(installDir + "/RhythmyxServer.cp"));
         ListIterator listIT = cpFileArr.listIterator();
         while (listIT.hasNext())
         {
            String str = (String) listIT.next();
            out.write(str);
            out.newLine();
         }
         out.close();
      } 
      catch (java.io.IOException e) 
      {
         RxLogger.logInfo("Could not write to file:"+cpFile.getName());
         RxLogger.logInfo(e.getMessage()); 
      }
      return;
   }
   
   /**
    * Read registry's VMOpts, add static VMOpts, then add java lib path
    * Next, read .ja file if it exists and append them at the end of the 
    * array list and remove duplicates preserving order.
    * @param svcName the Rhythmyx service name
    */
   private void purifyAndCreateExtraVMOptsFile(String svcName)
   {  
      String installDir  = getInstallDir(svcName); 
      int maxHeap = ms_jvmHeapMax; // 512
      
      // Build up VMOpts from registry key 
      RegVars regvars    = getCPandVMOptsFromRegistry(svcName);
      String[] oldVMOpts = regvars.getVmOpts();
      ArrayList<String> oldVMOptsArr = 
         new ArrayList<>((List<String>)Arrays.asList(oldVMOpts));
      
      // Build up the current opts
      String[] curOpts = ms_vmLibOpts.split("\\;");
      String svcLibPath = installDir + File.separator + "bin;"
      + installDir + File.separator + "plugins;";
      String libOpts = "-Djava.library.path=" + svcLibPath;
      ArrayList<String> curOptsArr = 
         new ArrayList<>((List<String>)Arrays.asList(curOpts));
      curOptsArr.add(libOpts);
      
      // Iterate oldOpts and prune curOpts for any identical 
      // as a special case hold the one that has higher jvmHeapSize used
      ListIterator oldOptsIt = oldVMOptsArr.listIterator();
      ListIterator curOptsIt = curOptsArr.listIterator();
      
      while ( oldOptsIt.hasNext() )
      {
         String opt = (String) oldOptsIt.next();
         if ( curOptsArr.contains(opt) )
         {
            // since it is the same in user defined opts delete from curOpts
            curOptsArr.remove((Object) opt);
         }
         else
         {
            if ( opt.startsWith("-Xmx") && opt.endsWith("m") )
            {
               String mbVal = opt.substring(4,opt.length()-1);
               int iMB = 0;
               try
               {
                  iMB = Integer.parseInt(mbVal);
               }
               catch (Exception e) { }
               /**
                * If the user defined option is > min required
                * (ms_jvmHeapMax), keep user defined option and delete
                * from curOptsArr
                */
               if ( iMB >= 512 )
               {
                  maxHeap = iMB;
                  while ( curOptsIt.hasNext() )
                  {
                     String curOpt = (String) curOptsIt.next();
                     if ( curOpt.startsWith("-Xmx") && curOpt.endsWith("m") )
                        curOptsIt.remove();
                  }
                  // reset curOptsIt for later use
                  curOptsIt = curOptsArr.listIterator();
               }
               else
               {
                  /**
                   * old opts is < min required (ms_jvmHeapMax), remove it
                   */ 
                  maxHeap = ms_jvmHeapMax;
                  oldOptsIt.remove();
               }
            }
         }                
      }
      
      // Yoo Hoo DONE!! merge curOptsArr
      oldVMOptsArr.addAll(curOptsArr);
      
      File vmOptsFile = new File(installDir + "/RhythmyxServer.ja");
      ArrayList<String> vmOptsFileArr = new ArrayList<>();
      vmOptsFileArr.ensureCapacity(20);
      
      if ( vmOptsFile.exists() )
      {
         try
         {
            java.io.BufferedReader in =
               new java.io.BufferedReader(
                     new java.io.FileReader(installDir + "/RhythmyxServer.ja"));
            String str;
            while ((str = in.readLine()) != null)
            {
               if ( str.startsWith("-Xmx") && str.endsWith("m") )
               {
                  String mbVal = str.substring(4, str.length()-1);
                  int iMB = 0;
                  try
                  {
                     iMB = Integer.parseInt(mbVal);
                  }
                  catch (Exception e) { }
                  /**
                   * If the user defined option from .ja is > current maxHeap 
                   *  keep .ja defined option and delete  from oldVMOptsArr
                   */
                  if ( iMB >= maxHeap )
                  {
                     maxHeap = iMB;
                     vmOptsFileArr.add(str);
                     // delete from oldVMOptsArray since it is < maxHeap
                     ListIterator oldVMOptsArrIt = oldVMOptsArr.listIterator();
                     while ( oldVMOptsArrIt.hasNext() )
                     {
                        String oldOpt = (String) oldVMOptsArrIt.next();
                        if ( oldOpt.startsWith("-Xmx") && oldOpt.endsWith("m") )
                           oldVMOptsArrIt.remove();
                     }
                  }
                  continue;
               }
               vmOptsFileArr.add(str);
            }
            in.close();
         }
         catch (java.io.IOException e)  
         {
            RxLogger.logInfo("Could not read file:"+vmOptsFile.getName());
            RxLogger.logInfo(e.getMessage());
         }
      }
      vmOptsFileArr.addAll(oldVMOptsArr);
      removeDuplicatesWithOrder(vmOptsFileArr);
      
      try {
         java.io.BufferedWriter out = 
            new java.io.BufferedWriter(
                  new java.io.FileWriter(installDir + "/RhythmyxServer.ja"));
         ListIterator listIT = vmOptsFileArr.listIterator();
         while (listIT.hasNext())
         {
            String str = (String) listIT.next();
            out.write(str);
            out.newLine();
         }
         out.close();
      } 
      catch (java.io.IOException e) 
      {
         RxLogger.logInfo("Could not write to file:"+vmOptsFile.getName());
         RxLogger.logInfo(e.getMessage()); 
      }  
   }

   
   /**
    * This function will update an existing service to reflect the existing
    * installation.
    *
    * Rhythmyx/Parameters key
    * The old and new classpaths will be merged.
    *
    * The old and new ExtraVMOptions will be merged.
    *
    * The RuntimeLib will always be overwritten.
    *
    * Rhythmyx key
    * The ImagePath will be updated to reflect the current installation
    * directory.
    *
    * @param strRootDir is the root directory being installed to and must not
    * not be <code>null</code>.
    * @param svcName the service name, assumed not <code>null</code>.
    * @param svcDesc the service description, assumed not
    * <code>null</code>.   
    *
    * @throws IllegalArgumentException if any parameter requirement is not met
    */
   private void updateService(
         String strRootDir,
         String svcName,
         String svcDesc)
   {
       throw new NotImplementedException("No longer use JNI registry methods need to replace");

   }
   
   
   /**
    * This function will create the nt service.
    *
    * @param strRootDir is the root directory being installed to and must not
    * not be <code>null</code>.
    * @param svcName the service name, assumed not <code>null</code>.
    * @param svcDesc the service description, assumed not <code>null</code>.   
    *
    * @throws IllegalArgumentException if any parameter requirement is not met
    */
   private void installService(
         String strRootDir,
         String svcName,
         String svcDesc)
   {
       throw new NotImplementedException("No longer use JNI registry methods need to replace");

   }
   
   /**
    * Get the runtime library to run java.
    *
    * @param strRootDir the installation directory, may not be 
    * <code>null</code>.
    * @return the full path to the java library to load while running the service.
    */
   private String getRuntimeLib(String strRootDir)
   {
      if (strRootDir == null)
         throw new IllegalArgumentException("root must not be null.");
      
      String strJava = strRootDir;
      
      if (m_strExtendedName != null && m_strExtendedName.length() > 0)
         strJava += File.separator + m_strExtendedName;
      
      strJava += File.separator
      + "jre"
      + File.separator
      + "bin"
      + File.separator
      + "client"
      + File.separator
      + "jvm.dll";
      return (strJava);
   }
   
   /**
    * Attempts to load the Rhythmyx service name and description from the given
    * properties file.
    * 
    * @param propsFile properties file which contains the service information,
    * assumed not <code>null</code>.
    * 
    * @return <code>true</code> if the service name property was found (not blank), <code>false</code> otherwise.
    */
   private boolean loadServiceNameAndDescription(File propsFile)
   {
      boolean svcNameExists = false;
      
      PSProperties props = new PSProperties();
      String propsFilePath = propsFile.getAbsolutePath();
      try
      {
         props = new PSProperties(propsFilePath);
      }
      catch (IOException ioe)
      {
         RxLogger.logError(
               "RxInstallServiceAction#loadServiceNameAndDescription : error " +
               "occurred reading properties file " + propsFilePath);
         RxLogger.logError(ioe);
      }
         
      String svcName = props.getProperty(InstallUtil.RHYTHMYX_SVC_NAME);
      if (StringUtils.isNotBlank(svcName))
      {
         m_serviceName = svcName;
         svcNameExists = true;
      }
      else
      {
         RxLogger.logWarn("RxInstallServiceAction#loadServiceNameAndDescription : Could not find property '"
               + InstallUtil.RHYTHMYX_SVC_NAME + "'."); 
      }
      
      String svcDesc = props.getProperty(InstallUtil.RHYTHMYX_SVC_DESC);
      if (StringUtils.isNotBlank(svcDesc))
      {
         m_serviceDescription = svcDesc;
      }
      else
      {
         RxLogger.logWarn("RxInstallServiceAction#loadServiceNameAndDescription : Could not find property '"
               + InstallUtil.RHYTHMYX_SVC_DESC + "'.  Using default service description '" + m_serviceDescription
               + "'."); 
      }
      
      return svcNameExists;
   }
   
   /*************************************************************************
    * Property Accessors and Mutators
    *************************************************************************/
   
   /**
    * Returns the files that will be removed from the classpath
    *
    * @return an array of file paths relative to the Rhythmyx root directory,
    * never <code>null</code>, may be empty
    */
   public String[] getRemoveFromClasspath()
   {
      return m_removeFromClasspath;
   }
   
   /**
    * Removes the specified files from the classpath.
    *
    * @param files the files to remove from the classpath. The path of the
    * jar files is relative to the Rhythmyx root directory.
    */
   public void setRemoveFromClasspath(String[] files)
   {
      if (files == null)
         files = new String[0];
      m_removeFromClasspath = files;
   }
   
   /**
    * Accessor for service program.
    * 
    * @return the service program.
    */
   public String getServiceProgram()
   {
      return (m_strServiceProgram);
   }
   
   /**
    * Mutator for service program.
    * 
    * @param strServiceProgram the service program name.
    */
   public void setServiceProgram(String strServiceProgram)
   {
      m_strServiceProgram = strServiceProgram;
   }
   
   /**                           
    * Accessor for extended name.
    * 
    * @return the extended name
    */
   public String getExtendedName()
   {
      return (m_strExtendedName);
   }
   
   /**
    * Mutator for extended name.
    * 
    * @param strExtendedName the new extended name.
    */
   public void setExtendedName(String strExtendedName)
   {
      m_strExtendedName = strExtendedName;
   }
   
   /**                          
    * Accessor for extra parameters.
    * 
    * @return array of extra parameters.
    */
   public String[] getExtraParam()
   {
      return (m_strExtraParam);
   }
   
   /**
    * Mutator for extra parameters.
    * 
    * @param strExtra array of extra parameters.
    */
   public void setExtraParam(String[] strExtra)
   {
      m_strExtraParam = strExtra;
   }
   
   /**                           
    * Accessor for LaunchClasses param.
    * 
    * @return array of launch classes.
    */
   public String[] getLaunchClasses()
   {
      return m_strLaunchClasses;
   }
   
   /**
    * Mutator for LaunchClasses param.
    * 
    * @param strLaunchClasses array of launch classes.
    */
   public void setLaunchClasses(String[] strLaunchClasses)
   {
      m_strLaunchClasses = strLaunchClasses;
   }
   
   /**
    * Returns the Rhythmyx Service Name.
    * @return the Rhythmyx Service Name, never <code>null</code> or empty.
    */
   public String getServiceName()
   {
      return m_serviceName;
   }
   
   /**
    * Returns the Rhythmyx Service Description.
    * @return the Rhythmyx Service Description, never <code>null</code> or
    * empty.
    */
   public String getServiceDescription()
   {
      return m_serviceDescription;
   }
   
   /**
    * Sets the Rhythmyx Service Description.
    * @param desc the description for the service.
    */
   public void setServiceDescription(String desc)
   {
      m_serviceDescription = desc;
   }
   
   /**
    * Sets the Rhythmyx Service Name.
    * @param serviceName the Rhythmyx Service Name, may not be <code>null</code>
    * or empty.
    * @throw IllegalArgumentException if serviceName is <code>null</code> or
    * empty.
    */
   public void setServiceName(String serviceName)
   {
      if ((serviceName == null) || (serviceName.trim().length() == 0))
         throw new IllegalArgumentException("serviceName may not be null or " +
               "empty");
      m_serviceName = serviceName;
   }
   
   /**
    * Accessor for the start command of the service.
    * 
    * @return the service start command.
    */
   public String getStartCmd()
   {
      return m_startCmd;
   }
   
   /**
    * Accessor for the stop command of the service.
    * 
    * @return the service stop command.
    */
   public String getStopCmd()
   {
      return m_stopCmd;
   }
   
   /**
    * Accessor for the working directory of the service.
    * 
    * @return the service working directory.
    */
   public String getWorkingDir()
   {
      return m_workingDir;
   }
   
   /**
    * Sets the start command of the service.
    * 
    * @param string the start command.
    */
   public void setStartCmd(String string)
   {
      m_startCmd = string;
   }
   
   /**
    * Sets the stop command of the service.
    * 
    * @param string the stop command.
    */
   public void setStopCmd(String string)
   {
      m_stopCmd = string;
   }
   
   /**
    * Sets the working directory of the service.
    * 
    * @param string the working directory.
    */
   public void setWorkingDir(String string)
   {
      m_workingDir = string;
   }
   
   /**
    * Accessor for the create window flag.
    * 
    * @return 'yes' to create a new window, 'no' otherwise.
    */
   public String getCreateWindow()
   {
      return m_createWindow;
   }
   
   /**
    * Sets the create window flag.
    * 
    * @param string 'yes' or 'no.'
    */
   public void setCreateWindow(String string)
   {
      m_createWindow = string;
   }
   
   /**
    * Accessor for the Rhythmyx service type.
    * 
    * @return service type.
    */
   public String getRxServiceType()
   {
      return m_rxServiceType;
   }
   
   /**
    * Sets the Rhythmyx service type.
    * 
    * @param string service type.
    */
   public void setRxServiceType(String string)
   {
      m_rxServiceType = string;
   }
   
   /**************************************************************************
    * Variables
    *************************************************************************/
   
   /**
    * Rhythmyx service type.
    */
   private String m_rxServiceType = "RhythmyxServer";
   
   /**
    * The parent key for the Rhythmyx service.
    */
   private static final String SERVICE_KEY =
      "SYSTEM\\CurrentControlSet\\Services\\";
   
   /**
    * The parent key for the OLD Rhythmyx service.
    */
   @SuppressWarnings("unused")
   private static final String OLD_SERVICE_START_KEY =
      "SYSTEM\\CurrentControlSet\\Services\\Rhythmyx";
   
   /**
    * The key for services.
    */
   @SuppressWarnings("unused")
   private static final String SERVICES_KEY =
      "SYSTEM\\CurrentControlSet\\Services";
   
   /**
    * The key for services.
    */
   @SuppressWarnings("unused")
   private static final String OLD_RX_KEY = "Rhythmyx";
   
   /**
    * The key that is used to create user defined parameters with services.
    */
   private static final String PARAMETERS_KEY = "Parameters";
   
   /**
    * The key that points to the classpath path to run under.
    */
   private static final String CLASSPATH_KEY = "CLASSPATH";
   
   /**
    * The key that points to the java library to use when lanching the Java class.
    */
   private static final String RUNTIMELIB_KEY = "RuntimeLib";
   
   /**
    * The key that points to extra vm options when starting the java service.
    */
   private static final String EXTRAVMOPTIONS_KEY = "ExtraVMOpts";
   
   /**
    * Launch class key used to pass class(s) names to the NT service
    * executable telling it what java classes it has to launch.
    * For now there should always be only one class to launch. 
    */
   private static final String LAUNCH_CLASSES_KEY = "LaunchClasses";
   
   /**
    * The key that points to the full path of the service program.
    */
   private static final String IMAGEPATH_KEY = "ImagePath";
   
   /**
    * The key that points to the description of the service.
    */
   private static final String DESCRIPTION_KEY   = "Description";
   
   /**
    * The key that points to the display name of the service.
    */
   private static final String DISPLAYNAME_KEY   = "DisplayName";
   
   /**
    * The key that points to the stop command of the service.
    */
   private static final String SHUTDOWN_KEY      = "Stop Cmd";
   
   /**
    * The key that points to the start command of the service.
    */
   private static final String STARTUP_KEY       = "Start Cmd";
   
   /**
    * The key that points to the working directory of the service.
    */
   private static final String WORKDIR_KEY       = "Working Dir";
   
   /**
    * The key that points to the create window value of the service.
    */
   private static final String CREATEWINDOW_KEY  = "Create Window";
   
   /**
    * The program to remove services.
    */
   @SuppressWarnings("unused")
   private static final String REMOVE_SERVICE_PROGRAM =
      "bin" + File.separator + "removeservice.exe";
   
   /**************************************************************************
    * Properties
    *************************************************************************/
   
   /**
    * Lists the jar files to remove from the classpath, set using Installshield
    * UI, never <code>null</code>, may be empty
    */
   private String[] m_removeFromClasspath =
      new String[] { "jdbc/oracle8/classes12.zip", "psctoolkit.jar" };
   
   /**
    * The service program, relative to the Rhythmyx root.
    */   
   private String m_strServiceProgram =
      "bin" + File.separator + "rxservice.exe";
   
   /**
    * The extended name.
    */
   private String m_strExtendedName = "";
   
   /**
    * Extra parameters.
    */
   private String[] m_strExtraParam =
      new String[] { "-Djava.security.policy=.rx.policy",
         //java security policy
         "-Xmx256m" //max heap size
   };
   
   /**
    * Classes to launch.
    */
   private String[] m_strLaunchClasses =
      new String[] { "com/percussion/server/PSServer" };
   
   /**
    * Start command.
    */
   private String m_startCmd = "$USER_INSTALL_DIR$\\PercussionCM.exe";
   
   /**
    * Stop command.
    */
   private String m_stopCmd =
      "$USER_INSTALL_DIR$\\AppServer\\bin\\shutdown_service.bat";
   
   /**
    * Create window.
    */
   private String m_createWindow = "yes";
   
   /**
    * Working directory.
    */
   private String m_workingDir = "$USER_INSTALL_DIR$";
   
   /**
    * stores the Rhythmyx Service Name, never <code>null</code> or empty
    */
   private String m_serviceName = "Percussion Service";
   
   /**
    * stores the Rhythmyx Service Desc, never <code>null</code> or empty
    */
   private String m_serviceDescription = "Percussion Service";
   
   /**
    * If you think a library that is used pre-55 is not needed in 55 and going
    * forward, add it to this list, so that cleanCP which reads the CLASSPATH
    * from the registry would *ALSO* remove the following going forward..
    */
   private static String[] ms_validExcludes =
   {
      "jre/lib/rt.jar",
      "jre/lib/ext/iiimp.jar",
      "jre/lib/i18n.jar",
      "jre/lib/jaws.jar",
      "jre/lib/plugprov.jar" };
   
   /**
    * These are the jars on the classpath of the exe process
    */
   private static String[] ms_ClassPath =
   {
      "lib/log4j.jar",
      "lib/xmlParserAPIs.jar",
      "lib/xercesImpl.jar",
      "lib/saxon.jar",
      "lib/rxextensions.jar",
      "lib/rxinstall.jar",
      "jdbc/Sybase/jConnect/sybase.jar",
      "lib/rxserver.jar",
      "lib/rxclient.jar",
      "lib/jaas.jar",
      "lib/jndi.jar",
      "lib/providerutil.jar",
      "lib/js.jar",
      "lib/servlet.jar",
      "lib/server.jar",
      "lib/tcljava.jar",
      "lib/jacl.jar",
      "lib/soap.jar",
      "lib/ant.jar",
      "lib/jasper.jar",
      "lib/jaxp.jar",
      "lib/parser.jar",
      "lib/ldapbp.jar",
      "lib/ldap.jar",
      "lib/nis.jar",
      "lib/html.jar",
      "lib/mail.jar",
      "lib/activation.jar",
      "lib/rxworkflow.jar",
      "lib/rxpublisher.jar",
      "lib/rxmisctools.jar",
      "lib/rxuploader.jar",
      "lib/Tidy.jar",
      "jdbc/oracle/classes12.jar",
      "jdbc/sprinta/Sprinta2000.jar",
      "lib/rxagent.jar",
      "lib/rxtablefactory.jar",
      "lib/percbeans.jar",
      "lib/rxi18n.jar",
      "lib/xml4j.jar",
      "lib/docucomp.jar",
      "lib/serveruicomp.jar",
      "lib/psctoolkit5.jar",
      "lib/userextensions.jar",
      "lib/jai_codec.jar",
      "lib/jai_core.jar",
      "jdbc/jtds/jtds.jar" 
   };
   
   /**
    * Maximum jvm heap size.
    */
   private static int ms_jvmHeapMax = 512; 
   
   /**
    * VM library options.
    */
   private static String ms_vmLibOpts =
      "-Djava.security.policy=.rx.policy;-Xmx" + ms_jvmHeapMax + "m;";
   /**************************************************************************
    * Main (for testing)
    *************************************************************************/
   
   /**
    * This can be used to test service install and update without the need
    * for manufacture and install.
    *
    * @param args no arguments expected, may be <code>null</code> or empty
    */
   public static void main(String[] args)
   {
      System.out.println("Before loading regisryDll");
      System.loadLibrary("PSJniRegistry");
      System.out.println("After loading regisryDll");
      RxInstallServiceAction svc = new RxInstallServiceAction();
      svc.purifyAndCreateExtraVMOptsFile("55.1");
   }
   
   
}
