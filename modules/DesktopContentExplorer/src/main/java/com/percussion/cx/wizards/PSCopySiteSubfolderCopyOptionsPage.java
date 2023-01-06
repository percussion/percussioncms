/******************************************************************************
 *
 * [ PSCopySiteSubfolderCopyOptionsPage.java ]
 * 
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.cx.wizards;

import com.percussion.cx.PSContentExplorerApplet;
import com.percussion.i18n.ui.PSI18NTranslationKeyValues;

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
public class PSCopySiteSubfolderCopyOptionsPage 
   extends PSCopySiteCopyOptionsPage
{
   /**
    * Instantiate with applet to make config options from applet available to
    * panel
    */
   public PSCopySiteSubfolderCopyOptionsPage(PSContentExplorerApplet applet)
   {
      super(applet);
      initPanel(createMainPanel());
   }

   /**
    * Construct a new copy options page.
    */
   public PSCopySiteSubfolderCopyOptionsPage()
   {
      initPanel(createMainPanel());
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
            "@Copy Site Subfolders"));
      m_noContent.setSelected(true);
      
      Component contentPane = this.getTopLevelAncestor();
     
      if (m_applet.isManagedNavUsed())
      {
         m_navigationContent = new JRadioButton(
            PSI18NTranslationKeyValues.getInstance().
            getTranslationValue(getClass().getName() + 
               "@Copy Site Subfolders with Navigation"));
         
         m_allContent = new JRadioButton(PSI18NTranslationKeyValues.getInstance().
               getTranslationValue(getClass().getName() + 
                  "@Copy Site Subfolders, Navigation and Content"));
      }
      else
      {
         m_navigationContent = null;
         m_allContent = new JRadioButton(PSI18NTranslationKeyValues.getInstance().
               getTranslationValue(getClass().getName() + 
                  "@Copy Site Subfolders and Content"));
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
}