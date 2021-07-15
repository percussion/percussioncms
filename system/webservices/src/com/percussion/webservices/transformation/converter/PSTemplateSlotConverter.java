/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
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
package com.percussion.webservices.transformation.converter;

import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.data.PSTemplateSlot;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.types.PSPair;
import com.percussion.webservices.assembly.data.PSTemplateSlotAllowedContentContent;
import com.percussion.webservices.assembly.data.PSTemplateSlotArgumentsArgument;
import com.percussion.webservices.assembly.data.PSTemplateSlotType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.Converter;

/**
 * Converts objects between the classes 
 * <code>com.percussion.services.assembly.data.PSTemplateSlot</code> and 
 * <code>com.percussion.webservices.assembly.data.PSTemplateSlot</code>.
 */
public class PSTemplateSlotConverter extends PSConverter
{
   /* (non-Javadoc)
    * @see PSConverter#PSConvert(BeanUtilsUtil)
    */
   public PSTemplateSlotConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);

      m_specialProperties.add("id");
      m_specialProperties.add("type");
      m_specialProperties.add("slottype");
      m_specialProperties.add("finder");
      m_specialProperties.add("systemslot");
      m_specialProperties.add("finderArguments");
      m_specialProperties.add("arguments");
      m_specialProperties.add("slotAssociations");
      m_specialProperties.add("allowedContent");
   }

   /* (non-Javadoc)
    * @see PSConverter#convert(Class, Object)
    */
   @SuppressWarnings("unchecked")
   @Override
   public Object convert(Class type, Object value)
   {
      Object result = super.convert(type, value);
      
      if (isClientToServer(value))
      {
         com.percussion.webservices.assembly.data.PSTemplateSlot orig = 
            (com.percussion.webservices.assembly.data.PSTemplateSlot) value;
         
         PSTemplateSlot dest = (PSTemplateSlot) result;
         
         // convert id
         Long id = orig.getId();
         if (id != null)
            dest.setGUID(new PSDesignGuid(id));

         // convert type
         Converter converter = getConverter(IPSTemplateSlot.SlotType.class);
         dest.setSlottype((IPSTemplateSlot.SlotType) converter.convert(
            IPSTemplateSlot.SlotType.class, orig.getType()));
         
         // convert finder
         dest.setFinderName(orig.getFinder());
         
         // convert system slot
         dest.setSystemSlot(orig.isIsSystemSlot());
         
         // convert finder arguments
         if (orig.getArguments() != null)
         {
            Map<String, String> arguments = new HashMap<String, String>();
            for (PSTemplateSlotArgumentsArgument argument : orig.getArguments())
               arguments.put(argument.getName(), argument.get_value());
            dest.setFinderArguments(arguments);
         }
         
         // convert allowed content
         PSTemplateSlotAllowedContentContent[] allowedContents = 
            orig.getAllowedContent();
         if (allowedContents != null)
         {
            Collection<PSPair<IPSGuid, IPSGuid>> slotAssociations = 
               new ArrayList<PSPair<IPSGuid, IPSGuid>>();
            for (PSTemplateSlotAllowedContentContent allowedContent : 
               allowedContents)
            {
               PSPair<IPSGuid, IPSGuid> pair = new PSPair<IPSGuid, IPSGuid>(
                     new PSDesignGuid(allowedContent.getContentTypeId()),
                     new PSDesignGuid(allowedContent.getTemplateId()));
               slotAssociations.add(pair);
            }
            dest.setSlotAssociations(slotAssociations);
         }
      }
      else
      {
         PSTemplateSlot orig = (PSTemplateSlot) value;
         
         com.percussion.webservices.assembly.data.PSTemplateSlot dest = 
            (com.percussion.webservices.assembly.data.PSTemplateSlot) result;
         
         // convert id
         IPSGuid guid = orig.getGUID();
         if (guid != null)
            dest.setId(new PSDesignGuid(guid).getValue());
         
         // convert type
         Converter converter = getConverter(PSTemplateSlotType.class);
         dest.setType((PSTemplateSlotType) converter.convert(
            PSTemplateSlotType.class, orig.getSlottypeEnum()));
         
         // convert finder
         dest.setFinder(orig.getFinderName());
         
         // convert system slot
         dest.setIsSystemSlot(orig.isSystemSlot());
         
         // convert finder arguments
         Map<String, String> finderArguments = orig.getFinderArguments();
         PSTemplateSlotArgumentsArgument[] arguments = 
            new PSTemplateSlotArgumentsArgument[finderArguments.size()];
         int index = 0;
         for (String key : finderArguments.keySet())
         {
            PSTemplateSlotArgumentsArgument argument = 
               new PSTemplateSlotArgumentsArgument();
            argument.setName(key);
            argument.set_value(finderArguments.get(key));
            
            arguments[index++] = argument;
         }
         dest.setArguments(arguments);
         
         // convert allowed content
         Collection slotAssociations = orig.getSlotAssociations();
         PSTemplateSlotAllowedContentContent[] allowedContents = 
            new PSTemplateSlotAllowedContentContent[slotAssociations.size()];
         index = 0;
         for (Object slotAssociation : slotAssociations)
         {
            PSPair<IPSGuid, IPSGuid> pair = 
               (PSPair<IPSGuid, IPSGuid>) slotAssociation;
            PSTemplateSlotAllowedContentContent allowedContent = 
               new PSTemplateSlotAllowedContentContent(
                  new PSDesignGuid(pair.getFirst()).getValue(), 
                  new PSDesignGuid(pair.getSecond()).getValue());
            
            allowedContents[index++] = allowedContent;
         }
         dest.setAllowedContent(allowedContents);
      }
      
      return result;
   }
}

