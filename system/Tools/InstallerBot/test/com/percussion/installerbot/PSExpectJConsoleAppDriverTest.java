/******************************************************************************
 *
 * [ PSExpectJConsoleAppDriverTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installerbot;

import java.io.File;
import java.io.IOException;

import com.percussion.installerbot.PSConsoleAppDriverException;
import com.percussion.installerbot.PSExpectJConsoleAppDriver;

import junit.framework.TestCase;

/**
 * This is not comprehensive test of {@link PSExpectJConsoleAppDriver}
 * because this class is only an adapter to third-party library. 
 *
 * @author Andriy Palamarchuk
 */
public class PSExpectJConsoleAppDriverTest extends TestCase
{
   /**
    * Tests basic functionality
    */
   public void testBasics() throws IOException
   {
      final File file = File.createTempFile("test", "log");
      // bad command
      try
      {
         final PSExpectJConsoleAppDriver driver =
            new PSExpectJConsoleAppDriver();
         driver.setLogFile(file.getAbsolutePath());
         driver.launchApplication("bad command", 5);
         fail();
      }
      catch (PSConsoleAppDriverException success) {}
      file.delete();
   }
}
