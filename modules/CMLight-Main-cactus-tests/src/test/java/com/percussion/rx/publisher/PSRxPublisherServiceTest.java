/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.rx.publisher;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.rx.publisher.IPSPublisherJobStatus.State;
import com.percussion.rx.publisher.data.PSDemandWork;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.publisher.IPSPubItemStatus;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.utils.guid.IPSGuid;

import java.util.ArrayList;
import java.util.List;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.junit.experimental.categories.Category;

/**
 * Test rxpublisher functionality as appropriate.
 * 
 * @author dougrand
 */
@Category(IntegrationTest.class)
public class PSRxPublisherServiceTest extends ServletTestCase
{
   /**
    * The publisher service, initialized on first use.
    */
   private static IPSRxPublisherService ms_rxpub = null;

   /**
    * Accessor that will lookup service.
    * 
    * @return the service, never <code>null</code>.
    */
   public IPSRxPublisherService getSvc()
   {
      if (ms_rxpub == null)
      {
         ms_rxpub = PSRxPublisherServiceLocator.getRxPublisherService();
      }
      return ms_rxpub;
   }

   public void testDummy()
   {
      
   }
   
 
   /**
    * Test demand publishing calls. Also tests some job status calls.
    * 
    */
   /*
   public void testDemandPublishing()
      throws InterruptedException, PSNotFoundException
   {
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      PSDemandWork work = new PSDemandWork();
      IPSGuid folderid = gmgr.makeGuid(new PSLocator(315));
      IPSGuid contentitem = gmgr.makeGuid(new PSLocator(372));
      work.addItem(folderid, contentitem);

      long requestid = getSvc().queueDemandWork(310, work);
      long rid = work.getRequest();
      assertEquals("Invalid request id", requestid, rid);
      
      State rstate = getSvc().getDemandWorkStatus(rid);
      int trycount = 30;
      while(rstate == null && trycount > 0)
      {
         Thread.sleep(1000);
         rstate = getSvc().getDemandWorkStatus(rid);
         trycount--;
      }
      assertTrue("Invalid state " + rstate,
            rstate != null && !rstate.isTerminal());
      
      IPSGuid edition = gmgr.makeGuid(310, PSTypeEnum.EDITION);
      long ejobid = getSvc().getEditionJobId(edition);
      trycount = 30;
      while(ejobid == 0 && trycount > 0)
      {
         Thread.sleep(1000);
         ejobid = getSvc().getEditionJobId(edition);
         trycount--;
      }
      assertTrue("Invalid edition job id: " + ejobid, ejobid != 0);

      boolean done = false;
      while (!done)
      {
         IPSPublisherJobStatus status = getSvc().getPublishingJobStatus(ejobid);
         if (status.getState().isTerminal())
         {
            done = true;
         }
         else
         {
            Thread.sleep(1000);
         }
      }
      rstate = getSvc().getDemandWorkStatus(rid);
      assertTrue("Not terminal state: " + rstate, rstate.isTerminal());
      
      IPSPublisherService psvc =
         PSPublisherServiceLocator.getPublisherService();
      List<IPSPubItemStatus> stati = psvc.findPubItemStatusForJob(ejobid);
      assertEquals("Unexpected status size", 1, stati.size());
      
      // test find last published items
      IPSGuid siteId = new PSGuid(PSTypeEnum.SITE, 301);
      List<Integer> contentIds = new ArrayList<Integer>();
      contentIds.add(new Integer(312));  // rffHome
      int lastPublished = psvc.findLastPublishedItemsBySite(siteId, contentIds);
      assertTrue(lastPublished >= 0);
   }*/
}
