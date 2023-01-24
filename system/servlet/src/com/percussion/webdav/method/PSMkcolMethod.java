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

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.webdav.PSWebdavServlet;
import com.percussion.webdav.PSWebdavStatus;
import com.percussion.webdav.error.IPSWebdavErrors;
import com.percussion.webdav.error.PSWebdavException;


/**
 * This class implements the MKCOL WebDAV method.
 */
public class PSMkcolMethod extends PSWebdavMethod
{

   /**
    * Constructs an instance from the given parameters.
    *
    * @param req   The servlet request, it may not be <code>null</code>.
    * @param resp  The servlet response, it may not be <code>null</code>.
    * @param servlet The webdav servlet, it may not be <code>null</code>.
    */
   public PSMkcolMethod(
      HttpServletRequest req,
      HttpServletResponse resp,
      PSWebdavServlet servlet)
   {
      super(req, resp, servlet);
   }

   // Implements PSWebdavMethod.parseRequest() 
   protected void parseRequest() throws PSWebdavException
   {
      // expecting empty body content only; otherwise response 415
      try 
      {
         byte[] content = getContent();
         if (content.length > 0)
         {
            String s = new String(content);
            if (s.trim().length() > 0)
            {
               Object[] args = {s};
               throw new PSWebdavException(IPSWebdavErrors.UNKNOWN_BODY_IN_MKCOL_REQ,
                     args, PSWebdavStatus.SC_UNSUPPORTED_MEDIA_TYPE);
            }
         }
      }
      catch (Exception e)  // this should not happen, in case it does, we don't
      {                    // care the content, just set the correct status
         Object[] args = {"...unknown..."};
         throw new PSWebdavException(IPSWebdavErrors.UNKNOWN_BODY_IN_MKCOL_REQ,
               args, PSWebdavStatus.SC_UNSUPPORTED_MEDIA_TYPE);
      }
      
      String compPath = getRxVirtualPath();
      m_compSummary = getComponentByPath(compPath);

      if (m_compSummary == null)
         m_parentSummary = getParentSummary(compPath);
   }

   // Implements PSWebdavMethod.processRequest() 
   protected void processRequest()
      throws PSWebdavException,
             IOException
   {
      if (m_compSummary != null)
      {
         // creating an existing folder is not allowed (by WebDAV spec).
         setResponseStatus(PSWebdavStatus.SC_METHOD_NOT_ALLOWED);
         return;
      }
      
      PSWebdavUtils.createFolder(
         getSourceFileName(),
         getConfig().getExcludeFolderProperties(),
         m_parentSummary.getCurrentLocator(),
         getRemoteRequester());
            
      setResponseStatus(PSWebdavStatus.SC_CREATED);
   }

   /**
    * The summary of the requeted object. It is initialized by
    * {@link #parseRequest()}. It may be <code>null</code> if the requested
    * object does not exist.
    */
   private PSComponentSummary m_compSummary = null;

   /**
    * The parent summary of the requeted object. It is initialized by
    * {@link #parseRequest()}, never <code>null</code> if m_compSummary is
    * <code>null</code> after that.
    */
   private PSComponentSummary m_parentSummary;

}



