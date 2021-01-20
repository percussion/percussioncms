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
package com.percussion.cms;

/**
 * This inteface is provided as a convenient mechanism for accessing the
 * various CMS system related error codes. Errors are in the range
 * 13001 - 14000. Within this range, errors are further broken down as follows:
 * <TABLE BORDER="1">
 * <TR><TH>Range</TH><TH>Component</TH></TR>
 * <TR><TD>1601 - 1700</TD><TD>This range is actually defined in the
 *    IPSServerErrors, but noted here. These codes should be moved here at
 *    some point.</TD></TR>
 * <TR><TD>13001 - 13100</TD><TD>general errors used all over</TD></TR>
 * <TR><TD>13101 - 13200</TD><TD>CMS Objectstore</TD></TR>
 * <TR><TD>13201 - 13250</TD><TD>Server side OS handlers</TD></TR>
 * <TR><TD>13251 - 14000</TD><TD>-unassigned-</TD></TR>
 * </TABLE>
 */
public interface IPSCmsErrors
{
   /**
    * Uses an existing message. Used to translate a SQLException to one of ours.
    * Assumed that the message was formatted w/
    * PSSqlException.getFormattedExceptionText().
    */
   public static final int SQL_EXCEPTION_WRAPPER = 1002;

   /**
    * While performing an operation on the system tables in the database, an
    * unexpected condition was found. This might be duplicate rows (because a
    * primary key was not properly defined), data that doesn't have the proper
    * format, etc.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the CMS component that was being processed
    *    when the error occurred, or the table name itself.</TD></TR>
    * <TR><TD>1</TD><TD>The primary key (seperated by commas if a multi-part
    *    key), if known.</TD></TR>
    * <TR><TD>2</TD><TD>A detail message about the problem, if known.</TD></TR>
    * </TABLE>
    */
   public static final int CORRUPT_DATABASE_ENTRY  = 13001;

   /**
    * An operation that required a valid content type id was supplied with
    * an invalid one. A valid content type is one that has a running handler.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the content type that was passed. May be
    *    the string based name or the numeric id.</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_CONTENT_TYPE_ID = 13002;

   /**
    * Caught an exception while send message to server
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the exception</TD></TR>
    * </TABLE>
    */
   public static final int ERROR_SEND_DATA = 13003;

   /**
    * Received un-expected data after sending message to server
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the expected node</TD></TR>
    * <TR><TD>1</TD><TD>the received XML</TD></TR>
    * </TABLE>
    */
   public static final int RECEIVED_UNKNOWN_DATA = 13004;

   /**
    * Folder operation failed
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>folder operation</TD></TR>
    * </TABLE>
    */
   public static final int FOLDER_OPERATION_FAILED = 13005;

   /**
    * Error message from a folder operation failure
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the error message</TD></TR>
    * </TABLE>
    */
   public static final int FOLDER_ERROR_MSG = 13006;

   /**
    * The requested folder operation failed because of insufficient privileges.
    * <p>
    * No arguments required for this message.
    */
   public static final int FOLDER_PERMISSION_DENIED = 13007;

   /**
    * Insufficient privileges to create the folder. The user must have read
    * access for the folder to be created successfully.
    * <p>
    * No arguments required for this message.
    */
   public static final int FOLDER_CREATE_ERROR = 13008;

   /**
    * Site lookup for given site id failed.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Name of the Rhythmyx intenrnal request resource to perform the site lookup</TD></TR>
    * <TR><TD>1</TD><TD>SiteId of the site to lookup</TD></TR>
    * </TABLE>
    */
   public static final int SITE_LOOKUP_FAILED = 13009;
   
   /**
    * Failed to clone (or create new copy) a Content Item since the Content 
    * Type of the Item is not visible by the Community
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the id of the Content Item</TD></TR>
    * <TR><TD>1</TD><TD>the revision of the Content Item</TD></TR>
    * <TR><TD>2</TD><TD>the id of the Content Type</TD></TR>
    * <TR><TD>3</TD><TD>the id of the Community</TD></TR>
    * </TABLE>
    * 
    */
   public static final int CONTENT_TYPE_NOT_VISIBLE_BY_COMMUNITY = 13010;
   
