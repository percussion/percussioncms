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
package com.percussion.share.dao.impl;

import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.share.dao.IPSRelationshipCataloger;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.util.PSSiteManageBean;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.system.IPSSystemWs;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@PSSiteManageBean("relationshipCataloger")
public class PSRelationshipCataloger implements IPSRelationshipCataloger
{
    private IPSSystemWs systemWs;
    private IPSIdMapper idMapper;
    private PSItemDefManager itemDefManager;
    
    @Autowired
    public PSRelationshipCataloger(IPSIdMapper idMapper, PSItemDefManager itemDefManager, IPSSystemWs systemWs)
    {
        super();
        this.idMapper = idMapper;
        this.itemDefManager = itemDefManager;
        this.systemWs = systemWs;
    }

    public List<String> findOwners(String id, String name, String contentType, String slotName) 
    {
        IPSGuid guid = idMapper.getGuid(id);
        
        PSRelationshipFilter filter = new PSRelationshipFilter();
        filter.setName(name);
        if (contentType != null) {
            long ctid = getContentTypeId(contentType);
            filter.setOwnerContentTypeId(ctid);
        }
        List<IPSGuid> guids = systemWs.findOwners(guid, filter);
        List<String> ids = new ArrayList<>();
        for (IPSGuid gid: guids) {
            ids.add(idMapper.getString(gid));
        }
        return ids;
    }
    
    private long getContentTypeId( String contentType ) 
    {
        try
        {
            return itemDefManager.contentTypeNameToId(contentType);
        }
        catch (PSInvalidContentTypeException e)
        {
            throw new RuntimeException("Invalid content type name", e);
        }
    
    }

}
