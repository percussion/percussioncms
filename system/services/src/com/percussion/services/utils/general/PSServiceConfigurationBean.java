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
package com.percussion.services.utils.general;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This java bean contains properties to be used by the various service beans
 * that require user modifiable configuration.
 * 
 * @author dougrand
 */
public class PSServiceConfigurationBean
{
   static Log ms_log = LogFactory.getLog("PSServiceConfigurationBean");

   /**
    * This property is used by the assembly service to decide if a content node
    * should or should not be cached in the memory cache. If the value is 
    * <code>0</code>, then no content nodes are cached. 
    */
   long m_maxCachedContentNodeSize = 0;
   
   /**
    * @see #getQuartzThreadCount()
    */
   private int m_quartzThreadCount = 3;
   
   /**
    * @see #getPublishJobTimeout()
    */
   private int m_publishJobTimeout = 600; 

   /**
    * @see #getPublishQueueTimeout()
    */
   private int m_publishQueueTimeout = 10; 

   /**
    * @see #getMaxRowsPerPageInViewPubLog()
    */
   private int m_maxRowsPerPageInViewPubLog = 300;
   
   /**
    * @see #getQuartzProperties()
    */
   private Properties m_quartzProperties = new Properties();
   
   /**
    * @see #isUseHttpsForSecureSite()
    */
   private boolean useHttpsForSecureSite = true;
   
   /**
    * Gets the maximum rows per page that is used when viewing 
    * a publish log of an edition. Defaults to <code>300</code>
    * if not set.
    * 
    * @return the maximum rows per page.
    */
   public int getMaxRowsPerPageInViewPubLog()
   {
      return m_maxRowsPerPageInViewPubLog;
   }
   
   /**
    * Sets the maximum rows per page that is used when viewing 
    * a publish log of an edition. Defaults to <code>300</code>
    * if not set.
    * 
    * @param maxRowPerPage the new maximum rows per page, must be
    * greater than <code>0</code>; otherwise it is ignored.
    */
   public void setMaxRowsPerPageInViewPubLog(int maxRowPerPage)
   {
      if (maxRowPerPage <= 0)
      {
         ms_log.warn("Ignore maxRowsPerPageInViewPubLog " + maxRowPerPage + ", because it is <= 0.");
         return;
      }
      
      ms_log.debug("maxRowsPerPageInViewPubLog = " + maxRowPerPage);
      m_maxRowsPerPageInViewPubLog = maxRowPerPage;
   }
   
   /**
    * Get the maximum size of content nodes cached by the assembly service. Note
    * that a content node may start by being cached, and may be removed later 
    * after the body fields and/or image fields are loaded.
    * @return the maxCachedContentNodeSize
    */
   public long getMaxCachedContentNodeSize()
   {
      return m_maxCachedContentNodeSize;
   }

   /**
    * Set the maximum size of cached content nodes, should only be called by
    * Spring as part of configuration.
    * 
    * @param maxCachedContentNodeSize the maxCachedContentNodeSize to set
    */
   public void setMaxCachedContentNodeSize(long maxCachedContentNodeSize)
   {
      ms_log.debug("maxCachedContentNodeSize = " + maxCachedContentNodeSize);
      m_maxCachedContentNodeSize = maxCachedContentNodeSize;
   }

   /**
    * Get thread count for the quartz package, which is the max number of 
    * concurrent jobs fired/triggered by the scheduler.
    * 
    * @return the thread count. It is default to 3 if not specified.
    * @deprecated use {@link #getQuartzProperties()} instead.
    */
   public int getQuartzThreadCount()
   {
      return m_quartzThreadCount;
   }

   /**
    * Set the thread count for the quartz scheduler.
    * @param threadCount the new thread count.
    * @deprecated use {@link #setQuartzProperties(Properties)} instead.
    */
   public void setQuartzThreadCount(int threadCount)
   {
      ms_log.debug("quartzThreadCount = " + threadCount);
      m_quartzThreadCount = threadCount;
   }
   
   /**
    * The publishing job time out in minutes. 
    * After a job sent all its job messages to the publishing queue,
    * it will be timed out if it does not receive any status update 
    * since its last update.  
    * 
    * @return the timeout. Defaults to 600 minutes (or 10 hours).
    * It is disabled if it is less than or equals to <code>0</code>
    */
   public int getPublishJobTimeout()
   {
      return m_publishJobTimeout;
   }

   /**
    * Set the publishing job time out in minutes.
    * 
    * @param timeout the new timeout, must not be less than or equal to <code>0</code>.
    * 
    * @see #getPublishJobTimeout()
    */
   public void setPublishJobTimeout(int timeout)
   {
      ms_log.debug("publishJobTimeout = " + timeout);
      m_publishJobTimeout = timeout;
   }
   
   /**
    * Gets the publish queue timeout in minutes. 
    * The system "notifies" all active publishing jobs when a message is processed
    * (from the publish queue) and update the job status.
    * 
    * A job will be time out if it does not received any notification within the 
    * specified time.

    * @return the time out. Defaults to 10 minutes. It is disabled if it is less than
    * or equals to <code>0</code>
    */
   public int getPublishQueueTimeout()
   {
      return m_publishQueueTimeout;
   }
   
   /**
    * Sets the publish queue time out.
    * 
    * @param timeout the new time out, must not be less than or equal to <code>0</code>.
    */
   public void setPublishQueueTimeout(int timeout)
   {
      ms_log.debug("publishQueueTimeout = " + timeout);
      m_publishQueueTimeout = timeout;
   }

   /**
    * Gets all quartz-related properties.
    * 
    * @return the quartzProperties
    */
   public Properties getQuartzProperties()
   {
      return m_quartzProperties;
   }

   /**
    * Sets the quartz-related properties.
    * 
    * @param quartzProperties the quartzProperties to set
    */
   public void setQuartzProperties(Properties quartzProperties)
   {
      m_quartzProperties = quartzProperties;
   }

   /**
    * @param useHttpsForSecureSite the useHttpsForSecureSite to set
    */
   public void setUseHttpsForSecureSite(boolean useHttpsForSecureSite)
   {
      this.useHttpsForSecureSite = useHttpsForSecureSite;
   }

   /**
    * Sets if the system should use HTTPS for the secure sites or not. Defaults
    * to <code>true</code>.
    * 
    * @return the useHttpsForSecureSite if <code>true</code> the system will use
    *         HTTPS for the secure sites. If <code>false</code>, HTTP will be
    *         used for secure sites.
    */
   public boolean isUseHttpsForSecureSite()
   {
      return useHttpsForSecureSite;
   }
}
