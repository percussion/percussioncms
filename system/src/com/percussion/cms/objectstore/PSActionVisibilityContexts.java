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
package com.percussion.cms.objectstore;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;

import java.util.Iterator;

import org.w3c.dom.Element;

public class PSActionVisibilityContexts
   extends PSDbComponentCollection
{
   /**
    * The default constructor to create parameters with empty list.
    */
   public PSActionVisibilityContexts()
   {
      super(PSActionVisibilityContext.class);
   }


   /**
    * Constructs this object from the supplied element. See {@link
    * #toXml(org.w3c.dom.Document)} for the expected form of xml.
    *
    * @param element the element to load from, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if element is <code>null</code>
    */
   public PSActionVisibilityContexts(Element element)
      throws PSUnknownNodeTypeException
   {
      super(element);
   }

   /**
    * If a context by the supplied name can be found, it is returned.
    *
    * @param name Name of the context. Never <code>null</code> or empty.
    *    Compare is case insensitive.
    *
    * @return The context by that name, or <code>null</code> if not found.
    */
   public PSActionVisibilityContext getContext(String name)
   {
      return getContextObject(name);
   }



   /**
    * Adds a new context w/ a single value.
    *
    * @param name The name of the context. Never <code>null</code> or empty.
    *
    * @param value One of the values. If <code>null</code>, "" is used.
    */
   public void addContext(String name, String value)
   {
      //contract is fulfilled by created instance
      addContext(name, new String[] {value});
   }


   /**
    * Adds a new context w/ multiple values.
    *
    * @param name The name of the context. Never <code>null</code> or empty.
    *    If a context by that name is present, the supplied values are assigned
    *    to the existing one.
    *
    * @param values Zero or more values. If <code>null</code>, a single value
    *    of "" is used.
    */
   public void addContext(String name, String[] values)
   {
      if (null == name || name.trim().length() == 0)
         throw new IllegalArgumentException("Name must be supplied.");

      if (null == values)
      {
         values = new String[1];
         values[0] = "";
      }

      PSActionVisibilityContext ctx = getContextObject(name);
      if (null == ctx)
      {
         //contract is fulfilled by created instance
         add(new PSActionVisibilityContext(name, values, null));
      }
      else
      {
         ctx.add(values);
      }
   }


   /**
    * If a context by the supplied name exists in this set, it is removed.
    * Otherwise, no action is taken.
    *
    * @param name If <code>null</code> or empty, no action is taken. The name
    *    is compared case insensitive.
    *
    * @return <code>true</code> if an entry was removed, <code>false</code>
    *    otherwise.
    */
   public boolean removeContext(String name)
   {
      if (null == name || name.trim().length() == 0)
         return false;

      return remove(getContextObject(name));
   }


   /**
    * This method is overridden to guarantee that the names of all members
    * forms a set. If you add a context that is already present, the values
    * in the new context replace those in the existing one.
    *
    * @param comp Never <code>null</code>. Must be a PSActionVisibilityContext.
    */
   @Override
   public void add(IPSDbComponent comp)
   {
      if (null == comp || !(comp instanceof PSActionVisibilityContext))
      {
         throw new IllegalArgumentException(
               "Can only add PSActionVisibilityContext components");
      }

      PSActionVisibilityContext newCtx = (PSActionVisibilityContext)comp;
      PSActionVisibilityContext ctx = getContextObject(newCtx.getName());

      if (null != ctx)
      {
         if (ctx == newCtx)
            return;  //tried to add self
         ctx.clear();
         Iterator values =  iterator();
         while (values.hasNext())
         {
            PSActionVisibilityContext itCtx = (PSActionVisibilityContext)values.next();
            ctx.setValue(itCtx.getValue());
         }
      }
      else
         super.add(comp);
   }


   /**
    * Finds the context in this set that has the specified name.
    *
    * @param name Never <code>null</code> or empty. The name is compared
    *    case insensitive.
    *
    * @return A valid context, or <code>null</code> if one can't be found.
    */
   private PSActionVisibilityContext getContextObject(String name)
   {
      Iterator it = iterator();
      while (it.hasNext())
      {
         PSActionVisibilityContext ctx = (PSActionVisibilityContext) it.next();
         if (ctx.getName().equalsIgnoreCase(name))
            return ctx;
      }
      return null;
   }

   /**
    * The constant to indicate root node name.
    */
   public static final String XML_NODE_NAME = "PSXActionVisibilityContexts";

   //xml constants
   private static final String PARAM_NODE_NAME = "PSXVisibilityContext";
   private static final String NAME_ATTR = "name";
}
