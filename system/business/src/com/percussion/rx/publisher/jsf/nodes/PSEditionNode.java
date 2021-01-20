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
package com.percussion.rx.publisher.jsf.nodes;

import com.percussion.rx.jsf.PSCategoryNodeBase;
import com.percussion.rx.jsf.PSNavigation;
import com.percussion.rx.jsf.PSNodeBase;
import com.percussion.rx.publisher.jsf.data.PSEditionContentListWrapper;
import com.percussion.rx.publisher.jsf.data.PSParameter;
import com.percussion.rx.publisher.jsf.utils.PSExtensionHelper;
import com.percussion.rx.ui.jsf.beans.PSHelpTopicMapping;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.content.IPSContentService;
import com.percussion.services.content.PSContentServiceLocator;
import com.percussion.services.content.data.PSKeyword;
import com.percussion.services.guidmgr.PSGuidHelper;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.publisher.IPSContentList;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.publisher.IPSEditionContentList;
import com.percussion.services.publisher.IPSEditionTaskDef;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherException;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.services.publisher.data.PSEdition;
import com.percussion.services.publisher.data.PSEditionContentList;
import com.percussion.services.publisher.data.PSEditionContentListPK;
import com.percussion.services.publisher.data.PSEditionType;
import com.percussion.services.sitemgr.IPSPublishingContext;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.utils.guid.IPSGuid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.trinidad.component.UIXEditableValue;

/**
 * This node represents an edition in the navigation tree.
 * 
 * @author dougrand
 */
public class PSEditionNode extends PSDesignNode
{
   /**
    * The outcome to forward to the associate content list.
    */
   private static final String ASSOCIATE_CONTENT_LIST_OUTCOME =
         "associate-content-list";

   /**
    * Logger.
    */
   private static Log ms_log = LogFactory.getLog(PSEditionNode.class);

   /**
    * Content List wrapper is used to display the available lists while 
    * selecting the associated Content Lists to current Edition.
    */
   public class CListWrapper
   {
      IPSContentList mi_clist = null;
      boolean mi_selected = false;

      /**
       * Constructs a wrapper with an Content List.
       * 
       * @param clist the Content List, never <code>null</code>.
       */
      public CListWrapper(IPSContentList clist)
      {
         if (clist == null)
            throw new IllegalArgumentException("clist may not be null.");
         
         mi_clist = clist;
      }
      
      /**
       * Determines if this Content List is selected.
       * 
       * @return <code>true</code> if it is selected.
       */
      public boolean getSelected()
      {
         return mi_selected;
      }
      
      /**
       * Sets the select status for the current Content List.
       * @param selected
       */
      public void setSelected(boolean selected)
      {
         mi_selected = selected;
      }
      
      /**
       * Gets the wrapped Content List.
       * 
       * @return the wrapped Content List, never <code>null</code>.
       */
      public IPSContentList getClist()
      {
         return mi_clist;
      }
   }
   
   /**
    * In order to handle the parameters for tasks well, this object acts as a
    * buffer that always presents all the possible parameters as defined by the
    * extension manager.
    */
   public class TaskEditItem
   {
      /**
       * The encapsulated task, never <code>null</code> after construction.
       */
      private IPSEditionTaskDef mi_task;

      /**
       * Holds the parameters while the task is being edited.
       */
      List<PSParameter> m_params = new ArrayList<PSParameter>();

      /**
       * Is this task selected for an action?
       */
      private boolean mi_selected;

      /**
       * Ctor.
       * 
       * @param task the task, assumed never <code>null</code>.
       */
      public TaskEditItem(IPSEditionTaskDef task) {
         mi_task = task;
         setupParameters();
      }

      /**
       * @return the task
       */
      public IPSEditionTaskDef getTask()
      {
         return mi_task;
      }

      /**
       * @param task the task to set
       */
      public void setTask(IPSEditionTaskDef task)
      {
         mi_task = task;
         setupParameters();
      }

      /**
       * @return the extension name
       */
      public String getExtensionName()
      {
         return mi_task.getExtensionName();
      }

      /**
       * Set the extension name and fix the cached parameters.
       * 
       * @param name the new extension name, never <code>null</code> or empty.
       */
      public void setExtensionName(String name)
      {
         if (StringUtils.isBlank(name))
         {
            throw new IllegalArgumentException("name may not be null or empty");
         }
         mi_task.setExtensionName(name);
         setupParameters();
      }

      /**
       * Lookup the extension name and set the set of exposed names, used to
       * filter the parameters. Then populate and/or extend the list of
       * parameters.
       */
      private void setupParameters()
      {
         m_params = PSExtensionHelper.setupParameters(mi_task.getExtensionName(),
               mi_task.getParams(), m_params);
      }

      /**
       * @return the complete parameters for the given extension, never
       *         <code>null</code>.
       */
      @SuppressWarnings("unchecked")
      public List<PSParameter> getParams()
      {
         return m_params;
      }

      /**
       * Set new params.
       * 
       * @param params the new param values, never <code>null</code>.
       */
      public void setParams(List<PSParameter> params)
      {
         if (params == null)
         {
            throw new IllegalArgumentException("params may not be null");
         }
         m_params = params;
      }

      /**
       * Save parameters back to the source object before saving the object to
       * the database.
       */
      public void save()
      {
         for (PSParameter p : m_params)
         {
            mi_task.setParam(p.getName(), p.getValue());
         }
      }

