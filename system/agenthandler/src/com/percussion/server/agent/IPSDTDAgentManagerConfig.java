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
