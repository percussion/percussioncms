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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Properties;

public class JettyStartUtils {

    private static final Logger log = LogManager.getLogger(JettyStartUtils.class);

    public static final String USAGE_RESOURCE_PATH = "usage.txt";
    private static final String JAVA_PROPS_PATH = "java.properties";
    private static boolean debugEnabled = false;

    private static PrintStream logOut = System.out;
    private static PrintStream logErr = System.err;

    private static Properties javaProps = new Properties();

    private static String OS = System.getProperty("os.name").toLowerCase();

    /**
     * Load java.properties from root directory
     */
    static {
        File rxDir = locateRxDir();
        File javaPropsFile = new File(rxDir.getAbsolutePath() + "/" + JAVA_PROPS_PATH);

        if (javaPropsFile.exists() && javaPropsFile.isFile()) {
            try (InputStream is = new FileInputStream(javaPropsFile)) {
                javaProps.load(is);
            } catch (FileNotFoundException fileNotFoundException) {
                error("Could not find resource: %s", fileNotFoundException, JAVA_PROPS_PATH);
            } catch (IOException ioException) {
                error("IOException loading resource: %s", ioException, JAVA_PROPS_PATH);
            }
        } else {
            error("%s was not found in server directory: %s.", JAVA_PROPS_PATH, rxDir);
        }
    }

    // from https://stackoverflow.com/questions/434718/sockets-discover-port-availability-using-java
    static int getRunningPid(int port) {
        int pid = -1;
        String windowsCmd = "wmic process where \"CommandLine like '%http.port=" + port + " %' and name='java.exe'\" get Name,ProcessId";
        String[] linuxCmd = {"/bin/sh", "-c", "ps -ef | grep \"\\http.port=" + port + " \" | grep -v grep | awk '{print $2}'"};

        Object command = isWindows() ? windowsCmd : linuxCmd;
        try {
            Process proc = null;
            if (isWindows())
                proc = Runtime.getRuntime().exec(windowsCmd);
            else
                proc = Runtime.getRuntime().exec(linuxCmd);

            // process the response
            String line = "";
            String errorLine = "";
            try (BufferedReader error = new BufferedReader(new InputStreamReader(proc.getErrorStream()))){
                 try(BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()))){
                        while ((errorLine = error.readLine()) != null) {
                            debug("get pid error line=%s", errorLine);
                        }
                        while ((line = input.readLine()) != null) {
                            String idString = line;
                            if (idString.startsWith("java.exe")) {
                                idString = line.split("java.exe")[1];
                            }
                            try {
                                pid = Integer.parseInt(idString.trim());
                                break;
                            } catch (NumberFormatException | NullPointerException e) {
                                continue;
                            }
                        }
                    }
                }
            } catch (IOException e) {
                log.error(e.getMessage());
                log.debug(e.getMessage(), e);
            }

        return pid;
    }

    static void killProcess(int pid) {
        System.out.println("Killing process " + pid);

        String cmd = isWindows() ? "taskkill /F /T /PID " + pid : "kill -9 " + pid;

        Process process;
        int exitCode = -1;
        try {
            process = Runtime.getRuntime().exec(cmd);
            exitCode = process.waitFor();
        } catch (IOException e) {
            System.out.println("Error cannot kill process with command :" + cmd);
            System.out.println("exit code " + exitCode);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

    }

    static Properties loadProperties(File file) {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(file)) {
            prop.load(input);
        } catch (IOException ex) {
            log.error(ex.getMessage());
            log.debug(ex.getMessage(), ex);
            System.exit(1);
        }
        return prop;
    }

    public static boolean isWindows() {
        return (OS.indexOf("win") >= 0);
    }

    static File locateRxDir(File f) {
        if (f == null)
            return null;
        if (f.isFile())
            return locateRxDir(f.getParentFile());
        else if (f.isDirectory()) {
            File configDir = new File(f, "rxconfig");
            if (configDir.exists())
                return f;
            else
                return locateRxDir(f.getParentFile());
        }
        return null;
    }

    public static File locateRxDir() {
        String deploydir = System.getProperty("rxdeploydir");

        if (deploydir == null)
            deploydir = System.getProperty("rxDir");

        if (deploydir != null)
            return new File(deploydir);

        debug("No rxdeploydir system property set calculating from start jar location");

        try {
            final File f = new File(PSServiceWrapper.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            return locateRxDir(f);
        } catch (URISyntaxException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
            System.exit(1);
        }
        return null;
    }

    public static void outputHelp() {
        try (InputStream is = JettyStartUtils.class.getResourceAsStream(USAGE_RESOURCE_PATH)) {
            byte[] buf = new byte[1024];
            int nr = is.read(buf);
            while (nr != -1) {
                System.out.write(buf, 0, nr);
                nr = is.read(buf);
            }
        } catch (IOException e) {
            error("Cannot find help resource in classpath for %s", e, USAGE_RESOURCE_PATH);
        }
    }

    /**
     * Returns the value of the requested property from the root
     * java.properties file.
     *
     * @param name the property to retrieve from the file.
     * @return the value of the requested property, may be <code>empty</code>,
     * never <code>null</code>.
     */
    public static String getJavaProperty(String name) {
        return javaProps.getProperty(name, "");
    }

    /**
     * Returns the java.properties file from the system root.
     * @return Server's root java.properties file if it exists, otherwise
     * returns an empty Properties object.
     */
    public static Properties getJavaProperties() {
        return javaProps == null ? new Properties() : javaProps;
    }

    public static void debug(String s, Object... args) {
        if (debugEnabled)
            logOut.println(String.format(s, args));
    }

    public static void debug(String s, Throwable t, Object... args) {
        if (debugEnabled) {
            logOut.println(String.format(s, args));
            log.error(logErr);
            log.debug(logErr);
        }
    }

    public static void error(String s, Object... args) {
        logErr.println(String.format(s, args));
    }

    public static void error(String s, Throwable t, Object... args) {
        logErr.println(String.format(s, args));
        log.error(logErr);
        log.debug(logErr);
    }

    public static void info(String s, Object... args) {
        logOut.println(String.format(s, args));
    }

    public static boolean isDebugEnabled() {
        return debugEnabled;
    }

    public static void setDebugEnabled(boolean debugEnabled) {
        JettyStartUtils.debugEnabled = debugEnabled;
    }


    public static PrintStream getLogOut() {
        return logOut;
    }

    public static void setLogOut(PrintStream logOut) {
        JettyStartUtils.logOut = logOut;
    }

    public static PrintStream getLogerr() {
        return logErr;
    }

    public static void setLogerr(PrintStream logerr) {
        JettyStartUtils.logErr = logerr;
    }

}
