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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class encapsulates the data used to notify each
 * {@link IPSTableChangeListener} during an update event.  This class also
 * handles notifying each listener as well.
 */
public class PSTableChangeData
{
   /**
    * Constructs this object, supplying the listeners to notify.
    *
    * @param listeners An iterator over <code>1</code> or more
    * {@link IPSTableChangeListener} objects, may not be <code>null</code> or
    * empty.
    *
    * @throws IllegalArgumentException if <code>listeners</code> is
    * <code>null</code> or empty.
    */
   public PSTableChangeData(Iterator listeners)
   {
      if(listeners == null || !listeners.hasNext())
         throw new IllegalArgumentException(
            "listeners may not be null or empty");
      m_listeners = new ArrayList();
      while(listeners.hasNext())
         m_listeners.add(listeners.next());
   }

   /**
    * Set the action type that should be used to create table change events to
    * notify the listeners. Must be called before {@link #expectsColumn} or {
    * @link #addColumnValue} methods are called to set column data.
    *
    * @param actionType One of the <code>PSTableChangeEvent.ACTION_xxx</code>
    * types.
    *
    * @throws IllegalArgumentException if actionType is not valid.
    */
   public void setActionType(int actionType)
   {
      if ( !PSTableChangeEvent.isValidAction( actionType ) )
      {
         throw new IllegalArgumentException("Invalid actionType");
      }
      m_actionType = actionType;
   }

   /**
    * Determine if a value for the specified column is expected by any of the
    * listeners.  May be called before <code>addColumnValue</code> to avoid
    * having to unnecessarily convert the value to a <code>String</code>.
    *
    * @param tableName The name of the table containing the column.  May not be
    * <code>null</code> or empty.
    * @param colName The name of the column, may not be <code>null</code> or
    * empty.
    *
    * @return <code>true</code> if one of the listeners is expecting a value for
    * this column, <code>false</code> otherwise.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws IllegalStateException if action type is not yet set.
    */
   public boolean expectsColumn(String tableName, String colName)
   {
      if(tableName == null || tableName.trim().length() == 0)
         throw new IllegalArgumentException(
            "tableName can not be null or empty.");

      if(colName == null || colName.trim().length() == 0)
         throw new IllegalArgumentException(
            "colName can not be null or empty.");

      if(m_actionType == PSTableChangeEvent.ACTION_UNDEFINED)
      {
         throw new IllegalStateException("Can not check for expected column " +
            "without specifying the action type");
      }

      Iterator listeners = m_listeners.iterator();
      while(listeners.hasNext())
      {
         IPSTableChangeListener listener =
            (IPSTableChangeListener) listeners.next();
         if(expectsColumn(listener, tableName, colName))
            return true;
      }
      return false;
   }
   
   /**
    * Adds the name of the table being processed.
    * 
    * @param tableName The name of the table being updated, may not be
    * <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if <code>tableName</code> is invalid.
    * @throws IllegalStateException if action type is not yet set.
    */
   public void addTable(String tableName)
   {
      if(tableName == null || tableName.trim().length() == 0)
         throw new IllegalArgumentException(
            "tableName can not be null or empty.");

      if(m_actionType == PSTableChangeEvent.ACTION_UNDEFINED)
         throw new IllegalStateException(
            "Can not add a table without specifying the action type");

      Map columnData = (Map) m_tableColumns.get(tableName);
      if(columnData == null)
      {
         columnData = new HashMap();
         m_tableColumns.put(tableName, columnData);
      }
   }

   /**
    * Adds the value of the specified column so that it will be supplied in the
    * PSTableChangeEvent when the listeners are notified.  The specified table
    * must already have been added by a call to {@link #addTable(String)}.
    *
    * @param tableName The name of the table containing the column.  May not be
    * <code>null</code> or empty.
    * @param colName The name of the column, may not be <code>null</code> or
    * empty.
    * @param value The value of the column, converted to a <code>String</code>.
    * May be <code>null</code> or emtpy.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws IllegalStateException if action type is not yet set or if the 
    * table has not been added.
    */
   public void addColumnValue(String tableName, String colName, String value)
   {
      if(tableName == null || tableName.trim().length() == 0)
         throw new IllegalArgumentException(
            "tableName can not be null or empty.");

      if(colName == null || colName.trim().length() == 0)
         throw new IllegalArgumentException(
            "colName can not be null or empty.");

      if(m_actionType == PSTableChangeEvent.ACTION_UNDEFINED)
         throw new IllegalStateException(
            "Can not add column data without specifying the action type");

      Map columnData = (Map) m_tableColumns.get(tableName);
      if(columnData == null)
         throw new IllegalStateException(
            "Cannot add column data without first adding the table");
         
      columnData.put(colName, value);
   }

