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

package com.percussion.extension;

/**
 * Numeric definitions for system error exceptions
 */
public interface IPSExtensionErrors
{
   /**
    * This is used to log thrown sql exception text.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>error text (sql exception text)</TD></TR>
    * </TABLE>
    */
   public static final int BACKEND_COLUMN_ERROR       = 7001;

   /**
    * This is used to report an invalid parameter.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>list of valid types</TD></TR>
    * </TABLE>
    */
   public static final int UNKNOWN_PARAMETER_TYPE     = 7002;

   /**
    * This is used to report an error locating resource files.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the extension name</TD></TR>
    * <TR><TD>1</TD><TD>the error</TD></TR>
    * </TABLE>
    */
   public static final int CATALOG_EXT_RESOURCE_ERROR     = 7003;

   /**
    * This is used to report a missing or invalid required extension parameter.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the name of the required parameter</TD></TR>
    * <TR><TD>1</TD><TD>the error message</TD></TR>
    * </TABLE>
    */
   public static final int EXT_MISSING_REQUIRED_PARAMETER_ERROR = 7004;

   /**
    * This is used to report a missing or invalid extension/html parameter.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the name of the parameter</TD></TR>
    * <TR><TD>1</TD><TD>the error message</TD></TR>
    * </TABLE>
    */
   public static final int EXT_MISSING_HTML_PARAMETER_ERROR = 7005;

   /**
    * The wrong number of parameters was passed in to an extension
    * method.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>expected number of parameters</TD></TR>
    * <TR><TD>0</TD><TD>number of values specified</TD></TR>
    * </TABLE>
    */
   public static final int EXT_PARAM_VALUE_MISMATCH   = 7006;

   /**
    * An invalid parameter was provided to an extension.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The entire message string</TD></TR>
    * </TABLE>
    */
   public static final int EXT_PARAM_VALUE_INVALID = 7007;

   /**
    * If the suppliedResources element is specified, also a deploy name is
    * expected in the extension definition.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The extension definition</TD></TR>
    * </TABLE>
    */
   public static final int EXT_INSTALLER_DEPLOY_NAME_EXPECTED  = 7008;

   /**
    * The resource is not supported for this extension definition.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The extension definition</TD></TR>
    * <TR><TD>0</TD><TD>The unsupported resource</TD></TR>
    * </TABLE>
    */
   public static final int EXT_INSTALLER_UNSUPPORTED_RESOURCE  = 7009;

   /**
    * The resource does not exist.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The resource</TD></TR>
    * </TABLE>
    */
   public static final int EXT_INSTALLER_RESOURCE_NOT_EXITING  = 7010;

   /**
    * The resource is not readable.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The resource</TD></TR>
    * </TABLE>
    */
   public static final int EXT_INSTALLER_RESOURCE_NOT_READABLE = 7011;

   /**
    * an exception occurred during extension processing
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the extension name</TD></TR>
    * <TR><TD>1</TD><TD>the exception text</TD></TR>
    * </TABLE>
    */
   public static final int EXT_PROCESSOR_EXCEPTION = 7012;

   /**
    * An unexpected extension type was encountered.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the class detected</TD></TR>
    * <TR><TD>1</TD><TD>the expected interface</TD></TR>
    * </TABLE>
    */
   public static final int UNEXPECTED_EXT_TYPE_EXCEPTION = 7013;

   /**
    * Error in loading or unloading extensions.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>function name</TD></TR>
    * <TR><TD>0</TD><TD>the message text</TD></TR>
    * </TABLE>
    */
   public static final int EXT_HANDLER_LOAD_UNLOAD_ERROR    = 7014;

   /**
    * Error in preparing extension handlers.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the reason of failure</TD></TR>
    * </TABLE>
    */
   public static final int EXT_HANDLER_PREPARE_ERROR        = 7015;

   /**
    * Error in storing extension handler definitions.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the reason of failure</TD></TR>
    * </TABLE>
    */
   public static final int EXT_HANDLER_DEF_STORE_ERROR      = 7016;

   /**
    * Error in installing or updating extensions.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the extension name</TD></TR>
    * <TR><TD>1</TD><TD>the reason of failure</TD></TR>
    * </TABLE>
    */
   public static final int EXT_INSTALL_UPDATE_ERROR         = 7017;

