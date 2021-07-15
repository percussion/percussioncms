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
package com.percussion.cms.objectstore.server;

import com.percussion.data.PSInternalRequestCallException;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This class is used by the CMS Layer to simulate outer joins when querying
 * objects.  It will replace a child element of a cms object's xml using the 
 * results of a query specified by an attribute on that child element.
 */
public class PSLoadChildDataExit extends PSDefaultExtension
   implements IPSResultDocumentProcessor
{
   /**
    * See interface for description.
    *
    * @return Always <code>false</code>.
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /**
    * See class description.
    *
    * @param params The parameters are as follows:
    * <ol>
    * <li>The name of the base element as a <code>String</code>, may not be
    * <code>null</code> or empty.  This is the parent of the element that will
    * contain the child data.</li>
    * <li>The name of the child element as a <code>String</code>, may not be
    * <code>null</code> or empty. This is the element that will contain the
    * child data</li>
    * <li>The name of the attribute on the child element that contains the query 
    * that will return the child data, as a <code>String</code>.  The returned 
    * xml element will replace the child element within the base element.</li>
    * </ol>
    *
    * @param request Guaranteed not <code>null</code> by interface.
    *
    * @param resultDoc Guaranteed not <code>null</code> by interface.
    *
    * @return The doc as supplied with the specified child element replaced by
    * the results of the query, never <code>null</code>.
    *
    * @throws PSParameterMismatchException if the correct parameters are not
    * supplied.
    * @throws PSExtensionProcessingException if there is an error parsing the
    * expected object XML in the supplied resultDoc or if a handler can't be
    * found for the supplied query.
    */
   public Document processResultDocument(Object[] params,
         IPSRequestContext request, Document resultDoc)
      throws PSParameterMismatchException, PSExtensionProcessingException
   {
      // get root element, return empty doc if supplied
      Element root = resultDoc.getDocumentElement();
      if (root == null)
         return resultDoc;
      
      if (params == null)
      {
         throw new PSParameterMismatchException("params not supplied (null)");
      }
      
      if (params.length == 0 || params[0] == null || 
         params[0].toString().trim().length() == 0)
      {
         throw new PSParameterMismatchException(
            "Base element not specified");
      }
      
      if (params.length < 2 || params[1] == null || 
         params[1].toString().trim().length() == 0)
      {
         throw new PSParameterMismatchException(
            "child element not specified");
      }
      
      if (params.length < 3 || params[2] == null || 
         params[2].toString().trim().length() == 0)
      {
         throw new PSParameterMismatchException(
            "query attribute not specified");
      }
      
      String baseElementName = params[0].toString();
      String childElementName = params[1].toString();
      String queryAttrName = params[2].toString();
      
      NodeList elements = root.getElementsByTagName(baseElementName);
      if (elements == null)
         return resultDoc;

      // need to collect elements into list since modifying the doc as we go
      // will slow down the performance of traversing the NodeList
      int len = elements.getLength();   
      List nodeList = new ArrayList(len);
      for (int i = 0; i < len; i++) 
      {
         nodeList.add(elements.item(i));
      }
      
      Iterator nodes = nodeList.iterator();
      while (nodes.hasNext())
      {
         Element baseEl = (Element)nodes.next();
         PSXmlTreeWalker tree = new PSXmlTreeWalker(baseEl);
         Element childEl = tree.getNextElement(childElementName, 
            PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
         if (childEl != null)
         {
            String query = childEl.getAttribute(queryAttrName);
            Element newChild = executeQuery(request, query);
            Element parent = (Element)childEl.getParentNode();
            if (parent != null && newChild != null)
            {
               newChild = (Element)resultDoc.importNode(newChild, true);
               parent.replaceChild(newChild, childEl);
            }
         }
      }
      
      return resultDoc;
   }
   
   /**
    * Executes the specified query as an internal request and returns the 
    * result doc's root element.
    * 
    * @param doc The doc to use, assumed not <code>null</code>.
    * @param req The request to use, assumed not <code>null</code>.
    * @param query The query to run, assumed not <code>null</code> or empty.
    * 
    * @return The element, may be <code>null</code> if a null or empty doc is 
    * returned.
    * 
    * @throws PSExtensionProcessingException if the requested resource doesn't 
    * exist or if there are any other errors.
    */
   private Element executeQuery(IPSRequestContext req, String query) 
      throws PSExtensionProcessingException
   {
      try 
      {
         IPSInternalRequest intReq = req.getInternalRequest(query);
         Document result = intReq.getResultDoc();
         return result.getDocumentElement();
      }
      catch (PSInternalRequestCallException e) 
      {
         throw new PSExtensionProcessingException(m_def.getRef().toString(), e);
      }
      
   }
}
