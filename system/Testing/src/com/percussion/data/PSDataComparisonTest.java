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

import java.math.BigDecimal;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class PSDataComparisonTest extends TestCase
{
   public PSDataComparisonTest(String str)
   {
      super(str);
   }

   public void testNumericComparisonsNotEqual()
   {
      Long lSmall = new Long(10);
      Long lBig = new Long(20);
      Integer iSmall = new Integer(10);
      Integer iBig = new Integer(20);
      String sSmall = new String("10");
      String sBig = new String("20");
      Double dSmall = new Double(10);
      Double dBig = new Double(20);
      BigDecimal bdSmall = new BigDecimal(10);
      BigDecimal bdBig = new BigDecimal(20);

      int ret;

      try {
         // Test left greater than
         ret = PSDataConverter.compare(iBig, iSmall);
         assertTrue("Error!  Expected greater than got: "+ret,(ret == 1));
         // Test left less than
         ret = PSDataConverter.compare(iSmall, iBig);
         assertTrue("Error!  Expected less than got: "+ret,(ret == -1));

         // Test Double/Int mix, it uses Double logic - both sides
         ret = PSDataConverter.compare(dBig, iSmall);
         assertTrue("D/I Error!  Expected greater than got: "+ret,(ret == 1));
         ret = PSDataConverter.compare(dSmall, iBig);
         assertTrue("D/I Error!  Expected less than got: "+ret,(ret == -1));
         ret = PSDataConverter.compare(iBig, dSmall);
         assertTrue("I/D Error!  Expected greater than got: "+ret,(ret == 1));
         ret = PSDataConverter.compare(iSmall, dBig);
         assertTrue("I/D Error!  Expected less than got: "+ret,(ret == -1));

         // Test String/number mix, w/string on both sides
         ret = PSDataConverter.compare(sBig, iSmall);
         assertTrue("S/I Error!  Expected greater than got: "+ret,(ret == 1));
         ret = PSDataConverter.compare(sSmall, iBig);
         assertTrue("S/I Error!  Expected less than got: "+ret,(ret == -1));
         ret = PSDataConverter.compare(iBig, sSmall);
         assertTrue("I/S Error!  Expected greater than got: "+ret,(ret == 1));
         ret = PSDataConverter.compare(iSmall, sBig);
         assertTrue("I/S Error!  Expected less than got: "+ret,(ret == -1));

         // Test Double/Long mix, it uses BigDecimal logic - both sides
         ret = PSDataConverter.compare(dBig, lSmall);
         assertTrue("D/L Error!  Expected greater than got: "+ret,(ret == 1));
         ret = PSDataConverter.compare(dSmall, lBig);
         assertTrue("D/L Error!  Expected less than got: "+ret,(ret == -1));
         ret = PSDataConverter.compare(lBig, dSmall);
         assertTrue("L/D Error!  Expected greater than got: "+ret,(ret == 1));
         ret = PSDataConverter.compare(lSmall, dBig);
         assertTrue("L/D Error!  Expected less than got: "+ret,(ret == -1));

         // Test BigDecimal/Long mix, it uses BigDecimal logic without
         // intermediary string convertsions
         ret = PSDataConverter.compare(bdBig, lSmall);
         assertTrue("BD/L Error!  Expected greater than got: "+ret,(ret == 1));
         ret = PSDataConverter.compare(bdSmall, lBig);
         assertTrue("BD/L Error!  Expected less than got: "+ret,(ret == -1));
         ret = PSDataConverter.compare(lBig, bdSmall);
         assertTrue("L/BD Error!  Expected greater than got: "+ret,(ret == 1));
         ret = PSDataConverter.compare(lSmall, bdBig);
         assertTrue("L/BD Error!  Expected less than got: "+ret,(ret == -1));


      } catch (Exception e) {
         assertTrue("Unexpected exception occurred: "+e.toString(), false);
      }

   }

   public void testNumericComparisons()
   {
      int ret;

      Integer i1 = new Integer(1);
      Integer i2 = new Integer(0);
      Long l1 = new Long(1);
      Long l2 = new Long(0);

      Double  d1 = new Double(1);
      Double  d2 = new Double(0);
      Double  d3 = new Double(.1);

      String  s1 = new String("1");
      String  s2 = new String("0");
      String  s3 = new String(".1");
      String  s4 = new String("1.0");

      String  invalidString = "invalidNumberValue";

      try {
         ret = PSDataConverter.compare(i1, s1);
         if (ret != 0)
            assertTrue("i1/s1 Integer/String comparison failed, expected equal: got " + ret, false);

         ret = PSDataConverter.compare(i1, s4);
         if (ret != 0)
            assertTrue("i1/s4 Integer/String comparison failed, expected equal: got " + ret, false);

         ret = PSDataConverter.compare(i2, s2);
         if (ret != 0)
            assertTrue("i2/s2 Integer/String comparison failed, expected equal: got" + ret, false);

         ret = PSDataConverter.compare(l1, s1);
         if (ret != 0)
            assertTrue("l1/s1 Long/String comparison failed, expected equal: got " + ret, false);

         ret = PSDataConverter.compare(l2, s2);
         if (ret != 0)
            assertTrue("l2/s2 Long/String comparison failed, expected equal: got" + ret, false);

         ret = PSDataConverter.compare(d1, s1);
         if (ret != 0)
            assertTrue("d1/s1 Double/String comparison failed, expected equal: got" + ret, false);

         ret = PSDataConverter.compare(d2, s2);
         if (ret != 0)
            assertTrue("d2/s2 Double/String comparison failed, expected equal: got" + ret, false);

         ret = PSDataConverter.compare(d3, s3);
         if (ret != 0)
            assertTrue("d3/s3 Double/String comparison failed, expected equal: got " + ret, false);

         ret = PSDataConverter.compare(d1, i1);
         if (ret != 0)
            assertTrue("d1/i1 Double/Integer comparison failed, expected equal: got " + ret, false);

         ret = PSDataConverter.compare(d2, i2);
         if (ret != 0)
            assertTrue("d2/d2 Double/Integer comparison failed, expected equal: got" + ret, false);

         ret = PSDataConverter.compare(d1, l1);
         if (ret != 0)
            assertTrue("d1/l1 Double/Long comparison failed, expected equal: got" + ret, false);

         ret = PSDataConverter.compare(d2, l2);
         if (ret != 0)
            assertTrue("d2/l2 Double/Long comparison failed, expected equal: got" + ret, false);

         try {
            ret = PSDataConverter.compare(i1, invalidString);
            assertTrue("Expected Exception did not occur (i1/l2)", false);
         } catch (IllegalArgumentException e) {
            System.err.println("i1/invalidString Expected exception: " + e.getLocalizedMessage());
         }

         try {
            ret = PSDataConverter.compare(l1, invalidString);
            assertTrue("Expected Exception did not occur (l1/invalidString)", false);
         } catch (IllegalArgumentException e) {
            System.err.println("l1/invalidString Expected exception: " + e.getLocalizedMessage());
         }

         try {
            ret = PSDataConverter.compare(d2, invalidString);
            assertTrue("Expected Exception did not occur (d2/invalidString)", false);
         } catch (IllegalArgumentException e) {
            System.err.println("d2/invalidString Expected exception: " + e.getLocalizedMessage());
         }

      } catch (Exception e) {
         assertTrue("Unexpected exception occurred: "+e.toString(), false);
      }
   }

   // Think of this like TestMain or something...
   // intead of exec'ing the suite, you just register the test methods,
   // however.
   public static Test suite()
   {
      TestSuite suite = new TestSuite();

      suite.addTest(new PSDataComparisonTest("testNumericComparisons"));
      suite.addTest(new PSDataComparisonTest("testNumericComparisonsNotEqual"));

      return suite;
   }

}
