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
