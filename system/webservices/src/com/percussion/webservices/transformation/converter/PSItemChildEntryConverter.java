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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.webservices.transformation.converter;

import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.cms.objectstore.PSItemChild;
import com.percussion.cms.objectstore.PSItemChildEntry;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.webservices.content.PSChildEntry;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConversionException;

/**
 * Converts objects between the classes
 * {@link com.percussion.cms.objectstore.PSItemChildEntry} and
 * {@link com.percussion.webservices.content.PSChildEntry}
 */
public class PSItemChildEntryConverter extends PSConverter
{
   /**
    * @see PSConverter#PSConverter(BeanUtilsBean)
    */
   public PSItemChildEntryConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);
   }

   /* (non-Javadoc)
    * @see PSConverter#convert(Class, Object)
    */
   @SuppressWarnings("unchecked")
   @Override
   public Object convert(@SuppressWarnings("unused") Class type, Object value)
   {
      if (value == null)
         return null;
    
      try
      {
         if (isClientToServer(value))
         {
            PSChildEntry orig = (PSChildEntry) value;
            
            PSLegacyGuid guid = new PSLegacyGuid(orig.getId());
            PSItemDefinition itemDefinition = 
               PSItemConverterUtils.getItemDefinition(guid.getContentTypeId());
            
            PSCoreItem item = new PSCoreItem(itemDefinition);
            PSItemChild child = item.getChildById(guid.getChildId());
            if (child == null)
               throw new IllegalArgumentException("Invalid child entry id");
            
            PSItemChildEntry dest = child.createChildEntry();
            dest.setAction(orig.getAction());
            dest.setChildRowId(guid.getUUID());
            
            PSItemConverterUtils.toServerFields(dest, guid, orig.getPSField());
            
            return dest;
         }
         else
         {
            PSItemChildEntry orig = (PSItemChildEntry) value;
            
            PSLegacyGuid guid = orig.getGUID();
            if (guid == null)
               throw new IllegalArgumentException(
                  "guid must be set on the child entry");
            PSItemDefinition itemDefinition = 
               PSItemConverterUtils.getItemDefinition(guid.getContentTypeId());
            
            PSChildEntry dest = new PSChildEntry();

            dest.setAction(orig.getAction());
            dest.setId((new PSDesignGuid(guid)).getValue());
            dest.setPSField(PSItemConverterUtils.toClientFields(
               orig.getAllFields(), itemDefinition.getName(), this));
            
            return dest;
         }
      }
      catch (Exception e)
      {
         throw new ConversionException(e.getLocalizedMessage());
      }
   }
}

