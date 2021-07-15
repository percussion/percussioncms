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
package com.percussion.services.assembly.jexl;

import java.util.List;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.junit.experimental.categories.Category;

/**
 * Test keyword utilities
 * 
 * @author dougrand
 */
@Category(IntegrationTest.class)
public class PSKeywordUtilsTest extends ServletTestCase
{
   /**
    * Test retrieval code
    */
   public void testRetrieval() 
   {
      PSKeywordUtils ku = new PSKeywordUtils();
      List<String[]> choices = ku.keywordChoices("Publishable");
      assertNotNull(choices);
      assertEquals(4, choices.size());
   }
   
   /**
    * Test formatting of html options
    */
   public void testChoices() 
   {
      PSKeywordUtils ku = new PSKeywordUtils();
      String compare = "<OPTION value='n'>Unpublish</OPTION>\n" + 
            "<OPTION value='y'>Publish</OPTION>\n" + 
            "<OPTION value='i'>Ignore</OPTION>\n" + 
            "<OPTION value='u' selected=\'true\'>Archive</OPTION>\n";
      String result = ku.keywordSelectChoices("Publishable","u");
      assertEquals(compare,result);
   }
   
   /**
    * Test label retrieval
    */
   public void testLabel()
   {
      PSKeywordUtils ku = new PSKeywordUtils();
      
      assertEquals("Archive",ku.getLabel("Publishable","u"));
      assertEquals("",ku.getLabel("Publishable","z"));
   }
   
   
   /**
    * Test label retrieval
    */
   public void testLocaleLabel()
   {
      PSKeywordUtils ku = new PSKeywordUtils();
      
      assertEquals("Archive",ku.getLabel("Publishable","u","en_us"));
      assertEquals("",ku.getLabel("Publishable","z","en_us"));
   }   
   
   /**
    * Test choices from a content type field
    */
   public void testFieldChoices()
   {
      PSKeywordUtils ku = new PSKeywordUtils();
      String ctn = "rffGeneric";
      
      assertEquals("Normal", ku.getChoiceLabel(ctn, "usage", "N"));
      assertEquals("Landing Page", ku.getChoiceLabel(ctn, "usage", "L"));
   }
}
