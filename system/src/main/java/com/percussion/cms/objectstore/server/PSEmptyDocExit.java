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
