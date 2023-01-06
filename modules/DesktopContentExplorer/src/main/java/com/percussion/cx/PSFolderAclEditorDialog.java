/******************************************************************************
 *
 * [ PSFolderAclEditorDialog.java ]
 * 
 * COPYRIGHT (c) 1999 - 2004 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.cx;

import com.percussion.UTComponents.UTFixedButton;
import com.percussion.border.PSFocusBorder;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSObjectAclEntry;
import com.percussion.cms.objectstore.PSSecurityProviderCataloger;
import com.percussion.cms.objectstore.PSSecurityProviderInstanceSummary;
import com.percussion.cx.catalogers.PSRoleCataloger;
import com.percussion.cx.catalogers.PSSubjectCataloger;
import com.percussion.guitools.PSAccessibleListSelectionListener;
import com.percussion.guitools.PSDialog;
import com.percussion.guitools.UTStandardCommandPanel;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
 * Folder ACL Editor Dialog that allows to catalog Role, User and Virtual ACL
 * entries and add those to the result ACL list.
 */
public class PSFolderAclEditorDialog extends PSDialog implements ActionListener
{
   /**
    * Constructor for this dialog. Creates all UI components,
    * sets up layout and loads current and cataloged acl entry lists.
    * @param parent Dialog, may be <code>null</code>.
    * @param folderMgr folder action manager, used to get server cataloged data,
    * never <code>null</code>.
    * @param enabled <code>true</code> indicates that the user can make
    * modifications and OK changes, <code>false</code> otherwise
    * @param curAclEntries currently set ACL entries, never <code>null</code>.
    */
   public PSFolderAclEditorDialog(Dialog parent,
                                  PSFolderActionManager folderMgr,
                                  boolean enabled,
                                  Enumeration curAclEntries)
   {
      super(parent, folderMgr.getApplet().getResourceString(
         PSFolderAclEditorDialog.class,
         "Folder ACL List Entry Editor"));
      setResizable(true);
      if (folderMgr==null)
         throw new IllegalArgumentException("folderMgr may not be null");

      if (curAclEntries==null)
         throw new IllegalArgumentException("curAclEntries may not be null");
      
      if(folderMgr.getApplet()==null)
         throw new IllegalArgumentException("applet may not be null");

      Collection aclEntries = new ArrayList();
      while(curAclEntries.hasMoreElements())
         aclEntries.add(curAclEntries.nextElement());

      m_folderMgr = folderMgr;
      m_enabled = enabled;
      m_applet = folderMgr.getApplet();

      initDialog(aclEntries);
   }

