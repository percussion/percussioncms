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
package com.percussion.services.contentmgr.impl.jsrdata;

import com.percussion.services.contentmgr.data.PSContentNode;
import com.percussion.utils.jsr170.PSMultiProperty;
import com.percussion.utils.jsr170.PSProperty;
import com.percussion.utils.jsr170.PSValueFactory;

import java.util.ArrayList;
import java.util.Collection;

import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.Value;

import junit.framework.TestCase;

/**
 * Test the property objects used to implement JSR-170
 * 
 * @author dougrand
 */
public class PSPropertyTest extends TestCase
{
   PSValueFactory fact = new PSValueFactory();

   /**
    * Useful static class for testing functionality of the property accessor
    * class
    */
   public static class TestMappingClass
   {
      long mi_lval = 123;
      double mi_dval = 31.3;
      String mi_sval = "How now brown cow";
      
      /**
       * Ctor
       */
      public TestMappingClass()
      {
         //
      }
      
      /**
       * Ctor
       * @param ival integer value to use for this test class, aka length
       * @param dval double value to use for this test class, aka density
       * @param sval string value to use for this test class, aka label
       */
      public TestMappingClass(long ival, double dval, String sval)
      {
         mi_lval = ival;
         mi_dval = dval;
         mi_sval = sval;
      }
      
      /**
       * Return the length value, from the initial ival in the ctor
       * @return the length
       */
      public long getLength()
      {
         return mi_lval;
      }
      
      /**
       * Return the density value, from the initial dval in the ctor
       * @return the density 
       */
      public double getDensity()
      {
         return mi_dval;
      }
      
      /**
       * Return the label value, from the initial sval in the ctor
       * @return the label
       */
      public String getLabel()
      {
         return mi_sval;
      }
   }
   
   /**
    * The multi mapping test class for testing the property accessor classes
    */
   public static class TestMultiMappingClass
   {
      Collection<Long> mi_lval = null;
      Collection<Double> mi_dval = null;
      Collection<String> mi_sval = null;
     
      
      /**
       * Ctor
       * @param ival integer values to use for this test class, aka length
       * @param dval double value to use for this test class, aka density
       * @param sval string value to use for this test class, aka labels
       */
      public TestMultiMappingClass(Collection<Long> ival,
            Collection<Double> dval, Collection<String> sval)
      {
         mi_lval = ival;
         mi_dval = dval;
         mi_sval = sval;
      }
      
      /**
       * Return the length values, from the initial ival collection in the ctor
       * @return the collection of length values
       */
      public Collection<Long> getLength()
      {
         return mi_lval;
      }
      
      /**
       * Return the density values, from the initial dval collection in the ctor
       * @return the collection of density values
       */
      public Collection<Double> getDensity()
      {
         return mi_dval;
      }
      
      /**
       * Return the label values, from the initial sval collection in the ctor
       * @return the collection of label values
       */
      public Collection<String> getLabel()
      {
         return mi_sval;
      }
   }   
   
   /**
    * Test various methods on the Property interface
    * @throws Exception
    */
   public void testMethods() throws Exception
   {
      Object val = new TestMappingClass();
      PSContentNode dummy = new PSContentNode(null, "root", null, null, null, null);
      PSProperty lprop = new PSProperty("length", dummy, val, null);
      PSProperty dprop = new PSProperty("density", dummy, val, null);
      PSProperty labelprop = new PSProperty("label", dummy, val, null);
      dummy.addProperty(lprop);
      dummy.addProperty(dprop);
      dummy.addProperty(labelprop);
      
      assertEquals(1, lprop.getDepth());
      assertEquals("/root/length", lprop.getPath());
      assertEquals(dummy, lprop.getAncestor(0));
      assertEquals(lprop, lprop.getAncestor(1));
      assertEquals(123, lprop.getLong());
      assertEquals(123.0, lprop.getDouble());
      assertEquals(fact.createValue(123), lprop.getValue());
      assertEquals("123", lprop.getString());
      assertEquals(false, lprop.getBoolean());
      assertEquals(PropertyType.LONG, lprop.getType());
      
      assertEquals(1, dprop.getDepth());
      assertEquals("/root/density", dprop.getPath());
      assertEquals(dummy, dprop.getAncestor(0));
      assertEquals(dprop, dprop.getAncestor(1));
      assertEquals(31.3, dprop.getDouble());
      assertEquals(fact.createValue(31.3), dprop.getValue());
      assertEquals("31.3", dprop.getString());
      assertEquals(false, dprop.getBoolean());
      assertEquals(PropertyType.DOUBLE, dprop.getType());  
      
      assertEquals("label", labelprop.getName());
      assertEquals("How now brown cow", labelprop.getString());
      assertEquals(fact.createValue("How now brown cow"), labelprop.getValue());
   }
   
   /**
    * Test multi property
    * @throws Exception
    */
   @SuppressWarnings("unchecked")
   public void testMulti() throws Exception
   {
      Collection<Long> longs = new ArrayList<Long>();
      Collection<Double> floats = new ArrayList<Double>();
      Collection<String> strings = new ArrayList<String>();
      longs.add(100L);
      longs.add(200L);
      longs.add(300L);
      floats.add(10.1);
      floats.add(10.2);
      floats.add(10.3);
      strings.add("One");
      strings.add("Two");
      strings.add("Three");
      
      TestMultiMappingClass tmm = 
         new TestMultiMappingClass(longs, floats, strings);
      
      PSContentNode dummy = new PSContentNode(null, "root", null, null, null, null);
      dummy.addProperty(new PSMultiProperty("rx:length", dummy, tmm));
      dummy.addProperty(new PSMultiProperty("rx:density", dummy, tmm));
      dummy.addProperty(new PSMultiProperty("rx:label", dummy, tmm));
      
      Property p = dummy.getProperty("length");
      assertNotNull(p);
      long l[] = p.getLengths();
      assertEquals(3, l.length);
      assertEquals(3, l[0]);
      assertEquals(3, l[1]);
      assertEquals(3, l[2]);
      Value v[] = p.getValues();
      assertEquals(100, v[0].getLong());
      assertEquals(200, v[1].getLong());
      assertEquals(300, v[2].getLong());
      
      p = dummy.getProperty("density");
      assertNotNull(p);
      l = p.getLengths();
      assertEquals(3, l.length);
      assertEquals(4, l[0]);
      assertEquals(4, l[1]);
      assertEquals(4, l[2]);
      v = p.getValues();
      assertEquals(10.1, v[0].getDouble());
      assertEquals(10.2, v[1].getDouble());
      assertEquals(10.3, v[2].getDouble());

      p = dummy.getProperty("label");
      assertNotNull(p);
      l = p.getLengths();
      assertEquals(3, l.length);
      assertEquals(3, l[0]);
      assertEquals(3, l[1]);
      assertEquals(5, l[2]);
      v = p.getValues();
      assertEquals("One", v[0].getString());
      assertEquals("Two", v[1].getString());
      assertEquals("Three", v[2].getString());
      
   }
}
