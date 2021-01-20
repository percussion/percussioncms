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

package com.percussion.data;


/**
 * The IPSBackEndErrors inteface is provided as a convenient mechanism
 * for accessing the various back-end and data related error codes. The
 * back end error code ranges are:
 * <TABLE BORDER="1">
 * <TR><TH>Range</TH><TH>Component</TH></TR>
 * <TR><TD>5000 - 5100</TD><TD>general errors used all over</TD></TR>
 * <TR><TD>5101 - 5200</TD><TD>connectivity/authorization errors</TD></TR>
 * <TR><TD>5201 - 5400</TD><TD>back-end data processing errors (in IPSDataErrors)</TD></TR>
 * <TR><TD>5401 - 5500</TD><TD>full user activity log messages</TD></TR>
 * <TR><TD>5501 - 5999</TD><TD>-unassigned-</TD></TR>
 * </TABLE>
 *
 * @author      Chad Loder
 * @version      1.0
 * @since      1.0
 */
public interface IPSBackEndErrors {

   /**
    * This error is reported by the
    * com.percussion.error.PSBackEndAuthorizationError object.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>host address</TD></TR>
    * <TR><TD>1</TD><TD>login id</TD></TR>
    * <TR><TD>2</TD><TD>driver</TD></TR>
    * <TR><TD>3</TD><TD>server</TD></TR>
    * </TABLE>
    */
   public static final int AUTHORIZATION_ERROR      = 5001;

   /**
    * This error is reported by the
    * com.percussion.error.PSLargeBackEndRequestQueueError object.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>session id</TD></TR>
    * <TR><TD>1</TD><TD>max queue size</TD></TR>
    * <TR><TD>2</TD><TD>driver</TD></TR>
    * <TR><TD>3</TD><TD>server</TD></TR>
    * </TABLE>
    */
   public static final int REQUEST_QUEUE_FULL      = 5002;

   /**
    * This error is reported by the
    * com.percussion.error.PSBackEndServerDownError object.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>driver</TD></TR>
    * <TR><TD>1</TD><TD>server</TD></TR>
    * </TABLE>
    */
   public static final int SERVER_DOWN_ERROR         = 5003;

   /**
    * the catalog name is not set appropriately after a connect.
    * another attempt to set it will be made
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>connection string</TD></TR>
    * <TR><TD>1</TD><TD>database name</TD></TR>
    * </TABLE>
    */
   public static final int SET_CATALOG_RETRY         = 5004;

   /**
    * the catalog name could not be set appropriately after a connect.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>database name</TD></TR>
    * </TABLE>
    */
   public static final int SET_CATALOG_FAILED      = 5005;

   /**
    * the connection attempt to the specified drver/server was interrupted
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>connect URL (jdbc:driver:server)</TD></TR>
    * </TABLE>
    */
   public static final int CONNECT_INTERRUPTED      = 5006;

   /**
    * an invalid login timeout value was specifed.
    * the JDBC default will be used
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the timeout value specified</TD></TR>
    * </TABLE>
    */
   public static final int LOGIN_TIMEOUT_INVALID_USING_DEFAULT   = 5007;

   /**
    * the specified JDBC driver failed to load
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the driver name</TD></TR>
    * <TR><TD>1</TD><TD>the failure description (e.getMessage)</TD></TR>
    * </TABLE>
    */
   public static final int JDBC_DRIVER_LOAD_FAILED   = 5008;

   /**
    * the specified JDBC driver's class was not found
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the driver name</TD></TR>
    * <TR><TD>1</TD><TD>the driver's class name</TD></TR>
    * </TABLE>
    */
   public static final int JDBC_CLASS_NOT_FOUND      = 5009;

   /**
    * the idle connection could not be released due to a lost monitor
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the driver/server name</TD></TR>
    * </TABLE>
    */
   public static final int CONN_RELEASE_MONITOR_LOST   = 5010;

   /**
    * a SQL statement cannot be built as no back-ends were specified
    * <p>
    * No arguments.
    */
   public static final int SQL_BUILDER_NO_BACK_ENDS   = 5011;

   /**
    * a SQL statement cannot be built as no back-end tables were specified
    * <p>
    * No arguments.
    */
   public static final int SQL_BUILDER_NO_BACK_END_TABLES   = 5012;

   /**
    * a SQL statement cannot be built as no connection is defined for
    * this back-end
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the server key</TD></TR>
    * </TABLE>
    */
   public static final int SQL_BUILDER_NO_CONN_DEFINED   = 5013;

