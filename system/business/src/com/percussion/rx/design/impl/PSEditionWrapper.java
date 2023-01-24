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
package com.percussion.rx.design.impl;

import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.publisher.IPSEditionTaskDef;
import com.percussion.services.publisher.data.PSEdition;
import com.percussion.utils.guid.IPSGuid;

import java.util.List;

/**
 * This class wrapps {@link IPSEdition} and
 * {@link IPSEditionTaskDef} of the pre/post tasks.
 * 
 * @author YuBingChen
 */
public class PSEditionWrapper
{
   public PSEditionWrapper(IPSEdition edition)
   {
      m_edition = edition;
   }
   
   /**
    * Calls {@link IPSEdition#getGUID() getEdition().getGUID()}.
    * 
    * @return the GUID of the Edition, never <code>null</code>.
    */
   public IPSGuid getGUID()
   {
      return m_edition.getGUID();
   }
   
   /**
    * Calls {@link IPSEdition#getName() getEdition().getName()}.
    * 
    * @return the name of the Edition, never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_edition.getName();
   }
   
   /**
    * Calls {@link PSEdition#getVersion() getEdition().getVersion()}.
    * 
    * @return the version of the Edition, never <code>null</code> or empty.
    */
   public Integer getVersion()
   {
      return ((PSEdition)m_edition).getVersion();
   }

   /**
    * Gets the Edition.
    * 
    * @return the edition, never <code>null</code>.
    */
   public IPSEdition getEdition()
   {
      return m_edition;
   }
   
   /**
    * Sets the pre-tasks for the current Edition.
    * 
    * @param preTasks the new pre-tasks, never <code>null</code>, but
    * may be empty.
    */
   public void setPreTasks(List<IPSEditionTaskDef> preTasks)
   {
      if (preTasks == null)
         throw new IllegalArgumentException("preTasks may not be null.");
      
      m_preTasks = preTasks;
   }
   
   /**
    * Sets the post-tasks for the current Edition.
    * 
    * @param postTasks the new post-tasks, never <code>null</code>, but
    * may be empty.
    */
   public void setPostTasks(List<IPSEditionTaskDef> postTasks)
   {
      if (postTasks == null)
         throw new IllegalArgumentException("postTasks may not be null.");
      
      m_postTasks = postTasks;
   }

   /**
    * Gets the pre-tasks for the current Edition.
    * 
    * @return the pre-tasks, it may be <code>null</code> if have never set
    * by {@link #setPreTasks(List)}.
    */
   public List<IPSEditionTaskDef> getPreTasks()
   {
      return m_preTasks;
   }
   
   /**
    * Gets the post-tasks for the current Edition.
    * 
    * @return the post-tasks, it may be <code>null</code> if have never set
    * by {@link #setPostTasks(List)}.
    */
   public List<IPSEditionTaskDef> getPostTasks()
   {
      return m_postTasks;
   }

   @Override
   public boolean equals(Object other)
   {
      if (!(other instanceof PSEditionWrapper))
         return false;
      
      return m_edition.equals(((PSEditionWrapper)other).getEdition());
   }
   
   /**
    * The Edition object, initialized by constructor, never <code>null</code>
    * after that.
    */
   private IPSEdition m_edition;
   
   /**
    * The pre-tasks of the current Edition. Default to <code>null</code> if has
    * not been set.
    */
   private List<IPSEditionTaskDef> m_preTasks = null;

   /**
    * The post-tasks of the current Edition. Default to <code>null</code> if has
    * not been set.
    */
   private List<IPSEditionTaskDef> m_postTasks = null;
   
}
