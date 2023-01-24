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
