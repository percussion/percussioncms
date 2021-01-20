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
    private Collection<IPSResourceListener> listeners = new LinkedList<IPSResourceListener>();

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
