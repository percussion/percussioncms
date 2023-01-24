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

package com.percussion.hooks.webservices;

import org.apache.soap.Envelope;
import org.apache.soap.SOAPException;
import org.apache.soap.rpc.SOAPContext;

/**
 * This class defines the actions associated with the Workflow group
 * of web services.
 *
 * All methods assume the Envelope and the SOAPContext objects are not
 * <code>null</code>. This is defined by the SOAP 1.1 message router
 * servlet.
 */
public class PSWSWorkflow extends PSWebServices
{
   public PSWSWorkflow()
      throws SOAPException
   {}

   /**
    * This operation is used to get back a history list of
    * a specific content item.
    *
    * @param   env      the full envelope of the SOAP message being sent, the
    *                     contents of the message must contain an
    *                     <code>AuditTrailRequest</code> element defined in
    *                     the sys_WorkflowParameters.xsd schema file
    *
    * @param   reqCtx   the context of the message being sent
    *
    * @param   resCtx   a location for the response to be sent back to the
    *                   requestor
    *
    * @throws  SOAPException @see sendToServer for more info
    */
   public void auditTrail(Envelope env,
                          SOAPContext reqCtx,
                          SOAPContext resCtx)
      throws SOAPException
   {
      sendToServer("auditTrail", env, reqCtx, resCtx);
   }

   /**
    * This operation is used to transition a specific content item to a new
    * state.
    *
    * @param   env      the full envelope of the SOAP message being sent, the
    *                     contents of the message must contain a
    *                     <code>TransitionItemRequest</code> element defined in
    *                     the sys_WorkflowParameters.xsd schema file
    *
    * @param   reqCtx   the context of the message being sent
    *
    * @param   resCtx   a location for the response to be sent back to the
    *                   requestor
    *
    * @throws  SOAPException @see sendToServer for more info
    */
   public void transitionItem(Envelope env,
                              SOAPContext reqCtx,
                              SOAPContext resCtx)
      throws SOAPException
   {
      sendToServer("transitionItem", env, reqCtx, resCtx);
   }

   /**
    * This operation is used retrieve the transition list of a specific
    * content item.
    *
    * @param   env      the full envelope of the SOAP message being sent, the
    *                     contents of the message contain a
    *                     <code>TransitionListRequest</code> element defined in
    *                     the sys_WorkflowParameters.xsd schema file
    *
    * @param   reqCtx   the context of the message being sent
    *
    * @param   resCtx   a location for the response to be sent back to the
    *                   requestor
    *
    * @throws  SOAPException @see sendToServer for more info
    */
   public void transitionList(Envelope env,
                              SOAPContext reqCtx,
                              SOAPContext resCtx)
      throws SOAPException
   {
      sendToServer("transitionList", env, reqCtx, resCtx);
   }
}
