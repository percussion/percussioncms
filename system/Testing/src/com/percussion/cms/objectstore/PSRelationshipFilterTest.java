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
package com.percussion.cms.objectstore;

import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for the {@link PSRelationshipSet} class.
 */
public class PSRelationshipFilterTest extends TestCase
{
   // see base class
   public PSRelationshipFilterTest(String name)
   {
      super(name);
   }

   /**
    * Tests the Relationship Filter
    *
    * @throws Exception if an error occurs
    */
   public void testAll() throws Exception
   {
      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setDependentContentTypeId(311);
      testRoundTrip(filter);
      assertTrue(filter.getDependentContentTypeId() == 311);

      filter.setDependentContentTypeId(-1);
      testRoundTrip(filter);
      assertTrue(filter.getDependentContentTypeId() == -1);
      assertTrue(filter.getDependentContentTypeIds() == null);

      filter.setDependentContentTypeId(-2);
      testRoundTrip(filter);
      assertTrue(filter.getDependentContentTypeId() == -1);
      assertTrue(filter.getDependentContentTypeIds() == null);

      assertTrue(filter.getDependents() == null);
      filter.setDependent(new PSLocator(2, -1));
      testRoundTrip(filter);
      assertTrue(filter.getDependents() != null);

      filter.setDependent(null);
      testRoundTrip(filter);
      assertTrue(filter.getDependents() == null);

      filter.setName(PSRelationshipConfig.TYPE_ACTIVE_ASSEMBLY);
      testRoundTrip(filter);

      List<String> names = new ArrayList<String>();
      names.add(PSRelationshipConfig.TYPE_ACTIVE_ASSEMBLY);
      names.add(PSRelationshipConfig.TYPE_ACTIVE_ASSEMBLY_MANDATORY);
      names.add(PSRelationshipConfig.TYPE_FOLDER_CONTENT);
      testRoundTrip(filter);
   }

   /**
    * Testing to/from XML methods
    *
    * @param filter the tested object, assumed not <code>null</code>.
    */
   private void testRoundTrip(PSRelationshipFilter filter)
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element filterEl = filter.toXml(doc);
      PSRelationshipFilter tgtFilter = new PSRelationshipFilter(filterEl);

      assertTrue(filter.equals(tgtFilter));
   }

   // collect all tests into a TestSuite and return it - see base class
   public static Test suite()
   {
      TestSuite suite = new TestSuite();

      suite.addTest(new PSRelationshipFilterTest("testAll"));

      return suite;
   }
}
