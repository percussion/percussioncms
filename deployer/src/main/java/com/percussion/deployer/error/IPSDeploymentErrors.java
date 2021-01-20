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

package com.percussion.deployer.error;

/**
 * The IPSDeploymentErrors inteface is provided as a convenient mechanism
 * for accessing the various deployent related error codes.
 */
public interface IPSDeploymentErrors
{
   /**
    * The deployment version returned by the server is different than the
    * version of the client.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the server version</TD></TR>
    * </TABLE>
    */
   public static final int SERVER_VERSION_INVALID = 1;

   /**
    * The response returned by the server is missing an element.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The request type</TD></TR>
    * <TR><TD>1</TD><TD>The missing element name</TD></TR>
    * </TABLE>
    */
   public static final int SERVER_RESPONSE_ELEMENT_MISSING = 2;

   /**
    * The response returned by the server contains a malformed element.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The request type</TD></TR>
    * <TR><TD>1</TD><TD>The malformed element name</TD></TR>
    * <TR><TD>2</TD><TD>The error text</TD></TR>
    * </TABLE>
    */
   public static final int SERVER_RESPONSE_ELEMENT_INVALID = 3;

   /**
    * Attempt to use connection to server that is not connected.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The server name</TD></TR>
    * </TABLE>
    */
   public static final int NOT_CONNECTED_ERROR = 4;

   /**
    * Unexpected error
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The error message</TD></TR>
    * </TABLE>
    */
   public static final int UNEXPECTED_ERROR = 5;

   /**
    * The response returned by the server contains a malformed element.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The status code</TD></TR>
    * <TR><TD>1</TD><TD>The request type</TD></TR>
    * </TABLE>
    */
   public static final int SERVER_RESPONSE_EMPTY = 6;

   /**
    * The server returned an unexpected error message.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The request type</TD></TR>
    * <TR><TD>1</TD><TD>The status code</TD></TR>
    * <TR><TD>2</TD><TD>The response xml</TD></TR>
    * </TABLE>
    */
   public static final int SERVER_ERROR_RESPONSE = 7;

   /**
    * The server received a request with a null input document.
    * <p>
    * There are no arguments for this message.
    */
   public static final int NULL_INPUT_DOC = 8;

   /**
    * The server failed to retrieve the repository info
    * <p>
    * There are no arguments for this message.
    */
   public static final int NULL_REPOSITORY_INFO = 9;

   /**
    * The server received an invalid request type.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The request type</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_REQUEST_TYPE = 10;

   /**
    * Error writing to the archive file
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The archive file name</TD></TR>
    * <TR><TD>1</TD><TD>The error</TD></TR>
    * </TABLE>
    */
   public static final int ARCHIVE_WRITE_ERROR = 11;

   /**
    * Error reading from the archive file
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The archive file name</TD></TR>
    * <TR><TD>1</TD><TD>The error</TD></TR>
    * </TABLE>
    */
   public static final int ARCHIVE_READ_ERROR = 12;

   /**
    * The required request property was not specified in the catalog call.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the name of the property</TD></TR>
    * </TABLE>
    */
   public static final int CATALOG_REQD_PROP_NOT_SPECIFIED   = 13;

   /**
    * The request received by the server is malformed.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The request element</TD></TR>
    * <TR><TD>1</TD><TD>The error mesage</TD></TR>
    * </TABLE>
    */
   public static final int SERVER_REQUEST_MALFORMED = 14;

   /**
    * The object requested from the server cannot be located.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The object type</TD></TR>
    * <TR><TD>1</TD><TD>The name of the object</TD></TR>
    * </TABLE>
    */
   public static final int SERVER_OBJECT_NOT_FOUND = 15;

   /**
    * The request recieved by the server contained an invalid required
    * parameter.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The param name</TD></TR>
    * <TR><TD>1</TD><TD>The param value</TD></TR>
    * </TABLE>
    */
   public static final int SERVER_REQUEST_PARAM_INVALID = 16;

