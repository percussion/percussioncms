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
package com.percussion.cms.handlers;

import com.percussion.cms.PSCmsException;
import com.percussion.fastforward.globaltemplate.PSGlobalTemplate;
import com.percussion.fastforward.globaltemplate.PSGlobalTemplateException;
import com.percussion.fastforward.globaltemplate.PSRxGlobals;
import com.percussion.server.IPSHandlerStateListener;
import com.percussion.server.IPSHttpErrors;
import com.percussion.server.IPSLoadableRequestHandler;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSConsole;
import com.percussion.server.PSHandlerStateEvent;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestContext;
import com.percussion.server.PSResponse;
import com.percussion.server.PSServer;
import com.percussion.server.cache.IPSCacheHandler;
import com.percussion.server.cache.PSAssemblerCacheHandler;
import com.percussion.server.cache.PSCacheManager;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.general.IPSRhythmyxInfo;
import com.percussion.services.general.PSRhythmyxInfoLocator;
import com.percussion.util.PSFileFilter;
import com.percussion.util.PSFilteredFileList;
import com.percussion.utils.tools.PSPatternMatcher;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Appender;

import org.apache.log4j.Logger;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;


import org.apache.logging.log4j.core.layout.PatternLayout;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * Loadable request handler to update the global templates to propagate the
 * changes made to the global template source HTML files. This class also
 * implements the {@link com.percussion.server.IPSHandlerStateListener}so that
 * it becomes listner to the global template application state change by
 * registering itself as global template Rhythmyx application state change
 * listener with the server. This does not really handle any user requests
 * except for a default one (currenlty) that is to get the status log of
 * previous global template update. The main objective of this handler is to
 * update the global templates when the global template Rhythmyx application is
 * started. Refer to
 * {@link com.percussion.fastforward.globaltemplate.PSGlobalTemplate}
 *  
 */
