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

 
package com.percussion.deployer.objectstore.idtypes;

import com.percussion.deployer.objectstore.IPSDeployComponent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Describes the location of a literal id within a <code>PSApplication</code>
 * object.
 */
public abstract class PSApplicationIdContext implements IPSDeployComponent
{
   /**
    * Get the context as a <code>String</code> that can be displayed to an
    * end user.  Context objects should call 
    * {@link #addParentDisplayText(String)} to append the display text of their 
    * parent contexts.
    * 
    * @return The text, never <code>null</code> or empty.
    */
   public abstract String getDisplayText();
   
   /**
    * Sets the parent context of this context.  This is to allow nesting of
    * contexts within each other to define an ID location in terms of another
    * context.
    * 
    * @param parent The parent context, may be <code>null</code> to clear the
    * parent context if it has been set.
    */
   public void setParentCtx(PSApplicationIdContext parent)
   {
      m_parent = parent;
   }
   
   /**
    * Get the parent context of this context.  See 
    * {@link #setParentCtx(IPSApplicationIdContext) setParentCtx()}  for more 
    * info.
    * 
    * @return The parent context, may be <code>null</code> if this object does
    * not have a parent.
    */
   public PSApplicationIdContext getParentCtx()
   {
      return m_parent;
   }
   
   /**
    * Gets the current top-level parent context. Resets the current root to be
    * the next level down.
    * 
    * @return The root context, never <code>null</code>, may be the same context
    * instance this method was called upon.
    * 
    * @throws IllegalStateException if this method has already been called on
    * the bottom-most context (as there is no context one level down from that
    * context).
    */
   public PSApplicationIdContext getCurrentRootCtx()
   {
      if (m_isCurrentRoot && m_isFinalRoot)
         throw new IllegalStateException(
            "Cannot call getCurrentRootCtx again on bottom context");
            
      PSApplicationIdContext root = this;
      if (m_isCurrentRoot)
      {
         // we are at the bottom, so return this and set final root so you can't
         // call this method again, and so resetCurrentRootCtx() won't 
         // erroneously move the current root.
         m_isFinalRoot = true;
      }
      else
      {
         PSApplicationIdContext last = this;
         PSApplicationIdContext parent = getParentCtx();
         while (parent != null && !root.m_isCurrentRoot)
         {
            last = root;
            root = parent;
            parent = parent.getParentCtx();
         }
         
         root.m_isCurrentRoot = false;
         last.m_isCurrentRoot = true;
      }
      
      return root;
   }
   
   /**
    * Gets the context that is the next level down, which would be returned by
    * the next call to {@link #getCurrentRootCtx()}.  This method does not
    * affect the current root setting.
    * 
    * @return The next root context, never <code>null</code>, may be the same 
    * context instance this method was called upon.
    */
   public PSApplicationIdContext getNextRootCtx()
   {
      PSApplicationIdContext root = this;
      PSApplicationIdContext parent = getParentCtx();
      while (parent != null && !root.m_isCurrentRoot)
      {
         root = parent;
         parent = parent.getParentCtx();
      }
      
      return root;
   }
   

   /**
    * Resets the root context up one level unless the current root is already
    * the actual root.
    */
   public void resetCurrentRootCtx()
   {
      boolean isNext = false;
      
      // if last call to getCurrentRootCtx() returned this context, then don't
      // change the current root, but reset the final root
      if (m_isCurrentRoot && m_isFinalRoot)
      {
         m_isFinalRoot = false;
      }
      else
      {
         PSApplicationIdContext parent = this;
         while (parent != null)
         {
            if (isNext)
            {
               parent.m_isCurrentRoot = true;
               break;
            }
            else if (parent.m_isCurrentRoot)
            {
               parent.m_isCurrentRoot = false;
               isNext = true;
            }
            parent = parent.getParentCtx();
         }
      }
   }
   
