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

package com.percussion.cx;

import com.percussion.border.PSFocusBorder;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSDisplayFormat;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cx.catalogers.PSCommunityCataloger;
import com.percussion.cx.objectstore.PSNode;
import com.percussion.design.objectstore.PSEntry;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.guitools.PSAccessibleActionListener;
import com.percussion.guitools.PSPropertyPanel;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.UIManager;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * General Panel on the Cx Folder Properties Dialog.
 * Allows users to change folder name, community, description and display format.
 */
public class PSFolderGeneralPanel extends PSPropertyPanel
{
   /**
    * The only constructor.
    * 
    * @param parentDialog parent dialog, may be <code>null</code>.
    * @param folder shared instance of the PSFolder, never <code>null</code>.
    * @param folderMgr the manager to handle folder related actions, may not be
    * <code>null</code>
    * @param folderNode the node that represents the folder to modify, may be
    * <code>null</code> if this is a new folder.
    * @param parentFolderNode the folder node in which the new folder need to be
    * created or the node that is parent of the supplied folder node, may not be
    * <code>null</code>
    * @param navSelectionPath current content navigation path, such as //Folder/
    * Folder1/Folder2, never <code>null</code>.
    * @param isNewFolder <code>true</code> if a new folder, <code>false</code>
    * otherwise.
    * @param editable <code>true</code> if any data can be entered,
    * <code>false</code> otherwise.
    * @param userCommunityId the community id of the current session.
    * @param folderCommId community id of the current folder, -1 if it is a new
    * folder or any community (all communities).
    * 
    * @throws PSCmsException if any cataloger fails to fetch data from the
    * server
    */
   public PSFolderGeneralPanel(Dialog parentDialog,
      PSFolder folder, PSFolderActionManager folderMgr,
      PSNode folderNode, PSNode parentFolderNode,
      String navigationPath, boolean isNewFolder, boolean editable,
      int userCommunityId, int folderCommId)

      throws PSCmsException
   {
      if (folder==null)
         throw new IllegalArgumentException("folder may not be null");
      if (folderMgr==null)
         throw new IllegalArgumentException("folderMgr may not be null");
      if (parentFolderNode==null)
         throw new IllegalArgumentException("parentFolderNode may not be null");
      if (folderMgr.getApplet()==null)
         throw new IllegalArgumentException("applet may not be null");

      m_parentDialog = parentDialog;
      m_folder = folder;
      m_folderMgr = folderMgr;
      m_folderNode = folderNode;
      m_parentFolderNode = parentFolderNode;
      m_navSelectionPath = navigationPath;
      m_isNewFolder = isNewFolder;
      m_editable = editable;
      m_applet = folderMgr.getApplet();

      init(userCommunityId, folderCommId);
   }

