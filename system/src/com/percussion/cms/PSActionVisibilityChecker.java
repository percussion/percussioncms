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
package com.percussion.cms;

import com.percussion.cms.objectstore.PSAction;
import com.percussion.cms.objectstore.PSActionVisibilityContext;
import com.percussion.cms.objectstore.PSActionVisibilityContexts;
import com.percussion.cms.objectstore.PSObjectPermissions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * This class knows how to determine if a given user should be allowed to see a
 * given {@link com.percussion.cms.objectstore.PSAction action} for a given
 * object in some state. It performs its work in different environments by
 * abstracting the state data into a class that can be implemented appropriate
 * to each environment.
 * <p>
 * An instance is created for a specific action by providing the action or the
 * visibility contexts to the ctor. The object state(s) are provided when the
 * check is performed.
 * 
 * @author paulhoward
 * @see PSActionVisibilityObjectState
 * @see PSActionVisibilityGlobalState
 */
public class PSActionVisibilityChecker
{
   /**
    * Create a checker based on the visibility contexts of some action.
    * 
    * @param actionUuid The id of the action that owns the supplied contexts. No
    * checking is done on this value. It is not used by this class, but may be
    * retrieved by the caller via the {@link #getActionUuid()} method.
    * 
    * @param supportsMultipleObjects <code>true</code> if the associated
    * action can be applied to multiple objects in a single request,
    * <code>false</code> otherwise.
    * 
    * @param ctx May be <code>null</code>, in which case the
    * <code>isVisible</code> methods will always return <code>true</code>.
    * The reference to the supplied param is kept as a member of this class, so
    * changes to this collection will affect the behavior of this class.
    */
   public PSActionVisibilityChecker(int actionUuid,
         boolean supportsMultipleObjects, PSActionVisibilityContexts ctx)
   {
      m_actionUuid = actionUuid;
      m_supportsMulti = supportsMultipleObjects;
      if (ctx != null && !ctx.isEmpty())
         m_contexts = ctx;
   }

   /**
    * Convenience method that gets the contexts from the supplied action and
    * calls the other ctor.
    * 
    * @param action Never <code>null</code>.
    * 
    * @throws NullPointerException If action is <code>null</code>.
    */
   public PSActionVisibilityChecker(PSAction action)
   {
      // default to false, then set below
      this(action.getId(), false, action.getVisibilityContexts());
      String val = action.getProperty(PSAction.PROP_MUTLI_SELECT);
      m_supportsMulti = val != null && val.equalsIgnoreCase("yes");
   }

   public boolean supportsMultipleObjects()
   {
      return m_supportsMulti;
   }

   /**
    * Perform the calculation to determine if the user operating in the supplied
    * environment(s) should be allowed to see the action supplied in the ctor.
    * This check includes a check as to whether the action supports multiple
    * objects. If you want to skip this as part of the check (because you want
    * to disable rather than hide the menu,) then submit each instance
    * seperately. You can determine the multiple object support by calling
    * {@link #supportsMultipleObjects()}.
    * 
    * @param globalState May be <code>null</code> or empty. If not supplied,
    * all global factors are skipped in the visibility calculation.
    * 
    * @param objStates May be <code>null</code> or empty. <code>null</code>
    * entries in the collection will be skipped.
    * 
    * @return <code>true</code> if the supplied param is <code>null</code>
    * or empty. <code>true</code> if all instances evaluate to having
    * visibility according to the contexts supplied in the ctor. Note that the
    * actual calculation is to determine if the action is hidden in a given
    * environment, therefore, if any one of the supplied environments would hide
    * the action, <code>false</code> is returned.
    */
   public boolean isVisible(PSActionVisibilityGlobalState globalState,
         Collection<PSActionVisibilityObjectState> objStates)
   {
      if (objStates != null && objStates.size() > 1 && !supportsMultipleObjects())
         return false;
      if (m_contexts == null)
         return true;

      boolean visible = true;
      if (globalState != null)
         visible = checkGlobalState(globalState);
      if (objStates != null && visible)
         visible = checkObjectState(objStates);
      return visible;
   }

