/******************************************************************************
 *
 * [ PSSaveSearchDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.cx;

import com.percussion.border.PSFocusBorder;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.guitools.PSDialog;
import com.percussion.guitools.PSPropertyPanel;
import com.percussion.guitools.UTStandardCommandPanel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.text.MessageFormat;

/**
 * The dialog for entering search details to save a 'New Search'.
 */
public class PSSaveSearchDialog extends PSDialog
{
   /**
    * Constructs the dialog with supplied parent.
    *
    * @param parent the parent frame of this dialog, may be <code>null</code>
    */
   public PSSaveSearchDialog(Frame parent, PSContentExplorerApplet applet)
   {
      super(parent, applet.getResourceString(
         PSSaveSearchDialog.class, "Save Search"));
      
      if (applet == null)
         throw new IllegalArgumentException("applet must not be null");
      m_applet = applet;

      initDialog();
   }

   /**
    * Initializes the dialog framework.
    */
   private void initDialog()
   {
      PSPropertyPanel mainPanel = new PSPropertyPanel();

      m_searchNameField = new JTextField();
      mainPanel.addPropertyRow(m_applet.getResourceString(
         getClass(), "Search Name:"), m_searchNameField);
      m_searchDescField = new JTextArea(10, 50);
      m_searchDescField.setLineWrap(true);
      m_searchDescField.setWrapStyleWord(true);

      mainPanel.addPropertyRow(m_applet.getResourceString(
         getClass(), "Description:"), m_searchDescField);

      m_currentUserRadio = new JRadioButton(
            m_applet.getResourceString(
         getClass(), "Current user"));
      m_currentCommunityRadio = new JRadioButton(
            m_applet.getResourceString(
         getClass(), "Current community"));
      m_allCommunitiesRadio = new JRadioButton(
            m_applet.getResourceString(
         getClass(), "All communities"));
      
      ButtonGroup group = new ButtonGroup();
      group.add(m_currentUserRadio);
      group.add(m_currentCommunityRadio);
      group.add(m_allCommunitiesRadio);

      /*ph: I think it would be better to default to current user, but this
       * is done to maintain backwards behavioral compatibility.
       */
      if(m_applet.isContentRestrict())
         m_currentCommunityRadio.setSelected(true);
      else
         m_allCommunitiesRadio.setSelected(true);
      mainPanel.addPropertyRow(m_applet.getResourceString(
         getClass(), "Show to:"), new JComponent[]
         {m_currentUserRadio, m_currentCommunityRadio, 
         m_allCommunitiesRadio});
      mainPanel.setAlignmentX(LEFT_ALIGNMENT);

      JPanel commandPanel = new JPanel(new BorderLayout());
      UTStandardCommandPanel defCommandPanel = 
          new UTStandardCommandPanel(this, SwingConstants.HORIZONTAL, true);
      commandPanel.add(defCommandPanel, BorderLayout.EAST);
      getRootPane().setDefaultButton(defCommandPanel.getOkButton());
      
      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
      panel.add(mainPanel, BorderLayout.NORTH);
      panel.add(Box.createVerticalStrut(20));
      panel.add(commandPanel, BorderLayout.SOUTH);

      setContentPane(panel);

      pack();
      center();
      setResizable(true);
      
      // Add focus highlights
      PSDisplayOptions dispOptions =
         (PSDisplayOptions)UIManager.getDefaults().get(
            PSContentExplorerConstants.DISPLAY_OPTIONS);
      PSFocusBorder focusBorder = new PSFocusBorder(1, dispOptions);
      focusBorder.addToAllNavigable(panel);      
   }

   /**
    * Validates name is entered for the search, updates the user selections and
    * disposes dialog.
    */
   public void onOk()
   {
      int searchNameLength = m_searchNameField.getText().trim().length();
      if(searchNameLength == 0)
      {
         ErrorDialogs.showErrorMessage(this,
               m_applet.getResourceString(getClass(),
            "The search name must be entered to save new search"),
            m_applet.getResourceString(getClass(), "Error"));
         m_searchNameField.requestFocus();
         return;
      }

      if(searchNameLength > PSSearch.INTERNALNAME_LENGTH)
      {
         ErrorDialogs.showErrorMessage(this,
            MessageFormat.format(
                  m_applet.getResourceString(getClass(),
               "The search name must not exceed {0} characters."),
               new String[] {String.valueOf(PSSearch.INTERNALNAME_LENGTH)} ),
               m_applet.getResourceString(getClass(), "Error"));
         m_searchNameField.requestFocus();
         return;
      }

      int showTo = PSSearch.SHOW_TO_ALL_COMMUNITIES;
      if(m_currentUserRadio.isSelected())
         showTo = PSSearch.SHOW_TO_USER;
      else if(m_currentCommunityRadio.isSelected())
         showTo = PSSearch.SHOW_TO_COMMUNITY;
         
      m_userSearchSel = new SearchDetails(m_searchNameField.getText(),
         m_searchDescField.getText(), showTo);

      super.onOk();
   }

   /**
    * Gets the name entered by the user for the search. Must be called
    * after the dialog is disposed by clicking OK. Use {@link #isOk()} to
    * determine that after the control is passed to the caller from this modal
    * dialog.
    *
    * @return the name, never <code>null</code> or empty.
    */
   public String getSearchName()
   {
      checkOkClicked();
      return m_userSearchSel.m_searchName;
   }

