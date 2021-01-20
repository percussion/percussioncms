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

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A container to hold PSJexlBinding (a name value paired entity. Name may be 
 * null.Currently, used by templates, but can be used by any rx element that 
 * holds JEXL expressions
 * 
 * @see PSJexlBinding
 * 
 * @author vamsinukala
 */
public class PSJexlBindings
{

   /**
    * The bindings that are being transformed with new IDs.
    */
   private List<PSJexlBinding> m_bindings = new ArrayList<PSJexlBinding>();

   /**
    * hold a cloned copy of all the bindings. This list is useful when replacing
    * multiple ids in a single expression *AND* that binding has no "KEY"(name)
    */
   private PSJexlBindings m_srcBindings = null;

   /**
    * Get the current bindings
    * @return the bindings never <code>null</code>
    */
   public List<PSJexlBinding> getBindings()
   {
      return m_bindings;
   }

   /**
    * Add the bindings list, never <code>null</code> may be empty
    * @param bindings never <code>null</code>
    */
   public void setBindings(List<PSJexlBinding> bindings)
   {
      if (bindings == null)
         throw new IllegalArgumentException("bindings may not be null");

      m_bindings = bindings;
   }

   /**
    * Add a binding to the list of bindings
    * @param ix the index for this binding >= 0
    * @param name name of the binding may be <code>null</code> or empty
    * @param value the expression 
    */
   public void addBinding(int ix, String name, String value)
   {
      if (ix < 0)
         throw new IllegalStateException("ix may not be less than 1");

      PSJexlBinding b = new PSJexlBinding(ix, name, value);
      getBindings().add(b);
   }

   /**
    * Retrieve the jexl binding by name, never <code>null</code>
    * @param name the name of this binding, never <code>null</code>
    * @return the binding if present, may be <code>null</code>
    */
   public PSJexlBinding getByName(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException(
               "binding name may not be null or empty");
      for (PSJexlBinding bind : getBindings())
      {
         if (StringUtils.isNotBlank(bind.getName())
               && bind.getName().equals(name))
            return bind;
      }
      return null;
   }

   /**
    * Retrieve the jexl binding by expression, never <code>null</code>
    * @param expression never <code>null</code> or empty
    * @return the binding if present, may be <code>null</code>
    */
   public PSJexlBinding getByExpression(String expression)
   {
      if (StringUtils.isBlank(expression))
         throw new IllegalArgumentException(
               "expression may not be null or empty");
      for (PSJexlBinding bind : getBindings())
      {
         if (bind.getExpression().equals(expression))
            return bind;
      }
      return null;
   }

   /**
    * Retrieve the jexl binding by index, always >= 0
    * @param ix the position of this binding in the list of bindings
    * @return the binding if present, may be <code>null</code>
    */
   public PSJexlBinding getByIndex(int ix)
   {
      if (ix < 0)
         throw new IllegalArgumentException(
               "binding index may not be less than 0");
      for (PSJexlBinding bind : getBindings())
      {
         if (bind.getIndex() == ix)
            return bind;
      }
      return null;
   }

   /**
    * the copier of the current bindings list
    * @return the container of these bindings
    */
   protected PSJexlBindings clone()
   {
      List<PSJexlBinding> bindings = new ArrayList<PSJexlBinding>(getBindings()
            .size());
      Iterator<PSJexlBinding> it = getBindings().iterator();
      while (it.hasNext())
      {
         PSJexlBinding b = it.next();
         bindings.add(b.clone());
      }
      PSJexlBindings jexlBindings = new PSJexlBindings();
      jexlBindings.setBindings(bindings);
      return jexlBindings;
   }

   /**
    * method to create source bindings that are needed for indexing bindings
    * that have the null keys. This must be called before performing 
    * transformations. These bindings can be retrieved by getSrcBindings()
    * @return the bindings
    */
   public PSJexlBindings backupBindings()
   {
      if (m_srcBindings != null)
         m_srcBindings.getBindings().clear();

      m_srcBindings = clone();
      return this;
   }

   /**
    * get the original untransformed bindings. 
    * @return the original bindings may be <code>null</code>
    */
   public PSJexlBindings getSrcBindings()
   {
      return m_srcBindings;
   }
}
