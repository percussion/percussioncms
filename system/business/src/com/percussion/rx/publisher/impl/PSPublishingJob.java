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
package com.percussion.rx.publisher.impl;

 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterators;
 import com.percussion.design.objectstore.PSLocator;
 import com.percussion.design.objectstore.PSNotFoundException;
 import com.percussion.error.PSExceptionUtils;
 import com.percussion.extension.IPSExtensionManager;
 import com.percussion.extension.PSExtensionException;
 import com.percussion.extension.PSExtensionRef;
 import com.percussion.extension.PSParameterMismatchException;
 import com.percussion.rx.publisher.IPSEditionTask;
 import com.percussion.rx.publisher.IPSEditionTaskStatusCallback;
 import com.percussion.rx.publisher.IPSPublisherItemStatus;
 import com.percussion.rx.publisher.IPSPublisherJobStatus;
 import com.percussion.rx.publisher.IPSPublisherJobStatus.ItemState;
 import com.percussion.rx.publisher.IPSPublisherJobStatus.State;
 import com.percussion.rx.publisher.IPSPublishingJobStatusCallback;
 import com.percussion.rx.publisher.IPSRxPublisherServiceInternal;
 import com.percussion.rx.publisher.PSPublisherUtils;
 import com.percussion.rx.publisher.data.PSCancelPublishingMessage;
 import com.percussion.rx.publisher.data.PSJobControlMessage;
 import com.percussion.rx.publisher.data.PSPubItemStatus;
 import com.percussion.rx.publisher.data.PSPublisherJobStatus;
 import com.percussion.rx.publisher.jsf.nodes.PSPublishingStatusHelper;
 import com.percussion.security.PSSecurityProvider;
 import com.percussion.server.PSRequest;
 import com.percussion.server.PSServer;
 import com.percussion.server.cache.PSExitFlushCache;
 import com.percussion.services.assembly.IPSAssemblyItem;
 import com.percussion.services.assembly.IPSAssemblyResult;
 import com.percussion.services.assembly.IPSAssemblyService;
 import com.percussion.services.assembly.IPSAssemblyTemplate;
 import com.percussion.services.assembly.PSAssemblyException;
 import com.percussion.services.assembly.PSAssemblyServiceLocator;
 import com.percussion.services.assembly.jexl.PSDocumentUtils;
 import com.percussion.services.assembly.jexl.PSStringUtils;
 import com.percussion.services.catalog.PSTypeEnum;
 import com.percussion.services.guidmgr.IPSGuidManager;
 import com.percussion.services.guidmgr.PSGuidManagerLocator;
 import com.percussion.services.jms.IPSQueueSender;
 import com.percussion.services.publisher.IPSContentList;
 import com.percussion.services.publisher.IPSDeliveryType;
 import com.percussion.services.publisher.IPSEdition;
 import com.percussion.services.publisher.IPSEditionContentList;
 import com.percussion.services.publisher.IPSEditionTaskDef;
 import com.percussion.services.publisher.IPSEditionTaskLog;
 import com.percussion.services.publisher.IPSPubItemStatus;
 import com.percussion.services.publisher.IPSPubStatus;
 import com.percussion.services.publisher.IPSPubStatus.EndingState;
 import com.percussion.services.publisher.IPSPublisherService;
 import com.percussion.services.publisher.PSPublisherServiceLocator;
 import com.percussion.services.publisher.data.PSContentListItem;
 import com.percussion.services.publisher.data.PSContentListResults;
 import com.percussion.services.publisher.data.PSEditionType;
 import com.percussion.services.publisher.data.PSSiteItem;
 import com.percussion.services.publisher.impl.PSIteratorChain;
 import com.percussion.services.sitemgr.IPSSite;
 import com.percussion.services.sitemgr.IPSSiteManager;
 import com.percussion.services.sitemgr.PSSiteManagerLocator;
 import com.percussion.services.utils.xml.PSObjectStream;
 import com.percussion.util.IPSHtmlParameters;
 import com.percussion.util.PSBaseHttpUtils;
 import com.percussion.util.PSStopwatch;
 import com.percussion.util.PSXMLDomUtil;
 import com.percussion.utils.guid.IPSGuid;
 import com.percussion.utils.request.PSRequestInfo;
 import com.percussion.utils.timing.PSTimer;
 import com.percussion.xml.PSXmlDocumentBuilder;
 import com.percussion.xml.PSXmlTreeWalker;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.exception.ExceptionUtils;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;

 import javax.naming.NameNotFoundException;
 import java.io.StringReader;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.concurrent.atomic.AtomicReference;

 import static com.percussion.rx.publisher.PSPublisherUtils.getContentList;
 import static com.percussion.rx.publisher.PSPublisherUtils.getEditionContentList;
 import static org.apache.commons.lang.Validate.notNull;

/**
 * Implement the actual job for publishing. Jobs run for the length of time that
 * the work to be done is active. They are spawned by the publishing service,
 * one per request. The service dispatches job related requests to the correct
 * job based on the id.
 * <p>
 * A job goes through a number of stages:
 * <ul>
 * <li>The edition is loaded and the content lists are evaluated one at a time.
 * <li>The results of the content list are translated into
 * {@link IPSAssemblyItem}s and queued in the assembly queue.
 * <li>This object tracks results from the assembly and delivery process.
 * <li>When all work is finished (everything is assembled and delivered, or has
 * failed) then this thread will cease to run.
 * </ul>
 * When the job is waiting for completion, it is important to call 
 * <code>notifyAll</code> to make sure that any waiting threads are woken since
 * multiple executing threads may be waiting. Those that are not appropriate 
 * will re-enter the wait state.
 * 
 * @author dougrand
 */
public class PSPublishingJob implements Runnable
{
   /**
    * The amount of time in milliseconds to wait for all commits to complete.
    */
   private static final int COMMITTAL_WAIT_TIME = 2 * 1000 * 3600;

   /**
    * The amount of time to wait before polling the job status in milliseconds.
    */
   private static final int POLL_TIME = 1000;

   /**
    * The following are attribute and element definitions for content lists.
    */
   private static final String ATTR_UNPUBLISH = "unpublish";

   /**
    * 
    */
   private static final String ELEM_LOCATION = "location";

   /**
    * 
    */
   private static final String ATTR_DELIVERY = "deliverytype";

   /**
    * 
    */
   private static final String ELEM_CONTENTURL = "contenturl";

   /**
    * 
    */
   private static final String ELEM_CONTENTITEM = "contentitem";

   /**
    * The root element of the Content List in XML
    */
   private static final String ELEM_CONTENTLIST = "contentlist";

   /**
    * The element contains the URL of next page of the Content List
    */
   private static final String ELEM_NEXTPAGE = "PSXNextPage";

    /*
     * The parameter key we use to generate next numbers for each publication
     * which is a unit of publication.
     */
   public static final String NEXTNUMBER_PUBLICATIONS = "PUBLICATIONS";

   /**
    * Marker to identify if a request has been made to rerun this edition as soon 
    * as it has successfully completed.
    */
   private boolean m_rerun=false;

   /**
    * Store error or other message to return to the ui.
    */
   private String m_message;

   /**
    * Store list of updates for job
    */
   public ConcurrentLinkedQueue<IPSPublisherItemStatus> m_updates = new ConcurrentLinkedQueue<>();
   
   
   /**
    * Comparator to sort edition content lists by sequence information.
    */
   static class EditionClistSorter implements Comparator<IPSEditionContentList>
   {
      public int compare(IPSEditionContentList o1, IPSEditionContentList o2)
      {
         int seq1 = o1.getSequence() == null ? 0 : o1.getSequence();
         int seq2 = o2.getSequence() == null ? 0 : o2.getSequence();
         return seq1 - seq2;
      }
   }
   /**
    * The status callback, holds information for use by edition tasks.
    * The callback fetches the status of the job from the database on 
    * first invocation. The lifetime of the instance are only the post
    * edition tasks.
    */
   class EditionStatusCallback implements IPSEditionTaskStatusCallback
   {
      /**
       * The status, initially <code>null</code> and loaded on demand.
       */
      List<IPSPubItemStatus> m_status = null;
      Iterable<IPSPubItemStatus> m_statusIterable = null;
      
      private IPSPubStatus m_currentPubStatus;
      
      public EditionStatusCallback(IPSPubStatus pubStatus)
      {
         super();
         m_currentPubStatus = pubStatus;
      }
      
      public IPSPubStatus getCurrentPubStatus()
      {
         return m_currentPubStatus;
      }

      public List<IPSPubItemStatus> getJobStatus()
      {

   
         if (m_status == null)
         {
            IPSPublisherService psvc = PSPublisherServiceLocator
            .getPublisherService();
            m_status = psvc.findPubItemStatusForJob(m_jobid);
         }
         
         return m_status;
      }

      public Iterable<IPSPubItemStatus> getIterableJobStatus()
      {
         if (m_statusIterable == null)
         {
            IPSPublisherService psvc = PSPublisherServiceLocator
            .getPublisherService();
            m_statusIterable = psvc.findPubItemStatusForJobIterable(m_jobid);
         }
         
         return m_statusIterable;
      }
   }
   
   /**
    * Logger.
    */
   private static final Logger log = LogManager.getLogger(PSPublishingJob.class);

   /**
    * The assembly service, never <code>null</code>.
    */
   private static IPSAssemblyService ms_asm = PSAssemblyServiceLocator
         .getAssemblyService();

   /**
    * The job id.
    */
   long m_jobid;

   /**
    * The edition being run, never <code>null</code> after ctor.
    */
   private IPSGuid m_editionId;

   /**
    * The name of the edition, it will be set before starting the job.
    */
   private IPSEdition m_edition;
   
   /**
    * Each item is tracked. When all items are in a finished state, either
    * failed or delivered, the job is complete.
    */
   private Map<Long, AtomicReference<IPSPublisherJobStatus.ItemState>> m_items 
      = new ConcurrentHashMap<>();

   /**
    * The state of the job.
    */
   private State m_jobState = State.INITIAL;

   /**
    * Determines if any (job level) error occurs during the publishing process.
    * Default to <code>false</code>. This is job level error or exception,
    * such as errors during executing ContentList, commit, unpublishing, ...etc,
    * but it is not the item level errors. The job level errors will be logged
    * by log4j (in server.log), but the item level errors may log into
    * publishing history table.
    */
   private boolean m_hasJobError = false;
   
   /**
    * Determines if there is any edition task error during the publishing
    * process. Default to <code>false</code>.
    */
   private boolean m_hasTaskError = false;
   
   /**
    * The publisher queue sender. Initialized by constructor, never 
    * <code>null</code> or modified after that.
    */
   private IPSQueueSender m_publishSender = null;   

   /**
    * This tracks whether the job was canceled or not. If a job is canceled,
    * then the final state is canceled and not complete.
    */
   private boolean m_canceled = false;
   
