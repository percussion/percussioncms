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

import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlDocumentBuilder;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

// Test case
public class PSChoicesTest extends TestCase
{
   public PSChoicesTest(String name)
   {
      super(name);
   }

   public void testEquals() throws Exception
   {
   }

   public void testXmlGlobal() throws Exception
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(doc, "Test");

      // create test object
      PSChoices testTo = new PSChoices(120);
      Element elem = testTo.toXml(doc);
      PSXmlDocumentBuilder.copyTree(doc, root, elem, true);

      // create a new object and populate it from our testTo element
      PSChoices testFrom = new PSChoices(elem, null, null);
      assertTrue(testTo.equals(testFrom));
   }

   public void testXmlLocal() throws Exception
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(doc, "Test");

      // create test object
      PSEntry entry1 = new PSEntry("1111", new PSDisplayText("one"));
      PSEntry entry2 = new PSEntry("2222", new PSDisplayText("two"));
      PSEntry entry3 = new PSEntry("3333", new PSDisplayText("three"));
      PSCollection choices = new PSCollection(entry1.getClass());
      choices.add(entry1);
      choices.add(entry2);
      choices.add(entry3);
      PSDefaultSelected default1 = new PSDefaultSelected();
      PSCollection defaults = new PSCollection(default1.getClass());
      defaults.add(default1);
      PSNullEntry nullEntry = new PSNullEntry("null", new PSDisplayText("nullEntry"));
      PSChoices testTo = new PSChoices(choices);
      testTo.setNullEntry(nullEntry);
      testTo.setDefaultSelected(defaults);
      Element elem = testTo.toXml(doc);
      PSXmlDocumentBuilder.copyTree(doc, root, elem, true);

      // create a new object and populate it from our testTo element
      PSChoices testFrom = new PSChoices(elem, null, null);
      Document doc2 = PSXmlDocumentBuilder.createXmlDocument();
      Element root2 = PSXmlDocumentBuilder.createRoot(doc2, "Test");
      Element elem2 = testFrom.toXml(doc);
      PSXmlDocumentBuilder.copyTree(doc2, root2, elem2, true);
      assertTrue(testTo.equals(testFrom));
   }

   public void testXmlLookup() throws Exception
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(doc, "Test");

      // create test object
      PSParam param1 = new PSParam("p1", new PSTextLiteral("param1"));
      PSParam param2 = new PSParam("p2", new PSTextLiteral("param2"));
      PSParam param3 = new PSParam("p3", new PSTextLiteral("param3"));
      PSCollection parameters = new PSCollection(param1.getClass());
      parameters.add(param1);
      parameters.add(param2);
      parameters.add(param3);
      PSUrlRequest request = new PSUrlRequest("newRequest", "http://38.227.11.8/Rhythmyx/1111.htm", parameters);
      PSChoices testTo = new PSChoices(request, PSChoices.TYPE_LOOKUP);
      Element elem = testTo.toXml(doc);
      PSXmlDocumentBuilder.copyTree(doc, root, elem, true);

      // create a new object and populate it from our testTo element
      PSChoices testFrom = new PSChoices(elem, null, null);
      assertTrue(testTo.equals(testFrom));
   }

   public void testChoiceFilter() throws Exception
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(doc, "Test");

      // create test object
      PSParam param1 = new PSParam("p1", new PSTextLiteral("param1"));
      PSParam param2 = new PSParam("p2", new PSTextLiteral("param2"));
      PSParam param3 = new PSParam("p3", new PSTextLiteral("param3"));
      PSCollection parameters = new PSCollection(param1.getClass());
      parameters.add(param1);
      parameters.add(param2);
      parameters.add(param3);
      PSUrlRequest request = new PSUrlRequest("newRequest", "http://38.227.11.8/Rhythmyx/1111.htm", parameters);

      //test PSChoiceFilter
      PSUrlRequest requestFilterUrl =
         new PSUrlRequest("filterRequest", "http://localhost/Rhythmyx/777.xml", parameters);

      PSCollection dependentFields = new PSCollection(PSChoiceFilter.DependentField.class);
      dependentFields.add(new PSChoiceFilter.DependentField("my_fieldRef1", "optional"));
      dependentFields.add(new PSChoiceFilter.DependentField("my_fieldRef2", "required"));

      PSChoiceFilter chFilter = new PSChoiceFilter(dependentFields, requestFilterUrl);

      PSChoices testTo = new PSChoices(request, PSChoices.TYPE_LOOKUP);
      testTo.setChoiceFilter(chFilter);

      Element elem = testTo.toXml(doc);
      PSXmlDocumentBuilder.copyTree(doc, root, elem, true);

      // create a new object and populate it from our testTo element
      PSChoices testFrom = new PSChoices(elem, null, null);
      assertTrue(testTo.equals(testFrom));
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite();

      suite.addTest(new PSChoicesTest("testXmlGlobal"));
      suite.addTest(new PSChoicesTest("testXmlLocal"));
      suite.addTest(new PSChoicesTest("testXmlLookup"));
      suite.addTest(new PSChoicesTest("testChoiceFilter"));


      return suite;
   }
}
