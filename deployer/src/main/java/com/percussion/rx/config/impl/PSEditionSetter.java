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
package com.percussion.rx.config.impl;

import com.percussion.rx.config.IPSConfigHandler.ObjectState;
import com.percussion.rx.design.IPSAssociationSet;
import com.percussion.rx.design.impl.PSEditionWrapper;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.publisher.IPSEditionTaskDef;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.types.PSPair;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Edition property setter. 
 *
 * @author YuBingChen
 */
public class PSEditionSetter extends PSSimplePropertySetter
{
   @Override
   protected boolean applyProperty(Object obj, ObjectState state,
         List<IPSAssociationSet> aSets, String propName, Object propValue)
      throws Exception
   {
      if (! (obj instanceof PSEditionWrapper))
         throw new IllegalArgumentException("obj type must be PSEditionWrapper.");
      
      PSEditionWrapper wrapper = (PSEditionWrapper) obj;
      if (PRE_TASKS.equals(propName))
      {
         setTasks(wrapper, propValue, true);
      }
      else if (POST_TASKS.equals(propName))
      {
         setTasks(wrapper, propValue, false);
      }
      else
      {
         super.applyProperty(wrapper.getEdition(), state, aSets, propName,
               propValue);
      }
      
      return true;
   }

   /*
    * //see base class method for details
    */
   @Override
   protected Object getPropertyValue(Object obj, String propName) throws PSNotFoundException {
      PSEditionWrapper wrapper = (PSEditionWrapper) obj;
      if (PRE_TASKS.equals(propName))
      {
         if (wrapper.getPreTasks().isEmpty())
            return Collections.emptyList();
         else
            return convertListTaskToListMap(wrapper.getPreTasks());
      }
      else if (POST_TASKS.equals(propName))
      {
         if (wrapper.getPostTasks().isEmpty())
            return Collections.emptyList();
         else
            return convertListTaskToListMap(wrapper.getPostTasks());
      }
      
      return super.getPropertyValue(obj, propName);
   }
   
   /**
    * Converts a list of tasks to a list of maps.
    * 
    * @param tasks the list of tasks, 
    * 
    * @return
    */
   private List<Map<String, Object>> convertListTaskToListMap(
         List<IPSEditionTaskDef> tasks)
   {
      List<Map<String, Object>> result = new ArrayList<>();
      for (IPSEditionTaskDef def : tasks)
      {
         Map<String, Object> tmap = convertTaskToMap(def);
         result.add(tmap);
      }
      return result;
   }

   /**
    * Converts the specified task to a map, which contains all known properties
    * of the task definition.
    * 
    * @param taskDef the task, assumed not <code>null</code>.
    * 
    * @return the converted map, never <code>null</code>, but may be empty.
    */
   private Map<String, Object> convertTaskToMap(IPSEditionTaskDef taskDef)
   {
      Map<String, Object> taskMap = new HashMap<>();
    
      if (StringUtils.isNotBlank(taskDef.getExtensionName()))
      {
         taskMap.put(EXT_NAME, taskDef.getExtensionName());
         
         Map<String, String> params = taskDef.getParams();
         List<PSPair<String, String>> pairs = new ArrayList<>();
         for (String k : params.keySet())
         {
            pairs.add(new PSPair<>(k, params.get(k)));
         }
         taskMap.put(EXT_PARAMS, pairs);
      }
      
      return taskMap;
   }
   
   @SuppressWarnings("unchecked")
   private void setTasks(PSEditionWrapper wrapper, Object propValue,
         boolean isPreTasks)
   {
      List<Map> srcTasks = new ArrayList<>();
      if (propValue instanceof List)
      {
         srcTasks.addAll((List<Map>) propValue);
      }
      else if ((!(propValue instanceof Map)) || (!((Map)propValue).isEmpty()))
      {
         throw new IllegalArgumentException(
               "A list of Edition task type must be List or empty Map.");
      }
      
      IPSGuid id = wrapper.getEdition().getGUID();
      List<IPSEditionTaskDef> tasks = new ArrayList<>();
      // backwards process pre-tasks, so that the seq# of 1st task is smallest 
      if (isPreTasks)
         Collections.reverse(srcTasks);
      
      for (int i=0; i < srcTasks.size(); i++)
      {
         Map<String, Object> props = srcTasks.get(i);
         if (props.isEmpty())
         {
            // ignore the empty map in a list, which can be created by the 
            // following XML section:
            //       <property name="preTasks">
            //          <propertySet/> 
            //       </property>
            continue;
         }
         
         int seq = (isPreTasks) ? (i+1) * -1 : (i+1);
         IPSEditionTaskDef task = createTask(props, seq, id);
         tasks.add(task);
      }
      
      if (isPreTasks)
         wrapper.setPreTasks(tasks);
      else
         wrapper.setPostTasks(tasks);
   }
   
   @SuppressWarnings("unchecked")
   private IPSEditionTaskDef createTask(Map<String, Object> props, int seq,
         IPSGuid editionId)
   {
      IPSPublisherService srv = PSPublisherServiceLocator.getPublisherService();
      IPSEditionTaskDef task = srv.createEditionTask();
      task.setEditionId(editionId);
      task.setSequence(seq);
      for (Map.Entry<String, Object> entry : props.entrySet())
      {
         String key = entry.getKey();
         Object value = entry.getValue();
         if (key.equals(EXT_NAME))
         {
            task.setExtensionName(value.toString());
         }
         else if (key.equals(CONT_ON))
         {
            Boolean v = (Boolean) convertValue(value, Boolean.class);
            task.setContinueOnFailure(v);
         }
         else if (key.equals(EXT_PARAMS))
         {
            if (!(value instanceof List))
               throw new IllegalArgumentException(
                     "The extensionParams property type must be List.");
            List<PSPair<String, String>> params = (List<PSPair<String, String>>) value;
            for (PSPair<String, String> pair : params)
            {
               task.setParam(pair.getFirst(), pair.getSecond());
            }
         }
      }
      return task;
   }
   
   /**
    * The name of the edition pre-task property.
    */
   public static final String PRE_TASKS = "preTasks";
   
   /**
    * The name of the edition post-task property
    */
   public static final String POST_TASKS = "postTasks";
   
   /**
    * Task specific property names
    */
   private static final String EXT_NAME = "extensionName";
   private static final String EXT_PARAMS = "extensionParams";
   private static final String CONT_ON = "continueOnFailure";
}
