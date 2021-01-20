/*[ ScriptTestFailedException.java ]*******************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.autotest.empire.script;

/**
 * Exception to signal that a test case failed
 */
public class ScriptTestFailedException extends Exception
{
   public ScriptTestFailedException(String msg)
   {
      super(msg);
   }
}
