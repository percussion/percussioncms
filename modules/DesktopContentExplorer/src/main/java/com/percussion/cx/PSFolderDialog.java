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
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSObjectAcl;
import com.percussion.cms.objectstore.PSObjectAclEntry;
import com.percussion.cms.objectstore.PSObjectPermissions;
import com.percussion.cms.objectstore.PSUserInfo;
import com.percussion.cx.error.PSContentExplorerException;
import com.percussion.cx.objectstore.PSNode;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.guitools.PSDialog;
import com.percussion.guitools.UTStandardCommandPanel;
import com.percussion.util.PSPathUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;

/**
 * The dialog to create and modify Folder options.
 */
public class PSFolderDialog extends PSDialog
{

   /**
    * Constructs the dialog with supplied options.
    *
    * @param parent the parent frame of the dialog, may be <code>null</code>.
    * @param userInfo The info of the user editing or creating the folder,
    * never <code>null</code>.
    * @param folderMgr the manager to handle folder related actions, may not be
    * <code>null</code>
    * @param folderNode the node that represents the folder to modify, supply
    * <code>null</code> to create a new folder.
    * @param parentFolderNode the folder node in which the new folder need to
    * be created or the node that is parent of the supplied folder node, may not
    * be <code>null</code>
    * @param navSelectionPath current content navigation path of the form:
    * //Folder/Folder1/Folder2, never <code>null</code>.
    *
    * @throws PSContentExplorerException if loading the folder being edited is
    * failed
    * @throws PSCmsException if any used cms cataloger fails to fetch needed data
    */
   public PSFolderDialog(Frame parent, PSUserInfo userInfo,
                         PSFolderActionManager folderMgr, PSNode folderNode,
                         PSNode parentFolderNode, String navSelectionPath)
      throws PSContentExplorerException, PSCmsException 

   {
      super(parent, folderMgr.getApplet().getResourceString(
         PSFolderDialog.class,
         folderNode == null ? "Create folder" : "Edit folder"));
      
      setResizable(true);
      
      if(userInfo == null)
         throw new IllegalArgumentException("userInfo may not be null.");

      if(folderMgr == null)
         throw new IllegalArgumentException("folderMgr may not be null.");
      
      if(folderMgr.getApplet() == null)
         throw new IllegalArgumentException("applet may not be null.");

      if(navSelectionPath == null)
         throw new IllegalArgumentException("navSelectionPath may not be null.");

      if(parentFolderNode == null)
         throw new IllegalArgumentException("parentFolderNode may not be null.");

      m_userInfo = userInfo;
      m_folderMgr = folderMgr;
      m_applet = folderMgr.getApplet();
      m_folderNode = folderNode;
      m_parentFolderNode = parentFolderNode;
      m_navSelectionPath = navSelectionPath;

      if(folderNode == null)
      {
         PSFolder parentFolder = m_folderMgr.loadFolder(parentFolderNode);
         if (parentFolder==null)
            throw new IllegalArgumentException("parent folder can not be null");

          //inherit ACL from the parent + add current user as an Admin user.
         PSObjectAclEntry curUserAclEntry = new PSObjectAclEntry(
            PSObjectAclEntry.ACL_ENTRY_TYPE_USER,
            m_userInfo.getUserName(),
            PSObjectAclEntry.ACCESS_ADMIN | PSObjectAclEntry.ACCESS_WRITE |
            PSObjectAclEntry.ACCESS_READ);

         PSObjectAcl parentAcl = (PSObjectAcl)parentFolder.getAcl().clone();

         parentAcl.add(curUserAclEntry);

         m_folder = new PSFolder(m_applet.getResourceString(
            getClass(), "New Folder"), parentFolder.getCommunityId(),
            PSObjectAclEntry.ACCESS_ADMIN, "");

         m_folder.setAcl(parentAcl);
         
         m_folder.setLocale(parentFolder.getLocale());

         m_isNewFolder = true;
      }
      else
      {
         m_folder = m_folderMgr.loadFolder(folderNode);
      }

      initDialog();
   }

