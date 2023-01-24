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
package com.percussion.design.objectstore;

import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.Map;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author dougrand
 *
 * JUnit test for the PSSearchConfig object.
 */
public class PSSearchConfigTest extends TestCase
{
   /**
    * See JUnit framework description for details.
    */
   public static TestSuite suite()
   {
      //include all tests by default
      return new TestSuite(PSSearchConfigTest.class);
   }

   /**
    * See JUnit framework description for details.
    */
   public PSSearchConfigTest(String name)
   {
      super(name);
   }

   /**
    * Tests the add, remove and remove all methods for handling custom 
    * properties.
    */
   public void testCustomProps()
   {
      PSSearchConfig sc = new PSSearchConfig();
      sc.addCustomProp("p1", "v1");
      sc.addCustomProp("p2", "v2");
      sc.addCustomProp("p3", "v3");
      assertTrue(sc.getCustomProp("p2").equals("v2"));

      //test case sensitivity      
      sc.addCustomProp("P2", "alpha");
      assertTrue(sc.getCustomProp("p2").equals("v2"));
      
      //test that returned map is clone
      Map props = sc.getCustomProps();
      assertTrue(props.size() == 4);
      props.put("p2", "beta");
      assertTrue(sc.getCustomProp("p2").equals("v2"));
      
      sc.removeCustomProp("p2");
      assertTrue(sc.getCustomProp("p2") == null);

      assertTrue(sc.getCustomProp("p1").equals("v1"));      
      sc.removeAllCustomProps();
      assertTrue(sc.getCustomProp("p1") == null);      
   }

   /**
    * Creates 2 objects and changes each property; first on config 1, 
    * verifying they are not equal, then on config 2, verifying they are equal.
    * Every time equals is checked, the hashcode is checked as well.
    */
   public void testEquals()
   {
      PSSearchConfig sc = new PSSearchConfig();
      PSSearchConfig copy = new PSSearchConfig(sc);

      assertTrue(sc.equals(copy));
      assertTrue(sc.hashCode() == copy.hashCode());
      
      copy.setAdminMaster(!sc.isAdminMaster());     
      assertTrue(!sc.equals(copy));
      sc.setAdminMaster((copy.isAdminMaster()));
      assertTrue(sc.equals(copy));
      assertTrue(sc.hashCode() == copy.hashCode());
      
      copy.setFtsEnabled(!sc.isFtsEnabled());     
      assertTrue(!sc.equals(copy));
      sc.setFtsEnabled((copy.isFtsEnabled()));
      assertTrue(sc.equals(copy));
      assertTrue(sc.hashCode() == copy.hashCode());
      
      copy.addCustomProp("p1", "v1");
      copy.addCustomProp("p2", "v2");     
      assertTrue(!sc.equals(copy));
      //verify order independence
      sc.addCustomProp("p2", "v2");     
      sc.addCustomProp("p1", "v1");
      assertTrue(sc.equals(copy));
      assertTrue(sc.hashCode() == copy.hashCode());
   }
   
   /**
    * Tests the <code>toXml</code> and <code>fromXml</code> methods.
    * 
    * @throws Exception
    */
   public void testPersistence() throws Exception
   {
      // compare w/ all default values
      PSSearchConfig sc = new PSSearchConfig();
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element el = sc.toXml(doc);
      
      PSSearchConfig compare = new PSSearchConfig();
      compare.fromXml(el, null, null);
      
      assertTrue(sc.equals(compare));
      assertTrue(sc.hashCode() == compare.hashCode());
      
      //compare w/ non-default values
      flipIt(sc);
      el = sc.toXml(doc);
      compare = new PSSearchConfig();
      compare.fromXml(el, null, null);
      assertTrue(sc.equals(compare));
   }
   
   /**
    * Flips all boolean bits in sc and sets some values for host, port and 
    * 2 custom properties. These values differ each time this method is called
    * by maintaining a class counter and using it as part of each value.
    * 
    * @param sc Assumed not <code>null</code>.
    */
   private void flipIt(PSSearchConfig sc)
   {
      sc.setAdminMaster(!sc.isAdminMaster());
      sc.setFtsEnabled(!sc.isFtsEnabled());
      int count = ms_counter++;
      sc.addCustomProp("propname"  + count, "propvalue" + count);
      sc.addCustomProp("propname" + (count+1), "propvalue" + (count+1));  
   }
   
   /**
    * Used to generate unique values. Starts at 1. Increment after each use.
    */
   private static int ms_counter = 1;
}
