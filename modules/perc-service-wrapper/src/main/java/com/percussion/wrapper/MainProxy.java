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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;

import static com.percussion.wrapper.JettyStartUtils.debug;
import static com.percussion.wrapper.JettyStartUtils.info;
import static org.eclipse.jetty.start.StartLog.error;

public class MainProxy {

    private static final Logger log = LogManager.getLogger(MainProxy.class);

    private static final String JETTY_START_MAIN_CLASS = "org.eclipse.jetty.start.Main";

    private Object main = null;

    public MainProxy(File startJar){
        URLClassLoader child = null;
        try {
            child = new URLClassLoader(new URL[]{startJar.toURI().toURL()}, MainProxy.class.getClassLoader());
            Thread.currentThread().setContextClassLoader(child);

            Class cls = Class.forName(JETTY_START_MAIN_CLASS, true, child);
            Constructor<?> constructor = cls.getConstructor();
            main = constructor.newInstance();
        } catch (MalformedURLException | ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
        }
    }

    public StartArgsProxy processCommandLine(String[] args)
    {

        try {
            Method proceesCommand = main.getClass().getMethod("processCommandLine",args.getClass());
            return new StartArgsProxy(proceesCommand.invoke(main,new Object[]{args}));
        } catch (IllegalAccessException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
        } catch (InvocationTargetException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
        } catch (NoSuchMethodException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
        }
        return null;
    }

    public void start(StartArgsProxy startArgs)
    {
        try {
            Method startCommand = main.getClass().getMethod("start",startArgs.getInstance().getClass());
            startCommand.invoke(main,new Object[]{startArgs.getInstance()});
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
        }


    }

    public boolean stop(int port,String key, int timeout)
    {
        try (Socket s = new Socket())
        {
            s.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(),port),2000);
            if (timeout > 0)
            {
                s.setSoTimeout(timeout * 1000);
            }

            try (OutputStream out = s.getOutputStream())
            {
                out.write((key + "\r\nstop\r\n").getBytes());
                out.flush();

                if (timeout > 0)
                {
                    info("Waiting %,d seconds for jetty to stop%n",timeout);
                    try(InputStream io = s.getInputStream()) {
                        try(InputStreamReader isr = new InputStreamReader(io)){
                            try(LineNumberReader lin = new LineNumberReader(isr)) {
                                String response;
                                while ((response = lin.readLine()) != null) {
                                    debug("Received \"%s\"", response);
                                    if ("Stopped".equals(response)) {
                                        debug(String.format("Server reports itself as Stopped"));
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (SocketTimeoutException e) {
                error("Timeout on connection to shutdown port");
            }catch (IOException e) {
                error("Error sending stop to jetty shutdown port",e);
            }
        }
        catch (SocketTimeoutException e) {
            error("Timeout on connection to shutdown port",e);
        }
       catch (SocketException e) {
           error("Timeout on connection to shutdown port",e);
        } catch (UnknownHostException e) {
            error("Error sending stop to jetty shutdown port",e);
        } catch (IOException e) {
            error("Error sending stop to jetty shutdown port",e);
        }
        return false;

    }

}