   /**
    * an exception occurred getting the data types for a SQL statement's
    * placeholders. Strings will be used, which may cause errors later.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the datasource name</TD></TR>
    * <TR><TD>1</TD><TD>the table name</TD></TR>
    * <TR><TD>2</TD><TD>Throwable.toString()</TD></TR>
    * </TABLE>
    */
   public static final int SQL_BUILDER_GET_DATATYPE_EXCEPTION   = 5014;

   /**
    * a SQL statement cannot be built as the placeholder is not
    * properly terminated
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the bad placeholder</TD></TR>
    * </TABLE>
    */
   public static final int SQL_BUILDER_VAR_NOT_TERMINATED   = 5015;

   /**
    * a SQL statement cannot be built as back-end column in the
    * data mapper is null
    * <p>
    * No arguments.
    */
   public static final int SQL_BUILDER_NO_BECOL_IN_MAP   = 5016;

   /**
    * a SQL statement cannot be built as back-end column in the
    * data mapper does not contain any select columns
    * <p>
    * No arguments.
    */
   public static final int SQL_BUILDER_NO_SELECT_COLS_IN_BECOL   = 5017;

   /**
    * the name of the back-end column specified for ORDER BY is null
    * <p>
    * No arguments.
    */
   public static final int SQL_BUILDER_ORDER_BY_COL_NULL   = 5018;

   /**
    * the execution plan cannot be built for a null application handler
    * <p>
    * No arguments.
    */
   public static final int EXEC_PLAN_APP_HANDLER_NULL   = 5019;

   /**
    * the execution plan cannot be built for a null data set
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the application name</TD></TR>
    * </TABLE>
    */
   public static final int EXEC_PLAN_DATA_SET_NULL      = 5020;

   /**
    * the execution plan cannot be built for a data set containing
    * no pipes
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the application name</TD></TR>
    * <TR><TD>1</TD><TD>the data set name</TD></TR>
    * </TABLE>
    */
   public static final int EXEC_PLAN_PIPES_NULL         = 5021;

   /**
    * the execution plan cannot be built for a data set containing
    * no query pipes
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the application name</TD></TR>
    * <TR><TD>1</TD><TD>the data set name</TD></TR>
    * </TABLE>
    */
   public static final int EXEC_PLAN_NO_QUERY_PIPES   = 5022;

   /**
    * the execution plan cannot be built for a data set containing
    * multiple query pipes
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the application name</TD></TR>
    * <TR><TD>1</TD><TD>the data set name</TD></TR>
    * </TABLE>
    */
   public static final int EXEC_PLAN_MULTIPLE_QUERY_PIPES   = 5023;

   /**
    * the execution plan cannot be built for a pipe containing
    * no back-end tables
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the application name</TD></TR>
    * <TR><TD>1</TD><TD>the data set name</TD></TR>
    * <TR><TD>2</TD><TD>the pipe name</TD></TR>
    * </TABLE>
    */
   public static final int EXEC_PLAN_NO_BETABLES_IN_PIPE   = 5024;

   /**
    * the result set could not be closed while releasing the exec data
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>SQLException.getMessage()</TD></TR>
    * </TABLE>
    */
   public static final int EXEC_DATA_CLOSE_RESULT_SET   = 5025;

   /**
    * the prepared stmt could not be closed while releasing the exec data
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>SQLException.getMessage()</TD></TR>
    * </TABLE>
    */
   public static final int EXEC_DATA_CLOSE_PREP_STMT   = 5026;

   /**
    * can't get a connection - none have been established
    * <p>
    * No arguments.
    */
   public static final int EXEC_DATA_NO_CONNECTIONS   = 5027;

   /**
    * can't get a connection - key out of range
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>connKey</TD></TR>
    * <TR><TD>1</TD><TD>valid range (0 - size-1)</TD></TR>
    * </TABLE>
    */
   public static final int EXEC_DATA_BAD_CONN_KEY      = 5028;

   /**
    * an exception occurred attempting to load the default
    * back-end credentials
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Exception.getMessage()</TD></TR>
    * </TABLE>
    */
   public static final int LOAD_DEF_CREDENTIALS_EXCEPTION   = 5029;

   /**
    * the number of connections to init in the db pool exceeds the max
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>max count</TD></TR>
    * <TR><TD>1</TD><TD>init count</TD></TR>
    * </TABLE>
    */
   public static final int DBPOOL_CONN_INIT_EXCEEDS_MAX   = 5030;

