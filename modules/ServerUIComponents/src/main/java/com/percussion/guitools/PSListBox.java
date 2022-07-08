/*[ PSListBox.java ]**********************************************************
 *
 * COPYRIGHT (c) 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.guitools;

import com.percussion.UTComponents.UTFixedButton;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * A simple class to create a list box with add and remove buttons below.
 * <code>onAdd()</code> is abstract, and caller must overide to provide the
 * implementation of the add button to supply the object to add to the list. 
 * The list will render objects using their <code>toString()</code> method.  
 * Mult-select is not currently supported. 
 */
public abstract class PSListBox extends JPanel
{
   /**
    * Construct the list box, providing its title and initial data.
    * 
    * @param title The title of the list, may not be <code>null</code> or empty.
    * Appears in the border used to group all list components.
    * @param listData The initial data, an iterator over zero or more objects
    * to include in the list.  May not be <code>null</code>, may be empty.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSListBox(String title, Iterator listData)
   {
      super();
      
      if (title == null || title.trim().length() == 0)
         throw new IllegalArgumentException("title may not be null or empty");
         
      if (listData == null)
         throw new IllegalArgumentException("listData may not be null");
         
      initPanel(title);
      initData(listData);
   }
   
   /**
    * Must be overriden by the derived class to handle clicking the "Add"
    * button.  Generally another dialog is shown to allow user to specify an
    * item to add to the list.
    * 
    * @return An object to add to the list, return <code>null</code> if none
    * to be provided.  <code>null</code> values are not added to the list.
    */
   public abstract Object onAdd();
   
   /**
    * May be overriden by the derived class to handle a double-click on a list
    * item.  Generally another dialog is shown to allow user to edit the 
    * selected item in the list.  The base class implementation simply returns
    * <code>null</code>, which results in a noop.
    * 
    * @param data The object represented by the current list selection, never
    * <code>null</code>.
    * 
    * @return The edited object.  If <code>null</code>, the edit event is 
    * ignored and the current selection is not modified.
    */
   public Object onEdit(Object data)
   {
      // noop in base class
      data = null;  // use reference to avoid eclipse warnings
      
      return data;
   }
   
   /**
    * Determine if the list already contains the supplied object.  Comparison
    * is made using the object's <code>equals</code> method.
    * 
    * @param obj The object to check, may not be <code>null</code>.
    * 
    * @return <code>true</code> if the object is contained in the list, 
    * <code>false</code> if not.
    * 
    * @throws IllegalArgumentException if <code>obj</code> is <code>null</code>.
    */
   public boolean containsItem(Object obj)
   {
      if (obj == null)
         throw new IllegalArgumentException("obj may not be null");
         
      return getListData().contains(obj);
   }

   /**
    * Get the contents of the list.
    * 
    * @return An iterator over zero or more objects that are currently contained
    * by this list.  Never <code>null</code>.
    */  
   public Iterator iterator()
   {
      return getListData().iterator();
   }
   
   /**
    * Add a listener to be informed of add, edit, and remove events.
    * 
    * @param listener The listener to add, may not be <code>null</code>.  
    * Upon notification, {@link ActionEvent#getActionCommand()} will return 
    * "add", "edit" or "remove".
    */
   public void addActionListener(ActionListener listener)
   {
      if (listener == null)
         throw new IllegalArgumentException("listener may not be null");

      m_actionListeners.add(listener);
   }
   
   /**
    * Get the contents of the list.
    * 
    * @return A list containing zero or more objects that are currently 
    * contained by this list.  Never <code>null</code>.
    */
   private List getListData()
   {
      List data = new ArrayList();
      DefaultListModel model = (DefaultListModel)m_list.getModel();
      for (int i = 0; i < model.getSize(); i++) 
      {
         data.add(model.getElementAt(i));
      }
      
      return data;
   }
   
