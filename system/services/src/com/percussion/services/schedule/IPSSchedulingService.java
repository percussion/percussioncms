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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.services.schedule;

import com.percussion.services.schedule.data.PSNotificationTemplate;
import com.percussion.services.schedule.data.PSScheduledTask;
import com.percussion.services.schedule.data.PSScheduledTaskLog;
import com.percussion.utils.guid.IPSGuid;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * The scheduling service manages the task schedules.
 * Internally schedules are passed to the Quartz scheduler.
 * Quartz is responsible for storing and managing the schedules.
 * 
 * @author Doug Rand
 * @author Andriy Palamarchuk
 */
public interface IPSSchedulingService
{
   /**
    * Creates a schedule. The created schedule is not saved yet.
    * It will have a fresh GUID assigned.
    *
    * @return the new schedule, never <code>null</code>.
    */
   PSScheduledTask createSchedule();

   /**
    * Loads all known schedules.
    * 
    * @return the collection, may be empty if there are no tasks scheduled 
    * in the system, but never <code>null</code>.
    * @throws PSSchedulingException on schedule loading error.
    */
   Collection<PSScheduledTask> findAllSchedules() throws PSSchedulingException;
   
   /**
    * Loads a schedule by name.
    * 
    * @param label the label of the schedule, never <code>null</code> or empty.
    * @return the schedule, or <code>null</code> if it does not exist.
    * @throws PSSchedulingException on schedule loading error.
    */
   PSScheduledTask findScheduleByName(String label) throws PSSchedulingException;

   /**
    * Finds the scheduled task with the specified id.
    * 
    * @param id the scheduled task ID, never <code>null</code>.
    * 
    * @return the schedule, or <code>null</code> if cannot find the task with
    *    the given ID.
    *    
    * @throws PSSchedulingException on schedule loading error.
    */
   PSScheduledTask findScheduledTaskById(IPSGuid id) throws PSSchedulingException;

   /**
    * Updates the schedule. All consequent calls will be rescheduled. 
    * 
    * @param schedule the task schedule, never <code>null</code>.
    * @throws PSSchedulingException on update error.
    */
   void saveSchedule(PSScheduledTask schedule) throws PSSchedulingException;

   /**
    * Executes the given task now.
    * 
    * @param schedule the to be executed task, never <code>null</code>.
    */
   public void runNow(PSScheduledTask schedule);

   /**
    * Removes the schedule from the system and cancel all the scheduled calls.
    * Does nothing if the schedule with the specified id does not exist.
    * 
    * @param scheduleId the ID of the schedule to delete, never 
    *    <code>null</code>.
    *    
    * @throws PSSchedulingException on deletion failure.
    */
   void deleteSchedule(IPSGuid scheduleId) throws PSSchedulingException;

   /**
    * Creates a schedule notification template.
    * The created template is not saved yet.
    * It will have a fresh GUID assigned.
    *
    * @return the new template, never <code>null</code>.
    */
   PSNotificationTemplate createNotificationTemplate();
   
   /**
    * Loads all known schedule notification templates.
    * 
    * @return the collection, may be empty if there are
    * no notification templates defined in the system,
    * but never <code>null</code>.
    */
   Collection<PSNotificationTemplate> findAllNotificationTemplates();
   
   /**
    * Provides set with the notifications labels.
    * @return the notification labels set. Never <code>null</code>.
    */
   Set<String> findAllNotificationTemplatesNames();

   /**
    * Loads the schedule notification template with the specified id.
    * 
    * @param id the schedule notification template id,
    * never <code>null</code>.
    * @return the schedule notification template,
    * or <code>null</code> if none is found for the given id.
    */
   PSNotificationTemplate findNotificationTemplateById(IPSGuid id);

   /**
    * Loads a schedule notification template by name from the database.
    * 
    * @param name the name of the notification template,
    * never <code>null</code> or empty.
    * @return the notification template,
    * or <code>null</code> if it does not exist.
    */
   PSNotificationTemplate findNotificationTemplateByName(String name);

   /**
    * Updates the schedule notification template.
    * 
    * @param notificationTemplate the notification template,
    * never <code>null</code>.
    */
   void saveNotificationTemplate(PSNotificationTemplate notificationTemplate);

   /**
    * Removes the schedule notification template from the system.
    * Fails if there are schedules using this notification template.
    *
    * @param template the notification template id, never <code>null</code>.
    */
   void deleteNotificationTemplate(IPSGuid templateId);
   
   /**
    * @return a new GUID of the task log. This is used to create a new 
    * entry for the task log repository.
    */
   IPSGuid createTaskLogId();
   
   /**
    * Loads a specified task log.
    * 
    * @param id the ID of the task log, never <code>null</code>.
    * 
    * @return the specified event log. It may be <code>null</code> if cannot
    *    find the specified event log. 
    */
   PSScheduledTaskLog findTaskLogById(IPSGuid id);

   /**
    * Saves the supplied task log.
    * @param eventLog the to be saved task log, never <code>null</code>.
    */
   void saveTaskLog(PSScheduledTaskLog eventLog);

   /**
    * Deletes a specified task log
    * @param id the ID of the deleted task log, never <code>null</code>.
    */
   void deleteTaskLog(IPSGuid id);
   
   /**
    * Deletes multiple of task log entries
    * @param ids the IDs of the to be deleted task log entries, 
    *    never <code>null</code>, may be empty.
    */
   void deleteTaskLogs(Collection<IPSGuid> ids);
   
   /**
    * Gets all task log entries. Note, the problemDesc property will not be
    * loaded, e.i, {@link PSScheduledTaskLog#getProblemDesc()} will return
    * <code>null</code> for all returned log entries.
    * 
    * @param maxResult the max number of log entries to be retrieved. The limit
    *    will not be set if it is less than or equal to <code>0</code>.
    *     
    * @return all log entries, which is sorted by the end time in descending 
    *    order. It never <code>null</code>, but may be empty.
    */
   List<PSScheduledTaskLog> findAllTaskLogs(int maxResult);

   /**
    * Remove all task log entries.
    */
   void deleteAllTaskLogs();
   
   /**
    * Delete all task log entries for which 
    * {@link PSScheduledTaskLog#getEndTime()} gives
    * a date older than the date supplied to this method.
    * 
    * @param beforeDate The date before which log entries should be deleted,
    * may not be <code>null</code>.
    */
   public void deleteTaskLogsByDate(Date beforeDate);

}
