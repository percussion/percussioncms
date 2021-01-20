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
      Set<File> ctrlFiles = new HashSet<File>();
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