   /**
    * Initializes this panel's UI
    * 
    * @param currCommunityId the community id of the current session.
    * @param folderCommId community id of the current folder, -1 if it is a new
    * folder or any community (all communities).
    * 
    * @throws PSCmsException if one of the used catalogers fail.
    */
   private void init(int currCommunityId, int folderCommId) throws PSCmsException
   {
      setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

      m_textFolderName = new JTextField(m_folder.getName());
      m_textFolderName.setEditable(m_editable);
      String accStr =
            m_applet.getResourceString(getClass(), "Folder Name:");
      setAccessibleInfo(m_textFolderName, accStr);

      addPropertyRow(
         accStr,
         m_textFolderName,
         PSContentExplorerApplet.getResourceMnemonic(
            getClass(),
            "@Folder Name:", 'N'));
      m_textFolderName.setEnabled(m_editable);

      if (m_isNewFolder && m_editable)
      {
         //select and set focus on the folder name
         m_textFolderName.selectAll();

         if (m_parentDialog!=null)
         {
            m_parentDialog.addWindowListener( new WindowAdapter() {
                  @Override
                  public void windowOpened( WindowEvent e ){
                     m_textFolderName.requestFocus();
                  }
               }
            );
         }
      }

     JTextField folderIdTextField =
        new JTextField(m_folder.getLocator().getPart(PSLocator.KEY_ID));
           folderIdTextField.setEnabled(false);

     addPropertyRow(
           m_applet.getResourceString(getClass(), "Folder ID:"),
           folderIdTextField);

      initCommunityComboBox(currCommunityId, folderCommId);

      JTextField textLocation = new JTextField(m_navSelectionPath);
      textLocation.setEnabled(false);
      textLocation.setEditable(false);
      accStr = m_applet.getResourceString(
            getClass(), "Location:");
      setAccessibleInfo( textLocation, accStr);

      addPropertyRow( accStr, textLocation,
      PSContentExploreAppletUtils.getResourceMnemonic(
         getClass(), "@Location:", 'L'));

      m_textFolderDescription = new JTextField(m_folder.getDescription());
      m_textFolderDescription.setEditable(m_editable);
      accStr = m_applet.getResourceString(
            getClass(), "Description:");
      setAccessibleInfo( m_textFolderDescription, accStr);

      addPropertyRow( accStr, m_textFolderDescription,
      PSContentExploreAppletUtils.getResourceMnemonic(
         getClass(), "@Description:", 'D'));
      m_textFolderDescription.setEnabled(m_editable);

      m_comboFolderLocale = new JComboBox();
      m_comboFolderLocale.setPreferredSize(new Dimension(150,20));
      m_comboFolderLocale.setMaximumSize(new Dimension(150,20));
      m_comboFolderLocale.setEditable(false);
      m_comboFolderLocale.addActionListener(new PSAccessibleActionListener());
      accStr = m_applet.getResourceString(
            getClass(), "Locale:");
      setAccessibleInfo(m_comboFolderLocale, accStr);


      addPropertyRow( accStr, m_comboFolderLocale,
      PSContentExploreAppletUtils.getResourceMnemonic(
         getClass(), "@Locale:", 'e'));

      Iterator iter = m_folderMgr.getLocaleCataloger().getLocales();
      while (iter.hasNext())
      {
         PSEntry element = (PSEntry) iter.next();
         m_comboFolderLocale.addItem(element);
         if(element.getValue().equals(m_folder.getLocale()))
            m_comboFolderLocale.setSelectedItem(element);
      }
      m_comboFolderLocale.setEnabled(m_isNewFolder && m_editable);

      m_comboDisplayFormat = new JComboBox();
      m_comboDisplayFormat.setPreferredSize(new Dimension(150,20));
      m_comboDisplayFormat.setMaximumSize(new Dimension(150,20));
      m_comboDisplayFormat.setEditable(false);
      m_comboDisplayFormat.setEnabled(m_editable);
      m_comboDisplayFormat.addActionListener(new PSAccessibleActionListener());

      accStr = m_applet.getResourceString(
            getClass(), "Default display format:");
      setAccessibleInfo(m_comboDisplayFormat, accStr);

      addPropertyRow( accStr, m_comboDisplayFormat,
      PSContentExploreAppletUtils.getResourceMnemonic(
         getClass(), "@Default display format:", 'f'));

      iter = m_folderMgr.getDisplayFormats();

      //sort disp formats
      List dispFormats = new ArrayList();
      while(iter.hasNext())
         dispFormats.add(iter.next());

      class DispFormatComparator implements Comparator
      {
         public DispFormatComparator() {}

         public int compare(Object left, Object right)
         {
            return left.toString().compareTo(right.toString());
         }
      }

      Collections.sort(dispFormats, new DispFormatComparator());

      iter = dispFormats.iterator();

      while(iter.hasNext())
         m_comboDisplayFormat.addItem(iter.next());
      if(m_isNewFolder)
         m_comboDisplayFormat.setSelectedIndex(0);
      else
      {
         PSDisplayFormat format = m_folderMgr.getDisplayFormatById(
            m_folder.getDisplayFormatPropertyValue(), true);

         m_comboDisplayFormat.setSelectedItem(format);
      }

      // Add focus highlights
      PSDisplayOptions dispOptions =
         (PSDisplayOptions)UIManager.getDefaults().get(
            PSContentExplorerConstants.DISPLAY_OPTIONS);
      PSFocusBorder focusBorder = new PSFocusBorder(1, dispOptions);
      focusBorder.addToAllNavigable(this);

      // global template selector
      m_comboGlobalTemplate = new JComboBox();
      m_comboGlobalTemplate.setPreferredSize(new Dimension(150,20));
      m_comboGlobalTemplate.setMaximumSize(new Dimension(150,20));
      m_comboGlobalTemplate.setEditable(false);
      m_comboGlobalTemplate.setEnabled(m_editable);

      m_comboGlobalTemplate.addActionListener(new PSAccessibleActionListener());
      String globalTemplateAccStr = m_applet.getResourceString(
         getClass(), "Global Template:");
      setAccessibleInfo(m_comboGlobalTemplate, globalTemplateAccStr);

      // Publish flag check box
      boolean shouldPublish = m_folder.isPublishOnlyInSpecialEdition();
      m_checkBoxPublishFolderFlag = new JCheckBox();
      accStr = m_applet.getResourceString(
               getClass(), "Publish Folder with Site:");
      setAccessibleInfo( m_checkBoxPublishFolderFlag, accStr);

      // Only display if this is a site folder
      if (m_navSelectionPath.startsWith("//Sites"))
      {
         addPropertyRow(globalTemplateAccStr, m_comboGlobalTemplate,
            PSContentExploreAppletUtils.getResourceMnemonic(getClass(),
               "@Global Template:", 'T'));

         Iterator globalTemplates = m_folderMgr.getGlobalTemplateCataloger()
            .getGlobalTemplates().iterator();
         m_comboGlobalTemplate.addItem(m_applet.getResourceString(
            getClass(), "Use default"));
         while (globalTemplates.hasNext())
            m_comboGlobalTemplate.addItem(globalTemplates.next().toString());

         String globalTemplate = m_folder.getGlobalTemplateProperty();
         if (globalTemplate == null || globalTemplate.trim().length() == 0)
            m_comboGlobalTemplate.setSelectedIndex(0);
         else
            m_comboGlobalTemplate.setSelectedItem(globalTemplate);

         // Folder is implied to be marked if any ancestor is marked
         boolean isImpliedMarked =
            PSCxUtil.shouldFolderBeMarked(
               m_folderNode, m_parentFolderNode, true, m_applet);
         m_checkBoxPublishFolderFlag.setSelected(shouldPublish || isImpliedMarked);
         if(isImpliedMarked)
            m_checkBoxPublishFolderFlag.setEnabled(false);
         addPropertyRow( accStr, m_checkBoxPublishFolderFlag,
            PSContentExploreAppletUtils.getResourceMnemonic(
               getClass(), "@Publish Folder with Site:", 'P'));
      }
   }

