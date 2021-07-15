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

package com.percussion.extensions.general;

// java
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSConsole;
import com.percussion.server.PSRequestValidationException;
import com.percussion.xml.PSXPathEvaluator;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This exit can be used for cascading delete operation, ie. deleting rows
 * from child tables when a row from parent table is deleted.
 * This method does not delete the child rows, instead it queries the
 * specified resource to obtain the rows from the child tables corresponding
 * to the parent table row which is being deleted. Then adds these rows to
 * the document which is processed by the update resource. The update
 * resource must include the mappings for these child table rows so that
 * these rows are deleted from the database. This ensures that all the rows
 * from the parent and child tables are deleted in a single transaction.
 * <p>
 * All the child elements of the root node of the XML document returned by
 * the query resource is appended to the root node of the XML document
 * obtained from the request context (ie, the document which is processed
 * by the update resource).
 */
public class PSCascadeDelete
   extends PSDefaultExtension
   implements IPSRequestPreProcessor
{
   /*
   * Implementation of the method in the interface
   * <code>com.percussion.extension.IPSRequestPreProcessor</code>
   * <p>
   * See {@link IPSExtension#init(IPSExtensionDef, File) init} for details.
   */
   public void init(IPSExtensionDef extensionDef, File file)
      throws PSExtensionException
   {
      super.init(extensionDef, file);
      ms_fullExtensionName = extensionDef.getRef().toString();
   }

   /*
   * Implementation of the method in the interface
   * <code>com.percussion.extension.IPSRequestPreProcessor</code>
   * <p>
   * See {@link IPSRequestPreProcessor#preProcessRequest(Object[],
   *      IPSRequestContext) preProcessRequest} for details.
   * <p>
   * @param params An array with elements as defined below.
   * <p>
   * Required Params
   * <p>
   * param[0] XPath expression to obtain the nodeset corresponding to the
   * parent table rows which are being deleted. If the XPath returns an
   * empty node set then the specified query resource to obtain the child
   * table rows is never called and no change is made to the document obtained
   * from the rquest context. The specified resource to obtain child rows
   * is called once for each parent row to be deleted.
   * <p>
   * param[1] URL of the query resource (relative to the Rhythmyx root) which
   * returns the rows of the child tables corresponding to the parent table row
   * which is being deleted. The resource should return an empty doc if no
   * child table row is to be deleted.
   * <p>
   * Optional params
   * <p>
   * param[2] to param[21]
   * Multiple name/value pairs specifying the html param name and value which
   * will be included when querying the specified resource to obtain the child
   * table rows. The value can be a literal or a xpath expression relative to
   * the currently selected node (ie, one of the nodes in the nodeset returned
   * by evaluating param[0]). For example, if the "name" attribute of the node
   * is to be used, specify "@name" for value.
   *  For example, the following can be supplied as parameters:
   * <ul>
   *   <li>param1 = "roleName"</li>
   *   <li>value1 = "./@name"</li>
   *   <li>param2 = "type"</li>
   *   <li>value2 = "10"</li>
   *   <li>param3 = "nodeTextValue"</li>
   *   <li>value3 = "."</li>
   * </ul>
   * <p>
   * If the param name or value is <code>null</code> or if the param name is
   * empty then the name value is ignored.
   * <p>
   * @param request the request context for this request,
   * never <code>null</code> (specified by the interface)
   *
   * @throws PSAuthorizationException Never.
   * @throws PSRequestValidationException Never.
   * @throws PSParameterMismatchException Never.
   * @throws PSExtensionProcessingException if the required params are missing
   * or invalid.
   */
   public void preProcessRequest(Object[] params, IPSRequestContext request)
      throws PSAuthorizationException,
            PSRequestValidationException,
            PSParameterMismatchException,
            PSExtensionProcessingException
   {
      request.printTraceMessage("PSCascadeDelete#preProcessRequest()");

      Document inputDoc = request.getInputDocument();
      if (inputDoc == null)
      {
         request.printTraceMessage("PSCascadeDelete#inputDoc is null");
         return;
      }
      Element parentRoot = inputDoc.getDocumentElement();
      if (parentRoot == null)
      {
         request.printTraceMessage("PSCascadeDelete#inputDoc root is null");
         return;
      }
      if (!parentRoot.hasChildNodes())
      {
         request.printTraceMessage(
            "PSCascadeDelete#inputDoc root has no child nodes");
         return;
      }

      String exMsg = "You must supply the XPath expression to obtain the " +
         "nodeset corresponding to the parent table rows which are being " +
         "deleted, and the URL of the query resource to obtain the " +
         "corresponding child table rows.";

      if ((params == null) || (params.length < 2) ||
         (params[0] == null) || (params[0].toString().trim().length() < 1) ||
         (params[0] == null) || (params[1].toString().trim().length() < 1))
      {
         throw new PSExtensionProcessingException(
            ms_fullExtensionName, new Exception(exMsg));
      }

      request.printTraceMessage("PSCascadeDelete#params = " + params);

      String xpath = params[0].toString().trim();
      String url = params[1].toString().trim();

      // build params map
      HashMap paramMap = new HashMap();
      int paramMaxIndex = params.length - 1;
      for (int paramIndex = 2; paramIndex < paramMaxIndex; paramIndex += 2)
      {
         Object key = params[paramIndex];
         Object val = params[paramIndex + 1];
         if ((key != null) && (val != null))
         {
            String strKey = key.toString().trim();
            String strVal = val.toString().trim();
            if (strKey.length() > 0)
               paramMap.put(strKey, strVal);
         }
      }

      IPSInternalRequest iReq = null;
      try
      {
         // get the node set corresponding to the deleted rows of the parent table
         PSXPathEvaluator xp = new PSXPathEvaluator(inputDoc);
         Iterator it = xp.enumerate(xpath, false);
         while (it.hasNext())
         {
            // for each node, evaluate the html param values
            Node node = (Node)it.next();
            PSXPathEvaluator xpNode = new PSXPathEvaluator(node);
            Map nodeParamMap = new HashMap(paramMap);
            Iterator paramIt = nodeParamMap.entrySet().iterator();
            while (paramIt.hasNext())
            {
               Map.Entry item = (Map.Entry)paramIt.next();
               String strVal = (String)item.getValue();
               try
               {
                  String newVal = xpNode.evaluate(strVal);
                  item.setValue(newVal);
               }
               catch (Exception e)
               {
               }
            }
            // query the resource to get the child table rows
            iReq = request.getInternalRequest(url, nodeParamMap, true);
            if (iReq == null)
               throw new PSExtensionProcessingException(0,
                  "Unable to locate handler for request: " + url);

            boolean isQuery =
               (iReq.getRequestType() == IPSInternalRequest.REQUEST_TYPE_QUERY);

            Document childDoc = null;
            if (isQuery)
               childDoc = iReq.getResultDoc();
            else
               iReq.performUpdate();

            if (childDoc != null)
            {
               Element childRoot = childDoc.getDocumentElement();
               if (childRoot != null)
               {
                  NodeList nl = childRoot.getChildNodes();
                  for (int i=0; i < nl.getLength(); i++)
                     PSXmlDocumentBuilder.copyTree(inputDoc, parentRoot, nl.item(i));
               }
               else
               {
                  request.printTraceMessage(
                     "PSCascadeDelete : Internal request returned empty doc");
               }
            }
         }
      }
      catch (Exception e)
      {
         PSConsole.printMsg(ms_fullExtensionName, e);
         throw new PSExtensionProcessingException(ms_fullExtensionName, e);
      }
   }

   /**
   * The fully qualified name of this extension, set in the <code>init()</code>
   * method. Never <code>null</code> or modified after that.
   */
   static private String ms_fullExtensionName = "";
}

