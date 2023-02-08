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
 * This exit adds sys_defaultcommunityid,sys_defaultcommunityname and 
 * sys_defaulthomepageurl attributes to the root element of result document. 
 * These are the attributes of the user roles and are obtained from the user 
 * context. If any of the above parameters <code>null</code> then these 
 * attributes will get empty values.
 */
public class PSAddDefaultCommunity implements
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
         String defcommid  = "";
         String defcommname = PSAuthenticateUser.getUserRoleAttribute(request,
            PSAuthenticateUser.SYS_DEFAULTCOMMUNITY);
         if(null == defcommname)
         {
            defcommname = "";
         }
         else
         {
            defcommid = PSAuthenticateUser.getCommunityId(
               request, defcommname);
            if(null == defcommid)
               defcommid  = "";
         }
         elem.setAttribute(ATTR_DEFAULT_COMMUNITY_NAME,defcommname);
         elem.setAttribute(ATTR_DEFAULT_COMMUNITY_ID,defcommid);
         //Now get the home page url for the user from his roles
         String homepageurl = PSAuthenticateUser.getUserRoleAttribute(request,
            SYS_DEFAULT_HOMEPAGEURL);
         if(null == homepageurl)
         {
            homepageurl = "";
         }
         elem.setAttribute(ATTR_DEFAULT_HOMEPAGE_URL, homepageurl);
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
    * default community id attribute.
    */
   static private final String ATTR_DEFAULT_COMMUNITY_ID =
      "sys_defaultcommunityid";

   /**
    * default community name attribute.
    */
   static private final String ATTR_DEFAULT_COMMUNITY_NAME =
      "sys_defaultcommunityname";

   /**
    * default homepage URL attribute.
    */
   static private final String ATTR_DEFAULT_HOMEPAGE_URL =
      "sys_defaulthomepageurl";

   /**
    * Name of user default homepage URL property for the role.
    */
   static public final String SYS_DEFAULT_HOMEPAGEURL = "sys_defaultHomepageURL";
}
