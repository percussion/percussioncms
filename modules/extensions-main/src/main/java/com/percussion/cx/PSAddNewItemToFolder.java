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

package com.percussion.cx;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.extension.*;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSConsole;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.util.IPSHtmlParameters;
import org.w3c.dom.Document;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Exit to add a newly created item to a specified folder. This typically used
 * to create an item and add to a folder from the content explorer UI. The
 * contentid is parsed from the request HTML parameters. Exit assumes that the
 * psredirect URL parameter exists in the request has the target folder id is
 * one of the HTML parameters in the redirect URL. After validadting these HTML
 * parameters, it instantiates the <code>PSRelationshipProcessorProxy</code> to
 * add the content itm to the folder.
 */
public class PSAddNewItemToFolder implements IPSResultDocumentProcessor
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
         return resultDoc;

      Map<String,Object> htmlParams = request.getParameters();
      if(htmlParams == null)
      {
         handleException(new Exception(
          "HTML Parameter list must not be null or empty for this operation"));
      }

      String contentid = null;
      String psredirect = request.getParameter(
         IPSHtmlParameters.DYNAMIC_REDIRECT_URL);
      /*
       * This new parameter is for Cougar Assets to be placed in a folder.
       * TODO: We should duplicate the behavior of this extension using
       * the new request parameter in a new extension.
       * -Adam Gent
       */
      String assetFolderId = request.getParameter("sys_asset_folderid");
      try
      {

         //This is the key that decides whether to continue processing or not.
         if((psredirect == null || psredirect.trim().length() == 0 ||
            psredirect.indexOf(PARAM_SYS_FOLDERID) < 0) &&
            assetFolderId == null)
         {
            return resultDoc;
         }
         //get the newly created item contentid
         contentid = request.getParameter(
            IPSHtmlParameters.SYS_CONTENTID);
         if(contentid == null || contentid.length() < 1)
         {
            //contentid is empty - do nothing
            return resultDoc;
         }

         String folderid = "";
         if (psredirect != null) {
            int index = psredirect.indexOf(PARAM_SYS_FOLDERID);
            
            if(index >= 0)
            {
               folderid = psredirect.substring(index +
                  PARAM_SYS_FOLDERID.length() + 1);
               index = folderid.indexOf('&');
               if(index > -1)
                  folderid = folderid.substring(0, index);
            }
            //Folder id is not provided, so do nothing for this item
            if(folderid.trim().length() == 0)
            {
               request.printTraceMessage(ms_fullExtensionName +
                  ": folderid is not found to add the item to the folder");
               if (assetFolderId == null)
                  return resultDoc;
               folderid = assetFolderId;
            }
         }
         else {
            folderid = assetFolderId;
         }
         

         request.printTraceMessage(ms_fullExtensionName +
            ": Adding content item <" + contentid + "> to folder <" +
            folderid + ">");
         PSServerFolderProcessor proxy = PSServerFolderProcessor.getInstance();

         PSLocator locatorParent = new PSLocator(folderid, "1");
         List<PSLocator> locatorList = new ArrayList<>();
         locatorList.add(new PSLocator(contentid, "1"));
         proxy.addChildren(
            locatorList, locatorParent);

         request.printTraceMessage(ms_fullExtensionName + ": leaving");
         return resultDoc;
      }
      catch(Exception e)
      {
         PSConsole.printMsg(ms_fullExtensionName, e);
         throw new PSExtensionProcessingException(ms_fullExtensionName, e);
      }
      finally
      {
         if (psredirect != null && contentid != null
               && contentid.trim().length() > 0)
         {
            if (psredirect.indexOf(PARAM_SYS_CONTENTID) > 0)
            {
               // If there's an existing parameter, do nothing
               request.setParameter(IPSHtmlParameters.DYNAMIC_REDIRECT_URL,
                     psredirect);
            }
            else
            {
               // Otherwise add the sys_contentid parameter to the 
               // psredirect string
               request.setParameter(IPSHtmlParameters.DYNAMIC_REDIRECT_URL,
                     psredirect + "&" + PARAM_SYS_CONTENTID + "=" + contentid);
            }
         }
      }

   }

   /**
    * Helper method to handle any exception. Basically throws the exception as
    * <code>PSExtensionProcessingException</code>.
    * @param e the exception, assumed never <code>null</code>
    * @throws PSExtensionProcessingException 
    */
   private void handleException(Exception e)
      throws PSExtensionProcessingException
   {
      throw new PSExtensionProcessingException(ms_fullExtensionName, e);
   }

  /**
    * The fully qualified name of this extension. Nerver <code>null</code> or
    * <code>empty</code> after initialization.
    */
  private String ms_fullExtensionName = "";

   /**
    * HTML parameter representing sysid for the item.
    */
   private static final String PARAM_SYS_FOLDERID = "sys_folderid";

   /**
    * HTML parameter representing sysid for the item.
    */
   private static  final String PARAM_SYS_CONTENTID = "sys_contentid";
}
