/******************************************************************************
 *
 * [ RxIAAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installanywhere;

import com.installshield.wizard.service.file.FileService;
import com.installshield.wizard.service.system.SystemUtilService;
import com.percussion.installer.RxVariables;
import com.percussion.installer.action.RxInstall;
import com.percussion.installer.action.RxLogger;
import com.percussion.installer.model.RxModel;
import com.percussion.util.IOTools;
import com.zerog.ia.api.pub.CustomCodeAction;
import com.zerog.ia.api.pub.InstallException;
import com.zerog.ia.api.pub.InstallerProxy;
import com.zerog.ia.api.pub.ReplayVariableService;
import com.zerog.ia.api.pub.ResourceAccess;
import com.zerog.ia.api.pub.ServiceAccess;
import com.zerog.ia.api.pub.UninstallerProxy;
import com.zerog.ia.api.pub.VariableAccess;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.lang.StringUtils;


/**
 * This is an abstract, intermediate level class which links the InstallAnywhere
 * platform {@link CustomCodeAction} class with Rx custom actions.  Each
 * custom action subclass will override the {@link #execute()} method with the
 * appropriate action implementation.  This method is invoked at install time
 * by {@link CustomCodeAction#install(com.zerog.ia.api.pub.InstallerProxy)}.
 * 
 * @author peterfrontiero
 */
