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
