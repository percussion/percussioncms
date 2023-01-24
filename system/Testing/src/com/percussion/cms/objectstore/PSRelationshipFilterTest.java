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
