/******************************************************************************
 *
 * [ IPSPagingControlListener.java ]
 * 
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.guitools;

/**
 * Listens to the paging control for events affecting the value
 * of the current page.
 * @author erikserating
 */
public interface IPSPagingControlListener
{
   /**
    * Method called by paging control whever the current page
    * value changes.
    * @param event never <code>null</code>.
    */
   public void onPageChange(PSPagingControlEvent event);
}
