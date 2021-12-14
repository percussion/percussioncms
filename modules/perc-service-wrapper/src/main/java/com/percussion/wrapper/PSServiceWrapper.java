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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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


