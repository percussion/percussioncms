/******************************************************************************
 *
 * [ PSSearchSimplePanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2010 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.search.ui;

import com.percussion.UTComponents.UTFixedHeightTextField;
import com.percussion.cms.objectstore.PSSProperty;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.guitools.PSAccessibleActionListener;
import com.percussion.guitools.PSPropertyPanel;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.i18n.ui.PSI18NTranslationKeyValues;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Map;

/**
 * The search ui components are shared among different clients. There are 3
 * pieces that make up the search ui: simple controls, advanced controls and 
 * the content editor fields. These are available in 3 classes, respectively:
 * <ol>
 *    <li>PSSearchSimplePanel (this class)</li>
 *    <li>PSSearchAdvancedPanel</li>
 *    <li>PSSearchFieldEditor</li>
 * <ol>
 * The simple panel contains those controls that are considered minimal to 
 * use the search interface. The advanced panel contains those controls that
 * are useful, but needed less frequently. The search field editor is
 * generally considered advanced, but the caller can place that as they wish.
 * <p>The controls on this panel include the following:
 * <ol>
 *    <li>Full text query editor</li>
 *    <li>Display format selector</li>
 *    <li>Max results</li>
 * <ol>
 *
 */
public class PSSearchSimplePanel 
   extends PSPropertyPanel 
   implements ActionListener
{
   /**
    * Creates the simple search panel.
    * @param isEngineAvailable flag to Indicate whether External search engine
    *    is available or not. If <code>true</code> then a text area for full
    *    text query and its corresponding components will be added, otherwise
    *    only display format, maxresults and case sensitive components will be added. 
    * @param dfmap map consisting of display format ids and display names must 
    *    not <code>null</code> or empty.
    * @param maxSearchResult the max row returned from a search result. 
    *    <code>-1</code> if unlimited.
    */
   public PSSearchSimplePanel(boolean isEngineAvailable, Map dfmap, 
      int maxSearchResult)
   {
      m_isEngineAvailable = isEngineAvailable;
      if(dfmap == null || dfmap.isEmpty())
      {
         throw new IllegalArgumentException(
            "display format map must not be null or empty");
      }
      m_displayFormatMap = dfmap;
      m_maxSearchResult = maxSearchResult;
      init();
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
      char mnemonic = (char) PSI18NTranslationKeyValues.getInstance().getMnemonic(
            lookup);
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
    */
   private void init()
   {  
      String label;
      char mnemonic;
      
      // add FTS query text and related component rows only if
      //search engine is available. 
      if (m_isEngineAvailable)
      {
         m_ftQuery.setLineWrap(true);
         m_ftQuery.setWrapStyleWord(true);
         JScrollPane queryPane = new JScrollPane(m_ftQuery);
         queryPane.setPreferredSize(new Dimension(300, 100));
         label = getLabel(I18N_SEARCH_FOR);
         mnemonic = getMnemonic(I18N_SEARCH_FOR);
         addPropertyRow(label, m_ftQuery, mnemonic);
      }
    
      //Add display format combo box row
      m_displayFormatCombo = new JComboBox(
         ApplicationDataComboModel.createApplicationDataComboModel(
            m_displayFormatMap));

      JComponent[] comp = {m_displayFormatCombo};
      label = getLabel(I18N_DISPLAY_FORMAT);
      m_displayFormatCombo.addActionListener(new PSAccessibleActionListener(label));
      mnemonic = getMnemonic(I18N_DISPLAY_FORMAT);
      addPropertyRow(label, comp, m_displayFormatCombo, mnemonic, null);
      
      Dimension rowDim = new Dimension(0,0);
      rowDim.height = m_displayFormatCombo.getPreferredSize().height;

      // Add Max rows components row
      JPanel p = new  JPanel();
      p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
      JLabel maximum = null;
      if (m_maxSearchResult < 0)
      {
         label = getLabel(I18N_UNLIMITED);
         if (!label.endsWith(":"))
            label += ":";
         mnemonic = getMnemonic(I18N_UNLIMITED);
         m_unlimitedCheckBox = new JCheckBox(label);
         m_unlimitedCheckBox.removeActionListener(this);
         m_unlimitedCheckBox.addActionListener(this);
         if (mnemonic != 0)
            m_unlimitedCheckBox.setMnemonic(mnemonic);
      }
      else
      {
         // Display allowed range
         label = getLabel(I18N_ALLOWED_RANGE);
         label = MessageFormat.format(label, new Object[] {
               "1", String.valueOf(m_maxSearchResult)});
         maximum = new JLabel(label);
      }
      
      m_maximumText = new UTFixedHeightTextField();
      p.add(m_maximumText);
      p.add(Box.createHorizontalStrut(5));
      if (m_maxSearchResult < 0)
         p.add(m_unlimitedCheckBox);
      else
         p.add(maximum);
      p.add(Box.createHorizontalGlue());
      JComponent[] panelComp = {p};
      label = getLabel(I18N_MAX_ROWS_RETURNED);
      if (!label.endsWith(":"))
         label += ":";
      mnemonic = getMnemonic(I18N_MAX_ROWS_RETURNED);
      addPropertyRow(label, panelComp, p, mnemonic, null);
      p.setPreferredSize(rowDim);
      p.setMinimumSize(rowDim);
      p.setMaximumSize(new Dimension(230, 30));
   }
   
   /**
    * Requests focus for the query box field component.
    */
   public void focusQueryBox(){
      m_ftQuery.requestFocus();
   }
   
   /**
    * Must have a valid number for max results, fts query may not be too long.
    * @param isQuiet boolean flag to indicate whether the system is
    *    running in quiet mode or not.
    * 
    * @return boolean <code>true</code> if validation succeeds, otherwise 
    * <code>false</code>. 
    */
   public boolean onValidateData(boolean isQuiet)
   {
      if (m_isEngineAvailable)
      {
         String ftq = m_ftQuery.getText();
         String msg = validateFTSSearchQuery(ftq,
            PSI18NTranslationKeyValues.getInstance(), null);
         if (msg != null)
         {
            if (!isQuiet)
            {
               ErrorDialogs.showErrorDialog(this, msg,
                  PSI18NTranslationKeyValues.getInstance().getTranslationValue(
                     getClass().getName() + "@Validation Error"),
                  JOptionPane.ERROR_MESSAGE);
            }
            m_ftQuery.requestFocus();
            return false;
         }
      }
      
      if (m_maxSearchResult >= 0 || !m_unlimitedCheckBox.isSelected())
      {
         String strValue = m_maximumText.getText();
         boolean isMaxValid = true;
         
         try
         {
            int val = Integer.parseInt(strValue);
            if ((val <= 0 || (m_maxSearchResult > 0 && val > m_maxSearchResult))
               && !isQuiet)
            {
               isMaxValid = false;
            }
         }
         catch (Exception e)
         {
            isMaxValid = false;
         }
         
         if (!isMaxValid)
         {
            ErrorDialogs.showErrorDialog(this, 
               PSI18NTranslationKeyValues.getInstance().getTranslationValue(
                  getClass().getName() + "@Invalid maximum results"),
                  PSI18NTranslationKeyValues.getInstance().getTranslationValue(
                     getClass().getName() + "@Validation Error"),
                     JOptionPane.ERROR_MESSAGE);
            m_maximumText.requestFocus();
            
            return false;
         }               
      }

      return true;
   }
   /**
    * Initializes this panel with the data from the supplied PSSearch object if
    * the bDirection is <code>false</code>. Updates the search object if the
    * bDirection is <code>true</code> with the values from the panel.
    * 
    * @param bDirection Supply <code>true</code> to update the data, <code>false
    *    </code>to initialize the panel.
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
            String ftq = m_ftQuery.getText();
            // Remove the property and set it again if it is not empty.
            search.removeProperty(PSSearch.PROP_FULLTEXTQUERY, null);
            if (ftq != null && ftq.trim().length() > 0)
               search.setProperty(PSSearch.PROP_FULLTEXTQUERY, ftq);
         }
         
         // Save the diplay format id
         ApplicationDataComboModel model = (ApplicationDataComboModel)
            m_displayFormatCombo.getModel();
         String strDisplayId = model.getSelectedId();
         if (strDisplayId == null || strDisplayId.trim().length() == 0)
         {
            m_displayFormatCombo.setSelectedIndex(0);
            model = (ApplicationDataComboModel)
               m_displayFormatCombo.getModel();
            strDisplayId = model.getSelectedId();
         }

         search.setDisplayFormatId(strDisplayId);


         // Save the result set size
         if (m_maxSearchResult < 0 && m_unlimitedCheckBox.isSelected())
         {
            search.setMaximumNumber(-1);
         }
         else
         {
            String strValue = m_maximumText.getText();

            try
            {
               int nNum = Integer.parseInt(strValue);
               search.setMaximumNumber(nNum);
            }
            catch (Exception e)
            {
               System.out.println("Error parsing the max results number "
                  + "value. Setting to max value");
               search.setMaximumNumber(PSSearch.DEFAULT_MAX);
            }
         }
      }
      else
      {
         if (m_isEngineAvailable)
         {
            // get previous value of full text query
            m_ftQuery.setText(search.getProperty(
              PSSearch.PROP_FULLTEXTQUERY));
         }
         ((ApplicationDataComboModel) m_displayFormatCombo.getModel()).
            setSelectedId(search.getDisplayFormatId());
         int nMax = search.getMaximumResultSize();

         if (nMax < 1)
         {
            if (m_maxSearchResult < 0)
            {
               m_maximumText.setText("");
               m_maximumText.setEnabled(false);
               m_unlimitedCheckBox.setSelected(true);
            }
            else
            {
               m_maximumText.setText(Integer.toString(m_maxSearchResult));
               m_maximumText.setEnabled(true);
            }
         }
         else
         {
            if (m_maxSearchResult < 0)
               m_unlimitedCheckBox.setSelected(false);
            m_maximumText.setText(Integer.toString(nMax));
         }
      }
   }
   
   /**
    * Validates that the value supplied for the full text query.
    *
    * @param query The query, may be <code>null</code> or emtpy.
    * @param translator the translator used to internationalize the error
    * message, if <code>null</code> is supplied, <code>PSI18nUtils</code>
    * will be used as translator along with the specified locale.
    * @param locale the locale for which to internationalize the error message,
    * may be <code>null</code> or empty in which case the default locale is
    * used. Ignored if the <code>translator</code> is not <code>null</code>.
    *
    * @return <code>null</code> if the query is valid, otherwise a non-
    * <code>null</code> internationalized error message.
    */
   public static String validateFTSSearchQuery(String query,
      PSI18NTranslationKeyValues translator, String locale)
   {
      String msg = null;

      if (query.length() > PSSProperty.VALUE_LENGTH)
      {
         String key = PSSearchSimplePanel.class.getName() +
         "@Search query too long";
         if (translator == null)
         {
            if (locale != null && locale.trim().length() > 0)
               msg = PSI18nUtils.getString(key, locale);
            else
               msg = PSI18nUtils.getString(key);
         }
         else
            msg = translator.getTranslationValue(key);

         msg = MessageFormat.format(msg, new Object[] {String.valueOf(
            PSSProperty.VALUE_LENGTH)});
      }

      return msg;
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
    * Event handler for unlimited checkbox
    */
   public void onUnlimited()
   {
      if (m_unlimitedCheckBox.isSelected())
      {
         m_maximumText.setEnabled(false);
      }
      else
      {
         m_maximumText.setEnabled(true);
      }
   }

   /**
    * A boolean flag to hold whether search engine is enabled
    * or not. <code>true</code> value indicates that search 
    * engine is enabled and query text and coressponding 
    * components will be displayed. 
    */
   private boolean m_isEngineAvailable = false;

   /**
    * Returns the Full text area component.
    * @return JTextArea Full text query text area component.
    */
   public JTextArea getQueryText()
   {
      return m_ftQuery;
   }

   /**
    * The text area to enter the full text search query. 
    * Never <code>null</code> after initialization.
    */
   private JTextArea m_ftQuery = new JTextArea();

   /*
    * Prefix for lookups for I18N strings
    */
   private static final String I18N_PREFIX=PSSearchSimplePanel.class.getName();
   
   private static final String I18N_MAX_ROWS_RETURNED = I18N_PREFIX
         + "@Max rows returned";

   private static final String I18N_UNLIMITED = I18N_PREFIX + "@Unlimited";

   private static final String I18N_DISPLAY_FORMAT = I18N_PREFIX
         + "@Display Format:";

   private static final String I18N_SEARCH_FOR = I18N_PREFIX + "@Search for:";

   private static final String I18N_ALLOWED_RANGE = I18N_PREFIX
         + "@AllowableRange";
   
   /**
    * Component to display display formats. Never <code>null</code>. 
    * Never modified after initialization.
    */
   protected JComboBox m_displayFormatCombo;

   /**
    * Text field for the number of maximum rows returned after performing 
    * the search. Never <code>null</code>. Never modified after initialization.
    */
   protected UTFixedHeightTextField m_maximumText;

   /**
    * Check box to indicate maximum rows returned after performing the search 
    * to be unlimited or limited.  Never <code>null</code>. Never modified after
    * initialization. 
    */
   protected JCheckBox m_unlimitedCheckBox;

   /**
    * A map consiting of displayformat ids and display names. Initialized in ctor
    * never <code>null</code> or empty.
    */
   private Map m_displayFormatMap = null;
   
   /**
    * The max rows returned from a search result. Default to <code>-1</code>
    * as unlimited.
    */
   private int m_maxSearchResult = -1;
}
