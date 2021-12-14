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

package com.percussion.debug;

import com.percussion.data.PSBinaryData;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Used to generate trace message for the Result SEt Trace message type (0x8000).
 * 
 * Prints out result set.  Binary data is displayed in hex format.
 */
public class PSTraceResultSet extends PSTraceMessage
{
   
   /**
    * Constructor for this class.
    * 
    * @param typeFlag the type of trace message this object will generate
    * @roseuid 3A032351007D
    */
   public PSTraceResultSet(int typeFlag)
   {
      super(typeFlag);
   }

   /**
    * Formats the output for the body of the message, extracting the information
    * required from the source object.
    *
    * @param source an array of objects containing the information required for the
    * trace message:
    * - PSResultSet the merged resultset after the join
    * @return the message body
    * @roseuid 3A03237C0148
    */
   protected String getMessageBody(Object source)
   {
      String msg = null;
      
      Object[] args = (Object[])source;

      if ((args.length != 2) || !(args[0] instanceof String[]))
         throw new IllegalArgumentException("Invalid source args");

      // need to print out result set
      msg = printResultSet(args);

      return msg;
   }
   
   //see parent class for javadoc
   protected String getMessageHeader()
   {
      return ms_bundle.getString("traceResultSet_dispname");
   }


   /**
    * formats result set into tabular format as a string
    *
    * @param rs the result set
    * @return the formatted output as a string
    */
   private String printResultSet(Object[] args)
   {
      StringBuilder buf = new StringBuilder();

      // get the column info
      String[] cols = (String[])args[0];
      int[] maxLen = new int[cols.length];

      // walk the column headers and save their lengths
      for (int i = 0; i < cols.length; i++)
      {
         String col = cols[i];
         if ((col == null) || (col.length() == 0))
            col = "col" + String.valueOf(i+1);
         else
            col = col.toLowerCase();

         // save the widths
         maxLen[i] = col.length();
      }

      // determine max width for each column
      Iterator rows = ((ArrayList)args[1]).iterator();
      while (rows.hasNext())
      {
         // check each column
         Object[] colData = (Object[])rows.next();
         for (int i = 0; i < colData.length; i++)
         {
            if(colData[i] != null)
            {
               int width = 0;
               // handle binary data differently
               if (colData[i] instanceof PSBinaryData)
               {
                  // check number of bytes
                  PSBinaryData data = (PSBinaryData)colData[i];
                  width = (data.getByteArray().length * BYTE2HEX_FACTOR);

                  // account for preprending the '0x'
                  width += HEX_PREFIX.length();
               }
               else
                  width = colData[i].toString().length();

               width = (width < MAX_COL_WIDTH ? width : MAX_COL_WIDTH);
               maxLen[i] = (width > maxLen[i]) ? width : maxLen[i];
            }
         }
         buf.append(NEW_LINE);

      }


      // now add the col names
      for (int i = 0; i < cols.length; i++)
      {
         buf.append(padLeft(cols[i], ' ', maxLen[i]));
         buf.append(COL_SPACER);
      }

      // add a row of underlines
      buf.append(NEW_LINE);
      for (int i = 0; i < cols.length; i++)
      {
         addChars(buf, '-', maxLen[i]);
         buf.append(COL_SPACER);
      }
      buf.append(NEW_LINE);

      // walk each row
      rows = ((ArrayList)args[1]).iterator();
      while (rows.hasNext())
      {
         // add each column, truncated as required
         Object[] colData = (Object[])rows.next();
         for (int i = 0; i < colData.length; i++)
         {
            if(colData[i] != null)
            {
               /* handle binary data so we convert to hex, and only the number
                * of bytes required
                */
               if (colData[i] instanceof PSBinaryData)
               {
                  PSBinaryData data = (PSBinaryData)colData[i];

                  // determine number of bytes that will fit, account for prefix
                  int size = ((MAX_COL_WIDTH - HEX_PREFIX.length())
                      / BYTE2HEX_FACTOR);

                  // see if we've got too many
                  byte[] source = data.getByteArray();
                  if (source.length > size)
                  {
                     // truncate it
                     byte[] target = new byte[size];
                     System.arraycopy(source, 0, target, 0, size);
                     data = new PSBinaryData(target);
                  }

                  // now output it as hex
                  buf.append(HEX_PREFIX);
                  buf.append(padLeft(data.toHexString(), ' ', maxLen[i]));
               }
               else
                  buf.append(padLeft(colData[i].toString(), ' ', maxLen[i]));
            }
            else
               buf.append(padLeft("", ' ', maxLen[i]));
            buf.append(COL_SPACER);
         }
         buf.append(NEW_LINE);

      }

      return new String(buf);
   }


   /**
    * appends the character to the buffer the specified number of times
    * @param buf the buffer to append to
    * @param addChar the character to repeat
    * @param len the number of chars to add
    */
   private void addChars(StringBuilder buf, char addChar, int len)
   {
      for (int i = 0; i < len; i++)
         buf.append(addChar);
   }

   /**
    * returns the supplied string adjusted to the specified length.  If the
    * string is longer than the specified witdth, it is truncated.  If it is
    * shorter than the specified width, it is padded with the specified char
    * @param source the string to pad
    * @param addChar the character to repeat
    * @param len the number of chars to add
    * @return the adjusted string
    */
   private String padLeft(String source, char addChar, int len)
   {
      int strLen = source.length();
      String result = null;

      if (strLen == len)
         // it fits already
         result = source;
      else if (strLen > len)
      {
         // need to truncate
         result = source.substring(0, len - 1);
      }
      else
      {
         // pad out to desired length
         StringBuilder buf = new StringBuilder(len);
         buf.append(source);
         addChars(buf, addChar, len - strLen);
         result = new String(buf);
      }

      return result;
   }



   /**
    * the max column width for result set printout
    */
   private static final int MAX_COL_WIDTH = 20;

   /**
    * the number of spaces between each column
    */
   private static final String COL_SPACER = "  ";

   /**
    * the number of hex characters per byte of data used for binary conversions
    */
   private static final int BYTE2HEX_FACTOR = 2;

   /**
    * the string preprended to binary data displayed as hex
    */
   private static final String HEX_PREFIX = "0x";
}
