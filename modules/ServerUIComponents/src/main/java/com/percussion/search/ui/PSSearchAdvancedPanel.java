/******************************************************************************
 *
 * [ PSSearchAdvancedPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.search.ui;

import com.percussion.cms.objectstore.PSSearch;
import com.percussion.guitools.PSPropertyPanel;
import com.percussion.i18n.ui.PSI18NTranslationKeyValues;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;


/**
 * See {@link PSSearchSimplePanel}'s class description for an overview. 
 * <p>This class groups the useful but less frequently used controls together.
 * Generally, these are the search-engine specific properties.
 * The controls that may be included in this panel are:
 * <ol>
 *    <li>Lucene 'Expand query with synonyms' property</li>
 *    <li>Use DB case sensitivity flag, if appropriate (full text not enabled
 *    and the underlying db is case sensitive)</li>
 * <ol>
 * The presence of the these controls can be controlled by the caller. Either
 * the full-text controls or the db flag is present, but never both.
 */
public class PSSearchAdvancedPanel 
   extends PSPropertyPanel 
   implements ActionListener
{
    /**
    * Creates the advanced search panel.
    * @param isEngineAvailable flag to indicate whether the external search 
    * engine is available or not. This controls which controls will be 
    * present. It is possible that an empty panel could be returned. 
    * @param isDBCaseSensitive flag to indicate whether database is case 
    * sensitive or not. 
    * @param isSynonymExpansionEnabled flag to indicate whether queries will be
    * expanded with synonyms.
    */
   public PSSearchAdvancedPanel(boolean isEngineAvailable, 
         boolean isDBCaseSensitive, boolean isSynonymExpansionEnabled)
   {
      m_isEngineAvailable = isEngineAvailable;
      m_isDBCaseSensitive = isDBCaseSensitive;
      m_isSynonymExpansionEnabled = isSynonymExpansionEnabled;
      init(this);
   }
   
   /**
    * Similar to {@link #PSSearchAdvancedPanel(boolean, boolean, boolean)},
    * except the rows are placed in a supplied panel rather than this one (see
    * that ctor for descriptions of other params). This ctor is provided so that
    * the rows could be added into the {@link PSSearchSimplePanel} if desired. 
    * <p>The returned panel is still used in the same way for data validation 
    * and transfer.
    * 
    * @param parent All property rows are added to this panel. Never 
    * <code>null</code>.
    */
   public PSSearchAdvancedPanel(PSPropertyPanel parent, 
         boolean isEngineAvailable, 
         boolean isDBCaseSensitive,
         boolean isSynonymExpansionEnabled)
   {
      m_isEngineAvailable = isEngineAvailable;
      m_isDBCaseSensitive = isDBCaseSensitive;
      m_isSynonymExpansionEnabled = isSynonymExpansionEnabled;
      init(parent);
   }
   
   /**
    * Use the {@link PSI18NTranslationKeyValues} class to lookup a mnemonic
    * string for the given lookup string value. Returns <code>0</code> if
    * no mnemonic is defined. The actual lookup adds the string <q>_mnemonic</q>
    * to the lookup.
    * 
    * @param lookup the lookup string, without the class or package name, 
    * assumed non-<code>null</code>
    * 
    * @return the mnemonic value as an integer or <code>0</code>
    * if no mnemonic is defined.
    */
   private char getMnemonic(String lookup)
   {
      char mnemonic = 
         (char) PSI18NTranslationKeyValues.getInstance().getMnemonic(lookup);
      return mnemonic;
   }
   /**
    * Use the {@link PSI18NTranslationKeyValues} class to lookup a label
    * string for the given lookup string value. Returns <code>null</code> if
    * no label is defined. 
    * 
    * @param lookup the lookup string, without the class or package name, 
    * assumed non-<code>null</code>
    * 
    * @return the label, or the lookup string after the '@' character if not
    * found
    */  
   private String getLabel(String lookup)
   {
      return PSI18NTranslationKeyValues.getInstance().getTranslationValue(
            lookup);
   }
   
   /**
    * Initializes the search simple panel.
    * 
    * @param target All rows are added to this panel. Assumed not 
    * <code>null</code>.
    */
   private void init(PSPropertyPanel target)
   {  
      String label;
      char mnemonic;
      
      if (m_isEngineAvailable)
      {
         label = getLabel(I18N_SYNONYM_EXPANSION);
         mnemonic = getMnemonic(I18N_SYNONYM_EXPANSION);
         target.addPropertyRow(label, m_synonymExpansionCheckBox, mnemonic);
      }
    
      if (!m_isEngineAvailable && m_isDBCaseSensitive)
      {
         label = getLabel(I18N_CASE_SENSITIVE);
         mnemonic = getMnemonic(I18N_CASE_SENSITIVE);
         target.addPropertyRow(label, m_caseSensitiveCheckBox, mnemonic);
      }
   }
   
   /**
    * There is nothing to validate.
    * @param isQuiet boolean flag to indicate whether the system is
    *    running in quiet mode or not.
    * @return Always <code>true</code>.
    */
   public boolean onValidateData(boolean isQuiet)
   {
      return true;
   }
   
   /**
    * Initializes this panel with the data from the supplied PSSearch object if
    * the bDirection is <code>false</code>. Updates the search object if the
    * bDirection is <code>true</code> with the values from the panel.
    * 
    * @param bDirection Supply <code>true</code> to update the data,
    * <code>false</code>to initialize the panel.
    * @param search PSSearch object must not be <code>null</code>.
    *  
    */
   public void updateData(boolean bDirection, PSSearch search)
   {
      if(search == null)
      {
         throw new IllegalArgumentException(
            "Search must not be null or empty");
      }
      if(bDirection)
      {
         if (m_isEngineAvailable)
         {
            if(m_synonymExpansionCheckBox.isSelected())
            {
               search.setProperty(PROP_SYNONYM_EXPANSION, BOOL_YES);
            }
            else
            {
               search.setProperty(PROP_SYNONYM_EXPANSION, BOOL_NO);
            }
         }
         if (!m_isEngineAvailable && m_isDBCaseSensitive)
         {
            boolean caseSensitive =
               (m_caseSensitiveCheckBox.isSelected() ? true : false);
            search.setCaseSensitive(caseSensitive);
         }
      }
      else
      {
         if (m_isEngineAvailable)
         {
            //set the synonym expansion control, use search config default if
            //search does not have a valid setting
            boolean selected = m_isSynonymExpansionEnabled;
            String synExpansion = search.getProperty(PROP_SYNONYM_EXPANSION);
            if (synExpansion != null)
               selected = synExpansion.equals(BOOL_YES);
                        
            m_synonymExpansionCheckBox.setSelected(selected);
         }
         if (!m_isEngineAvailable && m_isDBCaseSensitive)
         {
            m_caseSensitiveCheckBox.setSelected(search.isCaseSensitive());
         }
      }
   }
   
   /**
    * Get the synonym expansion setting from the supplied search. If the
    * supplied search does not define one or it is an invalid setting the
    * default will be used.
    * 
    * @param search the search from which to get the synonym expansion setting,
    * not <code>null</code>.
    * @return the synonym expansion setting from the supplied search, defaults
    *    to {@link #BOOL_NO} if the supplied search has no synonym expansion
    *    setting specified or the one specified is invalid. Never
    *    <code>null</code> or empty.
    */
   public static String getSynonymExpansion(PSSearch search)
   {
      if (search == null)
         throw new IllegalArgumentException("search cannot be null");

      String synonymExpansion = search.getProperty(PROP_SYNONYM_EXPANSION);
      
      if ((synonymExpansion == null) || !(synonymExpansion.equals(BOOL_YES) ||
            synonymExpansion.equals(BOOL_NO)))
         return BOOL_NO;
      else
         return synonymExpansion;
   }
   
   // see interface for description
   public void actionPerformed(ActionEvent e)
   {
      String strCmd = e.getActionCommand();
      strCmd = strCmd.replace(' ', '_');
      if(strCmd.endsWith(":"))
         strCmd = strCmd.substring(0,strCmd.length()-1);
      try
      {
         Method m = getClass().getDeclaredMethod("on" + strCmd, null);
         m.invoke(this, null);
      }
      catch (Exception ignore)
      {
        ignore.printStackTrace(System.out);
      }
   }

   /**
    * Exposes the synonym expansion checkbox control.
    * 
    * @return a reference to the control used for setting the expand query
    * with synonyms property.
    */
   public JCheckBox getSynonymExpansionControl()
   {
      return m_synonymExpansionCheckBox;
   }
   
   /**
    * This is the property name used to transfer the 'Expand query with
    * synonyms' checkbox value.
    */
   public static final String PROP_SYNONYM_EXPANSION = "synonym_expansion";

   /**
    * Constant for the "yes" boolean value.
    */
   public static final String BOOL_YES = "yes";

   /**
    * Constant for the "no" boolean value.
    */
   public static final String BOOL_NO = "no";

   /**
    * A boolean flag to hold whether search engine is enabled
    * or not. <code>true</code> value indicates that search 
    * engine is enabled and query text and corresponding
    * components will be displayed. 
    */
   private boolean m_isEngineAvailable = false;

   /**
    * Check box for determining if the query should be expanded using
    * synonyms. Never <code>null</code>. Never modified after
    * initialization.
    */
   private JCheckBox m_synonymExpansionCheckBox = new JCheckBox();
   
   /**
    * A boolean flag to indicate whether synonym expansion is enabled for
    * queries.
    */
   private boolean m_isSynonymExpansionEnabled;

   /**
    * Prefix for lookups for I18N strings
    */
   private static final String I18N_PREFIX = PSSearchAdvancedPanel.class.
         getName();
   
   /**
    * Lookup string for I18N synonym expansion label value
    */
   private static final String I18N_SYNONYM_EXPANSION = I18N_PREFIX
         + "@Expand query with synonyms";

   /**
    * Lookup string for I18N case sensitive label value
    */
   private static final String I18N_CASE_SENSITIVE = I18N_PREFIX
         + "@Case Sensitive";

   /**
    * Check box for determining if the query should treat text data in
    * case-sensitive manner. Never <code>null</code>. Never modified after
    * initialization.
    */
   private JCheckBox m_caseSensitiveCheckBox = new JCheckBox();

   /**
    * Flag to indicate whether database is case sensitve or not.
    */
   private boolean m_isDBCaseSensitive;
}
