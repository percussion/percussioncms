/*[ ScriptTimeoutException.java ]**********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.autotest.empire.script;

/**
 * Exception to signal that the interpreter timed out
 * while waiting for something.
 */
public class ScriptTimeoutException extends Exception
{
   public ScriptTimeoutException(String msg)
   {
      super(msg);
   }
}
