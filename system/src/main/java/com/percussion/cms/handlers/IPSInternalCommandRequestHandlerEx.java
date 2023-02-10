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
package com.percussion.cms.handlers;

import com.percussion.data.PSInternalRequestCallException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.PSRequest;
import org.w3c.dom.Document;

/**
 * Internal request access interface.  This will allow command handlers to
 * make requests through other request handlers directly through a method call.  
 */
public interface IPSInternalCommandRequestHandlerEx
{
   /**
    * Makes a request to the internal application using the porvided request.
    * See the implementing class for specific details about the returned 
    * document.
    *
    * @request the request to make, not <code>null</code>.
    * @return the document created through the internal request or 
    *    <code>null</code> if no data was returned.
    * @throws PSInternalRequestCallException if the internal request call 
    *    failed.
    * @throws PSAuthorizationException if the requester is not authorized to
    *    perform this request.
    * @throws PSAuthenticationFailedException if the authentication of the 
    *    requester failed.
    */
   public Document makeInternalRequestEx(PSRequest request)
      throws PSInternalRequestCallException, PSAuthorizationException,
      PSAuthenticationFailedException;
} 
