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

package com.percussion.deploy.objectstore;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.utils.testing.UnitTest;
import com.percussion.xml.PSXmlDocumentBuilder;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.junit.Assert.assertTrue;

/**
 * Unit test class for the <code>PSValidationResults</code> class.
 */
@Category(UnitTest.class)
public class PSValidationResultsTest
{

    public PSValidationResultsTest()
   {

   }

   /**
    * Test PSValidationResults class
    *
    * @throws Exception If there are any errors.
    */
   @Test
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
      results.addAbsentAncestor(dep1);
      results.addAbsentAncestor(dep2);

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
      results.addAbsentAncestor(dep1);
      results.addAbsentAncestor(dep2);
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

}
