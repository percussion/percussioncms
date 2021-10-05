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

import com.percussion.data.PSExecutionData;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.design.objectstore.PSSystemValidationException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.PSServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Each request handled by the modify command handler will be processed by a
 * particular plan.  Each plan consists of a set of steps, which are executed
 * in sequence.
 */
public class PSModifyPlan
{
   /**
    * Constructs an empty plan.
    *
    * @param type One of the plan types, specified by:
    * <ul>
    * <li>{@link #TYPE_INSERT_PLAN}</li>
    * <li>{@link #TYPE_UPDATE_PLAN}</li>
    * <li>{@link #TYPE_UPDATE_NO_BIN_PLAN}</li>
    * <li>{@link #TYPE_UPDATE_SEQUENCE}</li>
    * <li>{@link #TYPE_DELETE_COMPLEX_CHILD}</li>
    * </ul>
    * It is not enforced that type is one of these values.
    */
   public PSModifyPlan(int type)
   {
      m_type = type;
   }

   /**
    * Returns the type of this plan.  See {@link #PSModifyPlan(int)} for plan
    * types.
    *
    * @return the type.
    */
   public int getType()
   {
      return m_type;
   }

   /**
    * Adds a step to this plan.
    *
    * @param step The step to add, may not be <code>null</code>.
    *
    * @throws IllegalArgumentException if step is <code>null</code>.
    */
   public void addModifyStep(IPSModifyStep step)
   {
      if (step == null)
         throw new IllegalArgumentException("step may not be null");

      m_steps.add(step);
   }


   /**
    * Executes each step of the plan
    *
    * @param data The execution data.  May not be <code>null</code>.
    * @param appName The name of the app this step will execute against.  May
    * not be <code>null</code>.
    *
    * @return The number of steps executed.
    *
    * @throws PSAuthorizationException if the user is not authorized to
    * perform a step.
    * @throws PSAuthenticationFailedException if the user cannot be
    * authenticated.
    * @throws PSSystemValidationException if the step does any validation and the
    * validation fails.
    * @throws PSInternalRequestCallException if there are any other errors.
    */
   public int execute(PSExecutionData data, String appName)
      throws PSInternalRequestCallException, PSAuthorizationException,
      PSAuthenticationFailedException, PSSystemValidationException
   {
      if (data == null || appName == null)
         throw new IllegalArgumentException(
            "data and appName must be supplied");

      int stepCount = 0;
      Iterator steps = m_steps.iterator();
      while (steps.hasNext())
      {
         IPSModifyStep step = (IPSModifyStep)steps.next();
         // need to synchronize on this in case another thread is checking
         synchronized(step)
         {
            if (step.getHandler() == null)
               step.setHandler(PSServer.getInternalRequestHandler(appName +
                  "/" + step.getName()));
         }
         step.execute(data);
         stepCount++;
      }

      return stepCount;
   }

   /**
    * Returns an Iterator over <code>zero</code> or more PSModifyStep objects,
    * which is all the steps that have been added to this plan.
    *
    * @return The Iterator, never <code>null</code>, may be empty.
    */
   public Iterator getAllSteps()
   {
      return m_steps.iterator();
   }

   /**
    * Appends all modify steps found in the supplied plan to this plan.
    *
    * @param modifyPlan The plan whose steps are to be appended to this plan.
    * May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if modifyPlan is <code>null</code>.
    */
   public void addAllSteps(PSModifyPlan modifyPlan)
   {
      Iterator steps = modifyPlan.getAllSteps();
      while (steps.hasNext())
      {
         IPSModifyStep step = (IPSModifyStep)steps.next();
         addModifyStep(step);
      }
   }
   
   /**
    * Sets a map of binary field names that may be processed by this plan.  This 
    * method takes ownership of the supplied map and changes should not be made 
    * to the map after this call returns.
    * 
    * @param binFields A Map of binary field names possibly updated by this 
    * plan, never <code>null</code>, may be empty.  Key is the field name as a
    * <code>String</code>, value is a <code>List</code> of 
    * <code>PSConditionalEvaluator</code> objects used to determine if the field 
    * is modified. If any of the evaluators return <code>true</code>, the
    * field should be considered modified.  If the list of evaluators is 
    * <code>null</code>, then the field should always be considered to have
    * been modified.
    */
   public void setBinaryFields(Map binFields)
   {
      if (binFields == null)
         throw new IllegalArgumentException("binFields may not be null");

      m_binFields = binFields;      
   }
   
   /**
    * Get a Map of binary field names processed by this plan.  See 
    * {@link #setBinaryFields(Map)} for more info.
    * 
    * @return The map of field names and evaluators, never <code>null</code>, 
    * may be empty.  The returned map should be treated read-only as it is the 
    * copy owned by this class.
    */
   public Map getBinaryFields()
   {
      return m_binFields;
   }
   
   /**
    * Determines if the specified plan type may update item data.
    * 
    * @param planType The plan type to check, should be one of the 
    * <code>TYPE_xxx</code> constants.
    * 
    * @return <code>true</code> if the type would modify the item's field data, 
    * <code>false</code> otherwise. 
    */
   public static boolean updatesItemData(int planType)
   {
      boolean updatesData = false;
      switch (planType)
      {
         case TYPE_INSERT_PLAN :
         case TYPE_UPDATE_PLAN :
         case TYPE_UPDATE_NO_BIN_PLAN :
            updatesData = true;
            break;

         default :
            break;
      }
      
      return updatesData;
   }

   /**
    * Constant specifying a plan used to process an insert.
    */
   public static final int TYPE_INSERT_PLAN = 0;

   /**
    * Constant specifying a plan used to process a full update.
    */
   public static final int TYPE_UPDATE_PLAN = 1;

   /**
    * Constant specifying a plan used to process a update that excludes any
    * binary fields.
    */
   public static final int TYPE_UPDATE_NO_BIN_PLAN = 2;

   /**
    * Constant specifying a plan used to process an update of a complex child's
    * sequences.
    */
   public static final int TYPE_UPDATE_SEQUENCE = 3;

   /**
    * Constant specifying a plan used to process a delete of a complex child
    * row.
    */
   public static final int TYPE_DELETE_COMPLEX_CHILD = 4;

   /**
    * Constant specifying a plan used to process a delete of an entire item.
    */
   public static final int TYPE_DELETE_ITEM = 5;

   /**
    * The type of this plan.  Set in the constructor.
    */
   private int m_type;

   /**
    * The list of steps, never <code>null</code>, may be empty.  Steps are added
    * by a call to {@link #addModifyStep(IPSModifyStep) addModifyStep()}
    */
   private ArrayList m_steps = new ArrayList();

   /**
    * A Map of binary field names possibly updated by this 
    * plan, never <code>null</code>, may be empty.  Modified by calls to 
    * {@link #setBinaryFields(Map)}, see that method for more info.
    */
   private Map m_binFields = new HashMap();

}
