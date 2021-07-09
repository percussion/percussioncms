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

package com.percussion.tablefactory;

import com.percussion.util.PSDataTypeConverter;
import com.percussion.util.PSOutputEscaping;
import com.percussion.util.PSSqlHelper;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * This class represents a value to bind in a sql statement.  It is immutable
 * once constructed.
 */
public class PSJdbcStatementColumn
{
   /**
    * Constructor for this class.
    *
    * @param value The value to set on the column, may be <code>null</code> or
    * empty.
    * @param dataType, the jdbc datatype, one of the values defined in {@link
    * java.sql.Types}.
    * @param encoding, the method used to encode this column.  Valid values are
    * <code> PSJdbcColumnData.ENC_TEXT </code>,
    * <code> PSJdbcColumnData.ENC_BASE64 </code>
    * and <code> PSJdbcColumnData.ENC_ESCAPED </code>.
    *
    * @throws IllegalArgumentException if encoding specified is not a valid
    * value.
    */
   public PSJdbcStatementColumn(String value, int dataType, int encoding)
   {
      m_value = value;
      m_type = dataType;

      if (!PSJdbcColumnData.validEncoding(encoding))
         throw new IllegalArgumentException("invalid encoding");

      m_encoding = encoding;
   }

   public PSJdbcStatementColumn(PSJdbcBinaryColumnValue value, int dataType, int encoding){
      m_binaryValue = value;
      m_type = dataType;

      if (!PSJdbcColumnData.validEncoding(encoding))
         throw new IllegalArgumentException("invalid encoding");

      m_encoding = encoding;
   }
   
   /**
    * Constructor without encoding, included for backward compatibility, and
    * for systems that do not encode.
    *
    * @param value The value to set on the column, may be <code>null</code> or
    * empty.
    * @param dataType, the jdbc datatype, one of the values defined in {@link
    * java.sql.Types}.
    *
   **/
   public PSJdbcStatementColumn(String value, int dataType)
   {
      this(value, dataType, PSJdbcColumnData.ENC_TEXT);
   }
   /**
    * @return The value of this column, may be <code>null</code>, or emtpy.
    */
   public String getValue()
   {
      return m_value;
   }
   
   /***
    * @return  The binary column value - including stream for this column, may be null or empty.
    */
   public PSJdbcBinaryColumnValue getBinaryValue(){
      return  m_binaryValue;
   }

   /**
    * @return The jdbc data type of this column.
    */
   public int getType()
   {
      return m_type;
   }

   /**
    * @return the encoding method for this column
    */
   public int getEncoding()
   {
      return m_encoding;
   }

   /**
    * set the encoding method
    * @param encoding, the method used to encode this column.  Valid values are
    * <code> PSJdbcColumnData.ENC_TEXT </code>,
    * <code> PSJdbcColumnData.ENC_BASE64 </code>
    * and <code> PSJdbcColumnData.ENC_ESCAPED </code>.
    *
    * @throws IllegalArgumentException if encoding specified is not a valid
    * value.
    **/
   public void setEncoding(int encoding)
   {
      if (!PSJdbcColumnData.validEncoding(encoding))
         throw new IllegalArgumentException("invalid encoding");

      m_encoding = encoding;
   }

   /**
    * Sets this column's data on the prepared statement using the supplied
    * datatype.
    *
    * @param stmt The PreparedStatement, may not be <code>null</code>.
    * @param bindStart The index of the parameter to bind.
    *
    * @throws IllegalArgumentException if stmt is <code>null</code>.
    * @throws SQLException if an error occurs.
    * @throws IOExcpetion if an error occurs converting any binary values.
    */
   public void setDataFromString(PreparedStatement stmt, int bindStart)
      throws SQLException, IOException
   {
      if (stmt == null)
         throw new IllegalArgumentException("stmt may not be null");

      if (m_value == null && m_binaryValue==null)
         PSSqlHelper.setNullParameter(stmt, bindStart, m_type);
      else
      {
         if(m_binaryValue != null){
            //Set the default behavior to be to set a stream for binaries.
            stmt.setBinaryStream(bindStart, m_binaryValue.getStream(),m_binaryValue.getFileSize());
            return;
         }
         else if(m_encoding == PSJdbcColumnData.ENC_ESCAPED)
         {
            m_value= PSOutputEscaping.unEscape(m_value);
         }
         else if(m_encoding == PSJdbcColumnData.ENC_BASE64)
         {
            byte[] binData = PSDataTypeConverter.getBinaryFromBase64(m_value);
            // do not call
            // PSSqlHelper.setDataFromString
            // in this case because for BLOB data type setDataFromString will
            // try to convert m_value to binary format by calling
            // PSDataTypeConverter.getBinaryFromString
            // while m_value here is in binary format already
            PSSqlHelper.setDataFromByteArray(stmt, bindStart, binData, m_type);
            return;
         }
         PSSqlHelper.setDataFromString(stmt, bindStart, m_value, m_type);
      }
   }

   /**
    * This column's value, initialized in the ctor, may be <code>null</code> or
    * empty, never modified after that.
    */
   private String m_value = null;

   /**
    * This column's jdbc datatype, initialized in the ctor, never modified after
    * that.
    */
   private int m_type;

   /**
    * The encoding value for this column. Valid values are
    * <code> PSJdbcColumnData.ENC_TEXT </code>,
    * <code> PSJdbcColumnData.ENC_BASE64 </code>
    * and <code> PSJdbcColumnData.ENC_ESCAPED </code>.
    */
   private int m_encoding = PSJdbcColumnData.ENC_TEXT;


   private PSJdbcBinaryColumnValue m_binaryValue;
}
