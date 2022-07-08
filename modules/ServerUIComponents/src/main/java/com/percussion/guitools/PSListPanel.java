/******************************************************************************
 *
 * [ PSListPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.guitools;

import com.percussion.UTComponents.UTFixedButton;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * This class functions in a simlilar fashion as a <code>JTabbedPane</code>, but
 * instead of tabs, there is list box to one side displaying the title of each
 * panel, and a panel next to it that displays the panel corresponding to the 
 * selected item in the list.  There are optional reorder buttons that will 
 * allow for reordering the list.
 * <p>
 * Note: This class does not override all inherited methods and so the user of 
 * this class should not attempt to add, remove, or modify the
 * panel contents through the base class methods as the results will be 
 * unpredictable.  Only the methods defined by this class should be used to
 * modify the contents of this panel.
 */
public class PSListPanel extends JPanel implements ListSelectionListener
{
   /**
    * Constructs an empty list panel.  Panels should then be added using one of
    * the <code>addPanel()</code> methods.   By default, the first panel in the 
    * list is selected if any panels have been added.
    * 
    * @param labelText The text of the label that may appear above the list box, 
    * may be <code>null</code> or empty.
    * @param listAlignment Either {@link #ALIGN_LEFT} or {@link #ALIGN_RIGHT} to
    * determine the placement of the list.
    * @param allowReorder If <code>true</code>, buttons next to the list box
    * will allow the panel list to be reordered, otherwise the buttons are not
    * displayed.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSListPanel(String labelText, String listAlignment, 
      boolean allowReorder)
   {
      super();
      
      if (!(ALIGN_LEFT.equals(listAlignment) || 
         ALIGN_RIGHT.equals(listAlignment)))
      {
         throw new IllegalArgumentException("listAlignment is invalid");
      }
      
      m_labelText = labelText;
      m_allowReorder = allowReorder;
      initDialog(listAlignment);
   }
   
   /**
    * Overriden as this method is not supported. Manager supplied is ignored.
    */
   public void setLayout(LayoutManager mgr)
   {
      // noop;
   }
   
   /**
    * Sets the message to display when the list is empty.
    * 
    * @param message The message, may be <code>null</code> or empty to clear
    * the message.
    */
   public void setEmptyListMessage(String message)
   {
      if (message == null)
         message = "";
      
      m_emptyMessage = message;
   }
   
   /**
    * Adds the supplied panel, appending it to the end of the list.  
    * {@link #getName()} is called on the panel to get the title used in the
    * list.  The first panel in the list is selected by default.
    * 
    * @param panel The panel to add, may not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if <code>panel</code> is 
    * <code>null</code>.
    */
   public void addPanel(JPanel panel)
   {
      if (panel == null)
         throw new IllegalArgumentException("panel may not be null");
         
      addPanel(panel.getName(), panel, -1, false);
   }

   /**
    * Adds the panel with the specified title, appending it to the end of the 
    * list.  The first panel in the list is selected by default.
    * 
    * @param title The title that will appear in the list, may not be 
    * <code>null</code> or empty.
    * @param panel The panel to add, may not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    */   
   public void addPanel(String title, JPanel panel)
   {
      if (title == null || title.trim().length() == 0)
         throw new IllegalArgumentException("title may not be null or empty");
      
      if (panel == null)
         throw new IllegalArgumentException("panel may not be null");
         
      addPanel(title, panel, -1, false);
   }
   
   
   /**
    * Adds the supplied panel, inserting it at the specified index in the list.  
    * {@link #getName()} is called on the panel to get the title used in the
    * list.  The first panel in the list is selected by default.
    * 
    * @param panel The panel to add, may not be <code>null</code>.
    * @param index The index at which the panel is to be inserted.  
    * A value of <code>-1</code> or a value that exceeds the current panel count
    * will append the panel to the end of the list.  May not be less than 
    * <code>-1</code>.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    */
   public void addPanel(JPanel panel, int index)
   {
      if (panel == null)
         throw new IllegalArgumentException("panel may not be null");
      
      if (index < -1)
         throw new IllegalArgumentException("index may not be less than -1");
      
      addPanel(panel.getName(), panel, index, false);
   }
   
