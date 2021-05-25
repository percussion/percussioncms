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

import com.percussion.data.PSSqlException;
import com.percussion.util.PSPreparedStatement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

/**
 * This class provides a framework for creating test classes for workflow
 * classes and methods. There is a mechanism for specifying test data via
 * command line arguments, as well as utility methods. Classes extending
 * <CODE>PSAbstractWorkflowTest</CODE> must implement {@link #ExecuteTest}
 * and should implement {@link #GetArgValues} and {@link #HelpMessage} if
 * command line arguments are supported.
 */

public abstract class PSAbstractWorkflowTest
{

   private static final Logger log = LogManager.getLogger(PSAbstractWorkflowTest.class);

   /**
    * This is the executive method for running a test. It gets a database
    * connection and calls methods to parse the command line arguments, run the
    * test, and print out any exception information.
    *
    */
   public void Test()
   {
      Connection connection = null;
      try
      {
         if (m_bNeedConnection)
         {
            try
            {
               connection = PSConnectionMgr.getDebugConnection();
            }
            catch(Exception e)
            {
               throw new
               PSWorkflowTestException("Error getting the connection ", e);
            }
         }

         /*
          * Parse the arguments. If help option was specified, a message was
          * printed, so return.
          */
         if (!ParseArguments())
         {
             return;
         }

         // Test the code
         ExecuteTest(connection);
      }
      catch (PSWorkflowTestException e)
      {

         System.err.println("Exception while testing ");
         Throwable throwable =  e.getThrowable();
         System.out.println("throwable class = " + throwable.getClass());
         if (null != throwable)
         {
            if ((java.sql.SQLException.class).isInstance(throwable))
            {
               SQLException sqlEx = (SQLException)throwable;

               System.out.println("chain of SQL exceptions");
               for (;
                    null !=  sqlEx;
                    sqlEx = sqlEx.getNextException())
               {
                  System.out.println("   " + PSSqlException.toString(sqlEx));
               }
            }
            else
            {
               System.out.println(throwable);

            }
            System.out.println("Stack Trace for original exception:");
            log.error(throwable.getMessage());
            log.debug(throwable.getMessage(), throwable);
         }
         System.out.println("Stack Trace for testing code:");
         log.error(e.getMessage());
         log.debug(e.getMessage(), throwable);
      }
      finally
      {
         try
         {
            if(null != connection)
            {
               PSConnectionMgr.releaseDebugConnection(connection);
            }
         }
         catch(SQLException sqe)
         {
         }
      }
   }

   /**
    * Parse any arguments to the command, class specific arguments are parsed
    * via the method {@link #GetArgValues}. A help argument is defined here.
    *
    * @return  <CODE>true</CODE> if the execution should continue,
    *           <CODE>false</CODE>  if help option was specified, and a message
    *            was printed.
    */

   public boolean ParseArguments()
   {
      // Parse the arguments if any
      if (null != m_sArgs)
      {
         for (int i = 0; i < m_sArgs.length; i++)
         {
            i = GetArgValues(i);

            if (m_sArgs[i].equals("-h") || m_sArgs[i].equals("-help"))
            {
               System.out.println(HelpMessage());
               return false;
            }
         } // End loop over args
      } // End if args null
      return true;
   }

   /* Methods that can be overridden to allow use of input arguments */

   /**
    * Parse the current input argument, set corresponding private class member,
    * reading an additional argument if necessary.
    *
    * @param i  index into the array of arguments.

    * @return   index into the array of arguments, which will be incremented
    *           if a flag requiring an argument is parsed.
    */
   public int GetArgValues(int i)
   {
      /*
       * Commented out line below shows how to set member variables using
       * arguments. Add documentation to method HelpMessage
       */
//        if (m_sArgs[i].equals("-w") || m_sArgs[i].equals("-workflowid"))
//        {
//           m_nWorkflowID =  m_sArgs[++i];
//        }
      return i;
   }

   /**
    * Produce a "help" string for the test class, indicating command line
    * arguments options.
    *
    * @return  "help" string for the test class
    */
   public String HelpMessage()
   {
      // commented out line below shows how to include additional options
      StringBuffer buf = new StringBuffer();
      buf.append("Options are:\n");
//    buf.append("   -w, -workflowid    workflow ID\n");
      buf.append("   -h, -help          help\n");
      return buf.toString();
   }

   /*  Utility Methods */


   /**
    * Produces a string giving date information down to the millisecond in the
    * format mm/dd/yyyy hh:mm:ss:milli.
    *
    * @param date  date to be turned into a string
    * @return      string giving date information down to the millisecond
    */
   public static String DateString(Date date)
   {
      if (null == date)
      {
         return "";
      }

      Calendar calendar = Calendar.getInstance();
      calendar.setTime(date);
      return DateString(calendar);
   }

