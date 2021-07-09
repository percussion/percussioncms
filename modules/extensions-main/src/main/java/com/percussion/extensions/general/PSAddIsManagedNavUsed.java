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

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.fastforward.managednav.PSNavConfig;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSConsole;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This exit adds <code>isManagedNavUsed=yes</code> attribute to the root 
 * element of the result document if the managed navigation is configured 
 * (or the managed navigation properties file exists) for this server.
 * <p>
 * The supplied parameters will be ignored.
 */
public class PSAddIsManagedNavUsed implements
              IPSResultDocumentProcessor
{
   // Implementation of the method required by the interface IPSExtension.
   public void init(IPSExtensionDef extensionDef, File file)
      throws PSExtensionException
   {
      ms_fullExtensionName = extensionDef.getRef().toString();
   }

   // Implementation of the method required by the interface IPSExtension.
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   // Implementation of the method required by the interface IPSExtension.
   public Document processResultDocument(Object[] params,
      IPSRequestContext request, Document resDoc)
         throws PSParameterMismatchException,
               PSExtensionProcessingException
   {
      if(params == null || resDoc == null)
         return resDoc;
      Element elem = resDoc.getDocumentElement();
      if(elem == null)
         return resDoc;
      try
      {
         if (PSNavConfig.isManagedNavUsed())
            elem.setAttribute("isManagedNavUsed", "yes");
         else
            elem.setAttribute("isManagedNavUsed", "no");
      }
      catch(Throwable t) //should never happen!
      {
         PSConsole.printMsg(ms_fullExtensionName, t);
      }

      return resDoc;
   }

   /**
    * The fully qualified name of this extension.
    */
   static private String ms_fullExtensionName = "";
}
