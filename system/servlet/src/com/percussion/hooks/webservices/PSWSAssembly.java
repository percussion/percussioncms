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
 * This class defines the actions associated with the Assembly group
 * of web services.
 *
 * All methods assume the Envelope and the SOAPContext objects are not
 * <code>null</code>. This is defined by the SOAP 1.1 message router
 * servlet.
 */
public class PSWSAssembly extends PSWebServices
{
   public PSWSAssembly()
      throws SOAPException
   {}

   /**
    * This operation is used to obtain a completely assembled page
    * based on the specified variant.
    *
    * @param   env      the full envelope of the SOAP message being sent, the
    *                       contents of the message contain a
    *                       <code>CreateVariantRequest</code> element defined in
    *                       the sys_AssemblyParameters.xsd schema file
    *
    * @param   reqCtx   the context of the message being sent
    *
    * @param   resCtx   a location for the response to be sent back to the
    *                   requestor
    *
    * @throws  SOAPException @see sendToServer for more info
    */
   public void createVariant(Envelope env,
                             SOAPContext reqCtx,
                             SOAPContext resCtx)
      throws SOAPException
   {
      sendToServer("createVariant", env, reqCtx, resCtx);
   }
}