   /**
    * Invalid content type supplied. To be valid a handler must be running.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The content type name or id.</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_CONTENT_TYPE = 13102;

   /**
    * An operation attempted to open a content type and it could not
    * be opened or acquired.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the content type that was passed. May be
    *    the string based name or the numeric id.</TD></TR>
    * <TR><TD>1</TD><TD>The description of the possible problem.  If
    *    known.</TD></TR>
    * </TABLE>
    */
   public static final int CONTENT_TYPE_CANNOT_BE_OPENED = 13103;

   /**
    * An operation attempted to locate a  content item and it could not
    * be opened or acquired.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The contentId of the item to be located.</TD></TR>
    * <TR><TD>1</TD><TD>The revisionId of the item to be located.</TD></TR>
    * </TABLE>
    */
   public static final int CONTENT_ITEM_CANNOT_BE_LOCATED = 13104;

   /**
    * An operation attempted to locate a extract the datapipe from a
    * content editor and it returned null.
    * <p>
    */
   public static final int DATA_EXTRACTION_ERROR_NULL_DATAPIPE = 13106;

   /**
    * An error occurred when trying to convert a IPSFieldValue object to its
    * <code>String</code> representation.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The description of the possible problem.  If
    *    known.</TD></TR>
    * </TABLE>
    */
   public static final int PSFIELDVALUE_TO_STRING_ERROR = 13105;

   /**
    * A necessary XML document cannot be found.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Submitted location</TD></TR>
    * </TABLE>
    */
   public static final int REQUIRED_DOCUMENT_MISSING_ERROR = 13107;

   /**
    * An error occurred while parsing a XML Document.  Node type is unknown.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Brief information, Node name if possible.</TD></TR>
    * </TABLE>
    */
   public static final int MALFORMED_XML_DOCUMENT_UKNOWN_NODE_TYPE = 13108;

   /**
    * An error occurred while making an internal request in the CMS layer.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Location url or app/source.</TD></TR>
    * <TR><TD>1</TD><TD>The text of the source exception.</TD></TR>
    * </TABLE>
    */
   public static final int CMS_INTERNAL_REQUEST_ERROR = 13109;

   /**
    * Object does not have the same type of PSKey.
    *
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The expected key parts / definitions</TD></TR>
    * <TR><TD>1</TD><TD>The incountered key parts / definitions</TD></TR>
    * </TABLE>
    */
   public static final int KEY_PARTS_NOT_MATCH = 13110;

   /**
    * An operation on a key required that it already have a value, but it
    * didn't.
    */
   public static final int KEY_NOT_ASSIGNED = 13111;

   /**
    * Component type not supported by this processor.
    *
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The value returned by IPSDbComponent.getComponentType()
    *    method.</TD></TR>
    * <TR><TD>1</TD><TD>The base class name of the processor.</TD></TR>
    * </TABLE>
    */
   public static final int UNSUPPORTED_COMPONENT_TYPE = 13112;

   /**
    * A required property was missing from the processor config file.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the required property</TD></TR>
    * <TR><TD>1</TD><TD>The value returned by IPSDbComponent.getComponentType()
    *    method.</TD></TR>
    * </TABLE>
    */
   public static final int MISSING_PROPERTY = 13113;

   /**
    * The supplied document was of the wrong type.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>1</TD><TD>The value returned by IPSDbComponent.getComponentType()
    *    method.</TD></TR>
    * <TR><TD>2</TD><TD>Expected root node name</TD></TR>
    * <TR><TD>3</TD><TD>Actual root node name</TD></TR>
    * </TABLE>
    */
   public static final int SERIALIZED_COMPONENTS_WRONG_XML_DOC = 13114;

   /**
    * An xml document with no children was supplied, but children were
    * expected.
    */
   public static final int EMPTY_XML_DOCUMENT = 13115;

   /**
    * A required Rhythmyx resource could not be found. (Maybe it's not running?)
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the resource, in form app/resource</TD></TR>
    * </TABLE>
    */
   public static final int REQUIRED_RESOURCE_MISSING = 13116;

   /**
    * The configuration file for the CMS objectstore processors can't be found.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the config file.</TD></TR>
    * </TABLE>
    */
   public static final int PROCESSOR_CONFIG_MISSING = 13117;

