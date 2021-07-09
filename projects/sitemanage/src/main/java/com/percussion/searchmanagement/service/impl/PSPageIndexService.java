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

package com.percussion.searchmanagement.service.impl;

import static org.apache.commons.lang.Validate.notNull;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.itemmanagement.service.IPSWorkflowHelper;
import com.percussion.pagemanagement.dao.IPSPageDao;
import com.percussion.pagemanagement.dao.IPSPageDaoHelper;
import com.percussion.searchmanagement.service.IPSPageIndexService;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.util.PSSiteManageBean;
import com.percussion.utils.guid.IPSGuid;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

@PSSiteManageBean("pageIndexService")
public class PSPageIndexService implements IPSPageIndexService
{
    
    private static final Logger log = LogManager.getLogger(PSPageIndexService.class.getName());
    
    IPSIdMapper idMapper;

    IPSPageDao pageDao;

    IPSWorkflowHelper workflowHelper;

    IPSPageDaoHelper pageDaoHelper;

    PSIndexHelper indexHelper;

    @Autowired
    public PSPageIndexService(IPSIdMapper idMapper, IPSPageDao pageDao, IPSWorkflowHelper workflowHelper,
            IPSPageDaoHelper pageDaoHelper, PSIndexHelper indexHelper)
    {
        this.indexHelper = indexHelper;
        this.idMapper = idMapper;
        this.pageDao = pageDao;
        this.workflowHelper = workflowHelper;
        this.pageDaoHelper = pageDaoHelper;
    }

    public void index(Set<Integer> ids) throws PSValidationException {
        notNull(ids);

        Set<PSLocator> locators = new HashSet<>();

        for (Integer id : ids)
        {
            IPSGuid guid = PSGuidUtils.makeGuid(id, PSTypeEnum.LEGACY_CONTENT);
            String guidStr = idMapper.getString(guid);
            try
            {
                if (workflowHelper.isPage(guidStr))
                {
                    locators.add(idMapper.getLocator(guidStr));
                }
            }
            catch (PSNotFoundException e)
            {
                log.error("Error indexing page with id: " + id, e);
            }
            
            if (workflowHelper.isTemplate(guidStr))
            {
                Collection<Integer> pageIds = pageDaoHelper.findPageIdsByTemplate(guidStr);
                for (Integer pageId : pageIds)
                {
                    locators.add(new PSLocator(pageId.intValue()));
                }
            }
        }

        indexHelper.addItemsForIndex(locators);
    }

}
