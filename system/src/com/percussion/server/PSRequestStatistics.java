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

package com.percussion.server;

import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * This class is used to store the statistics for a request being
 * processed by the server.
 *
 * @see         com.percussion.server.PSRequest#getStatistics
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSRequestStatistics
{
   /**
    * Construct a request statistics object with the specified time.
    *
    * @param   startTime      the time/date the event was received
    */
   public PSRequestStatistics(Date startTime)
   {
      super();
      m_startTime = startTime;
   }

   /**
    * Construct a request statistics object using the current time as the
    * time the event was received.
    */
   public PSRequestStatistics()
   {
      super();
      m_startTime = new Date();
   }

   /**
    * Increment the number of database requests that were handled from
    * the cache.
    */
   public void incrementCacheHits()
   {
      m_cacheHits++;
   }

   /**
    * Get the number of database requests which could not be handled
    * from the cache.
    *
    * @return      the number of cache hits
    */
   public int getCacheHits()
   {
      return m_cacheHits;
   }

   /**
    * Increment the number of database requests which could not be handled
    * from the cache.
    */
   public void incrementCacheMisses()
   {
      m_cacheMisses++;
   }

   /**
    * Get the number of database requests which could not be handled
    * from the cache.
    *
    * @return      the number of cache misses
    */
   public int getCacheMisses()
   {
      return m_cacheMisses;
   }

   /**
    * Increment the number of rows selected by one for this request.
    * This is the number of virtual rows, mapping to XML objects.
    * If one row is selected from table A and one row from table B,
    * which are joined to create one result row, only one row selected
    * should be set.
    */
   public void incrementRowsSelected()
   {
      m_rowsSelected++;
   }

   /**
    * Increment the number of rows selected by the specified 
    * number for this request.
    * This is the number of virtual rows, mapping to XML objects.
    * If one row is selected from table A and one row from table B,
    * which are joined to create one result row, only one row selected
    * should be set.
    *
    * @param   rows      the number of rows to be added to the counter
    */
   public void incrementRowsSelected(int rows)
   {
      m_rowsSelected += rows;
   }

   /**
    * Get the number of virtual rows selected by by this request.
    *
    * @return      the number of rows
    */
   public int getRowsSelected()
   {
      return m_rowsSelected;
   }

   /**
    * Increment the number of rows updated by one for this request.
    * This is the number of virtual rows, mapping to XML objects.
    * If one row is updated from table A and one row from table B,
    * which are joined to create one result row, only one row updated
    * should be set.
    */
   public void incrementRowsUpdated()
   {
      m_rowsUpdated++;
   }

   /**
    * Increment the number of rows updated by the specified 
    * number for this request.
    * This is the number of virtual rows, mapping to XML objects.
    * If one row is updated from table A and one row from table B,
    * which are joined to create one result row, only one row updated
    * should be set.
    *
    * @param   rows      the number of rows to be added to the counter
    */
   public void incrementRowsUpdated(int rows)
   {
      m_rowsUpdated += rows;
   }

   /**
    * Get the number of virtual rows updated by by this request.
    *
    * @return      the number of rows
    */
   public int getRowsUpdated()
   {
      return m_rowsUpdated;
   }

   /**
    * Increment the number of rows inserted by one for this request.
    * This is the number of virtual rows, mapping to XML objects.
    * If one row is inserted from table A and one row from table B,
    * which are joined to create one result row, only one row inserted
    * should be set.
    */
   public void incrementRowsInserted()
   {
      m_rowsInserted++;
   }

   /**
    * Increment the number of rows inserted by the specified 
    * number for this request.
    * This is the number of virtual rows, mapping to XML objects.
    * If one row is inserted from table A and one row from table B,
    * which are joined to create one result row, only one row inserted
    * should be set.
    *
    * @param   rows      the number of rows to be added to the counter
    */
   public void incrementRowsInserted(int rows)
   {
      m_rowsInserted += rows;
   }

   /**
    * Get the number of virtual rows inserted by by this request.
    *
    * @return      the number of rows
    */
   public int getRowsInserted()
   {
      return m_rowsInserted;
   }

   /**
    * Increment the number of rows deleted by one for this request.
    * This is the number of virtual rows, mapping to XML objects.
    * If one row is deleted from table A and one row from table B,
    * which are joined to create one result row, only one row deleted
    * should be set.
    */
   public void incrementRowsDeleted()
   {
      m_rowsDeleted++;
   }

   /**
    * Increment the number of rows deleted by the specified 
    * number for this request.
    * This is the number of virtual rows, mapping to XML objects.
    * If one row is deleted from table A and one row from table B,
    * which are joined to create one result row, only one row deleted
    * should be set.
    *
    * @param   rows      the number of rows to be added to the counter
    */
   public void incrementRowsDeleted(int rows)
   {
      m_rowsDeleted += rows;
   }

   /**
    * Get the number of virtual rows deleted by by this request.
    *
    * @return      the number of rows
    */
   public int getRowsDeleted()
   {
      return m_rowsDeleted;
   }

   /**
    * Increment the number of rows skipped by one for this request.
    */
   public void incrementRowsSkipped()
   {
      m_rowsSkipped++;
   }

   /**
    * Increment the number of rows skipped by the specified 
    * number for this request.
    *
    * @param   rows      the number of rows to be added to the counter
    */
   public void incrementRowsSkipped(int rows)
   {
      m_rowsSkipped += rows;
   }

   /**
    * Get the number of virtual rows skipped by by this request.
    *
    * @return      the number of rows
    */
   public int getRowsSkipped()
   {
      return m_rowsSkipped;
   }

   /**
    * Increment the number of rows failed by one for this request.
    */
   public void incrementRowsFailed()
   {
      m_rowsFailed++;
   }

   /**
    * Increment the number of rows failed by the specified 
    * number for this request.
    *
    * @param   rows      the number of rows to be added to the counter
    */
   public void incrementRowsFailed(int rows)
   {
      m_rowsFailed += rows;
   }

   /**
    * Get the number of virtual rows failed by by this request.
    *
    * @return      the number of rows
    */
   public int getRowsFailed()
   {
      return m_rowsFailed;
   }

   /**
    * Did the request fail in processing?
    *
    * @return         <code>true</code> if it did
    */
   public boolean isFailure()
   {
      return m_isFailure;
   }

   /**
    * Set this request as a failure (it's successful by default).
    */
   public void setFailure()
   {
      m_isFailure = true;
   }

   /**
    * Set the time the event was completed to the specified time.
    *
    * @param      endTime      the time/date the event was completed
    */
   public void setCompletionTime(Date endTime)
   {
      m_endTime = endTime;
   }

   /**
    * Set the time the event was completed to the current time.
    */
   public void setCompletionTime()
   {
      m_endTime = new Date();
   }

   /**
    * Get the time the event was completed.
    *
    * @return      the time/date the event was completed
    */
   public Date getCompletionTime()
   {
      return m_endTime;
   }

   /**
    * Get the time the event was initiated.
    *
    * @return      the time/date the event was initiated
    */
   public Date getInitiationTime()
   {
      return m_startTime;
   }

   /**
    * Get the amount of time, in milliseconds, to process the request. If
    * the completion time was not set, it will be set in this call
    *
    * @return      the amount of time, in milliseconds or -1 if the start
    *               time is invalid (null)
    */
   public int getProcessingTime()
   {
      if (m_startTime == null)   // must have called constructor with null
         return -1;

      if (m_endTime == null)      // if not set, set it now
         setCompletionTime();

      return (int)(m_endTime.getTime() - m_startTime.getTime());
   }
    

    /**
     * Create an XML element for the statistics
     * @param doc a document to use for creating the statistics, 
     * never <code>null</code>
     * @return the element, never <code>null</code>
     */
   public Element toXml(Document doc)
   {
      Element root = PSXmlDocumentBuilder.createRoot(doc, "PSXExecStatistics");
      PSXmlDocumentBuilder.addElement(doc, root, "RowsInserted", String
            .valueOf(getRowsInserted()));
      PSXmlDocumentBuilder.addElement(doc, root, "RowsUpdated", String
            .valueOf(getRowsUpdated()));
      PSXmlDocumentBuilder.addElement(doc, root, "RowsDeleted", String
            .valueOf(getRowsDeleted()));
      PSXmlDocumentBuilder.addElement(doc, root, "RowsSelected", String
            .valueOf(getRowsSelected()));
      PSXmlDocumentBuilder.addElement(doc, root, "RowsSkipped", String
            .valueOf(getRowsSkipped()));
      PSXmlDocumentBuilder.addElement(doc, root, "RowsFailed", String
            .valueOf(getRowsFailed()));
      return root;
   }


   private java.util.Date      m_startTime      = null;
   private java.util.Date      m_endTime      = null;
   private int                  m_cacheHits       = 0;
   private int                  m_cacheMisses   = 0;
   private int                  m_rowsSelected   = 0;
   private int                  m_rowsInserted   = 0;
   private int                  m_rowsUpdated   = 0;
   private int                  m_rowsDeleted   = 0;
   private int                  m_rowsSkipped   = 0;
   private int                  m_rowsFailed   = 0;
   private boolean            m_isFailure      = false;
}

