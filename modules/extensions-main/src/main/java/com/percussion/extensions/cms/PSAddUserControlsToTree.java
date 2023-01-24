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

package com.percussion.extensions.cms;

import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSCustomControlManager;
import com.percussion.services.general.IPSRhythmyxInfo;
import com.percussion.services.general.PSRhythmyxInfoLocator;
import com.percussion.xmldom.PSXmlDomContext;
import com.percussion.xmldom.PSXmlDomUtils;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A Rhythmyx post-exit to load all user control XML documents from the file
 * system and add to the result document.
 */
public class PSAddUserControlsToTree extends PSDefaultExtension
      implements IPSResultDocumentProcessor
{
   /**
    * This method handles the post-exit request by loading the specified file,
    * parsing it into a Document, and appending it to the parentNode in the
    * result document.
    *
    * @param params an array of objects representing the parameters.  Not used.
    * @param request the request context for this request
    * @param resultDoc the XML document resulting from the Rhythmyx server
    * operation.
    * 
    * @return The XML result document.
    *
    * @throws PSExtensionProcessingException when a run time error is detected.
    */
   public Document processResultDocument(Object[] params,
                                         IPSRequestContext request,
                                         Document resultDoc)
      throws PSExtensionProcessingException
   {
      PSXmlDomContext contxt =
            new PSXmlDomContext(this.getClass().toString(), request);

      // collect user control files
      Set<File> ctrlFiles = new HashSet<>();
      String rootDir = 
         (String) PSRhythmyxInfoLocator.getRhythmyxInfo().getProperty(
            IPSRhythmyxInfo.Key.ROOT_DIRECTORY);
      ctrlFiles.add(new File(rootDir,
            "rx_resources/stylesheets/rx_Templates.xsl"));
      ctrlFiles.addAll(PSCustomControlManager.getInstance().getControlFiles());      
      
      Element destNode =
            PSXmlDomUtils.findElement(".", resultDoc);
      if (destNode == null)
      {
         contxt.printTraceMessage( "Destination element . not found in result");
         return resultDoc;
      }

      // load and parse the files and add to the result doc
      for (File ctrlFile : ctrlFiles)
      {
         try
         {
            Document sourceDoc = PSXmlDomUtils.loadXmlDocument(contxt,
                  ctrlFile);
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
