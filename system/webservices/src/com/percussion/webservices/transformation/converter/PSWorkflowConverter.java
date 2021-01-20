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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.webservices.transformation.converter;

import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.workflow.data.PSNotificationDef;
import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSTransition;
import com.percussion.services.workflow.data.PSTransitionBase;
import com.percussion.services.workflow.data.PSTransitionRole;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.webservices.system.PSWorkflowRole;
import com.percussion.webservices.system.Transition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.Converter;

/**
 * Converts between {@link com.percussion.services.workflow.data.PSWorkflow} and
 * {@link com.percussion.webservices.system.PSWorkflow}.  Does additional
 * conversion of child transitions.
 */
public class PSWorkflowConverter extends PSConverter
{
   /**
    * See {@link PSConverter#PSConverter(BeanUtilsBean) super()}
    * 
    * @param beanUtils
    */
   public PSWorkflowConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);
      m_specialProperties.add("id");
      m_specialProperties.add("initialState");
      m_specialProperties.add("typedId");
      m_specialProperties.add("notificationDefs");
   }

   @Override
   public Object convert(Class type, Object value)
   {
      if (value == null)
         return null;
      
      if (isClientToServer(value))
      {
         // only reading from server is supported
         throw new ConversionException(
            "Conversion not supported from client to server");
      }
      else
      {
         try
         {
            PSWorkflow srcwf = (PSWorkflow) value;
            com.percussion.webservices.system.PSWorkflow tgtwf = 
               (com.percussion.webservices.system.PSWorkflow) 
               super.convert(type, value);
            
            // handle id, this is the long value as it may be used for search
            tgtwf.setId(srcwf.getGUID().longValue());
            
            // handle the typed id, includes all id information 
            tgtwf.setTypedId(new PSDesignGuid(srcwf.getGUID()).getValue());
            
            // handle notification definitions
            List<PSNotificationDef> notificationDefs = srcwf.getNotificationDefs();
            com.percussion.webservices.system.PSNotificationDef[] notificationDefsArray = 
               new com.percussion.webservices.system.PSNotificationDef[notificationDefs.size()];
            int index = 0;
            for (PSNotificationDef notificationDef : notificationDefs)
            {
               Converter converter = getConverter(notificationDef.getClass());
               com.percussion.webservices.system.PSNotificationDef def = 
                  (com.percussion.webservices.system.PSNotificationDef) converter.convert(
                  com.percussion.webservices.system.PSNotificationDef.class, 
                  notificationDef);
               notificationDefsArray[index++] = def;
            }
            tgtwf.setNotifications(notificationDefsArray);
            
            // now handle child exceptions:
            Map<Long, PSState> stateMap = getStateMap(srcwf);
            Map<Long, PSWorkflowRole> roleMap = getRoleMap(tgtwf);
            
            com.percussion.webservices.system.PSState[] states = 
               tgtwf.getStates();
            for (int i = 0; i < states.length; i++)
            {
               fixupTransitions(srcwf.getStates().get(i).getTransitions(),
                  states[i].getTransitions(), stateMap, roleMap);
               fixupTransitions(srcwf.getStates().get(i).getAgingTransitions(),
                  states[i].getAgingTransitions(), stateMap, roleMap);
            }
            
            return tgtwf;
         }
         catch (Exception e)
         {
            throw new ConversionException(value.toString(), e);
         }
      }
   }

   /**
    * Sets missing state names and role collections on the supplied transitions.
    * 
    * @param srcTrans The source transitions, assumed not <code>null</code>. 
    * @param tgtTrans The target transitions to fixup, assumed not 
    * <code>null</code> and to have the same number of elements as the source
    * transition list.
    * @param stateMap Map of state ids to source state objects, assumed not 
    * <code>null</code> and to contain all states referenced by the source
    * transitions.
    * @param roleMap Map of role ids to target workflow roles, assumed not
    * <code>null</code> and to have all roles referenced by the source
    * transitions.
    */
   private void fixupTransitions(List<? extends PSTransitionBase> srcTrans, 
      Transition[] tgtTrans, Map<Long, PSState> stateMap, 
      Map<Long, PSWorkflowRole> roleMap)
   {
      for (int i = 0; i < tgtTrans.length; i++)
      {
         PSTransitionBase src = srcTrans.get(i);
         Transition tgt = tgtTrans[i];
         
         // set to and from state names
         tgt.setFromState(getStateName(src.getStateId(), stateMap));
         tgt.setToState(getStateName(src.getToState(), stateMap));
         
         // set transition roles 
         if (src instanceof PSTransition)
         {
            fixupTransitionRoles((PSTransition)src, 
               (com.percussion.webservices.system.PSTransition)tgt, 
               roleMap);
         }
      }
   }

   /**
    * Sets the list of workflow roles on the target transition using the source 
    * transition data and the supplied role map.
    * 
    * @param src The source transition, assumed not <code>null</code>.
    * @param tgt The target transition, assumed not <code>null</code>.
    * @param roleMap Map of role ids to target workflow roles, assumed not
    * <code>null</code> and to have all roles referenced by the source
    * transition.
    */
   private void fixupTransitionRoles(PSTransition src, 
      com.percussion.webservices.system.PSTransition tgt, Map<Long, 
      PSWorkflowRole> roleMap)
   {
      List<PSTransitionRole> transRoleList = src.getTransitionRoles();
      PSWorkflowRole[] roles = new PSWorkflowRole[transRoleList.size()];
      for (int j = 0; j < roles.length; j++)
      {
         long roleId = transRoleList.get(j).getRoleId();
         roles[j] = roleMap.get(roleId);
         if (roles[j] == null)
         {
            throw new ConversionException(
               "No workflow role found with id: " + roleId);
         }
      }
      
      tgt.setRoles(roles);
   }

   /**
    * Get the name of the specified state from the supplied map.
    * 
    * @param stateId The id to get.
    * @param stateMap Map of state ids to source states, assumed not 
    * <code>null</code>.
    * 
    * @return The specified state, never <code>null</code>.
    */
   private String getStateName(long stateId, Map<Long, PSState> stateMap)
   {
      PSState state = stateMap.get(stateId);
      if (state == null)
      {
         throw new ConversionException("No state found with id: " + stateId);
      }
      return state.getName();
   }

   /**
    * Create a map target workflow roles.
    * 
    * @param wf The target workflow object, may not be <code>null</code>.
    * 
    * @return The map of role id to workflow role, never <code>null</code>.
    */
   public static Map<Long, PSWorkflowRole> getRoleMap(
      com.percussion.webservices.system.PSWorkflow wf)
   {
      if (wf == null)
         throw new IllegalArgumentException("wf may not be null");

      Map<Long, PSWorkflowRole> roleMap = new HashMap<Long, PSWorkflowRole>();
      
      for (PSWorkflowRole role : wf.getRoles())
      {
         roleMap.put(role.getId(), role);
      }
      
      return roleMap;
   }

   /**
    * Create a map of source states.
    * 
    * @param wf The source workflow object, may not be <code>null</code>.
    * 
    * @return The map, never <code>null</code>.
    */
   public static Map<Long, PSState> getStateMap(PSWorkflow wf)
   {
      if (wf == null)
         throw new IllegalArgumentException("wf may not be null");
      
      Map<Long, PSState> stateMap = new HashMap<Long, PSState>();
      for (PSState state : wf.getStates())
      {
         stateMap.put(state.getStateId(), state);
      }
      
      return stateMap;
   }

}

