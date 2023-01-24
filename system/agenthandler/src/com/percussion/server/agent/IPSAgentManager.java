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