   /**
    * This tracks whether the {@link #acknowledgeCommit(boolean)}} method has been
    * called. The job waits on this value when all work has been completed
    * and it is waiting for the signal.
    */
   private volatile boolean m_committed = false;

   /**
    * The current content list being run. This value will be <code>null</code>
    * if there is no content list being run.
    */
   private String m_currentContentList = null;

   /**
    * Last access time, updated by the publisher business service when this
    * object is accessed. It is used to determine when jobs can be reaped.
    */
   private long m_lastAccessTime = System.currentTimeMillis();

   /**
    * The thread that actually runs the job, never <code>null</code> after ctor.
    */
   private Thread m_jobThread = null;

   /**
    * The business publisher service, wired in during ctor, never changed
    * afterward, never <code>null</code>.
    */
   private IPSRxPublisherServiceInternal m_rxpubsvc;

   /**
    * The edition tasks that are part of this job, initialized on first use.
    */
   private List<IPSEditionTaskDef> m_tasks;

   /**
    * The callback for status, <code>null</code> until the post edition tasks
    * are being run, and <code>null</code> after they are run as well.
    */
   private IPSEditionTaskStatusCallback m_statusCallback;
   
   /**
    * The notification call back object. It is <code>null</code> if there is
    * no need to perform the notification; otherwise this is called when the
    * job is finished. 
    */
   private IPSPublishingJobStatusCallback m_notifyCallback;
   
   /**
    * Record when this job was created.
    */
   private Date m_startTime = new Date();
   
   /**
    * Record the end time, <code>null</code> until then.
    */
   private Date m_endTime = null;

   /**
    * The site's guid, never <code>null</code>.
    */
   private IPSGuid m_siteguid;

   /**
    * The site's guid, never <code>null</code>.
    */
   private IPSGuid m_pubserverguid;

   /**
    * The total number of items in QUEUE state.
    */
   private AtomicInteger m_queued = new AtomicInteger(0);

   /**
    * The total number of items in PAGED state.
    */
   private AtomicInteger m_paged = new AtomicInteger(0);
   
   /**
    * The total number of items in PAGED state.
    */
   private AtomicInteger m_deliveryQueued = new AtomicInteger(0);
   
   /**
    * The total number of items in FAILED state.
    */
   private AtomicInteger m_failed = new AtomicInteger(0);

   /**
    * The total number of items in ASSEMBLED state.
    */
   private AtomicInteger m_assembled = new AtomicInteger(0);
   
   /**
    * The total number of items PREPARED_FOR_DELIVERY state.
    */
   private AtomicInteger m_preparedForDelivery = new AtomicInteger(0);
   
   /**
    * The total number of items in DELIVERY state.
    */
   private AtomicInteger m_delivered = new AtomicInteger(0);
   
   /**
    * Publishing job timeout in milli-seconds. The job will be aborted 
    * if it has not received any messages relate to this job.  
    * It is initialized by constructor, never modified after that.
    * <p>
    * This is a workaround in case the JMS queue malfunction. 
    */
   private final int m_publishJobTimeout;
   
   /**
    * Publishing queue timeout in milli-seconds. The job will be aborted 
    * if it has not received any messages from the publishing queue.  
    * It is initialized by constructor, never modified after that.
    * <p>
    * This is a workaround in case the JMS queue malfunction. 
    */
   private final int m_publishQueueTimeout;
   
   /**
    * Records the last time the status has been updated.
    */
   private long m_lastStatusUpdateTime = System.currentTimeMillis();
   
   /**
    * Records the last time the notification of a job status has been broadcast.
    */
   private long m_lastNotificationTime = System.currentTimeMillis();

   private boolean m_cleanPublish;

 

   /**
    * Default constructor for unit test use only.
    */
   PSPublishingJob()
   {
      m_publishJobTimeout = 60 * 60000; // 60 minutes
      m_publishQueueTimeout = 10 * 60000; // 10 minutes
   }
   
   /**
    * Create the job, which starts the actual work as well.
    * 
    * @param publisherSender the publisher queue sender, never <code>null</code>.
    * @param edition the edition being used for this job, never 
    *   <code>null</code>.
    * @param rxpub the business publisher service, never <code>null</code>.
    */
   public PSPublishingJob(IPSQueueSender publisherSender,
         IPSGuid edition, IPSRxPublisherServiceInternal rxpub,
         IPSPublishingJobStatusCallback notifyCallback) 
   {
      notNull(publisherSender, "publisherSender may not be null");
      notNull(edition, "edition may not be null");
      notNull(rxpub, "rxpub may not be null");

      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      m_jobid = gmgr.createId(NEXTNUMBER_PUBLICATIONS);

      m_editionId = edition;
      m_publishSender = publisherSender;
      m_rxpubsvc = rxpub;
      m_notifyCallback = notifyCallback;
      m_jobThread = new Thread(this, "PublishingJob-" + m_jobid);
      
      m_cleanPublish = checkIsFullPublish(edition);
      
      
      int jobTimeout = rxpub.getConfigurationBean().getPublishJobTimeout();
      int queueTimeout = rxpub.getConfigurationBean().getPublishQueueTimeout();
      
      m_publishJobTimeout = jobTimeout * 60000;
      m_publishQueueTimeout = queueTimeout * 60000;
      
      log.debug("Publish job time out is set to {} minutes.",jobTimeout);
      log.debug("Publish queue time out is set to {} minutes.", queueTimeout);
   }
  
   /**
    * Need a mechanism for the Metadata delivery handler to check if a job is a full job
    * Unfortunately this information is stored on the content list and not the
    * edition,  the mechanism is also different for CM1 and Rhythmyx. 
    * We cannot guarantee with Rhythmyx that a job without any content lists
    * marked as incremental is the main full edition for a site.  This
    * code only returns true for a CM1 Full publish.
    * Could later be expanded to mark an edition to cleaup for other handlers like
    * ftp or filesystem
    * 
    * @param edition
    * @return
    */
   private boolean checkIsFullPublish(IPSGuid edition)
   {
      TreeSet<IPSEditionContentList> clists = getEditionContentList(edition);
      for (IPSEditionContentList edclist : clists)
      {
         IPSContentList clist = PSPublisherUtils.getContentList(edclist);
         if (clist.getType().equals(IPSContentList.Type.INCREMENTAL))
            return false;
         if (!clist.getExpander().equals("Java/global/percussion/system/perc_ResourceTemplateExpander"))
            return false;
         if (!clist.getGenerator().equals("Java/global/percussion/system/sys_SearchGenerator"))
            return false;
      }
      return true;
   }

   /**
    * Start the job thread running.
    */
   public void startJob()
   {
      m_jobThread.start();
   }

   /**
    * Updates the last status and last notification
    * time stamps.
    */
   private void updateAccessTime()
   {
      m_lastStatusUpdateTime = System.currentTimeMillis();
      m_lastNotificationTime = System.currentTimeMillis();
   }
   
   /**
    * The notification is called whenever an item state of a job is updated
    * (after the system received a message and processed the message).
    * This is used as a heart-beat of the publishing queue. 
    */
   public void notifyStatusUpdate()
   {
      m_lastNotificationTime = System.currentTimeMillis();
   }
   
   /**
    * Test to see if the publishing job has timed out.
    * Checks see if the queue heart beat and job timeout have
    * been exceeded.
    * No error will be thrown only logged.
    * @return <code>true</code> timeout has been reached.
    */
   private boolean isTimeout()
   {
      // test queue heartbeat timeout
      long delta = System.currentTimeMillis() - m_lastNotificationTime;
      if (m_publishQueueTimeout > 0 && delta > m_publishQueueTimeout)
      {
         logTimeoutWarning(true);
         cancel(false);
         return true;            
      }

      // test job timeout
      delta = System.currentTimeMillis() - m_lastStatusUpdateTime;
      if (m_publishJobTimeout > 0 && delta > m_publishJobTimeout)
      {
         logTimeoutWarning(false);
         cancel(false);
         return true;
      }
      
      return false;
   }

   /**
    * This is used for debugging only. It calculates the
    * summary of {@link #m_items}.
    */
   private class StateSummaryLogger
   {
      int mi_queue = 0;
      int mi_assembled = 0;
      int mi_failed = 0;
      int mi_preparedForDelivery = 0;
      int mi_delivered = 0;
      int mi_cancelled = 0;
      int mi_paged = 0;
      int mi_deliveryQueued = 0;

      public StateSummaryLogger()
      {
         for (AtomicReference<IPSPublisherJobStatus.ItemState> state : m_items.values())
         {
            updateCount(state.get());         
         }
      }
      
      /*
       * (non-Javadoc)
       * @see java.lang.Object#toString()
       */
      public String toString()
      {
         return "queue = " + mi_queue + ", assembled = " + mi_assembled
               + ", failed = " + mi_failed + ", preparedForDelivery = "
               + mi_preparedForDelivery + ", delivery queued = " + mi_deliveryQueued
               + ", delivered = " + mi_delivered
               + ", mi_cancelled = " + mi_cancelled + ", mi_paged = "
               + mi_paged;
      }

      /**
       * Updates the summary with the specified state.
       * @param state the state, assumed not <code>null</code>.
       */
      private void updateCount(IPSPublisherJobStatus.ItemState state)
      {
         switch (state)
         {
            case QUEUED:
               mi_queue++;
               break;
            case ASSEMBLED:
               mi_assembled++;
               break;
            case FAILED:
               mi_failed++;
               break;
            case PREPARED_FOR_DELIVERY:
               mi_preparedForDelivery++;
               break;
            case DELIVERED:
               mi_delivered++;
               break;
            case CANCELLED:
               mi_cancelled++;
               break;
            case PAGED:
               mi_paged++;
               break;
            case DELIVERY_QUEUED:
               mi_deliveryQueued++;
               break;
         }
      }
   }

   /**
    * Logs warning messages when timeout occurs.
    * 
    * @param isQueueTimeout <code>true</code> if is queue timeout; 
    * otherwise it is the job timeout.
    */
   private void logTimeoutWarning(boolean isQueueTimeout)
   {
      if (isQueueTimeout)
      {
         log.warn("Edition "
                     + getEditionLabel()
                     + " has been aborted by publish queue timed out. The job is still waiting for "
                     + m_queued + " unprocessed items.");
      }
      else
      {
         log.warn("Edition "
                     + getEditionLabel()
                     + " has been aborted by job timed out. The job is still waiting for "
                     + m_queued + " unprocessed items.");
      }

      StateSummaryLogger stateSum = new StateSummaryLogger();
      log.debug("Item map summary: " + stateSum.toString());

   }
   