   /**
    * Adds the specified panel, inserting it at the specified index in the list 
    * with the supplied title.
    * 
    * @param title The title that will appear in the list, may not be 
    * <code>null</code> or empty.
    * @param panel The panel to add, may not be <code>null</code>.
    * @param index The index at which the panel is to be inserted.  
    * A value of <code>-1</code> or a value that exceeds the current panel count
    * will append the panel to the end of the list.  May not be less than 
    * <code>-1</code>.
    * @param selected If <code>true</code>, the added panel will be selected.
    * If <code>false</code>, the currently selected panel will remain selected,
    * or if none are selected, the first panel will be selected.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    */
   public void addPanel(String title, JPanel panel, int index, boolean selected)
   {
      if (title == null || title.trim().length() == 0)
         throw new IllegalArgumentException("title may not be null or empty");
      
      if (panel == null)
         throw new IllegalArgumentException("panel may not be null");
         
      int curSelected = m_list.getSelectedIndex();
      
      if (index == -1 || index >= m_model.size())
      {
         m_model.addElement(new ListElement(title, panel));
         index = m_model.size() - 1;
      }
      else
         m_model.add(index, new ListElement(title, panel));

      // update display panel preferred size to account for new panel
      updateDisplayPanelSize(panel);
      
      // default new selection to the first item if none selected
      int newSelection = 0;
      
      // handle users selected choice
      if (selected)
         newSelection = index;
      else
      {
         // change to reflect the addition if inserted above in the list
         if (curSelected != -1)
            newSelection = curSelected >= index ? curSelected + 1 : curSelected;
      }
      
      // update the list selection
      resetSelection(newSelection);
   }

   /**
    * Removes the supplied panel from the list.
    * 
    * @param panel The panel to remove, may not be <code>null</code>.  The first
    * matching panel found in the list is removed.
    * 
    * @throws IllegalArgumentException if <code>panel</code> is 
    * <code>null</code>.
    */
   public void removePanel(JPanel panel)
   {
      if (panel == null)
         throw new IllegalArgumentException("panel may not be null");
      
      int index = getIndex(panel);
      
      if (index != -1)
         removePanel(index);
   }
   
   /**
    * Removes the panel at the specified index.  
    * 
    * @param index The index into the list specifying the panel to remove.  Must
    * be greater than or equal to zero, and less than {@link #getPanelCount()}.
    * 
    * @throws IllegalArgumentException if <code>index</code> is 
    * invalid.
    */
   public void removePanel(int index)
   {
      checkIndex(index);
      
      int selected = m_list.getSelectedIndex();
      m_model.removeElementAt(index);
      
      // change selection to reflect the removal if removed above in the list
      if (selected != -1)
         selected = selected >= index ? selected - 1 : selected;
      
      resetSelection(selected);
   }
   
   /**
    * Removes all panels from the list.
    */
   public void removeAll()
   {
      m_model.removeAllElements();
      resetSelection(-1);
   }
   
   /**
    * Gets the count of panels in the list.
    * 
    * @return The count, will be greater than or equal to zero.
    */
   public int getPanelCount()
   {
      return m_model.size();
   }
   
   /**
    * Gets the selected panel's index.  
    * 
    * @return The index, will be greater than or equal to zero, and less than 
    * {@link #getPanelCount()}, or if no panels have been added, <code>-1</code>
    * is returned.
    */
   public int getSelectedIndex()
   {
      return m_list.getSelectedIndex();
   }
   
   /**
    * Selects the panel at the specified index in the list.
    * 
    * @param index The index into the list specifying the panel to select.  Must
    * be greater than or equal to zero, and less than {@link #getPanelCount()}.
    * 
    * @throws IllegalArgumentException if <code>index</code> is 
    * invalid.
    * @throws IllegalStateException if no panels have been added to the list.
    */
   public void setSelectedIndex(int index)
   {
      checkState();
      checkIndex(index);
      
      resetSelection(index);
   }
   
