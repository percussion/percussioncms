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
package com.percussion.fastforward.managednav.sql;

/**
 * Relationship data object. Used for loading data about slot relationships
 * directly from the backend SQL database.
 * 
 * @author DavidBenua
 *  
 */
public class PSNavSqlRelation
{
   /**
    * Construct a relationship object from a content id and variant.
    * 
    * @param contentid
    * @param variant
    *  
    */
   public PSNavSqlRelation(int contentid, int variant)
   {
      m_contentid = contentid;
      m_variant = variant;
   }

   /**
    * Gets the content id.
    * 
    * @return Returns the contentid.
    */
   public int getContentid()
   {
      return m_contentid;
   }

   /**
    * Gets the variant id.
    * 
    * @return Returns the variant.
    */
   public int getVariant()
   {
      return m_variant;
   }

   /**
    * Content id of dependent item.
    */
   private int m_contentid;

   /**
    * Variant id of dependent item.
    */
   private int m_variant;

}
