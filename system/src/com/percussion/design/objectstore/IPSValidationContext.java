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

package com.percussion.design.objectstore;

import com.percussion.design.objectstore.server.IPSObjectStoreHandler;
import com.percussion.server.PSUserSession;

import java.util.List;

/**
 * The IPSValidation class is used in the
 * {@link com.percussion.design.objectstore.IPSComponent#validate validate} method
 * of IPSComponent.
 * <P>
 * A component should validate itself along these lines:
 * <OL>
 * <LI>Call {@link #startValidation startValidation}. If startValidation returns
 * <CODE>false</CODE>, the component may optionally skip the rest of the steps.
 * <LI>Validate the fields of the object. Check that all non-component fields have
 * the correct values, check that all required fields are present, check that no
 * invalid flag bits are set on all bit fields.
 * <LI>If the object has child components, it should <CODE>pushParent(this)</CODE>,
 * then call validate on all child components, and finally <CODE>popParent</CODE>.
 * Note: If the object refers to components that are not semantically children,
 * it should not validate them until the next step. An example of a non-child
 * reference is the reference from a PSBackEndColumn to the PSBackEndTable.
 * <LI>If the object refers to components that are not children, it should
 * validate them last.
 * </OL>
 */
public interface IPSValidationContext
{
   /**
    * Gets the parent component "stack", which may be read-only. You should
    * only modify this list through the {@link #pushParent pushParent}
    * and {@link #popParent popParent} methods.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/6/28
    * 
    * @return   List The parent list. Should never be <CODE>null</CODE>.
    *
    * @see <a href="java.util.Collections.html#unmodifiableList">Java Spec</a>
    */
   public List getParentList();
   
   /**
    * Updates the parent list by adding the given component to the end of the
    * given list. If <CODE>pushParent</CODE> is <CODE>null</CODE>, this method
    * must be a no-op.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/6/28
    * 
    * @param   pushParent The parent component to add to the end of the list.
    * 
    * @see com.percussion.design.objectstore.PSComponent#updateParentList
    */
   public void pushParent(IPSComponent pushParent);

   /**
    * Pops the most recently added parent off the parent list.
    * If the parent list is empty, an unchecked exception may be thrown.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/6/28
    *
    * @see com.percussion.design.objectstore.PSComponent#resetParentList
    */
   public void popParent();

   /**
    * Peeks at the most recently added parent off the parent list.
    * If the parent list is empty, <CODE>null</CODE> will be returned.
    *
    * @author   chad loder
    *
    * @version 1.0 1999/6/28
    *
    * @return IPSComponent The parent, or <CODE>null</CODE> if the list
    * is empty.
    *
    * @see com.percussion.design.objectstore.PSComponent#resetParentList
    * @see #popParent
    */
   public IPSComponent peekParent();

   /**
    * Gets the object store handler associated with this validation context. May
    * return <CODE>null</CODE>, in which case the validating objects should
    * do as much validation as possible without an object store handler. No
    * object should report validation errors simply because this method
    * returns <CODE>null</CODE> (even if it means no validation can be done).
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/6/28
    * 
    * @return   IPSObjectStoreHandler (can be <CODE>null</CODE>)
    */
   public IPSObjectStoreHandler getObjectStoreHandler();

   /**
    * Gets the user session for this context. May return <CODE>null</CODE>.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/6/30
    * 
    * @return   PSUserSession (can be <CODE>null</CODE>).
    */
   public PSUserSession getSession();

   /**
    * Gets the container for this context. May return <CODE>null</CODE>.
    * @author   chad loder
    * 
    * @version 1.0 1999/6/30
    * 
    * @return   IPSDocument (can be <CODE>null</CODE>).
    */
   public IPSDocument getContainer();

   /**
    * Registers a validation warning with the validation context. The context
    * may choose to discard warnings or to raise a PSValidationException,
    * which must not be caught by the validate method.
    * <P>
    * Warnings are caused by conditions that may produce inefficient or
    * unexpected behavior during runtime. If the condition will usually
    * cause incorrect behavior, consider calling validationError instead.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/6/28
    * 
    * @param   component The component to which the warning pertains.
    * @param   errorCode The error code for the warning message.
    * @param   args The arguments to the warning message. Can be null.
    * <CODE>null</CODE>.
    */
   public void validationWarning(
      IPSComponent component,
      int errorCode,
      Object[] args)
      throws PSValidationException;

   /**
    * Calls {@link #validationWarning(IPSComponent, int, Object[]) validationWarning}
    * with an array containing one element, which is <CODE>arg</CODE>.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/6/28
    * 
    * @throws   PSValidationException
    *
    * @see #validationWarning(IPSComponent, int, Object[])
    */
   public void validationWarning(
      IPSComponent component,
      int errorCode,
      Object arg)
      throws PSValidationException;

   /**
    * Registers a validation error with the validation context. The context
    * may choose to discard errors or to raise a PSValidationException,
    * which must not be caught by the validate method.
    * <P>
    * Validation errors are caused by conditions that will produce incorrect
    * behavior when this object is used or referenced. If the condition
    * will not cause errors in the current version of the product but may
    * cause errors in future versions, use validationWarning instead.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/6/28
    * 
    * @param   component The component to which the warning pertains.
    * @param   errorCode The error code for the warning message.
    * @param   args The arguments to the warning message. Can be null.
    */
   public void validationError(
      IPSComponent component,
      int errorCode,
      Object[] args)
      throws PSValidationException;

   /**
    * Calls {@link #validationError(IPSComponent, int, Object[]) validationError}
    * with an array containing one element, which is <CODE>arg</CODE>.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/6/28
    * 
    * @throws   PSValidationException
    *
    * @see #validationError(IPSComponent, int, Object[])
    */
   public void validationError(
      IPSComponent component,
      int errorCode,
      Object arg)
      throws PSValidationException;

   /**
    * Marks the start of an object's validation. Returns <CODE>true</CODE>
    * if this component should continue validating, returns <CODE>false</CODE>
    * if this component does not need to validate. Components are required
    * to call this method before validating, but are not required to check
    * the return value.
    * <P>
    * In other words, it must always be safe for a component to continue
    * validating itself and its children even when this method returns
    * <CODE>false</CODE>. It must also be safe for a component to
    * skip validation or only partially validate when this method returns
    * <CODE>false</CODE>.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/6/29
    * 
    * @param   component The component that is being validated.
    * @param   message An optional message. Can be <CODE>null</CODE>.
    * 
    * @return   boolean <CODE>true</CODE> if the component should continue
    * validating, <CODE>false</CODE> if this component does not need to
    * validate.
    */
   public boolean startValidation(IPSComponent component, String message);

}
