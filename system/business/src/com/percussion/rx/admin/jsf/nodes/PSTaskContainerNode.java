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

import static org.apache.commons.lang.Validate.notNull;

import com.percussion.rx.jsf.PSEditableNodeContainer;
import com.percussion.rx.jsf.PSNodeBase;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.schedule.IPSSchedulingService;
import com.percussion.services.schedule.PSSchedulingException;
import com.percussion.services.schedule.PSSchedulingServiceLocator;
import com.percussion.services.schedule.data.PSScheduledTask;
import com.percussion.services.schedule.data.PSScheduledTask.ByLabelComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The backing beans for the container of scheduled tasks.
 * 
 * @author Andriy Palamarchuk
 */
public class PSTaskContainerNode extends PSEditableNodeContainer
{
   /**
    * The logger for the site container node.
    */
   private static final Logger ms_log =
         LogManager.getLogger(PSTaskContainerNode.class);

   /**
    * Constructor.
    */
   public PSTaskContainerNode()
   {
      super("The scheduled tasks.",
            ADMIN_NOTIFICATION_VIEWS, "Scheduled Tasks");
   }

   // see base
   @Override
   protected boolean findObjectByName(String name)
   {
      final IPSSchedulingService service = getSchedulingService();
      try
      {
         return service.findScheduleByName(name) != null;
      }
      catch (PSSchedulingException e)
      {
         ms_log.error("Failure to find a schedule by name", e);
         return false;
      }
   }
   
   /**
    * Action to create a new site, and add it to the tree.
    * @return the perform action for the site node, which will navigate to the
    * editor.
    */
   public String createEvent() throws PSNotFoundException {
      final PSScheduledTask event = getSchedulingService().createSchedule();
      event.setName(getUniqueName("TimedEvent", false));
      return initNewEvent(event);
   }

   /**
    * Initializes a newly created timed event.
    * 
    * @param event the event, not <code>null</code>.
    * @return the outcome, never <code>null</code> or empty.
    */
   String initNewEvent(PSScheduledTask event)
   {
      notNull(event);
      if (StringUtils.isBlank(event.getCronSpecification()))
            event.setCronSpecification("0 0 0 1 1 ? 2050");
      try
      {
         getSchedulingService().saveSchedule(event);
      }
      catch (PSSchedulingException e)
      {
         ms_log.error("Failed to update a timed event", e);
         return "ADMIN_NOTIFICATION_VIEWS";
      }
      
      final PSTaskNode node = new PSTaskNode(event);
      addNode(node);
      return node.perform();
   }

   @Override
   public List<? extends PSNodeBase> getChildren() throws PSNotFoundException {
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
      List<PSScheduledTask> notifications;
      try
      {
         notifications = new ArrayList<>(
               getSchedulingService().findAllSchedules());
      }
      catch (PSSchedulingException e)
      {
         ms_log.error("Failure to load all the timed events", e);
         throw new RuntimeException(e);
      }
      Collections.sort(notifications, new ByLabelComparator());
      for (PSScheduledTask notification : notifications)
      {
         addNode(new PSTaskNode(notification));
      }
   }

   @Override
   public String returnToListView()
   {
      return "return-to-events";
   }

   @Override
   public String getHelpTopic()
   {
      return "ScheduledTaskList";
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
    * Outcome for the events view page.
    */
   public static final String ADMIN_NOTIFICATION_VIEWS = "admin-timed-events";
}
