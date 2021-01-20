package com.percussion.installer.action;

import com.zerog.ia.api.pub.CustomCodeAction;
import com.zerog.ia.api.pub.InstallException;
import com.zerog.ia.api.pub.InstallerProxy;
import com.zerog.ia.api.pub.UninstallerProxy;

/**
 * This is a simple CustomCodeAction that aborts the installation.
 * 
 * At uninstall-time, it does nothing.
 * 
 * @version 3.0.0
 */
public class RxAbortInstallAction extends CustomCodeAction
{
   /**
    * This is the method that is called at install-time. The InstallerProxy
    * instance provides methods to access information in the installer, set
    * status, and control flow.
    * 
    * @see com.zerog.ia.api.pub.CustomCodeAction#install
    */
   public void install(InstallerProxy ip) throws InstallException
   {
      int exitcode = Integer.parseInt(ip.substitute("$EXIT_CODE$"));
      ip.abortInstallation(exitcode);
   }

   /**
    * This is the method that is called at uninstall-time. The DataInput
    * instance provides access to any information written at install-time with
    * the instance of DataOutput provided by UninstallerProxy.getLogOutput().
    * 
    * @see com.zerog.ia.api.pub.CustomCodeAction#uninstall
    */
   public void uninstall(UninstallerProxy up) throws InstallException
   {
   }

   /**
    * This method will be called to display a status message during the
    * installation.
    * 
    * @see com.zerog.ia.api.pub.CustomCodeAction#getInstallStatusMessage
    */
   public String getInstallStatusMessage()
   {
      return "";
   }

   /**
    * This method will be called to display a status message during the
    * uninstall.
    * 
    * @see com.zerog.ia.api.pub.CustomCodeAction#getUninstallStatusMessage
    */
   public String getUninstallStatusMessage()
   {
      return "";
   }
}
