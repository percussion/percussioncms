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

package com.percussion.services.workflow.data;

import static org.apache.commons.lang.Validate.notNull;

import java.util.ArrayList; 
import java.util.List;

import com.percussion.services.workflow.data.PSTransitionHib.TransitionType;

/**
 * The utility class used to manage persisted and non-persisted transition objects.
 * such as copy non-persisted objects to persisted objects and convert
 * persisted objects to non-persisted objects.
 * <p>
 * Note, these transformation are needed to keep "hibernate" working properly.
 * Otherwise we are having trouble with hibernate for the CRUD operation
 * when using 2 classes (PSTransition & PSAgingTransition) to share the same table.
 * 
 * @author YuBingChen
 */
class PSTransformTransitionUtils
{
   /**
    * Merging the non-persisted (and non-aging) transitions to persisted transitions. 
    * 
    * @param transitions the non-persisted transitions, not <code>null</code>, may be empty.
    * @param hibs the persisted transitions, not <code>null</code>, may be empty.
    */
   static void copyTransitions(List<PSTransition> transitions, List<PSTransitionHib> hibs)
   {
      notNull(transitions);
      notNull(hibs);
      
      TransitionsCopier merger = new TransitionsCopier();
      merger.copyList(transitions, hibs);
   }

   /**
    * Merging the non-persisted (and aging) transitions to persisted transitions. 
    * 
    * @param transitions the non-persisted transitions, not <code>null</code>, may be empty.
    * @param hibs the persisted transitions, not <code>null</code>, may be empty.
    */
   static void copyAgingTransitions(List<PSAgingTransition> transitions, List<PSTransitionHib> hibs)
   {
      AgingTransitionsCopier merger = new AgingTransitionsCopier();
      merger.copyList(transitions, hibs);
   }

   /**
    * Converts and filters the given persisted transitions to non-persisted transitions.
    * 
    * @param transitionHibs the persisted transitions, not <code>null</code>, but may be empty.
    * @param type the transition type to filter with.
    * 
    * @return the converted transitions, not <code>null</code>, but may be empty.
    */
   static List<? extends PSTransitionBase> convertTransitions(List<PSTransitionHib> transitionHibs, TransitionType type)
   {
      List<PSTransitionBase> result = new ArrayList<>();
      for (PSTransitionHib hib : transitionHibs)
      {
         if (type == hib.getTransitionType())
         {
            if (type == TransitionType.TRANSITION)
               result.add(getTransition(hib));
            else
               result.add(getAgingTransition(hib));
         }
      }
      return result;
   }
   
   /**
    * Copy non-persisted notification to persisted notification objects or vice versa.
    * 
    * @param src the source objects, assumed not <code>null</code>, may be empty.
    * @param tgt the target objects, assumed not <code>null</code>, may be empty.
    */
   private static void copyNotifications(List<PSNotification> src, List<PSNotification> tgt)
   {
      NotificationCopier merger = new NotificationCopier();
      merger.copyList(src, tgt);
   }

   /**
    * Copy non-persisted transition-role to persisted transition-role objects or vice versa.
    * 
    * @param src the source objects, assumed not <code>null</code>, may be empty.
    * @param tgt the target objects, assumed not <code>null</code>, may be empty.
    */
   private static void copyTransitionRoles(List<PSTransitionRole> src, List<PSTransitionRole> tgt)
   {
      RolesCopier merger = new RolesCopier();
      merger.copyList(src, tgt);
   }
   
   /**
    * Converts the non-persisted aging-transition object to the persisted transition object.
    * 
    * @param ageTrans the aging-transition object, assumed not <code>null</code>.
    * 
    * @return the converted transition, not <code>null</code>.
    */
   private static PSTransitionHib getTransitionHib(PSAgingTransition ageTrans)
   {
      PSTransitionHib hib = new PSTransitionHib();
      hib.setTransitionType(TransitionType.AGING);
      copyBaseProperties(ageTrans, hib, true);
      copyAgingProperties(ageTrans, hib);
      return hib;
   }

