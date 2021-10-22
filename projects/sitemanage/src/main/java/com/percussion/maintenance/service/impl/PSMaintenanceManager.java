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
package com.percussion.maintenance.service.impl;

import com.percussion.cms.IPSConstants;
import com.percussion.maintenance.service.IPSMaintenanceManager;
import com.percussion.maintenance.service.IPSMaintenanceProcess;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;


public class PSMaintenanceManager implements IPSMaintenanceManager
{

    private ConcurrentMap<String, IPSMaintenanceProcess> workingProcesses = new ConcurrentHashMap<>();
    private AtomicBoolean hasErrors = new AtomicBoolean(false);
    private static final Logger log = LogManager.getLogger(IPSConstants.SERVER_LOG);
    
    @Override
    public void startingWork(IPSMaintenanceProcess process)
    {
        Validate.notNull(process);
        Validate.notEmpty(process.getProcessId());

        IPSMaintenanceProcess curProc = workingProcesses.putIfAbsent(process.getProcessId(), process);
        if (curProc != null)
        {
            throw new IllegalStateException("A process with that ID is already running: " + process.getProcessId());            
        }
        
        log.info("Process starting work: {}" , process.getProcessId());
    }

    @Override
    public boolean isWorkInProgress()
    {
        return !workingProcesses.isEmpty();
    }

    @Override
    public void workCompleted(IPSMaintenanceProcess process)
    {
        removeRunningProcess(process);
        log.info("Process completed work: {}" , process.getProcessId());
    }

    @Override
    public boolean hasFailures()
    {
        return hasErrors.get();
    }


    @Override
    public void workFailed(IPSMaintenanceProcess process)
    {
        hasErrors.set(true);
        removeRunningProcess(process);
        log.warn("Process completed work with failures: {}" , process.getProcessId());
    }
    
    

    @Override
    public boolean clearFailures()
    {
        return hasErrors.getAndSet(false);
    }

    /**
     * Removes the supplied process from the running process map
     * 
     * @param process
     * 
     * @throws IllegalStateException if the process is not found in the map
     */
    private void removeRunningProcess(IPSMaintenanceProcess process)
    {
        IPSMaintenanceProcess proc = workingProcesses.remove(process.getProcessId());
        if (proc == null)
        {
            throw new IllegalStateException("No process found running with id: " + process.getProcessId());
        }
    }

}
