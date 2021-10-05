/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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

import com.percussion.design.objectstore.PSDateLiteral;
import com.percussion.design.objectstore.PSLiteral;
import com.percussion.design.objectstore.PSLiteralSet;
import com.percussion.design.objectstore.PSNumericLiteral;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.server.PSConsole;
import com.percussion.util.PSDataTypeConverter;
import org.apache.commons.lang3.time.FastDateFormat;

import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * The PSDataConverter class is used to convert potential comparable data
 * from one certain type to another. When using this class, two kinds of data
 * with different type will be able to be compared with each other.
 * <p>
 * For example, suppose we have the following data:
 * java.math.BigDecimal num = 10;
 * String oneNumberInString = 20;
 * If we were asked to determine mathematically whether a number 10 is less than
 * the other number 20, then what we can do here is to convert oneNumberInString
 * from type String to type java.math.BigDecimal. Once this is done, since both
 * num and oneNumberInString are of the same type, mathematical operation can be
 * applied.
 * <p>
 * Currently, the convertable data types are BigDecimal, PSNumericLiteral, Date,
 * PSDateLiteral, String, PSTextLiteral, and File.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public abstract class PSDataConverter
{
   /**
    * Convert an object to one specified data type, if possible. However, this
    * method will not convert type PSLiteralSet so that it won't be changed.
    * To handle type PSLiteralSet which is quite complicated, in class
    * com.percussion.data.PSConditionalEvaluator, method makeComparable2,
    * the data in the set is type determined and comparing operation performed.
    * <p>
    * If dateFormat is null, then our default formats will be activated one by one.
    * This only happens when trying to convert a type of String or PSTextLiteral to
    * a type of Date. A typical case is when data object is a String/PSTextLiteral,
    * srcType is DATATYPE_TEXT, and dstType is DATATYPE_DATE. If data is not in
    * a supported date format pattern, then a IllegalArgumentException is thrown.
    * <p>
    * With default format being activated, here are some working cases:
    * (1) "1999-08-12 00:00:00.123";
    * (2) "1999.08.12";
    * (3) "1999.08.12 AD";
    * (4) "1999.08.12 AD at 14:04:24";
    * (5) "1999.08.12 at 01:01:01 PDT".
    *
    * @param   data         the object to convert
    *
    * @param   dstType      the data type to convert to
    *
    * @param   dateFormat   the dateFormat of a string representing a date
    *
    * @return               the converted object
    */
   public static Object convert(Object data, int dstType,
                                FastDateFormat dateFormat)
   {
      int srcType = getDataType(data);

      switch (srcType)
      {
         case DATATYPE_NUMERIC:
            if (data instanceof PSNumericLiteral)
               data = ((PSNumericLiteral)data).getNumber();

            switch (dstType) {
               case DATATYPE_DATE:
                  if(data != null)
                     return new Date(((java.lang.Number)data).longValue());
                  break;
               case DATATYPE_TEXT:
                  return data.toString();
               case DATATYPE_INT:
               case DATATYPE_LONG:
               case DATATYPE_DOUBLE:
                  throw new IllegalArgumentException("data invalid conversion" +
                     getTypeString(srcType) + " " + getTypeString(dstType));
               default:

            }
            break;

         case DATATYPE_DOUBLE:
            switch (dstType) {
               case DATATYPE_NUMERIC:
                  return new BigDecimal(((java.lang.Number)data).doubleValue());

               case DATATYPE_DATE:
                  return new Date(((java.lang.Number)data).longValue());

               case DATATYPE_TEXT:
                  return data.toString();

               case DATATYPE_INT:
               case DATATYPE_LONG:
                  throw new IllegalArgumentException("data invalid conversion" +
                     getTypeString(srcType) + " " + getTypeString(dstType));
               default:
            }
            break;

         case DATATYPE_LONG:
            switch (dstType) {
               case DATATYPE_NUMERIC:
                  return BigDecimal.valueOf(((java.lang.Number)data).longValue());

               case DATATYPE_DATE:
                  return new Date(((java.lang.Number)data).longValue());

               case DATATYPE_TEXT:
                  return data.toString();
               case DATATYPE_INT:
               case DATATYPE_DOUBLE:
                  throw new IllegalArgumentException("data invalid conversion" +
                     getTypeString(srcType) + " " + getTypeString(dstType));
               default:
            }
            break;

         case DATATYPE_INT:
            switch (dstType) {
               case DATATYPE_NUMERIC:
                  return BigDecimal.valueOf(((java.lang.Number)data).longValue());

               case DATATYPE_DATE:
                  return new Date(((java.lang.Number)data).longValue());

               case DATATYPE_TEXT:
                  return data.toString();

               case DATATYPE_LONG:
                  return new Long(((java.lang.Number)data).longValue());

               case DATATYPE_DOUBLE:
                  return new Double(((java.lang.Number)data).doubleValue());
               default:
            }
            break;

         case DATATYPE_DATE:
            if (data instanceof PSDateLiteral)
               data = ((PSDateLiteral)data).getDate();

            switch (dstType) {
               case DATATYPE_NUMERIC:
                  return BigDecimal.valueOf(((java.util.Date)data).getTime());

               case DATATYPE_LONG:
                  return new Long(((java.util.Date)data).getTime());

               case DATATYPE_DATE:
                  return (java.util.Date)data;

               case DATATYPE_TEXT:
                  return data.toString();

               case DATATYPE_DOUBLE:
               case DATATYPE_INT:
                  throw new IllegalArgumentException("data invalid conversion" +
                      getTypeString(srcType) + " " + getTypeString(dstType));
            }
            break;

         case DATATYPE_NULLTEXT: /* Nulltext added BugId: Rx-99-10-0122 */
         case DATATYPE_TEXT:
            data = data.toString();

            switch (dstType) {
               case DATATYPE_NUMERIC:
                  try {
                     return new BigDecimal((String) data);
                  } catch (NumberFormatException e)
                  {
                     throw new IllegalArgumentException("cannot convert with reason" +
                        getTypeString(srcType) + " " + getTypeString(dstType) + " " + e.toString());
                  }

               case DATATYPE_LONG:
                  try {
                     return new Long((String) data);
                  } catch (NumberFormatException e)
                  {
                     throw new IllegalArgumentException("cannot convert with reason" +
                        getTypeString(srcType) + " " + getTypeString(dstType) + " " + e.toString());
                  }


               case DATATYPE_INT:
                  try {
                     return new Integer((String) data);
                  } catch (NumberFormatException e)
                  {
                     throw new IllegalArgumentException("cannot convert with reason" +
                        getTypeString(srcType) + " " + getTypeString(dstType) + " " + e.toString());
                  }


               case DATATYPE_DOUBLE:
                  try {
                     return new Double((String) data);
                  } catch (NumberFormatException e)
                  {
                     throw new IllegalArgumentException("cannot convert with reason" +
                        getTypeString(srcType) + " " + getTypeString(dstType) + " " + e.toString());
                  }


               case DATATYPE_DATE:
                  Date day = null;
                  //check String to convert against specified dateformat,
                  //if not check against all valid formats
                  if(dateFormat != null) {
                       try{
                        day = dateFormat.parse((String)data);
                        return day;
                     } catch (Exception e){
                        String subsystem = "DataConverter";
                        String msg = "Not in specified format, ";
                        msg += "Trying to check all valid formats";
                        PSConsole.printMsg(subsystem, msg);
                        //make format null,so it checks against all valid formats
                        dateFormat = null;
                     }
                  }

                  if (dateFormat == null){
                     try {
                        day = parseStringToDate((String)data);
                        return day;
                     } catch (Exception e){
                        String subsystem = "DataConverter";
                        String msg = "One recommended text pattern is yyyy-MM-dd, ";
                        msg += "such as 2000-03-30 19:04:45";
                        PSConsole.printMsg(subsystem, msg);
                        throw new IllegalArgumentException("argument error");
                     }
                  }


               case DATATYPE_TEXT:
                  return data;
            }
            break;
         case DATATYPE_FILE:
            switch (dstType)
            {
               case DATATYPE_NULL:
               case DATATYPE_NULLTEXT:
                  // Return either null (file is empty) or data (file is not empty)
                  File f = (File) data;
                  if (f.length() > 1)
                     return data;
                  else
                     return null;
               default:
                  String subSystem = "DataConverter";
                  String msg = "Can not compare file with specified type.";
                  throw new IllegalArgumentException("cannot convert with reason" +
                     getTypeString(srcType) + " " + getTypeString(dstType) + " " + msg);
            }
      }

      return data;
   }

    /**
    * Convert a text object delimited by commas or a List of objects
    * to PSLiteralSet of either PSTextLiteral or PSNumericLiteral or
    * PSDateLiteral.
    *
    * notes: If none of the above then returns an unconverted object.
    * It returns an object of PSLiteralSet with elements of PSDateLiteral
    * if all tokens of the data is in the format of Date.
    * Else an object of PSLiteralSet with elements of PSTextLiteral
    *
    * @param data   the object to convert, must not be <code>null</code>.
    *
    * @return the converted object, never <code>null</code>.
    */
   public static Object convertToSet(Object data)
   {
      if(data == null)
         throw new IllegalArgumentException("data to convert can not be null");

      if (data instanceof PSLiteralSet)
      {
         return data; //already a literal set
      }

      PSLiteralSet literalSet = new PSLiteralSet(PSLiteral.class);
      List<String>  valueSet = new ArrayList<>();

      if (getDataType(data) == DATATYPE_TEXT)
      {
         StringTokenizer st = new StringTokenizer(data.toString(), ",");
         while(st.hasMoreTokens())
         {
            valueSet.add(st.nextToken());
         }

         if (valueSet.isEmpty())
            valueSet.add(data.toString()); //a set can have only one item

         boolean isDate = true;

         //Check whether all values are of Type Date, If not treat them as text
         Iterator<String> itValueSet = valueSet.iterator();

         while(isDate && itValueSet.hasNext())
         {
            PSDateLiteral day = null;
            try
            {
               StringBuilder formatBuf = new StringBuilder();
               Date date = PSDataTypeConverter.parseStringToDate(
                  itValueSet.next(), formatBuf);

               if (date != null)
               {
                  day = new PSDateLiteral(date, FastDateFormat.getInstance(
                     formatBuf.toString()));

                  literalSet.add(day);
               }
            }
            catch(IllegalArgumentException pe)
            {
               throw new IllegalArgumentException("Illegal Date Argument");
            }

            isDate = (day != null);
         }
         
         // try now for numeric, default to text
         boolean treatAsText = !isDate;
         if (treatAsText)
         {
            // assume we'll succeed
            treatAsText = false;
            literalSet.clear();
            for (String text : valueSet)
            {
               try
               {
                  int numVal = Integer.parseInt(text);
                  literalSet.add(new PSNumericLiteral(numVal, 
                     new DecimalFormat()));
               }
               catch (NumberFormatException e)
               {
                  treatAsText = true;
                  break;
               }
            }
         }

         if(treatAsText)
         {
            literalSet.clear();

            itValueSet = valueSet.iterator();

            while(itValueSet.hasNext())
            {
               PSTextLiteral text = new PSTextLiteral(itValueSet.next());
               literalSet.add(text);
            }
         }

         return literalSet;
      }


      if (data instanceof List)
      {
         List list = (List)data;
         Iterator it = list.iterator();

         while (it.hasNext())
         {
            PSLiteral literal = null;
            Object obj = it.next();

            if (obj instanceof PSLiteral)
            {
               //already PSLiteral - no need to convert

               literalSet.add(obj);
               continue;
            }

            int type = getDataType(obj);

            switch(type)
            {
            case DATATYPE_NUMERIC:
            case DATATYPE_LONG:
            case DATATYPE_INT:
            case DATATYPE_DOUBLE:
               try
               {
                  literal = new PSNumericLiteral((Number)obj, new DecimalFormat());
               }
               catch(IllegalArgumentException pe)
               {
                  throw new IllegalArgumentException("Illegal Numeric Argument");
               }
               break;

            case DATATYPE_DATE:
               try
               {
                  literal = new PSDateLiteral((Date)obj, FastDateFormat.getInstance());
               }
               catch(IllegalArgumentException pe)
               {
                  throw new IllegalArgumentException("Illegal Date Argument");
               }
               break;

            case DATATYPE_TEXT:
               literal = new PSTextLiteral(obj.toString());
               break;

            default:
               return data;
            }

            literalSet.add(literal);
         }

         return literalSet;
      }


      return data; //no conversion made
   }

   /**
    * Convert data from one type to another type. This is the same as calling
    * method convert(data, dstType, null). Any default date format
    * will not be activated unless conversion between String/PSTextLiteral
    * and Date/PSDateLiteral is performed. If this is the case, then the String
    * representing for a date have to be in one default date format in order to
    * perform conversion successfully.
    * <p>
    * Example: String oneDateInString = "1999.08.12 AD at 14:04:56";
    * which means August 12, 1999 after domino, at the fourteenth hour, the fourth
    * minitue, and the fifty sixth second. If oneDateInString is not in any given
    * date format pattern, then an exception will be thrown. A typical case is
    * when data is a String/PSTextLiteral, srcType is DATATYPE_TEXT, and dstType
    * is DATATYPE_DATE.
    */
   public static Object convert(Object data, int dstType)
   {
      return convert(data, dstType, null);
   }

   /**
    * Determine the data type we can most easily convert an object to.
    *
    * @param   data         the data object to check
    *
    * @return               the PSDataConverter.DATATYPE_xxx type flag
    */
   public static int getDataType(Object data)
   {
      if (data == null)
         return DATATYPE_NULL;
      else if ((data instanceof PSDateLiteral) ||
         (data instanceof java.util.Date))
         return DATATYPE_DATE;
      else if ((data instanceof PSNumericLiteral) ||
         (data instanceof java.math.BigInteger) ||
         (data instanceof java.math.BigDecimal))
         return DATATYPE_NUMERIC;
      else if ((data instanceof java.lang.Byte) ||
               (data instanceof java.lang.Short) ||
               (data instanceof java.lang.Integer))
         return DATATYPE_INT;
      else if ((data instanceof java.lang.Float) ||
               (data instanceof java.lang.Double))
         return DATATYPE_DOUBLE;
      else if (data instanceof java.lang.Long)
         return DATATYPE_LONG;
      else if (data instanceof PSTextLiteral)
         return DATATYPE_TEXT;
      else if (data instanceof java.lang.String)
      {
         /* Nulltext added BugId: Rx-99-10-0122, this is to allow
            the recognition of a null column value, which rhythmyx
            converts to a null string. */
         if (((String)data).equals(""))
            return DATATYPE_NULLTEXT;
         else
            return DATATYPE_TEXT;
      }
      else if (data instanceof byte[])
         return DATATYPE_BINARY;
      else if (data instanceof PSLiteralSet) {
         PSLiteralSet ls = (PSLiteralSet)data;

         if (ls.isEmpty())
            return DATATYPE_NULL;

         return getDataType(ls.get(0)) | DATATYPE_SET_FLAG;
      }
      else if (data instanceof List) {
         List ls = (List)data;

         if (ls.isEmpty())
            return DATATYPE_NULL;

         return getDataType(ls.get(0)) | DATATYPE_SET_FLAG;
      }
      else if (data instanceof File)
      {
         return DATATYPE_FILE;
      }

      return DATATYPE_UNKNOWN;
   }

   /**
    * Determine the best data type to use when comparing the specified types.
    *
    * @param   leftType      the left data type
    *
    * @param   rightType   the right data type
    *
    * @return               the PSDataConverter.DATATYPE_xxx type flag
    */
   public static int getBestComparisonType(int leftType, int rightType)
   {
      /* Special case the case where the DB contains null
         ("" here) to use string comparison, so an error will
         not occur when saying, for instance 1=NULL
         BugId: Rx-99-10-0122 */
      if ((leftType == DATATYPE_NULLTEXT) ||
         (rightType == DATATYPE_NULLTEXT))
      {
         if ((leftType == DATATYPE_FILE) ||
            (rightType == DATATYPE_FILE))
         {
            return DATATYPE_NULLTEXT;
         }
         return DATATYPE_TEXT;
      }

      switch (leftType)
      {
         case DATATYPE_NUMERICSET:
         case DATATYPE_NUMERIC:
            switch (rightType) {
               case DATATYPE_NUMERIC:
               case DATATYPE_NUMERICSET:
               case DATATYPE_DATE:
               case DATATYPE_DATESET:
               case DATATYPE_TEXT:
               case DATATYPE_TEXTSET:
               case DATATYPE_DOUBLE:
               case DATATYPE_LONG:
               case DATATYPE_INT:
                  return DATATYPE_NUMERIC;
            }
            break;

         case DATATYPE_LONG:
            switch (rightType) {
               case DATATYPE_NUMERIC:
               case DATATYPE_DOUBLE:
               case DATATYPE_NUMERICSET:
               case DATATYPE_DATE:
               case DATATYPE_DATESET:
                  return DATATYPE_NUMERIC;

               case DATATYPE_INT:
               case DATATYPE_LONG:
               case DATATYPE_TEXT:
               case DATATYPE_TEXTSET:
                  return DATATYPE_LONG;
            }
            break;

         case DATATYPE_INT:
            switch (rightType) {
               case DATATYPE_NUMERIC:
               case DATATYPE_NUMERICSET:
               case DATATYPE_DATE:
               case DATATYPE_DATESET:
                  return DATATYPE_NUMERIC;

               case DATATYPE_INT:
                  return DATATYPE_INT;

               case DATATYPE_TEXT:
               case DATATYPE_TEXTSET:
// promote string ints to double   return DATATYPE_INT;
               case DATATYPE_DOUBLE:
                  return DATATYPE_DOUBLE;

               case DATATYPE_LONG:
                  return DATATYPE_LONG;
            }
            break;

         case DATATYPE_DOUBLE:
            switch (rightType) {
               case DATATYPE_LONG:
               case DATATYPE_NUMERIC:
               case DATATYPE_NUMERICSET:
               case DATATYPE_DATE:
               case DATATYPE_DATESET:
                  return DATATYPE_NUMERIC;

               case DATATYPE_INT:
               case DATATYPE_TEXT:
               case DATATYPE_TEXTSET:
               case DATATYPE_DOUBLE:
                  return DATATYPE_DOUBLE;
            }
            break;

         case DATATYPE_DATESET:
         case DATATYPE_DATE:
            switch (rightType) {
               case DATATYPE_INT:
               case DATATYPE_LONG:
               case DATATYPE_DOUBLE:
               case DATATYPE_NUMERIC:
               case DATATYPE_NUMERICSET:
               case DATATYPE_DATE:
               case DATATYPE_DATESET:
                  return DATATYPE_NUMERIC;

               case DATATYPE_TEXT:
               case DATATYPE_TEXTSET:
                  return DATATYPE_DATE;
            }
            break;

         case DATATYPE_TEXTSET:
         case DATATYPE_TEXT:
            switch (rightType) {
               case DATATYPE_NUMERIC:
               case DATATYPE_NUMERICSET:
                  return DATATYPE_NUMERIC;

               case DATATYPE_LONG:
                  return DATATYPE_LONG;

               case DATATYPE_INT:
// promote string ints to double   return DATATYPE_INT;
               case DATATYPE_DOUBLE:
                  return DATATYPE_DOUBLE;

               case DATATYPE_DATE:
               case DATATYPE_DATESET:
                  return DATATYPE_DATE;

               case DATATYPE_TEXT:
               case DATATYPE_TEXTSET:
               case DATATYPE_BINARY:
               case DATATYPE_BINARYSET:
                  return DATATYPE_TEXT;
            }
            break;

         case DATATYPE_BINARYSET:
         case DATATYPE_BINARY:
            return DATATYPE_BINARY;

         case DATATYPE_NULL:
            if (rightType == DATATYPE_FILE)
               return DATATYPE_NULL;
            else
               return rightType;

         case DATATYPE_FILE:
            switch (rightType) {
               case DATATYPE_NULL:
               case DATATYPE_NULLTEXT:
                  return rightType;
            }
         break;

         case DATATYPE_UNKNOWN:
            switch (rightType) {
               case DATATYPE_TEXT:
               case DATATYPE_TEXTSET:
                  return DATATYPE_TEXT;
            }
      }

      return DATATYPE_UNKNOWN;
   }

   /**
    * Get the text description of the specified data type.
    *
    * @param   type      the DATATYPE_xxx type
    *
    * @return            the text description of the type
    */
   public static String getTypeString(int type)
   {
      switch (type)
      {
         case DATATYPE_NUMERICSET:
            return "numeric set";

         case DATATYPE_NUMERIC:
            return "numeric";

         case DATATYPE_INT:
            return "integer";

         case DATATYPE_LONG:
            return "long";

         case DATATYPE_DOUBLE:
            return "double";

         case DATATYPE_DATESET:
            return "date set";

         case DATATYPE_DATE:
            return "date";

         case DATATYPE_TEXTSET:
            return "text set";

         case DATATYPE_TEXT:
            return "text";

         case DATATYPE_BINARYSET:
            return "binary set";

         case DATATYPE_BINARY:
            return "binary";

         case DATATYPE_NULL:
            return "null";

         case DATATYPE_NULLTEXT:
            return "null text";

         case DATATYPE_FILE:
            return "file";

         // case DATATYPE_UNKNOWN:
         default:
            return "unknown";
      }
   }


   /**
    * Compare the two objects comparable and process the comparison.
    *
    * @param   left         the left object
    *
    * @param   right         the right object
    *
    * @return               -1 if left is less than right; 0 if they are
    *                        equal; 1 if left is greater than right
    */
   public static int compare(Object left, Object right)
   {
      int leftType  =   getDataType(left);
      int rightType =   getDataType(right);
      int bestType  =   getBestComparisonType(leftType, rightType);

      // we don't support comparing sets at this time
      if (((leftType & DATATYPE_SET_FLAG) != 0) &&
         ((rightType & DATATYPE_SET_FLAG) != 0))
      {
         throw new IllegalArgumentException("type comparison unsupported: " +
            left.getClass().getName() + " " + right.getClass().getName());
      }

      try {
         left = PSDataConverter.convert(left, bestType);
      } catch (Exception e) {
         throw new IllegalArgumentException("unsupported conversion: " +
            PSDataConverter.getTypeString(leftType) + " " +
            PSDataConverter.getTypeString(bestType) + " " + left);
      }

      try {
         right = PSDataConverter.convert(right, bestType);
      } catch (Exception e) {
         throw new IllegalArgumentException("unsupported conversion: " +
            PSDataConverter.getTypeString(leftType) + " " +
            PSDataConverter.getTypeString(bestType) + " " + right);
      }

      File f = null;

      if (leftType == DATATYPE_FILE)
         f = (File) left;

      if (rightType == DATATYPE_FILE)
         f = (File) right;

      switch (bestType)
      {
         case DATATYPE_NUMERIC:
         case DATATYPE_LONG:
         case DATATYPE_INT:
         case DATATYPE_DOUBLE:
            java.lang.Comparable compLeft = (java.lang.Comparable) left;
            java.lang.Comparable compRight = (java.lang.Comparable) right;
            return compLeft.compareTo(compRight);
            //compareNumeric(left, right);

         case DATATYPE_DATE:
            Date leftDate = (Date)left;
            Date rightDate = (Date)right;
            return leftDate.compareTo(rightDate);

         case DATATYPE_TEXT:
            String leftString = (String)left;
            String rightString = (String)right;
            return leftString.compareTo(rightString);

         case DATATYPE_NULL:
         case DATATYPE_NULLTEXT:
            if (f != null)
            {
               if (leftType == DATATYPE_FILE)
                  return 1;
               else if (rightType == DATATYPE_FILE)
                  return -1;
            }
            else
               return 0;   // if they're both NULL, they're obviously equal

         // case DATATYPE_BINARY:
         // case DATATYPE_UNKNOWN:
         // case DATATYPE_NUMERICSET:
         // case DATATYPE_DATESET:
         // case DATATYPE_TEXTSET:
         // case DATATYPE_BINARYSET:
         default:
            Object[] args = { left.getClass().getName(),
               right.getClass().getName() };
            throw new IllegalArgumentException("unsupported comarison: " +
               left.getClass().getName() +  " " + right.getClass().getName() );
      }
   }

   /**
    * Try to parse a given string to a date.  See {@link
    * PSDataTypeConverter#parseStringToDate(String)} for more info.
    *
    * @param myText a given string to be parsed.  May not be <code>null</code>.
    *
    * @return a valid Date object.
    *
    * @throws ParseException if myText is not able to be parsed
    *
    * @deprecated Use {@link
    * com.percussion.util.PSDataTypeConverter#parseStringToDate(String)}
    * instead.
    */
   public static Date parseStringToDate(String myText)
      throws ParseException
   {
      if (myText == null)
         throw new IllegalArgumentException("myText may not be null");

      Date result = PSDataTypeConverter.parseStringToDate(myText);
      if (result == null)
          throw new ParseException("Text is not in date pattern", 0);

      return result;
   }

   public static final int DATATYPE_NUMERIC            = 0x0001;
   public static final int DATATYPE_DATE               = 0x0002;
   public static final int DATATYPE_TEXT               = 0x0004;
   public static final int DATATYPE_BINARY             = 0x0008;
   public static final int DATATYPE_INT                = 0x0010;
   public static final int DATATYPE_LONG               = 0x0020;
   public static final int DATATYPE_DOUBLE             = 0x0040;
   public static final int DATATYPE_FILE               = 0x0080;
   public static final int DATATYPE_NULL               = 0x1000;
   public static final int DATATYPE_UNKNOWN            = 0x2000;
   public static final int DATATYPE_SET_FLAG           = 0x8000;
   public static final int DATATYPE_NULLTEXT           = DATATYPE_TEXT | DATATYPE_NULL;
   public static final int DATATYPE_NUMERICSET         = DATATYPE_NUMERIC | DATATYPE_SET_FLAG;
   public static final int DATATYPE_DATESET            = DATATYPE_DATE | DATATYPE_SET_FLAG;
   public static final int DATATYPE_TEXTSET            = DATATYPE_TEXT | DATATYPE_SET_FLAG;
   public static final int DATATYPE_BINARYSET          = DATATYPE_BINARY | DATATYPE_SET_FLAG;

   private static java.text.DateFormat m_dateFormat = null;

}
