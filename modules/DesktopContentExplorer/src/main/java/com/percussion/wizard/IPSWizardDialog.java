/******************************************************************************
 *
 * [ IPSWizardDialog.java ]
 * 
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.wizard;

/**
 * This interface should be implemented by all wizard type dialogs. It defines
 * generic functionality and constants.
 */
public interface IPSWizardDialog
{
   /**
    * This method handles the action if the back button of a wizard dialog is
    * pushed.
    */
   public void onBack();

   /**
    * This method handles the action if the next button of a wizard dialog is
    * pushed.
    */
   public void onNext();

   /**
    * This method handles the action if the finish button of a wizard dialog 
    * is pushed.
    */
   public void onFinish();
   
   /**
    * This method handles the action if the cancel button of a wizard dialog 
    * is pushed.
    */
   public void onCancel();
   
   /**
    * Get the result data for this wizard dialog.
    * 
    * @return an array of wizard page data as <code>Object</code>, never 
    *    <code>null</code>, each page data may be <code>null</code>. Page
    *    data are returned in the order the user walked throug the pages.
    */
   public Object[] getData();
   
   /**
    * Indicates that this dialog is showing the first wizard page.
    */
   public final static int TYPE_FIRST = 0;
   
   /**
    * Indicates that this dialog is showing a middle wizard page.
    */
   public final static int TYPE_MID = 1;
   
   /**
    * Indicates that this dialog is showing the last wizard page.
    */
   public final static int TYPE_LAST = 2;
   
   /**
    * An array with all valid page types.
    */
   public final static int[] TYPES =
   {
      TYPE_FIRST,
      TYPE_MID,
      TYPE_LAST
   };
}
