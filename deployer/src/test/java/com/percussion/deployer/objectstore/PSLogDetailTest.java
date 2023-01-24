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
 * Unit test class for the <code>PSLogDetail</code> class.
 */
public class PSLogDetailTest extends TestCase
{
   /**
    * Construct this unit test
    *
    * @param name The name of this test.
    */
    public PSLogDetailTest(String name)
   {
      super(name);
   }

   /**
    * Test all features of PSLogDetail class
    *
    * @throws Exception If there are any errors.
    */
   public void testAll() throws Exception
   {
      // prepare data
      PSValidationResults vResults = new PSValidationResults();
      PSDbmsMap dbmsMap = new PSDbmsMap("srcServer1");
      PSIdMap idMap = new PSIdMap("sourceServer3");
      PSTransactionLogSummary txnlog = new PSTransactionLogSummary();

      PSLogDetail srcDetail = new PSLogDetail(vResults, idMap, dbmsMap, txnlog);
      PSLogDetail srcDetailNoIdMap = new PSLogDetail(vResults, null, dbmsMap,
         txnlog);

      // object -> XML -> object
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element element = srcDetail.toXml(doc);
      PSLogDetail tgtDetail = new PSLogDetail(element);

      element = srcDetailNoIdMap.toXml(doc);
      PSLogDetail tgtDetailNoIdMap = new PSLogDetail(element);

      // source should be the same as the target object.
      assertTrue( srcDetail.equals(tgtDetail) );
      assertTrue( srcDetailNoIdMap.equals(tgtDetailNoIdMap) );
   }

   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSLogDetailTest("testAll"));
      return suite;
   }

}
