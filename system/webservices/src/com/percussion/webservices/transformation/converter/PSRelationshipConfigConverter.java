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

import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.error.PSExceptionUtils;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.webservices.system.RelationshipConfigSummary;
import com.percussion.webservices.system.RelationshipConfigSummaryType;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConversionException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;

/**
 * Convert between 
 * {@link com.percussion.design.objectstore.PSRelationshipConfig} and
 * {@link com.percussion.webservices.system.PSRelationshipConfig}
 * <p>
 * {@link com.percussion.design.objectstore.PSRelationshipConfig} and
 * {@link com.percussion.webservices.system.RelationshipConfigSummary}
 */
public class PSRelationshipConfigConverter extends PSTransitionBaseConverter
{

   /**
    * See {@link PSConverter#PSConverter(BeanUtilsBean)}
    *
    * @param beanUtils
    */
   public PSRelationshipConfigConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);
   }

   @Override
   public Object convert(Class type, Object value)
   {
      if (value == null)
         return null;

      if (isClientToServer(value))
      {
         // client to server conversion only support
         // com.percussion.webservices.system.PSRelationshipConfig, but 
         // not its derived parent class, RelationshipConfigSummary
         if (!(value instanceof com.percussion.webservices.system.PSRelationshipConfig))
            throw new ConversionException(
                  "Conversion not supported from client to server for "
                        + value.getClass().getName());
         
         com.percussion.webservices.system.PSRelationshipConfig source
          = (com.percussion.webservices.system.PSRelationshipConfig) value;
         String xmlString = source.getRelationshipConfig();
         if (xmlString == null || xmlString.trim().length() == 0)
            throw new IllegalArgumentException("RelationshipConfig property must not be null or empty.");
         PSRelationshipConfig target = getRelationshipConfig(xmlString);
         
         target.setName(source.getName());
         target.setLabel(source.getLabel());
         target.setDescription(source.getDescription());
         target.setCategory(source.getCategory());
         if (source.getType().getValue().equals(
               RelationshipConfigSummaryType._system))
            target.setType(PSRelationshipConfig.RS_TYPE_SYSTEM);
         else
            target.setType(PSRelationshipConfig.RS_TYPE_USER);
         
         return target;
      }
      else  // convert from objectstore to one of the client objects
      {
         PSRelationshipConfig source = (PSRelationshipConfig) value;
         if (type == RelationshipConfigSummary.class)
         {
            RelationshipConfigSummary tgt = new RelationshipConfigSummary();
            setConfigSummaryProperties(source, tgt);
            return tgt;
         }
         else
         {
            com.percussion.webservices.system.PSRelationshipConfig tgt =
               new com.percussion.webservices.system.PSRelationshipConfig();
            setConfigSummaryProperties(source, tgt);
            Document doc = PSXmlDocumentBuilder.createXmlDocument();
            String xmlString = PSXmlDocumentBuilder.toString(source.toXml(doc));
            tgt.setRelationshipConfig(xmlString);
            return tgt;
         }
      }
   }

   /**
    * Set the properties from the source to target object.
    * @param source the source, assumed not <code>null</code>.
    * @param target the target, assumed not <code>null</code>.
    */
   private void setConfigSummaryProperties(PSRelationshipConfig source, 
         RelationshipConfigSummary target)
   {
      long id = (new PSDesignGuid(source.getGUID())).getValue();
      RelationshipConfigSummaryType type;
      if (source.isSystem())
        type = RelationshipConfigSummaryType.system;
      else
        type = RelationshipConfigSummaryType.user;
      
      target.setId(id);
      target.setCategory(source.getCategory());
      target.setDescription(source.getDescription());
      target.setLabel(source.getLabel());
      target.setName(source.getName());
      target.setType(type);
   }
   
   /**
    * Creates {@link PSRelationshipConfig} object from its XML representation.
    * 
    * @param xmlString the XML representation of the to be created object,
    *    assumed not be <code>null</code> or empty.
    * 
    * @return the created object, never <code>null</code>.
    */
   private PSRelationshipConfig getRelationshipConfig(String xmlString)
   {
      try
      {
         StringReader reader = new StringReader(xmlString);
         Document doc = PSXmlDocumentBuilder.createXmlDocument(reader, false);
         return new PSRelationshipConfig(doc.getDocumentElement());         
      }
      catch (IOException | SAXException | PSUnknownNodeTypeException e)
      {
         log.error(PSExceptionUtils.getMessageForLog(e));
         throw new RuntimeException("Failed to create an XML document from \""
               + xmlString + "\"", e);
      }
   }
}

