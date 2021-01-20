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
