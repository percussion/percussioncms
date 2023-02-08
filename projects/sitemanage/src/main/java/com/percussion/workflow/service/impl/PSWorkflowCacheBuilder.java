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
package com.percussion.workflow.service.impl;

import com.percussion.error.PSExceptionUtils;
import com.percussion.maintenance.service.IPSMaintenanceManager;
import com.percussion.maintenance.service.IPSMaintenanceProcess;
import com.percussion.services.workflow.IPSWorkflowService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author JaySeletz
 *
 */
public class PSWorkflowCacheBuilder implements Runnable, IPSMaintenanceProcess
{
    private static final Logger log = LogManager.getLogger(PSWorkflowCacheBuilder.class);
    
    static final String MAINT_PROC_NAME = PSWorkflowCacheBuilder.class.getName();
    
    private IPSWorkflowService workflowService;
    private IPSMaintenanceManager maintenanceManager;
    
    public PSWorkflowCacheBuilder(IPSWorkflowService workflowService, IPSMaintenanceManager maintenanceManager)
    {
        this.workflowService = workflowService;
        this.maintenanceManager = maintenanceManager;
    }

    /**
     * @param workflowService
     */
    public void buildWorkflowCache()
    {
        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void run()
    {
        try
        {
            maintenanceManager.startingWork(this);
            buildCache();
            maintenanceManager.workCompleted(this);
        }
        catch (Exception e)
        {
            log.error("Failed to build the workflow cache, the cache will be filled on demand, Error: {}", PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            maintenanceManager.workFailed(this);
        }
    }

    private void buildCache()
    {
        log.info("Initializing workflow cache");
        workflowService.findWorkflowsByName("");
        log.info("Workflow cache initialized");       
    }

    @Override
    public String getProcessId()
    {
        return MAINT_PROC_NAME;
    }

}
