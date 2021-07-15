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
package com.percussion.fastforward.managednav;

import com.percussion.cms.objectstore.PSContentTypeVariant;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.IPSHtmlParameters;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

/**
 * This class copies the Navon Tree variant into another assembler request. This
 * extension is intended for use on the Navon content assemblers.
 * <p>
 * The reason for the indirect approach is that by registering the NavTree
 * variant, the assembler cache will store the tree for us automatically. This
 * means that the tree will be built only the first time it is requested. Since
 * the same tree will be used repeatedly for each Navon variant that is
 * assembled, this should speed the process up.
 * <p>
 * The call to the Navon Tree assembler is made via internal request. The
 * resulting document replaces the current XML from the query.
 * <p>
 * This extension has no parameters.
 * 
 * @author DavidBenua
 *  
 */
public class PSNavTreeLinkExtension extends PSDefaultExtension
      implements
         IPSResultDocumentProcessor
{
   /**
    * This extension never modifies the stylesheet.
    * 
    * @see com.percussion.extension.IPSResultDocumentProcessor#canModifyStyleSheet()
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /**
    * Execute the extension. There are no parameters
    * 
    * @see com.percussion.extension.IPSResultDocumentProcessor#processResultDocument(java.lang.Object[],
    *      com.percussion.server.IPSRequestContext, org.w3c.dom.Document)
    */
   public Document processResultDocument(Object[] params,
         IPSRequestContext req, Document resultDoc)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {
      log.debug("start of NavTreeLinkExtension");
      try
      {
         Document rDoc = getTreeVariantXMLClean(req, null);

         return rDoc;
      }
      catch (PSNavException e)
      {
         req.printTraceMessage(e.getMessage());
         log.error("PSNavException found: {}", e.getMessage());
         log.debug(e.getMessage(), e);

         throw new PSExtensionProcessingException(0, e.getMessage());

      }
      catch (Exception ex)
      {
         log.error(ex.getMessage());
         log.debug(ex.getMessage(),ex);
         throw new PSExtensionProcessingException(this.getClass().getName(), ex);
      }
   }

   /**
    * Gets the Clean XML for the NavTree. The Clean XML has all of the links and
    * attributes cleaned up so that they match the parameters of the original
    * request.
    * 
    * @param req the parent request context
    * @param loc the locator of the self node. If <code>null</code> the
    *           content id and revision from the parent context will be used.
    * @return the clean XML documnet. Never <code>null</code>
    * @throws PSNavException
    */
   public static Document getTreeVariantXMLClean(IPSRequestContext req,
         PSLocator loc) throws PSNavException
   {
      log.debug("loading Clean XML");
      Map landingPageParams = PSNavXMLUtils.getLandingPageMap(req);

      if (loc == null)
      {
         log.debug("getting root from current request");
         loc = new PSLocator(req.getParameter(IPSHtmlParameters.SYS_CONTENTID),
               req.getParameter(IPSHtmlParameters.SYS_REVISION));
      }
      PSNavonStack stack = new PSNavonStack(req, loc);
      
      log.debug("Request for XML from cache");
    
      Document resDoc = getTreeVariantXMLRoot(req, stack.peek()
            .getCurrentLocator());
      PSNavTreeXMLUtils.AddAssemblerProperties(req, resDoc);
      PSNavXMLUtils.processXMLTree(req, resDoc, stack, landingPageParams);

      return resDoc;

   }

   /**
    * Gets the Raw XML for the NavTree. The Raw xml is the XML stored in the
    * cache. Only the root level attributes (e.g. the color selector and the
    * variable selector) have been modified.
    * 
    * @param req the parent request context.
    * @param loc the locator of the self node. If <code>null</code> the
    *           content id and revision from the parent context will be used.
    * @return the Raw XML document. Never <code>null</code>
    * @throws PSNavException
    */
   public static Document getTreeVariantXMLRaw(IPSRequestContext req,
         PSLocator loc) throws PSNavException
   {
      log.debug("loading Raw XML");
      if (loc == null)
      {
         log.debug("getting root from current request");
         loc = new PSLocator(req.getParameter(IPSHtmlParameters.SYS_CONTENTID),
               req.getParameter(IPSHtmlParameters.SYS_REVISION));
      }
      PSNavonStack stack = new PSNavonStack(req, loc);
      Document resDoc = getTreeVariantXMLRoot(req, stack.peek()
            .getCurrentLocator());
      PSNavXMLUtils.setRootAttributes(req, resDoc, stack);
      PSNavTreeXMLUtils.AddAssemblerProperties(req, resDoc);
      return resDoc;
   }

   /**
    * Gets the NavTree XML from the root.
    * 
    * @param req the parent request context.
    * @param loc the locator for the root node.
    * @return the NavTree XML document.
    * @throws PSNavException
    */
   public static Document getTreeVariantXMLRoot(IPSRequestContext req,
         PSLocator loc) throws PSNavException
   {
      PSNavConfig config = PSNavConfig.getInstance(req);

      log.debug("Loading Root XML");

      PSContentTypeVariant treeVar = config.getTreeVariant();
      if (treeVar == null)
      {
         String errMsg = "Tree Variant not found, check configuration";
         log.error(errMsg);
         throw new PSNavException(errMsg);
      }
      String treeURL = treeVar.getAssemblyUrl();
      log.debug("Tree Assembler URL is {}", treeURL);

      Map intParams = new HashMap();

      //always build tree in context 0, site 0.
      intParams.put(IPSHtmlParameters.SYS_CONTEXT, "0");
      intParams.put(IPSHtmlParameters.SYS_SITEID, "0");

      //use caller's AUTHTYPE
      PSNavUtil.copyParam(req, intParams, IPSHtmlParameters.SYS_AUTHTYPE);

      if (loc != null)
      {
         log.debug("Overriding Content Id and Revision");
         intParams.put(IPSHtmlParameters.SYS_CONTENTID, loc
               .getPart(PSLocator.KEY_ID));
         log.debug("Content Id {}", loc.getPart(PSLocator.KEY_ID));
         intParams.put(IPSHtmlParameters.SYS_REVISION, loc
               .getPart(PSLocator.KEY_REVISION));
         log.debug("Revision {}", loc.getPart(PSLocator.KEY_REVISION));
      }
      else
      {
         log.debug("Using default content id and revision");
         PSNavUtil.copyParam(req, intParams, IPSHtmlParameters.SYS_CONTENTID);
         PSNavUtil.copyParam(req, intParams, IPSHtmlParameters.SYS_REVISION);
      }

      String variantId = String.valueOf(treeVar.getVariantId());
      log.debug("Tree Variant is {}", variantId);
      intParams.put(IPSHtmlParameters.SYS_VARIANTID, variantId);

      // retrieves the cached XML document object from the NavConfig. This is
      // to avoid caching the potential huge XML doc (may be > 1MB) in the 
      // assembly cache with a big "maxPageSize", so we don't have to cache
      // the huge tree along with other huge binary data.
            
      log.debug("calling NavTree variant - with new cache (on3)");
      PSNavUtil.logMap(intParams, "Tree Variant Parameters", log);

      Document resDoc = config.retrieveNavTreeXML(intParams);
      
      if (resDoc == null) 
      {
         IPSInternalRequest intReq = req.getInternalRequest(treeURL, intParams,
            false);
         if (intReq == null)
         {
            String errMsg = "Cannot locate assembler for tree. Check configuration";
            log.error(errMsg);
            throw new PSNavException(errMsg);
          }
          try
          {
            log.debug("loading variant document");
            resDoc = intReq.getResultDoc();
          }
          catch (PSInternalRequestCallException e)
          {
            throw new PSNavException(PSNavTreeLinkExtension.class, e);
          }
          catch (Exception ex)
          {
            log.error(PSNavTreeXMLUtils.class.getName(), ex);
            log.debug(ex.getMessage(),ex);
            throw new PSNavException(PSNavTreeLinkExtension.class, ex);
          }

          log.debug("Storing XML document in cache");
          config.storeNavTreeXML(resDoc, intParams);
      }
      else 
      {
          log.debug("Got XML document from cache");
      }
      log.debug("Variant id = {}", req.getParameter(IPSHtmlParameters.SYS_VARIANTID));
      PSNavXMLUtils.overrideTheme(req, resDoc);
      return resDoc;
   }

   /**
    * Reference to Log4j singleton object used to log any errors or debug info.
    */
   private static final Logger log = LogManager.getLogger(PSNavTreeLinkExtension.class);

}
