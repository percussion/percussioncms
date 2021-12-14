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
