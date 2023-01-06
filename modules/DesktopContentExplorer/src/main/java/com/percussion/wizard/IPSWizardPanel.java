/******************************************************************************
 *
 * [ IPSWizardPanel.java ]
 * 
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.wizard;

/**
 * This interface defines common functionality which must be implemented for
 * all panels used in a wizard dialog to show the various wizard UI pages.
 */
public interface IPSWizardPanel
{
   /**
    * Validates all UI components of this panel. If any validations fail, a 
    * <code>PSWizardValidationError</code> is thrown. Typically a wizard dialog 
    * calls this method each time a user pushes the wizards next button.
    * 
    * @throws PSWizardValidationError for any validation error.
    */
   public void validatePanel() throws PSWizardValidationError;
   
   /**
    * This method determines whether the next wizard page can be skipped or not.
    * Typically this is done based on user input for this panel and 
    * handled appropriatly by the wizard dialog.
    * 
    * @return <code>true</code> if the next wizard page can be skipped, 
    *    <code>false</code> otherwise.
    */
   public boolean skipNext();
   
   /**
    * Get the summary for this wizard panel. Typically this summary is 
    * collected from all active wizard pages and shown in the last wizard page
    * to the user before the actual action takes place.
    * 
    * @return the summary for this wizard panel, never <code>null</code>, may
    *    be empty.
    */
   public String getSummary();
   
   /**
    * Get the panel data.
    * 
    * @return the panel data, may be <code>null</code>.
    */
   public Object getData();
   
   /**
    * Set the panel input data. Each panel can have optional data for which it
    * will be initialized.
    * 
    * @param data the data to initialize the panel with, may be 
    *    <code>null</code>.
    */
   public void setData(Object data);
   
   /**
    * Get the instruction text shown for this wizard page.
    * 
    * @return the instruction text shown on this wizard page, never 
    *    <code>null</code>, may be empty.
    */
   public String getInstruction();
   
   /**
    * Sets the instruction text for this wizard panel.
    * 
    * @param instruction the new instructions to set for this wizard panel,
    *    not <code>null</code> or empty.
    */
   public void setInstruction(String instruction);
}
