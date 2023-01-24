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
