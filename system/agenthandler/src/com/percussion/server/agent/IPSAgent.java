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

import org.w3c.dom.Element;

/**
 * This is the interface all Rhythmyx server agents must implement.
 */
public interface IPSAgent
{
   /**
    * This method is the first one to be called by the Agent Manager just after
    * instantiating the implementing class object. This is called only once in
    * the object's life cycle.
    * @param configData - DOM Element representing the configuration data
    * defined in the configuration file. The implementor dictates the DTD for
    * the element depending on what he needs.
    * @throws PSAgentException if initialization fails for any reason
    */
   void init(Element configData) throws PSAgentException;

   /**
    * This method is the engine of the agent and called any time a service from
    * this agent is requested by the Agent Manager.
    * @param action - name of the action to be excuted by the agent.
    * @param params - Map (name, value pairs) of all parameters from the
    * request
    * @param response <code>IPSAgentHandlerResponse</code> object that can be
    * used to set the results of execution of the action. If the requested
    * action is not implmented or enough parameters are not supplied to execute
    * the action, the result can be set to error.
    */
   void executeAction(String action, Map params,
      IPSAgentHandlerResponse response);

   /**
    * This method is called by the Agent Manager while shuttingdown. May be
    * used to do any cleaning.
    */
   void terminate();
}
