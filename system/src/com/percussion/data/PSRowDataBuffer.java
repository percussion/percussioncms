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

import com.percussion.util.PSSqlHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;


/* JDBC sucks. You can only load data from a row once, and in the
 * appropriate left-right order. As such, we must store the row data
 * by reading it all into our array which will act as the row
 * buffer.
 */
public class PSRowDataBuffer
{
   /**
    * Construct a data buffer for storing the current rows data from a
    * result set.
    *
    * @param      rs               the result set to read from
    *
    * @exception   SQLException   if a SQL error occurs
    */
   public PSRowDataBuffer(ResultSet rs)
      throws java.sql.SQLException
   {
      super();

      ResultSetMetaData meta = rs.getMetaData();
      /* Can we get the database meta data here? */

      m_columnCount = meta.getColumnCount();
      m_curRow = new Object[m_columnCount];
      m_resultSet = rs;
      m_hasData = true;

      m_columnTypes = new int[m_columnCount];
      for (int i = 0; i < m_columnCount; i++)
         m_columnTypes[i] = meta.getColumnType(i+1);
   }

   /**
    * get the number of columns in the combined result set.
    */
   public int getColumnCount()
   {
      return m_columnCount;
   }

   /**
    * Gets the row data that was read by the last call to {@link #readRow()}.
    * Note, the returned row may not be the "current" row if the
    * {@link #skipRow()} is called right before calling this method.
    *
    * @return the row data, which contains appropriately sized data area to use
    *    as the row's data buffer.
    */
   public Object[] getCurrentRow()
   {
      return m_curRow;
   }

   /**
    * Skips the next row in the buffer if there are any unread data.
    *
    * @return <code>true</code> if successfully skipped the next row;
    *    <code>false</code> if there is no unread data in the buffer.
    *
    * @throws SQLException if a SQL error occurs
    */
   public boolean skipRow() throws java.sql.SQLException
   {
      if (!m_hasData)
      {
         return false;
      }
      else
      {
         m_hasData = m_resultSet.next();
         return m_hasData;
      }
   }


   /**
    * read the next row from the result set.
    *
    * @exception   SQLException   if a SQL error occurs
    */
   public boolean readRow()
      throws java.sql.SQLException
   {
      if (!m_hasData)
         return false;

      if ( (m_hasData = m_resultSet.next()) ) {
         for (int i = 0; i < m_columnCount; i++) {
            m_curRow[i] = getResultColumn(m_resultSet, m_columnTypes[i], i+1);
            if (m_curRow[i] instanceof byte[])
               m_curRow[i] = new PSBinaryData((byte []) m_curRow[i]);

            if (m_resultSet.wasNull())
               m_curRow[i] = null;
         }
      } else {
         for (int i = 0; i < m_columnCount; i++) {
            m_curRow[i] = null;
         }
      }

      return m_hasData;
   }

