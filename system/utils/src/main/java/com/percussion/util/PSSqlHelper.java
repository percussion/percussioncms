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
package com.percussion.util;

import com.percussion.utils.jdbc.IPSDatasourceManager;
import com.percussion.utils.jdbc.PSConnectionDetail;
import com.percussion.utils.jdbc.PSConnectionHelper;
import com.percussion.utils.jdbc.PSJdbcUtils;
import com.percussion.utils.tools.IPSUtilsConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.naming.NamingException;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Properties;

/**
 * This is a utility class providing common functionality/definitions for
 * the use of databases and JDBC classes.
 */
public class PSSqlHelper
{

   private static final String JBOSS_WRAPPED_CLASS="org.jboss.ejb.plugins.cmp.jdbc.WrappedStatement";

   /**
    * Convenience method that looks up the Rx server's database info and calls
    * {@link #qualifyTableName(String, String, String, String)}.
    * 
    * @throws SQLException See {@link PSConnectionHelper#getDbConnection()}.
    */
   public static String qualifyTableName(String tableName) 
      throws SQLException
   {
      PSConnectionDetail detail = getDefaultConnectDetail();
      return qualifyTableName(tableName, detail.getDatabase(), detail.getOrigin(), detail.getDriver());
   }

   
   /**
    * This will create a fully qualified table name. Depending on the
    * provided driver type we will return table, owner.table or db.owner.table.
    *
    * @param db the used database, may be <code>null</code>
    * @param table the table name to qualify, must be valid
    * @param owner the table owner, may be <code>null</code>
    * @param driver the driver type to qualify for, must be valid
    * @return the fully qualified table name
    * @throws IllegalArgumentException for any illegal argument
    */
   public static String qualifyTableName(String table, String db, String owner,
                                         String driver)
   {
      if (driver == null || driver.length() == 0)
         throw new IllegalArgumentException("we need a valid driver type");

      if (table == null || table.length() == 0)
         throw new IllegalArgumentException("we need a valid table name");

      if (driver.equalsIgnoreCase(PSJdbcUtils.DB2) ||
            driver.equalsIgnoreCase(PSJdbcUtils.DERBY_DRIVER))
      {
         // formats owner.table or table
         if ((owner == null) || (owner.trim().length() == 0))
            return table;
         return owner + "." + table;
      }

      if (driver.equals(PSJdbcUtils.ODBC) ||
               driver.equals(PSJdbcUtils.SPRINTA) ||
               driver.equals(PSJdbcUtils.JTDS_DRIVER)||
               driver.equals(PSJdbcUtils.MICROSOFT_DRIVER))
      {
         // can have the format db.owner.table, db..table or just table
         if (db == null || db.length() == 0)
         {
            if (owner == null || owner.length() == 0)
               return table;
            else
               return owner + "." + table;
         }
         else
         {
            if (owner == null || owner.trim().length() == 0)
               return db + ".." + table;
            else
               return db + "." + owner + "." + table;
         }
      }
      else if (driver.startsWith(PSJdbcUtils.ORACLE_PRIMARY))
      {

         if (owner == null || owner.trim().length() == 0)
         {
            if (db == null || db.trim().length() == 0)
               return table;
            else if (db.startsWith("@"))
               return table + db;
            else
               return db + "." + table;
         }
         else
         {
            if (db == null || db.trim().length() == 0)
               return owner + "." + table;
            else if (db.startsWith("@"))
               return owner + "." + table + db;
            else
               return owner + "." + db + "." + table;
         }
      }
      else if (driver.equalsIgnoreCase(PSJdbcUtils.MYSQL_DRIVER))
      {
         if (db == null || db.trim().length() == 0)
            return table;
         else
            return db + "." + table;   
      }

      // by default assume its only the table name
      return table;
   }

   /**
    * This will create a fully qualified view name. Depending on the
    * provided driver type we will return view, schema.view or
    * db.schema.view.
    *
    * Note: one only needs to call this method to qualify a view name
    * when a view is being created. For regular queries it appears to
    * be OK to use the qualifyTableName method for both tables
    * and views.
    *
    * @param db the used database, may be <code>null</code>
    * @param view the view name to qualify, never <code>null</code>
    * or <code>empty</code>.
    * @param owner the table owner, may be <code>null</code>
    * @param driver the driver type to qualify for, never <code>null</code>
    * never <code>empty</code>.
    * @return the fully qualified table name
    */
   public static String qualifyViewName(String view, String db, String owner,
                                        String driver)
   {
      if (driver == null || driver.length() == 0)
         throw new IllegalArgumentException("we need a valid driver type");

      if (view == null || view.length() == 0)
         throw new IllegalArgumentException("we need a valid table name");

      if (driver.equalsIgnoreCase(PSJdbcUtils.DB2) ||
            driver.equalsIgnoreCase(PSJdbcUtils.DERBY_DRIVER))
      {
         // formats owner.table or table
         if ((owner == null) || (owner.trim().length() == 0))
            return view;
         return owner + "." + view;
      }

      if (driver.equals(PSJdbcUtils.ODBC) ||
               driver.equals(PSJdbcUtils.SPRINTA) ||
               driver.equals(PSJdbcUtils.JTDS_DRIVER))
      {
         // SQL appears to freak out if we supply the db name
         if (owner == null || owner.length() == 0)
            return view;
         else
            return owner + "." + view;
      }
      else if (driver.startsWith(PSJdbcUtils.ORACLE_PRIMARY))
      {

         if (owner == null || owner.trim().length() == 0)
         {
            if (db == null || db.trim().length() == 0)
               return view;
            else if (db.startsWith("@"))
               return view + db;
            else
               return db + "." + view;
         }
         else
         {
            if (db == null || db.trim().length() == 0)
               return owner + "." + view;
            else if (db.startsWith("@"))
               return owner + "." + view + db;
            else
               return owner + "." + db + "." + view;
         }
      }

      // by default assume its only the view name
      return view;
   }


   /**
    * This will create a fully qualified primary key name. Depending on the
    * provided driver type we will return key, or db.key.
    *
    * @param db the used database, may be <code>null</code>
    * @param owner the table owner, may be <code>null</code>
    * @param driver the driver type to qualify for, must be valid
    * @return the fully qualified table name
    * @throws IllegalArgumentException for any illegal argument
    */
   public static String qualifyPrimaryKeyName(String key, String db,
      String owner, String driver)
   {
      if (driver == null || driver.length() == 0)
         throw new IllegalArgumentException("we need a valid driver type");

      if (key == null || key.length() == 0)
         throw new IllegalArgumentException("we need a valid table name");
         
      //for db2 return key only
      if (driver.equalsIgnoreCase(PSJdbcUtils.DB2))
         return key;

      // by default assume its only the key name
      return key;
   }

   /**
    * Extracts the catalog, origin and table from the supplied, possibly
    * fully qualified tableName. If found, the catalog and origin are returned
    * in the supplied buffers. The table name is returned from the method.
    * If tableName doesn't include an origin or catalog, the
    * corresponding buffers are not touched. The unqualified table name is
    * returned. If the driver is not recognized, the default pattern will be
    * assumed. The default pattern is 'catalog.origin.table'.
    *
    * @param driver The JDBC subprotocol name that is supplied as part of the
    *    connect string. For example: 'oracle:thin', 'inetdae7'. If <code>null
    *    </code> or empty, the default pattern is used. This is case sensitive.
    *
    * @param tableName Some form of the table name. If <code>null</code> or
    *    empty, the supplied buffers are untouched and the empty string is
    *    returned.
    *
    * @param originBuf If <code>tableName</code> is not <code>null</code>
    *    or empty, and it includes an origin/schema, that part of the name
    *    will be appended to this buffer. If this buf is <code>null</code>,
    *    it is skipped.
    *
    * @param catalogBuf If <code>tableName</code> is not <code>null</code>
    *    or empty, and it includes a catalog, that part of the name
    *    will be appended on this buffer. If this buf is <code>null</code>,
    *    it is skipped.
    *
    * @return If <code>tableName</code> is not <code>null</code>
    *    or empty, the base part of the name (w/o origin or db). Otherwise,
    *    the empty string is returned.
    */
   public static String parseTableName( String driver, String tableName,
         StringBuilder originBuf, StringBuilder catalogBuf )
   {
      if ( null == tableName || tableName.trim().length() == 0 )
         return "";
      if ( null == driver )
         driver = "";

      // after each if block below, tableName will be stripped of qualifiers
      int dotPos = tableName.indexOf('.');
      boolean hasDot = dotPos >= 0;
      if (driver.equals(PSJdbcUtils.DB2) || 
            driver.equals(PSJdbcUtils.DERBY_DRIVER) )
      {
         // format: db.table or table
         if ( hasDot )
         {
            if ( null != catalogBuf )
               catalogBuf.append(tableName.substring(0, dotPos));
            tableName = tableName.substring(dotPos+1);
         }
      }
      else if (driver.startsWith(PSJdbcUtils.ORACLE_PRIMARY))
      {
         // can have the format table, owner.table, table@db or owner.table@db
         int atPos = tableName.indexOf('@');
         if ( atPos > 0 )
         {
            if ( null != catalogBuf )
               catalogBuf.append(tableName.substring(atPos+1));
            tableName = tableName.substring(0, atPos);
         }
         if ( hasDot )
         {
            if ( null != originBuf )
               originBuf.append(tableName.substring(0, dotPos));
            tableName = tableName.substring(dotPos+1);
         }
      }
      else
      {
         // default to most common format; odbc, sprinta
         // formats: db.owner.table, db..table, owner.table or table
         if ( hasDot )
         {
            int lastDotPos = tableName.lastIndexOf('.');
            if ( lastDotPos > dotPos )
            {
               // we got all parts
               if ( null != catalogBuf )
                  catalogBuf.append(tableName.substring(0, dotPos));
               tableName = tableName.substring(dotPos+1);
            }
            lastDotPos = tableName.indexOf('.');
            if ( null != originBuf )
               originBuf.append(tableName.substring(0, lastDotPos));
            tableName = tableName.substring(lastDotPos+1);
         }
      }
      return tableName;
   }