      /**
       * @return the selected
       */
      public boolean getSelected()
      {
         return mi_selected;
      }

      /**
       * @param selected the selected to set
       */
      public void setSelected(boolean selected)
      {
         mi_selected = selected;
      }
   }

   /**
    * The edition, set when we're editing, <code>null</code> otherwise.
    */
   private IPSEdition m_edition = null;

   /**
    * The edition content lists, set when we're editing and <code>null</code>
    * otherwise.
    */
   private List<PSEditionContentListWrapper> m_eclists = null;

   /**
    * Edition content lists to be removed on save.
    */
   private List<PSEditionContentListWrapper> m_deletedEclists =
         new ArrayList<PSEditionContentListWrapper>();

   /**
    * The current pre-tasks for the edition, extracted on first access.
    */
   private List<TaskEditItem> m_preTasks = null;

   /**
    * The current post-tasks for the edition, extracted on first access.
    */
   private List<TaskEditItem> m_postTasks = null;


   /**
    * The tasks that will be deleted when the edition is saved.
    */
   private List<IPSEditionTaskDef> m_removedTasks
         = new ArrayList<IPSEditionTaskDef>();

   /**
    * Ctor.
    * 
    * @param edition the edition, never <code>null</code>.
    */
   public PSEditionNode(IPSEdition edition) {
      super(edition.getDisplayTitle(), edition.getGUID());
      calculateProperties(edition);
      m_edition = null;
   }

   /**
    * Calculate the properties, called by the ctor and the save methods.
    * 
    * @param edition the edition, assumed never <code>null</code>.
    */
   private void calculateProperties(IPSEdition edition)
   {
      getProperties().put("description", edition.getComment());
      getProperties().put("type", edition.getEditionType().getDisplayTitle());
   }

   /**
    * Make sure the edition is loaded before using values from the accessors.
    * Effectively lazy loads the edition on first access in the editor.
    */
   private void assureLoaded()
   {
      if (m_edition == null)
      {
         IPSPublisherService psvc = PSPublisherServiceLocator
         .getPublisherService();
         m_eclists = new ArrayList<PSEditionContentListWrapper>();
         m_edition = psvc.loadEditionModifiable(getGUID());
         List<IPSEditionContentList> associations = psvc
               .loadEditionContentLists(getGUID());
         for (IPSEditionContentList association : associations)
         {
            m_eclists.add(new PSEditionContentListWrapper(association,
                  m_edition.getSiteId()));
         }
         Collections.sort(m_eclists);
      }
   }

   /**
    * @return the edition name
    */
   public String getName()
   {
      assureLoaded();
      return m_edition.getDisplayTitle();
   }

   /**
    * Gets the edition embedded in this node.
    * 
    * @return the edition, never <code>null</code>. 
    */
   public IPSEdition getEdition()
   {
      assureLoaded();
      return m_edition;
   }
   
   /**
    * @param name the new value
    */
   public void setName(String name)
   {
      m_edition.setDisplayTitle(name);
   }

   /**
    * @return the edition comment or description
    */
   public String getDescription()
   {
      assureLoaded();
      return m_edition.getComment();
   }

   /**
    * @param description the new value
    */
   public void setDescription(String description)
   {
      m_edition.setComment(description);
   }

   /**
    * @return get the priority
    */
   public String getPriority()
   {
      assureLoaded();
      return Integer.toString(m_edition.getPriority().getValue());
   }

   /**
    * @param priority the new value for priority
    */
   public void setPriority(String priority)
   {
      if (StringUtils.isBlank(priority))
         throw new IllegalArgumentException("priority may not be null or empty.");

      int pvalue = Integer.parseInt(priority);
      ((PSEdition)m_edition).setPriorityInt(pvalue);
   }

   /**
    * @return the edition type, never <code>null</code>.
    */
   public Integer getEditionType()
   {
      return m_edition.getEditionType().getTypeId();
   }

   /**
    * @param type a new edition type, it is assumed to correspond to one of the
    *            valid values or it is ignored.
    */
   public void setEditionType(Integer type)
   {
      for (PSEditionType val : PSEditionType.values())
      {
         if (val.getTypeId() == type)
         {
            m_edition.setEditionType(val);
            break;
         }
      }
   }

   /**
    * Get the content list information.
    * 
    * @return a list of wrappers, never <code>null</code>.
    * @throws PSPublisherException
    */
   @SuppressWarnings("unused")
   public List<PSEditionContentListWrapper> getContentLists()
         throws PSPublisherException
   {
      assureLoaded();
      return m_eclists;
   }

   /**
    * Move the selected content-list or edition-task up in the list.
    * 
    * @return the outcome, <code>null</code> since we aren't navigating on
    *         this action.
    */
   public String moveSelectedUp()
   {
      if (moveSelectedContentListUp())
         return null;
      
      if (moveSelectedTaskUp(m_preTasks))
         return null;
      
      if (moveSelectedTaskUp(m_postTasks))
         return null;
      
      return PSNavigation.NONE_SELECT_WARNING;
   }

   /**
    * Move the selected content-list up in the list.
    * 
    * @return <code>true</code> if moved a ContentList up; otherwise return
    *    <code>false</code> if there is no selected ContentList.
    */
   private boolean moveSelectedContentListUp()
   {
      Integer sel = findSelectedCL();
      if (sel == null)
         return false;

      if (sel != null && sel > 0)
      {
         PSEditionContentListWrapper list = m_eclists.get(sel);
         m_eclists.remove(sel.intValue());
         m_eclists.add(sel.intValue() - 1, list);
         int seq = 1;
         for (PSEditionContentListWrapper l : m_eclists)
         {
            l.setSequence(seq++);
         }
      }
      return true;      
   }

