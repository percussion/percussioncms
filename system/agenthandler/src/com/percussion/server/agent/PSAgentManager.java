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

import com.percussion.server.PSConsole;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This class implements the interface <code>IPSAgentManager</code> and does the
 * following:
 * <ul>
 * <li>Instantiates and initializes all the agents from the configurtion XML
 * document. If instantiation fails for any agent that agent will be ignored
 * and initialization proceeds with the next in the document. Any request that
 * comes for the services from the uninitialized agent shall be handled as
 * though there is no such agent exists.
 * <li>The request handler always forwards the actions from a specified agent
 * to the Agent Manager (i.e. object of this class) via the method
 * <code>handleAction()</code>. The Agent Manager then requests the action from
 * the appropriate agent.
 * <li>Can close all agents initialized by calling the <code>terminate</code>
 * method of the agents.
 * </ul>
 */
public class PSAgentManager implements IPSAgentManager
{
   /**
    * Default constructor. Make it private to avoid instantiation in this form.
    */
   private PSAgentManager()
   {
   }

   /**
    * Constructor. All agents registered in the configuration document are
    * created, initialized and stored in a hashmap. Any agent that fails to be
    * initialized is ignored.
    * @param configDoc - the configuration XML document, must not be
    * <code>null</code>.
    */
   PSAgentManager(Document configDoc)
   {
      if(configDoc == null)
      {
         throw new IllegalArgumentException(
            "Configuration document for the agents must not be empty in " +
            "the constructor of Agent Manager");
      }

      NodeList nl =
         configDoc.getElementsByTagName(IPSDTDAgentManagerConfig.ELEM_AGENT);

      //No agents are configured. That is fine!
      if(nl == null || nl.getLength() < 1)
         return;

      Element elemAgent = null;
      for(int i=0; i<nl.getLength(); i++)
      {
         elemAgent = (Element)nl.item(i);
         try
         {
            createAgent(elemAgent);
         }
         catch(Exception e) //for any exception
         {
            PSConsole.printMsg(PSAgentRequestHandler.HANDLER, e.getMessage());
         }
      }
   }

   /**
    * Helper function that creates the agent object and puts in the hashmap.
    * @param elemAgent the configuration data element for the agent to be
    * created
    * @throws ClassNotFoundException if the agent implementation class is not
    * found in the classpath
    * @throws InstantiationException if the object is not instantiated from the
    * class
    * @throws IllegalAccessException if the object fails to be instantiated for
    * security reasons
    * @throws PSAgentException if Agent creation fails for any other reason
    * @throws IllegalArgumentException if the  argument is <code>null</code>
    */
   private void createAgent(Element elemAgent)
      throws   ClassNotFoundException,
               IllegalAccessException,
               InstantiationException,
               PSAgentException
   {
      if(elemAgent == null)
      {
         throw new IllegalArgumentException(
            "elemAgent object must not be empty in createAgent() method " +
            "of Agent Manager");
      }
      String name =
         elemAgent.getAttribute(IPSDTDAgentManagerConfig.ATTRIB_NAME);
      if(name == null || name.trim().length() < 1)
      {
         throw new PSAgentException(
            "Agent name attribute in its configuration data element must " +
            "have a valid value createAgent() method of Agent Manager");
      }
      String classs =
         PSUtils.getElemValue(elemAgent, IPSDTDAgentManagerConfig.ELEM_CLASS);
      if(classs == null || classs.trim().length() < 1)
      {
         throw new PSAgentException(
            "Implementing class name for the agent '" + name +
            "' must not be empty in createAgent() method " +
            "of Agent Manager");
      }
      IPSAgent agent =
            (IPSAgent)Class.forName(classs).newInstance();

      if(agent == null)
      {
         throw new PSAgentException(
            "Agent object for the agent '" + name +
            "' must not be empty in createAgent() method " +
            "of Agent Manager");
      }
      agent.init(elemAgent);

      m_Agents.put(name, agent);
   }

   /*
    * Implementation of the methods from the interface
    * <code>IPSAgentManager</code>
    */
   public void handleAction(Map params, IPSAgentHandlerResponse response)
   {
      if(response == null)
      {
         throw new IllegalArgumentException(
            "response object must not be empty in handleAction() method " +
            "of Agent Manager");
      }
      String msg = null;
      if(params == null)
      {
         msg = "Agent name must not be empty for Agent Manager to " +
            "request an action from an agent";
         response.setResponse(response.RESPONSE_TYPE_ERROR, msg);
      }
      String agentname = null;
      if(params.containsKey(IPSDTDAgentHandlerResponse.HANDLER_PARAM_AGENT_NAME))
      {
         agentname = params.get(
            IPSDTDAgentHandlerResponse.HANDLER_PARAM_AGENT_NAME).toString();
      }
      if(agentname == null)
      {
         msg = "Agent name must not be empty for Agent Manager to " +
            "request an action from an agent";
         response.setResponse(response.RESPONSE_TYPE_ERROR, msg);
      }
      IPSAgent agent = getAgentByName(agentname);

      if(agent == null)
      {
         msg = "Agent '" + agentname +
            "' is not registered by the Agent Manager";
         response.setResponse(response.RESPONSE_TYPE_ERROR, msg);
      }
      String agentaction = null;
      if(params.containsKey(IPSDTDAgentHandlerResponse.HANDLER_PARAM_ACTION))
      {
         agentaction = params.get(
            IPSDTDAgentHandlerResponse.HANDLER_PARAM_ACTION).toString();
      }
      if(agentaction == null)
      {
         msg = "Agent action name must not be empty for Agent Manager to " +
            "request an action from an agent";
         response.setResponse(response.RESPONSE_TYPE_ERROR, msg);
      }
      agent.executeAction(agentaction, params, response);
   }

   /*
    * Implementation of the methods from the interface
    * <code>IPSAgentManager</code>
    */
   public void close()
   {
      Collection coll = m_Agents.values();
      Iterator iter = coll.iterator();
      while(iter.hasNext())
      {
         IPSAgent agent = (IPSAgent)iter.next();
         agent.terminate();
         agent = null;
      }
      m_Agents.clear();
      m_Agents = null;
   }

   /**
    * Search for the agent by name in the HashMap and return.
    * @param name of the agent to earch in the map
    * @return <code>IPSAgent</code> that corresponds to the key value of the
    * agent name. Shall be <code>null</code> if no agent exists by this name.
    */
   public IPSAgent getAgentByName(String agentname)
   {
      if(agentname == null)
      {
         throw new IllegalArgumentException(
            "Agent name must not be empty in getAgentByName() method " +
            "of Agent Manager");
      }
      if(m_Agents.containsKey(agentname))
         return(IPSAgent)m_Agents.get(agentname);

      return null;
   }

   /**
    * Hashmap of the all agents that are successfully initialized. The key for
    * the map shall be the agent name and the value shall be the
    * <code>IPSAgent</code> object
    */
   protected HashMap m_Agents = new HashMap();
}
