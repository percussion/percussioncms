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

import com.percussion.rx.delivery.IPSDeliveryHandler;
import com.percussion.rx.delivery.IPSDeliveryManager;
import com.percussion.rx.delivery.IPSDeliveryResult;
import com.percussion.rx.delivery.IPSDeliveryResult.Outcome;
import com.percussion.rx.delivery.PSDeliveryException;
import com.percussion.rx.publisher.IPSAssemblyResultExpander;
import com.percussion.rx.publisher.IPSPublisherJobStatus;
import com.percussion.rx.publisher.IPSPublisherJobStatus.ItemState;
import com.percussion.rx.publisher.IPSRxPublisherServiceInternal;
import com.percussion.rx.publisher.PSRxPubServiceInternalLocator;
import com.percussion.rx.publisher.data.PSCancelPublishingMessage;
import com.percussion.rx.publisher.data.PSJobControlMessage;
import com.percussion.rx.publisher.data.PSPubItemStatus;
import com.percussion.security.PSSecurityProvider;
import com.percussion.server.PSRequest;
import com.percussion.services.PSBaseServiceLocator;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.assembly.IPSAssemblyResult.Status;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.assembly.PSTemplateNotImplementedException;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.filter.PSFilterException;
import com.percussion.services.publisher.IPSDeliveryType;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.services.pubserver.IPSPubServer;
import com.percussion.services.pubserver.IPSPubServerDao;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSBaseBean;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.utils.timing.PSStopwatch;
import com.percussion.webservices.PSWebserviceUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jcr.RepositoryException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.percussion.util.IPSHtmlParameters.SYS_UNPUBLISH_CHANGED_LOCATION;
import static org.apache.commons.lang.Validate.noNullElements;
import static org.apache.commons.lang.Validate.notNull;

/**
 * Handler that processes assembly items waiting in the assembly queue for
 * publishing. This handler processes the item and then invokes the delivery
 * manager to persist the assembled content. It also sends an acknowledgment
 * back to the publisher to allow the publisher to track progress.
 * <p>
 * There are multiple handlers running at a time. The number of handlers should
 * be tuned based on the count of processors on a given machine as well as the
 * tradeoff between processing available for publishing and processing available
 * for other user driven activity.
 * <p>
 * This processor receives messages from the publishing queue and feeds output
 * messages to the result queue.
 * 
 * @author dougrand
 */
@PSBaseBean("sys_publishQueueListener")
public class PSPublishHandler implements MessageListener
{
   /**
    * Logger used for publishing handler service.
    */
   private static final Logger log = LogManager.getLogger(PSPublishHandler.class);
   
   
   /**
    * The item states assigned on the particular delivery outcomes.
    */
   public static final Map<Outcome, ItemState> OUTCOME_STATE;
   static {
      final Map<Outcome, ItemState> m = new HashMap<>();
      m.put(Outcome.DELIVERED, ItemState.DELIVERED);
      m.put(Outcome.FAILED, ItemState.FAILED);
      m.put(Outcome.PREPARED_FOR_DELIVERY, ItemState.PREPARED_FOR_DELIVERY);
      m.put(Outcome.DELIVERY_QUEUED, ItemState.DELIVERY_QUEUED);
      OUTCOME_STATE = m;
   }
   
   /**
    * Cancellations are held in this list. Code using this list must synchronize
    * access. Cancellations are removed from this list when a new cancellation 
    * is received and old messages have expired.
    */
   private static List<PSCancelPublishingMessage> ms_cancellations 
      = new CopyOnWriteArrayList<>();

   /**
    * Map whose key is a delivery type name and value is a flag which indicates
    * whether or not empty delivery locations are allowed. 
    */
   private static Map<String, Boolean> ms_deliveryTypeToAllowsEmptyLocation
      = new ConcurrentHashMap<>();
   
   /**
    * The delivery manager to actually deliver the content. Set via spring.
    */
   private IPSDeliveryManager m_deliveryManager = null;
   
   private IPSAssemblyResultExpander m_paginationAssemblyResultExpander = null;
   
