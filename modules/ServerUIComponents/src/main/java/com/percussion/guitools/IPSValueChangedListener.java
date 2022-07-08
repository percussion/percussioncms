/******************************************************************************
*
* [ IPSValueChangedListener.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.guitools;

/**
 * Used by any AWT/Swing component to indicate the a value has changed.
 */
public interface IPSValueChangedListener
{
   /**
    * Called when a value is changed by the source component
    * @param event never <code>null</code>.
    */
   public void valueChanged(PSValueChangedEvent event);
}