   /**
    * Wraps a SAXException.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the xml document. If there isn't one,
    *    supply 'unknown'.</TD></TR>
    * <TR><TD>1</TD><TD>The text from the SAX exception.</TD></TR>
    * <TR><TD>2</TD><TD>The text from the exception contained within the
    *    SAX exception. If there isn't one, supply "none".</TD></TR>
    * </TABLE>
    */
   public static final int XML_PARSING_ERROR = 13118;

   /**
    * Multiple properties found for a processor under a single component type
    * in the processor config file.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the processor.</TD></TR>
    * <TR><TD>1</TD><TD>The name of the component type.</TD></TR>
    * <TR><TD>2</TD><TD>The name of the duplicated property.</TD></TR>
    * </TABLE>
    */
   public static final int DUPLICATE_PROCESSOR_PROPERTY = 13119;

   /**
    * Duplicate processors found under the same component type in the
    * processor config file.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the processor.</TD></TR>
    * <TR><TD>1</TD><TD>The name of the component type.</TD></TR>
    * </TABLE>
    */
   public static final int DUPLICATE_PROCESSOR_ENTRY = 13120;

   /**
    * There is no processor defined for the specified component type. One must
    * be defined in the processor configuration file.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The component type.</TD></TR>
    * <TR><TD>1</TD><TD>The type of the processor.</TD></TR>
    * </TABLE>
    */
   public static final int NO_PROCESSOR_ENTRY = 13121;

   /**
    * While instantiating a processor from a class name specified in a config
    * file, the required ctor was missing.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The fully qualified name of the class that is
    *    missing the desired ctor.</TD></TR>
    * <TR><TD>1</TD><TD>The number of parameters required for the method.</TD>
    *    </TR>
    * <TR><TD>2</TD><TD>The first parameter class name. If there is a second
    *    param, this guy should include a trailing comma.</TD></TR>
    * <TR><TD>3</TD><TD>The 2nd param class name, if there is one.</TD></TR>
    * </TABLE>
    */
   public static final int PROCESSOR_NO_SUCH_METHOD = 13122;

   /**
    * Instantiated class for objectststore processor must inherit from
    * PSCmsProcessor.
    */
   public static final int PROCESSOR_BAD_HERITAGE = 13123;

   /**
    * While instantiating a processor using reflection from a class name
    * specified in a config file, one of several possible exceptions occurred.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The fully qualified class name that caused the
    *    failure.</TD></TR>
    * <TR><TD>1</TD><TD>The type of the component for which the processor
    *    was being instantiated.</TD></TR>
    * <TR><TD>2</TD><TD>The text of the exception.</TD></TR>
    * </TABLE>
    */
   public static final int PROCESSOR_INSTANTIATION_ERROR = 13124;

   /**
    * While instantiating a component using reflection during re-serialization
    * of a db component, one of serveral possible exceptions occurred.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The fully qualified class name that caused the
    *    failure.</TD></TR>
    * <TR><TD>1</TD><TD>The component type (getComponentType()) that fromXml
    *    was called on.</TD></TR>
    * <TR><TD>2</TD><TD>The text of the exception.</TD></TR>
    * </TABLE>
    */
   public static final int COMPONENT_INSTANTIATION_ERROR = 13125;

   /**
    * During key assignment, the number of values returned to assign did not
    * match the number of parts in the key.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The component type as returned by getComponentType()
    *    that was performing the processing.</TD></TR>
    * </TABLE>
    */
   public static final int KEY_MISMATCH = 13126;

   /**
    * During key assignment, a parent key was supplied, but after assigning the
    * values to the available parts, there was no part left for the foreign
    * key assignment.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The component type as returned by getComponentType()
    *    that was performing the processing.</TD></TR>
    * </TABLE>
    */
   public static final int TOO_MANY_FOREIGN_KEY_PARTS = 13127;

   /**
    * During key assignment, a parent key was supplied, but there weren't
    * enough matching parts in the foreign key to fill the remaining parts of
    * the component key.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The component type as returned by getComponentType()
    *    that was performing the processing.</TD></TR>
    * </TABLE>
    */
   public static final int TOO_FEW_FOREIGN_KEY_PARTS = 13128;