   /**
    * The delivery manager to actually deliver the content. (auto) wired by spring.
    */
   private Map<String, IPSAssemblyResultExpander> m_assemblyResultExpanders = 
      new ConcurrentHashMap<>();

   /**
    * The rx publish service, (auto) wired by spring
    */
   private IPSRxPublisherServiceInternal m_rxPubService = null;
   
   /**
    * Constructor, auto wired via spring
    * 
    * @param deliveryMgr the delivery manager, not <code>null</code>.
    * @param rxPubService the publish service, not <code>null</code>.
    */
   @Autowired
   public PSPublishHandler(IPSDeliveryManager deliveryMgr, 
      IPSRxPublisherServiceInternal rxPubService)
   {
      Thread.currentThread().setPriority(4);
      m_deliveryManager = deliveryMgr;
      m_rxPubService = rxPubService;
   }
   

   public IPSAssemblyResultExpander getPaginationAssemblyResultExpander()
   {
      return m_paginationAssemblyResultExpander;
   }


   public void setPaginationAssemblyResultExpander(
         IPSAssemblyResultExpander paginationAssemblyResultExpander)
   {
      m_paginationAssemblyResultExpander = paginationAssemblyResultExpander;
   }
   
   /**
    * Custom assembly result expanders that are from spring.
    * The name of the bean is the key.
    * 
    * @return expanders never <code>null</code>.
    */
   public Map<String, IPSAssemblyResultExpander> getAssemblyResultExpanders()
   {
      return m_assemblyResultExpanders;
   }


   public void setAssemblyResultExpanders(
         Map<String, IPSAssemblyResultExpander> assemblyResultExpanders)
   {
      m_assemblyResultExpanders = assemblyResultExpanders;
   }
   
   public void onMessage(Message message)
   {
      try
      {
         if (message instanceof ObjectMessage)
         {
            ObjectMessage om = (ObjectMessage) message;
            Object objectMessage;
            try
            {
               objectMessage = om.getObject();
            }
            catch (JMSException e)
            {
               log.error("Problem getting message", e);
               return;
            }
   
            if (objectMessage instanceof PSCancelPublishingMessage)
            {
               onCancellation(objectMessage);
            }
            else if (objectMessage instanceof PSJobControlMessage)
            {
               onJobControl(objectMessage);
            }
            else if (objectMessage instanceof IPSAssemblyItem)
            {

               onWorkItem((IPSAssemblyItem)objectMessage);

            }
         }
      }
      catch (Exception e)
      {
         log.error(
               "Uncaught exception while handling message " + message, e);
      }
      finally
      {
         try
         {
            message.acknowledge();
         }
         catch (JMSException e)
         {
            log.error("Problem acknowledging message");
         }
      }
   }

   /**
    * Assembly and deliver an assembly work item. This method first initializes
    * server information so that any legacy assembly or other legacy code will
    * be able to operate correctly. Note the reset in the finally block.
    * <p>
    * The next thing done is to check for cancellations. Cancellations are held
    * in a static block, which is synchronized here for checking. The assembly
    * and delivery work is delegated to the appropriate handle methods.
    * <p>
    * Results for success are handled in
    * {@link #handleDelivery(IPSAssemblyResult)}
    * but failures are handled in this method.
    * <p>
    * Note that while one assembly item is being processed that with pagination
    * it is possible that many results will be returned.
    * 
    * @param work the assembly work item, assumed never <code>null</code>.
    */
   private void onWorkItem(IPSAssemblyItem work)
   {
      long jobId = work.getJobId();
      boolean active = m_rxPubService.isJobActive(jobId);
      if ( ! active )
      {
         log.debug("Ignore item: " + work.getId()
               + " for job: " + work.getJobId() 
               + " because its not an active job for this server.");
         return;
      }
      try
      {
         PSRequest req = PSRequest.getContextForRequest();
         PSRequestInfo.initRequestInfo((Map<String,Object>) null);
         req.setParameter("allowBinary","true");
         PSRequestInfo.setRequestInfo(PSRequestInfo.KEY_PSREQUEST, req);
         /*
          * TODO username set for web services calls!!!!!!!!!!!!!!!!!!
          * There are some web services called like IPContentWs.getItemGuid
          * that require the user name to be set in the thread.
          * FIXME: This should be fixed or revied.
          * Adam Gent
          */
         PSWebserviceUtils.setUserName(PSSecurityProvider.INTERNAL_USER_NAME);
         processWorkItem(work);
      }
      finally
      {
         PSRequestInfo.resetRequestInfo();
      }

   }
   
