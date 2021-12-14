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

import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSBackEndJoin;
import com.percussion.design.objectstore.PSBackEndTable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.security.SecureRandom;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Unit tests for the query joiner class. 
 *
 */
public class PSQueryJoinerTest extends TestCase
{
   public PSQueryJoinerTest(String name)
   {
      super(name);
      ms_rand = new SecureRandom();
   }

   public void testNormalJoin() throws Exception
   {
      int[] dataTypes =
      {
         // These types cannot be compared
         //   Boolean doesn't have a concept of < or >
         //   Byte[] doesn't implement Comparable, though we can fix this
         // Types.BIT ,   
         // Types.BINARY,
         // Types.VARBINARY ,
         // Types.LONGVARBINARY ,

         Types.TINYINT ,
         Types.SMALLINT,
         Types.INTEGER ,
         Types.BIGINT ,
         Types.FLOAT ,
         Types.REAL ,
         Types.DOUBLE ,
         Types.NUMERIC ,
         Types.DECIMAL,
         Types.CHAR,
         Types.VARCHAR ,
         Types.LONGVARCHAR ,
         Types.DATE ,
         Types.TIME ,
         Types.TIMESTAMP ,
         Types.NULL
      };

      for (int i = 0; i < dataTypes.length; i++)
      {
         testNormalJoin(dataTypes[i]);
      }
   }

