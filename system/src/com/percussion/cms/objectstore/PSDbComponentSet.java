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

import com.percussion.cms.IPSCmsErrors;
import com.percussion.cms.PSCmsException;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.util.PSStringOperation;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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
 * <p>Although this class doesn't implement the Set interface, it is meant to
 * behave like a Set. In fact, it is more restrictive as it is meant to
 * behave like a {@link com.percussion.util.PSCollection}. It implements many,
 * but not all of the methods of the Set interface, except it uses appropriate
 * types rather than Object.
 */
public class PSDbComponentSet extends PSDbComponent
{
   /**
    * Convenience method that calls {@link #PSDbComponentSet(String,
    * String) PSDbComponentSet(className, null)}.
    */
   public PSDbComponentSet(String className)
      throws ClassNotFoundException
   {
      this(className, null);
   }

   /**
    * Creates an empty Set that limits the objects that can be added
    * to the type specified. If an interface is supplied, an exception is
    * thrown.
    *
    * @param className  The fully qualified name of the class of objects to
    *    be stored in this collection. Only a single type should be stored.
    *    In other words, don't pass the name of an interface such as
    *    IPSDbComponent. Never <code>null</code> or empty.
    *
    * @param compType The value returned by the {@link
    * IPSDbComponent#getComponentType() getComponentType} method of the
    * components being stored in this collection. If the default is being
    * used, you can use the 1 param ctor instead of this one.
    * See {@link #PSDbComponentSet(String) PSDbComponentSet(String)}
    * May be <code>null</code> or empty in which case the default is used.
    *
    * @throws ClassNotFoundException If a class by the supplied name cannot
    *    be found.
    *
    * @throws NullPointerException if className is <code>null</code>.
    */
   public PSDbComponentSet(String className, String compType)
      throws ClassNotFoundException
   {
      this(Class.forName(className), compType);
   }


   /**
    * Convenience method that calls {@link #PSDbComponentSet(Class,
    * String) PSDbComponentSet(className, null)}.
    */
   public PSDbComponentSet(Class compClass)
   {
      this(compClass, null);
   }

   /**
    * Just like {@link #PSDbComponentSet(String, String)} except it takes a
    * component class as the parameter.
    *
    * @param compClass The component class, if <code>null</code>, the name
    *    is calculated by taking the base class name and replacing the
    *    leading PS with PSX. If there is no leading PS, the base class name
    *    is used. Each component added to this collection must match this
    *    name.
    */
   public PSDbComponentSet(Class compClass, String compType)
   {
      this();
      if (compClass == null)
         throw new IllegalArgumentException("Supplied class cannot be null.");

      if ((compType == null) || (compType.trim().length() < 0))
      {
         compType = compClass.getName().substring(
               compClass.getName().lastIndexOf('.')+1);
      }

      if (null == compType || compType.trim().length() == 0)
      {
         throw new IllegalArgumentException(
               "Component type cannot be null or empty.");
      }
      if (compClass.isInterface())
      {
         throw new IllegalArgumentException("Interfaces are not supported for "
               + "component collections, you must provide a class.");
      }
      m_class = compClass;
      m_memberComponentType = compType;
   }

   /**
    * Creates a Set from a previously serialized one. <code>src</code> must
    * contain the className attribute, or an exception will be thrown.
    *
    * @param src  Never <code>null</code>.
    *
    * @see #fromXml(Element) and single arg class ctor
    */
   public PSDbComponentSet(Element src)
      throws PSUnknownNodeTypeException
   {
      this(src, null);
   }
   
