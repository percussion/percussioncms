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


package com.percussion.server.cache;

import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.design.objectstore.PSRelationshipTest;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for the {@link PSContentItemDependencyTree} class.
 */
@Category(IntegrationTest.class)
public class PSContentItemDependencyTreeTest
{
   // see base class
   public PSContentItemDependencyTreeTest()
   {

   }
 
   /**
    * Test all public interfaces includeing the constructor.
    * @throws Exception if any errors occur.
    */
   @Test
   public void testPublicInterface() throws Exception
   {
      PSContentItemDependencyTree item = null;
      try
      {
         InputStream is = new FileInputStream(
            new File(RESOURCE_PATH + "relatedcontentitemset.xml"));
         Document doc = PSXmlDocumentBuilder.createXmlDocument(is, false);
         Element sourceNode = doc.getDocumentElement();
         assertNotNull(sourceNode);
         
         PSRelationshipSet relationships = 
            new PSRelationshipSet();
         
         PSRelationshipConfig config = 
            PSRelationshipTest.getConfigs().getConfigByNameOrCategory(
            PSRelationshipConfig.TYPE_ACTIVE_ASSEMBLY);
         assertNotNull(config);
         
         int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
            PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
         int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
            PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
         
         String data = null;
         Element node = null;
         PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
         
         node = tree.getNextElement(PSRelationship.XML_NODE_NAME, firstFlags);
         while (node != null)
         {
            PSRelationship relationship = new PSRelationship(
               (Element) tree.getCurrent(), null, null, config);
      
            relationships.add(relationship);
            
            node = tree.getNextElement(
               PSRelationship.XML_NODE_NAME, nextFlags);
         }         
         
         item = new PSContentItemDependencyTree(relationships);
            
         assertEquals(item.toString(), result_1);
      }
      catch (Exception e)
      {
         assertTrue("ctor failed", false);
      }
      
      try
      {
         List result = item.getDependentItems(1, 1, -1);
         System.out.println("\n\ngetDependentItems(1, 1, -1)\n" + 
            toString(result));

         assertEquals(toString(result), result_2);
      }
      catch (Exception e)
      {
         assertTrue("getDependentItems failed", false);
      }
      
      try
      {
         List result = item.getDependentItems(7, 1, -1);
         System.out.println("\n\ngetDependentItems(7, 1, -1)\n" + 
            toString(result));

         assertEquals(toString(result), result_3);
      }
      catch (Exception e)
      {
         assertTrue("getDependentItems failed", false);
      }
      
      try
      {
         List result = item.getDependentItems(6, 1, -1);
         System.out.println("\n\ngetDependentItems(6, 1, -1)\n" + 
            toString(result));

         assertEquals(toString(result), result_4);
      }
      catch (Exception e)
      {
         assertTrue("getDependentItems failed", false);
      }
      
      try
      {
         List result = item.getDependentItems(5, 1, -1);
         System.out.println("\n\ngetDependentItems(5, 1, -1)\n" + 
            toString(result));

         assertEquals(toString(result), result_5);
      }
      catch (Exception e)
      {
         assertTrue("getDependentItems failed", false);
      }

      try
      {
         List result = item.getDependentItems(-1, -1, 100);
         System.out.println("\n\ngetDependentItems(-1, -1, 100)\n" + 
            toString(result));

         assertEquals(toString(result), result_6);
      }
      catch (Exception e)
      {
         assertTrue("getDependentItems failed", false);
      }

      try
      {
         List result = item.updateDependency(1, 4, 2, 5, 100, new HashMap());
         System.out.println("\n\nupdateDependency(1, 4, 2, 5, 100)\n" + 
            toString(result));

         assertEquals(toString(result), result_7);
      }
      catch (Exception e)
      {
         assertTrue("getDependentItems failed", false);
      }
      
      try
      {
         List result = item.getDependentItems(5, 1, -1);
         System.out.println("\n\ngetDependentItems(5, 1, -1)\n" + 
            toString(result));

         assertEquals(toString(result), result_8);
      }
      catch (Exception e)
      {
         assertTrue("getDependentItems failed", false);
      }

      try
      {
         List result = item.updateDependency(1, 4, 2, 5, 100, new HashMap());
         System.out.println("\n\nupdateDependency(1, 4, 2, 5, 100)\n" + 
            toString(result));

         assertEquals(toString(result), result_10);
      }
      catch (Exception e)
      {
         assertTrue("addDependency failed", false);
      }

      try
      {

         List result = item.addDependency(8, 2, 1, 8, 100, new HashMap());
         System.out.println("\n\naddDependency(8, 2, 1, 8, 100)\n" + 
            toString(result));

         assertEquals(toString(result), result_12);
      }
      catch (Exception e)
      {
         assertTrue("addDependency failed", false);
      }
      
      try
      {
         List result = item.getDependentItems(8, 1, -1);
         System.out.println("\n\ngetDependentItems(8, 1, -1)\n" + 
            toString(result));

         assertEquals(toString(result), result_13);
      }
      catch (Exception e)
      {
         assertTrue("getDependentItems failed", false);
      }
      
      try
      {
         List result = item.removeDependency(8, new HashMap());
         System.out.println("\n\nremoveDependency(8)\n" + 
            toString(result));

         assertEquals(toString(result), result_14);
      }
      catch (Exception e)
      {
         assertTrue("removeDependency failed", false);
      }
      
      try
      {
         List result = item.getDependentItems(8, 1, -1);
         System.out.println("\n\ngetDependentItems(8, 1, -1)\n" + 
            toString(result));

         assertEquals(toString(result), result_15);
      }
      catch (Exception e)
      {
         assertTrue("getDependentItems failed", false);
      }
   }
  
