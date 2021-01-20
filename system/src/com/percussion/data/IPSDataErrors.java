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
 * The IPSDataErrors inteface is provided as a convenient mechanism
 * for accessing the various data related error codes. The data error
 * code ranges are:
 * <TABLE BORDER="1">
 * <TR><TH>Range</TH><TH>Component</TH></TR>
 * <TR><TD>5201 - 5400</TD><TD>back-end data processing errors</TD></TR>
 * <TR><TD>6001 - 7000</TD><TD>XML/general data processing errors</TD></TR>
 * </TABLE>
 * The text of the error messages is stored in the properties file
 * <code>com.percussion.error.PSErrorStringBundle</code>
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public interface IPSDataErrors {

   /**
    * This error is reported by the
    * com.percussion.error.PSBackEndQueryProcessingError object.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>session id</TD></TR>
    * </TABLE>
    */
   public static final int QUERY_PROCESSING_ERROR   = 5201;

   /**
    * This error is reported by the
    * com.percussion.error.PSBackEndUpdateProcessingError object.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>session id</TD></TR>
    * </TABLE>
    */
   public static final int UPDATE_PROCESSING_ERROR   = 5202;

   /**
    * the back-end column used in PSStatementColumnMapper.put
    * was not of type PSStatementColumn
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the specified object's class name</TD></TR>
    * </TABLE>
    */
   public static final int COLMAPPER_BE_COL_NOT_STMTCOL   = 5203;

   /**
    * the back-end values specified to the request link generator
    * don't pair up with the defined columns
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the number of columns required for the link</TD></TR>
    * <TR><TD>1</TD><TD>the number of value specified</TD></TR>
    * </TABLE>
    */
   public static final int REQ_LINK_BE_VALS_INVALID   = 5204;

   /**
    * an extractor cannot be generated without a replacement value
    * <p>
    * No arguments for this message.
    */
   public static final int REPLACEMENT_VALUE_REQD      = 5205;

   /**
    * the source data set specified to the request link generator
    * cannot be null
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the application name</TD></TR>
    * <TR><TD>1</TD><TD>the target data set name (if available)</TD></TR>
    * </TABLE>
    */
   public static final int REQ_LINK_SOURCE_DS_NULL      = 5206;

   /**
    * the source data set specified to the request link generator
    * cannot be null
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the application name</TD></TR>
    * <TR><TD>1</TD><TD>the source data set name (if available)</TD></TR>
    * </TABLE>
    */
   public static final int REQ_LINK_TARGET_DS_NULL      = 5207;

   /**
    * the back-end column is not part of the result set
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the back-end column name</TD></TR>
    * </TABLE>
    */
   public static final int BE_COL_EXTR_INVALID_COL      = 5209;

   /**
    * an exception occurred accessing the back-end column
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the back-end column name</TD></TR>
    * <TR><TD>1</TD><TD>the exception text</TD></TR>
    * </TABLE>
    */
   public static final int BE_COL_EXTR_EXCEPTION      = 5210;

   /**
    * an exception occurred locating the index of the back-end column
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the back-end column name</TD></TR>
    * <TR><TD>1</TD><TD>the exception text</TD></TR>
    * </TABLE>
    */
   public static final int BE_COL_GET_INDEX_EXCEPTION   = 5211;

   /**
    * the request link type is invalid
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the application name</TD></TR>
    * <TR><TD>1</TD><TD>the source data set name</TD></TR>
    * <TR><TD>2</TD><TD>the target data set name</TD></TR>
    * <TR><TD>3</TD><TD>the link type (update, insert, etc.)</TD></TR>
    * </TABLE>
    */
   public static final int REQ_LINK_TYPE_UNSUPPORTED   = 5212;

   /**
    * a query request link generator must be linked to a query pipe
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the application name</TD></TR>
    * <TR><TD>1</TD><TD>the source data set name</TD></TR>
    * <TR><TD>2</TD><TD>the target data set name</TD></TR>
    * </TABLE>
    */
   public static final int QUERY_LINK_TARGET_NOT_QUERY   = 5213;

   /**
    * the target of a query request link generator must contain a data selector
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the application name</TD></TR>
    * <TR><TD>1</TD><TD>the source data set name</TD></TR>
    * <TR><TD>2</TD><TD>the target data set name</TD></TR>
    * </TABLE>
    */
   public static final int QUERY_LINK_TARGET_SELECTOR_REQD   = 5214;

   /**
    * an update request link generator must be linked to an update pipe
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the application name</TD></TR>
    * <TR><TD>1</TD><TD>the source data set name</TD></TR>
    * <TR><TD>2</TD><TD>the target data set name</TD></TR>
    * <TR><TD>3</TD><TD>the link type (update, insert, etc.)</TD></TR>
    * </TABLE>
    */
   public static final int UPDATE_LINK_TARGET_NOT_UPDATE   = 5215;

   /**
    * the target of an update request link generator must contain a data updater
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the application name</TD></TR>
    * <TR><TD>1</TD><TD>the source data set name</TD></TR>
    * <TR><TD>2</TD><TD>the target data set name</TD></TR>
    * <TR><TD>3</TD><TD>the link type (update, insert, etc.)</TD></TR>
    * </TABLE>
    */
   public static final int UPDATE_LINK_TARGET_SYNC_REQD   = 5216;

   /**
    * the target of an update request link generator contains a key column
    * without a data mapping
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the application name</TD></TR>
    * <TR><TD>1</TD><TD>the source data set name</TD></TR>
    * <TR><TD>2</TD><TD>the target data set name</TD></TR>
    * <TR><TD>3</TD><TD>the link type (update, insert, etc.)</TD></TR>
    * <TR><TD>4</TD><TD>the back-end column name</TD></TR>
    * </TABLE>
    */
   public static final int UPDATE_LINK_KEY_NOT_MAPPED      = 5217;

   /**
    * At least 1 result set is required for Index Lookup Joining
    * <p>
    * No arguments.
    */
   public static final int INDEX_JOINER_RESULT_SET_REQD   = 5218;

   /**
    * the join column for the left side is not in the result set
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the back-end column name</TD></TR>
    * </TABLE>
    */
   public static final int INDEX_JOINER_LCOL_NOT_FOUND   = 5219;

   /**
    * At least 2 result sets are required for Sorted Result Joining
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the number of result sets found</TD></TR>
    * </TABLE>
    */
   public static final int SORTED_JOINER_2_RESULT_SETS_REQD   = 5220;

   /**
    * the join column for the left side is not in the result set
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the back-end column name</TD></TR>
    * </TABLE>
    */
   public static final int SORTED_JOINER_LCOL_NOT_FOUND   = 5221;

   /**
    * the join column for the right side is not in the result set
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the back-end column name</TD></TR>
    * </TABLE>
    */
   public static final int SORTED_JOINER_RCOL_NOT_FOUND   = 5222;

   /**
    * the generated column count does not match the expected count for
    * the joined result set
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the expected column count</TD></TR>
    * <TR><TD>1</TD><TD>the actual column count</TD></TR>
    * </TABLE>
    */
   public static final int SORTED_JOINER_COL_COUNT_MISMATCH   = 5223;

   /**
    * the new right side result set column count does not match the
    * expected count for the right side result set
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the expected column count</TD></TR>
    * <TR><TD>1</TD><TD>the actual column count</TD></TR>
    * </TABLE>
    */
   public static final int JOINED_ROW_BUF_RCOL_COUNT_MISMATCH   = 5224;

   /**
    * an exception occurred loading the XML document from cache
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the cache key</TD></TR>
    * <TR><TD>1</TD><TD>the excception text</TD></TR>
    * </TABLE>
    */
   public static final int CACHER_LOAD_XML_EXCEPTION      = 5225;

   /**
    * an exception occurred storing the XML document in cache
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the cache key</TD></TR>
    * <TR><TD>1</TD><TD>the excception text</TD></TR>
    * </TABLE>
    */
   public static final int CACHER_STORE_XML_EXCEPTION      = 5226;

   /**
    * an exception occurred loading the result page from cache
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the cache key</TD></TR>
    * <TR><TD>1</TD><TD>the excception text</TD></TR>
    * </TABLE>
    */
   public static final int CACHER_LOAD_RESPAGE_EXCEPTION   = 5227;

   /**
    * an exception occurred storing the result page in cache
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the cache key</TD></TR>
    * <TR><TD>1</TD><TD>the excception text</TD></TR>
    * </TABLE>
    */
   public static final int CACHER_STORE_RESPAGE_EXCEPTION   = 5228;

   /**
    * the entry cannot be stored in the cache (it's full)
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the cache key</TD></TR>
    * <TR><TD>1</TD><TD>the max size permitted</TD></TR>
    * <TR><TD>2</TD><TD>the current size stored</TD></TR>
    * <TR><TD>3</TD><TD>the entry size</TD></TR>
    * </TABLE>
    */
   public static final int CACHER_FULL                  = 5229;

   /**
    * an exception occurred attempting to remove the specified cache file
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the file name</TD></TR>
    * <TR><TD>1</TD><TD>the excception text</TD></TR>
    * </TABLE>
    */
   public static final int CACHER_FILE_REMOVE_EXCEPTION   = 5230;

   /**
    * log the occurrence setting we got from the DTD for the XML field
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the XML field name</TD></TR>
    * <TR><TD>1</TD><TD>the occurrence setting string</TD></TR>
    * </TABLE>
    */
   public static final int EXEC_PLAN_LOG_DTD_OCCURS      = 5231;

   /**
    * log an XML field we're collapsing on
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the XML field name</TD></TR>
    * </TABLE>
    */
   public static final int EXEC_PLAN_LOG_COLLAPSED_XML_FIELD   = 5232;

   /**
    * an exception occurred attempting to send a response from cache
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the session id</TD></TR>
    * <TR><TD>1</TD><TD>the exception text</TD></TR>
    * </TABLE>
    */
   public static final int CACHE_FILE_SEND_EXCEPTION      = 5233;

   /**
    * log the XML field being treated as the root for the walker when updating
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the XML field name</TD></TR>
    * </TABLE>
    */
   public static final int EXEC_PLAN_LOG_UPDATE_XML_WALKER_ROOT   = 5234;

   /**
    * log the XML field being treated as the root for the walker
    * when processing the updates for the specified statement
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the XML field name</TD></TR>
    * <TR><TD>1</TD><TD>the statement text</TD></TR>
    * </TABLE>
    */
   public static final int EXEC_PLAN_LOG_UPDATE_XML_STMT_WALKER   = 5235;

   /**
    * log the rebased XML field being treated as the root for the walker
    * when processing the updates for the specified statement
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the rebased XML field name</TD></TR>
    * <TR><TD>1</TD><TD>the statement text</TD></TR>
    * </TABLE>
    */
   public static final int EXEC_PLAN_LOG_REBASED_STMT_WALKER   = 5236;

   /**
    * the handler class for this UDF could not be loaded
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the udf name</TD></TR>
    * <TR><TD>1</TD><TD>the handler class name</TD></TR>
    * <TR><TD>2</TD><TD>the reason</TD></TR>
    * </TABLE>
    */
   public static final int UDF_HANDLER_NOT_LOADED         = 5237;

   /**
    * no updatable columns are defined for this table, it will be
    * removed from the update plan
    * <P>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the application name</TD></TR>
    * <TR><TD>1</TD><TD>the data set name</TD></TR>
    * <TR><TD>2</TD><TD>the table name:group name</TD></TR>
    * </TABLE>
    */
   public static final int EXEC_PLAN_IGNORE_NO_UPDCOL_UPDATE   = 5238;

   /**
    * no key columns are defined for this delete, which may be
    * undesirable (this is a warning)
    * <P>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the application name</TD></TR>
    * <TR><TD>1</TD><TD>the data set name</TD></TR>
    * <TR><TD>2</TD><TD>the sequence in the SQL plan (1 based)</TD></TR>
    * <TR><TD>3</TD><TD>the type of execution step</TD></TR>
    * <TR><TD>4</TD><TD>the text associated with the step (eg, prepared SQL)</TD></TR>
    * </TABLE>
    */
   public static final int EXEC_PLAN_LOG_SQL_PLAN      = 5239;

   /**
    * index lookup is not supported for full outer joins
    * <P>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the application name</TD></TR>
    * <TR><TD>1</TD><TD>the data set name</TD></TR>
    * </TABLE>
    */
   public static final int EXEC_PLAN_NO_INDEX_LOOKUP_FULL_OUTER   = 5240;

   /**
    * index lookup is not supported for right outer joins
    * <P>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the application name</TD></TR>
    * <TR><TD>1</TD><TD>the data set name</TD></TR>
    * </TABLE>
    */
   public static final int EXEC_PLAN_NO_INDEX_LOOKUP_RIGHT_OUTER   = 5241;

   /**
    * could not load our table meta data object (may not have conn?)
    * <P>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the application name</TD></TR>
    * <TR><TD>1</TD><TD>the data set name</TD></TR>
    * <TR><TD>2</TD><TD>table alias</TD></TR>
    * </TABLE>
    */
   public static final int CANNOT_LOAD_TABLE_META      = 5242;

   /**
    * could not load our index meta data object (may not have conn?)
    * <P>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the application name</TD></TR>
    * <TR><TD>1</TD><TD>the data set name</TD></TR>
    * <TR><TD>2</TD><TD>table alias</TD></TR>
    * </TABLE>
    */
   public static final int CANNOT_LOAD_INDEX_META      = 5243;

   /**
    * index lookup is not supported if the right side has no useful indices
    * <P>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the application name</TD></TR>
    * <TR><TD>1</TD><TD>the data set name</TD></TR>
    * <TR><TD>2</TD><TD>the table name</TD></TR>
    * </TABLE>
    */
   public static final int EXEC_PLAN_NO_INDEX_LOOKUP_INDICES   = 5244;

   /**
    * join cardinality could not be estimated between these tables
    * <P>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the application name</TD></TR>
    * <TR><TD>1</TD><TD>the data set name</TD></TR>
    * <TR><TD>2</TD><TD>the left table name</TD></TR>
    * <TR><TD>3</TD><TD>the right table name</TD></TR>
    * </TABLE>
    */
   public static final int EXEC_PLAN_JOIN_CARDINALITY_NOT_FOUND   = 5245;

   /**
    * log the estimated join cardinality between these tables
    * <P>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the application name</TD></TR>
    * <TR><TD>1</TD><TD>the data set name</TD></TR>
    * <TR><TD>2</TD><TD>the left table name</TD></TR>
    * <TR><TD>3</TD><TD>the right table name</TD></TR>
    * <TR><TD>4</TD><TD>the cardinality</TD></TR>
    * </TABLE>
    */
   public static final int EXEC_PLAN_LOG_JOIN_CARDINALITY   = 5246;

   /**
    * log the estimated cardinality for this table
    * <P>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the application name</TD></TR>
    * <TR><TD>1</TD><TD>the data set name</TD></TR>
    * <TR><TD>2</TD><TD>the table name</TD></TR>
    * <TR><TD>3</TD><TD>the cardinality</TD></TR>
    * </TABLE>
    */
   public static final int EXEC_PLAN_LOG_TABLE_CARDINALITY   = 5247;

   /**
    * log the estimated number of unique keys in this table
    * <P>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the application name</TD></TR>
    * <TR><TD>1</TD><TD>the data set name</TD></TR>
    * <TR><TD>2</TD><TD>the table name</TD></TR>
    * <TR><TD>3</TD><TD>the number of unique rows</TD></TR>
    * </TABLE>
    */
   public static final int EXEC_PLAN_LOG_UNIQUE_ROW_ESTIMATE   = 5248;

   /**
    * log the estimated selectivity between these tables
    * <P>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the application name</TD></TR>
    * <TR><TD>1</TD><TD>the data set name</TD></TR>
    * <TR><TD>2</TD><TD>the left table name</TD></TR>
    * <TR><TD>3</TD><TD>the right table name</TD></TR>
    * <TR><TD>4</TD><TD>the selectivity</TD></TR>
    * </TABLE>
    */
   public static final int EXEC_PLAN_LOG_SELECTIVITY   = 5249;

   /**
    * the number of joins defined is insufficient to handle the
    * number of tables being joined
    * <P>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the application name</TD></TR>
    * <TR><TD>1</TD><TD>the data set name</TD></TR>
    * <TR><TD>2</TD><TD>the number of tables</TD></TR>
    * <TR><TD>3</TD><TD>the number of joins</TD></TR>
    * </TABLE>
    */
   public static final int INSUFFICIENT_JOINS_FOR_TABLES   = 5250;

   /**
    * an exception occurred attempting to estimate the join cardinality
    * between these tables
    * <P>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the application name</TD></TR>
    * <TR><TD>1</TD><TD>the data set name</TD></TR>
    * <TR><TD>2</TD><TD>the left table name</TD></TR>
    * <TR><TD>3</TD><TD>the right table name</TD></TR>
    * <TR><TD>4</TD><TD>the exception text</TD></TR>
    * </TABLE>
    */
   public static final int EXEC_PLAN_JOIN_CARDINALITY_EXCEPTION   = 5251;

   /**
    * an exception occurred attempting to estimate the join selectivity
    * between these tables
    * <P>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the application name</TD></TR>
    * <TR><TD>1</TD><TD>the data set name</TD></TR>
    * <TR><TD>2</TD><TD>the left table name</TD></TR>
    * <TR><TD>3</TD><TD>the right table name</TD></TR>
    * <TR><TD>4</TD><TD>the exception text</TD></TR>
    * </TABLE>
    */
   public static final int EXEC_PLAN_JOIN_SELECTIVITY_EXCEPTION   = 5252;

   /**
    * the SQL builder only supports homogeneous joins
    * <P>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the driver used by the builder</TD></TR>
    * <TR><TD>1</TD><TD>the server used by the builder</TD></TR>
    * <TR><TD>2</TD><TD>the conflicting driver</TD></TR>
    * <TR><TD>3</TD><TD>the conflicting server</TD></TR>
    * </TABLE>
    */
   public static final int SQL_BUILDER_HOMOGENEOUS_JOIN_ONLY   = 5253;

   /**
    * multiple outer joins are not supported on the same tables
    * <P>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the left table</TD></TR>
    * <TR><TD>1</TD><TD>the right table</TD></TR>
    * </TABLE>
    */
   public static final int SQL_BUILDER_MULTIPLE_OUTERS_NOT_SUPPORTED   = 5254;

   /**
    * we do not support applying a translator to join conditions in a
    * heterogeneous join through the builder. the optimizer must build
    * a homogeneous join to handle this.
    * <P>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the left table</TD></TR>
    * <TR><TD>1</TD><TD>the right table</TD></TR>
    * </TABLE>
    */
   public static final int SQL_BUILDER_XLATOR_UNSUPPORTED_IN_HOMEGENEOUS   = 5255;

   /**
    * a join path could not be found between the two tables. When this
    * occurs, we can't perform the query
    * <P>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the left table</TD></TR>
    * <TR><TD>1</TD><TD>the right table</TD></TR>
    * </TABLE>
    */
   public static final int NO_JOIN_PATH_BETWEEN_TABLES   = 5256;

   /**
    * the statistics could not be loaded for the specified table
    * <P>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the table alias</TD></TR>
    * </TABLE>
    */
   public static final int OPTIMIZER_TABLE_STATS_NOT_LOADED   = 5257;

   /**
    * the left side of a where clause must be a back-end column
    * <P>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the bad replacement value</TD></TR>
    * </TABLE>
    */
   public static final int WHERE_VAR_MUST_BE_BACKEND_COL   = 5258;

   /**
    * the key used in get/setPrivateObject cannot be null
    * <P>
    * No arguments.
    */
   public static final int EXECDATA_PRIVATE_OBJ_KEY_NULL   = 5259;

   /**
    * This is used to report an invalid LOAD type.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>specified type</TD></TR>
    * </TABLE>
    */
   public static final int UNKNOWN_OPCODE_LOAD_TYPE    = 5260;

   /**
    * This is used to report an incorrect operator usage.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>left operand</TD></TR>
    * <TR><TD>0</TD><TD>operator</TD></TR>
    * <TR><TD>0</TD><TD>right operand</TD></TR>
    * </TABLE>
    */
   public static final int WRONG_OPERATOR_USAGE        = 5261;

   /**
    * This is used to report an incorrect data comparison.
    * <p>
    * One example is to compare a string abc with a number 123.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>left operand</TD></TR>
    * <TR><TD>0</TD><TD>operator</TD></TR>
    * <TR><TD>0</TD><TD>right operand</TD></TR>
    * </TABLE>
    */
   public static final int WRONG_DATA_COMPARISON       = 5262;

   /**
    * The data type is wrong for a left side value
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the left side data type</TD></TR>
    * </TABLE>
    */
   public static final int LVALUE_INVALID_TYPE         = 5263;

   /**
    * The data type is wrong for a right side value
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the right side data type</TD></TR>
    * </TABLE>
    */
   public static final int RVALUE_INVALID_TYPE         = 5264;

   /**
    * Conversion from the specified type to the specified type is not
    * supported
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the source data type</TD></TR>
    * <TR><TD>1</TD><TD>the target data type</TD></TR>
    * <TR><TD>2</TD><TD>the data being converted</TD></TR>
    * </TABLE>
    */
   public static final int UNSUPPORTED_CONVERSION      = 5265;

   /**
    * comparisons between these two types are not supported
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the source data type</TD></TR>
    * <TR><TD>1</TD><TD>the target data type</TD></TR>
    * </TABLE>
    */
   public static final int TYPE_COMPARISON_UNSUPPORTED   = 5266;

   /**
    * the specified operator is not supported with this type
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the data type</TD></TR>
    * <TR><TD>1</TD><TD>the operator</TD></TR>
    * </TABLE>
    */
   public static final int OPERATOR_INVALID_FOR_TYPE   = 5267;

   /**
    * The User Context value specifies an invalid type for extraction
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the user context type</TD></TR>
    * <TR><TD>1</TD><TD>the user context value</TD></TR>
    * </TABLE>
    */
   public static final int USER_CTX_INVALID_TYPE   = 5268;

   /**
    * data extractor cannot be created for the replacement value
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the replacement value type</TD></TR>
    * </TABLE>
    */
   public static final int DATA_EXTRACTOR_CREATE_ERROR      = 5269;


   /* **************************** XML ERRORS **************************** */

   /**
    * This is the generic error generating HTML message.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>session id</TD></TR>
    * <TR><TD>1</TD><TD>style sheet name</TD></TR>
    * </TABLE>
    */
   public static final int HTML_GENERATION_ERROR   = 6003;

   /**
    * Conversion cannot be performed on an empty result set.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int NO_DATA_FOR_CONVERSION   = 6004;

   /**
    * Conversion cannot be performed on multiple result sets.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>number of result sets</TD></TR>
    * </TABLE>
    */
   public static final int CANNOT_CONVERT_MULTIPLE_RESULT_SETS   = 6005;

   /**
    * The HTML converter cannot be used for the specified extension.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the request page extension</TD></TR>
    * </TABLE>
    */
   public static final int HTML_CONV_EXT_NOT_SUPPORTED   = 6006;

   /**
    * The XML converter cannot be used for the specified extension.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the request page extension</TD></TR>
    * </TABLE>
    */
   public static final int XML_CONV_EXT_NOT_SUPPORTED   = 6007;

   /**
    * A valid PSResponse object is required to perform a conversion.
    * <p>
    * No arguments passed in for this message.
    */
   public static final int NO_RESPONSE_OBJECT         = 6008;

   /**
    * This specified stylesheet could not be accessed for HTML conversion.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>session id</TD></TR>
    * <TR><TD>1</TD><TD>style sheet name</TD></TR>
    * <TR><TD>1</TD><TD>error description</TD></TR>
    * </TABLE>
    */
   public static final int HTML_GEN_BAD_STYLESHEET_URL   = 6009;

   /**
    * A matching style sheet could not be found for HTML conversion.
    * <p>
    * No arguments are passed in for this message.
    */
   public static final int HTML_GEN_NO_STYLESHEET      = 6010;

   /**
    * An exception was encountered while sending the response.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>session id</TD></TR>
    * <TR><TD>1</TD><TD>error description</TD></TR>
    * </TABLE>
    */
   public static final int SEND_RESPONSE_EXCEPTION      = 6011;

   /**
    * An exception was encountered during XML conversion.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>session id</TD></TR>
    * <TR><TD>1</TD><TD>error description</TD></TR>
    * </TABLE>
    */
   public static final int XML_CONV_EXCEPTION         = 6012;

   /**
    * An exception was encountered during style sheet merging.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>XML doc's root node tag</TD></TR>
    * <TR><TD>1</TD><TD>style sheet</TD></TR>
    * <TR><TD>2</TD><TD>error description</TD></TR>
    * </TABLE>
    */
   public static final int STYLESHEET_MERGE_EXCEPTION   = 6013;

   /**
    * the XML field name used in PSStatementColumnMapper.put
    * was not of type String
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the specified object's class name</TD></TR>
    * </TABLE>
    */
   public static final int COLMAPPER_XML_FIELD_NOT_STRING   = 6014;

   /**
    * The XML or HTML converter cannot be used for request redirection
    * <p>
    * No arguments.
    */
   public static final int REDIRECT_NOT_SUPPORTED_BY_CONVERTERS   = 6015;

   /**
    * the specified MIME output type is not valid
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the MIME type specified</TD></TR>
    * </TABLE>
    */
   public static final int MIME_CONV_INVALID_OUTPUT_TYPE   = 6016;

   /**
    * there must be 1 pipe in the data set for MIME output generation
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the number of pipes in the data set</TD></TR>
    * </TABLE>
    */
   public static final int MIME_CONV_ONE_PIPE_REQD         = 6017;

   /**
    * a query pipe is required for MIME output generation
    * <p>
    * No arguments.
    */
   public static final int MIME_CONV_QUERY_PIPE_REQD      = 6018;

   /**
    * there must be 1 mapping in the data mapper for MIME output generation
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the number of mappings in the data mapper</TD></TR>
    * </TABLE>
    */
   public static final int MIME_CONV_ONE_MAPPING_REQD      = 6019;

   /**
    * there must be 1 column used in the data mapping
    * for MIME output generation
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the number of columns in the data mapping</TD></TR>
    * </TABLE>
    */
   public static final int MIME_CONV_ONE_COLUMN_REQD      = 6020;

   /**
    * the result set must return only 1 column for MIME output generation
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the number of columns in the result set</TD></TR>
    * </TABLE>
    */
   public static final int MIME_CONV_MULTICOL_RESULT      = 6021;

   /**
    * The result set must contain only 1 row for MIME output generation
    * <p>
    * No arguments.
    */
   public static final int MIME_CONV_MULTIROW_RESULT      = 6022;

   /**
    * Multiple root elements defined in mapping
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>root element 1</TD></TR>
    * <TR><TD>1</TD><TD>root element 2</TD></TR>
    * </TABLE>
    */
   public static final int XML_TWO_ROOT_ELEMENTS         = 6023;

   /**
    * An XSL variable name was used for both a link URL name
    * and a data mapping field.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the variable name</TD></TR>
    * </TABLE>
    */
   public static final int XML_VAR_LINK_AND_MAPPING = 6024;

   /**
    * mapping to XML parent nodes (nodes with children nodes) is not
    * supported
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the XML parent node being mapped to</TD></TR>
    * </TABLE>
    */
   public static final int XML_PARENT_MAPPING_NOT_SUPPORTED   = 6029;

   /**
    * A virtual path could not be converted to a physical path.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The virtual path</TD></TR>
    * </TABLE>
    */
   public static final int VFS_CONVERT_PATH_ERROR = 6030;

   /**
    * Can't convert source type to destination type
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the source type</TD></TR>
    * <TR><TD>1</TD><TD>the destination type</TD></TR>
    * </TABLE>
    */
   public static final int DATA_INVALID_CONVERSION = 6031;

   /**
    *  A call is being mapped to a key column used by a link,
    *  any associated HTML parameters with the call will be passed with
    *  key infomation for that column.
    * <p>
    * No arguments.
    */
   public static final int WARN_CALL_MAPPED_KEY_COLUMN_ON_LINK = 6032;

   /**
    * Can't convert source type to destination type with reason
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the source type</TD></TR>
    * <TR><TD>1</TD><TD>the destination type</TD></TR>
    * <TR><TD>2</TD><TD>reason/exception info</TD></TR>
    * </TABLE>
    */
   public static final int DATA_CANNOT_CONVERT_WITH_REASON = 6033;

   /**
    * The handler cannot return multiple result sets.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>number of result sets</TD></TR>
    * </TABLE>
    */
   public static final int CANNOT_RETURN_MULTIPLE_RESULT_SETS = 6034;

   /**
    * The internal result handler cannot return the data in the specified way
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the result handler method</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_INTERNAL_RESULT_CALL = 6035;

   /**
    * An exception occurred while processing the internal result handler call.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>number of result sets</TD></TR>
    * </TABLE>
    */
   public static final int INTERNAL_RESULT_CALL_EXCEPTION = 6036;

   /**
    * An exception occurred while processing the internal resquest handler call.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>number of result sets</TD></TR>
    * </TABLE>
    */
   public static final int INTERNAL_REQUEST_CALL_EXCEPTION = 6037;

   /**
    * The internal request was not authorized to perform the requested action.
    * This error code is required because the <code>IPSInternalRequest</code>
    * interface does not allow <code>PSAuthorizationException</code> to be
    * thrown by the get methods.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR>
    * <TD>0</TD><TD>Text of the <code>PSAuthorizationException</code></TD>
    * </TR>
    * </TABLE>
    */
   public static final int INTERNAL_REQUEST_AUTHORIZATION_EXCEPTION = 6038;

   /**
    * The view name specified is not defined in the current set of views..
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR>
    * <TD>0</TD><TD>The name of the view</code></TD>
    * </TR>
    * </TABLE>
    */
   public static final int VIEW_NOT_FOUND = 6039;

   /**
    * The internal request was not authorized to perform the requested action.
    * This error code is required because the <code>IPSInternalRequest</code>
    * interface does not allow <code>PSAuthorizationException</code> to be
    * thrown by the get methods.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR>
    * <TD>0</TD><TD>Text of the <code>PSAuthorizationException</code></TD>
    * </TR>
    * </TABLE>
    */
   public static final int
      INTERNAL_REQUEST_AUTHENTICATION_FAILED_EXCEPTION = 6040;

   /**
    * The requested macro extractor class was not found.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the requested class</TD></TR>
    * <TR><TD>1</TD><TD>the stack trace</TD></TR>
    * </TABLE>
    */
   public static final int MACRO_EXTRACTOR_CLASS_NOT_FOUND = 6041;

   /**
    * The requested macro extractor class instantiation failed.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the requested class</TD></TR>
    * <TR><TD>1</TD><TD>the stack trace</TD></TR>
    * </TABLE>
    */
   public static final int MACRO_EXTRACTOR_INSTANTIATION_FAILED = 6042;

   /**
    * Insufficient access for the requested macro extractor class.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the requested class</TD></TR>
    * <TR><TD>1</TD><TD>the stack trace</TD></TR>
    * </TABLE>
    */
   public static final int MACRO_EXTRACTOR_ILLEGAL_ACCESS = 6043;

   /**
    * Invocation target error while instantiating the requested macro
    * extractor class.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the requested class</TD></TR>
    * <TR><TD>1</TD><TD>the stack trace</TD></TR>
    * </TABLE>
    */
   public static final int MACRO_EXTRACTOR_INVOCATION_TARGET_ERROR = 6044;

   /**
    * A required constructor was not found while instantiating the requested
    * macro extractor class.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the requested class</TD></TR>
    * <TR><TD>1</TD><TD>the stack trace</TD></TR>
    * </TABLE>
    */
   public static final int MACRO_EXTRACTOR_NO_SUCH_METHOD = 6045;
   
   /**
    * An invalid required parameter was supplied to this macro extactor.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the invalid parameter</TD></TR>
    * <TR><TD>1</TD><TD>The name of the macro</TD></TR>
    * <TR><TD>2</TD><TD>The value of the invalid parameter</TD></TR>
    * <TR><TD>3</TD><TD>The error message</TD></TR>
    * </TABLE>
    */
   public static final int MACRO_EXTRACTOR_INVALID_PARAMETER = 6046;
}