   public void testNormalJoin(int dataType) throws Exception
   {
      ArrayList leftColumn = new ArrayList();
      ArrayList rightColumn = new ArrayList();

      final int numRows = 120;
      /* based on this algorithm, we will end up with the following result
       * sets:
       *
       *      Left            Right
       *    ------------   ---------------
       *      0 x 30         0 x 40
       *      1 x 30         1 x 40
       *      2 x 30         2 x 40
       *      3 x 30
       *
       * This will result in 1200 0's, 1200 1's and 1200 2's, thus 3600
       * matches are expected.
       *
       * However, for bit data, we end up with:
       *
       *      Left            Right
       *    ------------   ---------------
       *      true  x 30      true x 40
       *      false x 90      false x 80
       *
       * This will result in 1200 true's and 7200 false's, thus 8400
       * matches are expected.
       *
       * The final case is NULL, which is 120 x 120 => 14400
       */
      final int matchingRows;
      if (dataType == Types.BIT)
         matchingRows = 8400;
      else if (dataType == Types.NULL)
         matchingRows = 14400;
      else
         matchingRows = 3600;
      java.util.Date now = new java.util.Date();
      Object leftData, rightData;
      for (int i = 0; i < numRows; i++)
      {
         switch (dataType) {
            // this is not comparable, can't be supported!
            // case Types.BIT:
            //    leftData = new Boolean(((i%4) == 0));
            //    rightData = new Boolean(((i%3) == 0));
            //    break;

            case Types.TINYINT:
               leftData = new Byte((byte)(i%4));
               rightData = new Byte((byte)(i%3));
               break;
               
            case Types.SMALLINT:
               leftData = new Short((short)(i%4));
               rightData = new Short((short)(i%3));
               break;

            case Types.INTEGER:
               leftData = new Integer(i%4);
               rightData = new Integer(i%3);
               break;

            case Types.BIGINT:
               leftData = new Long(i%4);
               rightData = new Long(i%3);
               break;

            case Types.FLOAT:
            case Types.REAL:
               leftData = new Float(i%4);
               rightData = new Float(i%3);
               break;

            case Types.DOUBLE:
               leftData = new Double(i%4);
               rightData = new Double(i%3);
               break;

            case Types.NUMERIC:
            case Types.DECIMAL:
               leftData = new java.math.BigDecimal(i%4);
               rightData = new java.math.BigDecimal(i%3);
               break;

            case Types.CHAR:
               leftData = String.valueOf(i%4) + "    ";
               rightData = String.valueOf(i%3) + "    ";
               break;

            case Types.VARCHAR:
               leftData = String.valueOf(i%4);
               rightData = String.valueOf(i%3);
               break;

            case Types.LONGVARCHAR:
               leftData = String.valueOf(i%4);
               rightData = String.valueOf(i%3);
               break;

            case Types.DATE:
               leftData = new java.sql.Date(now.getTime() + ((i%4) * 3600000));
               rightData = new java.sql.Date(now.getTime() + ((i%3) * 3600000));
               break;

            case Types.TIME:
               leftData = new java.sql.Time(now.getTime() + ((i%4) * 3600000));
               rightData = new java.sql.Time(now.getTime() + ((i%3) * 3600000));
               break;

            case Types.TIMESTAMP:
               leftData = new java.sql.Timestamp(now.getTime() + ((i%4) * 3600000));
               rightData = new java.sql.Timestamp(now.getTime() + ((i%3) * 3600000));
               break;

            // these are not comparable, can't be supported!
            // case Types.BINARY:
            // case Types.VARBINARY:
            // case Types.LONGVARBINARY:
            //    leftData = new byte[i%4];
            //    rightData = new byte[i%3];
            //    break;

            // case Types.NULL:
            default:   // NULL or don't know, so treat it as null
               dataType = Types.NULL;
               leftData = null;
               rightData = null;
               break;
         }

         leftColumn.add(leftData);
         rightColumn.add(rightData);
      }

      if (dataType != Types.NULL)
      {
         Collections.sort(leftColumn);
         Collections.sort(rightColumn);
      }

      switch (dataType) {
         case Types.BIT:
            System.err.println("Inner joining with BIT");
            break;

         case Types.TINYINT:
            System.err.println("Inner joining with TINYINT");
            break;
            
         case Types.SMALLINT:
            System.err.println("Inner joining with SMALLINT");
            break;

         case Types.INTEGER:
            System.err.println("Inner joining with INTEGER");
            break;

         case Types.BIGINT:
            System.err.println("Inner joining with BIGINT");
            break;

         case Types.FLOAT:
            System.err.println("Inner joining with FLOAT");
            break;

         case Types.REAL:
            System.err.println("Inner joining with REAL");
            break;

         case Types.DOUBLE:
            System.err.println("Inner joining with DOUBLE");
            break;

         case Types.NUMERIC:
            System.err.println("Inner joining with NUMERIC");
            break;

         case Types.DECIMAL:
            System.err.println("Inner joining with DECIMAL");
            break;

         case Types.CHAR:
            System.err.println("Inner joining with CHAR");
            break;

         case Types.VARCHAR:
            System.err.println("Inner joining with VARCHAR");
            break;

         case Types.LONGVARCHAR:
            System.err.println("Inner joining with LONGVARCHAR");
            break;

         case Types.DATE:
            System.err.println("Inner joining with DATE");
            break;

         case Types.TIME:
            System.err.println("Inner joining with TIME");
            break;

         case Types.TIMESTAMP:
            System.err.println("Inner joining with TIMESTAMP");
            break;

         case Types.BINARY:
            System.err.println("Inner joining with BINARY");
            break;

         case Types.VARBINARY:
            System.err.println("Inner joining with VARBINARY");
            break;

         case Types.LONGVARBINARY:
            System.err.println("Inner joining with LONGVARBINARY");
            break;

         case Types.NULL:
            System.err.println("Inner joining with NULL");
            break;

         default:
            System.err.println("Inner joining with " + dataType);
            break;
      }

      PSResultSetMetaData leftSchema = new PSResultSetMetaData();
      leftSchema.addColumnMetaData(new PSResultSetColumnMetaData("leftCol", Types.INTEGER, 1));

      PSResultSetMetaData rightSchema = new PSResultSetMetaData();
      rightSchema.addColumnMetaData(new PSResultSetColumnMetaData("rightCol", Types.INTEGER, 1));

      PSBackEndTable leftTab   = new PSBackEndTable("leftTable");
      PSBackEndTable rightTab   = new PSBackEndTable("rightTable");

      PSBackEndColumn leftCol      = new PSBackEndColumn(
         leftTab, "leftCol");

      PSBackEndColumn rightCol   = new PSBackEndColumn(
         rightTab, "rightCol");

      PSBackEndJoin join = new PSBackEndJoin(leftCol, rightCol);

      PSQueryJoiner joiner = new PSSortedResultJoiner(null, join, new String[] { "leftCol"},
         null, new String[] { "rightCol" }, null, 100);

      joiner.closeInputResultsAfterJoin(false);

      PSExecutionData data = new PSExecutionData(null, null, null);

      HashMap leftNames = new HashMap();
      leftNames.put("leftCol", new Integer(1));

      HashMap rightNames = new HashMap();
      rightNames.put("rightCol", new Integer(1));

      PSResultSet leftRs = new PSResultSet(new ArrayList[] { leftColumn} , leftNames, leftSchema);
      PSResultSet rightRs = new PSResultSet(new ArrayList[] { rightColumn}, rightNames, rightSchema);
      data.getResultSetStack().push(leftRs);
      data.getResultSetStack().push(rightRs);

      joiner.execute(data);

      PSResultSet joinedRs = (PSResultSet)data.getResultSetStack().pop();
      int rowCount = 0;
      while (joinedRs.next())
      {
         rowCount++;
      }

      try {
         assertEquals(matchingRows, joinedRs.getNumRows());
      } finally {
         leftRs.close();
         rightRs.close();
         joinedRs.close();
      }
   }

