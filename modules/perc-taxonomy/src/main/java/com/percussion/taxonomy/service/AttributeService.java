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
