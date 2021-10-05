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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides methods for accessing, adding and removing users
 * that have approved content transitions, and is a wrapper for the backend
 * table 'CONTENTAPPROVALS'. <BR>
 * A content item may not undergo a transition until the required number of
 * users have approved.  Once a transition is performed, all user approvals
 * which share its "from state" must be cleared. 
 */

public class PSContentApprovalsContext implements IPSContentApprovalsContext
{
   /**
    * Constructor specifying the workflowID, contentID and transition
    * information that define a collection of approvals.
    *
    * @param workflowID    ID of the workflow for this approval       
    * @param connection    data base connection       
    * @param contentID     ID of the content item for this approval      
    * @param transContext  transition context for this approval          
    * @throws              SQLException if an SQL error occurs
    */
   public PSContentApprovalsContext(int workflowID,
                                    Connection connection,
                                    int contentID,
                                    PSTransitionsContext transContext)
      throws SQLException
   {
      // Validate the input
      
      if (null == connection) 
      {
         throw new IllegalArgumentException("The connection may not be null");
      }

      if (null == transContext) 
      {
         throw new IllegalArgumentException( "The transitions context may " +
            "not be null in PSContentApprovalsContext");
      }

      // Assign the member variables
      m_nWorkflowID = workflowID;
      m_nContentID = contentID;
      m_nTransitionID = transContext.getTransitionID();
      m_nStateID = transContext.getTransitionFromStateID();
      m_Connection = connection;
      
      // Prepare and execute the query
      try
      {
         m_Statement =
            PSPreparedStatement.getPreparedStatement(m_Connection, QRYSTRING);
         m_Statement.clearParameters();
         m_Statement.setInt(1, workflowID);
         m_Statement.setInt(2, contentID);
         m_Statement.setInt(3, m_nTransitionID);
         m_Rs = m_Statement.executeQuery();
         while(moveNext())
         {
            // Accumulate a list of users who have approved
            if (null !=  m_sUserName) 
            {
               m_UserList.add(m_sUserName);
               m_nCount++;
            }
         }
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

   private boolean moveNext() throws SQLException
   {
      boolean bSuccess = m_Rs.next();
      if(false == bSuccess)
      {
         return bSuccess;
      }
      m_sUserName =
            PSWorkFlowUtils.trimmedOrNullString(m_Rs.getString("USERNAME"));

      m_roleIdList.add(new Integer(m_Rs.getInt("ROLEID")));
      return bSuccess;
   }

   /**
    * Returns a List of all the roleids in this context.  These
    * role ids are <code>Integer</code>
    *
    * @return never <code>null</code>
    */
   public List roleIdList()
   {
      return m_roleIdList;
   }
   /**
    * Return string representation of context, including all approvers.
    *
    * @return string representation of context, including all approvers.
    */
   public String toString()
   {
      StringBuilder buf = new StringBuilder();
      buf.append("\nPSContentApprovalsContext: \n" +
                 "Workflow ID =     " + m_nWorkflowID  + "\n" +
                 "Transition ID =   " + m_nContentID + "\n" +
                 "Content ID =      " + m_nTransitionID + "\n" +
                 "Total approvals = " + m_nCount + "\n");

      if (m_nCount > 0 ) 
      {
          buf.append("Approvers = \n");
          for (int i = 0 ; i < m_nCount; i++) 
          {             
             buf.append("            " + m_UserList.get(i)  + "\n");
          }
      }
      return buf.toString();
   }

   /**
    * Return string representation of context, listing only approver with the
    * given index.
    *
    * @param i  index of approver                   
    * @return   string representation of context, listing only approver with 
    *           the given index
    * @throws   IllegalArgumentException if the index exceeds the number of
    *           approvers.
    */
   public String toString(int i)
   {
      // The number of approvers must exceed the index
      if (i >= m_nCount ) 
      {
         throw new IllegalArgumentException(
               "The number of approvers must exceed the index.");
      } 
      
      return "\nPSContentApprovalsContext:" +  "\n" +
            "Workflow ID =     " + m_nWorkflowID  + "\n" +
            "Transition ID =   " + m_nContentID + "\n" +
            "Content ID =      " + m_nTransitionID + "\n" +
            "Total approvals = " + m_nCount + "\n" +
            "Approver " + i + "   =       " + m_UserList.get(i)  + "\n";
   }   
   
   /********* Implementation of IPSContentApprovalsContext interface *********/
   
   public void addContentApproval(String userName, int roleId)
      throws SQLException
   {
      int nRows = 0;
      
      try 
      {
         m_Statement = null;
         userName = PSWorkFlowUtils.trimmedOrNullString(userName);
         
         if (null == userName)
         {
            throw new IllegalArgumentException(
               "The user name for an approval may not be null or empty");
         }
         
         m_Statement =
            PSPreparedStatement.getPreparedStatement(
               m_Connection,
               INSERTSTRING);
         
         m_Statement.clearParameters();
         m_Statement.setInt(1, m_nWorkflowID);
         m_Statement.setInt(2, m_nContentID);
         m_Statement.setInt(3, m_nTransitionID);
         m_Statement.setInt(4, m_nStateID);
         m_Statement.setString(5, userName);
         m_Statement.setInt(6, roleId);
         nRows = m_Statement.executeUpdate();

         // Add the new user to the list
         if (nRows > 0 ) // Should always be true, since no exception thrown
         {
             m_UserList.add(userName);
             m_nCount += nRows;
         } 
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
    * Closes the result set and statement if necessary
    */
   private void close()
   {
      //release resources
      try
      {
         if(null != m_Rs)
         {
            m_Rs.close();
            m_Rs = null;
         }
      
         if(null!=m_Statement)
         {
            m_Statement.close();
            m_Statement = null;
         }
      }
      catch(SQLException e)
      {
      }
   }

   public boolean hasUserActed(String userName)
   {
      boolean hasActed = false;
      int count = 0;
      userName = PSWorkFlowUtils.trimmedOrNullString(userName);
      
      if (null == userName || 0 == userName.length())
      {
         return false;
      }

      try 
      {
         m_Rs = null; 
         m_Statement = null;
         m_Statement =
            PSPreparedStatement.getPreparedStatement(
               m_Connection,
               USER_QUERYSTRING);
         m_Statement.clearParameters();

         m_Statement.setInt(1, m_nWorkflowID);
         m_Statement.setInt(2, m_nContentID);
         m_Statement.setInt(3, m_nStateID);
         m_Statement.setString(4, userName);
         m_Rs = m_Statement.executeQuery();

         if (m_Rs.next() && (m_Rs.getInt(1) > 0)) 
         {
            hasActed = true;
         }
      }
      catch(SQLException e)
      {
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
      return hasActed;
   }

   /**
    * Has this role acted on this transition?
    *
    * @param roleId
    * @return <code>true</code> if it has, otherwise <code>false</code>.
    */
   public boolean hasRoleActed(int roleId)
   {
      boolean hasActed = false;
      int count = 0;

      if (roleId < 0 )
      {
         return false;
      }

      try
      {
         m_Rs = null;
         m_Statement = null;
         m_Statement =
            PSPreparedStatement.getPreparedStatement(
               m_Connection,
               ROLEID_QRYSTRING);
         m_Statement.clearParameters();

         m_Statement.setInt(1, m_nWorkflowID);
         m_Statement.setInt(2, m_nContentID);
         m_Statement.setInt(3, m_nStateID);
         m_Statement.setInt(4, roleId);
         m_Rs = m_Statement.executeQuery();

         if (m_Rs.next() && (m_Rs.getInt(1) > 0))
         {
            hasActed = true;
         }
      }
      catch(SQLException e)
      {
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
      return hasActed;
   }
   public List getApprovedUserNames()
   {
      return m_UserList;
   }

   public boolean getIsUserListed(String userName)
   {
      userName = PSWorkFlowUtils.trimmedOrNullString(userName);
      
      if (0 == m_nCount || null == userName || 0 == userName.length())
      {
         return false;
      }

      return (m_UserList.contains(userName));
   }

   public int getApprovedUserCount()
   {
      return m_nCount;
   }
   
   public boolean isEmpty()
   {
      return (0 == m_nCount);
   }

   public int emptyApprovals() throws SQLException
   {
      m_Statement =
         PSPreparedStatement.getPreparedStatement(m_Connection, DELETESTRING);
      try
      {
         m_Statement.clearParameters();
         m_Statement.setInt(1, m_nContentID);
         m_nCount = 0;
         m_UserList.clear();
         return m_Statement.executeUpdate();
      }
      finally
      {
         try
         {
            close();
         }
         catch(Exception ignore) {}
      }
   }

   /******** Database Related Members ********/

   /**
    * Connection to the database
    */
   private Connection m_Connection = null;
   
   /**
    * JDBC version of SQL statement to be executed.
    */
   private PreparedStatement m_Statement = null;
   
   /**
    * Result of database query
    */
   private ResultSet m_Rs = null;


   /**
    * User name at the current cursor position - updated every time moveNext().
    * is called
    */
   private String m_sUserName = "";
   
   /******** Context Defining Members ********/
   
   /**
    * ID of the workflow for this approval.
    */
   private int m_nWorkflowID = 0;

   /**
    * ID of the content item for this approval.
    */
   private int m_nContentID = 0;

   /**
    * ID of the transition for this approval.
    */
   private int m_nTransitionID = 0;

   /**
    * ID of the inital content state for this approval.
    */      
   private int m_nStateID = 0;

   /******** Context Data Members ********/
   
   /**
    * Number of approving users.
    */
   private int m_nCount = 0;


   /**
    * List of users that have approved the content transition.
    */
   private ArrayList m_UserList = new ArrayList();

   /**
    * List of roles ids that have approved the content transition.
    */
   private ArrayList m_roleIdList = new ArrayList();

   /**
    * static constant string that represents the qualified table name.
    */
   static private String TABLE_CAC =
      PSConnectionMgr.getQualifiedIdentifier("CONTENTAPPROVALS");

   /**
    * SQL query string to get data base records for the approvals.
    */
   private static String QRYSTRING = 
         "SELECT " + 
         TABLE_CAC + ".USERNAME, " + 
         TABLE_CAC + ".ROLEID" +
         " FROM " + 
         TABLE_CAC + 
         " WHERE (" + 
         TABLE_CAC + ".WORKFLOWAPPID=? AND " + 
         TABLE_CAC + ".CONTENTID=? AND " + 
         TABLE_CAC + ".TRANSITIONID=? )";

   /**
    * SQL query string to get all the role ids that have taken action for
    * this transition and this workflow.
    */
   private static String ROLEID_QRYSTRING =
         "SELECT COUNT(*) FROM " + 
         TABLE_CAC + 
         " WHERE (" + 
         TABLE_CAC + ".WORKFLOWAPPID=? AND " + 
         TABLE_CAC + ".CONTENTID=? AND " + 
         TABLE_CAC + ".TRANSITIONID=? AND " + 
         TABLE_CAC + ".ROLEID=?)";

   /**
    * SQL query string to get count of transition approvals on this content
    * item by this user from this state.
    */
   private static String USER_QUERYSTRING =  
      "SELECT COUNT(*) FROM " +
      TABLE_CAC + 
      " WHERE (" +
      TABLE_CAC + ".WORKFLOWAPPID=? AND " + 
      TABLE_CAC + ".CONTENTID=? AND " + 
      TABLE_CAC + ".STATEID=? AND " + 
      TABLE_CAC + ".USERNAME=?)";
   
   /**
    * SQL string to insert a data base record for a new approval.
    */   
   private static String INSERTSTRING =  
      "INSERT INTO " + 
      TABLE_CAC + 
      " (" +
      TABLE_CAC +".WORKFLOWAPPID, " +
      TABLE_CAC +".CONTENTID, " +
      TABLE_CAC +".TRANSITIONID, " +
      TABLE_CAC +".STATEID ," +
      TABLE_CAC +".USERNAME, " +
      TABLE_CAC +".ROLEID) " +
      "VALUES(?,?,?,?,?,?)";

   /**
    * SQL string to delete all data base record for approvals for given content
    * item with a given initial state.
    */    
   private static String DELETESTRING =  
      "DELETE FROM " + 
      TABLE_CAC + 
      " WHERE (" + 
      TABLE_CAC + ".CONTENTID=?)";
}
