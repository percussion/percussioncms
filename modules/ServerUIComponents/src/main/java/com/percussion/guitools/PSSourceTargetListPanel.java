/*******************************************************************************
 *
 * [ PSSourceTargetListPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.guitools;

import com.percussion.UTComponents.UTFixedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * NOTE: While reviewing please keep in mind that no data objects have been
 * used as of yet so ctor and other places may have to be modified but that
 * shouldn't affect the ui in general, hopefully.
 */

/**
 * A generic panel containing a source list and a target list with two buttons,
 * 'Add' - for adding items selected from source list to the target list and
 * 'Remove' - for removing selected items from the target list and them back
 * into source list.
 */
public class PSSourceTargetListPanel extends JPanel
{
   /**
    * Constructs the panel.
    *
    * @param srcLabel, label for the source list, cannot be <code>null</code> or
    * empty.
    *
    * @param targetLabel, label for the target list, cannot be <code>null</code>
    *  or empty.
    *
    * @param topBtnName, top button name, cannot be <code>null</code> or empty.
    *
    * @param btmBtnName, bottom button name, cannot be <code>null</code> or
    * empty.
    *
    * @throw IllegalArgumentException if any of the arguments are invalid.
    */
   public PSSourceTargetListPanel(String srcLabel, String targetLabel, String
      topBtnName, String btmBtnName)
   {
      loadPanel(srcLabel, targetLabel, topBtnName, btmBtnName, null, null);
   }
   
   /**
    * Constructs the panel.
    *
    * @param srcLabel, label for the source list, cannot be <code>null</code> or
    * empty.
    *
    * @param targetLabel, label for the target list, cannot be <code>null</code>
    *  or empty.
    *
    * @param topBtnName, top button name, cannot be <code>null</code> or empty.
    *
    * @param btmBtnName, bottom button name, cannot be <code>null</code> or
    * empty.
    *
    * @param topBtnMnem, bottom button mnemonic, may be <code>null</code> 
    * 
    * @param btmBtnMnem, bottom button mnemonic, may be <code>null</code> 
    * 
    * @throw IllegalArgumentException if any of the arguments are invalid.
    */
   
   public PSSourceTargetListPanel(String srcLabel, String targetLabel, 
         String topBtnName, String btmBtnName, String topBtnMnem, 
         String btmBtnMnem)
   {
      loadPanel(srcLabel, targetLabel, topBtnName, btmBtnName,
                topBtnMnem, btmBtnMnem);
   }
   
   /**
    * see doc for the above Constructor
    */
   private void loadPanel(String srcLabel, String targetLabel, 
         String topBtnName, String btmBtnName, String topBtnMnem, 
         String btmBtnMnem)
   {
      if (srcLabel == null || srcLabel.length() == 0)
         throw new IllegalArgumentException(
            "Source label cannot be null or empty");

      if (targetLabel == null || targetLabel.length() == 0)
         throw new IllegalArgumentException(
            "Target label cannot be null or empty");

      if (topBtnName == null || topBtnName.length() == 0)
         throw new IllegalArgumentException(
            "Top button name cannot be null or empty");

      if (btmBtnName == null || btmBtnName.length() == 0)
         throw new IllegalArgumentException(
            "Bottom button name cannot be null or empty");

      m_srcName =  srcLabel;
      m_targetName = targetLabel;
      m_topBtnName = topBtnName;
      m_btmBtnName = btmBtnName;
      m_btmBtnMnem   = btmBtnMnem;
      m_topBtnMnem   = topBtnMnem;
      init();
   }
   /**
    * Sets the model for the left list.
    *
    * @param mdl, model for the list, may not be <code>null</code> may be empty,
    * if <code>null</code> then the method silently returns.
    */
   public void setModel(ListModel mdl, boolean isSrc)
   {
      if (mdl == null)
         return;
      if (isSrc)
         m_srcList.setModel(mdl);
      else
         m_targetList.setModel(mdl);
   }

