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

import java.io.*;
import java.util.*;

import static com.percussion.wrapper.JettyStartUtils.info;
import static com.percussion.wrapper.JettyStartUtils.loadProperties;

public class JettyStartWrapper extends StartWrapper {
    private static final String FS = File.separator;
    private static final String JETTY_ROOT = "jetty";
    private static final String JETTY_BASE = JETTY_ROOT + FS + "base";
    private static final String JETTY_HOME = JETTY_ROOT + FS + "upstream";
    private static final String JETTY_DEFAULTS = JETTY_ROOT + FS + "defaults";
    private static final String JETTY_START_JAR = JETTY_HOME + FS + "start.jar";
    private static final String JETTY_STATE_FILE = "jetty.state";

    MainProxy mainProxy;

    private StartArgsProxy startArgs = null;

    public JettyStartWrapper(String name, File rootDir, String[] args) {
        super(name,rootDir,args);

        File startJar = new File(rootDir, JETTY_START_JAR);
        if (startJar.exists()) {
            setActive(true);
            File baseDir = new File(rootDir,JETTY_BASE);
            setCurrentDirectory(baseDir);
            setStateFile(new File(baseDir,JETTY_STATE_FILE));
            setStopKeySuffix("\r\nstop\r\n");
            setStopResponse("Stopped");

            // jetty writes state file
            setWriteToStateFile(false);
            File installationProps = new File(rootDir,"jetty"+File.separator+"base"+File.separator+"etc"+File.separator+"installation.properties");
            if (!installationProps.exists())
                throw new IllegalArgumentException("Cannot find properties file "+installationProps.getAbsolutePath());

            Properties prop = loadProperties(installationProps);
            port = Integer.parseInt(prop.getProperty("jetty.http.port","9992"));
            shutdownPort = Integer.parseInt(prop.getProperty("shutdown.port","8887"));
            stopKey = prop.getProperty("shutdown.key","SHUTDOWN");

            initState();

            mainProxy = new MainProxy(startJar);

            setArgs(overrideStartAgs(args));

            startArgs = mainProxy.processCommandLine(getArgs());

            isRun = startArgs.isRun();

            List<String> commandArgs = startArgs.getMainArgs();
            setStartCmd( commandArgs.toArray(new String[commandArgs.size()]));

        }
        else
        {
            state = ProcState.NOT_INSTALLED;
            info("Jetty server is not installed");
        }

    }

    public void callJettyCommand() {
        mainProxy.start(startArgs);
    }

    private String[] overrideStartAgs(String[] args) {
        LinkedHashSet<String> argsList = new LinkedHashSet<>(Arrays.asList(args));
        HashSet<String> argMap = new HashSet<>();
        for (String arg : argsList)
        {
            argMap.add(arg);
        }

        File defaultsFolder = new File(rootDir,JETTY_DEFAULTS);
        File baseDir = new File(rootDir,JETTY_BASE);
        File jettyRoot = new File(rootDir,JETTY_ROOT);
        File jettyHome = new File(rootDir,JETTY_HOME);

        setDefaultArg("-Djetty.http.port", String.valueOf(port), argsList);
        argsList.add("-Djetty.root="+jettyRoot.getAbsolutePath());
        argsList.add("-Djetty.home="+jettyHome.getAbsolutePath());
        argsList.add("-Djetty.base="+baseDir.getAbsolutePath());
        argsList.add("-Duser.dir="+baseDir.getAbsolutePath());
        argsList.add("--include-jetty-dir="+defaultsFolder);
        argsList.add("-Drxdeploydir="+rootDir.getAbsolutePath());

        argsList.add("-Djetty.perc.defaults="+defaultsFolder.getAbsolutePath());
        argsList.add("-DSTOP.PORT="+shutdownPort);
        argsList.add("-DSTOP.KEY="+ stopKey);

        argsList.add("jetty-started.xml");

        return argsList.toArray(new String[argsList.size()]);
    }

    private static void setDefaultArg(String key, String value, LinkedHashSet<String> argsList) {
        if (!argsList.contains(key))
            argsList.add(key+"="+value);
    }

    @Override
    public void startServer() {

        if (!isRun)
        {
            if (pid>0) {
                info("Server with http port %s running with process id %s state=%s",port,pid,state);
            }

            callJettyCommand();
            System.exit(0);

        }
        super.startServer();
    }

}
