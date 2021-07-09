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
package com.percussion.services.contentmgr.impl;

import com.percussion.services.contentmgr.impl.legacy.PSContentRepository;
import com.percussion.services.contentmgr.impl.legacy.PSTypeConfiguration;
import com.percussion.utils.types.PSPair;

import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * Handy utilities for the content manager
 * 
 * @author dougrand
 * 
 */
public class PSContentUtils
{
   /**
    * Resolve the reference to a field in terms of one of the generated classes.
    * The fieldname is either a direct field on the object, which remains
    * unmodified, or a reference to the component summary, which requires a
    * dereference path.
    * 
    * @param fieldname the fieldname, never <code>null</code> or empty
    * @param type specifies the type to resolve against, if specified it will
    *           cause this function to return <code>null</code> for fields
    *           that don't exist for the given type
    * 
    * @return the reference, the first element in the pair contains the field,
    *         the second contains a class reference. The class reference is
    *         <code>null</code> for content status fields and the primary key
    *         fields contentid and revision. If the field cannot be resolved
    *         then <code>null</code> is returned.
    */
   public static PSPair<String, Class> resolveFieldReference(String fieldname,
         PSTypeConfiguration type)
   {
      if (StringUtils.isBlank(fieldname))
      {
         throw new IllegalArgumentException(
               "fieldname may not be null or empty");
      }
      fieldname = internalizeName(fieldname);
      String mapped = PSContentRepository.mapCSFieldToProperty(fieldname);
      if (mapped != null)
      {
         fieldname = "cs.m_" + mapped;
         return new PSPair<>(fieldname, null);
      }
      else if (fieldname.equals("sys_contentid")
            || fieldname.equals("sys_revision"))
      {
         fieldname = "id." + fieldname;
         return new PSPair<>(fieldname, type.getMainClass());
      }
      else if (isNonPropertyRef(fieldname)
            || fieldname.startsWith("sys_componentsummary."))
      {
         return new PSPair<>(fieldname, null);
      }
      if (type != null)
      {
         for (PSTypeConfiguration.ImplementingClass c : type
               .getImplementingClasses())
         {
            Class clazz = c.getImplementingClass();
            List<String> props = type.getProperties().get(clazz);
            if (props != null && props.contains(fieldname))
            {
               return new PSPair<>(fieldname, clazz);
            }
         }
         if (type.getSimpleChildProperties().contains(fieldname))
         {
            return new PSPair<>(fieldname, type.getMainClass());
         }
      }
      return null;
   }

   /**
    * Check if this fieldname is a reference to one of a number of things that
    * are not a content item property. Used by the query engine to manage the
    * query creation process.
    * 
    * @param fieldname the fieldname, never <code>null</code> or empty
    * @return <code>true</code> if this is a folder or id collection reference
    */
   public static boolean isNonPropertyRef(String fieldname)
   {
      if (StringUtils.isBlank(fieldname))
      {
         throw new IllegalArgumentException(
               "fieldname may not be null or empty");
      }
      return fieldname.startsWith("f.") || isIdCollectionRef(fieldname);
   }

   /**
    * Internalize rx: names for content types and properties.
    * 
    * @param name the name, never <code>null</code> or empty
    * @return the cleaned up name
    */
   public static String internalizeName(String name)
   {
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name may not be null or empty");
      }
      if (name.startsWith("rx:"))
      {
         name = name.substring(3);
      }
      return name;
   }

   /**
    * Externalize rx: names for content types and properties.
    * 
    * @param name the name, never <code>null</code> or empty
    * @return the cleaned up name
    */
   public static String externalizeName(String name)
   {
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name may not be null or empty");
      }
      if (!name.startsWith("rx:"))
      {
         name = "rx:" + name;
      }
      return name;
   }

   /**
    * This method checks to see if the passed name is of the form t#.aaa.aaa.
    * 
    * @param name the id name, assumed never <code>null</code> or empty
    * @return <code>true</code> if the name matches the form t#.aaa.aaa where #
    *         is a number and aaa is an alpha string
    */
   public static boolean isIdCollectionRef(String name)
   {
      if (name.charAt(0) != 't')
         return false;
      if (name.length() < 5)
         return false;
      int pos = 1;
      // Skip digits
      while (pos < name.length())
      {
         char ch = name.charAt(pos);
         if (Character.isDigit(ch))
         {
            pos++;
         }
         else
         {
            break;
         }
      }
      if (pos >= name.length())
         return false; // Can't end before a dot
      if (name.charAt(pos++) != '.')
         return false; // Next char isn't a dot
      int dotcount = 0;
      // The rest should be alpha characters
      while (pos < name.length())
      {
         char ch = name.charAt(pos);
         if (Character.isLetter(ch))
         {
            pos++;
         }
         else if (ch == '.')
         {
            dotcount++;
            if (dotcount > 1)
               return false; // Exceeded allowed segments
            pos++;
         }
         else
         {
            return false;
         }
      }

      return true;
   }

   /**
    * Create the right hql reference for the given refs class
    * 
    * @param ref the field reference, never <code>null</code> and the
    *   referenced property must be non-empty
    * @param classes the in use classes, which are ordered, never
    *           <code>null</code>
    * @return the reference, never <code>null</code>
    */
   public static String makeQueryRef(PSPair<String, Class> ref,
         List<Class> classes)
   {
      if (ref == null)
      {
         throw new IllegalArgumentException("ref may not be null");
      }
      if (classes == null)
      {
         throw new IllegalArgumentException("classes may not be null");
      }
      if (StringUtils.isBlank(ref.getFirst()))
      {
         throw new IllegalArgumentException(
               "property reference must be non-empty");
      }
      StringBuilder b = new StringBuilder(3 + ref.getFirst().length());
      
      if (ref.getSecond() != null)
      {
         int i = classes.indexOf(ref.getSecond());
         if (i < 0)
            throw new IllegalStateException("Reference " 
                  + ref.getSecond().getCanonicalName()
                  + " refers to an unknown class");
         b.append('c');
         b.append(i);
         b.append('.');
       
      }
      else
      {
         // Do nothing for content status field, already referenced
      }
      b.append(ref.getFirst());
      return b.toString();
   }
}