   /**
    * Finds a selected task from the given task list.
    * @param tasks the task list in question, assumed not <code>null</code>.
    * @return the index of the selected task; it may be <code>-1</code> if 
    *    there is no selected task from the task list.
    */
   private int findSelectedTask(List<TaskEditItem> tasks)
   {
      int i = 0;
      for (TaskEditItem t : tasks)
      {
         if (t.mi_selected)
            return i;
         i++;
      }
      return -1;
   }

   /**
    * Move the selected task up from the given task list.
    * 
    * @param tasks the task list in question, assumed not <code>null</code>.
    * 
    * @return <code>true</code> if moved up a selected Content List; otherwise
    *    return <code>false</code> if there is no selected Content List.
    */
   private boolean moveSelectedTaskUp(List<TaskEditItem> tasks)
   {
      int i = findSelectedTask(tasks);
      if (i == -1)
         return false;
      
      if (i > 0)
      {
         TaskEditItem task = tasks.remove(i);
         tasks.add(i-1, task);
      }
      return true; 
   }

   /**
    * Move the selected task down from the given task list.
    * 
    * @param tasks the task list in question, assumed not <code>null</code>.
    * 
    * @return <code>true</code> if moved down a selected Content List; otherwise
    *    return <code>false</code> if there is no selected Content List.
    */
   private boolean moveSelectedTaskDown(List<TaskEditItem> tasks)
   {
      int i = findSelectedTask(tasks);
      if (i == -1)
         return false;
      
      if (i < tasks.size() -1)
      {
         TaskEditItem task = tasks.remove(i);
         tasks.add(i+1, task);
      }
      return true; 
   }
   

   /**
    * Move the selected content-list or edition-task down in the list.
    * 
    * @return the outcome, <code>null</code> since we aren't navigating on
    *         this action.
    */
   public String moveSelectedDown()
   {
      if (moveSelectedContentListDown())
         return null;
      
      if (moveSelectedTaskDown(m_preTasks))
         return null;
      
      if (moveSelectedTaskDown(m_postTasks))
         return null;
      
      return PSNavigation.NONE_SELECT_WARNING;
   }

   /**
    * Move the selected content-list down in the list.
    * 
    * @return <code>true</code> if moved down a selected Content List; otherwise
    *    return <code>false</code> if there is no selected Content List.
    */
   public boolean moveSelectedContentListDown()
   {
      Integer sel = findSelectedCL();
      if (sel == null)
         return false;
      
      if (sel != null && sel < (m_eclists.size() - 1))
      {
         PSEditionContentListWrapper list = m_eclists.get(sel);
         m_eclists.remove(sel.intValue());
         m_eclists.add(sel.intValue() + 1, list);
         int seq = 1;
         for (PSEditionContentListWrapper l : m_eclists)
         {
            l.setSequence(seq++);
         }
      }
      return true;
   }

   /**
    * Remove the selected content-list or edition-task.
    * 
    * @return the outcome, <code>null</code> since we aren't navigating on
    *         this action.
    */
   public String removeSelected()
   {
      Integer selIndex = findSelectedCL();
      if (removeContentList(selIndex))
         return null;
      
      if (removeSelectedTask())
         return null;
      
      return PSNavigation.NONE_SELECT_WARNING;
   }

   /**
    * Removes a selected pre/post task.
    * @return <code>true</code> if removed a selected task; otherwise return
    *    <code>false</code>.
    */
   private boolean removeSelectedTask()
   {
      if (removeTask(m_preTasks))
         return true;
      
      if (removeTask(m_postTasks))
         return true;
      
      return false;
   }

   /**
    * Removes a selected Content List.
    * @return <code>true</code> if removed a selected Content List; otherwise
    *    return <code>false</code> if there is no Content List selected.
    */
   private boolean removeContentList(Integer selIndex)
   {
      if (selIndex == null)
         return false;
         
      m_deletedEclists.add(m_eclists.get(selIndex));
      m_eclists.remove(selIndex.intValue());
      return true;
   }
   
   /**
    * Look through the current edition content lists and return the correct
    * selected list.
    * 
    * @return the selected list's index or <code>null</code> if nothing is
    *         selected.
    */
   private Integer findSelectedCL()
   {
      assureLoaded();
      
      Integer index = 0;
      
      for (PSEditionContentListWrapper list : m_eclists)
      {
         if (list.getSelected())
         {
            return index;
         }
         index++;
      }
      return null;
   }

   /**
    * Cancel the editor and navigate back to the list view.
    * 
    * @return the outcome
    */
   @Override
   public String cancel()
   {
      m_edition = null;
      m_eclists = null;
      m_preTasks = null;
      m_postTasks = null;
      return gotoParentNode();
   }

