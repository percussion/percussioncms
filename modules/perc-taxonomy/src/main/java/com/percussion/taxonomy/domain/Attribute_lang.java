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
 * Taxonomy Attribute_lang class to represent its attribute language
 *  
 */
public class Attribute_lang{

   private int id;
   private String name;
   private Attribute attribute;
   private Language language;

   /**
    * Returns unique id of the 
    *  @returns id - unique id of attribute language
    */
   public int getId(){
     return id;
   }

   /**
    * Set the id to uniquely identify a language
    * @param id
    */
   public void setId(int id){
     this.id = id;
   }

   /**
    * @return the language name
    */
   public String getName(){
     return name;
   }
   
   /**
    * Set a name of the language
    * @param name
    */
   public void setName(String name){
     this.name = name;
   }

   /**
    * Returns attribute
    * @return attribute 
    */
   public Attribute getAttribute(){
     return attribute;
   }

   /**
    * Set attribute value
    * @param attribute 
    */
   public void setAttribute(Attribute attribute){
     this.attribute = attribute;
   }

   /**
    * Returns language of an attribute
    * @return language
    */
   public Language getLanguage(){
     return language;
   }

   /**
    * Set attribute language
    * @param language
    */
   public void setLanguage(Language language){
     this.language = language;
   }
   
   /**
    * Returns a string representation of the object.
    */
/*   public String toString(){
       return "Attribute Lang ----------------------\n"+
               "ID: "+id+"\n"+
               "Name: "+name+"\n"+
               "Attribute: "+attribute+"\n"+
               "Language: "+language;
   }*/
}
