/*[ ScriptExecAbortedException.java ]******************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.autotest.empire.script;

/**
 * Exception to signal that execution has been aborted.
 * This may be ignored in some situations.
 */
public class ScriptExecAbortedException extends Exception
{
   public ScriptExecAbortedException(String msg)
   {
      super(msg);
   }
}