   /**
    * Save the m_edition and navigate back to the list view.
    * 
    * @return the outcome
    * @throws Exception
    */
   public String save() throws Exception
   {
      if (m_edition == null)
      {
         throw new IllegalStateException("Cannot save edition before loading");
      }
      IPSPublisherService psvc = PSPublisherServiceLocator
            .getPublisherService();
      psvc.saveEdition(m_edition);
      
      // Save ContentList
      int i = 0;
      if (m_eclists != null)
      {
         for (PSEditionContentListWrapper wrapper : m_eclists)
         {
            wrapper.getEclist().setSequence(i++);
            psvc.saveEditionContentList(wrapper.getEclist());
         }
      }
      // Remove ContentList
      for (PSEditionContentListWrapper wrapper : m_deletedEclists)
      {
         psvc.deleteEditionContentList(wrapper.getEclist());
      }
      
      // Save tasks
      i = -1 * m_preTasks.size();
      for (TaskEditItem t : m_preTasks)
      {
         t.mi_task.setSequence(i++);
         t.save();
         psvc.saveEditionTask(t.getTask());
      }
      i = 0;
      for (TaskEditItem t : m_postTasks)
      {
         t.mi_task.setSequence(i++);
         t.save();
         psvc.saveEditionTask(t.getTask());
      }
      // Remove tasks that have been deleted
      for (IPSEditionTaskDef task : m_removedTasks)
      {
         if (task.getVersion() != null)
            psvc.deleteEditionTask(task);
      }
      calculateProperties(m_edition);
      setTitle(m_edition.getDisplayTitle());
      return cancel();
   }

   /**
    * Handle the details of adding a new edition.
    * 
    * @param parent the parent that will contain the node, never
    *            <code>null</code>.
    * @param ed the to be saved edition, never <code>null</code>
    * @param node the edition node, never <code>null</code>
    * @param eclists the to be cloned content list for the new edition.
    *       It may be <code>null</code> if there is no content list
    *       for the new edition.
    * 
    * @return the outcome, never <code>null</code> or empty.
    */
   public String handleNewEdition(PSCategoryNodeBase parent, IPSEdition ed,
         PSEditionNode node, List<PSEditionContentListWrapper> eclists)
   {
      if (parent == null)
      {
         throw new IllegalArgumentException("parent may not be null");
      }
      if (ed == null)
      {
         throw new IllegalArgumentException("ed may not be null");
      }
      if (node == null)
      {
         throw new IllegalArgumentException("node may not be null");
      }

      IPSPublisherService psvc =
            PSPublisherServiceLocator.getPublisherService();

      psvc.saveEdition(ed);
      if (eclists != null)
      {
         IPSEditionContentList eclist;
         for (PSEditionContentListWrapper wrapper : eclists)
         {
            eclist = psvc.createEditionContentList();
            PSEditionContentListPK eclPK = 
               ((PSEditionContentList) eclist).getEditionContentListPK();
            eclPK.setEditionid(ed.getGUID().longValue());
            eclPK.setContentlistid(
                  wrapper.getEclist().getContentListId().longValue());
            eclist.copy(wrapper.getEclist());

            psvc.saveEditionContentList(eclist);
         }
      }
      return node.editNewNode(parent, node);
   }

   /**
    * Removes a selected task from the given task list.
    * @param tasks the task list in question, assumed not <code>null</code>.
    */
   private boolean removeTask(List<TaskEditItem> tasks)
   {
      Iterator<TaskEditItem> iter = tasks.iterator();
      while (iter.hasNext())
      {
         TaskEditItem item = iter.next();
         if (item.getSelected())
         {
            m_removedTasks.add(item.getTask());
            iter.remove();
            return true;
         }
      }
      return false;
   }


   /**
    * Add a pre task.
    */
   public void addPreTask()
   {
      getTasks();
      addTask(m_preTasks);
   }
   
   /**
    * Add a post task.
    */
   public void addPostTask()
   {
      getTasks();
      addTask(m_postTasks);
   }
   
   /**
    * Add a created Task to the given task list.
    * @param tasks the task list, assumed not <code>null</code>.
    */
   private void addTask(List<TaskEditItem> tasks)
   {
      IPSPublisherService psvc = PSPublisherServiceLocator
            .getPublisherService();
      IPSEditionTaskDef data = psvc.createEditionTask();
      data.setEditionId(m_edition.getGUID());
      // Set sequence to max
      int max = 0;
      for (TaskEditItem i : tasks)
      {
         Integer seq = i.getTask().getSequence();
         max = Math.max(max, seq != null ? seq : 0);
      }
      data.setSequence(max + 1);
      tasks.add(new TaskEditItem(data));
   }

   /**
    * @return selection items corresponding to the possible extension tasks
    *         registered to the system.
    */
   @SuppressWarnings("unchecked")
   public List<SelectItem> getTaskExtensionChoices()
   {
      return PSExtensionHelper
            .getTaskExtensionChoices("com.percussion.rx.publisher.IPSEditionTask");
   }

   /**
    * Get the tasks associated with the edition in an editable form.
    */
   private synchronized void getTasks()
   {
      IPSPublisherService psvc = PSPublisherServiceLocator
            .getPublisherService();
      if (m_preTasks == null || m_postTasks == null)
      {
         List<IPSEditionTaskDef> editionTasks = psvc
               .loadEditionTasks(getGUID());
         m_preTasks = new ArrayList<TaskEditItem>();
         m_postTasks = new ArrayList<TaskEditItem>();
         for (IPSEditionTaskDef t : editionTasks)
         {
            if (t.getSequence() < 0)
               m_preTasks.add(new TaskEditItem(t));
            else
               m_postTasks.add(new TaskEditItem(t));
         }
      }
   }

   /**
    * Get pre-tasks for this Edition.
    * 
    * @return the list of pre-tasks, never <code>null</code>, but may be
    * empty.
    */
   public List<TaskEditItem> getPreTasks()
   {
      getTasks();
      return m_preTasks;
   }

