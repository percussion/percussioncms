/*
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 * test.percussion.pso.demandpreview.service.impl DemandPublisherBeanTest.java
 *
 */
package test.percussion.pso.demandpreview.service.impl;

import static org.junit.Assert.*;

import java.util.concurrent.TimeoutException;

import com.percussion.services.error.PSNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.junit.Before;
import org.junit.Test;

import com.percussion.pso.demandpreview.service.impl.DemandPublisherBean;
import com.percussion.rx.publisher.IPSRxPublisherService;
import com.percussion.rx.publisher.IPSPublisherJobStatus.State;
import com.percussion.rx.publisher.data.PSDemandWork;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.utils.guid.IPSGuid;

public class DemandPublisherBeanTest
{
   private static final Logger log = LogManager.getLogger(DemandPublisherBeanTest.class);
   
   Mockery context; 
   TestableDemandPublisherBean cut; 
   IPSRxPublisherService rxPubSvc;
   
   @Before
   public void setUp() throws Exception
   {
     context = new Mockery(); 
     cut = new TestableDemandPublisherBean(); 
     rxPubSvc = context.mock(IPSRxPublisherService.class); 
     cut.setRxPubSvc(rxPubSvc); 
   }
   
   @Test
   public final void testQueueDemandWork()
   {
        final IPSEdition edition = context.mock(IPSEdition.class,"edition");
        final IPSGuid content = context.mock(IPSGuid.class,"content");
        final IPSGuid folder = context.mock(IPSGuid.class,"folder");
        final IPSGuid editionId = context.mock(IPSGuid.class,"editionId");
        
        try
      {
         context.checking(new Expectations(){{
              one(edition).getGUID();
              will(returnValue(editionId)); 
              one(editionId).getUUID();
              will(returnValue(12)); 
              one(rxPubSvc).queueDemandWork(with(equal(12)), with(any(PSDemandWork.class)));
              will(returnValue(42L)); 
              one(rxPubSvc).getDemandRequestJob(42L);
              will(returnValue(new Long(345L))); 
           }});
           
           long jobid = cut.queueDemandWork(edition, content, folder);
           assertEquals(42L, jobid);
           context.assertIsSatisfied();
      } catch (TimeoutException ex)
      {
         log.error("Timeout Exception " + ex,ex);
         fail("Exception"); 
      } catch (PSNotFoundException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
   }
   @Test
   public final void testWaitDemandWorkComplete()
   {
      final State tState = State.COMPLETED;
      final State qState = State.QUEUEING; 
      final Sequence seq = context.sequence("twait"); 
        try
      {
         context.checking(new Expectations(){{
           one(rxPubSvc).getDemandWorkStatus(42L);
           inSequence(seq); 
           will(returnValue(qState));
           one(rxPubSvc).getDemandWorkStatus(42L);
           inSequence(seq);
           will(returnValue(tState));
           }});
           
           State state = cut.waitDemandWorkComplete(42L); 
           assertTrue(state.isTerminal());
           assertEquals(State.COMPLETED, state); 
           context.assertIsSatisfied();
           
      } catch (TimeoutException ex)
      {
         log.error("Timeout Exception " + ex,ex);
         fail("Exception"); 
      } 
   }
   
   @Test
   public final void testWaitDemandWorkTimeout()
   {
      final State qState = State.QUEUEING;
      cut.setTimeout(3); 
      
      context.checking(new Expectations(){{
         atLeast(2).of(rxPubSvc).getDemandWorkStatus(42L); 
         will(returnValue(qState)); 
      }});
      
      try
      {
         cut.waitDemandWorkComplete(42L);
         fail("did not get timeout exception"); 
      } catch (TimeoutException ex)
      {
         log.info("Caught Expected Exception " + ex);
      } 
      context.assertIsSatisfied(); 
   }
   
   private class TestableDemandPublisherBean extends DemandPublisherBean
   {

      @Override
      public void setRxPubSvc(IPSRxPublisherService rxPubSvc)
      {
          super.setRxPubSvc(rxPubSvc);
      }
      
   }
}
