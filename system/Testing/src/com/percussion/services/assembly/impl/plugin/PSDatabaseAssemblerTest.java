/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
package com.percussion.services.assembly.impl.plugin;

import com.percussion.security.xml.PSSecureXMLUtils;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.assembly.data.PSAssemblyTemplate;
import com.percussion.services.assembly.data.PSAssemblyWorkItem;
import com.percussion.utils.jexl.PSJexlEvaluator;
import com.percussion.utils.tools.PSBaseXmlConfigTest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileWriter;

/**
 * Test the db assembly plugin
 * 
 * @author dougrand
 */
public class PSDatabaseAssemblerTest extends PSBaseXmlConfigTest
{
   private static File ms_test = new File(
         "UnitTestResources/com/percussion/tablefactory/multichild.xml");
   
   private static File ms_test_out = new File(
   "UnitTestResources/com/percussion/tablefactory/multichild_out.xml");
   
   static IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();

   /**
    * @throws Exception
    */
   @SuppressWarnings("unchecked")
   public void testDBAssembly() throws Exception
   {
      DocumentBuilderFactory f = PSSecureXMLUtils.getSecuredDocumentBuilderFactory(
             false);

      DocumentBuilder builder = f.newDocumentBuilder();

      Document doc = builder.parse(ms_test);

      PSJexlEvaluator bindings = setupBindings(doc);
      
      PSDatabaseAssembler dbassembler = new PSDatabaseAssembler();
      IPSAssemblyResult item = new PSAssemblyWorkItem();
      PSAssemblyTemplate tempt = new PSAssemblyTemplate();
      tempt.setTemplate("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
            "<tabledefset>\n" + 
            "  <tabledef allowSchemaChanges=\"n\" alter=\"n\" create=\"y\" delolddata=\"n\" isView=\"n\" name=\"TEST_PARENT\">\n" + 
            "     <rowdef>\n" + 
            "        <columndef action=\"c\" limitSizeForIndex=\"n\" name=\"CONTENTID\">\n" + 
            "           <jdbctype>INTEGER</jdbctype>\n" + 
            "           <allowsnull>no</allowsnull>\n" + 
            "        </columndef>\n" + 
            "        <columndef action=\"c\" limitSizeForIndex=\"n\" name=\"TITLE\">\n" + 
            "           <jdbctype>VARCHAR</jdbctype>\n" + 
            "           <size>50</size>\n" + 
            "           <allowsnull>yes</allowsnull>\n" + 
            "        </columndef>\n" + 
            "        <columndef action=\"c\" limitSizeForIndex=\"n\" name=\"BODY\">\n" + 
            "           <jdbctype>CLOB</jdbctype>\n" + 
            "           <allowsnull>yes</allowsnull>\n" + 
            "        </columndef>         \n" + 
            "     </rowdef>\n" + 
            "     <primarykey action=\"c\">\n" + 
            "        <name>CONTENTID</name>\n" + 
            "     </primarykey>\n" + 
            "  </tabledef>\n" + 
            "  <tabledef allowSchemaChanges=\"n\" alter=\"n\" create=\"y\" delolddata=\"n\" isView=\"n\" name=\"TEST_CHILD1\">\n" + 
            "     <rowdef>\n" + 
            "        <columndef action=\"c\" limitSizeForIndex=\"n\" name=\"CONTENTID\">\n" + 
            "           <jdbctype>INTEGER</jdbctype>\n" + 
            "           <allowsnull>no</allowsnull>\n" + 
            "        </columndef>\n" + 
            "        <columndef action=\"c\" limitSizeForIndex=\"n\" name=\"ROWID\">\n" + 
            "           <jdbctype>INTEGER</jdbctype>\n" + 
            "           <allowsnull>no</allowsnull>\n" + 
            "        </columndef>\n" + 
            "        <columndef action=\"c\" limitSizeForIndex=\"n\" name=\"DESCRIPTION\">\n" + 
            "           <jdbctype>VARCHAR</jdbctype>\n" + 
            "           <size>50</size>\n" + 
            "           <allowsnull>yes</allowsnull>\n" + 
            "        </columndef>\n" + 
            "     </rowdef>\n" + 
            "     <primarykey action=\"c\">\n" + 
            "        <name>ROWID</name>\n" + 
            "     </primarykey>\n" + 
            "     <foreignkey action=\"c\">\n" + 
            "        <fkColumn>\n" + 
            "           <name>CONTENTID</name>\n" + 
            "           <externalTable>TEST_PARENT</externalTable>\n" + 
            "           <externalColumn>CONTENTID</externalColumn>\n" + 
            "        </fkColumn>\n" + 
            "     </foreignkey>\n" + 
            "  </tabledef>\n" + 
            "  <tabledef allowSchemaChanges=\"n\" alter=\"n\" create=\"y\" delolddata=\"n\" isView=\"n\" name=\"TEST_CHILD2\">\n" + 
            "     <rowdef>\n" + 
            "        <columndef action=\"c\" limitSizeForIndex=\"n\" name=\"CONTENTID\">\n" + 
            "           <jdbctype>INTEGER</jdbctype>\n" + 
            "           <allowsnull>no</allowsnull>\n" + 
            "        </columndef>\n" + 
            "        <columndef action=\"c\" limitSizeForIndex=\"n\" name=\"ROWID\">\n" + 
            "           <jdbctype>INTEGER</jdbctype>\n" + 
            "           <allowsnull>no</allowsnull>\n" + 
            "        </columndef>\n" + 
            "        <columndef action=\"c\" limitSizeForIndex=\"n\" name=\"REGION\">\n" + 
            "           <jdbctype>INTEGER</jdbctype>\n" + 
            "           <allowsnull>yes</allowsnull>\n" + 
            "        </columndef>\n" + 
            "     </rowdef>\n" + 
            "     <primarykey action=\"c\">\n" + 
            "        <name>ROWID</name>\n" + 
            "     </primarykey>\n" + 
            "     <foreignkey action=\"c\">\n" + 
            "        <fkColumn>\n" + 
            "           <name>CONTENTID</name>\n" + 
            "           <externalTable>TEST_PARENT</externalTable>\n" + 
            "           <externalColumn>CONTENTID</externalColumn>\n" + 
            "        </fkColumn>\n" + 
            "     </foreignkey>\n" + 
            "  </tabledef>\n" + 
            "</tabledefset>");

      bindings.bind("$sys.template",tempt.getTemplate());
      item.setBindings(bindings.getVars());
      item.setTemplate(tempt);
      IPSAssemblyResult result = dbassembler.assembleSingle(item);

      String resultstring = new String(result.getResultData());
      
      File temp = getTempXmlFile();
      FileWriter w = new FileWriter(temp);
      w.write(resultstring);
      w.close();
      
      compareXmlDocs(ms_test_out,temp);
      
      deleteTmpFiles();
   }

