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
package com.percussion.fastforward.sfp;

import com.percussion.data.PSConversionException;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.extension.IPSExtensionManager;
import com.percussion.extension.IPSUdfProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionRef;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSServer;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSUrlUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;


/**
 * Defines methods to generate various types of assembly locations.
 */
public class PSSiteFolderContentListLinkGenerator
{
      /**
       * Generates an absolute URL to the assembler resource for the specified
       * variant, using the Rhythmyx server's external name and port. The URL
       * will contain request parameters for sys_contentid, sys_revision,
       * sys_authtype, sys_context, sys_variantid, sys_siteid, and rx_folder
       * (containing the content id of the site folder of the item being
       * assembled).
       * 
       * @param contentId
       * @param revision
       * @param variantId
       * @param variantAssemblerBase
       * 
       * @param folderId the ID of the site folder than contains the item. If
       *           provided, this value will be included in the URL as
       *           "sys_folderid"
       * @param request
    * @param protocol the URL protocol to use when creating content URLs, never
    *           <code>null</code> or empty
    * @param host the host name or ip address to use when creating content URLs,
    *           never <code>null</code> or empty
    * @param port the port number to use when creating content URLs
       * @param paramSetToPass Set of non-standard HTML parameters to pass from
       *           request context to each content item url in the content list,
       *           may be <code>null</code> or empty.
       * 
       * @return an absolute external URL to the assembler for the specified
       *         variant, never <code>null</code>.
       */
   public static URL generateAssemblerLink( 
      String contentId,
      String revision,
      String variantId,
      String variantAssemblerBase,
      String folderId,
      IPSRequestContext request, 
      String protocol, String host, int port,
      Set paramSetToPass)
   {
      if (protocol == null || protocol.trim().length() == 0)
      {
         throw new IllegalArgumentException("protocol must not be null or empty");
      }
      if (host == null || host.trim().length() == 0)
      {
         throw new IllegalArgumentException("host must not be null or empty");
      }
      HashMap paramMap = new HashMap(10);
      paramMap.put(IPSHtmlParameters.SYS_CONTENTID, contentId);
      paramMap.put(IPSHtmlParameters.SYS_REVISION, revision);
      paramMap.put(
         IPSHtmlParameters.SYS_AUTHTYPE,
         request.getParameter(IPSHtmlParameters.SYS_AUTHTYPE, "0"));
      paramMap.put(
         IPSHtmlParameters.SYS_CONTEXT,
         request.getParameter(IPSHtmlParameters.SYS_CONTEXT, "0"));

      //Add non-standard HTML parameters (if any) from the request.
      if(paramSetToPass != null)
      {
         Iterator iter = paramSetToPass.iterator();
         while (iter.hasNext())
         {
            String paramName = (String) iter.next();
            String paramvalue = request.getParameter(paramName);
            if(paramvalue != null)
               paramMap.put(paramName, paramvalue);
         }
      }

      paramMap.put(IPSHtmlParameters.SYS_VARIANTID, variantId);
      paramMap.put(
         IPSHtmlParameters.SYS_SITEID,
         request.getParameter(IPSHtmlParameters.SYS_SITEID));

      // include the folder content id as a parameter
      if (folderId != null)
      {
         paramMap.put(IPSHtmlParameters.SYS_FOLDERID, folderId);
      }

      // include nav_theme parameter, if supplied
      String navTheme =
         request.getParameter(PSSiteFolderContentList.NAV_THEME_PARAM_NAME);
      if (navTheme != null)
         paramMap.put(PSSiteFolderContentList.NAV_THEME_PARAM_NAME, navTheme);

      try
      {
         URL assembler =
            PSUrlUtils.createUrl(
               host,
               new Integer(port),
               variantAssemblerBase,
               paramMap.entrySet().iterator(),
               null,
               request);
         // Check and fix protocol
         if (assembler.getProtocol().equals(protocol) == false)
         {
            assembler = new URL(protocol, assembler.getHost(), 
                  assembler.getPort(), assembler.getFile());
         }
         return assembler;
      }
      catch (MalformedURLException e)
      {
         // this exception should never occur
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
         throw new RuntimeException(e.toString());
      }
   }

