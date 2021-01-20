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
