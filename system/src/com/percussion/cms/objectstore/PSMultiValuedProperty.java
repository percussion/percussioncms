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

import com.percussion.cms.PSCmsException;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * This class is designed to support properties that have multiple values,
 * each value stored as a row in the same table in the database.
 * <p>Since it is effectively a collection, it maintains a delete list.
 * <p>No order is maintained for the values that make up the property.
 * <p>The values within such a property must be unique, case-insensitive.
 * <p>The description for the property is stored in exactly 1 of the rows
 * that make up this multi-valued entity.
 *
 * @author Paul Howard
 * @version 1.0
 */
public abstract class PSMultiValuedProperty extends PSCmsProperty
{
   /**
    * no-args constructor
    */
   
   public PSMultiValuedProperty()
   {
   }
   
   /**
    * Creates a component with the supplied name and no values.
    *
    * @param member  The class for the types of properties to be stored
    *    in this object. Never <code>null</code>.
    *
    * @param name The property name, never empty or <code>null</code>. Once
    *    set, it is immutable.
    */
   protected PSMultiValuedProperty(Class member, String name)
   {
      super(getKeyDef(), name);
      m_props = new PSDbComponentCollection(member);
   }


   /**
    * Create a property from a previously persisted one.
    *
    * @param src Never <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException See {@link #fromXml(Element)}.
    */
   protected PSMultiValuedProperty(Element src)
      throws PSUnknownNodeTypeException
   {
      super(getKeyDef(), src.getAttribute(XML_ATTR_PROPNAME));
      fromXml(src);
   }

   /**
    * This class isn't a real component, so it just creates a fake key which
    * is never used.
    *
    * @return A PSSimpleKey with a key def of "unused".
    */
   private static PSKey getKeyDef()
   {
      return new PSSimpleKey("unused");
   }

   /**
    * See {@link IPSCmsComponent#toXml(Document) interface} for details.
    * The DTD for this class is as follows:
    * todo: add dtd
    */
   public Element toXml(Document doc)
   {
      // Threshold
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      Element root = m_props.toXml(doc, getNodeName());
      //this is used by the ctor when re-serializing
      root.setAttribute(XML_ATTR_PROPNAME, getName());
      return root;
   }


   //see base class for description
   public void fromXml(Element src)
      throws PSUnknownNodeTypeException
   {
      if (null == src)
         throw new IllegalArgumentException("Source cannot be null.");

      if (null == m_props)
      {
         m_props = new PSDbComponentCollection(src, getNodeName());
      }
      else
      {
         m_props = new PSDbComponentCollection(m_props.getMemberClass());
         m_props.fromXml(src, getNodeName());
      }
   }


   /**
    * See {@link PSCmsProperty#toDbXml(Document, Element, IPSKeyGenerator, 
    * PSKey)} for details. Although it appears to be a single property with many
    * values, it is stored as 3 seperate properties that happen to have the
    * same name.
    *
    * @param parent  Required.
    */
   public void toDbXml(Document doc, Element root, IPSKeyGenerator keyGen,
         PSKey parent)
      throws PSCmsException
   {
      if (null == parent || !parent.isAssigned())
      {
         throw new IllegalArgumentException(
               "Invalid parent key supplied. Must be assigned.");
      }
      if (m_props.getState() != DBSTATE_UNMODIFIED)
         m_props.toDbXml(doc, root, keyGen, parent);
   }

   /**
    * We must proxy the call to our collection. See base class for more details.
    */
   public int getState()
   {
      return m_props.getState();
   }

   /**
    * We must proxy the call to our collection. See base class for more details.
    */
   public void setState(int state)
   {
      // If we're cloning, and we have contained properties
      // that are persisted this will throw an exception because
      // we're setting the state of the key to 'new' on possibly
      // persisted children. We can ignore here because m_props
      // properly clones each of the child keys at a later point
      // with the proper state representation.
      if (m_isCloning)
         return;

      m_props.setState(state);
   }


   /**
    * Must be overridden because this is not a real db component, it must be
    * proxied to the collection.
    */
   public void setPersisted()
      throws PSCmsException
   {
      m_props.setPersisted();
   }


   /**
    * Must be overridden because this is not a real db component, it must be
    * proxied to the collection.
    */
   public void markForDeletion()
   {
      // Base class handling even
      super.markForDeletion();

      Iterator propsIter = m_props.iterator();

      while (propsIter.hasNext())
      {
         // Mark each contained property for deletion
         PSCmsProperty aProp = (PSCmsProperty) propsIter.next();
         aProp.markForDeletion();
      }
   }


   /**
    * Adds the supplied value to the set of values for this property. The set
    * of all values must be unique (case-sensitive) and none of them can be
    * null (although one empty is allowed).
    *
    * @param value Never <code>null</code>, may be empty. If there is already
    *    an entry with the same value, this is a noop.
    */
   public void add(String value)
   {
      if (null == value)
         throw new IllegalArgumentException("Value cannot be null.");

      add(new String[] {value});
   }


