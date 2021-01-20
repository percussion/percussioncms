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
package com.percussion.sitemanage.task.impl;

import java.util.Date;

import com.percussion.services.publisher.IPSPubItemStatus;
import com.percussion.services.publisher.IPSSiteItem.Operation;
import com.percussion.services.publisher.IPSSiteItem.Status;

public class MockPubItemStatus implements IPSPubItemStatus
{

    @Override
    public String getAssemblyUrl()
    {
        throw new UnsupportedOperationException("getAssemblyUrl is not yet supported");
    }

    @Override
    public int getContentId()
    {
        throw new UnsupportedOperationException("getContentId is not yet supported");
    }

    @Override
    public Date getDate()
    {
        throw new UnsupportedOperationException("getDate is not yet supported");
    }

    @Override
    public String getDeliveryType()
    {
        throw new UnsupportedOperationException("getDeliveryType is not yet supported");
    }

    @Override
    public Integer getElapsed()
    {
        throw new UnsupportedOperationException("getElapsed is not yet supported");
    }

    @Override
    public Integer getFolderId()
    {
        throw new UnsupportedOperationException("getFolderId is not yet supported");
    }

    @Override
    public String getLocation()
    {
        throw new UnsupportedOperationException("getLocation is not yet supported");
    }

    @Override
    public String getMessage()
    {
        throw new UnsupportedOperationException("getMessage is not yet supported");
    }

    @Override
    public Operation getOperation()
    {
        throw new UnsupportedOperationException("getOperation is not yet supported");
    }

    @Override
    public long getReferenceId()
    {
        throw new UnsupportedOperationException("getReferenceId is not yet supported");
    }

    @Override
    public int getRevisionId()
    {
        throw new UnsupportedOperationException("getRevisionId is not yet supported");
    }

    @Override
    public Status getStatus()
    {
        throw new UnsupportedOperationException("getStatus is not yet supported");
    }

    @Override
    public long getStatusId()
    {
        throw new UnsupportedOperationException("getStatusId is not yet supported");
    }

    @Override
    public Long getTemplateId()
    {
        throw new UnsupportedOperationException("getTemplateId is not yet supported");
    }

    @Override
    public Long getUnpublishRefId()
    {
        throw new UnsupportedOperationException("getUnpublishRefId is not yet supported");
    }

    @Override
    public boolean isHidden()
    {
        throw new UnsupportedOperationException("isHidden is not yet supported");
    }
}