   /**
    * Gets the description entered by the user for the search. Must be called
    * after the dialog is disposed by clicking OK. Use {@link #isOk()} to
    * determine that after the control is passed to the caller from this modal
    * dialog.
    *
    * @return the description, never <code>null</code>, may be empty.
    */
   public String getSearchDesc()
   {
      checkOkClicked();
      return m_userSearchSel.m_searchDesc;
   }

   /**
    * Gets the user selection for visibility of this search to 'Current User'
    * or not. Must be called after the dialog is disposed by clicking OK. Use
    * {@link #isOk()} to determine that after the control is passed to the
    * caller from this modal dialog.
    *
    * @return <code>true</code> if 'Current User' is chosen, <code>false
    * </code> otherwise.
    * 
    * @throws IllegalStateException if called before onOk has been called.
    */
   public boolean isCurrentUser()
   {
      checkOkClicked();
      return (m_userSearchSel.m_showTo==PSSearch.SHOW_TO_USER);
   }

   /**
    * Gets the user selection for visibility of this search to 'Current Community'
    * or not. Must be called after the dialog is disposed by clicking OK. Use
    * {@link #isOk()} to determine that after the control is passed to the
    * caller from this modal dialog.
    *
    * @return <code>true</code> if 'Current Community' is chosen, <code>false
    * </code> otherwise.
    * 
    * @throws IllegalStateException if called before onOk has been called.
    */
   public boolean isCurrentCommunity()
   {
      checkOkClicked();
      return (m_userSearchSel.m_showTo==PSSearch.SHOW_TO_COMMUNITY);
   }

   /**
    * Gets the user selection for visibility of this search to 'All communities'
    * or not. Must be called after the dialog is disposed by clicking OK. Use
    * {@link #isOk()} to determine that after the control is passed to the
    * caller from this modal dialog.
    *
    * @return <code>true</code> if 'All Communities' is chosen, <code>false
    * </code> otherwise.
    * 
    * @throws IllegalStateException if called before onOk has been called.
    */
   public boolean isAllCommunities()
   {
      checkOkClicked();
      return (m_userSearchSel.m_showTo==PSSearch.SHOW_TO_ALL_COMMUNITIES);
   }

   /**
    * Get the show to flag for this search dialog.
    * 
    * @return one of the SHOW_TO_XXXX flags to inidcate the search needs to be 
    * save for the current user, current community or all communities.
    * 
    * @throws IllegalStateException if called before onOk has been called.
    */
   public int getShowTo()
   {
      checkOkClicked();
      return m_userSearchSel.m_showTo;
   }

   /**
    * Checks whether OK is clicked or not by looking at the user selection
    * object.
    *
    * @throws IllegalStateException if the user selection is not set with a
    * value (is <code>null</code>)
    */
   private void checkOkClicked()
   {
      if(m_userSearchSel == null)
         throw new IllegalStateException(
            "User selections can be obtained only after this dialog is" +
            " disposed by clicking OK");
   }

   /**
    * The class to hold user selections in this dialog for search.
    */
   private class SearchDetails
   {
      /**
       * Constructs this object with supplied parameters.
       *
       * @param name name of the search, assumed not <code>null</code> or empty.
       * @param desc description of the search, assumed not <code>null</code>,
       * may be empty.
       * @param isAllCommunities supply <code>true</code> if 'All Communities'
       * is selected by user, otherwise <code>false</code>
       */
      private SearchDetails(String name, String desc, int showTo)
      {
         m_searchName = name;
         m_searchDesc = desc;
         m_showTo = showTo;
      }

      /**
       * The identifier or name of the search, initialized in the ctor and
       * never <code>null</code>, empty or modified after that.
       */
      private String m_searchName;

      /**
       * The description of the search, initialized in the ctor and
       * never <code>null</code>, may be empty and never modified after that.
       */
      private String m_searchDesc;

      /**
       * The visibility constant of the search to current user, current 
       * community or all communities, initialized to one of SHOW_TO_XXXX 
       * constants in the ctor and never modified after that.
       */
      private int m_showTo;
   }

   /**
    * The object to hold user selections, initialized to <code>null</code> and
    * set with user selections when OK is clicked in <code>onOk()</code>.
    */
   private SearchDetails m_userSearchSel = null;

   /**
    * The text field to enter the name for search, initialized in the
    * <code>initDialog()</code> and never <code>null</code> or modified after
    * that.
    */
   private JTextField m_searchNameField;

   /**
    * The text field to enter the description for search, initialized in the
    * <code>initDialog()</code> and never <code>null</code> or modified after
    * that.
    */
   private JTextArea m_searchDescField;

   /**
    * The radio button to choose 'Current User' option for
    * 'Show To ' field, initialized in the <code>initDialog()</code>
    * and never <code>null</code> or modified after that.
    */
   private JRadioButton m_currentUserRadio;

   /**
    * The radio button to choose 'Current Community' option for
    * 'Show To ' field, initialized in the <code>initDialog()</code>
    * and never <code>null</code> or modified after that.
    */
   private JRadioButton m_currentCommunityRadio;

   /**
    * The radio button to choose 'All Communities' option for
    * 'Show To ' field, initialized in the <code>initDialog()</code>
    * and never <code>null</code> or modified after that.
    */
   private JRadioButton m_allCommunitiesRadio;
   
   /**
    * A reference back to the applet that initiated this action manager.
    */
   private PSContentExplorerApplet m_applet;
   
}
