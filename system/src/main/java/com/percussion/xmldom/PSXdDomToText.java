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