   /**
    * The same as {@link #onWorkItem(IPSAssemblyItem)}, except this will not reset
    * the server information, {@link PSRequest}. 
    * 
    * @param work the to be processed work item, assumed not <code>null</code>. 
    */
   @SuppressWarnings("unchecked")
   private void processWorkItem(IPSAssemblyItem work)
   {
      try
      {
         IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
         IPSPublisherService psvc = PSPublisherServiceLocator
               .getPublisherService();
         IPSRxPublisherServiceInternal rxpsvc = 
               PSRxPubServiceInternalLocator.getRxPublisherService();
         List<IPSAssemblyResult> results = Collections.EMPTY_LIST;
         try
         {
          
           
            // Check cancellation
            if (!ms_cancellations.isEmpty())
            {
               synchronized (ms_cancellations)
               {
                  for (PSCancelPublishingMessage cp : ms_cancellations)
                  {
                     if (work.getJobId() == cp.getJobId())
                     {
                        log.info("Discarding cancelled item: " + work.getId());
                        // ignore/discard
                        return;
                     }
                  }
               }
            }
         
            
            PSPublishingJob job = rxpsvc.getPublishingJob(work.getJobId());
            if (job == null)
            {
               log.info("Discarding process item "+ work.getId()+" for old or cancelled job: " +work.getJobId() );
               // ignore/discard
               return;
            }
            
            if (StringUtils.isEmpty(work.getDeliveryPath()) && !isEmptyLocationAllowed(psvc, work.getDeliveryType()))
            {
               results = handleEmptyLocation(work);
            }
            else
            {
               results = handleAssembly(asm, psvc, work);
            }
         }
         catch (Exception e)
         {
            results = handleAssemblyException(work, e);
         }

         for (IPSAssemblyResult result : results)
         {
            if (result.isPaginated())
            {
               try
               {
                  IPSAssemblyResultExpander expander = getAssemblyResultExpander(result);
                  expander = expander == null ? getPaginationAssemblyResultExpander() : expander;
                  expandAssemblyResult(result, expander);
                  continue;
               }
               catch (Exception e)
               {
                  log.error("Error expanding assembly result id: "
                        + result.getId(), e);
                  result.setStatus(Status.FAILURE);
                  result.setMimeType("text/plain");
                  String message = "Error expanding assembly result: " + 
                     ExceptionUtils.getFullStackTrace(e);

                     result.setResultData(message.getBytes(StandardCharsets.UTF_8));

               }
            }
            if (result.getTemplate() == null && work.isPublish())
            {
               asm.handleItemTemplates(Collections
                     .singletonList((IPSAssemblyItem)result));
            }
            if (result.isSuccess())
            {
               handleDelivery(result);
            }
            else
            {
               handleFailedResult(result);
            }
         }
      }
      catch (Throwable th)
      {
         String msg = "Problem while handling assembly: ";
         setEncodedResultData(work, msg + th.getMessage()); 
         

            handleFailedResult((IPSAssemblyResult) work);

         
         log.error(msg, th);
      }
   }

