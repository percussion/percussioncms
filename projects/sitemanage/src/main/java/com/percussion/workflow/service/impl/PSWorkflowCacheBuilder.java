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
package com.percussion.workflow.service.impl;

import com.percussion.maintenance.service.IPSMaintenanceManager;
import com.percussion.maintenance.service.IPSMaintenanceProcess;
import com.percussion.services.workflow.IPSWorkflowService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author JaySeletz
 *
 */
public class PSWorkflowCacheBuilder implements Runnable, IPSMaintenanceProcess
{
    public static Log log = LogFactory.getLog(PSWorkflowCacheBuilder.class);
    
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
            log.error("Failed to build the workflow cache, the cache will be filled on demand", e);
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
