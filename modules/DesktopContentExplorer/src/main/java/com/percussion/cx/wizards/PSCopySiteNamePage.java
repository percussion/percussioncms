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

import com.percussion.cms.objectstore.PSSite;
import com.percussion.cx.PSContentExplorerApplet;
import com.percussion.cx.error.IPSContentExplorerErrors;
import com.percussion.cx.objectstore.PSNode;
import com.percussion.guitools.PSPropertyPanel;
import com.percussion.i18n.ui.PSI18NTranslationKeyValues;
import com.percussion.wizard.PSWizardPanel;
import com.percussion.wizard.PSWizardValidationError;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * The wizard page to request the new site name and site folder name from the
 * user.
 */
public class PSCopySiteNamePage extends PSWizardPanel
{
   public PSCopySiteNamePage(PSContentExplorerApplet applet)
   {
      super(applet);
      initPanel(createMainPanel());
   }
   /**
    * Construct a new panel.
    */
   public PSCopySiteNamePage()
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
         errors.append(
            PSI18NTranslationKeyValues.getInstance().getTranslationValue(
               getClass().getName() + "@Site Folder name is required."));
      }

      if (m_input.getFolderNames().contains(folderName))
      {
         errors.append("\n");
         errors.append(
            PSI18NTranslationKeyValues.getInstance().getTranslationValue(
               getClass().getName() + "@Site Folder name is invalid."));
      }

      if (m_input.hasSitesToCopy())
      {
         String siteName = m_siteName.getText().trim();
         if (siteName.length() == 0)
         {
            errors.append("\n");
            errors.append(
               PSI18NTranslationKeyValues.getInstance().getTranslationValue(
                  getClass().getName() + "@Site name is required."));
         }

         if (m_input.getSiteNames().contains(siteName))
         {
            errors.append("\n");
            errors.append(
               PSI18NTranslationKeyValues.getInstance().getTranslationValue(
                  getClass().getName() + "@Site name is invalid."));
         }
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
      StringBuffer buffer = new StringBuffer();
      buffer.append(
         PSI18NTranslationKeyValues.getInstance().getTranslationValue(
         getClass().getName() + "@New Folder Name:") + 
         m_folderName.getText());
      
      PSSite copiedSite = (PSSite) m_siteSelector.getSelectedItem();
      if (copiedSite != null)
      {
         buffer.append("\n");
         buffer.append(
            PSI18NTranslationKeyValues.getInstance().getTranslationValue(
            getClass().getName() + "@Copied Site Definition:") + 
            copiedSite.getName());
         
         buffer.append("\n");
         buffer.append(
            PSI18NTranslationKeyValues.getInstance().getTranslationValue(
            getClass().getName() + "@New Site Name:") + 
            m_siteName.getText());
      }
         
      return buffer.toString();
   }
   
   /**
    * Get the panel data.
    * 
    * @return the panel data as <code>PSCopySiteNamePage.OutputData</code> 
    *    object, never <code>null</code>.
    */
   public Object getData()
   {
      String copiedSiteName = null;
      String newSiteName = null;
      if (m_input.hasSitesToCopy())
      {
         copiedSiteName = m_siteSelector.getSelectedItem().toString();
         newSiteName = m_siteName.getText();
      }
      
      return new OutputData(copiedSiteName, newSiteName, 
         m_folderName.getText());
   }
   
   /**
    * Set and initialize the the panel data.
    * 
    * @param data the data as <code>PSCopySiteNamePage.InputData</code>
    *    object, not <code>null</code>.
    */
   public void setData(Object data)
   {
      if (!(data instanceof InputData))
         throw new IllegalArgumentException(
            "data must be an insanceof PSCopySiteNamePage.InputData");
      
      m_input = (InputData) data;
      super.setData(data);
      
      // update the main panel depending on the supplied input data
      Collection sitesToCopy = m_input.getSitesToCopy();
      
      Iterator sites = sitesToCopy.iterator();
      while (sites.hasNext())
         m_siteSelector.addItem(sites.next());
         
      if (sitesToCopy.size() > 0)
         m_siteSelector.setSelectedIndex(0);
   }
   
   /**
    * Creates the main page panel.
    * 
    * @return the new panel, never <code>null</code>.
    */
   private JPanel createMainPanel()
   {
      PSPropertyPanel panel = new PSPropertyPanel();

      panel.addPropertyRow(PSI18NTranslationKeyValues.getInstance()
         .getTranslationValue(getClass().getName() + "@New Folder Name"),
         new JComponent[]
         {
            m_folderName
         });

      panel.addPropertyRow(PSI18NTranslationKeyValues.getInstance()
         .getTranslationValue(getClass().getName() + 
            "@Site Definition to copy"),
         new JComponent[]
         {
            m_siteSelector
         });
      
      panel.addPropertyRow(PSI18NTranslationKeyValues.getInstance()
         .getTranslationValue(getClass().getName() + "@New Site Name"),
         new JComponent[]
         {
            m_siteName
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
       * @param source the source node that will be copied, not 
       *    <code>null</code>. 
       * @param sites all defined sites in the system as collection of 
       *    <code>PSSite</code> objects, not <code>null</code>, may be empty.
       * @param folderNames all folder names already existing in the target
       *    folder of the copy action as collection of <code>String</code>
       *    objects.
       */
      public InputData(PSNode source, Collection sites, Collection folderNames)
      {
         if (source == null)
            throw new IllegalArgumentException("source cannot be null");
         
         if (sites == null)
            throw new IllegalArgumentException("sites cannot be null");
         
         if (folderNames == null)
            throw new IllegalArgumentException("folderNames cannot be null");
         
         m_source = source;
         m_sites = sites;
         m_folderNames = folderNames;
      }
      
      /**
       * Get the source node that is being copied.
       * 
       * @return the source node to be copied, never <code>null</code>.
       */
      public PSNode getSource()
      {
         return m_source;
      }
      
      /**
       * Get all existing site names.
       * 
       * @return a collection with all site names existing in the system as 
       *    <code>String</code> objects, never <code>null</code>, may be empty.
       */
      public Collection getSiteNames()
      {
         Collection siteNames = new ArrayList();
         Iterator sites = m_sites.iterator();
         while (sites.hasNext())
            siteNames.add(((PSSite) sites.next()).getName());

         return siteNames;
      }
      
      /**
       * Get the names of all sites that have the supplied folderRoot as their
       * folder root. A case-insensitive compare is done.
       * 
       * @param folderRoot the folder root for which we want all site names,
       *    not <code>null</code> or empty.
       * @return a list will all site names which have the supplied folder root,
       *    never <code>null</code>, may be empty.
       */
      public Collection getSitesToCopy()
      {
         Collection siteNames = new ArrayList();
         
         String folderRoot = m_source.getName().toLowerCase();
         
         Iterator sites = m_sites.iterator();
         while (sites.hasNext())
         {
            PSSite site = (PSSite) sites.next();
            String test = site.getFolderRoot();
            if (test == null)
               continue;
            
            if (test.toLowerCase().endsWith(folderRoot))
               siteNames.add(site);
         }

         return siteNames;
      }
      
      /**
       * Are there site definitions to copy?
       * 
       * @return <code>true</code> if there are, <code>false</code> otherwise.
       */
      public boolean hasSitesToCopy()
      {
         return getSitesToCopy().size() > 0;
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
       * The source node that will be copied, initialized during construction,
       * never <code>null</code> or changed after that.
       */
      private PSNode m_source = null;
      
      /**
       * The collection of all existing sites as <code>PSSite</code> 
       * objects, initialized during construction, never <code>null</code> or
       * changed after that.
       */
      private Collection m_sites = null;
      
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
       * @param copiedSiteName the name of the site definition to be copied, 
       *    may be <code>null</code> or empty.
       * @param newSiteName the new site name, may be <code>null</code> or 
       *    empty.
       * @param folderName the new site folder name, assumed not 
       *    <code>null</code> or empty.
       */
      private OutputData(String copiedSiteName, String newSiteName, 
         String folderName)
      {
         m_copiedSiteName = copiedSiteName;
         m_newSiteName = newSiteName;
         m_folderName = folderName;
      }
      
      /**
       * Get the name of the copied site definition.
       * 
       * @return the name of the copied site definition, may be 
       *    <code>null</code> or empty.
       */
      public String getCopiedSiteName()
      {
         return m_copiedSiteName;
      }
      
      /**
       * Get the new site name.
       * 
       * @return the new site name, may be <code>null</code> or empty.
       */
      public String getNewSiteName()
      {
         return m_newSiteName;
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
       * The name of the copied site sitedefinition, initialized in 
       * constructor, may be<code>null</code> or empty, is never changed after 
       * that.
       */
      private String m_copiedSiteName = null;
      
      /**
       * The new site name, initialized in constructor, may be 
       * <code>null</code> or empty, is never changed after that.
       */
      private String m_newSiteName = null;

      /**
       * The new site folder name, initialized in constructor, never 
       * <code>null</code>, empty or changed after that.
       */
      private String m_folderName = null;
   }
   
   /**
    * This combo box shows all site definitions that are referenced by the 
    * source site folder being copied. The user can select the one that will
    * be copied together with the site folder.
    */
   private JComboBox m_siteSelector = new JComboBox();
   
   /**
    * A text field to request the new site name from the user, never
    * <code>null</code>.
    */
   private JTextField m_siteName = new JTextField();
   
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
