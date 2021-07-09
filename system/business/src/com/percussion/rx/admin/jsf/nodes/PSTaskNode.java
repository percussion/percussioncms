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

import static com.percussion.utils.string.PSStringUtils.notBlank;
import static org.apache.commons.lang.Validate.notNull;

import com.percussion.i18n.PSI18nUtils;
import com.percussion.rx.jsf.PSEditableNode;
import com.percussion.rx.publisher.jsf.data.PSParameter;
import com.percussion.rx.publisher.jsf.utils.PSExtensionHelper;
import com.percussion.security.PSRoleManager;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.schedule.IPSSchedulingService;
import com.percussion.services.schedule.PSSchedulingException;
import com.percussion.services.schedule.PSSchedulingServiceLocator;
import com.percussion.services.schedule.data.PSNotificationTemplate;
import com.percussion.services.schedule.data.PSNotifyWhen;
import com.percussion.services.schedule.data.PSScheduledTask;
import com.percussion.services.schedule.data.PSNotificationTemplate.ByLabelComparator;
import com.percussion.utils.guid.IPSGuid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The backing bean for a scheduled task.
 * 
 * @author Andriy Palamarchuk
 */
public class PSTaskNode extends PSEditableNode
{
   /**
    * Creates new node for the specified notification template.
    * @param event the notification template.
    * Not <code>null</code>.
    */
   public PSTaskNode(PSScheduledTask event)
   {
      super(getEventLabel(event), event.getId());
   }

   // see base
   @Override
   public String copy() throws PSNotFoundException {
      final PSScheduledTask copy = getSchedulingService().createSchedule();
      final IPSGuid id = copy.getId();
      copy.apply(getEvent());
      copy.setId(id);
      final String label =
            getContainer().getUniqueName(getEvent().getName(), true);
      copy.setName(label);
      return ((PSTaskContainerNode) getContainer()).initNewEvent(copy);
   }

   /**
    * Determines if there is any invalid input fields.
    * @return <code>true</code> all input fields are valid; otherwise return
    *    <code>false</code>.
    */
   private boolean isValidSchedule()
   {
      return StringUtils.isBlank(getNotifyWarning()) &&
            StringUtils.isBlank(getNotifyTemplateWarning());
   }

   /**
    * Set the {@link #m_event} from the input fields
    */
   private void setEventFromInput()
   {
      Map<String, String> params = new HashMap<>();
      for (PSParameter p : m_params)
      {
         params.put(p.getName(), p.getValue());
      }
      m_event.setParameters(params);
      
      // save notification template ID
      if (m_notifyTemplateId.equals(NOTIFY_TEMPLATE_NULL_ID))
      {
         m_event.setNotificationTemplateId(null);
      }
      else
      {
         IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
         IPSGuid id = gmgr.makeGuid(m_notifyTemplateId,
               PSTypeEnum.SCHEDULE_NOTIFICATION_TEMPLATE);
         m_event.setNotificationTemplateId(id);
      }
   }
   
   /**
    * Saves the changes to the node.
    * @return <code>true</code> if successfully saved the data.
    */
   private boolean doSave()
   {
      if (! isValidSchedule())
         return false;
      
      setEventFromInput();

      try
      {
         getSchedulingService().saveSchedule(getEvent());
      }
      catch (Exception e)
      {
         // yes, we catch Exception here, because some Quartz errors are
         // reported in runtime exceptions, for example the unsupported Cron
         // expression format is reported with UnsupportedOperationException
         ms_log.warn(
               "Failure to save the schedule " + getEvent().getName(), e);
         FacesContext.getCurrentInstance().addMessage(null,
               new FacesMessage(
                     FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage(),
                     null));
         return false;
      }
      
      return true;
   }

   /**
    * Save current data, then execute the specified task now.
    */
   public void runNow()
   {
      if (! isValidSchedule())
         return;
      
      setEventFromInput();
      try
      {
         getSchedulingService().runNow(getEvent());
      }
      catch (Exception e)
      {
         ms_log.warn(
               "Failure to execute the schedule " + getEvent().getName(), e);         
      }
   }
   
   /**
    * Saves the changes to the node.
    * @return the "save" string. Never <code>null</code>.
    */
   public String save()
   {
      if (!doSave())
         return null;

      setTitle(m_event.getName()); // update the tree node
      clearData();
      
      return gotoParentNode();
   }

