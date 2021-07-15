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
package com.percussion.cms.objectstore.server.util;

import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.utils.testing.UnitTest;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;

import static org.junit.Assert.assertEquals;

/**
* Test the {@link PSFieldFinderUtil} class
*/
@Category(UnitTest.class)
public class PSFieldFinderUtilTest
{
  /**
   * Test the getting fields that use taxonomy id 1
   * This includes one field from a child table
   * 
   * @throws Exception if the test fails
   */
  @Test
  public void testGetTaxonomyFields() throws Exception
  { 
        PSItemDefinition srcDef = loadItemDefinition("PSFieldFinderUtilTest1.xml");
        List<String> fieldList = PSFieldFinderUtil.getFields(srcDef, "taxonomy_id", "1");
        
        List<String> testFieldList = new ArrayList<String>();
        testFieldList.add("taxonomy");
        // field in child table
        testFieldList.add("taxonomyfield2");
       
        assertEquals(testFieldList,fieldList); 
  }
  /**
   * Test the id 1 does not match for a different parameter
   * 
   * @throws Exception if the test fails
   */
  @Test
  public void testNotGetOtherFields() throws Exception
  { 
        PSItemDefinition srcDef = loadItemDefinition("PSFieldFinderUtilTest1.xml");
        List<String> fieldList = PSFieldFinderUtil.getFields(srcDef, "translation_id", "1");
        
        List<String> testFieldList = new ArrayList<String>();
       
        assertEquals(testFieldList,fieldList); 
  }
  
  /**
   * Test the case of non string literal param PSXSingleHtmlParameter
   * 
   * @throws Exception if the test fails
   */
  @Test
  public void testSingleHtmlParam() throws Exception
  { 
        PSItemDefinition srcDef = loadItemDefinition("PSFieldFinderUtilTest1.xml");
        List<String> fieldList = PSFieldFinderUtil.getFields(srcDef, "helptext", "PSXSingleHtmlParameter/testparam");
        
        List<String> testFieldList = new ArrayList<String>();
        testFieldList.add("otherfield");
        assertEquals(testFieldList,fieldList); 
  }
  
  /**
   * Load the item definition from the specified file.
   * 
   * @param fileName the file name, relative to the source code location,
   *    may be <code>null</code> or empty to use the default.
   * @return the item definition created from the specified file, never
   *    <code>null</code>.
   * @throws Exception for any error loading the item definition.
   */
  public static PSItemDefinition loadItemDefinition(String fileName) 
     throws Exception
  {
     InputStream in = null;

     try
     {
        if (StringUtils.isBlank(fileName))
           fileName = "itemDefinition.xml";
        
        in = PSFieldFinderUtilTest.class.getResourceAsStream(fileName);
        Document doc = PSXmlDocumentBuilder.createXmlDocument(in, false);

        return new PSItemDefinition(doc.getDocumentElement());
     }
     finally
     {
        if (in != null)
           try { in.close(); } catch (IOException e) { /* ignore */ }
     }
  }
}

