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
package com.percussion.maintenance.service;

/**
 * Tracks if maintenance processes are in progress and records if any have had failures.
 *  
 * @author JaySeletz
 *
 */
public interface IPSMaintenanceManager
{

    /**
     * Called by processes that are starting maintenance work.  This will put the server into maintenance mode
     * until the work is completed.
     * 
     * @param process The process, not <code>null</code>.
     */
    void startingWork(IPSMaintenanceProcess process);

    /**
     * Determine if maintenance work is in progress.
     * 
     * @return <code>true</code> if so, <code>false</code> if not.
     */
    boolean isWorkInProgress();

    /**
     * Called by processes that have completed work previously started
     * 
     * @param process The process, not <code>null</code>.
     */
    void workCompleted(IPSMaintenanceProcess process);

    /**
     * Determine if maintenance work has failed.  May be called regardless of whether work is in progress.
     * 
     * @return <code>true</code> if a maintenance process has failed, <code>false</code> if not.
     */
    boolean hasFailures();

    /**
     * Called by failed processes that have completed work previously started
     * 
     * @param process The process, not <code>null</code>.
     */
    void workFailed(IPSMaintenanceProcess process);
    
    /**
     * If there are failures, clears them to allow the system to exit maintenance mode.  Since this could potentially
     * allow the system to be accessed while in an unstable state, it should be used with extreme care and requires
     * Admin privileges to execute.
     * 
     * @return <code>true</code> if there were previous failures to clear, <code>false</code> if there were no failures.
     */
    boolean clearFailures();
    
}
