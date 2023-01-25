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
 * This class defines the actions associated with the Content Meta group
 * of web services.
 *
 * All methods assume the Envelope and the SOAPContext objects are not
 * <code>null</code>. This is defined by the SOAP 1.1 message router
 * servlet.
 */
public class PSWSDesign extends PSWebServices
{
   public PSWSDesign()
      throws SOAPException
   {}

   /**
    * This operation is used to get back a list of content types
    * to be used in creating new content. See newItem method.
    *
    * @param   env      the full envelope of the SOAP message being sent, the
    *                       contents of the message contain a
    *                       <code>ContentTypeListRequest</code> element defined in
    *                       the sys_DesignParameters.xsd schema file
    *
    * @param   reqCtx   the context of the message being sent
    *
    * @param   resCtx   a location for the response to be sent back to the
    *                   requestor
    *
    * @throws  SOAPException @see sendToServer for more info
    */
   public void contentTypeList(Envelope env,
                               SOAPContext reqCtx,
                               SOAPContext resCtx)
      throws SOAPException
   {
      sendToServer("contentTypeList", env, reqCtx, resCtx);
   }

   /**
    * This operation is used to get back a specific content type object, used
    * by remote clients to create new content items. @see newItem
    *
    * @param   env      the full envelope of the SOAP message being sent, the
    *                       contents of the message contain a
    *                       <code>ContentTypeRequest</code> element defined in
    *                       the sys_DesignParameters.xsd schema file
    *
    * @param   reqCtx   the context of the message being sent
    *
    * @param   resCtx   a location for the response to be sent back to the
    *                   requestor
    *
    * @throws  SOAPException @see sendToServer for more info
    */
   public void contentType(Envelope env,
                           SOAPContext reqCtx,
                           SOAPContext resCtx)
      throws SOAPException
   {
      sendToServer("contentType", env, reqCtx, resCtx);
   }

   /**
    * This operation is used to obtain the list of variants associated with a
    * specific content item.
    *
    * @param   env      the full envelope of the SOAP message being sent, the
    *                       contents of the message contain a
    *                       <code>VariantListRequest</code> element defined in
    *                       the sys_DesignParameters.xsd schema file
    *
    * @param   reqCtx   the context of the message being sent
    *
    * @param   resCtx   a location for the response to be sent back to the
    *                   requestor
    *
    * @throws  SOAPException @see sendToServer for more info
    */
   public void variantList(Envelope env,
                           SOAPContext reqCtx,
                           SOAPContext resCtx)
      throws SOAPException
   {
      sendToServer("variantList", env, reqCtx, resCtx);
   }
}