   /**
    * an exception occurred attempting to establish the connection
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>datasource</TD></TR>
    * <TR><TD>1</TD><TD>error</TD></TR>
    * </TABLE>
    */
   public static final int DBPOOL_CONN_INIT_EXCEPTION      = 5031;

   /**
    * a SQL statement joining multiple tables cannot be built 
    * without join conditions
    * <p>
    * No arguments.
    */
   public static final int SQL_BUILDER_NO_JOINS            = 5032;

   /**
    * the execution plan cannot be built for a pipe containing
    * multiple back-end tables
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the application name</TD></TR>
    * <TR><TD>1</TD><TD>the data set name</TD></TR>
    * <TR><TD>2</TD><TD>the pipe name</TD></TR>
    * </TABLE>
    */
   public static final int EXEC_PLAN_MULTIPLE_BETABLES_IN_PIPE   = 5033;

   /**
    * The SQL builder only allows a single table per data modification
    * statement
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the table in the UPDATE</TD></TR>
    * <TR><TD>1</TD><TD>the table which cannot be added</TD></TR>
    * </TABLE>
    */
   public static final int SQL_BUILDER_MOD_SINGLE_TAB_ONLY   = 5034;

   /**
    * The SQL builder does not allow UDF mappings for
    * data modification statements
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the name of the UDF</TD></TR>
    * </TABLE>
    */
   public static final int SQL_BUILDER_UDF_NOT_SUPPORTED_IN_MOD   = 5035;

   /**
    * No tables specified for the data modification statement
    * <p>
    * No arguments.
    */
   public static final int SQL_BUILDER_MOD_TABLE_REQD         = 5036;

   /**
    * At least one column must be specified as updatable in the pipe
    * to build the SET clause of the UPDATE statement
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the table being updated</TD></TR>
    * </TABLE>
    */
   public static final int SQL_BUILDER_UPDATABLE_COL_REQD   = 5037;

   /**
    * An update or delete without a WHERE clause was detected
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the table being updated</TD></TR>
    * </TABLE>
    */
   public static final int SQL_BUILDER_UPD_OR_DEL_NO_WHERE   = 5038;

   /**
    * The specified column does not have a mapping defined for it
    * in the data modification statement
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the unmapped column</TD></TR>
    * </TABLE>
    */
   public static final int SQL_BUILDER_MOD_MAP_REQD         = 5039;

   /**
    * the execution plan cannot be built for a data set containing
    * no update pipes
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the application name</TD></TR>
    * <TR><TD>1</TD><TD>the data set name</TD></TR>
    * </TABLE>
    */
   public static final int EXEC_PLAN_NO_UPDATE_PIPES         = 5040;

   /**
    * the execution plan cannot be built for a data set containing
    * multiple update pipes
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the application name</TD></TR>
    * <TR><TD>1</TD><TD>the data set name</TD></TR>
    * </TABLE>
    */
   public static final int EXEC_PLAN_MULTIPLE_UPDATE_PIPES   = 5041;

   /**
    * a column was specified as being updatable and as the update key.
    * this is not currently supported
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the name of the table</TD></TR>
    * <TR><TD>1</TD><TD>the name of the column</TD></TR>
    * </TABLE>
    */
   public static final int EXEC_PLAN_COL_UPD_AND_KEY_NOT_SUPPORTED   = 5042;

   /**
    * an exception occurred attempting to load meta data for the table.
    * This may occur when a particular method is not supported by the driver.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the datasource name</TD></TR>
    * <TR><TD>3</TD><TD>the schema name</TD></TR>
    * <TR><TD>4</TD><TD>the table name</TD></TR>
    * <TR><TD>5</TD><TD>meta data operation</TD></TR>
    * <TR><TD>6</TD><TD>Throwable.toString()</TD></TR>
    * </TABLE>
    */
   public static final int LOAD_META_DATA_EXCEPTION   = 5043;

   /**
    * index lookup cannot be performed for the specified columns.
    * This is really a warning logged for debugging slow response times.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the driver name</TD></TR>
    * <TR><TD>1</TD><TD>the server name</TD></TR>
    * <TR><TD>2</TD><TD>the database name</TD></TR>
    * <TR><TD>3</TD><TD>the schema name</TD></TR>
    * <TR><TD>4</TD><TD>the table name</TD></TR>
    * <TR><TD>5</TD><TD>the column name(s)</TD></TR>
    * </TABLE>
    */
   public static final int NO_LOOKUP_INDEX_DEFINED      = 5044;

