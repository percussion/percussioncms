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

package com.percussion.data;

import com.percussion.content.IPSMimeContent;
import com.percussion.util.PSPurgableFileInputStream;
import com.percussion.util.PSPurgableTempFile;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Internal request access interface for returning mime content.
 * This will allow internal request handlers to retrieve mime content
 * from the PSResultSetMimeConverter.
 */
public class PSMimeContentResult implements IPSMimeContent
{
   /**
    * Construct a PSMimeContentResult, this will be a temporary file
    * containing raw binary data.
    *
    * @param f The file containing the data.   Never <code>null</code>.
    *
    * @param mimeType The mime type for the content of this file.  May be
    *          <code>null</code>.
    *
    * @throws IllegalArgumentException  if any argument is illegal
    */
   PSMimeContentResult(PSPurgableTempFile f, String mimeType)
   {
      if (f == null)
         throw new IllegalArgumentException("File must be specified.");

      m_file = f;
      m_mimeType = mimeType;
   }

   /**
    * A convenient way to get the file directly. The temp file will be
    * transferred to the caller after this call. This method can only be called
    * once for each instance of this class. The caller is responsible to 
    * delete the temp file. Caller can only make one call to either this 
    * method or {@link #getContent() getContent}, but not both per instance of
    * this class.
    *
    * @return The temporary file for this mime content result,
    *    never <code>null</code>; The caller is responsible to delete the
    *    temp file.
    *
    * @throws IllegalStateException if the temp file has been transferred
    */
   public PSPurgableTempFile getFileResource()
   {
      if (m_file == null)
         throw new IllegalStateException(
               "temp file object is null, it must be transferred already");

      m_fileLength = m_file.length(); // record the current file length
      PSPurgableTempFile tmpFile = m_file;
      m_file = null;

      return tmpFile;
   }

   /* *** IPSMimeContent Interface implementation *** */

   /**
    * Returns the byte stream for this content. This will be a file input
    * stream created by with the temporary file that was supplied at construct
    * time.  This method can only be called once for each instance of this
    * class. Caller can only make one call to either this method or 
    * {@link #getFileResource() getFileResource}, but not both per instance of
    * this class.
    *
    * @return   The byte stream. Caller is responsible to close the stream. The
    *    temp file will be deleted after the stream is closed.
    *
    * @throws IllegalStateException if the temp file has been transferred
    *         or if an exception occurs instantiating the file input stream
    */
   public InputStream getContent()
   {
      if (m_file == null)
         throw new IllegalStateException(
               "temp file object is null, it must be transferred already");

       try
      {
         m_fileLength = m_file.length(); // record the current file length
         PSPurgableTempFile tmpFile = m_file;
         m_file = null;

         return new PSPurgableFileInputStream(tmpFile);
      }
      catch (FileNotFoundException e)
      {
         throw new IllegalStateException(e.toString());
      }
   }

   /**
    * Returns the approximate length of this content in bytes.
    * This will be the length of the temporary file created by
    * the result set mime converter.
    *
    * @return   long The length of the temporary file, in bytes. If the temp
    *    file has been transferred, this is the length of the transferred file.
    */
   public long getContentLength()
   {
      return (m_fileLength == -1) ? m_file.length() : m_fileLength;
   }

   /**
    * Returns the name of this content, or <CODE>null</CODE> if the
    * content has no name.
    *
    * @return <code>null</code> This method is unsupported.
    */
   public String getName()
   {
      return null;
   }

   /**
    * Returns the name of the standard MIME type for this content
    * (e.g., text/xml).
    *
    * See http://www.isi.edu/in-notes/iana/assignments/media-types/media-types
    * for an official list of MIME types.
    *
    * This method must never return null. If the MIME type is not known,
    * it should return "application/octet-stream".
    *
    * @return   The mime type for this file.
    */
   public String getMimeType()
   {
      return m_mimeType == null ? "application/octet-stream" : m_mimeType;
   }

   /**
    * Returns the name of the transfer encoding applied to the content stream.
    * If the stream represents raw bytes (where any octet-sequence may occur),
    * the encoding will be "binary". If the stream represents base64 encoded
    * bytes, the encoding will be "base64" and so on. This may return
    * <CODE>null</CODE> if no encoding is defined.
    *
    * See IPSMimeContentTypes for some of the predefined encodings that
    * we support.
    *
    * @return "binary" - The mime converter always returns things in binary
    *    format.
    */
   public String getTransferEncoding()
   {
      return "binary";
   }

   /**
    * Gets the standard IANA name for the character encoding of this character
    * data, or <CODE>null</CODE> if the character is not applicable
    * (e.g., for binary content).
    *
    * @return   <code>null</code> This method is not supported.
    */
   public String getCharEncoding()
   {
      return null;
   }

   /**
    * Deletes the temp file. This method only need be called if the temp file
    * has not been transferred, that is neither {@link #getContent()} nor
    * {@link #getFileResource()} has been called.
    */
   public void release()
   {
      if (m_file != null)
         m_file.release();
   }

   /**
    * The file object representing the temporary file for this mime content.
    * Initialized by the constructor. It may be <code>null</code> after a
    * call to {@link #getFileResource()}.
    */
   private PSPurgableTempFile m_file;

   /**
    * The length of the file. Initialize to <code>-1</code>. It will be set
    * to the file length after the temp file is transferred to the caller,
    * which is happens in {@link #getFileResource()} or {@link #getContent()}
    */
   private long m_fileLength = -1;

   /**
    * The string representing the mime type for this mime content.  Can be
    * <code>null</code>, initialized by the constructor.
    */
   private String m_mimeType = null;
}