   /**
    * Initialize the community combo box which contains all community and the
    * community of the current session. This is to avoid the folder not visible
    * by the current user after the creating or modifying the folder.
    * 
    * @param currCommunityId the community id of the current session.
    * @param folderCommId community id of the current folder, -1 if it is a new
    * folder or any community (all communities).
    * 
    * @throws PSCmsException if error occurs.
    */
   private void initCommunityComboBox(int currCommunityId, int folderCommId)
      throws PSCmsException
   {
      m_comboFolderCommunity = new JComboBox();
      m_comboFolderCommunity.setPreferredSize(new Dimension(150,20));
      m_comboFolderCommunity.setMaximumSize(new Dimension(150,20));
      m_comboFolderCommunity.setEditable(false);
      m_comboFolderCommunity.setEnabled(m_editable);
      m_comboFolderCommunity.addActionListener(new PSAccessibleActionListener());
      String accStr = m_applet.getResourceString(
            getClass(), "Folder Community:");
      setAccessibleInfo( m_comboFolderCommunity, accStr);
      addPropertyRow(
         accStr, m_comboFolderCommunity,
         PSContentExploreAppletUtils.getResourceMnemonic(
            getClass(), "@Folder Community:", 'M'));
      PSCommunityCataloger commCataloger = m_folderMgr.getCommunityCataloger();
      Iterator itComm = commCataloger.getCommunities().iterator();

      /*make an extra instance of the Community class, so that we
        can add a "All" entry to the combo box with the id = -1.
        The commCataloger itself is not holding to this new community instance.
      */
      PSCommunityCataloger.Community commAll = commCataloger.createCommunity(-1,
            m_applet.getResourceString(getClass(),
         "All communities"), "");

      m_comboFolderCommunity.addItem(commAll);

      PSCommunityCataloger.Community selComm = null;

      // add only one entry for the current community and pick the community to
      // select
      while(itComm.hasNext())
      {
         PSCommunityCataloger.Community comm =
            (PSCommunityCataloger.Community)itComm.next();

         if (comm.getId() == currCommunityId)
         {
            m_comboFolderCommunity.addItem(comm);
         }
         if (comm.getId() == folderCommId)
         {
            selComm = comm;
         }
      }
      if (folderCommId==-1)
         selComm = commAll;
      //set community selection
      m_comboFolderCommunity.setSelectedItem(selComm);
   }

