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
package com.percussion.relationship.effect;

import com.percussion.data.PSInternalRequestCallException;
import com.percussion.error.PSNotFoundException;
import com.percussion.extension.IPSExtensionErrors;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.IPSHtmlParameters;
import org.w3c.dom.Document;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
