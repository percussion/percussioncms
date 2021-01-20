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