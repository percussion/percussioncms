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
package com.percussion.pagemanagement.assembler.impl;

import static org.apache.commons.lang.Validate.*;

import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.share.data.PSAbstractPersistantObject;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.util.PSSiteManageBean;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.content.IPSContentDesignWs;
import org.springframework.beans.factory.annotation.Autowired;

@PSSiteManageBean("itemIdResolver")
public class PSItemIdResolver
{
    IPSContentDesignWs contentDesignWs;
    IPSIdMapper idMapper;
    
    
    @Autowired
    public PSItemIdResolver(IPSContentDesignWs contentDesignWs, IPSIdMapper idMapper)
    {
        super();
        this.contentDesignWs = contentDesignWs;
        this.idMapper = idMapper;
    }



    public String getId(IPSAssemblyItem item) {
        IPSGuid pageGuid = item.getId();
        String id = idMapper.getString(pageGuid);
        return id;
    }
    
    public String getId(PSAbstractPersistantObject item) {
        notNull(item.getId(), "item id");
        IPSGuid guid = idMapper.getGuid(item.getId());
        guid = contentDesignWs.getItemGuid(guid);
        String pageId = idMapper.getString(guid);
        return pageId;
    }
    
    public void updateItemId(PSAbstractPersistantObject item) {
        if (item.getId() != null) {
            /*
             * We need to set the proper revisioned id on the item.
             */
            String pageId = getId(item);
            item.setId(pageId);
        }
    }
}
