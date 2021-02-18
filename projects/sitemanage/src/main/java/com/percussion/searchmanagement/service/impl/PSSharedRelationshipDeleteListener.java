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

    @SuppressWarnings("unchecked")
    public void notifyEvent(PSNotificationEvent event)
    {
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
