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

package com.percussion.server;

import com.percussion.data.PSInternalRequestCallException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;

import java.sql.ResultSet;

import org.w3c.dom.Document;

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
