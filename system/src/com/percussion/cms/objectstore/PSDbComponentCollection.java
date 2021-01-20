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
package com.percussion.cms.objectstore;

import com.percussion.cms.PSCmsException;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * This class allows management of a set of IPSDbComponents. Although not
 * really an IPSDbComponent itself, it behaves as one as far as the users
 * of this class are concerned. It's not a true component because it doesn't
 * have a key (it creates a fake one).
 * <p>Objects of this type store a set of all members that have been removed
 * since a certain point in time. This collection is then obtained by the
 * processor when the collection is saved. The delete list is cleared when
 * the object is constructed, created from xml, or cloned.
 * <p>The clone, hashCode and equals methods do not consider the delete list
 * when performing their operations.
 * <p><em>Note:<em>  You should only use this class if you have a true database
 * collection as any items removed from this collection will be permanently
 * removed from the db when this collection is saved. For example, if you
 * retrieved all slots from the system, you could use this class. After
 * performing all desired changes, the resulting collection would be saved.
 * The processor would insert, update and delete all slots as necessary to
 * make the database represent the current state of the saved collection.
 * <p>Although it doesn't implement the Collection interface, it is meant to
 * behave like one. In fact, it is more restrictive because it limits the
 * allowed object types to a type specified at creation.
 * <p>It implements many, but not all of the methods of the Collection
 * interface, except it uses appropriate types rather than
 * Object. Collection was not implemented because many of the methods don't make
 * sense in the context of how this collection will be used.
 *
 * @author Paul Howard
 * @version 1.0
 */
public class PSDbComponentCollection extends PSDbComponent
{
   /**
    * Convenience method that calls {@link #PSDbComponentCollection(String,
    * String) PSDbComponentCollection(className, null)}.
    */
   public PSDbComponentCollection(String className)
      throws ClassNotFoundException
   {
      this(className, null);
   }

   /**
    * Creates an empty collection that limits the objects that can be added
    * to the type specified. If an interface is supplied, an exception is
    * thrown.
    *
    * @param className  The fully qualified name of the class of objects to
    *    be stored in this collection. Only a single type should be stored.
    *    In other words, don't pass the name of an interface such as
    *    IPSDbComponent. Never <code>null</code> or empty.
    *
    * @param compType The value returned by the {@link
    *    IPSDbComponent#getComponentType() getComponentType} method of the
    *    components being stored in this collection. If the default is being
    *    used, you can use the 1 param ctor instead of this one. Never
    *    <code>null</code> or empty.
    *
    * @throws ClassNotFoundException If a class by the supplied name cannot
    *    be found.
    *
    * @throws NullPointerException if className is <code>null</code>.
    */
   public PSDbComponentCollection(String className, String compType)
      throws ClassNotFoundException
   {
      this(Class.forName(className), compType);
   }


   /**
    * Convenience method that calls {@link #PSDbComponentCollection(Class,
    * String) PSDbComponentCollection(className, null)}.
    */
   public PSDbComponentCollection(Class compClass)
   {
      this(compClass, null);
   }

   /**
    * Just like {@link #PSDbComponentCollection(String, String)} except it
    * takes a component class as the parameter.
    *
    * @param compClass The component class, if <code>null</code>, the name
    *    is calculated by taking the base class name and replacing the
    *    leading PS with PSX. If there is no leading PS, the base class name
    *    is used. Each component added to this collection must match this
    *    name.
    */
   public PSDbComponentCollection(Class compClass, String compType)
   {
      this();
      m_list = new PSDbComponentList(compClass, compType, false);
   }

   /**
    * Creates a list from a previously serialized one. src must contain the
    * className attribute, or an exception will be thrown.
    *
    * @param src  Never <code>null</code>.
    *
    * @see #fromXml(Element) and single arg class ctor
    */
   public PSDbComponentCollection(Element src)
      throws PSUnknownNodeTypeException
   {
      /*we don't call the super's ctor(Element) because we don't care about
         the key and db state */
      this();
      if (null == src)
         throw new IllegalArgumentException("Source element cannot be null.");
      m_list = new PSDbComponentList(src, getNodeName());
   }
   
