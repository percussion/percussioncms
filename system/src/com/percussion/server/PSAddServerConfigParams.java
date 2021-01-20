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

package com.percussion.server;

import com.percussion.cms.objectstore.PSUserInfo;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This extension adds the server configuration parameters to the result
 * document. All parameters are added as last child elements of the specified
 * element by name via first parameter. Element name is optional and if not
 * specified the root element of the result document is considered.
 * <p>
 * Note: Only user session timeout is being added now. This class needs to be
 * modified if more parameters are required.
 * </p>
 */
public class PSAddServerConfigParams
   implements IPSResultDocumentProcessor
{
   /**
    * Required by the interface. This exit never modifies the stylesheet.
    * @see IPSResultDocumentProcessor#canModifyStyleSheet()
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   // see IPSExtensionDef#init(IPSExtensionDef, File)
   public void init(IPSExtensionDef extensionDef, File file)
      throws PSExtensionException
   {
   }
   /*
    * Implementation of the method defined by the interface
    */
   public Document processResultDocument(Object[] params,
      IPSRequestContext request, Document resultDoc)
      throws PSExtensionProcessingException
   {
      String elemName = "";
      if(params.length > 0 && params[0] != null)
         elemName = params[0].toString().trim();

      Element elem = null;
      if(elemName.length() > 1)
      {
         NodeList nl = resultDoc.getElementsByTagName(elemName);
         if(nl!=null && nl.getLength() > 0)
            elem = (Element)nl.item(0);
      }
      if(elem == null) //element name is not specified
      {
         elem = resultDoc.getDocumentElement();
      }
      //Element is not specified or locatable, fallback to the root element
      if(elem == null)
         return resultDoc;

      PSServerConfiguration config = PSServer.getServerConfiguration();
      int sessionTimeOut = config.getUserSessionTimeout();
      Element child = (Element)elem.appendChild(resultDoc.createElement(
         ELEM_SESSIONTIMEOUT));
      child.appendChild(
         resultDoc.createTextNode(String.valueOf(sessionTimeOut)));

      return resultDoc;
   }

   /**
    * Name of the element for the user session timeout in seconds. This will
    * be the last child element of the element whose name is specified via first
    * parameter (optional) to the extension. If not specified, the element is
    * added as last child of the root element of the document.
    */
   static public final String ELEM_SESSIONTIMEOUT =
      PSUserInfo.XML_ELEM_SESSIONTIMEOUT;
}