   /**
    * First this method ensures that various referenced design objects such as
    * the edition and site can be loaded successfully. 
    * <p>
    * Then the pre-edition tasks are run.
    * <p>
    * The run method loads the edition and edition content lists. Each content
    * list is generated, and added to the assembly queue. Then the thread waits
    * for all outstanding work to be completed, as determined by the item
    * status.
    * <p>
    * After the work is completed, the job end message is sent around to the
    * processors, who can free resources and commit work held for the job.
    * <p>
    * Then the post-edition tasks are run.
    */
   public void run()
   {
      Thread.currentThread().setPriority(4);
      PSStopwatch sw = new PSStopwatch();
      Date start_time = new Date();
      IPSEdition ed = null;
      IPSSite site = null;
      IPSPublisherService psvc = PSPublisherServiceLocator
         .getPublisherService();
      
      try
      {
         sw.start();
         
         IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
         IPSSiteManager smgr = PSSiteManagerLocator.getSiteManager();

         psvc.initPublishingStatus(m_jobid, new Date(), m_editionId);
         ed = psvc.loadEdition(m_editionId);
         if (ed == null)
         {
            log.error("Couldn't find edition: " + m_editionId.getUUID()
                  + " exiting");
            return;
         }
         m_edition = ed;
         
         IPSGuid siteid = ed.getSiteId();
         if (siteid == null)
         {
            log.error("Destination site must be configured for edition "
                  + getEditionLabel() + " exiting");
            return;
         }
         m_siteguid = siteid;
         site = smgr.loadUnmodifiableSite(m_siteguid);
         m_pubserverguid = ed.getPubServerId();
         changeState(State.PRETASKS);
         flushAllCacheIfInPublishHub();
         runEditionTasks(ed, site, start_time, null, -1);
         sendJobStartMessage(m_siteguid, m_pubserverguid);

         changeState(State.QUEUEING);
         /*
          * TODO ADAM GENT need to handle this as an iterator.
          */
         Set<PSSiteItemKey> unpublishedKeys = new HashSet<>();
         if (ed.getEditionType().equals(PSEditionType.NORMAL))
         {
            log.debug("Queuing unpblishing Items");
            unpublishedKeys = processUnpublishingItems(psvc, ed, site);
         }
         log.debug("Queuing work Items");
         if (!queueWorkItems(psvc, ed, site, unpublishedKeys))
         {
            // Canceled
            return;
         }
         if (!m_canceled)
         {
            changeState(State.WORKING);
            log.info("All items for job "+m_jobid+ " Queued. State changed to WORKING" );
            debugStatusCounts();
            waitForJobCompletion();
         }
      }
      catch (Exception e)
      {
         if(e.getCause() instanceof NameNotFoundException) {
            cancel(false);
            psvc.cancelUnfinishedJobItems(this.m_jobid);
            String msg = "User Needs to restart jetty for initializing new datasource created for publishing.";
            m_publishSender.sendMessage(msg, IPSQueueSender.PRIORITY_HIGHEST);
            changeState(State.PUBSERVERNEWDBCONFIG);
            log.error(msg, e);
         }else {

            cancel(false);
            m_message = ExceptionUtils.getRootCauseMessage(e);
            log.error("Cancelling due to error: "
                    + "Unexpected problem while running publishing Job id="
                    + m_jobid + ", Edition " + getLogLabel(ed), e);
         }
      }
      finally
      {
         finishEdition(sw, start_time, ed, site, psvc);
      }
   }

   public void waitForStatusWrite()
   {
      while (m_updates.size()>0)
      {
        int updated=m_rxpubsvc.flushStatusToDatabase(m_updates);
        log.info("Updated database with "+updated+" remaining status items "+m_updates.size()+" remaining" );
      }
      
      /*
      try
      {
      while (!m_updates.isEmpty())
      {
            ms_log.debug("Waiting for all job statuses to be flushed to DB, queue size = "+m_updates.size());
            
           Thread.sleep(5000); 
      }
      }
      catch (InterruptedException e)
      {
         Thread.currentThread().interrupt(); 
         ms_log.debug("waitForStatusWrite interrupted");
      }
      */
      
   }
   
   /**
    * Finishes the edition, such as commit the publishing, process edition tasks, ...etc.
    * 
    * @param editionTimer the edition timer, not <code>null</code>.
    * @param start_time the starting time of the edition, not <code>null</code>.
    * @param ed the edition, not <code>null</code>.
    * @param site the published site, not <code>null</code>.
    * @param psvc the publisher service, not <code>null</code>.
    */
   private void finishEdition(PSStopwatch editionTimer, Date start_time, IPSEdition ed,
         IPSSite site, IPSPublisherService psvc)
   {
      if (!m_canceled)
         commitJobAndEndingJob();
         
      waitForStatusWrite();
      
      editionTimer.stop();
      log.info("Main Publishing Job '" + m_jobid + "' for edition " + getLogLabel(ed) + " completed in " + editionTimer);
      // Capture the end time
      Date end_time = new Date();
      
 
      
      IPSPubStatus pubStatus = psvc.updateCounts(m_jobid);
      
      if (! m_canceled)
      {
         PSStopwatch tasksTimer = new PSStopwatch();
         tasksTimer.start();
         log.info("Running Post Edition Tasks of '" + m_jobid + "' for edition " + getLogLabel(ed));
         runPostTasks(ed, site, pubStatus, start_time, end_time, (long)editionTimer.elapsed());
         tasksTimer.stop();
         log.info("Finished Post Edition Tasks of '" + m_jobid + "' for edition " + getLogLabel(ed) + " completed in " + tasksTimer);
      }
      
      EndingState endStatus = getEndingStatus();
      psvc.finishedPublishingStatus(m_jobid, new Date(), endStatus);
      changeState(PSPublishingStatusHelper.getJobState(endStatus));
      
       
      psvc.updateItemPubDateByJob(m_jobid,start_time);
      
     
      m_endTime = new Date();
      log.info("Publishing Finished '" + m_jobid + "' for edition " + getLogLabel(ed));
      if (m_notifyCallback != null)
         m_notifyCallback.notifyStatus(getStatus());
   }

   

   /**
    * Inject a request object into current thread if there is no such object in current thread.
    * The request object is needed while executing a content list
    * specified Edition. For example, generating location and/or from
    * a custom item filter, ...etc.
    * 
    * Note, Caller should call {@link #resetRequestInfo()} after the request is no use any more
    * if it returns <code>true</code>.
    * 
    * @return <code>true</code> if injected a request object; otherwise
    * there is a request in current thread already.
    * 
    */
   private boolean initRequestInfo()
   {
      boolean isCreatedRequest = false;
      
      PSRequest req = (PSRequest) PSRequestInfo
      .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      if (req == null)
      {
         isCreatedRequest = true;
         req = PSRequest.getContextForRequest();
         PSRequestInfo.initRequestInfo((Map<String,Object>) null);
         PSRequestInfo.setRequestInfo(PSRequestInfo.KEY_PSREQUEST, req);
         PSRequestInfo.setRequestInfo(PSRequestInfo.KEY_USER, PSSecurityProvider.INTERNAL_USER_NAME);
      }
      
      return isCreatedRequest;
   }
   
   /**
    * Reset whatever has done by {@link #initRequestInfo()}.
    */
   private void resetRequestInfo()
   {
      PSRequestInfo.resetRequestInfo();
   }
   
   /**
    * Flushes all caches, which simply calls the sys_FlushCache Pre-Exit.
    * This sys_FlushCache Exit only flushes all caches for a Publishing Hub,
    * but do nothing for a Content Hub. This is assuming there is only one
    * Content Hub and one or more Publishing Hub in a Rhythmyx server cluster
    * envirement.
    */
   private void flushAllCacheIfInPublishHub()
   {
      PSStopwatch sw = new PSStopwatch();
      sw.start();
      PSExitFlushCache flushCache = new PSExitFlushCache();
      try 
      {
         flushCache.preProcessRequest(null, null);
      } 
      catch (PSParameterMismatchException e) 
      {
         // should never be here.
         log.error("Flush cache failed", e);
      }
      sw.stop();
      log.debug("Done flushAllCache " + sw.toString());
   }
   
   /**
    * Commit the job if it has not been canceled. Send message to end the job
    * assumed all items have been published, delivered and committed (if the 
    * job has not been canceled).
    */
   private void commitJobAndEndingJob()
   {
      changeState(State.COMMITTING);
      log.info("Entering COMITTING State for job "+m_jobid);
      // Wait for a while. The high priority message will run around and
      // will eventually cause the acknowledge message to be called.
      synchronized (this)
      {
         try
         {
            sendJobEndMessage();
         }
         catch(Exception e)
         {
            log.error("Could not send job end message!", e);
            // Can't wait if we didn't send end message
            m_committed = true;
         }
         while(! m_committed)
         {
            try
            {
               wait(COMMITTAL_WAIT_TIME);
            }
            catch (InterruptedException e)
            {
               log.error(PSExceptionUtils.getMessageForLog(e));
               Thread.currentThread().interrupt();
              
            }
         }
      } 
   }

   /**
    * Perform all post tasks for the given Edition (if there is any).
    * 
    * @param edition the Edition, assumed not <code>null</code>.
    * @param site the Site of the Edition, assumed not <code>null</code>.
    * @param start_time starting time of the job, assumed not <code>null</code>.
    * @param end_time ending time of the job, assumed not <code>null</code>.
    * @param duration duration of the job, assumed greater than <code>0</code>.
    */
   private void runPostTasks(IPSEdition edition, IPSSite site,
         IPSPubStatus pubStatus,
         Date start_time, Date end_time, long duration)
   {
      changeState(State.POSTTASKS);
      m_statusCallback = new EditionStatusCallback(pubStatus);
      boolean isCreatedRequest = false;
      try
      {
         isCreatedRequest = initRequestInfo();
         runEditionTasks(
               edition, site, start_time, end_time, duration);
      }
      catch(Exception e)
      {
         // Ignore at this point
      }
      finally
      {
         if (isCreatedRequest)
            resetRequestInfo();
      }
      m_statusCallback = null;      
   }

   /**
    * @return equivalent ending state of {@link #m_jobState}, never 
    *    <code>null</code>.
    */
   private EndingState getEndingStatus()
   {
      if (m_canceled)
      {
         if (m_jobState.ordinal() == State.PUBSERVERNEWDBCONFIG.ordinal())
            return EndingState.RESTARTNEEDED;
        else if (m_jobState.ordinal() == State.ABORTED.ordinal())
            return EndingState.ABORTED;
         else
            return EndingState.CANCELED_BY_USER;
      }

      if (m_hasJobError || m_hasTaskError ||  m_failed.get() > 0)
      {
         return EndingState.COMPLETED_W_FAILURE;
      }
      else
      {
         return EndingState.COMPLETED;
      }
   }

