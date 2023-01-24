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
package com.percussion.foldermanagement.data;

import com.percussion.share.data.PSAbstractDataObject;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author JaySeletz
 *
 */
@XmlRootElement(name = "GetAssginedFoldersJobStatus")
public class PSGetAssignedFoldersJobStatus extends PSAbstractDataObject
{
    public List<PSFolderItem> getFolderItems()
    {
        return folderItems;
    }
    public void setFolderItems(List<PSFolderItem> folderItems)
    {
        this.folderItems = folderItems;
    }
    public String getStatus()
    {
        return status;
    }
    public void setStatus(String status)
    {
        this.status = status;
    }
    public long getJobId()
    {
        return jobId;
    }
    public void setJobId(long jobId)
    {
        this.jobId = jobId;
    }
    private List<PSFolderItem> folderItems;
    private String status;
    private String message;
    private long jobId;

    public String getMessage() {  return message; }

    public void setMessage(String message) {  this.message = message;}
}
