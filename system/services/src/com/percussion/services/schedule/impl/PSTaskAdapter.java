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
package com.percussion.services.schedule.impl;

import com.percussion.design.objectstore.PSAttribute;
import com.percussion.design.objectstore.PSAttributeList;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSSubject;
import com.percussion.extension.IPSExtensionManager;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionRef;
import com.percussion.security.PSRoleManager;
import com.percussion.security.PSSecurityProvider;
import com.percussion.server.PSServer;
import com.percussion.services.schedule.IPSSchedulingService;
import com.percussion.services.schedule.IPSTask;
import com.percussion.services.schedule.IPSTaskResult;
import com.percussion.services.schedule.PSSchedulingException;
import com.percussion.services.schedule.PSSchedulingException.Error;
import com.percussion.services.schedule.PSSchedulingServiceLocator;
import com.percussion.services.schedule.data.PSNotificationTemplate;
import com.percussion.services.schedule.data.PSNotifyWhen;
import com.percussion.services.schedule.data.PSScheduledTask;
import com.percussion.services.schedule.data.PSScheduledTaskLog;
import com.percussion.services.schedule.data.PSTaskResult;
import com.percussion.services.system.IPSSystemService;
import com.percussion.services.system.PSSystemServiceLocator;
import com.percussion.services.utils.jexl.PSServiceJexlEvaluatorBase;
import com.percussion.services.utils.jexl.PSVelocityUtils;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.jexl.PSJexlEvaluator;
import com.percussion.workflow.PSWorkFlowUtils;
import com.percussion.workflow.mail.PSMailMessageContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.RuntimeServices;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Adapts Rhythmyx scheduler tasks {@link IPSTask} to the Quartz job interface,
 * so Quartz can run them. 
 * <p>
 * Note, the same job can be triggered by Quartz, but this class will prevent
 * the same task to be executed concurrently. 
 */
public class PSTaskAdapter implements Job
{
   /**
    * Logger for this class.
    */
   private static Log ms_log = LogFactory.getLog(PSTaskAdapter.class);
   
   // see base
   public void execute(JobExecutionContext context)
   {
      SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
      PSScheduledTask job = PSScheduleUtils.getStoredSchedule(context.getJobDetail());
      
      if ((!PSServer.isInitialized()))
      {
         System.out.println("Server is not initialized - skip executing job: "
               + job.toString());
         return;
      }
      
      runJob(job, context.getScheduler(), false);

   }

   /**
    * Run a supplied job.
    * 
    * @param job the to be executed job, not <code>null</code>.
    * @param scheduler the Quartz scheduler, not <code>null</code>.
    * @param isRunNow <code>true</code> if the task is manually invoked, in 
    *    this case, the task will be fired without check if the same task is
    *    already running or not; otherwise, the execution of the task will be
    *    skip if the same task is already running.
    */
   public static void runJob(PSScheduledTask job, Scheduler scheduler,
         boolean isRunNow)
   {
      if (job == null)
         throw new IllegalArgumentException("job may not be null.");
      if (scheduler == null)
         throw new IllegalArgumentException("scheduler may not be null");
      
      IPSTaskResult result;
      if (isRegisteredServer(job))
      {
         result = executeTask(job, isRunNow);         
      }
      else
      {
         if (!isRunNow) // skip log/notification for scheduled invocation
            return;

         long startTime = System.currentTimeMillis();
         String httpPort = PSServer.getListenerPort() == -1 ? "" : String
               .valueOf(PSServer.getListenerPort());
         String httpsPort = PSServer.getSslListenerPort() == 0 ? "" : String
               .valueOf(PSServer.getSslListenerPort());
         
         PSSchedulingException se = new PSSchedulingException(
               Error.SKIP_EXE_TASK_NOT_REG_SERVER.ordinal(), job.getId()
                     .toString(), job.getName(), job.getServer(),
               PSServer.getHostName(), PSServer.getFullyQualifiedHostName(), 
               PSServer.getHostAddress(), httpPort, httpsPort);
         String skipMsg = se.getLocalizedMessage();
         result = getErrorResult(job, skipMsg, startTime);
         logTaskExecution(job, result, startTime, getCurServer());
      }

      notifyTaskResult(job, result);
      
   }
   
