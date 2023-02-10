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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class contains a list of <code>PSJdbcExecutionStepLog</code>
 * objects which encapsulate the result of execution of each step
 * of an execution plan.
 */
public class PSJdbcExecutionPlanLog
{
   /**
    * Returns an iterator over the list of
    * <code>PSJdbcExecutionStepLog</code> objects.
    *
    * @return an iterator over the list of
    * <code>PSJdbcExecutionStepLog</code> objects, never <code>null</code>,
    * may be empty.
    */
   public Iterator<PSJdbcExecutionStepLog> getStepLogs()
   {
      return m_logDataList.iterator();
   }

   /**
    * Adds a <code>PSJdbcExecutionStepLog</code> object to its internal list.
    *
    * @param stepLogData the <code>PSJdbcExecutionStepLog</code> object
    * containing the result of execution of a step, never
    * <code>null</code>
    *
    * @throws IllegalArgumentException if stepLogData is <code>null</code>
    */
   public void addStepLogData(PSJdbcExecutionStepLog stepLogData)
   {
      if (stepLogData == null)
         throw new IllegalArgumentException("stepLogData may not be null");
      m_logDataList.add(stepLogData);
   }

   /**
    * Removes all the elements from its internal list of
    * <code>PSJdbcExecutionStepLog</code> objects.
    */
   public void clearStepLogs()
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
      Element   root = doc.createElement(NODE_NAME);
      Iterator<PSJdbcExecutionStepLog> list = getStepLogs();
      while (list.hasNext())
      {
         PSJdbcExecutionStepLog stepLogData =
                 list.next();
         root.appendChild(stepLogData.toXml(doc));
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
   public String toString()
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = toXml(doc);
      return PSXmlDocumentBuilder.toString(root);
    }

   /**
    * The name of this objects root Xml element.
    */
   public static final String NODE_NAME = "PlanLogData";

   /**
    * Contains a list of <code>PSJdbcExecutionStepLog</code> objects,
    * never <code>null</code>, may be empty
    */
   private List<PSJdbcExecutionStepLog> m_logDataList = new ArrayList<>();

}

