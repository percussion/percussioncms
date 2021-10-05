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

package com.percussion.cms.objectstore.server;

import com.percussion.cms.IPSCmsErrors;
import com.percussion.cms.IPSConstants;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.PSItemDefSummary;
import com.percussion.content.IPSMimeContentTypes;
import com.percussion.data.IPSInternalResultHandler;
import com.percussion.data.PSExecutionData;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.data.PSMimeContentResult;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.security.PSSecurityToken;
import com.percussion.server.PSInternalRequest;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSPurgableTempFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * This class encapsulates the knowledge to get the data for any binary 
 * field within a particular content type. 
 * <p>Instances of this class are immutable.
 *
 * @author paulhoward
 */
public class PSFieldRetriever
{
   /**
    * The only ctor.
    * 
    * @param contentTypeId The type from which data will be retrieved. The id
    * is validated when the data is requested.
    */
   public PSFieldRetriever(long contentTypeId)
   {
      m_contentTypeId = contentTypeId;
   }
   
   /**
    * Makes an internal request to the content editor specified in the ctor
    * to obtain the binary data for the supplied field using the binary
    * command. 
    * <p>
    * No validation is performed on the supplied name (i.e. that it names
    * an existing binary field).
    * 
    * @param req used to obtain a security token. If not supplied, the rx
    *    server user will be used.
    * @param itemId the item that contains the field to be retrieved. Never
    *    <code>null</code>. Revision must be > 0.
    * @param fieldName never <code>null</code> or empty.
    * @param childRowId the row id if the data is to be loaded from a child
    *    item, &lt; 0 otherwise.
    * @return an array containing the data. Never <code>null</code>, may be
    *    empty.
    * @throws PSInvalidContentTypeException if the type supplied in the ctor
    *    does not identify a running content editor.
    * @throws PSCmsException If the fieldName is not contained by the content
    *    editor set in ctor or it is not a 'binary' field or any problems occur 
    *    obtaining the data. 
    */
   public byte[] getFieldContent(PSRequest req, PSLocator itemId,
      String fieldName, int childRowId) throws PSCmsException,
      PSInvalidContentTypeException
   {
      String urlToApp = getRequestUrl(req, itemId, fieldName, childRowId);
      Map params = prepareParams(itemId, fieldName, childRowId);
      
      byte[] results = requestData(req, urlToApp, params);
      if (null == results)
         results = new byte[0];
      
      return results;
   }

   /**
    * Makes an internal request to the content editor specified in the ctor
    * to obtain the binary temp file for the supplied field using the binary
    * command. 
    * <p>
    * No validation is performed on the supplied name (i.e. that it names
    * an existing binary field).
    * 
    * @param req used to obtain a security token. If not supplied, the rx
    *    server user will be used.
    * @param itemId the item that contains the field to be retrieved. Never
    *    <code>null</code>. Revision must be > 0.
    * @param fieldName never <code>null</code> or empty.
    * @param childRowId the row id if the data is to be loaded from a child
    *    item, &lt; 0 otherwise.
    * @return a purgable temp file containing the data, may be 
    *    <code>null</code> if the field does not contain any data.
    * @throws PSInvalidContentTypeException if the type supplied in the ctor
    *    does not identify a running content editor.
    * @throws PSCmsException If the fieldName is not contained by the content
    *    editor set in ctor or it is not a 'binary' field or any problems occur 
    *    obtaining the data. 
    */
   public PSPurgableTempFile getFieldContentFile(PSRequest req, 
      PSLocator itemId, String fieldName, int childRowId) 
      throws PSCmsException, PSInvalidContentTypeException
   {
      String urlToApp = getRequestUrl(req, itemId, fieldName, childRowId);
      Map params = prepareParams(itemId, fieldName, childRowId);

      PSMimeContentResult mimeContent = requestMimeContent(req, urlToApp, 
         params);
      if (mimeContent != null)
         return mimeContent.getFileResource();
      
      return null;
   }
   
