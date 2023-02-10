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
package com.percussion.foldermanagement.service;

import com.percussion.foldermanagement.data.PSFolderItem;
import com.percussion.foldermanagement.data.PSGetAssignedFoldersJobStatus;
import com.percussion.foldermanagement.data.PSWorkflowAssignment;
import com.percussion.share.test.PSDataServiceRestClient;

import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;

/**
 * @author miltonpividori
 * 
 */

public class PSFolderServiceRestClient extends PSDataServiceRestClient<PSFolderItem>
{
    public PSFolderServiceRestClient(String url)
    {
        super(PSFolderItem.class, url, "/Rhythmyx/services/foldermanagement/folders");
        setPostContentType(MediaType.APPLICATION_XML);
    }

    public List<PSFolderItem> getAssociatedFolders(String workflowName, String path, Boolean includeFoldersWithDifferentWorkflow)
    {
        getRequestHeaders().put("Accept", MediaType.APPLICATION_XML);
        return getObjectsFromPath(concatPath(getPath(), "/" + (StringUtils.isNotEmpty(workflowName) ? workflowName : "/"),
                "/", path.startsWith("//") ? path.substring(1) : path, "?includeFoldersWithDifferentWorkflow=" +
                includeFoldersWithDifferentWorkflow.toString()), PSFolderItem.class);
    }
    
    public void save(PSWorkflowAssignment workflowAssignment)
    {
        postObjectToPath(concatPath(getPath(), "workflowassignment"), workflowAssignment);
    }
    
    public String startGetAssociatedFoldersJob(String workflowName, String path, Boolean includeFoldersWithDifferentWorkflow)
    {
        getRequestHeaders().put("Accept", MediaType.APPLICATION_XML);
        return GET(concatPath(getPath(), "/GetAssociatedFoldersJob/start/" + (StringUtils.isNotEmpty(workflowName) ? workflowName : "/"),
                "/", path.startsWith("//") ? path.substring(1) : path, "?includeFoldersWithDifferentWorkflow=" +
                        includeFoldersWithDifferentWorkflow.toString()));
    }
    
    public PSGetAssignedFoldersJobStatus getAssociatedFoldersJobResults(String jobId)
    {
        getRequestHeaders().put("Accept", MediaType.APPLICATION_XML);
        return getObjectFromPath(concatPath(getPath(), "GetAssociatedFoldersJob/status", jobId), PSGetAssignedFoldersJobStatus.class);
    }
}
