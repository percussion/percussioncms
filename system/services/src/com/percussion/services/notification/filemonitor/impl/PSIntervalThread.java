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

