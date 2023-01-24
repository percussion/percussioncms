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

package com.percussion.server.agent;

/**
 * This interface defines the string constants for the agent manager
 * configuration XML document.
 */

public interface IPSDTDAgentManagerConfig
{
   /**
    * Root element consisting of one or more agents.
    */
   static final String ELEM_AGENT_LIST = "agentlist";

   /**
    * Element representing one agent.
    */
   static final String ELEM_AGENT = "agent";

   /**
    * Name of the element specifying the class that implments the agent.
    */
   static final String ELEM_CLASS = "class";

   /**
    * Name of the optional element indicating the schedule parameters, i.e.
    * elay and interval in the case of scheduled agent.
    */
   static final String ELEM_SCHEDULE = "schedule";

   /**
    * Attribute of the 'agent' element specifying the name of the agent.
    */
   static final String ATTRIB_NAME = "name";

   /**
    * Name of the attribute to specify the agent service type.
    */
   static final String ATTRIB_SERVICE_TYPE = "servicetype";

   /**
    * Name of the attribute to specify initial delay in seconds.
    */
   static final String ATTRIB_DELAY = "delay";

   /**
    * Name of the attribute to specify interval in seconds.
    */
   static final String ATTRIB_INTERVAL = "interval";

   /**
    * Attribute value for the servicetype indicating that the agent provides
    * scheduled services.
    */
   static final String SERVICE_TYPE_SCHEDULED = "scheduled";

   /**
    * Attribute value for the servicetype indicating that the agent provides
    * ondemand services.
    */
   static final String SERVICE_TYPE_fixed = "fixed";
}
