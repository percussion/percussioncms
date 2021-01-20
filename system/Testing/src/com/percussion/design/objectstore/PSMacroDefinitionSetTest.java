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

import com.percussion.xml.PSXmlDocumentBuilder;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.w3c.dom.Document;

/**
 * Tests the XML serialisation functionality for 
 * <code>PSMacroDefinitionSet</code> and <code>PSMacroDefinition</code> 
 * objectstore classes.
 */
public class PSMacroDefinitionSetTest extends TestCase
{
   /**
    * Construct a new test for the supplied name.
    * 
    * @param name the test name, assumed not <code>null</code> or empty.
    */
   public PSMacroDefinitionSetTest(String name)
   {
      super(name);
   }

   /**
    * Test XML serialization for code>PSMacroSet</code> and 
    * <code>PSMacro</code>.
    * 
    * @throws Exception for any error.
    */
   public void testXml() throws Exception
   {
      PSMacroDefinition macro1 = new PSMacroDefinition("$macro1", 
         PSMacroDefinition.class.getName());
      macro1.setDescription("description 1");
      
      PSMacroDefinition macro2 = new PSMacroDefinition("$macro2", 
         PSMacroDefinitionSet.class.getName());
      
      PSMacroDefinitionSet macros1 = new PSMacroDefinitionSet();
      macros1.add(macro1);
      macros1.add(macro2);
      
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      System.out.println("macroDefinitionSet 1:\n" +
         PSXmlDocumentBuilder.toString(macros1.toXml(doc)));
      
      PSMacroDefinitionSet macros2 = new PSMacroDefinitionSet(
         macros1.toXml(doc));
      System.out.println("macroDefinitionSet 2:\n" +
         PSXmlDocumentBuilder.toString(macros2.toXml(doc)));
         
      assertTrue(macros1.equals(macros2));
   }

   /**
    * The test suit that specifies all axecuted tests.
    * 
    * @return the test suit, never <code>null</code>.
    */
   public static Test suite()
   {
      TestSuite suite = new TestSuite();

      suite.addTest(new PSMacroDefinitionSetTest("testXml"));

      return suite;
   }
}