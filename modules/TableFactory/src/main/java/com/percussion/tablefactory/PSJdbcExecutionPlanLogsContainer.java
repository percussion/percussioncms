/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.tablefactory;

import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class contains a list of <code>PSJdbcExecutionPlanLog</code>
 * objects. It encapsulates the log of execution of multiple execution plans.
 */
public class PSJdbcExecutionPlanLogsContainer
{
   /**
    * Returns an iterator over the list of
    * <code>PSJdbcExecutionPlanLog</code> objects.
    *
    * @return an iterator over the list of
    * <code>PSJdbcExecutionPlanLog</code> objects, never <code>null</code>,
    * may be empty.
    */
   public Iterator<PSJdbcExecutionPlanLog> getPlanLogs()
   {
      return m_logDataList.iterator();
   }

   /**
    * Adds a <code>PSJdbcExecutionPlanLog</code> object to its internal list.
    *
    * @param planLogData the <code>PSJdbcExecutionPlanLog</code> object
    * containing the log of execution of a plan, never
    * <code>null</code>
    *
    * @throws IllegalArgumentException if planLogData is <code>null</code>
    */
   public void addPlanLogData(PSJdbcExecutionPlanLog planLogData)
   {
      if (planLogData == null)
         throw new IllegalArgumentException("planLogData may not be null");
      m_logDataList.add(planLogData);
   }

   /**
    * Removes all the elements from its internal list of
    * <code>PSJdbcExecutionPlanLog</code> objects.
    */
   public void clearPlanLogs()
   {
      m_logDataList.clear();
   }

   /**
    * Serializes this object's state to Xml.
    *
    * @param doc the document to use when creating elements, may not be
    * <code>null</code>.
    *
    * @return the element containing this object's state,
    * never <code>null</code>.
    *
    * @throws IllegalArgumentException if doc is <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      // create the root element
      Element  root = doc.createElement(NODE_NAME);
      Iterator<PSJdbcExecutionPlanLog> list = getPlanLogs();
      while (list.hasNext())
      {
         PSJdbcExecutionPlanLog planLogData =
                 list.next();
         root.appendChild(planLogData.toXml(doc));
      }

      return root;
    }

   /**
    * Serializes this object's state to String.
    *
    * @return the string containing this object's state,
    * never <code>null</code> or empty
    *
    */
   public synchronized String toString()
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = toXml(doc);
      return PSXmlDocumentBuilder.toString(root);
    }

   /**
    * Logs all execution plan logs to the supplied print stream. The provided
    * stream is only borrowed and will therefor not be closed.
    * 
    * @param ps the print stream to where all execution plan logs will be 
    *    written, not <code>null</code>.
    * @throws IllegalArgumentException if the provided print stream is 
    *    <code>null</code>.
    */
   public void write(PrintStream ps)
   {
      if (ps == null)
        throw new IllegalArgumentException("print stream cannot be null");
        
      ps.print(this);
   }
   
   /**
    * Writes the log data to the provided log file.
    *
    * @param logFile the file in which to log including path information, not 
    *    <code>null</code> or empty.
    * @throws IllegalArgumentException if the supplied log file is 
    *    <code>null</code>.
    * @throws PSJdbcTableFactoryException if anything goes wrong writing the
    *    log file.
    */
   public void write(String logFile) throws PSJdbcTableFactoryException
   {
      if ((logFile == null) || (logFile.trim().length() == 0))
         throw new IllegalArgumentException("logFile may not be null or empty");

      FileWriter writer = null;
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = toXml(doc);
      doc.appendChild(root);

      try
      {
         writer = new FileWriter(logFile);
         PSXmlDocumentBuilder.write(doc, writer, "UTF-8");
      }
      catch (Exception e)
      {
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.LOG_FILE_WRITE_ERROR);
      }
      finally
      {
         try
         {
            if (writer != null)
               writer.close();
         }
         catch (IOException ioex)
         {
            /* no-op */
         }
      }
   }

   /**
    * Analyzed all plan logs and determines the database action taken.
    * 
    * @return the database action performed, one of INSERT, UPDATE, DELETE or
    *    UNKNOWN.
    */
   public synchronized int getDbActionType()
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = toXml(doc);
      NodeList queries = root.getElementsByTagName(
         PSJdbcExecutionStepLog.QUERY_NODE_NAME);
      NodeList updateCounts = root.getElementsByTagName(
         PSJdbcExecutionStepLog.UPDATE_COUNT_NODE_NAME);
      
      int action = UNKNOWN;
      
      /* 
       * we use the first query with an update count greater than 0 to 
       * determine the db action
       */
      Node query = null;
      Node updateCount = null;
      int count = 0;
      for (int i=0; i<updateCounts.getLength(); i++)
      {
         query = queries.item(i);
         updateCount = updateCounts.item(i);
         Node updateCountData = updateCount.getFirstChild();
         if (updateCountData instanceof Text)
         {
            String updateCountString = ((Text) updateCountData).getData();
            count = Integer.parseInt(updateCountString);
            if (count > 0)
               break;
         }
      }
      if (count > 0 && query != null)
      {
         Node queryData = query.getFirstChild();
         if (queryData instanceof Text)
         {
            String queryString = ((Text) queryData).getData().toLowerCase();
            if (queryString.startsWith("insert"))
               action = INSERT;
            else if (queryString.startsWith("update"))
               action = UPDATE;
            else if (queryString.startsWith("delete"))
               action = DELETE;
         }
      }

      return action;      
   }
   
   /**
    * The name of this objects root Xml element.
    */
   public static final String NODE_NAME = "LogData";

   /**
    * Contains a list of <code>PSJdbcExecutionPlanLog</code> objects,
    * never <code>null</code>, may be empty
    */
   private List<PSJdbcExecutionPlanLog> m_logDataList = new ArrayList<>();

   /** The action value used for UNKNOWN database actions. */
   public static final int UNKNOWN = -1;
   
   /** The action value used for INSERT database actions. */
   public static final int INSERT = 0;
   
   /** The action value used for UPDATE datasbase actions. */
   public static final int UPDATE = 1;
   
   /** The action value used for DELETE datasbase actions. */
   public static final int DELETE = 2;
}