   /**
    * An error occurred when storing an extension resource.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the extension name</TD></TR>
    * <TR><TD>1</TD><TD>the resource name</TD></TR>
    * <TR><TD>2</TD><TD>the description of the error</TD></TR>
    * </TABLE>
    */
   public static final int EXT_RESOURCE_STORE_ERROR             = 7018;

   /**
    * An error occurred when deleting an extension resource.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the extension name</TD></TR>
    * <TR><TD>1</TD><TD>the resource name</TD></TR>
    * <TR><TD>2</TD><TD>the description of the error</TD></TR>
    * </TABLE>
    */
   public static final int EXT_RESOURCE_DELETE_ERROR            = 7019;

   /**
    * The named extension could not be found.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the extension name</TD></TR>
    * </TABLE>
    */
   public static final int EXT_NOT_FOUND = 7020;

   /**
    * The named extension already exists.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the extension name</TD></TR>
    * </TABLE>
    */
   public static final int EXT_ALREADY_EXISTS = 7021;

  /**
    * An error occurred when constructing or initializing the extension
    * manager.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>error text</TD></TR>
    * </TABLE>
    */
   public static final int EXT_MANAGER_INIT_FAILED = 7022;

   /**
    * An error occurred when shutting down the extension manager.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>error text</TD></TR>
    * </TABLE>
    */
   public static final int EXT_MANAGER_SHUTDOWN_FAILED = 7023;

   /**
    * An error occurred when constructing or initializing an extension
    * handler.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the unique extension handler name</TD></TR>
    * <TR><TD>1</TD><TD>error text</TD></TR>
    * </TABLE>
    */
   public static final int EXT_HANDLER_INIT_FAILED = 7024;

   /**
    * An error occurred when shutting down an extension handler.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the unique extension handler name</TD></TR>
    * <TR><TD>1</TD><TD>error text</TD></TR>
    * </TABLE>
    */
   public static final int EXT_HANDLER_SHUTDOWN_FAILED = 7025;

   /**
    * An error occurred when constructing or initializing an extension.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the unique extension handler name</TD></TR>
    * <TR><TD>1</TD><TD>the unique extension name</TD></TR>
    * <TR><TD>2</TD><TD>error text</TD></TR>
    * </TABLE>
    */
   public static final int EXT_INIT_FAILED = 7026;

   /**
    * The extension def does not specify that it implements the correct
    * interface for the context in which it is being used.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the extension ref string</TD></TR>
    * <TR><TD>1</TD><TD>the expected interface</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_EXT_TYPE_EXCEPTION = 7027;

   /**
    * Unknow effect processing error.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The exception message</TD></TR>
    * </TABLE>
    */
   public static final int UNKNOWN_EFFECT_PROCESSING_ERROR = 7028;

   /**
    * An invalid XML element was supplied.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The received element name.</TD></TR>
    * <TR><TD>1</TD><TD>The expected element name.</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_XML_ELEMENT = 7029;

   /**
    * An invalid XML element was supplied, missing a required attribute.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The missing attribute name.</TD></TR>
    * </TABLE>
    */
   public static final int MISSING_REQUIRED_ATTRIBUTE = 7030;
   
   /**
    * Class not found for the supplied type string.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The class name.</TD></TR>
    * </TABLE>
    */
   public static final int CLASS_NOT_FOUND = 7031;

   /**
    * The parameters of an exit must be not null.
    */
   public static final int INVALID_NULL_PARAMS = 7032;
   

   /**
    * A required parameter is missing
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The missing parameter number or name.</TD></TR>
    * </TABLE>
    */
   public static final int MISSING_REQUIRED_PARAM_NO = 7033;

   /**
    * An invalid string parameter.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The invalid parameter number or name.</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_STRING_PARAM = 7034;

   /**
    * An invalid number parameter.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The invalid parameter number or name.</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_NUMBER_PARAM = 7035;

   /**
    * An invalid boolean parameter.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The invalid parameter number or name.</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_BOOLEAN_PARAM = 7036;
   
   /**
    * An invalid date parameter.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The invalid parameter number or name.</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_DATE_PARAM = 7037;

   /**
    * An invalid index value.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The invalid index value.</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_INDEX_VALUE = 7038;
   
   /**
    * An invalid index default value.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The invalid index value.</TD></TR>
    * </TABLE>
    */  
   public static final int INVALID_NUMBER_DEFAULT = 7039;

   /**
    * error compiling the JS function with message
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>function name</TD></TR>
    * <TR><TD>0</TD><TD>the message text</TD></TR>
    * </TABLE>
    */
   public static final int JS_COMPILE_FAILED          = 7301;