   /**
    * Creates a instance from the supplied array of Elements each
    * of which represents one Item of this Collection.
    * This is a convenience method which is helpful because processor
    * proxies are normally returning an array of items rather than
    * a root element.
    *
    * @param items A valid array of elements that meet the dtd defined
    *    by the supplied compClass, never <code>null</code>.
    *
    * @param compClass The component class, if <code>null</code>, the name
    *    is calculated by taking the base class name and replacing the
    *    leading PS with PSX. If there is no leading PS, the base class name
    *    is used. Each component added to this collection must match this
    *    name.
    *
    * @throws PSUnknownNodeTypeException If the supplied source element does
    *    not conform to the dtd defined in the <code>fromXml<code> method.
    */
   public PSDbComponentCollection(Element[] items, Class compClass)
      throws PSUnknownNodeTypeException
   {
      this();
      m_list = new PSDbComponentList(items, compClass);
   }
   
   /**
    * Creates a instance from the supplied array of Elements each
    * of which represents one Item of this Collection.
    * This is a convenience method which is helpful because processor
    * proxies are normally returning an array of items rather than
    * a root element.
    *
    * @param items A valid array of elements that meet the dtd defined
    *    by the supplied compClass, never <code>null</code>.
    *
    * @param compClass The component class, if <code>null</code>, the name
    *    is calculated by taking the base class name and replacing the
    *    leading PS with PSX. If there is no leading PS, the base class name
    *    is used. Each component added to this collection must match this
    *    name.
    *
    * @param compType The value returned by the {@link
    *    IPSDbComponent#getComponentType() getComponentType} method of the
    *    components being stored in this collection. If the default is being
    *    used, you can use the 1 param ctor instead of this one. Never
    *    <code>null</code> or empty.
    *
    * @throws PSUnknownNodeTypeException If the supplied source element does
    *    not conform to the dtd defined in the <code>fromXml<code> method.
    */
   public PSDbComponentCollection(Element[] items, Class compClass, String compType)
      throws PSUnknownNodeTypeException
   {
      this();
      m_list = new PSDbComponentList(items, compClass, compType);
   }


   /**
    * To provide access to {@link #fromXml(Element,String)} during
    * construction.
    *
    * @param nodeName May be <code>null</code> or empty. In that case, it's
    *    equivalent to calling the 1 param ctor.
    */
   PSDbComponentCollection(Element src, String nodeName)
      throws PSUnknownNodeTypeException
   {
      this();
      if (null == src)
         throw new IllegalArgumentException("Source element cannot be null.");
      fromXml(src, nodeName);
   }

   /**
    * For use when constructing from xml.
    */
   private PSDbComponentCollection()
   {
      super(new PSKey(new String [] {"unused"}));
   }

   /**
    * The class of the contained component.
    *
    * @return class object contained by this list.
    *    Never <code>null</code>.
    */
   public Class getMemberClass()
   {
      return m_list.getMemberClass();
   }


   /**
    * The type of components allowed in this list, as set in the ctor.
    *
    * @return The component type of the members of this list. Never
    *    <code>null</code> or empty.
    */
   public String getMemberComponentType()
   {
      return m_list.getMemberComponentType();
   }


   /**
    * See {@link IPSDbComponent#toXml(Document) interface} for description.
    * This class is implemented in terms of a PSDbComponentList and returns
    * an element that matches the dtd of the at component.
    */
   @Override
   public Element toXml(Document doc)
   {
      return m_list.toXml(doc, getNodeName());
   }


   /**
    * For use by classes using this class as part of its implementation. Allows
    * the calling class to set the tag name of the element returned by this
    * method.
    *
    * @param doc Never <code>null</code>.
    *
    * @param nodeName The name to use for the element returned by this method.
    *    If <code>null</code> or empty, the node name of this class is used.
    * @return the XML representation of the object, never <code>null</code>.
    */
   Element toXml(Document doc, String nodeName)
   {
      if (null == nodeName || nodeName.trim().length() == 0)
         nodeName = getNodeName();
      return m_list.toXml(doc, nodeName);
   }


   /**
    * See {@link #toXml(Document) toXml} and {@link
    * IPSCmsComponent#fromXml(Element) interface} for details.
    * <p>See {@link PSDbComponentList} class for implementation details.
    */
   @Override
   public void fromXml(Element src)
      throws PSUnknownNodeTypeException
   {
      fromXml(src, null);
   }


   /**
    * Same as fromXml(src), except it allows the caller to set the expected
    * node name of the top level element.
    *
    * @param nodeName If <code>null</code> or empty, getNodeName() is used.
    */
   void fromXml(Element src, String nodeName) throws PSUnknownNodeTypeException
   {
      if (null == nodeName || nodeName.trim().length() == 0)
         nodeName = getNodeName();
      if(m_list == null)
         m_list = new PSDbComponentList(src, nodeName);
      else
         m_list.fromXml(src, nodeName);
   }

