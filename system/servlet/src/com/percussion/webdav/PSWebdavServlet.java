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
package com.percussion.webdav;

import com.percussion.hooks.PSServletBase;
import com.percussion.utils.servlet.PSServletUtils;
import com.percussion.webdav.error.IPSWebdavErrors;
import com.percussion.webdav.error.PSWebdavException;
import com.percussion.webdav.method.PSMethodFactory;
import com.percussion.webdav.method.PSWebdavMethod;
import com.percussion.webdav.objectstore.PSWebdavConfig;
import com.percussion.webdav.objectstore.PSWebdavConfigDef;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * This is the WebDAV servlet class, which provides WebDAV services
 * for all WebDAV Client.
 */
@SuppressWarnings(value={"unchecked"})
public class PSWebdavServlet extends PSServletBase
{
   private static final long serialVersionUID = 1L;

   /**
    * Initialize the log and WebDAV configuration from the specified 
    * servlet configuration.
    *
    * @param config a servlet configuration object, not <code>null</code>.
    * 
    * @throws ServletException if initialization failed.
    */
   public void init(ServletConfig config) throws ServletException,
         IllegalArgumentException
   {
      super.init(config); // this will initialize the log4j
      ms_logger.debug("WebDAV Initialized");

   }

   // see HttpServlet.service(HttpServletRequest req, HttpServletResponse resp)
   protected void service(HttpServletRequest req, HttpServletResponse resp)
         throws ServletException, IOException
   {
      try
      {
         PSWebdavMethod method = PSMethodFactory.createMethod(req.getMethod(),
               req, resp, this);
         method.execute();

      }
      catch (PSWebdavException e)
      {
         sendError(e, resp);
      }
      catch (ServletException e)
      {
         // If ServletException just throw it so
         // the user sees a stacktrace.
         throw e;
      }
      catch (Throwable e)
      {
         sendError(PSWebdavStatus.SC_INTERNAL_SERVER_ERROR, e, resp);
      }
   }

   /**
    * Convenient error handling routine. It sets the status code of the response
    * to the status code of the webdav exception (if it has been set); otherwise
    * the status code will be set to <code>
    * PSWebdavStatus.SC_INTERNAL_SERVER_ERROR</code>.
    *
    * @param e The webdav exception, assume not <code>null</code>.
    * @param resp The servlet response object, assume not <code>null</code>.
    */
   private void sendError(PSWebdavException e, HttpServletResponse resp)
   {
      int statusCode = (e.getStatusCode() != -1) ? e.getStatusCode()
            : PSWebdavStatus.SC_INTERNAL_SERVER_ERROR;
      sendError(statusCode, e, resp);
   }

   /**
    * Retrieves webdav configuration content for the current servlet.
    * 
    * @return The input stream of the configuration content, never
    *    <code>null</code>. Caller is responsible to close the 
    *    input stream.
    * 
    * @throws PSWebdavException if config does not exist.
    */
   public InputStream getWebdavConfigDef() throws PSWebdavException
   {
      String filename = getServletConfig().getInitParameter(CONFIG_FILE_PATH);
      File configDir = new File(PSServletUtils.getUserConfigDir(), CONFIG_BASE);
      File configFile = new File(configDir, filename);

      try(InputStream  in = new FileInputStream(configFile)) {
         return in;
      }catch (IOException e)
      {
         throw new PSWebdavException(IPSWebdavErrors.FILE_DOES_NOT_EXIST,
            filename);
      }

   }

   /**
    * Get the webdav configuration object of the current servlet. This object 
    * will be registered so that it can be accessed by different WebDAV
    * servlet instance within the same container. 
    * 
    * @return The current configuration, never <code>null</code> after the
    *    {@link #init(ServletConfig)} has been invoked.
    * 
    * @throws ServletException if an error occurs.
    */
   public PSWebdavConfig getWebdavConfig()
         throws ServletException
   {
      if (m_config == null)
      {

         try {
            // load the config file.
            try (InputStream in = getWebdavConfigDef()) {

               PSWebdavConfigDef configDef = new PSWebdavConfigDef(in);
               m_config = new PSWebdavConfig(configDef);
               m_config.setRxServletURI(getRhythmyxServletURI());
               m_config.setRxUriPrefix(getRxUriPrefixParameter());

               // register the configure object. synchronize the "put" operation
               synchronized (ms_webdavConfigMap) {
                  String servletName = getServletConfig().getServletName();
                  if (ms_webdavConfigMap.get(servletName) == null) {
                     String rootPath = m_config.getRootPath();
                     if (!isNestedPath(rootPath)) {
                        ms_webdavConfigMap.put(servletName, m_config);
                        ms_logger.debug("Added servletName: " + servletName);
                     } else {
                        ms_logger.error("WebDAV configuration Error: \""
                                + rootPath + "\" is a nested root.");
                        throw new ServletException("Nested Rhythmyx RootPath "
                                + rootPath);
                     }
                  }
               }
            }
         }
         catch (Exception e)
         {
            e.printStackTrace();
            throw new ServletException(e);
         }
      }

      return m_config;
   }

