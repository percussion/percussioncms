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