   /**
    * Creates a instance from the supplied array of Elements each
    * of which represents one Item of this Set.
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
   public PSDbComponentSet(Element[] items, Class compClass)
      throws PSUnknownNodeTypeException
   {
      this(items, compClass, null);
   }
   
   /**
    * Creates a instance from the supplied array of Elements each
    * of which represents one Item of this Set.
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
    * IPSDbComponent#getComponentType() getComponentType} method of the
    * components being stored in this collection. If the default is being
    * used, you can use the 1 param ctor instead of this one. Never
    * <code>null</code> or empty.
    *
    * @throws PSUnknownNodeTypeException If the supplied source element does
    *    not conform to the dtd defined in the <code>fromXml<code> method.
    */
   public PSDbComponentSet(Element[] items, Class compClass, String compType)
      throws PSUnknownNodeTypeException
   {
      if (compClass == null)
         throw new IllegalArgumentException("Supplied class cannot be null.");
   
      if (compClass.isInterface())
      {
         throw new IllegalArgumentException("Interfaces are not supported for "
               + "component collections, you must provide a class.");
      }
      
      if (null == compType)
      {
         compType = compClass.getName().substring(
               compClass.getName().lastIndexOf('.')+1);
      }

      if (null == compType || compType.trim().length() == 0)
      {
         throw new IllegalArgumentException(
               "Component type cannot be null or empty.");
      }
   
      m_class = compClass;
      m_memberComponentType = compType;
   
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
   
      //create a root element which name is constructed from the item class
      Element root = PSXmlDocumentBuilder.createRoot(doc,
         getNodeName());
   
      for (int i = 0; items != null && i < items.length; i++)
      {
         Node node = doc.importNode(items[i], true);
         root.appendChild(node);
      }
     
      //now we can reuse existing fromXml to create this Set.
      fromXml(root);
   }

   /**
    * To provide access to {@link #fromXml(Element,String)} during
    * construction.
    *
    * @param nodeName May be <code>null</code> or empty. In that case, it's
    *    equivalent to calling the 1 param ctor.
    */
   PSDbComponentSet(Element src, String nodeName)
      throws PSUnknownNodeTypeException
   {
      /*we don't call the super's ctor(Element) because we don't care about
         the key and db state */
      this();
      if (null == src)
         throw new IllegalArgumentException("Source element cannot be null.");
      fromXml(src, nodeName);
   }

   /**
    * For use when constructing from xml.
    */
   private PSDbComponentSet()
   {
      super(new PSKey(new String [] {"unused"}));
   }

   /**
    * The class of the contained component.
    *
    * @return class object contained by this Set.
    *    Never <code>null</code>.
    */
   public Class getMemberClass()
   {
      return m_class;
   }

   /**
    * Same as toXml(doc), except it allows the caller to set the node name of
    * the top level element.
    *
    * @param nodeName Node name as returned by
    * {@link com.percussion.cms.objectstore.PSDbComponentSet#getNodeName()
    * getNodeName(String)}. This is the name of the root node of the Set.
    * May not be <code>null</code> or empty.
    */
   Element toXml(Document doc, String nodeName)
   {
      // Threshold
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      if (null == nodeName || nodeName.trim().length() == 0)
      {
         throw new IllegalArgumentException(
               "Element node name must be supplied.");
      }

      //we don't call super because we don't care about those values
      Element root = doc.createElement(nodeName);
      root.setAttribute(XML_ATTR_STATE, STATE_LABELS[getState()]);
      root.setAttribute(XML_ATTR_CLASS, getMemberClass().getName());

      /**
       * Write out the component Set objects below
       */
      if (isEmpty() && m_deleteList.isEmpty())
         return root;

      Iterator iter = iterator();
      while (iter.hasNext())
      {
         IPSDbComponent comp = (IPSDbComponent) iter.next();
         Element elChild = comp.toXml(doc);
         root.appendChild(elChild);
      }

      //serialize the delete list
      if (m_deleteList.isEmpty())
         return root;

      iter = m_deleteList.iterator();
      while (iter.hasNext())
      {
         IPSDbComponent comp = (IPSDbComponent) iter.next();
         Element elChild = comp.toXml(doc);
         root.appendChild(elChild);
      }

      return root;
   }


