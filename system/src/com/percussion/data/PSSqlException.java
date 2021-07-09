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

package com.percussion.data;

import com.percussion.error.PSException;
import com.percussion.util.PSSqlHelper;
import com.percussion.utils.jdbc.IPSConnectionInfo;
import com.percussion.utils.jdbc.PSConnectionDetail;
import com.percussion.utils.jdbc.PSConnectionHelper;

import java.sql.SQLException;
import java.util.Locale;

import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;


/**
 * The PSSqlException class extends the JDBC SQLException class,
 * additionally implementing our exception interface.
 *
 * @see         com.percussion.error.PSException
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSSqlException extends java.sql.SQLException
   implements com.percussion.error.IPSException
{
   /**
    * Construct an exception for messages taking only a single argument.
    *
    * @param   msgCode         the error string to load
    *
    * @param   singleArg      the argument to use as the sole argument in
    * the error message
    */
   public PSSqlException(int msgCode, Object singleArg, String sqlState)
   {
      this(msgCode, new Object[] { singleArg }, sqlState);
   }

   /**
    * Construct an exception for messages taking an array of
    * arguments. Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    *
    * @param   msgCode         the error string to load
    *
    * @param   arrayArgs      the array of arguments to use as the arguments
    * in the error message
    */
   public PSSqlException(int msgCode, Object[] arrayArgs, String sqlState)
   {
      super("", sqlState, msgCode);
      m_exception = new PSException(msgCode, arrayArgs);
   }

   /**
    * Construct an exception for messages taking no arguments.
    *
    * @param   msgCode         the error string to load
    */
   public PSSqlException(int msgCode, String sqlState)
   {
      this(msgCode, null, sqlState);
   }

   /**
    * Returns the localized detail message of this exception.
    *
    * @param   locale      the locale to generate the message in
    *
    * @return               the localized detail message
    */
   public java.lang.String getLocalizedMessage(java.util.Locale locale)
   {
      return m_exception.getLocalizedMessage(locale);
   }

   /**
    * Returns the localized detail message of this exception in the
    * default locale for this system.
    *
    * @return               the localized detail message
    */
   public java.lang.String getLocalizedMessage()
   {
      return m_exception.getLocalizedMessage(Locale.getDefault());
   }

   /**
    * Returns the detail message of this exception.
    *
    * @return               the detail message
    */
   public java.lang.String getMessage()
   {
      return m_exception.getLocalizedMessage(Locale.getDefault());
   }

   /**
    * Returns a description of this exception. The format used is
    * "ExceptionClass: ExceptionMessage"
    *
    * @return               the description
    */
   public java.lang.String toString()
   {
      return this.getClass().getName() + ": " +
         m_exception.getLocalizedMessage(Locale.getDefault());
   }

   /**
    * Get the parsing error code associated with this exception.
    *
    * @return   the error code
    */
   public int getErrorCode()
   {
      return m_exception.getErrorCode();
   }

   /**
    * Get the parsing error arguments associated with this exception.
    *
    * @return   the error arguments
    */
   public Object[] getErrorArguments()
   {
      return m_exception.getErrorArguments();
   }

   /**
    * Set the arguments for this exception.
    *
    * @param   msgCode         the error string to load
    *
    * @param   errorArg         the argument to use as the sole argument in
    * the error message
    */
   public void setArgs(int msgCode, Object errorArg)
   {
      m_exception.setArgs(msgCode, new Object[] { errorArg } );
   }

   /**
    * Set the arguments for this exception.
    *
    * @param   msgCode         the error string to load
    *
    * @param   errorArgs      the array of arguments to use as the arguments
    *  in the error message
    */
   public void setArgs(int msgCode, Object[] errorArgs)
   {
      m_exception.setArgs(msgCode, errorArgs);
   }

   /**
    * Is this exception caused by a feature not being supported? There is one
    * SQL-92 class of error set in the SQL state (0A). ODBC also defines its own
    * SQL states which are IM001 and S1C00.
    * 
    * @param e the SQL exception to check, may not be <code>null</code>.
    * @param driverName The name of the jdbc driver, may not be 
    * <code>null</code> or empty.
    * 
    * @return <code>true</code> if this exception is due to the feature not
    * being supported
    */
   public static boolean isFeatureNotSupported(SQLException e, 
      String driverName)
   {
      if (e == null)
         throw new IllegalArgumentException("e may not be null");
      
      if (StringUtils.isEmpty(driverName))
         throw new IllegalArgumentException(
            "driverName may not be null or empty");
      
      if (PSSqlHelper.isOracle(driverName))
      {
         int code = e.getErrorCode();
         return (code == 17023); // Unsupported feature
      }

      // dbreslau: use of getSQLState() here is questionable, since I suspect
      // that the error code is filled in more reliably than the SQLState is.
      // Also, the error codes checked for here are specific to one
      // database, and I'm not even sure which one.  (DB/2 on AS/400?)

      final String sqlState = e.getSQLState();
      if (sqlState == null)
         return false;

      // if schemas are not supported, this is not a problem
      return (sqlState.startsWith("0A") || sqlState.equals("S1C00") ||
         sqlState.equals("IM001"))   ;
   }

   /**
    * Is this exception, or any of its chained exceptions, caused by a feature
    * not being supported? There is one SQL-92 class of error set in the SQL
    * state (0A). ODBC also defines its own SQL states which are IM001 and
    * S1C00.
    * 
    * @author chadloder
    * 
    * @param e the SQL Exception to be tested, may not be <code>null</code>.
    * @param driverName the name of the driver of the connection on which the
    * exception was thrown; must not be <code>null</code> or empty.
    * 
    * @return <code>true</code> if the cause was due to a feature not supported,
    * <code>false</code> otherwise.
    */
   public static boolean hasFeatureNotSupported(SQLException e, 
      String driverName)
   {
      if (e == null)
         throw new IllegalArgumentException("e may not be null");
      
      for (SQLException cur = e; cur != null; cur = cur.getNextException())
      {
         if (isFeatureNotSupported(cur, driverName))
            return true;
      }

      return false;
   }

   /**
    * Version of {@link #hasFeatureNotSupported(SQLException, String)} that 
    * takes a connection info object used to determine the driver type.
    * 
    * @param e The exception to check, may not be <code>null</code>.
    * @param connInfo The connection info, may be <code>null</code> to indicate
    * the default connection was used.
    * 
    * @return <code>true</code> if the cause was due to a feature not supported,
    * <code>false</code> if not or if there was an error getting the driver
    * type from the connection info.
    */
   public static boolean hasFeatureNotSupported(SQLException e, 
      IPSConnectionInfo connInfo)
   {
      // false result by default if we have an error here so original exception
      // is dealt with
      boolean result = false;
      try
      {
         PSConnectionDetail detail = PSConnectionHelper.getConnectionDetail(
            connInfo);
         result = hasFeatureNotSupported(e, detail.getDriver());
      }
      catch (NamingException ne)
      {
      }
      catch (SQLException se)
      {
      }
      
      return result;
   }
   
   /**
    * Returns true if the given exception indicates that the connection
    * is (or is probably) not in a useable state.
    *
    * @author   chadloder
    *
    * @version 1.3 1999/07/27
    *
    * @param   e the SQL Exception to be tested
    * @param   driverName the name of the driver of the connection on which
    * the exception was thrown; must not be <code>null</code> or empty.
    *
    * @return   <code>true</code> if the error is <em>definitely</em>
    * a connection error.  The test is not exhaustive; <code>false</code>
    * does <em>not</em> necessarily indicate absence of a connection failure.
    */
   private static boolean isConnectionError(SQLException e, String driverName)
   {
      if (PSSqlHelper.isOracle(driverName))
      {
         int code = e.getErrorCode();
         return (
               code == 17002 // IO Exception
               || code == 17027 //  Stream has already been closed
               || code == 17008 // Closed Connection
               || code == 1089  // immediate shutdown in progress
               );
      }

      // dbreslau: use of getSQLState() here is questionable, since it's
      // supposed to be used only for server-side errors, not connection
      // errors.  Also, the error code checked for here is specific to one
      // database, and I'm not even sure which one.  (DB/2 on AS/400?)
      final String str = e.getSQLState();
      return (str != null && str.startsWith("08"));
   }

   /**
    * Returns true if the given exception, or any of its chained
    * exceptions, is a connection error.
    *
    * @author   chadloder
    *
    * @version 1.5 1999/09/09
    *
    *
    * @param   e the SQL Exception to be tested
    * @param   driverName the name of the driver of the connection on which
    * the exception was thrown; must not be <code>null</code> or empty.
    *
    * @return   <code>true</code> if the connection <em>definitely</em>
    * has a connection error.  The test is not exhaustive; <code>false</code>
    * does <em>not</em> necessarily indicate lack of a connection failure.
    */
   public static boolean hasConnectionError(SQLException e, String driverName)
   {
      for (SQLException cur = e; cur != null; cur = cur.getNextException())
      {
         if (isConnectionError(cur, driverName))
            return true;
      }

      return false;
   }

   /**
    * Returns true if the given exception indicates that the server is
    * down.
    *
    * @param   e the SQL Exception to be tested
    * @param   driverName the name of the driver of the connection on which
    * the exception was thrown; must not be <code>null</code> or empty.
    *
    * @return   <code>true</code> if the server is <em>definitely</em>
    * down.  The test is not exhaustive; <code>false</code>
    * does <em>not</em> necessarily indicate that the server is up.
    */
   private static boolean isServerDownError(SQLException e, String driverName)
   {
      if (PSSqlHelper.isOracle(driverName))
      {
         // Can't tell if server is down; use hasConnectionError instead.
         return false;
      }

      /* We will assume most connection errors are that the server is down.
       * Error 08004, however, is when the server rejects the connection,
       * such as when invalid credentials are used. In this case, the
       * server is not actually down.
       */
      // dbreslau:  The error code checked for here is specific to one
      // database, and I'm not even sure which one.  (DB/2 on AS/400?)
      final String str = e.getSQLState();
      return (str != null && str.startsWith("08") && !str.endsWith("004"));
   }

   /**
    * Returns true if the given exception, or any of its chained
    * exceptions, is a server down error. This is a special class of
    * connection errors.
    *
    * @param   e the SQL Exception to be tested
    * @param   driverName the name of the driver of the connection on which
    * the exception was thrown; must not be <code>null</code> or empty.
    *
    * @return   <code>true</code> if the server is <em>definitely</em>
    * down.  The test is not exhaustive; <code>false</code>
    * does <em>not</em> necessarily indicate that the server is up.

    */
   public static boolean hasServerDownError(SQLException e, String driverName)
   {

      if (PSSqlHelper.isOracle(driverName))
      {
         // Can't tell if server is down; use hasConnectionError instead.
         return false;
      }

      for (SQLException cur = e; cur != null; cur = cur.getNextException())
      {
         if (isServerDownError(cur, driverName))
            return true;
      }

      return false;
   }

   public static String toString(SQLException e)
   {
      StringBuffer buf = new StringBuffer();
      if (null != e)
      {
         buf.append(e.getClass().getName());
         buf.append(": ");
         buf.append( getFormattedExceptionText(e));
      }
      return buf.toString();
   }

   /**
    * Returns a formatted string containing the test of all of the exceptions
    * contained in the supplied SQLException.
    * <p>There seems to be a bug in the Sprinta driver. We get an exception
    * for Primary key constraint violation, which has a sql warning as the
    * next exception (warning). But this next warning has a circular
    * reference to itself in the next link. So we check for this problem and
    * limit the max errors we will process.
    *
    * @param e The exception to process. If <code>null</code>, an empty
    *    string is returned.
    *
    * @return The string, never <code>null</code>, may be empty.
    */
   public static String getFormattedExceptionText(SQLException e)
   {
      if ( null == e )
         return "";

      StringBuffer errorText   = new StringBuffer();

      int errNo = 1;
      final int maxErrors = 20;
      for ( ; e != null && errNo <= maxErrors; )
      {
         errorText.append( "[" );
         errorText.append( errNo );
         errorText.append( "] " );
         errorText.append( e.getSQLState());
         errorText.append( ": " );
         errorText.append( e.getMessage());
         errorText.append( " " );
         SQLException tmp = e.getNextException();
         if ( e == tmp )
            break;
         else
            e = tmp;
         errNo++;
      }
      if ( errNo == maxErrors + 1 )
      {
         errorText.append( "[Maximum # of error messages (" );
         errorText.append( maxErrors );
         errorText.append(  ") exceeded. Rest truncated]" );
      }

      return errorText.toString();
   }

   protected PSException   m_exception;
}

