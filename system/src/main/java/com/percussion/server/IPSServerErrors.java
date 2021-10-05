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

package com.percussion.server;


/**
 * The IPSServerErrors inteface is provided as a convenient mechanism
 * for accessing the various server related error codes. The server error
 * code ranges are:
 * <TABLE BORDER="1">
 * <TR><TH>Range</TH><TH>Component</TH></TR>
 * <TR><TD>1000 - 1100</TD><TD>general errors used all over</TD></TR>
 * <TR><TD>1101 - 1175</TD><TD>application handlers</TD></TR>
 * <TR><TD>1176 - 1200</TD><TD>loadable handlers</TD></TR>
 * <TR><TD>1201 - 1300</TD><TD>server internals (non-exposed stuff)</TD></TR>
 * <TR><TD>1301 - 1400</TD><TD>request preparation (listener, parser, etc.)
 *     </TD></TR>
 * <TR><TD>1401 - 1500</TD><TD>server console messages</TD></TR>
 * <TR><TD>1501 - 1600</TD><TD>remote console</TD></TR>
 * <TR><TD>1601 - 1700</TD><TD>content editor processing errors</TD></TR>
 * <TR><TD>1701 - 2000</TD><TD>-unassigned-</TD></TR>
 * </TABLE>
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public interface IPSServerErrors {
   /**
    * This is used to log native (eg, DBMS, LDAP, etc.) error codes/text.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>error code</TD></TR>
    * <TR><TD>1</TD><TD>error text (description)</TD></TR>
    * </TABLE>
    */
   public static final int NATIVE_ERROR               = 1001;

   /**
    * This is simply used to dump raw data associated with an error.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>raw data</TD></TR>
    * </TABLE>
    */
   public static final int RAW_DUMP                   = 1002;

   /**
    * This error is reported by the
    * com.percussion.error.PSDataConversionError object.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>session id</TD></TR>
    * <TR><TD>1</TD><TD>source type</TD></TR>
    * <TR><TD>2</TD><TD>dest type</TD></TR>
    * </TABLE>
    */
   public static final int DATA_CONV_ERROR            = 1003;

   /**
    * a request document was not supplied with the request
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>XML request document type (DTD name)</TD></TR>
    * </TABLE>
    */
   public static final int REQ_DOC_MISSING            = 1004;

   /**
    * the request document sent is not of the expected type
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the document type (DTD) expected</TD></TR>
    * <TR><TD>1</TD><TD>the document type (DTD) specified</TD></TR>
    * </TABLE>
    */
   public static final int REQ_DOC_INVALID_TYPE       = 1005;

   /**
    * at least one argument passed into a method is out of the range.
    * This is used by com.percussion.error.PSIllegalArgumentException
    * Example: Expect myMethod(int a) and a < 5, but use myMethod(6).
    * Example: Expect myMethod(String name) and name is either John
    * or Doe, but use myMethod(JohnDoe).
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the description of the error</TD></TR>
    * <TR><TD>1</TD><TD>the name of the method</TD></TR>
    * </TABLE>
    */
   public static final int ARGUMENT_OUT_OF_RANGE      = 1006;

   /**
    * at least one argument passed into a method is not acceptable.
    * This is used by com.percussion.error.PSIllegalArgumentException
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the description of the error</TD></TR>
    * <TR><TD>1</TD><TD>the name of the method</TD></TR>
    * </TABLE>
    */
   public static final int ARGUMENT_ERROR      = 1007;

   /**
    * an unexpected error occurred while processing the request
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the requestor's session id</TD></TR>
    * </TABLE>
    */
   public static final int UNKNOWN_PROCESSING_ERROR   = 1008;

   /**
    * an unexpected error occurred while sending a response
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the requestor's session id (or null if not known)</TD></TR>
    * <TR><TD>1</TD><TD>a description of the error</TD></TR>
    * </TABLE>
    */
   public static final int RESPONSE_SEND_ERROR        = 1009;

   /**
    * A redirect url is too long for Internet Explorer.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The URL</TD></TR>
    * <TR><TD>1</TD><TD>The length allowed</TD></TR>
    * </TABLE>
    */
   public static final int REDIRECT_URL_TOO_LONG = 1010;

   /**
    * When a PSLogError is wrapped in a PSErrorException, use this code.
    * It contains a placeholder for the messages generated by the log error.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The text of the log error</TD></TR>
    * <TR><TD>1</TD><TD>The error code of the log error</TD></TR>
    * </TABLE>
    */
   public static final int WRAPPED_LOG_ERROR = 1011;

   /**
    * The fully qualified host name could not be determined.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The host name used instead.</TD></TR>
    * </TABLE>
    */
   public static final int UNKNOWN_HOST = 1012;
   
   /**
    * The requested server lock could not be acquired.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The list of resources requested by the lock.</TD></TR>
    * <TR><TD>1</TD><TD>The list of conflicting locks.</TD></TR>
    * </TABLE>
    */
   public static final int SERVER_LOCK_NOT_ACQUIRED = 1013;
   

   /**
    * A serious problem occurred while modifying data in an SQL table.
    * 
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The affected table</TD></TR>
    * </TABLE>
    */
   public static final int SQL_PROBLEM = 1014;
   
   /**
    * The Server sets a FileLock while running. An error message if a 
    * server failed to acquire a lock.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Exception message</TD></TR>
    * </TABLE>
    */
   public static final int RUNNING_SERVER_LOCK_NOT_ACQUIRED = 1015;

   /**
    * This error is reported by the
    * com.percussion.error.PSApplicationAuthorizationError object.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>host address</TD></TR>
    * <TR><TD>1</TD><TD>login id</TD></TR>
    * </TABLE>
    */
   public static final int AUTHORIZATION_ERROR        = 1101;

   /**
    * This error is reported by the
    * com.percussion.error.PSPoorResponseTimeError object.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>session id</TD></TR>
    * <TR><TD>1</TD><TD>response time (in seconds)</TD></TR>
    * </TABLE>
    */
   public static final int POOR_RESPONSE_TIME         = 1102;

   /**
    * This error is reported by the
    * com.percussion.error.PSLargeApplicationRequestQueueError object.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>session id</TD></TR>
    * <TR><TD>1</TD><TD>max queue size</TD></TR>
    * </TABLE>
    */
   public static final int REQUEST_QUEUE_FULL         = 1103;

   /**
    * This error is reported by the
    *  com.percussion.error.PSValidationError object.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>session id</TD></TR>
    * </TABLE>
    */
   public static final int VALIDATION_ERROR           = 1104;

   /**
    * Used when the user has attempted and failed too many login attempts.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>session id</TD></TR>
    * </TABLE>
    */
   public static final int TOO_MANY_LOGIN_ATTEMPTS    = 1105;

   /**
    * Thrown during app initialization for an empty data set (no pipes).
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>data set name</TD></TR>
    * <TR><TD>1</TD><TD>application name</TD></TR>
    * </TABLE>
    */
   public static final int EMPTY_DATASET              = 1106;

   /**
    * Thrown during app initialization for no data set definitions.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>application name</TD></TR>
    * </TABLE>
    */
   public static final int NO_DATASET_DEFINITIONS     = 1107;

   /**
    * This error is reported by the
    * com.percussion.error.PSRequestPreProcessingError object.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>host address</TD></TR>
    * </TABLE>
    */
   public static final int REQUEST_PREPROC_ERROR      = 1108;

   /**
    * This error is reported by the PSApplicationHandler constructor if
    * the specified application is NULL
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int NULL_APPLICATION_ERROR     = 1109;

   /**
    * The data set associated with the request was not found.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>session id</TD></TR>
    * <TR><TD>1</TD><TD>URL</TD></TR>
    * <TR><TD>2</TD><TD>app name</TD></TR>
    * </TABLE>
    */
   public static final int APP_DATASET_NOT_FOUND      = 1110;

   /**
    * The data set handler associated with the request was not found.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>session id</TD></TR>
    * <TR><TD>1</TD><TD>app name</TD></TR>
    * <TR><TD>2</TD><TD>data set name</TD></TR>
    * <TR><TD>3</TD><TD>request type</TD></TR>
    * </TABLE>
    */
   public static final int APP_DATASET_HANDLER_NOT_FOUND = 1111;

   /**
    * The data set is not valid.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>data set name</TD></TR>
    * <TR><TD>1</TD><TD>app name</TD></TR>
    * <TR><TD>2</TD><TD>description</TD></TR>
    * </TABLE>
    */
   public static final int APP_DATASET_INVALID        = 1112;

   /**
    * No query pipes were found in the data set.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>data set name</TD></TR>
    * <TR><TD>1</TD><TD>app name</TD></TR>
    * </TABLE>
    */
   public static final int APP_NO_QUERY_PIPES_IN_DATASET    = 1113;

   /**
    * No update pipes were found in the data set.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>data set name</TD></TR>
    * <TR><TD>1</TD><TD>app name</TD></TR>
    * </TABLE>
    */
   public static final int APP_NO_UPDATE_PIPES_IN_DATASET   = 1114;

   /**
    * the requested data set was not found
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>data set name</TD></TR>
    * <TR><TD>1</TD><TD>app name</TD></TR>
    * </TABLE>
    */
   public static final int APP_DATASET_DEF_NOT_FOUND  = 1115;

   /**
    * an exception occurred attempting to load the login page
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>app name</TD></TR>
    * <TR><TD>1</TD><TD>login page URL</TD></TR>
    * <TR><TD>1</TD><TD>exception text</TD></TR>
    * </TABLE>
    */
   public static final int APP_LOGIN_PAGE_EXCEPTION   = 1116;

   /**
    * an exception occurred defining the role entry
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>1</TD><TD>exception text</TD></TR>
    * </TABLE>
    */
   public static final int DEFINE_ROLE_ENTRY_EXCEPTION   = 1117;

   /**
    * an exception occurred starting the application
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>app name</TD></TR>
    * <TR><TD>1</TD><TD>exception text</TD></TR>
    * </TABLE>
    */
   public static final int APP_START_EXCEPTION  = 1118;

   /**
    * The specified validation criteria was not met.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>validation criteria</TD></TR>
    * </TABLE>
    */
   public static final int VALIDATION_RULES_NOT_MET   = 1119;

   /**
    * Used when the user has been authenticated, but he/she doesn't have
    * access for the requested action.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>login id</TD></TR>
    * </TABLE>
    */
   public static final int NO_AUTHORIZATION          = 1120;


   /**
    * Error loading the Content Editor system def
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Exception text</TD></TR>
    * </TABLE>
    */
   public static final int CE_SYSTEM_DEF_LOAD = 1121;

   /**
    * Error loading a Content Editor shared def
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The filename</TD></TR>
    * <TR><TD>1</TD><TD>Exception text</TD></TR>
    * </TABLE>
    */
   public static final int CE_SHARED_DEF_LOAD = 1122;

   /**
    * The file requested in an internal lookup request does not exist.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The filename</TD></TR>
    * </TABLE>
    */
   public static final int APP_FILE_DOES_NOT_EXIST = 1123;

   /**
    * Only XML files are supported in internal requests.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The filename</TD></TR>
    * </TABLE>
    */
   public static final int APP_ONLY_XML_FILES_SUPPORTED = 1124;

   /**
    * Warn the user that we added missing system mandatory fields automatically.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the list of added field names</TD></TR>
    * </TABLE>
    */
   public static final int APP_ADDED_SYSTEM_MANDATORY_FIELDS = 1125;

   /**
    * The action set handler was not supplied with its required parameters.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Argument</th><th>Description</th></tr>
    * <tr><td>0</td><td>content id label</td></tr>
    * <tr><td>1</td><td>contenttype id label</td></tr>
    * </table>
    */
   public static final int ACTION_SET_MISSING_REQUIRED_PARAMS = 1176;


   /**
    * The action set handler could not determine a content editor URL from the
    * supplied parameters.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Argument</th><th>Description</th></tr>
    * <tr><td>0</td><td>content id label</td></tr>
    * <tr><td>1</td><td>Supplied content id</td></tr>
    * <tr><td>2</td><td>contenttype id label</td></tr>
    * <tr><td>3</td><td>Supplied contenttype id</td></tr>
    * </table>
    */
   public static final int ACTION_SET_COULD_NOT_DETERMINE_CE = 1177;

   /**
    * The URL specified by the action could not be resolved to a content editor
    * resource.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Argument</th><th>Description</th></tr>
    * <tr><td>0</td><td>the name of the action that failed</td></tr>
    * <tr><td>1</td><td>the URL that failed</td></tr>
    * </table>
    */
   public static final int ACTION_SET_ACTION_NOT_FOUND = 1178;

   /**
    * The replacement value for the stylesheet URL did not resolve.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Argument</th><th>Description</th></tr>
    * <tr><td>0</td><td>the stylesheet object that caused the failure</td></tr>
    * </table>
    */
   public static final int ACTION_SET_INVALID_STYLESHEET = 1179;


   /**
    * An extension provided for use with an action is invalid.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Argument</th><th>Description</th></tr>
    * <tr><td>0</td><td>reference of the extension that caused the failure</td>
    * </tr>
    * <tr><td>1</td><td>name of the action with which the extension is
    * associated</td></tr>
    * </table>
    */
   public static final int ACTION_SET_INVALID_EXTENSION = 1180;

   /**
    * An action set with the same name as an existing action set was loaded.
    * Action set names must be unique.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Argument</th><th>Description</th></tr>
    * <tr><td>0</td><td>name of the duplicated action set</td></tr>
    * </table>
    */
   public static final int ACTION_SET_DUPLICATE_NAME = 1181;

   /**
    * The application used to determine the content editor URL could not be
    * found.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Argument</th><th>Description</th></tr>
    * <tr><td>0</td><td>name of the cataloging application</td></tr>
    * </table>
    */
   public static final int ACTION_SET_MISSING_CATALOGER = 1182;

   /**
    * An action with the same name as an existing action within the same
    * action set was defined.  Action names must be unique within a set.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Argument</th><th>Description</th></tr>
    * <tr><td>0</td><td>name of the duplicated action</td></tr>
    * <tr><td>0</td><td>name of the action set</td></tr>
    * </table>
    */
   public static final int ACTION_DUPLICATE_NAME = 1183;

   /**
    * The configuration file is <code>null</code> of empty for a loadable
    * handler.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Argument</th><th>Description</th></tr>
    * <tr><td>0</td><td>name of handler</td></tr>
    * </table>
    */
   public static final int LOADABLE_HANDLER_CONFIGURATION_FILE_IS_NULL = 1184;

   /**
    * The configuration file is invalid or malformed for a loadable handler.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Argument</th><th>Description</th></tr>
    * <tr><td>0</td><td>name of handler</td></tr>
    * <tr><td>1</td><td>message</td></tr>
    * </table>
    */
   public static final int LOADABLE_HANDLER_CONFIGURATION_FILE_IS_INVALID = 1185;

   /**
    * An unexpected exception occurred in a loadable handler.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Argument</th><th>Description</th></tr>
    * <tr><td>0</td><td>name of handler</td></tr>
    * <tr><td>1</td><td>message</td></tr>
    * </table>
    */
   public static final int LOADABLE_HANDLER_UNEXPECTED_EXCEPTION = 1186;

   /**
    * The server is shutting down.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int SERVER_SHUTDOWN_MSG        = 1201;

   /**
    * The server is shutting down due to a fatal error.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int FATAL_SERVER_ERROR_MSG     = 1202;

   /**
    * When the core server catches an exception in it's catch-all
    * (for unforeseen exceptions) this is used to log it as it ends up
    * killing us.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Exception.getMessage()</TD></TR>
    * </TABLE>
    */
   public static final int EXCEPTION_NOT_CAUGHT       = 1203;

   /**
    * Logged message when the server can't load a config file.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Exception.getMessage()</TD></TR>
    * </TABLE>
    */
   public static final int LOAD_CONFIG_FAILURE        = 1204;

   /**
    * Logged message when PSLogManager init fails due to bad config info.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Exception.getMessage()</TD></TR>
    * </TABLE>
    */
   public static final int LOG_INIT_BAD_CONFIG        = 1205;

   /**
    * Logged message when PSLogManager init fails due to I/O error.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Exception.getMessage()</TD></TR>
    * </TABLE>
    */
   public static final int LOG_INIT_FILE_ERROR        = 1206;

   /**
    * Logged message when PSLogManager init fails due to JDBC (SQL) error.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Exception.getMessage()</TD></TR>
    * </TABLE>
    */
   public static final int LOG_INIT_SQL_ERROR         = 1207;

   /**
    * Logged message when PSLogManager init fails due to a missing JDBC
    * driver.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Exception.getMessage()</TD></TR>
    * </TABLE>
    */
   public static final int LOG_INIT_DRIVER_ERROR      = 1208;

   /**
    * The server is shutting down due to an interrupt signal (eg, CTRL-C).
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int SERVER_INTERRUPT_CAUGHT    = 1209;

   /**
    * Initialization of the object store failed.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Exception.getMessage()</TD></TR>
    * </TABLE>
    */
   public static final int OBJECT_STORE_INIT_FAILED   = 1210;

   /**
    * Logged message when PSLogManager init fails due to SAXException.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>SAXException.getMessage()</TD></TR>
    * </TABLE>
    */
   public static final int LOG_INIT_XML_FILE_ERROR    = 1211;

   /**
    * An internal error (end-condition) was encountered.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int INTERNAL_SERVER_ERROR_MSG  = 1212;

   /**
    * An exception occurred while waiting for a user thread to come
    * available.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the exception text</TD></TR>
    * </TABLE>
    */
   public static final int USER_THR_POOL_WAIT_EXCEPTION  = 1213;

   /**
    * Stream state errors: <p>
    * For each error, an operation was requested that expected the stream to be
    * in a certain state, which it wasn't.
    * <p>
    * No arguments are passed in for these messages.
    */
   public static final int STREAM_NOT_OPENED    = 1214;
   public static final int STREAM_ALREADY_OPENED   = 1215;
   public static final int STREAM_ALREADY_CLOSED   = 1216;

   /**
    * A connection is requested when the event queue manager is null.
    * <p>
    * No arguments are passed in for these messages.
    */
   public static final int EVENT_QUEUEMGR_NULL     = 1217;

   /**
    * An exception occurred which would have terminated a running user
    * thread, but we caught it and merely logged it.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the exception text</TD></TR>
    * </TABLE>
    */
   public static final int USER_THR_UNCAUGHT_EXCEPTION = 1218;

   /**
    * Ran out of memory when executing a log read.
    * <p>
    * No arguments are passed in for this message.
   */
   public static final int LOG_SIZE_TOO_BIG = 1219;

   /**
    * The server is currently unavailable.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int SERVER_UNAVAILABLE_ERROR_MSG = 1220;

   /**
    * The server is still coming up.  This request may not have initialized yet.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int SERVER_INITIALIZING_ERROR_MSG = 1221;


   /**
    * An uncaught exception that would have terminated a daemon thread occurred.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Name of the daemon thread</TD></TR>
    * <TR><TD>1</TD><TD>Text of the exception</TD></TR>
    * </TABLE>
    */
   public static final int DAEMON_THR_UNCAUGHT_EXCEPTION = 1222;

   /**
    * An unexpected exception caused the server brand validator to fail.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Text of the exception</TD></TR>
    * </TABLE>
    */
   public static final int SERVER_BRAND_VALIDATOR_FAILURE = 1223;


   /**
    * An unexpected exception occurred while performing some cache operation.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Text of the exception</TD></TR>
    * </TABLE>
    */
   public static final int CACHE_UNEXPECTED_EXCEPTION = 1224;

   /**
    * Cache failed to start.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Text of the error</TD></TR>
    * </TABLE>
    */
   public static final int CACHE_NOT_STARTED = 1225;

   /**
    * No internal request handler was found to handle the specified request.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The request page (app/resource)</TD></TR>
    * </TABLE>
    */
   public static final int CACHE_NO_INTERNAL_REQUEST_HANDLER = 1226;

   /**
    * An error occurred making an internal request while performing a cache
    * operation.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The request page (app/resource)</TD></TR>
    * <TR><TD>1</TD><TD>Text of the exception</TD></TR>
    * </TABLE>
    */
   public static final int CACHE_INTERNAL_REQUEST_FAILURE = 1227;

   /**
    * Skipped cache dependency because an invalid relationship was returned.
    *
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The ID of the relationship row</TD></TR>
    * <TR><TD>1</TD><TD>Text of the exception</TD></TR>
    * </TABLE>
    */
   public static final int CACHE_DEPENDENCY_SKIPPED = 1228;

   /**
    * Could not update the current revision from the CONTENTSTATUS table.
    *
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>1</TD><TD>Text of the exception</TD></TR>
    * </TABLE>
    */
   public static final int CACHE_CURRENTREVISION_UPDATE_FAILURE = 1229;

   /**
    * Could not store cache item to disk.
    *
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>1</TD><TD>The item tried to store.</TD></TR>
    * <TR><TD>2</TD><TD>Text of the exception.</TD></TR>
    * </TABLE>
    */
   public static final int CACHE_STORE_TO_DISK_FAILURE = 1230;

   /**
    * Cache failed to start.
    */
   public static final int CACHE_START_FAILED = 1231;

   /**
    * An error occurred making an internal request while performing a
    * relationship operation.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The request page (app/resource)</TD></TR>
    * <TR><TD>1</TD><TD>Text of the exception</TD></TR>
    * </TABLE>
    */
   public static final int RELATIONSHIP_INTERNAL_REQUEST_FAILURE = 1232;

   /**
    * An internal core request resource is missing. The server cannot start
    * without this.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The request page (app/resource)</TD></TR>
    * <TR><TD>1</TD><TD>Text of the exception</TD></TR>
    * </TABLE>
    */
   public static final int MISSING_INTERNAL_REQUEST_RESOURCE = 1233;

   /**
    * An invalid server configuration was found.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>1</TD><TD>The reason</TD></TR>
    * <TR><TD>0</TD><TD>Text of the exception</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_CONFIGURATION = 1234;

   /**
    * An invalid configuration object for type
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>1</TD><TD>The object type provided</TD></TR>
    * <TR><TD>0</TD><TD>The object type required</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_CONFIG_OBJECT = 1235;

   /**
    * An unknown server configuration.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>1</TD><TD>The reason</TD></TR>
    * </TABLE>
    */
   public static final int UNKNOWN_CONFIGURATION = 1236;

   /**
    * An unknown relationship configuration.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>1</TD><TD>The requested relationship configuration name</TD></TR>
    * </TABLE>
    */
   public static final int UNKNOWN_RELATIONSHIP_CONFIGURATION = 1237;

   /**
    * An unknown clone handler configuration.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>1</TD><TD>The requested clone handler configuration name</TD></TR>
    * </TABLE>
    */
   public static final int UNKNOWN_CLONEHANDLER_CONFIGURATION = 1238;

   /**
    * An unknown error occurred while processing relationship effects.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>1</TD><TD>The error message</TD></TR>
    * </TABLE>
    */
   public static final int UNKNOWN_EFFECT_PROCESSING_ERROR = 1239;

   /**
    * A status message reporting that an effect failed.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>1</TD><TD>The effect name that failed.</TD></TR>
    * </TABLE>
    */
   public static final int EFFECT_PROCESSING_FAILED = 1240;

   /**
    * The requested relationship already exists for the current item.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>1</TD><TD>The relationship name.</TD></TR>
    * <TR><TD>2</TD><TD>The content id.</TD></TR>
    * </TABLE>
    */
   public static final int RELATIONSHIP_ALREADY_EXISTS = 1241;
   
   /**
    * A rx configuration that is requested to edit, but the configuration is not
    * allowed to edit.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Name of the configuration</TD></TR>
    * </TABLE>
    */
   public static final int CONFIG_NOT_ALLOWED_EDIT = 1242;
   
   /**
    * A rx configuration that is requested to edit, but the configuration is 
    * already locked by some other user.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Name of the configuration</TD></TR>
    * <TR><TD>1</TD><TD>The name of the user currently holding the lock</TD></TR>
    * </TABLE>
    */
   public static final int CONFIG_LOCKED = 1243;
   
   /**
    * A rx configuration that is requested to edit, but the configuration is 
    * already locked by some user in other session.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Name of the configuration</TD></TR>
    * </TABLE>
    */
   public static final int CONFIG_LOCKED_SAME = 1244;
   
   /**
    * A workflow transition was rejected through a strong dependency effect.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Te rejected transition</TD></TR>
    * </TABLE>
    */
   public static final int TRANSITION_REJECTED = 1245;


   /**
    * The same type is returned by more than one cache handler
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The type</TD></TR>
    * </TABLE>
    */
   public static final int CACHE_HANDLER_DUPE_TYPE = 1246;
   
   /**
    * Community authentication failed, user not in any valid community.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Current user name</TD></TR>
    * <TR><TD>1</TD><TD>The community id</TD></TR>
    * </TABLE>
    */
   public static final int COMMUNITIES_AUTHENTICATION_FAILED_INVALID_COMMUNITY = 
      1247;

   /**
    * Community authentication failed, unexpected error.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The error text</TD></TR>
    * </TABLE>
    */
   public static final int COMMUNITIES_AUTHENTICATION_FAILED_ERROR = 
      1248;

   /**
    * Load configurations failed, unexpected error.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The error text</TD></TR>
    * </TABLE>
    */
   public static final int ERROR_LOAD_CONFIGS = 1249;
   
   /**
    * Update configuration failed, unexpected error.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The to be updated configuration name</TD></TR>
    * <TR><TD>1</TD><TD>The error text</TD></TR>
    * </TABLE>
    */
   public static final int ERROR_UPDATE_CONFIG = 1250;
   
   /**
    * PSRequestParser throws this when the request line is invalid.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>request line</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_REQUEST_LINE       = 1301;

   /**
    * PSRequestParser throws this when the request method is bad.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>request method</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_REQUEST_METHOD     = 1302;

   /**
    * PSRequestParser throws this when the content length is non-numeric.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>content length</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_CONTENT_LENGTH     = 1303;

   /**
    * PSRequestParser throws this when the content type is not supported.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>content type</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_CONTENT_TYPE       = 1304;

   /**
    * PS*ContentParser throws this when the content type is not supported.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>content type</TD></TR>
    * <TR><TD>0</TD><TD>supported content type(s)</TD></TR>
    * </TABLE>
    */
   public static final int PARSER_UNSUPPORTED_CONTENT_TYPE  = 1305;

   /**
    * PSXmlContentParser throws this when it catches a SAXException.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>SAXException.getMessage()</TD></TR>
    * </TABLE>
    */
   public static final int XML_PARSER_SAX_ERROR       = 1306;

   /**
    * PSFormContentParser throws this when it encounters a hex char not
    * in %XX format.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>form data</TD></TR>
    * </TABLE>
    */
   public static final int FORM_PARSER_BAD_HEX_CHAR   = 1307;

   /**
    * An appropriate request handler could not be found for the request.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>session id</TD></TR>
    * <TR><TD>1</TD><TD>URL</TD></TR>
    * </TABLE>
    */
   public static final int REQUEST_HANDLER_NOT_FOUND  = 1308;

   /**
    * PSRequestParser throws this when the request page type is not
    * supported.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>request page extension</TD></TR>
    * <TR><TD>1</TD><TD>URL</TD></TR>
    * </TABLE>
    */
   public static final int REQUEST_PAGE_TYPE_ERROR    = 1309;

   /**
    * PSFormContentParser throws this in multipart forms with an invalid
    * Content-Type line.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Content-Type line</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_MULTIPART_CONTENT_TYPE   = 1310;

   /**
    * PSFormContentParser throws this in multipart forms with an invalid
    * Content-Disposition line.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Content-Disposition line</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_MULTIPART_CONTENT_DISP   = 1311;

   /**
    * PSRequestQueue gives this message to remove a request from the queue.
    */
   public static final int REQUEST_WAIT_TOO_LONG   =   1312;

   /**
    * PSRequestQueue gives this message if an invalid value was supplied
    * for request timeout.
    *
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The invalid value</TD></TR>
    * <TR><TD>1</TD><TD>The default value that will be used.</TD></TR>
    * </TABLE>
    */
   public static final int REQUEST_TIMEOUT_INVALID_USING_DEFAULT = 1313;

   /**
    * PSRequestQueue gives this message if an invalid value was supplied
    * for request queue depth.
    *
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The invalid value</TD></TR>
    * <TR><TD>1</TD><TD>The default value that will be used.</TD></TR>
    * </TABLE>
    */
   public static final int REQUEST_QUEUE_DEPTH_INVALID_USING_DEFAULT = 1314;

   /**
    * The content length specified does not match the length of data read.
    *
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>requestors session id</TD></TR>
    * <TR><TD>1</TD><TD>content type</TD></TR>
    * <TR><TD>2</TD><TD>expected length (from headers)</TD></TR>
    * <TR><TD>3</TD><TD>data length actually read</TD></TR>
    * </TABLE>
    */
   public static final int CONTENT_LENGTH_DOES_NOT_MATCH_DATA_READ = 1315;

   /**
    * PSRequestListener throws this when it can't accept a request.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int CAPACITY_REACHED_MSG       = 1330;

   /**
    * PSRequestListener shuts down the server when it can't create a
    * listener.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Exception.getMessage()</TD></TR>
    * </TABLE>
    */
   public static final int LISTENER_DEAD              = 1331;

   /**
    * PSHttpRequestDispatcher throws this when a NULL listener is passed as a
    * param.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int NULL_CONNECTION_LISTENER         = 1332;

   /**
    * Unable to locate the request handler config file
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The file name</TD></TR>
    * </TABLE>
    */
   public static final int REQUEST_HANDLER_CONFIG_NOT_FOUND = 1333;

   
   
   /**
    * An error occurred loading the request handler configuration.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the exception text</TD></TR>
    * </TABLE>
    */
   public static final int REQUEST_HANDLER_CONFIG_ERROR = 1334;

   /**
    * An error occurred creating the loadable request handler.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the class name</TD></TR>
    * <TR><TD>1</TD><TD>the exception text</TD></TR>
    * </TABLE>
    */
   public static final int LOADABLE_REQUEST_HANDLER_CREATE_ERROR = 1335;

   /**
    * server initialization under way
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int SERVER_INIT_START          = 1401;

   /**
    * configuration (properties files) being loaded
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int LOADING_CONFIG             = 1402;

   /**
    * initialization completed
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int SERVER_INIT_END            = 1403;

   /**
    * server shutdown under way
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int SERVER_TERM_START          = 1404;

   /**
    * server shutdown completed
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int SERVER_TERM_END            = 1405;

   /**
    * signal (CTRL-C) handler not installed
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int SIG_HANDLER_NOT_STARTED    = 1406;

   /**
    * DB connection pool being initialized
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int DB_POOL_INIT               = 1411;

   /**
    * DB connection pool being shutdown
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int DB_POOL_TERM               = 1412;

   /**
    * error shutting down DB connection pool
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Exception.getMessage()</TD></TR>
    * </TABLE>
    */
   public static final int DB_POOL_TERM_EXCEPTION     = 1413;

   /**
    * user thread pool being initialized
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int USER_THREAD_POOL_INIT      = 1414;

   /**
    * user thread pool being shutdown
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int USER_THREAD_POOL_TERM      = 1415;

   /**
    * error shutting down user thread pool
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Exception.getMessage()</TD></TR>
    * </TABLE>
    */
   public static final int USER_THREAD_POOL_TERM_EXCEPTION  = 1416;

   /**
    * object store being initialized
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int OBJECT_STORE_INIT          = 1417;

   /**
    * object store being shutdown
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int OBJECT_STORE_TERM          = 1418;

   /**
    * error shutting down object store
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Exception.getMessage()</TD></TR>
    * </TABLE>
    */
   public static final int OBJECT_STORE_TERM_EXCEPTION   = 1419;

   /**
    * request handlers being initialized
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int REQ_HANDLER_INIT           = 1420;

   /**
    * request handlers being shutdown
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int REQ_HANDLER_TERM           = 1421;

   /**
    * error shutting down request handlers
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Exception.getMessage()</TD></TR>
    * </TABLE>
    */
   public static final int REQ_HANDLER_TERM_EXCEPTION = 1422;

   /**
    * log manager being initialized
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int LOG_MGR_INIT               = 1423;

   /**
    * log manager being shutdown
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int LOG_MGR_TERM               = 1424;

   /**
    * error shutting down log manager
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Exception.getMessage()</TD></TR>
    * </TABLE>
    */
   public static final int LOG_MGR_TERM_EXCEPTION     = 1425;

   /**
    * error manager being initialized
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int ERROR_MGR_INIT             = 1426;

   /**
    * error manager being shutdown
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int ERROR_MGR_TERM             = 1427;

   /**
    * error shutting down error manager
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Exception.getMessage()</TD></TR>
    * </TABLE>
    */
   public static final int ERROR_MGR_TERM_EXCEPTION   = 1428;

   /**
    * request queue being initialized
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int REQ_QUEUE_INIT             = 1429;

   /**
    * request queue being shutdown
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int REQ_QUEUE_TERM             = 1430;

   /**
    * error shutting down request queue
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Exception.getMessage()</TD></TR>
    * </TABLE>
    */
   public static final int REQ_QUEUE_TERM_EXCEPTION   = 1431;

   /**
    * request listeners being initialized
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int REQ_LISTENER_INIT          = 1432;

   /**
    * request listeners being shutdown
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int REQ_LISTENER_TERM          = 1433;

   /**
    * error shutting down request listeners
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Exception.getMessage()</TD></TR>
    * </TABLE>
    */
   public static final int REQ_LISTENER_TERM_EXCEPTION   = 1434;

   /**
    * initializing an application
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>application name</TD></TR>
    * </TABLE>
    */
   public static final int APPLICATION_INIT              = 1435;

   /**
    * error occurred initializing an application
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>application name</TD></TR>
    * <TR><TD>1</TD><TD>error text</TD></TR>
    * </TABLE>
    */
   public static final int APPLICATION_INIT_EXCEPTION    = 1436;

   /**
    * initializing an application
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>application name</TD></TR>
    * </TABLE>
    */
   public static final int APPLICATION_TERM              = 1437;

   /**
    * security provider pool being initialized
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int SEC_POOL_INIT                 = 1438;

   /**
    * security provider pool being shutdown
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int SEC_POOL_TERM                 = 1439;

   /**
    * error shutting down security provider pool
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Exception.getMessage()</TD></TR>
    * </TABLE>
    */
   public static final int SEC_POOL_TERM_EXCEPTION       = 1440;

   /**
    * error initializing request queue
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Exception.toString()</TD></TR>
    * </TABLE>
    */
   public static final int REQ_QUEUE_INIT_EXCEPTION      = 1441;

   /**
    * initialization of the application completed
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>application name</TD></TR>
    * </TABLE>
    */
   public static final int APPLICATION_INIT_COMPLETED    = 1442;

   /**
    * error initializing extension manager
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Exception.getMessage()</TD></TR>
    * </TABLE>
    */
   public static final int EXTENSION_MGR_INIT_EXCEPTION = 1443;

   /**
    * error shutting down extension manager
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Exception.getMessage()</TD></TR>
    * </TABLE>
    */
   public static final int EXTENSION_MGR_TERM_EXCEPTION = 1444;

   /**
    * warning that server is interpreting URL's case insensitive
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int SERVER_INIT_CASE_INSENSITIVE_URLS = 1445;

   /**
    * An unexpected exception occurred. This message is used to be printed to
    * the server console.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The localized message of the exception.</TD></TR>
    * </TABLE>
    */
   public static final int UNEXPECTED_EXCEPTION_CONSOLE = 1446;

   /**
    * An unexpected exception occurred. This message is used to be logged to
    * the server log.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The error message.</TD></TR>
    * <TR><TD>1</TD><TD>The exception call-stack.</TD></TR>
    * </TABLE>
    */
   public static final int UNEXPECTED_EXCEPTION_LOG = 1447;

   /**
    * Warning printed on the console if the system has more processors than
    * the number of processors supported by the brand code.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The number of processors for which Rhythmyx is licensed.</TD></TR>
    * <TR><TD>1</TD><TD>The number of processors in the system.</TD></TR>
    * <TR><TD>2</TD><TD>The number of processors for which Rhythmyx is licensed.</TD></TR>
    * </TABLE>
    */
   public static final int PROCESSOR_VALIDATION_ERROR = 1448;

   /**
    * Macros being initialized
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int MACROS_INIT = 1449;

   /**
    * The defined user macro definition file is invalid.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The user macro file name.</TD></TR>
    * <TR><TD>0</TD><TD>The exception message.</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_USER_MACROS = 1450;
   
   /**
    * Initialized and started the lock manager console message.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int LOCK_MANAGER_INITIALIZED = 1451;

   /**
    * Invalid control file, extension
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The file path</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_CTRL_FILE_EXT = 1452;
   
   /**
    * Invalid control file, missing control definition
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The file path</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_CTRL_FILE_MISSING_CTRL = 1453;
   
   /**
    * Invalid control file, multiple control definitions
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The file path</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_CTRL_FILE_MULT_CTRLS = 1454;
   
   /**
    * Invalid control file, name
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The file path</TD></TR>
    * <TR><TD>1</TD><TD>The control name</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_CTRL_FILE_NAME = 1455;
   
   /**
    * PSRemoteConsole constructor was called with null as the conn object
    * <p>
    * No arguments are passed in for this message.
    *
    */
   public static final int RCONSOLE_CONN_OBJ_NULL     = 1501;

   /**
    * PSRemoteConsole.execute was called with null or an empty string
    * <p>
    * No arguments are passed in for this message.
    *
    */
   public static final int RCONSOLE_CMD_EMPTY         = 1502;

   /**
    * An invalid console command was specified.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the console command specified</TD></TR>
    * <TR><TD>1</TD><TD>the list of valid commands</TD></TR>
    * </TABLE>
    */
   public static final int RCONSOLE_INVALID_CMD       = 1503;

   /**
    * An invalid subcommand was used for the specified console command.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the console command specified</TD></TR>
    * <TR><TD>1</TD><TD>the invalid subcommand specified</TD></TR>
    * <TR><TD>2</TD><TD>the list of valid subcommands</TD></TR>
    * </TABLE>
    */
   public static final int RCONSOLE_INVALID_SUBCMD    = 1504;

   /**
    * A subcommand is required for the specified console command but
    * none was specified.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the console command specified</TD></TR>
    * <TR><TD>1</TD><TD>the list of valid subcommands</TD></TR>
    * </TABLE>
    */
   public static final int RCONSOLE_SUBCMD_REQD       = 1505;

   /**
    * An argument was specified where one was not expected
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the console command specified</TD></TR>
    * <TR><TD>1</TD><TD>the unexpected argument</TD></TR>
    * </TABLE>
    */
   public static final int RCONSOLE_UNEXPECTED_ARGS   = 1506;

   /**
    * The application name must be specified for this command
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the console command specified</TD></TR>
    * </TABLE>
    */
   public static final int RCONSOLE_APP_NAME_REQD     = 1507;

   /**
    * An exception was encountered trying to build the handler
    * for the specified command
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the console command specified</TD></TR>
    * <TR><TD>0</TD><TD>Throwable.getMessage</TD></TR>
    * </TABLE>
    */
   public static final int RCONSOLE_GET_HANDLER_EXCEPTION   = 1508;

   /**
    * The application specified is not active
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the console command specified</TD></TR>
    * <TR><TD>1</TD><TD>the name of the application</TD></TR>
    * </TABLE>
    */
   public static final int RCONSOLE_APP_NOT_ACTIVE    = 1509;

   /**
    * statistics reporting is not enabled for the specified application
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the console command specified</TD></TR>
    * <TR><TD>1</TD><TD>the name of the application</TD></TR>
    * </TABLE>
    */
   public static final int RCONSOLE_APP_STATS_NOT_ENABLED   = 1510;

   /**
    * server shutdown has been scheduled due to STOP SERVER command
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the amount of time the server will stop in</TD></TR>
    * </TABLE>
    */
   public static final int RCONSOLE_SERVER_SHUTDOWN_SCHEDULED  = 1511;

   /**
    * statistics reporting is not enabled for the specified application
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the console command specified</TD></TR>
    * </TABLE>
    */
   public static final int RCONSOLE_SERVER_STATS_NOT_ENABLED   = 1512;

   /**
    * the specified application has been shutdown
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the name of the application</TD></TR>
    * </TABLE>
    */
   public static final int RCONSOLE_APP_SHUTDOWN            = 1513;

   /**
    * the specified application has been started
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the name of the application</TD></TR>
    * </TABLE>
    */
   public static final int RCONSOLE_APP_STARTED             = 1514;

   /**
    * the specified application has been restarted
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the name of the application</TD></TR>
    * </TABLE>
    */
   public static final int RCONSOLE_APP_RESTARTED           = 1515;

   /**
    * An exception was encountered while processing
    * the specified command
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the console command specified</TD></TR>
    * <TR><TD>0</TD><TD>Throwable.getMessage</TD></TR>
    * </TABLE>
    */
   public static final int RCONSOLE_EXEC_EXCEPTION          = 1516;

   /**
    * Invalid arguments were specified.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the console command specified</TD></TR>
    * <TR><TD>0</TD><TD>the invalid argument</TD></TR>
    * </TABLE>
    */
   public static final int RCONSOLE_INVALID_ARGS            = 1517;

   /**
    * An error occurred during getting/parsing the command.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Throwable.getMessage</TD></TR>
    * </TABLE>
    */
   public static final int RCONSOLE_COMMAND_EXCEPTION       = 1518;

   /**
    * Generic remote console command error message
    * <p>
    * There are no arguments passed in for this message.
    */
   public static final int RCONSOLE_COMMAND_ERROR_MSG       = 1519;

   /**
    * Generic hook request error message
    * <p>
    * There are no arguments passed in for this message.
    */
   public static final int HOOK_REQUEST_ERROR_MSG           = 1520;

   /**
    * No input document was specified for the Hook Request.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Throwable.getMessage</TD></TR>
    * </TABLE>
    */
   public static final int HOOK_REQUEST_DOC_REQUIRED        = 1521;

   /**
    * An invocation error occurred in the Hook Request Handler.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Throwable.getMessage</TD></TR>
    * </TABLE>
    */
   public static final int HOOK_REQUEST_INVOCATION_EXCEPTION   = 1522;

   /**
    * An invalid request type was detected in the Hook Request Handler.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Throwable.getMessage</TD></TR>
    * </TABLE>
    */
   public static final int HOOK_REQUEST_INVALID_TYPE        = 1523;

   /**
    * The Hook Request Handler failed to send a response.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Throwable.getMessage</TD></TR>
    * </TABLE>
    */
   public static final int HOOK_REQUEST_RESPONSE_EXCEPTION  = 1524;

   /**
    * The Hook Request detected a null response context.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Throwable.getMessage</TD></TR>
    * </TABLE>
    */
   public static final int HOOK_REQUEST_RESPONSE_NULL       = 1525;

   /**
    * Brand code is invalid
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int INVALID_BRAND_CODE            = 1526;


   /**
    * The product has expired - the eval license ran out
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int PRODUCT_EXPIRED            = 1527;

   /**
    * log dump can't send message if the server config does not exist
    * <p>
    * There are no arguments passed in for this message.
    */
   public static final int RCONSOLE_SRVCONFIG_REQD_FOR_MAILTO  = 1528;

   /**
    * log dump can't send message if the server config does not contain
    * a notifier object
    * <p>
    * There are no arguments passed in for this message.
    */
   public static final int RCONSOLE_NOTIFIER_REQD_FOR_MAILTO   = 1529;

   /**
    * log dump can't send message if the server config does not contain
    * a host name in its notifier object
    * <p>
    * There are no arguments passed in for this message.
    */
   public static final int RCONSOLE_NOTIFIER_HOST_REQD_FOR_MAILTO = 1530;

   /**
    * log dump can't send message if the server config does not contain
    * a from name in its notifier object
    * <p>
    * There are no arguments passed in for this message.
    */
   public static final int RCONSOLE_NOTIFIER_FROM_REQD_FOR_MAILTO = 1531;

   /**
    * Message for 'flush cache [keys]' console command.
    * <p>
    * There are no arguments passed in for this message.
    */
   public static final int RCONSOLE_CACHE_FLUSHED        = 1532;

   /**
    * Message for 'start cache' console command.
    * <p>
    * There are no arguments passed in for this message.
    */
   public static final int RCONSOLE_CACHE_STARTED        = 1533;

   /**
    * Message for 'stop cache' console command.
    * <p>
    * There are no arguments passed in for this message.
    */
   public static final int RCONSOLE_CACHE_STOPPED        = 1534;

   /**
    * Error message (the server is already stopped caching) for 'stop cache'
    * console command.
    * <p>
    * There are no arguments passed in for this message.
    */
   public static final int RCONSOLE_CACHE_ALREADY_STOPPED   = 1535;

   /**
    * Message for 'restart cache' console command.
    * <p>
    * There are no arguments passed in for this message.
    */
   public static final int RCONSOLE_CACHE_RESTARTED         = 1536;

   /**
    * Insufficient number of cache keys provided for flushing cache items.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>1</TD><TD>the required number of cache keys</TD></TR>
    * </TABLE>
    */
   public static final int INSUFFICIENT_NUM_CACHE_KEYS = 1537;

   /**
    * Missing required cache key for flushing cache items.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>1</TD><TD>the missing key name</TD></TR>
    * </TABLE>
    */
   public static final int MISSING_CACHE_KEY = 1538;

   /**
    * Invalid numeric value provided for a cache key for flushing cache items.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>1</TD><TD>the invalid value</TD></TR>
    * <TR><TD>2</TD><TD>cache key name</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_NUMBER_CACHE_KEY   = 1539;

   /**
    * Revision id may not be specified if the contentid is not specified for
    * flushing assembler cache items.
    * <p>
    * No arguments passed for this message.
    */
   public static final int INVALID_REVISION_CACHE_KEY = 1540;

   /**
    * Error code indicates that console cache command
    * (start, stop, restart etc.) failed.
    * <p>
    * No arguments passed for this message.
    */
   public static final int RCONSOLE_UNABLE_TO_EXECUTE_CACHE_COMMAND = 1541;

   /**
    * I18n (TMX) resource bundle reloaded to cache.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int RCONSOLE_I18NRESOURCES_RELOADED = 1542;

   /**
    * Debug mode set to on/off.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>'on'/'off'</TD></TR>
    * </TABLE>
    * One argument is passed in for this message which is the debug mode that
    * can be either 'on' or 'off'
    */
   public static final int RCONSOLE_DEBUG_SETTING = 1543;

   /**
    * The debug mode must be specified for this command
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int RCONSOLE_DEBUGMODE_REQD = 1544;

   /**
    * The generic code for a successfully executed command with no result 
    * text.
    * <p>
    * There are no arguments passed in for this message.
    */
   public static final int RCONSOLE_SUCCESS = 1545;

   /**
    * This console command requires that a content item id be supplied.
    * <p>
    * There are no arguments passed in for this message.
    */
   public static final int RCONSOLE_REQUIRES_CONTENTID = 1546;

   /**
    * The command cannot be carried out for the following reason.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The reason the command can't complete.</TD></TR>
    * </TABLE>
    */
   public static final int RCONSOLE_COMMAND_CANT_RUN = 1547;

   /**
    * The following content types were successfully processed.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>A comma separated list of the content type names.</TD>
    * </TR>
    * </TABLE>
    */
   public static final int RCONSOLE_CONTENT_TYPES_PROCESSED = 1548;

   /**
    * This command is not available because the full text engine is not
    * enabled.
    * <p>
    * There are no arguments passed in for this message.
    */
   public static final int RCONSOLE_FTS_DISABLED = 1549;
   
   /**
    * The following number of items were queued for indexing.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The number of items queued.</TD>
    * </TR>
    * </TABLE>
    */
   public static final int RCONSOLE_ITEMS_INDEXED = 1550;
   
   /**
    * While queuing items for indexing, content types were specified that are
    * not available for indexing.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The number of items queued.</TD>
    * <TR><TD>1</TD><TD>A comma separated list of the unavailable content type 
    * names.</TD>
    * </TR>
    * </TABLE>
    */
   public static final int RCONSOLE_ITEMS_INDEXED_INVALID_CTYPES = 1551;

   /**
    * While querying a cached item from the console, the item id was specified
    * does not exist in the item cache.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The content id of the item.</TD>
    * </TR>
    * </TABLE>
    */
   public static final int CANNOT_FIND_CACHED_ITEMSUMMARY = 1552;

   /**
    * While querying a cached folder relationship from the console, the 
    * specified relationship id does not exist in the folder relationship cache.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The folder relationship id</TD>
    * </TR>
    * </TABLE>
    */
   public static final int CANNOT_FIND_CACHED_FOLDER_RELATIONSHIP = 1553;
   
   /**
    * Message for 'flush foldercache' console command.
    * <p>
    * There are no arguments passed in for this message.
    */
   public static final int RCONSOLE_FOLDERCACHE_FLUSHED        = 1554;
   
   /**
    * The in memory and repository items of search queue are cleared.
    * <p>
    * There are no arguments passed in for this message.
    */
   public static final int RCONSOLE_SEARCH_QUEUE_CLEARED = 1555;
   
   /**
    * While initializing an editor builder, a mapping was supplied which had
    * no control name. All mappings must have a control name.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the dataset</TD></TR>
    * </TABLE>
    */
   public static final int CE_MISSING_CONTROL_NAME = 1601;


   /**
    * While initializing an editor builder, the fieldset referenced by a
    * mapper could not be found.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the referenced fieldset</TD></TR>
    * </TABLE>
    */
   public static final int CE_MISSING_FIELDSET = 1602;


   /**
    * While initializing an editor builder, the requestor for the editor could
    * not be found.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the content editor.</TD></TR>
    * </TABLE>
    */
   public static final int CE_MISSING_REQUESTOR = 1603;


   /**
    * While initializing an editor builder, the requestor did not have the
    * required form action.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the content editor.</TD></TR>
    * </TABLE>
    */
   public static final int CE_MISSING_FORMACTION = 1604;


   /**
    * While initializing an editor builder, an IPSBackEndMapping was supplied,
    * but it didn't implement the IPSReplacementValue interface.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the field containing the mapping.</TD></TR>
    * </TABLE>
    */
   public static final int CE_UNSUPPORTED_MAPPING_TYPE = 1605;


   /**
    * While initializing an editor builder, the mappings for a fieldset were
    * missing.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    *    <TR><TH>Arg</TH><TH>Description</TH></TR>
    *    <TR><TD>0</TD><TD>The name of the fieldset which is missing the
    *       mappings. </TD></TR>
    * </TABLE>
    */
   public static final int CE_MISSING_MAPPINGS = 1606;


   /**
    * While initializing an editor builder, the field named by a display
    * mapping could not be found.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    *    <TR><TH>Arg</TH><TH>Description</TH></TR>
    *    <TR><TD>0</TD><TD>The name of the missing field.</TD></TR>
    *    <TR><TD>1</TD><TD>The label of the mapping referencing this field.</TD></TR>
    * </TABLE>
    */
   public static final int CE_MISSING_FIELD = 1607;


   /**
    * While attempting to create the output document, the result set stack was
    * empty for this fieldset.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    *    <TR><TH>Arg</TH><TH>Description</TH></TR>
    *    <TR><TD>0</TD><TD>The name of the fieldset being processed.</TD></TR>
    * </TABLE>
    */
   public static final int CE_MISSING_RESULTSET = 1608;


   /**
    * A SQLException occurred during processing and was translated into a
    * PSException.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    *    <TR><TH>Arg</TH><TH>Description</TH></TR>
    *    <TR><TD>0</TD><TD>The text from all SQL exceptions in the exception
    *       chain.</TD></TR>
    * </TABLE>
    */
   public static final int CE_SQL_ERRORS = 1609;


   /**
    * When initializing a builder, a page map is required, but one was not
    * supplied.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    *    <TR><TH>Arg</TH><TH>Description</TH></TR>
    *    <TR><TD>0</TD><TD>The name of the fieldset being processed.</TD></TR>
    * </TABLE>
    */
   public static final int CE_MISSING_PAGEMAP = 1610;

   /**
    * When initializing a builder, each mapping that contains a ref to a
    * fieldset must contain a mapper. This mapper will reference another
    * fieldset. This error indicates that fieldset was not found.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    *    <TR><TH>Arg</TH><TH>Description</TH></TR>
    *    <TR><TD>0</TD><TD>The name of the fieldset being processed.</TD></TR>
    *    <TR><TD>1</TD><TD>The name of the child fieldset.</TD></TR>
    * </TABLE>
    */
   public static final int CE_MISSING_CHILDMAPPER = 1611;

   /**
    * When building the page map for all editors, the page map is created
    * first, then the builders are added by looking up an id. If the page id
    * is not found in the map, this error is thrown.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    *    <TR><TH>Arg</TH><TH>Description</TH></TR>
    *    <TR><TD>0</TD><TD>The type of id being used as the key.</TD></TR>
    *    <TR><TD>1</TD><TD>The id that couldn't be found, as a String.</TD></TR>
    * </TABLE>
    */
   public static final int CE_MISSING_PAGEMAP_ENTRY = 1612;

   /**
    * When processing a request, we got a result set, but it had no data.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    *    <TR><TH>Arg</TH><TH>Description</TH></TR>
    *    <TR><TD>0</TD><TD>The name of the fieldset that defined this editor
    *       </TD></TR>
    * </TABLE>
    */
   public static final int CE_NO_DATA_IN_RESULT_SET = 1613;

   /**
    * A string was supplied for a number, but it couldn't be recognized.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    *    <TR><TH>Arg</TH><TH>Description</TH></TR>
    *    <TR><TD>0</TD><TD>The string that we attempted to parse.</TD></TR>
    *    <TR><TD>0</TD><TD>The context of the number.</TD></TR>
    *    <TR><TD>0</TD><TD>The text of the parse exception.</TD></TR>
    * </TABLE>
    */
   public static final int CE_BAD_NUMBER_FORMAT = 1614;

   /**
    * The page id supplied with the request does not identify an existing
    * editor builder.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    *    <TR><TH>Arg</TH><TH>Description</TH></TR>
    *    <TR><TD>0</TD><TD>The value that was passed, as a String.</TD></TR>
    * </TABLE>
    */
   public static final int CE_INVALID_PAGEID = 1615;

   /**
    * The parent editor for the page id supplied with the request identifies
    * more than 1 parent. We're not set up to handle this. Should only happen
    * if the design changes but code isn't updated.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    *    <TR><TH>Arg</TH><TH>Description</TH></TR>
    *    <TR><TD>0</TD><TD>The pageid that was sent, as a String.</TD></TR>
    * </TABLE>
    */
   public static final int CE_AMBIGUOUS_PAGEID = 1616;

   /**
    * The parent editor for the page id supplied with the request could not
    * be found.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    *    <TR><TH>Arg</TH><TH>Description</TH></TR>
    *    <TR><TD>0</TD><TD>The pageid that was sent, as a String.</TD></TR>
    * </TABLE>
    */
   public static final int CE_NO_PARENT = 1617;

   /**
    * A fieldset contained fields from more than 1 user table. This is not
    * supported.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    *    <TR><TH>Arg</TH><TH>Description</TH></TR>
    *    <TR><TD>0</TD><TD>The name of the fieldset.</TD></TR>
    *    <TR><TD>1</TD><TD>The name of the first table.</TD></TR>
    *    <TR><TD>2</TD><TD>The name of the second table.</TD></TR>
    * </TABLE>
    */
   public static final int CE_MULTIPLE_TABLES_NOT_SUPPORTED = 1618;

   /**
    * While trying to create the output document, we found a converter that
    * is not supported by the command handler.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    *    <TR><TH>Arg</TH><TH>Description</TH></TR>
    *    <TR><TD>0</TD><TD>The location of the file that was used to get the
    *       merger object.</TD></TR>
    * </TABLE>
    */
   public static final int CE_UNSUPPORTED_MERGER = 1619;

   /**
    * While trying to build a dataset, a table specified in a fieldset was
    * not found in the tableset list.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    *    <TR><TH>Arg</TH><TH>Description</TH></TR>
    *    <TR><TD>0</TD><TD>The name of the offending table.</TD></TR>
    * </TABLE>
    */
   public static final int CE_MISSING_TABLE = 1620;

   /**
    * A table row builder requires at least 1 backend column.
    * <p>
    * This error has no arguments.
    */
   public static final int CE_BACKEND_COL_REQUIRED = 1621;

   /**
    * The fieldset must have a set of choices defined, but none were found.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    *    <TR><TH>Arg</TH><TH>Description</TH></TR>
    *    <TR><TD>0</TD><TD>The name of the offending fieldset.</TD></TR>
    * </TABLE>
    */
   public static final int CE_MISSING_CHOICESET = 1624;

   /**
    * This fieldset type does not support a choice list, but one was found.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    *    <TR><TH>Arg</TH><TH>Description</TH></TR>
    *    <TR><TD>0</TD><TD>The name of the offending fieldset.</TD></TR>
    * </TABLE>
    */
   public static final int CE_CHOICESET_NOT_SUPPORTED = 1625;

   /**
    * While processing a modify request, a required parameter was found to be
    * invalid.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the parameter</TD></TR>
    * <TR><TD>1</TD><TD>The value of the parameter</TD></TR>
    * </TABLE>
    */
   public static final int CE_MODIFY_INVALID_PARAM = 1626;

   /**
    * A mapping in the content editor definition that is a placeholder for a
    * system def mapping references a system field that has been excluded.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the field</TD></TR>
    * </TABLE>
    */
   public static final int CE_EXCLUDED_SYSTEM_FIELD_MAPPED = 1627;

   /**
    * While processing a request, a required parameter was found to be
    * invalid, either the value was invalid or it was empty or missing.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the parameter</TD></TR>
    * <TR><TD>1</TD><TD>Why it's invalid.</TD></TR>
    * </TABLE>
    */
   public static final int CE_INVALID_PARAM = 1628;

   /**
    * Only existing documents can be previewed. Must supply item id to preview.
    * <p>
    * There are no arguments passed in for this message.
    */
   public static final int CE_CANT_PREVIEW_NEWDOC  = 1629;

   /**
    * A modify step that performs a query and validates the results encountered
    * a validation failure. This should be wrapped by another error with more
    * info.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The column name</TD></TR>
    * <TR><TD>1</TD><TD>The expected value</TD></TR>
    * <TR><TD>2</TD><TD>The received value</TD></TR>
    * </TABLE>
    */
   public static final int CE_MODIFY_VALIDATION_FAIL = 1632;

   /**
    * A modify step that performs a query and validates the results got no
    * rows back from the query. This should be wrapped by another error with
    * more info.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The request name</TD></TR>
    * <TR><TD>1</TD><TD>The first column name to validate</TD></TR>
    * </TABLE>
    */
   public static final int CE_MODIFY_VALIDATION_NO_ROWS = 1633;

   /**
    * A modify step that performs a query and validates the results encountered
    * an error trying to validate. This should be wrapped by another error with
    * more info.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The request name</TD></TR>
    * <TR><TD>1</TD><TD>The error msg</TD></TR>
    * </TABLE>
    */
   public static final int CE_MODIFY_VALIDATION_EXCEPTION = 1634;

   /**
    * Invalid choices lookup extension used.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The extension used</TD></TR>
    * </TABLE>
    */
   public static final int CE_INVALID_CHOICES_LOOKUP_EXTENSION = 1635;

   /**
    * Provided extension for choice lookup's not found.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The extension used</TD></TR>
    * </TABLE>
    */
   public static final int CE_CHOICES_LOOKUP_EXTENSION_NOT_FOUND = 1636;

   /**
    * No rows found to copy for supplied contentid/revisionid.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The content id</TD></TR>
    * <TR><TD>1</TD><TD>The revision id</TD></TR>
    * </TABLE>
    */
   public static final int CE_COPY_REVISION_NOT_FOUND = 1637;

   /**
    * No redirects found in the application flow for a specified command name.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    *    <TR><TH>Arg</TH><TH>Description</TH></TR>
    *    <TR><TD>0</TD><TD>The command name.</TD></TR>
    * </TABLE>
    */
   public static final int CE_MISSING_REDIRECTS = 1638;

   /**
    * No redirects evaluated to true.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    *    <TR><TH>Arg</TH><TH>Description</TH></TR>
    * </TABLE>
    */
   public static final int CE_NO_REDIRECT_URL = 1639;

   /**
    * Trying to update an item that has not been checked out.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int CE_MODIFY_VALIDATION_FAIL_NOT_CHECKOUT = 1640;

   /**
    * When getting the table name from a backend table that was attached to
    * a backend column, it was <code>null</code> or empty.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>(Optional) The name of the column containing the table
    *    </TD></TR>
    * </TABLE>
    */
   public static final int CE_MISSING_TABLE_NAME = 1641;

   /**
    * No view set is defined in the content editor.
    * <p>
    * There are no arguments passed in for this message.
    */
   public static final int CE_VIEW_SET_MISSING = 1644;

   /**
    * Content Editor system def is invalid
    * <p>
    * There are no arguments passed in for this message.
    */
   public static final int CE_SYSTEM_DEF_INVALID   = 1676;

   /**
    * Content Editor shared def is invalid
    * <p>
    * There are no arguments passed in for this message.
    */
   public static final int CE_SHARED_DEF_INVALID   = 1677;

   /**
    * Duplicate tableset alias.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The alias</TD></TR>
    * <TR><TD>1</TD><TD>The first table name</TD></TR>
    * <TR><TD>2</TD><TD>The second table name</TD></TR>
    * </TABLE>
    */
   public static final int CE_TABLE_ALIAS_DUPLICATE   = 1678;

   /**
    * Missing credentials in a table locator.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the table locator</TD></TR>
    * </TABLE>
    */
   public static final int CE_MISSING_CREDENTIALS = 1685;

   /**
    * Creating the choices lookup URL failed.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The base URL</TD></TR>
    * <TR><TD>1</TD><TD>The query parameters</TD></TR>
    * <TR><TD>2</TD><TD>The anchor</TD></TR>
    * </TABLE>
    */
   public static final int CE_INVALID_CHOICES_LOOKUP_URL = 1686;

   /**
    * While creating the binary datasets a duplicate field submit name was
    * found.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The duplicate submit name</TD></TR>
    * </TABLE>
    */
   public static final int CE_DUPLICATE_SUBMIT_NAME = 1687;

   /**
    * A needed content editor application / resource is not running
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The application/resource name</TD></TR>
    * </TABLE>
    */
   public static final int CE_NEEDED_APP_NOT_RUNNING = 1688;

   /**
    * While initializing an editor builder, a mapping was supplied which had
    * no control. All mappings must have a control.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the field</TD></TR>
    * </TABLE>
    */
   public static final int CE_MISSING_CONTROL = 1689;

   /**
    * A data element was requested from a display field builder that should not
    * be shown.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    *    <TR><TD>0</TD><TD>The name of the fieldset being processed.</TD></TR>
    * </TABLE>
    */
   public static final int CE_FIELD_NOT_DISPLAYED = 1690;

   /**
    * The specified default workflow was found in the exclusion list. You cannot
    * exclude the default workflow.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    *    <TR><TD>0</TD><TD>The default workflow id.</TD></TR>
    * </TABLE>
    */
   public static final int CE_DEFAULT_WF_EXCLUDED = 1691;

   /**
    * The specified default workflow was not found in the inclusion list. You
    * must include the default workflow.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    *    <TR><TD>0</TD><TD>The default workflow id.</TD></TR>
    * </TABLE>
    */
   public static final int CE_DEFAULT_WF_NOT_INLUDED = 1692;


   /**
    * All PSFieldSet and PSField names must be unique
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The duplicate field or fieldset name</TD></TR>
    * <TR><TD>1</TD><TD>The name of the dataset</TD></TR>
    * <TR><TD>2</TD><TD>The name of the application</TD></TR>
    * </TABLE>
    */
   public static final int CE_DUPLICATE_FIELD_NAME_ERROR = 1693;

   /**
    * All PSFieldSet and PSField names should be unique
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The duplicate field or fieldset name</TD></TR>
    * <TR><TD>1</TD><TD>The name of the dataset</TD></TR>
    * <TR><TD>2</TD><TD>The name of the application</TD></TR>
    * </TABLE>
    */
   public static final int CE_DUPLICATE_FIELD_NAME_WARNING = 1694;

   /**
    * Duplicate values found for entries in choice list.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int CE_DUPLICATE_CHOICES = 1695;

   /**
    * Trying to update an older edit revision.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int CE_MODIFY_VALIDATION_FAIL_OLD_EDITREVISION = 1696;

   /**
    * <p>Contentid is required for comparision but not supplied</p>
    * <p>The arguments passed in for this message are:</p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>compare document number</TD></TR>
    * </TABLE>
    */
   public static final int COMPARE_CONTENTID_REQUIRED = 1697;

   /**
    * <p>Variantid is required for comparision but not supplied</p>
    * <p>The arguments passed in for this message are:</p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>compare document number</TD></TR>
    * </TABLE>
    */
   public static final int COMPARE_VARIANTID_REQUIRED = 1698;
   /**
    * <p>Revision is required for comparision but not supplied</p>
    * <p>The arguments passed in for this message are:</p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>compare document number</TD></TR>
    * </TABLE>
    */
   public static final int COMPARE_REVISION_REQUIRED = 1699;

   /**
    * HTTP error occurred while getting the asembly page
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int COMPARE_HTTP_CONNECTION_ERROR = 1700;

   /**
    * The application used to determine the assembly URL could not be
    * found.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Argument</th><th>Description</th></tr>
    * <tr><td>0</td><td>name of application</td></tr>
    * </table>
    */
   public static final int COMPARE_IREQ_CANNOTBE_NULL = 1701;

   /**
    * The extension specified in the request URL to determine the assembly URL
    * is not supported
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int COMPARE_IREQ_CALL_EXCEPTION = 1702;
   /**
    * Assembly url is empty for the given variantid
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>variantid</TD></TR>
    * </TABLE>
    */
   public static final int COMPARE_ASSEMBLY_URL_EMPTY = 1703;
   /**
    * Unable to generate fully qualified url for assembly page
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int COMPARE_MALLFORMED_URL_ERROR = 1704;
   /**
    * Error occurred while initializing docurun
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>docuRun Error Code</TD></TR>
    * </TABLE>
    */
   public static final int COMPARE_DOCURUN_INITIALIZATION_ERROR = 1705;
   /**
    * Error occurred while comparing the documents using the docurun
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>docuRun Error Code</TD></TR>
    * </TABLE>
    */
   public static final int COMPARE_DOCURUN_COMPARE_ERROR = 1706;
   

   /**
    * All backend column names used by PSFields should be unique
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The duplicated col name</TD></TR>
    * <TR><TD>1</TD><TD>The field names using the col name</TD></TR>
    * <TR><TD>2</TD><TD>The name of the dataset</TD></TR>
    * <TR><TD>3</TD><TD>The name of the application</TD></TR>
    * </TABLE>
    */
   public static final int CE_DUPLICATE_COL_NAME_WARNING = 1707;
   
   /**
    * An error occurred running a field transformation
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The field name</TD></TR>
    * <TR><TD>1</TD><TD>The error message</TD></TR>
    * </TABLE>
    */
   public static final int FIELD_TRANSFORM_ERROR = 1708;
   
   /**
    * An exception occurred while attempting to run the legacy
    * namespace cleanup. Clean up was skipped.
    * <p>
    *  The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Exception class name</TD></TR>
    * <TR><TD>1</TD><TD>The error message</TD></TR>
    * </TABLE>
    */
   public static final int NORUN_NAMESPACE_CLEANUP_WARNING = 1709; 
}

