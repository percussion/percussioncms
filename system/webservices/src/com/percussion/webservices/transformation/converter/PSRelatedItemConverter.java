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

import com.percussion.cms.objectstore.PSAaRelationship;
import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.cms.objectstore.PSItemChild;
import com.percussion.cms.objectstore.PSItemChildEntry;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.PSItemRelatedItem;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.webservices.content.PSItem;
import com.percussion.webservices.content.PSRelatedItem;
import com.percussion.webservices.content.PSRelatedItemAction;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.Iterator;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.Converter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;

/**
 * Converts objects between the classes
 * {@link com.percussion.cms.objectstore.PSItemRelatedItem} and
 * {@link com.percussion.webservices.content.PSRelatedItem}
 * 
 * When converting from server to client, the converted item does not contain
 * its related item or binary data (if there is any).
 */
public class PSRelatedItemConverter extends PSConverter
{
   /**
    * @see PSConverter#PSConverter(BeanUtilsBean)
    */
   public PSRelatedItemConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);
   }

   /* (non-Javadoc)
    * @see PSConverter#convert(Class, Object)
    */
   @SuppressWarnings("unchecked")
   @Override
   public Object convert(@SuppressWarnings("unused")
   Class type, Object value)
   {
      if (value == null)
         return null;
      
      try
      {
         if (isClientToServer(value))
         {
            PSRelatedItem orig = (PSRelatedItem) value;
            
            PSItemRelatedItem dest = new PSItemRelatedItem();
            PSAaRelationship relationship = (PSAaRelationship) getConverter(
               PSAaRelationship.class).convert(PSAaRelationship.class, 
                  orig.getPSAaRelationship());
            dest.setAction(orig.getAction().getValue());
            dest.setDependentId(relationship.getDependent().getId());
            dest.setRelatedType(relationship.getConfig().getName());
            dest.setRelationshipId(relationship.getId());
            dest.setRelationship(relationship);
            
            PSItem relatedItem = orig.getPSItem();
            Converter itemConverter = getConverter(relatedItem.getClass());
            PSCoreItem destRelatedItem = (PSCoreItem) itemConverter.convert(
               PSCoreItem.class, relatedItem);
            dest.setRelatedItemData(destRelatedItem.toXml(
               PSXmlDocumentBuilder.createXmlDocument()));

            return dest;
         }
         else // from Server to Client
         {
            // the converted (client) item does not contain its related items
            // or binary data.
            PSItemRelatedItem orig = (PSItemRelatedItem) value;
            
            PSRelatedItem dest = new PSRelatedItem();
            dest.setAction((PSRelatedItemAction) getConverter(
               PSRelatedItemAction.class).convert(PSRelatedItemAction.class, 
                  orig.getAction()));
            dest.setPSAaRelationship(
               (com.percussion.webservices.content.PSAaRelationship) getConverter(
                  com.percussion.webservices.content.PSAaRelationship.class).convert(
                     com.percussion.webservices.content.PSAaRelationship.class, 
                     orig.getRelationship()));
            
            PSCoreItem relatedItem = getCoreItemFromItemData(orig);
            
            Converter itemConverter = getConverter(PSCoreItem.class);
            
            // set the entry GUID needed for the converter
            Iterator<PSItemChild> children = relatedItem.getAllChildren();
            while (children.hasNext())
            {
               PSItemChild child = children.next();
               Iterator<PSItemChildEntry> entries = child.getAllEntries();
               while (entries.hasNext())
               {
                  PSItemChildEntry entry = entries.next();
                  entry.setGUID(new PSLegacyGuid(relatedItem.getContentTypeId(), 
                     child.getChildId(), entry.getChildRowId()));
               }
            }
            
            dest.setPSItem((PSItem) itemConverter.convert(PSItem.class, 
               relatedItem));
            
            return dest;
         }
      }
      catch (Exception e)
      {
         ms_log.error("Unknown exception", e);
         throw new ConversionException(e.getLocalizedMessage());
      }
   }

   /**
    * Gets the {@link PSCoreItem} from the Item Data in XML
    * 
    * @param itemData the Item Data in XML format, assumed not <code>null</code>.
    * 
    * @return the created Item, never <code>null</code>.
    * 
    * @throws Exception if any error occurs.
    */
   private PSCoreItem getCoreItemFromItemData(PSItemRelatedItem orig) 
      throws Exception
   {
      Element itemData = orig.getRelatedItemData();
      if (itemData == null)
         throw new IllegalStateException(
               "The related item data must not be null");
      
      String contentTypeId = itemData.getAttribute(
            PSCoreItem.ATTR_CONTENTTYPE);
      PSItemDefinition def = PSItemConverterUtils.getItemDefinition(
            Long.valueOf(contentTypeId));
      PSCoreItem relatedItem = new PSCoreItem(def);
      relatedItem.loadXmlData(itemData);
      
      return relatedItem;
   }
   
   /**
    * Logger for the assembler.
    */
   public static final Logger ms_log = LogManager.getLogger(PSRelatedItemConverter.class);

}