   private boolean checkObjectState(
         Collection<PSActionVisibilityObjectState> objStates)
   {
      for (PSActionVisibilityObjectState os : objStates)
      {
         if (os == null)
            continue;
         Iterator ctxIter = m_contexts.iterator();
         while (ctxIter.hasNext())
         {
            PSActionVisibilityContext vc = (PSActionVisibilityContext) ctxIter.next();
            String context = vc.getName();
            
            Iterator valIter = vc.iterator();
            while(valIter.hasNext())
            {
               String value = (String)valIter.next();

               if (context
                     .equals(PSActionVisibilityContext.VIS_CONTEXT_FOLDER_SECURITY))
               {
                  int access = PSObjectPermissions.translateAccess(value);

                  int higherAccessLevel = PSObjectPermissions
                        .getHigherAccessLevel(access);

                  PSObjectPermissions perm = os.getFolderPermissions();

                  if (perm != null && !perm.hasAccess(higherAccessLevel))
                     return false;
               }
               else if (context.equals(
                     PSActionVisibilityContext.VIS_CONTEXT_CHECKOUT_STATUS))
               {
                  // The SomeOneElse and MySelf check is for backward
                  // compatibility. These are deprecated.
                  String coStatus = os.getCheckoutStatus();
                  if (value.equalsIgnoreCase(coStatus)
                        || (value.equalsIgnoreCase("MySelf") 
                              && coStatus.equalsIgnoreCase("Checked Out By Me"))
                        || (value.equalsIgnoreCase("SomeOneElse") 
                              && coStatus.equalsIgnoreCase("Checked Out")))
                  {
                     return false;
                  }
               }
               else if (context
                     .equals(PSActionVisibilityContext.VIS_CONTEXT_PUBLISHABLE_TYPE))
               {
                  if (value.equalsIgnoreCase(os.getPublishableType()))
                     return false;
               }
               else
               {
                  if (context.equals(
                        PSActionVisibilityContext.VIS_CONTEXT_ASSIGNMENT_TYPE))
                  {
                     if (convert(value) == os.getAssignmentType())
                        return false;
                  }
                  else if (context.equals(
                        PSActionVisibilityContext.VIS_CONTEXT_CONTENT_TYPE))
                  {
                     if (convert(value) == os.getContentTypeUuid())
                        return false;
                  }
                  else if (context.equals(
                        PSActionVisibilityContext.VIS_CONTEXT_OBJECT_TYPE))
                  {
                     if (convert(value) == os.getObjectType())
                        return false;
                  }
                  else if (context.equals(
                        PSActionVisibilityContext.VIS_CONTEXT_WORKFLOWS_TYPE))
                  {
                     if (convert(value) == os.getWorkflowAppUuid())
                        return false;
                  }
               }
            }
         }
      }
      return true;
   }

   /**
    * Change a string to its integer representation.
    * 
    * @param num Either blank or an integer.
    * 
    * @return -1 if <code>num</code> is blank, otherwise the number that was
    * represented by the supplied string.
    * 
    * @throws NumberFormatException if <code>num</code> is not blank and is not
    * an integer.
    */
   private int convert(String num)
   {
      int ctxValue = -1;
      if (StringUtils.isNotBlank(num))
         ctxValue = Integer.parseInt(num);
      return ctxValue;
   }
   
   /**
    * Checks the values in the supplied state against those in the local
    * contexts. If a match is found (meaning the action should be hidden,) then
    * <code>false</code> is returned. Assumes that {@link #m_contexts} is not
    * <code>null</code>.
    * 
    * @param globalState Assumed not <code>null</code>.
    */
   private boolean checkGlobalState(PSActionVisibilityGlobalState globalState)
   {
      Iterator iter = m_contexts.iterator();
      while (iter.hasNext())
      {
         PSActionVisibilityContext vc = (PSActionVisibilityContext) iter.next();
         String context = vc.getName();

         if (context.equals(PSActionVisibilityContext.VIS_CONTEXT_COMMUNITY))
         {
            Iterator valIter = vc.iterator();
            String communityId = String.valueOf(globalState
                  .getCommunityUuid());
            while (valIter.hasNext())
            {
               String value = (String) valIter.next();
               if (value.equals(communityId))
               {
                  return false;
               }
            }
         }
         else if (context
               .equals(PSActionVisibilityContext.VIS_CONTEXT_ROLES_TYPE))
         {
            Iterator roleIter = globalState.getRoles().iterator();
            Iterator valIter = vc.iterator();
            List<String> roleList = new ArrayList<>();
            List<String> valueList = new ArrayList<>();
            while (roleIter.hasNext())
            {
               String role = (String) roleIter.next();
               roleList.add(role);
            }
            while (valIter.hasNext())
            {
               String value = (String) valIter.next();
               valueList.add(value);
            }
            if (valueList.containsAll(roleList))
               return false;
         }
         else if (context
               .equals(PSActionVisibilityContext.VIS_CONTEXT_LOCALES_TYPE))
         {
            Iterator valIter = vc.iterator();
            String locale = globalState.getLocale();
            while (valIter.hasNext())
            {
               String value = (String) valIter.next();
               if (value.equalsIgnoreCase(locale))
                  return false;
            }
         }
         else if (context
               .equals(PSActionVisibilityContext.VIS_CONTEXT_CLIENT_CONTEXT))
         {
            // drag drop, multi-select, etc..
            Iterator valIter = vc.iterator();
            while (valIter.hasNext())
            {
               String value = (String) valIter.next();
               if (value.equalsIgnoreCase(globalState.getClientContext()))
                  return false;
            }
         }
      }
      return true;
   }

   /**
    * Convenience method that calls
    * {@link #isVisible(PSActionVisibilityGlobalState, Collection)} after wrapping
    * the supplied instance.
    * 
    * @param ctxInstance May be <code>null</code> or empty.
    */
   public boolean isVisible(PSActionVisibilityGlobalState globalState,
         PSActionVisibilityObjectState ctxInstance)
   {
      return isVisible(globalState, Collections.singleton(ctxInstance));
   }

   /**
    * @return The id of the action supplied in the ctor.
    */
   public int getActionUuid()
   {
      return m_actionUuid;
   }

   /**
    * A flag that indicates whether the associated action can accept multiple
    * objects in a single request for processing. <code>true</code> means it
    * can.
    */
   private boolean m_supportsMulti;

   /**
    * This is kept for the user's convenience. It is never directly used by this
    * class. Set in the ctor, then never modified.
    */
   private int m_actionUuid;

   /**
    * Set in ctor, then never modified. May be <code>null</code>, but never
    * empty.
    */
   private PSActionVisibilityContexts m_contexts;
}