   /**
    * Get post-tasks for this Edition.
    * @return the list of post-tasks, never <code>null</code>, but may be empty.
    */
   public List<TaskEditItem> getPostTasks()
   {
      getTasks();
      return m_postTasks;
   }
   
   /**
    * Is there any pre-tasks for this Edition.
    * @return <code>true</code> if there are any tasks.
    */
   public boolean getHasPreTasks()
   {
      return getPreTasks().size() > 0;
   }

   /**
    * Is there any post-tasks for this Edition.
    * @return <code>true</code> if there are any tasks.
    */
   public boolean getHasPostTasks()
   {
      return getPostTasks().size() > 0;
   }

   @Override
   public String delete()
   {
      assureLoaded();
      IPSPublisherService psvc = PSPublisherServiceLocator
            .getPublisherService();
      psvc.deleteEdition(m_edition);
      remove();
      return navigateToList();
   }

   /**
    * Copy the edition.
    * 
    * @return the outcome, may be <code>null</code> if no node is selected
    */
   @Override
   public String copy()
   {
      assureLoaded();
      IPSEdition copiedEdition = new PSEdition();
      copiedEdition.copy(m_edition);
      ((PSEdition) copiedEdition).setGUID(PSGuidHelper.generateNext(
            PSTypeEnum.EDITION));
      IPSSite site = ((PSEditionContainerNode) getParent()).getSiteParent();
      copiedEdition.setSiteId(site.getGUID());
      String name = getContainer().getUniqueName(copiedEdition.getName(), true);
      copiedEdition.setName(name);
      
      PSEditionNode node = new PSEditionNode(copiedEdition);

      return handleNewEdition((PSCategoryNodeBase) getParent(), copiedEdition,
            node, m_eclists);
   }

   @Override
   public String navigateToList()
   {
      return PSEditionContainerNode.EDITION_VIEWS;
   }

   public EditionCListAssociation getEclist()
   {
      return m_ecList;
   }
   
   private EditionCListAssociation m_ecList = new EditionCListAssociation(); 

   /**
    * Add content list action.
    * 
    * @return the outcome
    */
   public String addContentList()
   {
      m_ecList.reset();
      m_ecList.mi_isNew = true;

      return ASSOCIATE_CONTENT_LIST_OUTCOME;
   }

   /**
    * Edit content list action.
    * 
    * @return the outcome
    */
   public String editContentList()
   {
      Integer index = findSelectedCL();
      if (index == null)
         return PSNavigation.NONE_SELECT_WARNING;
      
      if (!m_ecList.setLocalVariables(index))
         return null;
      
      return ASSOCIATE_CONTENT_LIST_OUTCOME;
   }
   

   /**
    * It manages the association between Edition and ContentList. 
    */
   public class EditionCListAssociation
   {
      /**
       * The name of the default delivery context.
       */
      private final static String DEFAULT_DELIVERY_CTX = "Publish";
      
      /**
       * If this is a new content list association this field will be set to
       * <code>true</code>, <code>false</code> when editing an existing
       * association.
       */
      private boolean mi_isNew;
      
      /**
       * The current Content List, which is <code>null</code> for adding
       * a new association, not <code>null</code> for editing one.
       */
      private IPSContentList mi_srcCList = null;

      /**
       * If set, this string should filter the content lists we can select from in
       * the association page.
       */
      private String mi_contentListFilter = null;

      /**
       * Should the displayed lists in the association page be limited to those
       * currently connected to the site? Defaults to <code>true</code>.
       */
      private boolean mi_limitContentListsToSite = true;

      /**
       * What assembly context to use in the association?
       */
      private Integer mi_assemblyContext = null;

      /**
       * What delivery context to use in the association?
       */
      private Integer mi_deliveryContext = null;

      /**
       * What authtype to use in the association?
       */
      private String mi_authtype = null;

      /**
       * The current filtered and sorted list.
       */
      private List<CListWrapper> mi_clists = new ArrayList<CListWrapper>();
      
      /**
       * This is used to cache the content lists belongs to others sites.
       */
      private List<CListWrapper> mi_otherSiteCLists = new ArrayList<CListWrapper>();
      
      /**
       * This is used to cache the content lists belongs to current site and
       * unused.
       */
      private List<CListWrapper> mi_siteAndUnusedCLists = new ArrayList<CListWrapper>();
      
      /**
       * Reset common properties at the beginning of entering the 
       * Association Editor. 
       */
      private void reset()
      {
         mi_clists.clear();
         mi_otherSiteCLists.clear();
         mi_siteAndUnusedCLists.clear();
         mi_contentListFilter = null;
         mi_authtype = null;
         mi_assemblyContext = null;
         mi_srcCList = null;
      }
      
      /**
       * Set m_apXXX variables from the given index of the m_eclists
       * @param index the index of m_eclists, assumed not <code>null</code>.
       * @return <code>true</code> if successfully done; return <code>false</code>
       *    if failed to load content list from the index element.
       */
      private boolean setLocalVariables(Integer index)
      {
         reset();
         mi_isNew = false;
         
         IPSPublisherService psvc = PSPublisherServiceLocator
         .getPublisherService();
         PSEditionContentListWrapper selected = m_eclists.get(index);
         String cname = selected.getContentlistname();
         mi_srcCList = psvc.findContentListByName(cname);
         if (mi_srcCList == null)
         {
            ms_log.error("Cannot find content list with name: " + cname);
            return false;            
         }
         mi_authtype = selected.getAuthtype();
         mi_assemblyContext = null;
         if (selected.getEclist().getAssemblyContextId() != null)
            mi_assemblyContext = selected.getEclist().getAssemblyContextId()
                  .getUUID();
         mi_deliveryContext = selected.getEclist().getDeliveryContextId()
                  .getUUID();
         return true;
      }
      
