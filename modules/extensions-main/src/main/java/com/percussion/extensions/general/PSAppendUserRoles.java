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

