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