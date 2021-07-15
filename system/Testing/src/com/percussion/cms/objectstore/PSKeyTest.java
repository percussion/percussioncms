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

package com.percussion.cms.objectstore;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.xml.PSXmlDocumentBuilder;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Unit test class for the <code>PSKey</code> class.
 */
public class PSKeyTest extends TestCase
{
   /**
    * Construct this unit test
    *
    * @param name The name of this test.
    */
    public PSKeyTest(String name)
   {
      super(name);
   }

   /**
    * Test constructing this object using parameters
    *
    * @throws Exception If there are any errors.
    */
   public void testConstructor() throws Exception
   {

      String[] def1 = new String[] {"name1"};
      String[] val1 = new String[] {"value1"};
      String[] def2 = new String[] {"name1", "name2"};
      String[] val2 = new String[] {"value1", "value2"};
      int[] intVal1 = new int[] {11};
      int[] intVal2 = new int[] {11, 22};
      
      // these should work fine
      
      assertTrue(testCtorValid(def1, val1, true));
      assertTrue(testCtorValid(def1, intVal1, false));
      assertTrue(testCtorValid(def2, val2, true));
      assertTrue(testCtorValid(def2, intVal2, false));

      // should be a problem
      assertTrue(!testCtorValid(null, val1, true));
      assertTrue(!testCtorValid(def1, null, false));
      assertTrue(!testCtorValid(def1, val2, true));
      assertTrue(!testCtorValid(def2, val1, false));

      // create empty keys
      PSKey k1 = new PSKey(def1);
      PSKey k2 = new PSKey(def2);
      PSKey k3 = new PSKey(def2);
      
      assertTrue(! k1.equals(k2));
      assertTrue(k2.equals(k3));
   }

   /**
    * Tests the equals and to/from XML methods
    *
    * @throws Exception if there are any errors.
    */
   public void testEquals() throws Exception
   {
      String[] def1 = new String[] {"name2", "Name1"};
      String[] val1 = new String[] {"22", "11"};
      String[] def2 = new String[] {"name1", "Name2"};
      String[] val2 = new String[] {"11", "22"};
      int[] intVal2 = new int[] {11, 22};

      PSKey k1 = new PSKey(def1, val1, true);
      PSKey k2 = new PSKey(def2, val2, true); 
      PSKey k3 = new PSKey(def2, intVal2, true);
      
      //*** Testing equals
      
      // both definition and values have different order for k1 and k2,
      // but they are the same set of values
      assertTrue(k1.equals(k2)); 
      
      // k1 has String[] input, k3 has int[] input, both they should be same
      assertTrue(k1.equals(k3));
            
      //*** Testing XML
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element k1El = k1.toXml(doc);
      PSKey targetKey = new PSKey(k1El);
      
      assertTrue(k1.equals(targetKey));
      
      
      //PSKey fk = new PSKey(PSFolder.KEY_PARTS);
      //assertTrue(fk.isSameType(locator));

      //*** Testing clone      
      targetKey = (PSKey) k1.clone();
      assertTrue(k1.equals(targetKey));
   }

   /**
    * Tests the PSSimpleKey
    *
    * @throws Exception if there are any errors.
    */
   public void testSimpleKey() throws Exception
   {
      PSSimpleKey sk1 = new PSSimpleKey("simple", 12);
      String name = sk1.getKeyName();
      int value = sk1.getKeyValueAsInt();
      
      assertTrue(name.equals("simple"));
      assertTrue(value == 12);
   }
   
   /**
    * Tests the PSLocator
    *
    * @throws Exception if there are any errors.
    */
   public void testLocator() throws Exception
   {
      // testing isPersisted and not needGenerate
      PSLocator locator = new PSLocator(10, 1);
      
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      //System.out.println("locator: \n" + 
      //   PSXmlDocumentBuilder.toString(locator.toXml(doc)) );
               
      Element locatorEl = locator.toXml(doc);
      
      PSLocator targetLocator = new PSLocator(locatorEl);
      
      assertTrue(locator.equals(targetLocator));
      assertTrue(locator.getId() == 10);
      assertTrue(locator.getRevision() == 1);
      assertTrue(locator.isPersisted());
      assertTrue(! locator.needGenerateId());
      
      // testing not persisted and needGenerate
      locator = new PSLocator(10, 1, false);
      assertTrue(! locator.isPersisted());
      assertTrue( locator.needGenerateId());
      
      // testing persisted and not needGenerate
      locator = new PSLocator(10, 1, true);
      assertTrue(locator.isPersisted());
      assertTrue(! locator.needGenerateId());
      
      // testing empty locator
      locator = new PSLocator();
      assertTrue(! locator.isAssigned());
      assertTrue(! locator.isPersisted());
      assertTrue(locator.needGenerateId());
      
      locatorEl = locator.toXml(doc);
      targetLocator = new PSLocator(locatorEl);
      assertTrue(locator.equals(targetLocator));
      
      //System.out.println("locator: \n" + 
      //   PSXmlDocumentBuilder.toString(locator.toXml(doc)) );
   }
   
   /**
    * Constructs a <code>PSKey</code> object using the
    * supplied params and catches any exception.  For params,
    * see {@link PSKey} ctor.
    *
    * @return <code>true</code> if no exceptions were caught, <code>false</code>
    * otherwise.
    */
   private boolean testCtorValid(String[] def, Object value, boolean persisted)
   {
      try
      {
         PSKey key;
         
         if (value instanceof String[])
            key = new PSKey(def, (String[])value, persisted);
         else
            key = new PSKey(def, (int[])value, persisted);
      }
      catch (Exception ex)
      {
         return false;
      }

      return true;
   }

   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSKeyTest("testConstructor"));
      suite.addTest(new PSKeyTest("testEquals"));
      suite.addTest(new PSKeyTest("testSimpleKey"));
      suite.addTest(new PSKeyTest("testLocator"));
      
      return suite;
   }


   public static void main(String args[]) 
   {         
      junit.textui.TestRunner.run(PSKeyTest.class);
   }

}
