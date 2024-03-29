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

package com.percussion.searchmanagement.service.impl;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.error.PSExceptionUtils;
import com.percussion.search.PSSearchIndexEventQueue;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.inject.Singleton;
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
@Singleton
public class PSIndexHelper implements Runnable
{
    private static final Logger log = LogManager.getLogger(PSIndexHelper.class);

    private final PSSearchIndexEventQueue queue = PSSearchIndexEventQueue.getInstance();

    private CopyOnWriteArrayList<PSLocator> ids;

    private static final Object lock = new Object();

    private final Thread thread;

    public PSIndexHelper()
    {
        ids = new CopyOnWriteArrayList<>();
        thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }


    /**
     * Add items to the concurrent data structure so that they can
     * be processed by the background process.
     * @param locas<PSLocator> locas
     */
    @SuppressFBWarnings("NN_NAKED_NOTIFY")
    public void addItemsForIndex(Set<PSLocator> locas)
    {
        try
        {
            ids.addAll(locas);
        }
        catch (Exception e)
        {
            log.warn("Could not add Item ids to be indexed: {} Error: {}" , this.getClass().getName(),
                    PSExceptionUtils.getMessageForLog(e));
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

        //idle in background until there is content to be indexed.
        synchronized (lock) {
            while (ids.isEmpty()) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        try
        {
            for (PSLocator locator : ids)
            {
                queue.indexItem(locator);
                ids.remove(locator);
            }
        }
        catch (Exception e)
        {
            log.warn("Trouble adding content to search index queue - {} Error: {}",
                    PSIndexHelper.class.getName(),
                    PSExceptionUtils.getMessageForLog(e));
        }
    }

    @Override
    public void run()
    {
        do {
            try {
                index();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

        } while (!Thread.currentThread().isInterrupted());
    }
}