   /**
    * Sets the title color of the panel at the specified index.
    * 
    * @param index The index of the panel for which the title color is to be 
    * set.  Must be greater than or equal to zero, and less than 
    * {@link #getPanelCount()}.
    * 
    * @param color The color, may be <code>null</code> to clear any previously
    * set color.
    */
   public void setTitleColor(int index, Color color)
   {
      checkIndex(index);

      // set color and trigger model change
      int[] sel = m_list.getSelectedIndices();
      ListElement el = (ListElement)m_model.remove(index);
      el.setTitleColor(color);
      m_model.add(index, el);
      m_list.setSelectedIndices(sel);
   }

   /**
    * Sets the title image of the panel at the specified index.
    * 
    * @param index The index of the panel for which the title image is to be 
    * set.  Must be greater than or equal to zero, and less than 
    * {@link #getPanelCount()}.
    * 
    * @param image The image, may be <code>null</code> to clear any previously
    * set image.  If supplied, image is displayed at the leading end of the 
    * title.
    */
   public void setTitleImage(int index, ImageIcon image)
   {
      checkIndex(index);

      // add image, and trigger model change
      int[] sel = m_list.getSelectedIndices();
      ListElement el = (ListElement)m_model.remove(index);
      el.setTitleImage(image);
      m_model.add(index, el);
      m_list.setSelectedIndices(sel);
   }
   
   /**
    * Gets the currently selected panel.  
    * 
    * @return The selected panel, never <code>null</code> .
    * 
    * @throws IllegalStateException if no panels have been added to the list.
    */
   public JPanel getSelectedPanel()
   {
      checkState();
      
      ListElement el = (ListElement)m_list.getSelectedValue();
      return el.getPanel();
   }
   
   /**
    * Sets the supplied panel as selected.
    * 
    * @param panel The panel to select, may not be <code>null</code>.  The first 
    * matching panel found in the list will be selected.
    * 
    * @throws IllegalStateException if no panels have been added to the list.
    * @throws IllegalArgumentException if <code>panel</code> is 
    * <code>null</code> or if it is not found in the list.
    */
   public void setSelectedPanel(JPanel panel)
   {
      checkState();
         
      if (panel == null)
         throw new IllegalArgumentException("panel may not be null");
      
      int index = getIndex(panel);
      if (index == -1)
         throw new IllegalArgumentException("panel not found in list");
      
      setSelectedIndex(index);
   }
   
   /**
    * Gets the panel with the matching title.
    * 
    * @param title The title to check for, may not be <code>null</code> or 
    * empty.
    * 
    * @return The first panel found in the list with the specified title, or 
    * <code>null</code> if no matching panel is found.
    * 
    * @throws IllegalArgumentException if <code>title</code> is 
    * <code>null</code> or empty.
    */
   public JPanel getPanel(String title)
   {
      if (title == null || title.trim().length() == 0)
         throw new IllegalArgumentException("title may not be null or empty");
      
      JPanel panel = null;
      
      for (int i = 0; i < m_model.size(); i++) 
      {
         ListElement el = (ListElement)m_model.get(i);
         if (el.getTitle().equals(title))
         {
            panel = el.getPanel();
            break;
         }
      }
      
      return panel;
   }
   
   /**
    * Gets the index of the first matching panel found in the list.
    * 
    * @param panel The panel to match, may not be <code>null</code>.
    * 
    * @return The index, or <code>-1</code> if no matching panel is found.
    * 
    * @throws IllegalArgumentException if <code>panel</code> is 
    * <code>null</code>.
    */
   public int getIndex(JPanel panel)
   {
      if (panel == null)
         throw new IllegalArgumentException("panel may not be null");
      
      int index = -1;
      
      for (int i = 0; i < m_model.size(); i++) 
      {
         ListElement el = (ListElement)m_model.get(i);
         JPanel test = el.getPanel();
         if (panel == test)
         {
            index = i;
            break;
         }
      }
      
      return index;
   }
   
   /**
    * Get the panel at the specified index.
    * 
    * @param index The index of the panel to return, greater than or equal to 
    * zero, and less than {@link #getPanelCount()}.
    * 
    * @return The panel, never <code>null</code>.
    * 
    * @throws IllegalArgumentException if <code>index</code> is invalid.
    */
   public JPanel getPanel(int index)
   {
      checkIndex(index);
      
      ListElement el = (ListElement)m_model.get(index);
      
      return el.getPanel();
   }
   
