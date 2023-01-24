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
package com.percussion.pagemanagement.extension;

import com.percussion.pagemanagement.dao.IPSPageDaoHelper;
import com.percussion.pagemanagement.dao.IPSTemplateDao;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.ui.data.PSDisplayPropertiesCriteria;
import com.percussion.ui.service.IPSListViewProcessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.percussion.util.PSSiteManageBean;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Fills in template name for all pages in the supplied criteria.
 * 
 * @author JaySeletz
 */
@PSSiteManageBean("pageListViewProcessor")
public class PSPageListViewProcessor implements IPSListViewProcessor
{
    private IPSPageDaoHelper pageDaoHelper;
    private IPSTemplateDao templateDao;
    private IPSIdMapper idMapper;
    private static final Logger log = LogManager.getLogger(PSPageListViewProcessor.class);

    @Autowired
    public PSPageListViewProcessor(IPSPageDaoHelper pageDaoHelper, IPSTemplateDao templateDao, IPSIdMapper idMapper)
    {
        this.pageDaoHelper = pageDaoHelper;
        this.templateDao = templateDao;
        this.idMapper = idMapper;
    }
    
    @Override
    public void process(PSDisplayPropertiesCriteria criteria)
    {
        Validate.notNull(criteria);
        Map<String, PSPathItem> pageMap = getPageMap(criteria.getItems());
        List<Integer> contentIds = getPageIdList(pageMap.keySet());
        Map<String, String> pageToTemplateIdMap = pageDaoHelper.findTemplateUsedByCurrentRevisionOfPages(contentIds);
        Map<String, String> linkTextMap = pageDaoHelper.findLinkTextForCurrentRevisionOfPages(contentIds);
        Map<String, String> templateMap = getTemplateMap(pageToTemplateIdMap.values());
        
        for (Entry<String, PSPathItem> entry : pageMap.entrySet())
        {
            String contentId = entry.getKey();
            PSPathItem item = entry.getValue();
            
            String templateName = null;
            String templateId = pageToTemplateIdMap.get(contentId);
            if (templateId != null) {
                templateName = templateMap.get(templateId);
            }
            
            if (templateName == null) {
                templateName = "";
            }
            
            item.getDisplayProperties().put(TEMPLATE_NAME, templateName);
            
            String linkText = linkTextMap.get(contentId);
            if (StringUtils.isBlank(linkText)) {
                linkText = item.getName();
            }
            
            item.getDisplayProperties().put(LINK_TEXT, linkText);
        }
    }

    /**
     * Get a map of page content id to path item for all of the supplied items that are 
     * pages.
     * 
     * @param items The items, not <code>null</code>.
     * 
     * @return The map, not <code>null</code>.
     */
    private Map<String, PSPathItem> getPageMap(List<PSPathItem> items)
    {
        Map<String, PSPathItem> pageMap = new HashMap<>();
        
        for (PSPathItem item : items)
        {
            if (!item.isPage()) {
                continue;
            }
            
            pageMap.put(String.valueOf(idMapper.getContentId(item.getId())), item);
        }
        
        return pageMap;
    }


    private List<Integer> getPageIdList(Set<String> pageIds)
    {
        List<Integer> contentIds = new ArrayList<>();
        for (String id : pageIds)
        {
            contentIds.add(Integer.parseInt(id));
        }
        return contentIds;
    }

    
    /**
     * Get a map of template id to name
     * 
     * @param templateIds The ids to check
     * 
     * @return The map, will only contain entries of the ids for which a template could be found.
     */
    private Map<String, String> getTemplateMap(Collection<String> templateIds)
    {
        Map<String, String> templateMap = new HashMap<>();
        for (String templateId : templateIds)
        {
            String templateName = templateMap.get(templateId);
            if (templateName == null)
            {PSTemplate template = null;
                try {
                    template = templateDao.find(templateId);
                } catch (PSDataServiceException e) {
                    log.warn("Template {} not found.",templateName);
                }
                if (template != null) {
                    templateMap.put(templateId, template.getName());
                }
            }
        }
        return templateMap;
    }

}
