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

import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notNull;

import com.percussion.assetmanagement.service.impl.PSWidgetAssetRelationshipService;
import com.percussion.cms.PSRelationshipChangeEvent;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.searchmanagement.service.IPSPageIndexService;
import com.percussion.services.notification.IPSNotificationListener;
import com.percussion.services.notification.IPSNotificationService;
import com.percussion.services.notification.PSNotificationEvent;
import com.percussion.services.notification.PSNotificationEvent.EventType;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.util.PSSiteManageBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * This class is notified on relationship changes.  It is used to re-index pages which are affected by the deletion of
 * shared assets.
 * 
 * @author peterfrontiero
 */

@PSSiteManageBean("sharedRelationshipDeleteListener")
public class PSSharedRelationshipDeleteListener implements IPSNotificationListener
{
    IPSPageIndexService indexService;

    @Autowired
    public PSSharedRelationshipDeleteListener(IPSNotificationService notificationService, 
            IPSPageIndexService indexService)
    {
        if (notificationService != null)
        {
            notificationService.addListener(EventType.RELATIONSHIP_CHANGED, this);
        }
        
        this.indexService = indexService;
    }

    public void notifyEvent(PSNotificationEvent event) throws PSValidationException {
        notNull(event, "event");
        isTrue(EventType.RELATIONSHIP_CHANGED == event.getType(), 
                "Should only be registered for relationship changes.");

        // filter out all relationship changes except delete
        PSRelationshipChangeEvent relEvent = (PSRelationshipChangeEvent) event.getTarget();
        if (relEvent.getAction() != PSRelationshipChangeEvent.ACTION_REMOVE)
        {
            return;
        }
        
        // filter out all relationships except shared
        Set<Integer> sharedOwnerIds = new HashSet<>();
        
        Iterator iter = relEvent.getRelationships().iterator();
        while (iter.hasNext())
        {
            PSRelationship rel = (PSRelationship) iter.next();
            if (rel.getConfig().getName().equals(PSWidgetAssetRelationshipService.SHARED_ASSET_WIDGET_REL_TYPE))
            {
                sharedOwnerIds.add(rel.getOwner().getId());
            }
        }
        
        if (!sharedOwnerIds.isEmpty())
        {
            // index the shared asset owners
            indexService.index(sharedOwnerIds);
        }
    }
    
}