   /**
    * Initializes the dialog framework and sets initial state.
    * @param curAclEntries currently set ACL entries, never <code>null</code>.
    */
   private void initDialog(Collection curAclEntries)
   { 
      JPanel mainPanel = new JPanel();
      mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

      JPanel northPanel = new JPanel(new BorderLayout());
      northPanel.add(createCatalogSelectorPanel(), BorderLayout.WEST);
      UTStandardCommandPanel defCommandPanel = new UTStandardCommandPanel(
         this, SwingConstants.RIGHT, true);
      JButton btnOK = defCommandPanel.getOkButton();
      btnOK.setEnabled(m_enabled);

      Border border1    = BorderFactory.createEmptyBorder(2,2,2,2);
      JPanel southPanel = new JPanel();
      southPanel.setBorder(border1);
      southPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
      southPanel.add(defCommandPanel, BorderLayout.EAST);

      JPanel centerPanel = new JPanel();
      centerPanel.setBorder(new TitledBorder(
              new EtchedBorder(EtchedBorder.LOWERED), ""));
      centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));

      m_catalogAclList = new JList(new DefaultListModel());
      centerPanel.add(createAclListPanel(m_catalogAclList,
            m_applet.getResourceString(
            getClass(), "Cataloged Entries")));
      m_catalogAclList.addListSelectionListener(
         new PSAccessibleListSelectionListener());

      centerPanel.add(Box.createHorizontalStrut(10));
      centerPanel.add(createAddRemoveButtonPanel());
      centerPanel.add(Box.createHorizontalStrut(10));

      JPanel curAclListPanel = new JPanel();
      curAclListPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
      
      m_curAclList = new JList(new DefaultListModel());
      m_curAclList.addListSelectionListener(
         new PSAccessibleListSelectionListener());
      curAclListPanel.add(createAclListPanel(m_curAclList,
            m_applet.getResourceString(getClass(),
              "Current ACL Entries")));
      centerPanel.add(curAclListPanel);
      centerPanel.add(Box.createHorizontalGlue());
      
      mainPanel.add(Box.createVerticalStrut(5));
      mainPanel.add(northPanel);
      mainPanel.add(Box.createVerticalStrut(10));
      mainPanel.add(centerPanel);
      mainPanel.add(Box.createVerticalStrut(5));
      mainPanel.add(southPanel);

      mainPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

      setContentPane(mainPanel);

      //get cataloged data for currently selected radio button,
      //that serves as a catalog selector.
      Collection catalogedAclEntries = getCatalogEntries();

      //load current ACL list
      insertCurrentAclListEntries(curAclEntries);

      //load catalog list
      insertCatalogedAclListEntries(catalogedAclEntries);

      pack();
      center();
      
      // Add focus highlights
      PSDisplayOptions dispOptions =
         (PSDisplayOptions)UIManager.getDefaults().get(
            PSContentExplorerConstants.DISPLAY_OPTIONS);
      PSFocusBorder focusBorder = new PSFocusBorder(1, dispOptions);
      focusBorder.addToAllNavigable(mainPanel);
   }

   /**
    * Inserts and then sorts acl entries into the current acl entries list.
    * While inserting enforces uniqueness and also makes sure that in the
    * cataloged list there is no redundant entries and if so removes them
    * from the cataloged list.
    * @param aclEntries collection of PSObjectAclEntry, never <code>null</code>.
    */
   private void insertCurrentAclListEntries(Collection aclEntries)
   {
      if (aclEntries==null)
         throw new IllegalArgumentException("aclEntries may not be null");

      if (aclEntries.size()<=0)
         return; //nothing to insert

      Iterator itNewEntries = aclEntries.iterator();

      DefaultListModel curListModel =
         (DefaultListModel)m_curAclList.getModel();

      DefaultListModel catalogListModel =
         (DefaultListModel)m_catalogAclList.getModel();

      while(itNewEntries.hasNext())
      {
         Object newEntry = itNewEntries.next();

         if (!curListModel.contains(newEntry))
         {
            //add to cur list
            curListModel.addElement(newEntry);

            //remove from catalog list
            catalogListModel.removeElement(newEntry);
         }
      }

      sortListModelEntries(curListModel);
   }

   /**
    * Inserts and then sorts acl entries into the catalog acl entries list.
    * While inserting enforces uniquiness and also skips those ones that
    * have already been added to the current acl entries list.
    * @param aclEntries collection of acl entries, never <code>null</code>
    */
   private void insertCatalogedAclListEntries(Collection aclEntries)
   {
      if (aclEntries==null)
         throw new IllegalArgumentException("aclEntries may not be null");

      if (aclEntries.size()<=0)
         return; //nothing to insert

      Iterator itNewEntries = aclEntries.iterator();

      DefaultListModel curListModel =
         (DefaultListModel)m_curAclList.getModel();

      DefaultListModel catalogListModel =
         (DefaultListModel)m_catalogAclList.getModel();

      while(itNewEntries.hasNext())
      {
         Object newEntry = itNewEntries.next();

         if (!curListModel.contains(newEntry))
            catalogListModel.addElement(newEntry);
      }

      sortListModelEntries(catalogListModel);
   }

   /**
    * Depending on the currently selected radio button, catalogs roles, users,
    * or virtual entries then creates corresponding ACL Entry objects and caches
    * them for subsequent use. The PSRoleCataloger and PSSubjectCataloger are
    * used to lazily fetch catalog data from the server.
    * @return collection of PSObjectAclEntry objects,
    * never <code>null</code>, may be <code>empty</code>.
    */
   private Collection getCatalogEntries()
   {
      try
      {
         //which catalog?
         if (m_rbRoles.isSelected())
         {
            if (m_catalogedRoleAcls!=null) //see if it is already cached
               return Collections.unmodifiableCollection(m_catalogedRoleAcls);

            //get Role ACLs
            PSRoleCataloger roleCatalog = m_folderMgr.getRoleCataloger();
            Collection roles = roleCatalog.getRoles();

            Iterator itRoles = roles.iterator();

            m_catalogedRoleAcls = new ArrayList();

            while(itRoles.hasNext())
            {
               PSRoleCataloger.Role role = (PSRoleCataloger.Role)itRoles.next();

               //create ACL Entry out of the role

               PSObjectAclEntry aclEntry = new PSObjectAclEntry(
                  PSObjectAclEntry.ACL_ENTRY_TYPE_ROLE,
                  role.getName(),
                  PSObjectAclEntry.ACCESS_READ);

               m_catalogedRoleAcls.add(aclEntry);
            }

            return Collections.unmodifiableCollection(m_catalogedRoleAcls);
         }
         else if (m_rbUsers.isSelected())
         {
            if (m_catalogedUserAcls!=null) //see if it is already cached
               return Collections.unmodifiableCollection(m_catalogedUserAcls);

            //get User ACLs
            PSSubjectCataloger subjectCatalog = m_folderMgr.getSubjectCataloger();
            Collection subjects = subjectCatalog.getSubjects();

            Iterator itSubjects = subjects.iterator();

            m_catalogedUserAcls = new ArrayList();

            while(itSubjects.hasNext())
            {
               PSSubjectCataloger.Subject subject =
                  (PSSubjectCataloger.Subject)itSubjects.next();

               //create ACL Entry out of the user

               PSObjectAclEntry aclEntry = new PSObjectAclEntry(
                  PSObjectAclEntry.ACL_ENTRY_TYPE_USER,
                  subject.getName(),
                  PSObjectAclEntry.ACCESS_READ);

               m_catalogedUserAcls.add(aclEntry);
            }

            return Collections.unmodifiableCollection(m_catalogedUserAcls);
         }
         else if (m_rbVirtual.isSelected())
         {
            if (m_catalogedVirtualAcls!=null) //see if it is already cached
               return Collections.unmodifiableCollection(m_catalogedVirtualAcls);

            m_catalogedVirtualAcls = new ArrayList();

            //create Virtual ACLs
            PSObjectAclEntry aclEntry = new PSObjectAclEntry(
                  PSObjectAclEntry.ACL_ENTRY_TYPE_VIRTUAL,
                  PSObjectAclEntry.ACL_ENTRY_EVERYONE,
                  PSObjectAclEntry.ACCESS_READ);

            m_catalogedVirtualAcls.add(aclEntry);

            aclEntry = new PSObjectAclEntry(
                  PSObjectAclEntry.ACL_ENTRY_TYPE_VIRTUAL,
                  PSObjectAclEntry.ACL_ENTRY_FOLDER_COMMUNITY,
                  PSObjectAclEntry.ACCESS_READ);

            m_catalogedVirtualAcls.add(aclEntry);

            return Collections.unmodifiableCollection(m_catalogedVirtualAcls);
         }
      }
      catch(PSCmsException cmsEx)
      {
         m_applet.displayErrorMessage(this,
           getClass(), "Failed To Get Catalog Information, Exception Msg: <{0}>.",
           new String[]{cmsEx.getLocalizedMessage()}, "Error", null);
      }

      return new ArrayList();
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
       */
      public Component getListCellRendererComponent(JList list,
                                                    Object value,
                                                    int index,
                                                    boolean isSelected,
                                                    boolean cellHasFocus)
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
            if (list == m_curAclList)
               m_catalogAclList.clearSelection();
            else
               m_curAclList.clearSelection();

            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());

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
         JList srcList = null;

         if (event.getSource() == m_catalogAclList)
            srcList = m_catalogAclList;
         else if (event.getSource() == m_curAclList)
            srcList = m_curAclList;
         else
            return;

         DefaultListModel listModel = (DefaultListModel)srcList.getModel();

         int index = srcList.locationToIndex(event.getPoint());

         if (index > -1 && index < listModel.size())
         {
            PSObjectAclEntry aclEntry =
               (PSObjectAclEntry)srcList.getModel().getElementAt(index);

            String tooltip = getToolTipText(aclEntry);

            srcList.setToolTipText(tooltip);
         }
         else
            srcList.setToolTipText(null);
      }

      /**
       * Depending on the ACL Entry type creates i18n ready tooltip text.
       * @param aclEntry currenly selected ACL Entry, may be <code>null</code>
       * @return tooltip string, either a non-empty string or <code>null</code>.
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
    * Creates a new panel with Add, Remove and New buttons.
    * @return panel object , never <code>null</code>.
    */
   private JPanel createAddRemoveButtonPanel()
   {
      JPanel addRemovePanel = new JPanel(new BorderLayout());

      JPanel btnPanel = new JPanel();
      btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.Y_AXIS));

      m_btnAdd = new UTFixedButton(
            m_applet.getResourceString(getClass(), "Add >>"),
         new Dimension(110, 24));
      m_btnAdd.setDefaultCapable(false);
      m_btnAdd.addActionListener(this);
      m_btnAdd.setEnabled(m_enabled);
      m_btnAdd.setMnemonic(PSContentExplorerApplet.getResourceMnemonic(
            getClass(), "Add >>", 'A'));

      m_btnRemove = new UTFixedButton(
            m_applet.getResourceString(getClass(), "<< Remove"),
          new Dimension(110, 24));
      m_btnRemove.setDefaultCapable(false);
      m_btnRemove.addActionListener(this);
      m_btnRemove.setEnabled(m_enabled);
      m_btnRemove.setMnemonic(PSContentExplorerApplet.getResourceMnemonic(
            getClass(), "<< Remove", 'R'));

      m_btnNew = new UTFixedButton(
            m_applet.getResourceString(getClass(), "New..."),
         new Dimension(110, 24));
      m_btnNew.setDefaultCapable(false);
      m_btnNew.addActionListener(this);
      m_btnNew.setEnabled(m_enabled);
      m_btnNew.setMnemonic(PSContentExplorerApplet.getResourceMnemonic(
            getClass(), "New...", 'N'));

      btnPanel.add(Box.createVerticalStrut(50));
      btnPanel.add(m_btnAdd);
      btnPanel.add(Box.createVerticalStrut(5));
      btnPanel.add(m_btnRemove);
      btnPanel.add(Box.createVerticalStrut(30));
      btnPanel.add(m_btnNew);

      addRemovePanel.add(btnPanel, BorderLayout.CENTER);

      return addRemovePanel;
   }

   /**
    * Creates a new panel with Radio buttons.
    * @return panel object , never <code>null</code>
    */
   private JPanel createCatalogSelectorPanel()
   {
      JPanel radioGroupPanel = new JPanel(new BorderLayout());

      JPanel rbPanel = new JPanel();
      rbPanel.setLayout(new BoxLayout(rbPanel, BoxLayout.Y_AXIS));
      rbPanel.setBorder(new TitledBorder(
         new EtchedBorder(EtchedBorder.LOWERED),
         m_applet.getResourceString(getClass(), 
         "Show Catalog of")));
      rbPanel.add(Box.createVerticalStrut(5));

      //create permissions checkboxes
      m_rbRoles = new JRadioButton(
            m_applet.getResourceString(getClass(), "Roles"));
      m_rbRoles.addActionListener(this);
      m_rbRoles.setSelected(true);
      m_rbRoles.setMnemonic(
         PSContentExploreAppletUtils.getResourceMnemonic(getClass(), "Roles", 'o'));

      m_rbUsers = new JRadioButton(
            m_applet.getResourceString(getClass(), "Users"));
      m_rbUsers.addActionListener(this);
      m_rbUsers.setMnemonic(
         PSContentExploreAppletUtils.getResourceMnemonic(getClass(), "Users", 'U'));

      m_rbVirtual = new JRadioButton(
            m_applet.getResourceString(getClass(), "Virtual"));
      m_rbVirtual.addActionListener(this);
      m_rbVirtual.setMnemonic(
         PSContentExploreAppletUtils.getResourceMnemonic(getClass(), 
         "Virtual", 'V'));

      ButtonGroup group = new ButtonGroup();
      group.add(m_rbRoles);
      group.add(m_rbUsers);
      group.add(m_rbVirtual);

      rbPanel.add(m_rbRoles);
      rbPanel.add(m_rbUsers);
      rbPanel.add(m_rbVirtual);
      rbPanel.setPreferredSize(new Dimension(200, 110));
      rbPanel.setMinimumSize(new Dimension(200, 110));

      radioGroupPanel.add(rbPanel, BorderLayout.CENTER);

      return radioGroupPanel;
   }

   /**
     * Creates a new panel with JList to display ACL Entries
     * @param aclList acl list to create panel with and setup listeners
     * etc. Must have DefaultListModel., must never be <code>null</code>.
     * @param listLabel label for the list, never <code>null</code>.
     * @return panel object, never <code>null</code>.
    */
   private JPanel createAclListPanel(JList aclList, String listLabel)
   {
      if (aclList==null)
         throw new IllegalArgumentException("aclList may not be null");

      if (listLabel==null)
         throw new IllegalArgumentException("listLabel may not be null");

      JPanel aclPanel = new JPanel();
      aclPanel.setLayout(new BoxLayout(aclPanel, BoxLayout.Y_AXIS));
      aclPanel.setAlignmentY(CENTER_ALIGNMENT);

      JLabel label = new JLabel(listLabel, SwingConstants.LEFT);
      label.setAlignmentX(LEFT_ALIGNMENT);

      aclPanel.add(label);
      aclPanel.add(Box.createVerticalStrut(5));

      aclList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      aclList.addMouseMotionListener(new AclListMouseMotion());
      aclList.setCellRenderer(new AclListCellRenderer());

      JScrollPane aclListScroller = new JScrollPane(aclList);
      aclListScroller.setPreferredSize(new Dimension(200, 200));
      aclListScroller.setMinimumSize(new Dimension(200, 200));
      aclListScroller.setMaximumSize(new Dimension(250, Short.MAX_VALUE));
      aclListScroller.setAlignmentX(LEFT_ALIGNMENT);

      aclPanel.add(aclListScroller);

      return aclPanel;
   }

   /**
    * Handles all custom button actions on this dialog.
    * @param e current action, assumed never <code>null</code>.
    */
   public void actionPerformed(ActionEvent e)
   {
      if (!m_enabled)
         return;

      AbstractButton btn = null;

      if (e.getSource() instanceof AbstractButton)
      {
         btn = (AbstractButton)e.getSource();
      }
      else
         return;

      if (btn == m_rbRoles || btn == m_rbUsers || btn == m_rbVirtual)
      {
         DefaultListModel catalogModel =
         (DefaultListModel)m_catalogAclList.getModel();

         catalogModel.removeAllElements();

         Collection catalogedAclEntries = getCatalogEntries();
         insertCatalogedAclListEntries(catalogedAclEntries);
      }
      else if (btn == m_btnAdd)
      {
         onAdd();
      }
      else if (btn == m_btnRemove)
      {
         onRemove();
      }
      else if (btn == m_btnNew)
      {
         onNew();
      }
   }

   /**
    * Handles 'Add' operation by moving cataloged ACL entries to the current
    * acl entries list.
    */
   private void onAdd()
   {
      DefaultListModel catalogModel =
         (DefaultListModel)m_catalogAclList.getModel();

      if (catalogModel.isEmpty())
         return;

      int[] sel_indices = m_catalogAclList.getSelectedIndices();

      ArrayList selectedEntries = new ArrayList();

      for(int i = sel_indices.length-1; i >= 0; i--)
      {
         int selInd = sel_indices[i];
         if ( selInd > -1 && selInd < catalogModel.size())
            selectedEntries.add(catalogModel.getElementAt(selInd));
      }

      if (selectedEntries.size()<=0)
         return;

      insertCurrentAclListEntries(selectedEntries);
   }

   /**
    * Handles 'Remove' operation by moving current acl entries to the cataloged
    * acl entries list.
    */
   private void onRemove()
   {
      DefaultListModel curModel =
         (DefaultListModel)m_curAclList.getModel();

      if (curModel.isEmpty())
         return;

      int[] sel_indices = m_curAclList.getSelectedIndices();

      ArrayList selectedEntries = new ArrayList();

      for(int i = sel_indices.length-1; i >= 0; i--)
      {
         int selInd = sel_indices[i];
         if (selInd > -1 && selInd < curModel.size())
            selectedEntries.add(curModel.getElementAt(selInd));
      }

      if (selectedEntries.size()<=0)
         return;

      //remove selected entries from the current ACL list
      Iterator itSelected = selectedEntries.iterator();

      while(itSelected.hasNext())
         curModel.removeElement(itSelected.next());

      //insert them into the catalog list
      insertCatalogedAclListEntries(selectedEntries);
   }

   /**
    * Launches New User ACL Entry dialog and if OKed creates and inserts
    * a new acl entry into the current acl entries list.
    */
   private void onNew()
   {
      try
      {
         PSSecurityProviderCataloger providerCataloger =
            m_folderMgr.getSecurityProviderCataloger();

         PSACLNewUserDialog dlg =
            new PSACLNewUserDialog(this, providerCataloger.getProviders(), m_applet);

         dlg.setVisible(true);

         if (!dlg.isOk())
            return;

         PSSecurityProviderInstanceSummary summary = dlg.getSelectedProvider();

         if (summary==null)
            return;

         PSObjectAclEntry aclEntry = new PSObjectAclEntry(
            PSObjectAclEntry.ACL_ENTRY_TYPE_USER,
            dlg.getUserName(),
            PSObjectAclEntry.ACCESS_READ);

         ArrayList alNewEntry = new ArrayList();
         alNewEntry.add(aclEntry);

         insertCurrentAclListEntries(alNewEntry);
      }
      catch(PSCmsException cmsEx)
      {
         m_applet.displayErrorMessage(this,
           getClass(), "Failed To Catalog Security Providers, Exception Msg: <{0}>.",
           new String[]{cmsEx.getLocalizedMessage()}, "Error", null);
      }
   }


   /**
    * Sorts List model that contains ACL Entries of type PSObjectAclEntry.
    * @param listModel ACL list model to sort, never <code>null</code>
    */
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
    * @return collection of user selected ACL Entries, of type PSObjectAclEntry,
    * never <code>null</code>, may be <code>empty</code>.
    */
   public Collection getResultAclEntries()
   {
      DefaultListModel curListModel =
         (DefaultListModel)m_curAclList.getModel();

      ArrayList curAcls = new ArrayList();

      Enumeration enumCurAcls = curListModel.elements();

      while(enumCurAcls.hasMoreElements())
         curAcls.add(enumCurAcls.nextElement());

      return curAcls;
   }


   /** Cached collection of cataloged Role ACLs, it is lazily initilized
    *  in the {@link #getCatalogEntries()}, never <code>null</code> after that.
   */
   private Collection m_catalogedRoleAcls;

   /** Cached collection of cataloged User ACLs, it is lazily initilized
    *  in the {@link #getCatalogEntries()}, never <code>null</code> after that.
   */
   private Collection m_catalogedUserAcls;

   /** Cached collection of Virtual ACLs, it is lazily initilized.
    *  in the {@link #getCatalogEntries()}, never <code>null</code> after that.
   */
   private Collection m_catalogedVirtualAcls;

   /**
    *  <code>true</code> indicates that user can make and save modifications
    *  to any control in this dialog, <code>false</code> otherwise.
   */
   private boolean m_enabled;

   /**
    * The manager that handles the creating or updating folders, initialized in
    * the ctor and never <code>null</code> or modified after that.
    */
   private PSFolderActionManager m_folderMgr;

   /**
    * Add button
   */
   private JButton m_btnAdd;

   /**
    * Remove button
   */
   private JButton m_btnRemove;

   /**
    * New button
   */
   private JButton m_btnNew;

   /** Radio button that controls cataloging of Role ACLs
    */
   private JRadioButton m_rbRoles;

   /** Radio button that controls cataloging of User ACLs
    */
   private JRadioButton m_rbUsers;

   /** Radio button that controls cataloging of Virtual ACLs
    */
   private JRadioButton m_rbVirtual;

   /**
    * List that shows Catologed ACL entries
   */
   private JList m_catalogAclList;

   /**
    * List that shows Currently selected ACL entries
   */
   private JList m_curAclList;
   
   /**
    * A reference back to the applet that initiated the action manager.
    */
   private PSContentExplorerApplet m_applet;
}