   /**
    * Initializes the dialog framework and sets initial state.
    *
    * @throws PSCmsException if any used cms cataloger fails to fetch needed data
    */
   private void initDialog() throws PSCmsException
   {
      PSObjectPermissions perm = m_folder.getPermissions();
      if (!m_isNewFolder && !perm.hasAdminAccess())
      {
         setTitle(m_applet.getResourceString(getClass(),
         "Edit folder - Read only"));
      }

      //create main panel
      JPanel mainPanel = new JPanel(new BorderLayout());
      mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

      //tabbed panel
      JPanel tabbedPanel = new JPanel(new BorderLayout());

      //tabbed pane
      JTabbedPane tabbedPane = new JTabbedPane();

      //create and add tab panels

      //general panel
      String pStr = m_applet.getResourceString(
            getClass(), "General");

      int folderCommId = m_folder.getCommunityId();
      m_fGeneralPanel = new PSFolderGeneralPanel(this,
         m_folder, m_folderMgr, m_folderNode, m_parentFolderNode,
         m_navSelectionPath, m_isNewFolder, 
         perm.hasAdminAccess(), m_userInfo.getCommunityId(), folderCommId);
      char mnemonic = PSContentExplorerApplet.getResourceMnemonic(
         getClass(), "General", 'G');
      int mnemonicIx = pStr.indexOf(mnemonic); 
      
      tabbedPane.add(pStr, m_fGeneralPanel);
      tabbedPane.setDisplayedMnemonicIndexAt(0, mnemonicIx);
      tabbedPane.setMnemonicAt(0, (int)mnemonic);
      tabbedPane.setToolTipTextAt(0, pStr + " tab");
      
      m_publishFolderFlagInitialState = 
         m_fGeneralPanel.isPublishFolderFlagSelected();
      
      //security panel
      pStr = m_applet.getResourceString(
            getClass(), "Security");
      m_fSecurityPanel = new PSFolderSecurityPanel(this,
         m_folder, m_userInfo, m_folderMgr, perm.hasAdminAccess());
      
      tabbedPane.add(pStr, m_fSecurityPanel);
      mnemonic = PSContentExplorerApplet.getResourceMnemonic(
         getClass(), "Security", 'S');
      mnemonicIx = pStr.indexOf(mnemonic);
      tabbedPane.setDisplayedMnemonicIndexAt(1, mnemonicIx);
      tabbedPane.setMnemonicAt(1, (int)mnemonic);
      tabbedPane.setToolTipTextAt(1, pStr + " tab");
      
      //custom panel
      pStr = m_applet.getResourceString(
              getClass(), "Custom");
      
      m_fPropertiesPanel =
         new PSFolderPropertiesPanel(m_folder, perm.hasAdminAccess(), m_applet);
      
      tabbedPane.add(pStr, m_fPropertiesPanel);
      mnemonic = PSContentExplorerApplet.getResourceMnemonic(
         getClass(), "Custom", 'u');
      tabbedPane.setDisplayedMnemonicIndexAt(2, pStr.indexOf(mnemonic));
      tabbedPane.setMnemonicAt(2, (int)(""+mnemonic).toUpperCase().charAt(0));
      tabbedPane.setToolTipTextAt(2, pStr + " tab");
            
      tabbedPanel.add(tabbedPane, BorderLayout.CENTER);
      tabbedPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
      tabbedPanel.setPreferredSize(new Dimension(450, 310));
      
      UTStandardCommandPanel defCommandPanel = new UTStandardCommandPanel(
         this, SwingConstants.RIGHT, true);
      JPanel southPanel = new JPanel();
      southPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));      
      southPanel.add(defCommandPanel, BorderLayout.EAST);
      
      //add tabbed panel and command panel to the main panel
      mainPanel.add(tabbedPanel, BorderLayout.CENTER);
      mainPanel.add(southPanel, BorderLayout.SOUTH);

      //based on the user access level disable or enable OK button.
      JButton jbOk = defCommandPanel.getOkButton();
      jbOk.setEnabled(perm.hasAdminAccess());

      if (perm.hasAdminAccess()) {
         //make OK a default button
         getRootPane().setDefaultButton( jbOk );
      }

      setContentPane(mainPanel);

      pack();
      center();
      setResizable(true);
      
      // Add focus highlights
      PSDisplayOptions dispOptions =
         (PSDisplayOptions)UIManager.getDefaults().get(
            PSContentExplorerConstants.DISPLAY_OPTIONS);
      PSFocusBorder focusBorder = new PSFocusBorder(1, dispOptions);
      focusBorder.addToAllNavigable(mainPanel);
   }

   /**
    * Depending on the folder permissions, invokes onOk on all tab panels,
    * each of which then saves their own data in the shared PSFolder instance.
    * User must have Admin access to be able to modify and persist any data.
    * If all the envoked panels succeed, then uses the FolderActionManager to
    * create or save an updated PSFolder data, then hides and disposes this dialog.
    */
   public void onOk()
   {
      // Switch cursor to the hourglass
      this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

      try
      {

         PSObjectPermissions perm = m_folder.getPermissions();

         if (!m_isNewFolder && !perm.hasAdminAccess())
            return;

         if (!m_fGeneralPanel.onOk())
            return;

         if (!m_fSecurityPanel.onOk())
            return;

         if (!m_fPropertiesPanel.onOk())
            return;

         try
         {
            if (m_isNewFolder)
            {
               PSFolder folder = m_folderMgr.createFolder(
                  m_parentFolderNode, m_folder);
               
               String folderid = folder.getLocator().getPart(PSLocator.KEY_ID);
               m_applet.toggleFlaggedFolder(
                  folderid, folder.isPublishOnlyInSpecialEdition());
            }
            else
            {
               m_folderMgr.modifyFolder(m_folder);
               if (!m_publishFolderFlagInitialState 
                  && m_fGeneralPanel.isPublishFolderFlagSelected()
                  && PSPathUtil.isPathUnderSiteFolderRoot(m_navSelectionPath))
               {
                  m_folderMgr.clearDescendentPublishFlags(m_folder);
               }
            }

         }
         catch(PSCmsException e)
         {
            ErrorDialogs.showErrorMessage(this,
               e.getLocalizedMessage(),
               m_applet.getResourceString(getClass(), "Error") );
            return;
         }
      }
      finally
      {
         this.setCursor(Cursor.getDefaultCursor());
      }

      super.onOk();
   }
   
   /**
    * Get the folder being edited by this dialog
    * 
    * @return The folder, never <code>null</code>.  Modifications to the folder
    * are persisted by {@link #onOk()}
    */
   public PSFolder getFolder()
   {
      return m_folder;
   }

   /**
    * Holds current naviagation tree selection path
    * never <code>null</code>, and never <code>empty</code>.
    */
   private String m_navSelectionPath = "";

   /**
    * The folder to create or edit, initialized in the ctor and modified in
    * <code>onOk()</code> as per user selections. Never <code>null</code> after
    * that.
    */
   private PSFolder m_folder;

   /**
    * The parent folder node of the new folder being created, initialized in the
    * ctor if the dialog is invoked to create a new folder and never modified
    * after that. <code>null</code> if the dialog is invoked to edit the folder.
    */
   private PSNode m_parentFolderNode;

   /**
    * The folder node of the folder being modified, initialized in the
    * ctor and never modified after that. <code>null</code> if the dialog is
    * invoked to create the folder.
    */
   private PSNode m_folderNode;

   /**
    * The manager that handles the creating or updating folders, initialized in
    * the ctor and never <code>null</code> or modified after that.
    */
   private PSFolderActionManager m_folderMgr;

   /**
    * The info of the user editing or creating the folder, initialized in the
    * ctor and never <code>null</code> or modified after that.
    */
   private PSUserInfo m_userInfo;

   /**
    * The flag that specifies whether folder is being created or edited, <code>
    * true</code> indicates new and vice-versa. Initialized in the ctor and
    * never modified after that.
    */
   private boolean m_isNewFolder = false;

   /**
    * FolderGeneralPanel, intialized in {@link #initDialog()}.
    */
   private PSFolderGeneralPanel   m_fGeneralPanel;

   /**
    * FolderSecurityPanel, intialized in {@link #initDialog()}.
    */
   private PSFolderSecurityPanel  m_fSecurityPanel;

   /**
    * FolderPropertiesPanel, intialized in {@link #initDialog()}.
    */
   private PSFolderPropertiesPanel  m_fPropertiesPanel;
   
   /**
    * Indicates the intial state of the publish folder flag checkbox
    * in the general panel. Set in {@link #initDialog}.
    */
   private boolean m_publishFolderFlagInitialState;
   
   /**
    * default serail# avoid warning 
    */
   private static final long serialVersionUID = 1L;
   
   /**
    * A reference back to the applet that initiated the action manager.
    */
   private PSContentExplorerApplet m_applet;
}
