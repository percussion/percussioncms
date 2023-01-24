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
package com.percussion.itemmanagement.data;

import com.percussion.share.data.PSItemProperties;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * @author leonardohildt
 * 
 */
@XmlRootElement(name = "ApprovableItem")
public class PSApprovableItem extends PSItemProperties
{

    private static final long serialVersionUID = 1L;

    private Boolean approve = false;

    private String approvalStatus;

    private String approvalMessage;

    public PSApprovableItem()
    {
    }

    public Boolean isApprove()
    {
        return approve;
    }

    /**
     * @param approve indicates whether the item should be approve or not.
     */
    public void setApprove(Boolean approve)
    {
        this.approve = approve;
    }

    public String getApprovalStatus()
    {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus)
    {
        this.approvalStatus = approvalStatus;
    }

    public String getApprovalMessage()
    {
        return approvalMessage;
    }

    public void setApprovalMessage(String approvalMessage)
    {
        this.approvalMessage = approvalMessage;
    }
}