      /**
       * Get the actual help file name for the current page.
       * 
       * @return  the help file name, never <code>null</code> or empty.
       */
      public String getHelpFile()
      {
         return PSHelpTopicMapping.getFileName("AssociateContentlist");
      }

      /**
       * Determines if creating an association or edition existing one.
       * @return <code>true</code> if creating an association.
       */
      public boolean isNew()
      {
         return mi_isNew;
      }
      
      /**
       * Determines if need to display a warning message when there is nothing
       * selected in the content list table.
       * 
       * @return <code>true</code> if need to display the warning message.
       */
      public boolean getSelectWarning()
      {
         /**
          * Default to give warning is not empty, which will be override later
          * when an element is selected.
          * 
          * However, default to false if the list (mi_clists) is EMPTY; 
          * otherwise the UI may crash when involving a lot of content lists.
          * We have to move the EMPTY list (empty getCandidateContentLists()) 
          * logic into JSP page as part of EL (expression language), see
          * AssociateContentlist.jsp for detail. 
          */
         boolean isSelected = mi_clists.isEmpty() ? false : true;
         for (CListWrapper w : mi_clists)
         {
            if (w.getSelected())
            {
               isSelected = false;
               break;
            }
         }
         
         return isSelected;
      }
      
      /**
       * Add the content list selected in the associate content list page to the
       * edition, or save the new information about a specific content list.
       * 
       * @return the outcome "done" if successful; <code>null</code> if missing
       * required data (this will make UI remain at the same page).
       */
      public String handleAssociation()
      {
         // get selected Content List & update the edited one if needed
         boolean foundSrcCList = false;
         List<IPSContentList> selectedCLists = new ArrayList<IPSContentList>();
         for (CListWrapper w : mi_clists)
         {
            if (w.getSelected())
            {
               if (mi_srcCList != null
                     && w.getClist().getGUID().equals(mi_srcCList.getGUID()))
               {
                  Integer index = findSelectedCL();
                  PSEditionContentListWrapper wrapper = m_eclists.get(index);
                  setCLWrapperProps(wrapper);
                  foundSrcCList = true;
                  continue;
               }
               
               selectedCLists.add(w.getClist());
            }
         }

         // if nothing is selected, back to the same page
         if (selectedCLists.isEmpty() && (!foundSrcCList))
            return null;

         // remove the replaced Content List
         if ((!mi_isNew) && (!foundSrcCList) && mi_srcCList != null)
         {
            Integer index = findSelectedCL();
            removeContentList(index);
         }

         // append the new Content List
         int maxseq = 0;
         if (m_eclists != null)
         {
            for (PSEditionContentListWrapper e : m_eclists)
            {
               maxseq = Math.max(maxseq, e.getSequence());
            }
         }
         else
         {
            m_eclists = new ArrayList<PSEditionContentListWrapper>();
         }
         for (IPSContentList c : selectedCLists)
         {
            PSEditionContentListWrapper wrapper = createCLWrapper(maxseq + 1,
                  c);
            setCLWrapperProps(wrapper);
            m_eclists.add(wrapper);
         }
         Collections.sort(m_eclists);

         return "done";
      }

      /**
       * Creates an Content List Wrapper. Assume {@link #mi_srcCList} 
       * is not <code>null</code>}.
       *  
       * @return the created Content List Wrapper. Never <code>null</code>.
       * The selected flag is on for the returned Content List. 
       */
      PSEditionContentListWrapper createCLWrapper(int sequence, 
            IPSContentList clist)
      {
         IPSPublisherService psvc = PSPublisherServiceLocator
               .getPublisherService();
         IPSEditionContentList eclist = psvc.createEditionContentList();
         PSEditionContentListPK eclPK = 
            ((PSEditionContentList) eclist).getEditionContentListPK();
         eclPK.setEditionid(m_edition.getGUID().longValue());
         eclPK.setContentlistid(clist.getGUID().longValue());
         eclist.setSequence(sequence);
         
         PSEditionContentListWrapper wrapper = 
            new PSEditionContentListWrapper(eclist, m_edition.getSiteId());
         wrapper.setSelected(true);

         return wrapper;
      }
      
      /**
       * Sets the properties of the supplied Edition/ContentList wrapper
       * to the user selected properties.
       * @param wrapper the wrapper, assumed not <code>null</code>.
       */
      private void setCLWrapperProps(PSEditionContentListWrapper wrapper)
      {
         // set delivery context
         IPSGuid deliveryId = null;
         if (mi_deliveryContext != null)
            deliveryId = PSGuidUtils.makeGuid(mi_deliveryContext,
                  PSTypeEnum.CONTEXT);
         wrapper.getEclist().setDeliveryContextId(deliveryId);
         
         // set assembly context
         IPSGuid assemblyId = null;
         if (mi_assemblyContext != null)
            assemblyId = PSGuidUtils.makeGuid(mi_assemblyContext,
                  PSTypeEnum.CONTEXT);
         wrapper.getEclist().setAssemblyContextId(assemblyId);

         // set auth type
         Integer authtype = null;
         if (StringUtils.isNotBlank(mi_authtype)
               && StringUtils.isNumeric(mi_authtype))
         {
            authtype = new Integer(mi_authtype);
         }
         wrapper.getEclist().setAuthtype(authtype);
         wrapper.init();         
      }