   /**
    * Notifies all listeners supplied during construction using the collected
    * change events created for the entire update request.
    */
   public void notifyListeners()
   {      
      Iterator events = m_tableChangeEvents.iterator();
      while (events.hasNext())
      {
         PSTableChangeEvent event = (PSTableChangeEvent) events.next();

         Iterator listeners = m_listeners.iterator();
         while(listeners.hasNext())
         {
            IPSTableChangeListener listener = 
               (IPSTableChangeListener) listeners.next();

            if (isListenerInterested(listener, event.getTableName()))
               listener.tableChanged(event);
         }
      }
      
      m_tableChangeEvents.clear();
   }
   
   /**
    * Creates and collects the table change event for the currently executed
    * update statement if at least one row was changed.
    *  
    * @param rowCount the number of rows changed while executing the current
    *    update statement.
    */
   public void collectTableChangeEvent(int rowCount)
   {
      if (rowCount > 0)
      {
         Iterator tableColumns = m_tableColumns.entrySet().iterator();
         while (tableColumns.hasNext())
         {
            Map.Entry tableColumn = (Map.Entry) tableColumns.next();
            String tableName = (String) tableColumn.getKey();
            Map columnData = (Map) tableColumn.getValue();
            PSTableChangeEvent changeEvent = new PSTableChangeEvent(tableName, 
               m_actionType, columnData);
            
            m_tableChangeEvents.add(changeEvent);
         }
      }
   }
   
   /**
    * Clears all column data so that a new row of data may be processed.  Does
    * not affect the list of listeners that will be notified.
    */
   public void clearData()
   {
      m_tableColumns.clear();
      m_actionType = PSTableChangeEvent.ACTION_UNDEFINED;
   }
   
   /**
    * Determine if a value for the specified column is expected by the supplied
    * listener. Checks the cached map <code>m_listenerInterestedChanges</code>
    * before getting from the listener.
    *
    * @param listener The listener to check with, assumed not to be
    * <code>null</code>
    * @param tableName The name of the table containing the column, assumed not
    * to be <code>null</code> or empty.
    * @param colName The name of the column to check, assumed not be <code>null
    * </code> or empty.
    *
    * @return <code>true</code> if the listener is interested in, otherwise
    * <code>false</code>
    */
   private boolean expectsColumn(IPSTableChangeListener listener,
      String tableName, String colName)
   {
      List intColumns = getInterestedColumns(listener, tableName);

      //Now check whether the column is interested by listener or not
      if(intColumns != null && intColumns.contains(colName))
         return true;

      return false;
   }

   /**
    * Gets the list of interested columns of the supplied listener either from
    * cache or from listener for the specified table name and current action
    * type set with the instance.
    *
    * @param listener The listener to check with, assumed not to be
    * <code>null</code>
    * @param tableName The name of the table containing the columns, assumed not
    * to be <code>null</code> or empty.
    *
    * @return the list of columns, may be <code>null</code> if the listener is
    * not interested to be notified for the specified table and current action
    * type set with this instance. May be empty if it is interested in table
    * changed event for the current action, but not interested in any of the
    * columns.
    */
   private List getInterestedColumns(IPSTableChangeListener listener,
      String tableName)
   {
      List intColumns = null;

      //Check whether this listener has cache
      List intrstChanges = (List) m_listenerInterestedChanges.get(listener);
      if(intrstChanges == null) //if not get from listener and cache
      {
         intColumns = getColumnsFromListener(listener, tableName);
         ListenerInterestedChanges change =
            new ListenerInterestedChanges(m_actionType, tableName, intColumns);
         intrstChanges = new ArrayList();
         intrstChanges.add(change);
         m_listenerInterestedChanges.put(listener, intrstChanges);
      }
      else //check cache
      {
         Iterator changes = intrstChanges.iterator();

         boolean found = false;
         while(changes.hasNext())
         {
            ListenerInterestedChanges change =
               (ListenerInterestedChanges) changes.next();

            if(change.getActionType() == m_actionType &&
               change.getTableName().equals( tableName) )
            {
               found = true;
               intColumns = change.getColumns();
               break;
            }
         }
         //not cached for specified action type and tablename, so cache now
         if(!found)
         {
            intColumns = getColumnsFromListener(listener, tableName);
            ListenerInterestedChanges change =
               new ListenerInterestedChanges(
               m_actionType, tableName, intColumns);
            intrstChanges.add(change);
         }
      }

      return intColumns;
   }

   /**
    * Gets the list of interested columns of the supplied listener for the
    * specified table name and current action type.
    *
    * @param listener The listener to check with, assumed not to be
    * <code>null</code>
    * @param tableName The name of the table containing the columns, assumed not
    * to be <code>null</code> or empty.
    *
    * @return the list of columns, may be <code>null</code> if the listener is
    * not interested to be notified for the specified table and current action
    * type set with this instance. May be empty if it is interested in table
    * changed event for the current action, but not interested in any of the
    * columns.
    */
   private List getColumnsFromListener(IPSTableChangeListener listener,
      String tableName)
   {
      List intColumns = null;

      Iterator columns = listener.getColumns(tableName, m_actionType);
      if(columns != null)
      {
         intColumns = new ArrayList();
         while(columns.hasNext())
            intColumns.add(columns.next());
      }
      return intColumns;
   }

