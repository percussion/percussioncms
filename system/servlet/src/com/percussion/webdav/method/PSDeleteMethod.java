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



