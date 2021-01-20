/******************************************************************************
 *
 * [ PSCheckRunningServer.java ]
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

import org.apache.tools.ant.BuildException;

public class RxCheckRunningServer extends RxIAAction
{
   // see base class
   @Override
   public void execute()
   {
      boolean cm1Running = InstallUtil.checkServerRunning(getInstallValue(RxVariables.INSTALL_DIR));
      setInstallValue(RxVariables.RX_CM1_RUNNING, Boolean.toString(cm1Running));
   }
}