   /**
    * Gets an iterator over the list of panels.  Will not include hidden
    * panels (see {@link #hidePanel(JPanel)}.
    * 
    * @return The iterator, never <code>null</code>, may be empty.  Backed by
    * an immutable list, so its contents will not change as panels are hidden
    * and shown.
    */
   public Iterator getPanels()
   {
      List panelList = new ArrayList();
      for (int i = 0; i < m_model.size(); i++) 
      {
         ListElement el = (ListElement)m_model.get(i);
         panelList.add(el.getPanel());
      }
      return panelList.iterator();
   }
   
   /**
    * Gets the title of the panel at the specified index.
    * 
    * @param index The index of the panel in the list, must be greater than or 
    * equal to zero, and less than {@link #getPanelCount()}. 
    * 
    * @return The title, never <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if <code>index</code> is invalid.
    */
   public String getTitle(int index)
   {
      checkIndex(index);
      
      ListElement el = (ListElement)m_model.get(index);
      
      return el.getTitle();
   }
   
   /**
    * Hides the specified panel.  Panel may be unhidden by calling 
    * {@link #showPanel(JPanel)}.  Not allowed if reordering is enabled (see
    * ctor for more info).
    * 
    * @param panel The panel to hide, may not be <code>null</code>.
    * 
    * @return <code>true</code> if the specified panel was hidden, 
    * <code>false</code> if it was not found in the list.
    * 
    * @throws IllegalStateException if reordering is enabled.
    */
   public boolean hidePanel(JPanel panel)
   {
      if (panel == null)
         throw new IllegalArgumentException("panel may not be null");
      
      if (m_allowReorder)
         throw new IllegalStateException(
            "Cannot hide panels if reorder is enabled");
      
      boolean hidden = false;
      if (m_hiddenPanels.containsKey(panel))
         return hidden;
         
      int curSelIndex = m_list.getSelectedIndex();
      JPanel curSelPanel = getPanel(curSelIndex);
      int index = getIndex(panel);
      if (index != -1)
      {
         HiddenPanel hiddenPanel = new HiddenPanel();
         hiddenPanel.m_listElement = (ListElement) m_model.remove(index);
         hiddenPanel.m_index = index;         
         m_hiddenPanels.put(panel, hiddenPanel);
         
         if (curSelPanel != panel)
            setSelectedPanel(curSelPanel);
         else
            resetSelection(index);
         hidden = true;
      }
      
      return hidden;
   }
   
   /**
    * Shows the previously hidden panel.  Attempts to show it at the original 
    * index if available, otherwise it is added to the end of the list.
    * 
    * @param panel The panel to show, may not be <code>null</code>.
    * 
    * @return <code>true</code> if a the same panel was hidden and is now
    * showing, <code>false</code> if the panel was not currently hidden.
    */
   public boolean showPanel(JPanel panel)
   {
      if (panel == null)
         throw new IllegalArgumentException("panel may not be null");
      
      boolean shown = false;
      
      HiddenPanel hidden = (HiddenPanel) m_hiddenPanels.remove(panel);
      if (hidden != null)
      {
         ListElement el = hidden.m_listElement;
         int index = hidden.m_index;
         int curSelected = m_list.getSelectedIndex();
         
         if (index == -1 || index >= m_model.size())
         {
            m_model.addElement(el);
            index = m_model.size() - 1;
         }
         else
            m_model.add(index, el);

         // default new selection to the first item if none selected
         int newSelection = 0;

         // change to reflect the addition if inserted above in the list
         if (curSelected != -1)
            newSelection = curSelected >= index ? curSelected + 1 : 
               curSelected;
         
         // update the list selection
         resetSelection(newSelection);            
         shown = true;
         
      }
      
      return shown;
   }
   
   /**
    * Get all panels that are currently hidden
    * 
    * @return An iterator over zero or more <code>JPanel</code> objects, 
    * never <code>null</code>.  Iterator is backed by immutable list, so its
    * contents will not change as panels are hidden and shown.  
    */
   public Iterator getHiddenPanels()
   {
      List hiddenList = new ArrayList();
      
      // return list in reverse order so indexes are likely to be correctly
      // restored
      hiddenList.addAll(m_hiddenPanels.keySet());
      Collections.reverse(hiddenList);
      
      return hiddenList.iterator();
   }
   