   @SuppressWarnings("unchecked")
   private PSJexlEvaluator setupBindings(Document doc)
   {
      PSJexlEvaluator eval = new PSJexlEvaluator();

      Element el = doc.getDocumentElement();
      eval.bind("$db.database", el.getAttribute("dbname"));
      eval.bind("$db.drivertype", el.getAttribute("drivertype"));
      eval.bind("$db.origin", el.getAttribute("origin"));
      eval.bind("$db.resource", el.getAttribute("resourceName"));

      NodeList tables = doc.getElementsByTagName("table");
      // Only one
      processTable(tables.item(0), -1, eval);

      return eval;
   }

   private void processTable(Node el, int index, PSJexlEvaluator eval)
   {
      NodeList children = el.getChildNodes();
      int count = children.getLength();
      int childindex = 0;
      int rowindex = 0;
      
      String name = ((Element) el).getAttribute("name");
      if (index < 0)
         eval.bind("$db.parent",name);
      else
         eval.bind("$db.child[" + index + "]",name);

      for (int i = 0; i < count; i++)
      {
         Node child = children.item(i);
         if (! (child instanceof Element)) continue;
         String elname = child.getNodeName();
         if (elname.equals("row"))
         {
            NodeList columns = child.getChildNodes();
            int colcount = columns.getLength();
            for (int j = 0; j < colcount; j++)
            {
               Node col = columns.item(j);
               if (! (col instanceof Element)) continue;
               Element colel = (Element) col;
               
               String colelname = col.getNodeName();

               if (colelname.equals("column"))
               {
                  String columnname = colel.getAttribute("name");
                  String colvalue = colel.getTextContent().trim();
                  if (index < 0)
                  {
                     eval.bind("$row." + columnname, colvalue);
                  }
                  else
                  {
                     eval.bind("$child[" + index + "]." + columnname + "["
                           + rowindex + "]", colvalue);
                  }
               }
               else if (colelname.equals("childtable"))
               {
                  processTable(col, childindex++, eval);
               }
            }
            rowindex++;
         }

      }
   }

   @Override
   protected String getFilePrefix()
   {
      return "dbassembler";
   }
}
