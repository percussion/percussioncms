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