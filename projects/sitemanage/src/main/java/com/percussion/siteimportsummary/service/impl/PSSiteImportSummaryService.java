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

package com.percussion.siteimportsummary.service.impl;

import com.percussion.services.siteimportsummary.IPSSiteImportSummaryDao;
import com.percussion.services.siteimportsummary.data.PSSiteImportSummary;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.sitesummaryservice.service.IPSSiteImportSummaryService;

import java.util.Map;

import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("siteImportSummaryService")
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
