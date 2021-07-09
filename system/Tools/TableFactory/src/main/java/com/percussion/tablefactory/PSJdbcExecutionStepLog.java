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

package com.percussion.tablefactory;

import com.percussion.xml.PSXmlDocumentBuilder;

import java.sql.Connection;
import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class encapsulates the log of execution of each step of an execution
 * plan.
 */
public class PSJdbcExecutionStepLog
{
   /**
    * Returns the connection object in string form.
    *
    * @return the string representing the connection object, never
    * <code>null</code>, may be empty
    */
   public String getConnectionString()
   {
      return m_connString;
   }

   /**
    * Sets the connection string from the connection object
    *
    * @param conn Connection object used for the current transaction which
    * generated this result data, may be <code>null</code>
    */
   public void setConnectionString(Connection conn)
   {
      if (conn != null)
         m_connString = conn.toString();
   }

   /**
    * Returns the database server name.
    *
    * @return the database server name, never <code>null</null>, may be empty
    */
   public String getDBServer()
   {
      return m_dbServer;
   }

   /**
    * Sets the database server name.
    *
    * @param dbServer the database server name, may be <code>null</code> or
    * empty
    */
   public void setDBServer(String dbServer)
   {
      if (!((dbServer == null) || (dbServer.trim().length() == 0)))
         m_dbServer = dbServer;
   }

   /**
    * Returns the database name.
    *
    * @return the database name, never <code>null</null>, may be empty
    */
   public String getDatabaseName()
   {
      return m_dbName;
   }

   /**
    * Returns the database type.
    *
    * @return the database type, never <code>null</null>, may be empty
    */
   public String getDatabaseType()
   {
      return m_dbType;
   }

   /**
    * Sets the database properties.
    *
    * @param name the database name, may be <code>null</code> or empty
    * @param type the database type, may be <code>null</code> or empty
    */
   public void setDatabase(String name, String type)
   {
      if (!((name == null) || (name.trim().length() == 0)))
         m_dbName = name;

      if (!((type == null) || (type.trim().length() == 0)))
         m_dbType = type;
   }

   /**
    * Returns the table name.
    *
    * @return the table name, never <code>null</null>, may be empty
    */
   public String getTable()
   {
      return m_table;
   }

   /**
    * Sets the table name.
    *
    * @param table the table name, may be <code>null</code> or empty
    */
   public void setTable(String table)
   {
      if (!((table == null) || (table.trim().length() == 0)))
         m_table = table;
   }

   /**
    * Returns the row containing the primary key columns of the current step.
    *
    * @return the row containing the primary key columns of the current step,
    * may be <code>null</null>
    */
   public PSJdbcRowData getPrimaryKey()
   {
      return m_pkKeyRowData;
   }

   /**
    * Sets the row containing the primary key columns of the current step.
    *
    * @param pkKeyRowData the row containing the primary key columns,
    * may be <code>null</code>
    */

   public void setPrimaryKey(PSJdbcRowData pkKeyRowData)
   {
      if (pkKeyRowData != null)
         m_pkKeyRowData = pkKeyRowData;
   }

   /**
    * Returns the sql query associated with the current step.
    *
    * @return the sql query associated with the current step,
    * never <code>null</null>, may be empty
    */
   public String getSqlQuery()
   {
      return m_sqlQuery;
   }

   /**
    * Sets the sql query associated with the current step.
    *
    * @param sqlQuery the sql query associated with the current step,
    * may be <code>null</code> or empty
    */
   public void setSqlQuery(String sqlQuery)
   {
      if (!((sqlQuery == null) || (sqlQuery.trim().length() == 0)))
         m_sqlQuery = sqlQuery;
   }

   /**
    * Returns the number of rows updated as a result of execution of the
    * current step.
    *
    * @return number of rows updated as a result of execution of the
    * current step
    */
   public int getUpdateCount()
   {
      return m_updateCount;
   }

