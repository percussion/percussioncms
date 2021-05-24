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
package com.percussion.extensions.general;

import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.fastforward.utils.PSUtils;
import com.percussion.server.IPSRequestContext;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.StringWriter;
import java.io.Writer;

/**
 * 
 * <p>
 * Title: PSIncrementalContentFilter
 * </p>
 * <p>
 * This is a Rhythmyx Post-Exit which will filter out contentitems from a
 * content list which have already been published or unpublished.
 * <p>
 * This exit assumes that it has been added to a standard Rhythmyx content list,
 * as defined in /Rhythmyx/DTD/contentlist.dtd. It examines each
 * <code>&lt;contentitem&gt;</code> node in the result document. All content
 * items which have a valid entry in the RXSITEITEMS table are removed from the
 * result document.
 * <p>
 * There are 2 parameters, the name of the Internal Request which is used to
 * perfom the lookup into RXSITEITEMS and the name of the switch parameter.
 * <p>
 * If the app/resource identified by the internal request is not valid, no items
 * get filtered and a trace message will be written to show the error.
 * <p>
 * The switch parameter is the <em>name</em> of an HTML parameter that is used
 * to switch the filter on and off. If this parameter is specified, the exit
 * will look for an HTML parameter with that name. If this parameter is found,
 * the exit will filter the content list. If the parameter is not found, then
 * the exit will return the content list unmodified. Note that the
 * <em>value</em> of the HTML parameter is not examined. Only the presence or
 * absence of the HTML parameter.
 * <p>
 * If the switch parameter is not specified, then the exit always filters the
 * content.
 * <p>
 * Typically, this parameter will be used when adding this exit to a generic
 * content list resource. In this manner, a single content list resource can be
 * used for both full and incremental editions, simply by registering it with
 * different HTML parameters.
 * <p>
 * The Internal request name must be in the form
 * <code>&lt;ApplicationName&gt;/&lt;RequestName&gt;</code>. This request is
 * assumed to take the following parameters: <table>
 * <tr>
 * <td>sys_contentid</td>
 * <td>The content id of the item</td>
 * </tr>
 * <tr>
 * <td>sys_variantid</td>
 * <td>The variant id of the item</td>
 * </tr>
 * <tr>
 * <td>sys_context</td>
 * <td>The context where the edition is published</td>
 * </tr>
 * <tr>
 * <td>sys_siteid</td>
 * <td>The site id where the edition is published</td>
 * </tr>
 * <tr>
 * <td>puboperation</td>
 * <td>normally <code>publish</code> or <code>unpublish</code></td>
 * </tr>
 * <tr>
 * <td>pubdate</td>
 * <td>the last modify date of the item. The resource will check for any rows
 * in RXSITEITEMS <b>after </b> this date.</td>
 * </tr>
 * </table>
 */
public class PSIncrementalContentFilter extends PSDefaultExtension
      implements
         IPSResultDocumentProcessor
{

   private static final Logger log = LogManager.getLogger(PSIncrementalContentFilter.class);

   /**
    * Process the request after the content list query has run.
    * 
    * @param params the callers parameters. Only the first element is examined.
    *           It must contain the name of the item status resource.
    * @param request the request context of the parent request. May not be
    *           <code>null</code>.
    * @param resultDoc the XML document returned by the parent request. Must
    *           conform to the <code>/Rhythmyx/DTD/contentlist.dtd</code>.
    *           May not be <code>null</code>.
    * @return the modified result document. Never <code>null</code>.
    * @throws PSParameterMismatchException when parameters are invalid.
    * @throws PSExtensionProcessingException when a run time error is detected.
    */
   public Document processResultDocument(Object[] params,
         IPSRequestContext request, Document resultDoc)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {

      if (null == params)
         throw new IllegalArgumentException("Parameters cannot be null.");
      if (null == request)
         throw new IllegalArgumentException("Request cannot be null.");
      if (null == resultDoc)
         throw new IllegalArgumentException("Result document cannot be null.");

      String internalReqName = params[0].toString();
      if (internalReqName.length() == 0)
      {
         request.printTraceMessage("no request name");
         throw new PSParameterMismatchException(
               "The request name must be provided");
      }
      String switchParameter = params[1].toString();
      if (switchParameter.length() > 0)
      { //the switchParameter is specified.
         // does it exist in the callers parameter map?
         if (request.getParameter(switchParameter) == null)
         { //no, not present
            request.printTraceMessage("HTML parameter " + switchParameter
                  + " not present. Filtering disabled.");
            return resultDoc; // don't filter if the parameter is not present.
         }
      }
      else
      {
         request.printTraceMessage("switch parameter not present");
      }

      request.printTraceMessage("filtering enabled");
      try
      {
         boolean isEmpty = true;
         Element rootNode = resultDoc.getDocumentElement();
         if (null == rootNode)
            return resultDoc;
         String context = rootNode.getAttribute(XML_ATTR_CONTEXT);
         PSXmlTreeWalker resultWalker = new PSXmlTreeWalker(rootNode);

         Element resElem = resultWalker.getNextElement(NODE_CONTENTITEM,
               resultWalker.GET_NEXT_ALLOW_CHILDREN);
         while (resElem != null)
         {
            if (PSUtils.isValid(resElem, request, internalReqName, context))
            {
               request.printTraceMessage("Keeping Item");
               isEmpty = false;
               resElem = resultWalker.getNextElement(NODE_CONTENTITEM,
                     resultWalker.GET_NEXT_ALLOW_SIBLINGS);
            }
            else
            {
               request.printTraceMessage("Removing Item");
               Element nextRes = resultWalker.getNextElement(NODE_CONTENTITEM,
                     resultWalker.GET_NEXT_ALLOW_SIBLINGS);
               rootNode.removeChild((Node) resElem);
               resElem = nextRes;
            }

         }

         if (isEmpty)
         {
            request.printTraceMessage("returning empty list");
            //this is to fool the publisher, which expects an empty
            // <contentitem> if this CList is empty.
            PSXmlDocumentBuilder.addEmptyElement(resultDoc, rootNode,
                  NODE_CONTENTITEM);
            return resultDoc;
         }
         else
         {
            StringWriter sw = new StringWriter();
            PSXmlDocumentBuilder.write(resultDoc, (Writer) sw);
            request.printTraceMessage("returning XML doc: \n" + sw.toString());
            return resultDoc;
         }
      }
      catch (Exception ex)
      {
         log.error(ex.getMessage());
         log.debug(ex.getMessage(), ex);
         throw new PSExtensionProcessingException(this.getClass().getName(), ex);
      }
   }

   /**
    * Method required by the IPSResultDocumentProcessor interface.
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }



   /**
    * The Element name for a content item
    */
   private static final String NODE_CONTENTITEM = "contentitem";

   /**
    * The XML attribute for context
    */
   private static final String XML_ATTR_CONTEXT = "context";


}