   /**
    * Get a list of all contexts contained in this context, including all 
    * parents and this context object.
    * 
    * @return An iterator over one or more {@link PSApplicationIdContext}
    * objects, never <code>null</code>, guaranteed to include at least this
    * context object.
    */
   public Iterator getAllContexts()
   {
      List ctxList = new ArrayList();
      
      PSApplicationIdContext parent = this;
      while (parent != null)
      {
         ctxList.add(parent);
         parent = parent.getParentCtx();
      }
      
      return ctxList.iterator();
   }
   
   /**
    * Updates the value of any member objects.  Base class implementation is a 
    * noop. Derived classes with members that contain an object holding the 
    * value that this context represents should override this method to update 
    * the value of the object and notify any listeners.
    * 
    * @param value The new value, may not be <code>null</code> and must be an 
    * appropriate object for the current context.
    * 
    * @throws IllegalArgumentException if <code>value</code> is invalid.
    */
   public void updateCtxValue(Object value)
   {
      if (value == null)
         throw new IllegalArgumentException("value may not be null");
         
      // noop in base class
   }
   
   /**
    * Get the name that may be used to identify this context's value.  Base
    * class implementation returns <code>null</code>.  Derived classes that can
    * provide a value should override this method.
    *  
    * @return The identifier, never empty, may be <code>null</code>.
    */
   public String getIdentifier()
   {
      return null;
   }
   
   /**
    * See {@link IPSDeployComponent#copyFrom(IPSDeployComponent)} for info.  
    * Derived classes should call this method to handle base class members.
    */   
   public void copyFrom(IPSDeployComponent obj)
   {
      if ( obj == null )
         throw new IllegalArgumentException("obj may not be null");

      if (!(obj instanceof PSApplicationIdContext))
         throw new IllegalArgumentException("obj wrong type");

      PSApplicationIdContext other = (PSApplicationIdContext)obj;
      m_parent = other.m_parent;
   }

   /**
    * See {@link IPSDeployComponent#equals(Object)} for info.  Derived classes
    * should call this method to check base class equality.  This method does
    * not consider the current root value.
    */   
   public boolean equals(Object obj)
   {
      boolean isEqual = true;

      if (!(obj instanceof PSApplicationIdContext))
         isEqual = false;
      else 
      {
         PSApplicationIdContext other = (PSApplicationIdContext)obj;
         if (m_parent == null ^ other.m_parent == null)
            isEqual = false;
         else if (m_parent != null && 
            !m_parent.equals(other.m_parent))
         {
            isEqual = false;
         }
      }
      
      return isEqual;
   }
   
   // see IPSDeployComponent
   public int hashCode()
   {
      return m_parent == null ? 0 : m_parent.hashCode();
   }
   
   
   /**
    * See if this listener should add itself as a listener of the supplied
    * context, and add that context as a listener of this context. Walks the 
    * supplied ctx and this context from it's top-most parent down comparing 
    * contexts.  For each context that has matching data, calls 
    * {@link #checkAddListener(PSApplicationIdContext)}.  Should only be called
    * on the bottom-most child context of a context chain.
    * 
    * @param ctx The context to compare with this context, may not be 
    * <code>null</code>.  Should be the bottom-most child context of a context
    * chain.
    */
   public void setListeners(PSApplicationIdContext ctx)
   {
      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");
      
      if (ctx == this)
         return;
         
      
      // walk down from root in tandem, check for matches (same data). as soon 
      // as no match, break for each match, call checkListener(ctx)
      while (getNextRootCtx() != this && ctx.getNextRootCtx() != ctx)
      {
         PSApplicationIdContext thisCur = getCurrentRootCtx();
         PSApplicationIdContext otherCur = ctx.getCurrentRootCtx();

         if (!thisCur.hasSameData(otherCur))
            break;
                  
         thisCur.checkAddListener(otherCur);
      }
      
      // clear root ctx info when done
      clearCurrentRootCtx();
      ctx.clearCurrentRootCtx();
   }
   