   /**
    * Checks to see if the index refers to a valid index into the current
    * panel list.
    * 
    * @param index The index to check.  Must be less than the number of panels
    * currently in the list, and greater than or equal to zero.
    * 
    * @throws IllegalArgumentException If the index is invalid.
    */
   private void checkIndex(int index)
   {
      if (index >= m_model.size() || index < 0)
         throw new IllegalArgumentException("invalid index");
   }
   
   /**
    * Checks to see if any panels have been added.  Used by methods that require
    * at least one panel to have been added.
    * 
    * @throws IllegalStateException if no panels have been added.
    */
   private void checkState()
   {
      if (m_model.size() == 0)
         throw new IllegalStateException("no panels added");
   }
   
   /**
    * Resets the current selection in the list to a valid selection. Usually 
    * called when an entry has been added or removed.  Resets the selection to 
    * the provided <code>index</code> if it is valid, otherwise sets it to the 
    * previous item in the list if there is one.  If the list is now empty,
    * clears the bottom panel.
    * 
    * @param index The index to use for the selection.
    */
   private void resetSelection(int index)
   {
      
      if (m_model.size() == 0)
      {
         m_displayPanel.removeAll();
         JPanel emptyPanel = new JPanel(new BorderLayout());
         emptyPanel.add(new JLabel(m_emptyMessage, SwingConstants.CENTER), 
            BorderLayout.CENTER);
         m_displayPanel.add(emptyPanel, BorderLayout.CENTER);
         refreshPanel();
      }
      else 
      {
         int curSelected = m_list.getSelectedIndex();
         int newSelected = 0;
         // first check if selecting a valid item in the list
         if (index < m_model.size() && index >= 0)
            newSelected = index;
         // if trying to select one greater than the last element in the list,
         // select the last element.  Usually caused by removing the last panel
         // while it is selected.
         else if (index < m_model.size() + 1 && index > 0)
            newSelected = index - 1;
            
         // update the list selection
         m_list.setSelectedIndex(newSelected);
         
         // handle ui update if selection index didn't really change - this
         // happens if the selected item is removed and the selected index
         // remains the same, but there is now a different panel at that index 
         // in the model.
         if (newSelected == curSelected)
            changeDisplayPanel(newSelected);
      }
   }
   
   /**
    * Initializes the panel's default components.
    * 
    * @param listAlignment One of the <code>ALIGN_xxx</code> constants, 
    * determines placement of the listbox.
    */
   private void initDialog(String listAlignment)
   {
      super.setLayout(new BorderLayout(5, 0));
      
      // create display panel
      m_displayPanel = new JPanel(new BorderLayout());
      m_displayPanel.setAlignmentX(CENTER_ALIGNMENT);
      m_displayPanel.setAlignmentY(CENTER_ALIGNMENT);
      
      // adding a border will throw off prefered size calculations for the 
      // display panel, so wrap it with another panel and add the border to
      // that.
      JPanel borderPanel = new JPanel(new BorderLayout());
      borderPanel.setBorder(BorderFactory.createEtchedBorder(
         EtchedBorder.LOWERED));
      borderPanel.add(m_displayPanel, BorderLayout.CENTER);
      add(borderPanel, BorderLayout.CENTER);
      
      // create list panel
      JPanel listPanel = createListPanel();
      listPanel.setAlignmentX(CENTER_ALIGNMENT);
      listPanel.setAlignmentY(CENTER_ALIGNMENT);
      add(listPanel, listAlignment);
   }
   
