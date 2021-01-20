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

import com.percussion.util.PSCollection;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The PSCollectionComponent class implements some of the IPSComponent
 * interface as a convenience to objects extending this class which are
 * also extensions of the collection class.
 *
 * @author       Tas Giakouminakis
 * @version     1.0
 * @since    1.0
 */
public abstract class PSCollectionComponent
   extends com.percussion.util.PSCollection implements IPSComponent
{
   /**
    * Simplified method for converted a collection to XML.
    *
    * @param   doc         the XML document
    *
    * @param   root         the element to add the XML objects to
    *
    * @param   coll         a collection containing IPSComponent objects
    */
   public static void appendCollectionToXml(
      Document doc, Element root, PSCollection coll)
   {
      if (coll != null) {
         IPSComponent entry;
         int size = coll.size();
         for(int i=0; i < size; i++)
         {
            entry = (IPSComponent)coll.get(i);
            root.appendChild(entry.toXml(doc));
         }
      }
   }

   /**
    * Construct a collection component to store objects of the specified
    * type.
    *
    * @param      className   the name of the class which this collection's
    *                         members must be or extend
    *
    * @exception   ClassNotFoundException   if the specified class cannot be
    *                                     found
    */
   protected PSCollectionComponent(java.lang.String className)
      throws ClassNotFoundException
   {
      super(className);
   }

   /**
    * Construct a collection component to store objects of the specified
    * type with the specified initial capacity and with its capacity increment 
    * equal to zero.
    *
    * @param cl the class which this collection's members must be or extend
    * @param initialCapacity   the initial capacity of the collection.
    */
   public PSCollectionComponent(Class cl,
         int initialCapacity)
   {
      super(cl, initialCapacity);
   }

   /**
    * Construct a collection component to store objects of the specified
    * class.
    *
    * @param      cl           the class which this collection's
    *                         members must be or extend
    */
   protected PSCollectionComponent(Class cl)
   {
      super(cl);
   }

   /**
    * Get the id assigned to this component.
    *
    * @return                the id assigned to this component
    */
   public int getId()
   {
      return m_id;
   }

   /**
    * Get the id assigned to this component.
    *
    * @param    id       the to assign the component
    */
   public void setId(int id)
   {
      m_id = id;
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param c a valid PSComponent. 
    */
   public void copyFrom( PSCollectionComponent c )
   {
      if ( null == c )
         throw new IllegalArgumentException( "invalid object for copy");
      setId( c.getId());
      // copy all elements in supplied collection to this one
      int size = c.size();
      for ( int index = 0; index < size; index++ )
      {
         add( c.get( index ));
      }
      setId( c.getId());
   }

   /**
    * This method is called to create an XML element node with the
    * appropriate format for the given object. An element node may contain a
    * hierarchical structure, including child objects. The element node can
    * also be a child of another element node.
    *
    * @return     the newly created XML element node
    */
   public abstract Element toXml(Document doc);

   /**
    * This method is called to populate an object from an XML
    * element node. An element node may contain a hierarchical structure,
    * including child objects. The element node can also be a child of
    * another element node.
    *
    * @exception PSUnknownNodeTypeException   if the XML element node does not
    *                                         represent a type supported
    *                                         by the class.
    */
   public abstract void fromXml(Element sourceNode, IPSDocument parentDoc,
                        java.util.ArrayList parentComponents)
      throws PSUnknownNodeTypeException;

   /**
    * Validates this object within the given validation context. The method
    * signature declares that it throws PSValidationException, but the
    * implementation must not directly throw any exceptions. Instead, it
    * should register any errors with the validation context, which will
    * decide whether to throw the exception (in which case the implementation
    * of <CODE>validate</CODE> should not catch it unless it is to be
    * rethrown).
    *
    * @param   cxt The validation context.
    *
    * @throws   PSValidationException According to the implementation of the
    * validation context (on warnings and/or errors).
    */
   public void validate(IPSValidationContext cxt) throws PSValidationException
   {
      for (int i = 0; i < size(); i++)
      {
         Object o = get(i);
         if (o instanceof IPSComponent)
         {
            IPSComponent comp = (IPSComponent)o;
            comp.validate(cxt);
         }
      }
   }

   /**
    * Add this to the list of parent objects in the array list.
    * <P>
    * After a call to this method, the caller should keep get the size
    * of the arraylist so that a call to resetParentList can
    * be made (with size - 1) to allow for proper reset.
    *
    * @param      parentComponents      the parent list
    *
    * @return      the new parent list (in case parentComponents was null)
    */
   protected java.util.ArrayList updateParentList(
      java.util.ArrayList parentComponents)
   {
      if (parentComponents == null)
         parentComponents = new java.util.ArrayList();

      parentComponents.add(this);

      return parentComponents;
   }

   /**
    * Reset the list of parent objects in the array list to the specified
    * size.
    *
    * @param      parentComponents      the parent list
    *
    * @param      size                  the size to set the list to
    */
   protected void resetParentList(
      java.util.ArrayList parentComponents, int size)
   {
      if (parentComponents == null)
         return;

      if (size == 0)
         parentComponents.clear();
      else {
         for (int i = parentComponents.size(); i > size; ) {
            i--;
            parentComponents.remove(i);
         }
      }
   }

   /**
    * The id assigned to this component.
    */
   protected int m_id = 0;
}

