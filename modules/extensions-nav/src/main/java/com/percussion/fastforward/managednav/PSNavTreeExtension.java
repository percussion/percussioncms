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
package com.percussion.fastforward.managednav;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.error.PSExceptionUtils;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.assembly.impl.nav.PSNavConfig;
import com.percussion.util.IPSHtmlParameters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import java.util.Map;

/**
 * The Nav Tree Extension builds the Nav Tree. This extension is a post-exit
 * only. It builds the tree with the current node as the "self" node.
 * 
 * @author DavidBenua
 *  
 */
public class PSNavTreeExtension extends PSDefaultExtension
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
      // this exit never modifies the stylesheet
      return false;
   }

   /**
    * Process the request. Gets the XML document.
    * 
    * @see com.percussion.extension.IPSResultDocumentProcessor#processResultDocument(java.lang.Object[],
    *      com.percussion.server.IPSRequestContext, org.w3c.dom.Document)
    */
   public Document processResultDocument(Object[] params,
         IPSRequestContext req, Document resultDoc)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {
      log.debug("start of NavTreeExtension");

      PSNavUtil.logMap(req.getTruncatedParameters(), "Request Parameters", log);
      try
      {
         PSNavConfig config = PSNavConfig.getInstance(req);
         log.debug("Nav Theme is {}", req.getParameter(config.getNavThemeParamName()));

         String contentid = req.getParameter(IPSHtmlParameters.SYS_CONTENTID);
         String revision = req.getParameter(IPSHtmlParameters.SYS_REVISION);

         PSLocator loc = new PSLocator(contentid, revision);

         Map<String,String> landingPageParams = PSNavXMLUtils.getLandingPageMap(req);

         PSNavonStack stack = new PSNavonStack(req, loc);

         if (contentid.equals(stack.getId(0)))
         { //current id is same as top of stack
            log.debug("Request for root node");
            PSNavTree tree = config.retrieveNavTree(req);
            if (tree == null) 
            {
               tree = new PSNavTree(req);
               config.storeNavTree(tree, req);
            }
           return tree.toXml(req);
         }
         else
         {
            //should only happen on preview of NavonTree Variant l
            log.debug("getting tree document for preview");
            PSLocator stackLoc = stack.peek(0).getCurrentLocator();
            log.debug("top of stack locator {}", stackLoc.getPart(PSLocator.KEY_ID));
            return  PSNavTreeLinkExtension.getTreeVariantXMLClean(req,
                  stackLoc);
         }

      }
      catch (PSNavException e)
      {
         req.printTraceMessage(e.getMessage());
         log.error("PSNavException found: {}",PSExceptionUtils.getMessageForLog(e));
         log.debug(PSExceptionUtils.getDebugMessageForLog(e));
         log.error(PSExceptionUtils.getMessageForLog(e));

         throw new PSExtensionProcessingException(0, PSExceptionUtils.getMessageForLog(e));

      }
      catch (Exception ex)
      {
         log.error(getClass().getName(), ex);
         log.debug(ex.getMessage(),ex);
         throw new PSExtensionProcessingException(getClass().getName(), ex);
      }

   }

   /**
    * Reference to Log4j singleton object used to log any errors or debug info.
    */
   Logger log = LogManager.getLogger(getClass());
}
