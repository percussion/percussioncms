/******************************************************************************
 *
 * [ PSAAClientActionFactoryTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.content.ui.aa.actions;

import junit.framework.TestCase;

/**
 * Unit Test for the AA Client action factory.
 */
public class PSAAClientActionFactoryTest extends TestCase
{

   public void testGetAction()
   {
      PSAAClientActionFactory factory = PSAAClientActionFactory.getInstance();
      assertNotNull(factory);
      
      Object obj = factory.getAction("__WillNeverExistTestClass");
      assertNull(obj);
      obj = factory.getAction("Move");
      assertNotNull(obj);
      
      String className = obj.getClass().getName();
      String expectedName = "com.percussion.content.ui.aa.actions.impl.PSMoveAction";
      assertEquals(className, expectedName);
   }
   
}