   /**
    * Clears listeners set by {@link #setListeners(PSApplicationIdContext)}.
    * See that method for details on how listeners are set and the supplied
    * <code>ctx</code> param.
    * 
    * @param ctx The context to use, may not be <code>null</code>.  
    */
   public void clearListeners(PSApplicationIdContext ctx)
   {
      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");

      while (getNextRootCtx() != this && ctx.getNextRootCtx() != ctx)
      {
         PSApplicationIdContext thisCur = getCurrentRootCtx();
         PSApplicationIdContext otherCur = ctx.getCurrentRootCtx();

         if (!thisCur.hasSameData(otherCur))
            break;
                  
         thisCur.checkRemoveListener(ctx);
      }
      
      // clear root ctx info when done
      clearCurrentRootCtx();
      ctx.clearCurrentRootCtx();
   }
   
   /**
    * Check to see if this context should add itself as listener of other 
    * context and if so adds itself as a listener of the other, and also adds 
    * the other context as a listener of this context. This should be overriden 
    * by sub-classes that also override {@link #updateCtxValue(Object)}, 
    * but their implementation of that method does not update all application 
    * data held by that context, so that they may be informed of changes to the
    * parts of the data not updated by {@link #updateCtxValue(Object)} and thus
    * keep their state in sync with the application data as it is transformed. 
    * A noop in base class.
    * 
    * @param ctx The context to check, may not be <code>null</code>.
    */
   protected void checkAddListener(PSApplicationIdContext ctx)
   {
      // noop in base class, just enforce contract
      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");
   }
   
   /**
    * Removes any listeners set by 
    * {@link #checkAddListener(PSApplicationIdContext)}.  Must be overrien if
    * that method is also overriden.  See that method for details.
    * 
    * @param ctx The context to check, may not be <code>null</code>.
    */
   protected void checkRemoveListener(PSApplicationIdContext ctx)
   {
      // noop in base class, just enforce contract
      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");      
   }
   
   /**
    * Determines if two different contexts contain the same object data.  Some 
    * contexts represent different parts of the same object data, and to 
    * represent that context, they contain the entire object data.  This method
    * determines if that is the case, if they hold the same object data.  Base
    * class method returns the result of calling <code>equals()</code>.  Any
    * derived class for which that is not correct must override this method.
    * 
    * @param ctx The context to check, may be <code>null</code>.
    *  
    * @return <code>true</code> if the objects hold the same data, 
    * <code>false</code> otherwise.
    */
   protected boolean hasSameData(PSApplicationIdContext ctx)
   {
      return equals(ctx);
   }
   
   /**
    * Add another ctx as a change listener on this context.  Listeners should be
    * notified by calls to the {@link #updateCtxValue(Object)} when it is 
    * overriden be derrived classes.
    * 
    * @param ctx The context to add as a listener of updates, may not be 
    * <code>null</code>.
    */
   void addCtxChangeListener(PSApplicationIdContext ctx)
   {
      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");
         
      // don't add self
      if (this != ctx)
         m_listeners.add(ctx);
   }   

   /**
    * Removes the supplied ctx as a change listener of this context.  See 
    * {@link #addCtxChangeListener(PSApplicationIdContext) for more info.
    * 
    * @param ctx The context to remove, may not be <code>null</code>.
    */
   void removeCtxChangeListener(PSApplicationIdContext ctx)
   {
      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");

      m_listeners.remove(ctx);
   }
   