   /**
    * Send notification for a finished job if the event is defined to do so.
    * 
    * @param job the finished job, assumed not <code>null</code>.
    * @param result the result of the finished job, assumed not <code>null</code>.
    */
   private static void notifyTaskResult(PSScheduledTask job, IPSTaskResult result)
   {
      if (!needToNotify(job, result))
         return;

      try
      {
         PSJexlEvaluator eval = getEvaluator(job, result);
         
         PSNotificationTemplate t = getScheduleService()
               .findNotificationTemplateById(job.getNotificationTemplateId());
         
         String subject = getEvaluateSubject(t.getSubject(), eval);

         String message = getNotifyMessage(t, eval.getVars());

         sendNotification(job, subject, message);
      }
      catch (Exception e)
      {
         e.printStackTrace();
         ms_log.error("Failed notification for task: " + job.getName(), e);
      }
   }

   /**
    * Get the JEXL evaluator for the supplied job and its result.
    * @param job the job, assumed not <code>null</code>.
    * @param result the result of the job run, assumed not <code>null</code>.
    * @return the evaluator, never <code>null</code>.
    */
   private static PSJexlEvaluator getEvaluator(PSScheduledTask job,
         IPSTaskResult result)
   {
      PSJexlEvaluator eval = new PSJexlEvaluator();
      for (Map.Entry<String, Object> entry : result.getNotificationVariables()
            .entrySet())
      {
         eval.bind(entry.getKey(), entry.getValue());
      }
      
      eval.bind("$sys.taskName", job.getName());
      
      if (getToolsMap() != null)
         eval.bind("$tools", getToolsMap());

      return eval;
   }
   
   /**
    * Sends a notification for the specified job and message.
    * 
    * @param job the finished job, assumed not <code>null</code>.
    * @param subject the subject of the notification, assumed not
    *    <code>null</code> or empty.
    * @param message the message body, assumed not <code>null</code>.
    */
   private static void sendNotification(PSScheduledTask job, String subject,
         String message)
   {
      String to = getNotifyTo(job);

      PSMailMessageContext mailMsg = new PSMailMessageContext(
              PSSecurityProvider.INTERNAL_USER_NAME, to, null,
              subject, message, null, getMailDomain(), getSmtpHost(),
              getSmtpUsername(), getSmtpPassword(), getSmtpIsTLSEnabled(),
              getSmtpPort(), getSmtpSSLPort(), getSmtpBounceAddr());
      
      IPSSystemService svc = PSSystemServiceLocator.getSystemService();
      svc.sendEmail(mailMsg);
   }
   
   /**
    * See {@link #getMailDomain()}
    */
   private static String ms_mailDomain = null;
   
   /**
    * See {@link #getSmtpHost()}.
    */
   private static String ms_smtpHost = null;

   /**
    * See {@link #getSmtpUsername()}.
    */
   private static String ms_smtpUsername = null;

   /**
    * See {@link #getSmtpPassword()}.
    */
   private static String ms_smtpPassword = null;

   /**
    * See {@link #getSmtpPort()}.
    */
   private static String ms_smtpPort = null;

   /**
    * See {@link #getSmtpIsTLSEnabled()}.
    */
   private static String ms_smtpIsTLSEnabled = null;

   /**
    * See {@link #getSmtpSSLPort()}.
    */
   private static String ms_smtpSSLPort = null;

   /**
    * See {@link #getSmtpBounceAddr()}.
    */
   private static String ms_smtpBounceAddr = null;
   
   /**
    * Get the mail domain that is defined in workflow properties file.
    * @return the mail domain, it may be <code>null</code> or empty if not
    *    defined.
    */
   private static String getMailDomain()
   {
      if (ms_mailDomain != null)
         return ms_mailDomain;
      
      ms_mailDomain = PSWorkFlowUtils.getProperty("MAIL_DOMAIN");
      return ms_mailDomain;
   }
   
