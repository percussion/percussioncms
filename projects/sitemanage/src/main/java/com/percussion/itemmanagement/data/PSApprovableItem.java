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