   /**
    * Notify all change listeners, passing them the supplied ctx.
    * 
    * @param ctx The changed context, may not be <code>null</code>.
    */
   void notifyCtxChangeListeners(PSApplicationIdContext ctx)
   {
      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");

      // call ctxValueUpdated on all listeners
      Iterator listeners = m_listeners.iterator();
      while (listeners.hasNext())
      {
         PSApplicationIdContext listener = 
            (PSApplicationIdContext)listeners.next();
         listener.ctxValueUpdated(ctx);
      }
   }
   
   
   /**
    * Method called by {@link #notifyCtxChangeListeners(PSApplicationIdContext)}
    * from their {@link #updateCtxValue(Object)} method.  Must be overriden
    * by any context overriding 
    * {@link #checkAddListener(PSApplicationIdContext)} to be notified of 
    * changes to matching contexts.
    * 
    * @param ctx The updated context, may not be <code>null</code>.
    * 
    * @throws UnsupportedOperationException always in the base class.  
    */
   protected void ctxValueUpdated(PSApplicationIdContext ctx)
   {
      // suppress eclipse warning
      if (null == ctx);
      
      // would be a bug to call this if not implemented
      throw new UnsupportedOperationException(
         "ctxValueUpdated() not implemented");
   }
   
   /**
    * This method is used to get the string resources used for display text.
    *
    * @return the bundle, never <code>null</code>.
    * 
    * @throws MissingResourceException if the bundle cannot be loaded.
    */
   protected static ResourceBundle getBundle()
   {
      if (ms_bundle == null)
      {
         ms_bundle = ResourceBundle.getBundle(
            "com.percussion.deployer.client.PSDeployStringResources");
      }

      return ms_bundle;
   }
   
   /**
    * If necessary, appends the parents diplay text.  This will recurse up the
    * parent tree.
    * 
    * @param text The child's display text, may not be <code>null</code> or 
    * empty.
    * 
    * @return The text with any parent text appended.
    * 
    * @throws IllegalArgumentException if <code>text</code> is <code>null</code> 
    * or empty.
    */
   protected String addParentDisplayText(String text)
   {
      if (text == null || text.trim().length() == 0)
         throw new IllegalArgumentException("text may not be null or empty");
         
      PSApplicationIdContext parent = getParentCtx();
      if (parent != null)
      {
         text += SPACE + getBundle().getString("appIdCtxMsgJoin") +  
            SPACE + parent.getDisplayText();
      }   
      
      return text;
   }
   
   /**
    * Clears any current root context state of this ctx and all parents as if no 
    * calls to {@link #getCurrentRootCtx()} have been made.
    */
   private void clearCurrentRootCtx()
   {
      m_isCurrentRoot = false;
      m_isFinalRoot = false;
      
      
      if (m_parent != null)
         m_parent.clearCurrentRootCtx();
   }
   
   /**
    * The parent context of this context, may be <code>null</code>, modified
    * by calls to {@link #setParentCtx(PSApplicationIdContext)}
    */
   private PSApplicationIdContext m_parent = null;
   
   /**
    * Used to mark a parent context as the current root, used by 
    * {@link #getCurrentRootCtx()} to return the current root.
    */
   private boolean m_isCurrentRoot = false;

   /**
    * Used to mark a context as the "final" or bottom root.  If set to 
    * <code>true</code>, then this context has been returned by a call to 
    * {@link #getCurrentRootCtx()}, and it also has no children (meaning that 
    * method cannot be called again).  Set back to <code>false</code> by the
    * next call to {@link #resetCurrentRootCtx()}.
    */
   private boolean m_isFinalRoot = false;
   
   /**
    * String bundle used for message formats.  <code>null</code> until loaded
    * by a call to {@link #getBundle()}, never <code>null</code> after that.
    */
   private static ResourceBundle ms_bundle = null;

   /**
    * Constant for a space.
    */
   private static final String SPACE = " ";   
   
   /**
    * List of context change listeners, never <code>null</code>, may be empty.
    * Contexts are added and removed by calls to
    * {@link #addCtxChangeListener(PSApplicationIdContext)} and 
    * {@link #removeCtxChangeListener(PSApplicationIdContext)}, respectively.
    */
   private List m_listeners = new ArrayList();
}
