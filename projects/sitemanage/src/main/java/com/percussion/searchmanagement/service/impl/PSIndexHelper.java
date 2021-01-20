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

package com.percussion.searchmanagement.service.impl;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.search.PSSearchIndexEventQueue;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
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
    public static Log log = LogFactory.getLog(PSIndexHelper.class);

    private PSSearchIndexEventQueue queue;

    private CopyOnWriteArrayList<PSLocator> ids;

    private final static Object lock = new Object();

    public Thread thread;

    public PSIndexHelper()
    {
        ids = new CopyOnWriteArrayList<PSLocator>();
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
     * @param Set<PSLocator> locas
     */
    public void addItemsForIndex(Set<PSLocator> locas)
    {
        try
        {
            ids.addAll(locas);
        }
        catch (Exception e)
        {
            log.warn("Could not ad Item ids to be indexed: " + this.getClass().getName());
        }
        finally
        {
            synchronized (lock)
            {
                lock.notify();
            }
        }
    }

    /**
     * The real work of the background process.
     * Adds the locators into the search index queue
     */
    public synchronized void index()
    {
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
                log.warn(this.getClass().getName() + " Thre InterruptedException " + e.getMessage());
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
            log.warn("Trouble adding content to search index queue - " + PSIndexHelper.class.getName(),e);
        }
    }

    @Override
    public void run()
    {
        while (true)
            index();
    }
}
