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

package com.percussion.server;

import com.percussion.data.PSInternalRequestCallException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import org.w3c.dom.Document;

import java.sql.ResultSet;

/**
 * Proxies <code>PSInternalRequest</code> to prevent access to the getRequest
 * method by user-space code.  All methods in the interface are passed
 * through to the real object.
 */
public class PSInternalRequestProxy implements IPSInternalRequest
{
   /**
    * Constructs a new <code>PSInternalRequestProxy</code> that proxies the
    * supplied internal request.
    *
    * @param internalRequest the object to be proxied, not <code>null</code>
    * @throws IllegalArgumentException if <code>internalRequest</code> is
    * <code>null</code>
    */
   public PSInternalRequestProxy(PSInternalRequest internalRequest)
   {
      if (internalRequest == null)
         throw new IllegalArgumentException(
            "May not proxy a null internal request.");
      m_internalRequest = internalRequest;
   }


   // see IPSInternalRequest interface
   public void makeRequest()
      throws PSInternalRequestCallException, PSAuthorizationException,
      PSAuthenticationFailedException
   {
      m_internalRequest.makeRequest();
   }


   // see IPSInternalRequest interface
   public void performUpdate()
      throws PSInternalRequestCallException, PSAuthorizationException,
      PSAuthenticationFailedException
   {
      m_internalRequest.performUpdate();
   }


   // see IPSInternalRequest interface
   public Document getResultDoc()
      throws PSInternalRequestCallException
   {
      return m_internalRequest.getResultDoc();
   }


   // see IPSInternalRequest interface
   public ResultSet getResultSet()
      throws PSInternalRequestCallException
   {
      return m_internalRequest.getResultSet();
   }


   // see IPSInternalRequest interface
   public void cleanUp()
   {
      m_internalRequest.cleanUp();
   }


   // see IPSInternalRequest interface
   public IPSRequestContext getRequestContext()
   {
      return m_internalRequest.getRequestContext();
   }

   // see IPSInternalRequest interface
   public int getRequestType()
   {
      return m_internalRequest.getRequestType();
   }

   // see IPSInternalRequest interface
   public byte[] getMergedResult() throws PSInternalRequestCallException
   {
      return m_internalRequest.getMergedResult().toByteArray();
   }
   
   // see IPSInternalRequest interface
   public byte[] getContent() throws PSInternalRequestCallException
   {
      return m_internalRequest.getContent().toByteArray();
   }

   public boolean isBinary(PSRequest req)
   {
      return m_internalRequest.isBinary(req);
   }
   
   /**
    * The internal request being proxied.  Assigned in the ctor and never
    * <code>null</code> or modified after that.
    */
   PSInternalRequest m_internalRequest;

}