   /**
    * Generates the publishing location for the specified content item's variant
    * in the specified context. Calls the sys_casGeneratePubLocation exit to do
    * the heavy lifting.
    * 
    * @param contentId
    * @param revision
    * @param variantId
    * @param context
    * @param folderPath
    * @param request
    * 
    * @return the publishing location produced by the scheme generator
    *         registered for the specified variant/context. Never
    *         <code>null</code>, will be empty if an error occurs while
    *         calling the generator (or if no generator is registered for the
    *         variant/context).
    * @deprecated use
    *             {@link #generatePubLocation(String, String, String, String, String, String, IPSRequestContext)}
    *             Created a new method that takes the folderid parameter to fix
    *             the bug RX-13461.
    */
   public String generatePubLocation(
      String contentId,
      String revision,
      String variantId,
      String context,
      String folderPath,
      IPSRequestContext request)
   {
      return generatePubLocation(contentId, revision, variantId, context,
            folderPath, null, request);
   }

   /**
    * Generates the publishing location for the specified content item's variant
    * in the specified context.  Calls the sys_casGeneratePubLocation exit to do
    * the heavy lifting.
    * @param contentId
    * @param revision
    * @param variantId
    * @param context
    * @param folderPath
    * @param folderId
    * @param request
    *
    * @return the publishing location produced by the scheme generator
    * registered for the specified variant/context.  Never <code>null</code>,
    * will be empty if an error occurs while calling the generator (or if no
    * generator is registered for the variant/context).
    */
   public String generatePubLocation(
      String contentId,
      String revision,
      String variantId,
      String context,
      String folderPath,
      String folderId,
      IPSRequestContext request)
   {
      String pubLocation = "";

      try
      {
         request.setPrivateObject(PSSite.SITE_PATH_NAME, folderPath);
         Object[] params = new Object[6];
         params[0] = variantId;
         params[1] = contentId;
         params[2] = revision;
         params[3] = context;
         if(folderId != null)
            params[5] = folderId;
         pubLocation = (String) getGenerator().processUdf(params, request);
      }
      catch (PSConversionException e)
      {
         log.error(this.getClass().getName(), e);
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
      }
      finally
      {
         request.setPrivateObject(PSSite.SITE_PATH_NAME, null);
      }
      return pubLocation;
   }

   
   /**
    * Generates a URL to a resource named "null" in the current application.
    * This resource should return a trivial XML document; the URL is used as
    * the assembly URL when an item has no publishable variants because the
    * publisher requires a valid content URL.
    * @param request
    *
    * @return a URL to a resource named "null.xml" in the same application as
    * the origining request, never null.
    */
   public URL generateNullLink(IPSRequestContext request)
   {
      try
      {
         // no host or port specified means the values come from origining request
         URL assembler =
            PSUrlUtils.createUrl(null, null, "null.xml", null, null, request);
         return assembler;
      }
      catch (MalformedURLException e)
      {
         // this exception should never occur
         log.error(this.getClass().getName(), e);
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
         throw new RuntimeException(e.toString());
      }
   }

   /**
    * Initializes the sys_casGeneratePubLocation UDF and caches it in a member
    * variable.  If any errors occur, they are logged but not propagated.
    *
    * TODO: replace with a more robust method when the server supports one
    */
   protected void initGeneratorUDF()
   {
      try
      {
         PSExtensionRef extRef = new PSExtensionRef(GENERATE_PUB_LOCATION_REF);
         IPSExtensionManager extMgr = PSServer.getExtensionManager(null);
         m_generatorUDF =
            (IPSUdfProcessor) extMgr.prepareExtension(extRef, null);
      }
      catch (PSNotFoundException e)
      {
         log.error(this.getClass().getName(), e);
      }
      catch (PSExtensionException e)
      {
         log.error(this.getClass().getName(), e);
      }

   }
   
   /**
    * Get the ready to use location generator UDF which is
    * <em>sys_casGeneratePubLocation</em>.
    * 
    * @return location generator UDF, never <code>null</code>
    */
   public synchronized IPSUdfProcessor getGenerator()
   {
      if (m_generatorUDF == null)
         initGeneratorUDF();
      return m_generatorUDF;
   }

   /**
    * Caches the sys_casGeneratePubLocation UDF used to build pub locations.
    * Initialized in {@link #initGeneratorUDF()}, never <code>null</code> after 
    * that.
    */
   private IPSUdfProcessor m_generatorUDF = null;

   private static final Logger log = LogManager.getLogger(PSSiteFolderContentListLinkGenerator.class);

   /**
    * String constant for the assembly generation UDF.
    */
   private static final String GENERATE_PUB_LOCATION_REF =
      "Java/global/percussion/contentassembler/sys_casGeneratePubLocation";

   /**
    * Name of the HTML parameter used in Database Publishing. Each item's 
    * content URL will be added with this param only if it exists in the 
    * request.  
    */
   private static final String DBPUBACTION = "dbpubaction";
}
