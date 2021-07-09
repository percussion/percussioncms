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

package com.percussion.deploy.objectstore;

import com.percussion.utils.testing.UnitTest;
import com.percussion.xml.PSXmlDocumentBuilder;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.junit.Assert.assertTrue;

/**
 * Unit test class for the <code>PSLogDetail</code> class.
 */
@Category(UnitTest.class)
public class PSLogDetailTest
{
    public PSLogDetailTest()
   {

   }

   /**
    * Test all features of PSLogDetail class
    *
    * @throws Exception If there are any errors.
    */
   @Test
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

}
