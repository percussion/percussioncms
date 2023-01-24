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

package com.percussion.services;

import com.percussion.server.PSServer;
import com.percussion.servlets.PSContextLoaderListener;
import com.percussion.utils.io.PathUtils;
import com.percussion.utils.jndi.PSJndiObjectLocator;
import com.percussion.utils.servlet.PSServletUtils;
import com.percussion.utils.xml.PSEntityResolver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

      PSServer.setRxDir(PathUtils.getRxDir(null));
      PSEntityResolver.setResolutionHome(PathUtils.getRxDir(null));

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
   private static final Logger log = LogManager.getLogger(PSContextLoader.class);

}
