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
package com.percussion.foldermanagement.service.impl;

import com.percussion.foldermanagement.data.PSFolderItem;
import com.percussion.foldermanagement.data.PSGetAssignedFoldersJobStatus;
import com.percussion.foldermanagement.service.IPSFolderService;
import com.percussion.share.async.impl.PSAsyncJob;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author JaySeletz
 *
 */
@Component("getAssignedFoldersJob")
@Scope("prototype")
public class PSGetAssignedFoldersJob extends PSAsyncJob
{
    private IPSFolderService folderService;
    private String workflowName;
    private String path;
    private boolean includeFoldersWithDifferentWorkflow;
    PSGetAssignedFoldersJobStatus status;
    
    private static final Logger log = LogManager.getLogger(PSGetAssignedFoldersJob.class);

    
    @Override
    public void doRun()
    {
        try
        {
            if (!isCancelled())
            {    
                List<PSFolderItem> folderItems = folderService.getAssignedFolders(workflowName, path,
                        includeFoldersWithDifferentWorkflow);
                status.setFolderItems(folderItems);
                
                status.setStatus(String.valueOf(COMPLETE_STATUS));
                setResult(status);
                setStatus(COMPLETE_STATUS);
                setStatusMessage("completed");
            }

        }
        catch (Exception e)
        {
            if (!isCancelled())
            {
                log.error(e.getLocalizedMessage(), e);
            }
            setStatus(ABORT_STATUS);
            setStatusMessage("aborted");
        }
        finally
        {
            setCompleted();
        }
    }


    @Override
    protected void doInit(Object config) throws IPSFolderService.PSWorkflowNotFoundException {
        Object[] args = (Object[]) config;
        workflowName = (String) args[0];
        path = (String) args[1];
        includeFoldersWithDifferentWorkflow = (Boolean) args[2];
        Validate.notEmpty(workflowName, "workflowName cannot be empty");
        Validate.notEmpty(path, "path cannot be empty");
        folderService.validateWorkflow(workflowName);
        status = new PSGetAssignedFoldersJobStatus();
    }

    @Autowired
    public void setFolderService(IPSFolderService folderService)
    {
        this.folderService = folderService;
    }
}
