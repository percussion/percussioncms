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
