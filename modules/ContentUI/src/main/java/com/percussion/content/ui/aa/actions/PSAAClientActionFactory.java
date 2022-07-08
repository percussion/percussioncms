/*
 *     Percussion CMS
 *     Copyright (C) 1999-2022 Percussion Software, Inc.
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
