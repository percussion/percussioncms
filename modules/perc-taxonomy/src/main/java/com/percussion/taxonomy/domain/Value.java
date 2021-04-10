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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
