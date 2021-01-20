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
package com.percussion.rx.admin.jsf.nodes;

import com.percussion.rx.jsf.PSEditableNodeContainer;
import com.percussion.rx.jsf.PSNodeBase;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.schedule.IPSSchedulingService;
import com.percussion.services.schedule.PSSchedulingServiceLocator;
import com.percussion.services.schedule.data.PSNotificationTemplate;
import com.percussion.services.schedule.data.PSNotificationTemplate.ByLabelComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The container of task notification templates.
 * 
 * @author Andriy Palamarchuk
 */
public class PSTaskNotificationContainerNode
      extends PSEditableNodeContainer
{
   /**
    * Creates new node.
    */
   public PSTaskNotificationContainerNode()
   {
      super("The templates of the scheduled events messages.",
            ADMIN_NOTIFICATION_VIEWS, "Task Notifications");
   }

   /**
    * Action to create a new event notification, and add it to the tree.
    * @return the perform action for the event notification node,
    * which will navigate to the editor.
    */
   public String createNotification()
   {
      final PSNotificationTemplate notification =
            getSchedulingService().createNotificationTemplate();
      return initNewNotification(notification);
   }
   
   /**
    * Initializes a newly created notification.
    * 
    * @param notification the notification, assumed never <code>null</code>.
    * @return the outcome, never <code>null</code> or empty.
    */
   private String initNewNotification(PSNotificationTemplate notification)
   {
      notification.setName(getUniqueName("Notification", false));
      getSchedulingService().saveNotificationTemplate(notification);
      
      final PSTaskNotificationNode node =
            new PSTaskNotificationNode(notification);
      addNode(node);
      return node.perform();
   }

   @Override
   public List<? extends PSNodeBase> getChildren()
   {
      if (m_children == null)
      {
         initChildrenNodes();
      }
      return super.getChildren();
   }

   /**
    * Initializes the children nodes.
    */
   private void initChildrenNodes()
   {
      final List<PSNotificationTemplate> notifications =
            new ArrayList<PSNotificationTemplate>(
                  getSchedulingService().findAllNotificationTemplates());
      Collections.sort(notifications, new ByLabelComparator());
      for (PSNotificationTemplate notification : notifications)
      {
         addNode(new PSTaskNotificationNode(notification));
      }
   }

   /**
    * Provides current scheduling service.
    * @return the scheduling service. Not <code>null</code>.
    */
   private IPSSchedulingService getSchedulingService()
   {
      return PSSchedulingServiceLocator.getSchedulingService();
   }

   /**
    * Get the type of the currently selected node. Used for the removal 
    * confirmation page to display information about the currently selected
    * object being removed.
    * 
    * @return the type, or the string "unknown". Never <code>null</code>.
    */
   @Override
   public String getSelectedType()
   {
      return PSTypeEnum.SCHEDULE_NOTIFICATION_TEMPLATE.getDisplayName();
   }

   @Override
   public Set<Object> getAllNames()
   {
      final Set<Object> names = new HashSet<Object>();
      for (final PSNotificationTemplate notificationTemplate
            : getSchedulingService().findAllNotificationTemplates())
      {
         names.add(notificationTemplate.getName());
      }
      return names;
   }

   @Override
   public String returnToListView()
   {
      return "return-to-events-notifications";
   }

   
   @Override
   public String getHelpTopic()
   {
      return "TaskNotificationList";
   }

   // see base
   @Override
   protected boolean findObjectByName(String name)
   {
      final IPSSchedulingService service = getSchedulingService();
      return service.findNotificationTemplateByName(name) != null;
   }

   /**
    * Outcome for the notifications page.
    */
   public static final String ADMIN_NOTIFICATION_VIEWS =
         "admin-timed-event-notifications";
}