   /**
    * See {@link IPSDbComponent#toDbXml(Document, Element, IPSKeyGenerator, 
    * PSKey) IPSDbComponent} for full details.
    * <p>Creates an xml fragment properly formatted for the Rhythmyx server
    * that will modify the database to make the db instances of the objects
    * of the specified type consistent with these objects.
    * <p>If the state of this object is DBSTATE_UNMODIFIED, <code>null</code>
    * is returned.
    */
   @Override
   public void toDbXml(Document doc, Element root, IPSKeyGenerator keyGen,
         PSKey parent)
      throws PSCmsException
   {
      m_list.toDbXml(doc, root, keyGen, parent);
   }


   /**
    * See {@link IPSCmsComponent#equals(Object) interface} for description.
    * This method ignores the delete list when performing the compare.
    * The order of objects within the collection is also irrelevant.
    */
   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;

      // Threshold
      if (null == obj || !getClass().isInstance(obj))
         return false;

      PSDbComponentCollection other = (PSDbComponentCollection) obj;

      // Ignore delete list and ordering
      // Threshold - check class child object matches
      Class otherClass = other.m_list.getMemberClass();
      if (otherClass != m_list.getMemberClass())
         return false;

      PSDbComponentList c = other.m_list;

      // Size threshold
      if (c.size() != size())
         return false;

      if (!equalsIgnoreOrder(c.iterator(), iterator()))
         return false;