   /**
    * Handle a given failed result.
    * 
    * @param result the failed result, assumed not <code>null</code>.
    */
   private void handleFailedResult(IPSAssemblyResult result)
   {
      ItemState state = IPSPublisherJobStatus.ItemState.FAILED;
      long pubServerId = result.getPubServerId() == null ? -1 : result.getPubServerId();
      if (result.getPubServerId()==null)
      {
         log.error("PubServerId not passed in IPSAssemblyItem: {}" , result);
      }
      
      PSPubItemStatus status = new PSPubItemStatus(result.getReferenceId(),
            result.getJobId(), pubServerId, result.getDeliveryContext(), state);
      status.extractInfo(result);
      
      // On failure, the result data is the message
      if (result.getResultData() != null)
      {
         status.addMessage(new String(result.getResultData(), StandardCharsets.UTF_8));
      }
      else
      {
         status.addMessage("Failed");
      }
      
      updateItemStatus(status);
      
   }
   
   /**
    * Handle the given pagenated result.
    * @param result the pagenated result, assumed not <code>null</code>.
    * @throws Exception if error occurs when evaluate.
    */
   private void expandAssemblyResult(IPSAssemblyResult result, IPSAssemblyResultExpander expander) throws Exception
   {
      IPSRxPublisherServiceInternal rxpsvc = 
         PSRxPubServiceInternalLocator.getRxPublisherService();

      
      Long original = result.getReferenceId();
      List<IPSAssemblyItem> results;
      results = expander.expand(result);
      notNull(results, "results should not be null");
      noNullElements(results);
      List<IPSAssemblyItem> addedItems = new ArrayList<>(results);
      Iterator<IPSAssemblyItem> iter = addedItems.iterator();
      /*
       * Filter out the original item.
       */
      IPSAssemblyItem originalItem = null;
      while(iter.hasNext()) {
         IPSAssemblyItem i = iter.next();
         if (i.getReferenceId() == original) {
            originalItem = i;
            iter.remove();
         }
      }
      
      
      // add the new items to the job, must be done before updating
      // current job status
      rxpsvc.addWorksForJob(result.getJobId(), addedItems);
      
      // update the job status for the current work item.
      ItemState state = IPSPublisherJobStatus.ItemState.PAGED;
      PSPubItemStatus status = new PSPubItemStatus(result
            .getReferenceId(), result.getJobId(), result.getPubServerId(), result.getDeliveryContext(),
            state);
      status.extractInfo(result);
      status.addMessage("Expanded into " + results.size() + " pages");
      updateItemStatus(status);

      handleChangedLocations(results);

      if(originalItem != null)
         processWorkItem(originalItem); // TODO - this line may not work for CM system, paginated items
      
      // process the new expanded items.
      for (IPSAssemblyItem i : addedItems)
      {
         processWorkItem(i);
      }
   }
   /**
    * Handles un-publish previous (published) locations for the specified 
    * paginated items if the items have changed the publishing location. 
    * The process can only be enabled if the parameter, 
    * {@link IPSHtmlParameters#SYS_UNPUBLISH_CHANGED_LOCATION} equals <code>true</code>,
    * is specified in the given paginated items; otherwise do nothing.
    * 
    * @param pagedItems the paginated items, assumed all items are based on one 
    * content item, so that the content IDs are the same. Assumed it is not
    * <code>null</code>, but may be empty. Assumed the 1st element is the 1st 
    * page of the item.
    */
   private void handleChangedLocations(List<IPSAssemblyItem> pagedItems) throws PSNotFoundException {
      if (pagedItems.isEmpty())
         return;
      
      IPSAssemblyItem item1 = pagedItems.get(0);
      String handleUnpublish = item1.getParameterValue(SYS_UNPUBLISH_CHANGED_LOCATION, "false");
      if (! "true".equals(handleUnpublish))
         return;
      
      // do nothing if is not originated from an paginated item
      if (item1.getPage() == null)
         return; 
      
      PSPublishingJob job = m_rxPubService.getPublishingJob(item1.getJobId());
      if (job == null)
         return;
      
      Collection<IPSAssemblyItem> unpubItems = job.getUnpublishPaginatedItems(pagedItems);
      
      if (unpubItems.size()>0)
      {
          log.debug("Unpublish list size = {} path={}",
                  unpubItems.size(),
                  unpubItems.iterator().next().getDeliveryPath());
      }
     
      // Need to set item status as queued
      //m_rxPubService.addWorksForJob(item1.getJobId(), new ArrayList<IPSAssemblyItem>(unpubItems));
      
      // Do the actual un-publish operation
     /* for (IPSAssemblyItem item : unpubItems)
      {
         processWorkItem(item);
      }
      */      
   }
   
