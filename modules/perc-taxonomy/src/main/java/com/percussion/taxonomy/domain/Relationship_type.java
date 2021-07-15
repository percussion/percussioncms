/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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

package com.percussion.taxonomy.domain;


/**
 * Taxonomy Relationship_type class to represent relationship information between taxons.
 *  
 */
public class Relationship_type{

   /**
    * Constant to identify related node
    */
   public static final int RELATED = 1;
   /**
    * Constant to identify similar node
    */
   public static final int SIMILAR = 2;
   
   private int id;
   private String relationship_type;

   /**
    * Returns unique relationship type id
    * @return id - int unique id of relationship type
    */
   public int getId(){
     return id;
   }

   /**
    * Set unique relationship type id
    * @param id - unique int value for relationship id  
    */
   public void setId(int id){
     this.id = id;
   }

   /**
    * Returns relationship type
    * @return relationship_type - String relationship type
    */
   public String getRelationship_type(){
     return relationship_type;
   }

   /**
    * Set Relationship type
    * @param relationship_type - String value of relationship type
    */
   public void setRelationship_type(String relationship_type){
     this.relationship_type = relationship_type;
   }

}
