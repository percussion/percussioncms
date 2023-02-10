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


package com.percussion.wrapper;

import com.percussion.i18n.rxlt.PSRxltMain;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.percussion.wrapper.JettyStartUtils.debug;
import static com.percussion.wrapper.JettyStartUtils.error;
import static com.percussion.wrapper.JettyStartUtils.outputHelp;

/**
 * Wrapper for Service/Daemon
 *
 * @author luisteixeira
 */
public class PSServiceWrapper {
    private static final String FS = File.separator;
    private static final String JETTY_BASE = "jetty" + FS + "base";
    private static final String DTS_ROOT = "Deployment";
    private static final String DTS_STAGING_ROOT = "Staging" + FS + "Deployment";
    private static final String STAGING = "Staging";
    private static boolean isStagingOnly = false;
    private static File rxDir = null;

    /**
     * Entry point for Service/Daemon.  Will start the following in this order:
     * <p>
     * 1. Derby Database
     * 2. Tomcat
     * 3. Server
     * <p>
     * If Derby doesn't start it will fail.
     */
    @SuppressFBWarnings("INFORMATION_EXPOSURE_THROUGH_AN_ERROR_MESSAGE")
    public static void main(String[] args) {

        rxDir = JettyStartUtils.locateRxDir();

        String dirName = rxDir.getName();
        if (STAGING.equals(dirName)) {
            isStagingOnly = true;
        }

        if (rxDir == null) {
            error("Cannot locate Percussion Folder from jar.");
            System.exit(1);
        }

        PercArgs percArgs = new PercArgs(args);

        // Handle Help text
        if (percArgs.isHelp()) {
            outputHelp();
            System.exit(0);
        }

        //Rxlt Tool Invoker
        if(percArgs.isRxltTool()){
            PSRxltMain.main(percArgs.getFilteredArgs());
            System.exit(0);
        }

        JettyStartUtils.setDebugEnabled(percArgs.isDebugStartup());

        debug("isStagingOnly directory: %s", isStagingOnly);

        String[] filteredArgs = percArgs.getFilteredArgs();

        System.setProperty("user.dir", new File(rxDir, JETTY_BASE).getAbsolutePath());

        JettyStartUtils.info("Deploy directory is %s", rxDir.getAbsolutePath());

        // Jetty Commands
        StartWrapper jettyWrapper = new JettyStartWrapper("Jetty", rxDir, filteredArgs);

        // Production DTS Commands
        File dtsRoot = new File(rxDir, DTS_ROOT);
        StartWrapper dtsWrapper = new DtsStartWrapper("Production DTS", dtsRoot, filteredArgs);

        // Staging DTS Commands
        // Using DTS_ROOT here as a hack to get around the fact that the
        // service-wrapper needs to live in the Staging subdirectory.
        File stagingDtsRoot = null;
        if (isStagingOnly) {
            stagingDtsRoot = new File(rxDir.getParentFile(), DTS_STAGING_ROOT);
        } else {
            stagingDtsRoot = new File(rxDir, DTS_STAGING_ROOT);
        }
        debug("The staging DTS Root is: %s", stagingDtsRoot.getAbsolutePath());
        StartWrapper stagingDtsWrapper = new DtsStartWrapper("Staging DTS", stagingDtsRoot, filteredArgs);

        List<StartWrapper> wrapperList = new ArrayList<>();

        // If this is a staging-only directory, we need to not
        // display the output for the other services.  Done because the
        // production DTS output is getting mixed up with the staging output.
        if (isStagingOnly) {
            wrapperList.add(stagingDtsWrapper);
        } else {
            wrapperList.add(jettyWrapper);
            wrapperList.add(dtsWrapper);
            wrapperList.add(stagingDtsWrapper);
        }

        if (percArgs.isStatus()) {
            wrapperList.stream().forEach(StartWrapper::printStateString);
            System.exit(0);
        }

        // start and stop processes
        if (jettyWrapper.isActive()) {
            debug("Jetty Server Found");
            if (percArgs.isStopServer()) {
                jettyWrapper.stopServer(percArgs.isForce());
            }

            if (percArgs.isStartServer()) {
                jettyWrapper.startServer();
            }
        }

        if (dtsWrapper.isActive() && !isStagingOnly) {
            debug("Production DTS Server Found");
            if (percArgs.isStopDTS())
                dtsWrapper.stopServer(percArgs.isForce());
            if (percArgs.isStartDTS())
                dtsWrapper.startServer();
        }

        if (stagingDtsWrapper.isActive()) {
            debug("Staging DTS Server Found");
            if (percArgs.isStopStagingDTS())
                stagingDtsWrapper.stopServer(percArgs.isForce());
            if (percArgs.isStartStagingDTS())
                stagingDtsWrapper.startServer();
        }


    }
}  


