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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.search;

import com.percussion.search.data.PSSearchIndexQueueItem;

import java.util.Collection;
import java.util.List;

import org.hibernate.HibernateException;
import org.springframework.dao.DataAccessResourceFailureException;

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