   /**
    * Discover and queue items that should be unpublished. Items are unpublished
    * for a number of reasons:
    * <ul>
    * <li>The original item has been purged or archived
    * <li>The item is no longer in the original source folder, i.e. it has been
    * moved within the site or removed from the site.
    * </ul>
    * Unpublish then falls into two categories. Historically, some publishing
    * handlers required that the item be reassembled, c.f. the database 
    * publishing handler. However, the new delivery handlers do not require 
    * this. If a handler does require this and the item has been purged then
    * unpublishing will fail for that item since the original item is missing.
    * 
    * @param psvc the publishing service, assumed never <code>null</code>.
    * @param ed the edition, assumed never <code>null</code>.
    * @param site the site, assumed never <code>null</code>.
    * 
    * @return a set of site item IDs that have been sent to publishing queue
    * for un-publish. 
    */
   private Set<PSSiteItemKey> processUnpublishingItems(IPSPublisherService psvc,
         IPSEdition ed, IPSSite site)
   {
      PSTimer timer = new PSTimer(log);
      log.debug("Begin unpublish items for Edition " + getLogLabel(ed));
      
      List<Long> refs = psvc.findReferenceIdsToUnpublishByServer(ed.getPubServerOrSiteId(), site.getUnpublishFlags());
      List<IPSPubItemStatus> stati =
            psvc.findPubItemStatusForReferenceIds(refs);
      List<PSSiteItem> siteitems = psvc.findSiteItemsForReferenceIds(refs);
      Map<Long,PSSiteItem> refToSiteItem = new HashMap<>();
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
      PSStringUtils sutils = new PSStringUtils();
      
      for(PSSiteItem item : siteitems)
      {
         refToSiteItem.put(item.referenceId, item);
      }
      
      List<IPSAssemblyItem> queuedItems = new ArrayList<>();
      for(IPSPubItemStatus status : stati)
      {
         if (log.isDebugEnabled())
            logUnpublishedItemStatus(status);
         
         if (m_canceled) return Collections.emptySet(); 
         PSSiteItem siteitem = refToSiteItem.get(status.getReferenceId());
         IPSAssemblyItem item = asm.createAssemblyItem();
         item.setPublish(false);
         item.setUnpublishRefId(status.getReferenceId());
         String url = status.getAssemblyUrl();
      
         if(StringUtils.contains(url, "sys_publish=publish"))
         {
        	 url = StringUtils.replace(url, "sys_publish=publish", "sys_publish=unpublish");
         }
         // Get the query portion of the url
         int q = url != null ? url.indexOf('?') : -1;
         if (q < 0)
         {
            String message = "Bad content url found: " + url;
            log.warn(message);
            recordFailureStatus(item, message);
            continue;
         }
         String query = url.substring(q + 1);
         try
         {
            IPSDeliveryType dtype = 
               psvc.loadDeliveryType(status.getDeliveryType());
            Map<String, String> params = sutils.stringToMap(query);
            for (Map.Entry<String, String> entry : params.entrySet())
            {
               item.setParameterValue(entry.getKey(), entry.getValue());
            }            
            item.setDeliveryType(status.getDeliveryType());
            item.setDeliveryPath(status.getLocation());
            if (status.getFolderId() != null && status.getFolderId() != 0)
            {
               // the folder ID may be negative when the item is moved
               // to different folder.
               int folderId = status.getFolderId() > 0
                     ? status.getFolderId()
                     : status.getFolderId() * -1;
               item.setFolderId(folderId);
               item.setParameterValue(IPSHtmlParameters.SYS_FOLDERID, String.valueOf(folderId));
            }
            item.setJobId(m_jobid);
            item.setReferenceId(gmgr.createLongId(PSTypeEnum.PUB_REFERENCE_ID));
            item.setParameterValue(IPSHtmlParameters.SYS_TEMPLATE, 
                  status.getTemplateId().toString());
            Map<String, String> queryParams = getUrlParams(status
                  .getAssemblyUrl());

            //assuming here that the 2 params don't appear more than once
            if (!queryParams.containsKey(IPSHtmlParameters.SYS_REVISION)
                  || !queryParams.containsKey(IPSHtmlParameters.SYS_CONTENTID))
            {
               handleFailure(item,
                  "Problem processing unpublish item for reference id: "
                  + status.getReferenceId()
                  + " - Couldn't extract contentid or revision from assemblyUrl: "
                  + status.getAssemblyUrl(), null);
               return Collections.emptySet();
            }
            
            item.setId(gmgr.makeGuid(new PSLocator(
                  queryParams.get(IPSHtmlParameters.SYS_CONTENTID), 
                  queryParams.get(IPSHtmlParameters.SYS_REVISION))));
            item.setAssemblyUrl(url);
            
            Long pubServerId = ed.getPubServerId() == null ? null : (long)ed.getPubServerId().getUUID();
            item.setPubServerId(pubServerId);
            
            processAndQueueUnpublishItem(siteitem.contextId, 
                  siteitem.unpublishInfo, item, dtype, queuedItems);
         }
         catch (Exception e)
         {
            handleFailure(item,
                  "Problem processing unpublish item for reference id: "
                        + status.getReferenceId(), e);
         }
      }
      if (!queuedItems.isEmpty())
         sendItemsToPubQueue(queuedItems.iterator(), ed);

      timer.logElapsed("Finish unpublish " + queuedItems.size() + " items for Edition " 
            + getLogLabel(ed));

      return getSiteItemKeys(queuedItems);
   }

   /**
    * Logs the specified item status for un-publish.
    * 
    * @param status the item status in question, assumed not <code>null</code>.
    */
   private void logUnpublishedItemStatus(IPSPubItemStatus status)
   {
      if (!log.isDebugEnabled())
         return;
      
      log.debug("Unpublishing Ref-ID: " + status.getReferenceId()
            + ", Content-ID: " + status.getContentId() + ", Folder-ID: "
            + status.getFolderId() + ", URL: " + status.getAssemblyUrl()
            + ", Location: " + status.getLocation());
   }

   /**
    * Gets the message priority of submitting worker items for the given
    * Edition.
    * 
    * @param edition the Edition, assumed not <code>null</code>.
    * 
    * @return the message queue priority.
    */
   private int getMsgPriority(IPSEdition edition)
   {
      return edition.getPriority().getValue();
   }
   
   /**
    * Get the parameters from a given URL.
    * @param url the URL in question, expected to be in the form of URL.
    * @return the parameters in a map, never <code>null</code>, but may be 
    *    empty if there is no parameters or query string in the URL.
    */
   private Map<String, String> getUrlParams(String url)
   {
      Map<String, Object> params = PSBaseHttpUtils.parseQueryParamsString(url,
            false, false);

      Map<String, String> result = new HashMap<>();
      for (Map.Entry<String, Object> entry : params.entrySet())
         result.put(entry.getKey(), entry.getValue().toString());
      
      return result;
   }
   /**
    * 
    * @param item The item being processed that failed. Assumed not
    *        <code>null</code>.
    * @param msg This optional text is written to the logger and as part of the
    *        message in the status. If no exception is provided, it is also used
    *        as the status message. May be <code>null</code> or empty.
    * @param e If this failure was due to an exception, pass it here. May be
    *        <code>null</code>.
    */
   private void handleFailure(IPSAssemblyItem item, String msg, Exception e)
   {
      log.warn(msg, e);
      recordFailureStatus(item, e != null ? e.getLocalizedMessage() : msg);
   }

   /**
    * Unpublishing items need to be setup with the information contained
    * in the corresponding site item before being sent. Additionally, there
    * is the potential for an item to show up that does not have a component
    * summary, but for which the delivery type demands that the item be 
    * assembled. These items must fail.
    * 
    * @param context the delivery context
    * @param unpublishingInfo the unpublishing information, 
    *       may be <code>null</code>
    * @param item the work item, assumed never <code>null</code>.
    * @param dtype the delivery type, assumed never <code>null</code>.
    * @param queuedItems this is used to collect the queued item, so that it
    *    will be send to the queue in bulk (or send all collected items to the
    *    publishing queue in one shot). Assumed not <code>null</code>, but may
    *    be empty. 
    */
   void processAndQueueUnpublishItem(int context, byte[] unpublishingInfo, 
         IPSAssemblyItem item, IPSDeliveryType dtype, List<IPSAssemblyItem> queuedItems)
   {
      item.setElapsed(0);
      item.setStatus(IPSAssemblyResult.Status.SUCCESS);
      item.setDeliveryContext(context);
      if ((!dtype.isUnpublishingRequiresAssembly()) && unpublishingInfo != null)
      {
         item.setResultData(unpublishingInfo);
      }
      if (unpublishingInfo == null 
            && (!dtype.isUnpublishingRequiresAssembly()))
      {
         String msg = "Cannot unpublish item " + item.getId()
               + " for delivery type " + dtype.getName()
               + " which requires unpublishing information.";
         log.error(msg);
         recordFailureStatus(item, msg);
      }
      else
      {
         updateJobWithWorkItem(item, queuedItems);
      }
   }

   /**
    * Calls the service to record a failure, generally from queuing or preparing
    * an item for assembly or unpublishing.
    * @param work the work item, assumed never <code>null</code>.
    * @param messages an optional additional message or messages
    */
   private void recordFailureStatus(IPSAssemblyItem work, Object... messages)
   {
      IPSPublisherService psvc = PSPublisherServiceLocator
            .getPublisherService();
      if (work.getTemplate() == null)
      {
         IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
         try
         {
            asm.handleItemTemplates(Collections.singletonList(work));
         }
         catch (PSAssemblyException e)
         {
            log.error("Problem trying to fetch template information "
                  + "- skipping status", e);
            return;
         }
      }
      PSPubItemStatus status = new PSPubItemStatus(work.getReferenceId(),
            work.getJobId(), work.getPubServerId(), work.getDeliveryContext(), IPSPublisherJobStatus.ItemState.FAILED);
      status.extractInfo(work);
      for (Object message : messages)
      {
         String msg = message.toString();
         if (StringUtils.isNotEmpty(msg))
         {
            status.addMessage(msg);
         }
      }
      updateItemState(status);      
      updateJobStatus(work.getReferenceId(), ItemState.FAILED);
   }
   
