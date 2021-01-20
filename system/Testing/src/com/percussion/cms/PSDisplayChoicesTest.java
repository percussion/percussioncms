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

