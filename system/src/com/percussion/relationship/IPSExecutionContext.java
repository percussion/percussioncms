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

import com.percussion.design.objectstore.PSRelationship;

import java.util.Set;

/**
 * This is an empty interface to define the execution context needed for
 * relationship effects. Depending on where relationships are processed, the
 * execution context will be different. All context classes that can be
 * used to process relationships must implement this interface. See
 * <code>com.percussion.design.objectstore.PSRelationship</code> and
 * PSXRelationshipSet.dtd for a description of relationships.
 */
public interface IPSExecutionContext
{
   /**
    * Returns the effect execution context type.
    *
    * @return teh effect execution context.
    */
   public int getContextType();

   /**
    * Is this called before construction? See {@link #RS_PRE_CONSTRUCTION} for
    * detail info.
    *
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */
   public boolean isPreConstruction();

   /**
    * @deprecated use {@link #isPreConstruction()} instead.
    */
   public boolean isPostConstruction();

   /**
    * @deprecated use {@link #isPreConstruction()} instead.
    */
   public boolean isConstruction();

   /**
    * Is this called before destruction? See {@link #RS_PRE_DESTRUCTION} for
    * detail info.
    *
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */
   public boolean isPreDestruction();

   /**
    * @deprecated use {@link #isPreDestruction()} instead.
    */
   public boolean isPostDestruction();

   /**
    * @deprecated use {@link #isPreDestruction()} instead.
    */
   public boolean isDestruction();

   /**
    * Is this called before a workflow transition?
    *
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */
   public boolean isPreWorkflow();

   /**
    * Is this called after a workflow transition?
    *
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */
   public boolean isPostWorkflow();

   /**
    * Is this called before a checkin?
    *
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */
   public boolean isPreCheckin();

   /**
    * Is this called before a checkin?
    *
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    * @deprecated use {@link #isPreCheckin()} instead.
    */
   public boolean isCheckin();

   /**
    * Is this called after a checkout?
    *
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */
   public boolean isPostCheckout();

   /**
    * Is this called after a checkout?
    *
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    * @deprecated use {@link #isPostCheckout()} instead.
    */
   public boolean isCheckout();

   /**
    * Is this called before an object update? See {@link #RS_PRE_UPDATE} for
    * detail info.
    *
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */
   public boolean isPreUpdate();

   /**
    * @deprecated use {@link #isPreUpdate()} instead.
    */
   public boolean isPostUpdate();

   /**
    * @deprecated use {@link #isPreUpdate()} instead.
    */
   public boolean isUpdate();

   /**
    * Is this called before a clone process
    *
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */
   public boolean isPreClone();

   /**
    * This context is used when performing various actions that may affect
    * relationships. At any given time, exactly 1 relationship is being
    * processed. This method allows the effect write to obtain that
    * relationship.
    *
    * @return The relationship that is currently being processed, may be <code>
    *    null</code> if no relationship is available yet. If the current
    *    context is <code>RS_CONSTRUCTION</code>, then this is a newly created
    *    relationship that has not been persisted. Any changes to this object
    *    will affect what is persisted.
    */
   public PSRelationship getCurrentRelationship();

   /**
    * When a relationship request is made, it may result in many relationships
    * being processed (recursion). This method will return the relationships
    * 'back to the top' of the recursion. While procesing the topmost
    * relationship, this method returns an empty list.
    *
    * @return Never <code>null</code>, may be empty. Although each entry is the
    *    actual relationship, any changes will not affect the persisted value.
    *    However, they could have an affect on other effects that call this
    *    method. Therefore, these should be treated as read-only. They are not
    *    cloned to save processing time.
    */
   public PSRelationship getOriginatingRelationship();

   /**
    * Contains the set of all relationships that have been processed up to this
    * point in time.
    * 
    * @return Never <code>null</code>, but may be empty if this information
    * is not tracked. The caller takes ownership of the returned set. Each entry
    * is a <code>PSRelationship</code>. The caller should not modify the
    * members, treat them as read-only.
    */
   public Set getProcessedRelationships();
   
