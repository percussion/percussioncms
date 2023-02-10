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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;

/***
 * Listens for ant build output and passes the messages to the Installer if running
 * in installer mode.  For dev installs does nothing.
 *
 * See https://ant.apache.org/manual/develop.html#buildevents
 */
public class AntBuildListener implements BuildListener {

    private static final Logger log = LogManager.getLogger(AntBuildListener.class);


    @Override
    public void buildStarted(BuildEvent buildEvent) {
        try {
            if (MainIAInstall.installProxy != null) {
                Main.currentLineNo.getAndIncrement();
                MainIAInstall.showProgress(MainIAInstall.installProxy, Main.currentLineNo.get(), "Installing files...", buildEvent.getMessage());
            }
        }catch(Exception e){
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
        }
    }

    @Override
    public void buildFinished(BuildEvent buildEvent) {
        try {
            if (MainIAInstall.installProxy != null) {
                Main.currentLineNo.getAndIncrement();
                MainIAInstall.showProgress(MainIAInstall.installProxy, Main.currentLineNo.get(), "Installation complete.", buildEvent.getMessage());
            }
        }catch(Exception e){
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
        }
    }

    @Override
    public void targetStarted(BuildEvent buildEvent) {
        try {
            if (MainIAInstall.installProxy != null) {
                Main.currentLineNo.getAndIncrement();
                MainIAInstall.showProgress(MainIAInstall.installProxy, Main.currentLineNo.get(), "Starting Install phase...", buildEvent.getMessage());
            }
        }catch(Exception e){
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
        }
    }

    @Override
    public void targetFinished(BuildEvent buildEvent) {
        try {
            if (MainIAInstall.installProxy != null) {
                Main.currentLineNo.getAndIncrement();
                MainIAInstall.showProgress(MainIAInstall.installProxy, Main.currentLineNo.get(), "Install phase complete.", buildEvent.getMessage());
            }
        }catch(Exception e){
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
        }
    }

    @Override
    public void taskStarted(BuildEvent buildEvent) {
        try {
            if (MainIAInstall.installProxy != null) {
                Main.currentLineNo.getAndIncrement();
                MainIAInstall.showProgress(MainIAInstall.installProxy, Main.currentLineNo.get(), "Starting task...", buildEvent.getMessage());
            }
        }catch(Exception e){
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
        }
    }

    @Override
    public void taskFinished(BuildEvent buildEvent) {
        try {
            if (MainIAInstall.installProxy != null) {
                Main.currentLineNo.getAndIncrement();
                MainIAInstall.showProgress(MainIAInstall.installProxy, Main.currentLineNo.get(), "Task complete.", buildEvent.getMessage());
            }
        }catch(Exception e){
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
        }
    }

    @Override
    public void messageLogged(BuildEvent buildEvent) {
        try {
            if (MainIAInstall.installProxy != null) {
                Main.currentLineNo.getAndIncrement();
                MainIAInstall.showProgress(MainIAInstall.installProxy, Main.currentLineNo.get(), "Running installation...", buildEvent.getMessage());
            }
        }catch(Exception e){
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
        }
    }
}
