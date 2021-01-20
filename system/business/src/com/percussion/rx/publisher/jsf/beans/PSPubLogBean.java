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
package com.percussion.rx.publisher.jsf.beans;

import com.percussion.rx.jsf.PSNodeBase;
import com.percussion.rx.publisher.IPSRxPublisherServiceInternal;
import com.percussion.rx.publisher.PSRxPublisherServiceLocator;
import com.percussion.rx.publisher.jsf.data.PSPubItemEntry;
import com.percussion.services.publisher.IPSPubItemStatus;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.services.publisher.data.PSSortCriterion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.trinidad.model.CollectionModel;
import org.apache.myfaces.trinidad.model.SortCriterion;

/**
 * Maintain the information to scroll through a list of log items. 
 * It paginates the list and only hold one page of data/rows at any given time.
 * 
 * @author dougrand
 */
public class PSPubLogBean extends CollectionModel
{
   /**
    * Logger
    */
   private static Log ms_log = LogFactory.getLog(PSPubLogBean.class);

   /**
    * Holds the current job id.
    */
   private long m_jobId = -1;

   /**
    * The row count, -1 when nothing is loaded.
    */
   private int m_count = -1;

   /**
    * Holds the current index.
    */
   private int m_index = -1;

   /**
    * Sort criteria, setup initially in the ctor.
    */
   private List<SortCriterion> m_sortCriteria = null;

   /**
    * Runtime navigation, used to set this data on the created pub items.
    */
   private PSRuntimeNavigation m_runtimeNavigation;

   /**
    * A container class contains the paginated range.
    */
   private class PageRange
   {
      /**
       * The starting (0 based) row of the range.
       */
      private int mi_startRow = -1;
      
      /**
       * The ending (0 based) row of the range.
       */
      private int mi_endRow = -1;
      
      /**
       * All publishing item entries for this range.
       */
      PSPubItemEntry mi_entries[] = null;
   }
   
   /**
    * The current paginated range. 
    */
   private PageRange m_currRange = new PageRange();
   
   /**
    * The reference IDs in the sorted order for the publishing job 
    * {@link #m_jobId}.
    */
   private long[] m_refIds = null;
   
   /**
    * Create an instance of the publishing log bean.
    * 
    * @param runtimeNavigation the Runtime navigation, never <code>null</code>.
    */
   public PSPubLogBean(PSRuntimeNavigation runtimeNavigation) 
   {
      if (runtimeNavigation == null)
      {
         throw new IllegalArgumentException("runtimeNavigation may not be null");
      }
      
      m_sortCriteria = new ArrayList<SortCriterion>();
      m_sortCriteria.add(new SortCriterion("referenceId", true));
      m_runtimeNavigation = runtimeNavigation;
   }

   /**
    * Get the number of rows per page for the paginated table.
    * @return the rows per page.
    */
   public int getPageRows()
   {
      int rowsPerPage = PSNodeBase.getPageRows(getRowCount());
      return Math.min(rowsPerPage, getMaxRowPerPage());
   }   
   
   /**
    * Gets the configured maximum rows per page. This is used to re-adjust
    * the default calculation.
    * 
    * @return the configured maximum rows per page.
    */
   private int getMaxRowPerPage()
   {
      if (maxRowPerPage != null)
         return maxRowPerPage.intValue();
      
      IPSRxPublisherServiceInternal service = (IPSRxPublisherServiceInternal) PSRxPublisherServiceLocator
            .getRxPublisherService();
      return service.getConfigurationBean().getMaxRowsPerPageInViewPubLog();
   }
   
   /**
    * @see #getMaxRowPerPage()
    */
   private static Integer maxRowPerPage = null;
   
   /**
    * @return the jobId
    */
   public long getJobId()
   {
      return m_jobId;
   }

   /**
    * @param jobId the jobId to set
    */
   public void setJobId(long jobId)
   {
      if (m_jobId == jobId) return;
      m_jobId = jobId;
      reset();
   }

   /**
    * On various changes to the criteria, reset the stored data.
    */
   private void reset()
   {
      m_refIds = null;
      m_currRange = new PageRange();
      m_count = -1;
   }

   @Override
   public Object getRowKey()
   {
      if (m_jobId < 0)
         return null;
      Integer rval = m_index > -1 ? new Integer(m_index) : null;
      ms_log.debug("getRowKey: " + rval);
      return rval;
   }

   @Override
   public void setRowKey(Object key)
   {
      ms_log.debug("setRowKey: " + key);
      if (key == null)
         m_index = -1;
      else
         m_index = ((Integer) key).intValue();
   }

   @Override
   public int getRowCount()
   {
      if (m_jobId < 0)
         return 0;
      if (m_refIds == null)
         getRowData();
      
      ms_log.debug("getRowCount: " + m_count);
      return m_count;
   }

