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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// Test case
public class PSFieldValidationRulesTest extends TestCase
{
   public PSFieldValidationRulesTest(String name)
   {
      super(name);
   }

   public void testEquals() throws Exception
   {
   }

   public void testXml() throws Exception
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(doc, "Test");

      // create test object
      PSRule rule = new PSRule(new PSExtensionCallSet());
      PSCollection ruleCol = new PSCollection(rule.getClass());
      ruleCol.add(rule);
      ruleCol.add(rule);
      ruleCol.add(rule);
      PSCollection ruleRefs = new PSCollection("java.lang.String");
      ruleRefs.add("ref1");
      ruleRefs.add("ref2");
      ruleRefs.add("ref3");

      PSFieldValidationRules testTo = new PSFieldValidationRules();
      testTo.setApplyWhen(new PSApplyWhen());
      testTo.setName("fieldValidation");
      testTo.setRuleReferences(ruleRefs);
      testTo.setRules(ruleCol);
      testTo.setErrorMessage(new PSDisplayText("one"));
      Element elem = testTo.toXml(doc);
      root.appendChild(elem);

      // create a new object and populate it from our testTo element
      PSFieldValidationRules testFrom = new PSFieldValidationRules(elem, null, null);
      assertTrue(testTo.equals(testFrom));
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite();

      suite.addTest(new PSFieldValidationRulesTest("testXml"));
      
      return suite;
   }
}