   static Object getResultColumn(ResultSet rs, int colType, int index)
      throws java.sql.SQLException
   {
      Object colData = null;
      if (!PSSqlBuilder.isJdbcDataType(colType)) {
         /* Attempt to ascertain special case types from our
            kludge layer (contained in the Sql Builder)
            Bug Id: -00-01-0001 */

         short newType = PSSqlBuilder.guessNativeDataTypeConversion((short) colType);
         colType = newType;
      }
      else if ( colType == Types.OTHER )
      {
         /* One of the changes they made between 1.2 and 1.3 in the bridge
            surrounds the reported JDBC type. Previously, all NLS and datetime
            data types (at least for SQL server) were reported as their native
            sql type. We created a map to fixup the types in PSSqlBuilder.
            In 1.3, the datetimes now have the correct type, but all NLS types
            have the type OTHER. I would expect this to mean that if they are
            passed to the getObject method, it would handle them correctly.
            Uh-uh. It does nothing with these types. So what I do here is get
            the sql type from the result set, which is the underlying sql type,
            and use that with the map. The map converts all of the NLS types
            to their corresponding non-NLS types (JDBC doesn't have specific
            NLS types because the std types support internationalized charsets).
         */
         
      }

      /*
       * PSResultSet doesn't support LOB methods, any data stored in one
       * will be accessible through the corresponding LONG-based calls.
       */
      if (rs instanceof PSResultSet ||
        (rs instanceof PSFilterResultSetWrapper &&
           ((PSFilterResultSetWrapper)rs).isResultSetInstanceOfPSResultSet()))
      {
         if (colType == Types.CLOB)
            colType = Types.LONGVARCHAR;
         if (colType == Types.BLOB)
            colType = Types.LONGVARBINARY;
      }

      switch (colType) {
         case Types.BINARY:
         case Types.VARBINARY:
         case Types.LONGVARBINARY:
            /**
             * *** NOTE *** NOTE *** NOTE *** NOTE *** NOTE *** NOTE ***
             *
             * This rather ugly piece of code is required for various
             * kludgy reasons.
             *
             *  1. jdbc clears the stream after a call to next or getXXX
             *  2. rs.getBytes does character translation?! which breaks
             *  binary data
             *  3.   the length of the stream is not guaranteed -- that is,
             *  a call to available is not necessarily the amount of
             *  data pending!
             *
             * So instead, I'm reading the data a chunk at a time into
             * an output stream. The last chunk of data read will be less
             * than our stream size, which is our end condition. We can
             * then use the byte[] to create a byte array input stream.
             */
            byte[] aData = new byte[2048];
            InputStream rowData = rs.getBinaryStream(index);
            if ( null == rowData )
            {
               colData = null;
            }
            else
            {
               ByteArrayOutputStream bout = new ByteArrayOutputStream();
               try {
                  int iRead = rowData.read(aData);
                  if (iRead < 0)
                     return null;
                  else
                     bout.write(aData, 0, iRead);

                  for (; iRead == aData.length; ) {
                     iRead = rowData.read(aData);
                     if (iRead > 0) // it could be -1 if no more data
                        bout.write(aData, 0, iRead);
                  }
               } catch (java.io.IOException e) {
                  throw new java.sql.SQLException(e.getMessage());
               }
               colData = new PSBinaryData(bout.toByteArray());
            }
            break;

         case Types.BLOB:
            Blob b = rs.getBlob(index);
            if (b == null)
            {
               colData = null;
            }
            else
            {
               long blen = b.length();
               if (blen > (long) Integer.MAX_VALUE)
                  throw new SQLException("Length of blob " + blen +
                  " is greater then maximum allowed by Rhythmyx at this time");
               colData = new PSBinaryData(b.getBytes(1, (int) blen));
            }
            break;

         /* This, in addition to the type-fixup logic  fixes the SQL server
            ODBC driver bug which returns NULL when getObject on a text
            column instead of the corresponding string! */
         case Types.CHAR:
         case Types.VARCHAR:
//         case Types.LONGVARCHAR:
            colData = rs.getString(index);
            break;

         /* A few bugs appeared with 1.3 JRE. I will describe them here. I
            don't want to build our own JRE again, so I'm doing work-arounds
            here, one of which is very ugly. All the bugs have been reported
            in the Java Bug Report (bug ids listed first). I limit the scope
            of these hacks to the bridge driver by checking the datatype of
            the result set.

            1.Bug Id  4400343: ResultSet.getString() returns "" if datatype is
            LONGVARCHAR and length is 1.
            [I fix this by using the getCharacterStream() method rather than
            the getString method we used to use. Because of a bug with this
            method for datatype ntext, I can't use it for all LONGVARCHAR
            types.] This was the original fix. However, it only worked for
            1.3.1, not 1.3.0. In the latter, we had to use getAsciiStream or
            we got back a null between every character.

            2. Bug Id  4404714: SQL Server: getString() on text column with
            null value throws Exception. (This is fixed in 3.1.1)
            This bug occurs due to another bug in the driver that causes the
            column length to be returned as INT_MAX or 1 less all the time,
            even when the column is null. Because of this, the code goes into
            a block (that was added in 1.3) to use getAsciiStream. Of course
            a null stream is returned (correctly), but the return is never
            checked. In 1.3.1, a NullPointerException is thrown. I get around
            this by catching the Exception and setting the colData to null.
            In 1.3.0, the NullPtrExc is caught and wrapped in a SQLException.
            It would be very ugly to differentiate this exception from a valid
            one, so in this case, we use getAsciiStream instead.

            3. Bug Id  4379373: UTF-16 returned as UTF-8 from SQL Server for
            type ntext field (nvarchar OK).
            This is VERY bizarre. An example will explain it best. If I have
            the string "abc" in the database, what I get back is the following
            string "610062006300". The astute observer will notice that this
            is a representation of the Unicode-16 hex values in little endian
            format. I used this knowlegde and created a fixup routine that
            converts such a string to its proper form. I only do this for
            ntext fields.
            NOTE: This will have to be removed once we upgrade (assuming they
            fix the problem).

            You'll notice that I get the native type from the result set meta
            data to determine what to do. This is because the java types for
            all nls datatypes are 1111, which is not very useful. These are
            converted to the corresponding non-nls type at the top of this
            method.
         */

         case Types.LONGVARCHAR:
         {
               colData = rs.getString(index);
            break;
         }
         case Types.CLOB:
            Clob c = rs.getClob(index);

            if (c == null)
            {
               colData = null;
            }
            else
            {
               long clen = c.length();

               int ilen = 0;
               if (clen > (long) Integer.MAX_VALUE)
               {
                  throw new SQLException("Length of clob " + clen +
                  " is greater then maximum allowed by Rhythmyx at this time");
               }
               try
               {
                  colData =
                     PSSqlHelper.getClobColumnData(c.getCharacterStream());
               }
               catch (IOException ioe)
               {
                  throw new SQLException(ioe.getLocalizedMessage());
               }
            }
            break;

         // A workaround for Oracle 10g driver for 'DATE' column, which stores
         // both date and time. The rs.getObject(int) will return a 'Date'
         // object for the 'DATE' column and the 'Date.toString()' method only
         // returns date info (YYYY-MM-DD), but the 'Timestamp.toString()' will
         // return both date & time (YYYY-MM-DD HH24:MI:SS.D).
         case Types.DATE:
            colData = rs.getTimestamp(index);
            break;

         default:
            // by default, we'll assume we have a readily consumable object
            colData = rs.getObject(index);
            break;
      }

      if (rs.wasNull()) {
         colData = null;
      }

      return colData;
   }