   /**
    * Creates the list panel including the label, listbox, and if appropriate,
    * the reorder buttons.
    * 
    * @return The newly created panel, never <code>null</code>.
    */
   private JPanel createListPanel()
   {
      // create panel to hold label above and list box panel below
      JPanel listPanel = new JPanel();
      listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
      JLabel label = new JLabel(m_labelText, SwingConstants.LEFT);
      label.setAlignmentX(LEFT_ALIGNMENT);
      listPanel.add(label);
      listPanel.add(Box.createVerticalStrut(5));
      
      // create a panel to hold the listbox and optional buttons
      JPanel listBoxPanel = new JPanel();
      listBoxPanel.setLayout(new BoxLayout(listBoxPanel, BoxLayout.X_AXIS));
      listBoxPanel.setAlignmentX(LEFT_ALIGNMENT);
      
      // create the listbox
      m_model = new DefaultListModel();
      m_list = new JList(m_model);      
      m_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      m_list.addListSelectionListener(this);
      m_list.setCellRenderer(new DefaultListCellRenderer()
      {
         public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus)
         {
            ImageListCellRenderer rend = new ImageListCellRenderer();
            
            ListElement element = null;
            ImageIcon image = null;
            Color color = null;
            
            if (index < m_model.size())
            {
               element = (ListElement)m_model.get(index);
               if (element != null)
               {
                  color = element.getTitleColor();
                  image = element.getTitleImage();
               }
            }
            
            Component comp = rend.getListCellRendererComponent(list, value, 
               index, isSelected, cellHasFocus, image);
            
            if (color != null)
               comp.setForeground(color);
            
            return comp;
         }
      });
      
      JScrollPane listPane = new JScrollPane(m_list);
      listPane.setAlignmentY(CENTER_ALIGNMENT);
      listBoxPanel.add(listPane);      
      
      // add optional buttons
      if (m_allowReorder)
      {
         // create panel for up and down buttons
         listBoxPanel.add(Box.createHorizontalStrut(5));
         JPanel movePanel = new JPanel();      
         movePanel.setLayout(new BoxLayout(movePanel, BoxLayout.Y_AXIS));
         movePanel.add(Box.createVerticalGlue());
         UTFixedButton upButton = new UTFixedButton(ms_imageLoader.getImage(
            getResourceString("gif_up")), new Dimension(24,24));  
         upButton.setEnabled(true);
         upButton.addActionListener(new ActionListener() 
         {
            public void actionPerformed(ActionEvent e) 
            {
               moveUp();
            }
         });
         movePanel.add(upButton);
         movePanel.add(Box.createVerticalStrut(10));
         UTFixedButton downButton = new UTFixedButton(ms_imageLoader.getImage(
            getResourceString("gif_down")), new Dimension(24,24) ); 
         downButton.setEnabled(true);         
         downButton.addActionListener(new ActionListener() 
         {
            public void actionPerformed(ActionEvent e) 
            {
               moveDown();
            }
         });
         movePanel.add(downButton);
         movePanel.add(Box.createVerticalGlue());
         movePanel.setAlignmentY(CENTER_ALIGNMENT);
         listBoxPanel.add(movePanel);    
      }
      
      listPanel.add(listBoxPanel);
      
