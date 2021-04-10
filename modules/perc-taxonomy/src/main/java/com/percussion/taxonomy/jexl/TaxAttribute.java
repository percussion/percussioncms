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
