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
package com.percussion.search;

import com.percussion.search.data.PSSearchIndexQueueItem;
import org.hibernate.HibernateException;
import org.springframework.dao.DataAccessResourceFailureException;

import java.util.Collection;
import java.util.List;

/**
 * IPSSearchIndexQueue - This is the interface that is used to persist Search
 * Index Events to a repository so that they will eventually be processed.
 * Methods for retrieving, saving and deleting are provided.
 * 
 * Class description
 * 
 * @author BillLanglais
 */
public interface IPSSearchIndexQueue
{
   
   /**
    * Retrieves a list full of Search Queue Items limiting the length of the
    * list by count. Events are loaded sorted by QueueId in ascending order.
    * Subsequent loads start with the next Queue Event after the last one loaded
    * previously ensuring a event is only read once unless
    * {@link com.percussion.search.impl.PSSearchIndexQueue#resetQueueLoadPosition() }
    * is called.
    *
    * @param count - Maximum number of items 0 means no limit
    * @return Collection of Queue Items found in repository never
    * <code>null</code> but could be empty
    */
   public List<PSSearchIndexQueueItem> loadItems(int count);

   /**
    * Persists a item to the database. A unique sequential queueId is assigned.
    *
    * @param event - item to be saved must not be <code>null</code>
    * @return queueId assigned to the item.
    * @throws IllegalArgumentException
    */
   public int saveItem(PSSearchIndexQueueItem event);

   /**
    * Deletes items from the database
    *
    * @param queueIds - list of IDs to delete, if <code>null</code> or empty it
    * does nothing
    */
   public void deleteItems(Collection<Integer> queueIds);

   /**
    * This method is used by the public method to delete all index items related
    * to an id.  This can be used to clean up purged items from the queue.
    * 
    * @param id - content id of item to delete related index queue items
    * @throws DataAccessResourceFailureException
    * @throws IllegalStateException
    * @throws HibernateException
    */
   public void deleteIdItems(int id) throws DataAccessResourceFailureException,
         IllegalStateException, HibernateException;

   /**
    * This method is used by the public method to delete all index items related
    * to an type id.  This can be used to clean up invalid content type items from the queue.
    * 
    * @param id - content id of item to delete related index queue items
    * @throws DataAccessResourceFailureException
    * @throws IllegalStateException
    * @throws HibernateException
    */
   public void deleteTypeIdItems(long id) throws DataAccessResourceFailureException,
   IllegalStateException, HibernateException;
   
   /**
    * Deletes all items from the database
    *
    */
   public void deleteAllItems();

   /**
    * @return the number of items in the repository
    */
   public int getEventCount();

   /**
    * When no events returned wait
    * @throws InterruptedException 
    */
   public void waitForPoll(long timeout) throws InterruptedException;

   public void shutdown();

   public boolean isShutdown();

   public void pollNow();
   
}