   /**
    * Given a String representation of the data, sets the data on the prepared
    * statement using the supplied datatype.
    *
    * @param stmt The PreparedStatement, may not be <code>null</code>.
    * @param bindStart The index of the parameter to bind.
    * @param value The value to bind.  May not be <code>null</code>.
    * @param dataType The jdbc datatype, one of the constant values from
    * {@link java.sql.Types}.
    *
    * @throws IllegalArgumentException if any parameter is invalid.
    * @throws SQLException if an error occurs setting the value.
    * @throws IOException if an error occurs converting a binary value from
    * a Base64Encoded format.
    */
   public static void setDataFromString(PreparedStatement stmt, int bindStart,
      String value, int dataType) throws SQLException, IOException
   {
      if (stmt == null)
         throw new IllegalArgumentException("stmt may not be null");

      if (value == null)
         throw new IllegalArgumentException("value may not be null");


      switch (dataType) {
         case Types.INTEGER:
            stmt.setInt(bindStart, Integer.parseInt(value.trim()));
            break;

         case Types.SMALLINT:
            stmt.setShort(bindStart, Short.parseShort(value.trim()));
            break;

         case Types.BIT:
            value = value.trim();
            stmt.setBoolean(bindStart,
               (value.equals("1") || value.equalsIgnoreCase("true")) );
            break;

         case Types.TINYINT:
            stmt.setByte(bindStart, Byte.parseByte(value.trim()));
            break;

         case Types.BIGINT:
            stmt.setLong(bindStart, Long.parseLong(value.trim()));
            break;

         case Types.DOUBLE:
            stmt.setDouble(bindStart, Double.valueOf(value.trim()).doubleValue());
            break;

         case Types.NUMERIC:
         case Types.DECIMAL:
            stmt.setBigDecimal(bindStart, new BigDecimal(value.trim()));
            break;

         case Types.FLOAT:
         case Types.REAL:
            stmt.setObject(bindStart, Float.valueOf(value.trim()), dataType);
            break;

         case Types.DATE:
            Date date = null;
            java.util.Date day = null;
            value = value.trim();
            try {
               day = PSDataTypeConverter.parseStringToDate(value);
               if (day != null)
                  date = new Date(day.getTime());
            } catch (Exception e) { }

            if (date != null)
               stmt.setDate(bindStart, date);
            else   // finally, try for a driver string convert
               stmt.setObject(bindStart, value, dataType);
            break;

         case Types.TIME:
            Time time = null;
            java.util.Date timeAsDay = null;
            value = value.trim();

            try {   // first pass use SQL Time, format = HH:mm:ss
               time = Time.valueOf(value);
            } catch (Exception e) { }

            if (time == null) {   // next pass use Java Date
               try {
                  timeAsDay = PSDataTypeConverter.parseStringToDate(value);
                  time = new Time(timeAsDay.getTime());
               } catch (Exception e) { }
            }

            if (time != null)
               stmt.setTime(bindStart, time);
            else   // finally, try for a driver string convert
               stmt.setObject(bindStart, value, dataType);
            break;

         case Types.TIMESTAMP:
            Timestamp timestamp = null;
            java.util.Date stampDay = null;
            value = value.trim();

            try
            {
               /* first pass use SQL Timestamp,
                * format = yyyy-mm-dd HH:mm:ss.fffffffff
                */
               timestamp = Timestamp.valueOf(value);
            } catch (Exception e) { }

            if (timestamp == null) {   // next pass use Java Date
               try {
                  stampDay = PSDataTypeConverter.parseStringToDate(value);
                  if (stampDay != null)
                     timestamp = new Timestamp(stampDay.getTime());
               } catch (Exception e) { }
            }

            if (timestamp != null)
            {
               stmt.setTimestamp(bindStart, timestamp);
            }
            else
            {   // finally, try for a driver string convert
               stmt.setObject(bindStart, value, dataType);
            }
            break;

         case Types.BINARY:
         case Types.VARBINARY:
         case Types.LONGVARBINARY:
         case Types.BLOB:
            /* the big question here is what's the source format?
             * it should be either hex or base64. How do we figure this
             * out?! For now, we'll analyze the data and take a guess.
             */
            byte[] binData = PSDataTypeConverter.getBinaryFromString(value);
            setDataFromByteArray(stmt, bindStart, binData, dataType);
            break;

         case Types.CHAR:
         case Types.VARCHAR:
         case Types.LONGVARCHAR:
         case Types.CLOB:
            /* 
            * For Oracle driver, always use "setCharacterStream".
            * "setString" fails with ORA-17070 if the string's length exceeds 
            * a certain size, which is generally 25% of the column size.
            * ORA-17070: Data size bigger than max size for this type
            * TODO: Verify this string length restriction is still valid 
            *       in currently supported JDBC drivers 
            */
            boolean isOracleStmt = false;
            try
            {
               if (getOracleStatement(stmt) != null)
                  isOracleStmt = true;
            }
            catch (NoClassDefFoundError e)
            {
               // Oracle driver jar may not be in classpath,
               // isOracleStmt must be still false
            }
           /* 
            * If a String is longer than 2k, then the "setString" method for 
            * the JDBC "PreparedStatement" class will not work. 
            * Therefore, we use a "StringReader" to create a character
            * stream and use "setCharacterStream" instead.
            * TODO: Verify that this 2K restriction exists in currently 
            *       supported JDBC drivers (it may not be true anymore)
            */
            if ((isOracleStmt) || (value.length() > 1800))
            {
               stmt.setCharacterStream(bindStart, new StringReader(value),
                  value.length());
            }
            else
            {
               stmt.setString(bindStart, value);
            }
            break;

         default:
            stmt.setObject(bindStart, value, dataType);
            break;
      }
   }

   /**
    * Given a byte array, sets the data on the prepared
    * statement using the supplied datatype.
    *
    * @param stmt The PreparedStatement, may not be <code>null</code>.
    * @param bindStart The index of the parameter to bind.
    * @param value The value to bind.  May not be <code>null</code>.
    * @param dataType The jdbc datatype, one of the constant values from
    * {@link java.sql.Types}.
    *
    * @throws IllegalArgumentException if any parameter is invalid.
    * @throws SQLException if an error occurs.
    */
   public static void setDataFromByteArray(
      PreparedStatement stmt, int bindStart, byte[] value, int dataType)
      throws SQLException, IOException
   {
      if (stmt == null)
         throw new IllegalArgumentException("stmt may not be null");

      if (value == null)
         throw new IllegalArgumentException("value may not be null");

      switch (dataType) {
         case Types.BINARY:
         case Types.VARBINARY:
         case Types.LONGVARBINARY:
            stmt.setBytes(bindStart, value);
            break;

         case Types.BLOB:
            // setBinaryStream may not like an empty stream
            if (value.length == 0)
               setNullParameter(stmt, bindStart, dataType);
            else
            {
               ByteArrayInputStream bBuffer = new ByteArrayInputStream(value);
               stmt.setBinaryStream(bindStart, bBuffer, value.length);
            }
            break;

         case Types.CLOB:
         case Types.CHAR:
         case Types.VARCHAR:
         case Types.LONGVARCHAR:
            /* 
             * Be sure this character data (which is passed in a raw stream of
             *  bytes), is encoded correctly using UTF-8 (that is, 
             *  IPSUtilsConstants.RX_JAVA_ENC) before storing it in the 
             *  database.
             */
            /*
            * If a String is longer than 2k, then the "setString" method for
            * the JDBC "PreparedStatement" class will not work. Therefore, 
            * we use a StringReader to build a character stream and use 
            * "setCharacterStream" method, instead. Note that 
            * "setCharacterStream"s "length" parameter takes a character count, 
            * NOT a byte count, so we hold on to the string until the 
            * character count has been passed.
            * TODO: Verify that this 2K restriction exists in currently 
            *       supported JDBC drivers (it may not be true anymore)
            */
            if (value.length > 1800)
            {
               String str = new String( value, IPSUtilsConstants.RX_JAVA_ENC);
               StringReader strRdr = new StringReader(str);
               stmt.setCharacterStream(bindStart, strRdr, str.length());
            }
            else
            {
               String str = new String( value, IPSUtilsConstants.RX_JAVA_ENC);
               stmt.setString(bindStart, str);
            }
            break;

         default:
            stmt.setObject(bindStart, value, dataType);
            break;
      }
   }

