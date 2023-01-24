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

import com.percussion.cms.objectstore.PSFolder;
import com.percussion.services.content.data.PSItemSummary;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.webservices.common.ObjectType;
import com.percussion.webservices.common.Reference;
import com.percussion.webservices.content.PSItemSummaryOperation;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.Converter;

/**
 * Converts objects between the classes
 * {@link com.percussion.services.content.data.PSItemSummary} and
 * {@link com.percussion.webservices.content.PSItemSummary}
 */
public class PSItemSummaryConverter extends PSConverter
{
   /**
    * @see PSConverter#PSConverter(BeanUtilsBean)
    */
   public PSItemSummaryConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);
   }

   /* (non-Javadoc)
    * @see PSConverter#convert(Class, Object)
    */
   @Override
   public Object convert(Class type, Object value)
   {
      if (value == null)
         return null;
      
      try
      {
         if (isClientToServer(value))
         {
            com.percussion.webservices.content.PSItemSummary orig = 
               (com.percussion.webservices.content.PSItemSummary) value;
            
            PSItemSummary dest = new PSItemSummary();
            toServer(orig, dest);
            
            return dest;
         }
         else
         {
            PSItemSummary orig = (PSItemSummary) value;
            
            com.percussion.webservices.content.PSItemSummary dest = 
               new com.percussion.webservices.content.PSItemSummary();
            toClient(orig, dest);
            
            return dest;
         }
      }
      finally
      {
      }
   }
   
   /**
    * Copy the supplied client object to the server object.
    * 
    * @param orig the client object, not <code>null</code>.
    * @param dest the server object, not <code>null</code>.
    */
   protected void toServer(
      com.percussion.webservices.content.PSItemSummary orig, 
      PSItemSummary dest)
   {
      if (orig == null)
         throw new IllegalArgumentException("orig cannot be null");
      
      if (dest == null)
         throw new IllegalArgumentException("dest cannot be null");
      
      dest.setGUID(new PSLegacyGuid(orig.getId()));
      dest.setName(orig.getName());

      ObjectType sourceObjectType = orig.getObjectType();
      Converter converter = getConverter(ObjectType.class);
      PSItemSummary.ObjectTypeEnum objectType = 
         (PSItemSummary.ObjectTypeEnum) converter.convert(
            PSItemSummary.ObjectTypeEnum.class, sourceObjectType);
      dest.setObjectType(objectType);

      if (objectType == PSItemSummary.ObjectTypeEnum.FOLDER)
      {
         dest.setContentTypeId(PSFolder.FOLDER_CONTENT_TYPE_ID);
      }
      else
      {
         dest.setContentTypeId((int)orig.getContentType().getId());
         dest.setContentTypeName(orig.getContentType().getName());
      }

      Collection<PSItemSummary.OperationEnum> operations = 
         new ArrayList<PSItemSummary.OperationEnum>();
      for (PSItemSummaryOperation sourceOperation : orig.getOperation())
      {
         converter = getConverter(PSItemSummaryOperation.class);
         PSItemSummary.OperationEnum operation = 
            (PSItemSummary.OperationEnum) converter.convert(
               PSItemSummary.OperationEnum.class, sourceOperation);
         operations.add(operation);
      }
      dest.setOperations(operations);
   }
   
   /**
    * Copy the supplied server object to the client object.
    * 
    * @param orig the server object, not <code>null</code>.
    * @param dest the client object, not <code>null</code>.
    */
   protected void toClient(PSItemSummary orig, 
      com.percussion.webservices.content.PSItemSummary dest)
   {
      if (orig == null)
         throw new IllegalArgumentException("orig cannot be null");
      
      if (dest == null)
         throw new IllegalArgumentException("dest cannot be null");
      
      dest.setId(orig.getGUID().longValue());
      dest.setName(orig.getName());

      PSItemSummary.ObjectTypeEnum sourceObjectType = 
         orig.getObjectType();
      Converter converter = getConverter(
         PSItemSummary.ObjectTypeEnum.class);
      ObjectType objectType = (ObjectType) converter.convert(
         ObjectType.class, sourceObjectType);
      dest.setObjectType(objectType);

      // expose content type for item only, but not for folder, which is 
      // Rhythmyx's implementation detail should be hide from the user. 
      if (sourceObjectType == PSItemSummary.ObjectTypeEnum.ITEM)
      {
         dest.setContentType(new Reference(orig.getContentTypeId(), 
            orig.getContentTypeName()));
      }

      PSItemSummaryOperation[] operations = 
         new PSItemSummaryOperation[orig.getOperations().size()];
      int index = 0;
      for (PSItemSummary.OperationEnum sourceOperation : orig.getOperations())
      {
         converter = getConverter(PSItemSummary.OperationEnum.class);
         PSItemSummaryOperation operation = 
            (PSItemSummaryOperation) converter.convert(
               PSItemSummaryOperation.class, sourceOperation);
         operations[index++] = operation;
      }
      dest.setOperation(operations);
   }
}

