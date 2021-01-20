/*[ ScriptInterruptedException.java ]******************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.autotest.empire.script;

/**
 * Exception to signal that the script execution was
 * interrupted (e.g., the user told the client to shut down).
 */
public class ScriptInterruptedException extends Exception
{
   public ScriptInterruptedException(String msg)
   {
      super(msg);
   }
}

