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

package com.percussion.webservices.rhythmyx;

import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.PSTestBase;
import com.percussion.webservices.PSTestUtils;
import com.percussion.webservices.content.ContentSOAPStub;
import com.percussion.webservices.content.LoadItemsRequest;
import com.percussion.webservices.content.PSItem;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertTrue;

/**
 * Test case for loading related items
 */
@Category(IntegrationTest.class)
public class LoadRelatedItemTest extends PSTestBase
{
   /**
    * Construct the default content test case.
    */
   public LoadRelatedItemTest()
   {
   }

   @Test
   public void testLoadRelatedContentItems() throws Exception
   {
      ContentSOAPStub binding = getContentSOAPStub(null);

      PSTestUtils.setSessionHeader(binding, m_session);
      long id = SystemTestCase.getLegacyGuid(499, -1);
      LoadItemsRequest req = new LoadItemsRequest();
      req.setId(new long[] { id });
      req.setIncludeBinary(false);
      req.setAttachBinaries(false);
      req.setIncludeChildren(false);
      req.setIncludeRelated(true);
      PSItem[] items = binding.loadItems(req);
      assertTrue(items != null);
      assertTrue(items.length == 1);
      // there are 3 related items in 3 different slot
      assertTrue(items[0].getSlots().length == 3);

      req.setSlotName(new String[] {"rffContacts","rffList","rffSidebar"});
      items = binding.loadItems(req);
      assertTrue(items[0].getSlots().length == 3);

      req.setSlotName(new String[] {"rffContacts","rffList"});
      items = binding.loadItems(req);
      assertTrue(items[0].getSlots().length == 2);

      req.setSlotName(new String[] {"rffContacts"});
      items = binding.loadItems(req);
      assertTrue(items[0].getSlots().length == 1);
   }

}
