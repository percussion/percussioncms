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
package com.percussion.search.impl;

import com.percussion.cms.PSEditorChangeEvent;
import com.percussion.search.IPSSearchIndexQueue;
import com.percussion.search.PSSearchEditorChangeEvent;
import com.percussion.search.PSSearchIndexQueueLocator;
import com.percussion.search.data.PSSearchIndexQueueItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.junit.experimental.categories.Category;

/**
 * Class description - Unit tests for the FTS Search Indexer
 * {@link com.percussion.search.PSSearchIndexEventQueue}
 *
 * @author BillLanglais
 */
@Category(IntegrationTest.class)
public class PSSearchIndexQueueTest extends ServletTestCase
{
   @Override
   protected void setUp()
   {
      m_queueService = PSSearchIndexQueueLocator.getPSSearchIndexQueue();
      // Make sure the queue repository is empty
      m_queueService.deleteAllItems();
   }

   @Override
   protected void tearDown()
   {
      // Make sure the queue repository is empty
      m_queueService.deleteAllItems();
   }

   /**
    * Test method for
    * {@link com.percussion.search.impl.PSSearchIndexQueue#loadItems(int)} .
    */
   public void fixme_testLoadItems()
   {
      List<Integer> queueIds = initializeQueueEvents(ITEMS_CREATED);

      assertEquals(
            "queueService.Size() did not return the correct number of items",
            m_queueService.getEventCount(), ITEMS_CREATED);

      List<PSSearchIndexQueueItem> items = null;

      items = m_queueService.loadItems(5);

      assertEquals("loadItems did not return the correct number of items", 5,
            items.size());

      Integer x = queueIds.get(4);
      Integer y = items.get(4).getQueueId();

      assertEquals("loadItems did not return the items in the correct order",
            x, y);

      items = m_queueService.loadItems(0);
      assertEquals("loadItems did not return the correct number of items",
            ITEMS_CREATED, items.size());

      items = m_queueService.loadItems(0);
      assertEquals("loadItems did not return the correct number of items", 0,
            items.size());
      
      items = m_queueService.loadItems(50);
      assertEquals("loadItems did not return the correct number of items", 50,
            items.size());

      items = m_queueService.loadItems(-1);
      assertEquals("loadItems did not return the correct number of items",
            ITEMS_CREATED - 50, items.size());
   }

   /**
    * Test method for: .
    */
   public void fixme_testDeleteItems()
   {
      int initialCount = ITEMS_CREATED * 5;

      List<Integer> queueIds = initializeQueueEvents(initialCount);

      assertEquals(
            "initialization did not create the correct number of items",
            initialCount, m_queueService.getEventCount());

      Collection<Integer> queueIdsDelete = new ArrayList<Integer>();

      for (int cnt = 0; cnt < 50; cnt++)
      {
         queueIdsDelete.add(queueIds.get(cnt));
      }

      m_queueService.deleteItems(queueIdsDelete);
      assertEquals("Delete items did not delete the correct number of items",
            initialCount - 50, m_queueService.getEventCount());

      queueIdsDelete.clear();
      for (int cnt = 50; cnt < initialCount; cnt++)
      {
         queueIdsDelete.add(queueIds.get(cnt));
      }

      m_queueService.deleteItems(queueIdsDelete);
      assertEquals("Delete items did not delete all of the items", 0,
            m_queueService.getEventCount());

   }

   /**
    * Test method for
    * {@link com.percussion.search.impl.PSSearchIndexQueue#deleteAllItems() } .
    */
   public void fixme_testDeleteAllItems()
   {
      int initialCount = ITEMS_CREATED * 2;

      initializeQueueEvents(initialCount);

      m_queueService.deleteAllItems();
      assertEquals("Delete items did not delete all of the items", 0,
            m_queueService.getEventCount());
   }

   /**
    * Initializes two dummy events to use for test and then fills up the
    * repository with events. This also serves as a test for
    * {@link com.percussion.search.impl.PSSearchIndexQueue#saveItem(PSSearchIndexQueueItem)}
    *
    * @return a list of QueueIds associated with the persisted events. Never
    * <code> null</code>
    */
   private List<Integer> initializeQueueEvents(int itemsToCreate)
   {
      event = new PSSearchEditorChangeEvent(
            PSEditorChangeEvent.ACTION_REINDEX, 442, 1, -1, -1, 307, false);
      event.setPriority(100);
      item = new PSSearchIndexQueueItem(event);

      eventCommit = new PSSearchEditorChangeEvent(
            PSEditorChangeEvent.ACTION_REINDEX, 442, 1, -1, -1, 307, true);
      eventCommit.setPriority(100);
      itemCommit = new PSSearchIndexQueueItem(eventCommit);

      // First we need to create a bunch of items
      // We are not testing the contents of the event so we can just put any
      // text in place of what would be XML

      List<Integer> queueIds = new ArrayList<Integer>();

      for (int inc = 0; inc < itemsToCreate; ++inc)
      {

         if (inc < itemsToCreate - 1)
         {
            queueIds.add(m_queueService.saveItem(item));
         }
         else
         {
            queueIds.add(m_queueService.saveItem(itemCommit));

         }
      }
      return queueIds;
   }

   /**
    * Negative test for {@link com.percussion.search.impl.PSSearchIndexQueue} .
    */
   public void fixme_testNegativePSSearchIndexQueue()
   {
      try
      {
         @SuppressWarnings("unused")
         PSSearchIndexQueueItem junk = new PSSearchIndexQueueItem(null);
         fail("Creating PSSearchIndexQueueItem "
               + "with null should have thrown a exception");
      }
      catch (NullPointerException e)
      {

      }
      try
      {
         m_queueService.saveItem(null);
         fail("save with null should have thrown a exception");
      }
      catch (NullPointerException e)
      {

      }
   }

   public void testDummy()
   {
      // do nothing as a temp workaround the obfuscation ission.
   }
   
   private static final int ITEMS_CREATED = 1000;

   private IPSSearchIndexQueue m_queueService = null;

   private PSSearchEditorChangeEvent event;

   private PSSearchIndexQueueItem item;

   private PSSearchEditorChangeEvent eventCommit;

   private PSSearchIndexQueueItem itemCommit;
}
