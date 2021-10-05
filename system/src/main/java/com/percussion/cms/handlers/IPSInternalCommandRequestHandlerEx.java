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
