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

import com.percussion.services.content.data.PSItemStatus;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.webservices.common.Reference;

import org.apache.commons.beanutils.BeanUtilsBean;

/**
 * Converts objects between the classes
 * {@link com.percussion.services.content.data.PSItemStatus} and
 * {@link com.percussion.webservices.content.PSItemStatus}
 */
public class PSItemStatusConverter extends PSConverter
{
   /*
    * (non-Javadoc)
    * 
    * @see PSConverter#PSConvert(BeanUtilsUtil)
    */
   public PSItemStatusConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);
   }

   /*
    * (non-Javadoc)
    * 
    * @see PSConverter#convert(Class, Object)
    */
   @Override
   public Object convert(Class type, Object value) {
      if (value == null)
         return null;

      if (isClientToServer(value))
      {
         com.percussion.webservices.content.PSItemStatus source = 
            (com.percussion.webservices.content.PSItemStatus) value;

         PSLegacyGuid id = new PSLegacyGuid(source.getId());
         Long fromStateId = null;
         String fromState = null;
         Long toStateId = null;
         String toState = null;
         if (source.getFromState() != null)
         {
            fromStateId = source.getFromState().getId();
            fromState = source.getFromState().getName();
         }
         if (source.getToState() != null)
         {
            toStateId = source.getToState().getId();
            toState = source.getToState().getName();
         }
         PSItemStatus target = new PSItemStatus(id.getContentId(), 
               source.isDidCheckout(), source.isDidTransition(), fromStateId, 
               fromState, toStateId, toState);

         return target;
      }
      else // convert from objectstore to webservice
      {
         PSItemStatus source = (PSItemStatus) value;
         long id = new PSDesignGuid(
            new PSLegacyGuid(source.getId(), -1)).getValue();
         Reference fromState = null;
         Reference toState = null;
         if (source.getFromStateId() != null)
            fromState = new Reference(source.getFromStateId(), 
                  source.getFromState());
         if (source.getToStateId() != null)
            toState = new Reference(source.getToStateId(), source.getToState());
         
         com.percussion.webservices.content.PSItemStatus target;
         target = new com.percussion.webservices.content.PSItemStatus(
               fromState, toState, id, source.isDidCheckout(), 
               source.isDidTransition());

         return target;
      }
   }
}
