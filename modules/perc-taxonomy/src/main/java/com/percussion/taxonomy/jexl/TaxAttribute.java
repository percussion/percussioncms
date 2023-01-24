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
package com.percussion.taxonomy.jexl;

import com.percussion.taxonomy.domain.Attribute;
import com.percussion.taxonomy.domain.Attribute_lang;

import java.util.ArrayList;
import java.util.List;

/**
 * Read only class for use in Jexl that represents a Taxonomy Attribute.
 * 
 * @author stephenbolton
 *
 */
public class TaxAttribute
{
   private int id;
   
   private String name;

   private boolean isMultiple;

   private boolean isRequired;

   private List<String> langs;

   /**
    * Initialize the attribute from the Hibernate object
    * @param attLang
    */
   public TaxAttribute(Attribute_lang attLang) 
   {
      Attribute att =attLang.getAttribute();
      this.id=att.getId();
      this.name=attLang.getName();
      this.isMultiple=att.getIs_multiple();
      this.isRequired=att.getIs_required();
      this.langs=new ArrayList<String>();
      for (Attribute_lang lang : att.getAttribute_langs()) {
         this.langs.add(lang.getLanguage().getName());
      }
   }
   
   /**
    * The attribute id
    * @return the id
    */
   public int getId()
   {
      return id;
   }
   
   /**
    * The attribute name
    * @return the name
    */
   public String getName()
   {
      return name;
   }

   /**
    * Is this a multi valued attribute
    * @return is this a multi valued attribute
    */
   public boolean isMultiple()
   {
      return isMultiple;
   }

   /**
    * Is this attribute defined as required.
    * @return is this field required.
    */
   public boolean isRequired()
   {
      return isRequired;
   }

   /**
    * Return a list of languages available for this Attribute.  
    * @return list of language codes
    */
   public List<String> getLangs()
   {
      return langs;
   }

}
