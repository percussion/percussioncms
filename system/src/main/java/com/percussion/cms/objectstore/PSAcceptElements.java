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
package com.percussion.cms.objectstore;

/**
 * This class is used to define rules for elements to be included in the
 * Element returned in {@link PSItemComponent#toXml(Document,PSAcceptElements)} 
 * calls.  This may be expanded at a later date to provide more flexibility of 
 * including and excluding elements based on object values in the 
 * sys_StandardItem.xsd.
 */
public class PSAcceptElements
{
   /**
    * Creates an instance with the with the boolean rules specified
    * by the parameters.
    *
    * @param includeFields
    * @param includeChilds
    * @param includeRelated
    * @param includeBinary
    */
   public PSAcceptElements(boolean includeFields, boolean includeChildren,
       boolean includeRelated, boolean includeBinary)
   {
      m_includeFields = includeFields;
      m_includeChildren = includeChildren;
      m_includeRelated = includeRelated;
      m_includeBinary  = includeBinary;

   }

   /**
    * Specifies the inclusion of binary values in
    * {@link PSFieldItem#toXml(Document)} calls.  Include binary field values?
    *
    * @return <code>true</code> is they are to be included, otherwise <code>
    * false</code>.
    */
   public boolean includeBinary()
   {
      return m_includeBinary;
   }

   /**
    * Use to indicate if fields should be included in a toXml call, default
    * is <code>true</code>, may change.
    */
   private boolean m_includeFields = true;

   /**
    * Use to indicate if child should be included in a toXml call, default
    * is <code>true</code>, may change.
    */
   private boolean m_includeChildren = true;

   /**
    * Use to indicate if related items should be included in a toXml call,
    * default is <code>true</code>, may change.
    */
   private boolean m_includeRelated = true;

   /**
    * Use to indicate if binary fields should be included in a toXml call,
    * default is <code>false</code>, may change.
    */
   private boolean m_includeBinary = true;
}
