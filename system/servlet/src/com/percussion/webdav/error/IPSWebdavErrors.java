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

package com.percussion.webdav.error;

/**
 * The IPSWebdavErrors interface provides a convenient mechanism
 * for accessing webdav related error codes. The resource bundle file is
 * at com.percussion.hooks.PSServletErrorBundle.properties. The webdav error
 * code ranges are:
 * <TABLE BORDER="1">
 * <TR><TH>Range</TH><TH>Component</TH></TR>
 * <TR><TD>70001 - 70100</TD><TD>xml errors</TD></TR>
 * <TR><TD>70101 - 70500</TD><TD>general errors</TD></TR>
 * </TABLE>
 *
 */
public interface IPSWebdavErrors
{
   /**
    * XML Attribute must be specified for element
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>attribute name</TD></TR>
    * <TR><TD>1</TD><TD>element name</TD></TR>
    * </TABLE>
    */
   public static final int XML_ATTRIBUTE_MUST_BE_SPECIFIED = 70001;
   
   /**
   * Invalid xml format
   * <p>
   * The arguments passed in for this message are:
   * <TABLE BORDER="1">
   * <TR><TH>Arg</TH><TH>Description</TH></TR>
   * <TR><TD>0</TD><TD>element node name</TD></TR>
   * </TABLE>
   */
   public static final int XML_INVALID_FORMAT = 70002;
   
   /**
   * Element cannot be empty
   * <p>
   * The arguments passed in for this message are:
   * <TABLE BORDER="1">
   * <TR><TH>Arg</TH><TH>Description</TH></TR>
   * <TR><TD>0</TD><TD>element node name</TD></TR>
   * </TABLE>
   */
   public static final int XML_ELEMENT_CANNOT_BE_EMPTY = 70003;
   
   /**
   * Failed to create XML from requested content.
   * <p>
   * The arguments passed in for this message are:
   * <TABLE BORDER="1">
   * <TR><TH>Arg</TH><TH>Description</TH></TR>
   * <TR><TD>0</TD><TD>the exception message</TD></TR>
   * </TABLE>
   */
   public static final int XML_FAILED_CREATE_DOC_FROM_CONTENT = 70004;
   
   /**
   * Unsupported webdav method
   * <p>
   * The arguments passed in for this message are:
   * <TABLE BORDER="1">
   * <TR><TH>Arg</TH><TH>Description</TH></TR>
   * <TR><TD>0</TD><TD>the name of the method</TD></TR>
   * </TABLE>
   */
   public static final int UNSUPPORTED_METHOD = 70101;
   
   /**
    * Mimetypes are required if default is set to false
    */
   public static final int MIMETYPES_REQUIRED = 70102;
   
   /**
    * Cannot have duplicate properties.
    */
   public static final int CANNOT_HAVE_DUPLICATE_PROPERTIES = 70103;
   
   /**
   * Missing required property
   * <p>
   * The arguments passed in for this message are:
   * <TABLE BORDER="1">
   * <TR><TH>Arg</TH><TH>Description</TH></TR>
   * <TR><TD>0</TD><TD>name of property</TD></TR>
   * </TABLE>
   */
   public static final int MISSING_REQUIRED_PROPERTY = 70104;
   
   /**
    * Configuration can only have one default content type.
    */
   public static final int CAN_ONLY_HAVE_ONE_DEFAULT_CONTENTTYPE = 70105;
   
   /**
   * IO exception occurred
   * <p>
   * The arguments passed in for this message are:
   * <TABLE BORDER="1">
   * <TR><TH>Arg</TH><TH>Description</TH></TR>
   * <TR><TD>0</TD><TD>Exception message</TD></TR>
   * </TABLE>
   */
   public static final int IO_EXCEPTION_OCCURED = 70106;
   
   /**
   * Missing required property
   * <p>
   * The arguments passed in for this message are:
   * <TABLE BORDER="1">
   * <TR><TH>Arg</TH><TH>Description</TH></TR>
   * <TR><TD>0</TD><TD>exception message</TD></TR>
   * </TABLE>
   */
   public static final int SAX_EXCEPTION_OCCURED = 70107;
   
   /**
   * File does not exist
   * <p>
   * The arguments passed in for this message are:
   * <TABLE BORDER="1">
   * <TR><TH>Arg</TH><TH>Description</TH></TR>
   * <TR><TD>0</TD><TD>file path</TD></TR>
   * </TABLE>
   */
   public static final int FILE_DOES_NOT_EXIST = 70108;
   
   /**
    * Parser configration error
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>file path</TD></TR>
    * </TABLE>
    */
   public static final int PARSER_CONFIG_ERROR = 70109;
   
