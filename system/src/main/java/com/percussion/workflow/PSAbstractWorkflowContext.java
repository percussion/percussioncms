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
package com.percussion.workflow;

import com.percussion.util.PSPreparedStatement;

import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * This abstract class provides methods and members and a framework for
 * implementing workflow contexts; contexts that retrieve multiple data sets
 * should implement {@link PSAbstractMultipleRecordWorkflowContext}, which
 * extends this class. <BR>
 * Creating a read-only workflow context minimally requires implementing a
 * constructor,  the methods {@link #getQueryString},
 * {@link #setQueryParameters}, {@link #setQueryParameters} and any get methods
 * required by the corresponding workflow interface.<BR>
 * Some of the functionality that <CODE>PSAbstractWorkflowContext</CODE>
 * supports is not currently being used. The method  {@link #refreshData}
 * causes data to be read again from the database, and requires the method
 * {@link reinitializeDataMembers} . The method
 * {@link #getBackEndData(boolean)} makes it possible to create contexts that
 * manage their own database connections.
 */

/*
 * A constructor that allows a context to manage its own database connection
 * must set <CODE>m_bManageOwnConnection = true</CODE>. Such a context can also
 * have a constructor that uses a supplied connection.
 *
 * For example, for the PSTransitionNotificationsContext one would make a more
 * general constructor based on the current constructor, which would be called
 * by the other constructors. See sample code below:
 * //*
 *  * Constructor specifying the workflowID and transition ID
 *  * for the collection of notifications, and allowing the connection to
 *  * be supplied, or obtained and managed by the context
 *  *
 *  * @param workflowID          ID of the workflow for these notifications
 *  * @param transitionID        ID of the transition for these notifications
 *  * @param manageOwnConnection <CODE>true</CODE> if the context should obtain
 *  *                            and manage its own database connection,
 *  *                            else <CODE>false</CODE>.
 *  * @param connection          data base connection
 *  * @throws                    SQLException if an SQL error occurs
 *  * @throws IllegalArgumentException if the connection is <CODE>null</CODE>
 *  *         and manageOwnConnection is <CODE>false</CODE>

 * public PSTransitionNotificationsContext(int workflowID,
 *                                         int transitionID,
 *                                         boolean manageOwnConnection,
 *                                         Connection connection)
 *    throws SQLException
 * {
 *    m_bManageOwnConnection = manageOwnConnection;
 *    // Validate the input
 *    if(!m_bManageOwnConnection && null == connection)
 *    {
 *       throw new IllegalArgumentException("Connection cannot be null");
 *    }

 *    // Assign the member variables
 *    m_Connection = connection;
 *    m_nWorkflowID = workflowID;
 *    m_nTransitionID = transitionID;

 *    // Get the data from the database
 *    getBackEndData();
 * }
 * //*
 *  * Constructor specifying the workflowID and notification ID and database
 *  * connection.
 *  *
 *  * @param workflowID      ID of the workflow for this notification
 *  * @param notificationID  database notification ID
 *  * @param connection      data base connection
 *  * @throws                        SQLException if an error occurs
 *  * @throws IllegalArgumentException if the connection is <CODE>null</CODE>
 *  ///
 * public PSNotificationsContext(int workflowID,
 *                               int notificationID,
 *                               Connection connection)
 *    throws SQLException, PSEntryNotFoundException
 * {
 *    this(workflowID, notificationID, false, connection);
 * }
 *
 * //**
 *  * Constructor specifying the workflowID and notification ID.
 *  *
 *  * @param workflowID      ID of the workflow for this notification
 *  * @param notificationID  database notification ID
 *  * @throws                        SQLException if an error occurs
 * //
 * public PSTransitionNotificationsContext(int workflowID,
 *                                         int transitionID)
 *    throws SQLException
 * {
 *    this(workflowID, notificationID, true, null);
 * }
 */


public abstract class PSAbstractWorkflowContext
{
   /**
    * This is the executive method for moving data from the data base to
    * context member variables without reinitializing the
    * context data members. It delegates the work to
    * {@link #getBackEndData(boolean}
    *
    * @throws  SQLException if an SQL error occurs
    * @throws NamingException if a datasource cannot be found
    */
   protected void getBackEndData()
      throws SQLException, NamingException
   {
      getBackEndData(false);
   }

   /**
    * Manages moving of data from the data base to context member variables.
    * If required obtains and frees database connection, and reinitializes
    * context data members. It delegates the database work to
    * {@link #getDataFromDataBase}.
    *
    *
    * @param bReinitializeDataMembers   <CODE>true</CODE>  if data members
    *                                   should be reinitialized
    *                                   else <CODE>false</CODE>.
    * @throws                           SQLException if an SQL error occurs
    * @throws NamingException if a datasource cannot be resolved
    */
   protected void getBackEndData(boolean bReinitializeDataMembers)
      throws SQLException, NamingException
   {
      if (m_bManageOwnConnection)
      {
         m_Connection = PSConnectionMgr.getNewConnection();
      }
      try
      {
         if (bReinitializeDataMembers)
         {
            reinitializeDataMembers();
         }
         getDataFromDataBase();
      }
      finally
      {
         try
         {
            close();
            // Release connection if context is managing it
            if (m_bManageOwnConnection)
            {
               PSConnectionMgr.releaseConnection(m_Connection);
               m_Connection = null;
            }
         }
         catch(Exception e)
         {
            if (m_bManageOwnConnection)
            {
               m_Connection = null;
            }
         }
      }
   }

   /**
    * Gets data from the database: prepares and executes an SQL prepared
    * statement, and moves the data from the result set into the member
    * variables.<BR>
    * The context specific work is delegated to the methods the methods
    * {@link #getQueryString}, {@link #setQueryParameters}, and
    * {@link #setQueryParameters}.
    */
   public void getDataFromDataBase()
      throws SQLException
   {
      m_nCount = 0;
      m_sQueryString = getQueryString();
      m_Statement =
         PSPreparedStatement.getPreparedStatement(m_Connection, m_sQueryString);
      m_Statement.clearParameters();
      setQueryParameters();
      m_Rs = m_Statement.executeQuery();

      if(!m_Rs.next())
      {
         return;
      }

      m_nCount++;

      resultSetMove();

      if (m_Rs.next())
      {
         throw new SQLException("Multiple database entries found, when " +
                                "the context should uniquely specify entry");
      }
   }

   /**
    * Forces data to be read again from the database. The method  {@link
    * #reinitializeDataMembers} should be overridden if this method is used.
    *
    * @throws  SQLException if an error occurs
    * @throws NamingException if a datasource cannot be resolved
    */
   void refreshData()
      throws SQLException, NamingException
   {
      // Reinitialize data members and get data from the back end
      getBackEndData(true);
   }

   /**
    * Reinitializes the data members for this context; override this method to
    * allow the context to be refreshed from the database<BR>
    */
   protected void reinitializeDataMembers()
   {
      throw new UnsupportedOperationException(
         "This context does not support refreshing the context " +
         "from the database");

      // see PSNotificationsContext for a sample implementation
   }

  /**
   * Closes the result set and prepared statement if necessary
   */
   protected void close()
   {
      //release resources
      try
      {
         if(null != m_Rs)
         {
            m_Rs.close();
            m_Rs = null;
         }
         if(null != m_Statement)
         {
            m_Statement.close();
            m_Statement = null;
         }
      }
      catch(SQLException e)
      {
      }
   }

   /**
    * Throws a <CODE>PSEntryNotFoundException</CODE> if no database records
    * were  found. This method should be called by constructors of contexts
    * for which data should exist e.g. <CODE>PSNotificationsContext(int
    * workflowID, int notificationID, Connection connection)</CODE> but not
    * contexts for which it is legitimate for no corresponding data to exist,
    * e.g. <CODE>PSTransitionNotificationsContext(int workflowID, int
    * transitionID, Connection connection)</CODE>. The difference is that a
    * transition need not have any notifications associated with it, but that a
    * notification ID that is referenced by the TRANSITIONNOTIFICATIONS table
    * should exist.
    *
    * @param message  text for explanatory error message
    * @throws         PSEntryNotFoundException no database records were found
    */
   protected void throwErrorIfEntryNotFound(String message)
      throws PSEntryNotFoundException
   {
      if (0 == getCount())
      {
         throw new PSEntryNotFoundException(message);
      }
   }

   /**
    * Gets the number of database records found with valid data
    *
    * @return number of database records, if some methods return a
    * <CODE>List</CODE>, the size of the <CODE>List</CODE>.
    */
   public int getCount()
   {
      return m_nCount;
   }

   /* Utility methods */

   protected static void setDate(PreparedStatement stmt,
                                 int index,
                                 Date date)
      throws SQLException
   {
       Timestamp tmpTimeStamp = null;
       tmpTimeStamp =
                  PSWorkFlowUtils.timestampFromDate(date);
       stmt.setTimestamp(index, tmpTimeStamp);
   }

   /*
    * It would be useful to have get and set methods for string data that would
    * trim non-null values,  get and set methods for boolean data to convert
    * to and from "Y" or "N", and a get method to parse a delimited string into
    * a <CODE>List</CODE>
    */

   /* Abstract Methods */

   /**
    * Get the SQL query string used to create the JDBC prepared statement.
    * The simplest implementation would simply return a variable that is
    * a private static final String. If the context has multiple constructors
    * the correct query string might have to be selected or constructed.
    *
    * @return the SQL query string used to create the JDBC prepared statement
    */
    // see PSNotificationsContext for a sample implementation
   protected abstract String getQueryString();


   /**
    * Assign values to the query parameters using the context defining members
    * which were set in the constructor.
    *
    * @throws  SQLException if an SQL error occurs
    */
    // see PSNotificationsContext for a sample implementation
   protected abstract void setQueryParameters()
      throws SQLException;


   /**
    * Assign values from the result set to the context data members. This may
    * involve "massaging" the data, e.g. converting a "Y" or "N" to a
    * <CODE>boolean</CODE>,  or parsing a comma-delimited string into a
    * <CODE>List</CODE>.  The data base fields must be accessed in the  order
    * in which they appear in the SQL query.
    *
    * @throws  SQLException if an SQL error occurs
    */
    // see PSNotificationsContext for a sample implementation
   protected abstract void resultSetMove()
      throws SQLException;

   /*
    * To do It would be nice to have a way to get the context name for use in
    * error messages.
    */
   /******** Database Related Member Variables ********/

   /** Connection to the database */
   protected Connection m_Connection = null;

   /** SQL query string used to create the JDBC prepared statement. */
   protected String m_sQueryString = null;

   /** JDBC version of SQL statement to be executed. */
   protected PreparedStatement m_Statement = null;

   /** Result of database query */
   protected ResultSet m_Rs = null;

   /** Number of database records found */
   protected int m_nCount = 0;

   /**
    * <CODE>true</CODE> if the context should obtain and manage its own
    * database connection , else <CODE>false</CODE>.
    */
   protected boolean m_bManageOwnConnection = false;

}

