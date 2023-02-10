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