   /**
    * Load and run edition tasks for the specified edition. For post edition
    * tasks this method first determines whether the job was entirely 
    * successful.
    * 
    * @param edition the edition, assumed never <code>null</code>.
    * @param site the site, assumed never <code>null</code>.
    * @param start_time the start time for the job.
    * @param end_time the end time for the job, <code>null</code> for pre
    * edition tasks.
    * @param duration the duration of the job. If <code>-1</code> then the job
    * has not yet run and the pre-edition tasks should be run. Otherwise the
    * post edition tasks should be run.
    */
   private void runEditionTasks(IPSEdition edition, IPSSite site,
         Date start_time, Date end_time, long duration)
   {
      final IPSPublisherService psvc = 
         PSPublisherServiceLocator.getPublisherService();
      if (m_tasks == null)
      {
         m_tasks = 
            psvc.loadEditionTasks(edition.getGUID());
      }
      boolean post = m_statusCallback != null;
      boolean success;
      
      if (post)
      {
         IPSPublisherJobStatus status = getStatus();
         success = status.countFailedItems() == 0;
      }
      else
      {
         success = true;
      }
      
      IPSEditionTaskLog log;
      
      for(IPSEditionTaskDef task : m_tasks)
      {
         // Tasks will be in order, so we can just run through and run each
         // one
         if ((post && task.getSequence() >= 0) ||
               (!post && task.getSequence() < 0))
         {
            log = psvc.createEditionTaskLog();
            log.setTaskId(task.getTaskId());
            log.setJobId(m_jobid);
            log.setStatus(false);
            
            PSPublishingJob.log.info("Running edition task " + task.getExtensionName());
            PSStopwatch sw = new PSStopwatch();
            sw.start();
            try
            {
               IPSEditionTask ext = getTask(task.getExtensionName());
               ext.perform(edition, site, start_time, end_time, m_jobid,
                     duration, success, task.getParams(), 
                     m_statusCallback);
               log.setStatus(true);
            }
            catch(Exception e)
            {
               m_hasTaskError = true;
               
               String msg = "Failure running edition task name="
                     + task.getExtensionName()
                     + ". The underlying error is: " + e.getLocalizedMessage();
               String serverLog = "Failed to run edition task name="
                  + task.getExtensionName()
                  + ", while running publishing Job id=" + m_jobid
                  + ", Edition " + getLogLabel(edition) + ".";
               if (task.getContinueOnFailure())
               {
                  PSPublishingJob.log.warn(serverLog, e);
                  log.setMessage(msg);
               }
               else
               {
                  if (post)
                  {
                     msg = msg + ", terminating task processing";
                     PSPublishingJob.log.error(serverLog, e);
                     log.setMessage(msg);
                     return;
                  }
                  else
                  {
                     msg = msg + ", terminating edition";
                     PSPublishingJob.log.error(serverLog, e);
                     log.setMessage(msg);
                     throw new RuntimeException(e);
                  }
               }
            }
            finally
            {
               sw.stop();
               log.setElapsed((int) sw.elapsed());
               psvc.saveEditionTaskLog(log);
            }
         }
      }
      
   }
   