   /**
    * Returns a String representation for provided list.
    * 
    * @param list the list to create a String for, assumed not <code>null</code>.
    * @return the list as String, never <code>null</code>.
    */
   public String toString(List list)
   {
      StringBuilder buf = new StringBuilder("result(");
      for (int i=0; i<list.size(); i++)
      {
         String[] values = (String[]) list.get(i);
         buf.append(values[0] + ":" + values[1] + ", ");
      }
      buf.append(")");
         
      return buf.toString();
   }
   /**
    * Defines the path to the files used by this unit test, relative from the
    * E2 root.
    */
   private static final String RESOURCE_PATH =
      "/com/percussion/server/cache/";
    
   /**
    * Expected test results.
    */  
   private static final String result_1 = "Dependencies(key= 7 value= dependency=(7, 6, 3, 1, 100), )key= 6 value= dependency=(6, 4, 3, 1, 100), )key= 5 value= dependency=(5, 1, 4, 1, 100), )key= 4 value= dependency=(4, 2, 2, 1, 100), )key= 3 value= dependency=(3, 5, 1, 1, 100), )key= 2 value= dependency=(2, 3, 1, 1, 100), )key= 1 value= dependency=(1, 7, 7, 1, 100), )";
   private static final String result_2 = "result(7:1, 3:1, )";
   private static final String result_3 = "result(1:1, 3:1, )";
   private static final String result_4 = "result(7:1, 1:1, 3:1, )";
   private static final String result_5 = "result(7:1, 2:1, 4:1, 1:1, 3:1, )";
   private static final String result_6 = "result(7:1, 2:1, 4:1, 1:1, 3:1, )";
   private static final String result_7 = "result(7:1, 2:1, 1:1, 3:1, )";
   private static final String result_8 = "result(7:1, 2:1, 1:1, 4:2, 3:1, )";
   private static final String result_10 = "result(7:1, 2:1, 1:1, 3:1, )";
   private static final String result_12 = "result(7:1, 1:1, 3:1, )";
   private static final String result_13 = "result(7:1, 2:1, 1:1, 3:1, )";
   private static final String result_14 = "result(7:1, 2:1, 1:1, 3:1, )";
   private static final String result_15 = "result()";
}