      return true;
   }


   /**
    * See {@link IPSCmsComponent#hashCode() interface} for description.
    * This method ignores the delete list when performing the calculation.
    * The hash code is order invariant.
    */
   @Override
   public int hashCode()
   {
      return hashCodeIgnoresOrder(m_list.iterator());
   }


   /**
    * See {@link PSDbComponentList#getState()} for details.
    */
   @Override
   public int getState()
   {
      return m_list.getState();
   }


   /**
    * See {@link PSDbComponentList#setState(int)} for details.
    */
   @Override
   public void setState(int newState)
   {
      m_list.setState(newState);
   }

   /**
    * See {@link PSDbComponentList#setPersisted()} for details.
    */
   @Override
   public void setPersisted()
      throws PSCmsException
   {
      m_list.setPersisted();
   }

   /**
    * See {@link PSDbComponentList#markForDeletion()} for details.
    */
   @Override
   public void markForDeletion()
   {
      m_list.markForDeletion();
   }


   /**
    * See {@link IPSCmsComponent#clone() interface} for description. This
    * class varies in its implementation by not including the deleted list
    * in the cloned object and each cloned member will be as described in
    * {@link IPSDbComponent#clone()}.
    */
   @Override
   public Object clone()
   {
      PSDbComponentCollection copy = null;

      // If clone a persisted component list, the isPersisted() method
      // of this class may cause the super.clone() throw exception in
      // super.setState(). The workaround is to change isPersisted() behavior
      // to always return <code>false</code> during clone, set the behavior
      // back afterwards.

      m_isCloning = true;
      try
      {
         copy = (PSDbComponentCollection) super.clone();
      }
      finally
      {
         m_isCloning = false;
      }

      copy.m_list = (PSDbComponentList) m_list.clone();
      return copy;
   }


   /**
    * Adds the supplied component to this collection.
    *
    * @param comp The datatype must be the same as the type of this list
    *    or a ClassCastException will be thrown.
    *
    * @throws ClassCastException if the supplied component doesn't have the
    *    same type as the type supplied during construction.
    *
    * @throws IllegalStateException If the type of the supplied component
    *    doesn't match the type provided in the ctor.
    */
   public void add(IPSDbComponent comp)
   {
      m_list.add(comp);
   }

   /**
    * See {@link PSDbComponentList#clear()} for details.
    */
   public void clear()
   {
      m_list.clear();
   }


   /**
    * See {@link PSDbComponentList#contains(IPSDbComponent)} for details.
    */
   public boolean contains(IPSDbComponent comp)
   {
      return m_list.contains(comp);
   }


   /**
    * See {@link PSDbComponentList#isEmpty()} for details.
    */
   public boolean isEmpty()
   {
      return m_list.isEmpty();
   }


   /**
    * See {@link PSDbComponentList#iterator()} for details.
    */
   public Iterator iterator()
   {
      return m_list.iterator();
   }


   /**
    * Checks whether the components in thisIter and otherIter are equal
    * ignoring sequencing.
    *
    * @param thisIter  Assumed not <code>null</code> and that no entries are
    *    <code>null</code> and that the number of entries in both iterators is
    *    equal.
    *
    * @param otherIter  Assumed not <code>null</code> and that no entries
    *    are <code>null</code>.
    *
    * @return <code>true</code> means they are equal without respect to
    *    ordering, <code>false</code> otherwise.
    */
   static boolean equalsIgnoreOrder(Iterator thisIter, Iterator otherIter)
   {
      /* We build a map of all elements in the first iterator. Each entry in
         the map has a key equal to the compoent and a value Integer that is
         the number of components that are equal to this entry. This prevents
         us from thinking that 2 sets, one of which has a duplicate element,
         are the same */
      Map coll = new HashMap();
      while (thisIter.hasNext())
      {
         Object o = thisIter.next();
         if (null != coll.get(o))
         {
            Integer count = (Integer) coll.get(o);
            coll.put(o, new Integer(count.intValue()+1));
         }
         else
            coll.put(o, new Integer(1));
      }

      // for each other collection object
      while (otherIter.hasNext())
      {
         Object o = otherIter.next();
         if (coll.containsKey(o))
         {
            Integer count = (Integer) coll.get(o);
            int newCount = count.intValue()-1;
            if (newCount > 0)
               coll.put(o, new Integer(newCount));
            else
               coll.remove(o);
         }
         else
            return false;
      }

      return true;
   }


   /**
    * Calculates the hashcode of all components in the supplied iterator such
    * that the same value will be returned regardless of the order of the
    * comps in the iterator. This is done by adding all the hashcodes of the
    * comps. A constant value is added for <code>null</code> entries.
    *
    * @param comps Never <code>null</code>, but entries may be <code>null
    *    </code>.
    *
    * @return A value independent of the order. 0 is returned for the empty
    *    iterator.
    */
   static int hashCodeIgnoresOrder(Iterator comps)
   {
      if (null == comps)
         throw new IllegalArgumentException("Iterator cannot be null.");

      int hash = 0;
      while (comps.hasNext())
      {
         Object o = comps.next();
         if (null == o)
            hash += 31; // arbitrary, non-zero value
         else
            hash += o.hashCode();
      }
      return hash;
   }

   /**
    * See {@link PSDbComponentList#remove(IPSDbComponent)} for details.
    */
   public boolean remove(IPSDbComponent comp)
   {
      return m_list.remove(comp);
   }


   /**
    * See {@link PSDbComponentList#size()} for details.
    */
   public int size()
   {
      return m_list.size();
   }


   // see base class for descripion
   @Override
   public boolean equalsFull(Object obj)
   {
      if (!equals(obj))
         return false;

      PSDbComponentCollection c = (PSDbComponentCollection) obj;

      // threshold delete size
      Collection mine = m_list.getDeleteCollection();
      Collection other = c.m_list.getDeleteCollection();
      if (mine.size() != other.size())
         return false;

      if (!equalsIgnoreOrder(mine.iterator(), other.iterator()))
         return false;

      return true;
   }

   // see base class for description
   @Override
   public Object cloneFull()
   {
      PSDbComponentCollection copy = null;

      copy = (PSDbComponentCollection) super.clone();
      copy.m_list = (PSDbComponentList) m_list.cloneFull();
      return copy;
   }

   // see base class for description
   @Override
   public int hashCodeFull()
   {
      return m_list.hashCodeFull();
   }

   /**
    * see base class for description. Overriden to define this objects
    * persistence in terms of it's contained objects persistence. This
    * object's key is just a dummy key.
    * <p>
    * This method will affect super.clone(), it need to always return
    * <code>false</code> during call super.clone(). Set the behavior back
    * afterwards.
    */
   @Override
   public boolean isPersisted()
   {
      if (m_isCloning) // it is used in clone()
         return false;

      return m_list.isPersisted();
   }

   /**
    * see base class for description. Overriden to define this objects
    * assignment in terms of it's contained objects assignment. This
    * object's key is just a dummy key.
    */
   @Override
   public boolean isAssigned()
   {
      return m_list.isAssigned();
   }

   /**
    * Proxy implementation of sequenced list also representing the data
    * store for this unordered collection of objects. Initialized in ctor(s),
    * never <code>null</code> after that.
    */
   private PSDbComponentList m_list = null;

   /**
    * If clone a persisted component list, the isPersisted() method of this
    * class will effect super.clone(), may throw exception in super.setState().
    * The workaround is to change isPersisted() behavior to always return
    * <code>false</code> during clone, set the behavior back afterwards.
    */
   private boolean m_isCloning = false;

   public static final String XML_NODE_NAME = "PSXDbComponentCollection";
}