   /**
    * This is a hack to fix a odbc bridge bug. See the description inside
    * the getResultColumn method. Basically, it interprets a string of digits,
    * in which each group of 4 digits represents a UTF-16 character. The
    * digits are all converted and the characters they represent are returned
    * as a String.
    *
    * @param data The string to convert. If <code>null</code>, empty or not
    *    a multiple of 4 long, it is returned w/o conversion. Otherwise a
    *    conversion is attempted.
    *
    * @return The possibly converted string. May be <code>null</code> or empty
    *    if that's what was passed in.
    *
    * @throws SQLException If the 'UTF-16' character encoding is not supported
    *    by Java or IO exceptions occur in the local byte stream. Neither of
    *    these should ever happen.
    */
   private static String fixupUtf16SqlServerBug( String data )
      throws SQLException
   {
      if ( null == data || data.trim().length() == 0 )
         return data;

      // safety check, 4 chars represent a single unicode char
      if ( data.length() % 4 != 0 )
         return data;

      String val = null;
      try
      {
         char [] numBuf = new char[2];

         ByteArrayOutputStream os = new ByteArrayOutputStream();
         // add the byte order mark for little endian
         byte [] bom = new byte[2];
         bom[0] = (byte) 0xff;
         bom[1] = (byte) 0xfe;
         os.write( bom );
         int len = data.length();
         for ( int i = 0; i < len; i+=2 )
         {
            numBuf[0] = data.charAt(i);
            numBuf[1] = data.charAt(i+1);
            int utf16Octet = Integer.parseInt( new String( numBuf ), 16 );
            os.write((byte) utf16Octet );
         }
         val = new String( os.toByteArray(), "UTF-16" );
      }
      catch ( UnsupportedEncodingException e )
      {
         throw new SQLException( e.getLocalizedMessage());
      }
      catch ( IOException ioe )
      {
         throw new SQLException( ioe.getLocalizedMessage());
      }

      return val;
   }

   private Object[]      m_curRow;
   private ResultSet      m_resultSet;

   /**
    * Initialized by ctor; @see #hasData() for detail
    */
   private boolean      m_hasData;

   private int            m_columnCount;
   private int[]         m_columnTypes;
}

