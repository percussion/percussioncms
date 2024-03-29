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
package com.percussion.cms.objectstore;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import org.w3c.dom.Element;

import java.util.Iterator;

/**
 * The class that is used to represent properties as defined by
 * 'sys_Props.dtd'.
 */
public class PSChildActions
   extends PSDbComponentCollection
{
   /**
    * The default constructor to create a container for menu items.
    */
   public PSChildActions()
   {
      super(PSMenuChild.class);
   }

   /**
    * Constructs this object from the supplied element. See {@link
    * #toXml(org.w3c.dom.Document) } for the expected form of xml.
    *
    * @param element the element to load from, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if element is <code>null</code>
    */
   public PSChildActions(Element element)
      throws PSUnknownNodeTypeException
   {
      super(element);
   }

   public void add(PSAction action)
   {
      if (!action.isAssigned())
      {
         throw new IllegalArgumentException(
               "Action must be assigned before it can be added to another.");
      }
      PSMenuChild c = new PSMenuChild(action);
      add(c);
   }


   public void add(PSMenuChild child)
   {
      if (null == child)
      {
         throw new IllegalArgumentException(
               "Supplied action reference can't be null.");
      }
      PSMenuChild existing = getChild(child);
      if (null == existing)
         super.add(child);
   }


   /**
    * This method is overridden to guarantee that each action is added only
    * once.
    *
    * @param comp Never <code>null</code>. Must be a PSAction or PSMenuAction.
    */
   @Override
   public void add(IPSDbComponent comp)
   {
      if (null == comp
            || !((comp instanceof PSAction) || comp instanceof PSMenuChild))
      {
         throw new IllegalArgumentException(
               "Can only add PSAction or PSMenuChild components");
      }

      if (comp instanceof PSAction)
         add((PSAction) comp);
      if (comp instanceof PSMenuChild)
         add((PSMenuChild) comp);
   }

   /**
    * Removes a specified action. If the supplied action exists in this set, 
    * it is removed. Otherwise, no action is taken.
    *
    * @param action the to be removed action. If <code>null</code>, no action 
    *    is taken. The name
    *    is compared case insensitive.
    *
    * @return <code>true</code> if an entry was removed, <code>false</code>
    *    otherwise.
    */
   public boolean removeAction(PSAction action)
   {
      if (null == action)
         return false;

      return remove(new PSMenuChild(action));
   }

   public boolean removeAction(PSMenuChild action)
   {
      if (null == action)
         return false;

      return super.remove(action);
   }

   /**
    * Finds the action reference in this collection that matches the supplied
    * one.
    *
    * @param child Never <code>null</code>.
    *
    * @return A valid entry, or <code>null</code> if one can't be found.
    */
   private PSMenuChild getChild(PSMenuChild child)
   {
      Iterator it = iterator();
      while (it.hasNext())
      {
         PSMenuChild tmp = (PSMenuChild) it.next();
         if (child.equals(tmp))
            return tmp;
      }
      return null;
   }

   @SuppressWarnings("hiding")
   public static final String XML_NODE_NAME = "PSXChildActions";
}