   /**
    * Helper method used by init() for setting accessibility info
    * @param jcmp cannot be <code>null</code>
    * @param accStr cannot be <code>null</code>
    */
   private void setAccessibleInfo(JComponent jcmp, String accStr)
   {
      jcmp.getAccessibleContext().setAccessibleDescription(accStr);
      jcmp.getAccessibleContext().setAccessibleName(accStr);
      jcmp.setToolTipText(accStr);
   }

   /**
    * Folder Dialog calls this method when user pushes OK button to save changes.
    * Depending on the folder ACL and user permissions the data may or may not
    * be saved.
    * @return <code>true</code> if success, <code>false</code> otherwise
    */
   public boolean onOk()
   {
      if (!m_editable)
         return false;

      if(m_textFolderName.getText().trim().length() == 0)
      {
         ErrorDialogs.showErrorMessage(m_parentDialog,
               m_applet.getResourceString(getClass(),
            "The folder name can not be empty."),
            m_applet.getResourceString(getClass(), "Error") );
         m_textFolderName.requestFocus();
         return false;
      }

      if (m_isNewFolder)
      {
         //Validate that it is not a duplicate name
         Iterator children = m_parentFolderNode.getChildren();
         if(children != null)
         {
            while(children.hasNext())
            {
               PSNode child = (PSNode)children.next();
               //If we are editing a folder, we should ignore that folder node
               //for comparison.
               if(child.isOfType(PSNode.TYPE_FOLDER) && child != m_folderNode)
               {
                  if(child.getName().equalsIgnoreCase(m_textFolderName.getText()))
                  {
                     m_applet.displayErrorMessage(m_parentDialog,
                        getClass(), "The folder with name <{0}> already exists. " +
                        "Please choose another name.",
                        new String[]{child.getName()}, "Error", null);

                     m_textFolderName.requestFocus();
                     m_textFolderName.selectAll();

                     return false;
                  }
               }
            }
         }
      }

      // Validate that the name does not contain a forward slash
      int slashpos = m_textFolderName.getText().indexOf('/');
      if (slashpos >= 0)
      {
         m_applet.displayErrorMessage(m_parentDialog,
               getClass(), "The name you have supplied, <{0}>, contains invalid " +
                     "characters",
               new String[]{m_textFolderName.getText()}, "Error", null);
         m_textFolderName.requestFocus();
         m_textFolderName.setCaretPosition(slashpos);
         return false;
      }

      //set folder name
      m_folder.setName(m_textFolderName.getText().trim());

      //set folder community id
      PSCommunityCataloger.Community selectedComm =
         (PSCommunityCataloger.Community)m_comboFolderCommunity.getSelectedItem();

      m_folder.setCommunityId(selectedComm.getId());

      //set folder description
      m_folder.setDescription(m_textFolderDescription.getText());

      //set folder locale
      PSEntry entry = (PSEntry) m_comboFolderLocale.getSelectedItem();
      m_folder.setLocale(entry.getValue());

      //set display format
      PSDisplayFormat format =
         (PSDisplayFormat)m_comboDisplayFormat.getSelectedItem();

      m_folder.setDisplayFormatPropertyValue(
         String.valueOf(format.getDisplayId()));

      // set global template
      String globalTemplate = null;
      Object selected = m_comboGlobalTemplate.getSelectedItem();
      if (selected != null)
      {
         globalTemplate = selected.toString();
         if (globalTemplate.equals(m_applet.getResourceString(
            getClass(), "Use default")))
            globalTemplate = null;
      }
      m_folder.setGlobalTemplateProperty(globalTemplate);

      //Folder is implied to be marked if any ancestor is marked
      boolean isImpliedMarked =
         PSCxUtil.shouldFolderBeMarked(
            m_folderNode, m_parentFolderNode, true, m_applet);
      //set folder publishing property
      if(!isImpliedMarked)
      {
         m_folder.setPublishOnlyInSpecialEdition(isPublishFolderFlagSelected());
         String folderid = m_folder.getLocator().getPart(PSLocator.KEY_ID);
         // Modify the applet's flagged folder set so that
         // the proper folder icon is displayed
         if(folderid != null && folderid.trim().length() > 0)
            m_applet.toggleFlaggedFolder(
               folderid, m_checkBoxPublishFolderFlag.isSelected());
      }

      return true;
   }

