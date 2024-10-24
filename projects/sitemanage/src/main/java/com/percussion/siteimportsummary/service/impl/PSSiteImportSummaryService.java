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

package com.percussion.siteimportsummary.service.impl;

import com.percussion.services.siteimportsummary.IPSSiteImportSummaryDao;
import com.percussion.services.siteimportsummary.data.PSSiteImportSummary;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.sitesummaryservice.service.IPSSiteImportSummaryService;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Component("siteImportSummaryService")
@Transactional()
public class PSSiteImportSummaryService implements IPSSiteImportSummaryService
{
    private IPSSiteImportSummaryDao summaryDao;
    
    @Autowired
    PSSiteImportSummaryService(IPSSiteImportSummaryDao summaryDao)
    {
        this.summaryDao = summaryDao;
    }
    @Override
    public PSSiteImportSummary find(int siteId)
    {
        return summaryDao.findBySiteId(siteId);
    }

    @Override
    public PSSiteImportSummary create(int siteId) throws IPSGenericDao.SaveException {
        PSSiteImportSummary summary = new PSSiteImportSummary();
        summary.setSiteId(siteId);
        summaryDao.save(summary);
        return find(siteId);
    }

    @Override
    public void deleteBySiteId(int siteId)
    {
        PSSiteImportSummary summary = find(siteId);
        if(summary != null) {
            summaryDao.delete(summary);
        }
    }

    @Override
    public PSSiteImportSummary update(int siteId, Map<SiteImportSummaryTypeEnum, Integer> fields) throws IPSGenericDao.SaveException {
        Validate.notNull(fields);
        PSSiteImportSummary summary = find(siteId);
        if(summary == null)
        {
            summary = create(siteId);
        }
        
        if(fields.get(SiteImportSummaryTypeEnum.FILES)!=null)
        {
            int files = summary.getFiles() + fields.get(SiteImportSummaryTypeEnum.FILES);
            summary.setFiles(files);
        }
        if(fields.get(SiteImportSummaryTypeEnum.PAGES)!=null)
        {
            int pages = summary.getPages() + fields.get(SiteImportSummaryTypeEnum.PAGES);
            summary.setPages(pages);
        }
        if(fields.get(SiteImportSummaryTypeEnum.STYLESHEETS)!=null)
        {
            int styleSheets = summary.getStylesheets() + fields.get(SiteImportSummaryTypeEnum.STYLESHEETS);
            summary.setStylesheets(styleSheets);
        }
        if(fields.get(SiteImportSummaryTypeEnum.TEMPLATES)!=null)
        {
            int templates = summary.getTemplates() + fields.get(SiteImportSummaryTypeEnum.TEMPLATES);
            summary.setTemplates(templates);
        }
        if(fields.get(SiteImportSummaryTypeEnum.INTERNALLINKS)!=null)
        {
            int internallinks = summary.getInternallinks() + fields.get(SiteImportSummaryTypeEnum.INTERNALLINKS);
            summary.setInternallinks(internallinks);
        }
        summaryDao.save(summary);
        return summary;
    }

}
