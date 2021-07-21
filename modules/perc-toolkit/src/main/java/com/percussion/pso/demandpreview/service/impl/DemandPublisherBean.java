/*
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 * com.percussion.pso.demandpreview.service.impl DemandPublisherBean.java
 *
 */
package com.percussion.pso.demandpreview.service.impl;


import com.percussion.error.PSExceptionUtils;
import com.percussion.pso.demandpreview.service.DemandPublisherService;
import com.percussion.rx.publisher.IPSPublisherJobStatus.State;
import com.percussion.rx.publisher.IPSRxPublisherService;
import com.percussion.rx.publisher.PSRxPublisherServiceLocator;
import com.percussion.rx.publisher.data.PSDemandWork;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.utils.guid.IPSGuid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeoutException;

/**
 * Provides a Spring Bean implementation of the Demand Publisher 
 * Service. The XML file that defines this bean will specify 
 * the Preview Site, Template and Context to be used on each 
 * site. 
 *
 * @author davidbenua
 *
 */
public class DemandPublisherBean implements DemandPublisherService
{
   /**
    * Logger for this class
    */
   private static final Logger log = LogManager.getLogger(DemandPublisherBean.class);
   
   /**
    * Service for RxPublisherService
    */
   private IPSRxPublisherService rxPubSvc = null; 
   /**
    * timeout in seconds
    */
   private long timeout = 100L; //seconds
   
   /**
    * Sleep interval for polling.
    */
   private long sleeptime = 500L; //milliseconds
   
   /**
    * Initialize this bean. Intended to be called from the 
    * Spring <code>init-method</code> attribute.
    *  
    */
   public void init()
   {
      if(rxPubSvc == null)
      {
         rxPubSvc = PSRxPublisherServiceLocator.getRxPublisherService(); 
      }
   }

   public long queueDemandWork(IPSEdition edition, IPSGuid content, IPSGuid folder)
      throws TimeoutException
   {
      log.trace("Queueing demand work..."); 
      PSDemandWork work = new PSDemandWork(); 
      work.addItem(folder, content);
      Long jobId = null; 
      int editionId = edition.getGUID().getUUID();
      log.debug("demand edition is {}",  editionId);
      log.debug("work contains {} items",  work.getContent().size() );
      long requestId=-1;
      try {
          requestId = rxPubSvc.queueDemandWork(editionId, work);
      } catch (PSNotFoundException e) {
         log.error(e.getMessage());
         log.debug(e.getMessage(),e);
      }
      log.debug("Started demand job. Request id = {}",  requestId);
      long timeLimit = System.currentTimeMillis() + timeout * 1000; 
      
      while(jobId == null)
      {
         jobId = rxPubSvc.getDemandRequestJob(requestId); 
         if(jobId == null)
         {
            log.debug("Job Not queued for request id {}" , requestId);
            long now = System.currentTimeMillis();  
            log.trace("time now {}" , now);
            if(now > timeLimit)
            {  
               throw new TimeoutException("Publishing did not complete before timeout. "); 
            }
            try
            {
               log.trace("sleeping..."); 
               Thread.sleep(sleeptime);
            } catch (InterruptedException ex)
            {
               log.error("Interrupted: {} " , PSExceptionUtils.getMessageForLog(
                       ex));
               Thread.currentThread().interrupt();
            } 
         }
      }
      log.debug("JobID {} for request id {}"  ,jobId , requestId);
      return requestId;    
   }

   public State waitDemandWorkComplete(long jobId) throws TimeoutException
   {  
      long timeLimit = System.currentTimeMillis() + timeout * 1000; 
      log.trace("time out " + timeLimit); 
      State state = State.INITIAL; 
      while(true)
      {
         state = rxPubSvc.getDemandWorkStatus(jobId);
         if(log.isDebugEnabled())
         {
            if(state == null)
            {
               log.debug("Status for job {} is null", jobId);
            }
         }
         if(state != null && state.isTerminal())
         {
            return state;  
         }
         long now = System.currentTimeMillis();  
         log.trace("time now {}" ,now);
         if(now > timeLimit)
         {  
            throw new TimeoutException("Publishing did not complete before timeout. "); 
         }
         try
         {
            log.trace("sleeping..."); 
            Thread.sleep(sleeptime);            
         } catch (InterruptedException ex)
         {
            log.error("Interrupted: {}" , PSExceptionUtils.getMessageForLog(ex));
            Thread.currentThread().interrupt();
         } 
         
      }
   }

   public void publishAndWait(IPSEdition edition, IPSGuid content, IPSGuid folder) 
      throws TimeoutException, PSAssemblyException 
   {
      long jobId = queueDemandWork(edition, content, folder); 
      State state = waitDemandWorkComplete(jobId);
      if(state != State.COMPLETED)
      {
         String emsg = "Publishing failed " + state; 
         log.error(emsg); 
         throw new PSAssemblyException(0, emsg);
      }
   }

   /**
    * Gets the timeout (in seconds)
    * @return the timeout
    */
   public long getTimeout()
   {
      return timeout;
   }

   /**
    * Sets the timeout (in seconds).
    * @param timeout the timeout to set
    */
   public void setTimeout(long timeout)
   {
      this.timeout = timeout;
   }

   /**
    * Sets the publisher service. 
    * Used for unit testing only.
    * @param rxPubSvc the rxPubSvc to set
    */
   protected void setRxPubSvc(IPSRxPublisherService rxPubSvc)
   {
      this.rxPubSvc = rxPubSvc;
   }
 
   
}