   /**
    * Test the joining on 20 random result sets.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/8/1
    * 
    * 
    * @throws   Exception
    * 
    */
   public void smokeTestJoins() throws Exception
   {
      int[] joinTypes = {   PSBackEndJoin.BEJ_TYPE_INNER,
                           PSBackEndJoin.BEJ_TYPE_LEFT_OUTER,
                           PSBackEndJoin.BEJ_TYPE_RIGHT_OUTER,
                           PSBackEndJoin.BEJ_TYPE_FULL_OUTER };
      for (int j = 0; j < joinTypes.length; j++)
      {
         for (int i = 0; i < 20; i++)
         {
            smokeTestJoin(joinTypes[j]);
         }

         System.gc();
      }
   }

   /**
    * Test the joining on a random result set, making sure nothing blows
    * up and that the data has the correct number of rows.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/8/1
    * 
    * 
    * @throws   Exception
    * 
    */
   public void smokeTestJoin(int joinType) throws Exception
   {
      PSResultSetMetaData schema = new PSResultSetMetaData();
      String[] columnNames = new String[]
      {
         // 1      2         3         4         5         6         7
         "colA",   "colB",   "colC",   "colD",   "colE",   "colF",   "colG"
      };
      int[] columnTypes = new int[]
      {
         // 1            2               3               4
         Types.INTEGER,   Types.VARCHAR,   Types.DOUBLE,   Types.FLOAT,
         // 5            6               7
         Types.VARCHAR,   Types.DOUBLE,   Types.INTEGER
      };

      for (int i = 0; i < columnNames.length; i++)
      {
         schema.addColumnMetaData(new PSResultSetColumnMetaData(
            columnNames[i], columnTypes[i], 1));
      }

      int leftCard = ms_rand.nextInt(1000);
      int rightCard = ms_rand.nextInt(1000);

      PSResultSet leftRs   = createResultSet(schema, leftCard, true);
      PSResultSet rightRs   = createResultSet(schema, rightCard, true);

      assertEquals(leftRs.getNumRows(), leftCard);
      assertEquals(rightRs.getNumRows(), rightCard);

      PSBackEndTable leftTab   = new PSBackEndTable("leftTable");
      PSBackEndTable rightTab   = new PSBackEndTable("rightTable");

      int whichCol = ms_rand.nextInt(columnNames.length);

      PSBackEndColumn leftCol      = new PSBackEndColumn(
         leftTab, columnNames[whichCol]);

      PSBackEndColumn rightCol   = new PSBackEndColumn(
         rightTab, columnNames[whichCol]);

      // left outer join
        // debug messages to System.err have been commented out as they are
        // not useful for routine use of this test
      PSBackEndJoin join = new PSBackEndJoin(leftCol, rightCol);
       if (joinType == PSBackEndJoin.BEJ_TYPE_LEFT_OUTER)
       {
//          System.err.println("Left Outer Joining " + leftCol.toString() +
//             " with " + rightCol.toString());
          join.setLeftOuterJoin();
       }
       else if (joinType == PSBackEndJoin.BEJ_TYPE_RIGHT_OUTER)
       {
//          System.err.println("Right Outer Joining " + leftCol.toString() +
//             " with " + rightCol.toString());
          join.setRightOuterJoin();
       }
       else if (joinType == PSBackEndJoin.BEJ_TYPE_FULL_OUTER)
       {
//          System.err.println("Full Outer Joining " + leftCol.toString() +
//             " with " + rightCol.toString());
          join.setFullOuterJoin();
       }
       else
       {   // default to inner
//          System.err.println("Inner Joining " + leftCol.toString() +
//             " with " + rightCol.toString());
          join.setInnerJoin();
       }

      PSQueryJoiner joiner = new PSSortedResultJoiner(null, join, columnNames, 
           null, columnNames, null, leftCard*rightCard);

      joiner.closeInputResultsAfterJoin(false);

      PSExecutionData data = new PSExecutionData(null, null, null);

      data.getResultSetStack().push(leftRs);
      data.getResultSetStack().push(rightRs);

      joiner.execute(data);

      PSResultSet joinedRs = (PSResultSet)data.getResultSetStack().pop();

       // debug messages to System.err have been commented out as they are
       // not useful for routine use of this test
       
//       System.err.println("Left cardinality:  " + leftCard);
//       System.err.println("Right cardinality: " + rightCard);
//       System.err.println("Join cardinality:  " + joinedRs.getNumRows());
//       System.err.println("");

//       if (leftCard != joinedRs.getNumRows())
//       {
//          printResultSet(leftRs, new BufferedOutputStream(
//             new FileOutputStream(File.createTempFile("leftRs", ".dat"))));
//          printResultSet(rightRs, new BufferedOutputStream(
//             new FileOutputStream(File.createTempFile("rightRs", ".dat"))));
//          printResultSet(joinedRs, new BufferedOutputStream(
//             new FileOutputStream(File.createTempFile("joinedRs", ".dat"))));
//       }

      try {
         if (joinType == PSBackEndJoin.BEJ_TYPE_LEFT_OUTER)
         {
            assertTrue(
               "Left outer must be at least as large as left side",
               leftCard <= joinedRs.getNumRows());
         }
         else if (joinType == PSBackEndJoin.BEJ_TYPE_RIGHT_OUTER)
         {
            assertTrue(
               "Right outer must be at least as large as right side",
               rightCard <= joinedRs.getNumRows());
         }
         else if (joinType == PSBackEndJoin.BEJ_TYPE_FULL_OUTER)
         {
            int expected = Math.max(leftCard, rightCard);
            assertTrue(
               "Full outer must be at least as large as the larger side",
               expected <= joinedRs.getNumRows());
         }
         else
         {   // inner - ? what's the check here?!
         }
      } finally {
         leftRs.close();
         rightRs.close();
         joinedRs.close();
      }
   }

