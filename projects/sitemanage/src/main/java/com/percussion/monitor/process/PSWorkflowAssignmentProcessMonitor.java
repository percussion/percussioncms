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
package com.percussion.monitor.process;

import com.percussion.monitor.service.IPSMonitor;
import com.percussion.monitor.service.PSMonitorService;
import com.percussion.services.notification.IPSNotificationListener;
import com.percussion.services.notification.IPSNotificationService;
import com.percussion.services.notification.PSNotificationEvent;
import com.percussion.services.notification.PSNotificationEvent.EventType;
import com.percussion.services.notification.PSNotificationServiceLocator;

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
        
        StringBuilder buf;
        
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

    private static StringBuilder getFolderStatusMessage(int folderCount)
    {
        StringBuilder buf = new StringBuilder();
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

    private static StringBuilder getItemStatusMessage(int itemCount)
    {
        StringBuilder buf = new StringBuilder();

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
