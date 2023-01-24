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
package com.percussion.workflow;

import com.percussion.tablefactory.PSJdbcTableFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.naming.NamingException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Implementation of the <CODE>IPSNotificationsContext</CODE> which
 * provides methods for accessing the subject and content of notifications
 * associated with a particular workflow and transition.
 * @deprecated
 */
@Deprecated
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