      return listPanel;
   }
   
   /**
    * Handles the list selection change event. Resets the bottom panel to 
    * contain the panel corresponding to the current list selection.  See
    * {@link ListSelectionListener} for more information.
    */
   public void valueChanged(ListSelectionEvent e)
   {
      // show the correct panel
      if (!e.getValueIsAdjusting())
      {
         int index = m_list.getSelectedIndex();
         if (index < m_model.size() && index >= 0)
         {
            changeDisplayPanel(index);
         }
      }  
   }
   
   /**
    * Make the specified panel the currently displayed panel
    * 
    * @param index The index into the list of the panels to display, assumed to
    * be a valid index into the list.
    */
   private void changeDisplayPanel(int index)
   {
      ListElement element = (ListElement)m_model.get(index);
      m_displayPanel.removeAll();
      m_displayPanel.add(element.getPanel(), BorderLayout.CENTER);
      refreshPanel();
   }
   

   /**
    * UI doesn't always get the changes correct, so this forces the repaint so
    * it works correctly.
    */
   private void refreshPanel()
   {
      revalidate();
      repaint();
   }   
   
   /**
    * Checks preferred size of supplied panel against current preferred size
    * of display panel to ensure it is able to accomodate the largest height and
    * width of every panel.
    * 
    * @param newPanel The new panel to check, assumed not <code>null</code>.
    */
   private void updateDisplayPanelSize(JPanel newPanel)
   {
      Dimension cur = m_displayPanel.getPreferredSize();
      Dimension check = newPanel.getPreferredSize();
      int height = check.height > cur.height ? check.height : cur.height;
      int width = check.width > cur.width ? check.width : cur.width;
      m_displayPanel.setPreferredSize(new Dimension(width, height));
   }
   
   /**
    * Moves the currently selected item in the list up one position.  If the
    * list is empty or the first item in the list is selected, nothing happens.
    */
   private void moveUp()
   {
      int index = m_list.getSelectedIndex();
      if (m_model.size() > 0 && index > 0)
      {
         ListElement el = (ListElement)m_model.get(index);
         m_model.remove(index);
         m_model.add(--index, el);
         m_list.setSelectedIndex(index);
      }
   }
   
   /**
    * Moves the currently selected item in the list down one position.  If the
    * list is empty or the last item in the list is selected, nothing happens.
    */
   private void moveDown()
   {
      int index = m_list.getSelectedIndex();
      if (m_model.size() > 0 && index < m_model.size() - 1)
      {
         ListElement el = (ListElement)m_model.get(index);
         m_model.remove(index);
         if (++index < m_model.size())
            m_model.add(index, el);
         else
            m_model.addElement(el);
         
         m_list.setSelectedIndex(index);
      }
   }
   
   /**
    * Gets the resource string identified by the specified key.  If the
    * resource cannot be found, the key itself is returned.
    *
    * @param key identifies the resource to be fetched; assumed not <code>null
    * </code> or empty.
    * 
    * @return String value of the resource identified by <code>key</code>, or
    * <code>key</code> itself.
    */
   private static String getResourceString(String key)
   {
      String resourceValue = key;
      try
      {
         resourceValue = ms_res.getString( key );
      } 
      catch (MissingResourceException e)
      {
         // not fatal; warn and continue
         System.err.println( PSListPanel.class );
         System.err.println( e );
      }
      
      return resourceValue;
   }

   /**
    * Constant to indicate the list should be placed on the left side of the
    * display panel.
    */
   public static final String ALIGN_LEFT = BorderLayout.WEST;
   
   /**
    * Constant to indicate the list should be placed on the right side of the
    * display panel.
    */
   public static final String ALIGN_RIGHT = BorderLayout.EAST;
   
   
   /**
    * The list box containing the list of panel titles. Initialized during
    * construction, never <code>null</code> or modified after that.
    */
   private JList m_list;
   
   /**
    * The list model supplied when creating {@link #m_list}, initialized during
    * construction, never <code>null</code> or modified after that.
    */
   private DefaultListModel m_model;
   
   /**
    * The display panel, acts as a container for the currently selected panel
    * in the list.   Initialized during construction, never <code>null</code>
    * after that.  Displays different panels depending on the selection in 
    * {@link #m_list}.
    */
   private JPanel m_displayPanel;
   
   /**
    * Determines if the top panel will display up and down buttons to allow the
    * user to reorder the list items.  Intialized during constructor, never
    * modified after that.
    */
   private boolean m_allowReorder = false;
   
   /**
    * The label text to display above the list box, supplied during
    * construction, may be <code>null</code> or empty, never modified after 
    * that.
    */
   private String m_labelText;
   
   /**
    * Map of hidden panels. Key is the <code>JPanel</code> that is hidden, value 
    * is a {@link HiddenPanel}.  Never <code>null</code>.
    */
   private Map m_hiddenPanels = new LinkedHashMap();
   
   /**
    * Message to display when the list is empty.  Never <code>null</code>,
    * initially an empty string.
    */
   private String m_emptyMessage = "";
   
   /**
    * Image loader used to load images for buttons.  Initialized by static
    * initializer, never <code>null</code> after that.
    */
   private static BitmapManager ms_imageLoader;
   
   /**
    * Resource bundle for this class.  Initialized by static initializer, never 
    * <code>null</code> after that.
    */
   private static ResourceBundle ms_res = null;
   
   static
   {
      ms_imageLoader = BitmapManager.getBitmapManager(
         PSListPanel.class);
         
      try 
      {
         ms_res = ResourceBundle.getBundle(
            "com.percussion.guitools.GuitoolsResources",
            Locale.getDefault() );
      }
      catch(MissingResourceException mre)
      {
         mre.printStackTrace();
      }
   }

   /**
    * Utility class to use as object in the list model encapsulating a panel and
    * its title.
    */
   private class ListElement
   {
      /**
       * Construct the list element from its member data.
       * 
       * @param title The title of the panel, may not be <code>null</code> or 
       * empty.
       * @param panel The panel, may not be <code>null</code>.
       * 
       * @throws IllegalArgumentException if any param is invalid.
       */
      private ListElement(String title, JPanel panel)
      {
         if (title == null || title.trim().length() == 0)
            throw new IllegalArgumentException(
               "title may not be null or empty");
         
         if (panel == null)
            throw new IllegalArgumentException("panel may not be null");
         
         m_title = title;
         m_panel = panel;
      }
      
      /**
       * Get the title of the panel.
       * 
       * @return The title, never <code>null</code> or empty.
       */
      private String getTitle()
      {
         return m_title;
      }
      
      /**
       * Get the panel.
       * 
       * @return The panel, never <code>null</code>.
       */
      private JPanel getPanel()
      {
         return m_panel;
      }
      
      /**
       * Returns the title, used to display the item in the list.
       * 
       * @return The title, never <code>null</code> or empty.
       */
      public String toString()
      {
         return m_title;
      }
      
      /**
       * Sets the foreground color of the title.  See {@link #getTitleColor()}
       * for more info.
       *  
       * @param c The color, may be <code>null</code> to clear it.
       */
      private void setTitleColor(Color c)
      {
         m_color = c;
      }
      
      /**
       * Get the foreground color of the title.  If not set, the default color
       * is used.
       * 
       * @return The color, or <code>null</code> to indicate the default should
       * be used.
       */
      private Color getTitleColor()
      {
         return m_color;         
      }
      
      /**
       * Set the image to use to decorate the title.  See 
       * {@link #getTitleImage()} for more info.
       * 
       * @param image The image, may be <code>null</code>.
       */
      private void setTitleImage(ImageIcon image)
      {
         m_image = image; 
      }
      
      /**
       * Get the image used to decorate the title.  Image is displayed 
       * immediately before the title.
       * 
       * @return The image, or <code>null</code> if one has not been set.
       */
      private ImageIcon getTitleImage()
      {
         return m_image;
      }
      
      /**
       * The title of the panel, initialized during ctor, never 
       * <code>null</code>, empty, or modified after that.
       */
      private String m_title;
      
      /**
       * The panel, initialized during ctor, never <code>null</code> or modified 
       * after that.
       */
      private JPanel m_panel;
      
      /**
       * Color to use for foreground color of title in the list, may be 
       * <code>null</code> if never set.
       */
      private Color m_color = null;
      
      /**
       * Image icon to use to decorate the title in the list, may be 
       * <code>null</code> if never set.
       */
      private ImageIcon m_image;
   }  

   /**
    * Renderer to allow both text and an image.
    */
   private class ImageListCellRenderer extends DefaultListCellRenderer
   {
      /**
       * See {@link DefaultListCellRenderer#getListCellRendererComponent(
       * JList, Object, int, boolean, boolean)} for all info but the 
       * additional parameter noted below.
       * 
       * @param image The image to display, may be <code>null</code> if none is
       * desired, otherwise displayed at the leading end of the title.  Note 
       * that if supplied and the <code>value</code> supplied is also an 
       * <code>Icon</code>,then only the supplied <code>image</code> will 
       * be displayed.
       */
      public Component getListCellRendererComponent(JList list, 
         Object value, int index, boolean isSelected, 
         boolean cellHasFocus, ImageIcon image)
      {
         Component comp = super.getListCellRendererComponent(list, 
            value, index, isSelected, cellHasFocus);
         
         if (image != null)
            setIcon(image);
         
         return comp;
      }  
   }
   
   /**
    * Encapsulates info about a panel that has been hidden.
    */
   private class HiddenPanel
   {
      /**
       * The list element of the panel.
       */
      public ListElement m_listElement = null;
      
      /**
       * The index of the panel
       */
      public int m_index = -1;
   }
}