   /**
    * Validates all supplied parameters and returns the request url.
    * 
    * @param req used to obtain a security token. If not supplied, the rx
    *    server user will be used.
    * @param itemId the item that contains the field to be retrieved. Never
    *    <code>null</code>. Revision must be > 0.
    * @param fieldName never <code>null</code> or empty.
    * @param childRowId the row id if the data is to be loaded from a child
    *    item, &lt; 0 otherwise.
    * @return the prepared url
    */
   private String getRequestUrl(PSRequest req, PSLocator itemId, 
      String fieldName, int childRowId) 
      throws PSCmsException, PSInvalidContentTypeException
   {
      if (null == itemId || !isLocatorValid(itemId))
         throw new IllegalArgumentException("Invalid locator.");   

      if (null == fieldName || fieldName.trim().length() == 0)
         throw new IllegalArgumentException("fieldName cannot be null or empty");
      
      PSSecurityToken tok;
      if (req != null)
         tok = req.getSecurityToken();
      else
         tok = PSRequest.getContextForRequest().getSecurityToken();
      
      return getUrlToApp(tok);
   }
   
   /**
    * Is Locator valid?
    *
    * @param locator to check.  Assumed not <code>null</code>.
    * @return <code>true</code> if it has an id > 0 and a revision > 0, otherwise
    * <code>false</code>.
    */
   protected boolean isLocatorValid(PSLocator locator)
   {
      boolean isValid = true;
      if (locator.getId() < 1 || locator.getRevision() < 1)
         isValid = false;

      return isValid;
   }

   /**
    * Add the necessary parameters.
    * 
    * @param itemId assumed not <code>null</code> and valid. See 
    *    {@link #getFieldContent(PSRequest, PSLocator, String, int) 
    *    getFieldContent} for more info.
    * 
    * @param fieldName assumed not <code>null</code> or empty. See 
    *    {@link #getFieldContent(PSRequest, PSLocator, String, int) 
    *    getFieldContent} for more info.
    * @param childRowId the childRowId for retrieving the correct
    *    child row data. If < 0 then we are not trying to retrieve a child row. 
    */
   private Map prepareParams(PSLocator itemId, String fieldName, int childRowId)
   {
      Map<String, Object> params = new HashMap<>();
      params.put(IPSHtmlParameters.SYS_COMMAND,
         IPSMimeContentTypes.MIME_ENC_BINARY);
      params.put(IPSHtmlParameters.SYS_CONTENTID, 
         new Integer(itemId.getId()));
      params.put(IPSHtmlParameters.SYS_REVISION, 
         new Integer(itemId.getRevision()));
      params.put(IPSConstants.SUBMITNAME_PARAM_NAME, 
         fieldName);

      if (childRowId >= 0)
         params.put("sys_childrowid", Integer.toString(childRowId));
      
      return params;
   }

   // see interface for description
   public Object clone()
   {
      PSFieldRetriever copy;
      try
      {
         copy = (PSFieldRetriever)super.clone();
         //all member variables are handled by the base clone
      }
      catch (CloneNotSupportedException e)
      {
         /* not possible */
         throw new RuntimeException("PSFieldRetriever.clone() caught " +
            "exception: \n" + e.toString());         
      }
   
      return copy;
   }
   
   /**
    * Implements {@link Object#equals(Object)} for this class.  Does 
    * not consider the <code>PSRequest</code> object supplied during ctor.
    */   
   public boolean equals(Object o)
   {
      boolean isEqual = true;
      
      if (!(o instanceof PSFieldRetriever))
         isEqual = false;
      else
      {
         PSFieldRetriever other = (PSFieldRetriever)o;
         if (m_contentTypeId != other.m_contentTypeId)
            isEqual = false;
      }
      
      return isEqual;
   }
   
   // see base class
   public int hashCode()
   {
      return new Long(m_contentTypeId).hashCode();
   }
   