   /**
    * Get the SMPT Host from the workflow properties file.
    * @return the SMPT Host property, never <code>null</code> or empty.
    * @throws IllegalStateException if the property is not defined.
    */
   private static String getSmtpHost()
   {
      if (ms_smtpHost != null)
         return ms_smtpHost;

      ms_smtpHost = PSWorkFlowUtils.getProperty("SMTP_HOST");
      if (StringUtils.isBlank(ms_smtpHost))
      {
         throw new IllegalStateException(
            "SMTP_HOST does not exist in rxworkflow.properties.");
      }
      return ms_smtpHost;
   }

   /**
    * Get the SMTP user name that is defined in workflow properties file.
    * @return the user name, it may be <code>null</code> or empty if not
    *    defined.
    */
   private static String getSmtpUsername()
   {
      if (ms_smtpUsername != null)
         return ms_smtpUsername;

      ms_smtpUsername = PSWorkFlowUtils.getProperty("SMTP_USERNAME");
      return ms_smtpUsername;
   }

   /**
    * Get the SMTP bounce address that is defined in workflow properties file.
    * @return the bounce address, it may be <code>null</code> or empty if not
    *    defined.
    */
   private static String getSmtpBounceAddr()
   {
      if (ms_smtpBounceAddr != null)
         return ms_smtpBounceAddr;

      ms_smtpBounceAddr = PSWorkFlowUtils.getProperty("SMTP_BOUNCEADDR");
      return ms_smtpBounceAddr;
   }

   /**
    * Get the SMTP password that is defined in workflow properties file.
    * @return the password, it may be <code>null</code> or empty if not
    *    defined.
    */
   private static String getSmtpPassword()
   {
      if (ms_smtpPassword != null)
         return ms_smtpPassword;

      ms_smtpPassword = PSWorkFlowUtils.getProperty("SMTP_PASSWORD");
      return ms_smtpPassword;
   }

   /**
    * Get the SMTP SSL port that is defined in workflow properties file.
    * @return the SSL port, it may be <code>null</code> or empty if not
    *    defined.
    */
   private static String getSmtpSSLPort()
   {
      if (ms_smtpSSLPort != null)
         return ms_smtpSSLPort;

      ms_smtpSSLPort = PSWorkFlowUtils.getProperty("SMTP_SSLPORT");
      return ms_smtpSSLPort;
   }

   /**
    * Get the SMTP port that is defined in workflow properties file.
    * @return the port, it may be <code>null</code> or empty if not
    *    defined.
    */
   private static String getSmtpPort()
   {
      if (ms_smtpPort != null)
         return ms_smtpPort;

      ms_smtpPort = PSWorkFlowUtils.getProperty("SMTP_PORT");
      return ms_smtpPort;
   }

   /**
    * Determine whether TLS is enabled; defined in workflow properties file.
    * @return "true" if enabled, it may be <code>null</code> or empty if not
    *    defined.
    */
   private static String getSmtpIsTLSEnabled()
   {
      if (ms_smtpIsTLSEnabled != null)
         return ms_smtpIsTLSEnabled;

      ms_smtpIsTLSEnabled = PSWorkFlowUtils.getProperty("SMTP_TLSENABLED");
      return ms_smtpIsTLSEnabled;
   }

   /**
    * Get the notification target/to addresses.
    * 
    * @param job the finished job, assumed not <code>null</code>.
    * 
    * @return the target addresses, never <code>null</code> or empty.
    */
   private static String getNotifyTo(PSScheduledTask job)
   {
      String to = getEmailAddressesFromNotifyRole(job);

      String addresses = getEmailAddresses(job);
      if (StringUtils.isNotBlank(addresses))
      {
         if (StringUtils.isNotBlank(to))
            to = to + "," + addresses;
         else
            to = addresses;
      }
      if (StringUtils.isBlank(to))
         throw new IllegalStateException(
               "job does not contain a notification target/to address.");
      
      return to;
   }
   
