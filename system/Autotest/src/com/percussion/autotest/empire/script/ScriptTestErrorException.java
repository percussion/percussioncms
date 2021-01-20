/*[ ScriptTestErrorException.java ]********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.autotest.empire.script;

/**
 * Internal exception to signal that an unexpected error occurred
 * during a test case.
 */
public class ScriptTestErrorException extends Exception
{
   public ScriptTestErrorException(String msg)
   {
      super(msg);
   }
}
