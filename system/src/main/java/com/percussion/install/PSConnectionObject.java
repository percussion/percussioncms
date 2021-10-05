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
package com.percussion.install;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Utility class which manages the closing of connection related objects.  This
 * includes {@link Connection}, {@link Statement}, and {@link ResultSet}.
 * 
 * @author peterfrontiero
 */
public class PSConnectionObject
{
   /**
    * Create an uninitialized connection object.
    */
   public PSConnectionObject()
   {
      
   }
   
   /**
    * Create a connection object for a given connection.
    * 
    * @param conn The {@link Connection} object to be managed by this object,
    * may not be <code>null</code>.
    */
   public PSConnectionObject(Connection conn)
   {
      if (conn == null)
         throw new IllegalArgumentException("conn may not be null");
      
      m_conn = conn;
   }
   
   /**
    * Create a connection object for a given connection and statement.
    * 
    * @param conn The {@link Connection} object to be managed by this object,
    * may not be <code>null</code>.
    * @param stmt The {@link Statement} object to be managed by this object,
    * may not be <code>null</code>.
    */
   public PSConnectionObject(Connection conn, Statement stmt)
   {
      if (conn == null)
         throw new IllegalArgumentException("conn may not be null");
      
      if (stmt == null)
         throw new IllegalArgumentException("stmt may not be null");
      
      m_conn = conn;
      m_stmt = stmt;
   }
   
   /**
    * Create a connection object for a given connection, statement, and result
    * set.
    * 
    * @param conn The {@link Connection} object to be managed by this object,
    * may not be <code>null</code>.
    * @param stmt The {@link Statement} object to be managed by this object,
    * may not be <code>null</code>.
    * @param rs The {@link ResultSet} object to be managed by this object,
    * may not be <code>null</code>.
    */
   public PSConnectionObject(Connection conn, Statement stmt,
         ResultSet rs)
   {
      if (conn == null)
         throw new IllegalArgumentException("conn may not be null");
      
      if (stmt == null)
         throw new IllegalArgumentException("stmt may not be null");
      
      if (rs == null)
         throw new IllegalArgumentException("rs may not be null");
      
      m_conn = conn;
      m_stmt = stmt;
      m_rs = rs;
   }
   
   /**
    * Create a connection object for a given statement and result set.
    * 
    * @param stmt The {@link Statement} object to be managed by this object,
    * may not be <code>null</code>.
    * @param rs The {@link ResultSet} object to be managed by this object,
    * may not be <code>null</code>.
    */
   public PSConnectionObject(Statement stmt, ResultSet rs)
   {
      if (stmt == null)
         throw new IllegalArgumentException("stmt may not be null");
      
      if (rs == null)
         throw new IllegalArgumentException("rs may not be null");
      
      m_stmt = stmt;
      m_rs = rs;
   }
   
   /**
    * Set the result set to be managed by this object.
    * 
    * @param rs The result set, may not be <code>null</code>.
    */
   public void setResultSet(ResultSet rs)
   {
      if (rs == null)
         throw new IllegalArgumentException("rs may not be null");
      
      m_rs = rs;
   }
   
   /**
    * Set the statement to be managed by this object.
    * 
    * @param stmt The statement, may not be <code>null</code>.
    */
   public void setStatement(Statement stmt)
   {
      if (stmt == null)
         throw new IllegalArgumentException("stmt may not be null");
      
      m_stmt = stmt;
   }
   
   /**
    * Set the connection to be managed by this object.
    * 
    * @param conn The connection, may be <code>null</code>.
    */
   public void setConnection(Connection conn)
   {
      if (conn == null)
         throw new IllegalArgumentException("conn may not be null");
      
      m_conn = conn;
   }
   
   /**
    * Closes the result set object managed by this object if it has been
    * initialized.  The result set will be set to <code>null</code>.
    */
   public void closeResultSet()
   {
      try
      {
         if (m_rs != null)
         {
            m_rs.close();
            m_rs = null;
         }
      }
      catch (Exception e)
      {
      }
   }
   
   /**
    * Closes the statement object managed by this object if it has been
    * initialized.  The statement will be set to <code>null</code>.
    */
   public void closeStatement()
   {
      try
      {
         if (m_stmt != null)
         {
            m_stmt.close();
            m_stmt = null;
         }
      }
      catch (Exception e)
      {
      }
   }
   
   /**
    * Closes the connection object managed by this object if it has been
    * initialized.  The connection will be set to <code>null</code>.
    */
   public void closeConnection()
   {
      try
      {
         if (m_conn != null)
         {
            m_conn.close();
            m_conn = null;
         }
      }
      catch (Exception e)
      {
      }
   }
   
   /**
    * Closes the result set, statement, and connection objects managed by this
    * object if initialized.  The managed objects will be set to 
    * <code>null</code>.
    */
   public void close()
   {
      closeResultSet();
      closeStatement();
      closeConnection();
   }
   
   /**
    * The connection which may or may not be supplied during construction.
    */
   private Connection m_conn = null;
   
   /**
    * The statement which may or may not be supplied during construction.
    */
   private Statement m_stmt = null;
   
   /**
    * The result set which may or may not be supplied during construction.
    */
   private ResultSet m_rs = null;
}