   /**
    * Get and normalize the email addresses defined in the given  task. The
    * email addresses are not defined by the notify role of the task.
    * @param job the task in question, assumed not <code>null</code>.
    * @return the normalized email addresses. It may be <code>null</code> if
    *    the email addresses is not defined in the task.
    */
   private static String getEmailAddresses(PSScheduledTask job)
   {
      String addresses = job.getEmailAddresses();
      if (StringUtils.isBlank(addresses))
         return null;
      
      StringBuffer result = new StringBuffer();
      boolean isFirst = true;
      for (final String s : addresses.split(","))
      {
         final String entry = s.trim();
         if (StringUtils.isBlank(entry))
            continue;

         if (!isFirst)
            result.append(",");
         isFirst = false;
         result.append(normalizeEmailAddress(entry));
      }

      return result.toString();
   }
   
   /**
    * Normalize a given email address. Append mail domain to the supplied
    * email address if it does not contain one.
    * @param email the email address in question, assumed not <code>null</code>.
    * @return the normalized email address.
    */
   private static String normalizeEmailAddress(String email)
   {
      if (email.indexOf("@") == -1 && StringUtils.isNotBlank(getMailDomain()))
      {
         if (getMailDomain().startsWith("@"))
            return email + getMailDomain();
         else
            return email + "@" + getMailDomain();
      }
      
      return email;
   }
   
   /**
    * Get the email addresses from the notified role of the job.
    * @param job the task in question, assumed not <code>null</code>.
    * @return the email addresses. It may be <code>null</code> or empty if
    *    the notified role is not specified or there is no sys_email property
    *    specified in any of the members.
    */
   @SuppressWarnings("unchecked")
   private static String getEmailAddressesFromNotifyRole(PSScheduledTask job)
   {
      String roleName = job.getNotify();
      if (StringUtils.isBlank(roleName))
         return null;
      
      StringBuffer emails = new StringBuffer();
      boolean isFirst = true;
      PSRoleManager rmgr = PSRoleManager.getInstance();
      Set<PSSubject> users = rmgr.getSubjects(roleName, null);
      for (PSSubject user : users)
      {
         PSAttributeList atts = user.getAttributes();
         PSAttribute attr = atts.getAttribute("sys_email");
         if (attr != null)
         {
            String email = attr.getValues().get(0).toString();
            if (!isFirst)
               emails.append(",");
            isFirst = false;
            emails.append(normalizeEmailAddress(email));
         }
      }
      
      return emails.toString();
   }

   /**
    * Evaluate the supplied subject (in JEXL expression).
    * 
    * @param subject the subject in JEXL expression, 
    *    assumed not <code>null</code>.
    * @param eval the evaluator, assumed not <code>null</code>.
    * 
    * @return the evaluated subject, never <code>null</code>, may be empty.
    */
   private static String getEvaluateSubject(String subject,
         PSJexlEvaluator eval)
   {
      try
      {
         Object v = eval.evaluate(eval.createScript(subject));
         return v.toString();
      }
      catch (Exception e)
      {
         ms_log.error("Failed to evaluate subject: " + subject, e);
         return "";
      }
   }

   /**
    * Get the JEXL utilities / tools, which is loaded rom the tools.xml.
    * @return the map of the tools. It is <code>null</code> if failed to
    *    load the tools.
    */
   private static Map<String, Object> getToolsMap()
   {
      if (ms_toolsMap == null)
      {
         PSServiceJexlEvaluatorBase jexlBase = new PSServiceJexlEvaluatorBase(
               false);
         try
         {
            ms_toolsMap = jexlBase.getVelocityToolBindings();
         }
         catch (Exception e)
         {
            ms_toolsMap = null;
            ms_log.error("Failed to load Velocity Tools", e);
         }
      }
      return ms_toolsMap;
   }
   
   /**
    * The Velocity Tools, initialized by 
    * {@link #getToolsMap()}, never <code>null</code> after that.
    */
   private volatile static Map<String,Object> ms_toolsMap = null;
   
