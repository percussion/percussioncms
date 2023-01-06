/******************************************************************************
 *
 * [ PSContentCopyOptionsPage.java ]
 * 
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.cx.wizards;

import com.percussion.cx.PSContentExplorerApplet;
import com.percussion.i18n.ui.PSI18NTranslationKeyValues;
import com.percussion.wizard.PSWizardPanel;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import java.awt.FlowLayout;

/**
 * This wizard panel allows a user to select the option for how content 
 * should be ccopied
 */
public class PSContentCopyOptionsPage extends PSWizardPanel
{ 
   /**
    * Instantiate with applet to make config options from applet available to
    * panel
    */
   public PSContentCopyOptionsPage(PSContentExplorerApplet applet)
   {
      super(applet);
      initPanel(createMainPanel());
   }
   
   
   /**
    * Construct a new content copy options page.
    */
   public PSContentCopyOptionsPage()
   {
      initPanel(createMainPanel());
   }

   /* (non-Javadoc)
    * @see IPSWizardPanel#getSummary()
    */
   public String getSummary()
   {
      String summary = m_asLink.getText();
      if (m_asNewCopy.isSelected())
         summary = m_asNewCopy.getText();
         
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
      
      m_asLink = new JRadioButton(PSI18NTranslationKeyValues.getInstance()
         .getTranslationValue(getClass().getName() + "@Copy Content as link"));
      m_asLink.setSelected(true);

      m_asNewCopy = new JRadioButton(PSI18NTranslationKeyValues.getInstance()
         .getTranslationValue(
            getClass().getName() + "@Copy Content as new copy"));
      
      ButtonGroup copyOptions = new ButtonGroup();
      copyOptions.add(m_asLink);
      copyOptions.add(m_asNewCopy);
      
      box.add(m_asLink);
      box.add(m_asNewCopy);
      
      panel.add(box);
      
      return panel;
   }
   
   /**
    * Get the panel data.
    * 
    * @return the panel data as 
    *    <code>PSContentCopyOptionsPage.OutputData</code> object, 
    *    never <code>null</code>.
    */
   public Object getData()
   {
      return new OutputData(m_asLink.isSelected(), m_asNewCopy.isSelected());
   }
   
   /**
    * The data object returned by this wizard page.
    */
   public class OutputData
   {
      /**
       * Construct content copy options data object.
       *  
       * @param isAsLink <code>true</code> if content will be copied as link,
       *    <code>false</code> otherwise. 
       * @param isAsNewCopy <code>true</code> if content will be copied as new 
       *    copy, <code>false</code> otherwise.
       */
      private OutputData(boolean isAsLink, boolean isAsNewCopy)
      {
         if (isAsLink && !isAsNewCopy)
            m_copyOption = AS_LINK;
         else if (isAsNewCopy && !isAsLink)
            m_copyOption = AS_NEW_COPY;
         else
            throw new IllegalArgumentException(
               "only one option can be selected");
      }
      
      /**
       * Get the selected content copy option.
       * 
       * @return the selected content copy option, one of <code>AS_LINK</code>
       *    or <code>AS_NEW_COPY</code> values.
       */
      public int getCopyOption()
      {
         return m_copyOption;
      }
      
      /**
       * The option value if no content should be copied.
       */
      public static final int AS_LINK = 0;
      
      /**
       * The option value if only navigation content should be copied.
       */
      public static final int AS_NEW_COPY = 1;
      
      /**
       * The copy option, initialized by the constructor, always one valid
       * selected option after that.
       */
      private int m_copyOption = -1;
   }
   
   /**
    * The radio button used to select the option to copy content as link. 
    * Initialized in {@link createMainPanel()}, never <code>null</code> or 
    * changed after that.
    */
   private JRadioButton m_asLink = null;
   
   /**
    * The radio button used to select the option to copy content as new copy. 
    * Initialized in {@link createMainPanel()}, never <code>null</code> or 
    * changed after that.
    */
   private JRadioButton m_asNewCopy = null;
}