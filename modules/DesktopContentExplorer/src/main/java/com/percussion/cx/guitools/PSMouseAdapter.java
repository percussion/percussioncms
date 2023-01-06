/******************************************************************************
*
* [ PSMouseAdapter.java ]
* 
* COPYRIGHT (c) 1999 - 2004 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
******************************************************************************/
package com.percussion.cx.guitools;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * This class is used to get around a bug/change introduced in Java 1.4.2_05
 * where clicking the mouse button when the control key is depressed will
 * no longer produce a "mouseClicked" event. This is a problem for Apple
 * Mac's as they use the control/click as Microsoft Windows uses a right-click.
 * 
 * You cannot implement the "mousePressed", "mouseReleased", or "mouseClicked"
 * methods when extending this class, but should instead implement
 * the "mouseWasPressed", "mouseWasReleased", or "mouseWasClicked" methods
 */
public abstract class PSMouseAdapter extends MouseAdapter
{

   /* (non-Javadoc)
    * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
    */
   public final void mousePressed(MouseEvent e)
   {
      m_lastPressedComponent = e.getComponent();
      mouseWasPressed(e);
   }
   /* (non-Javadoc)
    * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
    */
   public final void mouseReleased(MouseEvent e)
   {
      mouseWasReleased(e);
      if(m_lastPressedComponent != null 
         && m_lastPressedComponent == e.getComponent())
      {
         mouseWasClicked(e);
         m_lastPressedComponent = null;
      }
   }
   /* (non-Javadoc)
    * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
    */
   public final void mouseClicked(MouseEvent e)
   {
      // Does nothing and cannot be implemented
   }
   
   /**
    * This method will be called when a mouse button is pressed.
    * @param e the <code>MouseEvent</code> that was fired, assumed
    * not <code>null</code>
    */
   public void mouseWasPressed(MouseEvent e)
   {
      // Does nothing
   }
   
   /**
    * This method will be called when a mouse button is released.
    * @param e the <code>MouseEvent</code> that was fired, assumed
    * not <code>null</code>
    */   
   public void mouseWasReleased(MouseEvent e)
   {
      // Does nothing
   }
   
   /**
    * This method will be called when a mouse button is clicked.
    * @param e the <code>MouseEvent</code> that was fired, assumed
    * not <code>null</code>
    */   
   public void mouseWasClicked(MouseEvent e)
   {
      // Does nothing   
   }
   
   /**
    *  The last component the mouse was on when it was pressed,
    *  may be <code>null</code>.
    */   
   private Component m_lastPressedComponent;
}