   // see base
   @Override
   public String delete()
   {
      try
      {
         getSchedulingService().deleteSchedule(m_event.getId());
         remove();
      }
      catch (PSSchedulingException e)
      {
         ms_log.error("Problem deleting a schedule " + m_event.getName(), e);
      }
      return navigateToList();
   }

   /**
    * Cancel the edit mode and discard all changes. 
    * @return the outcome to its parent node, never <code>null</code> or empty.
    */
   @Override
   public String cancel() 
   {
      clearData();
      
      return gotoParentNode();
   }

   /**
    * Clear current data, it forces reloading the event object when the node
    * is in edit mode next time around.
    */
   private void clearData()
   {
      m_event = null;
      m_notifyTemplateId = null;
      m_params.clear();
   }
   
   // see base
   @Override
   public String navigateToList()
   {
      return getCategoryKey();
   }

   // see base
   @Override
   protected String getCategoryKey()
   {
      return "admin-timed-events";
   }

   // see base
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
    * to request the label from.
    * If <code>null</code> the method throws
    * <code>IllegalArgumentException</code>.
    * @return the event label. Never <code>null</code> or blank.
    */
   private static String getEventLabel(PSScheduledTask event)
   {
      notNull(event);
      return event.getName();
   }

   /**
    * The notification template name.
    * @return the notification template name. Not <code>null</code> or empty.
    */
   public String getName()
   {
      return getEvent().getName();
   }
   
   /**
    * @param name the new name. Not <code>null</code> or blank.
    * @see #getName()
    */
   public void setName(String name)
   {
      notBlank(name);
      getEvent().setName(name);
   }

   /**
    * The timed event this node corresponds to. It will reload the schedule
    * object if not load yet.
    * 
    * @return the event provided in the constructor. Never <code>null</code>.
    */
   public PSScheduledTask getEvent()
   {
      if (m_event == null)
      {
         try
         {
            m_event = getSchedulingService().findScheduledTaskById(getGUID());
         }
         catch (PSSchedulingException e)
         {
            // should never be here
            e.printStackTrace();
            ms_log.error("Couldn't load schedule id=" + getGUID().toString(), e);
            throw new RuntimeException("Unknown exception", e);
         }
      }
      return m_event;
   }
   
   /** 
    * The choices for {@link PSNotifyWhen} to show in the editor dropdown.
    * @return the choices. Never <code>null</code> or empty.
    */
   public List<SelectItem> getNotifyWhenChoices()
   {
      final List<SelectItem> choices = new ArrayList<>();
      for (PSNotifyWhen when : PSNotifyWhen.values())
      {
         choices.add(new SelectItem(when, when.getLabel()));
      }
      return choices;
   }
   
   /**
    * The choices for the "Notify" field to show in the editor dropdown.
    * @return the choices - names for roles and subjects.
    * Never <code>null</code> or empty.
    */
   @SuppressWarnings({"unchecked"})
   public List<SelectItem> getNotifyRowChoices()
   {
      final List<SelectItem> choices = new ArrayList<>();
      choices.add(new SelectItem("", ""));
      final List<String> roles = getRoleManager().getRoles();
      for (final String role : roles)
      {
         choices.add(new SelectItem(role, role));
      }
      return choices;
   }

   /**
    * Set the notification template ID.
    * @param id the new ID, never <code>null</code>.
    */
   public void setNotifyTemplate(String id)
   {
      if (id == null)
         throw new IllegalArgumentException("id cannot be null.");
      
      m_notifyTemplateId = id;
   }
   
   /**
    * @return the notification template ID
    */
   public String getNotifyTemplate()
   {
      if (m_notifyTemplateId == null)
      {
         if (getEvent().getNotificationTemplateId() == null)
            m_notifyTemplateId = NOTIFY_TEMPLATE_NULL_ID;
         else
            m_notifyTemplateId = getEvent().getNotificationTemplateId()
                  .toString();
      }
      return m_notifyTemplateId;
   }
   
   /**
    * @return a list of all notification templates, never <code>null</code>, 
    *    but may be empty.
    */
   public List<SelectItem> getNotificationTemplateChoices()
   {
      final List<SelectItem> choices = new ArrayList<>();
      List<PSNotificationTemplate> ntList = new ArrayList<>(
            getSchedulingService().findAllNotificationTemplates());
      Collections.sort(ntList, new ByLabelComparator());
      
      choices.add(new SelectItem(NOTIFY_TEMPLATE_NULL_ID, ""));
      for (final PSNotificationTemplate nt : ntList)
      {
         choices.add(new SelectItem(nt.getId().toString(), nt.getName()));
      }
      return choices;      
   }
   