   /**
    * Converts the non-persisted transition object to the persisted transition object.
    * @param trans the non-persisted transition object, assumed not <code>null</code>.
    * @return the converted object, not <code>null</code>.
    */
   private static PSTransitionHib getTransitionHib(PSTransition trans)
   {
      PSTransitionHib hib = new PSTransitionHib();
      hib.setTransitionType(TransitionType.TRANSITION);
      copyTransition(trans, hib, true);
      
      return hib;
   }
   
   /**
    * Converts the persisted transition object to non-persisted transition
    * @param hib the persisted transition, assumed not <code>null</code>.
    * @return the converted non-persisted transition, not <code>null</code>.
    */
   private static PSTransition getTransition(PSTransitionHib hib)
   {
      PSTransition trans = new PSTransition();
      copyBaseProperties(hib, trans, true);
      copyTransitionProperties(hib, trans);

      return trans;
   }
   
   /**
    * Converts the given non-persisted transition objects to the persisted transition objects.
    * 
    * @param transitions the non-persisted transition objects, assumed not <code>null</code>, but may be empty.
    * @param transitionType indicates the type of non-persisted transition objects to be converted, 
    * assumed not <code>null</code>.
    * 
    * @return the converted persisted objects, never <code>null</code>, but may be empty. 
    */
   private static List<PSTransitionHib> getTransitionHibs(List<? extends PSTransitionBase> transitions, TransitionType transitionType)
   {
      PSTransitionHib hib;
      List<PSTransitionHib> hibs = new ArrayList<>();
      for (PSTransitionBase trans : transitions)
      {
         if (transitionType == TransitionType.TRANSITION)
            hib = getTransitionHib((PSTransition) trans);
         else
            hib = getTransitionHib((PSAgingTransition) trans);
         
         hibs.add(hib);
      }
      return hibs;
   }

   /**
    * Converts the persisted transition to (non-persisted) aging transition.
    * @param hib the persisted transition in question, not <code>null</code>.
    * @return the converted aging transition, never <code>null</code>.
    */
   private static PSAgingTransition getAgingTransition(PSTransitionHib hib)
   {
      PSAgingTransition ageTrans = new PSAgingTransition();
      copyBaseProperties(hib, ageTrans, true);
      copyAgingProperties(hib, ageTrans);
      
      return ageTrans;
   }
   
   /**
    * Copy the aging transition to the persisted transition.
    * 
    * @param src the aging transition, assumed not <code>null</code>.
    * @param hib the persisted transition, assumed not <code>null</code>.
    * @param isCopyId indicates if the copy includes coping ID or not.
    * <code>true</code> will copy ID; otherwise ID will not be copied.
    */
   private static void copyAgingTransition(PSAgingTransition src, PSTransitionHib hib, boolean isCopyId)
   {
      copyBaseProperties(src, hib, isCopyId);
      copyAgingProperties(src, hib);
   }
   
   /**
    * Same as {@link #copyAgingProperties(IPSAgingTransition, IPSAgingTransition)}, except
    * this is copy non-aging transition to persisted transition.
    */
   private static void copyTransition(PSTransition src, PSTransitionHib hib, boolean isCopyId)
   {
      copyBaseProperties(src, hib, isCopyId);
      copyTransitionProperties(src, hib);
   }
   
   /**
    * Copy aging transition specific properties from the source to target object.
    * @param src the source object, assumed not <code>null</code>.
    * @param tgt the target object, assumed not <code>null</code>.
    */
   private static void copyAgingProperties(IPSAgingTransition src, IPSAgingTransition tgt)
   {
      tgt.setInterval(src.getInterval());
      tgt.setSystemField(src.getSystemField());
      tgt.setType(src.getType());
   }
   
