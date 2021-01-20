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

package com.percussion.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class PSTableChangeDataTest extends TestCase
{
   /**
    * Constructs an instance of this class to run the test implemented by the
    * named method.
    *
    * @param methodName name of the method that implements a test
    */
   public PSTableChangeDataTest(String name)
   {
      super( name );
   }

   /**
    * Collects all the tests implemented by this class into a single suite.
    */
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest( new PSTableChangeDataTest( "testListenerComm" ) );
      return suite;
   }

   /**
    * Tests the communication of table change data object with the listeners set
    * with that object.
    * <br>
    * Tests the following cases with 2 listeners interested in different tables
    * and actions.
    * <ol>
    * <li>The expectsColumn and addColumnValue methods should fire
    * IllegalStateException before actioType is set on data object.</li>
    * <li>Test the expectsColumn method to return <code>true</code> only if the
    * column of the table is in interest of any of the listeners for that
    * particular action type.</li>
    * <li>Test for insert event by adding columns of the tables that are
    * interested by two different listeners and make sure that the corresponding
    * event is fired to the listener.</li>
    * <li>Test for update event and make sure that both listeners are notified
    * as they are interested in the update event of the columns of particular
    * table.</li>
    * <li>Test for delete event and make sure that only listener1 is notified
    * as listener2 is not interested in the delete event of the particular
    * table.</li>
    * </ol>
    * @throws Exception
    */
   public void testListenerComm() throws Exception
   {
      List interestedChanges1 = new ArrayList();

      List list = new ArrayList();
      list.add("CONTENTSTATEID");
      list.add("CONTENTTYPEID");
      list.add("CONTENTCHECKOUTUSERNAME");
      interestedChanges1.add( new TableChanges(
         PSTableChangeEvent.ACTION_INSERT, "CONTENTSTATUS", list) );

      List list1 = new ArrayList();
      list1.add("STATEID");
      list1.add("CONTENTID");
      interestedChanges1.add( new TableChanges(
         PSTableChangeEvent.ACTION_UPDATE, "CONTENTAPPROVALS", list1) );

      List list2 = new ArrayList();
      list2.add("USERNAME");
      interestedChanges1.add( new TableChanges(
         PSTableChangeEvent.ACTION_INSERT, "CONTENTADHOCUSERS", list2));
      interestedChanges1.add( new TableChanges(
         PSTableChangeEvent.ACTION_DELETE, "CONTENTADHOCUSERS", new ArrayList())
         );

      TableChangeListener listener1 = new TableChangeListener(
         interestedChanges1.iterator());

      List interestedChanges2 = new ArrayList();
      List list3 = new ArrayList();
      list3.add("DISPLAYTITLE");
      list3.add("AUTHORNAME");
      interestedChanges2.add( new TableChanges(
         PSTableChangeEvent.ACTION_INSERT, "RXARTICLE", list3) );
      interestedChanges2.add( new TableChanges(
         PSTableChangeEvent.ACTION_UPDATE, "CONTENTAPPROVALS", list1) );

      TableChangeListener listener2 = new TableChangeListener(
         interestedChanges2.iterator());

      List listeners = new ArrayList();
      listeners.add(listener1);
      listeners.add(listener2);

      PSTableChangeData data = new PSTableChangeData(listeners.iterator());
      
      final String tableNameCONTENTSTATUS = "CONTENTSTATUS";
      final String tableNameCONTENTADHOCUSERS = "CONTENTADHOCUSERS";
      final String tableNameCONTENTAPPROVALS = "CONTENTAPPROVALS";
      final String tableNameRXARTICLE = "RXARTICLE";
      
      /* Tests the expectsColumn and addColumnValue methods to fire
         IllegalStateException before actioType is set.
       */
      boolean didThrow = false;
      try
      {
         data.expectsColumn(tableNameCONTENTSTATUS, "CONTENTSTATEID");
      }
      catch(IllegalStateException e)
      {
         didThrow = true;
      }
      assertTrue( didThrow );

      didThrow = false;
      try
      {
         data.addColumnValue(tableNameCONTENTSTATUS, "CONTENTSTATEID", "2");
      }
      catch(IllegalStateException e)
      {
         didThrow = true;
      }
      assertTrue( didThrow );

      /* Test for insert action by adding columns of the tables that are
         interested by two different listeners and make sure that the
         corresponding event is fired to the listener.
       */
      data.setActionType(PSTableChangeEvent.ACTION_INSERT);
      
      data.addTable(tableNameCONTENTSTATUS);
      data.addTable(tableNameCONTENTADHOCUSERS);
      data.addTable(tableNameRXARTICLE);
      
      //listener1 interested
      assertTrue(data.expectsColumn(tableNameCONTENTSTATUS, "CONTENTSTATEID"));
      data.addColumnValue(tableNameCONTENTSTATUS, "CONTENTSTATEID", "2");
      assertTrue(data.expectsColumn(tableNameCONTENTADHOCUSERS, "USERNAME"));
      data.addColumnValue(tableNameCONTENTADHOCUSERS, "USERNAME", "testuser");

      //listener2 interested
      assertTrue(data.expectsColumn(tableNameRXARTICLE, "AUTHORNAME"));
      assertTrue(data.expectsColumn(tableNameRXARTICLE, "DISPLAYTITLE"));
      data.addColumnValue(tableNameRXARTICLE, "AUTHORNAME", "Syamala");
      data.addColumnValue(tableNameRXARTICLE, "DISPLAYTITLE", "testtitle");

      //no listeners interested
      assertTrue(!data.expectsColumn(tableNameCONTENTAPPROVALS, "STATEID"));

      data.collectTableChangeEvent(1);
      data.notifyListeners();
      assertTrue(listener1.getChangedTables().contains(tableNameCONTENTSTATUS));
      assertTrue(listener1.getChangedTables().contains(tableNameCONTENTADHOCUSERS));
      assertTrue(listener2.getChangedTables().contains(tableNameRXARTICLE));

      //Test that both listeners are notified as they are interested in update
      //action of 'CONTENTAPPROVALS' table.
      listener1.clearChangedTables();
      listener2.clearChangedTables();
      data.clearData();
      
      data.setActionType(PSTableChangeEvent.ACTION_UPDATE);
      data.addTable(tableNameCONTENTAPPROVALS);
      
      assertTrue(data.expectsColumn(tableNameCONTENTAPPROVALS, "STATEID"));
      assertTrue(data.expectsColumn(tableNameCONTENTAPPROVALS, "CONTENTID"));
      data.addColumnValue(tableNameCONTENTAPPROVALS, "STATEID", "3");
      data.addColumnValue(tableNameCONTENTAPPROVALS, "CONTENTID", "4");
      data.collectTableChangeEvent(1);
      data.notifyListeners();
      assertTrue(listener1.getChangedTables().contains(tableNameCONTENTAPPROVALS));
      assertTrue(listener2.getChangedTables().contains(tableNameCONTENTAPPROVALS));

      //Test that only listener1 is notified as this only interested in delete
      //action of 'CONTENTADHOCUSERS' table.
      listener1.clearChangedTables();
      listener2.clearChangedTables();
      data.clearData();
      
      data.setActionType(PSTableChangeEvent.ACTION_UPDATE);
      data.addTable(tableNameCONTENTADHOCUSERS);
      
      data.setActionType(PSTableChangeEvent.ACTION_DELETE);
      assertTrue(!data.expectsColumn(tableNameCONTENTADHOCUSERS, "STATEID"));
      data.addColumnValue(tableNameCONTENTADHOCUSERS, "STATEID", "3");
      data.collectTableChangeEvent(1);
      data.notifyListeners();
      assertTrue(listener1.getChangedTables().contains(tableNameCONTENTADHOCUSERS));
      assertTrue(!listener2.getChangedTables().contains(tableNameCONTENTADHOCUSERS));
   }

   /**
    * The table change listener class used to test <code>PSTableChangeData
    * </code> class.
    */
   private class TableChangeListener implements IPSTableChangeListener
   {
      /**
       * Creates an instance of the listener with list of interested <code>
       * TableChanges</code>.
       *
       * @param interestedChanges list of interested changes, may not be <code>
       * null</code> or empty.
       *
       * @throws IllegalArgumentException if interestedChanges is <code>null
       * </code> or empty.
       */
      public TableChangeListener(Iterator interestedChanges)
      {
         if(interestedChanges == null || !interestedChanges.hasNext())
            throw new IllegalArgumentException(
               "interestedChanges may not be null or empty");
         m_interestedChanges = new ArrayList();
         while(interestedChanges.hasNext())
            m_interestedChanges.add(interestedChanges.next());
      }

      //See interface for description.
      public Iterator getColumns(String tableName, int actionType)
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
         List columns = null;
         Iterator interestedChanges = m_interestedChanges.iterator();
         while(interestedChanges.hasNext())
         {
            TableChanges change =
               (TableChanges)interestedChanges.next();
            if(change.getActionType() == actionType &&
               change.getTableName().equals( tableName) )
            {
               columns = change.getColumns();
               break;
            }
         }
         if(columns != null)
            return columns.iterator();
         else
            return null;
      }

      /**
       * Make sures the correct event is fired and adds the table that is
       * changed to its own list. This is useful for further checking by test
       * class to make sure only the table changed events that are in interest
       * of this listener got fired.
       *
       * @param e the table changed event, may not be <code>null</code>
       *
       * @throws IllegalArgumentException if e is <code>null</code>
       */
      public void tableChanged(PSTableChangeEvent e)
      {
         if(e == null)
            throw new IllegalArgumentException("e may not be null");

         String tableName = e.getTableName();
         int actionType = e.getActionType();
         assertTrue(isInterested(tableName, actionType));
         m_changedTables.add(tableName);
      }

      /**
       * Determines whether this listener is interested in supplied table and
       * action.
       *
       * @param tableName the table name to check, assumed not to be <code>
       * null</code> or empty.
       * @param actionType assumed to be one of the <code>
       * PSTableChangeEvent.ACTION_xxx</code> types.
       *
       * @return <code>true</code> if this listener is interested, otherwise
       * <code>false</code>
       */
      private boolean isInterested(String tableName, int actionType)
      {
         List columns = null;
         Iterator interestedChanges = m_interestedChanges.iterator();
         while(interestedChanges.hasNext())
         {
            TableChanges change =
               (TableChanges)interestedChanges.next();
            if(change.getActionType() == actionType &&
               change.getTableName().equals( tableName) )
            {
               columns = change.getColumns();
               break;
            }
         }

         return columns != null;
      }

      /**
       * Clears the list of changed tables.
       */
      public void clearChangedTables()
      {
         m_changedTables.clear();
      }

      /**
       * @return the list of changed tables, never <code>null</code> may be
       * empty.
       */
      private List getChangedTables()
      {
         return m_changedTables;
      }

      /**
       * The list of interested <code>TableChanges</code>, initialized in ctor
       * and never <code>null</code> or modified after that.
       */
      private List m_interestedChanges;

      /**
       * The list of changed tables that were collected when the <code>
       * tableChanged</code> events are fired. Initialized to an empty list and
       * never <code>null</code> after that.
       */
      private List m_changedTables = new ArrayList();
   }

   /**
    * Utility class to represent the interested column changes of a listener for
    * a table and action.
    */
   private class TableChanges
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
       * not be <code>null</code>, but can be empty.
       */
      public TableChanges(int actionType, String tableName,
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

         if(columns == null)
            throw new IllegalArgumentException("columns can not be null");

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
       * @return the list of columns, never <code>null</code>, may be empty.
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
