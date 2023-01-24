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
package com.percussion.cms.objectstore.client;

import com.percussion.cms.objectstore.PSBinaryValue;

import java.io.IOException;
import java.io.InputStream;


/**
 * This class holds the value as binary.  Once the value is set via constructor
 * or setData, the arguments are immediately converted to a <code>byte[]</code>.
 * Also hold binary file meta data such as filename and content type.
 */
public class PSBinaryValueEx extends PSBinaryValue
{
   /**
    * Creates an instance with the binary <code>content</code> as its value.
    *
    * @param content - the bytes to be the value.  This class takes ownership
    * of the array.  If <code>null</code>, a new empty array will be created.
    * 
    * @param filename the filename for this binary. May be <code>null</code>.
    * 
    * @param contentType the content type for this binary. 
    * May be <code>null</code>.
    */
   public PSBinaryValueEx(byte[] content, String filename, String contentType)
   {
      super(content);
      m_filename = filename;
      m_contentType = contentType;
   }

   /**
    * Creates an instance with the <code>InputStream</code> <code>content</code>
    * as its value.
    *
    * @param content - the InputStream to be used as the value.  Must not be
    * <code>null</code>.  This method assumes ownership of the stream and is
    * 
    * responsible for closing it.
    * @param the filename for this binary. May be <code>null</code>.
    * 
    * @param contentType the content type for this binary. 
    * May be <code>null</code>.
    * 
    * @throws IOException if there is a problem with the stream.
    */
   public PSBinaryValueEx(
      InputStream content, String filename, String contentType)
      throws IOException
   {
      super(content);
      m_filename = filename;
      m_contentType = contentType;      
   }
  
  
   /**
    * Returns the content type for this binary.
    * @return the content type for this binary. May be <code>null</code>.
    */
   public String getContentType()
   {
      return m_contentType;
   }

   /**
    * Returns the filename for this binary.
    * @return the filename for this binary. May be <code>null</code>.
    */
   public String getFilename()
   {
      return m_filename;
   }

   /**
    * Sets the content type for this binary.
    * @param the content type for this binary. May be <code>null</code>.
    */
   public void setContentType(String string)
   {
      m_contentType = string;
   }

   /**
    * Sets the filename for this binary.
    * @param the filename for this binary. May be <code>null</code>.
    */
   public void setFilename(String string)
   {
      m_filename = string;
   }   
   
   /**
    * The filename for this binary. May be <code>null</code>.
    * Initialized in the ctor.
    */
   private String m_filename;
   
   /**
    * The content type for this binary. May be <code>null</code>.
    * Initialized in the ctor.
    */
   private String m_contentType;   

}
