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

import com.percussion.cms.objectstore.PSCmsObject;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.webservices.content.PSContentType;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.webservices.common.ObjectType;

import java.io.StringReader;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConversionException;
import org.w3c.dom.Document;

/**
 * Converts between {@link com.percussion.cms.objectstore.PSItemDefinition} and 
 * {@link PSContentType}
 */
public class PSContentTypeConverter extends PSConverter
{

   /**
    * See {@link PSConverter#PSConverter(BeanUtilsBean)}
    * 
    * @param beanUtils
    */
   public PSContentTypeConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);
   }

   @Override
   @SuppressWarnings("unused")
   public Object convert(Class type, Object value)
   {
      Object result;
      if (isClientToServer(value))
      {
         try
         {
            PSContentType ct = (PSContentType) value;
            String strCE = ct.getContentEditor();
            StringReader reader = new StringReader(strCE);
            Document doc = PSXmlDocumentBuilder.createXmlDocument(reader, 
               false);
            PSContentEditor ce = new PSContentEditor(doc.getDocumentElement(), 
               null, null, false);
            // get object type
            int objectType;
            if (ct.getObjectType().getValue().equals(ObjectType._folder))
               objectType = PSCmsObject.TYPE_FOLDER;
            else
               objectType = PSCmsObject.TYPE_ITEM;
            
            PSGuid guid = new PSGuid(PSTypeEnum.NODEDEF, ct.getId());
            com.percussion.cms.objectstore.PSContentType typeDef = 
               new com.percussion.cms.objectstore.PSContentType(
                     (int)guid.longValue(), ct.getName(), ct.getLabel(), 
                     ct.getDescription(), 
               ct.getRequestUrl(), ct.isHideFromMenu(), objectType);
            String appName = 
               com.percussion.cms.objectstore.PSContentType.getAppName(
                  ct.getRequestUrl());
            result = createDefinition(appName, typeDef, ce);
         }
         catch (Exception e)
         {
            throw new ConversionException(e);
         }
      }
      else
      {
         PSItemDefinition itemDef = (PSItemDefinition) value;
         
         String ceStr = PSXmlDocumentBuilder.toString(
            itemDef.getContentEditor().toXml(
            PSXmlDocumentBuilder.createXmlDocument())).trim();
         
         // get the object type
         ObjectType objectType;
         if (itemDef.getObjectType() == PSCmsObject.TYPE_FOLDER)
            objectType = ObjectType.folder;
         else
            objectType = ObjectType.item;
         
         PSDesignGuid guid = new PSDesignGuid(PSTypeEnum.NODEDEF, 
            itemDef.getTypeId());
         PSContentType contentType = new PSContentType(guid.getValue(), 
            itemDef.getDescription(), ceStr, itemDef.getName(), 
            itemDef.getLabel(), objectType, 
            itemDef.getEditorUrl(), itemDef.isHidden());
         
         result = contentType;
      }
      
      return result;
   }
   
   /**
    * Returns the item definition, this method will be overridden in
    * the workbench so it can return a <code>PSUiItemDefinition</code>. 
    * @param appName cannot be <code>null<code> or empty.
    * @param typeDef cannot be <code>null</code>.
    * @param ce cannot be <code>null</code>.
    * @return the item def object, never <code>null</code>
    */
   protected Object createDefinition(String appName,
      com.percussion.cms.objectstore.PSContentType typeDef, PSContentEditor ce)
   {
      return new PSItemDefinition(appName, typeDef, ce);
   }
}