   /**
    * error compiling the JS function with message and source
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>function name</TD></TR>
    * <TR><TD>0</TD><TD>the message text</TD></TR>
    * <TR><TD>1</TD><TD>the source of the error</TD></TR>
    * </TABLE>
    */
   public static final int JS_COMPILE_FAILED_SRC      = 7302;

   /**
    * error calling the JS function with message
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>function name</TD></TR>
    * <TR><TD>0</TD><TD>the message text</TD></TR>
    * </TABLE>
    */
   public static final int JS_CALL_FAILED             = 7303;

   /**
    * error calling the JS function with message and source
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>function name</TD></TR>
    * <TR><TD>0</TD><TD>the message text</TD></TR>
    * <TR><TD>1</TD><TD>the source of the error</TD></TR>
    * </TABLE>
    */
   public static final int JS_CALL_FAILED_SRC         = 7304;


   /**
    * the extractor returned null for the style sheet to use, which is
    * not permitted
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the extractor's source</TD></TR>
    * </TABLE>
    */
   public static final int SET_EMPTYXML_STYLESHEET_NULL_SS  = 7401;

   /**
    * the style sheet URL is invalid
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the extractor's source</TD></TR>
    * <TR><TD>1</TD><TD>the error text</TD></TR>
    * </TABLE>
    */
   public static final int SET_EMPTYXML_STYLESHEET_INVALID_URL = 7402;

   /**
    * A required HTML parameter is missing to run the exit.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The exit name</TD></TR>
    * <TR><TD>0</TD><TD>The parameter name</TD></TR>
    * </TABLE>
    */
   public static final int MISSING_HTML_PARAMETER = 7403;

   /**
    * Community authentication failed.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Current user name</TD></TR>
    * </TABLE>
    */
   public static final int COMMUNITIES_AUTHENTICATION_FAILED_NOCOMMUNITY = 7404;

   /**
    * Community authentication failed.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Current user name</TD></TR>
    * </TABLE>
    */
   public static final int 
         COMMUNITIES_AUTHENTICATION_FAILED_INVALID_COMMUNITY = 7405;

   /**
    * User name cannot be empty.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int EMPTY_USRNAME1 = 7406;

   /**
    * The user name must not be empty after removing host names
    * from the input parameter.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int EMPTY_USRNAME2 = 7407;

   /**
    * The role list of the authenticated user must not be empty.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>authenticated user name</TD></TR>
    * </TABLE>
    */
   public static final int EMPTY_ROLE_LIST = 7408;

   /**
    * Invalid workflow id.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>invalid workflow id</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_WORKFLOWID = 7409;

   /**
    * User is not authorized to create this type of content.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int ILLEGAL_CONTENTTYPE = 7410;

   /**
    * Operation not allowed while the content is checked out.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int ILLEGAL_IF_CHECKEDOUT = 7411;

   /**
    * Operation not allowed while the content is not checked out by you
    * and you are not the administrator.
    * An administrator might have overriden your checkout.
    * You must go back and check-out the item again before attempting
    * this operation.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int ILLEGAL_IFNOT_CHECKEDOUT = 7412;


   /**
    * Role error for workflow and state id.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>invalid state id</TD></TR>
    * <TR><TD>1</TD><TD>invalid workflow id</TD></TR>
    * <TR><TD>2</TD><TD>RoleException.toString()</TD></TR>
    * </TABLE>
    */
   public static final int ROLE_ERROR_STATEID_WORKFLOWID = 7413;

   /**
    * No Roles are assigned with assignment type >= for specified accessLevel
    * for specified  stateid and workflow id.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>required access level</TD></TR>
    * <TR><TD>1</TD><TD>contentstate id</TD></TR>
    * <TR><TD>2</TD><TD>workflow id</TD></TR>
    * </TABLE>
    */
   public static final int ROLES_NOT_ASSIGNED = 7414;

