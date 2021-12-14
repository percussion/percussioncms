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

import java.sql.Types;

/**
 * This class encapsulates each condition of the WHERE clause of the
 * SELECT statement.
 * For usage, see the main method of this class.
 */
public class PSJdbcSelectFilter
{
   /**
    * protected constructor to be used by derived classes such as
    * <code>PSJdbcFilterContainer</code>
    */
   protected PSJdbcSelectFilter()
   {
   }

   /**
    * Constructor
    *
    * @param filterString the conditional clause encapsulated by this object,
    * may not be <code>null</code> or empty, should not contain the
    * "WHERE" keyword.
    *
    * @throws IllegalArgumentException if <code>filterString</code> is invalid
    */
   public PSJdbcSelectFilter(String filterString)
   {
      if ((filterString == null) || (filterString.trim().length() < 1))
         throw new IllegalArgumentException(
            "filterString may not be null or empty");
      m_filter = filterString;
   }

   /**
    * Constructor
    * @param colName the name of the column, may not be <code>null</code> or
    * empty
    * @param op the conditional operator, should be a valid conditional
    * operator. use the static constants defined in this class for this
    * parameter
    * @param colValue the value of the column, may not be <code>null</code>,
    * may be empty
    * @param colDataType the jdbc data type of the column
    *
    * @throw IllegalArgumentException if colName or colValue is <code>null</code>
    * or colName is empty or op is an invalid conditional operator
    */
   public PSJdbcSelectFilter(String colName, int op,
      String colValue, int colDataType)
   {
      if ((colName == null) || (colName.trim().length() == 0))
         throw new IllegalArgumentException("colName may not be null or empty");

      if (colValue == null)
         throw new IllegalArgumentException("colValue may not be null");

      if (!isValidOp(op))
         throw new IllegalArgumentException("invalid conditional operator");

      m_filter = colName + getStringOp(op);

      if (!((op == IS_NOT_NULL) || (op == IS_NULL)))
      {
         switch (colDataType)
         {
            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.BIT:
            case Types.TINYINT:
            case Types.BIGINT:
            case Types.DOUBLE:
            case Types.NUMERIC:
            case Types.DECIMAL:
            case Types.FLOAT:
            case Types.REAL:
               m_filter += colValue;
               break;

            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
            case Types.BLOB:
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.CLOB:
               m_filter += "\'" + colValue + "\'";
               break;

            default:
               m_filter += "\'" + colValue + "\'";
               break;
         }
      }
   }

   /**
    * Returns the conditional clause encapsulated by this object, never
    * <code>null</code> or empty. the returned string does not contain the
    * "WHERE" keyword.
    * @return
    */
   public String toString()
   {
      return m_filter;
   }

   /**
    * Returns <code>true</code> if op is a valid conditional operator, else
    * returns <code>false</code>
    * @param op the conditional operator
    * @return <code>true</code> if op is a valid conditional operator, else
    * returns <code>false</code>
    */
   protected boolean isValidOp(int op)
   {
      switch(op)
      {
         case EQUALS:
         case NOT_EQUALS:
         case LESS_THAN:
         case LESS_THAN_OR_EQUALS:
         case GREATER_THAN:
         case GREATER_THAN_OR_EQUALS:
         case IS_NULL:
         case IS_NOT_NULL:
         case BETWEEN:
         case NOT_BETWEEN:
         case IN:
         case NOT_IN:
         case LIKE:
         case NOT_LIKE:
            return true;

         default:
            return false;
      }
   }

   /**
    * Returns the conditional operator specified by parameter op in string form
    * @param op a valid conditional operator
    * @return the string form of the conditional operator specified by
    * parameter op, may return <code>null</code> if op is an invalid
    * conditional operator
    */
   protected String getStringOp(int op)
   {
      switch(op)
      {
         case EQUALS:
            return STR_EQUALS;

         case NOT_EQUALS:
            return STR_NOT_EQUALS;

         case LESS_THAN:
            return STR_LESS_THAN;

         case LESS_THAN_OR_EQUALS:
            return STR_LESS_THAN_OR_EQUALS;

         case GREATER_THAN:
            return STR_GREATER_THAN;

         case GREATER_THAN_OR_EQUALS:
            return STR_GREATER_THAN_OR_EQUALS;

         case IS_NULL:
            return STR_IS_NULL;

         case IS_NOT_NULL:
            return STR_IS_NOT_NULL;

         case BETWEEN:
            return STR_BETWEEN;

         case NOT_BETWEEN:
            return STR_NOT_BETWEEN;

         case IN:
            return STR_IN;

         case NOT_IN:
            return STR_NOT_IN;

         case LIKE:
            return STR_LIKE;

         case NOT_LIKE:
            return STR_NOT_LIKE;

         default:
            return null;
      }
   }

   /**
    * stores the string representation of condition encapsulated
    * by this object, never <code>null</code>, may be empty
    */
   protected String m_filter = "";

   /**
    * Constant for adding the WHERE string to the select filter.
    * This class does not add the WHERE string to the filter implicitly. User
    * of this class has to concat the WHERE string.
    */
   public static final String WHERE = " WHERE ";

