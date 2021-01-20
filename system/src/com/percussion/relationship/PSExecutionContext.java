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
package com.percussion.relationship;

import com.percussion.data.PSExecutionData;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.relationship.effect.PSRelationshipEffectTestResult;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * The base class for all effect execution context objects. This is the default
 * implementation.
 */
public class PSExecutionContext implements IPSExecutionContext
{
   /**
    * Convenience ctor that calls
    * {@link #PSExecutionContext(int, PSExecutionData, Map)  this(context, data,
    * null)}.
    */
   public PSExecutionContext(int context, PSExecutionData data)
   {
      this(context, data, null);
   }

   
   /**
    * Constructs a default effect execution context.
    * 
    * @param context all valid execution contexts
    * @param data refernce to PSExecutionData object to extract relationship
    * information, must not be <code>null</code>.
    * @param processedRelationships A dynamic set of entries that tracks which
    * relationships have already been processed. Only the values are read and
    * they are expected to be a <code>PSRelationshipEffectTestResult</code>.
    * May be <code>null</code> if this information is not tracked. This class
    * treats the supplied object as read-only and does not take ownership so it
    * can be modified by the caller. [NB Although this may seem odd, it was 
    * done this way because this type was readily available by the creator of
    * this class and I was trying to minimize changes.]
    */
   public PSExecutionContext(int context, PSExecutionData data,
         Map processedRelationships)
   {
      if(data == null)
         throw new IllegalArgumentException("data must not be null");

      if(!isContextValid(context))
         throw new IllegalArgumentException("the supplied context is invalid");

      m_context = context;
      m_executionData = data;
      m_processedRelationships = processedRelationships;
   }

   /**
    * Set the activation end point for processing the current effect.
    * @param activationEndPoint must be one of the {@link PSRelationshipConfig}
    * ACTIVATION_ENDPOINT_XXXX values.
    * @throws IllegalArgumentException if the parameter is not one of the
    * ACTIVATION_ENDPOINT_XXXX values.
    */
   public void setActivationEndPoint(String activationEndPoint)
   {
      m_activationEndPoint =
         getActivationEndPointFromString(activationEndPoint);
   }

   /**
    * Helper method to set owner or dependent as activation end point for
    * processing the current effect.
    * @param isOwner <code>true</code> sets activation end point to owner and
    * <code>false</code> sets the activation end point to dependent.
    */
   public void setActivationEndPoint(boolean isOwner)
   {
      if(isOwner)
         m_activationEndPoint = RS_ENDPOINT_OWNER;
      else
         m_activationEndPoint = RS_ENDPOINT_DEPENDENT;
   }
   /**
    * helper method that evaluates the activation endpoint type constants from
    * the activation endpoint string.
    * @param activationEndPoint must be one of the ACTIVATION_ENDPOINT_XXXX
    * values defined in {@link PSRelationshipConfig}.
    * @return one of the RS_ENDPOINT_xxxx values ORed. Default is
    * RS_ENDPOINT_OWNER.
    * @throws IllegalArgumentException if the parameter is not one of the
    * ACTIVATION_ENDPOINT_XXXX values.
    */
   static public int getActivationEndPointFromString(String activationEndPoint)
   {
      if(!PSRelationshipConfig.isActivationEndPointValid(activationEndPoint))
      {
         throw new IllegalArgumentException(
         "activationEndPoint must be one of the ACTIVATION_ENDPOINT_XXXX values");
      }
      //default is owner
      int result = RS_ENDPOINT_OWNER;
      if(activationEndPoint.equalsIgnoreCase(
         PSRelationshipConfig.ACTIVATION_ENDPOINT_DEPENDENT))
      {
         result = RS_ENDPOINT_DEPENDENT;
      }
      else if(activationEndPoint.equalsIgnoreCase(
         PSRelationshipConfig.ACTIVATION_ENDPOINT_EITHER))
      {
         result |= RS_ENDPOINT_DEPENDENT;
      }
      return result;
   }

