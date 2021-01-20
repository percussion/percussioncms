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
package com.percussion.utils.jsr170;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Represents an absolute or relative JSR-170 path. A path is made up of path
 * elements. Each element can also index into a specific named element.
 * 
 * @author dougrand
 */
public final class PSPath
{
   private static class Element
   {
      int mi_index = -1;

      String mi_name = null;

      /* (non-Javadoc)
       * @see java.lang.Object#equals(java.lang.Object)
       */
      @Override
      public boolean equals(Object obj)
      {
         Element b = (Element) obj;
         return mi_index == b.mi_index &&
            mi_name.equals(b.mi_name);
      }

      /* (non-Javadoc)
       * @see java.lang.Object#hashCode()
       */
      @Override
      public int hashCode()
      {
         return mi_name.hashCode() + mi_index;
      }

      /* (non-Javadoc)
       * @see java.lang.Object#toString()
       */
      @Override
      public String toString()
      {
         StringBuffer b = new StringBuffer();
         b.append(mi_name);
         if (mi_index >= 0)
         {
            b.append('[');
            b.append(Integer.toString(mi_index));
            b.append(']');
         }
         return b.toString();
      }
      
      
   }

   /**
    * Holds the path elements, never <code>null</code> after construction.
    */
   private Element m_elements[] = null;

   /**
    * <code>true</code> if this path is relative
    */
   private boolean m_isRelative = true;
   
   /**
    * Copy elements from the from path to the new path. The new path is
    * relative if the fromPath is relative or the start is not zero.
    * @param fromPath the from path, assumed not <code>null</code>
    * @param start the start, assumed within range
    * @param end the end, assumed within range
    */
   private PSPath(PSPath fromPath, int start, int end)
   {
      m_elements = new Element[end - start];
      m_isRelative = fromPath.isRelative() || start > 0;
      for(int i = start; i < end; i++)
      {
         m_elements[i - start] = fromPath.m_elements[i];
      }
   }

   /**
    * Create a path
    * 
    * @param path path in string format, e.g. /a/b/c, never <code>null</code>
    *           or empty.
    */
   public PSPath(String path) {
      if (StringUtils.isBlank(path))
      {
         throw new IllegalArgumentException("path may not be null or empty");
      }
      if (path.equals("/") || path.equals("//"))
      {
         m_elements = new Element[0];
         m_isRelative = false;
         return;
      }
      
      String parts[] = path.split("/");
      int start = 0;      
      if (parts[0].length() == 0)
      {
         m_isRelative = false;
         start = 1;
      }
      m_elements = new Element[parts.length - start];
      for (int i = 0; i < (parts.length - start); i++)
      {
         Element e = new Element();
         String name = parts[i + start];
         int bracket = name.indexOf('[');
         if (bracket > 0)
         {
            int endbracket = name.indexOf(']');
            name = name.substring(0, bracket);
            if (endbracket <= (bracket + 1))
            {
               throw new IllegalArgumentException(
                     "Bad index specification in path: " + parts[i]);
            }
            String ind = parts[i].substring(bracket + 1, endbracket);
            try
            {
               e.mi_index = Integer.parseInt(ind);
            }
            catch (NumberFormatException ex)
            {
               throw new IllegalArgumentException("Badly formated index: "
                     + ind);
            }
         }
         e.mi_name = name;
         m_elements[i] = e;
      }
   }

   /**
    * Check if this is a relative path
    * 
    * @return <code>true</code> if this is a relative path
    */
   public boolean isRelative()
   {
      return m_isRelative;
   }

   /**
    * Count the path components
    * 
    * @return the number of components
    */
   public int getCount()
   {
      return m_elements.length;
   }

   /**
    * Get the index
    * 
    * @param component the index of the path component must be in the range 0 to 
    * the number of path components less 1
    * @return the index value or -1 if this path element is not indexed
    */
   public int getIndex(int component)
   {
      checkRef(component);
      return m_elements[component].mi_index;
   }
   
   /**
    * Get the last index
    * @return the last index or -1 if the last element is not indexed
    */
   public int getLastIndex()
   {
      return getIndex(getCount() - 1);
   }
   
   /**
    * Get the specified component's name
    * @param component the component index, must be in the range 0 to 
    * the number of path components less 1
    * @return the name, never <code>null</code> or empty
    */
   public String getName(int component)
   {
      checkRef(component);
      return m_elements[component].mi_name;
   }
   
   /**
    * Get the last path component
    * @return the last path component, never <code>null</code> or empty
    */
   public String getLastName()
   {
      return getName(getCount() - 1);
   }

   /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      //FB: NP_EQUALS_SHOULD_HANDLE_NULL_ARGUMENT NC 1-17-16
      if(obj == null) 
         return false;
      
      PSPath b = (PSPath) obj;
      if (b.m_elements.length != m_elements.length)
         return false;
      for(int i = 0; i < m_elements.length; i++)
      {
         if (m_elements[i].equals(b.m_elements[i]) == false)
            return false;
      }
      return true;
   }

   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      HashCodeBuilder hb = new HashCodeBuilder();
      for(int i = 0; i < m_elements.length; i++)
      {
         hb.append(m_elements[i]);
      }
      return hb.toHashCode();
   }

   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      StringBuilder b = new StringBuilder();
      if (!isRelative())
      {
         b.append("/");
      }
      for(int i = 0; i < m_elements.length; i++)
      {
         boolean last = (m_elements.length - i) == 1;
         Element e = m_elements[i];
         b.append(e.toString());
         if (! last)
         {
            b.append('/');
         }
      }
      return b.toString();
   }
   
   /**
    * Get a new path that has all but the last element. Useful for extracting
    * the node from a node/property path, or other similar requirements
    * @return a new PSPath that has the elements from this path, or the current
    * path if there are no elements to remove
    */
   public PSPath getAllButLast()
   {
      if (getCount() == 0)
         return this;
      else
         return new PSPath(this, 0, getCount() - 1);
   }
   
   /**
    * Get a new path that has all but the first element. Useful for traversing
    * a node structure.
    * @return a new PSPath that has the last n elements from this path, or the
    * current path if there are no elements
    */
   public PSPath getRest()
   {
      if (getCount() == 0)
         return this;
      else
         return new PSPath(this, 1, getCount());
   }

   /**
    * Get a subpath with a specific start and end location within the path. Does
    * not include the end location, which means that <code>end = getCount()</code>
    * will return to the end of the path.
    * 
    * @param start start location, zero based, cannot be negative or larger than
    * the count of elements less one. Start must be less than end.
    * @param end end location, zero based, cannot be negative or larger than the
    * count of elements
    * @return the subpath, including the start but not the end location
    */
   public PSPath subpath(int start, int end)
   {
      if (start > end)
      {
         throw new IllegalArgumentException("start must proceed end");
      }
      return new PSPath(this, start, end);
   }

   /**
    * Get a subpath, like calling {@link #subpath(int, int)} with the values
    * start and <code>end = getCount()</code>.
    * @param start start location, zero based, cannot be negative or larger than
    * the count of elements less one
    * @return the subpath, including the start to the end of the path
    */
   public PSPath subpath(int start)
   {
      return subpath(start,getCount());
   }
   
   /**
    * Check the index
    * @param component the component index
    */
   private void checkRef(int component)
   {
      if (component < 0 || component >= m_elements.length)
      {
         throw new IllegalArgumentException(
               "component must be positive and not exceed the number of path components");
      }
   }
}