   /**
    * See {@link IPSDbComponent#toXml(Document) interface} for description.
    * The dtd for this component is:
    * <pre><code>
    *    &lt;!ELEMENT getNodeName() (member.getNodeName()*&gt;
    *    &lt;!ATTLIST getNodeName()
    *       className #CDATA #OPTIONAL
    *       &gt;
    *
    *    &lt;!-- The className attribute is the fully qualified class name of
    *       the member elements. It will always be provided in the generated
    *       document. --&gt;
    *
    * </code></pre>
    */
   @Override
   public Element toXml(Document doc)
   {
      return toXml(doc, getNodeName());
   }

   /**
    * See {@link #toXml(Document) toXml} and {@link
    * IPSCmsComponent#fromXml(Element) interface} for details.
    * <p>If the className attribute is not present, a default
    * class name will be generated by taking the node name, and if
    * of the form 'PSXType', a class name of the form
    * 'com.percussion.cms.objectstore.PSType' will be used. If the
    * element doesn't begin with PSX, an exception will be thrown.
    * <p>If a class name is supplied, it will be used. If derived classes
    * limit the possible class names, then the derived class may override
    * this method to validate that the de-serialized name is an allowed one.
    * In general, this should not be necessary because this method does perform
    * a check of the node names and throws an exception if they don't match.
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
   void fromXml(Element src, String nodeName)
      throws PSUnknownNodeTypeException
   {
      //don't call super.fromXml because we don't use the key and state
      if (src == null)
         throw new IllegalArgumentException("Source element cannot be null.");

      m_set.clear();
      m_deleteList.clear();
      m_markedForDelete = false;

      String strClass = null;
      try
      {
         if ((nodeName == null) || (nodeName.trim().length() < 1))
            nodeName = getNodeName();

         PSXMLDomUtil.checkNode(src, nodeName);

         // if classname is already set, the xml must match that, otherwise we
         // set it from the xml
         strClass = src.getAttribute(XML_ATTR_CLASS).trim();

         PSXmlTreeWalker tree = new PSXmlTreeWalker(src);
         Element aEl = tree.getNextElement(
               PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);

         if (aEl == null)
            return;

         String strNodeName = aEl.getNodeName();

         // If classname is not supplied, define it conventionally
         if (m_class == null)
         {
            if (strClass.length() == 0)
            {
               strClass = "com.percussion.cms.objectstore.";
               if (!strNodeName.startsWith("PSX"))
               {
                  String[] args =
                  {
                     strClass,
                     getComponentType()
                  };
                  throw new PSUnknownNodeTypeException(
                        IPSCmsErrors.INVALID_ENTRY_CLASSNAME, args);
               }
               else
                  strClass +=
                        PSStringOperation.replace(strNodeName, "PSX", "PS");
            }
            m_class = Class.forName(strClass);
         }

         // Load the object and append to the current list
         Constructor ctor = m_class.getConstructor(new Class[] {Element.class});
         Element [] args = new Element[1];
         while (aEl != null && aEl.getNodeName().equalsIgnoreCase(strNodeName))
         {
            // Create the class
            args[0] = aEl;
            PSDbComponent aCmp = (PSDbComponent) ctor.newInstance(args);

            //must be set before we can add
            if (m_memberComponentType == null)
               m_memberComponentType = aCmp.getComponentType();

            if (aCmp.getState() == DBSTATE_MARKEDFORDELETE)
            {
               // add to delete list
               m_deleteList.add(aCmp);
            }
            else
            {
               // Add to Set
               add(aCmp);
            }
            // Get the next sibling node
            aEl = tree.getNextElement(PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
         }
      }
      catch (ClassNotFoundException e)
      {
         String[] args =
         {
            strClass,
            getComponentType(),
            e.getLocalizedMessage()
         };
         throw new PSUnknownNodeTypeException(
               IPSCmsErrors.COMPONENT_INSTANTIATION_ERROR, args);
      }
      catch (InstantiationException ie)
      {
         String[] args =
         {
            strClass,
            getComponentType(),
            ie.getLocalizedMessage()
         };
         throw new PSUnknownNodeTypeException(
               IPSCmsErrors.COMPONENT_INSTANTIATION_ERROR, args);
      }
      catch (IllegalAccessException iae)
      {
         String[] args =
         {
            strClass,
            getComponentType(),
            iae.getLocalizedMessage()
         };
         throw new PSUnknownNodeTypeException(
               IPSCmsErrors.COMPONENT_INSTANTIATION_ERROR, args);
      }
      catch (InvocationTargetException ite)
      {
         Throwable origException = ite.getTargetException();
         String msg = origException.getLocalizedMessage();
         String[] args =
         {
            strClass,
            getComponentType(),
            origException.getClass().getName() + ": " + msg
         };
         throw new PSUnknownNodeTypeException(
               IPSCmsErrors.COMPONENT_INSTANTIATION_ERROR, args);
      }
      catch (NoSuchMethodException nsme)
      {
         String[] args =
         {
            strClass,
            getComponentType(),
            nsme.getLocalizedMessage()
         };
         throw new PSUnknownNodeTypeException(
               IPSCmsErrors.COMPONENT_INSTANTIATION_ERROR, args);
      }
      catch (IllegalArgumentException iae)
      {
         //this should never happen because we checked ahead of time
         throw new RuntimeException(
               "Ctor parameter count changed unexpectedly.");
      }
   }


   /**
    * See {@link IPSDbComponent#markForDeletion() interface} for description.
    * It's the same as the base class, except when the toDbXml is called,
    * all objects in the collection at that time will be removed, not just
    * those in the collection when this method is called. To get the latter
    * behavior, call {@link #clear()}.
    */
   @Override
   public void markForDeletion()
   {
      m_markedForDelete = true;
   }