   /**
    * @return the name of the extension
    */
   public String getExtensionName()
   {
      return getEvent().getExtensionName();
   }
   
   /**
    * Set the name of the extension for this task.
    * @param name the new extension name. It is the fully qualified name of the
    *    extension, never <code>null</code> or empty. 
    */
   public void setExtensionName(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty.");
      
      getEvent().setExtensionName(name);
      setupParameters();
   }
   
   /**
    * @return selection items corresponding to the possible extension tasks
    *         registered to the system.
    */
   @SuppressWarnings("unchecked")
   public List<SelectItem> getTaskExtensionChoices()
   {
      return PSExtensionHelper
            .getTaskExtensionChoices("com.percussion.services.schedule.IPSTask");
   }

   /**
    * Lookup the extension name and set the set of exposed names, used to
    * filter the parameters. Then populate and/or extend the list of
    * parameters.
    */
   private void setupParameters()
   {
      m_params = PSExtensionHelper.setupParameters(getEvent().getExtensionName(),
            getEvent().getParameters(), m_params);
   }

   /**
    * @return the complete parameters for the given extension, never
    *         <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   public List<PSParameter> getParams()
   {
      if ((!StringUtils.isBlank(getEvent().getExtensionName()))
            && m_params.isEmpty())
      {
         setupParameters();
      }
            
      return m_params;
   }

   /**
    * This warning message is used to simulate trinidad's warning message.
    * The message should not be displayed/rendered when the notify entry can be
    * empty; otherwise the warning message should be rendered when notify entry
    * is required (cannot be empty).
    *   
    * @return the warning message for the notify entry. It can never be 
    *    <code>null</code>, but is empty if the notify entry is not required; 
    *    otherwise it is none empty string. 
    */
   public String getNotifyWarning()
   {
      if (isNotifyRequired())
      {
         if ( StringUtils.isBlank(getEvent().getNotify()) && 
               StringUtils.isBlank(getEvent().getEmailAddresses()) )
         {
            return PSI18nUtils.getString("jsf@notify_is_required");
         }
      }

      return "";
   }

   /**
    * @return none empty string if the Notification Template entry is required
    *    but it is empty; otherwise return an empty string.
    */
   public String getNotifyTemplateWarning()
   {
      if (isNotifyRequired() && StringUtils.isBlank(getNotifyTemplate()))
      {
         return PSI18nUtils.getString("jsf@select_a_value");
      }

      return "";      
   }
   
   @Override
   public String getHelpTopic()
   {
      return "ScheduledTask";
   }

   /**
    * Determines if the notification is required.
    * 
    * @return <code>true</code> if notification is required.
    */
   private boolean isNotifyRequired()
   {
      String notifyLabel = getEvent().getNotifyWhen().getLabel();
      boolean isNever = PSNotifyWhen.NEVER.getLabel().equalsIgnoreCase(notifyLabel);
      
      return !isNever;
   }
   
   /**
    * Holds the parameters while the task is being edited.
    */
   private List<PSParameter> m_params = new ArrayList<>();

   /**
    * A convenience method to access the role manager.
    * @return the role manager. Never <code>null</code>.
    */
   private PSRoleManager getRoleManager()
   {
      return PSRoleManager.getInstance();
   }

   /**
    * This is the wrapper of the .
    * The purpose of this is to be able to set <code>null</code> to the 
    * notification template ID of the event object. 
    * 
    * Default to <code>null</code>, which will force reset the value when it is
    * needed in edit mode.
    */
   private String m_notifyTemplateId = null;

   /**
    * @see #getEvent()
    * Default to <code>null</code> as it will be reload when it is needed in 
    * edit mode.
    */
   private PSScheduledTask m_event = null;

   /**
    * This is used in the choice list of notification templates. It represents
    * the empty entry of the choices. 
    */
   private static String NOTIFY_TEMPLATE_NULL_ID = "";

   /**
    * The logger for this class.
    */
   private static final Logger ms_log = LogManager.getLogger(PSTaskNode.class);
}
