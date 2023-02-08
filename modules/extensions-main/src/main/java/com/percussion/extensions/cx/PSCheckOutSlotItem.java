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
package com.percussion.extensions.cx;

import com.percussion.cms.IPSConstants;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSConsole;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSHtmlParameters;
import com.percussion.workflow.PSWorkFlowUtils;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Checks out a slot a item before expanding it. If the item is already
 * checked out by someone else, then does not show the slots for that item.
 */
public class PSCheckOutSlotItem extends PSDefaultExtension
   implements IPSResultDocumentProcessor
{
   // see IPSResultDocumentProcessor#canModifyStyleSheet()
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /**
    * If item is checked out to some one else then removes the child nodes
    * and retunrs the root node, if the item is not checked out to anybody
    * then tries to check out the item. If succeds returns the result doc as
    * is, fails removes the children and returns the root node.
    * If the item is checked out by the login user returns the result doc as is.
    */
   public Document processResultDocument(Object[] params,
      IPSRequestContext request, Document doc)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {
      String checkoutstatus = doc.getDocumentElement().
         getAttribute(ATTR_CHECKOUTSTATUS);
      int assignmenttype = Integer.parseInt(doc.getDocumentElement().
         getAttribute(ATTR_ASSIGNMENTTYPE));
      if (checkoutstatus.trim().length() == 0
         || checkoutstatus.equalsIgnoreCase(
            IPSConstants.CHECKOUT_STATUS_SOMEONEELSE)
         || assignmenttype < 2)
      {
         Element root = doc.getDocumentElement();
         while(root.hasChildNodes())
         {
            root.removeChild(root.getFirstChild());
         }
      }
      else if (
         checkoutstatus.equalsIgnoreCase(IPSConstants.CHECKOUT_STATUS_NOBODY))
      {
         String contentid = PSHtmlParameters.get(
            IPSHtmlParameters.SYS_CONTENTID, request);
         if(contentid != null && contentid.length() > 0)
         {
            Map chkoutMap = new HashMap();
            chkoutMap.put(IPSHtmlParameters.SYS_CONTENTID,contentid);
            chkoutMap.put(IPSHtmlParameters.SYS_COMMAND,WORKFLOW_COMMAND_NAME);
            chkoutMap.put(PSWorkFlowUtils.DEFAULT_ACTION_TRIGGER_NAME,
               WORKFLOW_CHECKOUT);
            try
            {
               String ceresource = getCeResourceName(contentid, request);
               IPSInternalRequest iReq1 =
                  request.getInternalRequest(ceresource,chkoutMap,false);
               iReq1.makeRequest();
               iReq1.cleanUp();
            }
            catch(Exception e)
            {
               Element root = doc.getDocumentElement();
               while(root.hasChildNodes())
               {
                  root.removeChild(root.getFirstChild());
               }
               PSConsole.printMsg("Extension", e);
            }
         }
      }
      return doc;
   }

   /**
    * This helper method gets the contenteditors inernalrequest name for
    * the given contentid.
    * @param contentid assumed not <code>null<code>
    * @param request IPSRequestContext assumed not <code>null<code>
    * @return internal request resource for the content editor of the item
    */
   private String getCeResourceName(String contentid, IPSRequestContext request)
   {
      Document doc = null;
      IPSInternalRequest iReq = null;
      try
      {
         request.setParameter(IPSHtmlParameters.SYS_CONTENTID, contentid);
         iReq = request.getInternalRequest(CONTENT_EDITOR_URLS);
         iReq.makeRequest();
         doc = iReq.getResultDoc();
      }
      catch(Exception e)
      {
         return null;
      }
      finally
      {
         if(iReq != null)
            iReq.cleanUp();
      }
      if(doc == null)
         return null;
      String ceurl = doc.getDocumentElement().getAttribute(ATTR_EDITURL);
      String result = "";
      //assumes at least one parameter i.e. contentid is part of the url
      int loc = ceurl.indexOf(".htm");
      if(loc < 0)
         return result;
      ceurl = ceurl.substring(0, loc);
      String ceresource = "";
      loc = ceurl.lastIndexOf('/');
      if(loc < 0)
         return result;
      ceresource = ceurl.substring(loc+1);
      ceurl = ceurl.substring(0, loc);
      loc = ceurl.lastIndexOf('/');
      if(loc < 0)
         return result;
      ceurl = ceurl.substring(loc+1);
      return (ceurl + "/" + ceresource);
   }
   /**
    * Constant for workflow sys command name
    */
   public static final String WORKFLOW_COMMAND_NAME = "workflow";

   /**
    * Constant for workflow action
    */
   public static final String WORKFLOW_CHECKOUT =  "checkout";

   /**
    * Constant for editurl Attribute
    */
   public static final String ATTR_EDITURL =  "editurl";

   /**
    * Constant for content editor urls resource name
    */
   public static final String CONTENT_EDITOR_URLS =
      "sys_ceSupport/contenteditorurls";

   /**
    * Constant for checkoutstatus Attribute
    */
   public static final String ATTR_CHECKOUTSTATUS =  "checkoutstatus";

   /**
    * Constant for assignmenttype Attribute
    */
   public static final String ATTR_ASSIGNMENTTYPE =  "assignmenttype";
}
