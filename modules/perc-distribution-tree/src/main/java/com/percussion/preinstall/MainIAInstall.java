/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.preinstall;

import com.zerog.ia.api.pub.*;

public class MainIAInstall extends CustomCodeAction {
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

          e.printStackTrace();
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
            e.printStackTrace();
        }
    }

    public static float calculatePercentage(int lineNo){
        return (lineNo * 100) / ESTIMATED_LINES;
    }
}