public class PSGlobalTemplateUpdateHandler
   implements IPSLoadableRequestHandler, IPSHandlerStateListener
{
   /**
    * Implementation of the interface method. Initializes the option parameters
    * from the configuration file. Registers itself as listerner (ph: listener) of the global 
    * template application startup. 
    * <p>
    * The DTD of the configuration file is:
    * &lt;!ELEMENT GlobalTemplateHandlerConfig (Option*)&gt;
    * &lt;!ELEMENT Option (#PCDATA)&gt;
    * &lt;!ATTLIST Option
    *    name (CleanAppFolder | GlobalTemplateApp | LogFile | 
    *    TouchVariants) #IMPLIED
    * <p>
    * If it fails to read the configuration file or any option in 
    * configuration file, default options are applied.
    * 
    * @see com.percussion.server.IPSLoadableRequestHandler#init(java.util.Collection, java.io.InputStream)
    * 
    * @see IPSLoadableRequestHandler
    */
   public void init(Collection requestRoots, InputStream cfgFileIn)
   {
      //Assume default request root as the handler name
      m_requestRoot = HANDLER;
      //Override with actual one.
      if (requestRoots != null && requestRoots.size() > 0)
      {
         //Strip /Rhythmyx/ off
         m_requestRoot = requestRoots.iterator().next().toString();
         m_requestRoot = m_requestRoot.substring(1);
         int index = m_requestRoot.indexOf('/');
         if (index != -1)
            m_requestRoot = m_requestRoot.substring(index + 1);
      }

      String logFile = DEFAULT_LOGFILE;
      Exception ex = null;
      try
      {
         Document cfgDoc =
            PSXmlDocumentBuilder.createXmlDocument(cfgFileIn, false);
         NodeList nlOption = cfgDoc.getElementsByTagName("Option");
         for (int i = 0; i < nlOption.getLength(); i++)
         {
            Element elemOption = (Element) nlOption.item(i);
            String name = elemOption.getAttribute("name");
            String value = null;
            Node node = elemOption.getFirstChild();
            if (node.getNodeType() == Node.TEXT_NODE)
               value = ((Text) node).getData().trim();
            if (StringUtils.isEmpty(value))
               continue;

            if (name.equals("GlobalTemplateApp"))
            {
               m_globalTemplateAppName = value;
            }
            else if (name.equals("TouchVariants"))
            {
               if (value.equalsIgnoreCase("no")
                  || value.equalsIgnoreCase("false"))
                  m_touchVariants = false;
            }
            else if (name.equals("LogFile"))
            {
               logFile = value;
            }
            else if (name.equals("CleanAppFolder"))
            {
               if (value.equalsIgnoreCase("no")
                  || value.equalsIgnoreCase("false"))
                  m_cleanAppFolder = false;
            }
         }
      }
      catch (IOException e)
      {
         ex = e;
      }
      catch (SAXException e)
      {
         ex = e;
      }
      IPSRhythmyxInfo rxInfo = PSRhythmyxInfoLocator.getRhythmyxInfo();
      m_rxRootDir = (String) rxInfo
            .getProperty(IPSRhythmyxInfo.Key.ROOT_DIRECTORY);
      if (ex != null)
      {
         PSConsole.printMsg(HANDLER, ex);
         PSConsole.printMsg(
            HANDLER,
            "Error loading options. Default options will be used");
      }

      // Build the log file path which is always under the request root.

      m_logFilePath = m_rxRootDir + File.separator + m_globalTemplateAppName
            + File.separator + logFile;

      // Register this as the global application startup state listener.
      PSServer.addHandlerStateListener(
         this,
         m_globalTemplateAppName,
         PSHandlerStateEvent.HANDLER_EVENT_STARTED);

      PSConsole.printMsg(
         HANDLER,
         "Global template update handler initialization completed");
   }

   /**
    * Implementation of the interface method.
    * @return name of this request handler. Hard coded name.
    * @see com.percussion.server.IPSRootedHandler#getName()
    */
   public String getName()
   {
      return HANDLER;
   }

   /** 
    * Implementation of the interface method. Return an iterator with just 
    * one entry for the request root which is the handler name returned by 
    * {@link #getName() getName()}
    * @see com.percussion.server.IPSRootedHandler#getRequestRoots()
    */
   public Iterator getRequestRoots()
   {
      final List<String> roots = new ArrayList<>(1);
      roots.add(getName());
      return roots.iterator();
   }

   /**
    * Implementation of the interface method. Currently supports only a 
    * default request that served with the log file created during last 
    * processing of global templates. The request does not an authentication 
    * of the user requesting the log file.
    * 
    * @see com.percussion.server.IPSRequestHandler#processRequest(PSRequest)
    */
   public void processRequest(PSRequest request)
   {
      PSResponse resp = request.getResponse();
      File file = new File(m_logFilePath);
      FileInputStream fis;
      try
      {
         fis = new FileInputStream(file);
         resp.setContent(fis, file.length(), "text/plain");
      }
      catch (FileNotFoundException e)
      {
         resp.setIsErrorResponse(true);
         resp.setStatus(IPSHttpErrors.HTTP_NOT_FOUND, e.getMessage());
      }
   }

   /**
    * Implementation of the interface method. Just prints the console 
    * indicating the handler is shut down.
    * @see com.percussion.server.IPSRequestHandler#shutdown()
    */
   public void shutdown()
   {
      PSConsole.printMsg(
         HANDLER,
         "Shutting down global template update handler");
   }

   /**
    * Implementation of the interface method. Responds to only startup event 
    * of the global template application. Actual processing of global 
    * templates is done here. 
    * @see com.percussion.server.IPSHandlerStateListener#stateChanged(PSHandlerStateEvent)
    */
   public void stateChanged(PSHandlerStateEvent e)
   {
      if (!m_globalTemplateAppName.equals(e.getHandlerName()))
         return;

      if (PSHandlerStateEvent.HANDLER_EVENT_STARTED != e.getStateEvent())
      {
         return;
      }
      updateGlobalTemplates();
   }

   /**
    * Implementation of the actual updating of global templates. 
    */
   private void updateGlobalTemplates()
   {
      File file = new File(m_logFilePath);
      
      PSRequest req = PSRequest.getContextForRequest();
      IPSRequestContext request = new PSRequestContext(req);
      
      Logger logger = Logger.getLogger("com.percussion.globaltemplates");


      Appender wa = logger.getAppender(
              "RXGLOBALTEMPLATES");

      if(wa!=null)
      {
          final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
          final Configuration config = ctx.getConfiguration();
          PatternLayout layout = PatternLayout.newBuilder().withPattern(PatternLayout.SIMPLE_CONVERSION_PATTERN).withConfiguration(config).build();

          FileAppender appender = FileAppender.newBuilder().setLayout(layout).withFileName(m_logFilePath).withAppend(false).withLocking(false).setName("RXGLOBALTEMPLATES").withImmediateFlush(true).build();
          appender.start();
          config.addAppender(appender);
          AppenderRef ref = AppenderRef.createAppenderRef("RXGLOBALTEMPLATES", Level.INFO, null);
          AppenderRef[] refs = new AppenderRef[] {ref};

          LoggerConfig loggerConfig = LoggerConfig.createLogger(false, Level.INFO, "com.percussion.globaltemplates",
                  "true", refs, null, config, null );
          loggerConfig.addAppender(appender, null, null);

          config.addLogger("com.percussion.globaltemplates", loggerConfig);
          ctx.updateLoggers();

      }



      logger.info("Processing started at " + new Date().toString());
      logger.info("Begin of Global template creation...");
      logger.info("");

      if (m_cleanAppFolder)
         cleanAppFolder(logger);

      Exception ex = null;
      String[] files = getSourceHtmlFiles();
      try
      {
         PSRxGlobals rxGlobals = new PSRxGlobals(logger);
         for (int i = 0; i < files.length; i++)
         {
            String srcHtmlFile = files[i];
            logger.info("");
            logger.info("Processing file: " + srcHtmlFile + "...");
            logger.info("");
            PSGlobalTemplate gt =
               new PSGlobalTemplate(m_rxRootDir, 
                  m_globalTemplateAppName,
                  srcHtmlFile,
                  logger);
            rxGlobals.addGlobalTemplate(gt);
         }
         rxGlobals.save();
         if (m_touchVariants)
            touchVariants(request);
      }
      catch (IOException e)
      {
         ex = e;
      }
      catch (SAXException e)
      {
         ex = e;
      }
      catch (PSCmsException e)
      {
         ex = e;
      }
      catch (PSGlobalTemplateException e)
      {
         ex = e;
      }
      finally
      {
         if (ex == null)
         {
            logger.info("Global template creation successful");
         }
         else
         {
            logger.error("Global template creation failed. " + ex.getMessage());
         }
         logger.info("Processing finished at " + new Date().toString());
      }
   }

   /**
    * Cleanup global template application folder in that keep only the source 
    * files whose names end with ".htm", ".html" and ".xhtml".
    * @param logger logger object the process log, assumed not <code>null</code>.
    */
   private void cleanAppFolder(Logger logger)
   {
      logger.info("Cleaning the global template application folder...");
      PSPatternMatcher pattern = new PSPatternMatcher('?', '*', "*.*");
      PSFileFilter filter = new PSFileFilter(PSFileFilter.IS_FILE);
      filter.setNamePattern(pattern);
      PSFilteredFileList lister = new PSFilteredFileList(filter);
      List listFiles = lister.getFiles(m_rxRootDir + File.separator
            + m_globalTemplateAppName);
      for (int i = listFiles.size() - 1; i >= 0; i--)
      {
         File file = (File) listFiles.get(i);
         //skip deleting all html files and our log file
         if (file.getName().indexOf(".htm") == -1
            && file.getName().indexOf(".xhtml") == -1
            && !file.getName().endsWith(".log"))
         {
            if (file.delete())
            {
               logger.info("Deleted file: " + file.getName());
            }
            else
            {
               logger.warn("Could not delete file: " + file.getName());
            }
         }
      }
   }

   /**
    * Helper method that fetches all html source files. The source files is 
    * assumed to have one of the three following file name extensions.
    * <ol>
    * <li>.htm</li>
    * <li>.html</li>
    * <li>.xhtml</li>
    * </ol>
    * @return never <code>null</code> or empty, may be 0 length
    */
   private String[] getSourceHtmlFiles()
   {
      PSPatternMatcher pattern = new PSPatternMatcher('?', '*', "*.*htm*");
      PSFileFilter filter = new PSFileFilter(PSFileFilter.IS_FILE);
      filter.setNamePattern(pattern);
      PSFilteredFileList lister = new PSFilteredFileList(filter);
      List listFiles = lister.getFiles(m_rxRootDir + File.separator
            + m_globalTemplateAppName);
      String[] files = new String[listFiles.size()];
      Iterator iter = listFiles.iterator();
      int i = 0;
      while (iter.hasNext())
         files[i++] = ((File) iter.next()).getName();

      return files;
   }

   /**
    * Touch all variants (XSL files) under Rhythmyx tree so that the XSL
    * processor would not cache the style sheets in memory. Also flushes entire
    * assembly cache.
    * 
    * @param request request context object used to load the variants registered
    *           with the system.
    * @throws PSCmsException if loading of variants fails for any reason.
    */
   private void touchVariants(IPSRequestContext request) throws PSCmsException
   {
      IPSAssemblyService assembly = PSAssemblyServiceLocator.getAssemblyService();
      Set<IPSAssemblyTemplate> templates;
      try
      {
         templates = assembly.findAllTemplates();
      }
      catch (PSAssemblyException e)
      {
         throw new PSCmsException(IPSServerErrors.UNEXPECTED_EXCEPTION_CONSOLE,
               e.getLocalizedMessage());
      }

      for(IPSAssemblyTemplate template : templates)
      {
         String xslPath = template.getStyleSheetPath();
         String app = template.getAssemblyUrl();
         if (StringUtils.isEmpty(app))
         {
            continue;
         }
         int loc = app.indexOf('/');
         if (loc != -1)
            app = app.substring(loc + 1);
         loc = app.lastIndexOf('/');
         if (loc != -1)
            app = app.substring(0, loc);

         File file = new File(app + File.separator + xslPath);
         file.setLastModified(new Date().getTime());
      }
      // Flush all assembly cache
      // get the assembler cache handler
      PSCacheManager mgr = PSCacheManager.getInstance();
      IPSCacheHandler handler =
         mgr.getCacheHandler(PSAssemblerCacheHandler.HANDLER_TYPE);

      // if caching not enabled, will be null
      if (handler == null)
         return;

      // create key map with all empty values
      String[] keys = handler.getKeyNames();
      int numKeys = keys.length;

      Map<String, String> keyMap = new HashMap<>(numKeys);
      //Set all keys empty to flush entire assembly cache
      for (int i = 0; i < numKeys; i++)
      {
         keyMap.put(keys[i], "");
      }
      mgr.flush(keyMap);
   }

   /**
    * Name of the global template applictaion. Read from the configuration 
    * file in {@link #init(Collection, InputStream) init()} method. Default 
    * value is {@link #DEFAULT_GLOBALTEMPLATE_APP_NAME}.
    */
   private String m_globalTemplateAppName = DEFAULT_GLOBALTEMPLATE_APP_NAME;

   /**
    * Default name for global templates application.
    */
   public static final String DEFAULT_GLOBALTEMPLATE_APP_NAME =
      "rxs_GlobalTemplates";

   /**
    * Default name of the log file to write the global template processing 
    * status.
    */
   private static final String DEFAULT_LOGFILE = "globaltemplates.log";

   /**
    * Name of this hanlder.
    */
   private static final String HANDLER = "GTHandler";

   /**
    * Requets root for this handler. Defaulted to {@link #HANDLER} and may be 
    * overridden in {@link #init(Collection, InputStream)}.
    */
   private String m_requestRoot = HANDLER;

   /**
    * Path of the log file. Gets initialized in init method.
    */
   private String m_logFilePath = "";

   /**
    * Flag to indicate the processing must touch all registered variant XSLs 
    * after global templates are updated. The default value is 
    * <code>true</code>. May be overridden in 
    * {@link #init(Collection, InputStream)} which is not a desired case.
    */
   private boolean m_touchVariants = true;

   /**
    * Flag to indicate the global template application folder to be cleaned 
    * before processing starts. The default value is <code>true</code>. May 
    * be overridden in {@link #init(Collection, InputStream)} which is not 
    * a desired case.
    */
   private boolean m_cleanAppFolder = true;

   /**
    * Location of the Rhythmyx root directory. Initialized in init method
    * and never <code>null</code> after that.
    */
   private String m_rxRootDir = "";
}
