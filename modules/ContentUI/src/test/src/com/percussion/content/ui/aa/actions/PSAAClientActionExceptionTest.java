/******************************************************************************
 *
 * [ PSAAClientActionExceptionTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.content.ui.aa.actions;

import junit.framework.TestCase;

import java.lang.reflect.InvocationTargetException;

/**
 * @author Andriy Palamarchuk
 */
public class PSAAClientActionExceptionTest extends TestCase
{
   /**
    * {@link InvocationTargetException} requires special handling because it
    * does not return message from the exception it wraps.
    */
   public void testInvocationTargetExceptionHandling()
   {
      final String message = "Error Message!";
      final Throwable e = new Exception(message);
      
      assertEquals(message, e.getMessage());
      final InvocationTargetException ie = new InvocationTargetException(e);
      assertNull("Message from the nested exception is not returned",
            ie.getMessage());
      
      assertEquals(e.toString(),
            new PSAAClientActionException(ie).getMessage());
      assertEquals(e.toString(),
            new PSAAClientActionException(ie).getLocalizedMessage()); 
      final String message2 = "Error Message!";
      assertEquals(message2,
            new PSAAClientActionException(message2, ie).getMessage()); 
      assertEquals(message,
            new PSAAClientActionException(message2, ie).getCause().getMessage()); 
   }
}
