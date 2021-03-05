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
package com.percussion.server.webservices;

import com.percussion.cms.IPSConstants;
import com.percussion.cms.PSApplicationBuilder;
import com.percussion.error.PSException;
import com.percussion.server.PSRequest;
import com.percussion.server.PSUserSession;
import com.percussion.server.PSUserSessionManager;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class is used to handle all miscellaneous related operations for 
 * webservices. These operations are specified in the "Miscellaneous" port in 
 * the <code>WebServices.wsdl</code>.
 *
 * @See {@link com.percussion.hooks.webservices.PSWSMiscellaneous}.
 */
class PSMiscellaneousHandler extends PSWebServicesBaseHandler
{
   /**
    * Operation to handle checkIn of a specific contentId.
    *
    * @param request The original request for the operation, 
    *    assumed not <code>null</code>
    * @param parent The parent document to add the response element to,
    *    assumed not <code>null</code> and it will already contain the correct
    *    base element for the response
    * 
    * @throws PSException
    */
   void checkInAction(PSRequest request, Document parent) throws PSException
   {
      // check for existence first, throws error if not found
      // also sets the request with the content id and revision
      validateContentKey(request);

      executeCheckInOut(request, IPSConstants.TRIGGER_CHECKIN);
      addResultResponseXml("success", 0, null, parent);
   }

   /**
    * Operation to handle checkOut of a specific contentId. The new revision
    * number will be set in the original request.
    *
    * @param request The original request for the operation, 
    *    assumed not <code>null</code>
    * @param parent The parent document to add the response element to,
    *    assumed not <code>null</code> and it will already contain the correct
    *    base element for the response
    * 
    * @throws PSException
    */
   void checkOutAction(PSRequest request, Document parent) throws PSException
   {
      // check for existence first, throws error if not found
      // also sets the request with the content id and revision
      validateContentKey(request);

      executeCheckInOut(request, IPSConstants.TRIGGER_CHECKOUT);
      addResultResponseXml("success", 0, null, parent);
   }

   /**
    * Operation to set the revision lock on the specified content item. This 
    * will change the state as if the item has been in the public state and 
    * every change will result in a new revision.
    *
    * @param request The original request for the operation, 
    *    assumed not <code>null</code>
    * @param parent The parent document to add the response element to,
    *    assumed not <code>null</code> and it will already contain the correct
    *    base element for the response
    * 
    * @throws PSException
    */
   void lockRevisionAction(PSRequest request, Document parent)
      throws PSException
   {
      // need to set the DBActionType param
      request.setParameter(
         PSApplicationBuilder.REQUEST_TYPE_HTML_PARAMNAME,
         PSApplicationBuilder.REQUEST_TYPE_VALUE_UPDATE);

      processContentIdAction(WS_LOCKREVISION, request, parent);
   }

   /**
    * This operation is used to login and return the sessionId.
    *
    * @param request The original request for the operation, 
    *    assumed not <code>null</code>
    * @param parent The parent document to add the response element to,
    *    assumed not <code>null</code> and it will already contain the correct
    *    base element for the response
    * 
    * @throws PSException
    */
   void loginAction(PSRequest request, Document parent) throws PSException
   {
      String path = WEB_SERVICES_APP + "/" + WS_LOGIN;
      processInternalRequest(request, path, parent);

      Element el =
         PSXMLDomUtil.getFirstElementChild(parent.getDocumentElement());
      if (el != null)
      {
         String name = PSXMLDomUtil.getUnqualifiedNodeName(el);
         if (name.equals("LoginData"))
         {
            el.setAttribute("hostUrl", request.getParameter(WS_HOSTURL));
         }
      }
   }

   /**
    * Operation to logout a user which releases the current sessionId.
    *
    * @param request The original request for the operation, 
    *    assumed not <code>null</code>
    * @param parent The parent document to add the response element to,
    *    assumed not <code>null</code> and it will already contain the correct
    *    base element for the response
    * 
    * @throws PSException
    */
   void logoutAction(PSRequest request, Document parent) throws PSException
   {
      PSUserSession session = request.getUserSession();
      PSUserSessionManager.releaseUserSession(session);
      addResultResponseXml("success", 0, null, parent);
   }

