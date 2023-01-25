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
package com.percussion.cx.wizards;

import com.percussion.cx.PSContentExplorerApplet;
import com.percussion.cx.error.IPSContentExplorerErrors;
import com.percussion.guitools.PSPropertyPanel;
import com.percussion.i18n.ui.PSI18NTranslationKeyValues;
import com.percussion.wizard.PSWizardPanel;
import com.percussion.wizard.PSWizardValidationError;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.util.Collection;

/**
 * The wizard page to request the new site folder name from the user.
 */
public class PSCopySiteSubfolderNamePage extends PSWizardPanel
{
   /**
    * Instantiate with applet to make config options from applet available to
    * panel
    */
   public PSCopySiteSubfolderNamePage(PSContentExplorerApplet applet)
   {
      super(applet);
      initPanel(createMainPanel());

   }

   /**
    * Construct a new panel.
    */
   public PSCopySiteSubfolderNamePage()
   {
      initPanel(createMainPanel());
   }

   /* (non-Javadoc)
    * @see IPSWizardPanel#validatePanel()
    */
   public void validatePanel() throws PSWizardValidationError
   {
      StringBuffer errors = new StringBuffer();

      String folderName = m_folderName.getText().trim();
      if (folderName.length() == 0)
      {
         errors.append("\n");
         errors.append("\t" +
            PSI18NTranslationKeyValues.getInstance().getTranslationValue(
               getClass().getName() + "@Site Subfolder name is required."));
      }

      if (m_input.getFolderNames().contains(folderName))
      {
         errors.append("\n");
         errors.append("\t" +
            PSI18NTranslationKeyValues.getInstance().getTranslationValue(
               getClass().getName() + "@Site Subfolder name is invalid."));
      }

      if (folderName.length() > MAX_FOLDER_LENGTH)
      {
         errors.append("\n");
         errors.append("\t" +
            PSI18NTranslationKeyValues.getInstance().getTranslationValue(
               getClass().getName() + "@Site Subfolder name has too many characters."));
      }

      if (errors.length() > 0)
         throw new PSWizardValidationError(
            IPSContentExplorerErrors.WIZARD_VALIDATION_ERROR,
               errors.toString());
   }

   /* (non-Javadoc)
    * @see IPSWizardPanel#getSummary()
    */
   public String getSummary()
   {
      return PSI18NTranslationKeyValues.getInstance().getTranslationValue(
         getClass().getName() + "@New Subfolder Name:\n\t")
         + m_folderName.getText();
   }

   /**
    * Get the panel data.
    *
    * @return the panel data as
    *    <code>PSCopySiteSubfolderNamePage.OutputData</code> object, never
    *    <code>null</code>.
    */
   public Object getData()
   {
      return new OutputData(m_folderName.getText());
   }

   /**
    * Set and initialize the the panel data.
    *
    * @param data the data as <code>PSCopySiteSubfolderNamePage.InputData</code>
    *    object, not <code>null</code>.
    */
   public void setData(Object data)
   {
      if (!(data instanceof InputData))
         throw new IllegalArgumentException(
            "data must be an insanceof PSCopySiteSubfolderNamePage.InputData");

      m_input = (InputData) data;
      super.setData(data);
   }

   /**
    * Create the page main panel.
    *
    * @return the main page panel, never <code>null</code>.
    */
   private JPanel createMainPanel()
   {
      PSPropertyPanel panel = new PSPropertyPanel();

      panel.addPropertyRow(PSI18NTranslationKeyValues.getInstance()
         .getTranslationValue(getClass().getName() + "@New Subfolder Name"),
         new JComponent[]
         {
            m_folderName
         });

      return panel;
   }

   /**
    * The data object to initialize this wizard page with.
    */
   public static class InputData
   {
      /**
       * Construct the input data for this wizard page.
       *
       * @param folderNames all folder names already existing in the target
       *    folder of the copy action as collection of <code>String</code>
       *    objects.
       */
      public InputData(Collection folderNames)
      {
         if (folderNames == null)
            throw new IllegalArgumentException("folderNames cannot be null");

         m_folderNames = folderNames;
      }

      /**
       * Get all in the target folder existing folder names.
       *
       * @return a collection with all folder names already existing in the
       *    target folder as <code>String</code> objects, never
       *    <code>null</code>, may be empty.
       */
      public Collection getFolderNames()
      {
         return m_folderNames;
      }

      /**
       * The collection of all existing folder names in the target folder as
       * <code>String</code> objects, initialized during construction,
       * never <code>null</code> or changed after that.
       */
      private Collection m_folderNames = null;
   }

   /**
    * The data object returned by this wizard page.
    */
   public class OutputData
   {
      /**
       * Constructs a new data object for the supplied parameters.
       *
       * @param folderName the new site folder name, assumed not
       *    <code>null</code> or empty.
       */
      private OutputData(String folderName)
      {
         m_folderName = folderName;
      }

      /**
       * Get the new site folder name.
       *
       * @return the new site folder name, never <code>null</code> or empty.
       */
      public String getFolderName()
      {
         return m_folderName;
      }

      /**
       * The new site folder name, initialized in constructor, never
       * <code>null</code>, empty or changed after that.
       */
      private String m_folderName = null;
   }

   /**
    * The maximum acceptable length of the new site folder name
    */
   private static final int MAX_FOLDER_LENGTH = 100;
   
   /**
    * A text field to request the new site folder name from the user, never
    * <code>null</code>.
    */
   private JTextField m_folderName = new JTextField();

   /**
    * The input data, set with the first call to {@link setData(Object)}, never
    * <code>null</code> after that.
    */
   private InputData m_input = null;
}
