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
package com.percussion.install;

import static org.junit.Assert.assertTrue;

import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class PSUpgradePluginUpdateCategoryTreeTest
{
   @Test
   public void test() throws Exception
   {
      FileInputStream in = null;
      try
      {
         PSUpgradePluginUpdateCategoryTree plugin = new PSUpgradePluginUpdateCategoryTree();
         plugin.setLogger(System.out);
         File catFile = new File("UnitTestResources/com/percussion/rxupgrade/CategoryTree.xml");
         in = new FileInputStream(catFile);
         Document doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
         doc = plugin.updateDocument(doc);
         NodeList nodes = doc.getElementsByTagName("Node");
         for(int i=0; i<nodes.getLength();i++)
         {
            Element node = (Element) nodes.item(i);
            String label = StringUtils.defaultString(node.getAttribute("label"));
            String id = StringUtils.defaultString(node.getAttribute("id"));
            assertTrue(label.equals(id));
         }  
      }
      finally
      {
         if(in != null)
         {
            in.close();
         }
      }
   }
   
}
