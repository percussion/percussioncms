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

import java.security.SecureRandom;
import java.util.Random;
import java.util.Vector;

import junit.framework.TestCase;

import org.apache.commons.lang.RandomStringUtils;

/**
 * Unit tests for the PSResultSet class
 *
 */
public class PSResultSetTest extends TestCase
{
   /**
    * Set the result set's data to our data, then iterate through the
    * result set and test values against our data
    */
   public void testRowValuesWithColumnIndices() throws java.sql.SQLException
   {
      PSResultSet result = new PSResultSet();
      
      // set the data in the result set
      result.setResultData(m_columns, m_columnNames );

      String theString;
      long theLong;
      double theDouble;

      int i = 0;

      assertTrue(result.isBeforeFirst());

      // now iterate through it and check the getXXX methods against our results
      while (result.next())
      {
         theLong = result.getLong(1);
         theString = result.getString(2);
         theDouble = result.getDouble(3);
         assertEquals(new Long(theLong), m_columns[0].elementAt(i));
         assertEquals(theString, m_columns[1].elementAt(i));
         assertEquals(new Double(theDouble), m_columns[2].elementAt(i));
         i++;
      }

      assertTrue(result.isAfterLast());
      assertTrue(i == NUM_ROWS);
   }

   /**
    * Set the result set's data to our data, then iterate through the
    * result set and test values against our data
    */
   public void testRowValuesWithColumnNames() throws java.sql.SQLException
   {
      PSResultSet result = new PSResultSet();
      
      // set the data in the result set
      result.setResultData(m_columns, m_columnNames );

      String theString;
      long theLong;
      double theDouble;

      int i = 0;

      assertTrue(result.isBeforeFirst());

      // now iterate through it and check the getXXX methods against our results
      while (result.next())
      {
         theLong = result.getLong("a");
         theString = result.getString("b");
         theDouble = result.getDouble("c");
         assertEquals(new Long(theLong), m_columns[0].elementAt(i));
         assertEquals(theString, m_columns[1].elementAt(i));
         assertEquals(new Double(theDouble), m_columns[2].elementAt(i));
         i++;
      }

      assertTrue(result.isAfterLast());
      assertTrue(i == NUM_ROWS);
   }

   @SuppressWarnings("unchecked")
   public void setUp()
   {
      m_columns = new Vector[3];
      
      m_columns[0] = new Vector(NUM_ROWS);
      m_columns[1] = new Vector(NUM_ROWS);
      m_columns[2] = new Vector(NUM_ROWS);

      // fill the first column with random longs
      // fill the second column with random strings
      // fill the third column with random doubles
      SecureRandom rand = new SecureRandom();
      for (int i = 0; i < NUM_ROWS; i++)
      {
         m_columns[0].addElement(new Long(rand.nextLong()));
         m_columns[1].addElement(RandomStringUtils.randomAscii(99));
         m_columns[2].addElement(new Double(rand.nextDouble()));
      }

      m_columnNames = new java.util.HashMap<String, Integer>();
      m_columnNames.put("a", new Integer(1));
      m_columnNames.put("b", new Integer(2));
      m_columnNames.put("c", new Integer(3));
   }

   private Vector[] m_columns;

   private static final int NUM_ROWS = 100;

   java.util.HashMap<String, Integer> m_columnNames;
}
