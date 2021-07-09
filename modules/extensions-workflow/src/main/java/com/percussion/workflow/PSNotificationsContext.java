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

import com.percussion.tablefactory.PSJdbcTableFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.NamingException;

/**
 * Implementation of the <CODE>IPSNotificationsContext</CODE> which
 * provides methods for accessing the subject and content of notifications
 * associated with a particular workflow and transition.
 */

public class PSNotificationsContext extends PSAbstractWorkflowContext
implements IPSNotificationsContext
{

   private static final Logger log = LogManager.getLogger(PSNotificationsContext.class);

   /**
    * Constructor specifying the workflowID and notification ID.
    *
    * @param workflowID      ID of the workflow for this notification
    * @param notificationID  database notification ID
    * @param connection      data base connection
    * @throws                        SQLException if an error occurs
    * @throws NamingException if a datasource cannot be resolved
    * @throws IllegalArgumentException if the connection is <CODE>null</CODE>
    */
   public PSNotificationsContext(int workflowID,
                                 int notificationID,
                                 Connection connection)
      throws SQLException, PSEntryNotFoundException, NamingException
   {
      // Validate and set the defining variables

      if(null == connection)
      {
         throw new IllegalArgumentException("Connection cannot be null");
      }
      m_nWorkflowID = workflowID;
      m_nNotificationID = notificationID;
      m_Connection = connection;

      // Get the data from the database
      getBackEndData();

      // Throw error if no database record was found
      throwErrorIfEntryNotFound("No notification with notification ID = " +
         notificationID + " exists in the workflow " + workflowID + ".");
   }

/* IMPLEMENTATION OF METHODS IN CLASS PSAbstractWorkflowContext  */

   public void reinitializeDataMembers()
   {
      m_sSubject = null;
      m_sBody = null;
   }

   protected String getQueryString()
   {
      return QRYSTRING;
   }

   protected  void setQueryParameters()
      throws SQLException
   {
      m_Statement.setInt(1, m_nWorkflowID);
      m_Statement.setInt(2, m_nNotificationID);
   }

   protected void resultSetMove()
      throws SQLException
   {
      //These must be in the same order as in the query
      m_sSubject = PSWorkFlowUtils.
            trimmedOrNullString(m_Rs.getString("SUBJECT"));
      try
      {
         //We always assume that BODY field is of type CLOB
         m_sBody = PSJdbcTableFactory.getClobColumnData(m_Rs,"BODY");
      }
      catch(IOException ioe)
      {
         log.error(ioe.getMessage());
         log.debug(ioe.getMessage());
      }
   }

/* IMPLEMENTATION OF IPSNotificationsContext INTERFACE */

   public String getSubject()
   {
      return m_sSubject;
   }

   public String getBody()
   {
      return m_sBody;
   }


   /******** Context Defining Members ********/

   /**
    * ID of the workflow for this item.
    */
   private int m_nWorkflowID = 0;

   /** the notification ID */
   private int m_nNotificationID = 0;

   /******** Context Data Members ********/

   /**
    *  Subject of the mail notification
    */
   String m_sSubject = null;

   /**
    * Body of the mail notification
    */
   private String m_sBody = null;

   /******** Database Related Variables ********/

   /**
    * static constant string that represents the qualified table name.
    */
   static private String TABLE_NC =
      PSConnectionMgr.getQualifiedIdentifier("NOTIFICATIONS");

   /**
    * SQL query string to get data base records for the notifications.
    */
   private static final String QRYSTRING =
   "SELECT " +
   TABLE_NC + ".SUBJECT," +
   TABLE_NC + ".BODY " +
   "FROM " +
   TABLE_NC +
   " WHERE (" +
   TABLE_NC + ".WORKFLOWAPPID=? " +
   "AND " +
   TABLE_NC + ".NOTIFICATIONID=?)";
}
