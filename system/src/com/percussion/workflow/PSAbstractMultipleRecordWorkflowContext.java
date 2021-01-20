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

import com.percussion.util.PSPreparedStatement;

import java.sql.SQLException;

/**
 * This abstract class provides methods and members and a framework for
 * implementing workflow contexts that retrieve multiple data sets.<BR>
 * Creating a read-only workflow context minimally requires implementing
 * {@link #AccumulateCurrentDataSet} {@link #MoveAccumulatedDataSet} in
 * addition to the methods required by {@link PSAbstractWorkflowContext}, which
 * this class extends. Those methods are: a constructor,
 * {@link PSAbstractWorkflowContext#getQueryString}, 
 * {@link PSAbstractWorkflowContext#setQueryParameters},
 * {@link PSAbstractWorkflowContext#setQueryParameters} and any get methods
 * required by the corresponding workflow interface.<BR>  
 */
 
public abstract class PSAbstractMultipleRecordWorkflowContext
   extends PSAbstractWorkflowContext 
{
   /**
    * Moves the next data set into the context data variables, so they can be
    * obtained via the context "get" methods.
    *
    * @return <CODE>true</CODE> if data has been moved into the context data
    * variables , else <CODE>false</CODE> if there are no more data sets.
    */
   public boolean moveNext() 
   {
      currentContextDataIndex++;
            
      return MoveAccumulatedDataSet(currentContextDataIndex);
   }

   
  /* Override of method in class PSAbstractWorkflowContext  */
   
   public void getDataFromDataBase()
      throws SQLException
   {
      int nLoc = 0;
      m_nCount = 0;
      m_sQueryString = getQueryString();
      m_Statement =
         PSPreparedStatement.getPreparedStatement(m_Connection, m_sQueryString);
      m_Statement.clearParameters();
      setQueryParameters();
      m_Rs = m_Statement.executeQuery();
      
      /* Accumulate the data from the result sets in array lists */
      while(m_Rs.next())
      {
         resultSetMove();
         AccumulateCurrentDataSet();
         m_nCount++;
      }

      /*
       * If more than one result set was found, move the first one into the
       * context data members, so it will be retrieved by "get" methods.
       */
      if (m_nCount > 1) 
      {
         MoveAccumulatedDataSet(0);
      } 
   }

   /**
    * Add data from the current result set to array lists or other
    * structures.  <BR>
    * Works in concert with {@link #MoveAccumulatedDataSet}
    */
   // see PSTransitionNotificationsContext for a sample implementation
   protected abstract void AccumulateCurrentDataSet();

   /*
    * Move the indexed data set from the structures in which they were
    * accumulated to the context data members. <BR>
    * Works in concert with {@link #AccumulateCurrentDataSet}
    *
    * @param index  zero-based index of the requested data set.
    */
   // see PSTransitionNotificationsContext for a sample implementation
   protected abstract boolean MoveAccumulatedDataSet(int index);
   
   /** Current index into context data array lists */
   protected int currentContextDataIndex = 0;

}
