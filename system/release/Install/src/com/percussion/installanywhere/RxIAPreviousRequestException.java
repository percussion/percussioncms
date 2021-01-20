/******************************************************************************
 *
 * [ RxIAPreviousRequestException.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installanywhere;

import com.zerog.ia.api.pub.PreviousRequestException;


/**
 * Simple wrapper class for the InstallAnywhere
 * {@link PreviousRequestException} class.  Used by custom consoles to
 * enable 'back' capability.
 * 
 * @author peterfrontiero
 */
public class RxIAPreviousRequestException extends PreviousRequestException
{
   /**
    * Default constructor.
    */
   public RxIAPreviousRequestException()
   {
      super();
   }
}