   /**
    * Failed to instantiate a dependency handler.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The handler class</TD></TR>
    * <TR><TD>1</TD><TD>The error</TD></TR>
    * </TABLE>
    */
   public static final int DEPENDENCY_HANDLER_INIT = 17;

   /**
    * Depedency handler reports a child type not found in the dependency map
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The child type</TD></TR>
    * <TR><TD>1</TD><TD>The parent type</TD></TR>
    * </TABLE>
    */
   public static final int CHILD_DEPENDENCY_TYPE_NOT_FOUND = 18;

   /**
    * Depedency Manager init failure
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The error</TD></TR>
    * </TABLE>
    */
   public static final int DEPENDENCY_MGR_INIT = 19;


   /**
    * Depedency def not found
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The dependency type</TD></TR>
    * </TABLE>
    */
   public static final int DEPENDENCY_DEF_NOT_FOUND = 20;

   /**
    * Cannot get connection to repository
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The error message</TD></TR>
    * </TABLE>
    */
   public static final int REPOSITORY_CONNECTION_ERROR = 21;

   /**
    * Cannot read from or write to the repository
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The error message</TD></TR>
    * </TABLE>
    */
   public static final int REPOSITORY_READ_WRITE_ERROR = 22;

   /**
    * Missing or empty column data in repository
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The table</TD></TR>
    * <TR><TD>1</TD><TD>The column</TD></TR>
    * <TR><TD>2</TD><TD>The value</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_REPOSITORY_COLUMN_VALUE = 23;

   /**
    * Missing id mapping for a dependency to be installed
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The type</TD></TR>
    * <TR><TD>1</TD><TD>The id</TD></TR>
    * <TR><TD>2</TD><TD>The source server</TD></TR>
    * </TABLE>
    */
   public static final int MISSING_ID_MAPPING = 24;

   /**
    * Missing dependency file on install
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The file type</TD></TR>
    * <TR><TD>1</TD><TD>The dependency type</TD></TR>
    * <TR><TD>2</TD><TD>The dependency id</TD></TR>
    * <TR><TD>3</TD><TD>The dependency name</TD></TR>
    * </TABLE>
    */
   public static final int MISSING_DEPENDENCY_FILE = 25;

   /**
    * Invalid dependency file on install
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The file type</TD></TR>
    * <TR><TD>1</TD><TD>The dependency type</TD></TR>
    * <TR><TD>2</TD><TD>The dependency id</TD></TR>
    * <TR><TD>3</TD><TD>The dependency name</TD></TR>
    * <TR><TD>4</TD><TD>The error</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_DEPENDENCY_FILE = 26;

   /**
    * Invalid id mapping to be saved
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The source repository</TD></TR>
    * <TR><TD>1</TD><TD>The source-id of a <code>PSIdMapping</code></TD></TR>
    * <TR><TD>2</TD><TD>The source-name of a <code>PSIdMapping</code></TD></TR>
    * </TABLE>
    */
   public static final int INVALID_SAVED_ID_MAP = 27;

   /**
    * The unexpected extra data when retrieving data from DPL_ID_MAPPING table.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>source repository</TD></TR>
    * <TR><TD>1</TD><TD>table name</TD></TR>
    * </TABLE>
    */
   public static final int UNEXPECTED_EXTRA_ROW = 28;

   /**
    * Unable to find a table
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>table name</TD></TR>
    * </TABLE>
    */
   public static final int UNABLE_FIND_TABLE = 29;

   /**
    * Unable to load ID types
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the dependency key</TD></TR>
    * <TR><TD>1</TD><TD>the error</TD></TR>
    * </TABLE>
    */
   public static final int ID_TYPE_MAP_LOAD = 30;

   /**
    * Server version mismatch
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the archive server version</TD></TR>
    * <TR><TD>1</TD><TD>the target server version</TD></TR>
    * </TABLE>
    */
   public static final int SERVER_VERSION_LOWER = 31;

   /**
    * Server build mismatch
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the archive server build</TD></TR>
    * <TR><TD>1</TD><TD>the target server build</TD></TR>
    * </TABLE>
    */
   public static final int SERVER_VERSION_HIGHER = 32;

