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
package com.percussion.tablefactory;


/**
 * The IPSTableFactoryErrors interface is provided as a convenient mechanism
 * for accessing the various related error codes.  Errors are
 * further broken down as follows:
 * <TABLE BORDER="1">
 * <TR><TH>Range</TH><TH>Component</TH></TR>
 * <TR><TD>1001 - 1100</TD><TD>general errors used all over</TD></TR>
 * <TR><TD>1101 - 1200</TD><TD>object store errors</TD></TR>
 * <TR><TD>1201 - 1300</TD><TD>SQL processing errors</TD></TR>
 * <TR><TD>1301 - 1400</TD><TD>Schema processing errors</TD></TR>
 * </TABLE>
 */
public interface IPSTableFactoryErrors
{
   /**
    * null specified as the Element in fromXml.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the expected XML Element tag</TD></TR>
    * </TABLE>
    */
   public static final int XML_ELEMENT_NULL        = 1001;

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
   public static final int XML_ELEMENT_WRONG_TYPE  = 1002;

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
   public static final int XML_ELEMENT_INVALID_ATTR   = 1003;

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
   public static final int XML_ELEMENT_INVALID_CHILD  = 1004;

   /**
    * No stylesheet for the provided name found.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the stylesheet name</TD></TR>
    * </TABLE>
    */
   public static final int STYLESHEET_NOT_FOUND  = 1005;

   /**
    * Transformation error using the supplied stylesheet.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the stylesheet name</TD></TR>
    * <TR><TD>1</TD><TD>the transformer error message</TD></TR>
    * </TABLE>
    */
   public static final int TRANSFORMATION_ERROR  = 1006;

   /**
    * Log file configuration error.
    * <p>
    * No arguments are supplied
    */
   public static final int LOG_FILE_CONF_ERROR  = 1007;

   /**
    * Error writing to log file.
    * <p>
    * No arguments are supplied
    */
   public static final int LOG_FILE_WRITE_ERROR  = 1008;

   /**
    * Unable to locate specified data type mappings
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the db alias to match</TD></TR>
    * <TR><TD>1</TD><TD>the driver to match</TD></TR>
    * <TR><TD>2</TD><TD>the os to match</TD></TR>
    * </TABLE>
    */
   public static final int DATA_TYPE_MAP_NOT_FOUND  = 1101;

   /**
    * An invalid data type mapping has been specified
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the jdbc type</TD></TR>
    * <TR><TD>1</TD><TD>the native type</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_DATA_TYPE_MAPPING  = 1102;

   /**
    * Unable to convert from jdbctype integer to String
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the jdbc type</TD></TR>
    * </TABLE>
    */
   public static final int JDBC_INT_DATA_TYPE_CONVERSION  = 1103;

   /**
    * Unable to convert from jdbctype String to integer
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the jdbc type</TD></TR>
    * </TABLE>
    */
   public static final int JDBC_STRING_DATA_TYPE_CONVERSION  = 1104;

   /**
    * Unable to decrypt the supplied password
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the password</TD></TR>
    * <TR><TD>1</TD><TD>the error text</TD></TR>
    * </TABLE>
    */
   public static final int PW_DECRYPTION_ERROR  = 1105;

   /**
    * An object was supplied a list of columns containing a duplicate
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The container (e.g. table, primary key)</TD></TR>
    * <TR><TD>1</TD><TD>The column</TD></TR>
    * </TABLE>
    */
   public static final int DUPLICATE_COLUMN  = 1106;

   /**
    * An object was supplied a column name that was <code>null</code> or empty.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The container (e.g. table, primary key)</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_COLUMN_NAME  = 1107;


   /**
    * Attempting to remove the last column from a table schema
    * <p>
    * No arguments are supplied
    */
   public static final int REMOVE_LAST_COLUMN  = 1108;

   /**
    * A table data collection set on a table schema collection contains a table
    * not found in the schema collection.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The table name</TD></TR>
    * </TABLE>
    */
   public static final int TABLE_SCHEMA_NOT_FOUND  = 1109;

   /**
    * A table data object set on a table schema contains a column not found in
    * the table schema.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The table name</TD></TR>
    * <TR><TD>1</TD><TD>The column name</TD></TR>
    * </TABLE>
    */
   public static final int COLUMN_NOT_FOUND  = 1110;

   /**
    * A table data object is set on a table schema and the table schema's
    * isAlter is <code>true</code>.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The table name</TD></TR>
    * </TABLE>
    */
   public static final int ALTER_TABLE_SET_DATA  = 1111;

