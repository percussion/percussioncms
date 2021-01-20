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
package com.percussion.services.schedule.data;

import static org.apache.commons.lang.Validate.notNull;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.utils.guid.IPSGuid;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * It contains information for executing and notification of a job.
 *
 * @author Andriy Palamarchuk
 */
public class PSJob implements Serializable
{
   /**
    * Creates a non-initialized schedule. 
    * Is required by serialization.
    */
   public PSJob()
   {
      super();
   }

   /**
    * Two schedule are equal only when their ID are equal.
    */
   @Override
   public boolean equals(Object o)
   {
      if (!(o instanceof PSJob))
      {
         return false;
      }
      if (super.equals(o))
      {
         return true;
      }
      final PSJob s = (PSJob) o;
      return getId() != null && s.getId() != null && getId().equals(s.getId());
   }

   @Override
   public int hashCode()
   {
      final int INITIAL_PRIME = 103;
      final int MULTIPLIER_PRIME = 853;

      return new HashCodeBuilder(INITIAL_PRIME, MULTIPLIER_PRIME)
            .append(getId())
            .toHashCode();
   }

   /**
    * Copies data to this schedule from the provided schedule.
    * @param job the schedule to copy data from. Not <code>null</code>.
    */
   public void apply(PSJob job)
   {
      notNull(job);
      
      setId(job.getId());
      setEmailAddresses(job.getEmailAddresses());
      setExtensionName(job.getExtensionName());
      setName(job.getName());
      setNotificationTemplateId(job.getNotificationTemplateId());
      setNotifyWhen(job.getNotifyWhen());
      getParameters().clear();
      getParameters().putAll(job.getParameters());
      setNotify(job.getNotify());
      setServer(job.getServer());
   }

   /**
    * The schedule id.
    * @return the id. Not <code>null</code>  after the schedule is created.
    */
   public IPSGuid getId()
   {
      return m_id;
   }

   /**
    * @param id the id to set schedule to. Can not be <code>null</code>.
    * @see #getId()
    */
   public void setId(IPSGuid id)
   {
      if (id == null)
      {
         throw new NullPointerException("Schedule id should not be null");
      }
      if (id.getType() != PSTypeEnum.SCHEDULED_TASK.getOrdinal())
      {
         throw new IllegalArgumentException(
               "Unexpected schedule id GUID type " + id.getType());
      }
      m_id = id;
   }

   /**
    * Human-readable schedule name.
    * 
    * @return the schedule label. It may be <code>null</code> or empty if it
    *    was created by {@link #PSJob()}, but has not call
    *    {@link #setName(String)} yet.
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * Sets a new label value. 
    * 
    * @param name the new name, never <code>null</code> or empty.
    */
   public void setName(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null.");
      
      m_name = name;
   }

   /**
    * The fully qualified name of the extension to run.
    * @return the extensionName. Not <code>null</code> after creation.
    */
   public String getExtensionName()
   {
      return m_extensionName;
   }

   /**
    * The extension name setter.
    * @see #getExtensionName()
    */
   public void setExtensionName(String extensionName)
   {
      m_extensionName = extensionName;
   }

   /**
    * Parameters to send to the task extension.
    * @return the parameters
    */
   public Map<String, String> getParameters()
   {
      return m_parameters;
   }
   
   /**
    * Sets the parameters.
    * @param params the new set of parameters, never <code>null</code>, but may
    *    by empty.
    */
   public void setParameters(Map<String, String> params)
   {
      if (params == null)
         throw new IllegalArgumentException("params may not be null.");
      
      m_parameters = params;
   }

   /**
    * When should the specified role and cc list be notified of the task's 
    * completion?
    * @return when to notify after a task run. Not <code>null</code>.
    * If the value returned by this field is not {@link PSNotifyWhen#NEVER},
    * the notification template id should be specified. This condition is not
    * enforced programmatically though to eliminate deadlock with the
    * {@link #getNotifyWhen()} restrictions.   
    */
   public PSNotifyWhen getNotifyWhen()
   {
      return m_notifyWhen;
   }