   /**
    * Helper method to see if a given context value is valid.
    * @param context context value to be checked
    * @return <code>true</code> if the given context is valid, <code>false</code>
    * otherwise.
    */
   static public boolean isContextValid(int context)
   {
      //must be between VALIDATION_MIN and VALIDATION_MAX inclusive
      return (context >= VALIDATION_MIN && context <= VALIDATION_MAX);
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.relationship.IPSExecutionContext#isPreConstruction()
    */
   public boolean isPreConstruction()
   {
      return m_context == IPSExecutionContext.RS_PRE_CONSTRUCTION;
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.relationship.IPSExecutionContext#isPostConstruction()
    */
   public boolean isPostConstruction()
   {
      return isPreConstruction();
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.relationship.IPSExecutionContext#isConstruction()
    */
   public boolean isConstruction()
   {
      return isPreConstruction();
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.relationship.IPSExecutionContext#isPreDestruction()
    */
   public boolean isPreDestruction()
   {
      return m_context == IPSExecutionContext.RS_PRE_DESTRUCTION;
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.relationship.IPSExecutionContext#isPostDestruction()
    */
   public boolean isPostDestruction()
   {
      return isPreDestruction();
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.relationship.IPSExecutionContext#isDestruction()
    */
   public boolean isDestruction()
   {
      return isPreDestruction();
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.relationship.IPSExecutionContext#isPreWorkflow()
    */
   public boolean isPreWorkflow()
   {
      return m_context == IPSExecutionContext.RS_PRE_WORKFLOW;
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.relationship.IPSExecutionContext#isPostWorkflow()
    */
   public boolean isPostWorkflow()
   {
      return m_context == IPSExecutionContext.RS_POST_WORKFLOW;
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.relationship.IPSExecutionContext#isPreCheckin()
    */
   public boolean isPreCheckin()
   {
      return m_context == IPSExecutionContext.RS_PRE_CHECKIN;
   }
   
   /*
    *  (non-Javadoc)
    * @see com.percussion.relationship.IPSExecutionContext#isCheckin()
    */
   public boolean isCheckin()
   {
      return isPreCheckin();
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.relationship.IPSExecutionContext#isPostCheckout()
    */
   public boolean isPostCheckout()
   {
      return m_context == IPSExecutionContext.RS_POST_CHECKOUT;
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.relationship.IPSExecutionContext#isCheckout()
    */
   public boolean isCheckout()
   {
      return isPostCheckout();
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.relationship.IPSExecutionContext#isPreUpdate()
    */
   public boolean isPreUpdate()
   {
      return m_context == IPSExecutionContext.RS_PRE_UPDATE;
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.relationship.IPSExecutionContext#isPostUpdate()
    */
   public boolean isPostUpdate()
   {
      return isPreUpdate();
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.relationship.IPSExecutionContext#isUpdate()
    */
   public boolean isUpdate()
   {
      return isPreUpdate();
   }

   /* (non-Javadoc)
    * @see com.percussion.relationship.IPSExecutionContext#isPreClone()
    */
   public boolean isPreClone()
   {
      return m_context == IPSExecutionContext.RS_PRE_CLONE;
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.relationship.IPSExecutionContext#getContextType()
    */
   public int getContextType()
   {
      return m_context;
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.relationship.IPSExecutionContext#getCurrentRelationship()
    */
   public PSRelationship getCurrentRelationship()
   {
      return m_executionData.getCurrentRelationship();
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.relationship.IPSExecutionContext#getOriginatingRelationship()
    */
   public PSRelationship getOriginatingRelationship()
   {
      return m_executionData.getOriginatingRelationship();
   }

   //see IPSExecutionContext for documentation 
   public Set getProcessedRelationships()
   {
      Set result = new HashSet();
      if (m_processedRelationships != null)
      {
         Iterator values = m_processedRelationships.values().iterator();
         while (values.hasNext())
         {
            result.add(((PSRelationshipEffectTestResult) values.next())
                  .getRelationship());
         }
      }
      return result;
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.relationship.IPSExecutionContext#getActivationEndPoint()
    */
   public int getActivationEndPoint()
   {
      return m_activationEndPoint;
   }
   
   /**
    * Access method for the execution data.
    * @return executiondata, never <code>null</code>.
    */
   public PSExecutionData getExecutionData()
   {
      return m_executionData;
   }

   /**
    * The execution context, initialized in ctor, never invalid after that.
    */
   private int m_context = -1;

   /**
    * Reference to the executiondata to access the most current relationship
    * chain. Set in the constructor, never <code>null</code> after that.
    */
   private PSExecutionData m_executionData = null;

   /**
    * Activation endpoint for the current effect. One of the RS_ENDPOINT_xxxx
    * values. Default is RS_ENDPOINT_OWNER. Must have been set just before
    * processing the effect by the command handler.
    */
   private int m_activationEndPoint = RS_ENDPOINT_OWNER;

   /**
    * May be <code>null</code>.
    */
   private Map m_processedRelationships;
}