   /**
    * While de-serializing a list/collection of objects, while attempting to
    * instantiate new ones using reflection, one of several possible exceptions
    * occurred.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The fully qualified class name that caused the
    *    failure.</TD></TR>
    * <TR><TD>1</TD><TD>The type of the list, as returned by the
    *    getComponentType() method.</TD></TR>
    * <TR><TD>2</TD><TD>The text of the exception.</TD></TR>
    * </TABLE>
    */
   public static final int LIST_ENTRY_INSTANTIATION_ERROR = 13129;

   /**
    * While de-serializing a PSDbComponentList of objects, the entry node name
    * found in the serialized stream didn't start with PSX and no className
    * attribute was present.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The node name.</TD></TR>
    * <TR><TD>1</TD><TD>The type of the list, as returned by the
    *    getComponentType() method.</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_ENTRY_CLASSNAME = 13130;

   /**
    * A lookup key for id allocation was not provided.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The component name as provided by getComponentType().
    *    </TD></TR>
    * </TABLE>
    */
   public static final int MISSING_LOOKUP_KEY = 13131;

   /**
    * While restoring a serialized object, a validation failed. A property
    * used as part of the key did not match the re-serialized key.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The component name as provided by getComponentType().
    *    </TD></TR>
    * <TR><TD>1</TD><TD>The data in the component.</TD></TR>
    * <TR><TD>2</TD><TD>The data in the key.</TD></TR>
    * </TABLE>
    */
   public static final int MISMATCH_BETWEEN_KEY_AND_DATA = 13132;

   /**
    * A wrapper for an IOException while communicating w/ the server. The
    * context should give an idea of what was being done when the exception
    * happened. For example, what was the request url.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Context - what were you doing when it happened.
    *    </TD></TR>
    * <TR><TD>1</TD><TD>The text of the exception.</TD></TR>
    * </TABLE>
    */
   public static final int COMM_ERROR_WITH_SERVER = 13133;

   /**
    * A wrapper for a SAXxception. The context should give an idea of what
    * was being done when the exception happened. For example, if processing
    * a document returned by the server, what was the request url.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Context - what were you doing when it happened.
    *    </TD></TR>
    * <TR><TD>1</TD><TD>The text of the exception.</TD></TR>
    * </TABLE>
    */
   public static final int SAX_PROCESSING_EXCEPTION = 13134;

   /**
    * An exit required a parameter that was not supplied or didn't have a
    * valid value.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Parameter name</TD></TR>
    * <TR><TD>1</TD><TD>The supplied value</TD></TR>
    * </TABLE>
    */
   public static final int MISSING_HTML_PARAMETER = 13135;

   /**
    * Failed to get component summaries, may caused by invalid content ids
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Parameter name</TD></TR>
    * <TR><TD>1</TD><TD>A list of content ids</TD></TR>
    * </TABLE>
    */
   public static final int FAIL_GET_COMPONENT_SUMMARIES = 13136;

   /**
    * Failed get parent folder, may caused by invalid child id
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Parameter name</TD></TR>
    * <TR><TD>1</TD><TD>content id of the child item</TD></TR>
    * </TABLE>
    */
   public static final int FAIL_GET_PARENT_FOLDER = 13137;

   /**
    * Failed open folder with a specified folder id
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Parameter name</TD></TR>
    * <TR><TD>1</TD><TD>folder id</TD></TR>
    * </TABLE>
    */
   public static final int FAIL_OPEN_FOLDER = 13138;

   /**
    * Invalid folder id
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>id</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_FOLDER_ID = 13139;
   
   /**
    * No content type definition found for the specified source object.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>source object id</TD></TR>
    * </TABLE>
    */
   public static final int CONTENTTYPE_DEFINITION_NOT_FOUND = 13140;
   
  /**
   * The creation of circular folder references is not allowed.
   * <p>
   * <TABLE BORDER="1">
   * <TR><TH>Arg</TH><TH>Description</TH></TR>
   * <TR><TD>0</TD><TD>The name of the target folder.</TD></TR>
   * <TR><TD>1</TD><TD>A list of id's with possible violators.</TD></TR>
   * </TABLE>
   */
   public static final int CIRCULAR_FOLDER_REFERENCE = 13141;
  
