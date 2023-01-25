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

import com.percussion.cms.objectstore.server.PSAuthTypes;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.extension.IPSExtensionErrors;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSConsole;
import com.percussion.server.PSServer;
import com.percussion.util.IPSHtmlParameters;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This exit acts like a dispatcher to the appropriate authtype implementation 
 * resource. Exit takes the authtype in the request (as HTML parameter
 * {@link com.percussion.util.IPSHtmlParameters#SYS_AUTHTYPE}), finds the
 * matching resource name using the singleton object of the class
 * {@link com.percussion.cms.objectstore.server.PSAuthTypes}, makes an internal
 * request and imports the &lt;linkurl&gt; nodes from the result document to the
 * original result document.The default value for the requested authtype is "0".
 */
public class PSAuthtypeDispatcher extends PSDefaultExtension
      implements
         IPSResultDocumentProcessor
{
   /**
    * Implementation of the method from the interface.
    * 
    * @return always <code>false</code>
    * @see com.percussion.extension.IPSResultDocumentProcessor
    *      #canModifyStyleSheet()
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /**
    * Implementation of the interface method. Does not need any parameters. See
    * the class description for more details.
    * 
    * @see com.percussion.extension.IPSResultDocumentProcessor#
    *      processResultDocument(java.lang.Object[], com.percussion.server.
    *      IPSRequestContext, org.w3c.dom.Document)
    */
   public Document processResultDocument(Object[] params,
         IPSRequestContext request, Document resultDoc)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {
      //Safe to assume auth type "0" if authtype parameter is missing in the 
      //request.
      String authtype = request.getParameter(IPSHtmlParameters.SYS_AUTHTYPE,
            "0").trim();
      if (authtype.length() == 0)
         authtype = "0";

      String resource = PSAuthTypes.getInstance().getResourceForAuthtype(
            authtype);
      if (resource == null)
      {
         String[] args =
         {authtype, PSAuthTypes.getInstance().getConfigFile().getPath()};
         throw new PSExtensionProcessingException(
               IPSExtensionErrors.AUTHTYPE_REGISTRATION_MISSING, args);
      }
      //Make the current siteid as the originating siteid if originating siteid
      //is missing in the request.
      String originalSiteId = request
            .getParameter(IPSHtmlParameters.SYS_ORIGINALSITEID);
      if (originalSiteId == null || originalSiteId.length() < 1)
      {
         request.setParameter(IPSHtmlParameters.SYS_ORIGINALSITEID, request
               .getParameterObject(IPSHtmlParameters.SYS_SITEID));
      }
      //Let root element get the original siteid attribute
      if (originalSiteId != null)
      {
         resultDoc.getDocumentElement().setAttribute(
               IPSHtmlParameters.SYS_ORIGINALSITEID, originalSiteId);
      }
      
      IPSInternalRequest iReq = request.getInternalRequest(resource.toString());
      if (iReq == null)
      {
         String[] args =
         {authtype, resource.toString(),
               PSAuthTypes.getInstance().getConfigFile().getPath()};
         throw new PSExtensionProcessingException(
               IPSExtensionErrors.AUTHTYPE_RESOURCE_MISSING, args);
      }
      try
      {
         Document doc = iReq.getResultDoc();
         NodeList nl = doc.getElementsByTagName("linkurl");
         for (int i = 0; i < nl.getLength(); i++)
         {
            Node node = nl.item(i);
            resultDoc.getDocumentElement().appendChild(
                  resultDoc.importNode(node, true));
         }
      }
      catch (PSInternalRequestCallException e)
      {
         PSConsole.printMsg("AuthtypeDlspatcher", e);
         throw new PSExtensionProcessingException(e.getErrorCode(), e
               .getErrorArguments());
      }
      return resultDoc;
   }
}
