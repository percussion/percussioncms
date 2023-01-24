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
 * Unit test for the <code>PSValidationResult</code> class.
 */
public class PSValidationResultTest extends TestCase
{
   /**
    * Construct this unit test
    *
    * @param name The name of this test.
    */
    public PSValidationResultTest(String name)
   {
      super(name);
   }

   /**
    * Test XML and Equals features for PSValidationResult class
    *
    * @throws Exception If there are any errors.
    */
   public void testXmlEquals() throws Exception
   {
      PSValidationResult vResult1 = getVResultNotAllowSkip();
      PSValidationResult tgtResult1 = getValidationResultFromXML(vResult1);

      PSValidationResult vResult2 = getValidationResult2();
      PSValidationResult tgtResult2 = getValidationResultFromXML(vResult2);

      // these should work fine
      assertTrue( vResult1.equals(tgtResult1) );
      assertTrue( vResult2.equals(tgtResult2) );

      // these should not work
      assertTrue( ! vResult1.equals(vResult2) );
   }

   /**
    * Test <code>allowSkip</code> method for PSValidationResult class
    *
    * @throws Exception If there are any errors.
    */
   public void testAllowSkip() throws Exception
   {
      PSValidationResult vResult1 = getVResultNotAllowSkip();
      PSValidationResult vResult2 = getValidationResult2();

      // these should work fine
      assertTrue( testSetSkip(vResult1, false) );
      assertTrue( testSetSkip(vResult2, false) );
      assertTrue( testSetSkip(vResult2, true) );

      // these should not work
      assertTrue( ! testSetSkip(vResult1, true) );
   }


   /**
    * @return <code>PSValidationResult</code> which does not allow to skip
    * intall.
    */
   public static PSValidationResult getVResultNotAllowSkip()
   {
      PSDeployableElement dep1 = new PSDeployableElement(
         PSDependency.TYPE_SHARED, "1", "TestElem", "Test Element",
         "myTestElement", true, false, false);

      PSValidationResult vResult1 = new PSValidationResult(dep1, true,
         "Error message", false);

      return vResult1;
   }

   /**
    * @return <code>PSValidationResult</code> which does allow to skip intall.
    */
   public static PSValidationResult getValidationResult2()
   {
      PSDeployableObject dep2 = new PSDeployableObject(
         PSDependency.TYPE_LOCAL, "1", "TestObj1", "Test Object1",
         "myTestObject1", true, false, true);
      PSValidationResult vResult2 = new PSValidationResult(dep2, true,
         "Error message 2", true);

      return vResult2;
   }

   /**
    * Convert a <code>ValidationResult</code> to XML, then create a new
    * object from it.
    *
    * @param vResult The object to be converted. Assume it is not
    * <code>null</code>
    *
    * @return The converted object (from XML).
    *
    * @throws PSUnknownNodeTypeException if any error occures.
    */
   private static PSValidationResult getValidationResultFromXML(
      PSValidationResult vResult) throws PSUnknownNodeTypeException
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element vrEl = vResult.toXml(doc);

      PSValidationResult tgtResult = new PSValidationResult(vrEl);

      return tgtResult;
   }

   /**
    * Testing <code>skipInstall</code>  method.
    *
    * @param vResult  The object to test with. Assunme not <code>null</code>
    * @param bSkipInstall The value pass to <code>skipInstall</code> method.
    *
    * @return <code>true</code> if no error; <code>false</code> otherwise.
    */
   private boolean testSetSkip(PSValidationResult vResult, boolean bSkipInstall)
   {
      try
      {
         vResult.skipInstall(bSkipInstall);
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
      suite.addTest(new PSValidationResultTest("testXmlEquals"));
      suite.addTest(new PSValidationResultTest("testAllowSkip"));
      return suite;
   }

}
