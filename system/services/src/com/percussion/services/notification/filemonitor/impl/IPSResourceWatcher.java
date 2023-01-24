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

/**
 * Representation of an object that watches a resource (directory, database
 * etc...) for any changes and notifies its list of listeners when an event
 * occurs.
 */
public interface IPSResourceWatcher {

    /**
     * Start watching the resource.
     */
    public void start();

    /**
     * Add a listener to this watcher.
     *
     * @param listener the listener to add
     */
    public void addListener(IPSResourceListener listener);

    /**
     * Remove a listener from this watcher.
     *
     * @param listener the listener to remove
     */
    public void removeListener(IPSResourceListener listener);

    /**
     * Stop the monitoring of the particular resource.
     */
    public void stop();
}