   /**
    * Authentication failed: user does not act in any roles.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int AUTHENTICATION_FAILED1 = 7415;

   /**
    * Authentication failed.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int AUTHENTICATION_FAILED2 = 7416;

   /**
    * The workflow action list  must not be empty.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int WKFLOW_ACTIONLIST_EMPTY = 7417;

   /**
    * The workflow context must not be null.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int WKFLOW_CONTEXT_NULL = 7418;

   /**
    * Supplied extension is not a workflow action extension.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int INVALID_WKFLOW_EXT = 7419;

   /**
    * Executable extension does not exist.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int EXEC_EXT_NOTFOUND = 7420;

   /**
    * Status document element name must not be empty.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int STATUS_DOC_EMPTY = 7421;

   /**
    * ContentID node name must not be empty.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int CONTENTID_NODENAME_EMPTY = 7422;

   /**
    * ContentID node missing or empty.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int CONTENTID_NODE_MISSING_EMPTY = 7423;

   /**
    * ContentID node missing.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int CONTENTID_NODE_MISSING = 7424;

   /**
    * Parameters to the exit cannot be null.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int EXIT_PARAM_NULL = 7425;

   /**
    * The content id must not be null.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int CONTENTID_NULL = 7426;

   /**
    * Unable to update published document.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int PUBDOC_UPDATE_ERROR = 7427;

   /**
    * You must specify an HTML Parameter for the return value.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int HTML_PARAM_NULL1 = 7428;

   /**
    * You must specify the html param name to store next number.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int HTML_PARAM_NULL2 = 7429;

   /**
    * You must specify the table name for which the next number to be generated.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int TABLE_NAME_NULL = 7430;

   /**
    * You must specify the primary key column name to store the next number.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int PRIMARY_KEY_NULL = 7431;

   /**
    * No role info private object provided.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int ROLEINFO_OBJ_NULL = 7432;

   /**
    * The role list must not be empty.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int ROLELIST_EMPTY = 7433;

   /**
    * Invalid number of parameters.
    * <p>
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>array of parameters</TD></TR>
    */
   public static final int INVALID_PARAM_NUM = 7434;

   /**
    * Only an administrator may perform a transitions if the content item
    * is checked out.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int ADMIN_CHECKOUT_ONLY = 7435;

   /**
    * The user does not belong to any of the roles required for this transition.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int INVALID_TRANSITION_ROLE = 7436;

   /**
    * A valid transition comment was required but not specified.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int TRANSITION_COMMENT_NOT_SPECIFIED = 7437;

   /**
    * You do not have the document checked out.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int DOC_NOT_CHECKEDOUT = 7438;

   /**
    * This document does not have an edit revision.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int EDIT_REVISION_MISSING = 7439;

   /**
    * Attempt to check in a revision which does not match with
    * the one checked out.
    * <p>
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>checkin request revision</TD></TR>
    * <TR><TD>1</TD><TD>edit revision</TD></TR>
    */
   public static final int CHECKIN_REVISION_MISMATCH = 7440;

   /**
    * You can not check in an item when it is checked out by
    * somebody else and you are not an administrator.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int CHECKIN_NOT_ALLOWED = 7441;

   /**
    * You can not check out when it is checked out by somebody.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int CHECKOUT_NOT_ALLOWED = 7442;

   /**
    * Attempt to check out a revision when there is already one checked out
    * with a different revision.
    * <p>
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>check out request revision</TD></TR>
    * <TR><TD>1</TD><TD>edit revision</TD></TR>
    */
   public static final int CHECKOUT_REVISION_MISMATCH = 7443;

   /**
    * Attempt to check out a revision  but the largest existing revision is
    * the one specified.
    * <p>
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>check out request revision</TD></TR>
    * <TR><TD>1</TD><TD>tip revision</TD></TR>
    */
   public static final int CHECKOUT_REVISION_LIMIT = 7444;

   /**
    * Specified user has already tried to transition this document.
    * <p>
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>user name</TD></TR>
    */
   public static final int TRANSITION_ATTEMPT = 7445;

   /**
    * Mail Domain  may not be null.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int MAIL_DOMAIN_NULL = 7446;

   /**
    * Mail Domain  may not be empty.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int MAIL_DOMAIN_EMPTY = 7447;

   /**
    * SMTP Host may not be null.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int SMTP_HOST_NULL = 7448;

   /**
    * SMTP Host may not be empty.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int SMTP_HOST_EMPTY = 7449;

   /**
    * User name may not be null or empty after trimming.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int USERNAME_NULL_EMPTY_TRIM = 7450;

   /**
    * Invalid adhoc type in data base.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int INVALID_ADHOC = 7451;

   /**
    * State role name may not be null or empty after trimming.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int STATEROLE_NULL_EMPTY_TRIM = 7452;

   /**
    * No records were found.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int NO_RECORDS = 7453;

   /**
    * No adhoc assignment found for list of unassigned users.
    * <p>
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>comma delimited string of unassigned users.</TD></TR>
    */
   public static final int ADHOC_ASSIGNMENT_NOT_FOUND = 7454;

