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

import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSResultPager;
import com.percussion.error.PSErrorHandler;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.log.PSLogHandler;
import com.percussion.log.PSLogManager;
import com.percussion.log.PSLogServerWarning;
import com.percussion.server.IPSRequestHandler;
import com.percussion.server.PSApplicationHandler;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.utils.jdbc.IPSConnectionInfo;
import com.percussion.utils.jdbc.PSConnectionDetail;
import com.percussion.utils.jdbc.PSConnectionHelper;
import com.percussion.xml.PSXmlTreeWalker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;

import javax.naming.NamingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;


/**
 * The PSExecutionData class is used primarily as a storage class for the
 * various pieces of context information passed along during the processing
 * of an execution plan. During each execution steps, any of this information
 * may be accessed, modified or removed. Additional information may also
 * be added.
 *
 * @see        PSQueryOptimizer
 * @see        IPSExecutionStep
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSExecutionData 
{
    private static final Logger ms_log = LogManager.getLogger(PSExecutionData.class);
   
   /**
    * Construct an execution object for this request.
    *
    * @param ah the application handler associated with this request, may be
    * <code>null</code>
    *
    * @param dh the data handler associated with this request
    * ({@link com.percussion.data.PSQueryHandler PSQueryHandler} or
    * {@link com.percussion.data.PSUpdateHandler PSUpdateHandler}).  May be
    * <code>null</code> as this parameter is never used by this class.
    *
    * @param req the request context, not <code>null</code>.
    */
   public PSExecutionData(PSApplicationHandler ah, IPSRequestHandler dh,
                          PSRequest req)
   {
      m_AppHandler = ah;
      m_ReqHandler = dh;
      m_Request = req;
      m_ResultSetStack = new Stack();
   }

   /**
    * Releases all of the resources held by this object.
    * <ol>
    * <li>Release temporary file
    * <li>Close all result sets
    * <li>Close all prepared statements
    * <li>Release connections to the database pool
    * </ol>
    */
   public void release()
   {
      /* Reset force row of nulls mode */
      m_forceRowOnNullResultSet = false;

      /* If we have a temp file resource, close and delete it */
      releaseTempFileResource();

      m_rsContext = null;
      m_rowBuffer = null;

      /* first close any result sets */
      if (m_ResultSetStack != null) {
         while (!m_ResultSetStack.empty()) {
            try {
               ((ResultSet)m_ResultSetStack.pop()).close();
            } catch (SQLException e) {
               Object[] args = { PSSqlException.toString(e) };
               PSLogManager.write(
                  new PSLogServerWarning(
                  IPSBackEndErrors.EXEC_DATA_CLOSE_RESULT_SET,
                  args, true, "ExecutionData"));
            }
         }
         m_ResultSetStack = null;
      }

      /* then close any statements (side effect: closes any result set
       * generated from the statement) */
      if (m_Statements != null) {
         for (int i = 0; i < m_Statements.size(); i++) {
            try {
               ((PreparedStatement)m_Statements.get(i)).close();
            } catch (SQLException e) {
               Object[] args = { PSSqlException.toString(e) };
               PSLogManager.write(
                  new PSLogServerWarning(
                  IPSBackEndErrors.EXEC_DATA_CLOSE_PREP_STMT,
                  args, true, "ExecutionData"));
            }
         }

         m_Statements = null;
      }

      /* finally, release the pooled connections */
      if (m_Connections != null) {
         for (int i = 0; i < m_Connections.size(); i++)
         {
            Connection conn = (Connection) m_Connections.get(i);
            try
            {
               conn.close();
            }
            catch (SQLException e)
            {
               ms_log.error("Problem closing db connection", e);
            }
         }
         m_Connections = null;
         m_connectionDetailList = null;
      }
   }

   /**
    * Gets the id assigned to the application that generated this data. This is
    * most commonly used when constructing a PSLogError subclass to report an
    * error through the error handler.
    *
    * @return the id of the application that generated this data, or 0 if no
    * application is associated with this data.
    */
   public int getId()
   {
      if (m_AppHandler != null)
         return m_AppHandler.getId();
      else
         return 0;
   }

   /**
    * Get the DB connection assigned to this request for the specified
    * key.
    *
    * @param   connKey     the key associated with this connection
    *
    * throws  SQLException   if a SQL error occurs
    *
    * @throws  PSIllegalArgumentException if connKey is out of range
    *                                        or no connections exist
    */
   public Connection getDbConnection(int connKey)
      throws SQLException, PSIllegalArgumentException
   {
      if ((m_Connections == null) || (m_Connections.size() == 0))
         throw new PSIllegalArgumentException(
            IPSBackEndErrors.EXEC_DATA_NO_CONNECTIONS);

      if (connKey >= m_Connections.size()) {
         Object[] args = {
            String.valueOf(connKey),
            "0 - " + String.valueOf(m_Connections.size()-1) };
         throw new PSIllegalArgumentException(
            IPSBackEndErrors.EXEC_DATA_BAD_CONN_KEY, args);
      }
      return (Connection) m_Connections.get(connKey);
   }


   /**
    * Get the DB connection detail assigned to this request for the specified
    * key.
    *
    * @param   connKey     the key associated with this connection
    *
    * @return The connection detail, never <code>null</code>.
    *
    * @throws  PSIllegalArgumentException if connKey is out of range
    *                                        or no connections exist
    */
   public PSConnectionDetail getDbConnectionDetail(int connKey)
      throws PSIllegalArgumentException
   {
      if ((m_Connections == null) || (m_Connections.size() == 0))
         throw new PSIllegalArgumentException(
            IPSBackEndErrors.EXEC_DATA_NO_CONNECTIONS);

      if (connKey >= m_Connections.size()) {
         Object[] args = {
            String.valueOf(connKey),
            "0 - " + String.valueOf(m_Connections.size()-1) };
         throw new PSIllegalArgumentException(
            IPSBackEndErrors.EXEC_DATA_BAD_CONN_KEY, args);
      }
      return m_connectionDetailList.get(connKey);
   }   

   /**
    * Get the DB connection assigned to this request for the specified
    * DB.
    *
    * @param   info        the connection info to add a connection from
    *
    * @return              the connection key
    *
    * @throws  SQLException   if a SQL error occurs
    */
   public int addDbConnection(IPSConnectionInfo info)
      throws SQLException
   {
      if (m_Connections == null)
      {
         m_Connections = new ArrayList();
         m_connectionDetailList = new ArrayList<>();
      }

      try
      {
         Connection conn = PSConnectionHelper.getDbConnection(info);
         PSConnectionDetail detail = PSConnectionHelper.getConnectionDetail(
            info);
         m_Connections.add(conn);
         m_connectionDetailList.add(detail);
      }
      catch (NamingException e)
      {
         throw new SQLException(e.getLocalizedMessage());
      }
      return (m_Connections.size() - 1);  /* return 0-based position */
   }

   /**
    * Gets the log handler for logging against this request.
    *
    * @return Either the log handler for the application that generated the
    * data or the server's log handler.  Never <code>null</code>.
    */
   public PSLogHandler getLogHandler()
   {
      if (m_LogHandler == null)
      {
         if (m_AppHandler != null)
            m_LogHandler = m_AppHandler.getLogHandler();

         if (m_LogHandler == null)
            m_LogHandler = PSServer.getLogHandler();
      }
      return m_LogHandler;
   }

   /**
    * Gets the error handler for reporting errors against this request.
    *
    * @return Either the error handler for the application that generated the
    * data or the server's error handler.  Never <code>null</code>.
    */
   public PSErrorHandler getErrorHandler()
   {
      if (m_ErrorHandler == null)
      {
         if (m_AppHandler != null)
            m_ErrorHandler = m_AppHandler.getErrorHandler();

         if (m_ErrorHandler == null)
            m_ErrorHandler = PSServer.getErrorHandler();
      }
      return m_ErrorHandler;
   }

   /**
    * Get the request object associated with this request. The request
    * object contains context information such as request parameters,
    * CGI variables and input data.
    *
    * @return           the request object
    */
   public PSRequest getRequest()
   {
      return m_Request;
   }

   /**
    * Sets the new request as provided.
    *
    * @param request teh new request, not <code>null</code>.
    * @return the request that has been replaced, never <code>null</code>.
    */
   public PSRequest setRequest(PSRequest request)
   {
      if (request == null)
         throw new IllegalArgumentException("request cannot be null");

      PSRequest current = m_Request;
      m_Request = request;

      return current;
   }

   /**
    * Get the stack containing the result sets. This can be used to merge
    * result sets then push the merged result set back on, etc.
    *
    * @return     the stack of result sets
    */
   public Stack getResultSetStack()
   {
      return m_ResultSetStack;
   }

   /**
    * Add a prepared statement to the list of statements. This is used
    * to accumulate a list which the handler can close afterwards.
    */
   public void addPreparedStatement(PreparedStatement stmt)
   {
      if (m_Statements == null)
         m_Statements = new ArrayList();

      m_Statements.add(stmt);
   }

   /**
    * Remove a prepared statement from the list of statements.
    */
   public void removePreparedStatement(PreparedStatement stmt)
   {
      if (m_Statements != null)
         m_Statements.remove(stmt);
   }

   /**
    * Get the list of prepared statements. This is used
    * by the handler to close any opened statements.
    */
   public List getPreparedStatements()
   {
      return m_Statements;
   }

   /**
    * Get the result set meta data object defining the structure of
    * the result set currently being processed. This is not available until it
    * is set by the user or {@link #getNextResultSet() getNextResultSet} has
    * been called.
    *
    * @return The meta data for the current result set, possibly <code>null
    *    </code>.
    */
   public ResultSetMetaData getCurrentResultSetMetaData()
   {
      return m_CurRSMetaData;
   }

   /**
    * Set the result set meta data object defining the structure of
    * the result set currently being processed.
    *
    * @param   meta     the result set meta data
    */
   public void setCurrentResultSetMetaData(ResultSetMetaData meta)
   {
      m_CurRSMetaData = meta;
   }

   /**
    * Get the data associated with the current result set row. This is not
    * available until it is set by the user or {@link #getNextResultSet()
    * getNextResultSet} has been called.
    *
    * @return The result row data, possibly <code>null</code>.
    */
   public Object[] getCurrentResultRowData()
   {
      return m_CurRowData;
   }

   /**
    * Set the data associated with the current result set row.
    *
    * @param   data     the result row data
    */
   public void setCurrentResultRowData(Object[] data)
   {
      m_CurRowData = data;
   }

   /**
    * Does the application that generated this data allow login information to
    * be passed through to the back end?
    *
    * @return <code>true</code> if passthru is enabled, <code>false</code> if
    * passthru is disabled or no application is associated with this data.
    */
   public boolean isBeLoginPassthruEnabled()
   {
      if (m_AppHandler != null)
         return m_AppHandler.getApplicationDefinition().isBeLoginPassthruEnabled();
      else
         return false;
   }

   /**
    * Get the walker being used to traverse the input XML document.
    *
    * @return           the walker for the request's input XML document
    */
   public PSXmlTreeWalker getInputDocumentWalker()
   {
      return m_inputDocumentWalker;
   }

   /**
    * Set the walker to use for traversal of the input XML document.
    *
    * @param   walker   the walker for the request's input XML document
    */
   public void setInputDocumentWalker(PSXmlTreeWalker walker)
   {
      m_inputDocumentWalker = walker;
   }


   /**
    * When processing multiple result sets, the processing may not be linear.
    * This method can be used to save the result set specific information in
    * this object while processing the next result set. It can later be restored
    * by calling {@link #restoreResultSetContext() restoreResultSetContext}.
    */
   public void saveResultSetContext()
   {
      m_rsContext = new ResultSetContext();
      m_rsContext.m_metaData = m_CurRSMetaData;
      m_rsContext.m_rowData = m_CurRowData;
      m_rsContext.m_rowBuffer = m_rowBuffer;
   }

   /**
    * This method is used to restore state that was saved with the {@link
    * #saveResultSetContext() saveResultSetContext} method. If no context had
    * ever been saved an exception is thrown. After the context is restored, the
    * saved context is cleared.
    *
    * @throws IllegalStateException if there is no context to restore.
    */
   public void restoreResultSetContext()
      throws IllegalStateException
   {
      if ( null == m_rsContext )
         throw new IllegalStateException( "No context to restore." );

      m_CurRSMetaData = m_rsContext.m_metaData;
      m_CurRowData = m_rsContext.m_rowData;
      m_rowBuffer = m_rsContext.m_rowBuffer;
      m_rsContext = null;
   }

   /**
    * Pops a result set off of the stack and sets up the meta data and the
    * current row buffer. If there are no more result sets, <code>null</code>
    * is returned and nothing is done. The current row buffer does not contain
    * any data. You must call {@link #readRow() readRow} to get each row of
    * data.
    *
    * @return the result set that was popped from the stack, <code>null</code>
    *    if there are no more result sets.
    *
    * @throws SQLException If any problems occur while getting the meta data or
    *    reading the result set.
    */
   public ResultSet getNextResultSet()
      throws SQLException
   {
      ResultSet rs = null;
      try
      {
         if ( !m_ResultSetStack.empty())
         {
            rs = (ResultSet) m_ResultSetStack.pop();
            m_CurRSMetaData = rs.getMetaData();
            m_rowBuffer = new PSRowDataBuffer(rs);
            m_CurRowData = m_rowBuffer.getCurrentRow();
         }
      }
      catch ( EmptyStackException e )
      {
         // ignore, it won't happen 'cause we check it first
      }
      return rs;
   }

   /**
    * This is for use with the {@link #getNextResultSet() getNextResultSet}
    * method. It causes the next row of data in the current result set to
    * become available in the array that was returned by the {@link
    * #getCurrentResultRowData() getCurrentResultRowData} method.
    *
    * @return <code>true if a new row was available in the result set, <code>
    *    false</code> otherwise.
    *
    * @throws IllegalStateException if called before <code>getNextResultRow
    *    </code> has been called.
    *
    * @throws SQLException If any problems occur while reading the result set.
    */
   public boolean readRow()
      throws IllegalStateException, SQLException
   {
      if ( null == m_rowBuffer )
         throw new IllegalStateException( "No row buffer available." );
      return m_rowBuffer.readRow();
   }

   /**
    * Get the index of the result page that was used to generate the
    * response. This is primarily used by the cacher.
    *
    * @return           the 0-based index; -1 if a result page was not used
    */
   int getResultPageIndex()
   {
      return m_resultPageIndex;
   }

   /**
    * Return a reference to the pager (if any) for the dataset.  This is used
    * to tune the Statement and ResultSet.
    *
    * @return the pager, may be <code>null</code>
    */
   public PSResultPager getPager()
   {
      if (m_ReqHandler instanceof PSQueryHandler)
      {
         PSQueryHandler qh = (PSQueryHandler) m_ReqHandler;
         return qh.getPager();
      }
      return null;
   }


   /**
    * Returns the first row number for the response, as specified by
    * the original request.
    *
    * @return The first row number; 0 if not specified
    */
   public int getFirstRowNumber()
   {
      int pagerFirstRow = 0;
      String firstIndex = m_Request.getParameter(
            PSResultSetXmlConverter.FIRST_QUERY_INDEX_PARAMETER_NAME);

      if (firstIndex != null)
      {
         try
         {
            pagerFirstRow = Integer.parseInt(firstIndex);
         }
         catch (NumberFormatException e)
         {
            // ignore
         }
      }
      return pagerFirstRow;
   }

   /**
    * Returns the calculated number of rows that this request may need to
    * obtain from the ResultSet.
    * @return maximum number of rows; -1 if not known or no limit.
    */
   public int getMaxRows()
   {
      int maxRows = -1;
      PSResultPager pager = getPager();
      if (pager == null)
         return -1;

      int maxPerPage = pager.getMaxRowsPerPage();
      if (maxPerPage <= 0)
         return -1;

      int maxForwardLinks = pager.getMaxPageLinks();
      if (maxForwardLinks <= 0)
         return -1;

      int firstRowNumber = getFirstRowNumber();

      maxRows = firstRowNumber + ((1 + maxForwardLinks ) * maxPerPage);

      return maxRows;
   }

   /**
    * Set the index of the result page that was used to generate the
    * response. This is primarily used by the cacher.
    *
    * @param   cacher   the cacher for this request
    */
   void setResultPageIndex(int index)
   {
      m_resultPageIndex = index;
   }


   protected void finalize()
      throws Throwable
   {
      release();  /* release the DB connections!!! */
      super.finalize();
   }

   /**
    * Set temp file resource information on this execution data.  This will
    * be used to allow the mime content converter to return the relevant
    * information instead of sending the response back directly, when needed.
    *
    * @param mc The mime content class containing the temp file information.
    *  Can be <code>null</code>.
    */
   public void setTempFileResource(PSMimeContentResult mc)
   {
      m_mimeContent = mc;
   }

   /**
    * Release the temp file resource (if one was set) by removing the
    * internal reference to the mime content result.
    */
   private void releaseTempFileResource()
   {
      if (m_mimeContent != null)
      {
         m_mimeContent.release();
         m_mimeContent = null;
      }
   }

   /**
    * Get the mime content object for the temp file associated with this
    * execution data.  This method may only be called once, after which the
    * internal mime content result will be cleared.  The caller then has
    * ownership of the mime content result.
    *
    * @return The mime content object, or <code>null</code> if one was not set.
    */
   public PSMimeContentResult getMimeContentResult()
   {
      PSMimeContentResult res = m_mimeContent;
      m_mimeContent = null;
      return res;
   }

   /**
    * Are we processing a null result set (one row of all <code>null</code>s)?
    *
    * @return  <code>true</code> if we are in process a fake result set
    *    containing all <code>null</code>s, <code>false</code> otherwise.
    */
   public boolean forceNullRowForNullResultSet()
   {
      return m_forceRowOnNullResultSet;
   }

   /**
    * Set whether or not we are forcing a row to be returned
    * on an empty result set.  This will cause all back-end data
    * extractors to return null immediately.
    *
    * @param forceNullRow  Force a row of <code>null</code>s for this
    *  empty result set?  Set to <code>true</code> to do so, or
    *  <code>false</code> for normal processing.
    */
   public void setForceRowOnNullResultSet(boolean forceNullRow)
   {
      m_forceRowOnNullResultSet = forceNullRow;
   }

   /**
    * Set a new input document.
    *
    * @param doc the new input document, might be <code>null</code>.
    */
   public void setInputDocument(Document doc)
   {
      m_Request.setInputDocument(doc);
   }

   /**
    * Get the current input document.
    *
    * @return the current input document, might be <code>null</code>.
    */
   public Document getInputDocument()
   {
      return m_Request.getInputDocument();
   }

   /**
    * Set the originating relationship.
    *
    * @param relationship the new originating relationship,
    *    may be <code>null</code>.
    */
   public void setOriginatingRelationship(PSRelationship relationship)
   {
      m_Request.setOriginatingRelationship(relationship);
   }

   /**
    * Get the originating relationship.
    *
    * @return the originating relationship, may be <code>null</code>.
    */
   public PSRelationship getOriginatingRelationship()
   {
      return m_Request.getOriginatingRelationship();
   }

   /**
    * Sets the object used to collect the data used for updating a row in the
    * backend.  See {@link #getTableChangeData()} for more info.
    *
    * @param tableChangeData The data object, may be <code>null</code> to
    * clear this value.
    */
   void setTableChangeData(PSTableChangeData tableChangeData)
   {
      m_tableChangeData = tableChangeData;
   }

   /**
    * Get the object used to collect the data used when updating a row in the
    * backend.  If one has been set, then it should be used to collect the
    * values bound to the specified columns.  These values will be used to
    * notify any {@link IPSTableChangeListener}s with a
    * {@link PSTableChangeEvent}.
    *
    * @return The data object, may be <code>null</code>.
    */
   PSTableChangeData getTableChangeData()
   {
      return m_tableChangeData;
   }

   /**
    * A container for the objects that are result set specific. Used to save/
    * restore result set context.
    */
   private class ResultSetContext
   {
      public ResultSetMetaData m_metaData;
      public Object [] m_rowData;
      public PSRowDataBuffer m_rowBuffer;
   }

   /**
    * Set method fot current relationship that can be processed by an effect.
    * @return current relationship, not <code>null</code> within an effect 
    * except during {@link com.percussion.relationship.IPSExecutionContext#
    * RS_PRE_CLONE clone context}. May be <code>null</code> otherwise.
    */
   public PSRelationship getCurrentRelationship()
   {
      return m_currentRelationship;
   }

   /**
    * Access method fot current relationship that can be processed by an effect.
    * @param currentRel must not be <code>null</code>.
    */
   public void setCurrentRelationship(PSRelationship currentRel)
   {
      if(currentRel == null)
         throw new IllegalArgumentException("currentRel must not be empty");
      m_currentRelationship = currentRel;
   }

   /**
    * Set method fot current relationship that can be processed by an effect.
    * @return current relationship, not <code>null</code> within an effect 
    * except during {@link com.percussion.relationship.IPSExecutionContext#
    * RS_PRE_CLONE clone context}. May be <code>null</code> otherwise.
    */
   public PSRelationship getSourceRelationship()
   {
      return m_sourceRelationship;
   }

   /**
    * Access method fot current relationship that can be processed by an effect.
    * @param currentRel must not be <code>null</code>.
    */
   public void setSourceRelationship(PSRelationship sourceRel)
   {
     
      m_sourceRelationship = sourceRel;
   }

   /**
    * Relationship that is being processed during current context. Set just 
    * before running effect.
    */
   private PSRelationship m_currentRelationship = null;

   /**
    * Original relationship that is being processed during current context. Set just 
    * before running effect. This can be used to get the source owner of an update.
    */
   private PSRelationship m_sourceRelationship = null;

   /**
    * The object used to collect the data used when updating a row in the
    * backend.  If one has been set, then it should be used to collect the
    * values bound to the specified columns. May be <code>null</code> if it is
    * not set.
    */
   private PSTableChangeData m_tableChangeData = null;

   /*
    * Are we returning all null column values for a null result set?
    *
    * <code>true</code> indicates that we are in this mode, <code>false</code>
    * indicates normal result set processing.
    */
   private boolean                     m_forceRowOnNullResultSet = false;

   /**
    * The storage location for the result set context object when a user saves
    * it. It is <code>null</code> until <code>saveResultSetContext</code> is
    * called. Then it is valid until <code>restoreResultSetContext</code> is
    * called, at which point it is cleared.
    */
   private ResultSetContext m_rsContext;

   /**
    * When <code>getNextResultSet</code> is called, it saves the generated row
    * buffer here for use by the <code>nextRow</code> method.
    */
   private PSRowDataBuffer m_rowBuffer;

   /**
    * The mime content associated with this execution data.  Can be
    * <code>null</code>.  Currently used by PSResultSetMimeConverter to
    * pass mime content back to internal request handlers.
    */
   private PSMimeContentResult         m_mimeContent    = null;

   private PSXmlTreeWalker             m_inputDocumentWalker = null;

   /**
    * The application handler that generated this execution data.  May be
    * <code>null</code>.  Assigned in the constructor and never modified after
    * that.
    */
   private PSApplicationHandler m_AppHandler = null;

   /** Not used by this class. */
   private IPSRequestHandler m_ReqHandler = null;

   private PSRequest                   m_Request         = null;
   private PSErrorHandler              m_ErrorHandler    = null;
   private PSLogHandler                m_LogHandler      = null;
   private List                        m_Connections     = null;
   private Stack                       m_ResultSetStack  = null;
   private List                        m_Statements      = null;
   private ResultSetMetaData           m_CurRSMetaData   = null;
   private Object[]                    m_CurRowData      = null;
   private int                         m_resultPageIndex = -1;

   
   /** 
    * List of connection detail that mirrors the {@link #m_Connections}
    * list.  Initialized by first call to 
    * {@link #addDbConnection(IPSConnectionInfo, long)}, never <code>null</code>
    * after that.
    */
   private List<PSConnectionDetail> m_connectionDetailList = null;

   static public final String    FIRST_QUERY_INDEX_PARAMETER_NAME = "psfirst";
}

