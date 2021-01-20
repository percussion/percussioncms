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