   /**
    * Cannot move a folder to its descendent folder.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the name of the moved folder</TD></TR>
    * <TR><TD>1</TD><TD>the id of the moved folder</TD></TR>
    * <TR><TD>2</TD><TD>the name of the target parent</TD></TR>
    * <TR><TD>3</TD><TD>the id target parent</TD></TR>
    * </TABLE>
    */
   public static final int CANNOT_MOVE_FOLDER_TO_ITS_DESCENDENT = 13142;
   
   /**
    * An unknown related type was supplied for a related item. Related types
    * must be relationship configurations of category 
    * <code>rs_activeassembly</code>.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The supplied related type.</TD></TR>
    * </TABLE>
    */
   public static final int UNKNOWN_RELATED_TYPE = 13146;
   
   /**
    * An invalid related type was supplied for a related item. Related types
    * must be relationship configurations of category 
    * <code>rs_activeassembly</code>.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The supplied related type.</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_RELATED_TYPE = 13147;
   
   /**
    * Cannot copy a folder to its descendent folder.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the name of the copied folder</TD></TR>
    * <TR><TD>1</TD><TD>the id of the copied folder</TD></TR>
    * <TR><TD>2</TD><TD>the name of the target parent</TD></TR>
    * <TR><TD>3</TD><TD>the id target parent</TD></TR>
    * </TABLE>
    */
   public static final int CANNOT_COPY_FOLDER_TO_ITS_DESCENDENT = 13143;
   
   /**
    * Moving an item from a folder is not allowed, when the source folder id 
    * is referenced by a cross site link (AA relationship).
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the moved item.</TD></TR>
    * <TR><TD>1</TD><TD>The content id of the moved item.</TD></TR>
    * <TR><TD>2</TD><TD>The name of the source folder.</TD></TR>
    * <TR><TD>3</TD><TD>The name of the target folder.</TD></TR>
    * <TR><TD>4</TD><TD>The folder id (or sys_folderid) property of the cross site link relationship.</TD></TR>
    * <TR><TD>5</TD><TD>The content id of the owner item of the cross site link relationship.</TD></TR>
    * </TABLE>
    */
    public static final int FOLDERID_REF_BY_CROSS_SITE_LINK = 13144;
   
    /**
     * Moving an item from a folder is not allowed, when the target folder  
     * is not under the site that is referenced by a cross site link 
     * (AA relationship).
     * <p>
     * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the moved item.</TD></TR>
    * <TR><TD>1</TD><TD>The content id of the moved item.</TD></TR>
    * <TR><TD>2</TD><TD>The name of the source folder.</TD></TR>
    * <TR><TD>3</TD><TD>The name of the target folder.</TD></TR>
    * <TR><TD>4</TD><TD>The site id (or sys_siteid) property of the cross site link relationship.</TD></TR>
    * <TR><TD>5</TD><TD>The content id of the owner item of the cross site link relationship.</TD></TR>
     * </TABLE>
     */
     public static final int SITEID_REF_BY_CROSS_SITE_LINK = 13145;

   /**
    * An invalid complex child field name was specified.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The child name.</TD></TR>
    * <TR><TD>1</TD><TD>The content type name or id.</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_CHILD_TYPE = 13148;
    
   /**
    * A request for an unknown relationship type was received.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The requested relationship type.</TD></TR>
    * </TABLE>
    */
   public static final int UNKNOWN_RELATIONSHIP_TYPE = 13201;

   /**
    * An invalid key type was supplied.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The required key type.</TD></TR>
    * <TR><TD>1</TD><TD>The supplied key type.</TD></TR>
    * </TABLE>
    */
   public static final int UNEXPECTED_KEY_TYPE = 13202;

   /**
    * A persisted key was expected.
    */
   public static final int PERSISTED_KEY_EXPECTED = 13203;

   /**
    * Invalid relatonship type for inserts.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The requested relationship type.</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_INSERT_RELATIONSHIP_TYPE = 13204;

   /**
    * Failed to generate a new relationship id.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The error message.</TD></TR>
    * </TABLE>
    */
   public static final int ID_GENERATOR_FAILED = 13205;

   /**
    * An unexpected error occurred, such as IOException.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The error message.</TD></TR>
    * </TABLE>
    */
   public static final int UNEXPECTED_ERROR = 13206;

