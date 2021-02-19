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
package com.percussion.rx.design.impl;

import com.percussion.rx.design.IPSAssociationSet;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.error.PSRuntimeException;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.publisher.IPSEditionTaskDef;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.utils.guid.IPSGuid;

import java.util.ArrayList;
import java.util.List;

/**
 * The Edition model. It manages {@link IPSEdition} and its child components,
 * {@link IPSEditionTaskDef}, which are the Edition's pre/post tasks.
 *
 * @author YuBingChen
 */
public class PSEditionModel extends PSDesignModel
{
   @Override
   public Object load(IPSGuid id) throws PSNotFoundException {
      return load(id, false);
   }

   @Override
   public Object loadModifiable(IPSGuid id) throws PSNotFoundException {
      return load(id, true);
   }

   /**
    * Loads an Edition.
    * 
    * @param id the ID of the loaded Edition, not <code>null</code>.
    * @param isModifiable <code>true</code> if loading a modifiable object.
    * 
    * @return the loaded Edition, never <code>null</code>.
    */
   public Object load(IPSGuid id, boolean isModifiable) throws PSNotFoundException {
      if (id == null)
         throw new IllegalArgumentException("ID of an Edition cannot be null.");
      
      IPSEdition edition;
      if (isModifiable)
         edition = (IPSEdition) super.loadModifiable(id);
      else
         edition = (IPSEdition) super.load(id);
      
      PSEditionWrapper wrapper = new PSEditionWrapper(edition);
      loadTasks(wrapper);
      return wrapper;
   }

   @Override
   public void save(Object obj, List<IPSAssociationSet> associationSets)
   {
      if (!(obj instanceof PSEditionWrapper))
         throw new IllegalArgumentException("obj must be PSEditionWrapper type.");
      
      PSEditionWrapper wrapper = (PSEditionWrapper) obj;
      super.save(wrapper.getEdition(), associationSets);
      saveEditionTasks(wrapper);
   }
   
   /**
    * Saves the pre and post edition tasks for the given edition wrapper.
    * 
    * @param wrapper the wrapper that contains to be saved tasks, assumed not
    * <code>null</code>. Do nothing if there is no edition tasks.
    */
   private void saveEditionTasks(PSEditionWrapper wrapper)
   {
      if (wrapper.getPostTasks() == null && wrapper.getPreTasks() == null)
         return;
      
      IPSPublisherService srv = (IPSPublisherService) getService();
      IPSGuid id = wrapper.getEdition().getGUID();
      
      List<IPSEditionTaskDef> tasks = srv.loadEditionTasks(id);

      // always replace pre/post tasks
      if (wrapper.getPreTasks() != null)
      {
         deleteTasks(getTasks(tasks, true));
         saveTasks(wrapper.getPreTasks());
      }
      if (wrapper.getPostTasks() != null)
      {
         deleteTasks(getTasks(tasks, false));
         saveTasks(wrapper.getPostTasks());
      }
   }
   
   @Override
   public IPSGuid nameToGuid(String name)
   {
      IPSPublisherService srv = (IPSPublisherService) getService();
      IPSEdition edition = srv.findEditionByName(name);
      if (edition == null)
         throw new PSRuntimeException("Cannot find Edition with name \"" + name
               + "\".");
      
      return edition.getGUID(); 
   }

   /**
    * Saves a list of edition tasks.
    * 
    * @param tasks the to be saved tasks, assumed not <code>null</code>, but
    * may be empty.
    */
   private void saveTasks(List<IPSEditionTaskDef> tasks)
   {
      IPSPublisherService srv = (IPSPublisherService) getService();
      for (IPSEditionTaskDef task : tasks)
      {
         srv.saveEditionTask(task);
      }
   }

   /**
    * Deletes a list of edition tasks.
    * 
    * @param tasks the to be deleted tasks, assumed not <code>null</code>, but
    * may be empty.
    */
   private void deleteTasks(List<IPSEditionTaskDef> tasks)
   {
      IPSPublisherService srv = (IPSPublisherService) getService();
      for (IPSEditionTaskDef task : tasks)
      {
         srv.deleteEditionTask(task);
      }
   }
   
   /**
    * Loads edition tasks for the given edition.
    * 
    * @param wrapper the wrapper of the edition, assumed not <code>null</code>.
    */
   private void loadTasks(PSEditionWrapper wrapper)
   {
      IPSPublisherService srv = (IPSPublisherService) getService();
      List<IPSEditionTaskDef> tasks = srv.loadEditionTasks(wrapper.getGUID());
      if (tasks != null)
      {
         List<IPSEditionTaskDef> preTasks = getTasks(tasks, true);
         if (!preTasks.isEmpty())
            wrapper.setPreTasks(preTasks);
         List<IPSEditionTaskDef> postTasks = getTasks(tasks, false);
         if (!postTasks.isEmpty())
            wrapper.setPostTasks(postTasks);
      }
   }
   
   /**
    * Gets the pre or post tasks from a list of tasks.
    * 
    * @param allTasks the list of tasks that may contain pre and/or post 
    * tasks, assumed not <code>null</code>, but may be empty.
    * @param isPreTask <code>true</code> if wants to get pre tasks.
    * 
    * @return the requested tasks, never <code>null</code>, but may be empty.
    */
   private List<IPSEditionTaskDef> getTasks(List<IPSEditionTaskDef> allTasks,
         boolean isPreTask)
   {
      List<IPSEditionTaskDef> tasks = new ArrayList<>();
      for (IPSEditionTaskDef task : allTasks)
      {
         if (isPreTask)
         {
            if (task.getSequence() < 0)
               tasks.add(task);
         }
         else
         {
            if (task.getSequence() >= 0)
               tasks.add(task);            
         }
      }
      return tasks;
   }

   @Override
   public void delete(IPSGuid guid) throws PSNotFoundException {
      if (guid == null || !isValidGuid(guid))
         throw new IllegalArgumentException("guid is not valid for this model");
      PSEditionWrapper wrapper = (PSEditionWrapper) loadModifiable(guid);
      loadTasks(wrapper);
      List<IPSEditionTaskDef> tasks = new ArrayList<>();
      deleteTasks(getTasks(tasks,true));
      deleteTasks(getTasks(tasks,false));
      IPSPublisherService srv = (IPSPublisherService) getService();
      srv.deleteEdition(wrapper.getEdition());
   }
}