   /**
    * Get the expander from the assembly result.
    * @param result never <code>null</code>.
    * @return maybe <code>null</code> if no expander exists.
    */
   private IPSAssemblyResultExpander getAssemblyResultExpander(
         IPSAssemblyResult result)
   {
      String expanderName = result.getParameterValue(
            IPSAssemblyResultExpander.ASSEMBLY_RESULT_EXPANDER_PARAM, null);
      return getAssemblyResultExpanders().get(expanderName);
   }
   
   /**
    * Handle a job control message. The only current actions are initializing
    * site information on the delivery manager for job start, and commit
    * on job end.
    * 
    * @param objectMessage the job control message, assumed never
    *           <code>null</code>.
    */
   private void onJobControl(Object objectMessage)
   {
      PSJobControlMessage jc = (PSJobControlMessage) objectMessage;
      IPSGuid serverguid = (IPSGuid) jc.getPubServerId();
      if (jc.getType().equals(PSJobControlMessage.ControlType.START))
      {
         IPSSiteManager smgr = PSSiteManagerLocator.getSiteManager();
         IPSGuid siteguid = (IPSGuid) jc.getData();
         IPSSite site;
         IPSPubServerDao psmgr = PSSiteManagerLocator.getPubServerDao();
         
         IPSPubServer server;
         try
         {
            site = smgr.loadSite(siteguid);
            server = serverguid != null ? psmgr.findPubServer(serverguid) : null;
            long jobId = jc.getJobId();
            m_deliveryManager.init(jobId, site, server);
         }
         catch (PSNotFoundException e)
         {
            log.error("Could not load site {}",  siteguid.toString());
         }
      } 
      else // jc.getType() == END
      {
         boolean isCommitWithError = false;
         try
         {
            Collection<IPSDeliveryResult> results = 
               m_deliveryManager.commit(jc.getJobId());
            for(IPSDeliveryResult result : results)
            {
               if (!result.hasUpdateSent())
               {
                  final ItemState state = OUTCOME_STATE.get(result.getOutcome());
                  PSPubItemStatus status = new PSPubItemStatus(
                        result.getReferenceId(), result.getJobId(), serverguid.getUUID(), result.getDeliveryContext(), state);
                  if (result.getUnpublishData() != null)
                  {
                     status.setUnpublishingInformation(result.getUnpublishData());
                  }
                  if (StringUtils.isNotBlank(result.getFailureMessage()))
                  {
                     status.addMessage(result.getFailureMessage());
                  }
                  status.setSiteId(jc.getSiteId());
                  updateItemStatus(status);
               }
            }
         }
         catch (Exception e)
         {
            log.error("Failed to commit publishing Job id=" + jc.getJobId(), e);
            isCommitWithError = true;
         }
       
         m_rxPubService.acknowledgeJobCommit(jc.getJobId(), isCommitWithError);
      }
   }

   /**
    * Handle cancellation message by recording it in the static list of such
    * messages. The list is synchronized for thread safety, and expired 
    * messages are removed from the list.
    * <p>
    * After recording the cancellation message, the delivery manager is told to
    * roll back pending work for the job and the message is passed on to the 
    * results processor.
    * 
    * @param objectMessage cancellation message, assumed never 
    * <code>null</code>.
    */
   private void onCancellation(Object objectMessage)
   {
      PSCancelPublishingMessage cancel =
            (PSCancelPublishingMessage) objectMessage;
      // Add cancellation after checking list for expiration
      synchronized (ms_cancellations)
      {
         Iterator<PSCancelPublishingMessage> iter = ms_cancellations.iterator();
         while (iter.hasNext())
         {
            PSCancelPublishingMessage cp = iter.next();
            if (cp.shouldDiscard())
               iter.remove();
         }
         ms_cancellations.add(cancel);
      }
      try
      {
         m_deliveryManager.rollback(cancel.getJobId());
         
         IPSPublisherService psvc = PSPublisherServiceLocator
               .getPublisherService();
         psvc.cancelUnfinishedJobItems(cancel.getJobId());
      }
      catch (PSDeliveryException e)
      {
         log.error("Failed to abort delivery manager", e);
      }
   }
   
