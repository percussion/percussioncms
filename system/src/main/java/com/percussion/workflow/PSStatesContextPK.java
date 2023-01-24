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
package com.percussion.workflow;

import org.apache.commons.lang.builder.EqualsBuilder;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * Primary key for state lookup
 * 
 * @author dougrand
 */
@Embeddable
public class PSStatesContextPK implements Serializable, IPSStatesContextPK {
   private static final long serialVersionUID = -5449054234437911309L;
   private int m_workflowid, m_stateid;
   
   /**
    * Default Ctor
    */
   public PSStatesContextPK()
   {
      
   }
   
   /**
    * Ctor to create new primary key with data
    * @param wf the workflow id
    * @param stid the state id
    */
   public PSStatesContextPK(int wf, int stid)
   {
      m_workflowid = wf;
      m_stateid = stid;
   }

   /**
    * @return Returns the stateid.
    */
   @Override
   @Column(name = "STATEID")
   public int getStateid()
   {
      return m_stateid;
   }

   /**
    * @param stateid The stateid to set.
    */
   @Override
   public void setStateid(int stateid)
   {
      m_stateid = stateid;
   }

   /**
    * @return Returns the workflowid.
    */
   @Override
   @Column(name = "WORKFLOWAPPID")
   public int getWorkflowAppId()
   {
      return m_workflowid;
   }

   /**
    * @param workflowid The workflowid to set.
    */
   @Override
   public void setWorkflowAppId(int workflowid)
   {
      m_workflowid = workflowid;
   }

   /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      return EqualsBuilder.reflectionEquals(this,obj);
   }

   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      return m_stateid ^ m_workflowid;
   }
}