   /**
    * Gets the internal request path.
    * 
    * @param tok Used to obtain the content editor def. Assumed not <code>null
    * </code>.
    * 
    * @throws PSInvalidContentTypeException if content type cannot be located.
    */
   private String getUrlToApp(PSSecurityToken tok)
      throws PSCmsException, PSInvalidContentTypeException
   {
      PSItemDefManager mgr = PSItemDefManager.getInstance();
      String url = null;
      PSItemDefSummary ids =
         mgr.getSummary(m_contentTypeId, tok);

      if (ids == null)
         throw new PSCmsException(
            IPSCmsErrors.INVALID_CONTENT_TYPE_ID,
            mgr.contentTypeIdToName(m_contentTypeId));

      url = ids.getEditorUrl();

      if (url == null)
         throw new PSCmsException(
            IPSCmsErrors.REQUIRED_DOCUMENT_MISSING_ERROR,
            url);

      return stripUrlExtras(url);
   }

   /**
    * Removes the "../" and ".html" from the content editor url.
    * 
    * @param theUrl assumed not <code>null</code> and containing "." and "/".
    * @return the supplied url with the prefix and suffix removed as described,
    *    never <code>null</code> or empty.
    */
   private String stripUrlExtras(String theUrl)
   {
      String ax = theUrl.substring(0, theUrl.lastIndexOf("."));
      return ax.substring(ax.indexOf("/") + 1, ax.length());
   }

   /**
    * Make the binary request and return the data.
    * 
    * @param requestUrl The url to use for the request, assumed not 
    * <code>null</code> or empty.
    * 
    * @return the data, may be <code>null</code> if there is no data.
    * 
    * @throws PSCmsException if there is a problem acquiring
    * the data.
    */
   private byte[] requestData(PSRequest request, String requestUrl, Map params) 
      throws PSCmsException
   {
      InputStream inStream = null;
      byte[] data = null;
      try
      {
         PSMimeContentResult mimeContent = requestMimeContent(request, 
            requestUrl, params);
         
         if (mimeContent != null)
         {
            inStream = mimeContent.getContent();
            if (inStream != null)
            {
               data = new byte[inStream.available()];
               inStream.read(data);
            }
         }
         
         return data;
      }
      catch (IOException e)
      {
         String[] errs = { "" + m_contentTypeId, e.getMessage()};
         throw new PSCmsException(IPSCmsErrors.CONTENT_TYPE_CANNOT_BE_OPENED,
            errs);
      }
      finally
      {
         if (inStream != null)
         {
            try
            {
               inStream.close();
            }
            catch (IOException e)
            {
               // ignore
            }
         }
      }
   }

   /**
    * Make the binary request.
    * 
    * @param requestUrl t url to use for the request, assumed not 
    *    <code>null</code> or empty.
    * @return the mime content result, may be <code>null</code> if there is 
    *    no data.
    * @throws PSCmsException if there is a problem acquiring the result.
    */
   private PSMimeContentResult requestMimeContent(PSRequest request, 
      String requestUrl, Map params) throws PSCmsException
   {
      PSExecutionData execData = null;
      try
      {
         PSInternalRequest ir = PSServer.getInternalRequest(requestUrl, 
            request, params, false);
         IPSInternalResultHandler rh = 
            (IPSInternalResultHandler) ir.getInternalRequestHandler();

         execData = rh.makeInternalRequest(ir.getRequest());

         return rh.getMimeContent(execData, false);
      }
      catch (PSInternalRequestCallException e)
      {
         String[] errs = { "" + m_contentTypeId, e.getMessage() };
         throw new PSCmsException(IPSCmsErrors.CONTENT_TYPE_CANNOT_BE_OPENED,
            errs);
      }
      catch (PSAuthorizationException e)
      {
         String[] errs = { "" + m_contentTypeId, e.getMessage() };
         throw new PSCmsException(IPSCmsErrors.CONTENT_TYPE_CANNOT_BE_OPENED,
            errs);
      }
      catch (PSAuthenticationFailedException e)
      {
         String[] errs = { "" + m_contentTypeId, e.getMessage() };
         throw new PSCmsException(IPSCmsErrors.CONTENT_TYPE_CANNOT_BE_OPENED,
            errs);
      }
      finally
      {
         if (execData != null)
            execData.release();
      }
   }
   
   /**
    * The content type id of the object to locate. Set in ctor, then never
    * modified. Not validated until the data is actually requested.
    */
   private long m_contentTypeId;
}
