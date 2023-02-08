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

import com.percussion.services.workflow.data.PSWorkflow;
import org.apache.commons.lang.StringUtils;

import java.sql.SQLException;

/**
 * Represents a workflow in the system. This object is not cached at this time
 * as it may be updated via the XML applications.
 */
public class PSWorkflowAppsContext implements IPSWorkflowAppsContext
{
   private int m_nWorkflowAppID = 0;
   private String m_sWorkflowAppName = "";
   private String m_sWorkflowAppAdmin = "";
   private String m_sWorkflowAppDesc = "";
   private int m_initialStateId = 0;

   /**
    * Default ctor
    */
   public PSWorkflowAppsContext()
   {
      
   }
   
   /**
    * Construct a workflow context from a source workflow
    * 
    * @param source The source, may not be <code>null</code>.
    */
   public PSWorkflowAppsContext(PSWorkflow source)
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");
      
      m_initialStateId = (int) source.getInitialStateId();
      m_nWorkflowAppID = (int) source.getGUID().longValue();
      m_sWorkflowAppAdmin = source.getAdministratorRole();
      m_sWorkflowAppDesc = source.getDescription();
      m_sWorkflowAppName = source.getName();
   }
   
   /**
    * Gets the Workflow application ID
    * @return  Workflow app name
    */
   public int getWorkFlowAppID()
   {
      return m_nWorkflowAppID;
   }
   
   /**
    * Set the new workflow app id
    * @param newid the new id
    */
   public void setWorkFlowAppID(int newid)
   {
      m_nWorkflowAppID = newid;
   }

   /**
    * Gets the Workflow application name
    * @return  Workflow app name
    */
   public String getWorkFlowAppName()
   {
      return m_sWorkflowAppName;
   }
   
   /**
    * Set the new workflow name
    * @param name the new name, never <code>null</code> or empty
    */
   public void setWorkFlowAppName(String name)
   {
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name may not be null or empty");
      }
      m_sWorkflowAppName = name;
   }

   /**
    * Gets the Workflow application description
    * @return  Workflow app description
    *
    */
   public String getWorkFlowAppDescription()
   {
      return m_sWorkflowAppDesc;
   }
   
   /**
    * Set a new description
    * @param desc the new description, may be <code>null</code> or empty
    */
   public void setWorkFlowAppDescription(String desc) 
   {
      m_sWorkflowAppDesc = desc;
   }

   /**
    * Gets the Workflow application's administrator name
    * @return  name of the Workflow application's administrator
    */
   public String getWorkFlowAdministrator()
   {
      return m_sWorkflowAppAdmin;
   }
   
   /**
    * Set a new administrator
    * @param admin the new admin name, never <code>null</code> or empty
    */
   public void setWorkFlowAdministrator(String admin)
   {
      if (StringUtils.isBlank(admin))
      {
         throw new IllegalArgumentException("admin may not be null or empty");
      }
      m_sWorkflowAppAdmin = admin;
   }
   
   

   /**
    * Gets the Workflow application's initial stateID
    * @return  Workflow's initial stateID
    */
   public int getWorkFlowInitialStateID() throws SQLException
   {
      return m_initialStateId;
   }
}
