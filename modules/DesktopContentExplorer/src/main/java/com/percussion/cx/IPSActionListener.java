/*[ IPSActionListener.java ]*************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.cx;

/**
 * The interface to define a listener that listens to the processing of an 
 * action. (Typically menu actions).
 */
public interface IPSActionListener
{
   /**
    * Notification event for the listener to inform the current executing action
    * is completed. It informs the location which should be refreshed.
    * 
    * @param actionEvent the event that describes the action to take by the 
    * listeners, may not be <code>null</code>
    * 
    * @throws IllegalArgumentException if actionEvent is <code>null</code>.
    */
   public void actionExecuted(PSActionEvent actionEvent);
   
   /**
    * Notification event for the listener to inform that an action is initiated
    * and may take some time to execute the action. Provides a monitor object to
    * monitor the action process. 
    * 
    * @param processMonitor the monitor may not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if processMonitor is <code>null</code>.
    */
   public void actionInitiated(PSProcessMonitor processMonitor);
   
}


