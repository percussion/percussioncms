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

import com.percussion.data.PSConversionException;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionManager;
import com.percussion.extension.PSExtensionRef;
import com.percussion.extension.PSSimpleJavaUdfExtension;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSServer;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author dougrand
 * 
 * Query the extension manager to provide a list of matching extensions.
 */
public class PSQueryExtensions extends PSSimpleJavaUdfExtension
{

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.extension.IPSUdfProcessor#processUdf(java.lang.Object[],
    *      com.percussion.server.IPSRequestContext)
    */
   public Object processUdf(Object[] params, IPSRequestContext request)
         throws PSConversionException
   {
      if (params.length < 1)
      {
         throw new PSConversionException(0,
               "At least one argument (root element name) must be specified");
      }

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      String elementName = params[0].toString();
      Element root = doc.createElement(elementName);
      doc.appendChild(root);

      String interfacePattern = null;
      String extensionNamePattern = null;

      if (params.length > 1)
      {
         extensionNamePattern = params[1].toString();
         if (extensionNamePattern.trim().length() == 0)
         {
            extensionNamePattern = null;
         }
      }

      if (params.length > 2)
      {
         interfacePattern = params[2].toString();
         if (interfacePattern.trim().length() == 0)
         {
            interfacePattern = null;
         }
      }

      PSExtensionManager mgr = (PSExtensionManager) PSServer
            .getExtensionManager(null);
      Iterator iterator;
      try
      {
         iterator = mgr.getExtensionNames(null, null, interfacePattern,
               extensionNamePattern);
      }
      catch (PSExtensionException e)
      {
         throw new PSConversionException(0, "Problem querying extensions: " +
               e.getLocalizedMessage());
         
      }
      while (iterator.hasNext())
      {
         PSExtensionRef exit = (PSExtensionRef) iterator.next();
         Element xfaceEl = doc.createElement("extension");
         Node text = doc.createTextNode(exit.getFQN());
         xfaceEl.setAttribute("name", exit.getExtensionName());
         xfaceEl.setAttribute("category", exit.getCategory());
         xfaceEl.setAttribute("context", exit.getContext());
         xfaceEl.setAttribute("handler", exit.getHandlerName());
         xfaceEl.appendChild(text);
         root.appendChild(xfaceEl);
      }

      return root;
   }

}
