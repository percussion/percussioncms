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

package com.percussion.data;

import com.percussion.debug.PSDebugLogHandler;
import com.percussion.debug.PSTraceMessageFactory;
import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.design.objectstore.PSLiteral;
import com.percussion.design.objectstore.PSLiteralSet;
import com.percussion.log.PSLogHandler;
import com.percussion.server.PSRequest;
import com.percussion.util.IPSUtilErrors;
import com.percussion.util.PSDataTypeConverter;
import com.percussion.util.PSPurgableTempFile;
import com.percussion.util.PSSqlHelper;
import com.percussion.utils.jdbc.oracle.wrapper.OracleTools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The PSStatementColumn class defines a column which is bound to a
 * particular statement. It provides a way for data to be stored in the
 * statement without knowledge of the actual statement or column. This
 * is contained within PSStatementColumnMapper objects. For each instance,
 * after call {@link #setData(PSExecutionData, PreparedStatement, int)}, the
 * caller is responsible to make a call to {@link #releaseData()} to release
 * the resources that may be left open by <code>setData()</code>.
 *
 * @see         PSStatementColumnMapper
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public class PSStatementColumn {
   /**
    * Create a column binding for the specified statement. The object knows
    * the statement and bind position for its column. This allows the
    * setData method to be called with the data for this column, without
    * regard for where it is going.
    *
    * @param   xmlField      the XML field containing the data for this column
    *
    * @param   type         the java.sql.Type data type to use when setting column data
    *
    * @param   col         the backend column object
    */
   public PSStatementColumn(IPSReplacementValue value, int type, PSBackEndColumn col)
   {
      this(value, type, col, null);
   }

   /**
    * Create a column binding for the specified statement. The object knows
    * the statement and bind position for its column. This allows the
    * setData method to be called with the data for this column, without
    * regard for where it is going.
    *
    * @param   xmlField      the XML field containing the data for this column
    *
    * @param   type         the java.sql.Type data type to use when setting column data
    *
    * @param   col         the backend column object
    *
    * @param   lci         The lob column initializer to use when supplying
    *                      placeholders for lob columns, can be
    *                      <code>null</code>, will be ignored when
    *                      <code>type</code> is not <code>Types.BLOB</code> or
    *                      <code>Types.CLOB</code>.
    */
   public PSStatementColumn(IPSReplacementValue value, int type,
      PSBackEndColumn col, IPSLobColumnInitializer lci)
   {
      super();

      if (type == Types.BLOB || type == Types.CLOB)
         m_lobColInitializer = lci;

      m_dataExtractor =
         PSDataExtractorFactory.createReplacementValueExtractor(value);
      m_dataType   = type;
      m_backEndColumn = col;

      /* Check to see if our driver/server is tweaked by the builder
      */
       PSBackEndTable table = (m_backEndColumn == null) ? null :
                              m_backEndColumn.getTable();

      if (table != null) 
      {
          String datasource = table.getDataSource();
         
         m_untweakNulls = PSDatabaseMetaData.unTweakDriverNulls(
            datasource);
      }
      else 
      {
         // Default to not tweaked, as of now most drivers aren't
         m_untweakNulls = false;
      }
   }

   /**
    * Store the specified data as the value of this bound column.
    * <p>
    * NOTE: There may be resources that were left open by this method, for
    *       example, an InputStream may be left open when set the data from
    *       a file. It is caller's resposibility to call {@link releaseData()
    *       releaseData} to release the resources.
    *
    * @param   data        the execution data associated with this plan
    *
    * @param   stmt        the prepared statement
    *
    * @param   bindStart   the starting position (1-based) to bind columns
    *
    * @return              the next bind position (1-based)
    *
    * @throws  SQLException   if a SQL error occurs
    */
   public int setData(PSExecutionData data, PreparedStatement stmt, int bindStart)
      throws SQLException, PSDataExtractionException
   {
      /* this flags that the column data was written as the place holder.
       * it is used for PSNativeStatement which builds the SQL string
       * each execution, allowing operators, etc. to be overwritten.
       */
      if (m_dataType == Types.NULL)
         return bindStart;

      /* If this is a clob or blob and there has been an initialization
       * replacement, then there was no bind on this column
       */
      if (m_lobColInitializer != null)
         return bindStart;

      Object value = null;
      if (m_dataType == Types.ARRAY)
      {
         value = m_dataExtractor.extract(data);
         if (value == null)
         {
            value = new ArrayList();
         }
         else if (!(value instanceof Collection))
         {
            Collection coll = new ArrayList();
            coll.add(value);
            value = coll;
         }
      }
      else
      {
         value = getPreparedValue(data, EMPTY_STRING);
      }

      PSLogHandler lh = data.getLogHandler();
      if (lh.isFullUserActivityLoggingEnabled()) {
         Object[] args = { String.valueOf(bindStart), value.toString() };
         lh.logFullUserActivityAction(
            data.getRequest(), IPSBackEndErrors.LOG_BOUND_COL_DATA, args,
            false);   // don't force logging, do it if permitted
      }

      PSDebugLogHandler dh = (PSDebugLogHandler)lh;
      if (dh.isTraceEnabled(PSTraceMessageFactory.RESOURCE_HANDLER_FLAG))
      {
         // don't print the entire value in case it's a big file
         String traceValue = value.toString();
         if (traceValue.length() > 25)
            traceValue = traceValue.substring(0, 25) + "...";
         Object args[] = {traceValue, "traceResourceHandler_bindValue"};
         dh.printTrace(PSTraceMessageFactory.RESOURCE_HANDLER_FLAG, args);
      }

      if (value instanceof URL)
      {
         value = new File(((URL) value).getFile());
      }

      if (value instanceof String)
      {
         String myString = (String) value;

         boolean isNull = (myString.length() == 0);
         /* now set the object based on data type. We may not have been able
          * to determine the data type when initializing, so let's look at
          * the object type and attempt to determine the best type. This
          * fixes the problem reported in bug id Rx-99-12-0001, though it
          * is not a fool-proof solution.
          */
         int dt = getAppropriateDataType(data, stmt, bindStart, value, isNull);

         if (isNull)
            PSSqlHelper.setNullParameter(stmt, bindStart, dt);
         else
            try
            {
               PSSqlHelper.setDataFromString(stmt, bindStart, myString, dt);
            }
            catch (IOException e)
            {
               throw new PSDataExtractionException(
                  com.percussion.util.IPSUtilErrors.BASE64_DECODING_EXCEPTION,
                  value);
            }

      } else
      {
         int dt = getAppropriateDataType(data, stmt, bindStart, value, false);

         if (value instanceof byte[])
         {
            try
            {
               PSSqlHelper.setDataFromByteArray(stmt, bindStart, (byte[])value,
                  dt);
            }
            catch (IOException e)
            {
               throw new PSDataExtractionException(
                  IPSUtilErrors.BASE64_DECODING_EXCEPTION, value);
            }
         }
         else if (value instanceof File)
         {
            String charSet = null;
            if (value instanceof PSPurgableTempFile)
            {
               PSPurgableTempFile f = (PSPurgableTempFile) value;
               charSet = f.getCharacterSetEncoding();
            }
            if ((charSet == null) || (charSet.length() == 0))
            {
               charSet = data.getRequest().getFileCharacterSet();
            }
            m_inputStream = PSSqlHelper.setDataFromFile(stmt, bindStart,
               (File)value, dt, charSet);
         }
         else if (value instanceof Number)   // fix for bug id Rx-99-11-0028
         {
            PSSqlHelper.setDataFromNumber(stmt, bindStart, (Number)value, dt);
         }
         else if ((value instanceof Collection) && (m_dataType == Types.ARRAY))
         {
            OracleTools.setDataFromCollection(stmt, bindStart,
               (Collection)value, dt);
         }
         else   // hope JDBC can figure it out
            stmt.setObject(bindStart, value, dt);
      }

      /* add table name to the table change data in case no columns are wanted
       * by the listeners
       */
      PSTableChangeData tableChangeData = data.getTableChangeData();
      if(tableChangeData != null)
      {
         String tableName = m_backEndColumn.getTable().getTable();
         tableChangeData.addTable(tableName);

         /* Check whether this column is expected by any of the table change
          * listeners and add the column value to the table change data
          */
         if(value instanceof String || value instanceof Number)
         {
            String columnName = m_backEndColumn.getColumn();
            if(tableChangeData.expectsColumn(tableName, columnName))
            {
               String myString = null;
               if(value instanceof String)
                  myString = (String) value;
               else if(value instanceof Number)
                  myString = String.valueOf(value);

               if(myString != null)
                  tableChangeData.addColumnValue(tableName, columnName, myString);
            }
         }
      }



      return ++bindStart;
   }

   /**
    * Close the input stream that may be opened by the column data. This method
    * should be called after the column data is no longer needed.
    */
   public void releaseData()
   {
      if (m_inputStream != null)
      {
         try { m_inputStream.close(); } catch (Exception e){}
         m_inputStream = null;
      }
   }

   private int getAppropriateDataType( PSExecutionData data,
                                       PreparedStatement stmt,
                                       int bindStart,
                                       Object value,
                                       boolean isNull)
      throws java.sql.SQLException
   {
      int dt = m_dataType;

      if (dt == UNKNOWN_JDBC_TYPE)
      {
         // try to get the type from the result set's meta-data
         if (dt == UNKNOWN_JDBC_TYPE)
            dt = determineColumnTypeFromMetaData();

         // if still unknown, do it based on value object
         if (dt == UNKNOWN_JDBC_TYPE)
            dt = resolveUnkownDataType(stmt, bindStart, value);
      } else
      {
         if (isNull)
         {
            /* If this is one of the kludged drivers, use the native
               data type instead of the jdbc data type we turned it into */
            if (m_untweakNulls)
            {
               dt = determineColumnTypeFromMetaData();
               if (dt != UNKNOWN_JDBC_TYPE)
               {
                  return dt;
               }
            }
         }
      }

      return dt;
   }

   /**
    * Get the place holder to use in the statement. When the column is
    * being used for a native statement (type = Types.NULL) we are
    * actually writing the current value as the place holder. This is
    * used by PSNativeStatement
    *
    * @param   data     the execution data associated with this plan
    *
    * @return            the place holder to use in the SQL statement
    */
   public String getPlaceHolder(PSExecutionData data)
      throws com.percussion.data.PSDataExtractionException
   {
      if (Types.NULL == m_dataType)
         return getValue(data).toString();
      else if (m_lobColInitializer != null)  // LOB type
      {
         Object o = getPreparedValue(data, null);
         return (m_dataType == Types.BLOB) ?
            m_lobColInitializer.getBlobInitializer(o, isPreparedObjectNull(o)):
            m_lobColInitializer.getClobInitializer(o, isPreparedObjectNull(o));
      }
      else
         return m_placeHolder;
   }

   /**
    * Sets the place holder to use in prepared statements for this object.
    *
    * @param placeHolder the string to se as a place holder for this object
    * in preparedStatements. May not be <code>null</code>, may be empty.
    */
   public void setPlaceHolder(String placeHolder)
   {
      if (placeHolder == null)
         throw new IllegalArgumentException("placeHolder may not be null");
      m_placeHolder = placeHolder;
   }

   /**
    * Determine if the current column value which would be bound is NULL.
    * If the statement block supports omit when NULL, it can choose to
    * ignore this due to the NULL value specification.
    *
    * @param   data     the execution data associated with this plan
    *
    * @return            <code>true</code> if the value to be bound is NULL
    *
    * @throws  PSDataExtractionException  if there is a data extraction
    *          exception determining what the current column value is
    */
   public boolean isNull(PSExecutionData data)
      throws PSDataExtractionException
   {
      Object o = getPreparedValue(data, null);

      return isPreparedObjectNull(o);
   }

   /**
    * Determine if the current column value which would be bound is NULL.
    * If the statement block supports omit when NULL, it can choose to
    * ignore this due to the NULL value specification.
    *
    * @param   o     the object extracted and prepared from execution data
    *                associated with this plan
    *
    * @return         <code>true</code> if the value of the object is
    *                <code>NULL</code>, <code>false</code> otherwise
    */
   private static boolean isPreparedObjectNull(Object o)
   {
      if (o == null)
         return true;

      if (o instanceof String)
         return (((String)o).length() == 0);
      else if (o instanceof byte[])
         return (((byte[])o).length == 0);
      else if (o instanceof java.io.File)
         return (((java.io.File)o).length() == 0);

      return false;
   }

   /**
    * Get the type that this statement column was constructed with.
    *
    * @return the jdbc type
    */
   public int getType()
   {
      return m_dataType;
   }

   /**
    * Store the specified data as the value of this bound column.
    * The empty string ("") is returned by default.
    *
    * @param   data     the execution data associated with this plan
    *
    * @throws  PSDataExtractionException  if there is a data extraction
    *          exception determining what the current column value is
    */
   public Object getValue(PSExecutionData data)
      throws PSDataExtractionException
   {
      return getValue(data, EMPTY_STRING);
   }

   /**
    * Store the specified data as the value of this bound column.
    *
    * @param   data           the execution data associated with this plan
    *
    * @param   defaultValue   the default value to use if not found
    *
    * @throws  PSDataExtractionException  if there is a data extraction
    *          exception determining what the current column value is
    */
   public Object getValue(PSExecutionData data, Object defaultValue)
      throws PSDataExtractionException
   {
      if (data == null)
         return defaultValue;

      PSRequest   req = data.getRequest();
      Object o = m_dataExtractor.extract(data);

      return (o == null) ? defaultValue : o;
   }

   /**
    * Get the specified data for binding this column's value, preparing
    * it from a list or literal.
    *
    * @param   data           the execution data associated with this plan
    *
    * @param   defaultValue   the default value to use if not found
    *
    * @throws  PSDataExtractionException  if getValue throws a data extraction
    *          exception retrieving the current column value
    */
   public Object getPreparedValue(PSExecutionData data, Object defaultValue)
      throws PSDataExtractionException
   {
      Object o = getValue(data, defaultValue);

      if (o instanceof PSLiteralSet)
         o = o.toString();
      else if (o instanceof List)
      {
         // if this is from a List, only grab the first entry
         List al = (List)o;
         if (al.size() == 0)
            o = defaultValue;
         else
            o = al.get(0);
      }

      if (o instanceof PSLiteral )
         o = o.toString();
      else if (o == null)
         o = defaultValue;

      return o;
   }

   /**
    * Get the data extractor used to get the replacement value which will
    * be used for this column.
    *
    * @return            the extractor for the replacement value
    */
   public IPSDataExtractor getReplacementValueExtractor()
   {
      return m_dataExtractor;
   }


   private int resolveUnkownDataType(
      PreparedStatement stmt, int bindStart, Object value)
      throws java.sql.SQLException
   {
      /* For now, we are returning the type based on the data passed in.
       * This helps correct bug id Rx-99-12-0001.
       *
       * In the future, we will try to determine the data type from the
       * SQL source, and then we can set the m_dataType var permanently.
       */

      int dt = Types.JAVA_OBJECT;

      if (value instanceof String)
         dt = Types.VARCHAR;
      else if (value instanceof byte[])
         dt = Types.VARBINARY;
      else if (value instanceof java.io.File)
         dt = Types.VARBINARY;   // todo: examine the file contents for better guess?
      else if (value instanceof Boolean)
         dt = Types.BIT;
      else if (value instanceof Byte)
         dt = Types.TINYINT;
      else if (value instanceof Short)
         dt = Types.SMALLINT;
      else if (value instanceof Integer)
         dt = Types.INTEGER;
      else if (value instanceof Long)
         dt = Types.BIGINT;
      else if (value instanceof BigInteger)
         dt = Types.BIGINT;
      else if (value instanceof Float)
         dt = Types.FLOAT;
      else if (value instanceof Double)
         dt = Types.DOUBLE;
      else if (value instanceof BigDecimal)
         dt = Types.NUMERIC;
      else if ((value instanceof java.util.Date) ||
               (value instanceof java.sql.Timestamp))
         dt = Types.TIMESTAMP;
      else if (value instanceof java.sql.Date)
         dt = Types.DATE;
      else if (value instanceof java.sql.Time)
         dt = Types.TIME;

      return dt;
   }

   /**
    * Deletegates to util class.
    *
    * @deprecated  Use {@link
    * com.percussion.util.PSDataTypeConverter#getBinaryFromBase64(String)}
    * instead.
    */
   public static byte[] getBinaryFromBase64(String value)
      throws PSDataExtractionException
   {
      try
      {
         return PSDataTypeConverter.getBinaryFromBase64(value);
      }
      catch (Exception e)
      {
         throw new PSDataExtractionException(
            IPSUtilErrors.BASE64_DECODING_EXCEPTION,
            value);
      }
   }

   int determineColumnTypeFromMetaData()
   {
      if (m_nativeDataType == UNKNOWN_JDBC_TYPE)
      {
         PSBackEndTable table = (m_backEndColumn == null) ? null :
                                 m_backEndColumn.getTable();

         try {
            PSDatabaseMetaData dbMeta = (table == null) ? null :
               PSOptimizer.getCachedDatabaseMetaData(table);

            PSTableMetaData tabMeta = (dbMeta == null) ? null :
               dbMeta.getTableMetaData(dbMeta.getConnectionDetail().getOrigin(), 
                  table.getTable());

            if (tabMeta != null)
            {
               // get the type
               m_nativeDataType = tabMeta.getColumnType(m_backEndColumn.getColumn());
            }
         } catch (java.sql.SQLException e)
         {
         }
      }

      return m_nativeDataType;
   }

   /**
    * Our flag that we don't yet know what the JDBC data type is. If
    * a DBMS vendor or JavaSoft ever adds, we're screwed.
    */
   public static final int         UNKNOWN_JDBC_TYPE      = -99999;

   protected static final int      DATA_FROM_PARAM      = 1;
   protected static final int      DATA_FROM_CGIVAR      = 2;
   protected static final int      DATA_FROM_XML         = 3;

   /**
    * The lob column initializer class, used to replace placeholders
    * for lob column base update statements.
    */
   protected IPSLobColumnInitializer m_lobColInitializer = null;

   protected IPSDataExtractor      m_dataExtractor;
   protected int                  m_dataType;
   protected int                 m_nativeDataType = UNKNOWN_JDBC_TYPE;
   protected PSBackEndColumn     m_backEndColumn;
   protected boolean             m_untweakNulls = false;
   protected String              m_server = null;

   private static final String   EMPTY_STRING = "";

   /**
    * The InputStream that may be left open by setData() method.
    */
   private InputStream m_inputStream = null;

   /**
    * The place holder to use in prepared statements for this object,
    * initialized to <code>" ? "</code>, modified using the
    * <code>setPlaceHoder</code> method.
    */
   private String m_placeHolder = " ? ";
}