   /**
    * Indicates that the publish folder flag checkbox is selected
    * @return <code>true</code> if the checkbox is selected
    */
   public boolean isPublishFolderFlagSelected()
   {
      return m_checkBoxPublishFolderFlag.isSelected();
   }

   /**
    * refrence to the parent dialog, invariant, used to supply to the child
    * error dialog.
    */
   private Dialog m_parentDialog;

   /**
    * The flag that specifies whether folder is being created or edited, <code>
    * true</code> indicates new and vice-versa. Initialized in the ctor and
    * never modified after that.
    */
   private boolean m_isNewFolder = false;

   /**
    * Holds current naviagation tree selection path.
    * never <code>null</code>, may be <code>empty</code>
    */
   private String m_navSelectionPath;

   /**
    *  <code>true</code> indicates that user can make and save modifications
    *  to any control on this panel, <code>false</code> otherwise.
    */
   private boolean m_editable;

   /**
    * The folder to create or edit, initialized in the ctor and modified in
    * <code>onOk()</code> as per user selections. Never <code>null</code> after
    * that.
    */
   private PSFolder m_folder;

   /**
    * The manager that handles the creating or updating folders, initialized in
    * the ctor and never <code>null</code> or modified after that.
    */
   private PSFolderActionManager m_folderMgr;

   /**
    * The parent folder node of the new or existing folder being created or
    * modified, initialized in the ctor, never modified after that.
    */
   private PSNode m_parentFolderNode;

   /**
    * The folder node of the folder being modified, initialized in the
    * ctor and never modified after that. <code>null</code> if the dialog is
    * invoked to create the folder.
    */
   private PSNode m_folderNode;

   /**
    * The text field to enter the folder name, initialized in <code>initDialog()
    * </code> and never <code>null</code> after that.
    */
   private JTextField m_textFolderName;

   /**
    * Dropdown combo element that allows user to set a community for a new folder
    * or change community for an existing one. Loaded from the data returned
    * by the PSCommunityCataloger. Initilized in <code>
    * initDialog()</code> and never <code>null</code> after that.
    */
   private JComboBox m_comboFolderCommunity = null;

   /**
    * The text field to enter the folder description, initialized in <code>
    * initDialog()</code> and never <code>null</code> after that.
    */
   private JTextField m_textFolderDescription = null;

   /**
    * Dropdown list box that allows user to choose a locale for the new
    * folder, initialized in <code> initDialog()</code> and never
    * <code>null</code> after that.
    */
   private JComboBox m_comboFolderLocale = null;

   /**
    * The combo-box to show all display formats available for folders,
    * initialized in <code>initDialog()</code> and never <code>null</code> or
    * modified after that.
    */
   private JComboBox m_comboDisplayFormat = null;

   /**
    * The checkbox representing a folder publish flag, initialized in
    * <code> initDialog()</code> and never <code>null</code> after that.
    */
   private JCheckBox m_checkBoxPublishFolderFlag = null;

   /**
    * The combo-box to show all global templates available, initialized in
    * <code>initDialog()</code> and never <code>null</code> or modified after
    * that.
    */
   private JComboBox m_comboGlobalTemplate = null;

   /**
    * default serial# to avoid warning
    */
   private static final long serialVersionUID = 1L;
   
   /**
    * A reference back to the applet that initiated the action manager.
    */
   private PSContentExplorerApplet m_applet;
}
