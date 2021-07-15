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
package com.percussion.relationship.effect;

import com.percussion.data.PSInternalRequestCallException;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.extension.IPSExtensionErrors;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.IPSHtmlParameters;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;

/**
 * This utility class is used for the effect package
 */
public class PSEffectUtils
{
   /**
    * The private constructor to prevent an instance of this class.
    */
   private PSEffectUtils()
   {
   }
   
   /**
    * Get current workflow state for the supplied item. The returned document
    * conform the following DTD:
    * &lt;!ELEMENT CurrentState EMPTY>
    * &lt;!ATTLIST isPublic CDATA #REQUIRED>
    * &lt;!ATTLIST stateId CDATA #REQUIRED>
    * &lt;!ATTLIST stateName CDATA #REQUIRED>
    * &lt;!ATTLIST workflowId CDATA #REQUIRED>
    *
    * @param request the request to operate on, it may not be <code>null</code>.
    * @param contentId the content id of the item, may not be <code>null</code>.
    * @param name the name of the registered effect, it may not be
    *    <code>null</code> or empty.
    * 
    * @return the requested document in the format described above. Never
    *    <code>null</code>, but may be empty if the item does not exist or 
    *    does not have workflow (for folders). 
    *
    * @throws PSInternalRequestCallException if any error occurs processing
    *    the internal request call.
    * @throws PSNotFoundException if a required resource cannot be found.
    */
   public static Document getWorkflowState(IPSRequestContext request, int contentId,
         String name) throws PSInternalRequestCallException,
         PSNotFoundException
   {
      String resource = SYS_PSXRELATIONSHIPSUPPORT + "/" + GET_CURRENTSTATE;

      Map params = new HashMap();
      params.put(IPSHtmlParameters.SYS_CONTENTID, Integer.toString(contentId));

      IPSInternalRequest ir = request.getInternalRequest(resource, params, false);

      if (ir != null)
      {
         return ir.getResultDoc();
      }
      else
      {
         String[] args = {name, resource};
         throw new PSNotFoundException(request.getUserLocale(),
            IPSExtensionErrors.MISSING_INTERNAL_REQUEST_RESOURCE, args);
      }
   }

   /**
    * Get current workflow state for the supplied item. The returned document
    * conform the following DTD:
    * &lt;!ELEMENT CurrentStates (CurrentState*)>
    * &lt;!ELEMENT CurrentState EMPTY>
    * &lt;!ATTLIST contentId CDATA #REQUIRED>
    * &lt;!ATTLIST isPublic CDATA #REQUIRED>
    *
    * @param request the request to operate on, it may not be <code>null</code>.
    * @param contentIds a collection of content ids (as <code>Integer</code> 
    * objects). It may not be <code>null</code>, but may be empty.
    * @param name the name of the registered effect, it may not be
    *    <code>null</code> or empty.
    * 
    * @return the requested document in the format described above. Never
    *    <code>null</code>, but may be empty if the item does not exist or 
    *    does not have workflow (for folders). 
    *
    * @throws PSInternalRequestCallException if any error occurs processing
    *    the internal request call.
    * @throws PSNotFoundException if a required resource cannot be found.
    */
   public static Document getWorkflowStates(IPSRequestContext request,
         Collection contentIds, String name)
         throws PSInternalRequestCallException, PSNotFoundException
   {
      String resource = SYS_PSXRELATIONSHIPSUPPORT + "/" + GET_CURRENTSTATE;

      Map params = new HashMap();
      params.put("sys_contentids", contentIds);

      IPSInternalRequest ir = request.getInternalRequest(resource, params, false);

      if (ir != null)
      {
         return ir.getResultDoc();
      }
      else
      {
         String[] args = {name, resource};
         throw new PSNotFoundException(request.getUserLocale(),
            IPSExtensionErrors.MISSING_INTERNAL_REQUEST_RESOURCE, args);
      }
   }
   
   /**
    * The name of the application used to query or update relationships in
    * the repository.
    */
   private static final String SYS_PSXRELATIONSHIPSUPPORT =
      "sys_psxRelationshipSupport";

   /**
    * The name of the query resource to get the current workflow state of an
    * item.
    */
   private static final String GET_CURRENTSTATE = "getCurrentState";
}
