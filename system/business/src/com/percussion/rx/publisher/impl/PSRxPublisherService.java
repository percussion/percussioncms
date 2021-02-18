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
package com.percussion.rx.publisher.impl;

import com.percussion.rx.delivery.PSConnectivityCheck;
import com.percussion.rx.publisher.IPSPublisherItemStatus;
import com.percussion.rx.publisher.IPSPublisherJobStatus;
import com.percussion.rx.publisher.IPSPublisherJobStatus.State;
import com.percussion.rx.publisher.IPSPublishingJobStatusCallback;
import com.percussion.rx.publisher.IPSRxPublisherServiceInternal;
import com.percussion.rx.publisher.data.PSDemandWork;
import com.percussion.rx.publisher.data.PSPubItemStatus;
import com.percussion.rx.publisher.jsf.nodes.PSPublishingStatusHelper;
import com.percussion.server.PSServer;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.jms.IPSQueueSender;
import com.percussion.services.publisher.*;
import com.percussion.services.pubserver.PSPubServerDaoLocator;
import com.percussion.services.pubserver.data.PSPubServer;
import com.percussion.services.utils.general.PSServiceConfigurationBean;
import com.percussion.util.PSDateFormatISO8601;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.ObjectFactory;

import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

import static com.percussion.rx.publisher.PSPublisherUtils.validateEditionForOnDemandContentLists;


/**
 * The implementation of the service. 
 * 
 * @author dougrand
 */
public class PSRxPublisherService implements IPSRxPublisherServiceInternal
{
   /**
    * Thread implementation that monitors work to be done by demand publishing.
    * This thread is notified when items are added to the demand queue. 
    * Otherwise it wakes every so often to see if there's work to be done and
    * queues it providing there is no current edition being run for the given
    * data. The queue will notify the thread to avoid the wait when possible.
    */
   class DemandMonitor implements Runnable
   {
      /**
       * The thread will wake after this interval in milliseconds even if 
       * there's no work to be done. This allows work that has been queued 
       * since the current edition was scheduled to be started. 
       */
      private static final int DEMAND_WAIT_TIME = 10000;

      public void run()
      {
         try
         {
            IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
            
               while(true)
               {
                  for(Integer editionid : m_demandWork.keySet())
                  {
                     Queue<PSDemandWork> q = m_demandWork.get(editionid);
                     if (q.size() > 0)
                     {
                        IPSGuid edition = gmgr.makeGuid(editionid.longValue(), 
                              PSTypeEnum.EDITION);
                        long jobid = getEditionJobId(edition);
                        IPSPublisherJobStatus.State state = 
                           IPSPublisherJobStatus.State.INACTIVE;
                        if (jobid != 0)
                        {
                           PSPublishingJob job = getPublishingJob(jobid);
                           state = job != null ? job.getState() : 
                              IPSPublisherJobStatus.State.INACTIVE;
                        }
                        if (state.equals(IPSPublisherJobStatus.State.INACTIVE) ||
                              state.isTerminal())
                        {
                           jobid = startPublishingJob(edition, null);
                           ms_log.debug("Started demand edition " + edition + 
                                 " job " + jobid);
                        }
                     }
                  }
                  synchronized(m_demandWork)
                  {
                     // the "wait" call below relinquishes any and all 
                     // synchronization claims on "m_demandWork"
                     m_demandWork.wait(DEMAND_WAIT_TIME);
                  }
            }
         }
         catch(Exception e)
         {
            ms_log.error("Problem in monitor thread", e);
         } 
      }
   }
   
   /**
    * Logger used for business publisher service
    */
   static Log ms_log = LogFactory.getLog(PSRxPublisherService.class);
   
      public final String DEFAULT_GENERATOR =
      "Java/global/percussion/system/sys_SelectedItemsGenerator";

   
   /**
    * A map of job id to job to allow dispatching requests from service methods
    * to the appropriate job.
    * 
    * NOTE, the accessing of this object is synchronized by "this" object or
    * all its calling methods are synchronized. 
    */
   protected static Map<Long, PSPublishingJob> m_jobs = 
      new ConcurrentHashMap<>();

