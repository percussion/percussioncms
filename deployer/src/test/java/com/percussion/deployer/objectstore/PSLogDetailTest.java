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
