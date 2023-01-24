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

package com.percussion.deployer.objectstore;

import com.percussion.xml.PSXmlDocumentBuilder;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Unit test class for both the <code>PSTransactionSummary</code> and
 * <code>PSTransactionLogSummary</code> classes.
 */
public class PSTransactionLogSummaryTest extends TestCase
{
   /**
    * Construct this unit test
    *
    * @param name The name of this test.
    */
    public PSTransactionLogSummaryTest(String name)
   {
      super(name);
   }

   /**
    * Test constructing <code>PSTransactionSummary</code> this object using
    * parameters
    *
    * @throws Exception If there are any errors.
    */
   public void testTranxConstructor() throws Exception
   {
      // these should work fine
      assertTrue(testCtorValid(123, "dep_1", "id_1", 
         PSTransactionSummary.TYPE_DATA,
         PSTransactionSummary.ACTION_CREATED));
      assertTrue(testCtorValid(100, "dep_2", "id_2", 
         PSTransactionSummary.TYPE_SCHEMA,
         PSTransactionSummary.ACTION_MODIFIED));


      // should be a problem
      assertTrue(! testCtorValid(123, "dep_1", "id_1", "Hello",
         PSTransactionSummary.ACTION_MODIFIED));
      assertTrue(! testCtorValid(123, "dep_1", "id_1", 
         PSTransactionSummary.TYPE_SCHEMA, 101));

      assertTrue(!testCtorValid(123, "", "", PSTransactionSummary.TYPE_DATA,
         PSTransactionSummary.ACTION_CREATED));
      assertTrue(!testCtorValid(123, null, null, PSTransactionSummary.TYPE_DATA,
         PSTransactionSummary.ACTION_CREATED));

      assertTrue(!testCtorValid(123, "f00", "foo", "",
         PSTransactionSummary.ACTION_CREATED));
      assertTrue(!testCtorValid(123, "foo", "foo", null,
         PSTransactionSummary.ACTION_CREATED));


   }

   /**
    * Tests all Xml functions, and uses equals as well.
    *
    * @throws Exception if there are any errors.
    */
   public void testXml() throws Exception
   {
      PSTransactionSummary tranx1 = new PSTransactionSummary(123, "dep1","id_1",
         PSTransactionSummary.TYPE_DATA, PSTransactionSummary.ACTION_CREATED);
      PSTransactionSummary tranx2 = new PSTransactionSummary(123, "dep1","id_1",
         PSTransactionSummary.TYPE_FILE, PSTransactionSummary.ACTION_MODIFIED);

      PSTransactionLogSummary src = new PSTransactionLogSummary();
      src.addTransaction(tranx1);
      src.addTransaction(tranx2);

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element srcEl = src.toXml(doc);

      PSTransactionLogSummary tgt = new PSTransactionLogSummary(srcEl);

      assertTrue(src.equals(tgt));
   }

   /**
    * Constructs a <code>PSTransactionSummary</code> object using the
    * supplied params and catches any exception.  For params,
    * see {@link PSTransactionSummary} ctor.
    *
    * @return <code>true</code> if no exceptions were caught, <code>false</code>
    * otherwise.
    */
   private boolean testCtorValid(int logId, String depDesc, String elementName,
      String elementType, int action)
   {
      try
      {
         PSTransactionSummary tranx = new PSTransactionSummary(logId, depDesc, 
            elementName, elementType, action);
      }
      catch (Exception ex)
      {
         return false;
      }

      return true;
   }

   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSTransactionLogSummaryTest("testTranxConstructor"));
      suite.addTest(new PSTransactionLogSummaryTest("testXml"));
      return suite;
   }

}
