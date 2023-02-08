/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