   /**
    * Sets the number of rows updated as a result of execution of the
    * current step.
    *
    * @param updateCount number of rows updated as a result of execution of
    * the current step
    */
   public void setUpdateCount(int updateCount)
   {
      m_updateCount = updateCount;
   }

   /**
    * Returns a <code>boolean</code> indicating the success or failure of the
    * execution of the current step.
    *
    * @return <code>true</code> if the current step was successfully executed,
    * <code>false</code> otherwise
    */
   public boolean isSuccess()
   {
      return m_success;
   }

   /**
    * Sets a <code>boolean</code> indicating the success or failure of the
    * execution of the current step.
    *
    * @param success a <code>boolean</code> indicating the success or failure
    * of the execution of the current step, <code>true</code> if successful,
    * <code>false</code> otherwise.
    */
   public void setSuccess(boolean success)
   {
      m_success = success;
   }

   /**
    * Returns the tablefactory error message associated with the
    * execution of the current step.
    *
    * @return the error message associated with the execution of
    * the current step, never <code>null</null>, may be empty
    */
   public String getErrorMessage()
   {
      return m_errorMsg;
   }

   /**
    * Sets the tablefactory error message associated with
    * the execution of the current step.
    *
    * @param errorMsg the error message associated with the execution
    * of the current step, may be <code>null</code> or empty
    */
   public void setErrorMessage(String errorMsg)
   {
      if (!((errorMsg == null) || (errorMsg.trim().length() == 0)))
         m_errorMsg = errorMsg;
   }

   /**
    * Returns the tablefactory message associated with the execution of
    * the current step.
    *
    * @return the message associated with the execution of the current step,
    * never <code>null</null>, may be empty
    */
   public String getMessage()
   {
      return m_msg;
   }

   /**
    * Sets the tablefactory message associated with the execution of
    * the current step.
    *
    * @param msg the message associated with the execution of the current step,
    * may be <code>null</code> or empty
    */
   public void setMessage(String msg)
   {
      if (!((msg == null) || (msg.trim().length() == 0)))
         m_msg = msg;
   }