   /**
    * Updates the status of the given item.
    * @param status the status of the item, assumed not <code>null</code>.
    */
   private void updateItemStatus(PSPubItemStatus status)
   {
      m_rxPubService.updateItemState(status);      
   }
   
   /**
    * Handle the assembly part of the processing. This method always assembles
    * the work item for publishing and conditionally assembles it for unpublish
    * subject to the configuration of the delivery location.
    * 
    * @param asm the assembly service, assumed never <code>null</code>.
    * @param psvc the publishing service, assumed never <code>null</code>.
    * @param work the assembly work item, assumed never <code>null</code>.
    * @return the results, never <code>null</code> and never empty.
    * 
    * @throws RepositoryException
    * @throws PSTemplateNotImplementedException
    * @throws PSAssemblyException
    * @throws PSFilterException
    */
   private List<IPSAssemblyResult> handleAssembly(IPSAssemblyService asm,
         IPSPublisherService psvc, IPSAssemblyItem work)
         throws RepositoryException,
         PSTemplateNotImplementedException, PSAssemblyException,
         PSFilterException
   {try {
      List<IPSAssemblyResult> results;
      boolean doassemble = true;
      if (!work.isPublish()) {
         // Before assembling, check the delivery
         String location = work.getDeliveryType();

         IPSDeliveryType dloc = psvc.loadDeliveryType(location);
         doassemble = dloc.isUnpublishingRequiresAssembly();
      }

      if (doassemble) {
         results = asm.assemble(Collections.singletonList(work));
      } else {
         results = Collections.singletonList((IPSAssemblyResult) work);
      }
      return results;
   } catch (PSNotFoundException e) {
      throw new RepositoryException(e);
   }
   }

   /**
    * Handle creating the result for an assembly work item which has an empty
    * delivery location.  The item is marked as a failure and an appropriate
    * error message is specified in the item's result data.
    * 
    * @param work the assembly work item, assumed never <code>null</code>.
    * @return the results, never <code>null</code> and never empty.
    */
   private List<IPSAssemblyResult> handleEmptyLocation(IPSAssemblyItem work)
   {
      // mark as failed, set error message
      work.setStatus(Status.FAILURE);
      setEncodedResultData(work, "No delivery location found for this item - skipping");
                        
      return Collections.singletonList((IPSAssemblyResult) work);
   }
   
   /**
    * Sets the result data for the specified assembly work item.  The
    * result data will be set to the specified message, UTF8 encoded.
    * The mimetype of the work item is also set.
    * 
    * @param work the assembly item, assumed not <code>null</code>.
    * @param msg the new result data, assumed not <code>null</code>.
    */
   private void setEncodedResultData(IPSAssemblyItem work, String msg)
   {
         work.setResultData(msg.getBytes(StandardCharsets.UTF_8));
         work.setMimeType("text/plain; charset=utf8");
   }
   