   /**
    * See {@link IPSDbComponent#setState(int) interface} for description.  This
    * class only cares if a state of <code>DBSTATE_MARKEDFORDELETE</code> is
    * set, which simply delegates the call to {@link #markForDeletion()}.  All
    * other states are ignored.
    */
   @Override
   public void setState(int newState)
   {
      if (!isValidState(newState))
         throw new IllegalArgumentException("Invalid state.");

      if (newState == DBSTATE_MARKEDFORDELETE)
         markForDeletion();
   }

   /**
    * See {@link IPSDbComponent#setPersisted() interface} for description.
    * Clears all entries in the delete list and removes all components that
    * are members if their state is DBSTATE_MARKEDFORDELETE.
    * Calls setPersisted on all other members.
    */
   @Override
   public void setPersisted()
      throws PSCmsException
   {
      Iterator it = m_set.iterator();
      while (it.hasNext())
      {
         IPSDbComponent c = (IPSDbComponent) it.next();
         if (c.getState() == DBSTATE_MARKEDFORDELETE)
         {
            c.setPersisted();
            it.remove();
         }
         else
            c.setPersisted();
      }

      //play it safe and mark these as well in case someone is keeping a ref
      it = m_deleteList.iterator();
      while (it.hasNext())
      {
         IPSDbComponent c = (IPSDbComponent) it.next();
         c.setPersisted();
      }
      m_deleteList.clear();

      m_markedForDelete = false;
   }


   /**
    * See {@link IPSDbComponent#toDbXml(Document, Element, IPSKeyGenerator, 
    * PSKey) IPSDbComponent} for full details.
    * <p>Creates an xml fragment properly formatted for the Rhythmyx server
    * that will modify the database to make the db instances of the objects
    * of the specified type consistent with these objects.
    * <p>All components that need to be removed get added w/ the DELETE action.
    * <p>If the state of this object is DBSTATE_UNMODIFIED, <code>null</code>
    * is returned.
    */
   @Override
   public void toDbXml(Document doc, Element root, IPSKeyGenerator keyGen,
         PSKey parent)
      throws PSCmsException
   {
      // Threshold - Unmodified list exists
      if (DBSTATE_UNMODIFIED == getState() && !m_markedForDelete)
         return;

      // process deletes first
      Iterator deletes = m_deleteList.iterator();
      while (deletes.hasNext())
      {
         PSDbComponent c = (PSDbComponent) deletes.next();
         c.toDbXml(doc, root, keyGen, parent);
      }

      int index = 0;
      // Modified list exists - create the xml for the server
      Iterator iter = iterator();
      while (iter.hasNext())
      {
         IPSDbComponent c = (IPSDbComponent) iter.next();
         // Check that this component is marked for delete
         if (m_markedForDelete && c.isPersisted())
            c.markForDeletion();

         if (c.getState() != DBSTATE_UNMODIFIED)
            c.toDbXml(doc, root, keyGen, parent);
      }
   }


