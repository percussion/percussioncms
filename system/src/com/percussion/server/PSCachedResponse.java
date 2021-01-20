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
package com.percussion.server;

import com.percussion.util.IOTools;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

import org.xml.sax.SAXException;

/**
 * A container for the persistable data from a {@link PSResponse} that may be
 * cached and then used to restore that data to another response so that the 
 * same data may be resent repeatedly.
 */
public class PSCachedResponse extends PSBaseResponse implements Serializable
{
   /**
    * Creates a cachable object from a valid response.  Use {@link 
    * #copyTo(PSResponse)} to restore the cached state to a new response.
    * 
    * @param response The response to cache, may not be <code>null</code>.
    * Content of this response is still available after this call.
    * 
    * @throws IllegalArgumentException if <code>response</code> is 
    *    <code>null</code>.
    * @throws IOException if anything goes wrong caching the input stream or
    *    input document.
    */
   public PSCachedResponse(PSResponse response) throws IOException
   {
      super(response);
      
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      try
      {
         if (m_isContentDoc)
         {
            if (m_contentDoc != null)
               PSXmlDocumentBuilder.write(m_contentDoc, os);
         }
         else
         {
            if (m_contentStream != null)
               IOTools.copyStream(m_contentStream, os);
         }

         m_content = os.toByteArray();
         if (!m_isContentDoc)
         {  // reset the content stream, since it has been used by
            // above and it is not resetable
            response.resetContentStream(new ByteArrayInputStream(m_content));
         }
            
         m_contentStream = null;
         m_contentDoc = null;
      }
      finally
      {
         os.close();
      }
   }
   
   /**
    * Copies the cached data from this object to the supplied 
    * <code>response</code>, replacing the data in that response.
    * 
    * @param response The response to which the data is copied, may not be
    * <code>null</code>.
    * 
    * @throws IllegalArgumentException if response is <code>null</code>.
    */
   public void copyTo(PSResponse response)
   {
      if (response == null)
        throw new IllegalArgumentException("response cannot be null");
        
      response.copyFrom(this);
      if (response.m_isContentDoc)
      {
         try
         {
            response.setContent(PSXmlDocumentBuilder.createXmlDocument(
               new ByteArrayInputStream(m_content), false), 
                  (String) m_entityHeaders.get(EHDR_CONT_TYPE));           
         }
         catch (IOException e)
         {
            /**
             * This cannot happen. The source is buildt in the constructor and
             * never changed after that.
             */
         }
         catch (SAXException e)
         {
            /**
             * This cannot happen. The source is buildt in the constructor and
             * never changed after that.
             */
         }
      }
      else
      {
         response.m_contentStream = new ByteArrayInputStream(m_content);
      }
   }
   
   /**
    * Returns the size of content contained in this response. The length only 
    * includes the byte array but not the object overhead. Used for statistics
    * and memory manager.
    * 
    * @return The content length, in Bytes.
    */
   public long getContentLength()
   {
      return m_content.length;
   }
   
   /**
    * The in-memory cache used for all resonses, whether it is an input stream
    * or a document, initialized in constructor, never changed after that.
    */
   private byte[] m_content = null;
}