   /**
    * The requested translation for the supplied item already exists.
    * <p>
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The language string</TD></TR>
    * <TR><TD>1</TD><TD>The content id</TD></TR>
    */
   public static final int TRANSLATION_ALREADY_EXISTS = 7455;

   /**
   * Not able to check-in item as an administrator has
   * overridden your checkout. You must go back
   * and check-out the item again before editing.
   * <p>
   * No arguments are passed in for this message.
   */
   public static final int ILLEGAL_IF_CHECKEDOUT_OVERRIDE = 7456;

   /**
    * Slot name must be unique.
    * <p>
    * No arguments passed in
    */
   public static final int VALIDATE_SLOTNAME_NOT_UNIQUE = 7457;

   /**
    * Error message specifying that items cannot be checked out if they are in
    * any public state.
    * <p>
    * No arguments passed in
    */
   public static final int CHECKOUT_FROM_PUBLIC_STATE = 7458;
   /**
    * The specified transition is invalid as the current state id of the item
    * does not match with the from stateid of this transition.
    * <p>
    * Five arguments are needed for this message namely, contentid, workflowid,
    * current stateid of the item, from stateid of transition and the
    * transitionid.
    */
   public static final int INVALID_TRANSITION = 7459;
   /**
    * The specified transition by internal name or id does not exist for the
    * current state of the content item.
    * <p>
    * Four arguments are needed for this message namely, contentid, workflowid,
    * current stateid of the item and intrenal name/transitonid of the 
    * transition.
    */
   public static final int MISSING_TRANSITION = 7460;

   /**
    * Authentication failed. User community and item community are different.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int 
         AUTHENTICATION_FAILED_DIFFERENT_ITEM_USER_COMMUNITIES = 7461;
    
   /**
    * Illegal execution context for the effect.
    * <p>
    * Two arguments are required for this message, namely the name of the effect
    * and the context names this effect works under.
    */
   public static final int ILLEGAL_EXECUTION_CONTEXT = 7462;

   /**
    * Current relationship is not promotable and is ignored.
    * <p>
    * Two arguments are required for this message, namely the name of the effect
    * and the relationship name this effect is trying to process.
    */
   public static final int NONPROMOTABLE_RELATIONSHIP = 7463;

   /**
    * Workflowid in the request is null while processing the effect and is
    * ignored.
    * <p>
    * One argument is required for this message, namely the name of the effect.
    */
   public static final int WORKFLOWID_IN_REQUEST_ISNULL = 7464;

   /**
    * Invalid workflow action for the effect and is ignored.
    * <p>
    * Two argument are required for this message, namely the name of the effect
    * and the workflow action name.
    */
   public static final int INVALID_WORKFLOW_ACTION = 7465;

   /**
    * The current item is not in public state and is ignored.
    * <p>
    * One argument is required for this message, namely the name of the effect.
    */
   public static final int ITEM_NOT_IN_PUBLIC_STATE = 7466;

   /**
    * This effect is triggered by itself and is ignored.
    * <p>
    * One argument is required for this message, namely the name of the effect.
    */
   public static final int EFFECT_SELF_TRIGGERED = 7467;

   /**
    * The force transition option must be 'yes' or 'no'.
    * <p>
    * Two arguments are required, namely the name of the effect and the current
    * or incorrect option for the effect.
    */
   public static final int INVALID_OPTION_FOR_FORCETRANSITION = 7468;

   /**
    * Invalid transition for this effect to run and hence is ignored.
    * <p>
    * Three arguments are required, namely the name of the effect, from state
    * and to state.
    */
   public static final int INVALID_TRANSITION_FOR_EFFECT = 7469;

   /**
    * The internal request required is missing on the server.
    * <p>
    * two arguments are required, namely the name of the effect and the
    * app/resource name.
    */
   public static final int MISSING_INTERNAL_REQUEST_RESOURCE = 7470;

   /**
    * A dependent item is not in a desired state to run this effect.
    * <p>
    * Two arguments are required, namely the name of the effect and the desired
    * state.
    */
   public static final int DEPENDENT_ITEM_NOT_IN_DESIRED_STATE = 7471;

