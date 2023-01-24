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
package com.percussion.taxonomy.service;

import com.percussion.taxonomy.domain.Attribute;
import com.percussion.taxonomy.domain.Attribute_lang;
import com.percussion.taxonomy.domain.Node;
import com.percussion.taxonomy.domain.Value;
import com.percussion.taxonomy.repository.AttributeDAO;
import com.percussion.taxonomy.repository.AttributeServiceInf;
import com.percussion.taxonomy.repository.Attribute_langServiceInf;
import com.percussion.taxonomy.repository.NodeServiceInf;
import com.percussion.taxonomy.repository.ValueServiceInf;

import java.util.Collection;
public class AttributeService implements AttributeServiceInf
{
   public AttributeService(AttributeDAO attributeDAO, Attribute_langServiceInf attributeLangService, 
         NodeServiceInf nodeService, ValueServiceInf valueService)
   {
      this.attributeDAO = attributeDAO;
      this.attributeLangService = attributeLangService;
      this.nodeService = nodeService;
      this.valueService = valueService;
   }

   public Collection getAllAttributes(int taxonomy_id, int language_id)
   {
      return attributeDAO.getAllAttributes(taxonomy_id, language_id);
   }

   public Collection getAttribute(int id)
   {
      return attributeDAO.getAttribute(id);
   }

   public void removeAttribute(Attribute attribute)
   {
      removeAttributeFromNodes(attribute);

      removeAttributeLanguages(attribute);

      attributeDAO.removeAttribute(attribute);
   }

   /**
    * Remove the associated Attribute languages for the given attribute.
    * 
    * @param attribute the attribute in question, not <code>null</code>.
    */
   private void removeAttributeLanguages(Attribute attribute)
   {
      if(attribute.getAttribute_langs() == null)
      {
         return;
      }
      
      for (Attribute_lang attrLang : attribute.getAttribute_langs())
      {
         attributeLangService.removeAttribute_lang(attrLang);
      }
   }

   /**
    * Removes the specified attribute from the nodes that have values for that
    * attribute.
    * 
    * @param attribute the attribute in question, not <code>null</code>.
    */
   private void removeAttributeFromNodes(Attribute attribute)
   {
      Collection<Node> nodes = nodeService.findNodesByAttribute(attribute);

      for (Node node : nodes)
      {
         // removes the values that are associated with the attribute
         for (Value value : node.getValues())
         {
            if (value.getAttribute().getId() == attribute.getId())
            {
               valueService.removeValue(value);
            }
         }
      }
   }

   public void saveAttribute(Attribute attribute)
   {
      attributeDAO.saveAttribute(attribute);
   }

   /**
    * Return all Attribute names and IDs
    */
   public Collection getAttributeNames(int taxonomy_id, int language_id)
   {
      return attributeDAO.getAttributeNames(taxonomy_id, language_id);
   }

   private AttributeDAO attributeDAO;

   private Attribute_langServiceInf attributeLangService;

   private NodeServiceInf nodeService;

   private ValueServiceInf valueService;


}