   /**
    * The type of components allowed in this Set, as set in the ctor.
    *
    * @return The component type of the members of this Set. Never
    *    <code>null</code> or empty.
    */
   public String getMemberComponentType()
   {
      return m_memberComponentType;
   }

   /**
    * The state of this object is calculated from the state of its elements.
    * If this collection has been marked for deletion, DBSTATE_MARKEDFORDELETE
    * is returned. Otherwise, if all elements are DBSTATE_UNMODIFIED and the
    * delete list is empty, or there are no elements and the delete list is
    * empty, DBSTATE_UNMODIFIED will be returned. Othwerwise, if all elements
    * are new and the delete list is empty, DBSTATE_NEW is returned. Otherwise,
    * if there are any entries in delete list or any modified entries,
    * DBSTATE_MODIFIED is returned.
    *
    * @return One of the DBSTATE_xxx values. See description for more details.
    */
   public int getState()
   {
      if (m_markedForDelete)
         return DBSTATE_MARKEDFORDELETE;

      // Threshold - If delete list has elements then
      // modification is necessary
      if (m_deleteList.size() > 0)
         return DBSTATE_MODIFIED;

      if (m_set.isEmpty())
         return DBSTATE_UNMODIFIED;

      Iterator iter = iterator();

      // check the kids
      boolean allNew = true;
      boolean atLeastOneNew = false;
      while (iter.hasNext())
      {
         IPSDbComponent dbComp = (IPSDbComponent) iter.next();
         int nState = dbComp.getState();

         if (nState != DBSTATE_NEW)
            allNew = false;
         if (nState == DBSTATE_NEW)
            atLeastOneNew = true;
         if (nState == DBSTATE_MODIFIED || nState == DBSTATE_MARKEDFORDELETE)
            return DBSTATE_MODIFIED;
      }

      return allNew ? DBSTATE_NEW :
            (atLeastOneNew ? DBSTATE_MODIFIED : DBSTATE_UNMODIFIED);
   }


   /**
    * See {@link IPSCmsComponent#equals(Object) interface} for description.
    * This method ignores the delete list and state when performing the compare.
    */
   @Override
   public boolean equals(Object obj)
   {
      if (null == obj || !getClass().isInstance(obj))
         return false;
      if (this == obj)
         return true;

      PSDbComponentSet other = (PSDbComponentSet) obj;

      // Threshold - check class child object matches
      if (!other.getMemberClass().equals(getMemberClass()))
         return false;

      return m_set.equals(other.m_set);
   }

   /**
    * Adds the supplied component to this Set.
    * Note: If you add a component whose state is DBSTATE_MARKEDFORDELETE,
    * this component will be removed from this Set when the
    * <code>setPersisted()</code> method is called. This is equivalent to
    * adding an unmarked component, then removing it.
    *
    * @param comp The datatype must be the same as the type of this Set, else
    *    a <code>ClassCastException</code> will be thrown.
    *    May not be <code>null</code>.
    *
    * @throws ClassCastException if the supplied component doesn't have the
    *    same type as the type supplied during construction.
    * @throws IllegalArgumentException if <code>comp</code> is <code>null</code>
    */
   public boolean add(IPSDbComponent comp)
   {
      // Threshold must be m_class
      checkType(comp, true);
      return m_set.add(comp);
   }