   /**
    * Sets the condition of when to notify about a task result. 
    * @param notifyWhen new value of when to notify. Not <code>null</code>.
    * @see #getNotifyWhen()
    */
   public void setNotifyWhen(PSNotifyWhen notifyWhen)
   {
      if (notifyWhen == null)
      {
         throw new IllegalArgumentException("The new value should not be null");
      }
      m_notifyWhen = notifyWhen;
   }

   /**
    * The schedule notification template id. 
    * @return the schedule template notification id. Can be <code>null</code> if
    * {@link #getNotifyWhen()} is <code>NEVER</code>. Otherwise should not
    * be <code>null</code>. This condition is not enforced by the class.
    */
   public IPSGuid getNotificationTemplateId()
   {
      return m_notificationTemplateId;
   }

   /**
    * Sets new schedule notification template id.
    * @param id the notification id template to set.
    * Can be <code>null</code> only if {@link #getNotifyWhen()} is
    * {@link PSNotifyWhen#NEVER}.
    * @see #getNotificationTemplateId()
    */
   public void setNotificationTemplateId(IPSGuid id)
   {
      if (id == null &&
            !PSNotifyWhen.NEVER.equals(getNotifyWhen()))
      {
         throw new IllegalArgumentException(
               "Null notification template only when notifyWhen is NEVER");
      }
   
      if (id != null)
      {
         final short type = id.getType();
         if (type !=
               PSTypeEnum.SCHEDULE_NOTIFICATION_TEMPLATE.getOrdinal())
         {
            throw new IllegalArgumentException(
                  "Unexpected schedule notification template id GUID type "
                  + id.getType());
         }
      }
      m_notificationTemplateId = id;
   }

   /**
    * The role or user to send email to notify results of running the task.
    * @return the role or user name. Can be empty or <code>null</code>.
    */
   public String getNotify()
   {
      return m_notify;
   }

   /**
    * @param notify the user or role name to notify.
    * Can be empty or <code>null</code>.
    * @see #getNotify()
    */
   public void setNotify(String notify)
   {
      m_notify = notify;
   }

   /**
    * The additional email addresses to send notification to, may be 
    * <code>null</code> or empty.
    * @return the additional email addresses separated by commas.
    */
   public String getEmailAddresses()
   {
      return m_emailList;
   }

   /**
    * @param emailList the new value for the list. Can be <code>null</code> or
    * empty.
    * @see #getEmailAddresses() 
    */
   public void setEmailAddresses(String emailList)
   {
      m_emailList = emailList;
   }

   @Override
   public String toString()
   {
      return "id=" + m_id.toString() + ", label=" + m_name;
   }

   /**
    * Gets the registered server for this task. The task can only be invoked on 
    * registered servers, it will not be invoked on the servers that are not 
    * registered for this task.
    * 
    * @return the host/IP and/or port pair in the format of &lt;host>[:port].
    *    the [:port] port is optional. It may be <code>null</code> or empty
    *    if this task can be invoked by all servers. 
    */
   public String getServer()
   {
      return m_server;
   }
   
   /**
    * Sets the name or IP address of the server.
    * 
    * @param server the new registered server in the format of &lt;host>[:port].
    *    It may be <code>null</code> or empty.
    * 
    * @see #getServer()
    */
   public void setServer(String server)
   {
      m_server = server;
   }
   
   /**
    * The server name or IP address. It may be <code>null</code> or empty.
    * @see #getServer()
    */
   private String m_server;
   
   /**
    * @see #getId()
    */
   private IPSGuid m_id;

   /**
    * Serialized class version number.
    */
   private static final long serialVersionUID = 1L;

   /**
    * @see #getExtensionName()
    */
   private String m_extensionName;

   /**
    * @see #getParameters()
    */
   private Map<String, String> m_parameters = new HashMap<String, String>();

   /**
    * @see #getNotifyWhen()
    */
   private PSNotifyWhen m_notifyWhen = PSNotifyWhen.NEVER;

   /**
    * @see #getNotificationTemplateId()
    */
   private IPSGuid m_notificationTemplateId;

   /**
    * @see #getNotify()
    */
   private String m_notify;

   /**
    * @see #getName()
    */
   private String m_name;

   /**
    * @see #getEmailAddresses()
    */
   private String m_emailList;
}
