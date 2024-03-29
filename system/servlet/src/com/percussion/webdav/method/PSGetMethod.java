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

import com.percussion.cms.objectstore.IPSFieldValue;
import com.percussion.cms.objectstore.PSBinaryValue;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.client.PSRemoteAgent;
import com.percussion.cms.objectstore.ws.PSClientItem;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.error.PSException;
import com.percussion.error.PSExceptionUtils;
import com.percussion.util.PSCharSets;
import com.percussion.webdav.PSWebdavServlet;
import com.percussion.webdav.PSWebdavStatus;
import com.percussion.webdav.error.PSWebdavException;
import com.percussion.webdav.objectstore.PSWebdavContentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Locale;


/**
 * This class implements the GET WebDAV method.
 */
public class PSGetMethod
   extends PSWebdavMethod
{

   private static final Logger log = LogManager.getLogger(PSGetMethod.class);

   /**
    * Constructs an instance from the given parameters.
    *
    * @param req   The servlet request, it may not be <code>null</code>.
    * @param resp  The servlet response, it may not be <code>null</code>.
    * @param servlet The webdav servlet, it may not be <code>null</code>.
    */
   public PSGetMethod(
      HttpServletRequest req,
      HttpServletResponse resp,
      PSWebdavServlet servlet)
   {
      super(req, resp, servlet);
   }

   // Implements PSWebdavMethod.processRequest() 
   protected void processRequest() throws PSWebdavException, IOException
   {
      String sysCommand = getRequest().getParameter("sys_command");
      if (sysCommand != null)
      {
         processSysCommand(sysCommand);
         return;
      }
      
      String compPath = getRxVirtualPath();

      PSComponentSummary compSummary = getComponentByPath(compPath);
      if (compSummary == null)
      {
         setResponseStatus(PSWebdavStatus.SC_NOT_FOUND);
      }
      else
      {
         if (compSummary.isFolder())
         {
            PSPropFindMethod propFind =
               new PSPropFindMethod(
                  getRequest(),
                  getResponse(),
                  getServlet());
            propFind.prepareForGetMethod();
            propFind.processRequest();
         }
         else
         {
            processRequestForItem(compSummary);
         }
      }
   }

   /**
    * Processing the given sys_command.
    * 
    * @param sysCommand The to be processed command, assume not 
    *    <code>null</code>.
    */
   private void processSysCommand(String sysCommand)
      throws PSWebdavException, IOException
   {
      try
      {
         if(sysCommand != null && 
            sysCommand.equalsIgnoreCase(VALIDATE_CONFIG))
         {
            PSWebdavConfigValidator validator = new PSWebdavConfigValidator(
                  getRequest(), getResponse(), getRemoteRequester());
            try(InputStream configInput = getServlet().getWebdavConfigDef()) {
               validator.validate(configInput, getServlet()
                       .getRegisteredRxRootPaths());
            }
         }
         else if (sysCommand != null && sysCommand.equalsIgnoreCase(GET_CONFIG))
         {
            try(InputStream configInput = getServlet().getWebdavConfigDef()) {
               outputWebdavConfigDef(configInput);
            }
         }
         else // unknown command
         {
            PrintWriter writer = getResponse().getWriter();
            Object[] args = {sysCommand};
            String help =
               PSWebdavConfigValidator.getResourceString("msg.help", args);
            writer.print(help);
            setResponseStatus(PSWebdavStatus.SC_OK);
         }
      }
      catch (Exception e)
      {
         log.error(PSExceptionUtils.getMessageForLog(e));
         log.debug(PSExceptionUtils.getDebugMessageForLog(e));
         
         getResponse().getWriter().print(PSWebdavStatus.getStatusText(PSWebdavStatus.SC_INTERNAL_SERVER_ERROR));
         getResponse().setStatus(PSWebdavStatus.SC_OK);
      }  

   }
   
   /**
    * Get the content of the requested item.
    *
    * @param compSummary The summary of the requested item, assume not 
    *    <code>null</code>.
    * 
    * @throws IOException if I/O error occurs.
    * 
    * @throws PSWebdavException if other error occurs. 
    */
   private void processRequestForItem(PSComponentSummary compSummary)
      throws PSWebdavException, IOException
   {
      PSRemoteAgent agent = getRemoteAgent();
      try
      {
         // get the correct locator
         PSLocator locator = PSWebdavUtils.getLocator(compSummary, getRequest());
         PSClientItem item = agent.openItem(locator, true, false);
         long id = compSummary.getContentTypeId();
         PSWebdavContentType contentType = getConfig().getContentType(id);

         // get mime type
         String fieldName = contentType.getFieldName(P_GETCONTENTTYPE);
         PSItemField itemField = PSWebdavUtils.getFieldByName(item, fieldName);
         IPSFieldValue mimeValue = itemField.getValue();
         String mimeType = "application/octet-stream"; // default mime type
         if (mimeValue != null &&
             mimeValue.getValueAsString().trim().length() != 0)
         {
            mimeType = itemField.getValue().getValueAsString();
         }

         // get last modified date
         Date lastModifiedDate = compSummary.getContentLastModifiedDate();
         
         String localeProp = compSummary.getLocale();

         // get the content
         itemField = PSWebdavUtils.getFieldByName(item, contentType.getContentField());
         IPSFieldValue fieldValue = itemField.getValue();
         byte[] data = null;
         if (fieldValue == null)
         {
            setResponseStatus(PSWebdavStatus.SC_NO_CONTENT);
         }
         else
         {
            String stringEncoding = null;
            if (fieldValue instanceof PSBinaryValue)
            {
               data = (byte[]) fieldValue.getValue();
            }
            else
            {
               String sdata = fieldValue.getValueAsString();
               stringEncoding = PSCharSets.rxJavaEnc();
               data = sdata.getBytes(stringEncoding);
               mimeType = mimeType + "; charset=" + PSCharSets.rxStdEnc();
            }

            // set response headers
            getResponse().setContentType(mimeType);
            if (lastModifiedDate != null)
            {
               getResponse().setDateHeader(
                  H_LASTMODIFIED,
                  lastModifiedDate.getTime());
            }
            if (localeProp != null && localeProp.trim().length() > 0)
            {
               Locale locale = new Locale(localeProp);
               getResponse().setLocale(locale);
            }
            
            // set the status and content of the response
            setResponse(data, mimeType, stringEncoding, PSWebdavStatus.SC_OK);
         }
      }
      catch (PSException e)
      {
         throw new PSWebdavException(e);
      }

   }
   
   /**
    * Retrieves and returns the webdav xml configuration file to
    * the requester as an xml document.
    * 
    * @param in InputStream of the configuration file, assume not 
    *    <code>null</code>.
    * 
    * @throws IOException for any IO errors
    */
   private void outputWebdavConfigDef(InputStream in) throws IOException
   {
      HttpServletResponse resp = getResponse();
      PrintWriter writer = resp.getWriter();
      
      int ch = -1;
      while((ch = in.read()) != -1)
         writer.write(ch);
         
      resp.addHeader("Content-Type", " text/xml");      
   }  
  
   public final static String VALIDATE_CONFIG = "validateConfig";
   public final static String GET_CONFIG = "getConfig";
}




