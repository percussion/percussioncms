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

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// Test case
public class PSContentEditorTest extends TestCase
{
   public PSContentEditorTest(String name)
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
      PSParam param1 = new PSParam("pssessionid", new PSUserContext("/User/SessionId"));
      PSCollection parameters1 = new PSCollection(param1.getClass());
      parameters1.add(param1);
      PSUrlRequest req1 = new PSUrlRequest("bannerincludeurl", "file:rx_resources/html/banner.html", parameters1);
      PSCollection sectLinkList = new PSCollection(req1.getClass());
      sectLinkList.add(req1);


      final String DATASOURCE = "rxdefault";
      PSBackEndCredential cred = new PSBackEndCredential("Cred1");
      cred.setDataSource(DATASOURCE);
      PSTableLocator tableLoc = new PSTableLocator(cred);
      PSTableRef tableRef1 = new PSTableRef("ARTICLE");
      PSTableRef tableRef2 = new PSTableRef("ARTICLEAUTHORS");
      PSTableRef tableRef3 = new PSTableRef("AUTHORSOCIETIES");
      PSCollection tableRefs = new PSCollection(tableRef1.getClass());
      tableRefs.add(tableRef1);
      tableRefs.add(tableRef2);
      tableRefs.add(tableRef3);
      PSTableSet ts = new PSTableSet(tableLoc, tableRefs);
      PSCollection tsCol = new PSCollection(ts.getClass());
      tsCol.add(ts);
      PSContainerLocator loc = new PSContainerLocator(tsCol);
      
      String tableName = "ARTICLEAUTHORS";
      PSBackEndTable table2 = new PSBackEndTable(tableName);
      table2.setDataSource(DATASOURCE);
      table2.setTable(tableName);
      
      PSField field4 = new PSField("firstname", new PSBackEndColumn(table2, "FIRSTNAME"));
      PSField field5 = new PSField("lastname", new PSBackEndColumn(table2, "LASTNAME"));
      PSFieldSet fs2 = new PSFieldSet("authorset");
      fs2.setType(PSFieldSet.TYPE_COMPLEX_CHILD);
      fs2.add(field4);
      fs2.add(field5);

      tableName = "ARTICLE";
      PSBackEndTable table1 = new PSBackEndTable(tableName);
      table1.setDataSource(DATASOURCE);
      table1.setTable(tableName);
      
      PSField field1 = new PSField("abstract", new PSBackEndColumn(table1, "ABSTRACT"));
      PSField field2 = new PSField("bodycontent", new PSBackEndColumn(table1, "BODYCONTENT"));
      PSField field3 = new PSField("authorage", new PSBackEndColumn(table1, "BODYCONTENT"));

      tableName = "AUTHORSOCIETIES";
      PSBackEndTable table3 = new PSBackEndTable(tableName);
      table3.setDataSource(DATASOURCE);
      table3.setTable(tableName);

      PSField field6 = new PSField("societyname", new PSBackEndColumn(table3, "SOCIETYNAME"));
      PSConditional cond1 = new PSConditional(new PSTextLiteral("authorage"), 
         PSConditional.OPTYPE_GREATERTHAN, new PSTextLiteral("0"), PSConditional.OPBOOL_AND);
      PSConditional cond2 = new PSConditional(new PSTextLiteral("authorage"), 
         PSConditional.OPTYPE_LESSTHAN, new PSTextLiteral("200"));
      PSCollection conditionals = new PSCollection(cond1.getClass());
      conditionals.add(cond1);
      conditionals.add(cond2);
      PSRule rule1 = new PSRule(conditionals);
      rule1.setErrorLabel(new PSDisplayText("AgeInvalid"));
      PSCollection rules = new PSCollection(rule1.getClass());
      rules.add(rule1);
      PSFieldValidationRules fieldRules = new PSFieldValidationRules();
      fieldRules.setRules(rules);
      field3.setValidationRules(fieldRules);
      PSFieldSet fs1 = new PSFieldSet("main");
      fs1.setRepeatability(PSFieldSet.REPEATABILITY_ONE_OR_MORE);
      fs1.add(field1);
      fs1.add(field2);
      fs1.add(field3);
      fs1.add(fs2);
      fs1.add(field6);
      
