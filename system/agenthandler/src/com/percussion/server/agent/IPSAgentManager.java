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

import java.util.Map;

/**
 * This is the interface all Rhythmyx Agent Managers must implement. It
 * provides methods to handle request from an agent and to terminate all agents.
 */
public interface IPSAgentManager
{
   /**
    * This method is invoked by the request handler whenever the action
    * requested is from an agent.
    * @param params - map of all parameters from the request, may be
    * <code>null</code> but normally is not.
    * @throws IllegalArgumentException if the response object is
    * <code>null</code>
    */
   void handleAction(Map params, IPSAgentHandlerResponse response);

   /**
    * This method terminates all the agents registered by the Agent manager by
    * calling the <code>terminate()</code> method of the each agent. This method
    * is typically called by the request handler during server shut down or to
    * reinitialize the agent manager when the configuration file on the disk
    * changes.
    */
   void close();
}
