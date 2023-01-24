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
