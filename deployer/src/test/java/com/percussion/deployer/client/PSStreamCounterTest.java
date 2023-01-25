/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