   /**
    * Operation to call any specified resource/application defined by the user.
    * All the top level elements and thier data will be set as html parameters
    * within the request, this is to allow current resource/applications that
    * have been created by users to work properly with web services. Sets the
    * parent doc with whatever the specified resource/application returns 
    * as it's XML result.
    *
    * @param request the original request for the operation, must contain a 
    *    <code>custom</code> parameter with the location of the 
    *    resource/application to be called assumed not <code>null</code>.
    * @param parent The parent document to add the response element to,
    *    assumed not <code>null</code> and it will already contain the correct
    *    base element for the response.
    * 
    * @throws PSException
    */
   void callDirectAction(PSRequest request, Document parent) throws PSException, IOException {
      String path = null;

      // load all the html parameters from the top level
      // xml input document
      Document inputDoc = request.getInputDocument();
      if (inputDoc != null)
      {
         Element root = inputDoc.getDocumentElement();
         if (root != null)
         {
            // get the application element
            Element appEl =
               PSXMLDomUtil.getFirstElementChild(root, EL_APPLOCATION);
            if (appEl != null)
            {
               path = PSXMLDomUtil.getElementData(appEl);

               // get the params element
               Element params = PSXMLDomUtil.getNextElementSibling(appEl);
               if (params != null)
               {
                  // make sure it's what we expect
                  PSXMLDomUtil.checkNode(params, EL_PARAMS);

                  // get each param element
                  Element param =
                     PSXMLDomUtil.getFirstElementChild(params);

                  Map parameters = new HashMap();
                  while (param != null &&
                     PSXMLDomUtil.getUnqualifiedNodeName(param).equals(EL_PARAM))
                  {
                     String name = param.getAttribute("name");
                     String value = PSXMLDomUtil.getElementData(param);

                     List values = (List) parameters.get(name);
                     if (values == null)
                     {
                        values = new ArrayList();
                        parameters.put(name, values);
                     }
                     values.add(value);

                     param = PSXMLDomUtil.getNextElementSibling(param);
                  }

                  Iterator walker = parameters.keySet().iterator();
                  while (walker.hasNext())
                  {
                     String name = (String) walker.next();
                     List values = (List) parameters.get(name);
                     if (values != null && values.size() > 0)
                     {
                        if (values.size() == 1)
                           request.setParameter(name, (String) values.get(0));
                        else
                           request.setParameter(name, values);
                     }
                  }
               }
            }
         }
      }

      if (path == null || path.trim().length() == 0)
      {
         // get the custom app path from the custom parameter
         path = request.getParameter("custom", null);
      }

      String data = null;
      if (path != null && !path.endsWith(".xml"))
      {
         try(ByteArrayOutputStream out = processMergeResultRequest(request, path)) {
            if (out != null)
               data = out.toString();
         }
      }
      else
      {
         Document dataDoc = processInternalRequest(request, path);
         if (dataDoc != null)
            data = PSXmlDocumentBuilder.toString(dataDoc);
      }

      if (data != null)
      {
         Element root = parent.getDocumentElement();
         PSXmlDocumentBuilder.addElement(parent, root, EL_XMLDATA, data);
      }
   }

   // used to determine where this request was originally sent to
   // set up in the webservices and passed as a parameter
   private static final String WS_HOSTURL = "wsHostUrl";

   /**
    * action string constants
    */
   private static final String WS_LOCKREVISION = "lockRevision";
   private static final String WS_LOGIN = "login";

   /**
    * Constants for XML elements/attributes defined in the 
    * schema <code>sys_MiscellaneousParameters.xsd</code>
    */
   private static final String EL_APPLOCATION = "AppLocation";
   private static final String EL_PARAMS = "Params";
   private static final String EL_PARAM = "Param";
}
