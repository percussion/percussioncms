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
package com.percussion.cms.objectstore.server;

import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;

/**
 * This exit checks for the presence of a root node in the supplied doc. If
 * there isn't one, it adds an empty one with a name supplied as a parameter.
 * This was created for use with resources used by CMS objectstore components.
 *
 * @author Paul Howard
 * @version 1.0
 */
public class PSEmptyDocExit extends PSDefaultExtension
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
    * @param params Must have at least 1, non-<code>null</code> entry.
    *    The first entry is taken and a toString is performed to get the name
    *    of the root element.
    *
    * @param request
    *
    * @param resultDoc Guaranteed not <code>null</code> by interface.
    *
    * @return The doc as supplied if it had a root node, otherwise,
    *    it will have a new root node whose name is the 1st param supplied to
    *    this method.
    *
    * @throws PSParameterMismatchException If the required parameter
    *    is missing.
    *
    * @throws PSExtensionProcessingException Never.
    */
   public Document processResultDocument(Object[] params,
         IPSRequestContext request, Document resultDoc)
      throws PSParameterMismatchException, PSExtensionProcessingException
   {
      if (null == params || params.length == 0
            || params[0].toString().trim().length() == 0)
      {
         throw new PSParameterMismatchException("MISSING_ROOT_NAME",
               "A parameter that gives the root node name must be supplied.");
      }

      String rootName = params[0].toString();
      if (resultDoc.getDocumentElement() == null)
         PSXmlDocumentBuilder.createRoot(resultDoc, rootName);
      return resultDoc;
   }
}
