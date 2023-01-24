/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.preinstall;

import com.percussion.error.PSExceptionUtils;
import com.zerog.ia.api.pub.CustomCodeAction;
import com.zerog.ia.api.pub.InstallException;
import com.zerog.ia.api.pub.InstallerProxy;
import com.zerog.ia.api.pub.ProgressAccess;
import com.zerog.ia.api.pub.UninstallerProxy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainIAInstall extends CustomCodeAction {

    private static final Logger log = LogManager.getLogger(MainIAInstall.class);

    public static final int ESTIMATED_LINES=30000;

    public static InstallerProxy installProxy=null;

    @Override
    public void install(InstallerProxy installerProxy) throws InstallException {
      try {
          installProxy = installerProxy;

          String installDir =  installProxy.substitute("$USER_INSTALL_DIR$");
         Main.main(new String[]{installDir});
         Boolean error=Main.error;
         if(error){
       PercussionCustomRuleFailureCase.rulePass=true;
         }else{
             PercussionCustomRuleSuccess.rulePass=true;
         }
      }catch(Exception e){
          log.error(PSExceptionUtils.getMessageForLog(e));
          log.debug(PSExceptionUtils.getDebugMessageForLog(e));
      }
    }

    @Override
    public void uninstall(UninstallerProxy uninstallerProxy) throws InstallException {
        //TODO: Not currently implemented
    }

    @Override
    public String getInstallStatusMessage() {
        return "Installing files...";
    }

    @Override
    public String getUninstallStatusMessage() {
        return "Uninstalling files...";
    }

    @Override
    public long getEstimatedTimeToInstall(InstallerProxy installerProxy) {
        return 100;
    }

    @Override
    public long getEstimatedTimeToUninstall(UninstallerProxy uninstallerProxy) {
        return 100;
    }
    public static void showProgress(ProgressAccess progressAccess, int lineNo, String actionTitle, String lineText) {
        try {
            progressAccess.setProgressTitle(actionTitle);
            progressAccess.setProgressStatusText(lineText);
            progressAccess.setProgressPercentage(calculatePercentage(lineNo));
        }catch (Exception e){
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
        }
    }

    public static float calculatePercentage(int lineNo){
        return (lineNo * 100) / ESTIMATED_LINES;
    }
}