   /**
    * Invalid relatonship type for active assembly.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Relationshipid</TD></TR>
    * <TR><TD>1</TD><TD>The requested relationship type.</TD></TR>
    * <TR><TD>2</TD><TD>The expected category.</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_AA_RELATIONSHIP_TYPE = 13207;

   /**
    * Unknown active assembly command.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The supplied command</TD></TR>
    * </TABLE>
    */
   public static final int UNKNOWN_AA_COMMAND = 13208;

   /**
    * Missing required parameter for the active assembly request handler.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The required parameters</TD></TR>
    * </TABLE>
    */
   public static final int MISSING_AA_PARAMETER = 13209;

   /**
    * Error occurred while reading data from the config of processor proxy.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The text of the exception</TD></TR>
    * </TABLE>
    */
   public static final int PROCESSOR_CONFIG_IO_ERROR = 13210;

   /**
    * The 'Default' named transition was selected for promotable versions but
    * none is specified in the workflow.
    *
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The workflowid</TD></TR>
    * <TR><TD>0</TD><TD>The stateid</TD></TR>
    * </TABLE>
    */
   public static final int UNDEFINED_DEFAULT_TRANSITION = 13211;

   /**
    * Failed to add child items to a folder due to non-unique child item 
    * name(s) in the current target or the new supplied child list.
    *
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The parent folder name</TD></TR>
    * <TR><TD>1</TD><TD>
    *    Comma separated list of item system titles whose 
    *    title is already present within the folder to which this item is 
    *    being added
    * </TD></TR>
    * <TR><TD>2</TD><TD>
    *    Comma separated list of system titles of all items which appear two 
    *    or more times in the supplied child list to be cloned and attached 
    *    to the target folder.
    * </TD></TR>
    * </TABLE>
    */
   public static final int DUPLICATE_ITEM_NAME = 13212;

   /**
    * Fail to delete a list of folders due to one of the element is not folder
    * component.
    *
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The non-folder component name</TD></TR>
    * </TABLE>
    */
   public static final int FAIL_DELETE_NON_FOLDER = 13213;

   /**
    * Fail to get component summaries due to unexpected error: {0}
    *
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The un-expected exception</TD></TR>
    * </TABLE>
    */
   public static final int GET_SUMMARIES_ERROR = 13214;

   /**
    * Un-expected error while catalogging from remote server
    *
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The catalog resource</TD></TR>
    * <TR><TD>1</TD><TD>The un-expected exception</TD></TR>
    * </TABLE>
    */
   public static final int UNEXPECTED_CATALOG_ERROR = 13215;

   /**
    * The length of a folder value is too long
    *
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the invalid value</TD></TR>
    * <TR><TD>1</TD><TD>The invalid length</TD></TR>
    * <TR><TD>2</TD><TD>The maximum length</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_FOLDER_VALUE = 13216;


   /**
    * An invalid relationship property value was supplied
    *
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The owner id</TD></TR>
    * <TR><TD>1</TD><TD>The dependent id</TD></TR>
    * <TR><TD>2</TD><TD>The name of the invalid property</TD></TR>
    * <TR><TD>3</TD><TD>The invalid value</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_RELATIONSHIP_PROP_VALUE = 13217;
   
   /**
    * The clone handler did not create the originating relationship.
    */
   public static final int NO_ORIGINATING_RELATIONSHIP = 13218;
   
   /**
    * An validation error occurs while processing a request
    *
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The resource path of the request</TD></TR>
    * <TR><TD>1</TD><TD>The validation error message</TD></TR>
    * </TABLE>
    */
   public static final int VALIDATION_ERROR = 13219;
   
   /**
    * An error occurred while using search web service.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The message from the exception caught, if any.</TD></TR>
    * </TABLE>
    */
   public static final int SEARCH_ERROR = 13220;
   
   /**
    * An error occurred while validating unique child names under a given folder
    * in the modify handler
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The child content id</TD></TR>
    * <TR><TD>1</TD><TD>The child revision</TD></TR>
    * <TR><TD>2</TD><TD>The child name</TD></TR>
    * <TR><TD>3</TD><TD>The parent folder name</TD></TR>
    * </TABLE>
    */
   public static final int MODIFY_ERROR_DUPLICATED_CHILDNAME = 13221;
   
