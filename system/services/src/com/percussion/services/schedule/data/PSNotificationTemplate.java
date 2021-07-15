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
package com.percussion.services.schedule.data;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.guid.IPSGuid;

import java.util.Comparator;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Template to generate notificaton for the results of the tasks ran by
 * the scheduler.
 * Note, notification templates don't have anything to do with the assembly
 * templates.
 * 
 * @author Andriy Palamarchuk
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "PSNotificationTemplate")
@Table(name = "PSX_SCH_NOTIF_TEMPLATE")
public class PSNotificationTemplate
{
   /**
    * The notification id.
    * @return notification id. Not <code>null</code>.
    */
   public IPSGuid getId()
   {
      return getGuidManager().makeGuid(
            id, PSTypeEnum.SCHEDULE_NOTIFICATION_TEMPLATE);
   }

   /**
    * @param id the new id of type
    * {@link PSTypeEnum#SCHEDULE_NOTIFICATION_TEMPLATE}. Not <code>null</code>.
    * @see #getId()
    */
   public void setId(IPSGuid id)
   {
      if (id == null)
      {
         throw new NullPointerException(
               "Notification template id should not be null");
      }
      if (id.getType() !=
            PSTypeEnum.SCHEDULE_NOTIFICATION_TEMPLATE.getOrdinal())
      {
         throw new IllegalArgumentException(
               "Unexpected schedule template id GUID type " + id.getType());
      }
      this.id = id.longValue();
   }

   /**
    * The human-readable label of the template. Should be unique among
    * all the notification templates.
    * @return the template label. Never <code>null</code> or blank.
    */
   public String getName()
   {
      return name;
   }

   /**
    * @param label the new label value. Not <code>null</code> or blank.
    * @see #getName()
    */
   public void setName(String label)
   {
      if (StringUtils.isBlank(label))
      {
         throw new IllegalArgumentException("Label can't be blank");
      }
      this.name = label;
   }

   /**
    * Get the JEXL expression of the notification subject. This expression
    * can use any of the binding (or context) variables used by the template
    * field {@link #getTemplate()}.
    * 
    * @return the subject of the notification template. It should not be
    *    <code>null</code> unless this object is just created by the default
    *    constructor.
    */
   public String getSubject()
   {
      return this.subject;
   }
   
   /**
    * Set a new subject.
    * @param sub the new subject, not <code>null</code> or empty.
    */
   public void setSubject(String sub)
   {
      if (StringUtils.isBlank(sub))
         throw new IllegalArgumentException("sub may not be null or empty.");
      
      this.subject = sub;
   }
   
   /**
    * The text of the velocity template, which generates the message text.
    * @return notification template. Can be <code>null</code> or blank. 
    */
   public String getTemplate()
   {
      return template;
   }

   /**
    * @param template the new template value. Can be <code>null</code> or blank.
    * @see #getTemplate()
    */
   public void setTemplate(String template)
   {
      this.template = template;
   }

   /**
    * Guid manager.
    * @return the guid manager. Never <code>null</code>.
    */
   private IPSGuidManager getGuidManager()
   {
      return PSGuidManagerLocator.getGuidMgr();
   }
   
   /**
    * Compares notification templates by label. 
    */
   public static class ByLabelComparator
         implements Comparator<PSNotificationTemplate>
   {
      // see base
      public int compare(PSNotificationTemplate t1, PSNotificationTemplate t2)
      {
         return t1.getName().compareTo(t2.getName());
      }
   }

   @Override
   public boolean equals(Object b)
   {
      if (!(b instanceof PSNotificationTemplate))
         return false;
      
      PSNotificationTemplate second = (PSNotificationTemplate) b;
      
      return new EqualsBuilder().append(id, second.id).append(name,
            second.name).append(subject, second.subject).append(template,
            second.template).append(version, second.version).isEquals();
   }

   @Override
   public int hashCode()
   {
      return new HashCodeBuilder().append(id).append(name).append(subject)
            .append(template).append(version).toHashCode();
   }

   /**
    * @see #getId()
    */
   @Id
   private long id;
   
   /**
    * @see #getName()
    */
   @Basic
   private String name;

   /**
    * @see #getSubject()
    */
   @Basic
   private String subject;

   /**
    * @see #getTemplate()
    */
   @Lob
   @Basic(fetch = FetchType.EAGER)
   private String template;

   /**
    * Used by Hibernate for persistence. 
    */
   @Version
   Integer version;
}
