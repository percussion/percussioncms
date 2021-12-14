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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *   Unit tests for the PSBijectionMap class
 */

public class PSBijectionMapTest extends TestCase
{
   private class Pairing
   {
      public Pairing(String A, String B) { a = A; b = B; }
      public String a;
      public String b;
   }

   public PSBijectionMapTest(String name)
   {
      super(name);
   }

   public void testPairings()
   {
      PSBijectionMap map = new PSBijectionMap(20);
      for (int i = 0; i < 20; i++)
      {
         map.put(m_pairs[i].a, m_pairs[i].b);
      }
      for (int j = 0; j < 20; j++)
      {
         assertEquals(map.getValue(m_pairs[j].a), m_pairs[j].b);
         assertEquals(map.getKey(m_pairs[j].b), m_pairs[j].a);
      }
   }

   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSBijectionMapTest("testPairings"));
      return suite;
   }

   // set up the data for the test
   public void setUp()
   {
      m_pairs = new Pairing[20];
      m_pairs[0] = new Pairing("0", "zero");
      m_pairs[1] = new Pairing("1", "one");
      m_pairs[2] = new Pairing("2", "two");
      m_pairs[3] = new Pairing("3", "three");
      m_pairs[4] = new Pairing("4", "four");
      m_pairs[5] = new Pairing("5", "five");
      m_pairs[6] = new Pairing("6", "six");
      m_pairs[7] = new Pairing("7", "seven");
      m_pairs[8] = new Pairing("8", "eight");
      m_pairs[9] = new Pairing("9", "nine");
      m_pairs[10] = new Pairing("10", "ten");
      m_pairs[11] = new Pairing("11", "eleven");
      m_pairs[12] = new Pairing("12", "twelve");
      m_pairs[13] = new Pairing("13", "thirteen");
      m_pairs[14] = new Pairing("14", "fourteen");
      m_pairs[15] = new Pairing("15", "fifteen");
      m_pairs[16] = new Pairing("16", "sixteen");
      m_pairs[17] = new Pairing("17", "seventeen");
      m_pairs[18] = new Pairing("18", "eighteen");
      m_pairs[19] = new Pairing("19", "nineteen");
   }

   private Pairing[] m_pairs = null;
}