   /**
    * Validation to ensure that the rootpaths are not nested.
    * Allowing nested root paths permits non-determinstic behavior.
    * Ensures that the path and any of its components aren't already 
    * registered.
    * 
    * @param rxRoot the Rhythmyx-root, it may be <code>null</code> or empty.
    * 
    * @return true if this path is in the Map already, or
    *         if this path is contained in a RootPath that exists in the Map.
    */
   private boolean isNestedPath(String rxRoot)
   {
      boolean status = false;

      Iterator it = getRegisteredRxRootPaths();
      while (it.hasNext())
      {
         String apath = (String) it.next();
         if ((apath.indexOf(rxRoot) != -1)
               || (rxRoot.indexOf(apath) != -1))
         {
            ms_logger.debug("Nested path found, the tested servletRoot: "
                  + rxRoot + ", the registered root path: " + apath);
            status = true;
         }
      }
      return status;
   }

   /**
    * Returns the rootPaths for all of the registered servlets, except one that
    * is reqistered by itself, which is recognized by the servlet root.
    * 
    * @return a list over zero or more Rhtyhmyx Root Paths, never 
    *    <code>null</code>.
    */
   public Iterator getRegisteredRxRootPaths()
   {
      String servletName = getServletConfig().getServletName();
      Iterator entries = ms_webdavConfigMap.entrySet().iterator();
      List paths = new ArrayList();
      Map.Entry entry;
      String key;
      PSWebdavConfig config;
      
      while (entries.hasNext())
      {
         entry = (Entry) entries.next();
         key = (String) entry.getKey();
         if (! key.equals(servletName))
         {
            config = (PSWebdavConfig) entry.getValue();
            paths.add(config.getRootPath());
         }
      }

      return paths.iterator();
   }

   /**
    * Get the Rhythmyx Servlet name from the servlet parameter, default to
    * "/Rhythmyx" if not defined.
    * 
    * @return the RX servlet name, never <code>null</code>
    */
   public String getRhythmyxServletURI()
   {
      String rxName = getServletConfig().getServletContext().getInitParameter(
            PARAM_RX_URI);

      if (rxName == null || rxName.trim().length() == 0)
         rxName = "/Rhythmyx";

      return rxName;
   }

   /**
    * Get the value of the context parameter, <code>PARAM_RX_URL_PREFIX</code>
    *  
    * @return the value, never <code>null</code>, but may be empty
    *    if it is not defined.
    */
   private String getRxUriPrefixParameter()
   {
      String rxUriPrefix = getServletConfig().getServletContext()
            .getInitParameter(PARAM_RX_URL_PREFIX);

      if (rxUriPrefix == null || rxUriPrefix.trim().length() == 0)
         rxUriPrefix = "";

      return rxUriPrefix;
   }

   /**
    * Convenient error handling routine. It logs the error and set the status
    * to the response object according to the given status code.
    *
    * @param statusCode The status code to be set to the response.
    * @param e The error exception, it may not be <code>null</code>.
    * @param resp The servlet response object, it may not be <code>null</code>.
    */
   protected void sendError(int statusCode, Throwable e,
         HttpServletResponse resp)
   {
      if (e == null)
         throw new IllegalArgumentException("e may not be null");
      if (resp == null)
         throw new IllegalArgumentException("resp may not be null");

      String statusText = PSWebdavStatus.getStatusText(statusCode);
      ms_logger.error(statusText, e);
      try
      {
         resp.sendError(statusCode, statusText);
      }
      catch (Throwable x)
      {
      }
      ;
   }


   /**
    * Location of the webdav config files relative to the user config dir.
    */
   private static final String CONFIG_BASE = "webdav";
   
   /**
    * Servlet init param for the configuration file path, which is the path 
    * relative to the {@link #CONFIG_BASE} directory.
    */
   public final static String CONFIG_FILE_PATH = "RxWebDAVConfig";

   /**
    * Name of the Rx servlet URI init param for the servlet.
    */
   public final static String PARAM_RX_URI = "RhythmyxServletURI";

   /**
    * Name of the url pattern prefix init param for the servlet.
    */
   public final static String PARAM_RX_URL_PREFIX = 
      "RhythmyxServlet_UrlPatternPrefix";

   /**
    * The configuration information, initialized by 
    * <code>init()</code>, never <code>null</code> or empty after that.
    */
   private PSWebdavConfig m_config;

   /**
    * The logger object for the current class, never <code>null</code>.
    */
   private Logger ms_logger = Logger.getLogger(PSWebdavServlet.class);

   /**
    * It contains a list of registered WebDAV config objects for all WebDAV
    * servlets in the same container. The map key is servlet root as  
    * <code>String</code>, the map value is the WebDAV config object as
    * <code>PSWebdavConfig</code>. It can never be <code>null</code>, but may
    * be empty. 
    */
   private static Map ms_webdavConfigMap = new HashMap();
}
