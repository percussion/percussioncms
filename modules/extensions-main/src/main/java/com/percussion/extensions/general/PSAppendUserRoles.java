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

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSConsole;

import java.io.File;
import java.util.StringTokenizer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This post-exit appends user roles to the root element of the result document
 * as per the DTD below:
 * &lt;root&gt;
 *  &lt;UserRoles&gt;
 *  &lt;Role&gt;Admin&lt;/Role&gt;
 *  &lt;Role&gt;Author&lt;/Role&gt;
 *  &lt;/UserRoles&gt;
 * &lt;/root&gt;
 *
 * Also there is an optional parameter that can be supplied when we need to use
 * a differnt element name for UserRoles in the above document. However, the
 * child element (of UserRoles) name is always "Role'.
 */
public class PSAppendUserRoles
   implements IPSResultDocumentProcessor
{
   /*
    * Implementation of the method defined by the interface
    */
   public Document processResultDocument(Object[] params,
      IPSRequestContext request, Document resultDoc)
      throws PSExtensionProcessingException
   {
      try
      {
         String roles = request.
            getUserContextInformation("roles/rolename", "").toString();

         StringTokenizer tokenizer = new StringTokenizer(roles, ",");
         Element urelem = null;
         Element elem = null;
         String role = null;
         String userRolesElemeName = DEFAULT_USEROLE_ELEMENT_NAME;
         if(params != null && params.length > 0)
         {
            String tmp = params[0].toString().trim();
            if(tmp.length() > 0)
               userRolesElemeName = tmp;
         }
         urelem = (Element)resultDoc.createElement(userRolesElemeName);
         while(tokenizer.hasMoreTokens())
         {
            role = tokenizer.nextElement().toString().trim();
            elem = (Element)resultDoc.createElement("Role");
            elem.appendChild(resultDoc.createTextNode(role));
            urelem.appendChild(elem);
         }
         resultDoc.getDocumentElement().appendChild(urelem);
      }
      catch(Exception e)
      {
         PSConsole.printMsg("Exit:" + ms_fullExtensionName, e);
      }
      return resultDoc;
   }

   /*
    * Implementation of the method defined by the interface
    */
   public void init(IPSExtensionDef extensionDef, File file)
      throws PSExtensionException
   {
      ms_fullExtensionName = extensionDef.getRef().toString();
   }

   /*
    * Implementation of the method defined by the interface
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }
   
   /**
    * The fully qualified name of this extension.
    */
   static private String ms_fullExtensionName = "";

   /**
    * Default UserRoles element name
    */
   static private String DEFAULT_USEROLE_ELEMENT_NAME = "UserRoles";
}

