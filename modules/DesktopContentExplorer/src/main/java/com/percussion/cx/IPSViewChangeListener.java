/*[ IPSViewChangeListener.java ]********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.cx;

/**
 * The interface to represent the change in the data of the view that does not 
 * represent "the change". Interface is implemented by classes that are to be 
 * notified of a views data changes.
 */
public interface IPSViewChangeListener
{    
   /**
    * Notifies the listener as the data is changed/modified and provides the 
    * modified data object. 
    * 
    * @param data the modifed data, must be an instance of object supported by 
    * the view. 
    * 
    * @throws IllegalArgumentException if data is not valid.
    */
   public void viewDataChanged(Object data);   
}

