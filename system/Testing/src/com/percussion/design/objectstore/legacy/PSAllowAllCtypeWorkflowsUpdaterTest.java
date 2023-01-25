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

package com.percussion.design.objectstore.legacy;

import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSWorkflowInfo;
import com.percussion.services.catalog.data.PSObjectSummary;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.utils.testing.IntegrationTest;
import org.apache.commons.collections.IteratorUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.*;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class PSAllowAllCtypeWorkflowsUpdaterTest
{

   @Test
   public void testUpdateInfo()
   {
      PSAllowAllCtypeWorkflowsUpdater updater = new PSAllowAllCtypeWorkflowsUpdater();
      PSWorkflowInfo info = new PSWorkflowInfo(PSWorkflowInfo.TYPE_EXCLUSIONARY, new ArrayList());
      updater.updateInfo(info);
      
      assertTrue(PSWorkflowInfo.TYPE_INCLUSIONARY.equals(info.getType()));
      assertFalse(info.isExclusionary());
      Iterator values = info.getValues();
      assertTrue(values.hasNext());
      
      Set<Integer> wfIdSet = new HashSet<Integer>();
      IPSWorkflowService svc = PSWorkflowServiceLocator.getWorkflowService();
      List<PSObjectSummary> sums = svc.findWorkflowSummariesByName(null);
      for (PSObjectSummary sum : sums)
      {
         wfIdSet.add(sum.getGUID().getUUID());
      }
      
      assertEquals(wfIdSet, new HashSet<Integer>(IteratorUtils.toList(values)));
   }

   @Test
   public void testCanUpdateComponent()
   {
      PSAllowAllCtypeWorkflowsUpdater updater = new PSAllowAllCtypeWorkflowsUpdater();
      assertTrue(updater.canUpdateComponent(PSContentEditor.class));
   }

}
