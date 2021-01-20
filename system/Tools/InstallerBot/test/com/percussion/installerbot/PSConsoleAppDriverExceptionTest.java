/*******************************************************************************
 *
 * [ PSConsoleAppDriverExceptionTest.java} ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *******************************************************************************/


package com.percussion.installerbot;

import org.apache.commons.lang.StringUtils;

import com.percussion.installerbot.PSConsoleAppDriverException;

import junit.framework.TestCase;

/**
 * @author Andriy Palamarchuk
 */
public class PSConsoleAppDriverExceptionTest extends TestCase
{
   /**
    * Tests basic functionality.
    */
   public void testBasics() {
      assertSame(MESSAGE, new PSConsoleAppDriverException(MESSAGE).getMessage());
      {
         final Exception e = new Exception(MESSAGE);
         assertSame(e, new PSConsoleAppDriverException(e).getCause());
         assertTrue(StringUtils.contains(
               new PSConsoleAppDriverException(e).getMessage(), MESSAGE));
      }
      {
         final PSConsoleAppDriverException e =
            new PSConsoleAppDriverException(MESSAGE);
         assertFalse(e.isTimeOut());
         e.setTimeOut(false);
         assertFalse(e.isTimeOut());
      }
   }

   /**
    * Sample message.
    */
   private static final String MESSAGE = "Message ";
}