   /**
    * Serializes this object's state to Xml.
    *
    * @param doc The document to use when creating elements.  May not be
    * <code>null</code>.
    *
    * @return The element containing this object's state,
    * never <code>null</code>.
    *
    * @throws IllegalArgumentException if doc is <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      // create the root element
      Element root = doc.createElement(NODE_NAME);

      PSXmlDocumentBuilder.addElement(doc, root,
         CONNECTION_NODE_NAME, m_connString);

      PSXmlDocumentBuilder.addElement(doc, root,
         DB_SERVER_NODE_NAME, m_dbServer);

      Element db = PSXmlDocumentBuilder.addEmptyElement(doc, root,
         DATABASE_NODE_NAME);
      PSXmlDocumentBuilder.addElement(doc, db,
         DATABASE_NAME_NODE_NAME, m_dbName);
      PSXmlDocumentBuilder.addElement(doc, db,
         DATABASE_TYPE_NODE_NAME, m_dbType);

      PSXmlDocumentBuilder.addElement(doc, root,
         TABLE_NODE_NAME, m_table);

      PSXmlDocumentBuilder.addElement(doc, root,
         QUERY_NODE_NAME, m_sqlQuery);

      if (m_pkKeyRowData != null)
      {
         Iterator pkeyList = m_pkKeyRowData.getColumns();
         if (pkeyList.hasNext())
         {
            Element pkey = PSXmlDocumentBuilder.addEmptyElement(doc, root,
               PRIMARY_KEY_NODE_NAME);

            while (pkeyList.hasNext())
            {
               PSJdbcColumnData colData = (PSJdbcColumnData)pkeyList.next();
               Element pkeyCol = PSXmlDocumentBuilder.addEmptyElement(doc, pkey,
                  PRIMARY_KEY_COL_NODE_NAME);
               PSXmlDocumentBuilder.addElement(doc, pkeyCol,
                  PRIMARY_KEY_COL_NAME_NODE_NAME, colData.getName());
               String colVal = colData.getValue();
               if (colVal == null)
                  colVal = "";
               PSXmlDocumentBuilder.addElement(doc, pkeyCol,
                  PRIMARY_KEY_COL_VALUE_NODE_NAME, colVal);
            }
         }
      }

      PSXmlDocumentBuilder.addElement(doc, root,
         UPDATE_COUNT_NODE_NAME, String.valueOf(m_updateCount));

      String successVal = SUCCESS_YES_VALUE;
      if (!m_success) successVal = SUCCESS_NO_VALUE;
      PSXmlDocumentBuilder.addElement(doc, root,
         SUCCESS_NODE_NAME, successVal);

      PSXmlDocumentBuilder.addElement(doc, root,
         MESSAGE_NODE_NAME, m_errorMsg);

      return root;
    }

   /**
    * Serializes this object's state to String.
    *
    * @return the string containing this object's state,
    * never <code>null</code> or empty
    *
    */
   public String toString()
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = toXml(doc);
      return PSXmlDocumentBuilder.toString(root);
    }

   /**
    * Constants for Xml Elements and Attibutes
    */
   public static final String NODE_NAME = "StepLogData";
   public static final String CONNECTION_NODE_NAME = "connection";
   public static final String DB_SERVER_NODE_NAME = "dbserver";
   public static final String DATABASE_NODE_NAME = "database";
   public static final String DATABASE_NAME_NODE_NAME = "name";
   public static final String DATABASE_TYPE_NODE_NAME = "type";
   public static final String TABLE_NODE_NAME = "table";
   public static final String QUERY_NODE_NAME = "query";
   public static final String PRIMARY_KEY_NODE_NAME = "primarykey";
   public static final String PRIMARY_KEY_COL_NODE_NAME = "column";
   public static final String PRIMARY_KEY_COL_NAME_NODE_NAME = "name";
   public static final String PRIMARY_KEY_COL_VALUE_NODE_NAME = "value";
   public static final String UPDATE_COUNT_NODE_NAME = "updatecount";
   public static final String SUCCESS_NODE_NAME = "successful";
   public static final String SUCCESS_YES_VALUE = "yes";
   public static final String SUCCESS_NO_VALUE = "no";
   public static final String MESSAGE_NODE_NAME = "message";

   /**
    * Message when the step is successfully executed.
    */
   public static final String SUCCESS_MSG = "Successful";

   /**
    * Message when the update step execution results in no rows being updated.
    */
   public static final String NO_ROWS_UPDATED_MSG = "No rows updated.";

   /**
    * stores the database connection in string form, never <code>null</code>,
    * may be empty
    */
   private String m_connString = "";

   /**
    * stores the database server name, never <code>null</code>,
    * may be empty
    */
   private String m_dbServer = "";

   /**
    * stores the database name, never <code>null</code>,
    * may be empty
    */
   private String m_dbName = "";

   /**
    * stores the database type, never <code>null</code>,
    * may be empty
    */
   private String m_dbType = "";

   /**
    * stores the table name, never <code>null</code>,
    * may be empty
    */
   private String m_table = "";

   /**
    * the row containing the primary key columns,
    * may be <code>null</code>
    */
   private PSJdbcRowData m_pkKeyRowData;

   /**
    * stores the current sql query, never <code>null</code>,
    * may be empty
    */
   private String m_sqlQuery = "";

   /**
    * stores the update count as a result of execution of the current step
    */
   private int m_updateCount = 0;

   /**
    * stores the result of execution of the current step, <code>true</code>
    * if the current step was successfully executed, else <code>false</code>
    */
   private boolean m_success = false;

   /**
    * stores the error message from the tablefactory,
    * never <code>null</code>, may be empty
    */
   private String m_errorMsg = "";

   /**
    * the message constructed from other variables in this class,
    * never <code>null</code>, may be empty
    */
   private String m_msg = "";

}
