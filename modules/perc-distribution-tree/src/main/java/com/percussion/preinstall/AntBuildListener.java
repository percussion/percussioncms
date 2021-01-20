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

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;

/***
 * Listens for ant build output and passes the messages to the Installer if running
 * in installer mode.  For dev installs does nothing.
 *
 * See https://ant.apache.org/manual/develop.html#buildevents
 */
public class AntBuildListener implements BuildListener {


    @Override
    public void buildStarted(BuildEvent buildEvent) {
        try {
            if (MainIAInstall.installProxy != null) {
                Main.currentLineNo++;
                MainIAInstall.showProgress(MainIAInstall.installProxy, Main.currentLineNo, "Installing files...", buildEvent.getMessage());
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void buildFinished(BuildEvent buildEvent) {
        try {
            if (MainIAInstall.installProxy != null) {
                Main.currentLineNo++;
                MainIAInstall.showProgress(MainIAInstall.installProxy, Main.currentLineNo, "Installation complete.", buildEvent.getMessage());
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void targetStarted(BuildEvent buildEvent) {
        try {
            if (MainIAInstall.installProxy != null) {
                Main.currentLineNo++;
                MainIAInstall.showProgress(MainIAInstall.installProxy, Main.currentLineNo, "Starting Install phase...", buildEvent.getMessage());
            }
        }catch(Exception e){
            e.printStackTrace();;
        }
    }

    @Override
    public void targetFinished(BuildEvent buildEvent) {
        try {
            if (MainIAInstall.installProxy != null) {
                Main.currentLineNo++;
                MainIAInstall.showProgress(MainIAInstall.installProxy, Main.currentLineNo, "Install phase complete.", buildEvent.getMessage());
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void taskStarted(BuildEvent buildEvent) {
        try {
            if (MainIAInstall.installProxy != null) {
                Main.currentLineNo++;
                MainIAInstall.showProgress(MainIAInstall.installProxy, Main.currentLineNo, "Starting task...", buildEvent.getMessage());
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void taskFinished(BuildEvent buildEvent) {
        try {
            if (MainIAInstall.installProxy != null) {
                Main.currentLineNo++;
                MainIAInstall.showProgress(MainIAInstall.installProxy, Main.currentLineNo, "Task complete.", buildEvent.getMessage());
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void messageLogged(BuildEvent buildEvent) {
        try {
            if (MainIAInstall.installProxy != null) {
                Main.currentLineNo++;
                MainIAInstall.showProgress(MainIAInstall.installProxy, Main.currentLineNo, "Running installation...", buildEvent.getMessage());
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