   /**
    * The key to the map is the edition id that is being queued. The value is
    * a queue of work waiting for the given edition.
    * 
    * NOTE, the accessing of this object is synchronized on this object itself.
    * Further more, don't try to lock/sychronize "m_demandWork" and "this" at
    * the same time, except in the {@link PSDemandWork} - avoid deadlock. 
    */
   Map<Integer,  ConcurrentLinkedQueue<PSDemandWork>> m_demandWork = 
      new ConcurrentHashMap<>();
   
   /**
    * This map holds the correspondence between the demand ids and the running
    * jobs.
    * 
    * NOTE, the accessing of this object is synchronized by "this" object or
    * all its calling methods are synchronized. 
    */
   private Map<Long, Long> m_demandRequestToJob = new ConcurrentHashMap<>();
   
   /**
    * The demand thread is started if this value is <code>null</code> or
    * if the thread is no longer running. Started in the 
    * {@link #queueDemandWork(int, PSDemandWork)} method.
    */
   private Thread m_demandMonitorThread = null;
   
   /**
    * How many status records to hold before updating the database.
    */
   public static int UPDATE_BATCH_SIZE = 100;

   /**
    * Publishing sender (as the JMS message producer) to send assembly items 
    * into the queue for publishing. It is wired by Spring, should not be
    * <code>null</code> if properly configured.
    */
   private ObjectFactory m_publishSenderFactory = null;

   /**
    * Service configuration bean. It is fired by Spring bean configuration.
    */
   private PSServiceConfigurationBean m_configurationBean;
   
   
   private static final Object startJobLock = new Object();
   
   /**
    * Required getter for Spring, not used otherwise. This (JMS) queue sender is 
    * used by the publishing job to send work items into the publishing queue.
    * 
    * @return the queue sender, never <code>null</code> after wiring by Spring.
    */
   public ObjectFactory getPublishSenderFactory()
   {
      return m_publishSenderFactory;
   }

   /**
    * Gets the configuration bean, which is wired by spring beans.
    *  
    * @return the configurationBean
    */
   public PSServiceConfigurationBean getConfigurationBean()
   {
      return m_configurationBean;
   }

   /*
    * Sets the configuration bean, which is wired by spring beans.
    * 
    * @param configurationBean the configurationBean to set
    */
   public void setConfigurationBean(
         PSServiceConfigurationBean configurationBean)
   {
      m_configurationBean = configurationBean;
   }
   
   /**
    * Setter used by Spring to wire the publishing sender.
    * 
    * @param publisherSender the new sender to set, never <code>null</code>
    */
   public void setPublishSenderFactory(ObjectFactory publisherSenderFactory)
   {
      if (publisherSenderFactory == null)
      {
         throw new IllegalArgumentException("publisherSenderFactory may not be null");
      }
      m_publishSenderFactory = publisherSenderFactory;
   }

