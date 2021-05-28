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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.percussion.wrapper.JettyStartUtils.*;

public abstract class StartWrapper {

    private static final Logger log = LogManager.getLogger(StartWrapper.class);

    protected static final String FS = File.separator;
    protected static final String CPS = System.getProperty("path.separator");
    protected final File rootDir;
    private final Object stateMonitor = new Object();
    protected String name;
    protected String[] args;
    protected boolean active;
    protected boolean isRun = true;
    protected String startupCheckString = null;
    protected int shutdownPort;
    protected String stopKey = "SHUTDOWN";
    protected int pid;
    protected int port;
    protected int startTimeout = 240;
    protected int stopTimeout;
    protected String stopKeySuffix = "";
    protected String stopResponse = null;
    protected File stateFile;
    protected File currentDirectory;
    protected boolean writeToStateFile = true;
    protected ProcState state = ProcState.STOPPED;
    private StateFileWatcher stateFileWatcher = null;
    private String[] startCmd;

    public StartWrapper(String name, File rootDir, String[] args) {
        this.name = name;
        this.rootDir = rootDir;
        this.currentDirectory = rootDir;
        this.args = args;
    }

    public File getCurrentDirectory() {
        return currentDirectory;
    }

    public void setCurrentDirectory(File currentDirectory) {
        this.currentDirectory = currentDirectory;
    }

    public File getRootDir() {
        return rootDir;
    }

    public String getName() {
        return name;
    }

    public String[] getArgs() {
        return args;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        if (active == false)
            state = ProcState.NOT_INSTALLED;
        this.active = active;
    }

    public int getShutdownPort() {
        return shutdownPort;
    }

    public void setShutdownPort(int shutdownPort) {
        this.shutdownPort = shutdownPort;
    }

