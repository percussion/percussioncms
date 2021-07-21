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
package com.percussion.services.notification.filemonitor.impl;

public abstract class PSIntervalThread implements Runnable {

    /**
     * Whether or not this thread is active.
     */
    private boolean active = false;

    /**
     * The interval in seconds to run this thread
     */
    private int interval = -1;

    /**
     * The name of this pool (for logging/display purposes).
     */
    private String name;

    /**
     * This instance's thread
     */
    private Thread runner;

    /**
     * Construct a new interval thread that will run on the given interval
     * with the given name.
     *
     * @param intervalSeconds the number of seconds to run the thread on
     * @param name            the name of the thread
     */
    public PSIntervalThread(int intervalSeconds, String name) {

        this.interval = intervalSeconds * 1000;
        this.name = name;
    }

    /**
     * Start the thread on the specified interval.
     */
    public void start() {

        active = true;

        //If we don't have a thread yet, make one and start it.
        if (runner == null && interval > 0) {
            runner = new Thread(this);
            runner.setDaemon(true);
            if (++ms_monitorNumber == 1)
               runner.setName("PS-FileMonitorThread");
            else
               runner.setName("PS-FileMonitorThread-" + ms_monitorNumber);
            
            runner.start();
        }
    }

    /**
     * Stop the interval thread.
     */
    public void stop() {
        active = false;
    }

    /**
     * Not for public use.  This thread is automatically
     * started when a new instance is retrieved with the getInstance method.
     * Use the start() and stop() methods to control the thread.
     *
     * @see Thread#run()
     */
    public void run() {

        //Make this a relatively low level thread
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

        //Pause this thread for the amount of the interval
        while (active) {
            try {
                doInterval();
                Thread.sleep(interval);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * String representation of this object.  Just the name given to it an
     * instantiation.
     *
     * @return the string name of this pool
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * The interval has expired and now it's time to do something.
     */
    protected abstract void doInterval();
    
    private static int ms_monitorNumber = 0;
}

