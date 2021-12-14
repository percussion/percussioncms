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
package com.percussion.rx.admin.jsf.nodes;

import com.percussion.rx.jsf.PSLockableNode;
import com.percussion.rx.jsf.PSNodeBase;
import com.percussion.rx.publisher.jsf.nodes.PSPublishingStatusHelper;
import com.percussion.rx.ui.jsf.beans.PSHelpTopicMapping;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.schedule.IPSSchedulingService;
import com.percussion.services.schedule.PSSchedulingServiceLocator;
import com.percussion.services.schedule.data.PSScheduledTaskLog;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PSTaskLogNode extends PSLockableNode
{
   /**
    * Constructor allowing to specify title, outcome and label.
    *
    * @param title never <code>null</code> or empty.
    * @param outcome the outcome, may be <code>null</code>.
    * @param label the value returned by {@link #getLabel()}.
    *    Can be <code>null</code> or blank.
    *    
    * @see PSNodeBase#PSNodeBase(String, String, String) 
    */
   public PSTaskLogNode(String title, String outcome, String label) 
   {
      super(title, outcome, label);
   }
   
   /**
    * The backing been for each row in event log table. This represent a
    * result of a task execution.
    */
   public class EventLog
   {
      /**
       * The task log, never <code>null</code> after constructor.
       */
      private PSScheduledTaskLog mi_log;
      
      /**
       * The task name, never <code>null</code> or empty after constructor.
       */
      private String mi_taskName;
      
      /**
       * The selected status
       */
      private boolean mi_selected;
      
      /**
       * Constructs an event log.
       * 
       * @param log the task log, never <code>null</code>.
       * @param taskName the task name, not <code>null</code> or empty.
       */
      public EventLog(PSScheduledTaskLog log, String taskName)
      {
         if (log == null)
            throw new IllegalArgumentException("log may not be null.");
         if (StringUtils.isBlank(taskName))
            throw new IllegalArgumentException(
                  "taskName may not be null or empty..");

         mi_log = log;
         mi_taskName = taskName;
         mi_selected = false;
      }
      
      /**
       * Display this log entry in the detail log page.
       * @return the outcome of the page.
       */
      public String showDetail()
      {
         m_detailLog = this;
         PSScheduledTaskLog detailLog = getService().findTaskLogById(mi_log.getId());
         if (detailLog != null)
            mi_log = detailLog;
         
         return "admin-scheduled-task-detail-log";
      }
      
      /**
       * It is the same as {@link PSScheduledTaskLog#getId()}
       */
      public IPSGuid getId()
      {
         return mi_log.getId();
      }
      
      /**
       * Get the name of the execution task
       * @return the task name, never <code>null</code> or empty.
       */
      public String getTaskName()
      {
         return mi_taskName;
      }
      
      /**
       * It is the same as {@link PSScheduledTaskLog#getStartTime()}
       */
      public String getStartTime()
      {
         return PSPublishingStatusHelper.getDatetime(mi_log.getStartTime());
      }
      
      /**
       * It is the same as {@link PSScheduledTaskLog#getElapsed()}
       */
      public String getElapsed()
      {
         return PSPublishingStatusHelper.convertMilliSecondToHhMmSs(mi_log
               .getElapsed());
      }
      
      /**
       * It is the same as {@link PSScheduledTaskLog#isSuccess()}
       */
      public boolean isSuccess()
      {
         return mi_log.isSuccess();
      }
      
      /**
       * Get the detail message from the task log entry
       * @return
       */
      public String getDetailMessage()
      {
         return mi_log.getProblemDesc();
      }
      
      /**
       * Gets server name or IP that executed the task.
       * @return the server name or IP, may be <code>null</code> or empty.
       */
      public String getServer()
      {
         return mi_log.getServer();
      }

      /**
       * Determines the selected status of this object.
       * @return <code>true</code> if it is selected; otherwise return
       *    <code>false</code>.
       */
      public boolean getSelected()
      {
         return mi_selected;
      }
      
      /**
       * Set the selected status for this object.
       * @param sel <code>true</code> if it is selected.
       */
      public void setSelected(boolean sel)
      {
         mi_selected = sel;
      }
      
      /**
       * Get the help file for the detail log page.
       * @return the help file name, never <code>null</code> or empty.
       */
      public String getHelpFile()
      {
         return PSHelpTopicMapping.getFileName("TaskDetailLog");
      }
   }
   
   /**
    * Get the detail log entry
    * @return the log entry, never <code>null</code>.
    */
   public EventLog getDetailLog()
   {
      if (m_detailLog == null)
         throw new IllegalStateException("m_detailLog must be set first.");
      
      return m_detailLog;
   }
   
   /**
    * Gets the ID/name mapping for all tasks.  
    * @return the ID/name mapping, never <code>null</code>.
    */
   private Map<IPSGuid, String> getTaskIdNameMap() throws PSNotFoundException {
      Map<IPSGuid,String> idnameMap = new HashMap<>();
      // find the task container node
      PSNodeBase parent = getParent();
      PSTaskContainerNode containerNode = null;
      for (PSNodeBase n : parent.getChildren())
      {
         if (n instanceof PSTaskContainerNode)
            containerNode = (PSTaskContainerNode) n;
      }
      if (containerNode == null)
         throw new IllegalStateException("Cannot find Event Container Node.");
      
      // build the map
      for (PSNodeBase n : containerNode.getChildren())
      {
         PSTaskNode event = (PSTaskNode) n;
         idnameMap.put(event.getGUID(), event.getName());
      }
      return idnameMap;
   }
   
   /**
    * Gets the rows for the Event log table
    * @return all event log entries, never <code>null</code>, may be empty.
    */
   public List<EventLog> getEventLogs() throws PSNotFoundException {
      Map<IPSGuid,String> idnameMap = getTaskIdNameMap();
      // 8640 log entries covers 30 days tasks that triggers every 5 minutes.
      List<PSScheduledTaskLog> logs = getService().findAllTaskLogs(8640);
      List<EventLog> eventLogs = new ArrayList<>();
      for (PSScheduledTaskLog log : logs)
      {
         
         String name = idnameMap.get(log.getTaskId());
         if (name == null)
            name = log.getTaskId().toString();  
         EventLog elog = new EventLog(log, name);
         eventLogs.add(elog);
      }
      
      m_eventLogs = eventLogs;
      return eventLogs;
   }
   
   /**
    * Deletes selected log entries.
    */
   public String deleteLogs() throws PSNotFoundException {
      if (m_eventLogs == null)
         getEventLogs();
         
      List<IPSGuid> ids = new ArrayList<>();
      for (EventLog log : m_eventLogs)
      {
         if (log.getSelected())
            ids.add(log.getId());
      }
      if (ids.isEmpty())
         return "admin-no-log-selection-warning";
      
      getService().deleteTaskLogs(ids);
      return perform();
   }
   
   /**
    * Deletes all log entries.
    */
   public String deleteAllLogs()
   {
      getService().deleteAllTaskLogs();
      return perform();
   }
   
   
   @Override
   public String getHelpTopic()
   {
      return "TaskLogs";
   }

   /**
    * Gets the number of rows per page for the table that displays
    * the task log entries for the current node.
    *  
    * @return the number of rows per page.
    */
   public int getPageRows() throws PSNotFoundException {
      setPageRows(null);
      return m_pageRows.intValue();
   }

   /**
    * Set the rows per page if needed.
    * @param entries the list of rows will be displayed on the table.
    */
   private void setPageRows(List<EventLog> entries) throws PSNotFoundException {
      if (m_pageRows != null)
         return;
      
      if (entries == null)
         entries = getEventLogs();
      
      m_pageRows = PSNodeBase.getPageRows(entries.size());
   }
   
   /**
    * Defaults to <code>null</code>. See {@link #getPageRows()} for detail.
    */
   private Integer m_pageRows = null;
   
   /**
    * Current scheduling service. Not <code>null</code>.
    */
   private IPSSchedulingService getService()
   {
      return PSSchedulingServiceLocator.getSchedulingService();
   }

   /**
    * It contains all log entries since last call to {{@link #getEventLogs()}.
    */
   private List<EventLog> m_eventLogs = null;
   
   /**
    * the current detail log entry.
    */
   private EventLog m_detailLog = null;
}
