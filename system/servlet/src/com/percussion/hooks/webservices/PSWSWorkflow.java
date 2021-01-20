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
