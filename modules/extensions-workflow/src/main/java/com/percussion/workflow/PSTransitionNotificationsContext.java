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

import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * The PSTransitionNotificationsContext class is a wrapper class providing
 * access to  the records and fields of the backend table
 * 'TRANSITIONNOTIFICATIONS'.
 * @deprecated
 */
@Deprecated
public class PSTransitionNotificationsContext
   extends PSAbstractMultipleRecordWorkflowContext
   implements IPSTransitionNotificationsContext

{
   /**
    * Constructor specifying the workflowID and transition ID
    * for the collection of notifications.
    *
    * @param workflowID    ID of the workflow for these notifications    
    * @param transitionID  ID of the transition for these notifications    
    * @param connection    data base connection       
    * @throws              SQLException if an SQL error occurs
    * @throws IllegalArgumentException if the connection is <CODE>null</CODE>
    * @throws NamingException if a datasource cannot be resolved
    */
   public PSTransitionNotificationsContext(int workflowID,
                                           int transitionID,
                                           Connection connection)
      throws SQLException, NamingException
   {
      // Validate the input
      if(null == connection)
      {
         throw new IllegalArgumentException("Connection cannot be null");
      }

      // Assign the member variables
      m_Connection = connection;
      m_nWorkflowID = workflowID;
      m_nTransitionID = transitionID;

      // Get the data from the database
      getBackEndData();
   }


/* IMPLEMENTATION OF METHODS IN CLASS PSAbstractWorkflowContext  */
   
   protected void reinitializeDataMembers()
   {
      m_nNotificationID = 0;
      m_nStateRoleRecipientTypes = 0;
      m_sAdditionalRecipientList = null;
      m_sCCList = null;
   }

   protected String getQueryString()
   {
      return QRYSTRING;
   }

   protected  void setQueryParameters()
        throws SQLException
   {
      int nLoc = 0;
      m_Statement.setInt(++nLoc, m_nWorkflowID);
      m_Statement.setInt(++nLoc, m_nTransitionID);
   }

   /*
    * Gets the next JDBC result set, and moves the data into member variables;
    * strings are trimmed.
    */
   protected void resultSetMove() throws SQLException
   {
      //These must be in the same order as in the query
      m_nNotificationID = m_Rs.getInt("NOTIFICATIONID");
      m_nStateRoleRecipientTypes = m_Rs.getInt("STATEROLERECIPIENTTYPES");
      m_sAdditionalRecipientList = PSWorkFlowUtils.trimmedOrEmptyString(
         m_Rs.getString("ADDITIONALRECIPIENTLIST"));
      m_sCCList = PSWorkFlowUtils.trimmedOrEmptyString(
         m_Rs.getString("CCLIST"));
   }

/* IMPLEMENTATION OF PSAbstractMultipleRecordWorkflowContext METHODS */
   
   protected void AccumulateCurrentDataSet()
   {
      m_nNotificationIDList.add(new Integer(m_nNotificationID));
      m_nStateRoleRecipientTypesList.add(
         new Integer(m_nStateRoleRecipientTypes));
      m_sAdditionalRecipientListList.add(m_sAdditionalRecipientList);
      m_sCCListList.add(m_sCCList);
      
      if (!m_bRequireToStateRoles) 
      {
         m_bRequireToStateRoles = notifyToStateRoles();
      } 
      if (!m_bRequireFromStateRoles) 
      {
         m_bRequireFromStateRoles = notifyFromStateRoles();
      }
   }

   protected boolean MoveAccumulatedDataSet(int i) 
   {
      if(i >= m_nCount || i < 0)
      {
         return false;
      }
      m_nNotificationID = ((Integer)m_nNotificationIDList.get(i)).intValue();
      m_nStateRoleRecipientTypes = ((Integer)
            m_nStateRoleRecipientTypesList.get(i)).intValue();
      m_sAdditionalRecipientList = (String)
            m_sAdditionalRecipientListList.get(i);
      m_sCCList = (String) m_sCCListList.get(i);
      
      return true;
   }
   
/* IMPLEMENTATION OF IPSTransitionNotificationsContext INTERFACE */

   public int getWorkflowID() 
   {
      return m_nWorkflowID;
   }

   public int getTransitionID() 
   {
      return m_nTransitionID;
   }
   
   public int getNotificationID() 
   {
      return m_nNotificationID;
   }
   
   public int getStateRoleRecipientTypes()
   {
      return 1;
      
   }
   
   public boolean notifyFromStateRoles() 
   {
      return (IPSTransitionNotificationsContext.ONLY_OLD_STATE_ROLE_RECIPIENTS
             == m_nStateRoleRecipientTypes ||
             IPSTransitionNotificationsContext.OLD_AND_NEW_STATE_ROLE_RECIPIENTS
             == m_nStateRoleRecipientTypes);
   }

   public boolean notifyToStateRoles()
   {
      return (IPSTransitionNotificationsContext.ONLY_NEW_STATE_ROLE_RECIPIENTS
              == m_nStateRoleRecipientTypes ||
            IPSTransitionNotificationsContext.OLD_AND_NEW_STATE_ROLE_RECIPIENTS
              == m_nStateRoleRecipientTypes);
   }

   public boolean requireFromStateRoles()
   {
      return m_bRequireFromStateRoles;
   }

   public boolean requireToStateRoles()
   {
      return m_bRequireToStateRoles;
   }
   
   public String getAdditionalRecipientList() 
   {
      return m_sAdditionalRecipientList;
   }
   public String getCCList() 
   {
      return m_sCCList;
   }
   
   /******** Context Defining Members ********/
   
   /**
    * ID of the workflow for this item.
    */
   private int m_nWorkflowID = 0;

   /**
    * ID of the transition for this item.
    */   
   private int m_nTransitionID = 0;

   /******** Context Data Members ********/


   /** the notification ID */
   private int m_nNotificationID = 0;
   
   /**
    * value indicating which state role recipients should receive
    * notification: none, from-state, to-state or both 
    */
   private int m_nStateRoleRecipientTypes = 0;

   /**  comma-delimited list of additional notification recipients */
   private String m_sAdditionalRecipientList = null;
   
   /**  comma-delimited list of CC notification recipients */
   private String m_sCCList = null;

   
   /** List of notification IDs */
   private ArrayList m_nNotificationIDList = new ArrayList();
   
   /**
    * List of values indicating which state role recipients should receive
    * notification: none, from-state, to-state or both 
    */
   private ArrayList m_nStateRoleRecipientTypesList = new ArrayList();

   /**  List of comma-delimited lists of additional notification recipients */
   private ArrayList m_sAdditionalRecipientListList = new ArrayList();
   
   /**  List of comma-delimited lists of CC notification recipients */
   private ArrayList m_sCCListList = new ArrayList();

   /**
    * <CODE>true</CODE> if from-state role recipients should receive
    * at least one notification, else <CODE>false</CODE>
    */
   private boolean m_bRequireFromStateRoles = false;

   /**
    * <CODE>true</CODE> if to-state role recipients should receive
    * at least one notification, else <CODE>false</CODE>
    */
   private boolean m_bRequireToStateRoles  = false;
   
   /******** Database Related Variables ********/

   /**
    * static constant string that represents the qualified table name.
    */
   static private String TABLE_TNC =
      PSConnectionMgr.getQualifiedIdentifier("TRANSITIONNOTIFICATIONS");

   /**
    * SQL query string to get data base records for the notifications.
    */

     private static final String QRYSTRING =
     "SELECT " +
     TABLE_TNC + ".NOTIFICATIONID, " +
     TABLE_TNC + ".STATEROLERECIPIENTTYPES, " +
     TABLE_TNC + ".ADDITIONALRECIPIENTLIST, " +
     TABLE_TNC + ".CCLIST " +
     "FROM " +
     TABLE_TNC +
     " WHERE (" +
     TABLE_TNC + ".WORKFLOWAPPID=? " +
     "AND " +
     TABLE_TNC + ".TRANSITIONID=?)";
}
