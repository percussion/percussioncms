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

package com.percussion.filetracker;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import java.util.EventObject;


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