   /**
    * A schema object (primary key, foreign key) was supplied a list
    * of columns containing one or more columns not defined in the table schema.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The table name</TD></TR>
    * <TR><TD>1</TD><TD>The container (e.g. primary key, foreign key)</TD></TR>
    * <TR><TD>2</TD><TD>The column(s)</TD></TR>
    * </TABLE>
    */
   public static final int MISSING_COLUMN  = 1112;

   /**
    * Error loading default datatype map file.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The message</TD></TR>
    * </TABLE>
    */
   public static final int LOAD_DEFAULT_DATATYPE_MAP  = 1113;

   /**
    * An object was supplied an invalid encoding.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The container (e.g. table, primary key)</TD></TR>
    * <TR><TD>0</TD><TD>The specified encoding.</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_ENCODING  = 1114;

   /**
    * Error loading table meta data.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The table name</TD></TR>
    * <TR><TD>0</TD><TD>The error text</TD></TR>
    * </TABLE>
    */
   public static final int SQL_TABLE_META_DATA  = 1201;

   /**
    * Unable to connect to the database.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The message</TD></TR>
    * </TABLE>
    */
   public static final int SQL_CONNECTION_FAILED  = 1202;

   /**
    * Unable to catalog the specified table.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The table name</TD></TR>
    * <TR><TD>1</TD><TD>The message</TD></TR>
    * </TABLE>
    */
   public static final int SQL_CATALOG_TABLE_FAILED  = 1203;

   /**
    * Error occurred binding a value to a prepared statement.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The value</TD></TR>
    * <TR><TD>1</TD><TD>The type</TD></TR>
    * <TR><TD>2</TD><TD>The message</TD></TR>
    * </TABLE>
    */
   public static final int SQL_BIND_PARAMETER  = 1204;

   /**
    * Error occurred cataloging table data.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The table name</TD></TR>
    * <TR><TD>1</TD><TD>The message</TD></TR>
    * </TABLE>
    */
   public static final int SQL_CATALOG_DATA  = 1205;

   /**
    * Unable to process table schema changes
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The table name</TD></TR>
    * <TR><TD>1</TD><TD>The message</TD></TR>
    * </TABLE>
    */
   public static final int SCHEMA_PROCESS_ERROR  = 1301;

   /**
    * Unable to check for existing data
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The table name</TD></TR>
    * <TR><TD>1</TD><TD>The message</TD></TR>
    * </TABLE>
    */
   public static final int CHECK_EXISTING_DATA  = 1302;

   /**
    * Unable to process table schema collection changes due to an error
    * during the processing of one of the tables.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The message</TD></TR>
    * </TABLE>
    */
   public static final int SCHEMA_COLL_PROCESS_ERROR  = 1303;

   /**
    * Cannot process alter for a table that does not exist
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The table name</TD></TR>
    * </TABLE>
    */
   public static final int ALTER_NO_TABLE  = 1304;

   /**
    * Cannot process data update for a table that does not have primary or
    * update keys.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The table name</TD></TR>
    * </TABLE>
    */
   public static final int UPDATE_DATA_NO_KEYS  = 1305;

   /**
    * Cannot process data update for a row that does not have a key value
    * defined.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The table name</TD></TR>
    * <TR><TD>1</TD><TD>The column name</TD></TR>
    * </TABLE>
    */
   public static final int UPDATE_DATA_NO_KEY_VALUE  = 1306;

   /**
    * Unable to process table data changes
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The table name</TD></TR>
    * <TR><TD>1</TD><TD>The message</TD></TR>
    * </TABLE>
    */
   public static final int DATA_PROCESS_ERROR  = 1307;

   /**
    * Current data in the database table contains a row that does not have a
    * key value defined.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The table name</TD></TR>
    * <TR><TD>1</TD><TD>The column name</TD></TR>
    * </TABLE>
    */
   public static final int UPDATE_DATA_NO_KEY_VALUE_IN_DB  = 1308;

   /**
    * Schema changes for "VIEW" is not supported.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The VIEW name</TD></TR>
    * </TABLE>
    */
   public static final int ALTER_VIEW_NOT_SUPPORTED  = 1309;

   /**
    * Data handler class not found in the classpath.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Data handler class name</TD></TR>
    * </TABLE>
    */
   public static final int DATA_HANDLER_CLASS_NOT_FOUND  = 1310;

}
