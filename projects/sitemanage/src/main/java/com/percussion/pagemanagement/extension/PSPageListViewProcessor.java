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
package com.percussion.pagemanagement.extension;

import com.percussion.pagemanagement.dao.IPSPageDaoHelper;
import com.percussion.pagemanagement.dao.IPSTemplateDao;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.share.service.IPSIdMapper;
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
            if (templateId != null)
                 templateName = templateMap.get(templateId);
            
            if (templateName == null)
                templateName = "";
            
            item.getDisplayProperties().put(TEMPLATE_NAME, templateName);
            
            String linkText = linkTextMap.get(contentId);
            if (StringUtils.isBlank(linkText))
                linkText = item.getName();
            
            item.getDisplayProperties().put(LINK_TEXT, linkText);
        }
    }

    /**
     * Get a map of page content id to path item for all of the supplied items that are 
     * pages.
     * 
     * @param criteria The items, not <code>null</code>.
     * 
     * @return The map, not <code>null</code>.
     */
    private Map<String, PSPathItem> getPageMap(List<PSPathItem> items)
    {
        Map<String, PSPathItem> pageMap = new HashMap<String, PSPathItem>();
        
        for (PSPathItem item : items)
        {
            if (!item.isPage())
                continue;
            
            pageMap.put(String.valueOf(idMapper.getContentId(item.getId())), item);
        }
        
        return pageMap;
    }


    private List<Integer> getPageIdList(Set<String> pageIds)
    {
        List<Integer> contentIds = new ArrayList<Integer>();
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
        Map<String, String> templateMap = new HashMap<String, String>();
        for (String templateId : templateIds)
        {
            String templateName = templateMap.get(templateId);
            if (templateName == null)
            {
                PSTemplate template = templateDao.find(templateId);
                if (template != null)
                    templateMap.put(templateId, template.getName());
            }
        }
        return templateMap;
    }

}