      PSDisplayMapper dispMapper = new PSDisplayMapper("main");
      dispMapper.setId(1);
      PSUISet uiSet1 = new PSUISet();
      uiSet1.setControl(new PSControlRef("StandardTextArea"));
      uiSet1.setLabel(new PSDisplayText("Abstract:"));
      PSUIDefinition uiDef = new PSUIDefinition(dispMapper);
      uiDef.appendMapping(dispMapper, new PSDisplayMapping("abstract", uiSet1));
      PSUISet uiSet2 = new PSUISet();
      uiSet2.setControl(new PSControlRef("StandardRtfEditor"));
      uiSet2.setLabel(new PSDisplayText("Body:"));
      uiDef.appendMapping(dispMapper, new PSDisplayMapping("bodycontent", uiSet2));
      PSParam param2 = new PSParam("ALIGN", new PSTextLiteral("center"));
      PSCollection parameters2 = new PSCollection(param2.getClass());
      parameters2.add(param2);
      PSUISet uiSet3 = new PSUISet();
      uiSet3.setControl(new PSControlRef("sys_TextBox", parameters2));
      uiSet3.setLabel(new PSDisplayText("Author Age:"));
      uiDef.appendMapping(dispMapper, new PSDisplayMapping("authorage", uiSet3));
      PSUISet uiSet4 = new PSUISet();
      uiSet4.setControl(new PSControlRef("sys_StandardSummaryTable"));
      uiSet4.setLabel(new PSDisplayText("Authors:"));
      uiDef.appendMapping(dispMapper, new PSDisplayMapping("authors", uiSet4));
      PSUISet uiSet5 = new PSUISet();
      uiSet5.setControl(new PSControlRef("sys_TextBox"));
      uiSet5.setLabel(new PSDisplayText("First Name:"));
      uiDef.appendMapping(dispMapper, new PSDisplayMapping("firstname", uiSet5));
      PSUISet uiSet6 = new PSUISet();
      uiSet6.setControl(new PSControlRef("sys_TextBox"));
      uiSet6.setLabel(new PSDisplayText("Last Name:"));
      uiDef.appendMapping(dispMapper, new PSDisplayMapping("lastname", uiSet6));
      ArrayList sharedIncludes = new ArrayList();
      sharedIncludes.add("group1");
      PSContentEditorMapper ceMapper = new PSContentEditorMapper(null, sharedIncludes, fs1, uiDef);
      PSContentEditorPipe pipe = new PSContentEditorPipe("cePipe", loc, ceMapper);

      PSParam param3 = new PSParam("ALIGN", new PSTextLiteral("center"));
      PSCollection parameters3 = new PSCollection(param3.getClass());
      parameters3.add(param3);
      PSUrlRequest newReq = new PSUrlRequest("new", "file:rx_resources/html/new.html", parameters3);
      PSCommandHandlerStylesheets commandHandlerSS = new PSCommandHandlerStylesheets("New", new PSStylesheet(newReq));

      PSParam param4 = new PSParam("ALIGN", new PSTextLiteral("center"));
      PSCollection parameters4 = new PSCollection(param4.getClass());
      parameters4.add(param4);
      PSUrlRequest redirect = new PSUrlRequest("redirect", "file:rx_resources/html/redirect.html", parameters4);
      PSApplicationFlow appFlow = new PSApplicationFlow("Redirect", redirect);

      PSContentEditor testTo = new PSContentEditor("ContentEditor", 1, 1, commandHandlerSS);
      testTo.setApplicationFlow(appFlow);
      testTo.setSectionLinkList(sectLinkList);
      testTo.setPipe(pipe);
      Element elem = testTo.toXml(doc);
      root.appendChild(elem);

      // create a new object and populate it from our testTo element
      PSContentEditor testFrom = new PSContentEditor(elem, null, null);
      Document doc2 = PSXmlDocumentBuilder.createXmlDocument();
      Element root2 = PSXmlDocumentBuilder.createRoot(doc2, "Test");
      Element elem2 = testFrom.toXml(doc2);
      root2.appendChild(elem2);
      assertTrue(testTo.equals(testFrom));
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      
      suite.addTest(new PSContentEditorTest("testXml"));
      
      return suite;
   }
}
