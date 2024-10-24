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

package com.percussion.util;

/**
 * The PSEntrySet class is used to provide a convenient container for
 * key/value pairs. Unlike the java.util.Map.Entry interface suggests,
 * this implementation is not backed by a mapping. As such, all references
 * to backend mappings should be ignored.
 * 
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSEntrySet implements java.util.Map.Entry
{
   /**
    * Create an entry set for the specified key and value.
    *
    * @param   key         the key corresponding to this entry
    *
    * @param   value         the value corresponding to this entry
    */
   public PSEntrySet(Object key, Object value)
   {
      super();
      m_key = key;
      m_value = value;
   }
   
   /**
    * Returns the key corresponding to this entry.
    *
    * @return               the key corresponding to this entry.
    */
   public Object getKey()
   {
      return m_key;
   }

   /**
    * Returns the value corresponding to this entry.
    *
    * @return               the value corresponding to this entry.
    */
   public Object getValue()
   {
      return m_value;
   }

   /**
    * Replaces the value corresponding to this entry with the specified
    * value.
    *
    * @param      value      new value to be stored in this entry.
    *
    * @return               old value corresponding to the entry.
    *
    * @exception   UnsupportedOperationException
    *                        if the put operation is not supported by the
    *                        backing map.
    *
    * @exception   ClassCastException
    *                        if the class of the specified value prevents it
    *                        from being stored in the backing map.
    *
    * @exception   IllegalArgumentException
    *                        if some aspect of this value prevents it from
    *                        being stored in the backing map.
    *
    * @exception   NullPointerException
    *                        the backing map does not permit null values,
    *                        and the specified value is null.
    */
   public Object setValue(Object value)
   {
      Object oldValue = m_value;
      m_value = value;
      return oldValue;
   }

   /**
    * Compares the specified object with this entry for equality.
    * Returns true if the given object is also a map entry and the
    * two entries represent the same mapping. More formally, two
    * entries e1 and e2 represent the same mapping if
    * <PRE><CODE>
    *     (e1.getKey()==null ?
    *      e2.getKey()==null : e1.getKey().equals(e2.getKey()))  &&
    *     (e1.getValue()==null ?
    *      e2.getValue()==null : e1.getValue().equals(e2.getValue()))
    * </CODE></PRE>
    * <P>
    * This ensures that the equals method works properly across
    * different implementations of the Map.Entry interface.
    *
    * @param      o         object to be compared for equality with this
    *                        map entry.
    *
    * @return               <code>true</code> if the specified object is
    *                        equal to this map entry.
    */
   public boolean equals(Object o)
   {
      if (o == null)
         return false;

      if (!(o instanceof java.util.Map.Entry))
         return false;

      java.util.Map.Entry entry = (java.util.Map.Entry)o;
      Object key = entry.getKey();
      Object val = entry.getValue();

      return ((m_key == null) ? (key == null) : m_key.equals(key)) &&
         ((m_value == null) ? (val == null) : m_value.equals(val));
   }

   /**
    * Returns the hash code value for this map entry. The hash code of a
    * map entry e is defined to be: 
    * <PRE><CODE>
    *     (e.getKey()==null   ? 0 : e.getKey().hashCode()) ^
    *     (e.getValue()==null ? 0 : e.getValue().hashCode())
    *
    * This ensures that e1.equals(e2) implies that
    * e1.hashCode()==e2.hashCode() for any two Entries e1 and e2,
    * as required by the general contract of Object.hashCode.
    *
    * @return               the hash code value for this map entry.
    *
    * @see                  java.lang.Object#hashCode
    * @see                  java.lang.Object#equals(java.lang.Object)
    * @see                  #equals(java.lang.Object)
    */
   public int hashCode()
   {
      return ((m_key == null) ? 0 : m_key.hashCode()) ^
         ((m_value == null) ? 0 : m_value.hashCode());
   }   


   protected Object m_key;
   protected Object m_value;
}

