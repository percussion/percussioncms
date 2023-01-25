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
 * This class defines the actions associated with the Content Data group
 * of web services.
 *
 * All methods assume the Envelope and the SOAPContext objects are not
 * <code>null</code>. This is defined by the SOAP 1.1 message router
 * servlet.
 */
public class PSWSContentData extends PSWebServices
{
   public PSWSContentData()
      throws SOAPException
   {}

   /**
    * This operation is used to obtain the main content of the
    * <code>contentId</code>. The parameters <code>contentId</code>,
    * <code>revision</code> and <code>dataVariant</code> within
    * the envelope, form a key that identifies a specific
    * document in the system.
    *
    * @param   env      the full envelope of the SOAP message being sent, the
    *                       contents of the message contain a
    *                       <code>OpenItemRequest</code> element defined in
    *                       the sys_ContentDataParameters.xsd schema file
    *
    * @param   reqCtx   the context of the message being sent
    *
    * @param   resCtx   a location for the response to be sent back to the
    *                   requestor
    *
    * @throws  SOAPException @see sendToServer for more info
    */
   public void openItem(Envelope env,
                        SOAPContext reqCtx,
                        SOAPContext resCtx)
      throws SOAPException
   {
      sendToServer("openItem", env, reqCtx, resCtx);
   }

   /**
    * This operation can be used to obtain all complex children of a specific
    * content id, defined by the contentId anre revision.
    *
    * @param   env      the full envelope of the SOAP message being sent, the
    *                       contents of the message contain a
    *                       <code>OpenChildRequest</code> element defined in
    *                       the sys_ContentDataParameters.xsd schema file
    *
    * @param   reqCtx   the context of the message being sent
    *
    * @param   resCtx   a location for the response to be sent back to the
    *                   requestor
    *
    * @throws  SOAPException @see sendToServer for more info
    */
   public void openChild(Envelope env,
                         SOAPContext reqCtx,
                         SOAPContext resCtx)
      throws SOAPException
   {
      sendToServer("openChild", env, reqCtx, resCtx);
   }

   /**
    * This operation is used to get a content item with all of its
    * related content relationships (and possibly related content data),
    * but no children.
    *
    * @param   env      the full envelope of the SOAP message being sent, the
    *                       contents of the message contain a
    *                       <code>OpenRelatedRequest</code> element defined in
    *                       the sys_ContentDataParameters.xsd schema file
    *
    * @param   reqCtx   the context of the message being sent
    *
    * @param   resCtx   a location for the response to be sent back to the
    *                   requestor
    *
    * @throws  SOAPException @see sendToServer for more info
    */
   public void openRelated(Envelope env,
                           SOAPContext reqCtx,
                           SOAPContext resCtx)
      throws SOAPException
   {
      sendToServer("openRelated", env, reqCtx, resCtx);
   }

   /**
    * This operation is used to obtain data for fields that have been
    * defined as ‘binary’, or to get a specific field within a content item.
    *
    * @param   env      the full envelope of the SOAP message being sent, the
    *                       contents of the message contain a
    *                       <code>OpenFieldRequest</code> element defined in
    *                       the sys_ContentDataParameters.xsd schema file
    *
    * @param   reqCtx   the context of the message being sent
    *
    * @param   resCtx   a location for the response to be sent back to the
    *                   requestor
    *
    * @throws  SOAPException @see sendToServer for more info
    */
   public void openField(Envelope env,
                         SOAPContext reqCtx,
                         SOAPContext resCtx)
      throws SOAPException
   {
      sendToServer("openField", env, reqCtx, resCtx);
   }

   /**
    * This operation is used to get back a document that contains empty elements
    * for all of the parent and complex child fields of a specific content type.
    * All default values will be filled in.
    *
    * @param   env      the full envelope of the SOAP message being sent, the
    *                       contents of the message contain a
    *                       <code>NewItemRequest</code> element defined in
    *                       the sys_ContentDataParameters.xsd schema file
    *
    * @param   reqCtx   the context of the message being sent
    *
    * @param   resCtx   a location for the response to be sent back to the
    *                   requestor
    *
    * @throws  SOAPException @see sendToServer for more info
    */
   public void newItem(Envelope env,
                       SOAPContext reqCtx,
                       SOAPContext resCtx)
      throws SOAPException
   {
      sendToServer("newItem", env, reqCtx, resCtx);
   }

   /**
    * This operation is used to create a clone of the content item specified.
    *
    * @param   env      the full envelope of the SOAP message being sent, the
    *                       contents of the message contain a
    *                       <code>NewCopyRequest</code> element defined in
    *                       the sys_ContentDataParameters.xsd schema file
    *
    * @param   reqCtx   the context of the message being sent
    *
    * @param   resCtx   a location for the response to be sent back to the
    *                   requestor
    *
    * @throws  SOAPException @see sendToServer for more info
    */
   public void newCopy(Envelope env,
                       SOAPContext reqCtx,
                       SOAPContext resCtx)
      throws SOAPException
   {
      sendToServer("newCopy", env, reqCtx, resCtx);
   }

   /**
    * This operation is used to modify existing content in the system, also if
    * there is no key, then it assumed to be an insert.
    *
    * @param   env      the full envelope of the SOAP message being sent, the
    *                       contents of the message contain a
    *                       <code>UpdateItemRequest</code> element defined in
    *                       the sys_ContentDataParameters.xsd schema file
    *
    * @param   reqCtx   the context of the message being sent
    *
    * @param   resCtx   a location for the response to be sent back to the
    *                   requestor
    *
    * @throws  SOAPException @see sendToServer for more info
    */
   public void updateItem(Envelope env,
                          SOAPContext reqCtx,
                          SOAPContext resCtx)
      throws SOAPException
   {
      sendToServer("updateItem", env, reqCtx, resCtx);
   }

   /**
    * This operation is used to promote a specified revision to the
    * current revision.
    *
    * @param   env      the full envelope of the SOAP message being sent, the
    *                       contents of the message contain a
    *                       <code>PromoteRevisionRequest</code> element defined in
    *                       the sys_ContentDataParameters.xsd schema file
    *
    * @param   reqCtx   the context of the message being sent
    *
    * @param   resCtx   a location for the response to be sent back to the
    *                   requestor
    *
    * @throws  SOAPException @see sendToServer for more info
    */
   public void promoteRevision(Envelope env,
                               SOAPContext reqCtx,
                               SOAPContext resCtx)
      throws SOAPException
   {
      sendToServer("promoteRevision", env, reqCtx, resCtx);
   }
}
