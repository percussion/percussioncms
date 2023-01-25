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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

public abstract class PSAbstractResourceWatcher extends PSIntervalThread 
   implements IPSResourceWatcher 
{

    /**
     * The list of listeners to notify when this resource changes.
     */
    private Collection<IPSResourceListener> listeners = new LinkedList<>();

    /**
     * Gets the total count of registered listeners.
     * 
     * @return the listener count.
     */
    public synchronized int getListenerCount()
    {
       return listeners.size();
    }
    
    /**
     * Constructor that takes the resource to watch.
     *
     * @param name            the resource to watch
     * @param intervalSeconds The interval to use when monitoring this
     *                        resource.  I.e., ever x seconds, check this resource to see
     *                        what has changed.
     */
    public PSAbstractResourceWatcher(int intervalSeconds, String name) {

        //Get the common thread interval stuff set up.
        super(intervalSeconds, name);
    }

    /**
     * Remove all listeners from this watcher.
     */
    public synchronized void removeAllListeners() {

        //Clear the list of all listeners
        listeners.clear();
    }

    /**
     * Add a listener for this resource.
     *
     * @param listener the listener to add
     */
    public synchronized void addListener(IPSResourceListener listener) {

        //And add the listener
        listeners.add(listener);
    }

    /**
     * Remove the listener for this resource.
     *
     * @param listener the listener to remove
     */
    public synchronized void removeListener(IPSResourceListener listener) {

        //Iterate through the listeners and remove the this listener
        listeners.remove(listener);
    }

    /**
     * When an item is added to this resource, this method will be called.  It
     * will fire the onAdd() method of all its listeners, passing in the item
     * that has been added.
     *
     * @param newResource the item that has been added to this resource.
     */
    protected synchronized void resourceAdded(Object newResource) {

        //Iterate through the listeners and fire the onAdd method
        Iterator<IPSResourceListener> listIt = listeners.iterator();

        while (listIt.hasNext()) {
            listIt.next().onAdd(newResource);
        }
    }

    /**
     * When an item is changed in this resource, this method will be called.
     * It will fire the onChange() method of all its listeners, passing in
     * the item that has been changed.
     *
     * @param changedResource the item that has been added to this resource.
     */
    protected synchronized void resourceChanged(Object changedResource) {

        //Iterate through the listeners and fire the onChange method
        Iterator<IPSResourceListener> listIt = listeners.iterator();

        while (listIt.hasNext()) {
            listIt.next().onChange(changedResource);
        }
    }

    /**
     * When an item is deleted in this resource, this method will be called.
     * It will fire the onChange() method of all its listeners, passing in
     * the item that has been deleted.
     *
     * @param deletedResource the item that has been added to this resource.
     */
    protected synchronized void resourceDeleted(Object deletedResource) {

        //Iterate through the listeners and fire the onDelete method
        Iterator<IPSResourceListener> listIt = listeners.iterator();

        while (listIt.hasNext()) {
            listIt.next().onDelete(deletedResource);
        }
    }

    /**
     * When monitoring starts on an item, this method will be called.
     *
     * @param monitoredResource the resource that is now being monitored
     */
    protected synchronized void monitoringStarted(Object monitoredResource) {

        //Iterate through the listeners and fire the onStart method
        Iterator<IPSResourceListener> listIt = listeners.iterator();

        while (listIt.hasNext()) {
            listIt.next().onStart(monitoredResource);
        }
    }

    /**
     * When monitoring stops on an item, this method will be called.
     *
     * @param notMonitoredResource the resource that is not being monitored anymore.
     */
    protected synchronized void monitoringStopped(Object notMonitoredResource) {

        //Iterate through the listeners and fire the onStop method
        Iterator<IPSResourceListener> listIt = listeners.iterator();

        while (listIt.hasNext()) {
            listIt.next().onStop(notMonitoredResource);
        }
    }

    /**
     * The interval has expired and now it's time to do something.
     */
    protected abstract void doInterval();
}
