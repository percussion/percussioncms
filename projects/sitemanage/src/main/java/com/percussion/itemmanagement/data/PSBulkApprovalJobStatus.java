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
package com.percussion.itemmanagement.data;

import com.percussion.share.data.PSAbstractDataObject;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Leonardo Hildt
 * 
 */
@XmlRootElement(name = "BulkApprovalJob")
public class PSBulkApprovalJobStatus extends PSAbstractDataObject
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Job Id for publishing job. Unique and never <code>null</code>.
     */
    private long jobId;

    /**
     * Job status
     */
    private String status;

    private PSApprovableItems items;

    public long getJobId()
    {
        return jobId;
    }

    public void setJobId(long jobId)
    {
        this.jobId = jobId;
    }
    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public PSApprovableItems getItems()
    {
        return items;
    }

    public void setItems(PSApprovableItems items)
    {
        this.items = items;
    }
}
