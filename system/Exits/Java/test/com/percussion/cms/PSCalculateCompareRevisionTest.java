/******************************************************************************
 *
 * [ PSCalculateCompareRevisionTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.cms;

import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestContext;

import junit.framework.TestCase;

/**
 * Test compare revision udf
 * 
 * @author dougrand
 */
public class PSCalculateCompareRevisionTest extends TestCase
{
   private static final String SYS_REVISION2 = "sys_revision2";

   /**
    * Test the algorithm specified in
    * @see PSCalculateCompareRevision
    */
   public void testRevisionCalculation1()
   {
      // cid1, cid2, r1, r2, expected value
      doTest(111, 111, 1, 0, 1);
      doTest(111, 111, 1, 2, 2);
      doTest(111, 111, 2, 0, 1);
      doTest(111, 111, 3, 0, 2);
      doTest(111, 111, 3, 1, 1);
   }
   
   /**
    * Test the algorithm specified in
    * @see PSCalculateCompareRevision
    */
   public void testRevisionCalculation2()
   {
      // cid1, cid2, r1, r2, expected value
      doTest(111, 112, 1, 0, 0);
      doTest(111, 112, 1, 1, 1);
      doTest(111, 112, 1, 2, 2);
   }   

   /**
    * Perform the test for a given set of values
    * 
    * @param cid1
    * @param cid2
    * @param revision1
    * @param revision2
    * @param expectedrevision
    */
   private void doTest(int cid1, int cid2, int revision1, int revision2,
         int expectedrevision) 
   {
      PSRequest req = new PSRequest(null, null, null, null);
      IPSRequestContext ctx = new PSRequestContext(req);
      PSCalculateCompareRevision ccr = new PSCalculateCompareRevision();
      Object arr[] = makeParameters(cid1, cid2, revision1, revision2);
      if (revision2 != 0)
      {
         req.setParameter(SYS_REVISION2, Integer.toString(revision2));
      }
      ccr.preProcessRequest(arr, ctx);
      String result = req.getParameter(SYS_REVISION2);
      Object expected = expectedrevision == 0 ? null : Integer.toString(expectedrevision);
      assertEquals(expected, result);
   }

   /**
    * Create an object parameter array with the arguments converted to strings
    * 
    * @param cid1 content id 1, non-zero required
    * @param cid2 content id 2, if zero then this is passed as an empty string
    * @param r1 revision 1, non-zero required
    * @param r2 revision 2, if zero then this is passed as an empty string
    * @return an allocated array of strings
    */
   private Object[] makeParameters(int cid1, int cid2, int r1, int r2)
   {
      String arr[] = new String[4];

      arr[0] = Integer.toString(cid1);
      arr[1] = cid2 == 0 ? "" : Integer.toString(cid2);
      arr[2] = Integer.toString(r1);
      arr[3] = r2 == 0 ? "" : Integer.toString(r2);
      return arr;
   }
}

