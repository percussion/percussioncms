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

package com.percussion.design.objectstore;

import java.util.Properties;

import junit.framework.TestCase;

import org.apache.commons.lang.RandomStringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.percussion.xml.PSXmlDocumentBuilder;
import static com.percussion.testing.PSTestCompare.assertEqualsWithHash;


/**
 *   Unit tests for the PSApplication class
 */

public class PSApplicationTest extends TestCase
{
   /**
    * Tests behavior of equals() and hashCode() methods.
    */
   public void testEqualsHashCode()
   {
      final int ID = 123;
      final String NAME = "NAME " + RandomStringUtils.random(2);
      final String DESCRIPTION = "DESCRIPTION " + RandomStringUtils.random(2);
      final String REQUEST_ROOT = "REQUEST ROOT " + RandomStringUtils.random(2); 

      final PSApplication a = new PSApplication(); 

      final PSApplication a2 = new PSApplication();
      assertEqualsWithHash(a, a2);

      a2.setId(ID);
      assertFalse(a.equals(a2));
      a.setId(ID);
      assertEqualsWithHash(a, a2);
      
      a2.setName(NAME);
      assertFalse(a.equals(a2));
      a.setName(NAME);
      assertEqualsWithHash(a, a2);
      
      a2.setDescription(DESCRIPTION);
      assertFalse(a.equals(a2));
      a.setDescription(DESCRIPTION);
      assertEqualsWithHash(a, a2);
      
      a2.setRequestRoot(REQUEST_ROOT);
      assertFalse(a.equals(a2));
      a.setRequestRoot(REQUEST_ROOT);
      assertEqualsWithHash(a, a2);
}

   
   public void testXml() throws Exception
   {
      // assert that two empty apps are equal
      PSApplication app = new PSApplication();
      PSApplication fromXmlApp = new PSApplication();
      assertEquals(app, fromXmlApp);

      // construct one from the other and assert they are still equal
      app.setName("UnitTestApp");
      assertTrue(!app.equals(fromXmlApp));
      fromXmlApp.fromXml(app.toXml());
      assertEquals(app.getName(), fromXmlApp.getName());
      assertEquals(app, fromXmlApp);

      fromXmlApp.fromXml(app.toXml());
      assertEquals(app, fromXmlApp);

      app.setDescription("An app for all seasons");
      assertTrue(!app.equals(fromXmlApp));
      fromXmlApp.fromXml(app.toXml());
      assertEquals(app, fromXmlApp);

      app.setEnabled(false);
      fromXmlApp.setEnabled(true);
      assertTrue(!app.equals(fromXmlApp));
      fromXmlApp.fromXml(app.toXml());
      assertEquals(app, fromXmlApp);

      app.setRequestRoot("/E2/UnitRoot");
      assertTrue(!app.equals(fromXmlApp));
      fromXmlApp.fromXml(app.toXml());
      assertEquals(app, fromXmlApp);

      app.setMaxThreads(17);
      assertTrue(!app.equals(fromXmlApp));
      fromXmlApp.fromXml(app.toXml());
      assertEquals(app, fromXmlApp);

      app.setMaxRequestTime(11);
      assertTrue(!app.equals(fromXmlApp));
      fromXmlApp.fromXml(app.toXml());
      assertEquals(app, fromXmlApp);

      app.setMaxRequestsInQueue(37);
      assertTrue(!app.equals(fromXmlApp));
      fromXmlApp.fromXml(app.toXml());
      assertEquals(app, fromXmlApp);

      app.setUserSessionEnabled(false);
      fromXmlApp.setUserSessionEnabled(true);
      assertTrue(!app.equals(fromXmlApp));
      fromXmlApp.fromXml(app.toXml());
      assertEquals(app, fromXmlApp);

      app.setUserSessionTimeout(117);
      assertTrue(!app.equals(fromXmlApp));
      fromXmlApp.fromXml(app.toXml());
      assertEquals(app, fromXmlApp);

      app.setRequestTypeHtmlParamName("ecstatic");
      assertTrue(!app.equals(fromXmlApp));
      fromXmlApp.fromXml(app.toXml());
      assertEquals(app, fromXmlApp);

      app.setRequestTypeValueQuery("tazo");
      assertTrue(!app.equals(fromXmlApp));
      fromXmlApp.fromXml(app.toXml());
      assertEquals(app, fromXmlApp);

      app.setRequestTypeValueInsert("chai");
      assertTrue(!app.equals(fromXmlApp));
      fromXmlApp.fromXml(app.toXml());
      assertEquals(app, fromXmlApp);

      app.setRequestTypeValueUpdate("colorado");
      assertTrue(!app.equals(fromXmlApp));
      fromXmlApp.fromXml(app.toXml());
      assertEquals(app, fromXmlApp);

      app.setRequestTypeValueDelete("wyoming");
      assertTrue(!app.equals(fromXmlApp));
      fromXmlApp.fromXml(app.toXml());
      assertEquals(app, fromXmlApp);

      app.setRevision("Application test program",
                                "This is a dummy revision");
      app.setRevision("Application test program",
                                "Yet another dummy revision");
      assertTrue(!app.equals(fromXmlApp));
      fromXmlApp.fromXml(app.toXml());
      assertEquals(app, fromXmlApp);

      Properties props = new Properties();
      props.setProperty("foo", "bar");
      app.setUserProperties(props);
      assertTrue(!app.equals(fromXmlApp));
      fromXmlApp.fromXml(app.toXml());
      assertEquals(app, fromXmlApp);

      /* try a passes on the doc
       *  - just the root
       *  - a simple node/value
       *  - some more complex hierarchies
       */
      Document propDoc = PSXmlDocumentBuilder.createXmlDocument();
      Element propRoot = PSXmlDocumentBuilder.createRoot(propDoc,
                                                         "AppTestRoot");
      app.setPropertyTree(propDoc);
      assertTrue(!app.equals(fromXmlApp));
      fromXmlApp.fromXml(app.toXml());
      assertEquals(app, fromXmlApp);

      // now test from the stored doc
      propDoc = app.getPropertyTree();
      propRoot = propDoc.getDocumentElement();

      PSXmlDocumentBuilder.addElement(propDoc,
                                      propRoot,
                                      "SimpleElement", "SimpleValue");
      app.setPropertyTree(propDoc);
      assertTrue(!app.equals(fromXmlApp));
      fromXmlApp.fromXml(app.toXml());
      assertEquals(app, fromXmlApp);

      PSXmlDocumentBuilder.addElement(
         propDoc,
         PSXmlDocumentBuilder.addEmptyElement(propDoc,
                                              propRoot,
                                              "Hierarchical"),
         "Value",
         "HierarchicalValue");
      app.setPropertyTree(propDoc);
      assertTrue(!app.equals(fromXmlApp));
      fromXmlApp.fromXml(app.toXml());
      assertEquals(app, fromXmlApp);
   }
}
