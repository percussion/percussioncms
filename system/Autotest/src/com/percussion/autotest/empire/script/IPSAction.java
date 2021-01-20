/*[ IPSAction.java ]***********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.autotest.empire.script;

/**
 * This is a wrapper class to encapsulate a script action that is to be
 * executed in a separate thread.
 *
 * @see EmpireGroupedRequestManager
 */
public abstract class IPSAction
{
   /**
    * Typically, this method will be overridden using an anonymous class to
    * execute the desired action.
    */
   public abstract void perform()
      throws Exception;
}
