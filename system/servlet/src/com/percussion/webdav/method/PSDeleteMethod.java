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
import com.percussion.cms.objectstore.client.PSRemoteAgent;
import com.percussion.cms.objectstore.client.PSRemoteException;
import com.percussion.util.PSWorkflowInfo;
import com.percussion.webdav.PSWebdavServlet;
import com.percussion.webdav.PSWebdavStatus;
import com.percussion.webdav.error.IPSWebdavErrors;
import com.percussion.webdav.error.PSWebdavException;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class implements the DELETE WebDAV method.
 */
public class PSDeleteMethod extends PSWebdavMethod
{

   /**
    * Constructs an instance from the given parameters.
    *
    * @param req   The servlet request, it may not be <code>null</code>.
    * @param resp  The servlet response, it may not be <code>null</code>.
    * @param servlet The webdav servlet, it may not be <code>null</code>.
    */
   public PSDeleteMethod(
      HttpServletRequest req,
      HttpServletResponse resp,
      PSWebdavServlet servlet)
   {
      super(req, resp, servlet);
   }

   // Implements PSWebdavMethod.parseRequest()
   protected void parseRequest() throws PSWebdavException
   {
   }


   // Implements PSWebdavMethod.processRequest()
   protected void processRequest()
      throws PSWebdavException,
             IOException
   {
      String compPath = getRxVirtualPath();

      PSComponentSummary compSummary = getComponentByPath(compPath);

      if (compSummary == null)
      {
         throw new PSWebdavException(
            IPSWebdavErrors.RESOURCE_NOT_FIND,
            compPath,
            PSWebdavStatus.SC_NOT_FOUND);
      }
      
      if (! validateLock(compSummary))
         return;
      
      PSRemoteAgent agent = getRemoteAgent();
      try
      {
         PSComponentSummary parent = null;
         PSWorkflowInfo info = getWorkflowInfo(); 
         String validTokens = getConfig().getPublicValidTokens() +
         "," + getConfig().getQEValidTokens();
         
         if ((compSummary.isFolder())
               || (!info.isValidState(compSummary.getWorkflowAppId(),
                     compSummary.getContentStateId(), validTokens)))
         {
            if (!getConfig().isDeleteAsPurge())
               parent = getParentSummary(compPath);

            deleteComponent(parent, compSummary);
            setResponseStatus(PSWebdavStatus.SC_NO_CONTENT);
         }
         else
         {
            m_logger
                  .error("Cannot delete an item that is in a public or quick-edit state");
            setResponseStatus(PSWebdavStatus.SC_UNPROCESSABLE_ENTITY);
         }
      }
      catch (PSRemoteException e)
      {
         throw new PSWebdavException(e);
      }
   }
   
   /**
    * log4j 
    */
   private static final Logger m_logger = LogManager.getLogger(PSDeleteMethod.class);
}