   /**
    * Handle the delivery of the work item. This sends two messages to the 
    * results processor. The first is the fact that the item has been 
    * assembled. Then after it has been delivered, the status of the delivery
    * is used to send a second message with the results from delivery. The last
    * thing this method does is to clear the results from the work item to 
    * reduce memory usage.
    * 
    * @param work the work item, never <code>null</code>.
    * 
    * @return the item state after delivery.
    */
   private ItemState handleDelivery(IPSAssemblyResult work)
   {
      ItemState state;
      PSStopwatch sw = new PSStopwatch();
      sw.start();
      try
      {
         // Send status
         state = IPSPublisherJobStatus.ItemState.ASSEMBLED;
         PSPubItemStatus status = new PSPubItemStatus(work.getReferenceId(),
               work.getJobId(), work.getPubServerId(), work.getDeliveryContext(), state);
         updateItemStatus(status);
         

         IPSDeliveryResult dresult = m_deliveryManager.process(work);
         String message = dresult.getFailureMessage();
         state = OUTCOME_STATE.get(dresult.getOutcome());
         status = new PSPubItemStatus(work.getReferenceId(), work.getJobId(), work.getPubServerId(), work.getDeliveryContext(),
               state);
         status.extractInfo(work);
         if (StringUtils.isNotEmpty(message))
         {
            status.addMessage(message);
         }
         if (dresult.getUnpublishData() != null)
         {
            status.setUnpublishingInformation(dresult.getUnpublishData());
         }
         updateItemStatus(status);
      }
      catch (Exception e)
      {
         log.error("Problem in delivery ", e);
         state = IPSPublisherJobStatus.ItemState.FAILED;
         PSPubItemStatus status = new PSPubItemStatus(work.getReferenceId(),
               work.getJobId(), work.getPubServerId(), work.getDeliveryContext(), state);
         status.addMessage("Problem in delivery: " + e.getLocalizedMessage());
         updateItemStatus(status);
      }
      finally
      {
         sw.stop();
         log.debug("Delivered item " + sw);
         // we're done with the item, clear
         work.clearResults();
      }
      return state;
   }

   /**
    * Handle an assembly exception by creating a new failure assembly result.
    * 
    * @param item the assembly work item, modified by this method to show a
    *           failed result.
    * @param e the exception that is being reported, never <code>null</code>.
    * @return the result, a list of one element.
    */
   @SuppressWarnings("deprecation")
   private List<IPSAssemblyResult> handleAssemblyException(
         IPSAssemblyItem item, Exception e)
   {
      IPSAssemblyResult work = (IPSAssemblyResult) item;
      // If we get here we had some bad failure, which should be
      // logged.
      // The item should also be marked as a failure for future
      // processing
      log.error("Problems assembly item, content guid: " + item.getId().toString(), e);
      if (work.getStatus() == null || work.getStatus().equals(Status.SUCCESS))
      {
         work.setStatus(Status.FAILURE);
         setEncodedResultData(work, e.getLocalizedMessage());
      }
      return Collections.singletonList(work);
   }
   
   /**
    * Determines if empty delivery locations are allowed by the specified delivery type.
    * 
    * @param psvc the publisher service, assumed not <code>null</code>.
    * @param deliveryType assumed to be the name of a valid delivery type.
    * 
    * @return <code>true</code> if empty delivery locations are allowed by the delivery
    * type, <code>false</code> otherwise.
    * 
    * @throws InvocationTargetException if an error occurs.
    * @throws IllegalAccessException if an error occurs.
    */
   private boolean isEmptyLocationAllowed(IPSPublisherService psvc, String deliveryType)
           throws InvocationTargetException, IllegalAccessException, PSNotFoundException {
      if (ms_deliveryTypeToAllowsEmptyLocation.containsKey(deliveryType))
      {
         return ms_deliveryTypeToAllowsEmptyLocation.get(deliveryType);
      }
      
      boolean isEmptyLocationAllowed = false;
      
      IPSDeliveryType dType = psvc.loadDeliveryType(deliveryType);
      IPSDeliveryHandler dHandler =
         (IPSDeliveryHandler) PSBaseServiceLocator.getBean(dType.getBeanName());
      Method[] methods = dHandler.getClass().getMethods();
      for (Method meth : methods)
      {
         if (meth.getName().equalsIgnoreCase("isEmptyLocationAllowed"))
         {
            isEmptyLocationAllowed = (Boolean) meth.invoke(dHandler);
            break;
         }
      }
      
      ms_deliveryTypeToAllowsEmptyLocation.put(deliveryType, isEmptyLocationAllowed);
      
      return isEmptyLocationAllowed;
   }
}
