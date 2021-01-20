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
package com.percussion.fastforward.managednav;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.IPSHtmlParameters;

import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

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
      m_log.debug("start of NavTreeExtension");

      PSNavUtil.logMap(req.getTruncatedParameters(), "Request Parameters", m_log);
      try
      {
         PSNavConfig config = PSNavConfig.getInstance(req);
         m_log.debug("Nav Theme is "
               + req.getParameter(config.getNavThemeParamName()));

         String contentid = req.getParameter(IPSHtmlParameters.SYS_CONTENTID);
         String revision = req.getParameter(IPSHtmlParameters.SYS_REVISION);

         PSLocator loc = new PSLocator(contentid, revision);

         Map landingPageParams = PSNavXMLUtils.getLandingPageMap(req);

         PSNavonStack stack = new PSNavonStack(req, loc);

         if (contentid.equals(stack.getId(0)))
         { //current id is same as top of stack
            m_log.debug("Request for root node");
            PSNavTree tree = config.retrieveNavTree(req);
            if (tree == null) 
            {
               tree = new PSNavTree(req);
               config.storeNavTree(tree, req);
            }
            Document treeDoc = tree.toXml(req);
            return treeDoc;
         }
         else
         {
            //should only happen on preview of NavonTree Variant l
            m_log.debug("getting tree document for preview");
            PSLocator stackLoc = stack.peek(0).getCurrentLocator();
            m_log.debug("top of stack locator "
                  + stackLoc.getPart(PSLocator.KEY_ID));
            Document doc = PSNavTreeLinkExtension.getTreeVariantXMLClean(req,
                  stackLoc);
            return doc;
         }

      }
      catch (PSNavException e)
      {
         req.printTraceMessage(e.getMessage());
         m_log.error("PSNavException found:" + e.getMessage());
         m_log.error(getClass().getName(), e);
         System.out.println(e.getMessage());
         e.printStackTrace();

         throw new PSExtensionProcessingException(0, e.getMessage());

      }
      catch (Exception ex)
      {
         m_log.error("unexcepted exception");
         m_log.error(getClass().getName(), ex);
         ex.printStackTrace();
         throw new PSExtensionProcessingException(getClass().getName(), ex);
      }

   }

   /**
    * Reference to Log4j singleton object used to log any errors or debug info.
    */
   Logger m_log = Logger.getLogger(getClass());
}