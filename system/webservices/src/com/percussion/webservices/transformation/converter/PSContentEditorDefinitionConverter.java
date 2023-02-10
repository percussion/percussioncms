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

import com.percussion.design.objectstore.PSContentEditorSharedDef;
import com.percussion.design.objectstore.PSContentEditorSystemDef;
import com.percussion.design.objectstore.PSUnknownDocTypeException;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.webservices.content.PSContentEditorDefinition;
import com.percussion.webservices.content.PSContentEditorDefinitionType;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.StringReader;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConversionException;
import org.w3c.dom.Document;

/**
 * Converts objects between the classes 
 * <code>com.percussion.design.objectstore.PSContentEditorSystemDef</code> or 
 * <code>com.percussion.design.objectstore.PSContentEditorSharedDef</code> and 
 * <code>com.percussion.webservices.content.data.PSContentEditorDefinition</code>.
 */
public class PSContentEditorDefinitionConverter extends PSConverter
{
   /* (non-Javadoc)
    * @see PSConverter#PSConvert(BeanUtilsUtil)
    */
   public PSContentEditorDefinitionConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);
   }

   /* (non-Javadoc)
    * @see PSConverter#convert(Class, Object)
    */
   @Override
   public Object convert(Class type, Object value)
   {
      Object result = null;
      if (isClientToServer(value))
      {
         PSContentEditorDefinition orig = (PSContentEditorDefinition) value;

         try
         {
            StringReader reader = new StringReader(orig.getDefinition());
            Document doc = PSXmlDocumentBuilder.createXmlDocument(reader, false);
            
            if (orig.getType().equals(PSContentEditorDefinitionType.system))
               result = new PSContentEditorSystemDef(doc);
            else
               result = createSharedDefinition(doc);
         }
         catch (Exception e)
         {
            throw new ConversionException(e);
         }
      }
      else
      {
         PSContentEditorDefinition dest = new PSContentEditorDefinition();

         if (value instanceof PSContentEditorSystemDef)
         {
            PSContentEditorSystemDef orig = (PSContentEditorSystemDef) value;
            dest.setName("ContentEditorSystemDefinition");
            dest.setType(PSContentEditorDefinitionType.system);

            String definition = PSXmlDocumentBuilder.toString(
               orig.toXml()).trim();
            dest.setDefinition(definition);
         }
         else
         {
            PSContentEditorSharedDef orig = (PSContentEditorSharedDef) value;
            dest.setName("ContentEditorSharedDefinition");
            dest.setType(PSContentEditorDefinitionType.shared);

            String definition = PSXmlDocumentBuilder.toString(
               orig.toXml()).trim();
            dest.setDefinition(definition);
         }
         
         result = dest;
      }
      
      return result;
   }
   
   /**
    * Returns the shared definition, this method will be overridden in
    * the workbench so it can return a <code>PSUiContentEditorSharedDef</code>. 
    * @param doc cannot be <code>null<code>.
    * @return the CE shared def object, never <code>null</code>
    * @throws PSUnknownNodeTypeException 
    * @throws PSUnknownDocTypeException 
    */
   protected Object createSharedDefinition(Document doc) 
      throws PSUnknownDocTypeException, PSUnknownNodeTypeException
   {
      return new PSContentEditorSharedDef(doc);
   }
}