   /**
    * Gets the model for the list specified by it's label name.
    *
    * @param listLabel, label of the list, may not be <code>null</code> or empty
    *
    * @return model for the list name supplied, may be <code>null</code>.
    *
    * @throws IllegalArgumentException if arguments are invalid
    */
   public DefaultListModel getModel(String listLabel)
   {
      if (listLabel != null && listLabel.length() != 0)
      {
         if ((m_srcName != null && listLabel.equals(m_srcName)))
         {
            return (DefaultListModel)m_srcList.getModel();
         }
         else if((m_targetName != null && listLabel.equals(m_targetName)))
         {
            return (DefaultListModel)m_targetList.getModel();
         }
         else
            throw new IllegalArgumentException(
               "List's label name does not match");
      }
      else
         throw new IllegalArgumentException(
            "List's label name has to be supplied");
   }

   /**
    * Initializes the panel.
    */
   private void init()
   {
      EmptyBorder emptyBorder = new EmptyBorder(10, 10, 10, 10);
      setBorder(emptyBorder);
      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

      //the panel with available packages to install
      JPanel srcPanel = createListPanel(true);
      add(srcPanel);
      add(Box.createHorizontalStrut(20));
      add(Box.createHorizontalGlue());

      //the panel with add and remove buttons
      JPanel addRemovePanel = new JPanel();
      addRemovePanel.setLayout(new BoxLayout(addRemovePanel, BoxLayout.Y_AXIS));

      m_addBtn = new UTFixedButton(m_topBtnName);
      if ( m_topBtnMnem != null )
         m_addBtn.setMnemonic(m_topBtnMnem.charAt(0));
      Dimension d = new Dimension(120, 24);
      m_addBtn.setPreferredSize(d);
      m_addBtn.setEnabled(false);
      m_addBtn.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            onAdd();
         }
      });
      addRemovePanel.add(m_addBtn);
      addRemovePanel.add(Box.createVerticalStrut(10));
      m_removeBtn = new UTFixedButton((m_btmBtnName));
      if ( m_btmBtnMnem != null )
         m_removeBtn.setMnemonic(m_btmBtnMnem.charAt(0));
      m_removeBtn.setPreferredSize(d);
      m_removeBtn.setEnabled(false);
      m_removeBtn.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            onRemove();
         }
      });
      addRemovePanel.add(m_removeBtn);
      add(addRemovePanel);
      add(Box.createHorizontalStrut(20));

      //the panel with selected packages to install
      JPanel targetPanel = createListPanel(false);
      add(targetPanel);

      //if no entries are there in either the install or available package list
      //then the buttons should be disabled or enabled accordingly.
      setButtonsState();
   }

   /**
    * Sets the buttons to enabled or disabled state based on the selection in
    * either list. If nothing is selected in either list both the buttons are
    * disabled. If an item is selected in left list then the top button is
    * enabled. If an item is selected in right list then the bottom button is
    * enabled.
    */
   private void setButtonsState()
   {
      if(m_srcList.getModel().getSize() == 0)
      {
         m_addBtn.setEnabled(false);
      }
      else
      {
         if (m_srcList.getSelectedIndices().length == 0)
            m_addBtn.setEnabled(false);
         else
            m_addBtn.setEnabled(true);
      }
      if(m_targetList.getModel().getSize() == 0)
      {
         m_removeBtn.setEnabled(false);
      }
      else
      {
         if (m_targetList.getSelectedIndices().length == 0)
            m_removeBtn.setEnabled(false);
         else
            m_removeBtn.setEnabled(true);
      }
   }

   public JList getList(boolean isLeft)
   {
      if (isLeft)
         return m_srcList;
      else
         return m_targetList;
   }



   /**
    * Adds items selected from left list to the right list.
    */
   private void onAdd()
   {
      action(m_srcList.getSelectedIndices(), m_srcList,  m_targetList);
   }

   /**
    * Removes items selected from right list and puts them to the left list.
    */
   private void onRemove()
   {
      action(m_targetList.getSelectedIndices(), m_targetList,  m_srcList);
   }

   /**
    * Add or remove operations are generically done based on selected indexes in
    * left list. The result of the action is shown in the right list.
    *
    * @param selectedIndices assumed length of this array is never 0.
    *
    * @param srcJList left list, assumed to be not <code>null</code>.
    *
    * @param destJList right list, assumed to be not <code>null</code>.
    */
   private void action(int[] selectedIndices, JList srcJList,
                       JList destJList)
   {
      ListModel installModel = destJList.getModel();
      int isize = installModel.getSize();
      List installList = new ArrayList();
      //add existing install entries
      //IndexedObject iObj = null;
      String str = "";
      if (isize != 0)
      {
         for(int k = 0; k < isize; k++)
         {
            str = (String)installModel.getElementAt(k);
            installList.add(str);
         }
      }
      //add selected entries
      DefaultListModel availableModel = (DefaultListModel)srcJList.getModel();
      int len = selectedIndices.length;
      for(int k = 0; k < len; k++)
      {
         str = (String)availableModel.getElementAt(selectedIndices[k]);
         installList.add(str);
      }
      //sort to maintain the order
      Collections.sort(installList);
      DefaultListModel newInstallModel = new DefaultListModel();
      int length = installList.size();
      for(int z = 0; z < length;z++)
      {
         Object o = installList.get(z);
         newInstallModel.addElement(o);
      }
      destJList.setModel(newInstallModel);
      //clear selected values from available package list
      for(int i = selectedIndices.length-1; i >= 0; i--)
      {
         Object obj =  availableModel.getElementAt(selectedIndices[i]);
         availableModel.removeElement(obj);
      }
      setButtonsState();
   }

   /**
    * Creates the list panel.
    *
    * @param isSrc, if it is <code>true</code> the left list is created else
    * the right list.
    *
    * @return, panel containing the list, neve <code>null</code>.
    */
   private JPanel createListPanel(boolean isSrc)
   {
      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      JLabel label = null;
      if (isSrc)
      {
         label = new JLabel(m_srcName);
         panel.add(label);

      }
      else
      {
         label = new JLabel(m_targetName);
         panel.add(label);
      }
      /**
       Collection col = m_packageMap.values();
      List lst = new ArrayList();
      lst.addAll(col);
      Collections.sort(lst);
      DefaultListModel dlm = new DefaultListModel();
      int len = lst.size();
      for(int z =0 ; z < len; z++)
       //{
         Object o = lst.get(z);
         dlm.addElement(o);
      }*/
      JScrollPane listPane = null;
      if (isSrc)
      {
         m_srcList = new JList(new DefaultListModel());
         m_srcList.setSelectionMode(
            ListSelectionModel.SINGLE_INTERVAL_SELECTION);
         listPane = new JScrollPane(m_srcList);

         m_srcList.addListSelectionListener(new LeftListListener());
      }
      else
      {
         m_targetList = new JList(new DefaultListModel());
         m_targetList.setSelectionMode(
            ListSelectionModel.SINGLE_INTERVAL_SELECTION);
         listPane = new JScrollPane(m_targetList);

         m_targetList.addListSelectionListener(new RightListListener());
      }
      listPane.setPreferredSize(new Dimension(150, 200));
      listPane.setAlignmentX(CENTER_ALIGNMENT);
      panel.add(listPane);
      panel.setAlignmentX(CENTER_ALIGNMENT);
      return panel;
   }

   /**
    * Enables <code>m_removeBtn</code> if there is a selection in </code>
    * m_targetList</code> list or else disables it.
    */
   private class RightListListener implements ListSelectionListener
   {
      public void valueChanged(ListSelectionEvent e)
      {
         if (m_targetList.getSelectedIndices().length == 0)
            m_removeBtn.setEnabled(false);
         else
            m_removeBtn.setEnabled(true);
      }
   }

   /**
    * Enables <code>m_addBtn</code> if there is
    * a selection in </code>m_srcList</code> list or else disables it.
    */
   private class LeftListListener implements ListSelectionListener
   {
      public void valueChanged(ListSelectionEvent e)
      {
         if (m_srcList.getSelectedIndices().length == 0)
            m_addBtn.setEnabled(false);
         else
            m_addBtn.setEnabled(true);
      }
   }

   /**
    * NOTE:: may change once the data objects are got.
    *
    * This is a wrapper class for <code>???</code> objects
    * which have to be displayed in a fixed order.
    * The order is determined by the first parameter of the class's constructor
    * and the wrapped object is the second parameter.
    */
   private static class IndexedObject implements Comparable
   {
      /**
       * Constructs an indexed wrapper object for <code>o</code> indexed on
       * <code>index</code>.
       * @param index object's index
       * @param o wrapped object assumed to be not <code>null</code>
       */
      IndexedObject(int index, Object o)
      {
         m_index = index;
         m_object = o;
      }

      /**
       * Gets the index of the wrapped object.
       * @return index of the wrapped object.
       */
      int getIndex()
      {
         return m_index;
      }

      /**
       * Gets the wrapped object.
       * @return, never <code>null</code>
       */
      Object getValue()
      {
         return m_object;
      }

      /**
       * Comparable interface implemented.Sorting based on the object's index
       * supplied.
       * @param comp object with which this object is being compared.
       * @return comparison result.
       *  1  - if the index of this object is greater than <code>comp<code>.
       * -1 - if the index of this object is less than <code>comp<code>.
       *  0  - if the index of this object is equal to <code>comp<code>.
       *
       */
      public int compareTo(Object comp)
      {
         IndexedObject io = (IndexedObject)comp;
         if (io.m_index < m_index)
            return 1;
         if (io.m_index > m_index)
            return -1;
         return 0;
      }

      /**
       * String representation of the object.
       * @return never <code>null</code> or empty.
       */
      public String toString()
      {
         return m_object.toString();
      }

      /**
       * Index of the wrapped object, <code>m_object</code>.
       */
      int m_index;

      /**
       * Object being wrapped for indexing purpose.Initialised in the
       * constructor and never <code>null</code> after that.
       */
      Object m_object;
   }

   //test code
   public static void main(String[] arg)
   {
      JFrame f = new JFrame("BoxLayoutDemo");
      Container contentPane = f.getContentPane();
      PSSourceTargetListPanel ac = new PSSourceTargetListPanel("Source", "Target"
            ,"Add", "Remove");

      DefaultListModel src = new DefaultListModel();
      IndexedObject a = new IndexedObject(0, "A");
      src.addElement(a);
      src.addElement(new IndexedObject(1, "B"));
      ac.setModel(src, true);


      contentPane.add(ac, BorderLayout.CENTER);
      f.addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent e) {
            System.exit(0);
         }
      });
      f.pack();
      f.setVisible(true);
   }
   //end

   /**
    * The list on the left side of the panel, initialized in {@link#
    * createListPanel(boolean)}, never <code>null</code> or modified after that.
    */
   private JList m_srcList;

   /**
    * The list on the right side of the panel, initialized in {@link#
    * createListPanel(boolean)}, never <code>null</code> or modified after that.
    */
   private JList m_targetList;

   /**
    * The top button for removing selected items from <code>m_srcList</code> to
    * <code>m_targetList</code>. Initialized in {@link#init()}, never <code>null
    * </code> or modified after that.
    */
   private UTFixedButton m_addBtn;

   /**
    * The bottom button for removing selected items from <code>m_targetList
    * </code> to <code>m_srcList</code>. Initialized in {@link#init()}, never
    * <code>null</code> or modified after that.
    */
   private UTFixedButton m_removeBtn;

   /**
    * Name of the label for the list on the left side of the panel. Initialized
    * in the ctor, never <code>null</code> or modified after that.
    */
   private String m_srcName;

   /**
    * Name of the label for the list on the right side of the panel. Initialized
    * in the ctor, never <code>null</code> or modified after that.
    */
   private String m_targetName;

   /**
    * Name  for the <code>m_addBtn</code>. Initialized in the ctor, never <code>
    * null</code> or modified after that.
    */
   private String m_topBtnName;

   /**
    * Name  for the <code>m_removeBtn</code>. Initialized in the ctor, never <code>
    * null</code> or modified after that.
    */
   private String m_btmBtnName;
   
   /**
    * Mnemonic  for the <code>m_btmBtnName</code>. Initialized in the ctor, 
    * may be <code>null</code>.
    */
   private String m_btmBtnMnem;
   
   /**
    * Mnemonic  for the <code>m_topBtn</code>. Initialized in the ctor, 
    * may be <code>null</code>.
    */
   private String m_topBtnMnem;
}