   /**
    * Archive ref already exists on the server
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the archive name</TD></TR>
    * </TABLE>
    */
   public static final int ARCHIVE_REF_FOUND = 33;

   /**
    * incomplete idtype mapping found when only complete entries expected
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The type of map's dependency</TD></TR>
    * <TR><TD>1</TD><TD>The id of the map's dependency</TD></TR>
    * <TR><TD>2</TD><TD>The value of the mapping</TD></TR>
    * <TR><TD>3</TD><TD>The mapping's context</TD></TR>
    * </TABLE>
    */
   public static final int INCOMPLETE_ID_TYPE_MAPPING = 34;

   /**
    * Missing row data in repository
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The table</TD></TR>
    * <TR><TD>1</TD><TD>The key col (may be list)</TD></TR>
    * <TR><TD>2</TD><TD>The key value (may be list)</TD></TR>
    * </TABLE>
    */
   public static final int MISSING_REPOSITORY_ROW = 35;

   /**
    * Missing target id in Id mapping on install
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The type</TD></TR>
    * <TR><TD>1</TD><TD>The id</TD></TR>
    * <TR><TD>2</TD><TD>The source server</TD></TR>
    * </TABLE>
    */
   public static final int INCOMPLETE_ID_MAPPING = 36;

   /**
    * Invalid target id in Id mapping on install
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The type</TD></TR>
    * <TR><TD>1</TD><TD>The id</TD></TR>
    * <TR><TD>2</TD><TD>The source server</TD></TR>
    * <TR><TD>3</TD><TD>The target id</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_ID_MAPPING_TARGET = 37;

   /**
    * Unable to find (id) in table
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The object value</TD></TR>
    * <TR><TD>1</TD><TD>The table name</TD></TR>
    * <TR><TD>2</TD><TD>The column name</TD></TR>
    * </TABLE>
    */
   public static final int VALUE_NOT_FOUND_IN_TABLE = 38;

   /**
    * Unable to find child dependency in the current object
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The id of the child dependency</TD></TR>
    * <TR><TD>1</TD><TD>The object type of the child dependency </TD></TR>
    * <TR><TD>2</TD><TD>The id of the current object</TD></TR>
    * <TR><TD>3</TD><TD>The object type of the current object</TD></TR>
    * </TABLE>
    */
   public static final int CHILD_DEP_NOT_FOUND = 39;

   /**
    * No rows to process
    */
   public static final int NO_ROWS_TO_PROCESS = 40;

   /**
    * Wrong dependency file type
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The wrong dependency file type</TD></TR>
    * <TR><TD>1</TD><TD>The expected dependency file type</TD></TR>
    * </TABLE>
    */
   public static final int WRONG_DEPENDENCY_FILE_TYPE = 41;

   /**
    * Id types expected for a dependency, but not found
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The dependency type</TD></TR>
    * <TR><TD>1</TD><TD>The dependency id</TD></TR>
    */
   public static final int MISSING_ID_TYPES = 42;

   /**
    * validation result is expected in import package, but not found
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The package name</TD></TR>
    */
   public static final int MISSING_VALIDATION_RESULTS = 43;

   /**
    * wrong format for the id of a pair id dependency object
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The current id</TD></TR>
    */
   public static final int WRONG_FORMAT_FOR_PAIRID_DEP_ID = 44;

   /**
    * Unable to find object represented by the dependency.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The id of the dependency</TD></TR>
    * <TR><TD>1</TD><TD>The object type of the dependency </TD></TR>
    * <TR><TD>3</TD><TD>The name of the dependency</TD></TR>
    * </TABLE>
    */
   public static final int DEP_OBJECT_NOT_FOUND = 45;

   /**
     * The object was already exclusively locked by someone else.
     * <p>
     * The arguments passed in for this message are:
     * <TABLE BORDER="1">
     * <TR><TH>Arg</TH><TH>Description</TH></TR>
     * <TR><TD>0</TD><TD>The name of the user currently holding the lock</TD>
     * </TR>
     * <TR><TD>1</TD><TD>How many minutes from now the lock will expire if the
     * user does not renew it</TD></TR>
     * </TABLE>
     */
   public static final int LOCK_ALREADY_HELD = 46;