   /**
    * Get the data for the current row.
    * @return the row data of the {@link #m_index}, never <code>null</code>.
    */
   private Object getCurrRow()
   {
      if (m_index < m_currRange.mi_startRow
            || m_index > m_currRange.mi_endRow)
      {
         // need to reset current range
         int range = PSNodeBase.getPageRows(m_count);
         for (int i=0; i<m_count; i++)
         {
            int start = i * range;
            int end = (i+1) * range;
            if (m_index >= start && m_index <= end)
            {
               setCurrRange(start, end);
               break;
            }
         }
      }
      
      return m_currRange.mi_entries[m_index - m_currRange.mi_startRow];
   }
   
   @Override
   public Object getRowData()
   {
      if (m_jobId < 0)
         return Collections.emptyMap();

      try
      {
         if (m_refIds == null)
         {
            setRefIds();
            m_count = m_refIds.length;
         }

         ms_log.debug("getRowData, index: " + m_index);
         if (isRowAvailable())
         {
            return getCurrRow();
         }
         else
         {
            return Collections.emptyMap();
         }
      }
      catch (Exception e)
      {
         ms_log.error("Problem getting log data", e);
         return Collections.emptyMap();
      }
   }

   /**
    * Set the reference ID buffer.
    */
   private void setRefIds()
   {
      IPSPublisherService psvc = PSPublisherServiceLocator
            .getPublisherService();
      List<PSSortCriterion> sortCrit = new ArrayList<PSSortCriterion>();
      for (SortCriterion criteria : getSortCriteria())
      {
         sortCrit.add(new PSSortCriterion(criteria.getProperty(), criteria.isAscending()));
      }
      
      List<Long> refIds = psvc.findRefIdForJob(m_jobId, sortCrit);
      m_refIds = new long[refIds.size()];
      int i=0;
      for (Long id : refIds)
      {
         m_refIds[i++] = id.longValue();
      }
   }
   
   /**
    * Set the current paginated range.
    * @param startRow the starting (0 based) row of the new range.
    * @param endRow the ending (0 based) row of the new range.
    */
   public void setCurrRange(int start, int end)
   {
      IPSPublisherService psvc = PSPublisherServiceLocator
            .getPublisherService();
      m_currRange = new PageRange();
      m_currRange.mi_startRow = start;
      m_currRange.mi_endRow = Math.min(end, m_count-1);

      m_currRange.mi_entries = new PSPubItemEntry[m_currRange.mi_endRow
            - m_currRange.mi_startRow + 1];
      List<Long> refs = new ArrayList<Long>();
      for (int j=m_currRange.mi_startRow; j<=m_currRange.mi_endRow; j++)
      {
         refs.add(m_refIds[j]);
      }
      Map<Long, IPSPubItemStatus> idMap = new HashMap<Long, IPSPubItemStatus>();
      for (IPSPubItemStatus entry : psvc.findPubItemStatusForReferenceIds(refs))
      {
         idMap.put(entry.getReferenceId(), entry);
      }
      for (int j=m_currRange.mi_startRow; j<=m_currRange.mi_endRow; j++)
      {
         Long refId = m_refIds[j];
         IPSPubItemStatus status = idMap.get(refId);
         if (status == null)
         {
            ms_log.error("Cannot find refId = " + refId);
            continue;
         }
         
         PSPubItemEntry entry = new PSPubItemEntry(m_runtimeNavigation, this,
               status, j);
         m_currRange.mi_entries[j - m_currRange.mi_startRow] = entry;
      }
   }

   @Override
   public int getRowIndex()
   {
      if (m_jobId < 0)
      {
         ms_log.debug("getRowIndex: -1");
         return -1;
      }
      else
      {
         ms_log.debug("getRowIndex: " + m_index);
         return m_index;
      }
   }

   @Override
   public Object getWrappedData()
   {
      return null;
   }

   @Override
   public boolean isRowAvailable()
   {
      if (m_refIds == null)
         getRowData();
      return m_index < m_count && m_index > -1;
   }

   @Override
   public void setRowIndex(int index)
   {
      ms_log.debug("setRowIndex: " + index);
      m_index = index;
   }

   @Override
   public void setWrappedData(@SuppressWarnings("unused")
   Object arg0)
   {
      throw new UnsupportedOperationException("Not supported");
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.apache.myfaces.trinidad.model.CollectionModel#isSortable(java.lang.String)
    */
   @Override
   public boolean isSortable(@SuppressWarnings("unused")
   String property)
   {
      return true;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.apache.myfaces.trinidad.model.CollectionModel#setSortCriteria(java.util.List)
    */
   @Override
   public void setSortCriteria(List<SortCriterion> criteria)
   {
      m_sortCriteria = criteria;
      reset();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.apache.myfaces.trinidad.model.CollectionModel#getSortCriteria()
    */
   @Override
   public List<SortCriterion> getSortCriteria()
   {
      return m_sortCriteria;
   }

   /**
    * Navigate to the detail view
    * 
    * @return the outcome
    */
   public String perform()
   {
      return "pub-runtime-log-item" ;
   }
}
