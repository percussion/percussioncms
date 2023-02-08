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
package com.percussion.cms.objectstore.server;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSBinaryLocator;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.server.PSRequest;
import com.percussion.util.PSPurgableTempFile;

/**
 * This class facilitates the locating of binary content through the use of
 * internal Rhythmyx objects and returning the data of that binary content.
 */
public class PSServerBinaryLocator extends PSFieldRetriever 
   implements IPSBinaryLocator
{
   /**
    * Convenience ctor that calls
    * {@link #PSServerBinaryLocator(PSRequest, PSLocator, String, long, int)
    * this(request, locator, fieldName, contentTypeId, -1)}.
    */
   public PSServerBinaryLocator(PSRequest request, PSLocator locator,
      String fieldName, long contentTypeId)
   {
      this(request, locator, fieldName, contentTypeId, -1);
   }

   /**
    * Constructs the object.
    * 
    * @param request to create an internal request. May be <code>null</code> to
    *    use a request for the internal user.
    * @param locator created to locate content items. Must have an id set and a
    *    revision. Must not be <code>null</code>.
    * @param fieldName the name of the field containing the binary information.
    *    Must not be <code>null</code> or empty.
    * @param contentTypeId the id of the content type containing the field.
    * @param childRowId the row id if the data is to be loaded from a child
    *    item, <code>-1</code> otherwise.
    */
    public PSServerBinaryLocator(PSRequest request, PSLocator locator,
      String fieldName, long contentTypeId, int childRowId)
    {
       super(contentTypeId);
       
       if (request == null)
          throw new IllegalArgumentException("request must not be null");

       if (locator == null && isLocatorValid(locator))
          throw new IllegalArgumentException(
            "locator must not be null or invalid");

       if (fieldName == null || fieldName.length() == 0)
          throw new IllegalArgumentException(
            "fieldName must not be null or empty");

       m_request = request;
       m_locator = locator;
       m_fieldName = fieldName;
       m_childRowId = childRowId;
   }

   /**
    * See interface for complete description.
    * 
    * @return A byte[] w/ 0 or more length, never <code>null</code>.
    */
   public Object getData() throws PSCmsException, PSInvalidContentTypeException
   {
      return getFieldContent(m_request, m_locator, m_fieldName, m_childRowId);
   }
   
   /* (non-Javadoc)
    * @see IPSBinaryLocator#getDataFile()
    */
   public PSPurgableTempFile getDataFile() throws PSCmsException, 
      PSInvalidContentTypeException
   {
      return getFieldContentFile(m_request, m_locator, m_fieldName, 
         m_childRowId);
   }

   // see interface for description
   @Override
   public Object clone()
   {
      PSServerBinaryLocator copy = (PSServerBinaryLocator) super.clone();
      if (m_request != null)
         copy.m_request = m_request.cloneRequest();
      copy.m_locator = (PSLocator) m_locator.clone();
   
      return copy;
   }
   
   /**
    * Implements {@link Object#equals(Object)} for this class. Does 
    * not consider the <code>PSRequest</code> object supplied during ctor.
    */   
   @Override
   public boolean equals(Object o)
   {
      boolean isEqual = true;
      
      if (!(o instanceof PSServerBinaryLocator))
         isEqual = false;
      else
      {
         PSServerBinaryLocator other = (PSServerBinaryLocator)o;
         if (!super.equals(o))
            isEqual = false;
         else if (m_childRowId != other.m_childRowId)
            isEqual = false;
         else if (!m_fieldName.equals(other.m_fieldName))
            isEqual = false;
         else if (!m_locator.equals(other.m_locator))
            isEqual = false;
      }
      
      return isEqual;
   }
   
   // see base class
   @Override
   public int hashCode()
   {
      /*
       * Need to combine the super hash and row id because they could 
       * theoretically be 'equal and opposite' in a different object. e.g.
       * say in obj 1, content id is 3 and row id is 7. In another obj, the
       * content id could be 7 while the row id is 3.
       */
      int hash = ("" + super.hashCode() + m_childRowId + m_fieldName).hashCode();
      hash += m_locator.hashCode();
      
      return hash;
   }
   
   /**
    * The request used to locate the binary object, may be <code>null</code> in
    * which case a request for the internal user will be used.
    */
   private PSRequest m_request;

   /**
    * The locator of the binary object, never <code>null</code>.
    */
   private PSLocator m_locator;

   /**
    * The fieldname in which the binary object is stored, never 
    * <code>null</code> or empty.
    */
   private String m_fieldName;

   /**
    * The childRowId for retrieving the correct child row data. If -1 then we 
    * are not trying to retrieve a child row. Set during ctor, never modified 
    * after that.
    */
   private int m_childRowId = -1;
}
