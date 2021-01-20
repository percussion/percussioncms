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



