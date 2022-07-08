/*[ PSEditTablePanel.java ]******************************************************
 *
 * COPYRIGHT (c) 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.guitools;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * @author DougRand
 *
 * A JPanel with a PSEditTable along with buttons to add/remove rows from
 * the table. Accessors are provided to allow a caller to obtain the parts
 * of this panel in order to modify it or add additional components.
 */
public class PSEditTablePanel extends JPanel
{
   /**
    * Internal table instance is created during initialization and is
    * never updated afterward. The table can be accessed and modified
    * by external code for many valid reasons.
    */
   private PSJTable        m_table;
   
   /**
    * The scrollpane that contains the table. This is created during
    * initialization and is never updated. External callers should have
    * no reason to modify this.
    */
   private JScrollPane     m_scroller;
   
   /**
    * Private button provided in the UI to allow the user to add elements
    * to the model. Created during initialization and never updated.
    */
   private JButton         m_addButton;
   
   /**
    * Private button provided in the UI to allow the user to remove elements
    * to the model. Created during initialization and never updated.
    */
   private JButton         m_removeButton;
   
   /**
    * newRowColumn this is the initial focus column to use 
    * when the user has added an item. This must be a positive 
    * integer greater than or equal to 0 and less than the 
    * number of columns in the model. This is set at construction
    * time and never updated.
    */
   private int             m_newRowColumn;

   ResourceBundleHelper m_res = new ResourceBundleHelper(getClass().getName());

   /**
    * Action to add a row to the table
    */
   protected class PanelAddActionListener implements ActionListener
   {
      /* (non-Javadoc)
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      public void actionPerformed(ActionEvent arg0)
      {
         try
         {
            addRow();
         }
         catch (Exception e)
         {
            throw new RuntimeException(e.getMessage());
         }
      }
   }

   /**
    * Action to remove a row from the table
    */
   protected class PanelRemoveActionListener implements ActionListener
   {
      /* (non-Javadoc)
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      public void actionPerformed(ActionEvent arg0)
      {
         removeRow();
      }
   }

   /**
    * Handle specific extra accelerators on the table
    */
   protected class PanelKeyListener implements KeyListener
   {
      ActionListener mi_add, mi_remove;

      public PanelKeyListener(ActionListener add, ActionListener remove)
      {
         mi_add = add;
         mi_remove = remove;
      }

      /* (non-Javadoc)
       * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
       */
      public void keyTyped(KeyEvent event)
      {
         // Ignore   
      }

      /* (non-Javadoc)
       * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
       */
      public void keyPressed(KeyEvent arg0)
      {
         // Ignore
      }

      /* (non-Javadoc)
       * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
       */
      public void keyReleased(KeyEvent event)
      {
         switch (event.getKeyCode())
         {
            case KeyEvent.VK_ESCAPE :
               m_table.removeEditor(); // Pop down the editor
               break;
            case KeyEvent.VK_INSERT :
               ActionEvent e1 =
                  new ActionEvent(event.getSource(), event.getID(), "internal");
               mi_add.actionPerformed(e1);
               break;
            case KeyEvent.VK_DELETE :
               ActionEvent e2 =
                  new ActionEvent(event.getSource(), event.getID(), "internal");
               mi_remove.actionPerformed(e2);
               break;
            default :
               // Do nothing
         }

      }
   }

   /**
    * Create a new table panel. A panel initially consists of a table that 
    * contains the passed model, and add and remove buttons placed in 
    * the <q>NORTH</q> position in the border layout. 
    * 
    * @param model this is the model to display in the table. This 
    * model will always be a subclass of PSEditTableModel. This 
    * parameter may not be <code>null</code>. The model must support
    * the interface <code>com.percussion.guitools.IPSCreateModelItem</code>
    * 
    * @param newRowColumn this is the initial focus column to use 
    * when the user has added an item. This must be a positive 
    * integer greater than or equal to 0 and less than the 
    * number of columns in the model
    */
   public PSEditTablePanel(
      TableModel model,
      int newRowColumn)
   {
      if (newRowColumn >= model.getColumnCount())
      {
         throw 
            new IllegalArgumentException("The new row column must be less than the column count of the model");
      }

      if (newRowColumn < 0)
      {
         throw new IllegalArgumentException("The new row column must not be negative");
      }

      if (model == null)
      {
         throw new IllegalArgumentException("The model must be specified");
      }

      if (! (model instanceof IPSCreateModelItem))
      {
         throw new IllegalArgumentException("The model must support IPSCreateModelItem");
      }

      init(model);

      m_newRowColumn = newRowColumn;
   }

