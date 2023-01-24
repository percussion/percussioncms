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
package com.percussion.webdav.method;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.client.PSRemoteAgent;
import com.percussion.cms.objectstore.client.PSRemoteException;
import com.percussion.cms.objectstore.ws.PSClientItem;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.webdav.PSWebdavServlet;
import com.percussion.webdav.PSWebdavStatus;
import com.percussion.webdav.error.IPSWebdavErrors;
import com.percussion.webdav.error.PSWebdavException;
import com.percussion.webdav.objectstore.PSWebdavContentType;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This class implements the UNLOCK WebDAV method.
 */
public class PSUnlockMethod extends PSWebdavMethod
{

   /**
    * Constructs an instance from the given parameters.
    *
    * @param req   The servlet request, it may not be <code>null</code>.
    * @param resp  The servlet response, it may not be <code>null</code>.
    * @param servlet The webdav servlet, it may not be <code>null</code>.
    */
   public PSUnlockMethod(HttpServletRequest req, HttpServletResponse resp,
         PSWebdavServlet servlet)
   {
      super(req, resp, servlet);
   }

   // Implements PSWebdavMethod.processRequest()
   protected void processRequest() throws PSWebdavException, IOException
   {
      String compPath = getRxVirtualPath();
      // make sure the component exists
      PSComponentSummary compSummary = getComponentByPathRqd(compPath);
      String remoteUser = getRequest().getRemoteUser();
      String checkoutUser = compSummary.getCheckoutUserName();

      if (compSummary.isFolder() || checkoutUser == null
            || (!checkoutUser.equalsIgnoreCase(remoteUser)))
      {
         // the item didn't lock by the current user
         setResponseStatus(PSWebdavStatus.SC_FORBIDDEN);
      }
      else
      {
         checkinComponent(compSummary);
         setResponseStatus(PSWebdavStatus.SC_NO_CONTENT);
      }
   }

   /**
    * Checkin the specified component. The value of the owner field will be 
    * cleared after afterwards.
    * 
    * @param compSummary The to be checked in component, assume not 
    *    <code>null</code>.
    * 
    * @throws PSWebdavException if an error occurs.
    */
   private void checkinComponent(PSComponentSummary compSummary)
         throws PSWebdavException
   {
      PSLocator locator = compSummary.getEditLocator();
      long id = compSummary.getContentTypeId();
      PSWebdavContentType contentType = getConfig().getContentType(id);
      if (contentType == null)
      {
         Object[] args = { new Long(id), compSummary.getName(),
               new Integer(locator.getId()), new Integer(locator.getRevision()) };
         throw new PSWebdavException(
               IPSWebdavErrors.CONTENTTYPE_NOT_CONFIGURED, args,
               PSWebdavStatus.SC_PRECONDITION_FAILED);
      }
      try
      {
         PSRemoteAgent agent = getRemoteAgent();
         PSClientItem item = agent.openItem(locator, false, false);
         PSItemField field = item.getFieldByName(contentType.getOwnerField());
         field.clearValues();

         // finally, save the item, and check in.
         agent.updateItem(item, true);
         
         PSWebdavUtils.transitionFromQuickEditToPublic(compSummary,
               getConfig(), getWorkflowInfo(), agent, getRequest(), false);         
      }
      catch (PSRemoteException e)
      {
         throw new PSWebdavException(e, PSWebdavStatus.SC_INTERNAL_SERVER_ERROR);
      }
   }
}