   /**
    * The lock cannot be extended or refreshed, because it is now being held by
    * someone else.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the user currently holding the lock</TD>
    * </TR>
    * <TR><TD>1</TD><TD>How many minutes from now the lock will expire if the
    * user does not renew it</TD></TR>
    * </TABLE>
    */
   public static final int LOCK_NOT_EXTENSIBLE_TAKEN = 47;

   /**
    * Specifies that cannot find data for a given table and filter
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The table name</TD></TR>
    * <TR><TD>1</TD><TD>The filter that used to query the database</TD></TR>
    * </TABLE>
    */
   public static final int CANNOT_FIND_DATA = 49;

   /**
    * The specified directory property in catalog user dependency files request
    * is invalid (Either does not exist or not a directory).
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the supplied directory</TD></TR>
    * </TABLE>
    */
   public static final int CATALOG_INVALID_DIRECTORY_SPECIFIED   = 50;

   /**
    * Max dep count returned by server exceeded.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The max count</TD></TR>
    * </TABLE>
    */
   public static final int MAX_DEP_COUNT_EXCEEDED = 51;

   /**
    * An empty package list was supplied to the server.
    * <p>
    * There are no arguments passed in for this.
    */
   public static final int EMPTY_PACKAGE_LIST = 52;

   /**
    * The lock cannot be extended or refreshed, because it has been taken and
    * released by someone else.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The last user to hold the lock</TD></TR>
    * </TABLE>
    */
   public static final int LOCK_NOT_EXTENSIBLE_TAKEN_RELEASED = 53;

   /**
    * The lock cannot be release due to an error.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The server name</TD></TR>
    * <TR><TD>1</TD><TD>The error message</TD></TR>
    * </TABLE>
    */
   public static final int LOCK_NOT_RELEASED = 54;

   /**
    * Rhythmyx Server is not licensed for Multi-Server Manager.
    * <p>
    * There are no arguments passed in for this.
    */
   public static final int MULTISERVER_MANAGER_DISABLED = 55;

   /**
    * Error loading application file
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The file name</TD></TR>
    * <TR><TD>1</TD><TD>The app name</TD></TR>
    * <TR><TD>2</TD><TD>The error message</TD></TR>
    * </TABLE>
    */
   public static final int APP_FILE_LOAD = 56;
   
   /**
    * The response returned by the server cannot be parsed.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The request type</TD></TR>
    * <TR><TD>1</TD><TD>The error text</TD></TR>
    * </TABLE>
    */
   public static final int SERVER_RESPONSE_PARSE_ERROR = 57;


   /**
    * Rhythmyx Server is not avaliable.
    * <p>
    * There are no arguments passed in for this.
    */
   public static final int SERVER_NOT_AVAILABLE = 58;

   
   /**
    * Error extracting an id from the <code>PSKey</code> of a cms object.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The component name</TD></TR>
    * <TR><TD>1</TD><TD>The component type</TD></TR>
    * </TABLE>
    */
   public static final int EXTRACT_ID_FROM_KEY = 59;
   
   /**
    * Error assigning an id for a new cms object.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The component name</TD></TR>
    * <TR><TD>1</TD><TD>The component type</TD></TR>
    * <TR><TD>1</TD><TD>The error message</TD></TR>
    * </TABLE>
    */
   public static final int ASSIGN_NEW_KEY = 60;

   /**
    * Missing the required data for updating the item summary cache when 
    * installing an item.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The new content id</TD></TR>
    * <TR><TD>1</TD><TD>The column name of the missing data</TD></TR>
    * <TR><TD>2</TD><TD>The table name of the missing data</TD></TR>
    * </TABLE>
    */
   public static final int MISSING_REQUIRED_CACHE_DATA = 61;