   /**
    * Constants to be used in the public constructor.
    */

   /**
    * Constant for use in the public constructor for creating a filter where
    * column value equals a specified value.
    */
   public static final int EQUALS = 0;

   /**
    * Constant for use in the public constructor for creating a filter where
    * column value does not equal a specified value.
    */
   public static final int NOT_EQUALS = 1;

   /**
    * Constant for use in the public constructor for creating a filter where
    * column value is less than a specified value.
    */
   public static final int LESS_THAN = 2;

   /**
    * Constant for use in the public constructor for creating a filter where
    * column value is less than or equal to a specified value.
    */
   public static final int LESS_THAN_OR_EQUALS = 3;

   /**
    * Constant for use in the public constructor for creating a filter where
    * column value is greater than a specified value.
    */
   public static final int GREATER_THAN = 4;

   /**
    * Constant for use in the public constructor for creating a filter where
    * column value is greater than or equal to a specified value.
    */
   public static final int GREATER_THAN_OR_EQUALS = 5;

   /**
    * Constant for use in the public constructor for creating a filter where
    * column value IS NULL.
    */
   public static final int IS_NULL = 6;

   /**
    * Constant for use in the public constructor for creating a filter where
    * column value IS NOT NULL.
    */
   public static final int IS_NOT_NULL = 7;

   /**
    * Constant for use in the public constructor for creating a filter where
    * column value lies between 2 other specified values.
    */
   public static final int BETWEEN = 8;

   /**
    * Constant for use in the public constructor for creating a filter where
    * column value does not lie between 2 other specified values.
    */
   public static final int NOT_BETWEEN = 9;

   /**
    * Constant for use in the public constructor for creating a filter where
    * column value matches one of the specified values.
    */
   public static final int IN = 10;

   /**
    * Constant for use in the public constructor for creating a filter where
    * column value does not match any of the specified values.
    */
   public static final int NOT_IN = 11;

   /**
    * Constant for use in the public constructor for creating a filter where
    * column value is similar to a specified value.
    */
   public static final int LIKE = 12;

   /**
    * Constant for use in the public constructor for creating a filter where
    * column value is not similar to a specified value.
    */
   public static final int NOT_LIKE = 13;

   /**
    * String equivalents of the constants defined above
    */
   protected static final String STR_EQUALS = " = ";
   protected static final String STR_NOT_EQUALS = " != ";
   protected static final String STR_LESS_THAN = " < ";
   protected static final String STR_LESS_THAN_OR_EQUALS = " <= ";
   protected static final String STR_GREATER_THAN = " > ";
   protected static final String STR_GREATER_THAN_OR_EQUALS = " >= ";

   protected static final String STR_IS_NULL = " IS NULL ";
   protected static final String STR_IS_NOT_NULL = " IS NOT NULL ";
   protected static final String STR_BETWEEN = " BETWEEN ";
   protected static final String STR_NOT_BETWEEN = " NOT BETWEEN ";
   protected static final String STR_IN = " IN ";
   protected static final String STR_NOT_IN = " NOT IN ";
   protected static final String STR_LIKE = " LIKE ";
   protected static final String STR_NOT_LIKE = " NOT LIKE ";

   protected static final String STR_AND = " AND ";
   protected static final String STR_OR = " OR ";

   /**
    * Main method.
    * @param args the arguments for testing this class, not used currently.
    * Running this class will produce the following output.
    *
    * Where clause :
    *  WHERE Name NOT LIKE 'a%'
    * Where clause :
    *  WHERE ((ID > 10) AND (Name LIKE 'a%')) OR ((AGE < 50) AND (Name IS NOT NULL ))
    */
   public static void main(String[] args)
   {
      PSJdbcSelectFilter filter = new PSJdbcSelectFilter("Name",
         PSJdbcSelectFilter.NOT_LIKE, "a%", Types.VARCHAR);
      System.out.println("Where clause : ");
      System.out.println(PSJdbcSelectFilter.WHERE + filter);
      System.out.println();

      PSJdbcFilterContainer container1 = new PSJdbcFilterContainer();
      container1.doAND(new PSJdbcSelectFilter("ID",
         PSJdbcSelectFilter.GREATER_THAN, "10", Types.INTEGER));
      container1.doAND(new PSJdbcSelectFilter("Name",
         PSJdbcSelectFilter.LIKE, "a%", Types.VARCHAR));

      PSJdbcFilterContainer container2 = new PSJdbcFilterContainer();
      container2.doAND(new PSJdbcSelectFilter("AGE",
         PSJdbcSelectFilter.LESS_THAN, "30", Types.INTEGER));
      container2.doAND(new PSJdbcSelectFilter("Name",
         PSJdbcSelectFilter.IS_NOT_NULL, "", Types.VARCHAR));

      PSJdbcFilterContainer container3 = new PSJdbcFilterContainer();
      container3.doAND(container1);
      container3.doOR(container2);

      System.out.println("Where clause : ");
      System.out.println(PSJdbcSelectFilter.WHERE + container3);
   }
}

