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

package com.percussion.xmldom;

import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import com.percussion.xml.PSXmlDocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A Rhythmyx post exit that copies an XML DOM tree from a temporary
 * private object into the result document.
 *
 * <p>The parameters for this exit are:
 * <table border="1">
 *   <tr><th>Param #</th><th>Description</th><th>Required?</th>
 *   <th>default value</th><tr>
 *   <tr>
 *     <td>1</td>
 *     <td>the name of the temporary XML document object.</td>
 *     <td>no</td>
 *     <td>XMLDOM</td>
 *   </tr>
 *   <tr>
 *     <td>2</td>
 *     <td>Source node name</td>
 *     <td>no</td>
 *     <td>"."</td>
 *   </tr>
 *   <tr>
 *     <td>3</td>
 *     <td>destination node in the result document.</td>
 *     <td>no</td>
 *     <td>"."</td>
 *   </tr>
 * </table>
 *
 * <p>The source and destination nodes may be omitted.  If the
 * source node is omitted or is "." (the period or dot character), the
 * source is the entire XML document contained in the object.</p>
 * <p> If the destination node is omitted, the source node will added
 * as a direct child of the document element (sometimes called root element)
 * of the XML result document.
 * </p>
 *
 */
public class PSXdCopyDom extends PSDefaultExtension
          implements IPSResultDocumentProcessor
{
  /**
   * Copy a subtree of the source XML document into the result document.
   *
   * @param params an array of parameters to the post exit.
   * See {@link PSXdCopyDom } for parameter details.
   *
   * @param request the com.percussion.server.IPSRequestContext object
   * for this particular request.
   *
   * @param resultDoc the org.w3c.dom.Document that results from the resource
   *  execution.
   *
   * @return the XML document to pass on to server for further processing.
   *
   * @throws PSExtensionProcessorException When an unexpected error condition
   *    occurs.
   * @throws PSParameterMismatchException This class will never throw this
   * exception.
   *
  **/
  public org.w3c.dom.Document processResultDocument(Object[] params,
           IPSRequestContext request, Document resultDoc)
             throws PSParameterMismatchException, PSExtensionProcessingException
    {

      PSXmlDomContext contxt = new PSXmlDomContext("PSXdCopyDom",request);

      String sourceObjectName = PSXmlDomUtils.getParameter(params,0,
                         PSXmlDomUtils.DEFAULT_PRIVATE_OBJECT);
      String sourceNodeName = PSXmlDomUtils.getParameter(params,1,".");
      String destNodeName = PSXmlDomUtils.getParameter(params,2,".");

      try
      {
         Document sourceDoc =
               (Document) request.getPrivateObject( sourceObjectName );
         if (null == sourceDoc)
         {
            contxt.printTraceMessage( "Source object not found: " +
                  sourceObjectName );
            return resultDoc;
         }

         Element sourceNode =
               PSXmlDomUtils.findElement( sourceNodeName, sourceDoc );
         if (null == sourceNode)
         {
            contxt.printTraceMessage( "Source element " + sourceNodeName +
                  " not found in source" );
            return resultDoc;
         }

         Element destNode =
               PSXmlDomUtils.findElement( destNodeName, resultDoc );
         if (null == destNode)
         {
            contxt.printTraceMessage( "Destination element " + destNodeName +
                  " not found in result" );
            return resultDoc;
         }

         PSXmlDocumentBuilder.copyTree( resultDoc, destNode, (Node) sourceNode );
      }
      catch (Exception e) { contxt.handleException(e); }

      return resultDoc;

  }

  /**
   * This exit will never modify the style sheet. This method is required by the
   * interface.
   */
  public boolean canModifyStyleSheet()
  {
      return false;
  }
}
