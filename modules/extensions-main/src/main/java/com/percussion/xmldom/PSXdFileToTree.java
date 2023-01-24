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

import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.general.IPSRhythmyxInfo;
import com.percussion.services.general.PSRhythmyxInfoLocator;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A Rhythmyx post-exit to load an XML document from the file system and add
 * it to the result document
 * <p>
 * <table border="1">
 *   <tr><th>Param #</th><th>Name</th><th>Description</th><th>Default</th><tr>
 *   <tr>
 *     <td>1</td>
 *     <td>fileName</td>
 *     <td>the name of the XML document file.  if path is not absolute, it
 * will be relative to the Rhythmyx server installation.</td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>2</td>
 *     <td>parentNode</td>
 *     <td>name of the element within the result document that file will be
 * appended to (or "." to select the root element)</td>
 *     <td>.</td>
 *   </tr>
 * </table>
 */
public class PSXdFileToTree extends PSDefaultExtension
      implements IPSResultDocumentProcessor
{
   /**
    * This method handles the post-exit request by loading the specified file,
    * parsing it into a Document, and appending it to the parentNode in the
    * result document.
    *
    * @param params an array of objects representing the parameters. See the
    * description under {@link PSXdFileToTree} for parameter details.
    * @param request the request context for this request
    * @param resultDoc the XML document resulting from the Rhythmyx server
    * operation.
    *
    * @throws PSParameterMismatchException if fileName is not supplied
    * @throws PSExtensionProcessingException when a run time error is detected.
    */
   public Document processResultDocument(Object[] params,
                                         IPSRequestContext request,
                                         Document resultDoc)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {
      PSXmlDomContext contxt =
            new PSXmlDomContext(this.getClass().toString(), request);

      // process the first param: the file name
      String fileName = PSXmlDomUtils.getParameter(params, 0, null);
      if (null == fileName) throw new PSParameterMismatchException(
            "missing required parameter: fileName");
      String rootDir = 
         (String) PSRhythmyxInfoLocator.getRhythmyxInfo().getProperty(
            IPSRhythmyxInfo.Key.ROOT_DIRECTORY);
      File sourceFile = new File(rootDir, fileName);

      // process the second param: the name of the result node to hold file
      String destNodeName = PSXmlDomUtils.getParameter(params, 1, ".");
      Element destNode =
            PSXmlDomUtils.findElement( destNodeName, resultDoc );
      if (null == destNode)
      {
         contxt.printTraceMessage( "Destination element " + destNodeName +
               " not found in result" );
         return resultDoc;
      }

      // load and parse the file and add it to the result doc
      try
      {
         Document sourceDoc = PSXmlDomUtils.loadXmlDocument(contxt, sourceFile);
         if (null != sourceDoc && null != sourceDoc.getDocumentElement())
         {
            Node importNode = resultDoc.importNode(
               sourceDoc.getDocumentElement(), true);
            destNode.appendChild(importNode);
         }
      } catch (Exception e)
      {
         contxt.handleException(e);
      }

      return resultDoc;
   }

   /**
    * This exit will never modify the stylesheet
    * @return <code>false</code>
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }

}