      /**
       * Get the possible content lists according to the current filter.
       * 
       * @return the lists, never <code>null</code> but might be empty. The entries
       * are in ascending alpha order.
       */
      public List<CListWrapper> getCandidateContentLists()
      {
         boolean isFirstCall = mi_clists.isEmpty();
         
         IPSGuid siteId = m_edition.getSiteId();
         List<CListWrapper> clists = new ArrayList<CListWrapper>(); 
         clists.addAll(getSiteAndUnusedCLists(siteId));
         if (!mi_limitContentListsToSite)
         {   
            clists.addAll(getOtherSiteCLists());
         }
         
         List<CListWrapper> filteredList = filterCLists(clists);
         mi_clists.clear();
         mi_clists.addAll(sortCLists(filteredList));
         
         if (isFirstCall)
            setSelectRowIfNeeded();
            
         return mi_clists;
      }
      
      /**
       * Sorts the specified Content Lists.
       * 
       * @param clists the Content Lists, assumed not <code>null</code>.
       * 
       * @return the sorted list, never <code>null</code>.
       */
      private Set<CListWrapper> sortCLists(List<CListWrapper> clists)
      {
         Set<CListWrapper> listset = new TreeSet<CListWrapper>(
               new Comparator<CListWrapper>()
               {
                  public int compare(CListWrapper o1, CListWrapper o2)
                  {
                     return o1.getClist().getName().compareToIgnoreCase(
                           o2.getClist().getName());
                  }
               });
         listset.addAll(clists);
         return listset;
      }
      
      /**
       * Gets the Content Lists that is either belong to the specified site
       * or not used by any site.
       * 
       * @param siteId the ID of the site, assumed not <code>null</code>.
       * 
       * @return the Content Lists of the site and unused by any sites.
       */
      private List<CListWrapper> getSiteAndUnusedCLists(IPSGuid siteId)
      {
         if (!mi_siteAndUnusedCLists.isEmpty())
            return mi_siteAndUnusedCLists;

         IPSPublisherService psvc = PSPublisherServiceLocator
               .getPublisherService();
         final List<IPSContentList> lists;
         lists = psvc.findAllContentListsBySite(siteId);
         lists.addAll(psvc.findAllUnusedContentLists());
         for (IPSContentList c : lists)
            mi_siteAndUnusedCLists.add(new CListWrapper(c));
         
         return mi_siteAndUnusedCLists;
      }
      
      /**
       * Determines if the specified Content Lists contains the given name.
       * 
       * @param name the looked up name, assumed not <code>null</code>.
       * @param clists the Content Lists, assumed not <code>null</code>.
       * 
       * @return <code>true</ocde> if the list does contain the specified
       * name.
       */
      private boolean containsName(String name, List<CListWrapper> clists)
      {
         for (CListWrapper w : clists)
         {
            if (name.equals(w.getClist().getName()))
               return true;
         }
         return false;
      }
      
      /**
       * Gets the Content Lists that belongs to all other sites, except the
       * current site.
       * 
       * @return the Content Lists, never <code>null</code>, but may be empty.
       */
      private List<CListWrapper> getOtherSiteCLists()
      {
         if (!mi_otherSiteCLists.isEmpty())
            return mi_otherSiteCLists;

         IPSPublisherService psvc = PSPublisherServiceLocator
               .getPublisherService();
         final List<IPSContentList> lists = psvc.findAllContentLists("");
         for (IPSContentList c : lists)
         {
            if (!containsName(c.getName(), mi_siteAndUnusedCLists))
               mi_otherSiteCLists.add(new CListWrapper(c));
         }
         return mi_otherSiteCLists;
      }
      
      /**
       * If editing an existing association, then set the row that is occupied 
       * by current Content List object; otherwise clear the selected row of
       * the table (for add new association).
       */
      private void setSelectRowIfNeeded()
      {
         if (mi_isNew || mi_srcCList == null)
            return;
         
         String name =  mi_srcCList.getName();
         for (CListWrapper w : mi_clists)
         {
            if (name.equals(w.getClist().getName()))
            {
               w.setSelected(true);
               break;
            }
         }
      }

      /**
       * Gets the number of rows per page for the table that displays the child 
       * components.
       * 
       * @return the number of rows per page.
       */
      public int getPageRows() throws Exception
      {
         return PSNodeBase.getPageRows(getCandidateContentLists().size());
      }
      
      /**
       * Filters the supplied list. Removes the ones already associated with 
       * the Edition and/or do not match the filter (if specified).
       * 
       * @param lists the to be filtered list.
       */
      private List<CListWrapper> filterCLists(List<CListWrapper> lists)
      {
         String filter = null;
         if (StringUtils.isNotBlank(mi_contentListFilter))
            filter = mi_contentListFilter.toLowerCase();
         
         List<CListWrapper> rval = new ArrayList<CListWrapper>();
         for (CListWrapper c : lists)
         {
            // is the current one
            if ((!mi_isNew)
                  && mi_srcCList != null
                  && c.getClist().getName().equalsIgnoreCase(
                        mi_srcCList.getName()))
            {
               if ((filter != null)
                     && (!c.getClist().getName().toLowerCase()
                           .contains(filter)))
               {
                  // skip this if does not match filter pattern
                  continue;
               }
               rval.add(c);
               continue;
            }
            
            // already associated with current Edition
            if (clistNameExists(c.getClist().getName()))
            {
               continue;
            }
            
            // does not match the filter pattern
            if ((filter != null)
                  && (!c.getClist().getName().toLowerCase().contains(filter)))
            {
               continue;
            }
            
            rval.add(c);
         }
         
         return rval;
      }
      
