/******************************************************************************
*
* [ PSValueChangedEvent.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
/**
 * 
 */
package com.percussion.guitools;

import java.awt.*;

/**
 * Event that is fired off when a value changes on an AWT/SWing
 * component.
 */
public class PSValueChangedEvent extends AWTEvent
{

  

   /**
    * @param event
    */
   public PSValueChangedEvent(Event event)
   {
      super(event);
   }

   /**
    * @param source
    * @param id
    */
   public PSValueChangedEvent(Object source, int id)
   {
      super(source, id);
   }
   
   /**
    * Machine generated serial version uid
    */
   private static final long serialVersionUID = 2116355251345021672L;

}