   /**
    * An error occurred while validating unique child names under a given folder
    * in folder relationship effect
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The child name</TD></TR>
    * <TR><TD>1</TD><TD>The parent folder name</TD></TR>
    * <TR><TD>2</TD><TD>The child content id</TD></TR>
    * <TR><TD>3</TD><TD>The child revision</TD></TR>
    * </TABLE>
    */
   public static final int FOLDER_REL_ERROR_DUPLICATED_CHILDNAME = 13222;
   
   /**
    * Failed to get the component summary from a given locator
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The locator id</TD></TR>
    * <TR><TD>1</TD><TD>The locator revision</TD></TR>
    * </TABLE>
    */
   public static final int FAILED_GET_SUMMARY = 13223;

   /**
    * Failed to add child items to a folder due to non-unique child item name(s)
    * but new copies were created.
    *
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Original message</TD></TR>
    * </TABLE>
    */
   public static final int DUPLICATE_ITEM_NAME_COPY_CREATED = 13224;

   /**
    * Error during execution of an iternal server request to check if a given
    * set of relationships by id exist in the system.
    * 
    * <p>
    * <TABLE BORDER="1">
    * <TR>
    * <TH>Arg</TH>
    * <TH>Description</TH>
    * </TR>
    * <TR>
    * <TD>0</TD>
    * <TD>Resource name for the internal request</TD>
    * </TR>
    * <TR>
    * <TD>1</TD>
    * <TD>The error message from the exception</TD>
    * </TR>
    * </TABLE>
    */
   public static final int RELATIONSHIP_EXISTENCE_CHECK_FAILED = 13225;
   
   /**
    * Error message shown if a user tries to create a relationship from a 
    * non-existing owner item.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The owner id</TD></TR>
    * </TABLE>
    */
   public static final int NON_EXITING_OWNER = 13226;
   
   /**
    * Error message shown if a user tries to create a relationship to a 
    * non-existing dependent item.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The dependent id</TD></TR>
    * </TABLE>
    */
   public static final int NON_EXITING_DEPENDENT = 13227;

   /**
    * An error occurred while validating unique child names under a given folder
    * in folder relationship effect for a child that has not yet been saved
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The child name</TD></TR>
    * <TR><TD>1</TD><TD>The parent folder name</TD></TR>
    * </TABLE>
    */
   public static final int FOLDER_REL_INSERT_ERROR_DUPLICATED_CHILDNAME = 13228;

   /**
    * The requested authtype has not been configured in the authtype configuration file.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Authtype value</TD></TR>
    * <TR><TD>1</TD><TD>Authtype configuration file path</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_AUTHTYPE = 13229;

   /**
    * The relationship is invalid because it has an invalid variantid.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Relationshipid</TD></TR>
    * <TR><TD>1</TD><TD>Variantid</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_AA_RELATIONSHIP = 13230;
   
   /**
    * The variant lookup failed because the variant was not registered.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Variantid</TD></TR>
    * </TABLE>
    */
   public static final int VARIANT_LOOKUP_FAILED = 13231;

   /**
    * Invalid Active Assembly relationship since the slot does not allow the 
    * variant.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Relationshipid</TD></TR>
    * <TR><TD>1</TD><TD>Slotid</TD></TR>
    * <TR><TD>2</TD><TD>Variantid</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_AA_RELATIONSHIP_SLOT_VARIANT = 13232;
   
   /**
    * The context for AA processor proxy must be either PSRequest or 
    * IPSRequestContext
    * <p>
    * No argument is required for this message.
    */
   public static final int INVALID_CONTEXT_FOR_AA_PROXY = 13233;

   /**
    * An attempt is made to assemble a snippet which was already assembled in 
    * the same parent context within the assembly tree.
    * 
    * <p>
    * <TABLE BORDER="1">
    * <TR>
    * <TH>Arg</TH>
    * <TH>Description</TH>
    * </TR>
    * <TR>
    * <TD>0</TD>
    * <TD>A semi colon separated list of names parameters that make the key for the assembly snippet being assembled</TD>
    * </TR>
    * <TR>
    * <TD>1</TD>
    * <TD>A semi colon separated list of values that correspond to the parameters in the first argument</TD>
    * </TR>
    * </TABLE>
    */
   public static final int ERROR_RECURSIVEASSEMBLY = 13234;
   
