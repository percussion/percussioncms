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

package com.percussion.deployer.client;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Unit test for the {@link PSInputStreamCounter} and 
 * {@link PSOutputStreamCounter} classes.
 */
public class PSStreamCounterTest extends TestCase
{
   /**
    * Construct this unit test
    * 
    * @param name The name of this test.
    */
   public PSStreamCounterTest(String name)
   {
      super(name);
   }
   
   /**
    * Tests the <code>PSInputStreamCounter</code> class
    * 
    * @throws Exception if any errors occur.
    */
   public void testInputStreamCounter() throws Exception
   {
      String str = 
         "The quick brown fox jumped over the lazy doc, deployment rocks!!!!";
      byte[] data = str.getBytes();
      ByteArrayInputStream in = new ByteArrayInputStream(data);
      PSInputStreamCounter ic = new PSInputStreamCounter(in);
      
      int total = 0;
      
      ic.read();
      total++;
      assertTrue(ic.getByteCount() == total);
      
      byte[] buf; 
      buf = new byte[2];
      total += ic.read(buf);
      assertTrue(ic.getByteCount() == total);
      
      buf = new byte[4];
      total += ic.read(buf, 0, buf.length);
      assertTrue(ic.getByteCount() == total);
      
   }
   
   /**
    * Tests the <code>PSOutputStreamCounter</code> class
    * 
    * @throws Exception if any errors occur.
    */
   public void testOutputStreamCounter() throws Exception
   {
      String str = 
         "The quick brown fox jumped over the lazy doc, deployment rocks!!!!";
      byte[] data = str.getBytes();
      ByteArrayOutputStream out = new ByteArrayOutputStream(data.length + 5);
      PSOutputStreamCounter oc = new PSOutputStreamCounter(out);
   
      int total = 0;
      
      oc.write(data[0]);
      total++;
      assertEquals(oc.getByteCount(), total);
      
      oc.write(data, 1, 4);
      total += 4;
      assertEquals(oc.getByteCount(), total);
      
      oc.write(data);
      total += data.length;
      assertEquals(oc.getByteCount(), total);
   }
   
   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSStreamCounterTest("testInputStreamCounter"));
      suite.addTest(new PSStreamCounterTest("testOutputStreamCounter"));
      return suite;
   }
   
}
