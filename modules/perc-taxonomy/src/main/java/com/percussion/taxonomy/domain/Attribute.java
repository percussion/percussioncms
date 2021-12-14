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

import java.sql.Timestamp;
import java.util.Collection;

/**
 *Taxonomy Attribute class to represent its taxon's attributes 
 *
 */

public class Attribute{

   private int id;
   private Taxonomy taxonomy;
   private boolean is_multiple;
   private int is_node_name;
   private boolean is_required;
   private boolean is_percussion_item;
   private String created_by_id;
   private Timestamp created_at;
   private String modified_by_id;
   private Timestamp modified_at;
   private Collection<Attribute_lang> attribute_langs;

   /**
    * Returns Collection of attribute languages
    * @return attribute_langs 
    */
   public Collection<Attribute_lang> getAttribute_langs(){
          return attribute_langs;
   }
       
   /**
    * Set Attribute languages
    * @param attribute_langs
    */
   public void setAttribute_langs(Collection<Attribute_lang> attribute_langs){
          this.attribute_langs = attribute_langs;
   }
       
   /**
    * Add Attribute language to a collection of attribute languages
    * @param attribute_lang
    */
 
   public void addAttribute_lang(Attribute_lang attribute_lang){
          attribute_lang.setAttribute(this);
          attribute_langs.add(attribute_lang);
   }
       
   /**
    * Returns unique attribute id
    * @return id - int value for attribute id
    */
   public int getId(){
     return id;
   }

   /**
    * Set attribute unique id
    * @param id - unique int value as attribute id
    */
   public void setId(int id){
     this.id = id;
   }
   
   /**
    * Returns whether an attibute is multiple type or not
    * @return - boolean value true or false for attibute type multiple or not
    */
   public boolean getIs_multiple(){
      return is_multiple;
   }
   
   
   /**
    * Set boolean true or false to determine whether its a multiple attribute or not
    * @param is_multiple - boolean value true or false for multiple attribute or not
    */
   public void setIs_multiple(boolean is_multiple){
      this.is_multiple = is_multiple;
   }

   /**
    * Returns taxonomy object
    * @return taxonomy - an object of Taxonomy
    */
   public Taxonomy getTaxonomy(){
     return taxonomy;
   }

   /**
    * Set taxonomy object
    * @param taxonomy
    */
   public void setTaxonomy(Taxonomy taxonomy){
     this.taxonomy = taxonomy;
   }

   /**
    * Returns int value to determine whether its a node name or not
    * @return is_node_name - int value to determine whether its a node name or not
    */
   public int getIs_node_name(){
     return is_node_name;
   }

   /**
    * Set whether its a node name or not
    * @param is_node_name - int value to determine whether its a node name or not
    */
   public void setIs_node_name(int is_node_name){
     this.is_node_name = is_node_name;
   }

   /**
    * Returns whether a attribute is required or not 
    * @return is_required - true or false value to determine whether its a required field or not
    */
   public boolean getIs_required(){
     return is_required;
   }

   /**
    * Set whether its a required attribute 
    * @param is_required - boolean true or false determines whether its a required field or not
    */
   public void setIs_required(boolean is_required){
     this.is_required = is_required;
   }

   /**
    * Returns true or false to identify whether the item is a percussion item or not
    * @return is_percussion_item - boolean value true or false
    */
   public boolean getIs_percussion_item(){
     return is_percussion_item;
   }

   /**
    * Set boolean true or false for is_perucssion_item
    * @param is_percussion_item boolean true or false for is_percussion_item
    */
   public void setIs_percussion_item(boolean is_percussion_item){
     this.is_percussion_item = is_percussion_item;
   }

   /**
    * Returns created by id
    * @return
    */
   public String getCreated_by_id(){
     return created_by_id;
   }

   /**
    * Set create by id
    * @param created_by_id
    */
   public void setCreated_by_id(String created_by_id){
     this.created_by_id = created_by_id;
   }

   /**
    * Returns created_at date
    * @return creaed_at - attribute create date
    */
   public Timestamp getCreated_at(){
     return created_at;
   }

   /**
    * Set created_at date
    * @param created_at - a date value to set as attribute create date
    */
   public void setCreated_at(Timestamp created_at){
     this.created_at = created_at;
   }

   /**
    * Returns modified by id
    * @return modified_by_id
    */
   public String getModified_by_id(){
     return modified_by_id;
   }

   /**
    * Set modified by id
    * @param modified_by_id
    */
   public void setModified_by_id(String modified_by_id){
     this.modified_by_id = modified_by_id;
   }

   /**
    * Returns modified date
    * @return modified_at - attribute modified date
    */
   public Timestamp getModified_at(){
     return modified_at;
   }

   /**
    * Set modified date
    * @param modified_at - a date value to set as attribute modified date
    */
   public void setModified_at(Timestamp modified_at){
     this.modified_at = modified_at;
   }
   
   /**
    * Returns a String representation of the object.
    */
  /* public String toString(){
       return  "Attribute ----------------------\n"+
               "ID: "+id+"\n"+
               "Taxonomy ID: "+taxonomy.getId()+"\n"+
               "Is Multiple? "+is_multiple+"\n"+
               "Is Node Name? "+is_node_name+"\n"+
               "Is Required? "+is_required+"\n"+
               "Is Percussion Item? "+is_percussion_item+"\n"+
               "Created By: "+created_by_id+"\n"+
               "Created At: "+created_at+"\n"+
               "Modified By ID: "+modified_by_id+"\n"+
               "Modified At: "+modified_at+"\n"+
               "Attribute Langs: "+attribute_langs;
   }*/
}
