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
