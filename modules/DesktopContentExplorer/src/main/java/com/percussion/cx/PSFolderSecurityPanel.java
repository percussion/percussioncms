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
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSFolderAcl;
import com.percussion.cms.objectstore.PSFolderPermissions;
import com.percussion.cms.objectstore.PSObjectAcl;
import com.percussion.cms.objectstore.PSObjectAclEntry;
import com.percussion.cms.objectstore.PSObjectPermissions;
import com.percussion.cms.objectstore.PSUserInfo;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.guitools.PSAccessibleListSelectionListener;
import com.percussion.util.PSLineBreaker;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * This Folder Security Panel loads a list of ACL entries from the PSFolder
 * and allows user to modify permissions for individual ACLs, add and remove
 * ACL entries. Depending on the Merged User permissions it either allows to
 * change and save entries or provides a read-only access.
 */
public class PSFolderSecurityPanel extends JPanel implements ActionListener
{
   /**
    * The only constructor of the security panel.
    * @param parentDialog parent dialog, may be <code>null</code>.
    * @param folder shared instance of the PSFolder, never <code>null</code>.
    * @param userInfo The info of the user editing or creating the folder
    * , never <code>null</code>.
    * @param folderMgr the manager to handle folder related actions, may not be
    * <code>null</code>
    * @param enabled <code>true</code> if any data can be entered,
    * <code>false</code> otherwise.
    */
   public PSFolderSecurityPanel(Dialog parentDialog, PSFolder folder,
      PSUserInfo userInfo, PSFolderActionManager folderMgr, boolean enabled)
   {
      if (folder==null)
         throw new IllegalArgumentException("folder may not be null");

      if (userInfo==null)
         throw new IllegalArgumentException("userInfo may not be null");

      if (folderMgr==null)
         throw new IllegalArgumentException("folderMgr may not be null");

      m_parentDialog = parentDialog;
      m_folder = folder;
      m_userInfo = userInfo;
      m_folderMgr = folderMgr;
      m_enabled = enabled;
      m_applet = folderMgr.getApplet();

      init();
   }

