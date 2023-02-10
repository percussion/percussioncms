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
