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
