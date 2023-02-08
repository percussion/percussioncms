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
 * Interface defining the basic resource listener methods for the 
 * generic resource monitoring system. This is extended and used
 * in the File Monitor Notification System.
 */
public interface IPSResourceListener {

    /**
     * Monitoring has just started on this new resource.
     *
     * @param monitoredResource the resource now being monitored.
     */
    public void onStart(Object monitoredResource);

    /**
     * Monitoring has just ended on this new resource.
     *
     * @param notMonitoredResource the resource not being monitored anymore.
     */
    public void onStop(Object notMonitoredResource);

    /**
     * Something has been added to this resource, or the resource itself has
     * been added.
     *
     * @param newResource the new resource
     */
    public void onAdd(Object newResource);


    /**
     * The resource has been changed.
     *
     * @param changedResource the changed resource
     */
    public void onChange(Object changedResource);


    /**
     * Something has been added to this resource, or the resource itself has
     * been added.
     *
     * @param deletedResource the deleted resource
     */
    public void onDelete(Object deletedResource);
}
