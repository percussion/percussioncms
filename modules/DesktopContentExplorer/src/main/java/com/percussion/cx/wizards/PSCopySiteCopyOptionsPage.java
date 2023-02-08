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
import com.percussion.i18n.ui.PSI18NTranslationKeyValues;
import com.percussion.wizard.PSWizardPanel;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import java.awt.Component;
import java.awt.FlowLayout;

/**
 * This wizard panel allows a user to select the option for what content
 * should be copied.
 */
public class PSCopySiteCopyOptionsPage extends PSWizardPanel
{
   /**
    * Instantiate with applet to make config options from applet available to
    * panel
    */
   public PSCopySiteCopyOptionsPage(PSContentExplorerApplet applet)
   {
      super(applet);
      initPanel(createMainPanel());
   }

   /**
    * Construct a new copy options page.
    */
   public PSCopySiteCopyOptionsPage()
   {
      initPanel(createMainPanel());
   }

   /* (non-Javadoc)
    * @see IPSWizardPanel#skipNext()
    */
   public boolean skipNext()
   {
      return !m_allContent.isSelected();
   }

   /* (non-Javadoc)
    * @see IPSWizardPanel#getSummary()
    */
   public String getSummary()
   {
      String summary = m_allContent.getText();
      if (m_noContent.isSelected())
         summary = m_noContent.getText();
      else if (m_navigationContent != null && m_navigationContent.isSelected())
         summary = m_navigationContent.getText();
         
      return summary;
   }
   
   /**
    * Create the wizard page main panel.
    * 
    * @return the new main panel, never <code>null</code>.
    */
   private JPanel createMainPanel()
   {
      JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

      Box box = new Box(BoxLayout.Y_AXIS);
      
      m_noContent = new JRadioButton(PSI18NTranslationKeyValues.getInstance().
         getTranslationValue(getClass().getName() + 
            "@Copy Site Folders"));
      m_noContent.setSelected(true);
      
      Component contentPane = this.getTopLevelAncestor();
         
      if (m_applet.isManagedNavUsed())
      {
         m_navigationContent = new JRadioButton(
            PSI18NTranslationKeyValues.getInstance().
            getTranslationValue(getClass().getName() + 
               "@Copy Site Folders with Navigation"));
         
         m_allContent = new JRadioButton(PSI18NTranslationKeyValues.getInstance().
            getTranslationValue(getClass().getName() + 
               "@Copy Site Folders, Navigation and Content"));
      }
      else
      {
         m_navigationContent = null;
         m_allContent = new JRadioButton(PSI18NTranslationKeyValues.getInstance().
               getTranslationValue(getClass().getName() + 
                  "@Copy Site Folders and Content"));
      }
      
      ButtonGroup copyOptions = new ButtonGroup();
      copyOptions.add(m_noContent);
      if (m_navigationContent != null)
         copyOptions.add(m_navigationContent);
      copyOptions.add(m_allContent);
      
      box.add(m_noContent);
      if (m_navigationContent != null)
         box.add(m_navigationContent);
      box.add(m_allContent);
      
      panel.add(box);
      
      return panel;
   }
   
   /**
    * Get the panel data.
    * 
    * @return the panel data as 
    *    <code>PSCopySiteCopyOptionsPage.OutputData</code> object, 
    *    never <code>null</code>.
    */
   public Object getData()
   {
      if (m_navigationContent != null)
         return new OutputData(m_noContent.isSelected(), 
            m_navigationContent.isSelected(), m_allContent.isSelected());
      else
         return new OutputData(m_noContent.isSelected(), 
            false, m_allContent.isSelected());
   }
   
   /**
    * The data object returned by this wizard page.
    */
   public class OutputData
   {
      /**
       * Construct copy options data object.
       *  
       * @param isNoContent <code>true</code> if no content will be copied,
       *    <code>false</code> otherwise. 
       * @param isNavigationContent <code>true</code> if navigation content
       *    will be copied, <code>false</code> otherwise.
       * @param isAllContent <code>true</code> if all content is copied,
       *    <code>false</code> otherwise.
       */
      private OutputData(boolean isNoContent, boolean isNavigationContent, 
         boolean isAllContent)
      {
         if (isNoContent && !(isNavigationContent && isAllContent))
            m_copyOption = NO_CONTENT;
         else if (isNavigationContent && !(isNoContent && isAllContent))
            m_copyOption = NAVIGATION_CONTENT;
         else if (isAllContent && !(isNoContent && isNavigationContent))
            m_copyOption = ALL_CONTENT;
         else
            throw new IllegalArgumentException(
               "only one option can be selected");
      }
      
      /**
       * Get the selected copy option.
       * 
       * @return the selected copy option, one of <code>NO_CONTENT</code>,
       *    <code>NAVIGATION_CONTENT</code> or <code>ALL_CONTENT</code> values.
       */
      public int getCopyOption()
      {
         return m_copyOption;
      }
      
      /**
       * The option value if no content should be copied.
       */
      public static final int NO_CONTENT = 0;
      
      /**
       * The option value if only navigation content should be copied.
       */
      public static final int NAVIGATION_CONTENT = 1;
      
      /**
       * The option value is all content should be copied.
       */
      public static final int ALL_CONTENT = 2;
      
      /**
       * The copy option, initialized by the constructor, always one valid
       * selected option after that.
       */
      private int m_copyOption = -1;
   }
   
   /**
    * The radio button used to select the option to copy no content. 
    * Initialized in {@link createMainPanel()}, never <code>null</code> or 
    * changed after that.
    */
   protected JRadioButton m_noContent = null;
   
   /**
    * The radio button used to select the option to copy navigation content. 
    * Initialized in {@link createMainPanel()}, never <code>null</code> or 
    * changed after that.
    */
   protected JRadioButton m_navigationContent = null;
   
   /**
    * The radio button used to select the option to copy all content. 
    * Initialized in {@link createMainPanel()}, never <code>null</code> or 
    * changed after that.
    */
   protected JRadioButton m_allContent = null;
}
