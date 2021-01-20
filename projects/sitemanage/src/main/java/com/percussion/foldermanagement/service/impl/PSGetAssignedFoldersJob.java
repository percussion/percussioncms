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
package com.percussion.foldermanagement.service.impl;

import com.percussion.foldermanagement.data.PSFolderItem;
import com.percussion.foldermanagement.data.PSGetAssignedFoldersJobStatus;
import com.percussion.foldermanagement.service.IPSFolderService;
import com.percussion.share.async.impl.PSAsyncJob;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
    
    private static final Log log = LogFactory.getLog(PSGetAssignedFoldersJob.class);

    
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
    protected void doInit(Object config)
    {
        Object[] args = (Object[]) config;
        workflowName = (String) args[0];
        path = (String) args[1];
        includeFoldersWithDifferentWorkflow = (Boolean) args[2];
        Validate.notEmpty(workflowName, "workflowName cannot be empty");
        Validate.notEmpty(path, "path cannot be empty");
        folderService.validateWorkflow(workflowName);
        status = new PSGetAssignedFoldersJobStatus();
    }

    
    @Override
    public void cancelJob()
    {
        super.cancelJob();
        //interruptJob();
    }

    @Autowired
    public void setFolderService(IPSFolderService folderService)
    {
        this.folderService = folderService;
    }
}
