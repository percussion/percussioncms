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
package com.percussion.dashboardmanagement.dao.impl;

import static java.util.Arrays.*;

import java.util.List;

import com.percussion.dashboardmanagement.dao.IPSDashboardDao;
import com.percussion.dashboardmanagement.data.PSDashboard;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.share.dao.PSGenericItemDao;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.util.PSSiteManageBean;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.content.IPSContentWs;
import org.springframework.beans.factory.annotation.Autowired;

@PSSiteManageBean("dashboardDao")
public class PSDashboardDao extends PSGenericItemDao<PSDashboard> implements IPSDashboardDao
{

    public static final String FOLDER_PATH = "//Folders/$System$/UserProfiles";

    @Autowired
    public PSDashboardDao(IPSContentWs contentWs, IPSContentMgr contentMgr, IPSIdMapper idMapper)
    {
        super(contentWs, contentMgr, idMapper, PSDashboard.class, "percUserProfile", FOLDER_PATH);
    }

    

    @Override
    protected IPSGuid findContentItemGuid(String id)
    {
        return findContentItemGuidWithSearch(id);
    }



    @Override
    protected List<String> getFolderPaths(PSDashboard object)
    {
        return asList(FOLDER_PATH);
    }
    

    

}
