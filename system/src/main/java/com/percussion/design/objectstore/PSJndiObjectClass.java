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

package com.percussion.design.objectstore;

/**
 * Class to define an objectClass and its properties for Jndi group providers.
 */
public class PSJndiObjectClass
{
   /**
    * Creates an instance with the provided values.
    *
    * @param objectClass The name of this object class.  May not be
    * <code>null</code> or empty.
    * @param memberAttr The name of the attribute containing the member list
    * of this object class.  May not be <code>null</code> or empty.
    * @param attrType The type of member list, one of the
    * MEMBER_ATTR_xxx types.
    */
   public PSJndiObjectClass(String objectClass, String memberAttr,
      int attrType)
   {
      if (objectClass == null || objectClass.trim().length() == 0)
         throw new IllegalArgumentException(
            "objectClass may not be null or emtpy");

      if (memberAttr == null || memberAttr.trim().length() == 0)
          throw new IllegalArgumentException(
             "memberAttr may not be null or emtpy");

      if (attrType != MEMBER_ATTR_DYNAMIC && attrType != MEMBER_ATTR_STATIC)
          throw new IllegalArgumentException(
             "invalid attrType");

      m_objectClass = objectClass;
      m_memberAttribute = memberAttr;
      m_attributeType = attrType;
   }

   /**
    * @return The name of this objectClass, never <code>null</code> or empty.
    */
   public String getObjectClassName()
   {
      return m_objectClass;
   }

   /**
    * @return The name of the member attribute, never <code>null</code> or
    * empty.
    */
   public String getMemberAttribute()
   {
      return m_memberAttribute;
   }

   /**
    * @return The type of the member list, one of the MEMBER_ATTR_xxx types.
    */
   public int getAttributeType()
   {
      return m_attributeType;
   }

   /**
    * compares this instance to another object.
    *
    * @param obj the object to compare
    * @return returns <code>true</code> if the object is a
    * PSJndiObjectClass with identical values. Otherwise returns
    * <code>false</code>.
    */
   public boolean equals(Object obj)
   {
      boolean isMatch = true;
      if (!(obj instanceof PSJndiObjectClass))
         isMatch = false;
      else
      {
         PSJndiObjectClass other = (PSJndiObjectClass)obj;
         if (!this.m_objectClass.equals(other.m_objectClass))
            isMatch = false;
         else if (!this.m_memberAttribute.equals(other.m_memberAttribute))
            isMatch = false;
         else if (this.m_attributeType != other.m_attributeType)
            isMatch = false;
      }

      return isMatch;
   }
   
   /**
    * Generates object hash code.
    */
   @Override
   public int hashCode()
   {
      return this.m_objectClass.hashCode()
            + m_memberAttribute.hashCode()
            + m_attributeType;
   }

   /**
    * The name of this objectClass.  Initialized during construction, never
    * <code>null</code>, empty, or modified after that.
    */
   private String m_objectClass = null;

   /**
    * The name of the attribute containing the member list.  Initialized
    * during construction, never <code>null</code>, empty, or modified after
    * that.
    */
   private String m_memberAttribute = null;

   /**
    * The type of memberlist contained in the member attribute.  One of the
    * MEMBER_ATTR_xxx types.  Set during the ctor, never modified after that.
    */
   private int m_attributeType = MEMBER_ATTR_STATIC;

   /**
    * Constant value to represent a member attribute whose value is a static
    * list of member's distinguished names.
    */
   public static final int MEMBER_ATTR_STATIC = 0;

   /**
    * Constant value to represent a member attribute whose value is a dynamic
    * list of search filters.
    */
   public static final int MEMBER_ATTR_DYNAMIC = 1;

   /**
    * Array of String values to represent a member attribute whose value is a
    * dynamic list of search filters.  Each of the <code>MEMBER_ATTR_xxx</code>
    * constants may be used as an index into this array to retrieve its
    * corresponding String representation.
    */
   public static final String[] MEMBER_ATTR_TYPE_ENUM = {"static", "dynamic"};
}
