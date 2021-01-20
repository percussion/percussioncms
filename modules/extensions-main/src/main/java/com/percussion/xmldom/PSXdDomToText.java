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

import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;

import org.w3c.dom.Document;

/**
 * A Rhythmyx extension that copies a temporary XML document into a field.
 * <p>
 * The text representation of the node or document is copied into a single field.
 * When called as a pre-exit, the output is stored in an HTML parameter. When
 * called as a post-exit, a single XML node is created which will contain the
 * resulting text.  When an entire XML document is copied, the result will be
 * escaped.
 * <p>
 * The parameters to the extension are:
 *
 * <table border="1">
 *   <tr><th>Param #</th><th>Name</th><th>Description</th><th>Required?</th>
 *   <th>default value</th><tr>
 *   <tr>
 *     <td>1</td>
 *     <td>sourceObjectName</td>
 *     <td>the name of the temporary XML document object.</td>
 *     <td>no</td>
 *     <td>XMLDOM</td>
 *   </tr>
 *   <tr>
 *     <td>2</td>
 *     <td>sourceNodeName</td>
 *     <td>identifies the node within the source document which will be copied</td>
 *     <td>no</td>
 *     <td>"." will cause the entire document to be copied.</td>
 *   </tr>
 *   <tr>
 *     <td>3</td>
 *     <td>destNodeName</td>
 *     <td>destination node in the result document.</td>
 *     <td>no</td>
 *     <td>&nbsp;</td>
 *   </tr>
 * </table>
 * <p>
 * When called as a pre-exit, the sourceName <code>InputDocument</code> can
 * also be supplied. This name refers to the XML document loaded with the
 * update request.
 * <p>
 * The output for a pre-exit is always stored in an HTML parameter.
 * <p>
 * When called as a post-exit, the output is a node in the XML result document
 * The special node name "." (period)  will cause the new document fragment
 * to be copied directly underneath the <code>&lt;root&gt;</code> element.
 * <p>
**/
public class PSXdDomToText extends PSDefaultExtension implements IPSRequestPreProcessor,
    IPSResultDocumentProcessor
{

   /**
    * This method handles the pre-exit request.
    *
    * @param params an array of objects representing the parameters. See the
    * description under {@link PSXdDomToText} for parameter details.
    *
    * @param request the request context for this request
    *
    * @throws PSExtensionProcessingException when a run time error is detected.
    *
    */
   public void preProcessRequest(Object[] params, IPSRequestContext request)
      throws PSAuthorizationException, PSRequestValidationException,
         PSParameterMismatchException, PSExtensionProcessingException
   {
      PSXmlDomContext contxt = new PSXmlDomContext(ms_className, request);

      String sourceObjectName = PSXmlDomUtils.getParameter(params,0,
         PSXmlDomUtils.DEFAULT_PRIVATE_OBJECT);
      String sourceNodeName = PSXmlDomUtils.getParameter(params,1,"");
      String destName = PSXmlDomUtils.getParameter(params,2,"");

      Document sourceDoc;
      if(sourceObjectName.equals("InputDocument"))
      {
         sourceDoc = request.getInputDocument();
      }
      else 
      {
         sourceDoc = (Document)request.getPrivateObject(sourceObjectName);
      }

      if(sourceDoc == null) 
      {
         request.printTraceMessage("Source Document not present");
         return;
      }

      String resultText = PSXmlDomUtils.copyTextFromDocument(contxt,sourceDoc,
         sourceNodeName);
      request.setParameter(destName, resultText);
   }

   /**
    * This method handles the post-exit request.
    *
    * @param params an array of objects representing the parameters. See the
    * description under {@link PSXdDomToText} for parameter details.
    *
    * @param request the request context for this request
    *
    * @param resultDoc the XML document resulting from the Rhythmyx server
    * operation.  The output text will be added as an XML node in this document.
    *
    * @throws PSExtensionProcessingException when a run time error is detected.
    *
    */
   public org.w3c.dom.Document processResultDocument(Object[] params,
      IPSRequestContext request, Document resultDoc)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {
      PSXmlDomContext contxt = new PSXmlDomContext(ms_className, request);

      String sourceObjectName = PSXmlDomUtils.getParameter(params, 0,
         PSXmlDomUtils.DEFAULT_PRIVATE_OBJECT);
      String sourceNodeName = PSXmlDomUtils.getParameter(params, 1, "");
      String destName = PSXmlDomUtils.getParameter(params, 2, "");

      Document sourceDoc = (Document)request.getPrivateObject(sourceObjectName);
      if(sourceDoc == null)
      {
         request.printTraceMessage("Source Document not present");
         return resultDoc;
      }

      String resultText = PSXmlDomUtils.copyTextFromDocument(contxt, sourceDoc,
         sourceNodeName);

      try
      {
         PSXmlDomUtils.addResultNode(contxt, resultDoc, destName, resultText);
      }
      catch (Exception e) { contxt.handleException(e); }

      return resultDoc;
   }

   /**
    * This exit will never modify the stylesheet.
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /**
    * the name of the class: used for error handling.
    */
   private static final String  ms_className = "PSXdDomToText";
}
