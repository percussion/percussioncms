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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.content;

/**
 * The IPSMimeContentTypes inteface is provided as a convenient mechanism
 * for storing the various mime type names.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public interface IPSMimeContentTypes
{
   /**
    * Content transfer encodings.
    */
   public static final String MIME_ENC_BINARY = "binary";

   public static final String MIME_ENC_BASE64 = "base64";

   public static final String MIME_ENC_QUOPRINT = "quoted-printable";

   /**
    * HTML FORMs use this type (same as GET request query string format).
    */
   public static final String MIME_TYPE_URLENCODED_FORM = 
      "application/x-www-form-urlencoded";

   /**
    * HTML FORMs use this type when they have file attachments.
    */
   public static final String MIME_TYPE_MULTIPART_FORM = "multipart/form-data";

   /**
    * XML data sent as being application specific.
    */
   public static final String MIME_TYPE_APPLICATION_XML = "application/xml";

   /**
    * XML data sent as raw text.
    */
   public static final String MIME_TYPE_TEXT_XML = "text/xml";

   /**
    * XSL data
    */
   public static final String MIME_TYPE_APPLICATION_XSL = "application/xsl-xml";

   /**
    * DTD data
    */
   public static final String MIME_TYPE_APPLICATION_DTD = "application/xml-dtd";

   /**
    * HTML data sent as raw text.
    */
   public static final String MIME_TYPE_TEXT_HTML = "text/html";

   /**
    * Raw text data.
    */
   public static final String MIME_TYPE_TEXT_PLAIN = "text/plain";

   /**
    * An octet stream for unknown mime types.
    */
   public static final String MIME_TYPE_OCTET_STREAM = 
      "application/octet-stream";

   /**
    * JSON mime type
    */
   public static final String MIME_TYPE_JSON = "application/json";
}
