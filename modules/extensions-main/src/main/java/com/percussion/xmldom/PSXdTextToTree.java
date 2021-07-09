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
package com.percussion.xmldom;

import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import com.percussion.xml.PSXmlTreeWalker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

/**
 * A Rhythmyx post-exit called to transform a text node into an XML tree
 * and add it to the result Document.
 */
public class PSXdTextToTree extends PSDefaultExtension
      implements IPSResultDocumentProcessor
{
   /**
    * This method handles the post-exit request.
    *
    * @param params an array of objects representing the parameters. See the
    * description under {@link PSXdTextToTree} for parameter details.
    *
    * @param request the request context for this request
    *
    * @param resultDoc the XML document resulting from the Rhythmyx server
    * operation.  The output text will be added as an XML node in this document.
    *
    * @throws PSExtensionProcessingException when a run time error is detected.
    *
    **/
   public Document processResultDocument(Object[] params,
                                         IPSRequestContext request,
                                         Document resultDoc)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {
      PSXmlDomContext contxt = new PSXmlDomContext(ms_className, request);
      String textSourceName = PSXmlDomUtils.getParameter(params, 0, null);
      if (null == textSourceName)
         throw new PSExtensionProcessingException(0, "Empty or null source name");

      setupContextParameters(contxt, params);

      try
      {
         request.printTraceMessage("Source name is: " + textSourceName);
         Iterator it = getNodes(resultDoc, textSourceName);
         while (it.hasNext())
         {
            Node sourceNode = (Node)it.next();
            request.printTraceMessage("Source node is " + sourceNode.getNodeName()
                                      + " -- " + sourceNode.toString());
            String textSource = PSXmlTreeWalker.getElementData(sourceNode);
            request.printTraceMessage("Source node is: " + textSource);

            Document tempResult =
                  PSXmlDomUtils.loadXmlDocument(contxt, textSource);

            // make sure the result is valid before using it (fix Rx-01-10-0061)
            if (null != tempResult && null != tempResult.getDocumentElement())
            {
               removeAllTextNodes(sourceNode);  // clean out destination node
               Node importNode = resultDoc.importNode(
                  tempResult.getDocumentElement(), true);
               sourceNode.appendChild(importNode);
            }
         }
      }
      catch (SAXParseException e)
      {
         // shouldn't get this (as PSXdTextCleanup should ensure well-formed)
         // but if we do, it isn't fatal
         contxt.handleException(e, false);
      }
      catch (Exception e)
      {
         contxt.handleException(e);
      }

      return resultDoc;
   }

   /**
    * Returns a list which is either empty or contains a single element. This
    * element is the first node in the document with the specified tag name
    * <code>textSourceName</code>
    *
    * @param resultDoc the document in which to search for the node with the
    * specified tag name, may not be <code>null</code>
    * @param textSourceName the tag name of the element to search for in the
    * document, the returned list contains the reference to the first element
    * in the document with matching tag name, may not be <code>null</code> or
    * empty
    *
    * @return an iterator over an empty or a single element list, never
    * <code>null</code>. The list contains <code>org.w3c.dom.Node</code> objects
    *
    * @throws IllegalArgumentException if <code>resultDoc</code> is
    * <code>null</code> or if <code>textSourceName</code> is <code>null</code>
    * or empty
    */
   protected Iterator getNodes(Document resultDoc, String textSourceName)
   {
      if (resultDoc == null)
         throw new IllegalArgumentException("resultDoc may not be null");

      if (textSourceName == null)
         throw new IllegalArgumentException(
            "textSourceName may not be null or empty");

      List nodeList = new ArrayList();
      PSXmlTreeWalker sourceWalker = new PSXmlTreeWalker(resultDoc);
      int flags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
         PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;
      Node sourceNode = sourceWalker.getNextElement(textSourceName, flags);
      if (sourceNode != null)
         nodeList.add(sourceNode);
      return nodeList.iterator();
   }

   /**
    * This exit will never modify the stylesheet
    **/
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /**
    * This method performs the common parameter checking and parsing.
    * @param contxt the XMLDOM context for this request
    * @param params the parameter object array from the original request.
    **/
   private void setupContextParameters(PSXmlDomContext contxt, Object[] params)
         throws PSExtensionProcessingException
   {
      // TODO: this method should be migrated to PSXmlDomContext
      if (params.length >= 2 && null != params[1]
            && params[1].toString().trim().length() > 0)
      {
         try
         {
            contxt.setTidyProperties(params[1].toString().trim());
         }
         catch (IOException e)
         {
            contxt.printTraceMessage("Tidy Properties file " +
                                     params[1].toString().trim() + " not found ");
            throw new PSExtensionProcessingException(ms_className, e);
         }
      }
      if (params.length >= 3 && null != params[2]
            && params[2].toString().trim().length() > 0)
      {
         contxt.setServerPageTags(params[2].toString().trim());
      }
   }

   /**
    * Remove all text and entity nodes from an element
    * @param el the parent element whose child nodes will be removed.
    **/
   private static void removeAllTextNodes(Node el)
   {
      NodeList children = el.getChildNodes();
      Node ch;
      int i;
      for (i = 0; i < children.getLength(); i++)
      {
         ch = children.item(i);
         if (ch.getNodeType() == Node.TEXT_NODE ||
               ch.getNodeType() == Node.ENTITY_REFERENCE_NODE)
         {
            el.removeChild(ch);
         }
      }
   }

   /**
    * The function name used for error handling
    */
   protected static final String ms_className = "PSXdTextToTree";
}
