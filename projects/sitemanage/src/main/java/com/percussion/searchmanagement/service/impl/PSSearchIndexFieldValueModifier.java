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
package com.percussion.searchmanagement.service.impl;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.search.IPSFieldValueModifier;
import com.percussion.search.PSSearchIndexEventQueue;
import com.percussion.services.notification.IPSNotificationListener;
import com.percussion.services.notification.IPSNotificationService;
import com.percussion.services.notification.PSNotificationEvent;
import com.percussion.services.notification.PSNotificationEvent.EventType;
import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.dao.PSDateUtils;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.util.PSDataTypeConverter;
import com.percussion.utils.types.PSPair;
import com.percussion.webservices.PSWebserviceUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Modifies field values before items are indexed, to ensure search queries behave as expected.  Currently
 * changes the last modified date/user to match the last user's changes if the item was actually last 
 * modified by the system during a transition from Pending to Live
 * 
 * @author JaySeletz
 *
 */
@Component("searchIndexFieldValueModifier")
public class PSSearchIndexFieldValueModifier implements IPSFieldValueModifier, IPSNotificationListener
{
    private IPSFolderHelper folderHelper;
    private IPSIdMapper idMapper;
    
    
    private static final Logger log = LogManager.getLogger(PSSearchIndexFieldValueModifier.class);
    
    @Autowired
    public PSSearchIndexFieldValueModifier(IPSFolderHelper folderHelper, IPSIdMapper idMapper, IPSNotificationService notificationService)
    {
        this.folderHelper = folderHelper;
        this.idMapper = idMapper;
        notificationService.addListener(EventType.CORE_SERVER_INITIALIZED, this);
    }


    @Override
    public void modifyFields(Map<String, Object> itemFragment)
    {
        try
        {
            // get item id
            String strContentId = (String) itemFragment.get("sys_contentid");
            int contentId = NumberUtils.toInt( strContentId);
            if (contentId <= 0)
                throw new IllegalArgumentException("Invalid or missing content id: " + strContentId);
            
            // get wf and state id, see if publishable
            int wfId = NumberUtils.toInt( (String) itemFragment.get("sys_workflowid"));
            String stateName = (String) itemFragment.get("sys_statename");
            
            
            if (wfId <= 0 || StringUtils.isBlank(stateName))
                return;
            
            String userName = (String) itemFragment.get("sys_contentlastmodifier");
            String strDate = (String) itemFragment.get("sys_contentlastmodifieddate");
            
            if (StringUtils.isBlank(userName) || StringUtils.isBlank(strDate))
                return;
            
            StringBuilder patternUsed = new StringBuilder();
            Date lastModified = PSDataTypeConverter.parseStringToDate(strDate, patternUsed);
            if (lastModified == null)
                return;
            
            PSWorkflow wf = PSWebserviceUtils.getWorkflow(wfId);
            PSState state = null;
            List<PSState> states = wf.getStates();
            for (PSState test : states)
            {
                if (test.getName().equals(stateName))
                    state = test;
            }
            
            if (state == null)
            {
                return;
            }
            
            PSPair<String, String> lastModInfo = folderHelper.fixupLastModified(idMapper.getGuid(new PSLocator(contentId)), userName, lastModified, state.isPublishable());
            itemFragment.put("sys_contentlastmodifier", lastModInfo.getFirst());
            itemFragment.put("sys_contentlastmodifieddate", PSDataTypeConverter.transformDateString(PSDateUtils.getDateFromString(lastModInfo.getSecond()), null, patternUsed.toString(), true));
        }
        catch (Exception e)
        {
            log.error("Failed to update last modifier fields for search indexing", e);
        }
        
    }


    @Override
    public void notifyEvent(PSNotificationEvent notification)
    {
        PSSearchIndexEventQueue.getInstance().setFieldValueModifier(this);
    }

}
