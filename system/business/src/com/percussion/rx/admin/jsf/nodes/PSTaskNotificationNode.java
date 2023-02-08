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
package com.percussion.rx.admin.jsf.nodes;

import static com.percussion.utils.string.PSStringUtils.notBlank;
import static org.apache.commons.lang.Validate.notNull;

import com.percussion.rx.jsf.PSEditableNode;
import com.percussion.services.schedule.IPSSchedulingService;
import com.percussion.services.schedule.PSSchedulingServiceLocator;
import com.percussion.services.schedule.data.PSNotificationTemplate;

/**
 * The backing bean for a task notification template.
 * 
 * @author Andriy Palamarchuk
 */
public class PSTaskNotificationNode extends PSEditableNode
{
   /**
    * Creates new node for the specified notification template.
    * @param notification the notification template.
    * Not <code>null</code>.
    */
   public PSTaskNotificationNode(
         PSNotificationTemplate notification)
   {
      super(getNotificationLabel(notification), notification.getId());
      m_notification = notification;
   }

   @Override
   public String copy()
   {
      // not implemented
      return null;
   }

   @Override
   public String delete()
   {
      getSchedulingService().deleteNotificationTemplate(
            getNotification().getId());
      remove();
      return navigateToList();
   }

   /**
    * Saves the changes to the node.
    * @return the "save" string. Never <code>null</code>.
    */
   public String save()
   {
      getSchedulingService().saveNotificationTemplate(getNotification());
      setTitle(getNotification().getName());
      
      return gotoParentNode();
   }

   @Override
   public String cancel() 
   {
      m_notification =
         getSchedulingService().findNotificationTemplateById(
               m_notification.getId());

      return gotoParentNode();
   }

   @Override
   public String navigateToList()
   {
      return getCategoryKey();
   }

   @Override
   protected String getCategoryKey()
   {
      return "admin-timed-event-notifications";
   }

   @Override
   protected String getOutcomePrefix()
   {
      return "admin-";
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
    * Retrieves notification template label.
    * For use in the constructor, so the constructor will be able to validate
    * the notification template variable before the super constructor is called.
    * @param notificationTemplate the notification template
    * to request the label from.
    * If <code>null</code> the method throws
    * <code>IllegalArgumentException</code>.
    * @return the notification template label. Never <code>null</code> or blank.
    */
   private static String getNotificationLabel(
         PSNotificationTemplate notificationTemplate)
   {
      notNull(notificationTemplate);
      return notificationTemplate.getName();
   }

   /**
    * The notification this field presents.
    * @return the notification template. Never <code>null</code>.
    */
   public PSNotificationTemplate getNotification()
   {
      return m_notification;
   }
   
   /**
    * The notification template name.
    * @return the notification template name. Not <code>null</code> or empty.
    */
   public String getName()
   {
      return getNotification().getName();
   }
   
   /**
    * @param name the new name. Not <code>null</code> or blank.
    * @see #getName()
    */
   public void setName(String name)
   {
      notBlank(name);
      getNotification().setName(name);
   }
   
   /**
    * The notification template.
    * @return the notification template. Can be <code>null</code> or empty.
    */
   public String getTemplate()
   {
      return getNotification().getTemplate();
   }
   
   /**
    * @param template the new template value. Can be null or empty. 
    * @see #getTemplate()
    */
   public void setTemplate(String template)
   {
      getNotification().setTemplate(template);
   }

   /**
    * @return the notification subject, maybe <code>null</code> or empty.
    */
   public String getSubject()
   {
      return getNotification().getSubject();
   }
   
   /**
    * @param sub the new subject, never <code>null</code> or empty. 
    * @see #getSubject()
    */
   public void setSubject(String sub)
   {
      getNotification().setSubject(sub);
   }

   
   @Override
   public String getHelpTopic()
   {
      return "TaskNotificationEditor";
   }
   
   /**
    * @see #getNotification()
    */
   private PSNotificationTemplate m_notification;
}
