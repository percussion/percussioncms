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

package com.percussion.servlets;

import com.percussion.server.PSServer;
import com.percussion.services.PSBaseServiceLocator;
import com.percussion.utils.jndi.PSJndiObjectLocator;
import com.percussion.utils.servlet.PSServletUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoaderListener;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

/**
 * This listener will start up the spring portion of
 * the server.
 * <p>
 * See spring doc.
 * @author adamgent
 */
public class PSContextLoaderListener extends ContextLoaderListener {


    public void contextDestroyed(ServletContextEvent event) {
        PSServer.shutdown();
        super.contextDestroyed(event);
    }

    public void contextInitialized(ServletContextEvent event) {

        ServletContext servletContext = event.getServletContext();
        ms_log.info("Initializing Root Web Application Context");
        // Setup rhythmyx dir information
        /*
        String rxDirParam = servletContext.getInitParameter("rxDir");
        final File apath;


        if (rxDirParam != null && rxDirParam.trim().length() > 0)
        {
            apath = new File(rxDirParam);
        }
        else
        {
            File spath = new File(servletContext.getRealPath("."));
            apath = spath.getParentFile().getParentFile();
        }


        PSEntityResolver.setResolutionHome(apath);
       */
        // initialize jndi prefix
        String jndiLookupPrefix = servletContext.getInitParameter("jndiPrefix");
        PSJndiObjectLocator.setPrefix(jndiLookupPrefix);
        PSServletUtils.initialize(servletContext);
        super.initWebApplicationContext(servletContext);
    }

    @Override
    protected ApplicationContext loadParentContext(ServletContext servletContext) {
        ms_log.info("Loading Service locators");
        PSBaseServiceLocator.init(servletContext);
        ms_log.info("Finished loading service locators");
        return PSBaseServiceLocator.getCtx();
    }
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Logger ms_log = LogManager.getLogger(PSContextLoaderListener.class);

}
