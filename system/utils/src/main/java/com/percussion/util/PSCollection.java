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
package com.percussion.util;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * The PSCollection class is used to maintain a collection of objects.
 * Objects can be added, changed or removed from the collection. All
 * objects in the collection must be of the same class.
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public class PSCollection extends PSConcurrentList
{
   /**
    * Construct a collection object to store objects of the specified type.
    *
    * @param      className   the name of the class which this collection's
    *                                                                     members must be or extend
    *
    * @exception   ClassNotFoundException   if the specified class cannot be
    *                                                                                 found
    */
   public PSCollection(String className)
      throws ClassNotFoundException
   {
      this(Class.forName(className));
   }

   /**
    * Construct a collection object to store objects of the specified type.
    *
    * @param      cl            the class which this collection's
    *                                                                     members must be or extend
    */
   public PSCollection(Class cl)
   {
      m_memberClass = cl;
   }

   /**
    * Construct a collection object to store objects of the specified type
    * with the specified initial capacity and with its capacity increment equal
    * to zero.
    *
    * @param cl the class which this collection's members must be or extend.
    * @param initialCapacity   the initial capacity of the collection.
    */
   public PSCollection(Class cl, int initialCapacity)
   {
      m_memberClass = cl;
   }

   /**
    * Construct a collection object from the objects in the supplied Iterator.
    *
    * @param i An iterator over <code>zero</code> or more objects.  All objects
    * must be of the same type.  May not be <code>null</code>.
    *
    * @throws ClassCastException if all of the objects under the iterator are
    * not of the same type.
    */
   public PSCollection(Iterator i)
      throws ClassCastException
   {
      m_memberClass = null;

      while (i.hasNext())
      {
         Object o = i.next();
         if (m_memberClass == null)
            m_memberClass = o.getClass();

         add(o);
      }

   }

   /**
    * Default constructor needed for serialization.
    */
   protected PSCollection()
   {
      super();
   }
   
   /**
    * Inserts the specified element at the specified index to the collection.
    * All elements in the collection must be of the class or extend the
    * class which was defined at construction of the collection. If the
    * object is of an incorrect type, an exception will be thrown.
    *
    * @param index the insert position
    * @param o   the object to add to the collection
    * @throws ClassCastException if the object is not of the appropriate class
    */
   public void add(int index, Object o)
      throws ArrayIndexOutOfBoundsException, ClassCastException
   {
      checkType(o);

      super.add(index, o);
   }

   /**
    * Adds the specified element to the collection. All elements in the
    * collection must be of the class or extend the class which was
    * defined at construction of the collection. If the object is of an
    * incorrect type, an exception will be thrown.
    *
    * @param o   the object to add to the collection
    * @return <code>true</code> if the object was added,
    *        throws ClassCastException otherwise
    * @throws ClassCastException if the object is not of the appropriate class
    */
   public boolean add(Object o)
      throws ClassCastException
   {
      checkType(o);

      return super.add(o);
   }

   /**
    * Adds the specified element to the collection. All elements in the
    * collection must be of the class or extend the class which was
    * defined at construction of the collection. If the object is of an
    * incorrect type, an exception will be thrown.
    *
    * @param o   the object to add to the collection
    * @throws ClassCastException if the object is not of the appropriate class
    */
   public void addElement(Object o)
      throws ClassCastException
   {
      add(o);
   }

   /**
    * Adds the specified collection to this collection. All elements in the
    * collection must be of the class or extend the class which was
    * defined at construction of the collection. If the object is of an
    * incorrect type, an exception will be thrown.
    *
    * @param c   the collection to add to this collection
    * @return <code>true</code> if the collection was added,
    *        throws ClassCastException otherwise
    * @throws ClassCastException if the object is not of the appropriate class
    */
   public boolean addAll(Collection c)
      throws ArrayIndexOutOfBoundsException, ClassCastException
   {
      checkType(c);

      return super.addAll(c);
   }

   /**
    * Inserts the specified collection to this collection at the specified
    * index. All elements in the collection must be of the class or extend
    * the class which was defined at construction of the collection. If the
    * object is of an incorrect type, an exception will be thrown.
    *
    * @param c   the collection to add to this collection
    * @return <code>true</code> if the collection was added,
    *        throws ClassCastException otherwise
    * @throws ClassCastException if the object is not of the appropriate class
    */
   public boolean addAll(int index, Collection c)
      throws ArrayIndexOutOfBoundsException, ClassCastException
   {
      checkType(c);

      return super.addAll(index, c);
   }

   /**
    * Sets the specified element in the collection. All elements in the
    * collection must be of the class or extend the class which was
    * defined at construction of the collection. If the object is of an
    * incorrect type, an exception will be thrown.
    *
    * @param index the index of the element to set
    * @param o the object to set
    * @return the previous element in the specified position
    * @throws ArrayIndexOutOfBoundsException   if index is out of range
    * @throws ClassCastException   if the object is not of the appropriate class
    */
   public Object set(int index, Object o)
      throws ArrayIndexOutOfBoundsException, ClassCastException
   {
      checkType(o);

      return super.set(index, o);
   }

   /**
    * Sets the specified element in the collection. All elements in the
    * collection must be of the class or extend the class which was
    * defined at construction of the collection. If the object is of an
    * incorrect type, an exception will be thrown.
    *
    * @param index the index of the element to set
    * @param o the object to set
    * @throws ArrayIndexOutOfBoundsException   if index is out of range
    * @throws ClassCastException   if the object is not of the appropriate class
    */
   public void setElementAt(Object o, int index)
      throws ArrayIndexOutOfBoundsException, ClassCastException
   {
      checkType(o);

      set(index, o);
   }

   public void insertElementAt(Object o, int i){
      this.add(i,o);
   }

   /**
    * Get the Class type of valid member objects.
    *
    * @return the class type
    */
   public Class getMemberClassType()
   {
      return m_memberClass;
   }

   /**
    * Get the name of the Class type of valid member objects.
    *
    * @return the name of the class type
    */
   public String getMemberClassName()
   {
      return m_memberClass.getName();
   }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PSCollection)) return false;
        PSCollection that = (PSCollection) o;
        return m_memberClass.equals(that.m_memberClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_memberClass);
    }

    /**
    * Check the type of the object and throw the appropriate execption if
    * it's the wrong type.
    *
    * @param o the object to check
    * @exception ClassCastException   if the object is of the wrong type
    */
   protected void checkType(Object o)
      throws ClassCastException
   {
      if (!(m_memberClass.isInstance(o)))
         throw new ClassCastException("Cannot add an object of class " +
            o.getClass().getName() + " to the collection of " +
            m_memberClass.getName());
   }

   /**
    * Check the type of all objects in the provided collection and throw the
    * appropriate execption if any of the collections objects has the wrong
    * type.
    *
    * @param c the collection to check
    * @exception ClassCastException   if any object in the provided collection
    *    is of the wrong type.
    */
   private void checkType(Collection c)
      throws ClassCastException
   {
      Iterator i = c.iterator();
      while (i.hasNext())
      {
         Object o = i.next();
         if (!(m_memberClass.isInstance(o)))
            throw new ClassCastException("Cannot add an object of class " +
               o.getClass().getName() + " to the collection of " +
               m_memberClass.getName());
      }
   }
   /**
    * Creates a deep copy of PSCollection object.
    * First clear the clone collection, then  check if the objects inside
    * the collection is immutable if so the method just add to the clone.
    * If not call the corresponding clone method for each of the object.
    * The caller must perform their own clone of this object if
    * the member of the collection are resources such as inputstream,
    * database connection etc. 'String' and 'File' are the only classes
    * considered immutable.
    * Each object inside the collection has to have clone() method.
    *
    * @return A new collection with each mutable member cloned and a reference
    *    copy of each immutable member.
    *
    * @throws InternalError If any mutable member doesn't implement the clone
    *    method or there are any problems executing that method.
    */
   public Object clone()
   {
      try {
         PSCollection copy = (PSCollection) super.clone();
         copy.clear();
         for (Iterator iter = this.iterator(); iter.hasNext(); ) {
            Object o = null;
            try {
               o = iter.next();
               //String is an immutable object don't need to clone
               if (o instanceof String) {
                  copy.add(o);
               }
               //File is an immutable object don't need to clone
               else if (o instanceof File) {
                  copy.add(o);
               } else {
                  copy.add(
                          o.getClass().getDeclaredMethod("clone", null).invoke(o, null));
               }
            } catch (NoSuchMethodException e) {
               //Object does not have clone implemented
               throw new InternalError("While attempting to clone PSCollection, "
                       + "a member was found that does not contain an accessible "
                       + "'clone' method."
                       + "\r\nThe offending class is " + o.getClass().getName());
            } catch (IllegalAccessException e) {
               //clone method is not accessible
               throw new InternalError(e.toString());
            } catch (InvocationTargetException e) {
               //clone method throw an exception
               throw new InternalError(e.toString());
            }
         }
         return copy;
      } catch (CloneNotSupportedException e) {
         throw new InternalError(e.toString());
      }
   }

   public  Object lastElement(){
      if(this.size()>0){
         return this.get(this.size()-1);
      }else{
         throw new NoSuchElementException();
      }
   }

   public void removeAllElements(){
      this.clear();
   }

   public Object elementAt(int index){
      return this.get(index);
   }

   public Object firstElement(){
      if(this.size()>0)
      return this.get(0);
      else
         throw new NoSuchElementException();
   }
   /**
    * The one and only valid class type for this collection.
    */
   private Class m_memberClass;
}