   /**
    * Copy the common properties of the transition type from source to target object.
    * @param src the source object, assumed not <code>null</code>.
    * @param tgt the target object, assumed not <code>null</code>.
    * @param isCopyId indicates if the copy includes coping ID or not.
    * <code>true</code> will copy ID; otherwise ID will not be copied.
    */
   private static void copyBaseProperties(IPSTransitionBase src, IPSTransitionBase tgt, boolean isCopyId)
   {
      if (isCopyId)
         tgt.setGUID(src.getGUID());
      
      tgt.setDescription(src.getDescription());
      tgt.setLabel(src.getLabel());
      copyNotifications(src.getNotifications(), tgt.getNotifications());
      tgt.setStateId(src.getStateId());
      tgt.setToState(src.getToState());
      tgt.setTransitionAction(src.getTransitionAction());
      tgt.setTrigger(src.getTrigger());
      tgt.setWorkflowId(src.getWorkflowId());
   }
   
   /**
    * Copy normal transition specific properties from the source to target object.
    * @param src the source object, assumed not <code>null</code>.
    * @param tgt the target object, assumed not <code>null</code>.
    */
   private static void copyTransitionProperties(IPSTransition src, IPSTransition tgt)
   {
      tgt.setAllowAllRoles(src.isAllowAllRoles());
      tgt.setApprovals(src.getApprovals());
      tgt.setDefaultTransition(src.isDefaultTransition());
      tgt.setRequiresComment(src.getRequiresComment());
      copyTransitionRoles(src.getTransitionRoles(), tgt.getTransitionRoles());
   }
   
   /**
    * The class used to copy a list of non-persisted transition to a list of persisted transitions.
    * 
    * @author YuBingChen
    *
    */
   private static class TransitionsCopier extends PSListCopier<PSTransition, PSTransitionHib>
   {
      /*
       * (non-Javadoc)
       * @see com.percussion.services.workflow.data.PSListCopier#copy(com.percussion.services.catalog.IPSCatalogIdentifier, com.percussion.services.catalog.IPSCatalogIdentifier)
       */
      protected void copy(PSTransition trans, PSTransitionHib hib)
      {
         copyTransition(trans, hib, false);
      }
      
      /*
       * (non-Javadoc)
       * @see com.percussion.services.workflow.data.PSListCopier#convertList(java.util.List)
       */
      protected List<PSTransitionHib> convertList(List<PSTransition> transList)
      {
         return getTransitionHibs(transList, TransitionType.TRANSITION);
      }

      /*
       * (non-Javadoc)
       * @see com.percussion.services.workflow.data.PSListCopier#isTargetElement(com.percussion.services.catalog.IPSCatalogIdentifier)
       */
      protected boolean isTargetElement(PSTransitionHib hib)
      {
         return hib.getTransitionType() == TransitionType.TRANSITION;
      }
   }
   
   /**
    * The copier used to copy a list of non-persisted aging transitions to a list of persisted transition objects.
    * @author YuBingChen
    *
    */
   private static class AgingTransitionsCopier extends PSListCopier<PSAgingTransition, PSTransitionHib>
   {
      protected void copy(PSAgingTransition trans, PSTransitionHib hib)
      {
         copyAgingTransition(trans, hib, false);
      }
      
      protected List<PSTransitionHib> convertList(List<PSAgingTransition> transList)
      {
         return getTransitionHibs(transList, TransitionType.AGING);
      }
      
      protected boolean isTargetElement(PSTransitionHib hib)
      {
         return hib.getTransitionType() == TransitionType.AGING;
      }
   }

   /**
    * The copier is used to copy a list of non-persisted notification objects to a list of persisted transition objects.
    * 
    * @author YuBingChen
    *
    */
   private static class NotificationCopier extends PSListCopier<PSNotification, PSNotification>
   {
      protected List<PSNotification> convertList(List<PSNotification> transList)
      {
         return transList;
      }
      
      protected void copy(PSNotification src, PSNotification tgt)
      {
         tgt.copy(src);
      }
   }

   /**
    * The copier is used to copy a list of non-persisted transition-role objects to a list of persisted transition-role objects.
    * 
    * @author YuBingChen
    *
    */
   private static class RolesCopier extends PSListCopier<PSTransitionRole, PSTransitionRole>
   {
      protected List<PSTransitionRole> convertList(List<PSTransitionRole> roleList)
      {
         return roleList;
      }
      
      protected void copy(PSTransitionRole src, PSTransitionRole tgt)
      {
         tgt.copy(src);
      }
   }
}
