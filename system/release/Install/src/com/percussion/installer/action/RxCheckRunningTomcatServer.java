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
import com.percussion.installanywhere.RxIAFileUtils;
import com.percussion.installer.RxVariables;

import org.apache.tools.ant.BuildException;



public class RxCheckRunningTomcatServer extends RxIAAction
{
   // see base class
   @Override
   public void execute()
   {
      boolean tomcatRunning = InstallUtil.checkTomcatServerRunning(getInstallValue(RxVariables.INSTALL_DIR));
      setInstallValue(RxVariables.RX_TOMCAT_RUNNING, Boolean.toString(tomcatRunning));
   }
}
