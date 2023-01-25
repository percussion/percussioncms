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