   /**
    * Creates a result set with the given number of rows, that conforms to the given
    * schema. The data will be more or less randomly generated.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/8/1
    * 
    * @param   schema
    * @param   numRows
    * 
    * @return   PSResultSet
    */
   protected static PSResultSet createResultSet(PSResultSetMetaData schema, int numRows, boolean sorted)
      throws SQLException
   {
      ArrayList[] cols = new ArrayList[schema.getColumnCount()];
      HashMap colNames = new HashMap(cols.length);

      for (int i = 1; i <= schema.getColumnCount(); i++) // 1-based
      {
         ArrayList list = new ArrayList(numRows);
         int type = schema.getColumnType(i);
         for (int k = 0; k < numRows; k++)
         {
            list.add(randomObject(type));
         }
         cols[i-1] = list;
         colNames.put(schema.getColumnName(i), new Integer(i));
      }

      if (sorted)
      {
         for (int i = 0; i < cols.length; i++)
         {
            Collections.sort(cols[i]);
         }
      }

      return new PSResultSet(cols, colNames, schema);
   }

   /**
    * Creates a random object of the corresponding SQL type. Not all
    * types are supported; mostly basic types.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/8/1
    * 
    * 
    * @param   javaSqlType
    * 
    * @return   Object
    * 
    * @throws IllegalArgumentException
    * 
    */
   protected static Object randomObject(int javaSqlType)
   {
      switch (javaSqlType)
      {
      case Types.DOUBLE:
         return new Double(ms_rand.nextDouble());
      case Types.FLOAT:
         return new Float(ms_rand.nextFloat());
      case Types.SMALLINT:
         // fall thru
      case Types.INTEGER:
         return new Integer(ms_rand.nextInt());
      case Types.NULL:
         return null;
      case Types.LONGVARCHAR:
         // fall thru
      case Types.VARCHAR:
         return RandomStringUtils.randomAscii(99);
      }
      
      throw new IllegalArgumentException("unsupported type: " + javaSqlType);
   }

   protected void printResultSet(PSResultSet rs, OutputStream out) throws Exception
   {
      OutputStreamWriter outWrite = new OutputStreamWriter(out);

      ResultSetMetaData md = rs.getMetaData();
      int cols = md.getColumnCount();
      
      // print result set header
      for (int i = 1; i <= cols; i++) // 1 based
      {
         String str = md.getColumnName(i) + "\t";
         outWrite.write(str, 0, str.length());
      }
      outWrite.write("\n", 0, 1);


      // print rows
      rs.setBeforeFirst();
      while (rs.next())
      {
         for (int i = 1; i <= cols; i++)
         {
            String str = "" + rs.getObject(i) + "\t";
            outWrite.write(str, 0, str.length());
         }
         outWrite.write("\n", 0, 1);
      }

      out.flush();
      out.close();
   }

   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSQueryJoinerTest("smokeTestJoins"));
      suite.addTest(new PSQueryJoinerTest("testNormalJoin"));
      return suite;
   }


   protected static SecureRandom ms_rand;
}