   /**
    * Adds the supplied value to the set of values for this property. The set
    * of all values must be unique (case-sensitive) and none of them can be
    * null (although one empty is allowed).
    *
    * @param values Never <code>null</code>, may be empty. If there is already
    *    an entry for any supplied value, it is skipped. If any entry is
    *    <code>null</code>, it is skipped.
    */
   public void add(String[] values)
   {
      if (null == values)
         throw new IllegalArgumentException("Values array cannot be null.");

      for (int i=0; i < values.length; i++)
      {
         if (null != values[i] && !contains(values[i]))
            m_props.add(createProperty(getName(), values[i]));
      }
   }


   /**
    * Creates a new property of a type derived from PSCmsProperty.
    *
    * @param name Never <code>null</code> or empty.
    *
    * @param value Maybe <code>null</code> or empty.
    *
    * @return Never <code>null</code>.
    */
   protected abstract PSCmsProperty createProperty(String name, String value);


   /**
    * Removes all entries in this list. For each removed component, if the
    * component state is DBSTATE_[UN]MODIFIED, it is added to the delete list.
    */
   public void clear()
   {
      m_props.clear();
   }


   /**
    * Scans the entire list looking for an entry that matches the supplied
    * value. For any entry, e, if e.equalsIgnoreCase(value) is <code>true
    * </code>, <code>true</code> is returned.
    *
    * @param value Never <code>null</code>, may be empty.
    *
    * @return <code>true</code> if value matches any entry in this case-
    *    insensitive, <code>false</code> otherwise.
    */
   public boolean contains(String value)
   {
      // Check the properties
      Iterator iter = m_props.iterator();

      while (iter.hasNext())
      {
         PSCmsProperty p = (PSCmsProperty) iter.next();

         if (p.getValue().equalsIgnoreCase(value))
            return true;
      }

      return false;
   }


   /**
    * Check whether this property has at least 1 value.
    *
    * @return <code>true</code> if size() > 0, <code>false</code>
    *    otherwise.
    */
   public boolean hasValues()
   {
      return (!m_props.isEmpty());
   }


   /**
    * Gets an immutable iterator over all the values in this property.
    *
    * @return An iterator over 0 or more entries, each of which is a
    *    String. Never <code>null</code>.
    */
   public Iterator iterator()
   {
      Collection c = new ArrayList();
      Iterator it = m_props.iterator();
      while (it.hasNext())
         c.add(((PSCmsProperty) it.next()).getValue());
      return c.iterator();
   }


   /**
    * Removes the occurrence of the specified value from this property. If
    * the component state is DBSTATE_[UN]MODIFIED, it is added to
    * the delete list.
    *
    * @param value Never <code>null</code>. The comparison is performed
    *    case-insensitive.
    *
    * @return <code>true</code> if a matching component was found and removed,
    *    <code>false</code> otherwise.
    */
   public boolean remove(String value)
   {
      // Check the properties
      Iterator iter = m_props.iterator();

      while (iter.hasNext())
      {
         PSCmsProperty p = (PSCmsProperty) iter.next();

         if (p.getValue().equalsIgnoreCase(value))
         {
            return m_props.remove(p);
         }
      }
      return false;
   }


   /**
    * Gets the number of values in this property.
    *
    * @return A value >= 0.
    */
   public int size()
   {
      return m_props.size();
   }


   //see base class
   public boolean equals(Object o)
   {
      if (o == this)
         return true;
      else if (!o.getClass().isInstance(this))
         return false;
      PSMultiValuedProperty other = (PSMultiValuedProperty)o;

      if (!getName().equalsIgnoreCase(other.getName()))
         return false;
      else if (!m_props.equals(other.m_props))
         return false;
      return true;
   }

   // see base class for description
   public boolean isPersisted()
   {
      // If we are calling super.clone from clone
      // and we are persisted an exception will be
      // thrown, so we overriden isPersisted to
      // return false during a clone
      if (m_isCloning)
         return false;

      return m_props.isPersisted();
   }

   // see base class for description
   public boolean isAssigned()
   {
      return m_props.isAssigned();
   }

   // see base class for description
   public Object clone()
   {
      PSMultiValuedProperty copy = null;

      // We are only worried about the base class
      // isPersisted here.
      m_isCloning = true;

      try
      {
         copy = (PSMultiValuedProperty) super.clone();
      }
      finally
      {
         m_isCloning = false;
      }

      // Handles the overriding of isPersisted in
      // the PSDbComponentList for us.
      copy.m_props = (PSDbComponentCollection) m_props.clone();
      return copy;
   }

   // see base class for description
   public int hashCode()
   {
      return super.hashCode() +
         m_props.hashCode();
   }

   private static final String XML_ATTR_PROPNAME = "propName";

   /**
    * This list contains 1 or more PSCmsProperty objects. All members of
    * this set have the same property name.
    */
   private PSDbComponentCollection m_props;

   /**
    * If cloning a persisted component list, the isPersisted() method of this
    * class will effect super.clone(), may throw exception in super.setState().
    * The workaround is to change isPersisted() behavior to always return
    * <code>false</code> during clone, set the behavior back afterwards.
    */
   private boolean m_isCloning = false;
}