   /**
    * Get the schedule service
    * @return the schedule service, never <code>null</code>.
    */
   private static IPSSchedulingService getScheduleService()
   {
      return PSSchedulingServiceLocator.getSchedulingService();
   }

   /**
    * Render the supplied job result with the specified notification template.
    * 
    * @param nt the notification template, assumed not
    *    <code>null</code>.
    * @param vars the job result, assumed not <code>null</code>.
    * 
    * @return the rendered text, never <code>null</code>, may be empty.
    */
   @SuppressWarnings("unchecked")
   private static String getNotifyMessage(PSNotificationTemplate nt, Map vars)
   {
      VelocityContext ctx = PSVelocityUtils.getContext(vars);
      
      try
      {
         Template t = PSVelocityUtils.compileTemplate(nt.getTemplate(),
               "EventNotification", getVelocityRS());         
         StringWriter writer = new StringWriter();
         t.merge(ctx, writer);
         writer.close();
         String message = writer.toString();
         return message;
      }
      catch (Exception e)
      {
         e.printStackTrace();
         ms_log.error("Failed to format Notification Template id="
               + nt.getId(), e);
         return null;
      }
   }
   
   /**
    * @return the Velocity Runtime object, never <code>null</code>.
    * 
    * @throws Exception if cannot get the velocity runtime.
    */
   private static RuntimeServices getVelocityRS() throws Exception
   {
      RuntimeServices rs = new RuntimeInstance();
      File logpath = new File(PSServer.getRxDir(), "notificationVelocity.log");
      rs.addProperty(RuntimeConstants.RUNTIME_LOG, logpath.getAbsolutePath());
      rs.init();
      
      return rs;
   }
   
   /**
    * Determines if the suppled job need to be notified.
    * @param job the job in question, assumed not <code>null</code>.
    * @param result the job result, assumed not <code>null</code>.
    * @return <code>true</code> if need to be notified.
    */
   private static boolean needToNotify(PSScheduledTask job, IPSTaskResult result)
   {
      if(PSWorkFlowUtils.getProperty(PSWorkFlowUtils.NOTIFICATION_ENABLE).equalsIgnoreCase("N"))
         return false; //Notifications aren't configured so skip it.

      PSNotifyWhen when = job.getNotifyWhen();
      return (when.ordinal() == PSNotifyWhen.ALWAYS.ordinal()
            || (when.ordinal() == PSNotifyWhen.FAILURE.ordinal() 
                  && (!result.wasSuccess())));
   }
   
