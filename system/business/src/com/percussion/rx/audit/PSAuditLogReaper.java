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
package com.percussion.rx.audit;

import com.percussion.services.audit.IPSDesignObjectAuditConfig;
import com.percussion.services.audit.IPSDesignObjectAuditService;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Automatically deletes old audit log entries on a daily basis as configured by
 * the {@link IPSDesignObjectAuditConfig}.
 */
public class PSAuditLogReaper extends Thread
{
   /**
    * Default ctor
    */
   public PSAuditLogReaper()
   {
      super();
      setDaemon(true);
      setName("AuditLogReaper");
   }

   /**
    * Called by Spring dependency injection to set the audit service on this 
    * object, used as an event to start the pruning thread.  The thread is not
    * started if pruning is disabled (see 
    * {@link IPSDesignObjectAuditConfig#getLogRetentionDays()}).  Method is 
    * synchronized to protect against multiple calls starting more than one
    * thread.
    * 
    * @param service The service to use, may not be <code>null</code>.
    * 
    * @throws IllegalStateException if the service has already been set.
    */
   public synchronized void setService(IPSDesignObjectAuditService service)
   {
      if (service == null)
         throw new IllegalArgumentException("service may not be null");
      
      if (m_service != null)
         throw new IllegalStateException("service has already been set");
      
      m_service = service;
      
      IPSDesignObjectAuditConfig config = m_service.getConfig();
      m_logRetentionDays = config.getLogRetentionDays();
      if (config.isEnabled())
      {
         ms_log.info("Design object audit Logging is enabled");
         
         if (m_logRetentionDays > 0)
         {
            ms_log.info("Design object audit log pruning is enabled, log " +
               "entries will be deleted after " + m_logRetentionDays +" days.");
            start();
         }
         else 
         {
            ms_log.info("Design object audit log pruning is disabled, log "
               + "entries will be saved indefinitely");
         }
      }
      else
      {
         ms_log.info("Design Audit Logging is disabled");
      }
   }
   
   /**
    * Deletes logs older than the interval specified by 
    * {@link IPSDesignObjectAuditConfig#getLogRetentionDays()}.  Sleeps for 24
    * hours between attempts.
    */
   @Override
   public void run()
   {
      ms_log.info("Starting Design Audit Log Reaper");
      
      // reap now, wait 24 hours and repeat
      try
      {
         while (true)
         {
            deleteLogEntries();
            
            long sleepTime = m_sleepIntervalMins * MINS_TO_MILLIS;
            ms_log.debug("Next audit log pruning will be in " + 
               m_sleepIntervalMins + " minutes");
            sleep(sleepTime);
         }
      }
      catch (InterruptedException e)
      {
         ms_log.info("Interrupted, shutting down");
      }
      catch (Throwable t)
      {
         ms_log.error("Error deleting design object audit logs", t);
      }
   }
   
   /**
    * Worker method to perform the delete of log entries, synchronizing on 
    * {@link #m_monitor} to prevent {@link #shutdown()} from executing during 
    * the deletion. See {@link #run()} for more details.
    */
   private void deleteLogEntries()
   {
      synchronized (m_monitor)
      {
         Date date = calculatedDeleteDate();
         ms_log.debug("Deleting design object audit logs older than " + date);      
         m_service.deleteAuditLogEntriesByDate(date);
      }
   }
   
   /**
    * Method to nicely interrupt this thread, synchronizes on {@link #m_monitor}
    * to prevent interruption of the {@link #deleteLogEntries()} method.
    */
   public void shutdown()
   {
      try
      {
         synchronized (m_monitor)
         {
            interrupt();
         }
      }
      catch (Exception e)
      {
         // ignore, we tried
      }
   }
   
   /**
    * Set the number of minutes to sleep between reaping.
    * 
    * @param mins The number of minutes, must be > 0.
    */
   public void setSleepIntervalMins(int mins)
   {
      if (mins <= 0)
         throw new IllegalArgumentException("mins must be > 0");
      
      m_sleepIntervalMins = mins;
   }
   
   /**
    * Gets the current date adjusted backwards by the value of 
    * {@link #m_logRetentionDays}.
    * 
    * @return The date, never <code>null</code>.
    */
   private Date calculatedDeleteDate()
   {
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.DAY_OF_MONTH, (m_logRetentionDays * -1));
      
      return cal.getTime();
   }
   
   /**
    * Number of days to keep logs for, set during first call to 
    * {@link #setService(IPSDesignObjectAuditService)}.
    */
   private int m_logRetentionDays;
   
   /**
    * Service to use to get the configuration and delete audit logs,
    * <code>null</code> until first call to
    * {@link #setService(IPSDesignObjectAuditService)}, never <code>null</code>
    * after that.
    */
   private IPSDesignObjectAuditService m_service = null;
   
   /**
    * Number of minutes to sleep between reaping, defaults to 24 hours.
    */
   private int m_sleepIntervalMins = 1440;
   
   /**
    * Number of milliseconds in a minute.
    */
   private static final long MINS_TO_MILLIS = 60 * 1000;
   
   /**
    * Logger for the reaper, never <code>null</code>.
    */
   private static Log ms_log = LogFactory.getLog(PSAuditLogReaper.class);
   
   /**
    * Object for locking synchronized blocks.
    */
   private Object m_monitor = new Object();
}
