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
package com.percussion.extensions.general;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.services.assembly.impl.nav.PSNavConfig;
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
