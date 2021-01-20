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

package com.percussion.wrapper;

import java.util.ArrayList;
import java.util.List;

public class PercArgs {
    private boolean startServer;
    private boolean startDTS;
    private boolean startStagingDTS;
    private boolean stopServer;
    private boolean stopDTS;
    private boolean stopStagingDTS;
    private boolean status;
    private boolean force;
    private boolean help;
    private boolean debugStartup;
    private boolean  rxltTool;

    private List<String> filteredArgs = new ArrayList<>();



    public PercArgs(String[] args) {
        boolean foundArg = false;

        for (String arg : args) {
            switch (arg) {
                case "--help":
                    help = true;
                case "--jettyHelp":
                    filteredArgs.add("--help");
                case "--debugWrapper":
                    debugStartup = true;
                case "--force":
                    force = true;
                case "--start":
                    foundArg = true;
                    startServer = true;
                    startDTS = true;
                    startStagingDTS = true;
                    break;
                case "--startServer":
                    foundArg = true;
                    startServer = true;
                    break;
                case "--startDTS":
                    foundArg = true;
                    startDTS = true;
                    break;
                case "--startStagingDTS":
                    foundArg = true;
                    startStagingDTS = true;
                    break;
                case "--stop":
                    foundArg = true;
                    stopServer = true;
                    stopDTS = true;
                    stopStagingDTS = true;
                    break;
                case "--stopServer":
                    foundArg = true;
                    stopServer = true;
                    break;
                case "--stopDTS":
                    foundArg = true;
                    stopDTS = true;
                    break;
                case "--stopStagingDTS":
                    foundArg = true;
                    stopStagingDTS = true;
                    break;
                case "--status":
                    foundArg = true;
                    status = true;
                    break;
                case "--rxlt":
                    foundArg = true;
                    rxltTool = true;
                    break;
                default:
                    filteredArgs.add(arg);
            }
        }
        // default to starting server
        if (!foundArg) {
            help = true;
        }
    }

    public boolean isStartServer() {
        return startServer;
    }

    public boolean isStartDTS() {
        return startDTS;
    }

    public boolean isStartStagingDTS() {
        return startStagingDTS;
    }

    public boolean isStopServer() {
        return stopServer;
    }

    public boolean isStopDTS() {
        return stopDTS;
    }

    public boolean isStopStagingDTS() {
        return stopStagingDTS;
    }

    public boolean isStatus() {
        return status;
    }

    public boolean isRxltTool() {
        return rxltTool;
    }

    /**
     * Converts the list of command-line arguments and converts
     * them to a String array.
     * @return the list of arguments in a String array.
     */
    public String[] getFilteredArgs() {
        return filteredArgs.toArray(new String[filteredArgs.size()]);
    }

    public boolean isDebugStartup() {
        return debugStartup;
    }

    public boolean isForce() {
        return force;
    }

    public boolean isHelp() { return help; }
}
