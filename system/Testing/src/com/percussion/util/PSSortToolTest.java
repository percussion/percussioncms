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
package com.percussion.util;

import java.util.Comparator;
import java.util.Random;
import java.util.Vector;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *   Unit tests for the PSSortTool class
 */

public class PSSortToolTest extends TestCase
{

   public PSSortToolTest(String name)
   {
      super(name);
   }

   /**
    * Test sorting a large-ish vector of Long objects
    * using QuickSort
    */
   public void testVectorQuickSort()
   {
      class LongComp implements Comparator
      {
         public LongComp() {}

         public int compare(Object left, Object right)
         {
            Long l = (Long)left;
            Long r = (Long)right;

            return l.compareTo(r);
         }
      }

      Vector cloneVec = (Vector)m_randomLongVector.clone();
      long startTime, endTime;
      startTime = System.currentTimeMillis();
      PSSortTool.QuickSort(cloneVec, new LongComp());
      endTime = System.currentTimeMillis();
      System.err.println(
         "QuickSort (vector) of " + cloneVec.size() + " elements took " + 
         (endTime - startTime) + " milliseconds.");
      int compVal = 1;
      for (int i = 0; i < (cloneVec.size() - 1); i++)
      {
         compVal = ((Long)cloneVec.elementAt(i)).compareTo((Long)cloneVec.elementAt(i+1));
         assertTrue("Element at: " + i, compVal <= 0);
      }
   }

   /**
    * Test sorting a large-ish array of Long objects
    * using QuickSort
    */
   public void testArrayQuickSort()
   {
      class LongComp implements Comparator
      {
         public LongComp() {}

         public int compare(Object left, Object right)
         {
            Long l = (Long)left;
            Long r = (Long)right;

            return l.compareTo(r);
         }
      }

      Long cloneVec[] = new Long[m_randomLongVector.size()];
      m_randomLongVector.copyInto(cloneVec);
      long startTime, endTime;
      startTime = System.currentTimeMillis();
      PSSortTool.QuickSort(cloneVec, new LongComp());
      endTime = System.currentTimeMillis();
      System.err.println(
         "QuickSort (array) of " + cloneVec.length + " elements took " + 
         (endTime - startTime) + " milliseconds.");
      int compVal = 1;
      for (int i = 0; i < (cloneVec.length - 1); i++)
      {
         compVal = ((Long)cloneVec[i]).compareTo((Long)cloneVec[i+1]);
         assertTrue("Element at: " + i, compVal <= 0);
      }
   }

   /**
    * Test sorting a large-ish array of Long objects
    * using MergeSort
    */
   public void testArrayMergeSort()
   {
      class LongComp implements Comparator
      {
         public LongComp() {}

         public int compare(Object left, Object right)
         {
            Long l = (Long)left;
            Long r = (Long)right;

            return l.compareTo(r);
         }
      }

      Long cloneVec[] = new Long[m_randomLongVector.size()];
      m_randomLongVector.copyInto(cloneVec);
      long startTime, endTime;
      startTime = System.currentTimeMillis();
      PSSortTool.MergeSort(cloneVec, new LongComp());
      endTime = System.currentTimeMillis();
      System.err.println(
         "MergeSort (array) of " + cloneVec.length + " elements took " + 
         (endTime - startTime) + " milliseconds.");
      int compVal = 1;
      for (int i = 0; i < (cloneVec.length - 1); i++)
      {
         compVal = ((Long)cloneVec[i]).compareTo((Long)cloneVec[i+1]);
         assertTrue("Element at: " + i, compVal <= 0);
      }
   }


   public void testVectorJdkSort()
   {
      class LongComp implements Comparator
      {
         public LongComp() {}

         public int compare(Object left, Object right)
         {
            Long l = (Long)left;
            Long r = (Long)right;

            return l.compareTo(r);
         }
      }

      Vector cloneVec = (Vector)m_randomLongVector.clone();
      long startTime, endTime;
      startTime = System.currentTimeMillis();
      java.util.Collections.sort(cloneVec, new LongComp());
      endTime = System.currentTimeMillis();
      System.err.println(
         "JDK sort (vector) of " + cloneVec.size() + " elements took " + 
         (endTime - startTime) + " milliseconds.");
      int compVal = 1;
      for (int i = 0; i < (cloneVec.size() - 1); i++)
      {
         compVal = ((Long)cloneVec.elementAt(i)).compareTo((Long)cloneVec.elementAt(i+1));
         assertTrue("Element at: " + i, compVal <= 0);
      }
   }

   public void testArrayJdkSort()
   {
      class LongComp implements Comparator
      {
         public LongComp() {}

         public int compare(Object left, Object right)
         {
            Long l = (Long)left;
            Long r = (Long)right;

            return l.compareTo(r);
         }
      }

      Long cloneVecArray[] = new Long[m_randomLongVector.size()];
      m_randomLongVector.copyInto(cloneVecArray);
      long startTime = System.currentTimeMillis(), endTime;
      java.util.Arrays.sort(cloneVecArray, new LongComp());
      endTime = System.currentTimeMillis();
      System.err.println(
         "JDK sort (array) of " + cloneVecArray.length + " elements took " + 
         (endTime - startTime) + " milliseconds.");
      int compVal = 1;
      for (int i = 0; i < (cloneVecArray.length - 1); i++)
      {
         compVal = ((Long)cloneVecArray[i]).compareTo((Long)cloneVecArray[i+1]);
         assertTrue("Element at: " + i, compVal <= 0);
      }
   }

   public void setUp()
   {
      m_randomLongVector = new Vector(VECTOR_SIZE);
      Random rand = new Random();
      for (int i = 0; i < VECTOR_SIZE; i++)
      {
         m_randomLongVector.addElement(new Long(rand.nextLong()));
      }
   }

   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSSortToolTest("testVectorQuickSort"));
      suite.addTest(new PSSortToolTest("testArrayQuickSort"));
      suite.addTest(new PSSortToolTest("testArrayJdkSort"));
      suite.addTest(new PSSortToolTest("testVectorJdkSort"));
      suite.addTest(new PSSortToolTest("testArrayMergeSort"));
      
      return suite;
   }

   private Vector m_randomLongVector;
   private static final int VECTOR_SIZE = 2048;
}
