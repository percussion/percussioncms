/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.filetracker;

import java.util.EventObject;

import javax.swing.CellEditor;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;


/**
 * A base class for CellEditors, providing default implementations for all
 * methods in the CellEditor interface and support for managing a series
 * of listeners.
 *
 */
public class AbstractCellEditor implements CellEditor
{
   protected EventListenerList listenerList = new EventListenerList();
   public Object getCellEditorValue() { return null; }
   public boolean isCellEditable(EventObject e) { return true; }
   public boolean shouldSelectCell(EventObject anEvent) { return false; }
   public boolean stopCellEditing() { return true; }
   public void cancelCellEditing() {}

   public void addCellEditorListener(CellEditorListener l)
   {
      listenerList.add(CellEditorListener.class, l);
   }

   public void removeCellEditorListener(CellEditorListener l)
   {
      listenerList.remove(CellEditorListener.class, l);
   }

   /**
    * Notify all listeners that have registered interest for
    * notification on this event type.
    * @see EventListenerList
    */
   protected void fireEditingStopped()
   {
      // Guaranteed to return a non-null array
      Object[] listeners = listenerList.getListenerList();
      // Process the listeners last to first, notifying
      // those that are interested in this event
      for (int i = listeners.length-2; i>=0; i-=2)
      {
         if (listeners[i]==CellEditorListener.class)
         {
            ((CellEditorListener)listeners[i+1]).
                                 editingStopped(new ChangeEvent(this));
         }
      }
   }

   /**
    * Notify all listeners that have registered interest for
    * notification on this event type.
    * @see EventListenerList
    */
   protected void fireEditingCanceled()
   {
      // Guaranteed to return a non-null array
      Object[] listeners = listenerList.getListenerList();
      // Process the listeners last to first, notifying
      // those that are interested in this event
      for (int i = listeners.length-2; i>=0; i-=2)
      {
         if (listeners[i]==CellEditorListener.class)
         {
            ((CellEditorListener)listeners[i+1]).
                                 editingCanceled(new ChangeEvent(this));
         }
      }
   }
}
