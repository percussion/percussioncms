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
import com.percussion.util.PSOsTool;
import com.percussion.util.PSResourceUtils;
import com.percussion.utils.container.PSContainerUtilsFactory;
import com.percussion.utils.container.PSStaticContainerUtils;
import com.percussion.utils.servlet.PSServletUtils;
import com.percussion.utils.spring.PSFileSystemXmlApplicationContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;
import org.springframework.web.context.support.XmlWebApplicationContext;

import javax.naming.NamingException;
import javax.naming.spi.NamingManager;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Create the appropriate spring context for use in locators. By configuring
 * this class appropriately, either a local or server configuration is loaded.
 * In particular, the {@link #init(ServletContext)} method is called by the
 * initialization servlet and sets up so that the server configuration is
 * loaded. Otherwise, the spring configuration uses the local definition and
 * initializes on first use.
 * 
 * @author dougrand
 */
public class PSBaseServiceLocator
{
   /**
    * This static holds a reference to the context used to initialize Spring. 
    * This is initialized from the application servlet, the initial call to
    * getBean, or on of the other <code>init</code> calls. 
    */
   private static volatile ConfigurableApplicationContext ms_context = null;

   /**
    * This is set to <code>true</code> once the jndi naming information is setup.
    */
   private static boolean ms_setNamingContextBuilder = false;

   /**
    * The logger to use in this class
    */
   private static Log ms_logger = LogFactory.getLog(PSBaseServiceLocator.class);

   /**
    * The location of the configuration directory in the source tree
    */
   private static final File ms_configdir = new File("ear/config");

   /**
    * The location of the spring directory under the configuration tree
    */
   private static final File ms_fileconfig = new File(ms_configdir, "spring");
   
   /**
    * For dynamically generated configuration files.
    */
   private static final String ms_generatedFileConfig = "/com/percussion/testing/local-beans.xml";

   /**
    * 
    */
   private static final File ms_hibernateconfig = new File(ms_configdir, "hibernate");

   private static volatile boolean isInitialized = false;
   private static volatile boolean initializing = false;
   /**
    * 
    */
   private static final String ms_hibernatepath = 
      "jetty/base/webapps/Rhythmyx/WEB-INF/classes";

   /**
    * track all the application contexts that are loaded. We need to tear
    * them down during shutdown.
    */
   private static ArrayList<ConfigurableApplicationContext> ms_ctxList = 
         new ArrayList<>();
   /**
    * Initialize the configuration for the server context. Only call this within
    * a J2EE container.
    * 
    * @param servletCtx the servlet context, must never be <code>null</code>
    */
    public static void init(ServletContext servletCtx)
    {
        if (!isInitialized)
        {

            synchronized (PSBaseServiceLocator.class)
            {
                if (!isInitialized)
                {
                    initializing=true;
                    ms_logger.info("Loading Container configuration");
                    PSContainerUtilsFactory.getConfigurationContextInstance().load();
                    ms_logger.info("Initializing Base Service Locator");
                    if (servletCtx == null)
                    {
                        throw new IllegalArgumentException("servletCtx must never be null");
                    }
                    XmlWebApplicationContext ctx = new XmlWebApplicationContext();
                     
                    ctx.setServletContext(servletCtx);

                    String sysConfigDir = PSServletUtils.getSpringConfigPath();
                    List<String> configFiles = new ArrayList<>();
                    configFiles.add(sysConfigDir + "/" + PSServletUtils.SERVER_BEANS_FILE_NAME);
                    configFiles.add(sysConfigDir + "/" + PSServletUtils.DESIGN_BEANS_FILE_NAME);
                    configFiles.add(sysConfigDir + "/" + PSServletUtils.DEPLOYER_BEANS_FILE_NAME);
                    configFiles.add(sysConfigDir + "/" + PSServletUtils.BEANS_FILE_NAME);

                    String[] files = configFiles.toArray(new String[configFiles.size()]);
                    ctx.setConfigLocations(files);
                    ms_context = ctx;
                    
                    ctx.refresh();
                    ms_ctxList.add(ctx);
                    
                    
                    XmlWebApplicationContext newContext = ctx;

                    // try loading cataloger configs as child context
                    ms_logger.info("Loading cataloger bean configurations");
                    configFiles.clear();
                    configFiles.add(sysConfigDir + "/" + PSServletUtils.CATALOGER_BEANS_FILE_NAME);
                    XmlWebApplicationContext childCtx = initChildCtx(ctx, configFiles);
                    if (childCtx != null)
                        ms_context = childCtx;

                    // try loading user configs as child context
                    configFiles = getUserConfigFiles();
                    if (!configFiles.isEmpty())
                    {
                        ms_logger.info("Loading user defined bean configurations");
                        childCtx = initChildCtx(ctx, configFiles);
                        if (childCtx != null)
                            ms_context = childCtx;
                    }

                    
                    ms_logger.info("Finished Initializing Base Service Locator");
                }
            }
        }
    }

   /**
    * Create a child context using the supplied parent and list of config files,
    * logs any errors.
    * 
    * @param parentCtx The context to set as the parent, assumed not 
    * <code>null</code>.
    * @param configFiles The list of configuration files to use to initialize
    * the context, assumed not <code>null</code> or empty.
    * 
    * @return The child context with the supplied parent set as its parent 
    * context, or <code>null</code> if there is an error initializing the ctx.
    */
   private static XmlWebApplicationContext initChildCtx(
      XmlWebApplicationContext parentCtx, List<String> configFiles)
   {
      try
      {
         String[] files = configFiles.toArray(new String[configFiles.size()]);
         XmlWebApplicationContext ctx = new XmlWebApplicationContext();
         ctx.setParent(ms_context);
         ctx.setServletContext(parentCtx.getServletContext());
         ctx.setConfigLocations(files);
         ctx.refresh();
         ms_ctxList.add(ctx);
         return ctx;
      }
      catch (Exception e)
      {
         ms_logger.error("Error loading child bean configurations: " + 
            e.getLocalizedMessage());
         return null;
      }
   }

   /**
    * Initializes the Spring context with the config files specified.
    * 
    * @param files the spring context config files
    * @throws PSMissingBeanConfigurationException
    */
   public static synchronized void initCtx(String[] files)
         throws PSMissingBeanConfigurationException
   {
      if (ms_context == null)
      {
         String[] fixedFiles = files;
         if (PSOsTool.isUnixPlatform())
         {
            //Bug in spring with absolute file paths.  Paths are repeated during
            //configuration which causes BeanDefinitionException.  Files cannot
            //be found.  Must prepend with '/' as workaround.
            for (int i=0; i < fixedFiles.length; i++)
               fixedFiles[i] = "/" + fixedFiles[i];
         }
         ms_context = new PSFileSystemXmlApplicationContext(fixedFiles);
      }
   }

   /**
    * Initializes the Spring context with the config files specified.  Also
    * sets the hibernate configuration directory and initializes the initial
    * context.
    * 
    * @param files the spring context config files
    * @param rxRoot the Rhythmyx root installation directory
    * @throws PSMissingBeanConfigurationException
    */
   public static synchronized void initCtxHib(String[] files, String rxRoot)
         throws PSMissingBeanConfigurationException
   {
      try
      {
         if (ms_context == null)
         {
            if (!ms_setNamingContextBuilder)
            {
               NamingManager
                     .setInitialContextFactoryBuilder(new SimpleNamingContextBuilder());
   
               ms_setNamingContextBuilder = true;
            }
            
            PSFileSystemXmlApplicationContext.setConfigDir(
                  new File(rxRoot, ms_hibernatepath));
            initCtx(files);
         }
      }
      catch (NamingException e)
      {
         ms_logger.error("Naming exception", e);
         throw new RuntimeException(e);
      }
   }
   
   /**
    * Checks to see if the base locator has been initialized
    * @return <code>true</code> if initialized
    */
   public static synchronized boolean isInitialized()
   {
      return ms_context != null;
   }
   
   /**
    * Sets the main application managed by this locator as a parent to the
    * supplied context. This method should be called after this locator's
    * context has been initialized, after the child context is constructed, but
    * before the child context is refreshed. The caller is responsible for
    * closing the supplied child context as necessary.
    * 
    * @param ctx The context to set the parent on, may not be <code>null</code>.
    * 
    * @throws IllegalStateException if {@link #isInitialized()} returns
    * <code>false</code>.
    */
   public static void addAsParentCtx(ConfigurableApplicationContext ctx)
   {
      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");
      
      if (!isInitialized())
         throw new IllegalStateException("Base context must be initialized");
      
      ctx.setParent(getCtx());
   }
   
   /**
    * Dynamically locate the user spring config files.  Ignores files ending in
    * "-servlet.xml" as these are loaded by the dispatch servlet.
    * 
    * @return The list of paths rooted relative to the servlet base directory,
    *         never <code>null</code>, may be empty.
    */
   private static List<String> getUserConfigFiles()
   {
      List<String> results = new ArrayList<>();

      String userConfigPath = PSServletUtils.getUserSpringConfigPath();
      File userConfigDir = PSServletUtils.getUserSpringConfigDir();

      File[] files = userConfigDir.listFiles();
      if (files != null)
      {
         for (int i = 0; i < files.length; i++)
         {
            if (!files[i].isFile())
               continue;
            String name = files[i].getName();
            if (name.endsWith("-servlet.xml"))
               continue;

            results.add(userConfigPath + "/" + name);
         }
      }

      return results;
   }

   /**
    * Destroy the configuration for the server context as well as any parent 
    * context. This should be called as part of server shutdown.
    */
   public static void destroy()
   {
      ms_logger.info("Destroying Base Service Locator");
      for(int i=ms_ctxList.size()-1; i>0; i--) 
      { 
         ConfigurableApplicationContext ctx = ms_ctxList.get(i);
         if ( ctx != null )
         {
            ConfigurableApplicationContext pCtx = 
               (ConfigurableApplicationContext) ctx.getParent();
            if ( pCtx!= null && pCtx.isActive())
               pCtx.close();
            ctx.close();
         }
      }
      ms_ctxList.clear();
   }

   /**
    * Lookup the given bean and return the bean from the spring configuration.
    * 
    * @param beanName the bean's name, must never be <code>null</code> or
    *           empty
    * @return the bean, never <code>null</code>
    * @throws PSMissingBeanConfigurationException if the bean is unknown to
    *            spring
    */
   public static Object getBean(String beanName)
         throws PSMissingBeanConfigurationException
   {
      if (StringUtils.isBlank(beanName))
      {
         throw new IllegalArgumentException("beanName may not be null or empty");
      }

      ApplicationContext ctx = getCtx();
      if (ctx == null)
         throw new RuntimeException("Application Context is null.");
      try
      {
         return getCtx().getBean(beanName);
      }
      catch (BeansException e)
      {
         throw new PSMissingBeanConfigurationException("Bean " + beanName
               + " is unknown", e);
      }
   }

   /**
    * Retrieve and initialize (if necessary) the Spring configuration object.
    * 
    * @return the configuration object, never <code>null</code>
    * @throws PSMissingBeanConfigurationException
    */
   public static  ApplicationContext getCtx()
         throws PSMissingBeanConfigurationException
   {
      try
      {
         if (!isInitialized)
         {
            synchronized(PSBaseServiceLocator.class)
            {
                
                if (!isInitialized && !initializing)
                {
                    initializing=true;
                    if (!ms_setNamingContextBuilder)
                    {
                       ms_logger.info("Setting initial test jndi context factory builder.");
                       NamingManager
                             .setInitialContextFactoryBuilder(new SimpleNamingContextBuilder());
                       ms_setNamingContextBuilder = true;
                    }
        
                    if (!System.getProperty("rxdeploydir").isEmpty())
                    {
                          loadFileConfig();
                                          }
                    else
                    {
                       ms_logger.error("Must be initialized via a servlet, or "
                             + "have a valid file configuration before use!");
                       throw new PSMissingBeanConfigurationException(
                             ms_fileconfig.getAbsolutePath());
                    }
                }
            }
         }
      }
      catch (BeansException e)
      {
         ms_logger.error("Problem in configuration", e);
         throw e;
      }
      catch (RuntimeException re)
      {
         ms_logger.error("Runtime exception", re);
         throw re;
      }
      catch (NamingException e)
      {
         ms_logger.error("Naming exception", e);
         throw new RuntimeException(e);
      }

      return ms_context;
   }

   /**
    * This is only used for testing configurations
    */
   private static void loadFileConfig()
   {
    /*  File configDir = new File(PSServer.getRxDir().getAbsolutePath() +
              "/jetty/base/webapps/Rhythmyx/WEB-INF/config/spring");

       File beans = new File(configDir, PSServletUtils.BEANS_FILE_NAME);
      File designBeans = new File(configDir,
            PSServletUtils.DESIGN_BEANS_FILE_NAME);
      File deployerBeans = new File(configDir,
            PSServletUtils.DEPLOYER_BEANS_FILE_NAME);
       File localbeans = null;
      try {
          localbeans = PSResourceUtils.getFile(PSBaseServiceLocator.class, ms_generatedFileConfig, configDir);
      }catch(IOException e){
          ms_logger.error("Failed to initialize local-beans.xml", e);
          throw new RuntimeException(e);
      }
      PSFileSystemXmlApplicationContext.setConfigDir(ms_hibernateconfig);
      ms_context = new PSFileSystemXmlApplicationContext(new String[] {
            localbeans.getPath(), beans.getPath(), designBeans.getPath(), 
            deployerBeans.getPath()});
  */}


 public static void setCtx(ConfigurableApplicationContext c){
    ms_context = c;
 }
}