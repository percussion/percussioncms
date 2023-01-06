/******************************************************************************
 *
 * [ PSWizardValidationError.java ]
 * 
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.wizard;

import com.percussion.error.PSException;

/**
 * This exception must be thrown for wizard page validation errors.
 */
public class PSWizardValidationError extends PSException
{
   /* (non-Javadoc)
    * @see PSException#PSException(int, Object) for documentation.
    */
   public PSWizardValidationError(int code, Object arg)
   {
      super(code, arg);
   }

   /* (non-Javadoc)
    * @see PSException#PSException(int, Object[]) for documentation.
    */
   public PSWizardValidationError(int code, Object[] args)
   {
      super(code, args);
   }
}
