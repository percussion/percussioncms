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