   /**
    * Duplicate content type names in the configuration file
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the name of the duplicated content type</TD></TR>
    * </TABLE>
    */
   public static final int DUPLICATE_CONTENTTYPE_NAMES = 70110;
   
   /**
    * Cannot find resource from its path
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the path of the resource</TD></TR>
    * </TABLE>
    */
   public static final int RESOURCE_NOT_FIND = 70111;
   
   /**
    * Not allow GET method for folder or collection resource
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the path of the folder or resource</TD></TR>
    * </TABLE>
    */
   public static final int FORBIDDEN_GET_FOLDER = 70112;
   
   /**
    * An expected header is missing from the request
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the expected missing header</TD></TR>
    * </TABLE>
    */
   public static final int HEADER_MISSING = 70113;
   
   /**
    * Source and Target are the same is forbidden
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>source path</TD></TR>
    * <TR><TD>1</TD><TD>method name</TD></TR>
    * </TABLE>
    */
   
   public static final int FORBIDDEN_SRC_TARGET_SAME = 70114;
   /**
    * Target cannot be overwrite according to "Overwrite" header of the request
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>target path</TD></TR>
    * </TABLE>
    */
   public static final int METHOD_FAIL_CANNOT_OVERWRITE = 70115;
   
   /**
    * Cannot find a field in an item
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the name of the field</TD></TR>
    * <TR><TD>1</TD><TD>the content-id of the item</TD></TR>
    * <TR><TD>2</TD><TD>the Rhythmyx content type id</TD></TR>
    * </TABLE>
    */
   public static final int ITEMFIELD_NOT_EXIST = 70116;
   
   /**
    * A requested lock scope is not allowed
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the requested lock scope</TD></TR>
    * <TR><TD>1</TD><TD>the supported lock scope</TD></TR>
    * </TABLE>
    */
   public static final int LOCKSCOPE_NOT_ALLOWED = 70117;

   /**
    * A requested lock type is not allowed
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the requested lock type</TD></TR>
    * <TR><TD>1</TD><TD>the supported lock type</TD></TR>
    * </TABLE>
    */
   public static final int LOCKTYPE_NOT_ALLOWED = 70118;
   
   /**
   * Fieldname element cannot be empty or missing
   * <p>
   * The arguments passed in for this message are:
   * <TABLE BORDER="1">
   * <TR><TH>Arg</TH><TH>Description</TH></TR>
   * <TR><TD>0</TD><TD>property name</TD></TR>
   * </TABLE>
   */
   public static final int FIELDNAME_CANNOT_BE_EMPTY_OR_MISSING = 70119;   
   
   /**
   * A content-type id does not exist in the WebDAV configuration
   * <p>
   * The arguments passed in for this message are:
   * <TABLE BORDER="1">
   * <TR><TH>Arg</TH><TH>Description</TH></TR>
   * <TR><TD>0</TD><TD>content-type id</TD></TR>
   * <TR><TD>1</TD><TD>sys_title</TD></TR>
   * <TR><TD>2</TD><TD>content-id</TD></TR>
   * <TR><TD>3</TD><TD>revision</TD></TR>
   * </TABLE>
   */
   public static final int CONTENTTYPE_NOT_CONFIGURED = 70120;   

   /**
    * Received unknown body content from a MKCOL request
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The unknown body content</TD></TR>
    * </TABLE>
    */
    public static final int UNKNOWN_BODY_IN_MKCOL_REQ = 70121;   

    /**
     * The URL of a header cannot be recognized.
     * <p>
     * The arguments passed in for this message are:
     * <TABLE BORDER="1">
     * <TR><TH>Arg</TH><TH>Description</TH></TR>
     * <TR><TD>0</TD><TD>The unknown URL</TD></TR>
     * <TR><TD>1</TD><TD>The header name</TD></TR>
     * </TABLE>
     */
     public static final int UNKNOWN_URL_FROM_HEADER = 70122;   

     /**
      * The URL of a header is malformed.
      * <p>
      * The arguments passed in for this message are:
      * <TABLE BORDER="1">
      * <TR><TH>Arg</TH><TH>Description</TH></TR>
      * <TR><TD>0</TD><TD>The malformed URL</TD></TR>
      * <TR><TD>1</TD><TD>The header name</TD></TR>
      * </TABLE>
      */
      public static final int MALFORMED_URL_FROM_HEADER = 70123;   
     
      /**
       * Cannot locate a transition from Public to Quick-Edit state
       */
      public static final int  NO_PUBLIC_AUTO_TRANSITION = 70124;

      /**
       * Cannot locate a transition from Quick-Edit to public state
       */
      public static final int NO_QE_AUTO_TRANSITION =70125;
}