   /**
    * when doing updates, inserts or deletes the back-end mapping must be
    * to a back-end column (eg, not a UDF)
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the application name</TD></TR>
    * <TR><TD>1</TD><TD>the data set name</TD></TR>
    * <TR><TD>2</TD><TD>the pipe name</TD></TR>
    * <TR><TD>3</TD><TD>the 1-based index of the mapping</TD></TR>
    * </TABLE>
    */
   public static final int UPDATE_MAP_NOT_TO_BECOL      = 5045;

   /**
    * an exception occurred attempting to release the connection
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>driver</TD></TR>
    * <TR><TD>1</TD><TD>server</TD></TR>
    * <TR><TD>2</TD><TD>Exception.getMessage()</TD></TR>
    * </TABLE>
    */
   public static final int DBPOOL_CONN_RELEASE_EXCEPTION   = 5046;

   /**
    * cross-dependencies when performing inserts or deletes cannot be
    * resolved
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the name of one table</TD></TR>
    * <TR><TD>1</TD><TD>the name of the other table</TD></TR>
    * </TABLE>
    */
   public static final int DATA_MOD_UNSUPPORTED_FOR_XDEPEND   = 5047;

   /**
    * a column was specified as being updatable
    * but was not found in the data mappings
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the name of the table</TD></TR>
    * <TR><TD>1</TD><TD>the name of the column</TD></TR>
    * </TABLE>
    */
   public static final int EXEC_PLAN_UPD_COL_NOT_MAPPED   = 5048;

   /**
    * a column was specified as being part of the update key
    * but was not found in the data mappings
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the name of the table</TD></TR>
    * <TR><TD>1</TD><TD>the name of the column</TD></TR>
    * </TABLE>
    */
   public static final int EXEC_PLAN_KEY_COL_NOT_MAPPED   = 5049;

   /**
    * SQLException occurs due to SQL access error.
    */
   public static final int DATABASE_ACCESS_ERROR         = 5050;

   /**
    * the connection attempt to the specified drver/server failed
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the connection string (JDBC URL)</TD></TR>
    * <TR><TD>1</TD><TD>the error text</TD></TR>
    * </TABLE>
    */
   public static final int BE_CONN_EXCEPTION               = 5051;

   /**
    * no available connection to service the request
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the connection string (JDBC URL)</TD></TR>
    * <TR><TD>1</TD><TD>max connection count</TD></TR>
    * </TABLE>
    */
   public static final int NO_AVAILABLE_BE_CONNS         = 5052;

   /**
    * literal cannot be used with various op codes (eg, IN, BETWEEN)
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>op code</TD></TR>
    * <TR><TD>1</TD><TD>the literal specified</TD></TR>
    * </TABLE>
    */
   public static final int SQL_BUILDER_LITSET_REQD_FOR_OP   = 5053;

   /**
    * literal sets can only be used with certain op codes (eg, IN, BETWEEN)
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the unsupported op code</TD></TR>
    * </TABLE>
    */
   public static final int SQL_BUILDER_LITSET_INVALID_FOR_OP   = 5054;

   /**
    * the literal set must be a specified size (eg, 2 for BETWEEN)
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the op code</TD></TR>
    * <TR><TD>1</TD><TD>the number of literals required</TD></TR>
    * <TR><TD>2</TD><TD>the number of literals specified</TD></TR>
    * </TABLE>
    */
   public static final int SQL_BUILDER_LITSET_WRONG_SIZE   = 5055;

   /**
    * the literal set cannot be empty
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the op code</TD></TR>
    * </TABLE>
    */
   public static final int SQL_BUILDER_LITSET_EMPTY      = 5056;

   /**
    * aliases are not supported by the back-end
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the driver name</TD></TR>
    * <TR><TD>1</TD><TD>the server name</TD></TR>
    * <TR><TD>2</TD><TD>the table name</TD></TR>
    * <TR><TD>3</TD><TD>the column name</TD></TR>
    * </TABLE>
    */
   public static final int SQL_BUILDER_ALIAS_UNSUPPORTED   = 5057;


   /**
    * log the execution of the specified prepared statement
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the prepared statement text</TD></TR>
    * </TABLE>
    */
   public static final int LOG_PREPARED_STMT            = 5401;

   /**
    * log data being bound to a prepared statement's column
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the bind position</TD></TR>
    * <TR><TD>1</TD><TD>the value being bound</TD></TR>
    * </TABLE>
    */
   public static final int LOG_BOUND_COL_DATA         = 5402;


   /**
    * The specified feature is not yet supported.
    *
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the name of the feature</TD></TR>
    * </TABLE>
    */
   public static final int NOT_YET_SUPPORTED         = 5999;

}