   /**
    * Initializes all ui components.
    * 
    * @param title The title used in the group border, assumed not
    * <code>null</code> or empty.
    */
   private void initPanel(String title)
   {
      setLayout(new BorderLayout());
      setBorder(PSDialog.createGroupBorder(title));
      
      // add list
      m_list = new JList(new DefaultListModel());
      m_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      m_list.addListSelectionListener(new ListSelectionListener()
      {
         public void valueChanged(ListSelectionEvent e)
         {
            //Ignore extra messages
            if (e.getValueIsAdjusting())
               return;

            // handle enabling/disabling the remove button based on if something
            // is selected
            if (m_list.isSelectionEmpty())
            {
               m_removeButton.setEnabled(false);
            }
            else
            {
               m_removeButton.setEnabled(true);
            }
         }
      });
      
      // add listener to handle doubleclick on list item
      m_list.addMouseListener(new MouseAdapter()
      {
         public void mouseReleased(MouseEvent e) 
         {
            // only handle double-click
            if (e.getClickCount() == 2)
            {
               // edit selected item
               int index = m_list.getSelectedIndex();
               if (index != -1)
               {
                  DefaultListModel model = (DefaultListModel)m_list.getModel();
                  Object o = model.getElementAt(index);
                  if (o != null)
                  {
                     Object edited = onEdit(o);
                     if (edited != null)
                     {
                        // only handle non-null return
                        model.setElementAt(edited, index);
                        informListeners(o, "edit");
                     }
                  }                  
               }               
            }
         }
      });
      
      // make list scrollable
      JScrollPane scrollPane = new JScrollPane(m_list);
      scrollPane.setAlignmentY(CENTER_ALIGNMENT);
      add(scrollPane, BorderLayout.CENTER);
      
      // add and remove buttons
      JPanel buttonPanel = new JPanel();
      buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
      m_addButton = new UTFixedButton(getResources().getString("add"));
      m_removeButton = new UTFixedButton(getResources().getString("remove"));
      
      m_addButton.addActionListener(new ActionListener() 
      {
         public void actionPerformed(ActionEvent e) 
         {
            Object o = onAdd();
            if (o != null)
            {
               DefaultListModel model = (DefaultListModel)m_list.getModel();
               model.addElement(o);
               informListeners(o, "add");
            }
         }
      });
      
      m_removeButton.addActionListener(new ActionListener() 
      {
         public void actionPerformed(ActionEvent e) 
         {
            if (!m_list.isSelectionEmpty())
            {
               DefaultListModel model = (DefaultListModel)m_list.getModel();
               Object o = model.getElementAt(m_list.getSelectedIndex());
               model.removeElementAt(m_list.getSelectedIndex());
               // that will cause nothing to be selected, so disable the button
               // as the listener does not fire in this case
               m_removeButton.setEnabled(false);
               informListeners(o, "remove");
            }
         }
      });
      
      // initially nothing is selected, so disable the remove button
      
      m_removeButton.setEnabled(false);
      buttonPanel.add(Box.createHorizontalGlue());
      buttonPanel.add(m_addButton);
      buttonPanel.add(Box.createHorizontalStrut(40));
      buttonPanel.add(m_removeButton);
      buttonPanel.add(Box.createHorizontalGlue());
      buttonPanel.setAlignmentX(CENTER_ALIGNMENT);
      JPanel bottomPanel = new JPanel(new BorderLayout());
      bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      bottomPanel.add(buttonPanel, BorderLayout.CENTER);
      add(bottomPanel, BorderLayout.SOUTH);
      
   }
   
   /**
    * Informs listeners of add, edit, or remove events.
    * 
    * @param o The object added or removed, assumed not <code>null</code>. 
    * @param cmd The command, assumed to be "add", "edit", or "remove". 
    */
   private void informListeners(Object o, String cmd)
   {
      ActionEvent event = new ActionEvent(o, 0, cmd);
      Iterator listeners = m_actionListeners.iterator();
      while (listeners.hasNext())
      {
         ActionListener listener = (ActionListener)listeners.next();
         listener.actionPerformed(event);
      }
   }

   /**
    * Initializes the data in the list.
    * 
    * @param listData An iterator over zero or more objects to add to the list,
    * assumed not <code>null</code>.
    */
   private void initData(Iterator listData)
   {
      DefaultListModel model = (DefaultListModel)m_list.getModel();
      while (listData.hasNext())
      {
         model.addElement(listData.next());
      }
   }
   
   /**
    * Get the resource bundle used by this class.
    * 
    * @return The bundle, should never be <code>null</code>.  If an unexpected
    * error occurs loading the bundle, the stack trace is written to the console
    * and <code>null</code> is returned (this should never happen as the bundle
    * is in the same jar as this class).
    */
   private ResourceBundle getResources()
   {
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
      return ms_res;
   }
   
   /**
    * The list, initialized in the <code>initPanel()</code> method, never
    * <code>null</code> after that.
    */
   private JList m_list;
   
   /**
    * The button used to add items to the list, initialized in the 
    * <code>initPanel()</code> method, never <code>null</code> after that.
    */
   private JButton m_addButton;
   
   /**
    * The button used to remove items from the list, initialized in the 
    * <code>initPanel()</code> method, never <code>null</code> after that.
    */
   private JButton m_removeButton;
   
   /**
    * Resource bundle for this class.  Initialized by call to 
    * <code>getResources()</code>, only <code>null</code> after that if it
    * could not be loaded.
    */
   private static ResourceBundle ms_res = null;
   
   /**
    * List of listeners to be notified of add or remove actions.  Never 
    * <code>null</code>, may be empty.  Modified by calls to 
    * {@link #addActionListener(ActionListener)}. 
    */   
   private List m_actionListeners = new ArrayList();
}
   