   /**
    * A dependent item is not in a state from where it cannot go to a desired
    * state.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The effect name.</TD></TR>
    * <TR><TD>1</TD><TD>The desired state.</TD></TR>
    * </TABLE>
    * Two arguments are required, namely the name of the effect and the desired
    * state.
    */
   public static final int DEPENDENT_ITEM_CANNOT_GOTO_DESIRED_STATE = 7472;

   /**
    * A custom error message that Validate effect throws.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The effect name.</TD></TR>
    * <TR><TD>1</TD><TD>The cause of the validation failure.</TD></TR>
    * </TABLE>
    */
   public static final int EFFECT_VALIDATE_MESSAGE = 7473;

   /**
    * The transition needed to move the promoted-over item to the 'archive' 
    * state failed for some reason (possibly validation failed).
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The error</TD></TR>
    * <TR><TD>1</TD><TD>The content id</TD></TR>
    * </TABLE>
    */
   public static final int PROMOTE_TRANSITION_FAILED = 7474;  
    
   /**
    * Bad information passed to the publish content extension.
    * <P>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Specifies the specific problem that occurred in
    * initialization.</TD></TR>
    * </TABLE>
    */
   public static final int BAD_PUBLISH_CONTENT_INITIALIZATION_DATA = 7475;
      
   /**
    * Some problem occurred while loading the properties file.
    * <P>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Specifies the specific problem that occurred.</TD></TR>
    * </TABLE>
    */
   public static final int BAD_PUBLISH_CONTENT_FILE_DATA = 7476;

   /**
    * Error message to indicate that the requested authtype value is not 
    * registered with the system.
    * <p>
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Authtype value requested</TD></TR>
    * <TR><TD>1</TD><TD>config file path</TD></TR>
    */
   public static final int AUTHTYPE_REGISTRATION_MISSING= 7477;

   /**
    * Error message to indicate that the resource implementing the requested 
    * authtype is missing.
    * <p>
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Authtype value requested</TD></TR>
    * <TR><TD>1</TD><TD>missing resource name</TD></TR>
    * <TR><TD>2</TD><TD>config file path</TD></TR>
    */
   public static final int AUTHTYPE_RESOURCE_MISSING = 7478;

   /**
    * Error message to indicate that the workflow comment exceeded the max length
    * of 255 characters.
    * <p>
    * No arguments are required to pass in
    */
   public static final int WF_COMMENT_CANNOT_EXCEED_255 = 7479;
   
   /**
    * Error message to indicate that a mandatory transition failed due to a
    * validation error.
    * <p>
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The transition name</TD></TR> 
    * <TR><TD>1</TD><TD>The content id</TD></TR>
    */
   public static final int MANDATORY_TRANSITION_VALIDATION_FAILURE = 7480;
   
   /**
    * Error message to indicate that a variant to be deleted was in use
    * by an existing relationship.
    * <table>
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The id of the variant</TD></TR>
    * <TR><TD>1</TD><TD>The content ids that are using the variant as a string</TD></TR>
    * </table> 
    */
   public static final int VARIANT_HAS_RELATIONSHIPS_ERROR = 7621;
   
   /**
    * Error message to indicate the path of a folder could not be loaded.
    * <table>
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The id of the folder</TD></TR>
    * </table> 
    */  
   public static final int FOLDER_PATH_ERROR = 7622;

   /**
    * Error message to indicate the jexl expression did not evaluate to return
    * a string
    * <table>
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The type that was found</TD></TR>
    * <TR><TD>1</TD><TD>The type that was expected</TD></TR>
    * </table> 
    */    
   public static final int JEXL_WRONG_RETURN_TYPE = 7623;
   
   /**
    * Error message to indicate the jexl expression failed to evaluate
    * <table>
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The failed expression</TD></TR>
    * </table> 
    */   
   public static final int JEXL_EVALUATION_FAILED = 7634;
   
   /**
    * Error message to indicate Error occurred when attempting
    *  to retrieve folder names
    * <table>
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The exception message</TD></TR>
    * </table> 
    */   
   public static final int ERROR_GETTING_FOLDER_NAMES = 7635;
   
   /**
    * Error message to indicate that no scheme could be found for the
    * specified template id, content id and context id
    * <table>
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The template id</TD></TR>
    * <TR><TD>1</TD><TD>The content type id</TD></TR>
    * <TR><TD>2</TD><TD>The context id</TD></TR>
    * </table> 
    */
   public static final int SCHEME_CANT_BE_FOUND = 7636;
}