   /**
    * An attempt was made to create or save a folder with an invalid name.
    * <p>
    * <TABLE BORDER="1">
    * <TR>
    * <TH>Arg</TH>
    * <TH>Description</TH>
    * </TR>
    * <TR>
    * <TD>0</TD>
    * <TD>The invalid name</TD>
    * </TR>
    * </TABLE>
    */
   public static final int INVALID_FOLDER_NAME = 13235;
   
   /**
    * Failed to create a relationship configuration set from its XML 
    * representation.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>error occurred</TD></TR>
    * </TABLE>
    */
   public static final int FAILED_GET_REL_CONFIG_FROM_XML = 13236;
   
   /**
    * The id of a system relationship config does not match the pre-defined 
    * one with the same name.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>name of the system relationship config</TD></TR>
    * <TR><TD>1</TD><TD>the right id of the system relationship config</TD></TR>
    * <TR><TD>2</TD><TD>the wrong id of the system relationship config</TD></TR>
    * </TABLE>
    */
   public static final int UNKOWN_SYS_REL_CONFIG_ID = 13238;

   /**
    * Failed to delete a (user) relationship config name. This relationship 
    * config may still be referenced by relationship instances.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the name of the deleted relationship config</TD></TR>
    * <TR><TD>1</TD><TD>the error occurred during the delete process</TD></TR>
    * </TABLE>
    */
   public static final int FAILED_DELETE_REL_CONFIG_NAME = 13240;

   /**
    * The id ''{0}'' of a relationship config ''{1}'' has already 
    * been used by other relationship config.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>id of the relationship config in question</TD></TR>
    * <TR><TD>1</TD><TD>name of the relationship config in question</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_REL_CONFIG_ID = 13241;

   /**
    * The name ''{0}'' of a relationship config has already 
    * been used by other relationship config.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>name of the relationship config in question</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_REL_CONFIG_NAME = 13242;
   
   /**
    * Loading sites from server failed with some message
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Error message from server</TD></TR>
    * </TABLE>
    */
   public static final int ERROR_LOADING_SITES = 13243;

   /**
    * Attempted to move items that are checked out to someone else from site 
    * folders.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>list of item content ids</TD></TR>
    * <TR><TD>1</TD><TD>Action name (move, remove)</TD></TR>
    * </TABLE>
    */
   public static final int CANNOT_MOVE_CHECKEDOUT_ITEMS = 13244;

   /**
    * Attempted to move items that are in public state from site folders.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>list of item content ids</TD></TR>
    * <TR><TD>1</TD><TD>Action name (move, remove)</TD></TR>
    * </TABLE>
    */
   public static final int CANNOT_MOVE_PUBLIC_ITEMS = 13245;

   /**
    * Attempted to move or remove items that participate in cross site linking 
    * and force action is required.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>list of item content ids</TD></TR>
    * <TR><TD>1</TD><TD>Action name (move, remove)</TD></TR>
    * </TABLE>
    */
   public static final int FORCE_MOVE_REMOVE_REQUIED = 13246;

   /**
    * Saving relationships to server failed with some message
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Error message from server</TD></TR>
    * </TABLE>
    */
   public static final int ERROR_SAVING_RELATIONSHIPS = 13247;

   /**
    * Failed to load the relationship with specified relationship id.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>relationship id</TD></TR>
    * </TABLE>
    */
   public static final int LOAD_AA_RELATIONSHIP_FAILED = 13248;

   /**
    * Folder action failed as well as cross links' save failed.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>action failure message</TD></TR>
    * <TR><TD>1</TD><TD>Save of partial relationships failure message</TD></TR>
    * </TABLE>
    */
   public static final int CROSSSITE_LINK_PROCESS_MULTI_ERROR = 13249;
   
   /**
    * Failed to get navon nodes, due to encounter an AA circular relationship.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the owner id of the bad relationship</TD></TR>
    * <TR><TD>1</TD><TD>XML representation of the bad relationship</TD></TR>
    * </TABLE>
    */
   public static final int FAILED_GET_NAVON_CIRCULAR_AA_RELATIONSHIP = 13250;
}
