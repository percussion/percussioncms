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
package com.percussion.webservices.transformation.converter;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.webservices.content.PSItem;
import com.percussion.webservices.content.PSItemFolders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.lang.StringUtils;

/**
 * Converts objects between the classes
 * {@link com.percussion.cms.objectstore.server.PSServerItem} and
 * {@link com.percussion.webservices.content.PSItem}
 * 
 * If the item contains related item, then the related item does not contain
 * its related items or binary data. See {@link PSRelatedItemConverter}
 */
public class PSItemConverter extends PSConverter
{
   /**
    * @see PSConverter#PSConverter(BeanUtilsBean)
    */
   public PSItemConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);
   }

   /* (non-Javadoc)
    * @see PSConverter#convert(Class, Object)
    */
   @Override
   public Object convert(@SuppressWarnings("unused") Class type, Object value)
   {
      if (value == null)
         return null;
      
      try
      {
         if (isClientToServer(value))
         {
            PSItem orig = (PSItem) value;
            
            PSLegacyGuid guid = new PSLegacyGuid(orig.getId());
            PSItemDefinition itemDefinition = 
               PSItemConverterUtils.getItemDefinition(orig.getContentType());
            
            PSCoreItem dest = new PSCoreItem(itemDefinition);
            dest.setContentId(guid.getContentId());
            if (guid.getRevision() != -1)
               dest.setRevision(guid.getRevision());
            if (!StringUtils.isBlank(orig.getSystemLocale()))
               dest.setSystemLocale(new Locale(orig.getSystemLocale()));
            if (!StringUtils.isBlank(orig.getDataLocale()))
               dest.setDataLocale(new Locale(orig.getDataLocale()));
            dest.setCheckedOutByName(orig.getCheckedOutBy());
            
            // convert fields
            PSItemConverterUtils.toServerFields(dest, guid, orig.getFields());
            
            // convert children
            PSItemConverterUtils.toServerChildren(dest, orig.getChildren(), 
               this);
            
            // convert related content
            PSItemConverterUtils.toServerRelatedContent(dest, orig.getSlots(), 
               this);
            
            // convert folders
            PSItemFolders[] folders = orig.getFolders();
            if (folders != null)
            {
               List<PSItemFolders> folderList = new ArrayList<>();
               folderList = Arrays.asList(folders);
               dest.setFolderPaths(PSItemConverterUtils.toServerFolders(
                     folderList));
            }

            return dest;
         }
         else
         {
            PSCoreItem orig = (PSCoreItem) value;
            
            PSLegacyGuid guid = new PSLegacyGuid(orig.getContentId(), 
               orig.getRevision());
            
            String contentType = orig.getItemDefinition().getName();
            
            PSItem dest = new PSItem();
            dest.setId(guid.longValue());
            dest.setContentType(contentType);
            if (orig.getSystemLocale() != null)
               dest.setSystemLocale(orig.getSystemLocale().getLanguage());
            if (orig.getDataLocale() != null)
               dest.setDataLocale(orig.getDataLocale().getLanguage());
            dest.setCheckedOutBy(orig.getCheckedOutByName());
            
            // convert fields
            dest.setFields(PSItemConverterUtils.toClientFields(
               orig.getAllFields(), contentType, this));

            // convert children
            dest.setChildren(PSItemConverterUtils.toClientChildren(
               orig.getAllChildren(), this));
            
            // convert related content
            dest.setSlots(PSItemConverterUtils.toClientRelatedContent(
               orig.getAllRelatedItems(), this));
            
            // convert folders
            dest.setFolders(PSItemConverterUtils.toClientFolders(
               orig.getFolderPaths()));
            
            return dest;
         }
      }
      catch (PSCmsException | PSAssemblyException e)
      {
         throw new ConversionException(e.getLocalizedMessage());
      }
   }
}
