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

import static com.percussion.wrapper.JettyStartUtils.error;
import static com.percussion.wrapper.JettyStartUtils.debug;
import static com.percussion.wrapper.JettyStartUtils.loadProperties;

public class DtsStartWrapper extends StartWrapper {
    private static final String DTS_PROPERTIES = "conf/perc/perc-catalina.properties";
    private static final String DTS_STATE = "dts.state";
    private static final String TOMCAT_STARTUP_CHECK = "Server startup in";

    public DtsStartWrapper(String name, File rootDir, String[] args) {
        super(name, rootDir, args);

        debug("rootDir in DTSStartWrapper is: %s", rootDir.getAbsolutePath());

        File dtsPropFile = new File(rootDir, DTS_PROPERTIES);
        setStateFile(new File(rootDir, DTS_STATE));
        setStartupCheckString(TOMCAT_STARTUP_CHECK);

        setActive(dtsPropFile.exists());

        if (isActive()) {
            Properties dtsProp = loadProperties(dtsPropFile);
            String dtsPortString = dtsProp.getProperty("http.port");
            String[] cmd = buildTomcatCommand(dtsProp,getArgs());
            debug("Command is :", cmd.toString());
            setStartCmd(cmd);

            if (dtsPortString == null) {
                error("Cannot find http.port in %s", dtsPropFile.getAbsolutePath());
            }
            setPort(Integer.parseInt(dtsPortString));
            String shutdownPortString = dtsProp.getProperty("shutdown.port");
            if (shutdownPortString == null) {
                error("Cannot find shutdown.port in %s", dtsPropFile.getAbsolutePath());
            }
            setShutdownPort(Integer.parseInt(shutdownPortString));

            setStopKey(dtsProp.getProperty("shutdown.key", "SHUTDOWN"));

            initState();
        }
    }

    private static String[] buildTomcatCommand(Properties dtsProp,String[] args) {
        LinkedHashMap<String, String> cmd = new LinkedHashMap<>();
        String java = JettyStartUtils.getJavaProperty("JAVA");
        debug("Java executable is %s", java);

        if ("".equals(java)) {
            java = "java";
        }

        cmd.put(java, "");
        cmd.put("-Dhttp.port", "9980");
        cmd.put("-Dhttps.port", "8443");
        cmd.put("-Dajp.port", "9982");
        cmd.put("-Dhttps.protocols", "TLSv1.2");
        cmd.put("-Dfile.encoding", "UTF-8");
        cmd.put("-Xmx1024m", "");
        cmd.put("-Djava.util.logging.manager", "org.apache.juli.ClassLoaderLogManager");
        cmd.put("-Dnet.sf.ehcache.skipUpdateCheck", "true");
        cmd.put("-Djava.net.preferIPv4Stack", "true");
        cmd.put("-Djava.endorsed.dirs", "Server" + FS + "endorsed");
        cmd.put("-Dcatalina.base", "Server");
        cmd.put("-Dcatalina.home", "Server");
        // cmd.put("-Dshutdown.port","5010");
        cmd.put("-Djava.io.tmpdir", "Server" + FS + "temp");
        cmd.put("-Dderby.system.home", "Server" + FS + "derbydata");

        for (String prop : dtsProp.stringPropertyNames()) {
            String newProp = prop.startsWith("-") ? prop : "-D" + prop;
            cmd.put(newProp, dtsProp.getProperty(prop));
        }

        cmd.put("-cp", "");
        cmd.put("Server" + FS + "bin" + FS + "bootstrap.jar" + CPS + "Server" + FS + "bin" + FS + "tomcat-juli.jar", "");
        cmd.put("org.apache.catalina.startup.Bootstrap", "");
        cmd.put("start", "");

        List<String> commandList = new ArrayList<>();

        for (Map.Entry<String, String> entry : cmd.entrySet()) {
            String item = entry.getKey();
            if (entry.getValue().length() > 0) {
                item = item + "=" + entry.getValue();
            }
            commandList.add(item);
        }

        //Adding other command Line arguments passed in
        if(args != null){
            commandList.addAll(Arrays.asList(args));
        }

        return commandList.toArray(new String[commandList.size()]);
    }
}