   /**
    * Failed to get a numeric data for updating the item summary cache when 
    * installing an item.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The new content id</TD></TR>
    * <TR><TD>1</TD><TD>The table name of the archived data</TD></TR>
    * <TR><TD>2</TD><TD>The exception when converting string to int</TD></TR>
    * </TABLE>
    */
   public static final int FAILED_GET_NUMERIC_CACHED_DATA = 62;
   
   /**
    * Slot definition already exists. Skipping installation of this slot.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The slot id</TD></TR>
    * </TABLE>
    */
  
   public static final int SLOT_DEFINITION_ALREADY_EXISTS = 63;
   
   /**
    * Application definition doesn't exist.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Application Name</TD></TR>
    * </TABLE>
    */
  
   public static final int APP_DEFINITION_DOESNOT_EXIST = 64;
 
   /**
    * Cannot find Dependency Definition with the objectType
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Object type</TD></TR>
    * </TABLE>
    */
   public static final int CANNOT_FIND_DEP_DEF = 65; 
   
   /**
    * Invalid objectType. The Dependency Definition is not deployable and 
    * there is no deployable parent neither.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Object type</TD></TR>
    * </TABLE>
    */
   public static final int DEP_DEF_NOT_DEPLOYABLE = 66; 
   
   /**
    * Cannot find (parent) Dependency Definition with the specified objectType.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Object type</TD></TR>
    * </TABLE>
    */
   public static final int CANNOT_FIND_PARENT_DEP_DEF = 67;
   
   /**
    * The parent Dependency Definition with objectType = "{0}" is not deployable.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Object type</TD></TR>
    * </TABLE>
    */
   public static final int PARENT_DEP_DEF_NOT_DEPLOYABLE=68;
   
   /**
    * The deployable ordered definition does not include all deplyable elements
    */
   public static final int INCOMPLATE_ORDER_DEF=69;
   
   /**
    * The parent of non-deployable dependency definition is not 1
    */
   public static final int INVALID_NUM_PARENT_DEFS=70;
   
   /**
    * The object type of the parent is not expected.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Object type of the invalid parent</TD></TR>
    * <TR><TD>1</TD><TD>Object type of the expected parent</TD></TR>
    * </TABLE>
    */
   public static final int UNEXPECTED_PARENT_TYPE=71;
   
   /**
    * The order definition does not include all child types of the 'Custom' element.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Object type of the "Custom" element</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_NUM_CHILD_DEFS=72;
   
   /**
    * GUID expected to be set for package
    */
   public static final int MISSING_PKG_GUID=73;
   
   /**
    * New package verion is lower then installed version.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the new package version</TD></TR>
    * <TR><TD>1</TD><TD>the installed package version</TD></TR>
    * </TABLE>
    */
   public static final int VERSION_LOWER_THEN_INSTALLED = 74;
   
   /**
    * Package dependencies not installed
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>ArrayList of packages not installed</TD></TR>
    * </TABLE>
    */
   public static final int PKG_DEP_VALIDATION = 76;
   
   /**
    * Package dependencies that have different version then the ones installed
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>ArrayList of packages and versions</TD></TR>
    * </TABLE>
    */
   public static final int PKG_DEP_VERSION_VALIDATION = 77;
   
   /**
    * The specified configuration file does not exist
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>configuration file path</TD></TR>
    * </TABLE>
    */
   public static final int CONFIG_DOES_NOT_EXIST = 78;

   /**
    * Package was created on system
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Package Name</TD></TR>
    * </TABLE>
    */
   public static final int PACKAGE_CREATED_ON_SYSTEM = 79;
   
   /**
    * Control cannot be packaged
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Control Name</TD></TR>
    * </TABLE>
    */
   public static final int CONTROL_NOT_PACKAGEABLE = 80;
   
   /**
    * Unable to connect to server.
    */
   public static final int UNABLE_TO_CONNECT_TO_SERVER = 81;

   /**
    * Failed to create component community associations.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Community Name</TD></TR>
    * </TABLE>
    */ 
   public static final int FAILED_TO_CREATE_COMPONENT_COMMUNITY_ASSNS = 82;
   
}