   /**
    * Adds all of the components in the supplied collection to this Set.
    *
    * @param coll The datatype of all components in this collection must be
    *    the same as the type of this Set, else a
    *    <code>ClassCastException</code> will be thrown.
    *    May not be <code>null</code>, may be empty.
    *
    * @throws ClassCastException if the supplied component doesn't have the
    *    same type as the type supplied during construction.
    * @throws IllegalArgumentException if <code>coll</code> is <code>null</code>
    */
   public boolean addAll(Collection coll)
   {
      if (coll == null)
         throw new IllegalArgumentException("coll may not be null");

      boolean changed = false;
      Iterator it = coll.iterator();
      while (it.hasNext())
      {
         PSDbComponent comp = (PSDbComponent)it.next();
         boolean added = add(comp);
         if (added && !changed)
            changed = true;
      }
      return changed;
   }

   /**
    * Verifies that <code>comp</code> is an instance of the class passed in
    * the ctor.
    *
    * @param comp object to check its type. May be <code>null</code>.
    *
    * @param shouldThrow If <code>true</code>, then an exception will be thrown
    *    if the supplied component is not of the correct type.
    *
    * @return <code>true</code> if the component is OK for this set,
    *    <code>false</code> otherwise.
    *
    * @throws ClassCastException if our child class is not assignable
    *    from <code>comp</code>.
    *
    * @throws IllegalArgumentException if comp is <code>null</code>.
    */
   private boolean checkType(IPSDbComponent comp, boolean shouldThrow)

   {
      if (comp == null)
      {
         if (shouldThrow)
            throw new IllegalArgumentException("Component cannot be null.");
         else
            return false;
      }
      if (!comp.getComponentType().equals(m_memberComponentType))
      {
         if (shouldThrow)
         {
            /*not quite right, but I don't want to add more exceptions for an
               extremely rare possibility */
            throw new ClassCastException("Expected component type " +
               m_memberComponentType + ". Got " + comp.getComponentType());
         }
         else
            return false;
      }

      boolean isValid = true;
      if (!getMemberClass().isAssignableFrom(comp.getClass()))
      {
         if (shouldThrow)
         {
            throw new ClassCastException(
               comp.getClass().getName()
               + " must be a " + getMemberClass().getName());
         }
         isValid = false;
      }

      return isValid;
   }

   /**
    * Removes all entries in this Set.
    */
   public void clear()
   {
      Iterator iter = iterator();

      while (iter.hasNext())
      {
         IPSDbComponent dbComp = (IPSDbComponent) iter.next();
         int nState = dbComp.getState();

         if (nState != DBSTATE_NEW)
            m_deleteList.add(dbComp);
      }

      m_set.clear();
   }


   /**
    * Scans the entire Set looking for an entry that matches the supplied
    * component. For any entry, e, if e.equals(comp) is <code>true</code>,
    * <code>true</code> is returned.
    *
    * @param comp If <code>null</code> or not the same datatype,
    *    <code>false</code> is returned.
    *
    * @return <code>true</code> if comp matches any entry in this Set using
    *    the equals method, <code>false</code> otherwise.
    */
   public boolean contains(IPSDbComponent comp)
   {
      if (!checkType(comp, false))
         return false;

      return m_set.contains(comp);
   }

   /**
    * Returns the component from this collection which matches the specified
    * component. This uses the <code>equals()</code> method to compare
    * components.
    *
    * @param comp the component whose matching component from this
    * collection is to be returned, may not be <code>null</code>. The
    * datatype must be the same as the type of this Set, else a
    * <code>ClassCastException</code> will be thrown.

    * @return the matching component from this collection if one is found,
    * otherwise <code>null</code>
    *
    * @throws ClassCastException if the supplied component doesn't have the
    * same type as the type supplied during construction.
    *
    * @throws IllegalArgumentException if <code>comp</code> is <code>null</code>
    */
   public IPSDbComponent get(IPSDbComponent comp)
   {
      // Threshold must be m_class
      checkType(comp, true);

      IPSDbComponent retComp = null;
      Iterator it = iterator();
      while (it.hasNext())
      {
         IPSDbComponent tempComp = (IPSDbComponent)it.next();
         if (comp.equals(tempComp))
         {
            retComp = tempComp;
            break;
         }
      }
      return retComp;
   }

