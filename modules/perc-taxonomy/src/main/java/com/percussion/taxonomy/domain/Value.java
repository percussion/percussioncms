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

import java.sql.Timestamp;


/**
 * Value model for taxonomy Value table
 *  
 */
public class Value{

   private int id;
   private String name;
   private Node node;
   private Attribute attribute;
   private Language lang;
   private String created_by_id;
   private Timestamp created_at;
   private int percussion_item_id;

   /**
    * Returns unique id of Value
    * @return id - unique int value as id
    */
   public int getId(){
     return id;
   }

   /**
    * Set unique id of Value
    * @param id - unique int value as id
    */
   public void setId(int id){
     this.id = id;
   }

   /**
    * Returns name of Value
    * @return name - String name value
    */
   public String getName(){
     return name;
   }

   /**
    * Set name of Value
    * @param name - String name of Value
    */
   public void setName(String name){
     this.name = name;
   }

   /**
    * Returns node as Node object 
    * @return
    */
   public Node getNode(){
     return node;
   }

   /**
    * Set node as Node Object
    * @param node
    */
   public void setNode(Node node){
     this.node = node;
   }

   /**
    * Returns attribute of the node
    * @return attribute - attribute of a node
    */
   public Attribute getAttribute(){
     return attribute;
   }

   /**
    * Set attribute of a node
    * @param attribute - 
    */
   public void setAttribute(Attribute attribute){
     this.attribute = attribute;
   }

   /**
    * Returns Language object
    * @return lang - Language object
    */
   public Language getLang(){
     return lang;
   }

   /**
    * Set Language object
    * @param lang 
    */
   public void setLang(Language lang){
     this.lang = lang;
   }

   /**
    * Returns created by id
    * @return created_by_id 
    */
   public String getCreated_by_id(){
     return created_by_id;
   }

   /**
    * Set created by id
    * @param created_by_id - String creted by id
    */
   public void setCreated_by_id(String created_by_id){
     this.created_by_id = created_by_id;
   }

   /**
    * Returns created date and time
    * @return created_at - created date and time 
    */
   public Timestamp getCreated_at(){
     return created_at;
   }

   
   /**
    * Set created date and time
    * @param created_at - created data and time
    */
   public void setCreated_at(Timestamp created_at){
     this.created_at = created_at;
   }

   /**
    * Returns percussion item id
    * @return - int percussion item id
    */
   public int getPercussion_item_id(){
     return percussion_item_id;
   }

   /**
    * Set percussion item id
    * @param percussion_item_id - int percussion item id
    */
   public void setPercussion_item_id(int percussion_item_id){
     this.percussion_item_id = percussion_item_id;
   }

}
