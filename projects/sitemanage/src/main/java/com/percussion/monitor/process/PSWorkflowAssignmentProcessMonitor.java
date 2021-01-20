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
package com.percussion.monitor.process;

import com.percussion.monitor.service.IPSMonitor;
import com.percussion.monitor.service.PSMonitorService;
import com.percussion.services.notification.IPSNotificationListener;
import com.percussion.services.notification.IPSNotificationService;
import com.percussion.services.notification.PSNotificationEvent;
import com.percussion.services.notification.PSNotificationServiceLocator;
import com.percussion.services.notification.PSNotificationEvent.EventType;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Monitor the number of folders/items undergoing workflow reassignment
 * @author JaySeletz
 *
 */
public class PSWorkflowAssignmentProcessMonitor implements IPSNotificationListener
{
    private static final String ITEM_STATUS_MSG_SOME = " items queued for workflow assignment";
    private static final String ITEM_STATUS_MSG_ONE = " item queued for workflow assignment";
    private static final String ITEM_STATUS_MSG_NONE = "No items queued for workflow assignment";

    private static final String FOLDER_STATUS_MSG_SOME = " folders submitted for workflow assignment";
    private static final String FOLDER_STATUS_MSG_ONE = " folder submitted for workflow assignment";

    
    private static IPSMonitor monitor = null;
    private static AtomicInteger curItemCount = new AtomicInteger(0);
    private static AtomicInteger curFolderCount = new AtomicInteger(0);
    
    public PSWorkflowAssignmentProcessMonitor()
    {
        monitor = PSMonitorService.registerMonitor("WorkflowAssignment", "Workflow assignment");
        IPSNotificationService notificationService = PSNotificationServiceLocator.getNotificationService();
        notificationService.addListener(EventType.WORKFLOW_FOLDER_ASSIGNMENT_QUEUEING, this);
        notificationService.addListener(EventType.WORKFLOW_FOLDER_ASSIGNMENT_PROCESSING, this);
        updateStatusMessage();
    }

    private static void updateStatusMessage()
    {
        int folderCount = curFolderCount.get();
        int itemCount = curItemCount.get();
        
        StringBuffer buf;
        
        if (itemCount > 0 || folderCount <= 0)
        {
            buf = getItemStatusMessage(itemCount);
        }
        else
        {
            buf = getFolderStatusMessage(folderCount);
        }
        
        monitor.setMessage(buf.toString());
    }

    private static StringBuffer getFolderStatusMessage(int folderCount)
    {
        StringBuffer buf = new StringBuffer();
        buf.append(folderCount);
        if (folderCount == 1)
        {
            buf.append(FOLDER_STATUS_MSG_ONE);
        }
        else
        {
            
            buf.append(FOLDER_STATUS_MSG_SOME);
        }
        return buf;
    }

    private static StringBuffer getItemStatusMessage(int itemCount)
    {
        StringBuffer buf = new StringBuffer();

        if (itemCount == 0)
        {
            buf.append(ITEM_STATUS_MSG_NONE);
        }
        else
        {
            buf.append(itemCount);
            buf.append(itemCount == 1 ? ITEM_STATUS_MSG_ONE : ITEM_STATUS_MSG_SOME);
        }
        return buf;
    }

    @Override
    public void notifyEvent(PSNotificationEvent notification)
    {
        if (EventType.WORKFLOW_FOLDER_ASSIGNMENT_PROCESSING.equals(notification.getType()))
        {
            updateItemCount(notification.getTarget());
        }
        else if (EventType.WORKFLOW_FOLDER_ASSIGNMENT_QUEUEING.equals(notification.getType()))
        {
            updateFolderCount(notification.getTarget());
        }
    }

    private void updateFolderCount(Object target)
    {
        if (target instanceof Integer)
        {
            int count = (Integer)target;
            curFolderCount.addAndGet(count);
            updateStatusMessage();
        }
    }
    
    private void updateItemCount(Object target)
    {
        if (target instanceof Integer)
        {
            int count = (Integer)target;
            curItemCount.addAndGet(count);
            updateStatusMessage();
        }
    }
}