   /**
    * Given a File object, sets the data on the prepared
    * statement using the supplied datatype.
    *
    * @param stmt The PreparedStatement, may not be <code>null</code>.
    * @param bindStart The index of the parameter to bind.
    * @param value The value to bind.  May not be <code>null</code>.  Must
    * reference an existing file.
    * @param dataType The jdbc datatype, one of the constant values from
    * {@link java.sql.Types}.
    * @param charset The character set to use when reading character data from
    * the file.  May be <code>null</code> or empty, in which case the default
    * characterset for the system is used.
    *
    * @return the InputSteam that is opened from the File. It may be
    *    <code>null</code> if there is no InputStream left open. It is caller's
    *    responsibility to close the input stream.
    *
    * @throws IllegalArgumentException if any parameter is invalid.
    * @throws SQLException if an error occurs.
    */
   public static InputStream setDataFromFile(
      PreparedStatement stmt, int bindStart, File value, int dataType,
      String charset)
      throws SQLException
   {
      boolean needToClose = false;
      BufferedInputStream buf = null;
      try {
         /* we can't close this stream as we don't know when the JDBC
          * driver will read from it. The finalizer should close the
          * stream, which is what we'll have to depend upon.
          */
         int fileLength = (int)value.length();

         /* Don't send the file data if it is zero length
          */
         if (fileLength == 0)
         {
            setNullParameter(stmt, bindStart, dataType);
         }
         else
         {
            buf = new BufferedInputStream(
               new FileInputStream(value));

            switch (dataType) {
               case Types.CHAR:
               case Types.VARCHAR:
               case Types.LONGVARCHAR:
               case Types.CLOB:
                  InputStreamReader reader;
                  if (charset == null || charset.length() == 0)
                  {
                     reader = new InputStreamReader(buf);
                  }
                  else
                  {
                     reader = new InputStreamReader(buf,
                        PSCharSets.getJavaName(charset));
                  }
                  /* TODO: "fileLength" should be a character count not byte.
                   */
                  stmt.setCharacterStream(bindStart, reader, fileLength);
                
                  break;

               // case Types.BINARY:
               // case Types.VARBINARY:
               // case Types.LONGVARBINARY:
               default:
                  /* if it's a binary data type it must be a binary stream.
                   * For any other data type, we'll also treat the file as a
                   * binary stream.
                   *
                   * Currently the Sybase driver (XXX: NOT USED ANYMORE)
                   * against version 12.5 will send
                   * anything set as a binary stream to the server as type
                   * IMAGE, and then refuse to do the implicit conversion if the
                   * column is of type BINARY or VARBINARY.  Since these fields
                   * have a max length of 255 in general, if the file length is
                   * 255 or less, use setBytes instead to getting an error back
                   * from the server.
                   */
                  if (fileLength <= 255)
                  {
                     // in this case we try to close the stream
                     needToClose = true;
                     byte[] bytes = new byte[fileLength];
                     buf.read(bytes, 0, fileLength);
                     stmt.setBytes(bindStart, bytes);
                  }
                  else
                  {
                     stmt.setBinaryStream(bindStart, buf, fileLength);
                  }
                  break;
            }
         }
      } catch (IOException e) {
         throw new SQLException(e.toString());
      }
      finally
      {
         if (needToClose && buf != null)
         {
            try {buf.close();} catch(IOException e){/* ignore */}
            buf = null;
         }
      }

      return buf;
   }

      