   /**
    * Load the task using the extensions manager.
    * @param name the name of the task, never <code>null</code> or empty.
    * @return the extension instance, never <code>null</code>.
    * @throws PSNotFoundException
    * @throws PSExtensionException
    */
   static IPSEditionTask getTask(String name) 
   throws PSNotFoundException, PSExtensionException
   {
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name may not be null or empty");
      }
      IPSExtensionManager emgr = PSServer.getExtensionManager(null);
      PSExtensionRef ref = new PSExtensionRef(name);
      return (IPSEditionTask) emgr.prepareExtension(ref, null);
   }

   /**
    * Queue all the work items in the edition. This method gets the status
    * id to use for all the results from this job. Then it delegates to 
    * {@link #processContentList(IPSPublisherService, IPSEditionContentList, IPSEdition, IPSSite, Set)}
    * to process each content list associated with the edition. 
    * 
    * @param psvc the publishing service, assumed never <code>null</code>.
    * @param ed the publishing edition, assumed not <code>null</code>.
    * @param site the site, assumed never <code>null</code>.
    * @return <code>true</code> if this was successful and <code>false</code>
    * if the job was canceled.
    * @param unpublishedKeys a set of keys of site items that have been 
    * un-published (or have been sent to publishing queue for un-publish),
    * not <code>null</code>, but may be empty.
    * 
    * @throws Exception if an error occurs.
    */
   private boolean queueWorkItems(IPSPublisherService psvc, IPSEdition ed,
         IPSSite site, Set<PSSiteItemKey> unpublishedKeys) throws Exception
   {
      TreeSet<IPSEditionContentList> sortedclists = getEditionContentList(ed.getGUID());

      for (IPSEditionContentList clist : sortedclists)
      {
         if (m_canceled)
         {
            log.info("Edition " + getLogLabel(ed) + " has been cancelled");
            return false;
         }
         processContentList(psvc, clist, ed, site, unpublishedKeys);         
      }
      setCurrentContentList(null);
      return true;
   }

   /**
    * Creates a set of site item keys from the specified assembly items.
    * 
    * @param items the items in question, assumed not <code>null</code>,
    * but may be empty.
    * 
    * @return the set of site item keys, never <code>null</code>, may be empty. 
    */
   private Set<PSSiteItemKey> getSiteItemKeys(List<IPSAssemblyItem> items)
   {
      Set<PSSiteItemKey> keys = new HashSet<>();
      for (IPSAssemblyItem item : items)
      {
         PSSiteItemKey key = new PSSiteItemKey(item);
         keys.add(key);
      }
      return keys;
   }
   
   /**
    * Determines if the specified Content list is a custom one that is either  
    * a legacy Content List and/or a Content List with a custom URI
    *   
    * @param clist the Content List in question, assumed not <code>null</code>.
    * 
    * @return <code>true</code> if it is a custom Content List; otherwise return
    * <code>false</code>.
    */
   private boolean isCustomContentList(IPSContentList clist)
   {
      if (clist.isLegacy())
         return true;
      
      // get URI
      String url = clist.getUrl();
      int index = url.indexOf('?');
      if (index <= 0)
         return false;
      
      String uri = url.substring(0, index);
      return ! PSPublisherUtils.SERVLET_URL_PATH.equals(uri);
   }
   
   /**
    * Process a single edition content list. 
    * 
    * @param psvc the publishing service, assumed never <code>null</code>.
    * @param eclist one edition content list to process, assumed never
    *           <code>null</code>.
    * @param ed the publishing edition, assumed not <code>null</code>.
    * @param site the site, assumed never <code>null</code>.
    */
   private void processContentList(final IPSPublisherService psvc,
         final IPSEditionContentList eclist, 
         final IPSEdition ed, 
         final IPSSite site, 
         final Set<PSSiteItemKey> unpublishKeys)
   {
      final IPSContentList contentlist = getContentList(eclist);
      if (contentlist == null)
      {
         log.warn("Could not load content list: "
               + eclist.getContentListId() + " - skipping");
         return;
      }
    
      boolean isCreatedRequest = false;

      PSStopwatch sw = new PSStopwatch();
      log.debug("Begin process content list " + getLogLabel(contentlist));
      PSObjectStream<IPSAssemblyItem> stream  = null;
      try
      {
         isCreatedRequest = initRequestInfo();
         
         sw.start();
         final ContentItems items;

         PSTimer timer = new PSTimer(log);
         if (isCustomContentList(contentlist))
            items = getLegacyContentListItems(site, eclist, contentlist);
         else
            items = getContentListItems(site, eclist, contentlist);
         timer.logElapsed("Execute content list " + getLogLabel(contentlist));
   
         final PSStringUtils sutils = new PSStringUtils();
         final int deliveryContext = eclist.getDeliveryContextId().getUUID();

         final PSLocationChangeHandler handler = isHandleChangedLocation(site,
               eclist, contentlist) && items.mi_estimatedSize < unpublishMaxThreshold()
               ? new PSLocationChangeHandler(this) : null;

         final Iterator<List<IPSAssemblyItem>> asmIt = 
            Iterators.transform(items.iterator(), new Function<ContentItem, List<IPSAssemblyItem>>(){
            public List<IPSAssemblyItem> apply(ContentItem item)
            {
               List<IPSAssemblyItem> rvalue = new ArrayList<>();
               try
               {
                  Long pubServerId = ed.getPubServerId() == null ? null : (long)ed.getPubServerId().getUUID();
                  processContentListItem(psvc, item,
                        sutils, items.mi_deliveryType, deliveryContext, rvalue, pubServerId);
               }
               catch (UnsupportedEncodingException e)
               {
                  throw new RuntimeException(e);
               }
               return rvalue;
            }
         });
         
         Iterator<IPSAssemblyItem> queuedItems = flatten(asmIt);
         
         if (handler != null) {
            Iterator<List<IPSAssemblyItem>> rechunkedIt = Iterators.partition(queuedItems, 200);
            Iterator<List<IPSAssemblyItem>> mixIt = Iterators.transform(rechunkedIt, new Function<List<IPSAssemblyItem>, List<IPSAssemblyItem>>() {
               public List<IPSAssemblyItem> apply(List<IPSAssemblyItem> assemblyItems)
               {
                  Collection<IPSAssemblyItem> changeLocationItems =
                          null;
                  try {
                     changeLocationItems = handler.getUnpublishingItemsByServer(ed.getPubServerOrSiteId(), deliveryContext, contentlist, unpublishKeys, assemblyItems);
                  } catch (com.percussion.services.error.PSNotFoundException e) {
                     log.warn(e.getMessage());
                     log.debug(e.getMessage(),e);
                  }
                  Iterator<IPSAssemblyItem> moreUnPubs = Iterators.filter(assemblyItems.iterator(), new Predicate<IPSAssemblyItem>()
                  {
   
                     public boolean apply(IPSAssemblyItem ai)
                     {
                        return ! ai.isPublish();
                     }
                     
                  });
                  /*
                   * Unpublish items now.
                   */
                  sendItemsToPubQueue(Iterators.concat(moreUnPubs, changeLocationItems.iterator()), ed);
                  ArrayList<IPSAssemblyItem> rvalue = new ArrayList<>();
                  for (IPSAssemblyItem i : assemblyItems) {
                     if (i.isPublish()) {
                        rvalue.add(i);
                     }
                  }
                  return rvalue;
               }
            });
         
            Iterator<IPSAssemblyItem> it = flatten(mixIt);
            queuedItems = it;
         }
         sendItemsToPubQueue(queuedItems, ed);
      }
      catch (Exception e)
      {
         String errorMsg = "Failed to process Content List " + getLogLabel(contentlist)
               + " while running publishing Job id=" + m_jobid + ", Edition " + getLogLabel(ed) + ". ";
         log.error(errorMsg, e);
         m_hasJobError = true;
         throw new RuntimeException(errorMsg, e);
      }
      finally
      {
         sw.stop();
         log.debug("Finish process content list " + getLogLabel(contentlist) + ". " + sw.toString());

         if (isCreatedRequest)
            resetRequestInfo();
         if (stream != null)
            stream.dispose();
      }
   }
   
   /**
    * Gets the unpublish threshold to determine
    * when the number of items is exceeded to not do unpublishing
    * @return the queue threshold <code>0</code> means the queue will always be used.
    */
   private static int unpublishMaxThreshold()
   {
      if (PSServer.getServerProps() == null) return Integer.MAX_VALUE;
      String prop = PSServer.getServerProps().getProperty("unpublishMaxThreshold", "2147483647");
      Integer p = Integer.parseInt(prop);
      if (p < 0) return Integer.MAX_VALUE;
      return p;
   }

   /**
    * Send specified items to publishing queue, which will publish the items
    * to the target location.
    * 
    * @param items the items, assumed not <code>null</code>.
    * @param ed the edition, assumed not <code>null</code>.
    */
   private void sendItemsToPubQueue(Iterator<IPSAssemblyItem> items, IPSEdition ed)
   {
      Iterator<IPSAssemblyItem> resetItems = resetTemplate(items);
      
      //  Iterator that is being passed produces may items and does a lot
      // of work while being consumed.  If this work is done in the m_publishSender
      // it locks the object preventing other messages, like cancel request from being sent
      // We will parse items here in batches of 100 and send them to unblock for other messages.
      List<IPSAssemblyItem> sendItems = new ArrayList<>();
      int count = 0;
      while (items.hasNext()) {
         // Items were still getting sent after the job is canceled.  
         if (m_canceled) return;
         sendItems.add(items.next());
         if (!items.hasNext() || (++count % 100 == 0)) {
           
            for (IPSAssemblyItem item : sendItems)
            {
               updateJobStatus(item.getReferenceId(), ItemState.QUEUED);
            }
            m_publishSender.sendMessages(sendItems, getMsgPriority(ed));
            sendItems.clear();
         }
      }
    
      
   }

   /**
    * Reset the template object for the specified items. In other words, the
    * template object will be set to <code>null</code>. This needs to be done
    * before sending the items to the publishing queue. This is because
    * the template object cannot be serialized (or some of the properties
    * of the template object cannot be serialized).
    * <p>
    * Note, the template object is not needed for queuing up the item because the
    * assembler can always retrieve the template ID from other properties
    * of the item, such as assembly-url.
    * 
    * @param queuedItems the items need to be send to publishing queue,
    * assumed not <code>null</code>. 
    */
   private Iterator<IPSAssemblyItem> resetTemplate(Iterator<IPSAssemblyItem> queuedItems)
   {
      Iterator<IPSAssemblyItem> items = Iterators.transform(queuedItems,
            new Function<IPSAssemblyItem, IPSAssemblyItem>()
            {
               public IPSAssemblyItem apply(IPSAssemblyItem item)
               {
                  item.setTemplate(null);
                  return item;
               }
            });
      
      return items;
   }
   
   /**
    * Gets the un-publish items for the specified (paginated) items.
    * 
    * @param pagedItems the paginated items. All items must be based on one 
    * content item, that is the content IDs are the same. It may not be
    * <code>null</code>, but may be empty (do nothing in this case).
    * Assumed the 1st element is the 1st page of the item.
    * 
    * @return the un-publish items, never <code>null</code>, but may be empty.
    */
   public Collection<IPSAssemblyItem> getUnpublishPaginatedItems(List<IPSAssemblyItem> pagedItems) throws com.percussion.services.error.PSNotFoundException {
      notNull(pagedItems);
      
      PSLocationChangeHandler handler = new PSLocationChangeHandler(this);
      return handler.getUnpublishPaginateedItems(pagedItems);
   }
   
   /**
    * Determines if the specified content list needs to handle un-publish
    * previously published location if the published location has been changed.
    * 
    * @param site the site to publish, assumed not <code>null</code>.
    * @param eclist one edition content list to process, assumed never <code>null</code>.
    * @param contentlist the content list in question, assumed not <code>null</code>.
    * 
    * @return <code>true</code> if the content list needs to un-publish
    * previously published items in case the locations of the published items 
    * have been changed. 
    */
   private boolean isHandleChangedLocation(IPSSite site, IPSEditionContentList eclist, 
         IPSContentList contentlist)
   {
      // TODO the following code is commented because still behaviour of the CM system needs to be determined that
      // whether it has to be the default behaviour of always having it to true. That is why for now it is returning
      // true always. And also when code is put back add the following import.
      //import static com.percussion.util.IPSHtmlParameters.SYS_UNPUBLISH_CHANGED_LOCATION;

      /* 
      String url = PSPublisherUtils.getCListDocumentURL(site.getGUID(),
            eclist, contentlist);
      Map<String, String> params = getUrlParams(url);
      String value = params.get(SYS_UNPUBLISH_CHANGED_LOCATION);
      return "true".equalsIgnoreCase(value); 
      */
      return true;
   }
   
   public static boolean isHandleChangedLocation()
   {
      // TODO Auto-generated method stub
      return true;
   }
   
   /**
    * Gets the label of a specified content list. It is used to log the 
    * content list as name / ID pair.
    * 
    * @param clist the content list in question, assumed not <code>null</code>.
    * 
    * @return the name / ID pair of the content list, never <code>null</code>.
    */
   private String getLogLabel(IPSContentList clist)
   {
      return "'" + clist.getName() + "' (" + clist.getGUID().getUUID() + ")";
   }
   
   /**
    * Gets the label of current edition for logging.
    * 
    * @return the label of the edition, never <code>null</code>.
    */
   private String getEditionLabel()
   {
      if (m_edition == null)
         return "" + m_editionId.getUUID();
      
      return getLogLabel(m_edition);
   }
   /**
    * Gets the label of a specified edition. It is used to log the
    * edition as name / ID pair.
    * 
    * @param edition the edition in question, assumed not <code>null</code>.
    * 
    * @return the name / ID pair of the edition, never <code>null</code>.
    */
   private String getLogLabel(IPSEdition edition)
   {
      return "'" + edition.getName() + "' (" + edition.getGUID().getUUID() + ")";
   }
   
   /**
    * Wait for all outstanding work to complete. The job has completed when all
    * items are in a terminal state.
    * 
    * @throws InterruptedException
    */
   private void waitForJobCompletion() throws InterruptedException
   {
      updateAccessTime();
      long counter=0;
      while (true)
      {
         synchronized (this)
         {
            wait(POLL_TIME);
            counter++;
         }
         if (m_queued.get() == 0 && m_assembled.get() == 0 && m_paged.get() == 0)
         {
            log.debug("Detected Job Completion (queued, assembled and paged are 0) for job " + m_jobid );
            debugStatusCounts();
            break;
         }
         if (m_canceled)
         {
            log.info("Edition " + getEditionLabel() + " has been cancelled");
            debugStatusCounts();
            break;
         }
         if (isTimeout())
         {
            log.error("Job Timeout " +m_jobid);
            debugStatusCounts();
            break;            
         }
         if (counter % 60 == 0)
         {
            debugStatusCounts();
         }

      }
   }

  
   
   private void debugStatusCounts()
   {
      if(log.isDebugEnabled())
      {
         log.debug("Counts for job "+m_jobid + ": Queued = "+m_queued.get()
         + ", Paged = "+m_paged.get() 
         + ", Assembled = "+m_assembled.get() 
         + ", Cancelled = "+m_delivered.get() 
         + ", Prepared For Delivery = "+m_preparedForDelivery.get());
         
      }
   }

   /**
    * Send a job start message.
    * 
    * @param siteguid the site guid, assumed never <code>null</code>.
    */
   private void sendJobStartMessage(IPSGuid siteguid, IPSGuid pubserverguid)
   {
      PSJobControlMessage msg = new PSJobControlMessage(m_jobid,
            PSJobControlMessage.ControlType.START, siteguid,
            pubserverguid, siteguid);
     /* PSJobControlMessage updateMsg = new PSJobControlMessage(m_jobid,
            PSJobControlMessage.ControlType.UPDATE_HANDLER, siteguid,
            pubserverguid, siteguid);
   
      ms_log.debug("Sending started update handler message for job "+m_jobid);
      m_publishSender.sendMessage(updateMsg, IPSQueueSender.PRIORITY_HIGHEST);   */
      log.debug("Sending Job Start message for job "+m_jobid);
      m_publishSender.sendMessage(msg, IPSQueueSender.PRIORITY_HIGHEST);
   }
   
   /**
    * Send a job end message.
    */
   private void sendJobEndMessage()
   {
         PSJobControlMessage msg = new PSJobControlMessage(m_jobid,
               PSJobControlMessage.ControlType.END, m_siteguid, m_pubserverguid, null);
         log.debug("Sending Job End message for job "+m_jobid);
         m_publishSender.sendMessage(msg, IPSQueueSender.PRIORITY_HIGHEST);
   }

   /**
    * It represent an item in a Content List.
    */
   class ContentItem
   {
      /**
       * The assembly URL of the item.
       */
      String mi_url;
      
      /**
       * The location of the item.
       */
      String mi_location;
      
      /**
       * Determines if it is to publish or un-publish the item. 
       * It is <code>true</code> if publishing the item. 
       */
      boolean mi_isPublish;
      
      /**
       * Constructs an instance from a given XML element.
       * @param elem the XML element, assumed not <code>null</code>.
       */
      ContentItem(Element elem)
      {
         PSXmlTreeWalker citem = new PSXmlTreeWalker(elem);
         mi_url = citem.getElementData(ELEM_CONTENTURL);
         String unpublish = elem.getAttribute(ATTR_UNPUBLISH);
         Element locationel = citem.getNextElement(ELEM_LOCATION);
         mi_location = locationel.getTextContent();

         //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
         // the following code is backwards compatible with previous releases
         // comment out for now until it is needed
         mi_isPublish = !"yes".equals(unpublish.toLowerCase())
                 && !"true".equals(unpublish.toLowerCase());
         
         // Validate information is present
         if (StringUtils.isBlank(mi_location))
         {
            throw new IllegalStateException("location may not be null or empty");
         }
         if (StringUtils.isBlank(mi_url))
         {
            throw new IllegalStateException("url may not be null or empty");
         }
      }
      
      /**
       * Constructs an instance from the given object.
       * 
       * @param item the source object, assumed not <code>null</code>.
       * @throws PSAssemblyException if cannot find the template of this item.
       */
      ContentItem(PSContentListItem item, boolean isPublish,
            IPSEditionContentList eclist, IPSContentList clist,
            IPSPublisherService pub, IPSAssemblyService asm)
         throws PSAssemblyException
      {
         IPSAssemblyTemplate template = asm.loadUnmodifiableTemplate(item
               .getTemplateId());

         mi_url = pub.constructAssemblyUrl(null, 0, null, item.getSiteId(),
               item.getItemId(), item.getFolderId(), template, clist
                     .getFilter(), getAssemblyContextId(eclist), isPublish);

         mi_location = item.getLocation();
         mi_isPublish = isPublish;
      }
   }

   /**
    * Gets the assembly context ID from the given Edition/ContentList 
    * association.
    *
    * @param eclist the Edition/ContentList association, assumed not 
    *    <code>null</code> and delivery context ID must be defined.
    *    
    * @return the assembly context ID if exists; otherwise return the 
    * delivery context ID if the assembly context is not defined, It may be 
    * <code>null</code> if neither assembly and delivery ID are defined.
    */
   private int getAssemblyContextId(IPSEditionContentList eclist)
   {
      IPSGuid ctxId = eclist.getAssemblyContextId();
      if (ctxId == null)
         ctxId = eclist.getDeliveryContextId();
      
      if (ctxId == null)
         throw new IllegalArgumentException(
               "eclist must contain a delivery and/or assembly context ID.");
      
      return ctxId.getUUID();
   }
   
   /**
    * It contains a collection of items of a Content List.
    */
   class ContentItems
   {
      /**
       * The delivery type of the Content List.
       */
      String mi_deliveryType;
      
      /**
       * The collection of items of the Content List.
       */
      Iterator<ContentItem> mi_items = null;
      
      List<ContentItem> mi_legacyItems = new ArrayList<>();
      
      long mi_estimatedSize = 0;
      
      Iterator<ContentItem> iterator() {
         if (mi_items == null) return mi_legacyItems.iterator();
         return mi_items;
      }
      
      
   }
   
   /**
    * Process the content list item from the returned content list document.
    * This method takes the item, parses out the important information, and
    * creates and queues a work item for the assembly engine.
    * 
    * @param psvc the publisher service, assumed never <code>null</code>.
    * @param cItem the item's element, assumed never <code>null</code>.
    * @param sutils the string utilities, used to parse the url, assumed never
    *           <code>null</code>.
    * @param deliverytype the delivery type to use, assumed never
    *           <code>null</code> or empty.
    * @param deliverycontext the delivery context
    * @param queuedItems this is used to collect the queued item, so that it
    *    will be send to the queue in bulk (or send all collected items to the
    *    publishing queue in one shot). Assumed not <code>null</code>, but may
    *    be empty. 
    * @throws UnsupportedEncodingException
    */
   private IPSAssemblyItem processContentListItem(IPSPublisherService psvc,
         ContentItem cItem, PSStringUtils sutils, String deliverytype,
         int deliverycontext, List<IPSAssemblyItem> queuedItems, Long pubserverid)
         throws UnsupportedEncodingException
   {
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      String url = cItem.mi_url;

      // Get the query portion of the url
      int q = url.indexOf('?');
      IPSAssemblyItem item = ms_asm.createAssemblyItem();
      item.setJobId(m_jobid);
      item.setReferenceId(gmgr.createLongId(PSTypeEnum.PUB_REFERENCE_ID));
      item.setDeliveryType(deliverytype);
      item.setDeliveryPath(cItem.mi_location);
      item.setPublish(cItem.mi_isPublish);
      item.setAssemblyUrl(url);
      item.setDeliveryContext(deliverycontext);
      item.setPubServerId(pubserverid);

      if (q < 0)
      {
         String message = "Bad content url found: " + url;
         log.warn(message);
         recordFailureStatus(item, message);
         return null;
      }

      String query = url.substring(q + 1);
      Map<String, String> params = sutils.stringToMap(query);
      for (Map.Entry<String, String> entry : params.entrySet())
      {
         item.setParameterValue(entry.getKey(), entry.getValue());
      }
      try
      {
         if (item.isPublish())
         {
            item.normalize();
            updateJobWithWorkItem(item, queuedItems);
         }
         else
         {
            queueUnpublishItem(item, psvc, gmgr, queuedItems);
         }
         return item;
      }
      catch (PSAssemblyException e)
      {
         String message = "Skipping assembly item because it couldn't be "
               + "normalized. Assembly url was: " + url;
         log.error(message, e);
         recordFailureStatus(item, message, e.getLocalizedMessage());
         return null;
      }
   }

   /**
    * Process and put a given unpublishing item on the publishing queue.
    * 
    * @param item the to be unpublished item, assumed not <code>null</code>.
    * @param psvc the publisher service, assumed not <code>null</code>.
    * @param gmgr the GUID service, assumed not <code>null</code>.
    */
   private void queueUnpublishItem(IPSAssemblyItem item,
         IPSPublisherService psvc, IPSGuidManager gmgr, 
         List<IPSAssemblyItem> queuedItems)
   {
      try
      {
         // Get prior site item information
         IPSAssemblyService asm = PSAssemblyServiceLocator
            .getAssemblyService();

         String contentidParam = item.getParameterValue(
               IPSHtmlParameters.SYS_CONTENTID, null);
         String revisionParam = item.getParameterValue(
               IPSHtmlParameters.SYS_REVISION, null);
         if (StringUtils.isBlank(contentidParam)
               || StringUtils.isBlank(revisionParam))
         {
            throw new RuntimeException(
                  "Unknown content-id and/or revision for item: " + item);
         }
         
         item.setId(gmgr
               .makeGuid(new PSLocator(contentidParam, revisionParam)));

         asm.handleItemTemplates(Collections.singletonList(item));
         Object data[] = psvc.findUnpublishInfoForAssemblyItem(item.getId(),
               item.getDeliveryContextId(), item.getTemplate().getGUID(), item
                     .getSiteId(), item.getPubServerId(), item.getDeliveryPath());
         if (data == null || data.length == 0)
         {
            throw new RuntimeException(
                  "Cannot find info for unpublishing item " + item);
         }
         if (StringUtils.isBlank((String) data[0]))
         {
            throw new RuntimeException(
                  "Cannot find delivery type for unpublishing item " + item);      
         }
         IPSDeliveryType dtype = 
            psvc.loadDeliveryType((String) data[0]);
         item.setUnpublishRefId((Long)data[1]);
         byte[] unpublishInfo = (byte[]) data[2];
         Integer folderid = (Integer) data[3];
         if (folderid != null)
         {
            item.setFolderId(folderid);
            item.setParameterValue(IPSHtmlParameters.SYS_FOLDERID, String.valueOf(folderid));
         }
         processAndQueueUnpublishItem(item.getDeliveryContext(),
               unpublishInfo, item, dtype, queuedItems);
      }
      catch(Exception e)
      {
         String message = "Skipping unpublished item because no " +
               "prior publishing information could be found."; 
         log.error(message, e);
         recordFailureStatus(item, message, e.getLocalizedMessage());
      }
   }
   
   /**
    * Updates the job with a single item for assembly and delivery. The item 
    * may be for publishing or unpublishing.
    * 
    *    If we pass queuedItems we expect the caller to update the QUEUED state for the item
    *    queued items is null when adding paged items.  These are not actually added to the publish
    *    queue but we need to add the queued status to prevent the publish job from finishing too early.

    * 
    * @param item the item, assumed never <code>null</code>.
    * @param queuedItems this is used to collect the queued item, so that it
    *    will be sent to the queue in bulk (or send all collected items to the
    *    publishing queue in one shot). It be may <code>null</code> if not 
    *    needed.
    *    If we pass queuedItems we expect the caller to update the QUEUED state for the item
    *    queued items is null when adding paged items.  These are not actually added to the publish
    *    queue but we need to add the queued status to prevent the publish job from finishing too early.
    */
   private void updateJobWithWorkItem(IPSAssemblyItem item, 
         List<IPSAssemblyItem> queuedItems)
   {
      if (queuedItems != null)
         queuedItems.add(item);
      else
         updateJobStatus(item.getReferenceId(), ItemState.QUEUED);

   }

   /**
    * Queue a list of items for assembly and delivery. The items may be for 
    * publishing or unpublishing.
    * <p>
    * Note, this is typically used for submitting a list of paginated items
    * while assembling items in the current edition. The priority of the 
    * submitted items will be ONE higher than the initially submitted items
    * for the current Edition. 
    * 
    * @param items the to be queued items, never <code>null</code>, may be 
    *    empty.
    */
   public void addWorkItems(List<IPSAssemblyItem> items)
   {
      if (items == null)
         throw new IllegalArgumentException("items may not be null.");
      if (items.isEmpty())
         return;
      
      for (IPSAssemblyItem item : items)
      {
         updateJobWithWorkItem(item, null);
      }
   }

   /**
    * Get the items for the given none-legacy Content List. This is more
    * efficient than
    * {@link #getLegacyContentListItems(IPSSite, IPSEditionContentList, IPSContentList)}
    * 
    * @param site the site to be published, assumed never <code>null</code>.
    * @param eclist the edition content list, assumed never <code>null</code>.
    * @param clist the none-legacy Content List itself, assumed never
    * <code>null</code>.
    * @return a document of the returned content list, never <code>null</code>.
    * 
    * @throws Exception if an error occurs.
    */
   private ContentItems getContentListItems(
         final IPSSite site,
         final IPSEditionContentList eclist, 
         final IPSContentList clist)
         throws Exception
   {
      ContentItems cItems = new ContentItems();
      PSTimer timer = new PSTimer(log);
      
      setCurrentContentList(clist.getName());
      
      // prepare data for getting the Content List Items
      String url = PSPublisherUtils.getCListDocumentURL(site.getGUID(),
            eclist, clist);
      Map<String, String> params = getUrlParams(url);
      
      String sys_publish = params.get(IPSHtmlParameters.SYS_PUBLISH);
      final boolean isPublish = (! "unpublish".equalsIgnoreCase(sys_publish));
      
      // get the Content List Items
      final IPSPublisherService pub = PSPublisherServiceLocator.getPublisherService();
      PSContentListResults clResults = pub.runContentList(clist, params,
            isPublish, eclist.getDeliveryContextId(), site.getGUID());
      
      timer.logElapsed("ExecuteContentList got items: ~" + clResults.getEstimatedSize());
      cItems.mi_estimatedSize = clResults.getEstimatedSize();
      // convert above Content List Items into {@link ContentItems}
      cItems.mi_deliveryType = params.get(IPSHtmlParameters.SYS_DELIVERYTYPE);
      
      setRequestParameters(params);
      final IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
      
      timer = new PSTimer(log);
      cItems.mi_items = Iterators.transform(clResults.iterator(), new Function<PSContentListItem, ContentItem>()
      {
         public ContentItem apply(PSContentListItem item)
         {
            try
            {
               return new ContentItem(item, isPublish , eclist, clist, pub, asm);
            }
            catch (PSAssemblyException e)
            {
               throw new RuntimeException(e);
            }
         }
      });
      timer.logElapsed("Create ContentItems/LocationGen Iterator ");
      
      return cItems;
   }

   /**
    * Set the specified parameters onto current request. The {@link PSRequest} object is needed
    * for {@link PSContentListItem#getLocation()}.
    * 
    * @param params the parameters to be set to the current or created 
    *    {@link PSRequest}.
    */
   private void setRequestParameters(Map<String, String> params)
   {
      PSRequest req = (PSRequest) PSRequestInfo
            .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      req.setParameters((HashMap)params);
   }

   /**
    * Get the items for the given legacy content list. This method works for
    * a Content List can be retrieved as XML document. So it works for both
    * legacy and none-legacy Content List.
    * 
    * @param site the site to be published, assumed never <code>null</code>.
    * @param eclist the edition content list, assumed never <code>null</code>.
    * @param clist the legacy content list itself, assumed never
    *           <code>null</code>.
    * @return a document of the returned content list, never <code>null</code>.
    * 
    * @throws Exception if an error occurs.
    */
   private ContentItems getLegacyContentListItems(IPSSite site,
         IPSEditionContentList eclist, IPSContentList clist)
         throws Exception
   {
      setCurrentContentList(clist.getName());
      
      String url = PSPublisherUtils.getCListDocumentURL(site.getGUID(),
            eclist, clist);
      url = url + "&" + IPSHtmlParameters.SYS_PUBLICATIONID + "="+ m_jobid;
      
      ContentItems cItems = new ContentItems();
      
      while (url != null)
      {
         url = getContentListItems(url, cItems);
      }

      return cItems;
   }

   /**
    * Get the items for the given URL of a Content List. 
    * 
    * @param url the URL of the Content List, assumed not <code>null</code> or
    *    empty.
    * @param cItems the buffer used to collect the items of the Content List,
    *    assumed not <code>null</code>.
    * 
    * @return the URL of the next-page of the Content List. It may be 
    *    <code>null</code> if there is no next-page.
    *    
    * @throws Exception if an error occurs.
    */
   private String getContentListItems(String url, ContentItems cItems)
         throws Exception
   {
      PSDocumentUtils dutils = new PSDocumentUtils();
      String result = dutils.getDocument(url);
      Document doc = PSXmlDocumentBuilder.createXmlDocument(new StringReader(
            result), false);
      
      Element contentlistelem = doc.getDocumentElement();
      PSXMLDomUtil.checkNode(contentlistelem, ELEM_CONTENTLIST);
      String deliveryType = contentlistelem.getAttribute(ATTR_DELIVERY);
      if (StringUtils.isBlank(deliveryType))
      {
         log.warn("Delivery type cannot be blank in contentlist, "
               + "skipping");
         cItems.mi_items = Collections.emptyIterator();
         return null;
      }
      cItems.mi_deliveryType = deliveryType;
      
      // Get the rest of the elements
      Element nextEl = PSXMLDomUtil.getFirstElementChild(contentlistelem);

      while (nextEl != null)
      {
         if (nextEl.getNodeName().equalsIgnoreCase(ELEM_CONTENTITEM))
         {
            ContentItem item = new ContentItem(nextEl);
            cItems.mi_legacyItems.add(item);
            nextEl = PSXMLDomUtil.getNextElementSibling(nextEl);
         }
         else if (nextEl.getNodeName().equalsIgnoreCase(ELEM_NEXTPAGE))
         {
            String nextPageUrl = PSXMLDomUtil.getElementData(nextEl);
            
            if (!nextPageUrl.startsWith("http://"))
            {
               // if does not have protocol specified, assume the URL does
               // does contain protocol, host & port, simply return as is
               return nextPageUrl;
            }
            
            // assume this is sending request to the same container and 
            // application, remove the protocol, host and port from the URL
            URL nextUrl = new URL(nextPageUrl);
            String queryString = nextUrl.getQuery();
            String urlPath = nextUrl.getPath();
            
            return urlPath + "?" + queryString;
         }
         else // skip unknown/important element, such as PSXPrevPage
         {
            nextEl = PSXMLDomUtil.getNextElementSibling(nextEl);
         }
      }
      return null;
   }


   /**
    * Change the state of this job.
    * 
    * @param state the new state, assumed never <code>null</code>.
    */
   private synchronized void changeState(State state)
   {
      m_jobState = state;
   }

   /**
    * Set the given item's state. This is called when the handler receives a
    * status update. The call is dispatched through the publisher. This method
    * only deals with local state, db changes are in the parent service.
    * 
    * @param status the new item status
    */
   public void updateItemState(IPSPublisherItemStatus status)
   {
      long refid = status.getReferenceId();
      ItemState state = status.getState();
      updateJobStatus(refid, state);
  
   }
   
   /**
    * Updates the {@link #m_items} for the specified reference ID
    * and the counters of the job according to the specified states.
    *  
    * @param refId the reference ID.
    * @param curState the current state of the updated item, assumed not
    *    <code>null</code>.
    */
   private void updateJobStatus(long refId,
         
         IPSPublisherJobStatus.ItemState curState)
   {
      AtomicReference<IPSPublisherJobStatus.ItemState> previousStateRef = m_items.get(refId);
      ItemState previousState = null;
      if (previousStateRef == null)
      {
         m_items.put(refId, new AtomicReference<>(curState));
      }
      else
      {
       
         previousState = previousStateRef.get();
         if (previousStateRef.get().isTerminal())
         {
            return;
         } 
         
         previousState = previousStateRef.getAndSet(curState);
            
      }
         log.trace("updating state for refId "+refId);
         updateJobStatus(previousState, curState);
   }

   /**
    * Updates the summary information for this job when updating the state of 
    * an item.
    * <p>
    * Note, this method is assumed to be synchronized by {@link #m_items} by
    * the caller.
    * 
    * @param preState the previous state of the updated item, may be 
    *    <code>null</code> if there is no previous state.
    * @param curState the current state of the updated item, assumed not 
    *    <code>null</code>.
    */
   private void updateJobStatus(
         IPSPublisherJobStatus.ItemState preState,
         IPSPublisherJobStatus.ItemState curState)
   {
      updateAccessTime();
       
         if (preState != null)
         {
            log.trace("Item State change from "+preState.name()+" to "+curState.name());
            switch (preState)
            {
               case QUEUED: m_queued.decrementAndGet(); break;
               case FAILED: m_failed.decrementAndGet(); break;
               case ASSEMBLED: m_assembled.decrementAndGet(); break;
               case PREPARED_FOR_DELIVERY: m_preparedForDelivery.decrementAndGet(); break;
               case DELIVERED: m_delivered.decrementAndGet(); break;
               case PAGED: m_paged.decrementAndGet(); break;
               case DELIVERY_QUEUED: m_deliveryQueued.decrementAndGet(); break;
               default: throw new IllegalStateException(
                     "Unrecognized item state: " + preState);
            }
         } else {
            log.trace("New Item State to "+curState.name());
         }
         
         switch (curState)
         {
            case QUEUED: m_queued.incrementAndGet(); break;
            case FAILED: m_failed.incrementAndGet(); break;
            case ASSEMBLED: m_assembled.incrementAndGet(); break;
            case PREPARED_FOR_DELIVERY: m_preparedForDelivery.incrementAndGet(); break;
            case DELIVERED: m_delivered.incrementAndGet(); break;
            case PAGED: m_paged.incrementAndGet(); break;
            case DELIVERY_QUEUED: m_deliveryQueued.incrementAndGet(); break;
            default: throw new IllegalStateException(
                  "Unrecognized item state: " + curState);
         }
    
   }
   
   /**
    * Get's the current status of the job. 
    * 
    * @return the status, never <code>null</code>.
    */
   public synchronized IPSPublisherJobStatus getStatus()
   {
      PSPublisherJobStatus rval = new PSPublisherJobStatus();
      rval.setJobId(m_jobid);
      rval.setTotalItems(m_items.size());
      rval.setQueuedAssemblyItems(m_queued.get());
      rval.setFailedItems(m_failed.get());
      rval.setAssembledDeliveryItems(m_assembled.get());
      rval.setItemsPreparedForDelivery(m_preparedForDelivery.get());
      rval.setDeliveredItems(m_delivered.get());
      rval.setState(m_jobState);
      rval.setStartTime(m_startTime);
      rval.setEditionId(m_editionId);
      rval.setRerunAfter(m_rerun);
      rval.setMessage(m_message);
      long end = m_endTime != null ? m_endTime.getTime() 
            : System.currentTimeMillis();
      rval.setElapsed(end - m_startTime.getTime());

      return rval;
   }

   /**
    * @return <code>true</code> if this job has been canceled.
    */
   public boolean isCanceled()
   {
      return m_canceled;
   }

   /**
    * Cancel this job. When canceled, the other components in the publishing
    * system will empty their incoming queues of items belonging to the
    * canceled job. This may take time, but no further work will be performed
    * on such items despite their being present in the queue.
    * <p>
    * Cancellation also means that if this class is still queuing content list
    * information, it will cease. Because the thread is interrupted, this
    * happens right away.
    * 
    * @param canceledByUser <code>true</code> if the job is canceled by user;
    *    <code>false</code> otherwise.
    */
   public void cancel(boolean canceledByUser)
   {
      m_canceled = true;
      // Send cancel message to assembly queue, which will repeat it to the
      // delivery queue and the status queue
      
      PSCancelPublishingMessage msg = new PSCancelPublishingMessage(m_jobid);
      m_publishSender.sendMessage(msg, IPSQueueSender.PRIORITY_HIGHEST);


      // Set a final status 
      if (canceledByUser)
         changeState(State.CANCELLED);
      else
         changeState(State.ABORTED);
   }

   /**
    * @return the edition ID
    */
   public IPSGuid getEdition()
   {
      return m_editionId;
   }

   /**
    * @return the jobid
    */
   public long getJobid()
   {
      return m_jobid;
   }

   /**
    * @return never <code>null</code>.
    */
   public IPSGuid getSite()
   {
      return m_siteguid;
   }
   
   /**
    * @return the name of the current content list that is being processed
    * or <code>null</code> if no content list is currently being processed.
    */
   public String getCurrentContentList()
   {
      return m_currentContentList;
   }

   /**
    * @param currentContentList the current content list name being processed
    * or <code>null</code>.
    */
   private void setCurrentContentList(String currentContentList)
   {
      m_currentContentList = currentContentList;
   }

   /**
    * @return the lastAccessTime
    */
   protected long getLastAccessTime()
   {
      return m_lastAccessTime;
   }

   /**
    * Reset the last access time.
    */
   protected void resetLastAccessTime()
   {
      m_lastAccessTime = System.currentTimeMillis();
   }

   /**
    * Set the committed flag and wake the waiting job so it can finish.
    * @param hasError <code>true</code> if an error occurs during commit
    *    process.
    */
   public synchronized void acknowledgeCommit(boolean hasError)
   {
      m_committed = true;
      if (hasError)
         m_hasJobError = true;
      
      notifyAll();
   }
   
   /**
    * @return the startTime
    */
   public Date getStartTime()
   {
      return m_startTime;
   }

   /**
    * @return the endTime
    */
   public Date getEndTime()
   {
      return m_endTime;
   }

   /**
    * Is this job in a terminal state?
    * @return <code>true</code> if the job is completed or cancelled.
    */
   public boolean isFinished()
   {
      return m_jobState.isTerminal();
   }

   /**
    * @return the current job state, never <code>null</code>.
    */
   public State getState()
   {
      return m_jobState;
   }

   /**
    * @return is the job marked for rerun after success
    */
   public boolean isRerun()
   {
      return m_rerun;
   }
   
   /**
    * Set if this job should rerun after success.
    */
   public void setRerun(boolean rerun)
   {
      m_rerun = rerun;
   }
   
   public boolean isCleanPublish()
   {
      return m_cleanPublish;
   }
   
   public ConcurrentLinkedQueue<IPSPublisherItemStatus> getUpdateQueue()
   {
      return m_updates;
   }

   /**
    * Flattens an iterator.
    * Hopefully google will add this someday.
    * http://code.google.com/p/guava-libraries/issues/detail?id=627
    * @param <T> item that your iterating on.
    * @param it never null.
    * @return never null.
    */
   public static <T> Iterator<T> flatten(final Iterator<? extends Iterable <T>> it) {
      return new PSIteratorChain<T>()
      {
         @Override
         protected Iterator<T> nextIterator()
         {
            if (it.hasNext()) {
               return it.next().iterator();
            }
            return null;
         }
      };
   }
}