public abstract class RxIAAction extends CustomCodeAction implements
IPSProxyLocator
{
   @SuppressWarnings("unused")
   @Override
   public void install(InstallerProxy arg0) throws InstallException
   {
      ms_curInstallerProxy = arg0;
      execute();      
   }

   @SuppressWarnings("unused")
   @Override
   public void uninstall(UninstallerProxy arg0) throws InstallException
   {
      // Not implemented at this time.
   }

   @Override
   public String getInstallStatusMessage()
   {
      return "";
   }

   @Override
   public String getUninstallStatusMessage()
   {
      return "";
   }
   
   /**
    * See {@link IPSProxyLocator#getProxy()} for details.
    * 
    * @return the proxy object for this action.
    */
   public Object getProxy()
   {
      return installerProxy;
   }
   
   /**
    * Registers variable using ReplayVariableService to support silent installs
    * and property file generation.  The variable is first set as an
    * InstallAnywhere variable.
    * 
    * @param var the variable to register, may not be <code>null</code> or 
    * empty.
    * @param val the value of the variable to set, may not be <code>null</code>.
    * @param cat Category type of variable, may not be <code>null</code>.
    */
   protected void registerVariable(String var, String val, VarCategoryType cat)
   {
      if (StringUtils.isEmpty(var))
      {
         throw new IllegalArgumentException("var may not be empty");
      }
      
      if (cat == null)
      {
         throw new IllegalArgumentException("cat may not be null");
      }
      
      if (val == null)
      {
         throw new IllegalArgumentException("val may not be null");
      }
      
      RxIAUtils.setValue(installerProxy, var, val);
      ReplayVariableService rvs = 
         (ReplayVariableService) installerProxy.getService(
               ReplayVariableService.class);
      rvs.register(var, cat.name());
   }
   
   @SuppressWarnings("unused")
   @Override
   public long getEstimatedTimeToInstall(InstallerProxy arg0)
   {
      return getEstTimeToInstall();
   }
   
   /**
    * Called by {@link #getEstimatedTimeToInstall(InstallerProxy)}.  Should be
    * overridden by subclasses when necessary.
    * 
    * @return the estimated time required for this action to complete during
    * install. 
    */
   protected long getEstTimeToInstall()
   {
      return 0;
   }
   
   /**
    * The main processing method to be overridden by all subclasses and called
    * at install time in {@link #install(InstallerProxy)}.
    */
   protected abstract void execute();
   
   /**
    * Calls {@link RxIAUtils#getValue(VariableAccess, String)} to get the
    * value of the specified variable.
    * 
    * @param var the install variable, may not be <code>null</code> or
    * empty.
    * 
    * @return the String value of the variable.
    */
   protected String getInstallValue(String var)
   {
      if (var == null || var.trim().length() == 0)
         throw new IllegalArgumentException("var may not be null or empty");
      
      return RxIAUtils.getValue(installerProxy, var);
   }
   
   /**
    * Calls {@link RxIAUtils#setValue(VariableAccess, String, String)} to set
    * the value of the specified variable.
    * 
    * @param var the install variable, may not be <code>null</code> or
    * empty.
    * @param val the value to set for the install variable, may not be
    * <code>null</code> or empty.
    */
   protected void setInstallValue(String var, String val)
   {
      if (var == null || var.trim().length() == 0)
         throw new IllegalArgumentException("var may not be null or empty");
      
      if (val == null || val.trim().length() == 0)
         throw new IllegalArgumentException("val may not be null or empty");
      
      RxIAUtils.setValue(installerProxy, var, val);
   }
   
   /**
    * Used to change the message displayed while this action is executing
    * during pre or post-install.  Calls
    * {@link InstallerProxy#setProgressDescription(java.lang.String)}.
    * 
    * @param description the message to be displayed.
    */
   protected void setProgressDescription(String description)
   {
      ms_curInstallerProxy.setProgressDescription(description);
   }
   
   /**
    * Used to change the percentage of the progress bar during install.  Calls
    * {@link InstallerProxy#setProgressPercentage(float)}.
    * 
    * @param percentage the new percentage for the progress bar.
    */
   protected void setProgressPercentage(float percentage)
   {
      ms_curInstallerProxy.setProgressPercentage(percentage);
   }
   
   /**
    * Used to change the text displayed above the progress bar while this action
    * is executing during install in GUI mode.  Calls
    * {@link InstallerProxy#setProgressStatusText(java.lang.String)}.
    * 
    * @param text the text to be displayed above the progress bar.
    */
   protected void setProgressStatusText(String text)
   {
      ms_curInstallerProxy.setProgressStatusText(text);
   }
   
   /**
    * Used to change the title of the panel displayed while this action is
    * executing during pre or post-install.  Calls
    * {@link InstallerProxy#setProgressTitle(java.lang.String)}.
    * 
    * @param title the panel title.
    */
   protected void setProgressTitle(String title)
   {
      ms_curInstallerProxy.setProgressTitle(title);
   }
   
   /**
    * Resolves all InstallAnywhere variables in the given string.
    * 
    * @param str all InstallAnywhere variables present in this string will be
    * resolved, replaced with the appropriate value.
    * 
    * @return the resolved string, never <code>null</code> or empty.
    */
   protected String resolveString(String str)
   {
      return RxIAUtils.resolve(installerProxy, str);
   }
   
   /**
    * See {@link RxIAUtils#getResource(ResourceAccess, String)} for details.
    * 
    * @param path the path of the resource.
    * 
    * @return a {@link URL} reference to the resource.     
    */   
   protected URL getResource(String path)
   {
      return RxIAUtils.getResource(installerProxy, path);
   }
   
   /**
    * Retrieves the specified resource from the installer archive if it exists.
    * The resource is copied to the installer's temp directory.
    * 
    * @param path the path of the desired resource in the archive.
    * 
    * @return a <code>File</code> object representing the requested resource or
    * <code>null</code> if the resource was not found.
    * 
    * @throws IOException if an error occurs reading or writing the resource.
    */
   protected File getResourceFile(String path) throws IOException
   {
      File resourceFile = null;
      String tempDir = getInstallValue(RxVariables.INSTALLER_TEMP_DIR);
      
      URL source = getResource(path);
      if (source != null)
      {
         File srcFile = new File(source.getPath());
         File tempFile = new File(tempDir, srcFile.getName());
         
         InputStream in = source.openStream();
         OutputStream out = new FileOutputStream(tempFile);
         
         try
         {
            IOTools.copyStream(in, out);
            resourceFile = tempFile;
         }
         finally
         {
            if (in != null)
            {
               in.close();
            }
            
            if (out != null)
            {
               out.close();
            }
         }
      }
      else
         RxLogger.logInfo("Could not locate resource: " + path);
      
      return resourceFile;
   }
   
   /**
    * See {@link RxIAUtils#getFileService(ServiceAccess)} for details.
    * 
    * @return the {@link FileService} for this action's proxy. 
    */
   protected FileService getFileService()
   {
      return RxIAUtils.getFileService(installerProxy);
   }
   
   /**
    * See {@link RxIAUtils#getSystemUtilService(ServiceAccess)} for details.
    * 
    * @return the {@link SystemUtilService} for this action's proxy.
    */
   protected SystemUtilService getSystemUtilService()
   {
      return RxIAUtils.getSystemUtilService(installerProxy);
   }
   
   /**
    * Determines if the current installer is running in console mode.
    * 
    * @return <code>true</code> if the installer is running in console mode,
    * <code>false</code> otherwise.
    */
   protected boolean isConsoleInstall()
   {
      String uiMode = getInstallValue(RxVariables.INSTALLER_UI);
      
      return uiMode.equalsIgnoreCase("CONSOLE");
   }
   
   /**
    * Determines if the current installer is running in silent mode.
    * 
    * @return <code>true</code> if the installer is running in silent mode,
    * <code>false</code> otherwise.
    */
   protected boolean isSilentInstall()
   {
      String uiMode = getInstallValue(RxVariables.INSTALLER_UI);
      
      return uiMode.equalsIgnoreCase("SILENT");
   }
   
   /**
    * Retrieves the list of current data models from either {@link RxIAConsole}
    * or {@link RxIAPanel} depending on the mode in which the installer is
    * running.
    * 
    * @return the list of <code>RxModel</code> objects currently in memory.
    */
   protected List<RxModel> getModels()
   {
      return isConsoleInstall() ?  RxIAConsole.getModels() :
         RxIAPanel.getModels();
   }
   
   /**
    * Gets the install variables.
    * 
    * @return the current set of install variables as an enumeration, never
    * <code>null</code>.
    */
   protected Enumeration getInstallVariables()
   {
      return installerProxy.getVariables();
   }
   
   /**
    * Local copy of the {@link InstallerProxy} object required for direct
    * access to the progress bar during installation by {@link RxInstall}.
    * Set in {@link #install(InstallerProxy)}.
    */
   private static InstallerProxy ms_curInstallerProxy;
   
   /**
    * The different categories of variables. 
    */
   protected enum VarCategoryType
   {
      INSTALLATION_CONFIGURATION,
      PRODUCT_SELECTION,
      UPGRADE_CONFIGURATION,
      REPOSITORY_CONFIGURATION,
      SERVER_PROPERTIES
   }
}