      /**
       * @return the candidate contexts to show, never <code>null</code>.
       */
      public SelectItem[] getCandidateContexts()
      {
         IPSSiteManager smgr = PSSiteManagerLocator.getSiteManager();
         List<IPSPublishingContext> ctxs = smgr.findAllContexts();
         SelectItem rval[] = new SelectItem[ctxs.size()];
         for (int i = 0; i < ctxs.size(); i++)
         {
            IPSPublishingContext ctx = ctxs.get(i);
            rval[i] = new SelectItem(ctx.getGUID().getUUID(), ctx.getName());

            // set the default delivery context if not defined yet
            if (DEFAULT_DELIVERY_CTX.equalsIgnoreCase(ctx.getName())
                  && mi_deliveryContext == null)
            {
               mi_deliveryContext = i;
            }
         }
         return rval;
      }

      /**
       * @return the apContentListFilter
       */
      public String getContentListFilter()
      {
         return mi_contentListFilter;
      }

      /**
       * Remove the content list filter.
       */
      public void clearFilter()
      {
         mi_clists.clear();
         mi_contentListFilter = null;
         FacesContext ctx = FacesContext.getCurrentInstance();
         UIComponent c = ctx.getViewRoot().findComponent("filteredText");
         ((UIXEditableValue) c).resetValue(); 
      }
      
      /**
       * Determines if the supplied name exists in the current associated
       * Content Lists.
       * @param name the name in questions, assumed not <code>null</code> or empty.
       * @return <code>true</code> if the name is one of the name in the associated
       *    Content List.
       */
      private boolean clistNameExists(String name)
      {
         for (PSEditionContentListWrapper w : m_eclists)
         {
            if (name.equalsIgnoreCase(w.getContentlistname()))
               return true;
         }
         return false;
      }

      /**
       * Sets the new value on the component. Does not skip to render response as
       * it is assumed to be called by a command. 
       * @param ev Supplied by framework, assumed never <code>null</code>.
       */
      public void contentListNameFilterChanged(ValueChangeEvent ev)
      {
         setContentListFilter(ev.getNewValue().toString());
      }
      
      /**
       * Sets the new value on the component, then skips to the render phase.
       * @param ev Supplied by framework, assumed never <code>null</code>.
       */
      public void contentListLimitFilterChanged(ValueChangeEvent ev)
      {
         setLimitContentListsToSite(((Boolean)ev.getNewValue()));
         FacesContext.getCurrentInstance().renderResponse();
      }
      

      /**
       * @param apContentListFilter the ContentListFilter to set
       */
      public void setContentListFilter(String apContentListFilter)
      {
         mi_clists.clear();
         mi_contentListFilter = apContentListFilter;
      }

      /**
       * @return the LimitContentListsToSite
       */
      public boolean getLimitContentListsToSite()
      {
         return mi_limitContentListsToSite;
      }

      /**
       * @param apLimitContentListsToSite the LimitContentListsToSite to set
       */
      public void setLimitContentListsToSite(boolean apLimitContentListsToSite)
      {
         mi_limitContentListsToSite = apLimitContentListsToSite;
      }

      /**
       * @return the AssemblyContext
       */
      public Integer getAssemblyContext()
      {
         return mi_assemblyContext;
      }

      /**
       * @param apAssemblyContext the AssemblyContext to set
       */
      public void setAssemblyContext(Integer apAssemblyContext)
      {
         mi_assemblyContext = apAssemblyContext;
      }

      /**
       * @return the deliveryContext
       */
      public Integer getDeliveryContext()
      {
         return mi_deliveryContext;
      }

      /**
       * @param apDeliveryContext the deliveryContext to set
       */
      public void setDeliveryContext(Integer apDeliveryContext)
      {
         mi_deliveryContext = apDeliveryContext;
      }

      /**
       * @return the authtype
       */
      public String getAuthType()
      {
         return mi_authtype;
      }

      /**
       * @param apAuthtype the Authtype to set
       */
      public void setAuthType(String apAuthtype)
      {
         mi_authtype = apAuthtype;
      }
      
     /**
      * Returns all authtypes registered with the system.
      * 
      * @return Never <code>null</code>. The first entry is the 'empty' entry.
      * Sorted in ascending alpha order.
      */
      public List<SelectItem> getCandidateAuthTypes()
      {
         List<SelectItem> result = new ArrayList<SelectItem>();
         IPSContentService csvc = PSContentServiceLocator.getContentService();
         List<PSKeyword> keywords = csvc.findKeywordsByLabel(
               "Authorization_Types", "label");
         if (keywords.size() == 0)
            return result;
         
         PSKeyword key = keywords.get(0);
         keywords = csvc.findKeywordChoices(key.getValue(), null);
         for (PSKeyword k : keywords)
         {
            result.add(new SelectItem(k.getValue(), k.getLabel()));
         }

         return result;
      }      
   }

   @Override
   public String getHelpTopic()
   {
      return "EditionEditor";
   }

}