   /**
    * Determines if the given task needs to be fired or executed.
    * 
    * @param task the to be executed task, assumed not <code>null</code>.
    * 
    * @throws PSSchedulingException if should skip firing the task.  
    */
   @SuppressWarnings({ "unchecked", "cast" })
   private static void validateExecution(PSScheduledTask task)
      throws PSSchedulingException
   {
      if (isJobActive(task.getId()))
      {
         PSSchedulingException se = new PSSchedulingException(
               Error.SKIP_FIRE_SCHEDULED_TASK.ordinal(), task.getId()
                     .toString(), task.getName());
         throw se;
      }
   }
   
   
   /**
    * Load the task using the extensions manager.
    * 
    * @param name the name of the task, never <code>null</code> or empty.
    * 
    * @return the extension instance, never <code>null</code>.
    * 
    * @throws PSNotFoundException if cannot find the extension.
    * @throws PSExtensionException error on prepare the extension.
    */
   @SuppressWarnings("unchecked")
   private static IPSTask getTask(String name)
      throws PSNotFoundException, PSExtensionException
   {
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name may not be null or empty");
      }
      IPSExtensionManager emgr = PSServer.getExtensionManager(null);
      PSExtensionRef ref = new PSExtensionRef(name);
      return (IPSTask) emgr.prepareExtension(ref, null);
   }

   /**
    * Execute the given task or job.
    * 
    * @param curJob the executed job, assumed not <code>null</code>.
    * @param isRunNow <code>true</code> if the task is manually invoked, in 
    *    this case, the task will be fired without check if the same task is
    *    already running or not; otherwise, the execution of the task will be
    *    skip if the same task is already running.
    * 
    * @return the result of the execution if the job is successfully executed;
    *    return <code>null</code> if failed to execute the job. 
    */
   private static IPSTaskResult executeTask(PSScheduledTask curJob,
         boolean isRunNow)
   {
      IPSTaskResult result = null;
      long startTime = System.currentTimeMillis();
      
      try
      {
         if (! isRunNow)
            validateExecution(curJob);
         
         addJobId(curJob.getId());
         IPSTask task = getTask(curJob.getExtensionName());
         result = task.perform(curJob.getParameters());
         return result;
      }
      catch (Exception e)
      {
         ms_log.error("Failed to execute job: " + curJob.toString(), e);
         result = getErrorResult(curJob, e, startTime);
         return result;
      }
      finally
      {
         logTaskExecution(curJob, result, startTime, getCurServer());
         removeJobId(curJob.getId());
      }
   }

   /**
    * Gets the current server instance, name / port pair in the format of
    * &lt;host>[:port]. The port is HTTP port if HTTP port is defined, or 
    * HTTPS port if HTTP port is not defined, but the HTTPS port is defined.
    * The port may be empty if neither HTTP and HTTPS are not defined.
    * 
    * @return the name and/or port pair, never <code>null</code> or empty.
    */
   private static String getCurServer()
   {
      String host = PSServer.getHostName();
      String port = "";
      if (PSServer.getListenerPort() != -1)
         port = String.valueOf(PSServer.getListenerPort());
      else if (PSServer.getSslListenerPort() != 0)
         port = String.valueOf(PSServer.getSslListenerPort());
      
      return (StringUtils.isBlank(port)) ? host : host + ":" + port;
   }
   
   /**
    * Gets the host name from a given server instance.
    * @param server the server instance in the format of &lt;host-name>[:port].
    *    Assumed not <code>null</code> or empty.
    * @return the host name part of the server instance. 
    *    Never <code>null</code>, but may be empty. 
    */
   private static String getServerName(String server)
   {
      String[] result = server.split(":");
      return result[0] == null ? "" : result[0];
   }

   /**
    * Gets the port from a given server instance.
    * @param server the server instance in the format of &lt;host-name>[:port].
    *    Assumed not <code>null</code> or empty.
    * @return the port part of the server instance. 
    *    Never <code>null</code>, but may be empty. 
    */
   private static String getServerPort(String server)
   {
      String[] result = server.split(":");
      return result.length > 1 ? result[1].trim() : "";
   }

   /**
    * Determines if the given task is registered for the current server.
    * @param curJob the task in question, assumed not <code>null</code>.
    * @return <code>true</code> if the task is registered for the current 
    *    server.
    */
   private static boolean isRegisteredServer(PSScheduledTask curJob)
   {
      if (StringUtils.isBlank(curJob.getServer()))
         return true;
      
      String server = curJob.getServer().trim();
      String host = getServerName(server);
      String port = getServerPort(server);
      boolean isHostMatch = false;
      
      // compare the (optional) port of the server instance 
      boolean isPortMatch = true;
      if (StringUtils.isNotBlank(port))
      {
         String httpPort = null;
         if (PSServer.getListenerPort() != -1)
            httpPort = String.valueOf(PSServer.getListenerPort());
         String httpsPort = null;
         if (PSServer.getSslListenerPort() != 0)
            httpsPort = String.valueOf(PSServer.getSslListenerPort());
         
         isPortMatch = (httpPort != null && httpPort.equals(port)) ||
               (httpsPort != null && httpsPort.equals(port));
      }
      
      // compare the host name
      try
      {
         isHostMatch = PSServer.getHostName().equalsIgnoreCase(host)
               || PSServer.getFullyQualifiedHostName().equalsIgnoreCase(host)
               || PSServer.getHostAddress().equalsIgnoreCase(host);
      }
      catch (Exception e)
      {
         ms_log.error("Failed to identify server name or IP address.", e);
      }
      
      if (ms_log.isDebugEnabled())
      {
         String hostName = PSServer.getHostName();
         if (isHostMatch && isPortMatch)
         {
            ms_log.debug("Task of '" + curJob.getName()
                  + "' is registered for the server '" + hostName + "'.");
         }
         else
         {
            ms_log.debug("Task of '" + curJob.getName()
                  + "' is not registered for the server '" + hostName + "'.");
         }
      }

      return isHostMatch && isPortMatch;
   }
   
   /**
    * Creates the task result for the failed task and the exception.
    * @param task the failed task, assumed not <code>null</code>.
    * @param e the cause of the failure, assumed not <code>null</code>.
    * @return the created task result, never <code>null</code>.
    */
   private static IPSTaskResult getErrorResult(PSScheduledTask task,
         Exception e, long startTime)
   {
      Throwable cause = e;
      if (e.getCause() != null)
         cause = e.getCause();
      String errorMsg = cause.getLocalizedMessage();
      if (StringUtils.isBlank(errorMsg))
         errorMsg = cause.toString();
      
      return getErrorResult(task, errorMsg, startTime);
   }

   /**
    * Creates a task result for the given failed or skipped task
    * @param task the failed task, assumed not <code>null</code>.
    * @param errorMsg the error message, assumed not <code>null</code> or empty.
    * @param startTime the start time of the task.
    * @return the created task result, never <code>null</code>.
    */
   private static IPSTaskResult getErrorResult(PSScheduledTask task,
         String errorMsg, long startTime)
   {
      return new PSTaskResult(false, errorMsg, PSScheduleUtils.getContextVars(
            task.getParameters(), startTime, System.currentTimeMillis()));
   }
   

   /**
    * Log the result of the task execution.
    * 
    * @param curJob the current job, assumed not <code>null</code>.
    * @param result the result of the execution, it may be <code>null</code>
    *    if fail or skip to execute the task.
    * @param startTime the start time of the execution.
    * @param server the server invoked or skipped the task, assumed not 
    *    <code>null</code>.
    */
   private static void logTaskExecution(PSScheduledTask curJob, 
         IPSTaskResult result, long startTime, String server)
   {
      try
      {
         long endTime = System.currentTimeMillis();
         IPSGuid id = getScheduleService().createTaskLogId();
         boolean wasSuccess = result == null ? false : result.wasSuccess();
         String resultMessage = result != null ? result.getProblemDescription()
               : "";
         PSScheduledTaskLog taskLog = new PSScheduledTaskLog(id, curJob.getId(),
               new Date(startTime), new Date(endTime), wasSuccess, resultMessage
                     , server);

         getScheduleService().saveTaskLog(taskLog);
      }
      catch (Exception e)
      {
         e.printStackTrace();
         ms_log.error("Failed to log task execution", e);
      }
   }
   
   /**
    * Add a new job to the active job's list.
    * @param id the ID of the active job, assumed not <code>null</code>.
    */
   private static synchronized void addJobId(IPSGuid id)
   {
      ms_activeJobs.add(id);
   }
   
   /**
    * Remove a job from the active job's list.
    * @param id the to be removed job ID, assumed not <code>null</code>.
    */
   private static synchronized void removeJobId(IPSGuid id)
   {
      ms_activeJobs.remove(id);
   }
   
   /**
    * Determines if a given job is active (or in the active job's list).
    * @param curId the id of the job in question, assumed not <code>null</code>.
    * @return <code>true</code> if the supplied job is active; otherwise
    *    return <code>false</code>.
    */
   private static synchronized boolean isJobActive(IPSGuid curId)
   {
      for (IPSGuid id : ms_activeJobs)
      {
         if (id.equals(curId))
            return true;
      }
      return false;
   }
   
   /**
    * It contains the IDs of all active jobs, which include scheduled and 
    * none scheduled (manually invoked) jobs. It never <code>null</code>, but
    * may be empty.
    */
   private static List<IPSGuid> ms_activeJobs = new ArrayList<IPSGuid>();
   
}
