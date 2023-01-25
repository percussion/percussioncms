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
import com.percussion.cms.objectstore.client.PSBinaryValueEx;
import com.percussion.cms.objectstore.client.PSRemoteAgent;
import com.percussion.cms.objectstore.client.PSRemoteException;
import com.percussion.cms.objectstore.ws.PSClientItem;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.error.PSException;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSCharSets;
import com.percussion.webdav.PSWebdavServlet;
import com.percussion.webdav.PSWebdavStatus;
import com.percussion.webdav.error.PSWebdavException;
import com.percussion.webdav.objectstore.PSWebdavContentType;


/**
 * This class implements the PUT WebDAV method.
 */
public class PSPutMethod extends PSWebdavMethod
{

   /**
    * Constructs an instance from the given parameters.
    *
    * @param req   The servlet request, it may not be <code>null</code>.
    * @param resp  The servlet response, it may not be <code>null</code>.
    * @param servlet The webdav servlet, it may not be <code>null</code>.
    */
   public PSPutMethod(
      HttpServletRequest req,
      HttpServletResponse resp,
      PSWebdavServlet servlet)
   {
      super(req, resp, servlet);
   }

   // Implements PSWebdavMethod.parseRequest() 
   protected void parseRequest() throws PSWebdavException
   {
      String compPath = getRxVirtualPath();
      m_compSummary = getComponentByPath(compPath);
         
      if (m_compSummary == null)
      {
         m_parentSummary = getParentSummary(compPath);
      }
   }
   
   // Implements PSWebdavMethod.processRequest() 
   protected void processRequest()
      throws PSWebdavException,
             IOException
   {
      if (m_compSummary != null && (! validateLock(m_compSummary)))
         return;
         
      try
      {
         PSRemoteAgent agent = getRemoteAgent();         
         PSClientItem item = null;
         String mimetype = getMimeType();
         PSWebdavContentType contentType = null;
         boolean checkinAfterUpdate = false;
         boolean hasTransToQuickEdit = false;
         
         if (m_compSummary != null)
         {
            // get an existing item.
            contentType = getConfig().getContentType(
                  m_compSummary.getContentTypeId());
            boolean openWithCheckout = false;
            String checkoutUser = m_compSummary.getCheckoutUserName();
            if (checkoutUser == null || checkoutUser.trim().length() == 0)
            {
               openWithCheckout = true;
               checkinAfterUpdate = true;

               try
               {
                  hasTransToQuickEdit = PSWebdavUtils
                        .transitionFromPublicToQuickEdit(m_compSummary,
                              getConfig(), getWorkflowInfo(), getRemoteAgent(),
                              getRequest());
               }
               catch (PSRemoteException pex)
               {
                  throw new PSWebdavException(pex);
               }
               
            }
            item = agent.openItem(
               m_compSummary.getEditLocator(), false, openWithCheckout);
         }
         else
         {
            // get a new item with its default value
            contentType = getConfig().getContentType(mimetype);
            item = agent.newItemDefault(String.valueOf(contentType.getId()));
            checkinAfterUpdate = true;
         }
         
         setItemFields(item, contentType, m_compSummary == null);
         
         // finally, save the item         
         PSLocator locator = agent.updateItem(item, checkinAfterUpdate);

         
         if (m_compSummary == null)
         {
            // create the folder relationship for new item
            PSWebdavUtils.addToParentFolder(
               m_parentSummary,
               locator,
               getRemoteRequester());
          
            setResponseStatus(PSWebdavStatus.SC_CREATED);
         }
         else
         {
            // for an existing item, transition back to public if needed
            if (hasTransToQuickEdit)
            {
               // reload the summary since the state of the item is changed
               m_compSummary = getComponentByPath(getRxVirtualPath());
               
               PSWebdavUtils.transitionFromQuickEditToPublic(m_compSummary,
                     getConfig(), getWorkflowInfo(), agent, getRequest(), true);
            }
            setResponseStatus(PSWebdavStatus.SC_OK);
         }
      }
      catch (PSException e)
      {
         throw new PSWebdavException(e);
      }

   }

   /**
    * Set the values for all WebDAV specific fields.  
    * 
    * @param item the updated item, assume not <code>null</code>. 
    * @param contentType the content type of the above item.
    * @param isNewItem if <code>true</code>, then the supplied item has not
    *    been persisted in the repository.
    * 
    * @throws IOException if fail to get the content from the request.
    */
   private void setItemFields(PSClientItem item,
         PSWebdavContentType contentType, boolean isNewItem)
      throws IOException
   {
      String mimetype = getMimeType();
      
      // set the display name
      String filename = getSourceFileName();
      String fieldName = contentType.getFieldName(P_DISPLAYNAME);
      PSItemField itemField = item.getFieldByName(fieldName);
      itemField.addValue(new PSTextValue(filename));
      
      itemField = item.getFieldByName("item_file_attachment_filename");
      if (itemField != null) {
         itemField.addValue(new PSTextValue(filename));
      }

      // set the sys_title if needed
      if ((!fieldName.equalsIgnoreCase(SYS_TITLE))
         && m_compSummary == null)
      {
         itemField = item.getFieldByName(SYS_TITLE);
         itemField.addValue(new PSTextValue(filename));
      }
      
      // set mime type field
      fieldName = contentType.getFieldName(P_GETCONTENTTYPE);
      itemField = item.getFieldByName(fieldName);
      itemField.addValue(new PSTextValue(mimetype));
      
      
      // set the content field
      itemField = item.getFieldByName(contentType.getContentField());
      byte[] content = getContent();
      
      // don't set the binary field for a new item with empty content
      if ((!isNewItem) || (content.length > 0))
      {
         if (itemField.getItemFieldMeta().isBinary())
         {
            itemField.addValue(new PSBinaryValueEx(content, filename, mimetype));            
         }
         else
         {
            String charset = getRequest().getCharacterEncoding();
            if (charset == null || charset.trim().length() == 0)
               charset = PSCharSets.rxJavaEnc();
            itemField.addValue(new PSTextValue(new String(content, charset)));
         }
      }
      
      // set the content length
      fieldName = contentType.getFieldName(P_GETCONTENTLENGTH);
      itemField = item.getFieldByName(fieldName);
      String length = String.valueOf(content.length);
      itemField.addValue(new PSTextValue(length));
      
      // set the modified date
      fieldName = contentType.getFieldName(P_GETLASTMODIFIED);
      itemField = item.getFieldByName(fieldName);
      if (itemField != null)
      {
         Date currentDate = new Date(System.currentTimeMillis());
         itemField.addValue(new PSDateValue(currentDate));
      }
      
      // set values for created item 
      
      if (m_compSummary == null)
      {
         // set the creation date 
         fieldName = contentType.getFieldName(P_CREATIONDATE);
         itemField = item.getFieldByName(fieldName);
         if (itemField != null)
         {
            Date currentDate = new Date(System.currentTimeMillis());
            itemField.addValue(new PSDateValue(currentDate));
         }
         
         // set the community id 
         itemField = item.getFieldByName(IPSHtmlParameters.SYS_COMMUNITYID);
         if (itemField != null)
         {
            String commId = String.valueOf(getConfig().getCommunityId());
            itemField.addValue(new PSTextValue(commId));
         }
         // set the locale
         itemField = item.getFieldByName(IPSHtmlParameters.SYS_LANG);
         if (itemField != null)
         {
            itemField.addValue(new PSTextValue(getConfig().getLocale()));
         }
      }
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




