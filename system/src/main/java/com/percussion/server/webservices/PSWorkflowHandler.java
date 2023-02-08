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

package com.percussion.server.webservices;

import com.percussion.error.PSException;
import com.percussion.server.PSRequest;
import com.percussion.util.PSXMLDomUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class is used to handle all workflow related operations for 
 * webservices. These operations are specified in the "Workflow" port in the
 * <code>WebServices.wsdl</code>.
 *
 * @See {@link com.percussion.hooks.webservices.PSWSWorkflow}.
 */
class PSWorkflowHandler extends PSWebServicesBaseHandler
{
   /**
    * Operation to return the audit trail of the specified content item.
    *
    * @param request The original request for the operation, 
    *    assumed not <code>null</code>
    * @param parent The parent document to add the response element to,
    *    assumed not <code>null</code> and it will already contain the correct
    *    base element for the response
    * 
    * @throws PSException
    */
   void auditTrailAction(PSRequest request, Document parent) throws PSException
   {
      processContentIdAction(WS_AUDITTRAIL, request, parent);
   }

   /**
    * Operation to handle transition of an item into a new state.
    *
    * @param request The original request for the operation, 
    *    assumed not <code>null</code>
    * @param parent The parent document to add the response element to,
    *    assumed not <code>null</code> and it will already contain the correct
    *    base element for the response
    * 
    * @throws PSException
    */
   void transitionItemAction(PSRequest request, Document parent)
      throws PSException
   {
      // check for existence first, throws error if not found
      // also sets the request with the content id and revision
      Element el = validateContentKey(request);

      el = PSXMLDomUtil.getNextElementSibling(el, EL_TRANSITIONID);
      String transitionId = PSXMLDomUtil.getElementData(el);
      String comment = null;
      String adhocList = "";

      while (el != null)
      {
         String name = PSXMLDomUtil.getUnqualifiedNodeName(el);
         if (name.equals(EL_COMMENT))
            comment = PSXMLDomUtil.getElementData(el);

         if (name.equals(EL_ADHOCUSERS))
            adhocList += ";" + PSXMLDomUtil.getElementData(el);

         el = PSXMLDomUtil.getNextElementSibling(el);
      }

      // skip the initial ';'
      if (adhocList.trim().length() > 1)
         adhocList = adhocList.substring(1);

      transitionItem(request, transitionId, comment, adhocList);
      
      addResultResponseXml("success", 0, null, parent);      
   }

   /** 
    * Operation to get the list of transitions available to the specified content
    * item. This list will contain all the transitions without regard to the role
    * the specific user is assigned to.
    * 
    * @param request The original request for the operation, 
    *    assumed not <code>null</code>
    * @param parent The parent document to add the response element to,
    *    assumed not <code>null</code> and it will already contain the correct
    *    base element for the response
    * 
    * @throws PSException
    */
   void transitionListAction(PSRequest request, Document parent)
      throws PSException
   {
      // check for existence first, throws error if not found
      // also sets the request with the content id and revision
      validateContentKey(request, true, false);

      String path = WEB_SERVICES_APP + "/" + WS_TRANSITIONLIST + ".html";
      getMergedResultDoc(request, path, parent);
   }

   /**
    * action string constants
    */
   private static final String WS_AUDITTRAIL = "auditTrail";
   private static final String WS_TRANSITIONLIST = "transitionList";

   /**
    * Constants for XML elements/attributes defined in the 
    * schema <code>sys_WorkflowParameters.xsd</code>
    */
   private static final String EL_TRANSITIONID = "TransitionId";
   private static final String EL_COMMENT = "Comment";
   private static final String EL_ADHOCUSERS = "AdhocUsers";
}
