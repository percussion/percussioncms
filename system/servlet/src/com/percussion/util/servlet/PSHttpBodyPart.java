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
package com.percussion.util.servlet;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

/**
 * This is a container class to hold a body part which may be used to POST
 * to a HTTP server.
 *
 * @author DavidBenua
 */
class PSHttpBodyPart
{
   /**
    * Constrcuts an instance from the given parameters.
    *
    * @param fieldName The name of the field, it may not be <code>null</code>
    *    or empty.
    *
    * @param filename the path name of the file to attach. It may be
    *    <code>null</code> or empty.
    *
    *  @param mimeType The mime type of the content, it may not be
    *    <code>null</code> or empty.
    *
    * @param encoding The encoding of the content, it may be <code>null</code>
    *    if the content is for in bytes only.
    *
    * @param bos The byte array which contains the content. It may be
    *    <code>null</code> if there is no content.
    */
   public PSHttpBodyPart(
      String fieldName,
      String fileName,
      String mimeType,
      String encoding,
      ByteArrayOutputStream bos)
   {
      if (fieldName == null || fieldName.trim().length() ==0)
         throw new IllegalArgumentException(
            "fieldName may not be null or empty.");
      if (mimeType == null || mimeType.trim().length() ==0)
         throw new IllegalArgumentException(
            "m_mimeType may not be null or empty.");

      m_fieldName = fieldName;
      m_fileName = fileName;
      m_mimeType = mimeType;
      m_encoding = encoding;
      m_bos = bos;
   }

   /**
    * Get the encoding of the content.
    *
    * @return The encoding, it may be <code>null</code> if the content is
    *    in bytes only.
    */
   public String getEncoding()
   {
      return m_encoding;
   }

   /**
    * Get the field name.
    *
    * @return The field name, it never <code>null</code> or empty.
    */
   public String getFieldName()
   {
      return m_fieldName;
   }
   
   /**
    * Get the file name.
    *
    * @return The file name, may be <code>null</code> or empty.
    */
   public String getFileName()
   {
      return m_fileName;
   }   

   /**
    * Get the mime type of the content.
    *
    * @return The mime type, never <code>null</code> or empty.
    */
   public String getMimeType()
   {
      return m_mimeType;
   }

   /**
    * Get the content as stream.
    *
    * @return The stream, it may be <code>null</code> if there is no content.
    */
   public OutputStream getStream()
   {
      return (OutputStream) m_bos;
   }

   /**
    * Get the content as byte array.
    *
    * @return The byte array, it may be <code>null</code> if there is no content
    */
   public byte[] getBytes()
   {
      if (m_bos == null)
         return null;
      else
         return m_bos.toByteArray();
   }

   /**
    * The name of the field, initialized by ctor, never <code>null</code>
    * or empty after that.
    */
   private String m_fieldName;
   
   /**
    * The name of the file, initialized by ctor, never <code>null</code>
    * or empty after that.
    */
   private String m_fileName;

   /**
    * The mime type of the content, initialized by ctor, never <code>null</code>
    * or empty after that.
    */
   private String m_mimeType;

   /**
    * The byte array which contains the content. It may be
    * <code>null</code> if there is no content.
    */
   private ByteArrayOutputStream m_bos;

   /**
    * Encoding The encoding of the content, it may be <code>null</code>
    * if the content is for in bytes only.
    */
   private String m_encoding;

}