   /**
    * Create UI components for this custom panel
    * 
    * @param table_model is the initial model for the created table
    */
   protected void init(TableModel table_model)
   {
      LayoutManager mgr = new BorderLayout();
      setLayout(mgr);

      m_table = new PSJTable(table_model);
      m_scroller = new JScrollPane(m_table);
      add(m_scroller, BorderLayout.CENTER);

      // Add buttons to add/remove rows
      Dimension dim = new Dimension(20, 20);
      m_addButton =
         new JButton(
            new ImageIcon(
               getClass().getResource(m_res.getResource("button.add"))));
      m_addButton.setPreferredSize(dim);
      m_addButton.setMinimumSize(dim);
      m_removeButton =
         new JButton(
            new ImageIcon(
               getClass().getResource(m_res.getResource("button.remove"))));
      m_removeButton.setPreferredSize(dim);
      m_removeButton.setMinimumSize(dim);

      JPanel buttonPane = new JPanel();
      buttonPane.setLayout(new FlowLayout());
      buttonPane.add(m_addButton);
      buttonPane.add(m_removeButton);
      add(buttonPane, BorderLayout.NORTH);

      ActionListener addListener = new PanelAddActionListener();
      ActionListener removeListener = new PanelRemoveActionListener();

      // Add listeners to buttons
      m_addButton.addActionListener(addListener);
      m_removeButton.addActionListener(removeListener);

      // Create a new Key listener for the table
      KeyListener keyListener =
         new PanelKeyListener(addListener, removeListener);
      m_table.addKeyListener(keyListener);

      m_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      ListSelectionModel model = m_table.getSelectionModel();
      model.addListSelectionListener(new ListSelectionListener()
      {
         public void valueChanged(ListSelectionEvent event)
         {
            if (m_table.getSelectedRow() >= 0)
            {
               m_removeButton.setEnabled(true);
            }
            else
            {
               m_removeButton.setEnabled(false);
            }
         }
      });
   }

   /**
    * Add a row to the model by instantiating the class specified
    * in the constructor. Once the row is created and added to the model,
    * which will cause the table&apos;s listeners to be notified and
    * the view to be updated. Last, start up the editor on the 
    * new row in the new row column specified in the constructor.
    */
   public void addRow() throws InstantiationException, IllegalAccessException
   {
      IPSCreateModelItem createModel =
         (IPSCreateModelItem) m_table.getModel();
         
      m_table.addRow(createModel.createInstance());
      m_table.editCellAt(m_table.getRowCount() - 1, m_newRowColumn);
      m_table.getEditorComponent().requestFocus();
      m_table.setSelectedRow(m_table.getRowCount() - 1);
   }

   /**
    * Remove a row from the model. This method makes sure to pop down any
    * existing editor as well as removing the row. Removing the row from
    * the table model will cause the listeners to be notified and the
    * table view to be updated.
    */
   public void removeRow()
   {
      int cursel = m_table.getSelectedRow();
      
      m_table.removeEditor();
      m_table.removeSelectedRow();
      
      cursel--; // Move to earlier row
      if (cursel < 0)
      {
         cursel = 0;
      }
      
      if (m_table.getRowCount() > 0)
      {
         m_table.setSelectedRow(cursel);
      }
   }

   /**
    * Move the currently selected row down one row (unless at the bottom).
    * This manipulates the model, which will cause the view to be updated.
    */
   public void moveSelectedDown()
   {
      int i = m_table.getSelectionModel().getMinSelectionIndex();
      int items = m_table.getRowCount();
      if (i < 0 || i >= items - 1)
         return;

      PSEditTableModel model = (PSEditTableModel) m_table.getModel();
      model.moveRow(i, i + 1);
      // Move selection with row
      m_table.setSelectedRow(i + 1);
   }

   /**
    * Move the currently selected row up one row (unless at the top).
    * This manipulates the model, which will cause the view to be updated.
    */
   public void moveSelectedUp()
   {
      int i = m_table.getSelectionModel().getMinSelectionIndex();
      if (i <= 0)
         return;

      PSEditTableModel model = (PSEditTableModel) m_table.getModel();
      model.moveRow(i, i - 1);
      // Move selection with row
      m_table.setSelectedRow(i - 1);
   }

   /**
    * Get the table implementation to allow a caller to manipulate it.
    * @return the table, which is guaranteed never to be <code>null</code>
    */
   public PSJTable getTable()
   {
      return m_table;
   }

   /**
    * Return the add button component. This allows a user of this class to 
    * manipulate the button.
    * @return the button
    */
   public JButton getAddButton()
   {
      return m_addButton;
   }

   /**
    * Return the remove button component. This allows a user of this class 
    * to manipulate the button.
    * @return the button
    */
   public JButton getRemoveButton()
   {
      return m_removeButton;
   }

}
