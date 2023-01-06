/*[ IPSSelectionListener.java ]*************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.cx;

/**
 * The interface to represent the selection change in main view panel of the 
 * applet. Interface is implemented by classes that are to be notified of a 
 * selection change.
 */
public interface IPSSelectionListener
{
   /**
    * Notification event that the current selection has changed in main view.
    * 
    * @param selection the object that encapsulates the selection details, may
    * not be <code>null</code>
    * 
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public void selectionChanged(PSSelection selection);
}

