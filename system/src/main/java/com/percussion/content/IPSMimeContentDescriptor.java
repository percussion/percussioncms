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
package com.percussion.content;

public interface IPSMimeContentDescriptor
{
   /**
    * Returns the name of the standard MIME type for this content (e.g., text/xml).
    * See http://www.isi.edu/in-notes/iana/assignments/media-types/media-types
    * for an official list of MIME types.
    *
    * This method must never return null. If the MIME type is not known,
    * it should return "application/octet-stream".
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/11/8
    * 
    * @return   String
    */
   public String getMimeType();

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
    */
   public String getTransferEncoding();

   /**
    * Gets the standard IANA name for the character encoding of this character
    * data, or <CODE>null</CODE> if the character is not applicable
    * (e.g., for binary content).
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/11/8
    * 
    * @return   String
    */
   public String getCharEncoding();

}
