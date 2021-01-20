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
package com.percussion.cms.objectstore.client;

import java.io.IOException;
import java.io.InputStream;

import com.percussion.cms.objectstore.PSBinaryValue;


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
