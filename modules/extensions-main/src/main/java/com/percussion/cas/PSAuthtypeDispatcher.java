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
