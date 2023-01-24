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
package com.percussion.extensions.general;

// percussion
import com.percussion.design.objectstore.IPSJavaPlugin;
import com.percussion.design.objectstore.IPSJavaPluginConfig;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.utils.server.IPSCgiVariables;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSConsole;
import com.percussion.server.PSServer;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The purpose of the exit is to
 * <ol>
 * <li> Choose the appropriate plug-in from the server configuration settings
 * based on the client OS and Browser. </li>
 * <li> Add the plug-in related attributes to the root element of the XML
 * document. </li>
 * <li> Add the cx options attributes to the root element of the XML document.
 * </li>
 * </ol>
 */
public class PSAddPluginProperties implements IPSResultDocumentProcessor
{
   /*
    * Implementation of the method required by the interface IPSExtension.
    */
   @SuppressWarnings("unused")
   public void init(IPSExtensionDef extensionDef, File file)
         throws PSExtensionException
   {
      ms_fullExtensionName = extensionDef.getRef().toString();
   }

   /*
    * Implementation of the method required by the interface
    * IPSResultDocumentProcessor.
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /*
    * Implementation of the method required by the interface
    * IPSResultDocumentProcessor.
    */
   @SuppressWarnings("unused")
   public Document processResultDocument(Object[] params,
         IPSRequestContext request, Document resDoc)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {
      return addPluginInfo(request, resDoc);
   }

   /**
    * Retrieves the Java plugin based on the user agent or returning the default
    * Java plugin and then adds the plugin info attributes to the root node of
    * the results document passed in.
    * 
    * @param request the request context, assumed not <code>null</code>.
    * 
    * @param resDoc the results document to be modified, may be
    *           <code>null</code>.
    * 
    * @return the Java plugin, never <code>null</code>.
    */
   public static Document addPluginInfo(IPSRequestContext request,
         Document resDoc)
   {
      if (resDoc == null)
      {
         return resDoc;
      }

      Element elem = resDoc.getDocumentElement();

      if (elem == null)
      {
         return resDoc;
      }

      try
      {
         // Get the useragent
         String userAgent = request
               .getCgiVariable(IPSCgiVariables.CGI_REQUESTOR_SOFTWARE);

         PSServerConfiguration serverConfig = PSServer.getServerConfiguration();

         // Get the plugin config
         IPSJavaPluginConfig pluginCfg = serverConfig.getJavaPluginConfig();

         // Get the plugin by passing the userAgent
         IPSJavaPlugin plugin = pluginCfg.getPlugin(userAgent);

         // If the plugin is null then get the default plugin
         if (plugin == null)
         {
            request
                  .printTraceMessage("Could not find a plugin for UserAgent : "
                        + userAgent);
            request.printTraceMessage("Adding default plugin.");
            plugin = pluginCfg.getDefaultPlugin();
         }

         // Get the required parameters from plugin
         String implementation_version = plugin.getVersionToUse();
         String codebase = plugin.getDownloadLocation();
         String appletcodebase = PSServer.getRequestRoot()
               + "/sys_resources/AppletJars";

         String classid = "";
         String version_type = "";

         if (plugin.isStaticVersioning())
         {
            classid = plugin.getStaticClsid();
            version_type = IPSJavaPlugin.VERSION_STATIC;
         }
         else
         {
            classid = IPSJavaPlugin.CLASSID_DYNAMIC;
            version_type = IPSJavaPlugin.VERSION_DYNAMIC;
         }

         // Add the attributes now
         elem.setAttribute(ATTR_VERSIONTYPE, version_type);
         elem.setAttribute(ATTR_IMPLEMENTATIONVERSION, implementation_version);

         elem.setAttribute(ATTR_CLASSID, classid);
         elem.setAttribute(ATTR_CODEBASE, codebase);
         elem.setAttribute("appletcodebase", appletcodebase);

         elem.setAttribute(ATTR_SEARCH_ENGINE_AVAILABLE, serverConfig
               .isSearchEngineAvailable() ? "yes" : "no");

         return resDoc;
      }
      catch (Throwable t) // should never happen!
      {
         PSConsole.printMsg(ms_fullExtensionName, t);
      }

      return resDoc;
   }

   /**
    * The fully qualified name of this extension.
    */
   private static String ms_fullExtensionName = "";

   /**
    * Constant for attribute name version type
    */
   public static final String ATTR_VERSIONTYPE = "version_type";

   /**
    * Constant for attribute name implementation version
    */
   public static final String ATTR_IMPLEMENTATIONVERSION = "implementation_version";

   /**
    * Constant for attribute name classid
    */
   public static final String ATTR_CLASSID = "classid";

   /**
    * Constant for attribute name codebase for plugin
    */
   public static final String ATTR_CODEBASE = "codebase";

   /**
    * Constant for attribute name that specifies if an external search engine is
    * available.
    */
   public static final String ATTR_SEARCH_ENGINE_AVAILABLE = "search_engine_available";

}
