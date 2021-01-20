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
import com.percussion.data.PSTableChangeEvent;
import com.percussion.extension.IPSExtensionErrors;
import com.percussion.server.cache.PSItemSummaryCache;
import com.percussion.util.PSPreparedStatement;
import com.percussion.util.PSSqlHelper;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class PSContentStatusContext implements IPSContentStatusContext
{
   public PSContentStatusContext(Connection connection, int contentID)
     throws SQLException, PSEntryNotFoundException
   {
      m_nContentID = contentID;
      m_Connection = connection;
      try
      {
         m_Statement =
            PSPreparedStatement.getPreparedStatement(m_Connection, QRYSTRING);
         m_Statement.clearParameters();
         m_Statement.setInt(1, contentID);
         m_Rs = m_Statement.executeQuery();

         if(false == m_Rs.next())
         {
           close();
           throw new PSEntryNotFoundException(IPSExtensionErrors.NO_RECORDS);
         }
    //do not change the order
         m_nStateID = m_Rs.getInt("CONTENTSTATEID");
         m_sCheckOutUserName = PSWorkFlowUtils.
          trimmedOrNullString(m_Rs.getString("CONTENTCHECKOUTUSERNAME"));
         m_sLastModifierName = PSWorkFlowUtils.
          trimmedOrEmptyString(m_Rs.getString("CONTENTLASTMODIFIER"));

         /*
          * Use getTimestamp because the JDBC-ODBC bridge for SQL Server does
          * not return minutes and seconds.
          */
         Timestamp tmpTimestamp = null;

         tmpTimestamp = m_Rs.getTimestamp("CONTENTLASTMODIFIEDDATE");
         m_LastModifiedDate =
               PSWorkFlowUtils.sqlDateFromTimestamp(tmpTimestamp);
         tmpTimestamp = m_Rs.getTimestamp("LASTTRANSITIONDATE");
         m_LastTransitionDate =
               PSWorkFlowUtils.sqlDateFromTimestamp(tmpTimestamp);
         m_nContentTypeID = m_Rs.getInt("CONTENTTYPEID");
         tmpTimestamp = m_Rs.getTimestamp("CONTENTCREATEDDATE");
         m_CreatedDate = PSWorkFlowUtils.sqlDateFromTimestamp(tmpTimestamp);
         m_sCreatedByName = PSWorkFlowUtils.
               trimmedOrEmptyString(m_Rs.getString("CONTENTCREATEDBY"));
         tmpTimestamp = m_Rs.getTimestamp("CONTENTSTARTDATE");
         m_StartDate = PSWorkFlowUtils.sqlDateFromTimestamp(tmpTimestamp);
         tmpTimestamp = m_Rs.getTimestamp("CONTENTEXPIRYDATE");
         m_ExpiryDate = PSWorkFlowUtils.sqlDateFromTimestamp(tmpTimestamp);
         tmpTimestamp = m_Rs.getTimestamp("REMINDERDATE");
         m_ReminderDate = PSWorkFlowUtils.sqlDateFromTimestamp(tmpTimestamp);
         m_nWorkflowID = m_Rs.getInt("WORKFLOWAPPID");
         m_sTitle = PSWorkFlowUtils.
               trimmedOrEmptyString(m_Rs.getString("TITLE"));
         m_nCurrentRevision = m_Rs.getInt("CURRENTREVISION");
         if (m_Rs.wasNull())
         {
            m_nCurrentRevision =
                  IPSConstants.NO_CORRESPONDING_REVISION_VALUE;
         }
         m_nEditRevision = m_Rs.getInt("EDITREVISION");
         if (m_Rs.wasNull())
         {
            m_nEditRevision =  IPSConstants.NO_CORRESPONDING_REVISION_VALUE;
         }
         m_nTipRevision = m_Rs.getInt("TIPREVISION");
         if (m_Rs.wasNull())
         {
            m_nTipRevision =  IPSConstants.NO_CORRESPONDING_REVISION_VALUE;
         }
         String sYes = PSWorkFlowUtils.
               trimmedOrEmptyString(m_Rs.getString("REVISIONLOCK"));
         if(!m_Rs.wasNull() && sYes.trim().equalsIgnoreCase("Y"))
         {
            m_bRevisionLocked = true;
         }

         tmpTimestamp = m_Rs.getTimestamp("STATEENTEREDDATE");
         m_StateEnteredDate =
               PSWorkFlowUtils.sqlDateFromTimestamp(tmpTimestamp);
         tmpTimestamp = m_Rs.getTimestamp("NEXTAGINGDATE");
         m_NextAgingDate = PSWorkFlowUtils.sqlDateFromTimestamp(tmpTimestamp);
         m_nNextAgingTransition = m_Rs.getInt("NEXTAGINGTRANSITION");
         tmpTimestamp = m_Rs.getTimestamp("REPEATEDAGINGTRANSSTARTDATE");
         m_RepeatedAgingTransitionStartDate =
               PSWorkFlowUtils.sqlDateFromTimestamp(tmpTimestamp);
         m_nCommunityId = m_Rs.getInt("COMMUNITYID");
         m_nObjectType = m_Rs.getInt("OBJECTTYPE");
      }
      catch(SQLException e)
      {
         close();
         throw e;
      }
   }

   /* Implementation of IPSContentStatusContext interface */

   public void setCurrentRevision(int currentRevision)
   {
      m_nCurrentRevision = currentRevision ;
   }

   public void setEditRevision(int editRevision)
   {
      m_nEditRevision = editRevision ;
   }

   public void setTipRevision(int tipRevision)
   {
      m_nTipRevision = tipRevision ;
   }

   public void lockRevision()
   {
      m_bRevisionLocked = true ;
   }

   public void setContentStateID(int stateID)
   {
      m_nStateID = stateID;
   }

   public void setContentCheckedOutUserName(String checkedUserName)
   {
      m_sCheckOutUserName = checkedUserName;
   }

   public Date getReminderDate()
   {
      return m_ReminderDate;
   }

   public Date getStateEnteredDate()
   {
      return m_StateEnteredDate;
   }

   public Date getNextAgingDate()
   {
      return m_NextAgingDate;
   }

   public int getNextAgingTransition()
   {
      return m_nNextAgingTransition;
   }

   public Date getRepeatedAgingTransitionStartDate()
   {
      return m_RepeatedAgingTransitionStartDate;
   }

   public void setLastTransitionDate()
   {
       m_LastTransitionDate = new Date(new java.util.Date().getTime());
   }

   public void setLastTransitionDate(Date lastTransitionDate)
   {
       m_LastTransitionDate = lastTransitionDate;
   }

   public void setStateEnteredDate()
   {
      m_StateEnteredDate = new Date(new java.util.Date().getTime());
   }

   protected void setStateEnteredDate(Date stateEnteredDate)
   {
      m_StateEnteredDate = stateEnteredDate;
   }

   public void setNextAgingDate(Date nextAgingDate)
   {
      m_NextAgingDate = nextAgingDate;
   }

   public void setNextAgingTransition(int nextAgingTransition)
   {
      m_nNextAgingTransition = nextAgingTransition;
   }

   public void setRepeatedAgingTransitionStartDate(
      Date repeatedAgingTransitionStartDate)
   {
      m_RepeatedAgingTransitionStartDate = repeatedAgingTransitionStartDate;
   }

   public String getTitle()
   {
      return m_sTitle;
   }

   public int getCurrentRevision()
   {
      return m_nCurrentRevision;
   }

   public int getEditRevision()
   {
      return m_nEditRevision;
   }

   public int getTipRevision()
   {
      return m_nTipRevision;
   }

   public boolean isRevisionLocked()
   {
       return m_bRevisionLocked;
   }

   public boolean neverAged()
   {
      return (null == m_StateEnteredDate);
   }

   public int getWorkflowID()
   {
      return m_nWorkflowID;
   }

   public int getContentStateID()
   {
      return m_nStateID;
   }

   /**
    * Returns the communityid to which this item belongs.
    *
    * @return > 0.
    */
   public int getCommunityID()
   {
      return m_nCommunityId;
   }

   /**
    * Get the value of the OBJECTYPE column. This column has been added since
    * version 5.0.
    * 
    * @return the object type, it should be one of the values in the 
    *    OBJECTTYPE column of the PSX_OBJECTS table
    */
   public int getObjectType()
   {
      return m_nObjectType;
   }

   public String getContentCheckedOutUserName()
   {
      if(null == m_sCheckOutUserName)
         return "";

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


   public Date getLastTransitionDate()
   {
      return m_LastTransitionDate;
   }

   public int getContentID()
   {
      return m_nContentID;
   }

   public int getContentTypeID()
   {
      return m_nContentTypeID;
   }

   public String getContentCreatedBy()
   {
      return m_sCreatedByName;
   }

   public Date getContentCreatedDate()
   {
      return m_CreatedDate;
   }

   public Date getContentStartDate()
   {
      return m_StartDate;
   }

   public Date getContentExpiryDate()
   {
      return m_ExpiryDate;
   }

   private void setInt(PreparedStatement stmt, int index, int value, String columnName, Map<String, String> columns) throws SQLException
   {
      stmt.setInt(index, value);
      columns.put(columnName, String.valueOf(value));
   }

   private void setString(PreparedStatement stmt, int index, String value, String columnName, Map<String, String> columns) throws SQLException
   {
      stmt.setString(index, value);
      columns.put(columnName, String.valueOf(value));
   }

   private void setDate(PreparedStatement stmt, int index, Date value, String columnName, Map<String, String> columns) throws SQLException
   {
      PSAbstractWorkflowContext.setDate(stmt, index, value);
      String svalue = value == null ? null : PSWorkFlowUtils.DateString(value);  
      columns.put(columnName, svalue);
   }
   
   private void notifyUpdateItem(Map<String, String> columns)
   {
      PSTableChangeEvent event = new PSTableChangeEvent(IPSConstants.CONTENT_STATUS_TABLE, PSTableChangeEvent.ACTION_UPDATE, columns);
      PSItemSummaryCache cache = PSItemSummaryCache.getInstance();
      if (cache != null)
      {
         cache.tableChanged(event);
      }
   }
   
   public void commit(Connection conn) throws SQLException
   {
      PreparedStatement stmt = null;
      Map<String, String> columns = new HashMap<String, String>();
      try
      {
         String updateString = UPDATEWHATSTRING;
         int nLoc = 0;

         updateString += WHERESTRING;
         conn.setAutoCommit(false);
         stmt = PSPreparedStatement.getPreparedStatement(conn, updateString);
         stmt.clearParameters();
         setInt(stmt, ++nLoc, m_nStateID, CONTENTSTATEID, columns);
         if(null == m_sCheckOutUserName)
         {
            m_sCheckOutUserName = "";
         }
         setString(stmt, ++nLoc, m_sCheckOutUserName, CONTENTCHECKOUTUSERNAME, columns);
         setInt(stmt, ++nLoc, m_nCurrentRevision, CURRENTREVISION, columns);
         setInt(stmt, ++nLoc, m_nEditRevision, EDITREVISION, columns);
         setInt(stmt, ++nLoc, m_nTipRevision, TIPREVISION, columns);
         if (m_bRevisionLocked)
         {
            setString(stmt, ++nLoc, "Y", REVISIONLOCK, columns);
         }
         else
         {
            setString(stmt, ++nLoc, "N", REVISIONLOCK, columns);
         }

         setDate(stmt, ++nLoc, m_LastTransitionDate, LASTTRANSITIONDATE, columns);
         setDate(stmt, ++nLoc, m_StateEnteredDate, STATEENTEREDDATE, columns);

         setInt(stmt, ++nLoc,m_nNextAgingTransition, NEXTAGINGTRANSITION, columns);

         setDate(stmt, ++nLoc, m_NextAgingDate, NEXTAGINGDATE, columns);
         setDate(stmt, ++nLoc, m_StartDate, CONTENTSTARTDATE, columns);
         setDate(stmt, ++nLoc, m_ExpiryDate, CONTENTEXPIRYDATE, columns);
         setDate(stmt, ++nLoc, m_ReminderDate, REMINDERDATE, columns);
         setDate(stmt, ++nLoc, m_RepeatedAgingTransitionStartDate,
               REPEATEDAGINGTRANSSTARTDATE, columns);
         setInt(stmt, ++nLoc, m_nContentID, CONTENTID, columns);
         stmt.executeUpdate();
         PSSqlHelper.commit(conn);
         notifyUpdateItem(columns);
      }
      finally
      {
         //release resources
         try
         {
            if(null != stmt)
            {
               stmt.close();
               stmt = null;
            }
         }
         catch(SQLException e)
         {
         }
         try
         {
           conn.setAutoCommit(true);
         }
         catch(SQLException e)
         {
         }
      }
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

   /**
    * Produce a string describing the content status aging related context
    * values, one to a line.
    *
    * @return string describing the content status  aging related context
    * values.
    */
   public String toString(boolean agingOnly)
   {
      if (!agingOnly)
      {
         return this.toString();
      }

      StringBuffer buf = new StringBuffer();
      try
      {
         buf.append("\nPSContentStatusContext:");
         buf.append("\nContent ID = ");
         buf.append(m_nContentID);
         buf.append("\nContent Start Date = ");
         if (null == getContentStartDate())
         {
            buf.append("null");
         }
         else
         {
            buf.append(new Timestamp(getContentStartDate().getTime()));
         }
         buf.append("\nContent Expiry Date = ");
         if (null == getContentExpiryDate())
         {
            buf.append("null");
         }
         else
         {
            buf.append(new Timestamp(getContentExpiryDate().getTime()));
         }

         buf.append("\nReminder Date = ");
         if (null == getReminderDate())
         {
            buf.append("null");
         }
         else
         {
            buf.append(new Timestamp(getReminderDate().getTime()));
         }

         buf.append("\nState Entered Date = ");
         if (null == getStateEnteredDate())
         {
            buf.append("null");
         }
         else
         {
            buf.append(new Timestamp(getStateEnteredDate().getTime()));
         }

         buf.append("\nRepeated Aging Transition Start Date = ");
         if (null == getRepeatedAgingTransitionStartDate())
         {
            buf.append("null");
         }
         else
         {
            buf.append(new Timestamp(
               getRepeatedAgingTransitionStartDate().getTime()));
         }

         buf.append("\nNext Aging Date = ");
         if (null == getNextAgingDate())
         {
            buf.append("null");
         }
         else
         {
            buf.append(new Timestamp(getNextAgingDate().getTime()));
         }
         buf.append("\nNextAgingTransition = ");
         buf.append(getNextAgingTransition());
         buf.append("\n") ;
      }
      catch (Exception e)
      {
         return "PSContentStatusContext toString: Caught exception: "
               + e;
      }
      return buf.toString();
   }

   /**
    * Produce a string describing the content status aging related context
    * values, one to a line.
    *
    * @return string describing the content status context values.
    */
   public String toString()
   {
      StringBuffer buf = new StringBuffer();
      try
      {
         buf.append("\nPSContentStatusContext:");
         buf.append("\nContent ID = ");
         buf.append(m_nContentID);
         buf.append("\nCurrent Revision = ");
         buf.append(getCurrentRevision());
         buf.append("\nEdit Revision = ");
         buf.append(getEditRevision());
         buf.append("\nTip Revision = ");
         buf.append(getTipRevision());
         buf.append("\nRevision Locked = ");
         buf.append(isRevisionLocked());
         buf.append("\nContent Stateid = ");
         buf.append(getContentStateID());
         buf.append("\nWorkflow AppID = ");
         buf.append(getWorkflowID());
         buf.append("\nTitle = ");
         buf.append(getTitle());
         buf.append("\nContent Checkout User Name = ");
         buf.append(getContentCheckedOutUserName());
         buf.append("\nContent Last Modifier = ");
         buf.append(getContentLastModifierName());
         buf.append("\nContent Last Modified Date = ");
         if (null == getContentLastModifiedDate())
         {
            buf.append("null");
         }
         else
         {
            buf.append(new Timestamp(getContentLastModifiedDate().getTime()));
         }
         buf.append("\nContent Last Transition Date = ");
         if (null == getLastTransitionDate())
         {
            buf.append("null");
         }
         else
         {
            buf.append(new Timestamp(getLastTransitionDate().getTime()));
         }
         buf.append("\nContent TypeID = ");
         buf.append(getContentTypeID());
         buf.append("\nContent Created Date = ");
         if (null == getContentCreatedDate())
         {
            buf.append("null");
         }
         else
         {
            buf.append(new Timestamp(getContentCreatedDate().getTime()));
         }
         buf.append("\nContent Created By = ");
         buf.append(getContentCreatedBy());
         buf.append("\nContent Start Date = ");
         if (null == getContentStartDate())
         {
            buf.append("null");
         }
         else
         {
            buf.append(new Timestamp(getContentStartDate().getTime()));
         }
         buf.append("\nContent Expiry Date = ");
         if (null == getContentExpiryDate())
         {
            buf.append("null");
         }
         else
         {
            buf.append(new Timestamp(getContentExpiryDate().getTime()));
         }

         buf.append("\nReminder Date = ");
         if (null == getReminderDate())
         {
            buf.append("null");
         }
         else
         {
            buf.append(new Timestamp(getReminderDate().getTime()));
         }

         buf.append("\nState Entered Date = ");
         if (null == getStateEnteredDate())
         {
            buf.append("null");
         }
         else
         {
            buf.append(new Timestamp(getStateEnteredDate().getTime()));
         }

         buf.append("\nRepeated Aging Transition Start Date = ");
         if (null == getRepeatedAgingTransitionStartDate())
         {
            buf.append("null");
         }
         else
         {
            buf.append(new Timestamp(
               getRepeatedAgingTransitionStartDate().getTime()));
         }

         buf.append("\nNext Aging Date = ");
         if (null == getNextAgingDate())
         {
            buf.append("null");
         }
         else
         {
            buf.append(new Timestamp(getNextAgingDate().getTime()));
         }
         buf.append("\nNextAgingTransition = ");
         buf.append(getNextAgingTransition());
         buf.append("\n") ;
      }
      catch (Exception e)
      {
         return "PSContentStatusContext toString: Caught exception: "
               + e;
      }
      return buf.toString();
   }

   /* The following set methods are for testing use only */

   /**
    * Sets the value of the content start date, which can be used to initiate a
    * system field aging transition.
    *
    * @param startDate  the content start date
    */
   void setStartDate(Date startDate)
   {
      m_StartDate = startDate;
   }

   /**
    * Sets the value of the content expiry date, which can be used to initiate
    * a system field aging transition.
    *
    * @param expiryDate  the content expiry date
    */
   void setExpiryDate(Date expiryDate)
   {
      m_ExpiryDate = expiryDate;
   }

   /**
    * Sets the value of the content reminder date, which can be used to
    * initiate a system field aging transition.
    *
    * @param reminderDate  the content reminder date
    */
   void setReminderDate(Date reminderDate)
   {
      m_ReminderDate = reminderDate;
   }

   /******** Context Defining Members ********/

   /** ID of this content item */
   private int m_nContentID = 0;


   /******** Context Data Members ********/

   /** ID of the workflow for this item  */
   private int m_nWorkflowID = 0;

   /** content type ID of this content */
   private int m_nContentTypeID = 0;
   /**
    * The content Id for this content status entry.
    * May not be more than 40 characters.
    */
   private String m_sSessionID = "";

   /** The ID of the current state at completion of transition or action */
   private int m_nStateID = 1;

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

   /** the largest numbered  revision that is not checked out */
   private int m_nCurrentRevision =
       IPSConstants.NO_CORRESPONDING_REVISION_VALUE;
   /**
    * if the content item that is not checked out =
    *          IPSConstants.NO_CORRESPONDING_REVISION_VALUE <BR>
    * otherwise =  revision of the  content item checked out for editing
    */
   private int m_nEditRevision =
       IPSConstants.NO_CORRESPONDING_REVISION_VALUE;

   /** the largest revision number for this content item */
   private int m_nTipRevision =
       IPSConstants.NO_CORRESPONDING_REVISION_VALUE;

   /**
    * <CODE>true</CODE> if a new revision should be made for each checkout,
    * else  <CODE>false</CODE>
    */
   private boolean m_bRevisionLocked  = false;

   /**
    * title of the content item
    * May not be more than 40 characters.
    */
   private String m_sTitle = null;

   /** the date/time this content item was last modified */
   private Date m_LastModifiedDate = null;

   /** date this content item last underwent a transition */
   private Date m_LastTransitionDate = null;

   /**
    * name of the user that created this content item
    * May not be more than 255 characters.
    */
   private String m_sCreatedByName = null;

   /** date this content item was created */
   private Date m_CreatedDate = null;

   /** date this content item is first "valid" */
   private Date m_StartDate = null;

   /** date this content item is last "valid" */
   private Date m_ExpiryDate = null;

   /** reminder date for this content item */
   private Date m_ReminderDate = null;

   /** date the content item entered the current state */
   private Date m_StateEnteredDate = null;

  /**
   * Date for the next aging transition for this content item or
   * <CODE>null</CODE> if there is none */
   private Date m_NextAgingDate = null;

   /** the ID of the next aging transition or 0 if there is none */
   private int m_nNextAgingTransition = 0;

   /** the date to increment to compute the next repeated aging transition */
   private Date m_RepeatedAgingTransitionStartDate = null;

   /** The ID of the community to which the item belongs */
   private int m_nCommunityId = 0;
   
   /**
    *  The object type (id) to which the item belongs. Initialized by ctor. See 
    *  {@link getObjectType()} for detail description. 
    */
   private int m_nObjectType = 0;
   
   /******** Database Related Variables ********/

   /** Connection to the database */
   private Connection m_Connection = null;

   /** JDBC version of SQL statement to be executed */
   private PreparedStatement m_Statement = null;

   /** Result of database query */
   private ResultSet m_Rs = null;

   /**
    * static constant string that represents the qualified table name.
    */
   static private String TABLE_CSC =
      PSConnectionMgr.getQualifiedIdentifier("CONTENTSTATUS");

   /** 
    * SQL query string to get data base record for the content status 
    */
   private static String QRYSTRING = 
      "SELECT " +
      TABLE_CSC + ".CONTENTSTATEID, " +
      TABLE_CSC + ".CONTENTCHECKOUTUSERNAME, " +
      TABLE_CSC + ".CONTENTLASTMODIFIER, " +
      TABLE_CSC + ".CONTENTLASTMODIFIEDDATE, " +
      TABLE_CSC + ".LASTTRANSITIONDATE, " +
      TABLE_CSC + ".CONTENTTYPEID, " +
      TABLE_CSC + ".CONTENTCREATEDDATE, " +
      TABLE_CSC + ".CONTENTCREATEDBY, " +
      TABLE_CSC + ".CONTENTSTARTDATE, " +
      TABLE_CSC + ".CONTENTEXPIRYDATE, " +
      TABLE_CSC + ".REMINDERDATE, " +
      TABLE_CSC + ".WORKFLOWAPPID, " +
      TABLE_CSC + ".TITLE, " +
      TABLE_CSC + ".CURRENTREVISION, " +
      TABLE_CSC + ".EDITREVISION, " +
      TABLE_CSC + ".TIPREVISION, " +
      TABLE_CSC + ".REVISIONLOCK, " +
      TABLE_CSC + ".STATEENTEREDDATE, " +
      TABLE_CSC + ".NEXTAGINGDATE, " +
      TABLE_CSC + ".NEXTAGINGTRANSITION, " +
      TABLE_CSC + ".REPEATEDAGINGTRANSSTARTDATE, " +
      TABLE_CSC + ".COMMUNITYID, " +
      TABLE_CSC + ".OBJECTTYPE " +
      "FROM " + TABLE_CSC +
      " WHERE " + 
      TABLE_CSC  + ".CONTENTID=?";

   /**
    * Portion of SQL update string specifying content status record fields to
    * be updated.
    */
   private static final String UPDATEWHATSTRING = "UPDATE " + TABLE_CSC +
     " SET " + 
     TABLE_CSC + ".CONTENTSTATEID=?, " +
     TABLE_CSC + ".CONTENTCHECKOUTUSERNAME=?, " +
     TABLE_CSC + ".CURRENTREVISION=?, " +
     TABLE_CSC + ".EDITREVISION=?, " +
     TABLE_CSC + ".TIPREVISION=?, " +
     TABLE_CSC + ".REVISIONLOCK=?, "+
     TABLE_CSC + ".LASTTRANSITIONDATE=?, "+
     TABLE_CSC + ".STATEENTEREDDATE=?, "+
     TABLE_CSC + ".NEXTAGINGTRANSITION=?, " +
     TABLE_CSC + ".NEXTAGINGDATE=?, " +
     TABLE_CSC + ".CONTENTSTARTDATE=?, " +
     TABLE_CSC + ".CONTENTEXPIRYDATE=?, " +
     TABLE_CSC + ".REMINDERDATE=?, " +
     TABLE_CSC + ".REPEATEDAGINGTRANSSTARTDATE=? ";

   /**
    * Portion of SQL update string specifying which status record fields to
    * update.
    */
   private static final String WHERESTRING
   = " WHERE " + TABLE_CSC + ".CONTENTID=?";
   
   /**
    * Column names for CONTENTSTATUS table
    */
   private final static String CONTENTID = "CONTENTID";
   private final static String CONTENTSTATEID = "CONTENTSTATEID";
   private final static String CONTENTCHECKOUTUSERNAME = "CONTENTCHECKOUTUSERNAME";
   private final static String CURRENTREVISION = "CURRENTREVISION";
   private final static String EDITREVISION = "EDITREVISION";
   private final static String TIPREVISION = "TIPREVISION";
   private final static String REVISIONLOCK = "REVISIONLOCK";
   private final static String LASTTRANSITIONDATE = "LASTTRANSITIONDATE";
   private final static String STATEENTEREDDATE = "STATEENTEREDDATE";
   private final static String NEXTAGINGTRANSITION = "NEXTAGINGTRANSITION";
   private final static String NEXTAGINGDATE = "NEXTAGINGDATE";
   private final static String CONTENTSTARTDATE = "CONTENTSTARTDATE";
   private final static String CONTENTEXPIRYDATE = "CONTENTEXPIRYDATE";
   private final static String REMINDERDATE = "REMINDERDATE";
   private final static String REPEATEDAGINGTRANSSTARTDATE = "REPEATEDAGINGTRANSSTARTDATE";

   
}
