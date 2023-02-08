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
package com.percussion.services.schedule.impl;

import com.percussion.error.PSExceptionUtils;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.schedule.IPSSchedulingService;
import com.percussion.services.schedule.PSSchedulingException;
import com.percussion.services.schedule.PSSchedulingException.Error;
import com.percussion.services.schedule.data.PSNotificationTemplate;
import com.percussion.services.schedule.data.PSScheduledTask;
import com.percussion.services.schedule.data.PSScheduledTaskLog;
import com.percussion.utils.guid.IPSGuid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Manage scheduled jobs for Rhythmyx. Uses the OpenSymphony Quartz scheduler
 * library.
 * Quartz <code>scheduler</code> and Hibernate <code>sessionFactory</code>
 * properties must be initialized before the service can be used.
 * 
 * @author Doug Rand
 * @author Andriy Palamarchuk
 */
@Transactional
public class PSSchedulingService
      implements IPSSchedulingService
{
   @PersistenceContext
   private EntityManager entityManager;

   private Session getSession(){
      return entityManager.unwrap(Session.class);
   }
 
   // see base
   @Transactional
   public PSScheduledTask createSchedule()
   {
      final PSScheduledTask s = new PSScheduledTask();
      s.setId(getGuidManager().createGuid(PSTypeEnum.SCHEDULED_TASK));
      return s;
   }

   // see base
   public PSScheduledTask findScheduledTaskById(IPSGuid id)
      throws PSSchedulingException
   {
      if (id == null)
      {
         throw new IllegalArgumentException("Schedule id may not be null");
      }
      try
      {
         return maybeCreateScheduleFromJob(id);
      }
      catch (SchedulerException e)
      {
         throw new PSSchedulingException(
               Error.SCHEDULER.ordinal(), e, e.getLocalizedMessage());
      }
   }

   /**
    * Creates a schedule from a quartz job for the provided id,
    * if the job exists.
    * @param id the schedule id. Assumed not <code>null</code>
    * @return the schedule. <code>null</code> of the schedule with the provided
    * id does not exist.
    * @throws SchedulerException on Quartz error.
    * @throws PSSchedulingException on scheduling service error.
    */
   private PSScheduledTask maybeCreateScheduleFromJob(IPSGuid id)
         throws SchedulerException, PSSchedulingException
   {
      final JobDetail jobDetail =
            getScheduler().getJobDetail(new JobKey(id.toString(),JOB_GROUP));
      if (jobDetail == null)
      {
         return null;
      }
      final PSScheduledTask storedSchedule = PSScheduleUtils
            .getStoredSchedule(jobDetail);
      if (storedSchedule == null)
      {
         throw new PSSchedulingException(
               Error.JOB_WITHOUT_SCHEDULE.ordinal(), id.toString());
      }

      final PSScheduledTask schedule = new PSScheduledTask();
      schedule.apply(storedSchedule);
      return schedule;
   }

   // see base
   public Collection<PSScheduledTask> findAllSchedules() throws PSSchedulingException
   {
      final List<PSScheduledTask> schedules = new ArrayList<>();
      try
      {
         
         final GroupMatcher<JobKey> groupMatcher = GroupMatcher.groupEquals(JOB_GROUP);
         
         for (JobKey jobKey : getScheduler().getJobKeys(groupMatcher))
         {
            final IPSGuid id = new PSGuid(jobKey.getName());
            final PSScheduledTask schedule = findScheduledTaskById(id);
            // there is no trigger corresponding to the job
            // probably data is broken for some reason
            if (schedule != null)
            {
               schedules.add(schedule);
            }
         }
      }
      catch (SchedulerException e)
      {
         throw new PSSchedulingException(
               Error.SCHEDULER.ordinal(), e, e.getLocalizedMessage());
      }
      return schedules;
   }


   // see base
   public PSScheduledTask findScheduleByName(String label)
         throws PSSchedulingException
   {
      SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
      for (final PSScheduledTask schedule : findAllSchedules())
      {
         if (schedule.getName().equals(label))
         {
            return schedule;
         }
      }
      return null;
   }

   // see base
   @Transactional(noRollbackFor = Exception.class)
   public void saveSchedule(PSScheduledTask schedule) throws PSSchedulingException
   {
      SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
      if (schedule == null)
      {
         throw new IllegalArgumentException(SCHEDULE_NOT_NULL);
      }

      try
      {
         apply(schedule);
      }
      catch (SchedulerException e)
      {
         throw new PSSchedulingException(
               Error.SCHEDULER.ordinal(), e, e.getLocalizedMessage());
      }
      catch (ParseException e)
      {
         throw new PSSchedulingException(Error.CRON_FORMAT.ordinal(),
               e, schedule.getCronSpecification(), e.getLocalizedMessage());
      }
   }

   /*
    * //see base class method for details
    */
   @Transactional
   public void runNow(PSScheduledTask schedule)
   {
      SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

      if (schedule == null)
         throw new IllegalArgumentException("schedule may not be null.");

      try {
         if (!m_scheduler.isStarted()) {
            m_scheduler.start();
         }

         PSTaskAdapter.runJob(schedule,m_scheduler,true);

      } catch (SchedulerException e) {
         ms_log.error("An unexpected error occurred while running job: {} Error: {}",
                 schedule.getName(),
                 PSExceptionUtils.getMessageForLog(e));
         ms_log.debug(PSExceptionUtils.getDebugMessageForLog(e));
      }

   }
   
   /**
    * It fires a non-scheduled job in a separate thread.
    */
   class PSRunNow extends Thread
   {
      /**
       * Creates an instance for an given job.
       * @param job the executed job, never <code>null</code>.
       */
      public PSRunNow(PSScheduledTask job)
      {
         super("RunNow");
         
         if (job == null)
            throw new IllegalArgumentException("job may not be null.");
         
         m_job = job;
      }
      
      @Override
      public void run()
      {
         PSTaskAdapter.runJob(m_job, getScheduler(), true);
      }
      
      /**
       * The executed job, init by ctor, never <code>null</code> after that.
       */
      private PSScheduledTask m_job;
   }
   /**
    * Applies the provided schedule by configuring Quartz to run it.
    * @param schedule the schedule. Assumed not <code>null</code>.
    * @throws SchedulerException on Quartz error.
    * @throws ParseException on failure to parse the schedule cron
    * specification.
    * @throws PSSchedulingException 
    */
   private void apply(PSScheduledTask schedule) throws SchedulerException,
         ParseException, PSSchedulingException
   {
      SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

      // create these first, so in case of an error here
      // the old definition is not touched
      final Trigger trigger = createTrigger(schedule);
      final JobDetail jobDetail = createJobDetail(schedule);

      // save previous job/trigger for recovering in the case of failure 
      PSScheduledTask previousSched = findScheduledTaskById(schedule.getId());

      if (previousSched != null)
      {
         deleteSchedule(previousSched.getId());
      }
      try
      {
         getScheduler().scheduleJob(jobDetail, trigger);
      }
      finally
      {
         if (previousSched != null && findScheduledTaskById(schedule.getId()) == null)
         {
            // In the case of failure, try to restore the old job.
            // Note, this still resets the last firing time for the job.
            saveSchedule(previousSched);
         }
      }
   }

   /**
    * Creates a Quartz trigger from a Rhythmyx schedule.
    * @param schedule the schedule to generate trigger for.
    * Assumed not <code>null</code>.
    * @return a trigger generated from the schedule data.
    */

   private Trigger createTrigger(PSScheduledTask schedule)
   {
      SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
      final String id = schedule.getId().toString();
      
      CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(id,TRIGGER_GROUP)
            .withSchedule(CronScheduleBuilder.cronSchedule(schedule.getCronSpecification()).withMisfireHandlingInstructionDoNothing()).build(); 
      
      // Workaround bug http://jira.opensymphony.com/browse/QUARTZ-566
      final String d = "dummy";
      trigger.getJobDataMap().put(d, d);
      return trigger;
   }

   /**
    * Creates a Quartz job detail object from a Rhythmyx schedule object.
    * @param schedule the Rhythmyx schedule to generate job detail for.
    * Assumed not null.
    * @return a job detail generated from the schedule data.
    * Not <code>null</code>.
    */
   @Transactional(noRollbackFor = Exception.class)
   public JobDetail createJobDetail(PSScheduledTask schedule)
   {
      SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
      final String id = schedule.getId().toString();
      
      //final JobDetail jobDetail =
      //      new JobDetail(id, JOB_GROUP, PSTaskAdapter.class);
      
      JobDetail jobDetail = JobBuilder.newJob(PSTaskAdapter.class)
            .withIdentity(id, JOB_GROUP).build();
      
      PSScheduleUtils.storeScheduleInJob(schedule, jobDetail);
      return jobDetail;
   }

   // see base
   @Transactional(noRollbackFor = Exception.class)
   public void deleteSchedule(IPSGuid scheduleId) throws PSSchedulingException
   {
      SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
      if (scheduleId == null)
         throw new IllegalArgumentException(SCHEDULE_NOT_NULL);

      try
      {
         // delete the job and its associated trigger(s).
         getScheduler().deleteJob(new JobKey(scheduleId.toString(), JOB_GROUP));
      }
      catch (SchedulerException e)
      {
         throw new PSSchedulingException(
               Error.SCHEDULER.ordinal(), e, e.getLocalizedMessage());
      } 
   }

   // see base
   @Transactional
   public PSNotificationTemplate createNotificationTemplate()
   {
      final PSNotificationTemplate t = new PSNotificationTemplate();
      t.setId(getGuidManager().createGuid(
            PSTypeEnum.SCHEDULE_NOTIFICATION_TEMPLATE));
      t.setName("SetNewName");
      t.setSubject("'Set a new subject'");
      return t;
   }

   // see base
   public PSNotificationTemplate findNotificationTemplateById(IPSGuid id)
   {
      if (id == null)
      {
         throw new IllegalArgumentException(
               "Notification template id may not be null");
      }
      return getSession().get(
            PSNotificationTemplate.class, id.longValue());
   }

   // see base
   @SuppressWarnings("unchecked")
   public PSNotificationTemplate findNotificationTemplateByName(String name)
   {
      final List<PSNotificationTemplate> results =
              getSession().createQuery(
                  "from PSNotificationTemplate where name = :name").setParameter(
                  "name", name).list();
      return results.isEmpty() ? null : results.get(0); 
   }

   // see base
   @SuppressWarnings("unchecked")
   public Collection<PSNotificationTemplate> findAllNotificationTemplates()
   {
      return getSession().createCriteria(PSNotificationTemplate.class).list();
   }

   // see base
   public Set<String> findAllNotificationTemplatesNames()
   {
      final Set<String> labels = new HashSet<>();
      for (PSNotificationTemplate n : findAllNotificationTemplates())
      {
         labels.add(n.getName());
      }
      return labels;
   }

   // see base
   @Transactional(noRollbackFor = Exception.class)
   public void saveNotificationTemplate(
         PSNotificationTemplate notificationTemplate)
   {
      if (notificationTemplate == null)
      {
         throw new IllegalArgumentException(
               "Notification template may not be null");
      }
      getSession().saveOrUpdate(notificationTemplate);
   }

   // see base
   @Transactional(noRollbackFor = Exception.class)
   public void deleteNotificationTemplate(IPSGuid templateId)
   {
      if (templateId == null)
         throw new IllegalArgumentException("templateId may not be null");

      Session sess = getSession();

         String sql = "delete from PSNotificationTemplate t where t.id = :id";
         Query hql = sess.createQuery(sql);
         hql.setParameter("id", templateId.longValue());
         hql.executeUpdate();

   }

   /**
    * Current Guid manager.
    * @return guid manager. Never <code>null</code>.
    */
   private IPSGuidManager getGuidManager()
   {
      return PSGuidManagerLocator.getGuidMgr();
   }

   /**
    * @return the Quartz scheduler. Never <code>null</code>.
    */
   private Scheduler getScheduler()
   {
      assert m_scheduler != null;
      return m_scheduler;
   }

   /**
    * Sets the Quart scheduler used by the service. This should be called
    * be the Spring framework.
    * 
    * @param scheduler the scheduler to assign. Not <code>null</code>. 
    */
   public void setScheduler(Scheduler scheduler)
   {
      if (scheduler == null)
      {
         throw new IllegalArgumentException(
               "Quartz scheduler should not be null.");
      }
      m_scheduler = scheduler;
      try
      {
         m_scheduler.start();
      }
      catch (Exception e)
      {
         ms_log.error("Failed to start the Quartz scheduler", e);
      }
   }

   // see base
   @Transactional
   public IPSGuid createTaskLogId()
   {
      return getGuidManager().createGuid(PSTypeEnum.SCHEDULE_TASK_LOG);
   }

   // see base
   public PSScheduledTaskLog findTaskLogById(IPSGuid id)
   {
      if (id == null)
         throw new IllegalArgumentException("Event log id may not be null");

      return  getSession().get(
            PSScheduledTaskLog.class, id.longValue());

   }

   // see base
   @Transactional(noRollbackFor = Exception.class)
   public void saveTaskLog(PSScheduledTaskLog taskLog)
   {
      if (taskLog == null)
         throw new IllegalArgumentException("taskLog may not be null");

      getSession().saveOrUpdate(taskLog);
   }
   
   // see base
   @Transactional(noRollbackFor = Exception.class)
   public void deleteTaskLog(IPSGuid id)
   {
      if (id == null)
         throw new IllegalArgumentException("id must not be null.");

      deleteTaskLogEntries(Collections.singleton(id));
   }
   
   /*
    * //see base class method for details
    */
   @Transactional(noRollbackFor = Exception.class)
   public void deleteTaskLogs(Collection<IPSGuid> ids)
   {
      if (ids == null)
         throw new IllegalArgumentException("ids may not be null.");
      
      deleteTaskLogEntries(ids);
   }

   /**
    * The same as {@link #deleteTaskLogs(Collection)}, except this method does
    * the real work.
    */
   @Transactional(noRollbackFor = Exception.class)
   public void deleteTaskLogEntries(Collection<IPSGuid> ids)
   {
      Session sess = getSession();

         String sql = "delete from PSScheduledTaskLog e where e.log_id = :logid";
         Query hql = sess.createQuery(sql);
         for (IPSGuid id : ids)
         {
            hql.setParameter("logid", id.longValue());
            hql.executeUpdate();
         }

   }

   /*
    * //see base class method for details
    */
   @SuppressWarnings({ "cast", "unchecked" })
   public List<PSScheduledTaskLog> findAllTaskLogs(int maxResult)
   {
      Session s = getSession();

         Criteria c = s.createCriteria(PSScheduledTaskLog.class);
         c.setProjection(Projections.projectionList().add(
               Projections.property("log_id")).add(
               Projections.property("task_id")).add(
               Projections.property("start_time")).add(
               Projections.property("end_time")).add(
               Projections.property("is_success")));
         c.addOrder(Order.desc("end_time"));
         if (maxResult > 0)
            c.setMaxResults(maxResult);
         
         List<Object[]> results = c.list();
         
         List<PSScheduledTaskLog> retval = new ArrayList<>();
         for (Object[] props : results)
         {
            retval.add(getScheduledTask(props));
         }
         return retval;

   }
   
   /**
    * Creates a task log entry from the given properties.
    * 
    * @param props the properties of the created log entry, assumed not
    * <code>null</code>.
    * 
    * @return the created log entry, which does not include the problem
    * description property, e.i, {@link PSScheduledTaskLog#getProblemDesc()}
    * will be <code>null</code> for the returned log entries. It is not 
    * <code>null</code>, but may be empty.
    */
   public PSScheduledTaskLog getScheduledTask(Object[] props)
   {
      Long logId = (Long) props[0];
      IPSGuid logGuid = getGuidManager().makeGuid(logId,
            PSTypeEnum.SCHEDULE_TASK_LOG);
      Long taskId = (Long) props[1];
      IPSGuid taskGuid = getGuidManager().makeGuid(taskId,
            PSTypeEnum.SCHEDULED_TASK);
      Date startTime = (Date) props[2];
      Date endTime = (Date) props[3];
      boolean isSuccess = ((Character) props[4]) == 'Y';
      
      return new PSScheduledTaskLog(logGuid, taskGuid,
            startTime, endTime, isSuccess);
   }
   
   /*
    * //see base class method for details
    */
   @Transactional(noRollbackFor = Exception.class)
   public void deleteAllTaskLogs()
   {
      Session sess = getSession();

         String sql = "delete from PSScheduledTaskLog";
         Query hql = sess.createQuery(sql);
         hql.executeUpdate();

   }

   /*
    * //see base class method for details
    */
   @Transactional(noRollbackFor = Exception.class)
   public void deleteTaskLogsByDate(Date beforeDate)
   {
      if (beforeDate == null)
         throw new IllegalArgumentException("beforeDate may not be null");
      
      Session session = getSession();

         String sql = "delete from PSScheduledTaskLog t where t.end_time < :endTime";
         Query hql = session.createQuery(sql);
         hql.setParameter("endTime", beforeDate);


   }
   
   /**
    * Quartz job group name for the quartz jobs used by this class.
    */
   private static final String JOB_GROUP = "rx";

   /**
    * Quartz trigger group name for the quartz triggers used by this class.
    */
   private static final String TRIGGER_GROUP = JOB_GROUP;

   /**
    * Validation exception message that the provided schedule is not null.
    */
   private static final String SCHEDULE_NOT_NULL = "Schedule may not be null";

   /**
    * @see #setScheduler(Scheduler)
    */
   private Scheduler m_scheduler;
   
   /**
    * The logger for this class.
    */
   private static final Logger ms_log = LogManager.getLogger(PSSchedulingService.class);   
}
