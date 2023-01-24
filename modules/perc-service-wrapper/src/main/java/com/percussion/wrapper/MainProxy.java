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


import com.percussion.error.PSExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.UnknownHostException;

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
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
        }
    }

    public StartArgsProxy processCommandLine(String[] args)
    {

        try {
            Method proceesCommand = main.getClass().getMethod("processCommandLine",args.getClass());
            return new StartArgsProxy(proceesCommand.invoke(main,new Object[]{args}));
        } catch (IllegalAccessException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
        } catch (InvocationTargetException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
        } catch (NoSuchMethodException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
        }
        return null;
    }

    public void start(StartArgsProxy startArgs)
    {
        try {
            Method startCommand = main.getClass().getMethod("start",startArgs.getInstance().getClass());
            startCommand.invoke(main,new Object[]{startArgs.getInstance()});
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
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
