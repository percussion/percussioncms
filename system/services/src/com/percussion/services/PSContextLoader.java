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

package com.percussion.services;

import com.percussion.server.PSServer;
import com.percussion.servlets.PSContextLoaderListener;
import com.percussion.utils.io.PathUtils;
import com.percussion.utils.jndi.PSJndiObjectLocator;
import com.percussion.utils.servlet.PSServletUtils;
import com.percussion.utils.xml.PSEntityResolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;

/**
 * 
 * The context loader loads the root spring context which is
 * first setup by {@link PSBaseServiceLocator#init(ServletContext)}.
 * <p>
 * The loader is kicked off by {@link PSContextLoaderListener}.
 * 
 * @author adamgent
 *
 */
public class PSContextLoader extends ContextLoader
{

   
   /**
    * Shuts down the server and the spring context. See spring doc.
    * @param servletContext The base servlet context.
    */
   @Override
   public void closeWebApplicationContext(ServletContext servletContext)
   {
      PSServer.shutdown();
      super.closeWebApplicationContext(servletContext);
   }

   /**
    * Initialiazes part of the server and then initializes spring. 
    * See spring doc.
    * @param servletContext The base servlet context.
    * @return the root web application context.
    */
   @Override
   public WebApplicationContext initWebApplicationContext(ServletContext servletContext)
         throws IllegalStateException, BeansException
   {
      log.info("Initializing Root Web Application Context");

      PSServer.setRxDir(PathUtils.getRxDir());
      PSEntityResolver.setResolutionHome(PathUtils.getRxDir());

      // initialize jndi prefix
      String jndiLookupPrefix = servletContext.getInitParameter("jndiPrefix");
      PSJndiObjectLocator.setPrefix(jndiLookupPrefix);
      PSServletUtils.initialize(servletContext);

      WebApplicationContext context = super.initWebApplicationContext(servletContext);
      log.info("Finished loading spring");
      return context;
   }

   /**
    * Delegates to PSBaseServiceLocator.
    * @param servletContext Base servlet context.
    */
   @Override
   protected ApplicationContext loadParentContext(ServletContext servletContext)
         throws BeansException
   {
      log.info("Loading Service locators");
      PSBaseServiceLocator.init(servletContext);
      log.info("Finished loading service locators");
      return PSBaseServiceLocator.getCtx();
   }

   /**
    * The log instance to use for this class, never <code>null</code>.
    */
   private static final Log log = LogFactory.getLog(PSContextLoader.class);

}