   /* (non-Javadoc) 
    * @see com.percussion.rx.publisher.impl.IPSRxPublisherService#executePushPublishStart(com.percussion.utils.guid.IPSGuid)
    */
   public long startPublishingJob(IPSGuid edition,
         IPSPublishingJobStatusCallback callback)
   {
      
      long jobid=-1;
      synchronized(startJobLock) 
      {
         reapActiveJobs();
         
         // Check existing jobs to see if the given Edition is already running
         for (Long key : m_jobs.keySet())
         {
            PSPublishingJob job = m_jobs.get(key);
            if (edition.equals(job.getEdition()) && ! job.isFinished())
            {
               // do not rerun for cm1
               // job.setRerun(true);
               
               ms_log.debug("edition :"+job.getEdition().getUUID()+" is currently running, marked to rerun after completion");
               // Now we are throwing exception when this could be counted as normal
               // processing.  Should use other mechanism.
               throw new IllegalStateException(
                     "Cannot start edition that is currently running: " + edition);
            }
         }
         
         
         // Start the job, create a new sender for each job, may be better to pass the factory and create on demand.
         IPSQueueSender publishSender = (IPSQueueSender)m_publishSenderFactory.getObject();
         
      // Start the job
      PSPublishingJob job = new PSPublishingJob(publishSender,
            edition, 
            this,
            callback);
      
      try{
         if (checkConnectivity(edition, job)) {
            m_jobs.put(job.getJobid(), job);
            job.startJob();
            return job.getJobid();
         }
      } catch (Exception e){
         job.cancel(true);
         throw new IllegalStateException(
               "Cannot connect to FTP Server - check configuration",e);
      }
      jobid = job.getJobid();
      }
      return jobid;
   }
 protected boolean checkConnectivity(IPSGuid edition, PSPublishingJob job)
   {
      IPSPublisherService pubService = PSPublisherServiceLocator.getPublisherService();
      IPSEdition editionObject =  pubService.loadEdition(edition);
      
      PSPubServer pubServer = PSPubServerDaoLocator.getPubServerManager()
            .loadPubServer(editionObject.getPubServerId());
      
      String pubServerType = pubServer.getPublishType();
      if (pubServerType != null
              && (pubServerType.equals("ftp") ||pubServerType.equals("sftp"))) {
          if (!PSConnectivityCheck.checkFTPConnectivity(edition, job, pubServer, pubServerType, pubServerType.equals("sftp")))
             return false;
  
      }
      return true;
   }
   /**
    *  Check existing jobs to see if any can be reaped.
    */
   private void reapActiveJobs()
   {
      // Check existing jobs to see if any can be reaped 
      long current = System.currentTimeMillis();
      Set<Long> keysToRemove = new HashSet<>();

      for (Long key : m_jobs.keySet())
      {
         PSPublishingJob job = m_jobs.get(key);
         if (job.getEndTime() != null
               && (current - job.getEndTime().getTime()) > REAP_TIME)
         {
            keysToRemove.add(key);
         }
      }

      removePublishingJobStatus(keysToRemove);      
   }
   
   /* (non-Javadoc) 
    * @see com.percussion.rx.publisher.impl.IPSRxPublisherService#getPushPublishState(long)
    */
   public IPSPublisherJobStatus getPublishingJobStatus(long jobId)
   {
      PSPublishingJob job = getPublishingJob(jobId);
      if (job == null)
      {
         throw new IllegalStateException("The publishing job " + jobId
               + " is unknown");
      }
      job.resetLastAccessTime();
      return job.getStatus();
   }
   
   /*
    * see base interface method for details
    */
   public void removePublishingJobStatus(Collection<Long> jobIds)
   {
     
      synchronized(startJobLock)
      {
         for (Long key : jobIds)
         {
            m_jobs.remove(key);
         }  
         
         Set<Long> requestsToRemove = new HashSet<>();
         for (Long requestid : m_demandRequestToJob.keySet())
         {
            Long jobid = m_demandRequestToJob.get(requestid);
            if (jobIds.contains(jobid))
            {
               requestsToRemove.add(requestid);
            }
         }
         for (Long requestid : requestsToRemove)
         {
            m_demandRequestToJob.remove(requestid);
         }
      }
      
   }

