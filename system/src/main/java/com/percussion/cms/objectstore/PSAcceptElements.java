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