   /**
    * The end point property determines which end of the relationship (owner
    * or dependent) causes the effect to be activated. This is set when the
    * effect is added to the relationship. If the effect was configured for
    * both, this method returns the endpoint that actually activated.
    *
    * @return Exactly of the RS_ENDPOINT_xxx values.
    */
   public int getActivationEndPoint();

   /**
    * The context type used to run effects before persisting a created 
    * relationship instance into the backend repository.
    */
   public static final int RS_PRE_CONSTRUCTION = 1;
   
   /**
    * @deprecated use {@link #RS_PRE_CONSTRUCTION} instead
    */
   public static final int RS_POST_CONSTRUCTION = RS_PRE_CONSTRUCTION;
   
   /**
    * @deprecated use {@link #RS_PRE_CONSTRUCTION} instead
    */
   public static final int RS_CONSTRUCTION = RS_PRE_CONSTRUCTION;

   /**
    * The context type used to run effects before removing a relationship 
    * instance from the backend repository.
    */
   public static final int RS_PRE_DESTRUCTION = 2;

   /**
    * @deprecated use {@link #RS_PRE_DESTRUCTION} instead
    */
   public static final int RS_POST_DESTRUCTION = RS_PRE_DESTRUCTION;

   /**
    * @deprecated use {@link #RS_PRE_DESTRUCTION} instead
    */
   public static final int RS_DESTRUCTION = RS_PRE_DESTRUCTION;

   /**
    * The context type used to run effects before a workflow transition is
    * executed.
    */
   public static final int RS_PRE_WORKFLOW = 3;

   /**
    * The context type used to run effects after a workflow transition is
    * executed.
    */
   public static final int RS_POST_WORKFLOW = 4;

   /**
    * The context type used to run effects before a checkin.
    */
   public static final int RS_PRE_CHECKIN = 5;

   /**
    * The context type used to run effects before a checkin.
    * @deprecated use {@link #RS_PRE_CHECKIN} instead
    */
   public static final int RS_CHECKIN = RS_PRE_CHECKIN;

   /**
    * The context type used to run effects after a checkout.
    */
   public static final int RS_POST_CHECKOUT = 6;

   /**
    * The context type used to run effects after a checkout.
    * @deprecated use {@link #RS_POST_CHECKOUT} instead.
    */
   public static final int RS_CHECKOUT = RS_POST_CHECKOUT;
   
   /**
    * The context type used to run effects before persisting an updated 
    * relationship instance in the backend repository.
    */
   public static final int RS_PRE_UPDATE = 7;

   /**
    * @deprecated use {@link #RS_PRE_UPDATE} instead
    */
   public static final int RS_POST_UPDATE = RS_PRE_UPDATE;

   /**
    * @deprecated use {@link #RS_PRE_UPDATE} instead
    */
   public static final int RS_UPDATE = RS_PRE_UPDATE;

   /**
    * The context type used to run effects just before creating a clone of an 
    * existing item is createed.
    */
   public static final int RS_PRE_CLONE = 8;

   /**
    * Minimum valid context value. Make sure it is minimum of all RS_XXXX values.
    */
   public static final int VALIDATION_MIN = RS_PRE_CONSTRUCTION;

   /**
    * Maximu valid context value. Make sure it is maximum of all RS_XXXX values.
    * Needs to be changed when we add a new context value.
    */
   public static final int VALIDATION_MAX = RS_PRE_CLONE;

   /**
    * Bit to indicate the activation endpoint as owner.
    */
   public static final int RS_ENDPOINT_OWNER = 1;

   /**
    * Bit to indicate the activation endpoint as dependent.
    */
   public static final int RS_ENDPOINT_DEPENDENT = RS_ENDPOINT_OWNER<<1;
}
