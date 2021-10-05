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