   /* (non-Javadoc) 
    * @see com.percussion.rx.publisher.impl.IPSRxPublisherService#cancelPushPublishState(long)
    */
   public void cancelPublishingJob(long jobId)
   {
      PSPublishingJob job = getPublishingJob(jobId);
      if (job == null)
      {
         throw new IllegalStateException("The publishing job " + jobId
               + " is unknown");
      }
      job.cancel(true);
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.rx.publisher.IPSRxPublisherServiceInternal#getPublishingJob(long)
    */
   public PSPublishingJob getPublishingJob(long jobID)
   {
      return m_jobs.get(jobID);
   }

   /* (non-Javadoc)
    * @see com.percussion.rx.publisher.IPSRxPublisherService#updateItemState(com.percussion.rx.publisher.data.PSPubItemStatus)
    */
   public void updateItemState(PSPubItemStatus status)
   {
      long jobid = status.getJobId();
      PSPublishingJob job = m_jobs.get(jobid);
      if (job == null)
      {
         throw new IllegalStateException("The publishing job " + jobid + " is unknown");
      }
      job.updateItemState(status);

      // Don't need to synchronize only updating date do not care if multiple
      // threads hit this
      notifyOtherJobs(jobid);
     
      addToStatusUpdateQueue(status);
     
   }
   
   public void addToStatusUpdateQueue(PSPubItemStatus status)
   {
      if (status.getState().isPersistable())
      {
         ConcurrentLinkedQueue<IPSPublisherItemStatus> updateQueue = m_jobs.get(status.getJobId()).getUpdateQueue();
         updateQueue.add(status);
         
         if (updateQueue.size()>20)
         {
            flushStatusToDatabase(updateQueue);
         }
      }
   }
   

   /**
    * Notifying all active jobs, except the specified job.
    * This is acting as heart-beat of the publishing queue.
    * 
    * 
    * @param jobId the specified job ID.
    */
   private void notifyOtherJobs(long jobId)
   {
      // do not care about synchronization as we are just setting last
      // modified time
      for (PSPublishingJob job : m_jobs.values())
      {
         if (job.getJobid() == jobId)
            continue;
         
         job.notifyStatusUpdate();
      }
   }

   
   /* (non-Javadoc)
    * @see com.percussion.rx.publisher.IPSRxPublisherService#flushStatusToDatabase()
    */
   public int flushStatusToDatabase(ConcurrentLinkedQueue<IPSPublisherItemStatus> updateQueue)
   {

      IPSPublisherService svc = PSPublisherServiceLocator
         .getPublisherService();
      
         List<IPSPublisherItemStatus> persistStatuses = new ArrayList<>();
   
         
         IPSPublisherItemStatus item = updateQueue.poll();
         
         while(item!=null && persistStatuses.size()<99)
         {
            persistStatuses.add(item);
            item = updateQueue.poll();
         }
         
         if (item!=null)
            persistStatuses.add(item);
         
         if (persistStatuses.size() > 0)
         {
            svc.updatePublishingInfo(persistStatuses);
         }
         return persistStatuses.size();
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.rx.publisher.IPSRxPublisherService#acknowledgeJobCommit(long)
    */
   public void acknowledgeJobCommit(long jobId, boolean hasError)
   {
         PSPublishingJob job = m_jobs.get(jobId);
         if (job == null)
         {
            throw new IllegalStateException("The publishing job " + jobId
                  + " is unknown");
         }
         job.acknowledgeCommit(hasError);
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.rx.publisher.IPSRxPublisherService#getActiveJobIds()
    */
   public Collection<Long> getActiveJobIds()
   {
      return getActiveJobIds(null);
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.rx.publisher.IPSRxPublisherServiceInternal#getActiveJobIds(com.percussion.utils.guid.IPSGuid)
    */
   public Collection<Long> getActiveJobIds(IPSGuid siteGuid)
   {
      reapActiveJobs();
      
      Map<IPSGuid,PSPublishingJob> editionToJob = 
         new HashMap<>();
      
      for(PSPublishingJob job : m_jobs.values())
      {
         if (job != null &&
                 job.getSite() != null
                 && siteGuid != null
                 && !job.getSite().equals(siteGuid))
         {
            continue;
         }
         
         IPSGuid edition = job.getEdition();
         PSPublishingJob former = editionToJob.get(edition);
         if (former == null || former.getStartTime().before(job.getStartTime()))
         {
            editionToJob.put(edition, job);
         }
      }
      
      Collection<Long> rval = new ArrayList<>();
      for(PSPublishingJob job : editionToJob.values()) 
      {
         rval.add(job.getJobid());
      }
      return rval;
   }
   
   public boolean isJobActive(long jobId) 
   {
      // m_jobs is now ConcurrentHashMap do not synchronize this with method
      return m_jobs.containsKey(jobId);
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.rx.publisher.IPSRxPublisherService#getEditionJobId(com.percussion.utils.guid.IPSGuid)
    */
   public long getEditionJobId(IPSGuid guid)
   {
      // Get the most recent job
      long idtoreturn = 0;
      for(PSPublishingJob job : m_jobs.values())
      {
         if (job.getEdition().equals(guid))
         {
            idtoreturn = Math.max(idtoreturn, job.getJobid());
         }
      }

      return idtoreturn;
   }
/*
    * (non-Javadoc)
    * @see com.percussion.rx.publisher.IPSRxPublisherService#getJobEditionId(com.percussion.utils.guid.IPSGuid)
    */
   public synchronized IPSGuid getJobEditionId(long jobId)
   {
      IPSGuid editionid = null;
      PSPublishingJob job = getPublishingJob(jobId);
      if(job != null)
         editionid = job.getEdition();
      return editionid;
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.rx.publisher.IPSRxPublisherService#getDemandWorkForEdition(int)
    * 
    * NOTE, don't "synchronized" on this method, which is to avoid 
    * lock/sychronize more than one objects (m_demandWork and "this") at the
    * same time.  
    */
   public Collection<PSDemandWork> getDemandWorkForEdition(int edition)
   {
      return getDemandWorkForEdition(edition, false);
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.rx.publisher.IPSRxPublisherService#getDemandWorkForEdition(int, boolean)
    * 
    * NOTE, don't "synchronized" on this method, which is to avoid 
    * lock/sychronize more than one objects (m_demandWork and "this") at the
    * same time.  
    */
   public Collection<PSDemandWork> getDemandWorkForEdition(int edition, boolean isRetrieveOnly)
   {
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      IPSGuid editionid = gmgr.makeGuid(edition, PSTypeEnum.EDITION);
      Long jobid = getEditionJobId(editionid);
      
      Queue<PSDemandWork> q = getDemandQueueForEdition(edition, isRetrieveOnly);
      if (q == null || q.size() == 0)
      {
         return Collections.emptyList();
      }
      else
      {
         Collection<PSDemandWork> rval = new ArrayList<>();
         PSDemandWork item = q.poll();
   
            while(item != null)
            {
               rval.add(item);
               m_demandRequestToJob.put(item.getRequest(), jobid);
               item = q.poll();
            }
    
         return rval;
      }
   }

   /**
    * Gets the work queue for the supplied edition.
    * 
    * @param edition the edition ID
    * @param isRetrieveOnly <code>true</code> if retrieve the queue only, but not remove
    * the items in the queue.
    * 
    * @return the queue. It is the cloned queue if retrieving queue only. 
    * It may be <code>null</code> or empty if the queue does not exist for the edition.
    */
   private Queue<PSDemandWork> getDemandQueueForEdition(int edition, boolean isRetrieveOnly)
   {      
      Queue<PSDemandWork> q = m_demandWork.get(edition);
      if (q == null || q.size() == 0 || (!isRetrieveOnly))
         return q;
      
      Queue<PSDemandWork> qCloned = new LinkedList<>();
      qCloned.addAll(q);
      return qCloned;
   }
   
   /* (non-Javadoc)
    * @see com.percussion.rx.publisher.IPSRxPublisherService#getDemandWorkStatus(long)
    */
   public State getDemandWorkStatus(long requestId)
   {
      Long jobid = getDemandRequestJob(requestId);
      
      if (jobid == null)
      {
         ms_log.debug("getDemandWorkStatus for request, no job");
         return null;
      }
      else
      {
         ms_log.debug("getDemandWorkStatus for request " + requestId + 
               " job " + jobid);
         PSPublishingJob job = m_jobs.get(jobid);
         if (job == null)
         {
            return null;
         }
         job.resetLastAccessTime();
         ms_log.debug("job state is " + job.getState());
         return job.getState();
      }
   }

   /* (non-Javadoc)
    * @see com.percussion.rx.publisher.IPSRxPublisherService#getDemandRequestJob(long)
    */
   public Long getDemandRequestJob(long requestid)
   {
      return m_demandRequestToJob.get(requestid);
   }
   public long queueDemandWork(int editionid, PSDemandWork work)
         throws PSNotFoundException
   {
      return queueDemandWork(editionid, work, DEFAULT_GENERATOR);
   }

   /* (non-Javadoc)
    * @see com.percussion.rx.publisher.IPSRxPublisherService#queueDemandWork(int, com.percussion.rx.publisher.data.PSDemandWork)
    * 
    * NOTE, don't "synchronized" on this method, which is to avoid 
    * lock/sychronize more than one objects (m_demandWork and "this") at the
    * same time.  
    */
   public long queueDemandWork(int editionid, PSDemandWork work, String generator) throws PSNotFoundException
   {
      validateEditionForOnDemandContentLists(editionid, generator);

      ConcurrentLinkedQueue<PSDemandWork> q = null;

      q = m_demandWork.get(editionid);
      if (q == null)
      {
         synchronized (m_demandWork)
         {
            if (q == null)
            {
               q = new  ConcurrentLinkedQueue<>();
               m_demandWork.put(editionid, q);
            }
         }
      }
      q.add(work);

      if (m_demandMonitorThread == null || !m_demandMonitorThread.isAlive())
      {
         m_demandMonitorThread = new Thread(new DemandMonitor(), "DemandPublishingMonitor");
         m_demandMonitorThread.setDaemon(true);
         m_demandMonitorThread.start();
      }
      else
      {
         synchronized (m_demandWork)
         {
            m_demandWork.notify();
         }
      }
      ms_log.info("Queued demand work " + work);
      return work.getRequest();
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.rx.publisher.IPSRxPublisherService#addWorkForJob(long, com.percussion.services.assembly.IPSAssemblyItem)
    */
   public synchronized void addWorksForJob(long jobId,
         List<IPSAssemblyItem> items)
   {
      if (items == null)
      {
         throw new IllegalArgumentException(
               "item may not be null");
      }
      if (items.isEmpty())
         return;
      
      // make sure all items belongs to the specified job
      List<IPSAssemblyItem> wItems = new ArrayList<IPSAssemblyItem>();
      for (IPSAssemblyItem item : items)
      {
         if (item.getJobId() != jobId)
            throw new IllegalArgumentException(
                  "The job ID of all items must be " + jobId);
         
         wItems.add(item);
      }
      
      PSPublishingJob job = m_jobs.get(jobId);
      if (job == null || job.getState().isTerminal())
      {
         throw new IllegalStateException("Job not found or not running");
      }
      job.addWorkItems(wItems);
   }
   
   /**
    * The default location for archiving publishing log into XML files.
    * This is relative to the root (or installation directory) of Rhythmyx.
    */
   public static String DEFAULT_ARCHIVE_LOC = "AppServer/server/rx/deploy/publogs.war";
   
   /**
    * The directory for archived publishing log files. It is default to
    * {@link #DEFAULT_ARCHIVE_LOC}, but it can be overridden by 
    * <code>pubLogArchiveLocation</code> property in server.properties file.
    */
   private String m_archiveLocation = null;
   
   /**
    * Get the archive location for the archived publishing log files. 
    * It is either the default value, {@link #DEFAULT_ARCHIVE_LOC}, or the 
    * <code>pubLogArchiveLocation</code> property in server.properties.
    * If there is no leading slash ('/') or leading drive letter (such as "C:"),
    * then assuming the path is relative to Rhythmyx root (or installation).
    * 
    * @return the normalized archive location. It is an absolute path, which 
    *    does not have trailing slash ('/') and does not have backward 
    *    slash '\\' neither. It can never be <code>null</code> or empty.
    */
   public String getArchiveLocation()
   {
      if (m_archiveLocation != null)
         return m_archiveLocation;
      
      String loc = PSServer.getServerProps().getProperty(
            "pubLogArchiveLocation");
      if (StringUtils.isBlank(loc))
         loc = DEFAULT_ARCHIVE_LOC;
      
      // normalize the path
      loc = loc.replace('\\', '/');
      if (loc.endsWith("/"))
      {
         loc = loc.substring(0, loc.length() - 1);
      }
      
      File fdir = new File(loc);
      if (! fdir.isAbsolute())
      {
         loc = PSServer.getRxDir().getAbsolutePath() + "/" + loc;
         loc = loc.replace('\\', '/');
      }
         
      m_archiveLocation = loc;
      return m_archiveLocation;
   }

   /**
    * The place holder for the default archive directory location.
    */
   String m_defaultArchiveLocation = null;
   
   /**
    * Gets the default archive directory path.
    * @return the default archive path, never <code>null</code> or empty.
    */
   private String getDefaultArchiveLocation()
   {
      if (m_defaultArchiveLocation != null)
         return m_defaultArchiveLocation;
      
      String loc = PSServer.getRxDir().getAbsolutePath() + '/'
            + DEFAULT_ARCHIVE_LOC;      
      m_defaultArchiveLocation = loc.replace('\\', '/');
      
      return m_defaultArchiveLocation; 
   }
   
   /*
    * //see base class method for details
    */
   public String archivePubLog(long jobid, HttpServletRequest req)
      throws IOException, XMLStreamException
   {
      String filename = "publog_" + jobid + ".xml";
      String archiveLink = null;
      String archiveDir = getArchiveLocation();
      String defaultDir = getDefaultArchiveLocation();
      if (req != null && defaultDir.equals(archiveDir))
      {
         String base = req.getScheme() + "://" + req.getServerName() +
         ":" + req.getServerPort();
         archiveLink = base + "/publogs/" + filename;
      }

      File archive = new File(archiveDir, filename);
      OutputStream os = new FileOutputStream(archive);
      Writer archiveWriter = new OutputStreamWriter(os, "UTF-8");
      PSDateFormatISO8601 fmt = new PSDateFormatISO8601();
      IPSPublisherService psvc = PSPublisherServiceLocator
         .getPublisherService();
      IPSPubStatus status = psvc.findPubStatusForJob(jobid);
      List<IPSPubItemStatus> stati = 
         psvc.findPubItemStatusForJob(jobid);
      
      try
      {
         XMLOutputFactory ofact = XMLOutputFactory.newInstance();
         XMLStreamWriter writer = ofact.createXMLStreamWriter(archiveWriter);
         writer.writeStartDocument("UTF-8", "1.0");
         writer.writeCharacters("\n");
         writer.writeStartElement("publication-log");
         writer.writeAttribute("statusid", Long.toString(jobid));
         writer.writeAttribute("start-dtm", fmt.format(status.getStartDate()));
         writer.writeAttribute("end-dtm", fmt.format(status.getEndDate()));
         writer.writeCharacters("\n");
         for(IPSPubItemStatus item : stati)
         {
            writer.writeStartElement("item");
            writer.writeAttribute("url", item.getAssemblyUrl());
            writer.writeAttribute("location", item.getLocation());
            writer.writeAttribute("elapsed", item.getElapsed() == null ? "0"
                  : Integer.toString(item.getElapsed()));
            if (item.getFolderId() != null)
            {
               writer.writeAttribute("folderid", item.getFolderId().toString());
            }
            writer.writeAttribute("reference-id", 
                  Long.toString(item.getReferenceId()));
            writer.writeAttribute("date", 
                  fmt.format(item.getDate()));
            writer.writeAttribute("status", item.getStatus().toString());
            writer.writeAttribute("templateid", item.getTemplateId().toString());
            String message = item.getMessage();
            for(String m : PSPublishingStatusHelper.splitMessages(message))
            {
               writer.writeStartElement("message");
               writer.writeCharacters(m);
               writer.writeEndElement();
               writer.writeCharacters("\n");
            }
            writer.writeEndElement();
            writer.writeCharacters("\n");
         }
         writer.writeEndElement();
         writer.writeEndDocument();
         
         return archiveLink != null ? archiveLink : archive.getAbsolutePath();
      }
      finally
      {
         IOUtils.closeQuietly(archiveWriter);
         IOUtils.closeQuietly(os);
      }
   }
   
   private ReentrantLock writeBlock = new ReentrantLock();

   
}
