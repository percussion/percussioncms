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
package com.percussion.content.ui.aa.actions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * The action factory is a singleton class that is responsible for
 * retrieving requested aa client actions. Actions are instantiated
 * via reflection.
 */
public class PSAAClientActionFactory
{
   
   // private ctor to inhibit external instansiation
   private PSAAClientActionFactory()
   {
   }
   
   /**
    * Retrieve the singleton instance of this factory.
    * @return the instance, never <code>null</code>.
    */
   public static final PSAAClientActionFactory getInstance()
   {
      if(ms_instance == null)
         ms_instance = new PSAAClientActionFactory();
      return ms_instance;
   }
   
   /**
    * Returns the action for the specified action type. Actions are expected
    * to be in the com.percussion.content.ui.aa.actions.impl package and must have 
    * the naming convention of PSXXXAction.
    * @param actionType the action type of the action to be returned.
    * @return the <code>IPSAAClientAction</code>. May be
    * <code>null</code> if the action type specified does not
    * exist.
    */
   public IPSAAClientAction getAction(String actionType)
   {
      IPSAAClientAction action = m_actions.get(actionType);
      if(action == null)
      {
          // Use reflection to instantiate the class
         String pack = getClass().getPackage().getName();
         String className = pack + ".impl.PS" + actionType + "Action";
         
         try
         {
            Class clazz = Class.forName(className);
            action = (IPSAAClientAction) clazz.newInstance();
            m_actions.put(actionType, action);
         }
         catch (ClassNotFoundException ignore)
         {
            // ignore
         }
         catch (InstantiationException e)
         {
            ms_log.error(e.getLocalizedMessage(), e);
         }
         catch (IllegalAccessException e)
         {
            ms_log.error(e.getLocalizedMessage(), e);
         }
         
      }
      return action;
   }   
   
   
   /**
    * The singleton instance of this factory. Initialized in
    * {@link #getInstance()}. Never <code>null</code> after that.
    */
   private static PSAAClientActionFactory ms_instance;
   private Map<String, IPSAAClientAction> m_actions =
      new HashMap<String, IPSAAClientAction>();
   
   /**
    * The logger for this class
    */
   private static Log ms_log = LogFactory.getLog(PSAAClientActionFactory.class);
   

}
