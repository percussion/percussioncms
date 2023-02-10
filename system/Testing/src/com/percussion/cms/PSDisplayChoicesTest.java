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
package com.percussion.cms;

import com.percussion.design.objectstore.PSChoiceFilter;
import com.percussion.design.objectstore.PSChoiceFilter.DependentField;
import com.percussion.design.objectstore.PSEntry;
import com.percussion.design.objectstore.PSParam;
import com.percussion.design.objectstore.PSUrlRequest;
import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlDocumentBuilder;
import junit.framework.TestCase;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Test case for the {@link PSDisplayChoices} class.
 */
public class PSDisplayChoicesTest extends TestCase
{
   /**
    * Test the basic constructor
    * 
    * @throws Exception if the test fails.
    */
   public void testCtor() throws Exception
   {
      PSDisplayChoices choices = new PSDisplayChoices(null, null);
      assertFalse(choices.areChoicesLoaded());
      assertNull(choices.getChoiceFilter());
      assertNotNull(choices.getChoices());
      assertFalse(choices.getChoices().hasNext());
      
      // create choices
      int size = 3;
      List<PSEntry> entries = createEntries(size);
      PSChoiceFilter filter = createFilter();
      choices = new PSDisplayChoices(entries.iterator(), filter);
      
      assertTrue(choices.areChoicesLoaded());
      Iterator<PSEntry> iter = choices.getChoices();
      for (int i = 0; i < size; i++)
      {
         assertTrue(iter.hasNext());
         assertEquals(entries.get(i), iter.next());
      }
      
      assertEquals(choices.getChoiceFilter(), filter);
   }
   
   /**
    * Test equals and hashcode
    * 
    * @throws Exception if the test fails.
    */
   public void testEquals() throws Exception
   {
      PSDisplayChoices choices1 = new PSDisplayChoices(null, null);
      PSDisplayChoices choices2 = new PSDisplayChoices(null, null);
      assertEquals(choices1, choices2);
      assertEquals(choices1.hashCode(), choices2.hashCode());
      
      int size = 0;
      List<PSEntry> entries = createEntries(size);
      choices1 = new PSDisplayChoices(entries.iterator(), null);
      assertFalse(choices1.equals(choices2));
      choices2 = new PSDisplayChoices(entries.iterator(), null);
      assertEquals(choices1, choices2);
      assertEquals(choices1.hashCode(), choices2.hashCode());
      
      size = 2;
      entries = createEntries(size);
      choices1 = new PSDisplayChoices(entries.iterator(), null);
      assertFalse(choices1.equals(choices2));
      choices2 = new PSDisplayChoices(entries.iterator(), null);
      assertEquals(choices1, choices2);
      assertEquals(choices1.hashCode(), choices2.hashCode());
      
      PSChoiceFilter filter = createFilter();
      choices1 = new PSDisplayChoices(null, filter);
      assertFalse(choices1.equals(choices2));
      choices2 = new PSDisplayChoices(null, filter);
      assertEquals(choices1, choices2);
      assertEquals(choices1.hashCode(), choices2.hashCode());
      
      choices1 = new PSDisplayChoices(entries.iterator(), filter);
      assertFalse(choices1.equals(choices2));
      choices2 = new PSDisplayChoices(entries.iterator(), filter);
      assertEquals(choices1, choices2);
      assertEquals(choices1.hashCode(), choices2.hashCode());
   }
   
   /**
    * Test XML serialization.
    * 
    * @throws Exception if the test fails.
    */
   public void testXml() throws Exception
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      PSDisplayChoices choices = new PSDisplayChoices(null, null);
      assertEquals(choices, new PSDisplayChoices(choices.toXml(doc)));
      
      choices = new PSDisplayChoices(createEntries(0).iterator(), null);
      assertEquals(choices, new PSDisplayChoices(choices.toXml(doc)));
      
      choices = new PSDisplayChoices(null, createFilter());
      assertEquals(choices, new PSDisplayChoices(choices.toXml(doc)));
      
      choices = new PSDisplayChoices(createEntries(3).iterator(), 
         createFilter());
      assertEquals(choices, new PSDisplayChoices(choices.toXml(doc)));
   }
   
   /**
    * Create a list of entries
    * @param size the size, assumed >= 0
    * 
    * @return the list of entries, never <code>null</code>.
    */
   private List<PSEntry> createEntries(int size)
   {
      List<PSEntry> entries = new ArrayList<PSEntry>();
      for (int i = 0; i < size; i++)
      {
         entries.add(new PSEntry(String.valueOf(i), "Val_" + i));
      }

      return entries;
   }
   
   /**
    * Create a filter
    * 
    * @return the filter, never <code>null</code>.
    */
   private PSChoiceFilter createFilter()
   {
      PSCollection fields = new PSCollection(DependentField.class);
      fields.add(new DependentField("test", 
         PSChoiceFilter.DependentField.TYPE_REQUIRED));
      PSUrlRequest lookup = new PSUrlRequest("test", null, new PSCollection(
         PSParam.class));
      
      return new PSChoiceFilter(fields, lookup);      
   }
}

