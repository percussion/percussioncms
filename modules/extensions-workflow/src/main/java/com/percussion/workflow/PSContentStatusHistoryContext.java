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
package com.percussion.workflow;
import com.percussion.cms.IPSConstants;
import com.percussion.extension.IPSExtensionErrors;
import com.percussion.util.PSPreparedStatement;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * A class that provides methods for creating new content status history
 * records and accessing fields in existing ones, and that is a wrapper for the
 * backend table 'CONTENTSTATUSHISTORY'.
 * @author Rammohan Vangapalli
 * @version 1.0
 * @since 2.0
 *
 */
public class PSContentStatusHistoryContext
   implements IPSContentStatusHistoryContext
{

   /**
    * Constructor specifying the workFlowID, connection, and contentID, used
    * for read-only access to content status history records.
    *
    * @param workFlowID   ID of the workflow for this item
    * @param connection   data base connection
    * @param contentID    ID of the content item
    * @throws             SQLException if a SQL error occurs
    * @throws             PSEntryNotFoundException if no records were returned
    */
   public PSContentStatusHistoryContext(int workFlowID,
                                        Connection connection,
                                        int contentID)
      throws SQLException, PSEntryNotFoundException
   {
      m_nWorkflowID = workFlowID;
      m_nContentID = contentID;
      m_Connection = connection;

      try
      {
         m_Statement =
            PSPreparedStatement.getPreparedStatement(m_Connection, QRYSTRING);
         m_Statement.clearParameters();
         m_Statement.setInt(1, workFlowID);
         m_Statement.setInt(2, contentID);
         m_Rs = m_Statement.executeQuery();

         while(m_Rs.next())
         {
            m_nCount++;
         }
         if(0==m_nCount)
         {
            close();
            throw new PSEntryNotFoundException(IPSExtensionErrors.NO_RECORDS);
         }

         try
         {
            m_Rs.close();
            m_Statement.close();
         }
         catch(Exception e)
         {
         }

         //redo the whole thing!!!
         m_Statement =
            PSPreparedStatement.getPreparedStatement(m_Connection, QRYSTRING);
         m_Statement.clearParameters();
         m_Statement.setInt(1, workFlowID);
         m_Statement.setInt(2, contentID);
         m_Rs = m_Statement.executeQuery();
         moveNext();
      }
      finally
      {
         try
         {
            close();
         }
         catch(Exception e)
         {
         }
      }
   }


   /**
    * Constructor for a new ContentStatusHistory entry specifying all required
    * data; commits the record to the data base and frees all JDBC objects.
    *
    * @param contentStatusHistoryID  ID for new ContentStatusHistory entry.
    * @param workFlowID        ID of the workflow for this item
    * @param connection        data base connection
    * @param contentID            ID of the content item
    * @param contentStatusContext PSContentStatusContext for the content item
    * @param statesContext        the PSStatesContext for the content state
    * @param transitionContext    the PSTransitionsContext for the transition
    * @param transitionComment    descriptive comment for this action
    * @param actorName            the name of the user that performed this
    *                             transition, check in or check out.
    * @param sessionID            the SessionID for this content status
    *              history entry.
    * @param roleName             list of names of assignee roles for the
    *                             state of the content item
    * @param baseRevisionNum      the base revision of the content item for
    *                             checkouts, otherwise the current revision.
    * @throws                  SQLException if a SQL error occurs
    * @throws                  PSEntryNotFoundException if no records were
    *                             returned for an existing entry.
    * @throws                  IllegalArgumentException if any of the input
    *                             parameters is not valid.
    */

   public PSContentStatusHistoryContext(
      int contentStatusHistoryID,
      int workFlowID,
      Connection connection,
      int contentID,
      PSContentStatusContext  contentStatusContext,
      IPSStatesContext statesContext,
      PSTransitionsContext transitionContext,
      String transitionComment,
      String actorName,
      String sessionID,
      String roleName,
      int baseRevisionNum)
      throws SQLException, PSEntryNotFoundException
   {
      // Initialize the data
      m_Connection = connection;

      if ( null ==  contentStatusContext)
      {
         throw new IllegalArgumentException("contentStatusContext is null in "
                                          + "PSContentStatusHistoryContext.");
      }

      if ( null ==  statesContext)
      {
         throw new IllegalArgumentException("statesContext is null in "
                                          + "PSContentStatusHistoryContext.");
      }
      m_EventTime = new Date(new java.util.Date().getTime());
      setContentStatusHistoryID(contentStatusHistoryID);
      setWorkFlowID(workFlowID);
      setContentID(contentID);
      setContentIsValid(statesContext.getIsValid());
      setStateName(statesContext.getStateName());
      setTitle(contentStatusContext.getTitle());
      setContentStateID(contentStatusContext.getContentStateID());
      setContentCheckoutUserName(
         contentStatusContext.getContentCheckedOutUserName());
      setContentLastModifierName(
         contentStatusContext.getContentLastModifierName());
      setContentLastModifiedDate(
         contentStatusContext.getContentLastModifiedDate());

      if (null == transitionContext)
      {
         setTransitionID(IPSConstants.TRANSITIONID_CHECKINOUT);
         setTransitionLabel(null);
      }
      else
      {
         setTransitionID(transitionContext.getTransitionID());
         setTransitionLabel(transitionContext.getTransitionLabel());
      }

      setTransitionComment(transitionComment);
      setActorName(actorName);
      setSessionID(sessionID);
      setContentStateRoleName(roleName);
      setRevision(baseRevisionNum);

      // Prepare the SQL statement
      prepareInsertStatement();

      // Commit the insert
      m_Statement.executeUpdate();

      m_Statement.close();

      close();
   }


   /**
    * Prepares the SQL INSERT statement to create a new entry in the
    * CONTENTSTATUSHISTORY table, using the connection associated with this
    * PSContentStatusHistoryContext. Sets EVENTTIME  to the current time and
    * gets LASTMODIFIEDDATE to the value from the content status record.
    */
   private void prepareInsertStatement() throws SQLException
   {
      m_Statement =
         PSPreparedStatement.getPreparedStatement(m_Connection, INSERTSTRING);
      int nLoc = 0;
      m_Statement.setInt(++nLoc, m_nWorkflowID);
      m_Statement.setString(++nLoc, m_sSessionID);
      m_Statement.setInt(++nLoc, m_nContentStatusHistoryID);
      m_Statement.setInt(++nLoc, m_nContentID);
      m_Statement.setInt(++nLoc, m_nStateID);
      m_Statement.setString(++nLoc, m_sActor);
      m_Statement.setInt(++nLoc, m_nTransitionID);
      if (m_bValid)
      {
         m_Statement.setString(++nLoc, "Y");
      }
      else
      {
         m_Statement.setString(++nLoc, "N");
      }
      m_Statement.setString(++nLoc, m_sRoleName);
      m_Statement.setString(++nLoc, m_sCheckOutUserName);
      m_Statement.setString(++nLoc, m_sLastModifierName);
      m_Statement.setString(++nLoc,  m_sTransitionComment);
      m_Statement.setInt(++nLoc, m_nRevision);
      m_Statement.setString(++nLoc, m_sTitle);
      m_Statement.setString(++nLoc, m_sTransitionLabel);
      m_Statement.setString(++nLoc, m_sStateName);

      PSAbstractWorkflowContext.setDate(m_Statement,
                                        ++nLoc,
                                        m_LastModifiedDate);

      PSAbstractWorkflowContext.setDate(m_Statement,
                                        ++nLoc,
                                        m_EventTime);

   }

   /**
    * Sets the content status history ID for this entry.
    *
    * @param   contentStatusHistoryID the content status history ID for this
    *                                 entry.
    *                                 Must be  <CODE>> 0</CODE>.
    */
   private void setContentStatusHistoryID(int contentStatusHistoryID)
       throws SQLException, PSEntryNotFoundException
   {
      m_nContentStatusHistoryID = contentStatusHistoryID;
   }

   /**
    * Sets the ContentID for the content item acted on.
    *
    * @param   contentID the ContentID for the content item.
    *                    Must be <CODE>> 0</CODE>.
    */
   private void setContentID(int contentID)
   {
      m_nContentID = contentID;
   }

   /**
    * Sets the workFlowID for the content item acted on.
    *
    * @param   workFlowID the workFlowID for the content item.
    */
   private void setWorkFlowID(int workFlowID)
   {
      m_nWorkflowID = workFlowID;
   }

   /**
    * Sets the revision for the content item acted on.
    *
    * @param   revision the revision for the content item. For checkout this
    *          is the base revision, which is either 1, or the revision of the
    *          content item copied to create the revision checked out.
    */
   private void setRevision(int revision)
   {
      m_nRevision = revision;
   }

   /**
    * Sets the title of the content item for this content status history entry.
    *
    * @param   title    the title of the content item
    *                   May not be more than 255 characters
    */
   private void setTitle(String title)
   {
      m_sTitle = title;
   }

   /**
    * Sets the SessionID for this content status history entry.
    *
    * @param   sessionID the SessionID for this content status history entry.
    */
   private void setSessionID(String sessionID)
   {
      m_sSessionID = sessionID;
   }

   /**
    * Sets the name of the user that performed this transition or action
    * including check in and check out.
    *
    * @param   actorName the name of the user that performed this action.
    */
   private void setActorName(String actorName)
   {
      m_sActor = actorName;
   }

   /**
    * Sets the TransitionID for this content status history entry.
    *
    * @param   transitionID the TransitionID for this content
    *                       status history entry. 0 for checkin or checkout.
    */
   private void setTransitionID(int transitionID)
   {
      m_nTransitionID = transitionID;
   }

   /**
    * Sets the Transition Label for this content status history entry.
    *
    * @param   transitionLabel  if this is a transition, the  label for the
    *                           transition.
    *                           It may not be more than 50 characters
    *                           otherwise <CODE>null</CODE> for a check in
    *                           or checkout.
    *                           The transition label will be set to
    *                           "CheckIn" for a check in,
    *                           "CheckOut for a check out
    */
   private void setTransitionLabel(String transitionLabel)
   {
      /*
       * for a transition, use the transition label, set special strings for
       *  check in or check out.
       */
      if (null == transitionLabel)
      {
         /*
          * It is a check in if the name of the user that checked out this
          * content item is null.
          */
         if (null == getContentCheckoutUserName())
         {
            m_sTransitionLabel = "CheckIn";
         }
         else
         {
            m_sTransitionLabel = "CheckOut";
         }
      }
      else
      {
         m_sTransitionLabel = transitionLabel;
      }
   }


   /**
    * Sets indicator as to whether this content is publishable
    *
    * @param   isValid <CODE>true</CODE> if content is publishable
    *                   else <CODE>false</CODE>
    */
   private void setContentIsValid(boolean isValid)
   {
      m_bValid = isValid;
   }

   /**
    * Sets the ContentStateID for this content status history entry.
    *
    * @param   contentStateID the ID of the current state at completion of
    * transition or action.
    */
   private void setContentStateID(int contentStateID)
   {
      m_nStateID = contentStateID;
   }

   /**
    * Sets the name of the current state at completion of transition or
    * action.
    *
    * @param   stateName the content state name for this content
    *                         status history entry.
    */
   private void setStateName(String stateName)
   {
      m_sStateName = stateName;
   }


   /**
    * Sets the assigned role name for the state of the content item for this
    * content status history entry.
    *
    * @param   roleName the role name the state of the content item for this
    *                   content status history entry.
    *                   May not be more than 50 characters
    */
   private void setContentStateRoleName(String roleName)
   {
      m_sRoleName = roleName;
   }

   /**
    * Sets the name of the user that checked out this content item.
    *
    * @param   userName name of the user that checked out this content item
    *          <CODE>null</CODE> if item is not checked out.
    *          May not be more than 255 characters.
    */
   private void setContentCheckoutUserName(String userName)
   {
      m_sCheckOutUserName = userName;
   }

   /**
    * Sets the name of the user that last modified out this content item.
    *
    * @param lastModifierName  name of the user that last modified this content
    *                          item.  May not be more than 255 characters.
    *                          Initial and final whitespace will be trimmed.
    */
   private void setContentLastModifierName(String lastModifierName)
   {
      m_sLastModifierName = lastModifierName;
   }


   /**
    * Sets the date this content item was last modified.
    *
    * @param date   the date this content item was last modified
    */
   private void setContentLastModifiedDate(Date date)
   {
      m_LastModifiedDate = date;
   }

   /**
    * Sets the descriptive comment for this transition in the content status
    * history entry.
    *
    * @param   commentText the descriptive comment the transition
    *                      May be <CODE>null</CODE>.
    *                      May not be more than 255 characters.
    */
   private void setTransitionComment(String transitionComment)
   {
      m_sTransitionComment = transitionComment;
   }

   /*
    * The following get methods all implement the
    * PSContentStatusHistoryContext interface.
    */

   public int getContentStatusHistoryID()
   {
      return m_nContentStatusHistoryID ;
   }

   public int getContentID()
   {
      return m_nContentID;
   }

   public int getRevision()
   {
      return m_nRevision;
   }

   public String getTitle()
   {
      return m_sTitle;
   }

   public String getSessionID()
   {
      return m_sSessionID;
   }

   public String getActorName()
   {
      return m_sActor;
   }

   public int getTransitionID()
   {
      return m_nTransitionID;
   }

   public boolean getContentIsValid()
   {
      return m_bValid;
   }

   public int getContentStateID()
   {
      return m_nStateID;
   }

   public String getContentStateName()
   {
      return m_sStateName;
   }

   public String getTransitionLabel()
   {
      return m_sTransitionLabel;
   }

   public String getContentStateRoleName()
   {
      return m_sRoleName;
   }

   public String getContentCheckoutUserName()
   {
      return m_sCheckOutUserName;
   }

   public String getContentLastModifierName()
   {
      return m_sLastModifierName;
   }

   public Date getContentLastModifiedDate()
   {
      return m_LastModifiedDate;
   }

   public Date getEventTime()
   {
      return m_EventTime;
   }

   public String getTransitionComment()
   {
      return m_sTransitionComment;
   }

   /*
    * Release JDBC statement and result set if necessary, turn on auto commit
    * if necessary.
    */
   public void close()
   {
      try
      {
         if(null != m_Connection && false == m_Connection.getAutoCommit())
            m_Connection.setAutoCommit(true);
      }
      catch(SQLException e)
      {
      }
      //release resouces
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

   /*
    * Gets the next JDBC result set, and moves the data into member variables;
    * strings are trimmed.
    */
   public boolean moveNext() throws SQLException
   {
      String valid = "";
      boolean bSuccess = m_Rs.next();
      if(false == bSuccess)
      {
         return bSuccess;
      }
      m_sSessionID =
            PSWorkFlowUtils.trimmedOrEmptyString(m_Rs.getString("SESSIONID"));
      m_nContentStatusHistoryID = m_Rs.getInt("CONTENTSTATUSHISTORYID");
      m_nContentID = m_Rs.getInt("CONTENTID");
      m_nStateID = m_Rs.getInt("STATEID");
      m_sActor = PSWorkFlowUtils.trimmedOrEmptyString(m_Rs.getString("ACTOR"));
      m_nTransitionID = m_Rs.getInt("TRANSITIONID");
      valid = PSWorkFlowUtils.trimmedOrEmptyString(m_Rs.getString("VALID"));
      m_bValid = (null != valid && valid.equalsIgnoreCase("Y"));
      m_sRoleName =
            PSWorkFlowUtils.trimmedOrEmptyString(m_Rs.getString("ROLENAME"));
      // A null string is used to indicate an item is not checked out.
      m_sCheckOutUserName = PSWorkFlowUtils.
            trimmedOrNullString(m_Rs.getString("CHECKOUTUSERNAME"));
      m_sLastModifierName = PSWorkFlowUtils.
            trimmedOrEmptyString(m_Rs.getString("LASTMODIFIERNAME"));
      /*
       * Use getTimestamp because the JDBC-ODBC bridge for SQL Server does
       * not return minutes and seconds.
       */
      Timestamp tmpTimestamp = null;

      tmpTimestamp = m_Rs.getTimestamp("LASTMODIFIEDDATE");
      m_LastModifiedDate = new Date(tmpTimestamp.getTime());
      tmpTimestamp = m_Rs.getTimestamp("EVENTTIME");
      m_EventTime = new Date(tmpTimestamp.getTime());

      m_sTransitionComment = PSWorkFlowUtils.trimmedOrEmptyString(
                           m_Rs.getString("TRANSITIONCOMMENT"));
      m_nRevision = m_Rs.getInt("REVISIONID");
      m_sTitle = PSWorkFlowUtils.trimmedOrEmptyString(m_Rs.getString("TITLE"));
      m_sTransitionLabel = PSWorkFlowUtils.
       trimmedOrEmptyString(m_Rs.getString("TRANSITIONLABEL"));
      m_sStateName =
          PSWorkFlowUtils.trimmedOrEmptyString(m_Rs.getString("STATENAME"));

      return bSuccess;
   }


   public boolean isEmpty()
   {
      return (0 == m_nCount);
   }

   /*
    * Creates a string containing the values of all fields in the current
    * status history record (the one to which the database cursor points), one
    * to record per line.
    */
   public String toString()
   {
      StringBuffer buf = new StringBuffer();
      try
      {
         buf.append("\nPSContentStatusHistoryContext: ");
         buf.append("\nRecord Count = ");
         buf.append(m_nCount);
         buf.append("\nWorkFlowAppID = ");
         buf.append(m_nWorkflowID);
         buf.append("\nSessionID  = ");
         buf.append(getSessionID());
         buf.append("\nID  = ");
         buf.append(getContentStatusHistoryID());
         buf.append("\nContent ID = ");
         buf.append(getContentID());
         buf.append("\nRevision Number = ");
         buf.append(getRevision());
         buf.append("\nTitle = ");
         buf.append(getTitle());
         buf.append("\nStateID = ");
         buf.append(getContentStateID());
         buf.append("\nActor = ");
         buf.append(getActorName());
         buf.append("\nTransition ID = ");
         buf.append(getTransitionID());
         buf.append("\nValid = ");
         buf.append(getContentIsValid());
         buf.append("\nRole Name = ");
         buf.append(getContentStateRoleName());
         buf.append("\nCheckout user name = ");
         buf.append(getContentCheckoutUserName());
         buf.append("\nLast modifier = ");
         buf.append(getContentLastModifierName());
         buf.append("\nLast modified = ");
         /* Use Timestamp to get minutes and seconds. */
         if (null == getContentLastModifiedDate())
         {
            buf.append("null");
         }
         else
         {
            buf.append(new Timestamp(getContentLastModifiedDate().getTime()));
         }

         buf.append("\nEvent Time = ");
         if (null == getEventTime())
         {
            buf.append("null");
         }
         else
         {
            buf.append(new Timestamp(getEventTime().getTime()));
         }
         buf.append("\nTransition Comment = ");
         buf.append(getTransitionComment());
         buf.append("\nTransition Label = ");
         buf.append(getTransitionLabel());
         buf.append("\nState Name = ");
         buf.append(getContentStateName());
         buf.append("\n");
      }
      catch (Exception e)
      {
         return "PSContentStatusHistoryContext toString: Caught exception: "
                 + e;
      }
      return buf.toString();
   }

   /******** Context Defining Members ********/

   /** ID of the workflow for this item */
   private int m_nWorkflowID = 0;

   /** ID of the content item for this item */
   private int m_nContentID = 0;

   /** ID for this content status history record */
   private int m_nContentStatusHistoryID = 0;


   /******** Context Data Members ********/

   /**
    * The SessionID for this content status history entry.
    * May not be more than 40 characters.
    */
   private String m_sSessionID = "";

   /** The ID of the current state at completion of transition or action */
   private int m_nStateID = 0;

   /**
    * Name of the current state at the time of transition or action.
    * May not be more than 50 characters.
    */
   private String m_sStateName = "";

   /**  name of the user that performed this transition or action */
   private String m_sActor = null;

   /**
    * the transition ID if the content item has undergone a transition
    * 0 for check in and check out.
    */
   private int m_nTransitionID = 0;

   /** <CODE>true</CODE> if content is publishable else <CODE>false</CODE> */
   private boolean m_bValid = false;

   /**
    * comma-separated list of assigned role names for the content
    * item's current state
    * May not be more than 255 characters.
    */
   private String m_sRoleName = null;

   /**
    * Name of the user that checked out this content item, or
    * <CODE>null</CODE> item is not checked out
    * May not be more than 255 characters.
    */
   private String m_sCheckOutUserName = null;

   /**
    * name of the user that last modified this content item
    * May not be more than 255 characters.
    */
   private String m_sLastModifierName = null;

   /**
    * The revision for the content item. For checkout the base revision, which
    * is either 1, or the revision of the content item copied to create the
    * revision checked out.
    */
   private int m_nRevision = 0;

   /**
    * title of the content item
    * May not be more than 40 characters.
    */
   private String m_sTitle = null;

   /** the date/time this content item was last modified */
   private Date m_LastModifiedDate = null;

   /** date/time when this transition or action took place */
   private Date m_EventTime = null;

   /**
    * the descriptive comment for this transition.
    * May not be more than 255 characters.
    */
   private String m_sTransitionComment = "";

   /** label of the transition or action */
   private String m_sTransitionLabel = "";


   /******** Database Related Variables ********/

   /** Connection to the database */
   private Connection m_Connection = null;

   /** JDBC version of SQL statement to be executed. */
   private PreparedStatement m_Statement = null;

   /** Result of database query */
   private ResultSet m_Rs = null;

   /** Number of database records found */
   private int m_nCount = 0;

   /**
    * static constant string that represents the qualified table name.
    */
   static private String TABLE_CSHC =
      PSConnectionMgr.getQualifiedIdentifier("CONTENTSTATUSHISTORY");

  /**
    * SQL query string to get data base records for the content status history.
    */
   private static final String QRYSTRING =
   "SELECT " +
   TABLE_CSHC + ".SESSIONID , " +
   TABLE_CSHC + ".CONTENTSTATUSHISTORYID , " +
   TABLE_CSHC + ".CONTENTID , " +
   TABLE_CSHC + ".STATEID , " +
   TABLE_CSHC + ".ACTOR , " +
   TABLE_CSHC + ".TRANSITIONID , " +
   TABLE_CSHC + ".VALID , " +
   TABLE_CSHC + ".ROLENAME , " +
   TABLE_CSHC + ".CHECKOUTUSERNAME , " +
   TABLE_CSHC + ".LASTMODIFIERNAME , " +
   TABLE_CSHC + ".LASTMODIFIEDDATE , " +
   TABLE_CSHC + ".EVENTTIME , " +
   TABLE_CSHC + ".TRANSITIONCOMMENT , " +
   TABLE_CSHC + ".REVISIONID , " +
   TABLE_CSHC + ".TITLE , " +
   TABLE_CSHC + ".TRANSITIONLABEL , " +
   TABLE_CSHC + ".STATENAME " +
   "FROM " + TABLE_CSHC +
   " WHERE (" + 
   TABLE_CSHC + ".WORKFLOWAPPID=? AND " +
   TABLE_CSHC + ".CONTENTID=?)";

  /**
    * SQL insert string to insert a new data base record for the content status
    * history.
    */
  private static final String INSERTSTRING  =
   "INSERT INTO " + TABLE_CSHC + 
   " (" +
   TABLE_CSHC + ".WORKFLOWAPPID , " +
   TABLE_CSHC + ".SESSIONID , " +
   TABLE_CSHC + ".CONTENTSTATUSHISTORYID , " +
   TABLE_CSHC + ".CONTENTID , " +
   TABLE_CSHC + ".STATEID , " +
   TABLE_CSHC + ".ACTOR , " +
   TABLE_CSHC + ".TRANSITIONID , " +
   TABLE_CSHC + ".VALID , " +
   TABLE_CSHC + ".ROLENAME , " +
   TABLE_CSHC + ".CHECKOUTUSERNAME , " +
   TABLE_CSHC + ".LASTMODIFIERNAME , " +
   TABLE_CSHC + ".TRANSITIONCOMMENT , " +
   TABLE_CSHC + ".REVISIONID , " +
   TABLE_CSHC + ".TITLE , " +
   TABLE_CSHC + ".TRANSITIONLABEL , " +
   TABLE_CSHC + ".STATENAME , " +
   TABLE_CSHC + ".LASTMODIFIEDDATE , " +
   TABLE_CSHC + ".EVENTTIME )" +
   " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,? )";
}