   /**
    * Produces a string giving date information down to the millisecond in the
    * format mm/dd/yyyy hh:mm:ss:milli.
    *
    * @param calendar  calendar specifying date to be turned into a string
    * @return          string giving date information down to the millisecond
    */
   public static String DateString(Calendar calendar)
   {
      if (null == calendar)
      {
          return "";
      }
      StringBuffer buf = new StringBuffer();
      buf.append((calendar.get(calendar.MONTH) + 1)+ "/" );
      buf.append(calendar.get(calendar.DAY_OF_MONTH)  + "/");
      buf.append(calendar.get(calendar.YEAR) + " ");
      buf.append(calendar.get(calendar.HOUR) + ":");
      buf.append(calendar.get(calendar.MINUTE) + ":");
      buf.append(calendar.get(calendar.SECOND) + ":");
      buf.append(calendar.get(calendar.MILLISECOND));
      return buf.toString();
   }

   /**
    * Returns the next available identification number for a given key,
    * updating the value in the NEXTNUMBER table. Based on the class
    * <CODE>PSExitNextNumber</CODE>.
    *
    * @param sKey  name of table for which next number is sought
    * @param conn  database connection
    * @return      next available identification number for the table
    * @throws   PSWorkflowTestException if an error occurs
    */
   public static synchronized Integer getNextNumber(String sKey,
                                                    Connection conn)
      throws PSWorkflowTestException
   {
      Integer iResult;
      PreparedStatement stmt = null;
      ResultSet rs = null;
      int rowcount = 0;

      try
      {
         boolean was_autocommit = conn.getAutoCommit();
         conn.setAutoCommit(false);
         
         /*
          * Don't need to set transaction isolation, since its done in
          * getDebugConnection.
          */

         stmt = PSPreparedStatement.getPreparedStatement(conn, QUERY_REQUEST);
         stmt.setString(1, sKey);
         rs = stmt.executeQuery();
         if(false == rs.next())
         {
            iResult = new Integer(1);
         }

         else
         {
            iResult = new Integer(rs.getInt(1)+1);
         }

         rs.close();
         rs=null;
         stmt.close();
         stmt=null; //keep this!
         stmt = PSPreparedStatement.getPreparedStatement(conn, QUERY_UPDATE);
         stmt.setInt(1,iResult.intValue());
         stmt.setString(2,sKey);
         rowcount = stmt.executeUpdate();
         if(0 == rowcount)
         {
            stmt.close();
            stmt = null; //keep this!
            stmt = PSPreparedStatement.getPreparedStatement(conn, QUERY_INSERT);
            stmt.setString(1,sKey);
            stmt.setInt(2,iResult.intValue());
            stmt.execute();
         }
         conn.commit();
         conn.setAutoCommit(was_autocommit);
      }
      catch (Exception e)
      {
         throw new PSWorkflowTestException("getNextNumber()", e);
      }
      finally
      {
         if(null != rs)
         {
            try {rs.close();} catch (Throwable T) {
               log.error(T.getMessage());
               log.debug(T.getMessage(), T);
            };
         }

         if(null != stmt)
         {
            try {stmt.close();} catch (Throwable T) {log.error(T.getMessage());
               log.debug(T.getMessage(), T);
            };
         }

      }
      return iResult;
   }

   /* Constants used by method getNextNumber */

   private static final String QUERY_REQUEST   =
   "SELECT NEXTNR FROM NEXTNUMBER WHERE KEYNAME = ?";
   private static final String QUERY_UPDATE    =
   "UPDATE NEXTNUMBER SET NEXTNR = ? WHERE KEYNAME = ?";
   private static final String QUERY_INSERT    =
   "INSERT INTO NEXTNUMBER(KEYNAME, NEXTNR) VALUES (?, ?)";
   private static final String GLOBAL_KEY = "RXKEYGLOBAL";

   /* Abstract Method, must be implemented */

   /**
    * Executes the context, exit etcetera specific test code.
    *
    * @param connection database connection
    * @throws  PSWorkflowTestException if an error occurs
    */
   public abstract void ExecuteTest(Connection connection)
      throws PSWorkflowTestException;

   /* Member variables */

   /**
    * <CODE>true</CODE> if the test needs a database connection ,
    * else <CODE>false</CODE>.
    */
   protected boolean m_bNeedConnection = true;

   /** Array of arguments to the command */
   protected String[] m_sArgs = null;
}
