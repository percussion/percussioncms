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
package com.percussion.search.lucene;

/**
 * This file consists of constants that are used in lucene search engine
 * implementation.
 * 
 */
public interface IPSLuceneConstants
{
   /**
    * Mime type for text content
    */
   public static final String MIME_TYPE_PLAIN_BY_TEXT = "text/plain";

   /**
    * Mime type for pdf content
    */
   public static final String MIME_TYPE_APPLICATION_BY_PDF = "application/pdf";

   /**
    * Mime type for html content
    */
   public static final String MIME_TYPE_TEXT_BY_HTML = "text/html";
   
   /**
    * Mime type for html content
    */
   public static final String MIME_TYPE_APPLICATION_BY_XHTML = "application/xhtml+xml";
   
   /**
    * Mime type for word content
    */
   public static final String MIME_TYPE_APPLICATION_BY_MSWORD = "application/msword";

   /**
    * Mime type for xml content
    */
   public static final String MIME_TYPE_TEXT_BY_XML = "text/xml";

   /**
    * Mime type for xml content
    */
   public static final String MIME_TYPE_APPLICATION_BY_XML = "application/xml";

   /**
    * Mime type for rtf content
    */
   public static final String MIME_TYPE_APPLICATION_BY_RTF = "application/rtf";

   /**
    * Mime type for excel content
    */
   public static final String MIME_TYPE_APPLICATION_BY_VNDMSEXCEL = "application/vnd.ms-excel";

   /**
    * Mime type for excel content
    */
   public static final String MIME_TYPE_APPLICATION_BY_EXCEL = "application/excel";

   /**
    * Mime type for power point content
    */
   public static final String MIME_TYPE_APPLICATION_BY_VNDMSPOWERPOINT = "application/vnd.ms-powerpoint";
   
   /**
    * Mime type for power point content
    */
   public static final String MIME_TYPE_APPLICATION_BY_MSPOWERPOINT = "application/mspowerpoint";
   
   /**
    * Mime type for word xml (docx) format
    */
   public static final String MIME_TYPE_APPLICATION_BY_OPENXML_MSWORD_DOC = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
   
   /**
    * Mime type for word template (dotx) format
    */
   public static final String MIME_TYPE_APPLICATION_BY_OPENXML_MSWORD_TEMPLATE = "application/vnd.openxmlformats-officedocument.wordprocessingml.template";

   /**
    * Mime type for powerpoint potx format
    */
   public static final String MIME_TYPE_APPLICATION_BY_OPENXML_MSPOWERPOINT_TEMPLATE = "application/vnd.openxmlformats-officedocument.presentationml.template";

   /**
    * Mime type for the powerpoint pptx format
    */
   public static final String MIME_TYPE_APPLICATION_BY_OPENXML_MSPOWERPOINT_PRES = "application/vnd.openxmlformats-officedocument.presentationml.presentation";

   /**
    * Mime type for the Excel xlsx format
    */
   public static final String MIME_TYPE_APPLICATION_BY_OPENXML_MSEXCEL_SHEET = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

   /**
    * Mime type for the Excel xltx format
    */
   public static final String MIME_TYPE_APPLICATION_BY_OPENXML_MSEXCEL_TEMPLATE = "application/vnd.openxmlformats-officedocument.spreadsheetml.template";


   /**
    * Name of the field that holds the content from all fields
    */
   public static final String LUCENE_KEY_FIELD_NAME = "psx:luckey";

   /**
    * Name of the field that holds the content from all fields
    */
   public static final String ALL_CONTENT_FIELD_NAME = "psx:allcontent";

   /**
    * Lucene Field Seperator for all content field
    */
   public static final String ALL_CONTENT_FIELD_SEPERATOR = " #RXLSEP# ";

   /**
    * A property that must be supplied when the instance of this class is
    * obtained. If exists must be a valid directory. All path separators are 
    * normalized to forward slash before use.
    */
   public static final String REQPROP_INDEX_ROOT_DIR = "indexRootDir";


}
