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
package com.percussion.design.objectstore;

import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlDocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// Test case
public class PSContentEditorSharedDefTest extends TestCase
{
   public PSContentEditorSharedDefTest(String name)
   {
      super(name);
   }

   public void testEquals() throws Exception
   {
   }

   public void testXml() throws Exception
   {
      PSBackEndCredential cred = new PSBackEndCredential("Cred1");
      cred.setDataSource("rxdefault");
      PSTableLocator tableLoc = new PSTableLocator(cred);
      PSTableRef tableRef1 = new PSTableRef("CESHAREDCATEGORY");
      PSCollection tableRefs = new PSCollection(tableRef1.getClass());
      tableRefs.add(tableRef1);
      PSTableSet ts = new PSTableSet(tableLoc, tableRefs);
      PSCollection tsCol = new PSCollection(ts.getClass());
      tsCol.add(ts);
      PSContainerLocator loc = new PSContainerLocator(tsCol);

      PSBackEndTable table1 = new PSBackEndTable("CESHAREDCATEGORY");
      PSField field1 = new PSField(PSField.TYPE_SHARED, "category1",
            new PSBackEndColumn(table1, "CATEGORY"));
      PSField field2 = new PSField(PSField.TYPE_SHARED, "category2",
            new PSBackEndColumn(table1, "TITLE"));
      PSFieldSet fs1 = new PSFieldSet("main");
      fs1.add(field1);
      fs1.add(field2);

      PSDisplayMapper dispMapper = new PSDisplayMapper("main");
      dispMapper.setId(1);
      PSUISet uiSet1 = new PSUISet();
      uiSet1.setName("SharedCategory1UI");
      uiSet1.setControl(new PSControlRef("StandardDropList"));
      uiSet1.setLabel(new PSDisplayText("First Category:"));
      PSChoices choice = new PSChoices(1);
      PSNullEntry nullEntry1 = new PSNullEntry("value", new PSDisplayText("-- Choose --"));
      choice.setNullEntry(nullEntry1);
      PSDefaultSelected defaultSelected = new PSDefaultSelected();
      PSCollection defaults = new PSCollection(defaultSelected.getClass());
      defaults.add(new PSDefaultSelected());
      choice.setDefaultSelected(defaults);
      uiSet1.setChoices(choice);
      PSUIDefinition uiDef = new PSUIDefinition(dispMapper);
      uiDef.appendMapping(dispMapper, new PSDisplayMapping("category1", uiSet1));
      PSUISet uiSet2 = new PSUISet();
      uiSet2.setName("SharedCategory2UI");
      uiSet2.setControl(new PSControlRef("StandardTextField"));
      uiSet2.setLabel(new PSDisplayText("Second Category:"));
      uiSet2.setErrorLabel(new PSDisplayText("***** Second category must be supplied *****"));
      uiDef.appendMapping(dispMapper, new PSDisplayMapping("category2", uiSet2));
      
      PSSharedFieldGroup fg = new PSSharedFieldGroup("group1", loc, fs1, uiDef);
      PSCollection fgCol = new PSCollection(fg.getClass());
      fgCol.add(fg);

      PSContentEditorSharedDef testTo = new PSContentEditorSharedDef(fgCol);
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(doc, "Test");
      Element elem = testTo.toXml(doc);
      Node node = doc.importNode(elem,true);
      root.appendChild(node);
      
      // create a new object and populate it from our testTo element
      PSContentEditorSharedDef testFrom = new PSContentEditorSharedDef(elem, null, null);
      Document doc2 = PSXmlDocumentBuilder.createXmlDocument();
      Element root2 = PSXmlDocumentBuilder.createRoot(doc2, "Test");
      Element elem2 = testFrom.toXml(doc);
      Node node2 = doc2.importNode(elem2,true);
      root2.appendChild(node2);
      assertTrue(testTo.equals(testFrom));
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite();

      suite.addTest(new PSContentEditorSharedDefTest("testXml"));
      
      return suite;
   }
}
