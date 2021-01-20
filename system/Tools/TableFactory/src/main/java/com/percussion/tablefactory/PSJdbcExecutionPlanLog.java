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

package com.percussion.tablefactory;

import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
   public Iterator getStepLogs()
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
      Iterator list = getStepLogs();
      while (list.hasNext())
      {
         PSJdbcExecutionStepLog stepLogData =
            (PSJdbcExecutionStepLog)list.next();
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
   private List m_logDataList = new ArrayList();

}

