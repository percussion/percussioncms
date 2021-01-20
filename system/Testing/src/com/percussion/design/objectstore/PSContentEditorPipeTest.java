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
public class PSContentEditorPipeTest extends TestCase
{
   public PSContentEditorPipeTest(String name)
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
      PSUISet uiSet = new PSUISet();
      PSParam param = new PSParam("param_1", new PSTextLiteral("value_1"));
      PSCollection parameters = new PSCollection(param.getClass());
      parameters.add(param);
      uiSet.setControl(new PSControlRef("sys_EditBox", parameters));
      PSDisplayMapping mapping = new PSDisplayMapping("DISPLAYTITLE", uiSet);
      PSUIDefinition uiDef = new PSUIDefinition(dispMapper);
      uiDef.appendMapping(dispMapper, mapping);
      PSContentEditorMapper ceMapper = new PSContentEditorMapper(null, null, fs, uiDef);

      PSContentEditorPipe testTo = new PSContentEditorPipe("cePipe", loc, ceMapper);
      Element elem = testTo.toXml(doc);
      root.appendChild(elem);

      // create a new object and populate it from our testTo element
      PSContentEditorPipe testFrom = new PSContentEditorPipe(elem, null, null);
      Document doc2 = PSXmlDocumentBuilder.createXmlDocument();
      Element root2 = PSXmlDocumentBuilder.createRoot(doc2, "Test");
      Element elem2 = testFrom.toXml(doc);
      Node node = doc2.importNode(elem2,true);
      root2.appendChild(node);
      assertTrue(testTo.equals(testFrom));
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite();

      suite.addTest(new PSContentEditorPipeTest("testXml"));
      
      return suite;
   }
}
