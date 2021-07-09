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
package com.percussion.log;

import com.percussion.data.PSJoinFormatter;
import com.percussion.data.PSSqlException;
import com.percussion.server.PSConsole;
import com.percussion.util.PSSQLStatement;
import com.percussion.util.PSSqlHelper;
import com.percussion.utils.jdbc.PSConnectionDetail;
import com.percussion.utils.jdbc.PSConnectionHelper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import javax.naming.NamingException;

/**
 * The PSBackEndLogReader class implements reading log entries from
 * a back-end data store.
 *
 * @see        PSBackEndLogWriter
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSBackEndLogReader implements IPSLogReader
{

   /**
    * Construct a back-end log reader. This is given package access with the
    * intent that only the PSLogManager object will instantiate it.
    * @throws SQLException if the back-end table cannot be accessed
    * @throws NamingException If the default connection details cannot be 
    * obtained.
    */
   PSBackEndLogReader()
      throws SQLException, NamingException
   {

      try
      {
         m_connDetails = PSConnectionHelper.getConnectionDetail(null);
         
         PSConsole.printMsg( ms_subsystem, 
            "Back end log reader connecting with CMS Repository datasource");
         
         internalOpen();
      }
      catch (SQLWarning w)
      {
         PSConsole.printMsg(
            ms_subsystem, "SQL warning", new String[] { w.toString() } );
      }
      catch (SQLException e)
      {
         // don't throw if it's a simple network problem...instead set it up
         // so that isOpen() will return false and let the people upstairs
         // deal with it...
         if (m_connDetails != null && 
            PSSqlException.hasConnectionError(e, m_connDetails.getDriver()))
         {
            PSLogManager.printSqlException(ms_subsystem,
               "Back end log reader has unexpectedly gone down.", e);

            // this connection is dead, so close it
            close();
         }
         else
            throw e;
      }
   }

   /* **************  IPSLogReader Interface Implementation ************** */

   /**
    * Close the log reader. This should only be called when it is no longer
    * needed. Any subsequent use of this object will throw an exception.
    */
   public synchronized void close()
   {
      PSConsole.printMsg(
         ms_subsystem, "Back end log reader closing upon request.");

      if (m_con != null)
      {
         try
         {
            m_con.close();
         } catch (Exception e)
         {
            // do nothing
         }
         m_con = null;
      }
   }

   /**
    * @return true if the writer is open, false otherwise
    *
    */
   public synchronized boolean isOpen()
   {
      try
      {
         if (m_con != null)
            return !m_con.isClosed();
      }
      catch (Exception e)
      {}
      return false;
   }

   /**
    * Fail-safe re-opening. To be used by PSLogManager only
    */
   protected synchronized boolean internalOpen() throws SQLException
   {
      if (isOpen())
         return true;

      try
      {
         m_con = PSConnectionHelper.getDbConnection(null);
      }
      catch (NamingException e)
      {
         throw new SQLException(e.getLocalizedMessage());
      }
      
      /*
       *  Create a join formatter for this connection to determine
       *  whether the formatter returned uses ON clauses in the FROM
       *  clause so that we can correctly set up the join for reading
       *  log entries and retrieve appropriate prefix/suffix info for
       *  columns and tables associated with the join.
       *  Addresses Bug Id: Rx-00-10-0003
       */
      m_joinFormatter = PSJoinFormatter.getJoinFormatter(m_con);

      return isOpen();
   }

   /**
    * Read log messages using the specified filter.
    *
    * @param      filter                  the log message filter
    *
    * @exception  IllegalStateException   if close has already been called
    *                                     on this reader
    */
   public synchronized void read(IPSLogReaderFilter filter)
      throws IllegalStateException
   {
      if (!isOpen())
         throw new IllegalStateException("cannot read from a closed log reader");

      String strQuery = null; // log this on error (if not null)

      Date startTime   = filter.getStartTime();
      Date endTime     = filter.getEndTime();
      long startMillis, endMillis;

      if (startTime != null)
         startMillis = startTime.getTime();
      else
         startMillis = 0;

      if (endTime != null)
         endMillis   = endTime.getTime();
      else
         endMillis   = Long.MAX_VALUE;

      // number of minutes since epoch
      int startMinutes  = (int)(startMillis / 60000L);      // includes more

      // Reason to add 0.99: it looks like later when the program executes "Where"
      // query, minutes is used as the unit, which will lose infomation.
      // It is not sure that endMicros really compensates the lose.
      // At least through primary testing, adding 0.99 in the minutes level does
      // not hurt.
      int endMinutes = (int)(0.99 + endMillis / 60000L); // excludes max. 0.6 sec

      // number of microseconds since beginning of minute
      int startMicros = 1000 * (int)(startMillis % 60000L);

      // add 999 because of our increment possibility
      int endMicros   = 999 + (1000 * (int)(endMillis % 60000L));

      // We are doing a left outer join.
      int joinType = PSJoinFormatter.JOIN_TYPE_LEFT_OUTER;

      /* The end effect of all this manipulation is to produce a query like this:
       * SELECT L.log_appl, L.log_type, D.log_id_high, D.log_id_low, D.log_seq,
       * D.log_subseq, D.log_subt, D.log_data from pslog L, pslogdat D
       * WHERE L.log_id_high = D.log_id_high AND L.log_id_low = D.log_id_low
       * AND   (
       *    (D.log_id_high = 15292727 AND D.log_id_low >= 51811000)
       *    OR D.log_id_high > 15292727
       *    )
       * AND   (
       *       (D.log_id_high = 15292728 AND D.log_id_low <= 21964999)
       *       OR D.log_id_high < 15292728
       *    )
       * ORDER BY D.log_id_high, D.log_id_low, D.log_seq DESC, D.log_subseq DESC
       */

      String andClause = " WHERE ";

      StringBuffer queryString = new StringBuffer(100);
      queryString.append( "SELECT L." ).append( PSLogDatabase.COL_LOG_APPL );
      queryString.append( ", L." ).append( PSLogDatabase.COL_LOG_TYPE );
      queryString.append( ", D." ).append( PSLogDatabase.COL_LOG_ID_HIGH );
      queryString.append( ", D." ).append( PSLogDatabase.COL_LOG_ID_LOW );
      queryString.append( "," );

      queryString.append( " D." ).append( PSLogDatabase.COL_LOG_SEQ );
      queryString.append( ", D." ).append( PSLogDatabase.COL_LOG_SUBSEQ );
      queryString.append( ", D." ).append( PSLogDatabase.COL_LOG_SUBT );
      queryString.append( ", D." ).append( PSLogDatabase.COL_LOG_DATA );
      queryString.append( " from " );

      queryString.append(
         m_joinFormatter.getLeftTablePrefix(joinType) +
         PSSqlHelper.qualifyTableName(
            PSLogDatabase.TABLE_PSLOG, m_connDetails.getDatabase(), 
            m_connDetails.getOrigin(), m_connDetails.getDriver()) + " L" +
         m_joinFormatter.getLeftTableSuffix(joinType) +
         m_joinFormatter.getRightTablePrefix(joinType) +
         PSSqlHelper.qualifyTableName(
            PSLogDatabase.TABLE_PSLOGDAT, m_connDetails.getDatabase(), 
            m_connDetails.getOrigin(), m_connDetails.getDriver()) + " D" +
         m_joinFormatter.getRightTableSuffix(joinType) + " ");

      String highIdJoin =
         m_joinFormatter.getLeftColumnPrefix(joinType) +
         "L." + PSLogDatabase.COL_LOG_ID_HIGH +
         m_joinFormatter.getLeftColumnSuffix(joinType) +
         "=" +
         m_joinFormatter.getRightColumnPrefix(joinType) +
         "D." + PSLogDatabase.COL_LOG_ID_HIGH +
         m_joinFormatter.getRightColumnSuffix(joinType);

      String lowIdJoin =
         m_joinFormatter.getLeftColumnPrefix(joinType) +
         "L." + PSLogDatabase.COL_LOG_ID_LOW +
         m_joinFormatter.getLeftColumnSuffix(joinType) +
         "=" +
         m_joinFormatter.getRightColumnPrefix(joinType) +
         "D." + PSLogDatabase.COL_LOG_ID_LOW +
         m_joinFormatter.getRightColumnSuffix(joinType);

      if (m_joinFormatter.usesOnClauseInFrom())
         queryString.append(
         " ON " + highIdJoin + " AND " + lowIdJoin);
      else
      {
         queryString.append(andClause);
         andClause = " AND ";
         queryString.append(highIdJoin + andClause + lowIdJoin);
      }

      /* calculating seconds/micros in the WHERE clause is a bit complex
       * and is quite a performance problem. Instead, we'll grab
       * everything in the minute range and weed out the seconds/micros
       * in the processing loop
       */
      if ((startTime != null) && (endTime != null))
      {
         queryString.append(andClause);
         queryString.append("D." + PSLogDatabase.COL_LOG_ID_HIGH + " BETWEEN ");
         queryString.append(startMinutes);
         queryString.append(" AND ");
         queryString.append(endMinutes);
         andClause = " AND ";
      }
      else if (startTime != null)
      {
         queryString.append(andClause);
         queryString.append("D." + PSLogDatabase.COL_LOG_ID_HIGH + " >= ");
         queryString.append(startMinutes);
         andClause = " AND ";
      }
      else if (endTime != null)
      {
         queryString.append(andClause);
         queryString.append("D." + PSLogDatabase.COL_LOG_ID_HIGH + " <= ");
         queryString.append(endMinutes);
         andClause = " AND ";
      }

      int[] applicationIds = filter.getApplicationIds();
      if ((applicationIds != null) && (applicationIds.length != 0))
      {
         queryString.append(andClause);
         queryString.append("(");
         for (int i = 0; i < applicationIds.length; i++)
         {
            if (i != 0)
               queryString.append(" OR ");
            queryString.append("L." + PSLogDatabase.COL_LOG_APPL + " = ");
            queryString.append(applicationIds[i]);
         }
         queryString.append(")");
         andClause = "AND";
      }

      int[] entryTypes = filter.getEntryTypes();
      if ((entryTypes != null) && (entryTypes.length != 0))
      {
         queryString.append(andClause);
         queryString.append("(");
         for (int i = 0; i < entryTypes.length; i++)
         {
            if (i != 0)
               queryString.append(" OR ");
            queryString.append("L." + PSLogDatabase.COL_LOG_TYPE + " = ");
            queryString.append(entryTypes[i]);
         }
         queryString.append(")");
         andClause = "AND";
      }

      queryString.append(
         " ORDER BY D." + PSLogDatabase.COL_LOG_ID_HIGH +
         ", D." + PSLogDatabase.COL_LOG_ID_LOW +
         ", D." + PSLogDatabase.COL_LOG_SEQ +
         ", D." + PSLogDatabase.COL_LOG_SUBSEQ);

      try
      {

//       PSConsole.printMsg(
//          ms_subsystem, "Read request using query string:",
//          new String[] { queryString.toString() });

         Statement stmt = null;
         ResultSet rs = null;
         try
         {
            try
            {
               stmt = PSSQLStatement.getStatement(m_con);
            } catch (SQLException e)
            {
               // maybe the connection was stale, try again with a new connection
               close();
               internalOpen();
               stmt = PSSQLStatement.getStatement(m_con);
            }
            strQuery = queryString.toString();
            rs = stmt.executeQuery(strQuery);

            PSConsole.printMsg(
               ms_subsystem, "Finished query.");

            int minutesSinceEpoch = -1, lastMinutesSinceEpoch = -1;
            int microsSinceMinute = -1, lastMicrosSinceMinute = -1;
            int sequence = -1, lastSequence = -1;
            int logType = -1, lastType = -1;
            int subType = -1, lastSubType = -1;
            int logAppl = -1, lastAppl = -1;
            String logData = "";

            ArrayList subMessages = new ArrayList();
            StringBuffer dataBuffer = new StringBuffer();
            int numRows = 0;
            boolean doWrite = false;

            for (boolean haveData = true; haveData; numRows++)
            {
               haveData = rs.next();
               if (haveData)
               {
                  // do we need to write the prior entry?
                  logAppl = rs.getInt(1);
                  logType = rs.getInt(2);
                  minutesSinceEpoch = rs.getInt(3);
                  microsSinceMinute = rs.getInt(4);
                  sequence = rs.getInt(5);
                  subType = rs.getInt(7);
                  logData = rs.getString(8);
                  numRows++;

                  // See Bug report: Rx-99-10-0163
                  // if we don't set doWrite=true here, then the first data in the log
                  // will not be read. That's because currently doWrite=false, and after this
                  // block, the program will execute "if (doWrite)"
                  doWrite = true; // make sure the first data will be read from the log
               }
               else if (numRows == 0)  // no rows match?
                  break;
               else {
                  // clear these so we can see if we need to store the
                  // last entry
                  lastMinutesSinceEpoch = 0;
                  lastMicrosSinceMinute = 0;
               }

               // build entire message which is keyed on the minutes/micros
               if ((minutesSinceEpoch == lastMinutesSinceEpoch) &&
                  (microsSinceMinute == lastMicrosSinceMinute))
               {
                  if (sequence != lastSequence)
                  {
                     // add completed submessage to message
                     subMessages.add(new PSLogSubMessage(
                        lastSubType, dataBuffer.toString()));
                     dataBuffer.setLength(0);

                     // update these, which are our sub-message markers
                     lastSubType = subType;
                     lastSequence = sequence;
                  }

                  // concatenate entire submessage
                  dataBuffer.append(logData);
               }
               else {
                  // do we need to save the last message?
                  if (doWrite) {
                     // compute the msec time for the message
                     long msgMsecSinceEpoch = (long)
                        (microsSinceMinute / 1000L + minutesSinceEpoch * 60000L);

                     java.util.Date newDate = new java.util.Date(msgMsecSinceEpoch);

                     subMessages.add(new PSLogSubMessage(
                        lastSubType, dataBuffer.toString()));
                     dataBuffer.setLength(0);

                     PSLogSubMessage[] messages = new PSLogSubMessage[subMessages.size()];
                     subMessages.toArray(messages);
                     subMessages.clear();

                     filter.processMessage(
                        new PSLogEntry(lastType, lastAppl, newDate, messages), true);

                     doWrite = false;
                  }

                  /* weed out the border line entries on our start/end time
                  * based on seconds/micros
                  */
                  if ((startTime != null) &&
                     (startMinutes == minutesSinceEpoch) &&
                     (microsSinceMinute <= startMicros))
                  {
                     continue;   // skip it as it's too old
                  }
                  else if ((endTime != null) &&
                     (endMinutes == minutesSinceEpoch) &&
                     (microsSinceMinute > endMicros))
                  {
                     continue;   // skip it as it's too new
                  }

                  // reset all the "last" pointer for the next loop
                  lastMinutesSinceEpoch = minutesSinceEpoch;
                  lastMicrosSinceMinute = microsSinceMinute;
                  lastSequence = sequence;
                  lastType = logType;
                  lastSubType = subType;
                  lastAppl = logAppl;
                  dataBuffer.append(logData);
                  doWrite = true;
               }
            }
         } finally { // statement try
            if (rs != null)
               try { rs.close(); } catch (Exception e) { /* ignore */ }

            if (stmt != null)
               try { stmt.close(); } catch (Exception e) { /* ignore */ }
         }
      } // end try
      catch (SQLWarning w)
      {
         PSConsole.printMsg(ms_subsystem,
            "SQL warning", new String[] { w.toString() } );
      }
      catch (SQLException e)
      {
         // don't throw if it's a simple network problem...instead set it up
         // so that isOpen() will return false and let the people upstairs
         // deal with it...
         if (e.getSQLState().startsWith("08"))
         {
            // this connection is dead, so close it
            close();
         }
         PSLogManager.printSqlException(ms_subsystem, "SQL Error", e);
      }
      filter.processMessage(null, true);
   }


   /**
    * The connection detail, initialized during construction, only 
    * <code>null</code> after if there was an error obtaining the detail.
    */
   private PSConnectionDetail m_connDetails = null;

   /**
    * The JDBC connection to the database.  Created in the
    * <code>internalOpen</code> method and kept open and reused until the log
    * reader is closed.
    */
   private Connection m_con = null;

   /**
    * The join formatter associated with the back end, since
    * we use a left outer join for the log query.
    */
   private PSJoinFormatter m_joinFormatter;

   private static final String ms_subsystem = "LogReader";
}
