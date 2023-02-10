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
package com.percussion.server;

import com.percussion.util.IOTools;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

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
