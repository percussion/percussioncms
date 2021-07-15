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
package com.percussion.design.objectstore;

import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlDocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// Test case
public class PSUIDefinitionTest extends TestCase
{
   public PSUIDefinitionTest(String name)
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
      PSEntry entry1 = new PSEntry("1111", new PSDisplayText("one"));
      PSEntry entry2 = new PSEntry("2222", new PSDisplayText("two"));
      PSEntry entry3 = new PSEntry("3333", new PSDisplayText("three"));
      PSCollection choiceCol = new PSCollection(entry1.getClass());
      choiceCol.add(entry1);
      choiceCol.add(entry2);
      choiceCol.add(entry3);
      PSChoices choices = new PSChoices(choiceCol);
      PSBackEndCredential cred = new PSBackEndCredential("credential_1");
      cred.setDataSource("rxdefault");
      PSTableLocator tl = new PSTableLocator(cred);
      PSTableRef tr = new PSTableRef("tableName_1", "tableAlias_1");
      PSTableSet ts = new PSTableSet(tl, tr);
      PSCollection tsCol = new PSCollection(ts.getClass());
      tsCol.add(ts);
      PSContainerLocator loc = new PSContainerLocator(tsCol);
      PSBackEndTable table = new PSBackEndTable("RXARTICLE");
      PSField f = new PSField("field_1", new PSBackEndColumn(table, "DISPLAYTITLE"));
      PSFieldSet fs = new PSFieldSet("fieldSet_1", f);
      PSDisplayMapper dispMapper = new PSDisplayMapper("fs_1");
      PSDisplayMapper dispMapper2 = new PSDisplayMapper("fs_2");
      PSUISet uiSet = new PSUISet();
      uiSet.setName("set_1");
      uiSet.setDefaultSet("default");
      uiSet.setChoices(choices);
      uiSet.setControl(new PSControlRef("control_1"));
      uiSet.setLabel(new PSDisplayText("label"));
      uiSet.setErrorLabel(new PSDisplayText("errorLabel"));
      uiSet.setReadOnlyRules(ruleCol);
      PSParam param = new PSParam("param_1", new PSTextLiteral("value_1"));
      param = new PSParam("param_1", new PSTextLiteral("value_1"));
      PSCollection parameters = new PSCollection(param.getClass());
      parameters.add(param);
      uiSet.setControl(new PSControlRef("sys_EditBox", parameters));
      PSDisplayMapping mapping1 = new PSDisplayMapping("One", uiSet);
      PSDisplayMapping mapping2 = new PSDisplayMapping("Two", uiSet);
      mapping2.setDisplayMapper(dispMapper2);
      PSDisplayMapping mapping3 = new PSDisplayMapping("Three", uiSet);
      PSUISet defaultUI = new PSUISet();
      PSCollection defaultUIs = new PSCollection(defaultUI.getClass());
      defaultUIs.add(defaultUI);
      defaultUI.setControl(new PSControlRef("sys_EditBox", parameters));
      PSUIDefinition testTo = new PSUIDefinition(dispMapper, defaultUIs);
      testTo.appendMapping(dispMapper, mapping1);
      testTo.appendMapping(dispMapper, mapping2);
      testTo.appendMapping(dispMapper, mapping3);

      Element elem = testTo.toXml(doc);
      root.appendChild(elem);

      // create a new object and populate it from our testTo element
      PSUIDefinition testFrom = new PSUIDefinition(elem, null, null);
      
      Document doc2 = PSXmlDocumentBuilder.createXmlDocument();
      Element root2 = PSXmlDocumentBuilder.createRoot(doc2, "Test");
      Element elem2 = testFrom.toXml(doc2);
      root2.appendChild(elem2);
      assertTrue(testTo.equals(testFrom));
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      
      suite.addTest(new PSUIDefinitionTest("testXml"));
      
      return suite;
   }
}
