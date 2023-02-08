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
package com.percussion.tomcat.valves;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.StringTokenizer;

import javax.servlet.ServletException;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.buf.CharChunk;
import org.apache.tomcat.util.buf.MessageBytes;

/**
 * This class is a very simple redirector, based on the same technique used by
 * the JBoss URL Rewrite valve. The intent is to forward requests from each cm1
 * host to the host that contains all of the delivery tier services, such as the
 * form processor. It does this by checking the list of supplied service names
 * against the path. If the path begins with any registered service name, the
 * request is redirected to the registered targetHost. It does this by making a
 * very low-level call to the adapter's service method, which re-assigns the new
 * host because we reset the server name.
 * <p>
 * The valve supports a couple of attributes:
 * 
 * <pre>
 *      &lt;Valve className=&quot;...&quot; targetHost=&quot;...&quot; serviceNames=&quot;...&quot; /&gt;
 * </pre>
 * 
 * <ol>
 *      <li>targetHost - this should be the name of the &lt;Host&gt; entry in the
 *      server.xml file that contains the delivery side apps. If not provided,
 *      defaults to localhost.</li>
 *      
 *      <li>serviceNames - a comma separated list of all servlet names that provide
 *      delivery side services</li>
 * </ol>
 */
public class PSSimpleRedirectorValve extends ValveBase implements Lifecycle
{
    private static final Logger log = LogManager.getLogger(PSSimpleRedirectorValve.class);

    /**
     * See class description.
     */
    private String targetHost = "localhost";

    /**
     * A temporary storage place for data used to generate the
     * {@link #servletUrls} member. Set to <code>null</code> when finished.
     */
    private String serviceNames;

    /**
     * All the services that we need to redirect for, with leading and trailing
     * slashes. Never <code>null</code> after {@link #start()} has been
     * called.
     */
    private String[] servletUrls;
    
    boolean started;
    
    public boolean isStarted()
    {
        return started;
    }

    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException
    {
        boolean matched = false;
        if (started && !targetHost.equals(request.getServerName()))
        {
            MessageBytes path = request.getRequestPathMB();
            boolean found = false;
            for (int i = 0; !found && i < servletUrls.length; i++)
            {
                if (path.startsWithIgnoreCase(servletUrls[i],0))
                {
                    matched = true;
                    CharChunk chunk = request.getCoyoteRequest().serverName().getCharChunk();
                    chunk.recycle();
                    chunk.append(targetHost);
                    request.getMappingData().recycle();
                    try
                    {
                        request.getConnector().getProtocolHandler().getAdapter().service(request.getCoyoteRequest(),
                                response.getCoyoteResponse());
                    }
                    catch (Exception e)
                    {
                        //will be handled higher up the stack
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        if (!matched)
            getNext().invoke(request, response);
    }

    /**
     * Provided to allow Tomcat to set this property from the attribute in
     * server.xml.
     * 
     * @param targetHost If <code>null</code> or empty, the property is not
     * set.
     */
    public void setTargetHost(String targetHost)
    {
        if (targetHost != null && targetHost.trim().length() > 0)
            this.targetHost = targetHost;
    }

    /**
     * Provided to allow Tomcat to set this property from the attribute in
     * server.xml.
     * 
     * @param serviceNames If <code>null</code> or empty, the property is not
     * set.
     */
    public void setServiceNames(String serviceNames)
    {
        if (serviceNames != null && serviceNames.trim().length() > 0)
            this.serviceNames = serviceNames;
    }

    /**
     * Performs some initialization.
     * @throws LifecycleException 
     */
    @Override
    public void startInternal() throws LifecycleException
    {
        log.info("Starting Simple Redirector valve");
        if (serviceNames == null)
        {
            servletUrls = new String[0];
            return;
        }

        StringTokenizer toker = new StringTokenizer(serviceNames, ",");
        Collection<String> urls = new ArrayList<>();
        while (toker.hasMoreTokens())
        {
            String s = toker.nextToken().trim();
            if (!s.startsWith("/"))
                s = "/" + s;
            if (!s.endsWith("/"))
                s = s + "/";
            urls.add(s);
        }
        servletUrls = urls.toArray(new String[0]);
        log.info("   Redirecting to " + targetHost + " for the following paths: " + Arrays.toString(servletUrls));
        serviceNames = null;
        
        started = true;
        if (getContainer()!=null)
            setState(LifecycleState.STARTING);
    }
    
    @Override
    public void stopInternal() throws LifecycleException
    {
        started=false;
        if (getContainer()!=null)
            setState(LifecycleState.STOPPING);
    }
    
    
    
}
