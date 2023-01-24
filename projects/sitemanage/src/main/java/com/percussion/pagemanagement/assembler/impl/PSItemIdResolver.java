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