    public boolean isRun() {
        return isRun;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int updatePid() {
        pid = getRunningPid(port);
        return pid;
    }

    public String[] getStartCmd() {
        return startCmd;
    }

    public void setStartCmd(String[] startCmd) {
        this.startCmd = startCmd;
    }

    public String getStopKeySuffix() {
        return stopKeySuffix;
    }

    public void setStopKeySuffix(String stopKeySuffix) {
        this.stopKeySuffix = stopKeySuffix;
    }

    public String getStartupCheckString() {
        return startupCheckString;
    }

    public void setStartupCheckString(String startupCheckString) {
        this.startupCheckString = startupCheckString;
    }

    public void setStateFile(File stateFile) {
        this.stateFile = stateFile;
    }

    public String getStopResponse() {
        return stopResponse;
    }

    public void setStopResponse(String stopResponse) {
        this.stopResponse = stopResponse;
    }

    public String getStopKey() {
        return stopKey;
    }

    public void setStopKey(String stopKey) {
        this.stopKey = stopKey;
    }

    public boolean isWriteToStateFile() {
        return writeToStateFile;
    }

    public void setWriteToStateFile(boolean writeToStateFile) {
        this.writeToStateFile = writeToStateFile;
    }

    protected void initState() {
        updatePid();
        loadStateFile();
        if (state == ProcState.STARTING) {
            stateFileWatcher = new StateFileWatcher(stateFile);
            stateFileWatcher.start();
        }
    }

    protected void setState(ProcState toState) {
        debug("Setting State method");
        setState(toState, writeToStateFile);
    }

    protected void setState(ProcState toState, boolean write) {

        synchronized (stateMonitor) {

            if (state != toState) {
                debug("Setting %s state from %s to %s", name, state, toState);
                state = toState;
                if (write) {
                    writeStateToFile(toState);
                }
                if (state == ProcState.STARTING) {
                    stateFileWatcher = new StateFileWatcher(stateFile);
                    stateFileWatcher.start();
                }
                stateMonitor.notifyAll();
            }
        }
    }

    private void writeStateToFile(ProcState toState) {
        try (PrintWriter out = new PrintWriter(stateFile, StandardCharsets.UTF_8.name())) {
            debug("Writing state %s to file %s", this.toString(), stateFile.toString());
            out.println(String.format("%s %s", toState, this.toString()));
        } catch (FileNotFoundException e) {
            error("Problem setting state to state %s in file %s", toState, stateFile.getAbsolutePath());
        } catch (UnsupportedEncodingException e) {
            // Not going to happen for UTF-8
        }
    }

    protected boolean waitForStart() {
        synchronized (stateMonitor) {
            try {
                stateMonitor.wait(this.startTimeout * 1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return (state == ProcState.STARTED);
    }


    public void loadStateFile() {
        String lastLine = "";
        ProcState returnState = ProcState.STOPPED;

        if (!stateFile.exists()) {
            debug("State file did not exist. Setting state to STOPPED.");
            setState(ProcState.STOPPED);
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(stateFile))) {
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                if (sCurrentLine.trim().length() > 0)
                    lastLine = sCurrentLine;
            }
        } catch (FileNotFoundException e) {
            debug("Cannot find state file ", e);
        } catch (IOException e) {
            debug("Cannot read state file ", e);
        }
        if (lastLine.length() > 0) {
            String fileState = lastLine.split(" ")[0];
            returnState = ProcState.valueOf(fileState);
        }

        if (returnState != ProcState.STOPPED && pid <= 0) {
            debug("Process not running for port %s but state file %s shows state %s process must have been killed", port, stateFile.getName(), returnState);
            returnState = ProcState.STOPPED;
        }
        // do not re write to file
        setState(returnState, false);
    }

    public void startServer() {
        if (state == ProcState.STOPPED) {
            info("Starting %s with http port %s", name, port);
            if (stateFile.exists())
                stateFile.delete();
            setState(ProcState.STARTING);

            try {
                debug("The commands being used to start the server are: %s", Arrays.toString(startCmd));
                final Process tomcatProc = Runtime.getRuntime().exec(startCmd, null, currentDirectory);
                try(InputStream is = tomcatProc.getInputStream()) {
                    new PSStreamGobbler(name,is ).start();
                    if (startupCheckString != null) {
                        try(InputStream eis = tomcatProc.getErrorStream() ) {
                            new PSStreamGobbler(name, eis, startupCheckString, () -> setState(ProcState.STARTED)).start();
                        }
                    } else {
                        try(InputStream eis = tomcatProc.getErrorStream() ) {
                            new PSStreamGobbler(name, eis).start();
                        }
                    }
                }
                updatePid();

                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    debug(" In shutdown hook, shutting down %s", name);
                    // reload state process could be killed from other wrapper
                    loadStateFile();
                    if (state != ProcState.STOPPING || state != ProcState.STOPPED)
                        stopServer();
                }));

                monitorNewProcess(tomcatProc);

            } catch (IOException e) {
                error("Cannot start %", e, name);
                System.exit(1);
            }
        } else {
            // NOTE: this text is grepped in install-dts-service.sh
            info("%s Server with http port %s is already running with process id %s state=%s", name, port, pid, state);
        }
    }

    public void stopServer() {
        stopServer(false);
    }

    public void stopServer(boolean force) {

        updatePid();
        if (state == ProcState.STOPPED) {
            info("%s already stopped", name);
        } else if (state == ProcState.STOPPING) {
            info("%s  already stopping", name);
        } else if (state == ProcState.STARTING) {
            info("%s Waiting to start before stopping", name);
            waitForStart();
        }

        if (state == ProcState.STARTED) {
            this.stop();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            updatePid();
            if (pid > 1) {
                if (force) {
                    JettyStartUtils.killProcess(pid);
                    info("Server not stopped on port %s with pid %s Killing process", port, pid);
                } else
                    info("Server not stopped on port %s with pid %s use --force option to kill", port, pid);
                updatePid();
            }
            loadStateFile();
        }
    }


    public boolean stop() {
        if (pid > 0) {
            setState(ProcState.STOPPING);
            info("Stopping %s with http port %s and shutdown port %s", name, port, shutdownPort);
            try (Socket s = new Socket()) {
                s.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), shutdownPort), 2000);
                if (stopTimeout > 0) {
                    s.setSoTimeout(stopTimeout * 1000);
                }

                try (OutputStream out = s.getOutputStream()) {
                    out.write((stopKey + stopKeySuffix).getBytes());
                    out.flush();
                } catch (SocketTimeoutException e) {
                    error("Timeout on connection to shutdown port");
                } catch (IOException e) {
                    error("Error connecting to shutdown port", e);
                }

                if (stopTimeout > 0 && stopResponse != null) {
                    info("Waiting for stop response '%s'", stopResponse);
                    try(InputStreamReader isr = new InputStreamReader(s.getInputStream())) {
                        try(LineNumberReader lin = new LineNumberReader(isr)) {
                            String response;
                            while ((response = lin.readLine()) != null) {

                                // "Stopped" for jetty
                                if (stopResponse.equals(response)) {
                                    info("Server reports itself as Stopped");
                                    return true;
                                } else {
                                    debug("Received \"%s\"", response);
                                }
                            }
                        }
                    }
                }
            } catch (SocketTimeoutException e) {
                error("Timeout on connection to shutdown port");
            } catch (SocketException e) {
                error("Error connecting to shutdown port", e);
            } catch (UnknownHostException e) {
                error("Error connecting to shutdown port", e);
            } catch (IOException e) {
                error("Error connecting to shutdown port", e);
            }

            try {
                updatePid();
                int count = 20;
                while (pid > 0 && count > 0) {
                    Thread.sleep(2000);
                    updatePid();
                    count--;
                }
                if (pid > 0) {
                    debug("Timeout waiting for server to stop after request");
                    return false;
                } else {
                    debug("Process ID was not greater than 0, setting state to STOPPED.");
                    setState(ProcState.STOPPED);
                    return true;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                debug("Interrupted exception waiting for server to stop");
            }

        }
        return false;
    }

    protected void monitorNewProcess(Process tomcatProc) {

        Thread t = new Thread(() -> {
            boolean finished = false;
            while (!finished) {
                try {
                    finished = tomcatProc.waitFor(5, TimeUnit.SECONDS);
                    if (!finished) {
                        debug("Not finished");
                    } else {
                        int exitValue = tomcatProc.exitValue();

                        info("DTS stopped with code %s", exitValue);
                        setState(ProcState.STOPPED);
                    }
                } catch (InterruptedException e) {
                    debug("DTS interrupted with code %s", tomcatProc.exitValue());
                    setState(ProcState.STOPPED);
                    Thread.currentThread().interrupt();
                }
            }
        });

    }

    private class StateFileWatcher extends Thread {

        Path myDir;
        WatchService watcher;
        String fileToCheck;

        StateFileWatcher(File file) {
            try {
                // do not wait on this thread
                this.setDaemon(true);
                myDir = Paths.get(file.getParent());
                fileToCheck = file.getName();
                watcher = myDir.getFileSystem().newWatchService();
                myDir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
            } catch (Exception e) {
                log.error(e.getMessage());
                log.debug(e.getMessage(), e);
            }
        }

        public void run() {
            while (true) {
                try {
                    WatchKey watchKey = watcher.take();
                    List<WatchEvent<?>> events = watchKey.pollEvents();
                    Path dir = (Path) watchKey.watchable();
                    for (WatchEvent<?> event : events) {
                        Path changed = (Path) event.context();
                        if (changed.toString().equals(fileToCheck)) {
                            loadStateFile();
                        }

                    }
                    watchKey.reset();
                } catch (Exception e) {
                    error("Error: %s", e, e.toString());
                }
            }
        }
    }

    /**
     * Prints the status of the services on the machine.  Called via the --status
     * flag in the command-line arguments.
     * <br/><br/>
     * Note: The order of the following output is used by the percussion-dts.sh file.
     * Changing the order of the output would require a change of the 'grep' and 'sed'
     * commands in that file.
     */
    public void printStateString() {
        if (state == ProcState.NOT_INSTALLED) {
            info("%s is not installed", name);
        } else if (state == ProcState.STOPPED) {
            info("%s is %s and is configured with port %s", name, state, port);
        } else {
            info("%s is %s, configured with port: %s, and running on process id: %s", name, state, port, pid);
        }
    }
}
