/*
 *     Percussion CMS
 *     Copyright (C) 1999-2022 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.guitools;

import com.percussion.util.PSCollection;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author DougRand
 *
 * This abstract table model class forms the base for all models that
 * use a PSCollection at their heart. It implements the basic functionality, but
 * you must subclass it to implement the column oriented methods that will extract
 * and update information from an object's fields.
 */
public abstract class PSEditTableModel implements TableModel
{
   /**
    * The maintained list of listeners for table modification events. These
    * listeners are all of the specific TableModelListener class. Each is called
    * in turn in notifyListeners using the given event.
    */
   private Collection m_tableModelListeners = new ArrayList();

   /**
    * The base model storage. The PSCollection may be of any concrete type since
    * this class is itself only an abstract class that may not be instantiated.
    */
   protected PSCollection m_rows = null;

   /**
    * Create the model from the passed collection. 
    * 
    * @param rows The initial model data, which must not be <code>null</code>
    * @throws IllegalArgumentException
    */
   protected PSEditTableModel(PSCollection rows)
      throws IllegalArgumentException
   {
      if (rows != null)
      {
         m_rows = rows;
      }
      else
      {
         throw new IllegalArgumentException("Passed model may not be null");
      }
   }

   /* (non-Javadoc)
    * @see javax.swing.table.TableModel#addTableModelListener(javax.swing.event.TableModelListener)
    */
   public void addTableModelListener(TableModelListener arg0)
   {
      m_tableModelListeners.add(arg0);
   }

   /* (non-Javadoc)
    * @see javax.swing.table.TableModel#getRowCount()
    */
   public int getRowCount()
   {
      return m_rows.size();
   }

   /* (non-Javadoc)
    * @see javax.swing.table.TableModel#removeTableModelListener(javax.swing.event.TableModelListener)
    */
   public void removeTableModelListener(TableModelListener arg0)
   {
      m_tableModelListeners.remove(arg0);
   }

   /* (non-Javadoc)
    * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
    */
   public abstract void setValueAt(Object arg0, int row, int column);

   /**
    * Move a row from the initial index to a new index and notify all listeners
    * 
    * @param initialIndex the valid range is greater than or equal to 
    * zero and less than the number of rows in the model
    * @param newIndex the valid range is greater than or equal to 
    * zero and less than the number of rows in the model
    */
   public void moveRow(int initialIndex, int newIndex)
   {
      checkRange(initialIndex);
      checkRange(newIndex);

      Object element = m_rows.get(initialIndex);
      m_rows.remove(initialIndex);
      m_rows.add(newIndex, element);
      int first, last;

      if (initialIndex < newIndex)
      {
         first = initialIndex;
         last = newIndex;
      }
      else
      {
         first = newIndex;
         last = initialIndex;
      }

      TableModelEvent event = new TableModelEvent(this, first, last);
      notifyListeners(event);
   }

   /**
    * This method checks the value passed against the actual bounds of the model
    * @param index the valid range is greater than or equal to 
    * zero and less than the number of rows in the model
    */
   private void checkRange(int index)
   {
      if (index < 0)
      {
         throw new IllegalArgumentException("Index on model may not be negative");
      }

      if (index >= m_rows.size())
      {
         throw new IllegalArgumentException(
            "Index on model may not exceed size: " + m_rows.size());
      }

   }

   /**
    * Remove a row from the model and notify all listeners
    * 
    * @param index the valid range is greater than or equal to 
    * zero and less than the number of rows in the model
    */
   public void removeRow(int index)
   {
      checkRange(index);

      TableModelEvent event =
         new TableModelEvent(
            this,
            index,
            m_rows.size(),
            TableModelEvent.ALL_COLUMNS,
            TableModelEvent.DELETE);
      m_rows.remove(index);
      notifyListeners(event);
   }

   /**
    * Add a row to the model and notify all listeners.
    * 
    * @param newRow must be of an appropriate type for the PSCollection 
    * or an exception will be thrown
    */
   public void appendRow(Object newRow)
   {
      m_rows.add(newRow);
      TableModelEvent event =
         new TableModelEvent(
            this,
            m_rows.size(),
            m_rows.size(),
            TableModelEvent.ALL_COLUMNS,
            TableModelEvent.INSERT);
      notifyListeners(event);
   }

   /**
    * Notify all listeners of the change to the table
    * @param event that indicates the kind of change
    */
   protected void notifyListeners(TableModelEvent event)
   {
      for (Iterator iter = m_tableModelListeners.iterator(); iter.hasNext();)
      {
         TableModelListener element = (TableModelListener) iter.next();
         element.tableChanged(event);
      }
   }

   /**
    * @return get the current PSCollection contained in the model
    */
   public PSCollection getRows()
   {
      return m_rows;
   }

}