   /**
    * Check whether this Set contains any entries.
    *
    * @return <code>true</code> if size() == 0, <code>false</code> otherwise.
    */
   public boolean isEmpty()
   {
      return size() == 0;
   }

   /**
    * Gets an immutable iterator, meaning that this Set cannot be modified
    * by the returned iterator.
    *
    * @return An iterator over 0 or more entries, each of which is an
    *    IPSDbComponent of the same type. Never <code>null</code>.
    */
   public Iterator iterator()
   {
      return Collections.unmodifiableSet(m_set).iterator();
   }

   /**
    * Removes the the specified component from this Set.
    * If the component key is persisted, it is added to the delete list.
    *
    * @param comp If <code>null</code> or if the component doesn't match the
    *    types of the members, <code>false</code> is returned.
    *
    * @return <code>true</code> if a matching component was found and removed,
    *    <code>false</code> otherwise.  Match is made based on the
    *    <code>equals</code> method of the supplied component.
    */
   public boolean remove(IPSDbComponent comp)
   {
      if (!checkType(comp, false))
         return false;

      Iterator it = m_set.iterator();
      while (it.hasNext())
      {
         Object obj = it.next();
         if (comp.equals(obj))
         {
            IPSDbComponent setComp = (IPSDbComponent)obj;
            if (setComp.isPersisted())
            {
               setComp.markForDeletion();
               m_deleteList.add(setComp);
            }
            it.remove();
            return true;
         }
      }
      return false;
   }


   /**
    * Gets the number of entries in this Set.
    *
    * @return A value >= 0.
    */
   public int size()
   {
      return m_set.size();
   }

   /**
    * This cloneFull includes the dummy key info as well as
    * the deletelist information.
    *
    * @see com.percussion.cms.objectstore.PSDbComponent#cloneFull()
    */
   @Override
   public Object cloneFull()
   {
      PSDbComponentSet copy = null;
      copy = (PSDbComponentSet) super.cloneFull();

      copy.m_set = new HashSet(); // objects added below
      copy.m_deleteList = new ArrayList(); // objects added below

      Iterator iter = iterator();
      Iterator delIter = m_deleteList.iterator();

      // Clone list of objects
      while (iter.hasNext())
      {
         IPSDbComponent c = (IPSDbComponent) iter.next();
         copy.add((IPSDbComponent) c.clone());
      }

      // Clone delete list of objects
      while (delIter.hasNext())
      {
         IPSDbComponent c = (IPSDbComponent) delIter.next();
         IPSDbComponent clone = (IPSDbComponent) c.clone();
         copy.add(clone);
         copy.remove(clone);
      }
      return copy;
   }

   /**
    * This includes the dummy key info as well as the deletelist information.
    *
    * @see com.percussion.cms.objectstore.PSDbComponent#equalsFull(Object)
    */
   @Override
   public boolean equalsFull(Object obj)
   {
      // Threshold
      if (!equals(obj))
         return false;

      //we don't need to check the key because that is never used by this class
      PSDbComponentSet c = (PSDbComponentSet) obj;
      if (getState() != c.getState())
         return false;

      //order is irrelevant in delete list
      return PSDbComponentCollection.equalsIgnoreOrder(m_deleteList.iterator(),
            c.m_deleteList.iterator());
   }

   /**
    * This includes the dummy key info as well as
    * the deletelist information.
    *
    * @see com.percussion.cms.objectstore.PSDbComponent#hashCodeFull()
    */
   @Override
   public int hashCodeFull()
   {
      int nHash = hashCode();
      return nHash + PSDbComponentCollection.hashCodeIgnoresOrder(
            m_deleteList.iterator()) + ("" + getState()).hashCode();
   }

   /**
    * See {@link IPSCmsComponent#hashCode() interface} for description.
    * This method ignores the delete list when performing the calculation.
    */
   @Override
   public int hashCode()
   {
      int hash = m_set.hashCode();
      return hash + m_class.hashCode();
   }

