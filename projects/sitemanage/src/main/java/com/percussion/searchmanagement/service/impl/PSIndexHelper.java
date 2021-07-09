/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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

package com.percussion.searchmanagement.service.impl;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.search.PSSearchIndexEventQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
/**
 * 
 * @author robertjohansen
 *
 *This class is meant to assist in loading locators
 *into the search index queue. It has its own thread to
 *handle the work so when large amounts of items need to be 
 *indexed due to a change of an item then we are not holding
 *up the UI and the user can continue his or her task. The processing
 *becomes a background process.
 */
@Component("indexHelper")
public class PSIndexHelper implements Runnable
{
    private static final Logger log = LogManager.getLogger(PSIndexHelper.class);

    private PSSearchIndexEventQueue queue;

    private CopyOnWriteArrayList<PSLocator> ids;

    private static final Object lock = new Object();

    private Thread thread;

    public PSIndexHelper()
    {
        ids = new CopyOnWriteArrayList<>();
        thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * get the search index queue instance. May not have been
     * initialized by the time this thread starts.
     * @return PSSearchIndexEventQueue queue
     */
    private PSSearchIndexEventQueue getIndexEventQueue()
    {
        if (queue == null)
            queue = PSSearchIndexEventQueue.getInstance();
        return queue;
    }

    /**
     * Add items to the concurrent data structure so that they can
     * be processed by the background process.
     * @param locas<PSLocator> locas
     */
    public void addItemsForIndex(Set<PSLocator> locas)
    {
        try
        {
            ids.addAll(locas);
        }
        catch (Exception e)
        {
            log.warn("Could not ad Item ids to be indexed: {}" , this.getClass().getName());
        }
        finally
        {
            synchronized (lock)
            {
                lock.notifyAll();
            }
        }
    }

    /**
     * The real work of the background process.
     * Adds the locators into the search index queue
     */
    public void index() throws InterruptedException {
        while (ids.isEmpty())
        {
            try
            {
                synchronized (lock)
                {
                    lock.wait();
                }
            }
            catch (InterruptedException e)
            {
                log.warn("{} Thread InterruptedException {}",
                        this.getClass().getName(),
                        e.getMessage());
                Thread.currentThread().interrupt();
                throw new InterruptedException(e.getMessage());
            }
        }

        try
        {

            for (PSLocator locator : ids)
            {
                getIndexEventQueue().indexItem(locator);
                ids.remove(locator);
            }
        }
        catch (Exception e)
        {
            log.warn("Trouble adding content to search index queue - {} Error: {}",
                    PSIndexHelper.class.getName(),
                    e.getMessage());
        }
    }

    @Override
    public void run()
    {
        while (true) {
            try {
                index();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            if(Thread.currentThread().isInterrupted()){
                break;
            }
        }
    }
}