   /**
    * creates and initilizes UI components of this panel and loads the data
    */
   private void init()
   {
      setLayout(new BorderLayout());

      JPanel northPanel = new JPanel(new BorderLayout());
      northPanel.add(createAclListPanel(), BorderLayout.CENTER);
      northPanel.add(createPermissionsPanel(), BorderLayout.EAST);
      northPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

      JPanel southPanel = new JPanel();
      southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.X_AXIS));
      southPanel.add(createButtonPanel());
      southPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

      add(northPanel, BorderLayout.CENTER);
      add(southPanel, BorderLayout.SOUTH);

      loadAclList(m_folder.getAcl());
   }

   /**
    * Folder Dialog calls this method when user pushes OK button to save changes.
    * Depending on the folder ACL and user permissions the data may or may not
    * be saved.
    * @return <code>true</code> if success, <code>false</code> otherwise
    */
   @SuppressWarnings("unchecked")
   public boolean onOk()
   {
      if (!m_enabled)
         return true;

      DefaultListModel aclListModel =
         (DefaultListModel)m_aclList.getModel();

      Enumeration enumAcls = aclListModel.elements();

      Collection collAcls = new ArrayList();

      while(enumAcls.hasMoreElements())
      {
         collAcls.add(enumAcls.nextElement());
      }

      PSLocator locator = m_folder.getLocator();
      PSFolderAcl folderAcl = new PSFolderAcl(
         locator.getId(), m_folder.getCommunityId());
      folderAcl.addAll(collAcls);

      //warn user if he happens to lock himslef out of this folder
      if (m_modified)
      {
         int userResponse = warnOnLockingYourselfOut(folderAcl);
         if (userResponse != JOptionPane.YES_OPTION)
         {
             //Make sure the focus is on this panel if user chooses no
             Component comp = getParent();
             if(comp instanceof JTabbedPane)
               ((JTabbedPane)comp).setSelectedComponent(this);
             return false;
         }
      }
      
      // set the modified ACL
      m_folder.getAcl().addAll(collAcls);
      m_modified = false;

      return true;
   }

   /**
    * Analyzes the ACL and warns the user if he happens to lock himslef
    * out of this folder.
    *
    * @param acl the folder ACL object with a set of ACL entries to analyze,
    * assumed not <code>null</code>
    *
    * @return JOptionPane response, such as YES_OPTION or NO_OPTION
    */
   private int warnOnLockingYourselfOut(PSFolderAcl acl)
   {
      int option = JOptionPane.YES_OPTION;
      int permissions = getUserPermission(acl);
      PSFolderPermissions folderPerm = new PSFolderPermissions(permissions);

      String messageKey = null;
      
      if(acl.size()<1)
      {
          messageKey = "@ACL entry list is empty";
      }
      else if (!folderPerm.hasReadAccess())
      {
         messageKey = "@Folder Access Denied";
      }
      else if (!folderPerm.hasAdminAccess())
      {
         messageKey = "@Are you sure you want to limit access for yourself?";
      }

      if (messageKey != null)
      {
         String msg = m_applet.getResourceString(
            getClass(), messageKey);
         PSLineBreaker breaker = new PSLineBreaker(msg, 80, 5);
         
         option = JOptionPane.showConfirmDialog(m_parentDialog,
            breaker.getLines(),
            m_applet.getResourceString(getClass(), "@Warning"),
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
      }
      return option;
   }

   /**
    * Returns the current user's access level for the folder whose ACL is
    * specified by <code>acl</code>. However this access level is not accurate
    * for the Rhythmyx super user, because there is no way to know if the
    * current user is a super user on the client (Content Explorer Applet) side.
    *
    * @param acl contains folder ACL entries which define the permission for a
    * user, role or virtual entry, assumed not <code>null</code>
    *
    * @return the access level calculated using the specified ACL
    */
   private int getUserPermission(PSFolderAcl acl)
   {
      // if no ACL is specified then everyone has all the permissions
      // this is also for backwards compatibility
      int permissions = PSObjectPermissions.ACCESS_DENY;

      if (acl.isEmpty())
      {
         permissions = PSObjectPermissions.ACCESS_ALL;
      }
      else
      {
         PSObjectAclEntry objectAclEntry = new PSObjectAclEntry(
            PSObjectAclEntry.ACL_ENTRY_TYPE_USER,
            m_userInfo.getUserName(),
            PSObjectAclEntry.ACCESS_DENY);

         objectAclEntry = (PSObjectAclEntry)acl.get(objectAclEntry);
         if (objectAclEntry != null)
         {
            // if user specific ACL entry is found then it solely
            // determines the user's privileges on the object and no
            // additional calculation is necessary
            permissions = objectAclEntry.getPermissions();
         }
         else
         {
            Iterator it = acl.iterator();
            while (it.hasNext())
            {
               PSObjectAclEntry aclEntry = (PSObjectAclEntry)it.next();
               if (aclEntry != null)
               {
                  switch (aclEntry.getType())
                  {
                     case PSObjectAclEntry.ACL_ENTRY_TYPE_USER:
                        // already processed the user.
                        break;

                     case PSObjectAclEntry.ACL_ENTRY_TYPE_ROLE:
                        String aclRole = aclEntry.getName();
                        Iterator itRole = m_userInfo.getRoles();
                        while (itRole.hasNext())
                        {
                           String userRole = (String)itRole.next();
                           if (userRole.equalsIgnoreCase(aclRole))
                              permissions = permissions |
                                 aclEntry.getPermissions();
                        }
                        break;

                     case PSObjectAclEntry.ACL_ENTRY_TYPE_VIRTUAL:
                        String virEntryName = aclEntry.getName();
                        if (virEntryName.equalsIgnoreCase(
                           PSObjectAclEntry.ACL_ENTRY_FOLDER_COMMUNITY))
                        {
                           int folderCommunity = acl.getCommunityId();
                           int userCommunity = m_userInfo.getCommunityId();
                           if ((folderCommunity == userCommunity) ||
                              (folderCommunity == -1))
                           {
                              permissions = permissions |
                                 aclEntry.getPermissions();
                           }
                        }
                        else if (virEntryName.equalsIgnoreCase(
                           PSObjectAclEntry.ACL_ENTRY_EVERYONE))
                        {
                           permissions = permissions |
                              aclEntry.getPermissions();
                        }
                        break;

                     default:
                        break;
                  }
               }
            }
         }
      }
      return permissions;
   }

   /**
    * Custom Cell Renderer for displaying ACL roles or members in List.
    */
   private class AclListCellRenderer extends DefaultListCellRenderer
   {
      /**
       * Gets cell renderer component for cell in the list. Sets an icon,
       * text to the cell and tooltip if the source list is role member list
       *
       * @see ListCellRenderer#getListCellRendererComponent
       **/
      public Component getListCellRendererComponent(JList list, Object value,
         @SuppressWarnings("unused")
         int index, boolean isSelected, boolean cellHasFocus)
      {
         PSObjectAclEntry aclEntry = null;

         if(value instanceof PSObjectAclEntry)
         {
            aclEntry = (PSObjectAclEntry)value;

            switch (aclEntry.getType())
            {
               case PSObjectAclEntry.ACL_ENTRY_TYPE_USER:
                  setIcon (PSImageIconLoader.loadIcon("user_member", false, m_applet));
                  break;
               case PSObjectAclEntry.ACL_ENTRY_TYPE_ROLE:
                  setIcon (PSImageIconLoader.loadIcon("group_member", false, m_applet));
                  break;
               case PSObjectAclEntry.ACL_ENTRY_TYPE_VIRTUAL:
                  setIcon (PSImageIconLoader.loadIcon("virtual_role", false, m_applet));
                  break;
               default:
               break; //shouldn't happen
            }

            setText(aclEntry.getName());
         }
         else
            setText(value.toString());

         if (isSelected) {

            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());

            if (aclEntry!=null) {
               updateCheckboxes(aclEntry);
            }
         }
         else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
         }
         
         // Add focus highlights
         PSDisplayOptions dispOptions =
            (PSDisplayOptions)UIManager.getDefaults().get(
               PSContentExplorerConstants.DISPLAY_OPTIONS);  
         if (cellHasFocus)
         {
            setBorder(new PSFocusBorder(1, dispOptions, true));
         }
         else
         {
            setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
         }           

         return this;
      }
   }

   /**
    * Custom MouseMotionListener for handling mouse moves over ACLs in the
    * ACL list and displaying tooltips with provider type and instance name.
    */
    private class AclListMouseMotion extends MouseMotionAdapter
    {
      public void mouseMoved(MouseEvent event)
      {
         if (event.getSource() == m_aclList)
         {
            DefaultListModel aclListModel =
               (DefaultListModel)m_aclList.getModel();

            int index = m_aclList.locationToIndex(event.getPoint());

            if (index > -1 && index < aclListModel.size())
            {
               PSObjectAclEntry aclEntry =
                  (PSObjectAclEntry)m_aclList.getModel().getElementAt(index);

               String tooltip = getToolTipText(aclEntry);

               m_aclList.setToolTipText(tooltip);
            }
            else
               m_aclList.setToolTipText(null);
         }
      }

      /**
       * Depending on the ACL Entry type creates i18n ready tooltip text.
       * @param aclEntry currenly selected ACL Entry, may be <code>null</code>
       * @return tooltip string, may be <code>null</code>.
       */
      private String getToolTipText(PSObjectAclEntry aclEntry)
      {
         if (aclEntry==null)
            return null;

         String tooltip = "";

         switch (aclEntry.getType())
         {
            case PSObjectAclEntry.ACL_ENTRY_TYPE_ROLE:
               tooltip += m_applet.getResourceString(
                  getClass(), "role:");
               break;
            case PSObjectAclEntry.ACL_ENTRY_TYPE_USER:
               tooltip += m_applet.getResourceString(
                  getClass(), "user:");
               break;
            case PSObjectAclEntry.ACL_ENTRY_TYPE_VIRTUAL:
               tooltip += m_applet.getResourceString(
                  getClass(), "virtual:");
               break;
         }

         tooltip += aclEntry.getName();

         return tooltip;
      }
   }

   /**
    * Creates a new ACL list panel.
    *
    * @return ACL list panel with the embedded JList as a main UI component,
    * never <code>null</code>
    */
   private JPanel createAclListPanel()
   {
      JPanel aclPanel = new JPanel();
      aclPanel.setLayout(new BoxLayout(aclPanel, BoxLayout.Y_AXIS));
      aclPanel.setAlignmentY(CENTER_ALIGNMENT);

      String name = m_applet.getResourceString(
            getClass(), "Names");
      JLabel label = new JLabel(name, SwingConstants.LEFT);
      label.setAlignmentX(LEFT_ALIGNMENT);
      

      aclPanel.add(label);
      aclPanel.add(Box.createVerticalStrut(5));

      m_aclList = new JList(new DefaultListModel());
      m_aclList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      m_aclList.addMouseMotionListener(new AclListMouseMotion());
      m_aclList.setCellRenderer(new AclListCellRenderer());
      m_aclList.addListSelectionListener(new PSAccessibleListSelectionListener());
      
      // Accessible info on label..
      label.setLabelFor(m_aclList);
      label.getAccessibleContext().setAccessibleDescription(name);
      label.getAccessibleContext().setAccessibleName(name);
      
      JScrollPane aclListScroller = new JScrollPane(m_aclList);
      aclListScroller.setPreferredSize(new Dimension(200,200));
      aclListScroller.setMinimumSize(new Dimension(150, 100));
      aclListScroller.setMaximumSize(new Dimension(200, Short.MAX_VALUE));
      aclListScroller.setAlignmentX(LEFT_ALIGNMENT);

      aclPanel.add(aclListScroller);

      m_aclList.addKeyListener( new KeyAdapter()
      {
         public void keyPressed (KeyEvent e)
         {
            if (e.getKeyCode() == KeyEvent.VK_DELETE)
               onRemove();
         }
      });

      return aclPanel;
   }

   /**
    * Creates a new Permissions Panel with a group of CheckBoxes, that represent
    * a set of permissions for a currently selected ACL entry.
    *
    * @return Permissions Panel with a group of CheckBoxes
    * , never <code>null</code>
    */
   private JPanel createPermissionsPanel()
   {
      JPanel permPanel = new JPanel(new BorderLayout());

      JPanel cbPanel = new JPanel();
      cbPanel.setLayout(new BoxLayout(cbPanel, BoxLayout.Y_AXIS));
      cbPanel.setBorder(new TitledBorder(
         new EtchedBorder(EtchedBorder.LOWERED),
         m_applet.getResourceString(getClass(), "Permissions")));
      cbPanel.add(Box.createVerticalStrut(5));

      //create permissions checkboxes
      String pStr = 
            m_applet.getResourceString(getClass(), "Read");
      m_cbReadPermission = new JCheckBox(pStr);
      m_cbReadPermission.setEnabled(m_enabled);
      m_cbReadPermission.addActionListener(this);
      m_cbReadPermission.setMnemonic(
         PSContentExplorerApplet.getResourceMnemonic(getClass(), "Read", 'R'));
      setAccessibleInfo(m_cbReadPermission, pStr);
      
      pStr = m_applet.getResourceString(getClass(), "Write");
         
      m_cbWritePermission = new JCheckBox(pStr);
      m_cbWritePermission.addActionListener(this);
      m_cbWritePermission.setEnabled(m_enabled);
      m_cbWritePermission.setMnemonic(
         PSContentExplorerApplet.getResourceMnemonic(getClass(), "Write", 'W'));
      setAccessibleInfo(m_cbWritePermission, pStr);
      
      pStr = m_applet.getResourceString(getClass(), "Admin");
      m_cbAdminPermission = new JCheckBox(pStr);
      m_cbAdminPermission.addActionListener(this);
      m_cbAdminPermission.setEnabled(m_enabled);
      m_cbAdminPermission.setMnemonic(
         PSContentExplorerApplet.getResourceMnemonic(getClass(), "Admin", 'm'));
      setAccessibleInfo(m_cbAdminPermission, pStr);

      cbPanel.add(m_cbReadPermission);
      cbPanel.add(m_cbWritePermission);
      cbPanel.add(m_cbAdminPermission);
      cbPanel.setPreferredSize(new Dimension(90, 70));

      permPanel.add(cbPanel, BorderLayout.CENTER);
      permPanel.setBorder(BorderFactory.createEmptyBorder(30,30,40,80));

      return permPanel;
   }

   /**
    * Creates a new Button Panel with with Add and Remove buttons in it.
    *
    * @return Add/Remove button Panel, never <code>null</code>
    */
   private JPanel createButtonPanel()
   {
      JPanel buttonPanel = new JPanel();

      buttonPanel.setLayout(
         new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
      buttonPanel.setAlignmentY(LEFT_ALIGNMENT);

      m_addButton = new JButton(
            m_applet.getResourceString(getClass(), "Add"));
      m_addButton.setPreferredSize(new Dimension(100, 25));
      m_addButton.setAlignmentX(LEFT_ALIGNMENT);
      m_addButton.addActionListener(this);
      m_addButton.setEnabled(m_enabled);
      m_addButton.setMnemonic(
         PSContentExploreAppletUtils.getResourceMnemonic(getClass(), "Add", 'A'));

      m_removeButton = new JButton(
            m_applet.getResourceString(getClass(), "Remove"));
      m_removeButton.setPreferredSize(new Dimension(100, 25));
      m_removeButton.setAlignmentX(LEFT_ALIGNMENT);
      m_removeButton.addActionListener(this);
      m_removeButton.setEnabled(m_enabled);
      m_removeButton.setMnemonic(
         PSContentExploreAppletUtils.getResourceMnemonic(getClass(), "Remove", 'v'));

      buttonPanel.add(m_addButton);
      buttonPanel.add(Box.createHorizontalStrut(10));
      buttonPanel.add(m_removeButton);

      buttonPanel.setEnabled(m_enabled);

      return buttonPanel;
   }

   /**
    * Loads and appropriately sorts the ACL list with cloned Acl entries.
    * Called once by the {@link #init()}.
    * @param acl current ACL for this folder, never <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   private void loadAclList(PSObjectAcl acl)
   {
      if (acl==null)
         throw new IllegalArgumentException("acl must not be null");

      Iterator acls = acl.iterator();

      ArrayList listCurEntries = new ArrayList();

      while(acls.hasNext())
          listCurEntries.add(acls.next());

      addUniqueListAclEntries(listCurEntries);

      sortListModelEntries((DefaultListModel)m_aclList.getModel());

      m_aclList.setSelectedIndex(0);

      m_aclList.repaint();
   }

   /**
    * Action Event handler, which handles all the button and check boxes events
    * on this panel.
    *
    * @param e action event, never <code>null</code>
   */
   public void actionPerformed(ActionEvent e)
   {
      if (e==null)
         throw new IllegalArgumentException("ActionEvent may not be null");

      if (!m_enabled)
         return;

      AbstractButton btn = null;

      if (e.getSource() instanceof AbstractButton)
      {
         btn = (AbstractButton)e.getSource();
      }
      else
         return;

      PSObjectAclEntry aclEntry = getCurAclEntry();

      if (btn == m_addButton)
      {
         onAdd();
      }
      else if (btn == m_removeButton)
      {
         if (aclEntry==null)
            return;

         onRemove();
      }
      else if (btn == m_cbReadPermission)
      {
         if (aclEntry==null)
            return;

         aclEntry.setReadAccess(m_cbReadPermission.isSelected());

         if (!m_cbReadPermission.isSelected()) {
            m_cbWritePermission.setSelected(false);
            m_cbAdminPermission.setSelected(false);
            aclEntry.setWriteAccess(false);
            aclEntry.setAdminAccess(false);
         }
         m_modified = true;
      }
      else if (btn == m_cbWritePermission)
      {
         if (aclEntry==null)
            return;

         aclEntry.setWriteAccess(m_cbWritePermission.isSelected());

         if (m_cbWritePermission.isSelected()) {
            m_cbReadPermission.setSelected(true);
            aclEntry.setReadAccess(true);
         }
         else {
            m_cbAdminPermission.setSelected(false);
            aclEntry.setAdminAccess(false);
         }
         
         m_modified = true;
      }
      else if (btn == m_cbAdminPermission)
      {
         if (aclEntry==null)
            return;

         aclEntry.setAdminAccess(m_cbAdminPermission.isSelected());

         if (m_cbAdminPermission.isSelected()) {
           m_cbWritePermission.setSelected(true);
           m_cbReadPermission.setSelected(true);
           aclEntry.setWriteAccess(true);
           aclEntry.setReadAccess(true);
         }
         
         m_modified = true;
      }
   }

   /**
    * Sets permissions checkboxes based on supplied entry.
    *
    * @param aclEntry currently active aclEntry that is used to update 
    *    permissions check boxes. If <code>null</code> then all checkboxes 
    *    will be unchecked.
    */
   private void updateCheckboxes(PSObjectAclEntry aclEntry)
   {
      if (aclEntry == null)
      {
         m_cbReadPermission.setSelected(false);
         m_cbWritePermission.setSelected(false);
         m_cbAdminPermission.setSelected(false);
         return;
      }

      m_cbReadPermission.setSelected(aclEntry
         .hasAccess(PSObjectAclEntry.ACCESS_READ));
      m_cbWritePermission.setSelected(aclEntry
         .hasAccess(PSObjectAclEntry.ACCESS_WRITE));
      m_cbAdminPermission.setSelected(aclEntry
         .hasAccess(PSObjectAclEntry.ACCESS_ADMIN));
   }

   /**
    * Launches the PSFolderAclEditorDialog that allows to add / create new ACL
    * entries for this folder.
    * Invoked by the action listener in response to the user pushing on the
    * 'Add' button.
    */
   private void onAdd()
   {
      if (!m_enabled)
         return;

      DefaultListModel aclListModel =
         (DefaultListModel)m_aclList.getModel();

      Enumeration curAcls = aclListModel.elements();

      //launch the ACL list editor dialog
      PSFolderAclEditorDialog aclEditorDlg = new PSFolderAclEditorDialog(
         m_parentDialog, m_folderMgr, m_enabled, curAcls);

      aclEditorDlg.setVisible(true);

      if(!aclEditorDlg.isOk())
         return;

      m_modified = true;
      
      Collection resultAclEntries = aclEditorDlg.getResultAclEntries();

      aclListModel.removeAllElements();

      if (resultAclEntries.size()<=0)
         return;

      //add Acl entries to the JList
      addUniqueListAclEntries(resultAclEntries);

      //sort List
      sortListModelEntries((DefaultListModel)m_aclList.getModel());
   }

   /**
    * Adds given collection of ACL entries to the JList. Ensures that only
    * unique entries are added.
    * @param aclNewEntries a collection of ACL Entries to add to the list,
    * never <code>null</code>, may be <code>empty</code>.
    */
   private void addUniqueListAclEntries(Collection aclNewEntries)
   {
      if (aclNewEntries==null)
         throw new IllegalArgumentException("aclEntries may not be null");

      if (aclNewEntries.size()<=0)
         return;

      DefaultListModel aclListModel =
         (DefaultListModel)m_aclList.getModel();

      Iterator itNewAcls = aclNewEntries.iterator();

      while(itNewAcls.hasNext())
      {
         Object objNewAcl = itNewAcls.next();

         //see if list already has this one
         if (!aclListModel.contains(objNewAcl))
            aclListModel.addElement(objNewAcl);
      }
   }

   /**
    * Sorts List model that contains ACL Entries
    * @param listModel ACL list model to sort, never <code>null</code>
    */
   @SuppressWarnings("unchecked")
   private void sortListModelEntries(DefaultListModel listModel)
   {
      if (listModel==null)
         throw new IllegalArgumentException("sorted listModel may not be null");

      if (listModel.isEmpty())
         return;

      Enumeration enumEntries = listModel.elements();

      ArrayList entriesToSort = new ArrayList();

      while(enumEntries.hasMoreElements())
         entriesToSort.add(enumEntries.nextElement());

      //sort ACL entries
      sortAclEntries(entriesToSort);

      //remove existing ones from the model
      listModel.removeAllElements();

      //add sorted ones
      Iterator itSortedEntries = entriesToSort.iterator();

      while(itSortedEntries.hasNext())
         listModel.addElement(itSortedEntries.next());
   }

   /**
    * Sorts given ACL entries. The sorting is done first by the ACL Entry type,
    * in order VIRTUAL then ROLE then USER; then each type group is also sorted
    * by the ACL names to alpha-order them.
    *
    * @param listAclEntries a list of ACL Entries to sort,
    * never <code>null</code>
    */
   @SuppressWarnings("unchecked")
   private void sortAclEntries(AbstractList listAclEntries)
   {
      if (listAclEntries==null)
         throw new IllegalArgumentException("listAclEntries may not be null");

      if (listAclEntries.size()<=0)
         return;

      /**
       * Special ACL Entry comparator class that is used to sort ACL Entries
       */
      class AclComparator implements Comparator
      {
         public int compare(Object left, Object right)
         {
            PSObjectAclEntry leftAcl = (PSObjectAclEntry)left;
            PSObjectAclEntry rightAcl = (PSObjectAclEntry)right;

            if (leftAcl.equals(rightAcl))
               return 0;

            int leftType = leftAcl.getType();
            int rightType = rightAcl.getType();

            if (leftType == PSObjectAclEntry.ACL_ENTRY_TYPE_VIRTUAL) {
               //we want VIRTUAL acls to show up first
               if (rightType != PSObjectAclEntry.ACL_ENTRY_TYPE_VIRTUAL)
                  return -1;
            }
            else if (leftType == PSObjectAclEntry.ACL_ENTRY_TYPE_ROLE) {
               //we want ROLE acls to show up after VIRTUALs
               if (rightType == PSObjectAclEntry.ACL_ENTRY_TYPE_VIRTUAL)
                  return 1;
               if (rightType == PSObjectAclEntry.ACL_ENTRY_TYPE_USER)
                  return -1;
            }
            else if (leftType == PSObjectAclEntry.ACL_ENTRY_TYPE_USER) {
               //we want USER acls to show up after ROLEs
               if (rightType != PSObjectAclEntry.ACL_ENTRY_TYPE_USER)
                  return 1;
            }

            //same type ACLs should be sorted in alpha order
            return leftAcl.getName().compareTo(rightAcl.getName());
         }
      }

      Collections.sort(listAclEntries, new AclComparator());
   }

   /**
    * Removes selected ACL Entry from the list.
    * Invoked by the action listener in response to the user pushing on the
    * 'Remove' button or hitting 'DEL' key.
    */
   private void onRemove()
   {
      if (!m_enabled)
         return;

      DefaultListModel aclListModel =
         (DefaultListModel)m_aclList.getModel();

      if (aclListModel.isEmpty())
         return;

      int selInd = m_aclList.getSelectedIndex();

      if (selInd < 0 || selInd >= aclListModel.size())
         return;

      Object selObj = aclListModel.getElementAt(selInd);
      m_folder.getAcl().remove((PSObjectAclEntry)selObj);

      aclListModel.removeElementAt(selInd);

      m_modified = true;
      
      updateCheckboxes(null);
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
    * @return currently selected single ACL Entry, may be <code>null</code>
    */
   private PSObjectAclEntry getCurAclEntry()
   {
      DefaultListModel aclListModel =
         (DefaultListModel)m_aclList.getModel();

      if (aclListModel.isEmpty())
         return null;

      int selInd = m_aclList.getSelectedIndex();

      if (selInd < 0 || selInd >= aclListModel.size())
         return null;

      Object selObj = aclListModel.getElementAt(selInd);

      return (PSObjectAclEntry)selObj;
   }

   /**
    * JList of ACL Entries
   */
   private JList     m_aclList;

   /** */
   private JCheckBox m_cbReadPermission;
   /** */
   private JCheckBox m_cbWritePermission;
   /** */
   private JCheckBox m_cbAdminPermission;

   /** */
   private JButton m_addButton;
   /** */
   private JButton m_removeButton;

   /** reference to the parent dialog, initialized in the ctor */
   private Dialog m_parentDialog;

   /** <code>true</code> indicates that the user has enough permissions
    * to allow him to edit and save data on this panel.
   */
   private boolean m_enabled;
   
   /**
    * <code>true</code> indicates the acl data has been modified since the last
    * save or since first initialized, <code>false</code> otherwise.
    */
   private boolean m_modified = false;

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
    * The info of the user editing or creating the folder, initialized in the
    * ctor and never <code>null</code> or modified after that.
    */
   private PSUserInfo m_userInfo;
   
   /**
    * A reference back to the applet that initiated the action manager.
    */
   private PSContentExplorerApplet m_applet;
}
