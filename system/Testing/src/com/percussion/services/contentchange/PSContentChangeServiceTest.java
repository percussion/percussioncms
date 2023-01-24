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
package com.percussion.services.contentchange;

import static org.junit.Assert.*;

import com.percussion.services.contentchange.data.PSContentChangeEvent;
import com.percussion.services.contentchange.data.PSContentChangeType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.percussion.share.dao.IPSGenericDao;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author JaySeletz
 *
 */
@Category(IntegrationTest.class)
public class PSContentChangeServiceTest
{

   @Test
   public void test() throws IPSGenericDao.SaveException {
      IPSContentChangeService changeSvc = PSContentChangeServiceLocator.getContentChangeService();
      assertNotNull(changeSvc);
      
      List<Integer> changedItems;
      Set<Integer> site1Ids = new HashSet<>();

      try
      {
         PSContentChangeEvent changeEvent = new PSContentChangeEvent();
         int contentId1 = 1110;
         changeEvent.setContentId(contentId1);
         changeEvent.setChangeType(PSContentChangeType.PENDING_LIVE);
         changeEvent.setSiteId(1);
         changeSvc.contentChanged(changeEvent);
         site1Ids.add(contentId1);
         
         int contentId2 = 1112;
         changeEvent.setContentId(contentId2);
         changeEvent.setChangeType(PSContentChangeType.PENDING_LIVE);
         changeEvent.setSiteId(1);
         changeSvc.contentChanged(changeEvent);
         site1Ids.add(contentId2);
         
         int contentId3 = 1113;
         changeEvent.setContentId(contentId3);
         changeEvent.setChangeType(PSContentChangeType.PENDING_STAGED);
         changeEvent.setSiteId(2);
         changeSvc.contentChanged(changeEvent);
         
         // do again, ensure one event saved
         changeSvc.contentChanged(changeEvent);
         
         changedItems = changeSvc.getChangedContent(1, PSContentChangeType.PENDING_LIVE);
         assertNotNull(changedItems);
         assertEquals(site1Ids.size(), changedItems.size());
         
         for (Integer contentId : changedItems)
         {
            assertTrue(site1Ids.remove(contentId));
         }
         
         assertTrue(site1Ids.isEmpty());
         
         changedItems = changeSvc.getChangedContent(1, PSContentChangeType.PENDING_STAGED);
         assertNotNull(changedItems);
         assertEquals(0, changedItems.size());
         
         changedItems = changeSvc.getChangedContent(2, PSContentChangeType.PENDING_STAGED);
         assertNotNull(changedItems);
         assertEquals(1, changedItems.size());
         assertEquals(contentId3, changedItems.get(0).intValue());
         
         changeSvc.deleteChangeEvents(-1, contentId1, PSContentChangeType.PENDING_LIVE);
         changedItems = changeSvc.getChangedContent(1, PSContentChangeType.PENDING_LIVE);
         assertNotNull(changedItems);
         assertEquals(1, changedItems.size());
         assertEquals(contentId2, changedItems.get(0).intValue());
                  
         changeSvc.deleteChangeEvents(1, contentId2, PSContentChangeType.PENDING_LIVE);
         changedItems = changeSvc.getChangedContent(1, PSContentChangeType.PENDING_LIVE);
         assertNotNull(changedItems);
         assertEquals(0, changedItems.size());
         
         changeSvc.deleteChangeEventsForSite(2);
         changedItems = changeSvc.getChangedContent(1, PSContentChangeType.PENDING_LIVE);
         assertNotNull(changedItems);
         assertEquals(0, changedItems.size());
         
      }
      finally
      {
         changeSvc.deleteChangeEventsForSite(1);
         changeSvc.deleteChangeEventsForSite(2);
      }
   }

}
