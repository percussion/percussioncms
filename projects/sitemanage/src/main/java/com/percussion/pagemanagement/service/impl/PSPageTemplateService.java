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
package com.percussion.pagemanagement.service.impl;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.Validate.isTrue;

import com.percussion.pagemanagement.dao.IPSPageDao;
import com.percussion.pagemanagement.dao.IPSTemplateDao;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.IPSPageTemplateService;

import java.util.List;

import com.percussion.share.service.exception.PSDataServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * @author JaySeletz
 *
 */
@Component("pageTemplateService")
@Lazy
public class PSPageTemplateService implements IPSPageTemplateService
{
    private IPSPageDao pageDao;
    private IPSTemplateDao templateDao;
    
    @Autowired
    public PSPageTemplateService(IPSPageDao pageDao, IPSTemplateDao templateDao)
    {
        this.pageDao = pageDao;
        this.templateDao = templateDao;
    }
    
    @Override
    public void changeTemplate(String pageId, String templateId) throws PSDataServiceException {
        isTrue(isNotBlank(pageId), "pageId may not be blank");
        isTrue(isNotBlank(templateId), "templateId may not be blank");
        
        PSPage page = pageDao.find(pageId);
        PSTemplate template = templateDao.find(templateId);

        if(page == null)
        {
            throw new PSDataServiceException("The page you have selected doesn't exist in the system. Please refresh and try again.");
        }
        
        if(template == null)
        {
            throw new PSDataServiceException("The template you have selected doesn't exist in the system. Please refresh and try again.");
        }
        page.setTemplateId(templateId);
        page.setTemplateContentMigrationVersion(template.getContentMigrationVersion());
        pageDao.save(page);
    }

    @Override
    public List<Integer> findPageIdsByTemplate(String templateId) throws IPSPageService.PSPageException {
        List<Integer> pageIds = pageDao.getPageIdsByFieldNameAndValue(FIELD_NAME_TEMPLATE_ID, templateId);
        return pageIds;
    }

}
