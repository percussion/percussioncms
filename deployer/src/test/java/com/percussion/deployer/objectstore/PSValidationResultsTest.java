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

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.xml.PSXmlDocumentBuilder;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Unit test class for the <code>PSValidationResults</code> class.
 */
public class PSValidationResultsTest extends TestCase
{
   /**
    * Construct this unit test
    *
    * @param name The name of this test.
    */
    public PSValidationResultsTest(String name)
   {
      super(name);
   }

   /**
    * Test PSValidationResults class
    *
    * @throws Exception If there are any errors.
    */
   public void testAll() throws Exception
   {
      // prepare data
      PSValidationResult vResult1 =
         PSValidationResultTest.getVResultNotAllowSkip();
      PSValidationResult vResult2 =
         PSValidationResultTest.getValidationResult2();

      PSDeployableElement dep1 = new PSDeployableElement(
         PSDependency.TYPE_SHARED, "3", "TestElem3", "Test Element3",
         "myTestElement3", true, false, false);
      PSDeployableObject dep2 = new PSDeployableObject(
         PSDependency.TYPE_LOCAL, "2", "TestObj2", "Test Object2",
         "myTestObject2", true, false, true);

      PSValidationResults results = new PSValidationResults();
      results.addResult(vResult1);
      results.addResult(vResult2);

      PSValidationResults tgtResults = object2Xml2Object(results);

      // compare objects have both lists
      assertTrue( results.equals(tgtResults) );

      // compare objects have empty lists
      PSValidationResults emptyResults = new PSValidationResults();
      PSValidationResults tgtEmptyResults = object2Xml2Object(emptyResults);

      assertTrue( emptyResults.equals(tgtEmptyResults) );

      // compare objects have only validation result list
      results = new PSValidationResults();
      results.addResult(vResult1);
      results.addResult(vResult2);
      tgtResults = object2Xml2Object(results);

      assertTrue( results.equals(tgtResults) );

      // compare objects have only dependency list
      results = new PSValidationResults();
      tgtResults = object2Xml2Object(results);

      assertTrue( results.equals(tgtResults) );
   }

   /**
    * Converts a given object to XML, then create another new object from
    * the XML.
    *
    * @param results The object to be converted, assume not <code>null</code>.
    *
    * @return The newly created object.
    *
    * @throws PSUnknownNodeTypeException if malformed XML occures
    */
   private PSValidationResults object2Xml2Object(PSValidationResults results)
      throws PSUnknownNodeTypeException
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element element = results.toXml(doc);
      return new PSValidationResults(element);
   }

   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSValidationResultsTest("testAll"));
      return suite;
   }

}