   /**
    * Checks whether the supplied listener is interested in the specified table
    * for the current action type set with the instance.
    *
    * @param listener listener to check, assumed not be <code>null</code>.
    * @param tableName name of the table changed, assumed not be <code>null
    * </code> or empty.
    *
    * @return <code>true</code> if it is interested in, otherwise <code>false
    * </code>
    */
   private boolean isListenerInterested(IPSTableChangeListener listener,
      String tableName)
   {
      List intColumns = getInterestedColumns(listener, tableName);
      return intColumns != null;
   }

   /**
    * The list of listeners (instances of {{@link IPSTableChangeListener}) that
    * are to be notified when a table change event occurs. The listeners are
    * notified only if there is a change in a table for a specific action that
    * is in interest of listeners. Initialized in ctor and never <code>null
    * </code> or modified after that.
    */
   private List m_listeners;

   /**
    * The map of listener interested column changes with {@link
    * IPSTableChangeListener} as key and list of {@link
    * ListenerInterestedChanges} with an entry for each table and action
    * the listener is interested in. Initially empty and gets filled up as and
    * when the <code>expectsColumn(String, String)</code> is called by querying
    * the listeners. Never <code>null</code>
    */
   private Map m_listenerInterestedChanges = new HashMap();

   /**
    * Represents the data modified by the update event, where the key is the
    * table name as a <code>String</code> and value is a <code>Map</code> of the 
    * column values of the table that are updated, with column name (<code>
    * String</code>) as key and the value is the object that was used to set the
    * column's value converted to a <code>String</code>. Initialized to an empty
    * map, never <code>null</code> after that.
    */
   private Map m_tableColumns = new HashMap();

   /**
    * The current action type that this instance is dealing with, set in
    * <code>setActionType(int)</code>. Initially set to 
    * <code>PSTableChangeEvent.ACTION_UNDEFINED</code>, and returned to this
    * value each time <code>clearData()</code> is called.
    */
   private int m_actionType = PSTableChangeEvent.ACTION_UNDEFINED;
   
   /**
    * A list of table change events collected for each update statement executed
    * which had at least one row changed. The list may be empty or contains
    * objects of type <code>PSTableChangeEvent</code>.
    */
   private List m_tableChangeEvents = new ArrayList();

   /**
    * Utility class to represent the interested column changes of a listener for
    * a table and action.
    */
   private class ListenerInterestedChanges
   {
      /**
       * Creates a new instance which represents the listener interested columns
       * for specifed table name and action.
       *
       * @param actionType One of the <code>PSTableChangeEvent.ACTION_xxx</code>
       * types.
       * @param tableName The name of the table, may not be <code>null</code>
       * or empty.
       * @param columns The list of column names listener interested in, may
       * be <code>null</code> or empty.
       * 
       * @throws IllegalArgumentException if any param is invalid.
       */
      public ListenerInterestedChanges(int actionType, String tableName,
         List columns)
      {
        if ( !PSTableChangeEvent.isValidAction( actionType ) )
         {
            throw new IllegalArgumentException("Invalid actionType");
         }

         if(tableName == null || tableName.trim().length() == 0)
         {
            throw new IllegalArgumentException(
               "tableName can not be null or empty.");
         }

         m_actType = actionType;
         m_tableName = tableName;
         m_columns = columns;
      }

      /**
       * Gets the action that the listener interested in.
       *
       * @return One of the <code>PSTableChangeEvent.ACTION_xxx</code> types
       */
      public int getActionType()
      {
         return m_actType;
      }

      /**
       * The name of the table that the listener interested in .
       *
       * @return The table name, never <code>null</code> or empty.
       */
      public String getTableName()
      {
         return m_tableName;
      }

      /**
       * Gets the list of column names that the listener interested in for the
       * specified action type and table name.
       *
       * @return the list of columns, may be <code>null</code> if the listener
       * is not interested in getting notified. May be empty if the listener is
       * interested in getting notified, but not interested in any of the 
       * columns.
       */
      public List getColumns()
      {
         return m_columns;
      }

      /**
       * The action type that the listener is interested in, must be one of the
       * <code>PSTableChangeEvent.ACTION_xxx</code> types. Set in ctor and never
       * modified after that.
       */
      private int m_actType;

      /**
       * The name of the table that the listener is interested in, initialized
       * in ctor and never <code>null</code> or modified after that.
       */
      private String m_tableName;

      /**
       * The list of column names that the listener is interested in for the
       * specifed table and action type, initialized in ctor and never modified
       * after that.
       */
      private List m_columns;
   }
}