   /**
    * Attempts to determine if the supplied statement is backed by an Oracle
    * based sql statement. It does this by checking the statement itself, and
    * also checking if it is one of the known wrapper statements. If it is a
    * wrapper statement, the underlying statement is checked.
    * 
    * @param stmt The statement to check. Assumed not <code>null</code>.
    * 
    * @return If the supplied statement is of the return type, or it is a
    * wrapper around the desired type, non-<code>null</code> is returned.
    * Otherwise, <code>null</code> is returned.
    * 
    * @throws SQLException if an error occurs getting the underlying statement
    * from the wrapper statement.
    */
   public static PreparedStatement getOracleStatement(
         PreparedStatement stmt) throws SQLException
   {
      if (stmt instanceof PSPreparedStatement)
   {
         PSPreparedStatement psStmt = (PSPreparedStatement) stmt;
         stmt = psStmt.getOriginalPreparedStatement();
      }
      
      if (stmt.getClass().getName().equals(JBOSS_WRAPPED_CLASS))
      {

        try
         {
            Class<?> jbossWrapped = Class.forName(JBOSS_WRAPPED_CLASS);
            Method underlying;
                underlying = jbossWrapped.getMethod("getUnderlyingStatement");
                Object s = underlying.invoke(stmt);
                if (PreparedStatement.class.isInstance(s))
                    stmt = (PreparedStatement)s;
         }
        catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | ClassNotFoundException e)
      {
            ms_log.debug("reflection to call "+JBOSS_WRAPPED_CLASS+".getUnderlyingStatement() failed assuming not an Jboss wrapped oracle prepared statment",e);
      }

   }
      return stmt.getClass().getName().equals("oracle.jdbc.OraclePreparedStatement")
         ?  stmt : null;
   }

   /**
    * Set the data for this column using numeric (Number) data. This was
    * created to fix bug id Rx-99-11-0028. Certain JDBC drivers, such as
    * Oracle, do not support calling setObject to do numeric conversions
    * (eg, from Integer to BigDecimal). We will now take it upon ourselves
    * to do the conversion, wherever possible.
    *
    * @param stmt The PreparedStatement, may not be <code>null</code>.
    * @param bindStart The index of the parameter to bind.
    * @param value The value to bind.  May not be <code>null</code>.
    * @param dataType The jdbc datatype, on of the constant values from
    * {@link java.sql.Types}.
    *
    * @throws IllegalArgumentException if any parameter is invalid.
    * @throws SQLException if an error occurs.
    */
   public static void setDataFromNumber(
      PreparedStatement stmt, int bindStart, Number value, int dataType)
      throws java.sql.SQLException,
         com.percussion.data.PSDataExtractionException
   {
      switch (dataType)
      {
         case Types.INTEGER:
            stmt.setInt(bindStart, value.intValue());
            break;

         case Types.SMALLINT:
            stmt.setShort(bindStart, value.shortValue());
            break;

         case Types.BIT:
            stmt.setBoolean(bindStart, (value.byteValue() == 1));
            break;

         case Types.TINYINT:
            stmt.setByte(bindStart, value.byteValue());
            break;

         case Types.BIGINT:
            stmt.setLong(bindStart, value.longValue());
            break;

         case Types.DOUBLE:
            stmt.setDouble(bindStart, value.doubleValue());
            break;

         case Types.NUMERIC:
         case Types.DECIMAL:
         {
            BigDecimal num = null;
            if ( value instanceof BigDecimal )
               num = (BigDecimal) value;
            else if ( value instanceof BigInteger )
               num = new BigDecimal((BigInteger) value );
            else
               num = new BigDecimal( value.doubleValue());

            stmt.setBigDecimal( bindStart, num );
            break;
         }

         case Types.FLOAT:
         case Types.REAL:
            stmt.setObject(bindStart, new Float(value.floatValue()), dataType);
            break;

         case Types.DATE:
            // this doesn't make much sense, but not much else we can do
            Date date = new Date(value.longValue());
            stmt.setDate(bindStart, date);
            break;

         case Types.TIME:
            // this doesn't make much sense, but not much else we can do
            Time time = new Time(value.longValue());
            stmt.setTime(bindStart, time);
            break;

         case Types.TIMESTAMP:
            // this doesn't make much sense, but not much else we can do
            Timestamp timestamp = new
               Timestamp(value.longValue());

            /* in order to fix bug id Rx-99-10-0209, we had to put in
             * this kludge. The basic problem is that MS SQL Server
             * is truncating the time component as the JDBC-ODBC Bridge
             * is sending in a fraction size beyond SQL Server's
             * capabilities. Unfortunately, SQL Server truncates too
             * much of the time, going all the way down to the seconds
             * component. As such, we've decided to take the timestamp
             * and convert it to a string. This appears to fix
             * SQL Server. For performance reasons, we need to revisit
             * this and only use setString when we're talking to
             * SQL Server.
             *
             * Unfortunately, the above bug fix caused bug id
             * Rx-99-11-0030 which prevents Oracle from processing
             * timestamp (Oracle DATE) data. This was a JDBC-ODBC bridge
             * problem, which we have resolved by changing the native
             * JdbcOdbc method which binds Timestamp data to check
             * the precision of the input data rather than send the
             * max precision. This appears to fix all drivers we've
             * tested with.
             */
            stmt.setTimestamp(bindStart, timestamp);
            break;

         case Types.CHAR:
         case Types.VARCHAR:
         case Types.LONGVARCHAR:
            stmt.setString(bindStart, value.toString());
            break;

         default:   // let's hope the driver knows what to do with this!!!
            /* the big question here is what's the source format?
             * For now, we'll fall through to the default logic and hope
             * the driver can deal with this. I don't really think this
             * make a lot of sense, so I'd hope we won't run into it.
             */
            stmt.setObject(bindStart, value, dataType);
            break;
      }
   }

   /**
    * Some jdbc datatypes returned by drivers are not valid types as enumerated
    * in {@link java.sql.Types}.  This method will convert those types to
    * valid jdbc datatypes.
    *
    * @param dbmsName The name of the backend as reported by {@link
    * java.sql.DatabaseMetaData#getDatabaseProductName()}.  May not be <code>
    * null</code> or empty.
    * @param typeName The String version of the jdbc type (TYPE_NAME) reported
    * by the driver from {@link java.sql.DatabaseMetaData#getTypeInfo()} or
    * {@link java.sql.DatabaseMetaData#getColumns(String, String, String,
    * String) DatabaseMetaData.getColumns()}.  May not be <code>null</code> or
    * empty.
    * @param jdbcType The numeric type (DATA_TYPE) reported by the driver along
    * with TYPE_NAME.
    *
    * @return The corrected jdbc type for the supplied typeName
    * @throws IllegalArgumentException if dbmsName or typeName is <code>null
    * </code> or emtpy.
    */
   public static short convertNativeDataType(String dbmsName, String typeName,
      short jdbcType)
   {
      if (dbmsName == null || dbmsName.trim().length() == 0)
         throw new IllegalArgumentException(
            "dbmsName may not be null or empty");

      if (typeName == null || typeName.trim().length() == 0)
         throw new IllegalArgumentException(
            "typeName may not be null or empty");

      String sqlLowerType = typeName.toLowerCase();

      /* MS SQL Server's ODBC driver erroneously reports its
       * datetime types using its native type rather than the
       * JDBC types. As such, we need to remap these ourselves.
       * This fixes bug id Rx-99-10-0209
       * In JRE 1.3, this is no longer true.
       *
       * We have also noticed the international char types have
       * this problem, as well as sysname.
       * In JRE 1.3, now all NLS types are specified as OTHER.
       * However, this map is still needed to map these types to
       * their correct JDBC type. We do that by using the real SQL
       * type (hard-coded here) rather than the type supplied in the
       * meta data. Then in the PSRowEditorBuffer, we get the
       * underlying SQL type from the jdbcodbc result set. See
       * PSRowDataBuffer for more details about this and other bridge
       * workarounds.
       */
      if (dbmsName.equals("Microsoft SQL Server"))
      {
         if (sqlLowerType.equals("nvarchar"))
         {   // this is really a JDBC varchar
            jdbcType = Types.VARCHAR;
         }
         else if (sqlLowerType.equals("nchar"))
         {   // this is really a JDBC char
            jdbcType = Types.CHAR;
         }
         else if (sqlLowerType.equals("ntext"))
         {   // this is really a JDBC CLOB
            jdbcType = Types.CLOB;
         }
         else if (sqlLowerType.equals("image"))
         {   // this is really a JDBC BLOB
            jdbcType = Types.BLOB;
         }
         else if (sqlLowerType.equals("sysname"))
         {   // this is really a JDBC varchar
            jdbcType = Types.VARCHAR;
         }
      }
      /* Apparently, this is a trend in MS products. MS Access
       * also reports its datetime types using its native type
       * rather than the JDBC types. It also has this problem
       * with its GUID.
       * This fixes the MS Access part of bug id Rx-99-11-0030
       */
      else if (dbmsName.equals("ACCESS"))
      {
         if (sqlLowerType.equals("datetime"))
         {
            /* these are really JDBC timestamp types as they
             * contain date and time values
             */
            jdbcType = Types.TIMESTAMP;
         }
         else if (sqlLowerType.equals("guid"))
         {
            /* these most closely map to JDBC CHAR strings. They
             * actually need a special escape sequence:
             * {guid 'nnnnnnnn-nnnn-nnnn-nnnn-nnnnnnnnnnnn'}
             * which means we may need to special case these in
             * the update engine
             */
            jdbcType = Types.CHAR;
         }
      }
      /*
       * Oracle misreports blob and clobs as {@link Types#OTHER}.
       */
      else if (dbmsName.equals("Oracle"))
      {
         if ( sqlLowerType.equals("blob"))
            jdbcType = Types.BLOB;
         else if ( sqlLowerType.equals("clob"))
            jdbcType = Types.CLOB;
         else if ( sqlLowerType.equals("float"))
            jdbcType = Types.FLOAT;
      }

      return jdbcType;
   }

   /**
    * Some jdbc datatypes returned by drivers are not valid types as enumerated
    * in {@link java.sql.Types}. This method will convert those types to
    * valid jdbc data types.
    *
    * @param jdbcType The numeric jdbc data type (DATA_TYPE) reported by the
    * driver along with TYPE_NAME.
    *
    * @param nativeType The database native data type (TYPE_NAME) reported
    * by the driver from {@link java.sql.DatabaseMetaData#getTypeInfo()} or
    * {@link java.sql.DatabaseMetaData#getColumns(String, String, String,
    * String) DatabaseMetaData.getColumns()}.  May not be <code>null</code> or
    * empty.
    *
    * @param driver The jdbc driver such as <code>SPRINTA</code> or
    * <code>JTDS_DRIVER</code> or <code>MICROSOFT_DRIVER</code> or
    * <code>ORACLE</code>. May not be <code> null</code> or empty.
    *
    * @return The corrected jdbc type for the supplied native data type
    *
    * @throws IllegalArgumentException if <code>driver</code> or
    * <code>nativeType</code> <code>null</code> or emtpy.
    */
   public static short convertNativeDataType(
      short jdbcType, String nativeType, String driver)
   {
      if ((driver == null) || (driver.trim().length() == 0))
         throw new IllegalArgumentException(
            "DBMS name and JDBC Driver both may not be null or empty");

      if ((nativeType == null) || (nativeType.trim().length() == 0))
         throw new IllegalArgumentException(
            "native data type may not be null or empty");

      boolean isSprinta = false;
      boolean isMSDriver = false;
      boolean isJTdsDriver = false;
      boolean isOracle = false;

      if (driver.equals(PSJdbcUtils.SPRINTA))
         isSprinta = true;

      if (driver.equals(PSJdbcUtils.JTDS_DRIVER))
         isJTdsDriver = true;

       if (driver.equals(PSJdbcUtils.MICROSOFT_DRIVER))
         isMSDriver = true;

      if (driver.equals(PSJdbcUtils.ORACLE))
         isOracle = true;

      String sqlLowerType = nativeType.toLowerCase();

      if (isSprinta || isMSDriver || isJTdsDriver)
      {
         if (sqlLowerType.equals("nvarchar"))
            jdbcType = Types.VARCHAR;
         else if (sqlLowerType.equals("nchar"))
            jdbcType = Types.CHAR;
         else if (sqlLowerType.equals("ntext"))
            jdbcType = Types.CLOB;
         else if (sqlLowerType.equals("image"))
            jdbcType = Types.BLOB;
         else if (sqlLowerType.equals("sysname"))
            jdbcType = Types.VARCHAR;
      }
      else if (isOracle)
      {
         if ( sqlLowerType.equals("blob"))
            jdbcType = Types.BLOB;
         else if ( sqlLowerType.equals("clob"))
            jdbcType = Types.CLOB;
         else if ( sqlLowerType.equals("float"))
            jdbcType = Types.FLOAT;
         // Correcting a bug in Oracle 10g driver for the 'DATE' column
         // The 'DATE' column in Oracle contains both date & time, 
         // so the SQL Type should be TIMESTAMP, but not DATE
         else if ( sqlLowerType.equals("date") && jdbcType == Types.DATE)
            jdbcType = Types.TIMESTAMP;
      }

      return jdbcType;
   }

   /**
    * Returns the value to use to specify that a column may be <code>NULL</code>
    * in a <code>CREATE TABLE</code> or <code>ALTER TABLE</code> SQL statement.
    * Databases vary in their requirements for this syntax.
    *
    * @param driver The name of the sub-protocol for the JDBC driver being used.
    * May not be <code>null</code> or empty.
    *
    * @return The text to include in the column defintion to allow <code>NULL
    * </code> values.  May be empty, never <code>null</code>.
    *
    * @throws IllegalArgumentException if driver is <code>null</code> or empty.
    */
   public static String getNullColumnSpecifier(String driver)
   {
      if (driver == null || driver.trim().length() == 0)
         throw new IllegalArgumentException("driver may not be null or empty");

      String nullClause;

      if (driver.equals(PSJdbcUtils.DB2) || 
            driver.equals(PSJdbcUtils.DERBY_DRIVER))
      {
         nullClause = "";
      }
      else
      {
         nullClause = " NULL";
      }

      return nullClause;
   }


   /**
    * Gets the maximum number of bytes permitted for a constraint name using
    * the specified driver.  While most databases use the same length for
    * all database objects (in which case this number could be determined
    * through the <code>DatabaseMetaData</code> interface), at least one (DB2)
    * requires constraint names to be shorter than other database objects.
    *
    * @param driver The name of the sub-protocol for the JDBC driver being used.
    * Will be matched case-sensitively.  May not be <code>null</code> or empty.
    *
    * @return the maximum number of bytes permitted for a constraint name using
    * the specified driver, or the least common denominator if no match is
    * found for the specified driver.
    *
    * @throws IllegalArgumentException if driver is <code>null</code> or empty.
    */
   public static int getMaxConstraintNameLength(String driver)
   {
      if (driver == null || driver.trim().length() == 0)
         throw new IllegalArgumentException("driver may not be null or empty");

      // default to the least common denominator
      int maxLength = 18;

      if (driver.equals(PSJdbcUtils.DB2))
         maxLength = 18;
      else if (driver.startsWith(PSJdbcUtils.ORACLE_PRIMARY))
         maxLength = 30;
      else if (driver.equals(PSJdbcUtils.SPRINTA))
         maxLength = 128;
      else if (driver.equals(PSJdbcUtils.DERBY))
         maxLength = 128;

      return maxLength;
   }

   /**
    * Used to determine if the specified DBMS supports auto-increment or
    * identity columns.  Not every DBMS supports auto-increment columns, and the
    * call to <code>DatabaseMetaData.getVersionColumns()</code> may be very
    * expensive (using Oracle, for example).
    *
    * @param driver The name of the sub-protocol for the JDBC driver being used.
    * Will be matched case-sensitively.  May not be <code>null</code> or empty.
    *
    * @return <code>true</code> if the specified driver supports auto-increment
    * columns, <code>false</code> if not.
    *
    * @throws IllegalArgumentException if driver is <code>null</code> or empty.
    */
   public static boolean supportsIdentityColumns(String driver)
   {
      if (driver == null || driver.trim().length() == 0)
         throw new IllegalArgumentException("driver may not be null or empty");

      boolean supported = true;

      if (driver.startsWith(PSJdbcUtils.ORACLE_PRIMARY) ||
          driver.contains(PSJdbcUtils.MYSQL_DRIVER))
         supported = false;

      return supported;
   }

   /**
    * Returns a query which can be executed to obtain metadata information
    * about the specified database object <code>objectName</code>. 
    * <p>
    * Currently this only returns a valid query for "db2" driver, and returns
    * <code>null</code> for other drivers. <br>
    * (<code>DatabaseMetadata.getTables()</code> and
    * <code>DatabaseMetaData.getColumns()</code> (only for Views) does not work
    * well with "db2" driver and so specific queries have to be executed to
    * check the specified table or view existence or to obtain the metadata for
    * views.)
    * <p>
    * If <code>isMetaQuery</code> is <code>true</code> and the specified
    * database object is a view then the query on execution returns the
    * metadeta information about the view, otherwise it returns whether or not
    * the table or view exists or not.
    * The query executed in this case is similar to:
    * SELECT * FROM RXRELATEDCONTENT WHERE 1 = 0
    * No rows is returned by execution of this query but the column information
    * for the specified view can be obtained from the
    * <code>ResultSetMetaData</code> object.
    *
    * If the object is a table (<code>isView</code> is <code>false</code>)
    * and <code>isMetaQuery</code> is <code>false</code> then the query
    * executed is similar to:
    *
    * SELECT 1 FROM SYSCAT.TABLES WHERE TABNAME = 'AGING' AND TABSCHEMA = 'ANIMESH'
    *
    * If the object is a view (<code>isView</code> is <code>true</code>) and
    * <code>isMetaQuery</code> is <code>false</code> then the query
    * executed is similar to:
    * SELECT 1 FROM SYSCAT.VIEWS WHERE VIEWNAME = 'RXRELATEDCONTENT' AND VIEWSCHEMA = 'ANIMESH'
    *
    * @param driver The name of the sub-protocol for the JDBC driver being used.
    * Will be matched case-insensitively. May not be <code>null</code> or empty.
    *
    * @param db name of the database, may be <code>null</code> or empty
    *
    * @param schema name of the schema which contains the specified database
    * object <code>objectName</code>, may be <code>null</code> or empty
    *
    * @param objectName the name of the database object whose metadata
    * information is required or whose existence is to be verified,
    * may not be <code>null</code> or empty
    *
    * @param isView <code>true</code> if the database object specified by
    * <code>objectName</code> is a VIEW, false otherwise.
    *
    * @param isMetaQuery <code>true</code> if the returned query on execution
    * should return metadata information about the specified database object,
    * <code>false</code> if the query should return whether the specified
    * database object exists or not.
    *
    * @return a query string which can be executed using a valid database
    * connection to obtain the metadata information about the specified database
    * object <code>objectName</code>. Currently this returns a valid query only
    * if <code>driver</code> equals "db2", returns <code>null</code> for all
    * other drivers, never empty if not <code>null</code>
    */
   public static String getMetaDataQuery(String driver, String db, String schema,
      String objectName, boolean isView, boolean isMetaQuery)
   {
      if ((driver == null) || (driver.trim().length() == 0))
         throw new IllegalArgumentException("driver may not be null or empty");

      if ((objectName == null) || (objectName.trim().length() == 0))
         throw new IllegalArgumentException(
            "objectName may not be null or empty");
            
      String query = null;
      if (driver.equalsIgnoreCase(PSJdbcUtils.DB2))
      {
         if (isMetaQuery && isView)
         {
            query = "SELECT * FROM ";
            query += qualifyViewName(objectName, db, schema, driver);
            query += " WHERE 1 = 0";
         }
         else if (!isMetaQuery)
         {
            if (isView)
               query = "SELECT 1 FROM SYSCAT.VIEWS WHERE VIEWNAME = '";
            else
               query = "SELECT 1 FROM SYSCAT.TABLES WHERE TABNAME = '";

            query += objectName;
            query += "'";

            if (!((schema == null) || (schema.trim().length() == 0)))
            {
               if (isView & !isMetaQuery)
                  query += " AND VIEWSCHEMA = '";
               else
                  query += " AND TABSCHEMA = '";
               query += schema.trim();
               query += "'";
            }
         }
      }

      return query;
   }

   /**
    * Sets the designated parameter in the specified prepared statement
    * <code>stmt</code> to SQL NULL.
    * <p>
    * Microsoft SQL Server and JTDS jdbc driver do not support
    * <code>java.sql.Types#BLOB</code>
    * or <code>java.sql.Types#CLOB</code> in the
    * {@link java.sql.PreparedStatement#setNull(int, int) setNull()} method,
    * this helper method calls the
    * <code>java.sql.PreparedStatement#setNull()</code> method with jdbc type
    * <code>java.sql.Types.LONGVARCHAR</code> if <code>jdbcDataType</code>
    * equals <code>java.sql.Types.CLOB</code> and with jdbc type
    * <code>java.sql.Types.LONGVARBINARY</code> if <code>jdbcDataType</code>
    * equals <code>java.sql.Types.BLOB</code> and the driver is
    * Microsoft SQL Server or JTDS jdbc driver
    * <p>
    * @param stmt the prepared statement whose parameter is to be set to
    * SQL NULL, may not be <code>null</code>
    * @param index the index of the parameter in the prepared statement, the
    * first parameter is 1, the second is 2 and so on.
    * @param jdbcDataType the SQL type code defined in
    * <code>java.sql.Types</code>
    * <p>
    * @throws SQLException if a database access error occurs
    * @throws IllegalArgumentException if <code>stmt</code> is <code>null</code>
    * <p>
    * @see java.sql.PreparedStatement#setNull(int, int)
    */
   public static void setNullParameter(PreparedStatement stmt, int index,
      int jdbcDataType) throws SQLException
   {
      if (stmt == null)
         throw new IllegalArgumentException("stmt may not be null");

      String url = stmt.getConnection().getMetaData().getURL();
      String driver = PSJdbcUtils.getDriverFromUrl(url);
      String driverName = stmt.getConnection().getMetaData()
            .getDatabaseProductName();
      boolean isASA = driver.equalsIgnoreCase(PSJdbcUtils.ODBC)
            && driverName.equals("Adaptive Server Anywhere");
      boolean isSQLServer = driver.equalsIgnoreCase(PSJdbcUtils.MSSQL)
            || driver.equalsIgnoreCase(PSJdbcUtils.MICROSOFT_DRIVER);
      boolean isJTDS = driver.equalsIgnoreCase(PSJdbcUtils.JTDS_DRIVER);
      boolean isMySQL = driver.equalsIgnoreCase(PSJdbcUtils.MYSQL_DRIVER);
     
      // Note: Currently (7/22/2005) jTDS does not appear to support null
      // values for BINARY fields.

      if (isASA || isSQLServer || isJTDS)
      {
         if (jdbcDataType == Types.CLOB)
            jdbcDataType = Types.LONGVARCHAR;
         else if (jdbcDataType == Types.BLOB)
            jdbcDataType = Types.LONGVARBINARY;
      }
      
      if(isMySQL){
         jdbcDataType = java.sql.Types.BLOB;
      }

      stmt.setNull(index, jdbcDataType);
   }

   /**
    * Returns the string that can be used to represent parameters for prepared
    * statements. 
    * <p>
    * This method should be called to obtain the substitution string
    * when using 
    * INSERT INTO TABLE1 SELECT A, B, ..., ?, C, ?, D ..., E FROM TABLE2 
    * type of prepared statement.
    * Such statements on DB2 require a
    * special syntax (CAST ? AS DATATYPE) instead of just "?". If the specified
    * driver is "db2", then it returns a string of the form
    * <code>CAST(? AS nativeType)</code> if <code>size</code> is
    * <code>null</code> or empty otherwise returns
    * <code>CAST(? AS nativeType(size))</code>. For non-db2 drivers it simple
    * returnes "?".
    * 
    * @param driver the database driver, may not be <code>null</code> or empty
    * @param nativeType the native data type, may not be <code>null</code> or
    *           empty if <code>driver</code> is "db2"
    * @param size the size of the column, may be <code>null</code> or empty
    * 
    * @return the parameter substitution string, never <code>null</code> or
    *         empty
    * 
    * @throws IllegalArgumentException if any param is invalid
    */
   public static String getParameterMarker(String driver, String nativeType,
      String size)
   {
      if ((driver == null) || (driver.trim().length() < 1))
         throw new IllegalArgumentException("driver may not be null or empty");

      String marker = "?";

      if (driver.equalsIgnoreCase(PSJdbcUtils.DB2))
      {
         if ((nativeType == null) || (nativeType.trim().length() < 1))
            throw new IllegalArgumentException(
               "nativeType may not be null or empty");

         marker = "CAST(? AS " + nativeType;
         if ((size != null) && (size.trim().length() > 0))
            marker += "(" + size.trim() + ")";
         marker += ")";
      }
      return marker;
   }

   /**
    * This will create SQL string for dropping the specified index.
    *
    * @param driver the database driver, may not be <code>null</code> or empty
    * @param table the table containing the index, may not be <code>null</code>
    * or empty
    * @param db database name, may be <code>null</code> or empty
    * @param owner the table owner, may be <code>null</code> or empty
    * @param indexName the name of the index to be dropped, may not be
    * <code>null</code> or empty
    *
    * @return the sql string which can be used to drop the specified index,
    * never <code>null</code> or empty
    *
    * @throws IllegalArgumentException if any argument is invalid
    */
   public static String getDropIndexStatement(String driver, String table,
      @SuppressWarnings("unused") String db, String owner, String indexName)
   {
      if ((driver == null) || (driver.length() == 0))
         throw new IllegalArgumentException("driver may not be null or empty");

      if ((table == null) || (table.length() == 0))
         throw new IllegalArgumentException("table may not be null or empty");

      if ((indexName == null) || (indexName.length() == 0))
         throw new IllegalArgumentException(
            "indexName may not be null or empty");
            
      String sql = "DROP INDEX ";

      if ((owner != null) && (owner.trim().length() > 0))
         sql += owner + ".";
      if (driver.equals(PSJdbcUtils.SPRINTA) || 
          driver.equals(PSJdbcUtils.JTDS_DRIVER))
         sql += table + ".";
      
      sql += indexName.trim();
      
      if (driver.equals(PSJdbcUtils.MYSQL_DRIVER))
      {
         if (StringUtils.startsWithIgnoreCase(indexName, "FK_"))
         {
            sql = "ALTER TABLE " + table + " DROP FOREIGN KEY " + indexName;
         }
         else
         {
            sql = "ALTER TABLE " + table + " DROP INDEX " + indexName;
         }
      }
      
      return sql;
   }


   /**
    * Creates sql statement for creating or replacing Oracle specific
    * data types. 
    * <p>
    * This returns sql statements such as :
    * CREATE OR REPLACE TYPE RXMASTER.RX_NUMBER_TABLE AS TABLE OF NUMBER
    *
    * @param typeName the name of the data type to be created, (such as
    * "RX_NUMBER_TABLE" in this example), may not be <code>null</code> or empty
    *
    * @param dataType the data type to be created, (such as "TABLE OF NUMBER"
    * in this example), may not be <code>null</code> or empty
    *
    * @param schema the name of the schema in which this data type is to be
    * created, may be <code>null</code> or empty. If <code>null</code> or empty,
    * then schema name is not prepended to the data type.
    *
    * @return the sql string which can be used to create the specified data
    * type, never <code>null</code> or empty
    */
   public static String getCreateOracleTypeSql(String typeName,
      String dataType, String schema)
   {
      if ((typeName == null) || (typeName.trim().length() < 1))
         throw new IllegalArgumentException(
            "typeName may not be null or empty");

      if ((dataType == null) || (dataType.trim().length() < 1))
         throw new IllegalArgumentException(
            "dataType may not be null or empty");

      String sql = "CREATE OR REPLACE TYPE ";

      if ((schema != null) && (schema.trim().length() > 0))
         sql = sql + schema.trim() + ".";

      sql = sql +  typeName + " AS " + dataType;

      return sql;
   }

   /**
    * Returns the value of a clob column contained in the specified reader
    * object, which is obtained using
    * <code>java.sql.ResultSet#getCharacterStream()</code> or
    * <code>java.sql.Clob#getCharacterStream()</code> method.
    * This method closes the specified reader <code>clobReader</code>.
    *
    * @param clobReader reader object representing the value of the CLOB column,
    * may be <code>null</code> in which case <code>null</code> is returned.
    *
    * @return the value of CLOB column obtained from the specified reader,
    * may be <code>null</code> or empty
    *
    * @throws IOException if any error occurs while converting the CLOB column
    * value to string
    */
   public static String getClobColumnData(Reader clobReader)
      throws IOException
   {
      if (clobReader == null)
         return null;

      String columnValue = null;
      StringWriter sw = null;
      try
      {
         sw = new StringWriter();
         IOTools.writeStream(clobReader, sw);
         columnValue = sw.toString();
      }
      finally
      {
         if (clobReader != null)
         {
            try
            {
               clobReader.close();
            }
            catch (Exception e)
            {
            }
         }
         if (sw != null)
         {
            try
            {
               clobReader.close();
            }
            catch (Exception e)
            {
            }
         }
      }
      return columnValue;
   }

   /**
    * Construct a jdbc url string from a given driver name and server name.
    * Correctly deals with cases where the name of the driver is not the
    * name of the second component in the jdbc url string.
    *
    * @param driverName Name of the driver, must never be <code>null</code>
    * @param serverName Name of the server or connection string,
    * must never be <code>null</code>
    * @return a string, never <code>null</code> or empty.
    */
   public static String getJdbcUrl(String driverName, String serverName)
   {
      return PSJdbcUtils.getJdbcUrl(driverName, serverName);
   }

   /**
    * Creates properties used for making JDBC connections.
    *
    * @param url  The URL used to for making JDBC connections,
    *    never <code>null</code>.
    * @param database The name of the database, it may be <code>null</code> or
    *    empty.
    * @param userid The user name of the database, never <code>null</code>.
    * @param password The password of the database user,
    *    never <code>null</code>.
    *
    * @return The properties of the JDBC connection, never <code>null</code>.
    */
   public static Properties makeConnectProperties(
      String url,
      String database,
      String userid,
      String password)
   {
      Properties props = new Properties();
      props.put("user", userid);

      // get the plaintext version of the password
      props.put("password", password);

      if (database != null)
      {
         props.put("catalog", database);
         props.put("db", database); // this was the ODBC name for it
      }

      /* drivers like IBM's require this to avoid popping up a dialog box */
      props.put("prompt", "false");

      return props;
   }

   /**
    * Determines if the supplied driver is for Oracle database.
    * 
    * @return <code>true</code> if the driver is for Oracle database; otherwise
    *    return <code>false</code>.
    */
   public static boolean isOracle(String driverName)
   {
      return (driverName.toUpperCase().indexOf("ORACLE") > -1);
   }
   
   /**
    * Determines if the supplied driver is for MySql database.
    * 
    * @return <code>true</code> if the driver is for MySql database; otherwise
    *    return <code>false</code>.
    */
   public static boolean isMysql(String driverName)
   {
      return (driverName.toUpperCase().indexOf("MYSQL") > -1);
   }

   /**
    * Determines if the supplied index name is a "backing index".
    * <p>
    * Certain RDBMS systems, such as Apache Derby have the concept of a 
    * "backing index". This is an index which backs up a constraint such as
    * a Primary key, Foreign key or Unique index. 
    * <p>
    * This causes a problem for the Table Factory: <br>
    * -Although they are not part of the original data model for the schema 
    * object (PSJdbcTableSchema), the JDBC driver's meta data facility 
    * (DatabaseMetaData) will find and return them as indexes. They are 
    * then included in the schema object's index list. <br>
    * -When the table factory goes to load data, it compares the table's schema
    * as it is stored in the RDBMS table "catalog" to the one in the table 
    * definition file (i.e. sys_cmstableData.xml). It finds these new indexes, 
    * and tries to remove them, thinking they were removed. The resulting 
    * DROP INDEX statements violate the constraints set up by these indexes, 
    * and the RDBMS (read: JDBC driver) throws exceptions.
    * <p>
    * Solution:<br>
    * Given the name of a index, the RDBMS is queried to see if the index
    * is a "backing index". If it is, it can be ignored (and this method
    * will return true). <br>
    * -Unique constraint keys are treated specially (for Derby, at least).
    * These actually should be included in the schema's index list and this 
    * method should return false. However, the index must be renamed from 
    * the system generated value, to the name given in the SQL statement 
    * for creating the index. The system generated value (for Derby) is of the 
    * form "SQLxxxxxxxxxxxxxxx" where "x" is a digit.
    * <p>
    *  For more information, check the reference manuals for the RDBMS. 
    *  For Derby, see the Derby Reference guide under:
    *   " SQL Clauses->CONSTRAINT clause->Backing Indexes"
    *  
    *
    * @param indexNameBuf Name of the index, not <code>null</code>, blank, or empty.
    *  If the return value is <code>false</code>, the name may have been changed
    *  by this method: if the index was a unique index, its name was modified to 
    *  be the name given to the index in the SQL statement.
    * @param md JDBC database Meta-data object for the DB. Not <code>null</code>.
    * 
    * @return <code>true</code> if the index is a backing index and may be
    *  ignored in the table schema; otherwise <code>false</code>. Note the 
    *  special case listed above for parameter <code>indexNameBuf</code> where 
    *  its value may change if method returns false.
    */
   public static boolean handleBackingIndex(StringBuilder indexNameBuf,
         DatabaseMetaData md) throws SQLException
   {
      String indexName = indexNameBuf.toString();
      if (StringUtils.isBlank(indexName))
      {
         throw new IllegalArgumentException(
               "indexName may not be empty, blanks, or null");
      }      
      if (md == null)
      {
         throw new IllegalArgumentException("md may not be null");
      }

      boolean ignoreBackingIndex = false;
      String driverName = PSJdbcUtils.getDriverFromUrl(md.getURL());

      // Currently the only RDBMS using backing indexes is Apache Derby
      if (driverName.equals(PSJdbcUtils.DERBY))
      {
         final String queryForPkAndUk = 
            "select constraintname,type from sys.sysconstraints " + 
            "  where constraintid =  " + 
                   "(select constraintid from sys.syskeys " + 
                   "  where conglomerateid = " +
                      "(select conglomerateid from sys.sysconglomerates " +
                      "   where isconstraint='1' and conglomeratename = ? ))";
         
         final String queryForFk = 
            "select constraintname,type from sys.sysconstraints " + 
            "  where constraintid =  " + 
                   "(select constraintid from sys.sysforeignkeys " + 
                   "  where conglomerateid = " +
                      "(select conglomerateid from sys.sysconglomerates " +
                      "   where isconstraint='1' and conglomeratename = ? ))";
         
         String renameIdx = 
               "rename index ";
            
            ResultSet rs = null;
            PreparedStatement st = null;
            Connection conn = md.getConnection();
           
            try
            {
               // First, look for constraint name in table holding
               // the Primary and Unique index info (sys.syskeys)
               st = conn.prepareStatement(queryForPkAndUk);
               st.setString(1, indexName);
               rs = st.executeQuery();
               ignoreBackingIndex = rs.next();

               // If they key is found, get name and the type of constraint.
               // -If the type is a unique index, rename the index from the
               // funky Derby convention of "SQLxxxxxxxxxxxxxxx" to the name
               // given in the CREATE INDEX or CREATE TABLE statement.
               // -Then, return a "false" value, so that the unique index 
               // can be treated as an index, (and included in the table 
               // schema object as a index).
               if (ignoreBackingIndex)
               {
                  String tName = rs.getString(1);
                  String tType = rs.getString(2);

                  if (tType.equals("U")) 
                  {
                     if (!tName.equals(indexName))
                     {
                        renameIdx = renameIdx + indexName + " to " + tName;
                        st = conn.prepareStatement(renameIdx);
                        int rc = st.executeUpdate();
                        if (rc==1)
                        {
                           indexNameBuf.replace(0,indexNameBuf.length()-1,tName);
                        }
                     }
                     ignoreBackingIndex = false; // pretend we didn;t find it ;-)
                  }
               }
               // If the constraint name does not represent a Primary or Unique 
               // key constraint, check to see if represents a Foreign key 
               // constraint
               else
               {
                  st.close();
                  st = conn.prepareStatement(queryForFk);
                  st.setString(1, indexName);
                  rs = st.executeQuery();
                  ignoreBackingIndex = rs.next();
               }
            }
            catch (SQLException e)
            {
               if (rs != null)
                  rs.close();
               if (st != null)
                  st.close();
               if (conn != null)
                  conn.close();
               throw e;
            }
            finally
            {
               if (rs != null)
               {
                  try { rs.close(); } catch (SQLException e) { /* ignore */ }
               }
            }
      }
         
       return ignoreBackingIndex;
   }

   /**
    * Check if the database that is connected to supports Unicode.
    * <p>
    * Calls {@link #supportsUnicode(Connection, String, String)} on fully
    * qualified RXDUAL table.
    * 
    * @param conn a sql connection to the database, never <code>null</code>
    * @param connDetail the details for this connection, never <code>null</code>
    * @return <code>true</code> if the database supports unicode,
    *         <code>false</code> otherwise
    * @throws SQLException
    */
   public static boolean supportsUnicode(Connection conn,
         PSConnectionDetail connDetail) throws SQLException
   {
      if (conn == null)
      {
         throw new IllegalArgumentException("conn may not be null");
      }
      
      if (connDetail == null)
      {
         throw new IllegalArgumentException("connDetail may not be null");
      }
      
      String qualifiedTableName = qualifyTableName("RXDUAL",
            connDetail.getDatabase(), connDetail.getOrigin(), 
            connDetail.getDriver());
      
      return supportsUnicode(conn, qualifiedTableName, "GB");
   }
   
   /**
    * Check if the database that is connected to supports Unicode. This method
    * is called by the server on startup to allow a warning since a non-unicode
    * compatible database causes problems. It can also be called by the
    * installer.
    * <p>
    * This works by saving and loading characters to the database and checking
    * for differences. The code here presumes that the differences will occur in
    * the given character range. This appears to be true for Oracle.
    * <p>
    * Note that the code here is careful to leave the table with no content. If
    * this state is not maintained, rhythmyx will probably fail in various
    * mysterious ways.
    * 
    * @param conn a sql connection to the database, never <code>null</code>
    * @param table a table to use for this test, never <code>null</code>
    * @param column a column in the table to use for this test, never
    * <code>null</code>, must exist in the table
    * @return <code>true</code> if the database supports unicode,
    *         <code>false</code> otherwise
    * @throws SQLException
    */
   public static boolean supportsUnicode(Connection conn, String table,
         String column)
    throws SQLException
   {
      if (conn == null)
      {
         throw new IllegalArgumentException("conn may not be null");
      }
      
      if (table == null)
      {
         throw new IllegalArgumentException("table may not be null");
      }
      
      if (column == null)
      {
         throw new IllegalArgumentException("column may not be null");
      }
      String insert = "insert into " + table + " (" + column + ") values(?)";
      String query = "select * from " + table;
      String delete = "delete from " + table;
      ResultSet rs = null;
      PreparedStatement st = null;
      try
      {
         StringBuilder buf = new StringBuilder();
         for (char ch = 0x21; ch < 0x017F; ch += 15)
         {
            st = PSPreparedStatement.getPreparedStatement(conn, delete);
            st.execute();
            st.close();
            for (char c1 = ch; c1 < (ch + 15); c1++)
            {
               buf.append(c1);
            }
            String input = buf.toString();
            buf.setLength(0);
            st = PSPreparedStatement.getPreparedStatement(conn, insert);
            st.setString(1, input);
            st.executeUpdate();
            st.close();
            st = PSPreparedStatement.getPreparedStatement(conn, query);
            rs = st.executeQuery();
            rs.next();
            String db = rs.getString(1);
            rs.close();
            rs = null;
            st.close();
            if (db == null || !db.equals(input))
               return false;
         }
         return true;
      }
      catch (SQLException e)
      {
         if (rs != null)
            rs.close();
         throw e;
      }
      finally
      {
         try
         {
            if (st != null)
               st.close();
         }
         catch (Exception e2)
         {
            // was worth trying
         }
         
         st = PSPreparedStatement.getPreparedStatement(conn, delete);
         st.execute();
         st.close();
      }
   }
   
   /**
    * Compares a given database version with the version of a database from a
    * specified connection.
    *
    * @param conn the database connection, never <code>null</code>
    * @param version the database version, i.e., 9.2.0.0.0, never <code>null</code>
    * @return 1 if conn > version, -1 if conn < version, and 0 if conn == v2
    * 
    * @throws SQLException, NumberFormatException
    */
   public static int compareVersions(Connection conn, String version) 
   throws SQLException
   {
      if (conn == null)
      {
         throw new IllegalArgumentException("conn may not be null");
      }
      
      if (version == null)
      {
         throw new IllegalArgumentException("version may not be null");
      }
                         
      String connVersion = parseDBVersion(conn.getMetaData().getDatabaseProductVersion());
      String tempVersion = "";
      
      for (int i = 0; i < connVersion.length(); i++)
      {
         char c = connVersion.charAt(i);
         if (c != '.')
            tempVersion += c;
      }
      
      connVersion = tempVersion;
      
      String minVersion = version;
      tempVersion = "";
      
      for (int i = 0; i < minVersion.length(); i++)
      {
         char c = minVersion.charAt(i);
         if (c != '.')
            tempVersion += c;
      }
      
      minVersion = tempVersion;
      
      int connVersionInt = Integer.parseInt(connVersion);
      int minVersionInt = Integer.parseInt(minVersion);
      
      int result = 0;
      if (connVersionInt > minVersionInt)
         result = 1;
      if (connVersionInt < minVersionInt)
         result = -1;
                   
      return result;
    } 
   
   /**
    * Parses the string returned by
    * <code>DatabaseMetaData.getDatabaseProductVersion()</code> to obtain
    * the database version string.
    *
    * @param metaVer the database version string returned by a call to
    * <code>DatabaseMetaData.getDatabaseProductVersion()</code>, never
    * <code>null</code> or empty
    *
    * @return the database version obtained by parsing the specified string,
    *  <code>0</code> if the parse does not succeed.
    */
   public static String parseDBVersion(String metaVer)
   {
      if (metaVer == null || metaVer.trim().length() == 0)
      {
         throw new IllegalArgumentException("metaVer may not be null or empty");
      }
      
      String dbVer = "0";
      char[] cVer = metaVer.toCharArray();
      for (int i = 0; i < (cVer.length-1); i++)
      {
         if (((Character.isSpaceChar(cVer[i])) ||
            (Character.isWhitespace(cVer[i]))) &&
            (Character.isDigit(cVer[i+1])))
         {
            String tempVer = String.valueOf(cVer[i+1]);
            int count = 0;
            for (int j = (i+2); j < cVer.length; j++)
            {
               char c = cVer[j];
               if (Character.isDigit(c))
               {
                  tempVer += c;
               }
               else if (c == '.')
               {
                  tempVer += ".";
                  count++;
               }
               else
               {
                  break;
               }
            }
            // This makes an assumption that database version will have atleast
            // 5 characters and atleast two "." characters.
            if ((count >= 2) && (tempVer.length() >= 5))
            {
               dbVer = tempVer;
               break;
            }
         }
      }
      return dbVer;
   }
   
   /**
    * Gets the default database connection info.
    * @return the connection info, never <code>null</code>.
    * @throws SQLException if failed to get the connection info.
    */
   private static PSConnectionDetail getDefaultConnectDetail() throws SQLException
   {
      if (ms_defaultConnInfo == null)
      {
         try
         {
            ms_defaultConnInfo = PSConnectionHelper.getConnectionDetail();
         }
         catch (NamingException e)
         {
            throw new SQLException(e.getLocalizedMessage());
         }
      }
      
      return ms_defaultConnInfo;
   }

   /**
    * This is used to cache the default connection info, see {@link #getDefaultConnectDetail()}
    */
   private static PSConnectionDetail ms_defaultConnInfo = null;
   
   /**
    * Commit a SQL connection, taking into account the state of auto commit
    * @param conn the connection, never <code>null</code>
    * @throws SQLException @see Connection#commit()
    */
   public static void commit(Connection conn) throws SQLException 
   {
      if (! conn.getAutoCommit())
      {
         conn.commit();
      }
   }
   
   /**
    * Rolls back a SQL connection, taking into account the state of auto commit
    * @param conn the connection, never <code>null</code>
    * @throws SQLException @see Connection#commit()
    */
   public static void rollback(Connection conn) throws SQLException 
   {
      if (! conn.getAutoCommit())
      {
         conn.rollback();
      }
   }

   /**
    * Determines if the current repository is an Oracle database.
    *
    * @return <code>true</code> if it is a Oracle database; otherwise return
    * <code>false</code>. 
    * 
    * @throws RuntimeException if 
    * {@link #createInstance(IPSDatasourceManager)} has not been called.
    * {@link #createInstance(IPSDatasourceManager)} method is called by 
    * spring wiring process.
    */
   public static boolean isOracle()
   {
      if (ms_isOracle != null)
         return ms_isOracle;
      
      try
      {
         PSConnectionDetail connDetail = PSConnectionHelper.getConnectionDetail();
         ms_isOracle = PSSqlHelper.isOracle(connDetail.getDriver());
         
         if (ms_isOracle)
            ms_log.debug("The repository is an Oracle database, driver is " + connDetail.getDriver());
         else
            ms_log.debug("The repository is not an Oracle database, driver is " + connDetail.getDriver());
         
         return ms_isOracle;
      }
      catch (Exception e)
      {
         ms_log.error("Failed to determine database type", e);
         throw new RuntimeException("Failed to determine database type", e);
      }
   }

   private static ArrayList<String> existingCMStables = new ArrayList<String>();

   /**
    * Utility method that can be used go validate if a table name exists on the database.  Intended
    * to be used when using dynamic table names to prevent against SQL Injection attacks.
    *
    * Table names are cached on first call, to refresh the cache pass true for the refresh parameter.
    *
    * @param t A candidate table name.
    * @param refresh When true, the list of valid tables is refreshed from the database.
    * @return true if the table is valid and exists, false if not.
    */
   public static boolean isExistingCMSTableName(Connection conn, String t, boolean refresh) {
      boolean ret = false;

      if (existingCMStables.size() == 0 || refresh) {
         try{
            existingCMStables.clear();
            String types[] = {"TABLE", "VIEW"};
            try(ResultSet rs = conn.getMetaData().getTables(conn.getCatalog(), conn.getSchema(), "%", types)) {
               while (rs.next()) {
                  existingCMStables.add(rs.getString("TABLE_NAME").toLowerCase());
               }
            }
         } catch (SQLException e) {
            ms_log.warn("Error listing database tables: " + e.getMessage());
         }
      }

      if(existingCMStables.contains(t.toLowerCase())){
         return true;
      }else{
         return false;
      }

   }


   /**
    * Determines if the current repository is an Oracle database. 
    * @see #isOracle() for detail.
    */
   private static Boolean ms_isOracle = null;
   

   /**
    * Logger for PSConnectionHelper.
    */
   private static final Logger ms_log = LogManager.getLogger("PSSqlHelper");
   
   /**
    * SQL State for sql exceptions which violate integrity constraints. A SQL
    * exception with this SQL State if thrown when an attempt to insert
    * duplicate row is made.
    */
   public static final String [] SQLSTATE_INTEGRITY_CONSTRAINT_VIOLATIONS =
      {
         "23000",    // ANSI/ISO SQL Standard SQL State Integrity
                     // constraint violation
         "23505",    // SQL State for DB2 driver
         "S1000"     // SQL State for JTDS driver
      };

   /**
    * SQL State for sql exceptions thrown when executing update statements that
    * specify selection criteria that return no rows to update.
    */
   public static final String [] SQLSTATE_NO_UPDATE_ROWS =
      {
         "02000"    // SQL State for DB2 driver
      };

   /**
    * Name of the descriptor of the table for storing numbers
    */
   public static final String NUMBER_TABLE_DESCRIPTOR_NAME = "RX_NUMBER_TABLE";


   /**
    * The maximum number of element in an IN clause of the SQL statement for
    * Oracle database.
    */
   public static final int MAX_IN_CLAUSE_4_ORACLE = 1000;
   
   /**
    * The maximum number of element in the IN clause of a SQL statement for
    * none Oracle database. Other database, such as MS SQL Server can handle 
    * more than 10,000 elements in the IN clause, but more memory and time
    * will be needed to process for a greater amount element in the IN clause.
    */
   public static final int MAX_IN_CLAUSE_4_NONEORACLE = 5000;
   
   /**
    * The currently supported minumum oracle version.
    */
   public static final String MIN_VERSION_ORACLE = "9.2.0.0.0";
}





