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

package com.percussion.services.legacy;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.services.content.data.PSItemStatus;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.data.PSItemEntry;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import com.percussion.webservices.security.IPSSecurityWs;
import com.percussion.webservices.security.PSSecurityWsLocator;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.cactus.ServletTestCase;
import org.junit.experimental.categories.Category;

/**
 * Testing PSCmsObjectMgr in the context of the server
 */
@Category(IntegrationTest.class)
public class PSCmsObjectMgrServletTest extends ServletTestCase
{
   /**
    * Test the state name of an cached item has been updated after transition the item
    * 
    * @throws Exception
    */
   public void testStateName() throws Exception
   {
      IPSSecurityWs secWs = PSSecurityWsLocator.getSecurityWebservice();
      secWs.login(request, response, "admin1", "demo", null, 
            "Enterprise_Investments_Admin", null);     
      
      IPSCmsObjectMgr cmsMgr = PSCmsObjectMgrLocator.getObjectManager();
      
      IPSItemEntry item335 = cmsMgr.findItemEntry(335);
      String stateNameBeforeEdit = item335.getStateName();
      
      PSLegacyGuid id = new PSLegacyGuid(335, -1);
      IPSContentWs cw = PSContentWsLocator.getContentWebservice();
      
      PSItemStatus status = cw.prepareForEdit(id);
      String stateNameForEdit = item335.getStateName();
      
      if (status.isDidTransition())
         assertFalse(stateNameBeforeEdit.equals(stateNameForEdit));
      else
         assertTrue(stateNameBeforeEdit.equals(stateNameForEdit));
      cw.releaseFromEdit(status, false);
      String stateNameAfterEdit = item335.getStateName();
      assertTrue(stateNameBeforeEdit.equals(stateNameAfterEdit));
   }

   /**
    * Test the last modified date and post date of an cached item have been updated
    * after the item is touched or set the post date.
    * 
    * @throws InterruptedException
    */
   @SuppressWarnings("unchecked")
   public void testTouchPostDateOnItemEntry() throws InterruptedException
   {
      IPSCmsObjectMgr cmsMgr = PSCmsObjectMgrLocator.getObjectManager();
      
      IPSItemEntry item335 = cmsMgr.findItemEntry(335);

      Date dateBeforeTouch = item335.getLastModifiedDate();
      Thread.sleep(20);
      Integer[] ids = new Integer[] {335};
      cmsMgr.touchItems( Arrays.asList(ids) );
      Date dateAfterTouch = item335.getLastModifiedDate();
      assertTrue(dateAfterTouch.after(dateBeforeTouch));
      
      ((PSItemEntry)item335).setPostDate(null);
      cmsMgr.setPostDate(Arrays.asList(ids));
      Thread.sleep(20);
      assertTrue(item335.getPostDate().before(new Date()));
   }
   
   private void resetPostDate(IPSCmsObjectMgr cmsMgr, List<PSComponentSummary> sums) throws Exception
   {
      for (PSComponentSummary sum : sums)
      {
         sum.setContentPostDate(null);
      }
      cmsMgr.saveComponentSummaries(sums);
   }
   
   public void testSetPostDate() throws Exception
   {
      IPSCmsObjectMgr cmsMgr = PSCmsObjectMgrLocator.getObjectManager();
      
      Set<Integer> ids = new HashSet<Integer>();
      List<PSComponentSummary> sums = cmsMgr.findComponentSummariesByType(311);
      
      try
      {
         resetPostDate(cmsMgr, sums);
         for (PSComponentSummary sum : sums)
            ids.add(sum.getContentId());

         cmsMgr.setPostDate(ids);

         Date date = null;
         for (PSComponentSummary sum : cmsMgr.findComponentSummariesByType(311))
         {
            Date postDate = sum.getContentPostDate();
            assertNotNull(postDate);
            if (date == null)
            {
               date = sum.getContentPostDate();
            }
            else
            {
               assertEquals(date, postDate);
            }
         }
         
         cmsMgr.setPostDate(ids);
         
         for (PSComponentSummary sum : cmsMgr.findComponentSummariesByType(311))
         {
            assertEquals(date, sum.getContentPostDate());
         }
      }
      finally
      {
         try
         {
            cmsMgr.saveComponentSummaries(sums);
         }
         catch (Exception e)
         {
            System.out.println(e.getMessage());
         }
      }
   }
}
