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
package com.percussion.cas;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSConsole;
import com.percussion.util.IPSHtmlParameters;

import java.io.File;
import java.net.URLEncoder;

import org.w3c.dom.Document;

/**
 * This exit inserts the newly created item as the related item of the parent
 * item. This is specifically written to process the the action of create an
 * item and insert into the slot at the same time. This exit goes onto the modify
 * handler of the Content Editor System Definition file. Whenever the psredirect
 * contains "updaterelateditems" (means insert the item as child item) the exit
 * is processed. Processing involves modifying the psredirect  appropriately to
 * be acceptible by the updaterelateditems Rhythmyx resource that modifies the
 * related content. This exit makes use of parameters defined in
 * <code>PSModifyRelatedContent</code> class.
 *
 * @see PSModifyRelatedContent
 */
public class PSInsertAsRelatedItem implements IPSResultDocumentProcessor
{
   // see IPSResultDocumentProcessor#canModifyStyleSheet()
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   // see IPSExtensionDef#init(IPSExtensionDef, File)
   public void init(IPSExtensionDef extensionDef, File file)
      throws PSExtensionException
   {
      ms_fullExtensionName = extensionDef.getRef().toString();
   }

/* Implementation of the method from the interface <code>IPSResultDocumentProcessor</code> */
   public Document processResultDocument(Object[] params,
      IPSRequestContext request, Document resultDoc)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {
      request.printTraceMessage(ms_fullExtensionName + ": entering");

      if(request == null) //never happens
      {
         return resultDoc;
      }

      String psredirect =
         request.getParameter(IPSHtmlParameters.DYNAMIC_REDIRECT_URL);
      //This is the key that decides whether to continue processing or not.
      if(psredirect == null || psredirect.trim().length() == 0 ||
         psredirect.indexOf(KEY_REDIRECTURL) < 0)
      {
         return resultDoc;
      }

      try
      {
         //get the newly created item contentid
         String contentid = request.getParameter(IPSHtmlParameters.SYS_CONTENTID);
         if(contentid == null || contentid.length() < 1)
         {
            //contentid is empty - do nothing
            return resultDoc;
         }

         int index = psredirect.indexOf(PSModifyRelatedContent.PARAM_ITEMVARIANTID);
         String varid = "";
         if(index > 0)
         {
            varid = psredirect.substring(
               index + PSModifyRelatedContent.PARAM_ITEMVARIANTID.length() + 1);
            index = varid.indexOf('&');
            if(index > -1)
               varid = varid.substring(0, index);
         }
         /*
          * modify the psredirect to be acceptable by the updaterelateditems
          * resource.
          */
         psredirect =
            psredirect + "&"
            + PSModifyRelatedContent.PARAM_CONIDVARID
            + "=" + contentid + ";" + varid
            + "&" + IPSHtmlParameters.SYS_COMMAND
            + "=" + PSModifyRelatedContent.COMMAND_INSERT
            + "&" + PARAM_HTTPCALLER
            + "=" + URLEncoder.encode(
               request.getParameter(PARAM_HTTPCALLER).toString());

         request.setParameter(IPSHtmlParameters.DYNAMIC_REDIRECT_URL, psredirect);
      }
      catch(Exception e)
      {
         PSConsole.printMsg(ms_fullExtensionName, e);
         throw new PSExtensionProcessingException(ms_fullExtensionName, e);
      }
      request.printTraceMessage(ms_fullExtensionName + ": leaving");

      return resultDoc;
   }

   /**
    * The fully qualified name of this extension.
    */
   static private String ms_fullExtensionName = "";

   /**
    * The fully qualified name of this extension. This exit is processed only if
    * this key exists in the psredirect URL in the request.
    */
   static private String KEY_REDIRECTURL = "updaterelateditems";

   /**
     * HTML parameter representing the http caller which is the actuall page
     * from which the create and insert the item is invoked. The update related
     * items resource will redirect the page to this one.
    */
   static private String PARAM_HTTPCALLER = "httpcaller";
}
