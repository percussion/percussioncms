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

package com.percussion.community;

//java
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSConsole;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This exit adds an communityenabled attribute to the root element of
 * result document. The value of this attribute is yes if communities
 * are enabled or no if communities are disabled.
 */
public class PSAddCommunityEnabledFlag implements
              IPSResultDocumentProcessor
{
   /*
    * Implementation of the method required by the interface IPSExtension.
    */
   public void init(IPSExtensionDef extensionDef, File file)
      throws PSExtensionException
   {
      ms_fullExtensionName = extensionDef.getRef().toString();
   }

   /*
    * Implementation of the method required by the interface
    * IPSResultDocumentProcessor.
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /*
    * Implementation of the method required by the interface
    * IPSResultDocumentProcessor.
    */
   public Document processResultDocument(Object[] params,
      IPSRequestContext request, Document resDoc)
         throws PSParameterMismatchException,
               PSExtensionProcessingException
   {
      try
      {
         Element elem = resDoc.getDocumentElement();
         if(PSAuthenticateUser.isCommunityEnabled())
         {
            elem.setAttribute(ATTR_COMMUNITIES_ENABLED,"yes");
         }
         else
         {
            elem.setAttribute(ATTR_COMMUNITIES_ENABLED,"no");
         }
      }
      catch(Throwable t)
      {
        PSConsole.printMsg(ms_fullExtensionName, t);
      }
      return resDoc;
   }

   /**
    * The fully qualified name of this extension.
    */
   static private String ms_fullExtensionName = "";

   /**
    * communities enalbed attribute name.
    */
   static private final String ATTR_COMMUNITIES_ENABLED = "communities_enabled";

}