   /**
    * See {@link IPSCmsComponent#clone() interface} for description. This
    * class varies in its implementation by not including the deleted list
    * in the cloned object and each cloned member will be as described in
    * {@link IPSDbComponent#clone()}. Except that any component whose state
    * is DBSTATE_MARKEDFORDELETE will not be cloned.
    */
   @Override
   public Object clone()
   {
      PSDbComponentSet copy = null;

      // If clone a persisted component list, the isPersisted() method
      // of this class may cause the super.clone() throw exception in
      // super.setState(). The workaround is to change isPersisted() behavior
      // to always return <code>false</code> during clone, set the behavior
      // back afterwards.

      m_isCloning = true;
      try
      {
         copy = (PSDbComponentSet) super.clone();
      }
      finally
      {
         m_isCloning = false;
      }

      copy.m_set = new HashSet(); // objects added below
      copy.m_deleteList = new ArrayList();

      Iterator iter = iterator();
      while (iter.hasNext())
      {
         IPSDbComponent c = (IPSDbComponent) iter.next();
         if (c.getState() != DBSTATE_MARKEDFORDELETE)
            copy.add((IPSDbComponent) c.clone());
      }

      return copy;
   }


   /**
    * This is for use by other objects using this class as a basis for
    * implementation.
    *
    * @return The collection that contains all the components that have been
    *    removed from this Set. Never <code>null</code>, may be empty. This
    *    must be treated as read-only and should not be cached.
    */
   Collection getDeleteCollection()
   {
      return m_deleteList;
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

      // Threshold - if we have member's in the delete list
      // return true
      if (m_deleteList.size() > 0)
         return true;

      // Check each child, if any one is persisted return
      // true
      Iterator comps = iterator();
      while (comps.hasNext())
      {
         if (((IPSDbComponent) comps.next()).isPersisted())
            return true;
      }

      return false;
   }

   /**
    * see base class for description. Overriden to define this objects
    * assignment in terms of it's contained objects assignment. This
    * object's key is just a dummy key.
    */
   @Override
   public boolean isAssigned()
   {
      // Threshold - if we have member's in the delete list
      // that objects key must have been persisted and assigned
      if (m_deleteList.size() > 0)
         return true;

      // Check each child, if any one is persisted return
      // true
      Iterator comps = iterator();
      while (comps.hasNext())
      {
         if (((IPSDbComponent) comps.next()).isAssigned())
            return true;
      }

      return false;
   }

   /**
    * Class object of child component being contained. Set by time ctor is
    * finished, then never changes after that (not even fromXml).
    */
   private Class m_class = null;

   /**
    * All the components managed by this Set that have not been deleted.
    * Never <code>null</code>. Every entry is an IPSDbComponent of the same
    * class.
    */
   private Set m_set = new HashSet();

   /**
    * List of components that have been removed from this collection. Never
    * <code>null</code>. Every entry is an IPSDbComponent of the same
    * class that has a state equal to DBSTATE_MARKEDFORDELETE.
    */
   private List m_deleteList = new ArrayList();

   /**
    * We use this flag instead of the base's state attribute because of the
    * interaction between the key state and the component state. Our key is
    * never persisted, so we could never set this state.
    * <p>Defaults to <code>false</code>. Cleared by setPersisted.
    */
   private boolean m_markedForDelete = false;

   /**
    * The component type name of the objects managed by this collection.
    * Set in ctor, then never <code>null</code> or empty. Never changed
    * after set.
    */
   private String m_memberComponentType;

   /**
    * If clone a persisted component Set, the isPersisted() method of this
    * class will effect super.clone(), may throw exception in super.setState().
    * The workaround is to change isPersisted() behavior to always return
    * <code>false</code> during clone, set the behavior back afterwards.
    */
   private boolean m_isCloning = false;

   // Public static defines
   public static final String XML_ATTR_CLASS = "className";
   public static final String XML_ATTR_SEQFLAG = "ordered";
   public static final String XML_FALSE = "no";

}
