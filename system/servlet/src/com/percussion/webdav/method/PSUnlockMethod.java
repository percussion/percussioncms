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

