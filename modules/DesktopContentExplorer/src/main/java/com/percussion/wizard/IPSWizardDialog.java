/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
