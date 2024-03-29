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
package com.percussion.fastforward.sfp;

import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.error.PSExceptionUtils;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionErrors;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.fastforward.utils.PSUtils;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSServer;
import com.percussion.server.cache.PSExitFlushCache;
import com.percussion.util.IPSHtmlParameters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * @author James Schultz
 */
public abstract class PSSiteFolderContentListBaseExit implements
      IPSResultDocumentProcessor
{

   private static final Logger log = LogManager.getLogger(PSSiteFolderContentListBaseExit.class);

   /**
    * Implementation of the method from the interface.
    * @return always <code>false</code>.
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /**
    * @param params values passed by the application invoking the exit
    * <ol start="0">
    * <li>filenameContext: id of the context used to generate content items
    * delivery locations. Defaults to the context of the current request.</li>
    * <li>deliveryType: determines the value of the deliverytype attribute of
    * the contentlist root element. Defaults to ftp.</li>
    * <li>isIncremental: defaults to n.</li>
    * <li>publishableContentValidValues: defaults to y (public states only).</li>
    * <li>maxRowsPerPage: enables pagination mode by determining the maximum
    * number of content items to appear in a single page of the content list.
    * Defaults to -1 (disables pagination mode; unlimited number of items)</li>
    * <li>contentResourceName: Content items and its variants lookup resource
    * name, default resource will be provided by the derived classes</li>
    * </ol>
    * <li>NonStandardParamsToPass: Comma separated list of all non standard HTML
    * parameters to pass on from request to the content URL for each item in the
    * content list</li>
    * </ol>
    * @param request
    * @param resultDoc
    * @return
    * @throws com.percussion.extension.PSParameterMismatchException if the current request does not
    * contain a sys_siteid parameter, if no site folder root is registered to
    * that site, or if the filenameContext parameter is mot provided.
    * @throws PSExtensionProcessingException
    */
   public Document processResultDocument(
      Object[] params,
      IPSRequestContext request,
      Document resultDoc)
      throws PSParameterMismatchException, PSExtensionProcessingException
   {

      Document contentListXml = null;

      String siteid = request.getParameter(IPSHtmlParameters.SYS_SITEID);
      if (siteid == null)
         throw new PSParameterMismatchException(
            IPSExtensionErrors.EXT_MISSING_REQUIRED_PARAMETER_ERROR,
            new Object[] { "PSXParam/sys_siteid", "null" });

      String folderPath = PSSite.lookupFolderRootForSite(siteid, request);

      if (folderPath == null || folderPath.trim().length() == 0)
         throw new PSParameterMismatchException(
            IPSExtensionErrors.EXT_MISSING_REQUIRED_PARAMETER_ERROR,
            new Object[] { "folderPath", "null" });

      String filenameContext =
         PSUtils.getParameter(
            params,
            0,
            request.getParameter(IPSHtmlParameters.SYS_CONTEXT, "0"));

      String deliveryType = PSUtils.getParameter(params, 1, "ftp");

      boolean isIncremental;
      String incremental = PSUtils.getParameter(params, 2, "n");
      if (incremental.startsWith("y"))
         isIncremental = true;
      else
         isIncremental = false;

      String publishableContentValidValues =
         PSUtils.getParameter(params, 3, "y");

      String maxRowsPerPageParam = PSUtils.getParameter(params, 4, "-1");
      int maxRowsPerPage = Integer.parseInt(maxRowsPerPageParam);

      String contentVariantResourceName  = PSUtils.getParameter(params, 5);

      //7th parameter is a list of non-standard parameters to pass from request
      //to content item URL.
      String paramString = PSUtils.getParameter(params, 6);
      
      String protocol = PSUtils.getParameter(params, 7, "http");
      String host = PSUtils.getParameter(params, 8, PSServer.getHostAddress());
      String port = PSUtils.getParameter(params, 9, Integer.toString(PSServer
            .getListenerPort()));
      String publishFolderPath = PSUtils.getParameter(params, 10, folderPath);
      
      Set paramSetToPass = new HashSet();
      if (paramString != null && paramString.length() > 0)
      {
         StringTokenizer tokenizer = new StringTokenizer(paramString, ",");
         while (tokenizer.hasMoreTokens())
         {
            String element = (String) tokenizer.nextToken().trim();
            if (element.length() > 0)
               paramSetToPass.add(element);
         }
      }

      try
      {
         // flush all cache if in publishing hub.
         PSServerConfiguration srvConfig = PSServer.getServerConfiguration();
         if (srvConfig.getServerType()
               == PSServerConfiguration.SERVER_TYPE_PUBLISHING_HUB)
         {
            PSExitFlushCache flushCache = new PSExitFlushCache();
            flushCache.preProcessRequest(null, request);
         }

         // generate the content list
         PSSiteFolderCListBase listBuilder =
            getSiteFolderCListInstance(
               request,
               isIncremental,
               publishableContentValidValues,
               contentVariantResourceName,
               maxRowsPerPage,
               protocol, host, port,
               paramSetToPass);
         // look for the parameter used by the result pager
         String indexOfFirstItem = request.getParameter("psfirst", "1");
         contentListXml =
            listBuilder.buildContentList(
               folderPath,
               deliveryType,
               filenameContext,
               Integer.parseInt(indexOfFirstItem), 
               publishFolderPath);
      }
      catch (RuntimeException rune)
      {
         log.error(rune.getMessage());
         log.debug(rune.getMessage(), rune);
         throw rune;
      }
      catch (Exception e)
      {
         log.error(PSExceptionUtils.getMessageForLog(e));
         log.debug(PSExceptionUtils.getDebugMessageForLog(e));
         throw new PSExtensionProcessingException(
            "PSSiteFolderContentListExit",
            e);
      }

      return contentListXml;
   }

   /**
    * Gets an instance of a site-folder-driven content list builder. Whether the
    * content list is full or incremental,
    * 
    * The a site folder content list builder that will use the specified request
    * for obtaining request parameters, logging, and making internal requests.
    * 
    * @param request the current request context, used to obtain request
    *           parameters, logging, and making internal requests. Not
    *           <code>null</code>.
    * @param isIncremental <code>true</code> to generate an incremental
    *           publishing content list, <code>false</code> for full
    *           publishing
    * @param publishableContentValidValues comma-delimited list of contentvalid
    *           values for workflow states that are eligible for publishing.
    *           Never <code>null</code> or empty.
    * @param contentResourceName the name of the application resource used for
    *           looking up the valid items and its variants. Must be supplied in
    *           the form &lt;ApplicationName&gt;/&lt;ResourceName&gt;
    * @param maxRowsPerPage the maximum number of items to publish on a page in
    *           the content list.
    * @param protocol the URL protocol to use when creating content URLs, never
    *           <code>null</code> or empty
    * @param host the host name or ip address to use when creating content URLs,
    *           never <code>null</code> or empty
    * @param port the port number to use when creating content URLs, never
    *           <code>null</code> or empty
    * @param paramSetToPass Set of non-standard HTML parameters to pass from
    *           request context to each content item url in the content list,
    *           must not be <code>null</code>, may be empty.
    * 
    * @return the instance of the site-folder-driven content list builder, never
    *         <code>null</code>.
    */
    protected abstract PSSiteFolderCListBase getSiteFolderCListInstance(
         IPSRequestContext request, boolean isIncremental,
         String publishableContentValidValues,
         String contentResourceName, int maxRowsPerPage,
         String protocol, String host, String port,
         Set paramSetToPass);

   /**
    * Implementation of the interface method. Does nothing.
    * 
    * @see com.percussion.extension.IPSExtension#init(com.percussion.extension.IPSExtensionDef, java.io.File)
    */
   public void init(IPSExtensionDef def, File codeRoot)
      throws PSExtensionException
   {
   }
}
