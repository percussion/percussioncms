/******************************************************************************
 *
 * [ PSPagingControlEvent.java ]
 * 
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.guitools;

import java.awt.*;

/**
 * Event fired when an action occurs on the paging control.
 * @author erikserating
 */
public class PSPagingControlEvent extends AWTEvent
{

   /**
    * Ctor
    * @param source the source object cannot be <code>null</code>
    * @param currentPage the current page indicated by the paging
    * control or -1 if no page selected.
    */
   public PSPagingControlEvent(Object source, int currentPage)
   {
      super(source, 1);
      m_currentPage = currentPage;
   }
   
   /**
    * The value of the current page as indicted by the paging
    * control.
    * @return may be -1 if no page selected.
    */
   public int getCurrentPage()
   {
      return m_currentPage;
   }

   /**
    * The current page value. Defaults to -1.
    */
   private int m_currentPage = -1;

}
