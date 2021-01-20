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
package com.percussion.cms.objectstore;

import com.percussion.design.objectstore.PSLocator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test the public interface documented for the active assembly processor proxy.
 * The processor functionality itself is not tested here but in autotests.
 */
public class PSActiveAssemblyProcessorProxyTest extends TestCase
{
   // see base class for documentation
   public PSActiveAssemblyProcessorProxyTest(String name)
   {
      super(name);
   }
   
   /**
    * Test all realtionship processor proxy constructors contracts.
    * 
    * @throws Exception for any error.
    */
   public void testConstructors() throws Exception
   {
      String type = "type";
      Object context = new Object();
      
      PSActiveAssemblyProcessorProxy processor = null;

      // avoid eclipse warning
      if (processor == null);
      
      // test valid constructor
      Exception exception = null;
      try
      {
         processor = new PSActiveAssemblyProcessorProxy(type, null);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception == null);
      
      /*
       * Test valid constructor: returns null pointer because we use an 
       * invalid type.
       */
      exception = null;
      try
      {
         processor = new PSActiveAssemblyProcessorProxy(type, context);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof NullPointerException);
   }
   
   /**
    * Test all public methods contracts.
    * 
    * @throws Exception for any error.
    */
   public void testPublicAPI() throws Exception
   {
      PSActiveAssemblyProcessorProxy processor = 
            new PSActiveAssemblyProcessorProxy("type", null);
      
      PSAaRelationshipList list = new PSAaRelationshipList();

      // test valid parameters: newSlotRelations=null
      Exception exception = null;
      try
      {
         processor.addSlotRelationships(null, -1);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      // test valid parameters: newSlotRelations=empty
      exception = null;
      try
      {
         processor.addSlotRelationships(list, -1);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      // test valid parameters: locator=null
      exception = null;
      try
      {
         processor.getItemSlots(null);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      // test valid parameters: locator=null
      exception = null;
      try
      {
         processor.getItemVariants(null);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      // test valid parameters: locator=null
      exception = null;
      try
      {
         processor.getRelationshipConfig(null);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      // test valid parameters: locator=null
      exception = null;
      try
      {
         processor.getSlotItems(null, null, -1);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      // test valid parameters: slot=null
      exception = null;
      try
      {
         processor.getSlotItems(new PSLocator(), null, -1);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      // test valid parameters: locator=null
      exception = null;
      try
      {
         processor.getSlotRelationships(null, null, -1);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      // test valid parameters: slot=null
      exception = null;
      try
      {
         processor.getSlotRelationships(new PSLocator(), null, -1);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);
      
      // test valid parameters: slotRelations=null
      exception = null;
      try
      {
         processor.reArrangeSlotRelationships(null, -1);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      // test valid parameters: slotRelations=empty
      exception = null;
      try
      {
         processor.reArrangeSlotRelationships(list, -1);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);
      
      // test valid parameters: existingSlotRelations=null
      exception = null;
      try
      {
         processor.removeSlotRelations(null);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      // test valid parameters: existingSlotRelations=empty
      exception = null;
      try
      {
         processor.removeSlotRelations(list);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);
      
      // test valid parameters: relationships=null
      exception = null;
      try
      {
         processor.save(null);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      // test valid parameters: relationships=empty
      exception = null;
      try
      {
         processor.save(list);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);

      // test valid parameters: aaRel=empty
      exception = null;
      try
      {
         processor.validateAaRelationship(null);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof IllegalArgumentException);
   }
   
   // see base class for documentation
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      
      suite.addTest(new PSActiveAssemblyProcessorProxyTest("testConstructors"));
      suite.addTest(new PSActiveAssemblyProcessorProxyTest("testPublicAPI"));
      
      return suite;
   }
}
