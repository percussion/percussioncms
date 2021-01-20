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

package com.percussion.design.objectstore;


/**
 * The IPSObjectStoreErrors inteface is provided as a convenient mechanism
 * for accessing the various object store related error codes. Object
 * Store errors are in the range 2001 - 3000. Within this range, errors are
 * further broken down as follows:
 * <TABLE BORDER="1">
 * <TR><TH>Range</TH><TH>Component</TH></TR>
 * <TR><TD>2001 - 2100</TD><TD>general errors used all over</TD></TR>
 * <TR><TD>2101 - 2200</TD><TD>PSObjectStore object</TD></TR>
 * <TR><TD>2201 - 2400</TD><TD>object store objects</TD></TR>
 * <TR><TD>2401 - 2500</TD><TD>Content Editor object store objects</TD></TR>
 * <TR><TD>2501 - 2800</TD><TD>-unassigned-</TD></TR>
 * <TR><TD>2801 - 3000</TD><TD>object store server handlers</TD></TR>
 * </TABLE>
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public interface IPSObjectStoreErrors {

   /**
    * null specified as the Element in fromXml.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the expected XML Element tag</TD></TR>
    * </TABLE>
    */
   public static final int XML_ELEMENT_NULL        = 2011;

   /**
    * the specified element in fromXml is not of the expected type.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the XML Element tag expected</TD></TR>
    * <TR><TD>1</TD><TD>the XML Element tag encountered</TD></TR>
    * </TABLE>
    */
   public static final int XML_ELEMENT_WRONG_TYPE  = 2012;

   /**
    * the id attribute of the element is missing or invalid
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the XML Element tag</TD></TR>
    * <TR><TD>1</TD><TD>the id specified</TD></TR>
    * </TABLE>
    */
   public static final int XML_ELEMENT_INVALID_ID  = 2013;

   /**
    * a required attribute of the XML element is missing or invalid
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the XML Element tag</TD></TR>
    * <TR><TD>1</TD><TD>the attribute name</TD></TR>
    * <TR><TD>2</TD><TD>the value specified</TD></TR>
    * </TABLE>
    */
   public static final int XML_ELEMENT_INVALID_ATTR   = 2014;

   /**
    * a required child element of the XML element is missing or invalid
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the XML Element tag</TD></TR>
    * <TR><TD>1</TD><TD>the child element name</TD></TR>
    * <TR><TD>2</TD><TD>the value specified</TD></TR>
    * </TABLE>
    */
   public static final int XML_ELEMENT_INVALID_CHILD  = 2015;

   /**
    * the value stored in an XML element was too long.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the name of the object</TD></TR>
    * <TR><TD>1</TD><TD>the expected number of characters</TD></TR>
    * <TR><TD>2</TD><TD>the value used</TD></TR>
    * </TABLE>
    */
   public static final int XML_ELEMENT_VALUE_TOO_BIG  = 2016;

   /**
    * the collection specified contains objects of an inappropriate type.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the human readable collection type</TD></TR>
    * <TR><TD>1</TD><TD>the class expected</TD></TR>
    * <TR><TD>2</TD><TD>the class used</TD></TR>
    * </TABLE>
    */
   public static final int COLL_BAD_CONTENT_TYPE      = 2017;

   /**
    * the collection could not be constructed as the content type's
    * class could not be found
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the desired content class</TD></TR>
    * </TABLE>
    */
   public static final int COLL_CONTENT_TYPE_NOT_FOUND   = 2018;

   /**
    * An object of the given class was asked to validate itself but it does not support
    * validation.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the class</TD></TR>
    * </TABLE>
    */
   public static final int VALIDATION_NOT_IMPLEMENTED = 2019;

   /**
    * The saved version of an application does not correspond to
    * the version that is to be saved.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>application name</TD></TR>
    * <TR><TD>1</TD><TD>incorrect major version</TD></TR>
    * <TR><TD>2</TD><TD>incorrect minor version</TD></TR>
    * <TR><TD>3</TD><TD>saved (correct) minor version</TD></TR>
    * <TR><TD>4</TD><TD>saved (correct) minor version</TD></TR>
    * </TABLE>
    */
   public static final int APP_VERSION_DOES_NOT_MATCH = 2020;

   /**
    * Object does not allow cloning.
    *
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The object type</TD></TR>
    * </TABLE>
    */
   public static final int OBJECT_CLONING_NOT_ALLOWED = 2021;

   /**
    * no log entries could be found using the specified search criteria
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>application name</TD></TR>
    * <TR><TD>1</TD><TD>start time</TD></TR>
    * <TR><TD>2</TD><TD>end time</TD></TR>
    * </TABLE>
    */
   public static final int GET_APP_LOG_NO_DATA     = 2101;

   /**
    * the PSObjectStore constructor was called with null
    * as the PSDesignerConnection object
    * <p>
    * No arguments are passed for this message.
    */
   public static final int CONN_OBJ_NULL           = 2102;


   /**
    * The document returned for a request was invalid or unknown.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the document root name, or null if not found</TD></TR>
    * </TABLE>
    */
   public static final int MALFORMED_RESPONSE_DOCUMENT = 2103;
   
   /**
    * the PSPipe name was set to null or empty
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int PIPE_NAME_EMPTY            = 2200;

   /**
    * null specified in call to PSAcl.setEntries.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int ACL_ENTRYLIST_NULL      = 2201;

   /**
    * empty collection specified in call to PSAcl.setEntries.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int ACL_ENTRYLIST_EMPTY     = 2202;

   /**
    * PSApplication.setAcl was called with no full design access ACL entries.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int APP_ACL_NO_MANAGER      = 2203;

   /**
    * the collection specified in call to PSAcl.setEntries
    * contains duplicate PSAclEntry objects.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>entry name</TD></TR>
    * <TR><TD>1</TD><TD>entry security provider instance</TD></TR>
    * </TABLE>
    */
   public static final int ACL_ENTRYLIST_DUPLICATE = 2204;

   /**
    * null or empty name specified in call to PSAclEntry.setName.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int ACL_ENTRY_NAME_EMPTY    = 2205;

   /**
    * the provider specified in a call to
    * PSAclEntry.setSecurityProviderType is invalid.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the type</TD></TR>
    * </TABLE>
    */
   public static final int ACL_ENTRY_SP_INVALID    = 2206;

   /**
    * the provider instance name is too long in a call to
    * PSAclEntry.setSecurityProviderInstance
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>max length permitted</TD></TR>
    * <TR><TD>1</TD><TD>length specified</TD></TR>
    * </TABLE>
    */
   public static final int ACL_ENTRY_SPINST_TOO_BIG   = 2207;

   /**
    * the PSXAclEntry object must contain a serverAccessLevel or
    * applicationAccessLevel entry.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int ACL_ENTRY_LEVEL_NOT_FOUND  = 2208;

   /**
    * the PSApplication object name was empty.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int APP_NAME_EMPTY             = 2209;

   /**
    * the PSApplication object name was too long.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>max length permitted</TD></TR>
    * <TR><TD>1</TD><TD>length specified</TD></TR>
    * </TABLE>
    */
   public static final int APP_NAME_TOO_BIG           = 2210;

   /**
    * the PSApplication object description was too long.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>max length permitted</TD></TR>
    * <TR><TD>1</TD><TD>length specified</TD></TR>
    * </TABLE>
    */
   public static final int APP_DESC_TOO_BIG           = 2211;

   /**
    * the PSApplication object request root was too long.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>max length permitted</TD></TR>
    * <TR><TD>1</TD><TD>length specified</TD></TR>
    * </TABLE>
    */
   public static final int APP_ROOT_TOO_BIG           = 2212;

   /**
    * PSApplication.setAcl was called with a null ACL.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int APP_ACL_NULL               = 2213;

   /**
    * PSApplication.setAcl was called with no ACL entries.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int APP_ACL_EMPTY              = 2214;

   /**
    * the PSBackEndCredential object alias name was null or empty.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int BE_CRED_ALIAS_NULL         = 2215;

   /**
    * the PSBackEndCredential object alias name was too long.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>max length permitted</TD></TR>
    * <TR><TD>1</TD><TD>length specified</TD></TR>
    * </TABLE>
    */
   public static final int BE_CRED_ALIAS_TOO_BIG      = 2216;

   /**
    * the PSBackEndCredential object comment was too long.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>max length permitted</TD></TR>
    * <TR><TD>1</TD><TD>length specified</TD></TR>
    * </TABLE>
    */
   public static final int BE_CRED_COMMENT_TOO_BIG    = 2217;

   /**
    * the entry name is too long in a call to PSAclEntry.setName
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>max length permitted</TD></TR>
    * <TR><TD>1</TD><TD>length specified</TD></TR>
    * </TABLE>
    */
   public static final int ACL_ENTRY_NAME_TOO_BIG     = 2218;

   /**
    * the value passed to PSApplication.setRequestTypeHtmlParamName
    * is too large
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>max length permitted</TD></TR>
    * <TR><TD>1</TD><TD>length specified</TD></TR>
    * </TABLE>
    */
   public static final int APP_REQ_PARAM_HTML_TOO_BIG = 2219;

   /**
    * the value passed to PSApplication.setRequestTypeValueXXX
    * is too large
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>max length permitted</TD></TR>
    * <TR><TD>1</TD><TD>length specified</TD></TR>
    * <TR><TD>2</TD><TD>request type</TD></TR>
    * </TABLE>
    */
   public static final int APP_REQ_TYPE_VALUE_TOO_BIG = 2220;

   /**
    * the value passed to PSBackEndColumn.setColumn is null or empty
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int BE_COL_NAME_EMPTY          = 2221;

   /**
    * the value passed to PSBackEndColumn.setColumn is too large
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>max length permitted</TD></TR>
    * <TR><TD>1</TD><TD>length specified</TD></TR>
    * </TABLE>
    */
   public static final int BE_COL_NAME_TOO_BIG        = 2222;

   /**
    * the back-end driver name was null or empty
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int BE_DRIVER_NULL             = 2223;

   /**
    * the back-end driver name was too long
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>max length permitted</TD></TR>
    * <TR><TD>1</TD><TD>length specified</TD></TR>
    * </TABLE>
    */
   public static final int BE_DRIVER_TOO_BIG          = 2224;

   /**
    * the back-end server name was too long
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>max length permitted</TD></TR>
    * <TR><TD>1</TD><TD>length specified</TD></TR>
    * </TABLE>
    */
   public static final int BE_SERVER_TOO_BIG          = 2225;

   /**
    * the back-end userid was too long
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>max length permitted</TD></TR>
    * <TR><TD>1</TD><TD>length specified</TD></TR>
    * </TABLE>
    */
   public static final int BE_UID_TOO_BIG             = 2226;

   /**
    * the back-end password was too long
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>max length permitted</TD></TR>
    * <TR><TD>1</TD><TD>length specified</TD></TR>
    * </TABLE>
    */
   public static final int BE_PASSWORD_TOO_BIG        = 2227;

   /**
    * the right side column of a back-end join was null
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int BE_JOIN_RCOL_NULL          = 2228;

   /**
    * the left side column of a back-end join was null
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int BE_JOIN_LCOL_NULL          = 2229;

   /**
    * the UDF exit must not be null when defining a UDF call
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int UDFCALL_EXIT_NULL          = 2230;

   /**
    * the UDF exit must not be null when defining a UDF call
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the valid class type</TD></TR>
    * <TR><TD>1</TD><TD>the class type of the object specified</TD></TR>
    * </TABLE>
    */
   public static final int JSCRIPT_EXIT_WRONG_TYPE    = 2231;

   /**
    * the back-end table alias name was null or empty
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int BE_TABLE_ALIAS_NULL        = 2232;

   /**
    * the back-end table alias name was too long
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>max length permitted</TD></TR>
    * <TR><TD>1</TD><TD>length specified</TD></TR>
    * </TABLE>
    */
   public static final int BE_TABLE_ALIAS_TOO_BIG     = 2233;

   /**
    * the back-end database name was too long
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>max length permitted</TD></TR>
    * <TR><TD>1</TD><TD>length specified</TD></TR>
    * </TABLE>
    */
   public static final int BE_DB_TOO_BIG              = 2234;

   /**
    * the back-end origin name was too long
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>max length permitted</TD></TR>
    * <TR><TD>1</TD><TD>length specified</TD></TR>
    * </TABLE>
    */
   public static final int BE_ORIGIN_TOO_BIG          = 2235;

   /**
    * the back-end table name was too long
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>max length permitted</TD></TR>
    * <TR><TD>1</TD><TD>length specified</TD></TR>
    * </TABLE>
    */
   public static final int BE_TABLE_TOO_BIG           = 2236;

   /**
    * the PSCustomError error code was null or empty
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int CUSTOM_ERROR_CODE_EMPTY    = 2237;


   /**
    * PSDataEncryptor had encryption enabled but no key strength set
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int DATAENC_KEY_STRENGTH_REQD  = 2238;

   /**
    * the XML field being mapped in the PSDataMapping was null or empty
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int DATAMAPPING_XML_FIELD_EMPTY   = 2239;

   /**
    * the XML field name in the PSDataMapping was too long
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>max length permitted</TD></TR>
    * <TR><TD>1</TD><TD>length specified</TD></TR>
    * </TABLE>
    */
   public static final int DATAMAPPING_XML_FIELD_TOO_BIG = 2240;

   /**
    * the PSBackEndColumn being mapped in the PSDataMapping was null
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int DATAMAPPING_BE_COL_NULL    = 2241;

   /**
    * the PSDataSet name was set to null or empty
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int DATASET_NAME_NULL          = 2242;

   /**
    * the PSDataSet name was too long
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>max length permitted</TD></TR>
    * <TR><TD>1</TD><TD>length specified</TD></TR>
    * </TABLE>
    */
   public static final int DATASET_NAME_TOO_BIG       = 2243;

   /**
    * the PSDataSet description was too long
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>max length permitted</TD></TR>
    * <TR><TD>1</TD><TD>length specified</TD></TR>
    * </TABLE>
    */
   public static final int DATASET_DESC_TOO_BIG       = 2244;

   /**
    * the PSDataSet PSPageDataTank object was set to null
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int DATASET_PAGE_TANK_NULL     = 2245;

   /**
    * the PSDataSet PSRequestor object was set to null
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int DATASET_REQUESTOR_NULL     = 2246;

   /**
    * the PSDataSet PSResultPageSet object was set to null
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int DATASET_RESULT_PAGES_NULL  = 2247;

   /**
    * the PSDataSet PSRequestLink object was set to null
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int DATASET_REQUEST_LINK_NULL  = 2248;

   /**
    * the PSPageDataTank source schema was set to null
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int PAGE_TANK_SCHEMA_NULL      = 2249;

   /**
    * the action type XML field name set in the PSPageDataTank was too long
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>max length permitted</TD></TR>
    * <TR><TD>1</TD><TD>length specified</TD></TR>
    * </TABLE>
    */
   public static final int PAGE_TANK_XML_FIELD_TOO_BIG   = 2250;
   
   /**
    * the PSPipe name was too long
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>max length permitted</TD></TR>
    * <TR><TD>1</TD><TD>length specified</TD></TR>
    * </TABLE>
    */
   public static final int PIPE_NAME_TOO_BIG          = 2252;

   /**
    * the PSPipe description was too long
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>max length permitted</TD></TR>
    * <TR><TD>1</TD><TD>length specified</TD></TR>
    * </TABLE>
    */
   public static final int PIPE_DESC_TOO_BIG          = 2253;

   /**
    * the PSPipe PSBackEndDataTank object was set to null
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int PIPE_BE_TANK_NULL          = 2254;

   /**
    * the PSUpdatePipe PSDataSynchronizer object was set to null
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int UPDATEPIPE_DATA_SYNC_NULL  = 2255;

   /**
    * an invalid action type was specified for the PSSimpleActionExit object
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int SIMPLE_EXIT_INVALID_TYPE   = 2256;

   /**
    * the PSNotifier provider type was set to an invalid value
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the type specified</TD></TR>
    * </TABLE>
    */
   public static final int NOTIFIER_PROVIDER_TYPE_INVALID   = 2257;

   /**
    * the PSNotifier server name was set to null or empty
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int NOTIFIER_SERVER_NULL       = 2258;

   /**
    * the PSNotifier server name was too long
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>max length permitted</TD></TR>
    * <TR><TD>1</TD><TD>length specified</TD></TR>
    * </TABLE>
    */
   public static final int NOTIFIER_SERVER_TOO_BIG    = 2259;

   /**
    * the PSNotifier from name was too long
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>max length permitted</TD></TR>
    * <TR><TD>1</TD><TD>length specified</TD></TR>
    * </TABLE>
    */
   public static final int NOTIFIER_FROM_TOO_BIG      = 2260;

   /**
    * the PSRole name was set to null or empty
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int ROLE_NAME_EMPTY            = 2261;

   /**
    * the PSRole name was too long
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>max length permitted</TD></TR>
    * <TR><TD>1</TD><TD>length specified</TD></TR>
    * </TABLE>
    */
   public static final int ROLE_NAME_TOO_BIG          = 2262;

   /**
    * the UDF exit name was set to null or empty
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int UDFEXIT_NAME_EMPTY         = 2263;

   /**
    * the UDF exit body was set to null or empty
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int UDFEXIT_BODY_EMPTY         = 2264;

   /**
    * the PSRecipient entry name was set to null or empty
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int RECIPIENT_NAME_EMPTY       = 2265;

   /**
    * the PSRecipient entry name was too long
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>max length permitted</TD></TR>
    * <TR><TD>1</TD><TD>length specified</TD></TR>
    * </TABLE>
    */
   public static final int RECIPIENT_NAME_TOO_BIG     = 2266;

   /**
    * the PSRequestor object's page name was set to null or empty
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int REQUESTOR_PAGE_NAME_NULL   = 2267;

   /**
    * the PSRequestLink object's data set name was set to null or empty
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int REQLINK_DATA_SET_NULL      = 2268;

   /**
    * the PSRequestLink object's target XML field name was too long
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>max length permitted</TD></TR>
    * <TR><TD>1</TD><TD>length specified</TD></TR>
    * </TABLE>
    */
   public static final int REQLINK_XML_FIELD_TOO_BIG  = 2269;

   /**
    * the PSBackEndDataTank tables were empty
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int BE_DATATANK_TABLES_EMPTY   = 2270;

   /**
    * the PSBackEndDataTank contains duplicate table names
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>table alias</TD></TR>
    * </TABLE>
    */
   public static final int BE_DATATANK_TABLES_DUP     = 2271;

   /**
    * the IPSExtensionDef class name was set to null or empty
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int JAVA_EXIT_CLASS_NULL       = 2272;

   /**
    * the IPSExtensionDef class name was too long
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>max length permitted</TD></TR>
    * <TR><TD>1</TD><TD>length specified</TD></TR>
    * </TABLE>
    */
   public static final int JAVA_EXIT_CLASS_TOO_BIG    = 2273;

   /**
    * the PSRoleSet security provider type is invalid
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the security provider type</TD></TR>
    * </TABLE>
    */
   public static final int ROLESET_PROVIDER_TYPE_INVALID = 2274;

   /**
    * the PSRoleSet security provider instance name was too long
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>max length permitted</TD></TR>
    * <TR><TD>1</TD><TD>length specified</TD></TR>
    * </TABLE>
    */
   public static final int ROLESET_PROVIDER_INST_TOO_BIG = 2275;

   /**
    * the PSConditional variable name was set to null or empty
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int COND_VAR_NAME_EMPTY        = 2276;

   /**
    * the PSConditional variable name was too long
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>max length permitted</TD></TR>
    * <TR><TD>1</TD><TD>length specified</TD></TR>
    * </TABLE>
    */
   public static final int COND_VAR_NAME_TOO_BIG      = 2277;

   /**
    * the PSConditional operator was unknown (unsupported by E2)
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>operator specified</TD></TR>
    * </TABLE>
    */
   public static final int COND_OPTYPE_UNKNOWN        = 2278;

   /**
    * the PSExtensionParamDef parameter name was set to null or empty
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int EXIT_PARAM_NAME_EMPTY      = 2279;

   /**
    * the PSExtensionParamDef parameter name was too long
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>max length permitted</TD></TR>
    * <TR><TD>1</TD><TD>length specified</TD></TR>
    * </TABLE>
    */
   public static final int EXIT_PARAM_NAME_TOO_BIG    = 2280;

   /**
    * the PSExtensionParamDef parameter data type name was too long
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>max length permitted</TD></TR>
    * <TR><TD>1</TD><TD>length specified</TD></TR>
    * </TABLE>
    */
   public static final int EXIT_PARAM_DT_TOO_BIG      = 2281;

   /**
    * the PSExtensionParamDef parameter description was too long
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>max length permitted</TD></TR>
    * <TR><TD>1</TD><TD>length specified</TD></TR>
    * </TABLE>
    */
   public static final int EXIT_PARAM_DESC_TOO_BIG    = 2282;

   /**
    * the PSSortedColumn's PSBackEndColumn object was set to null
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int SORTEDCOL_COL_NULL         = 2283;

   /**
    * the PSUpdateColumn's PSBackEndColumn object was set to null
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int UPDATECOL_COL_NULL         = 2284;

   /**
    * the PSDataSelector requires a native statement as that selection
    * method is currently set
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int DATASEL_NATIVE_STMT_REQD   = 2285;

   /**
    * the PSDataSelector requires a cache type setting as caching is
    * enabled
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int DATASEL_CACHE_TYPE_REQD    = 2286;

   /**
    * the number of parameters specified for the PSExtensionCall do not match
    * the number of parameters defined in the IPSExtensionDef
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the number of params in the UDF exit def</TD></TR>
    * <TR><TD>1</TD><TD>the number of params specified</TD></TR>
    * </TABLE>
    */
   public static final int UDFCALL_PARAM_COUNT_MISMATCH  = 2287;

   /**
    * the name of the CGI variable may not be null or empty
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int CGI_VAR_NAME_EMPTY            = 2288;

   /**
    * the name of the HTML parameter may not be null or empty
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int HTML_PARAM_NAME_EMPTY         = 2289;

   /**
    * the name of the XML field may not be null or empty
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int XML_FIELD_NAME_EMPTY          = 2290;

   /**
    * the name of the cookie may not be null or empty
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int COOKIE_NAME_EMPTY             = 2291;

   /**
    * The parameter passed to the copyFrom method may not be null.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int INVALID_OBJECT_FOR_COPY       = 2292;

   /**
    * the PSConditional boolean operator was unknown (unsupported by E2)
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>boolean operator specified</TD></TR>
    * </TABLE>
    */
   public static final int COND_BOOL_UNKNOWN             = 2293;

   /**
    * the date specified for a PSDateLiteral was invalid
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the date specified</TD></TR>
    * </TABLE>
    */
   public static final int LITERAL_DATE_INVALID          = 2294;

   /**
    * the date specified for a PSDateLiteral was invalid
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the format specified</TD></TR>
    * </TABLE>
    */
   public static final int LITERAL_DATEFMT_INVALID       = 2295;

   /**
    * the number specified for a PSNumericLiteral was invalid
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the number specified</TD></TR>
    * </TABLE>
    */
   public static final int LITERAL_NUMERIC_INVALID       = 2296;

   /**
    * the number specified for a PSNumericLiteral was invalid
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the format specified</TD></TR>
    * </TABLE>
    */
   public static final int LITERAL_NUMERICFMT_INVALID    = 2297;

   /**
    * the IPSExtensionDef description was too long
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>max length permitted</TD></TR>
    * <TR><TD>1</TD><TD>length specified</TD></TR>
    * </TABLE>
    */
   public static final int UDFEXIT_DESC_TOO_BIG    = 2298;

   /**
    * the PSApplication object request root is required.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the application name</TD></TR>
    * </TABLE>
    */
   public static final int APP_ROOT_REQD           = 2299;

   /**
    * the PSPageDataTank contains a source schema with an invalid URL
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the application name</TD></TR>
    * <TR><TD>1</TD><TD>the data set name</TD></TR>
    * <TR><TD>2</TD><TD>the specified schema source URL</TD></TR>
    * </TABLE>
    */
   public static final int PAGE_TANK_BAD_SCHEMA_URL   = 2300;

   /**
    * the style sheet specified on the PSResultPage has an invalid URL
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the application name</TD></TR>
    * <TR><TD>1</TD><TD>the data set name</TD></TR>
    * <TR><TD>2</TD><TD>the specified style sheet URL</TD></TR>
    * </TABLE>
    */
   public static final int STYLE_SHEET_BAD_URL        = 2301;

   /**
    * the exit must not be null when defining an exit call
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int EXITCALL_EXIT_NULL         = 2302;

   /**
    * the specified exit does not implement any of our interfaces
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>exit name</TD></TR>
    * <TR><TD>1</TD><TD>exit class</TD></TR>
    * </TABLE>
    */
   public static final int EXIT_INTERFACES_NOT_IMPLEMENTED  = 2303;

   /**
    * the specified class is not a known exit handler (we don't support it)
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>class of object specified as an exit</TD></TR>
    * </TABLE>
    */
   public static final int UNKNOWN_EXIT_TYPE          = 2304;

   /**
    * An unknown or unsupported join type was specified.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the type code</TD></TR>
    * </TABLE>
    */
   public static final int BE_JOIN_UNKNOWN_TYPE          = 2305;

   /**
    * The URL for a custom error was empty.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int CUSTOM_ERROR_URL_EMPTY = 2306;

   /**
    * The back end table object was null
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int BE_TABLE_NULL = 2307;


   /**
    * The JDBC driver class name was null or empty
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int JDBC_DRIVER_CLASS_NULL = 2308;

   /**
    * The JDBC driver class could not be loaded.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the class name</TD></TR>
    * <TR><TD>1</TD><TD>a brief description of the error</TD></TR>
    * </TABLE>
    */
   public static final int JDBC_DRIVER_CLASS_LOAD_ERROR = 2309;

   /**
    * The maximum connection parameter of the PSBackEndConnection
    * object was invalid.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the alias</TD></TR>
    * <TR><TD>1</TD><TD>the maximum</TD></TR>
    * </TABLE>
    */
   public static final int BE_CONN_MAXCONN_INVALID = 2310;

   /**
    * The minimum connection parameter of the PSBackEndConnection
    * object was invalid.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the alias</TD></TR>
    * <TR><TD>1</TD><TD>the minimum</TD></TR>
    * </TABLE>
    */
   public static final int BE_CONN_MINCONN_INVALID = 2311;

   /**
    * The idle timeout parameter of the PSBackEndConnection
    * object was invalid.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the alias</TD></TR>
    * <TR><TD>1</TD><TD>the timeout</TD></TR>
    * </TABLE>
    */
   public static final int BE_CONN_TIMEOUT_INVALID = 2312;

   /**
    * The value parameter of the PSExtensionParamValue was null.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int EXIT_PARAM_VALUE_NULL = 2313;

   /**
    * The data selector's selection type is invalid.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>selection type</TD></TR>
    * </TABLE>
    */
   public static final int DATASEL_SEL_TYPE_INVALID = 2314;

   /**
    * The data selector's cache type is invalid.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>cache type</TD></TR>
    * </TABLE>
    */
   public static final int DATASEL_CACHE_TYPE_INVALID = 2315;

   /**
    * The data selector's cache age interval is invalid.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>cache age interval (in minutes)</TD></TR>
    * </TABLE>
    */
   public static final int DATASEL_CACHE_AGE_INTERVAL_INVALID = 2316;

   /**
    * The URL of a login web page was null or empty.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int LOGIN_WEBPAGE_URL_EMPTY = 2317;

   /**
    * The logger options were invalid.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the option bits</TD></TR>
    * </TABLE>
    */
   public static final int LOGGER_OPTIONS_INVALID = 2318;

   /**
    * The recipients of a notifier were null or empty.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int NOTIFIER_RECIPIENTS_EMPTY = 2319;

   /**
    * The data updater of an update pipe must allow at
    * least one of the following types: insert, update, or delete.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int UPDATEPIPE_NO_SYNC_TYPES = 2320;

   /**
    * The PSConditional value was null or missing, but the
    * conditional's operator requires a value.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the operator</TD></TR>
    * </TABLE>
    */
   public static final int COND_VALUE_NULL = 2321;

   /**
    * The referenced UDF has not been defined.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the exit name</TD></TR>
    * </TABLE>
    */
   public static final int UDFCALL_EXIT_UNDEFINED = 2322;

   /**
    * There are not enough joins to relate all of the tables
    * in the data tank.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the name of the dataset containing the tank</TD></TR>
    * <TR><TD>1</TD><TD>the number of tables present</TD></TR>
    * <TR><TD>2</TD><TD>the number of joins present</TD></TR>
    * <TR><TD>3</TD><TD>the number of joins required</TD></TR>
    * </TABLE>
    */
   public static final int BE_TANK_JOINS_REQUIRED = 2323;

   /**
    * The group ID for the data mapping was invalid.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the invalid ID</TD></TR>
    * </TABLE>
    */
   public static final int DATAMAPPING_GROUP_ID_INVALID = 2324;

   /**
    * The data selector's cache age time is required.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the name of the data set to whose pipe the selector is
    * applied</TD></TR>
    * </TABLE>
    */
   public static final int DATASEL_CACHE_AGE_TIME_REQUIRED = 2325;

   /**
    * The data updater requires at least one update column.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the name of the data set to whose pipe the
    * updater (synchronizer) is applied</TD></TR>
    * </TABLE>
    */
   public static final int SYNC_NO_UPDATE_COLUMNS = 2326;

   /**
    * The ACL type was invalid.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>1</TD><TD>The name of the user, group, or role</TD></TR>
    * <TR><TD>0</TD><TD>The type specified</TD></TR>
    * </TABLE>
    */
   public static final int ACL_TYPE_INVALID = 2327;

   /**
    * The type of a PSXParam was not valid.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>1</TD><TD>The XML fieldname</TD></TR>
    * <TR><TD>0</TD><TD>A brief description of the error</TD></TR>
    * </TABLE>
    */
   public static final int XML_PARAM_INVALID = 2328;

   /**
    * The name of the relationship property may not be null or empty
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int RELATIONSHIP_PROPERTY_NAME_EMPTY = 2329;

   /**
    * The macro name may not be null or empty
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int MACRO_NAME_EMPTY = 2330;

   /**
    * the PSServerConfiguration object request root was too long.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>max length permitted</TD></TR>
    * <TR><TD>1</TD><TD>length specified</TD></TR>
    * </TABLE>
    */
   public static final int SRV_ROOT_TOO_BIG           = 2350;

   /**
    * PSServerConfiguration.setAcl was called with a null ACL.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int SRV_ACL_NULL               = 2351;

   /**
    * PSServerConfiguration.setAcl was called with no ACL entries.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int SRV_ACL_EMPTY              = 2352;

   /**
    * PSServerConfiguration.setAcl was called with no admin
    * access ACL entries.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int SRV_ACL_NO_ADMIN           = 2353;

   /**
    * the PSSecurityProviderInstance object name was too long.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>max length permitted</TD></TR>
    * <TR><TD>1</TD><TD>length specified</TD></TR>
    * </TABLE>
    */
   public static final int SPINST_NAME_TOO_BIG        = 2354;

   /**
    * the PSSecurityProviderInstance object type was invalid.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the type specified</TD></TR>
    * </TABLE>
    */
   public static final int SPINST_TYPE_INVALID        = 2355;

   /**
    * Tried to set PSAclEntry access level with to an invalid
    * access level.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the access level</TD></TR>
    * </TABLE>
    */
   public static final int ACL_SECURITY_LEVEL_INVALID = 2356;

   /**
    * The version of the PSApplication object was
    * null or invalid.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the invalid version</TD></TR>
    * </TABLE>
    */
   public static final int APP_VERSION_INVALID        = 2357;

   /**
    * The Java exit handler was null.
    * <p>
    * No arguments.
    */
   public static final int JAVA_EXIT_HANDLER_NULL     = 2358;

   /**
    * A param def for the specified Java exit was null.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the name of the exit handler</TD></TR>
    * <TR><TD>0</TD><TD>the 1-based param def index</TD></TR>
    * </TABLE>
    */
   public static final int JAVA_EXIT_HANDLER_NULL_PARAM_DEF = 2359;

   /**
    * The application has no data sets.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the application</TD></TR>
    * </TABLE>
    */
   public static final int APP_NO_DATASETS = 2360;

   /**
    * Application contains data sets with duplicate request pages.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the application name</TD></TR>
    * <TR><TD>1</TD><TD>the request page</TD></TR>
    * <TR><TD>2</TD><TD>the name of the data set containing
    * the duplicated request page</TD></TR>
    * </TABLE>
    */
   public static final int APP_REQUEST_ROOTS_DUP = 2361;

   /**
    * Application contains data sets with duplicate names.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>application name</TD></TR>
    * <TR><TD>1</TD><TD>data set name that is duplicated</TD></TR>
    * </TABLE>
    */
   public static final int APP_DATASET_NAMES_DUP = 2362;

   /**
    * For an extension call with a parameter, the paramater value should
    * should not be null.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int EXT_CALL_PARAM_VALUE_NULL = 2363;

   /**
    * Native select statements are not currently supported for
    * heterogenous joins.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int HETERO_NATIVE_SELECT_NOT_SUPPORTED  = 2364;

   /**
    * The data selector cannot be NULL.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int QPIPE_DATA_SELECTOR_NULL      = 2365;

   /**
    * The URL for a custom error page is invalid.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>application name</TD></TR>
    * <TR><TD>1</TD><TD>error description</TD></TR>
    * <TR><TD>2</TD><TD>URL specified</TD></TR>
    * <TR><TD>3</TD><TD>failure reason (eg, e.toString())</TD></TR>
    * </TABLE>
    */
   public static final int CUSTOM_ERROR_URL_INVALID      = 2366;

   /**
    * the PSDataSet contains an Xml field which is mapped into two different
    * request links.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the xml field</TD></TR>
    * </TABLE>
    */
   public static final int DATASET_XMLFIELD_MULTI_LINK_ERROR = 2367;

   /**
    * The Subject type was invalid.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the user or group</TD></TR>
    * <TR><TD>1</TD><TD>The type specified</TD></TR>
    * </TABLE>
    */
   public static final int SUBJECT_TYPE_INVALID = 2368;

   /**
    * The number of parameters specified for the database function call does not
    * match the number of parameters defined in the database function definition
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the number of params specified</TD></TR>
    * <TR><TD>1</TD><TD>the database function name</TD></TR>
    * <TR><TD>2</TD><TD>the number of params in the function definition</TD></TR>
    * </TABLE>
    */
   public static final int DATABASE_FUNCTION_CALL_PARAM_COUNT_MISMATCH  = 2370;

   /**
    * NULL specified as a parameter value for database function call.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the database function name</TD></TR>
    * </TABLE>
    */
   public static final int DATABASE_FUNCTION_PARAM_VALUE_NULL = 2371;

   /**
    * Database function definition not found for the specified function and
    * driver.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the database function name</TD></TR>
    * <TR><TD>1</TD><TD>the database driver type</TD></TR>
    * </TABLE>
    */
   public static final int DATABASE_FUNCTION_DEFINITION_NOT_FOUND = 2372;

   /**
    * Unsupported database function parameter type.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>parameter type</TD></TR>
    * <TR><TD>1</TD><TD>database function name</TD></TR>
    * </TABLE>
    */
   public static final int UNSUPPORTED_DATABASE_FUNCTION_PARAMETER_TYPE = 2373;

   /**
    * Error while parsing database function body.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the database function name</TD></TR>
    * <TR><TD>1</TD><TD>parse error message</TD></TR>
    * </TABLE>
    */
   public static final int DATABASE_FUNCTION_PARSE_ERROR = 2374;
   
   /**
    * The request name is not unique, and there are
    * no selection parameters to distinguish them
    * <P>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the page in question</TD></TR>
    * <TR><TD>1</TD><TD>The name of the application</TD></TR>
    * </TABLE> 
    */
   public static final int REQUEST_NAME_DUP = 2375;
   
   /**
    * A JDBC driver configuration could not be located or created.
    * <P>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The driver name</TD></TR>
    * </TABLE> 
    */
   public static final int NO_JDBC_DRIVER_CONFIG = 2376;

   /**
    * A JNDI datasource could not be located or created.
    * <P>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The driver name</TD></TR>
    * <TR><TD>1</TD><TD>The server name</TD></TR>
    * </TABLE> 
    */
   public static final int NO_JNDI_DATASOURCE = 2377;
   
   /**
    * A datasource connection configuration could not be located or created.
    * <P>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The JNDI datasource name</TD></TR>
    * <TR><TD>1</TD><TD>The database name</TD></TR>
    * <TR><TD>2</TD><TD>The origin</TD></TR>
    * </TABLE> 
    */
   public static final int NO_DATASOURCE_CONNECTION = 2378;

   /**
    * An application contains local backend credentials which are no longer
    * supported.
    * <P>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The application name</TD></TR>
    * </TABLE> 
    */   
   public static final int APP_BACKEND_CREDS_NOT_SUPPORTED = 2379;
   
   /**
    * An application contains a custom login page which is no longer supported.
    * <P>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The application name</TD></TR>
    * </TABLE> 
    */   
   public static final int APP_LOGIN_PAGE_NOT_SUPPORTED = 2380;
   
   /**
    * the PSField / PSFieldSet name is not unique accross all item fields /
    * field sets.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the field / field set name</TD></TR>
    * </TABLE>
    */
   public static final int FIELD_NAME_NOT_UNIQUE = 2401;

   /**
    * unsupported field type.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>allowed field types</TD></TR>
    * </TABLE>
    */
   public static final int UNSUPPORTED_FIELD_TYPE = 2402;

   /**
    * unsupported occurrence dimension
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>allowed occurrence dimension types</TD></TR>
    * </TABLE>
    */
   public static final int UNSUPPORTED_OCCURRENCE_DIMENSION = 2403;

   /**
    * unsupported occurrence multi valued type
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>null or the pipe class</TD></TR>
    * </TABLE>
    */
   public static final int UNSUPPORTED_OCCURRENCE_MULTI_VALUED_TYPE = 2404;

   /**
    * invalid content editor content type
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>content type</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_CONTENT_TYPE = 2405;

   /**
    * invalid content editor workflow ID
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>workflow ID</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_WORKFLOW_ID = 2406;

   /**
    * content editor shared definition needs a collection of group fields
    * with at least one entry.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * </TABLE>
    */
   public static final int INVALID_CONTENT_EDITOR_SHARED_DEF = 2407;

   /**
    * unsupported PSChoice type
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>allowed types</TD></TR>
    * </TABLE>
    */
   public static final int UNSUPPORTED_CHOICE_TYPE = 2408;

   /**
    * unsupported sort order
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>allowed sort orders</TD></TR>
    * </TABLE>
    */
   public static final int UNSUPPORTED_SORT_ORDER = 2409;

   /**
    * invalid global table ID
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the table ID</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_GLOBAL_TABLE_ID = 2410;

   /**
    * the local choices are <code>null</code> or empty.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * </TABLE>
    */
   public static final int LOCAL_CHOICES_NULL_OR_EMPTY = 2411;

   /**
    * the lookup choices are <code>null</code>
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * </TABLE>
    */
   public static final int LOOKUP_CHOICES_NULL = 2412;

   /**
    * an application flow must have at least one redirect
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * </TABLE>
    */
   public static final int INVALID_APPLICATION_FLOW = 2413;

   /**
    * the command handler must have at least one stylesheet
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * </TABLE>
    */
   public static final int INVALID_COMMAND_HANDLER_STYLESHEETS = 2414;

   /**
    * a conditional exist must have a valid extension call set
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * </TABLE>
    */
   public static final int INVALID_CONDITIONAL_EXIT = 2415;

   /**
    * a conditional request must have at least one valid rule
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * </TABLE>
    */
   public static final int INVALID_CONDITIONAL_REQUEST = 2416;

   /**
    * a conditional stylesheet must have at least one valid rule
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * </TABLE>
    */
   public static final int INVALID_CONDITIONAL_STYLESHEET = 2417;

   /**
    * a conatiner locator must have at least one valid table set.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * </TABLE>
    */
   public static final int INVALID_CONTAINER_LOCATOR = 2418;

   /**
    * the command handler reference is invalid
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * </TABLE>
    */
   public static final int INVALD_COMMAND_HANDLER_REFERENCE = 2419;

   /**
    * The redirector is invalid. Must at least conatain a default request.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * </TABLE>
    */
   public static final int INVALD_REDIRECT = 2420;

   /**
    * each command handler must have at least one redirect
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * </TABLE>
    */
   public static final int INVALID_COMMAND_HANDLER_REDIRECTS = 2421;

   /**
    * The URL request is invalid. Must have the parts or the UDF.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * </TABLE>
    */
   public static final int INVALID_URL_REQUEST = 2422;

   /**
    * The content editor mapper must have a filed set and a UI definition.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * </TABLE>
    */
   public static final int INVALID_CONTENT_EDITOR_MAPPER = 2423;

   /**
    * The content editor pipe must have a locator and mapper.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * </TABLE>
    */
   public static final int INVALID_CONTENT_EDITOR_PIPE = 2424;

   /**
    * The control reference name is required.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * </TABLE>
    */
   public static final int INVALID_CONTROL_REF = 2425;

   /**
    * unsupported PSDefaultSelected type
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>allowed types</TD></TR>
    * </TABLE>
    */
   public static final int UNSUPPORTED_DEFAULT_SELECTED_TYPE = 2426;

   /**
    * The default selected is invalid (misses a required field)
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * </TABLE>
    */
   public static final int INVALID_DEFAULT_SELECTED = 2427;

   /**
    * unsupported PSFieldSet type
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>allowed types</TD></TR>
    * </TABLE>
    */
   public static final int UNSUPPORTED_FIELD_SET_TYPE = 2428;

   /**
    * unsupported PSFieldSet repeatability
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>allowed repeatability settings</TD></TR>
    * </TABLE>
    */
   public static final int UNSUPPORTED_FIELD_SET_REPEATABILITY = 2429;

   /**
    * The field set is invalid (misses a required element)
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * </TABLE>
    */
   public static final int INVALID_FIELD_SET_NAME = 2430;

   /**
    * The display mapper is invalid (misses a required element)
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * </TABLE>
    */
   public static final int INVALID_DISPLAY_MAPPER = 2431;

   /**
    * The display mapping is invalid (misses a required element)
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * </TABLE>
    */
   public static final int INVALID_DISPLAY_MAPPING = 2432;

   /**
    * The display text is invalid (misses a required element)
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * </TABLE>
    */
   public static final int INVALID_DISPLAY_TEXT = 2433;

   /**
    * The entry is invalid (misses a required element)
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * </TABLE>
    */
   public static final int INVALID_ENTRY = 2434;

   /**
    * The field is invalid (misses a required element)
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * </TABLE>
    */
   public static final int INVALID_FIELD = 2435;

   /**
    * The field translation is invalid (misses a required element)
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * </TABLE>
    */
   public static final int INVALID_FIELD_TRANSLATION = 2436;

   /**
    * unsupported include when attribute
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>allowed types</TD></TR>
    * </TABLE>
    */
   public static final int UNSUPPORTED_INCLUDE_WHEN = 2437;

   /**
    * The parameter is invalid (misses a required element)
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * </TABLE>
    */
   public static final int INVALID_PARAM = 2438;

   /**
    * The shared field group is invalid (misses a required element)
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * </TABLE>
    */
   public static final int INVALID_SHARED_FIELD_GROUP = 2439;

   /**
    * The stylesheet is invalid (misses a required element)
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * </TABLE>
    */
   public static final int INVALID_STYLESHEET = 2440;

   /**
    * The table locator is invalid (misses a required element)
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * </TABLE>
    */
   public static final int INVALID_TABLE_LOCATOR = 2441;

   /**
    * The table reference is invalid (misses a required element)
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * </TABLE>
    */
   public static final int INVALID_TABLE_REF = 2442;

   /**
    * The table set is invalid (misses a required element)
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * </TABLE>
    */
   public static final int INVALID_TABLE_SET = 2443;

   /**
    * The default UI is invalid (misses a required element)
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * </TABLE>
    */
   public static final int INVALID_UI_DEFINITION = 2444;

   /**
    * unsupported data hiding option
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>allowed data hiding options</TD></TR>
    * </TABLE>
    */
   public static final int UNSUPPORTED_DATA_HIDING = 2445;

   /**
    * The form action is invalid (misses a required element)
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * </TABLE>
    */
   public static final int INVALID_FORM_ACTION = 2446;

   /**
    * The action link is invalid (misses a required element)
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * </TABLE>
    */
   public static final int INVALID_ACTION_LINK = 2447;

   /**
    * The action link location is invalid (misses a required element)
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * </TABLE>
    */
   public static final int INVALID_LOCATION = 2448;

   /**
    * The custom action group is invalid (misses a required element)
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * </TABLE>
    */
   public static final int INVALID_CUSTOM_ACTION_GROUP = 2449;

   /**
    * System table in ContentEditorSystemDef not found.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * </TABLE>
    */
   public static final int SYSTEM_TABLE_NOT_FOUND = 2450;

   /**
    * The fieldset is only allowed to have a specified number of fields.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    *    <TR><TH>Arg</TH><TH>Description</TH></TR>
    *    <TR><TD>0</TD><TD>The fieldset name.</TD></TR>
    *    <TR><TD>1</TD><TD>The min # of fields, as a String.</TD></TR>
    *    <TR><TD>2</TD><TD>The max # of fields, as a String.</TD></TR>
    * </TABLE>
    */
   public static final int CE_INCORRECT_FIELD_COUNT = 2451;

   /**
    * The specified object type is not allowed to contain fieldsets.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    *    <TR><TH>Arg</TH><TH>Description</TH></TR>
    *    <TR><TD>0</TD><TD>The object type name.</TD></TR>
    * </TABLE>
    */
   public static final int CE_CANNOT_HAVE_FIELDSETS = 2452;


   /**
    * The tables referred by fieldsets in content editor do not exist in
    * database.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    *    <TR><TH>Arg</TH><TH>Description</TH></TR>
    *    <TR><TD>0</TD><TD>The List of table names separated by ',' which do not
    * exist</TD></TR>
    * </TABLE>
    */
   public static final int CE_NOT_EXIST_TABLES = 2453;

   /**
    * The table to be created already exists in the database.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    *    <TR><TH>Arg</TH><TH>Description</TH></TR>
    *    <TR><TD>0</TD><TD>Table Name</TD></TR>
    * </TABLE>
    */
   public static final int CREATE_TABLE_EXISTS = 2454;

   /**
    * The Content Editor System Definition file does not exist.
    */
   public static final int CE_SYSTEM_DEF_NOT_FOUND = 2455;

   /**
    * The Content Editor Shared Definition files do not exist.
    */
   public static final int CE_SHARED_DEF_NOT_FOUND = 2456;

   /**
    * The field is missing required element for some action.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>field name</TD></TR>
    * <TR><TD>1</TD><TD>missing element name</TD></TR>
    * </TABLE>
    */
   public static final int CE_MISSING_FIELD_ELEMENT = 2457;

   /**
    * A shared def group included by a local def does not exist in the shared
    * def.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The group name</TD></TR>
    * </TABLE>
    */
   public static final int CE_INCLUDED_GROUP_INVALID = 2458;

   /**
    * A shared group has been included but the shared def is not found or is
    * invalid.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    *    <TR><TD>0</TD><TD>The group name.</TD></TR>
    * </TABLE>
    */
   public static final int CE_SHARED_GROUP_NO_DEF = 2459;

   /**
    * One or more shared field that are excluded is not found in any included
    * shared groups.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    *    <TR><TD>0</TD><TD>The field name(s).</TD></TR>
    * </TABLE>
    */
   public static final int CE_SHARED_EXCLUDE_INVALID = 2460;

   /**
    * One or more system fields that are excluded is not found in system def.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    *    <TR><TD>0</TD><TD>The field name(s).</TD></TR>
    * </TABLE>
    */
   public static final int CE_SYSTEM_EXCLUDE_INVALID = 2461;

   /**
    * Field specified in the excluded list is not found
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The field</TD></TR>
    * </TABLE>
    */
   public static final int CE_EXCLUDED_FIELD_MISSING  = 2462;

   /**
    * Display Mapping in content editor pipe with child mapper references
    * system/shared def field mapping without one
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The field</TD></TR>
    * </TABLE>
    */
   public static final int CE_MAPPING_INVALID_CHILD   = 2463;

   /**
    * Display Mapping in content editor pipe with child mapper references
    * system/shared def field mapping without one
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The field</TD></TR>
    * </TABLE>
    */
   public static final int CE_MAPPING_INVALID_CHILD_FIELDS  = 2464;

   /**
    * Display Mapping in content editor pipe references invalid default uiset
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The default UISet name</TD></TR>
    * </TABLE>
    */
   public static final int CE_MAPPING_INVALID_DEFAULT_UISET = 2465;

   /**
    * While merging fields from system or shared def, the field name was
    * already found in the content editor, but the object type was different.
    * A PSFieldSet and a PSField within the same PSFieldSet may not have the
    * same name.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the duplicate field</TD></TR>
    * </TABLE>
    */
   public static final int CE_DUPLICATE_MERGED_FIELD_NAME = 2466;

   /**
    * While merging fields from system or shared def, a field definition was
    * overridden in the local def, but some values supplied attempted to
    * override an invalid member of the field from the source field.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the field</TD></TR>
    * <TR><TD>1</TD><TD>The type of the field</TD></TR>
    * <TR><TD>2</TD><TD>The setting overridden</TD></TR>
    * </TABLE>
    */
   public static final int CE_INVALID_FIELD_OVERRIDE = 2467;

   /**
    * Shared group display mappers must have parent mappings in the item's
    * display mapper with a matching fieldSetRef.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>1</TD><TD>The name of the fieldset ref</TD></TR>
    * </TABLE>
    */
   public static final int CE_UNUSED_MAPPER = 2468;

   /**
    * Shared groups cannot contain parent fieldsets.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the shared group</TD></TR>
    * <TR><TD>1</TD><TD>The name of the fieldset</TD></TR>
    * </TABLE>
    */
   public static final int CE_INVALID_SHARED_FIELDSET_TYPE = 2469;
   /**
    * Shared field set is missing required child display mapping in shared
    * definition or the mapping is invalid.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>fieldset name</TD></TR>
    * </TABLE>
    */
   public static final int CE_MISSING_OR_INVALID_CHILD_DISPLAY_MAPPING = 2470;
   /**
    * Shared group name and its fieldSet must have matching names.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Shared group name</TD></TR>
    * </TABLE>
    */
   public static final int CE_GROUPNAME_AND_FIELDSETNAME_MUST_MATCH = 2471;
   /**
    * FieldSet name and DisplayMappers FieldSetRef must match.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Shared group name</TD></TR>
    * </TABLE>
    */
   public static final int CE_FIELDSETNAME_AND_FIELDSETREF_MUST_MATCH = 2472;

   /**
    * Shared field set is missing required child display mapping in shared definition.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>fieldset name</TD></TR>
    * </TABLE>
    */
   public static final int CE_MISSING_CHILD_DISPLAY_MAPPING = 2473;

   /**
    * Choice Filter is missing a required child element.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The child element name</TD></TR>
    * </TABLE>
    */
   public static final int CHOICE_FILTER_MISSING_REQUIRED_CHILD = 2474;

   /**
    * Choice Filter DependentField missing required attribute.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The attribute name</TD></TR>
    * </TABLE>
    */
   public static final int CHOICE_FILTER_DEPENDENT_FIELD_MISSING_ATTR = 2475;


   /**
    * the specified method is not yet supported
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the unsupported method</TD></TR>
    * </TABLE>
    */
   public static final int METHOD_NOT_SUPPORTED       = 2801;

   /**
    * the specified application object could not be found
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the name of the app</TD></TR>
    * </TABLE>
    */
   public static final int APP_NOT_FOUND              = 2802;

   /**
    * an application of the specified name already exists
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the name of the app</TD></TR>
    * </TABLE>
    */
   public static final int APP_NAME_ALREADY_EXISTS    = 2803;

   /**
    * an exception occurred while loading the application
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the application name</TD></TR>
    * <TR><TD>1</TD><TD>the exception text</TD></TR>
    * </TABLE>
    */
   public static final int APP_LOAD_EXCEPTION         = 2804;

   /**
    * the server configuration could not be found
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int SERVER_CFG_NOT_FOUND       = 2805;

   /**
    * an exception occurred while loading the server configuration
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the exception text</TD></TR>
    * </TABLE>
    */
   public static final int SERVER_CFG_LOAD_EXCEPTION  = 2806;

   /**
    * the user configuration could not be found
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the name of the user</TD></TR>
    * </TABLE>
    */
   public static final int USER_CFG_NOT_FOUND         = 2807;

   /**
    * an exception occurred while loading the user configuration
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the name of the user</TD></TR>
    * <TR><TD>1</TD><TD>the exception text</TD></TR>
    * </TABLE>
    */
   public static final int USER_CFG_LOAD_EXCEPTION    = 2808;

   /**
    * The application file was not found.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the application</TD></TR>
    * <TR><TD>1</TD><TD>The name of the file</TD></TR>
    * </TABLE>
    */
   public static final int APP_FILE_NOT_FOUND = 2809;

   /**
    * The application directory was not found.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the application</TD></TR>
    * <TR><TD>1</TD><TD>The name of the directory</TD></TR>
    * </TABLE>
    */
   public static final int APP_DIR_NOT_FOUND = 2810;

   /**
    * The subdirectory for the application file could not
    * be created.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the application</TD></TR>
    * <TR><TD>1</TD><TD>The name of the file, including the path</TD></TR>
    * </TABLE>
    */
   public static final int APP_FILE_MKSUBDIR_ERROR = 2811;

   /**
    * The object was already exclusively locked by someone else.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the locked object</TD></TR>
    * <TR><TD>1</TD><TD>The name of the user currently holding the lock</TD></TR>
    * <TR><TD>2</TD><TD>How many minutes from now the lock will expire if the
    * user does not renew it</TD></TR>
    * </TABLE>
    */
   public static final int LOCK_ALREADY_HELD = 2812;

   /**
    * Interrupted while waiting for a lock.
    * <p>
    * There are no arguments for this message.
    */
   public static final int LOCK_WAIT_INTERRUPTED = 2813;

   /**
    * An invalid lock key was specified.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The value of the key</TD></TR>
    * </TABLE>
    */
   public static final int LOCK_BAD_KEY = 2814;

   /**
    * An invalid locker ID was specified.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The value of the locker id</TD></TR>
    * </TABLE>
    */
   public static final int LOCK_BAD_LOCKER_ID = 2815;

   /**
    * An invalid expiration was specified.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The value of the expiration (in ms)</TD></TR>
    * </TABLE>
    */
   public static final int LOCK_BAD_EXPIRATION = 2816;

   /**
    * A lock was requested for an object that cannot be locked.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The class of the object, or "null"</TD></TR>
    * </TABLE>
    */
   public static final int LOCK_BAD_OBJECT = 2817;

   /**
    * An invalid lock type was specified.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The invalid lock type</TD></TR>
    * </TABLE>
    */
   public static final int LOCK_BAD_TYPE = 2818;


   /**
    * The name of the (application or extension) file was null or empty.
    * <p>
    * There are no arguments for this message.
    */
   public static final int APP_FILE_NAME_NULL = 2819;

   /**
    * The (application or extension) file input stream was null or invalid.
    * <p>
    * There are no arguments for this message.
    */
   public static final int APP_FILE_STREAM_NULL = 2820;

   /**
    * More than one read was attempted from a file stream. The file could be an
    * application file, or an extension file, or any other kind of file.
    * <p>
    * There are no arguments for this message.
    */
   public static final int APP_FILE_STREAM_EXHAUSTED = 2821;

   /**
    * An IO error occurred while reading from, encoding, or decoding a file.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The file name</TD></TR>
    * <TR><TD>1</TD><TD>A description of the IO error</TD></TR>
    * </TABLE>
    */
   public static final int APP_FILE_IO_ERROR = 2822;

   /**
    * An object store handler received a request it did not understand.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The request type</TD></TR>
    * </TABLE>
    */
   public static final int REQ_UNKNOWN_TYPE = 2823;

   /**
    * An object store handler received a request with a null document.
    * <p>
    * There are no arguments for this message.
    */
   public static final int REQ_DOCUMENT_NULL = 2824;

   /**
    * An attempt to rename an application root directory failed for
    * unknown reasons.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The original application root directory</TD></TR>
    * <TR><TD>0</TD><TD>The new application root directory</TD></TR>
    * </TABLE>
    */
   public static final int APP_ROOT_RENAME_FAILED = 2825;

   /**
    * An IO error occurred while reading or writing
    * to the object store.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The app file name</TD></TR>
    * <TR><TD>1</TD><TD>A description of the IO error</TD></TR>
    * </TABLE>
    */
   public static final int HANDLER_IO_ERROR = 2826;

   /**
    * The application file name does not match the application
    * request root.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The app file name</TD></TR>
    * <TR><TD>1</TD><TD>The app request root</TD></TR>
    * </TABLE>
    */
   public static final int APP_FILE_ROOT_MISMATCH = 2827;

   /**
    * The object store handler was constructed with null properties.
    * <p>
    * There are no arguments for this message.
    */
   public static final int HANDLER_PROPERTIES_NULL = 2828;

   /**
    * The XML object store handler was constructed with a null or
    * invalid object dir.
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The object dir, or null</TD></TR>
    * </TABLE>
    */
   public static final int HANDLER_OBJECTDIR_INVALID = 2829;

   /**
    * An unexpected exception occurred in the object store handler.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The exception text</TD></TR>
    * </TABLE>
    */
   public static final int HANDLER_UNEXPECTED_EXCEPTION = 2830;

   /**
    * An unexpected exception occurred while validating an application.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The application name</TD></TR>
    * <TR><TD>1</TD><TD>A description of the exception</TD></TR>
    * </TABLE>
    */
   public static final int VALIDATION_UNEXPECTED_EXCEPTION = 2831;

   /**
    * A lockfile is corrupt.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The lock file name</TD></TR>
    * </TABLE>
    */
   public static final int LOCK_CORRUPT_LOCKFILE = 2832;

   /**
    * An IO error occurred while locking or unlocking an object.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The lock file name</TD></TR>
    * <TR><TD>1</TD><TD>A description of the IO error</TD></TR>
    * </TABLE>
    */
   public static final int LOCK_IO_EXCEPTION = 2833;

   /**
    * The object was already exclusively locked by the user requesting
    * the lock, but under a different user session.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the locked object</TD></TR>
    * <TR><TD>1</TD><TD>The name of the user currently holding the lock</TD></TR>
    * <TR><TD>2</TD><TD>How many minutes from now the lock will expire if the
    * user does not renew it</TD></TR>
    * <TR><TD>3</TD><TD>The session id of the session holding the lock</TD></TR>
    * </TABLE>
    */
   public static final int LOCK_ALREADY_HELD_SAME_USER = 2834;

   /**
    * The application object could not be converted to an appropriate format
    * for the current Rhythmyx version.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The version of the application</TD></TR>
    * <TR><TD>1</TD><TD>The version being converted to</TD></TR>
    * <TR><TD>2</TD><TD>The exception text returned during conversion</TD></TR>
    * </TABLE>
    */
   public static final int DOC_CONVERSION_FAILED = 2835;

   /**
   *The mapper do not have mappings
   *
   */
   public static final int APP_MAPPER_EMPTY=2836;

   /**
    * the character encoding map could not be found
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The file name we were looking for.</TD></TR>
    * </TABLE>
    */
   public static final int CHARACTER_SET_MAP_NOT_FOUND         = 2837;

   /**
    * the character encoding map could not be loaded
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The file name we tried to load.</TD></TR>
    * </TABLE>
    */
   public static final int CHARACTER_SET_LOAD_EXCEPTION        = 2838;

   /**
    * the feature set document could not be loaded.  This is not
    * the same as the file not existing - that is not an error
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The description of the error. </TD></TR>
    * </TABLE>
    */
   public static final int FEATURE_SET_LOAD_EXCEPTION       = 2839;

   /**
    * the resource should be locked by the current session but is not
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the locked object</TD></TR>
    * </TABLE>
    */
   public static final int LOCK_NOT_HELD                    = 2840;

   /**
    * An exception occurred while loading the role configuration.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the exception text</TD></TR>
    * </TABLE>
    */
   public static final int ROLE_CFG_LOAD_EXCEPTION = 2841;

   /**
    * An exception occurred while generating an id for a new component.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the exception text</TD></TR>
    * </TABLE>
    */
   public static final int DB_COMPONENT_NEW_ID = 2842;

   /**
    * An exception occurred while attempting to load the specified component.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the component type</TD></TR>
    * <TR><TD>1</TD><TD>the exception text</TD></TR>
    * </TABLE>
    */
   public static final int DB_COMPONENT_LOAD_EXCEPTION = 2843;

   /**
    * An exception occurred while attempting to load a related component.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the relation type</TD></TR>
    * <TR><TD>1</TD><TD>the related component type</TD></TR>
    * <TR><TD>2</TD><TD>the related component's id</TD></TR>
    * <TR><TD>3</TD><TD>the related component dataset name</TD></TR>
    * </TABLE>
    */
   public static final int RELATED_DB_COMPONENT_LOAD_EXCEPTION = 2844;

   /**
    * A minor validation exception occurred while validating CE included shared
    * groups where duplicate field names were detected.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>duplicate shared field name</TD></TR>
    * <TR><TD>1</TD><TD>shared group names</TD></TR>
    * </TABLE>
   */
   public static final int DUPLICATE_SHARED_FIELD_VALIDATION_WARNING = 2845;

   /**
    * An error that occurred while cataloging CE fields' choices
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Content type</TD></TR>
    * <TR><TD>1</TD><TD>Field type</TD></TR>
    * <TR><TD>2</TD><TD>Field name</TD></TR>
    * <TR><TD>3</TD><TD>Choices lookup url</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_CE_FIELD_CHOICES_ERROR = 2846;
   
   /**
    * The appfile cannot be renamed because it already exists
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>old path</TD></TR>
    * <TR><TD>1</TD><TD>new path</TD></TR> 
    * </TABLE>
    */
   public static final int APP_FILE_EXISTS_RENAME_ERROR = 2847;

   /**
    * The server does not support application roles any more
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    *    <TR><TH>Arg</TH><TH>Description</TH></TR>
    *    <TR><TD>0</TD><TD>Comma separated list of roles as a String</TD></TR>
    * </TABLE>
    */
   public static final int APP_ROLES_NOT_SUPPORTED = 2369;

   /**
    * the lookup choices are <code>null</code>
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * </TABLE>
    */
   public static final int LOOKUP_TABLE_INFO_NULL = 2848;
   
}

