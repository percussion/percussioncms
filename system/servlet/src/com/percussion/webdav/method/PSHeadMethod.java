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
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSDateValue;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.PSTextValue;
import com.percussion.cms.objectstore.client.PSRemoteAgent;
import com.percussion.cms.objectstore.client.PSRemoteException;
import com.percussion.cms.objectstore.ws.PSClientItem;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.webdav.PSWebdavServlet;
import com.percussion.webdav.PSWebdavStatus;
import com.percussion.webdav.error.PSWebdavException;
import com.percussion.webdav.objectstore.PSWebdavContentType;

/**
 * This class implements the HEAD WebDAV method.
 */
public class PSHeadMethod extends PSWebdavMethod
{

   /**
    * Constructs an instance from the given parameters.
    *
    * @param req   The servlet request, it may not be <code>null</code>.
    * @param resp  The servlet response, it may not be <code>null</code>.
    * @param servlet The webdav servlet, it may not be <code>null</code>.    
    */
   public PSHeadMethod(
      HttpServletRequest req,
      HttpServletResponse resp,
      PSWebdavServlet servlet)
   {
      super(req, resp, servlet);
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
         setResponseStatus(PSWebdavStatus.SC_NOT_FOUND);
         return;
      }

      try
      {
         if (! compSummary.isFolder())
         {  
            // get the length, mime-type and last modified date
            PSRemoteAgent agent = getRemoteAgent();
            PSLocator locator = PSWebdavUtils.getLocator(compSummary,
                  getRequest());
            PSClientItem item = agent.openItem(locator, false, false);
            long id = compSummary.getContentTypeId();
            PSWebdavContentType contentType = getConfig().getContentType(id);
            // get length
            int length = 0;
            String fieldName = contentType.getFieldName(P_GETCONTENTLENGTH);
            PSItemField field = PSWebdavUtils.getFieldByName(item, fieldName);
            PSTextValue textValue = (PSTextValue) field.getValue();
            if (textValue != null)
               length = Integer.parseInt(textValue.getValueAsString());
            
            // get mime type
            String mimetype = null;
            fieldName = contentType.getFieldName(P_GETCONTENTTYPE);
            field = PSWebdavUtils.getFieldByName(item, fieldName);
            textValue = (PSTextValue) field.getValue();
            if (textValue != null)
               mimetype = textValue.getValueAsString();
               
            // get last modified
            Date lastModified = null;
            fieldName = contentType.getFieldName(P_GETLASTMODIFIED);
            field = item.getFieldByName(fieldName);
            if (field != null)
            {
               PSDateValue dateValue = (PSDateValue) field.getValue();
               if (dateValue != null)
                  lastModified = (Date) dateValue.getValue();
            }
            else // let's try to get it from the summary
            {
               lastModified = compSummary.getContentLastModifiedDate();
            }

            // set the response header 
            HttpServletResponse resp = getResponse();
            resp.setContentLength(length);
            if (mimetype != null)  
               resp.setContentType(mimetype);
            if (lastModified != null)
               resp.setDateHeader(H_LASTMODIFIED, lastModified.getTime());          
         }
         
         setResponseStatus(PSWebdavStatus.SC_OK);
      }
      catch (PSRemoteException e)
      {
         throw new PSWebdavException(e);
      }
   }

}



