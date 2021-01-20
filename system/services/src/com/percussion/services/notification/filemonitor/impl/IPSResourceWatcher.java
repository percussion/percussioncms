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

