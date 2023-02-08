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

import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.security.data.PSCommunityVisibility;
import com.percussion.utils.guid.IPSGuid;

import org.apache.commons.beanutils.BeanUtilsBean;

/**
 * Converts objects between the classes 
 * <code>com.percussion.services.security.data.PSCommunityVisibility</code> and 
 * <code>com.percussion.webservices.security.data.PSCommunityVisibility</code>.
 */
public class PSCommunityVisibilityConverter extends PSConverter
{
   /* (non-Javadoc)
    * @see PSConverter#PSConvert(BeanUtilsUtil)
    */
   public PSCommunityVisibilityConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);

      m_specialProperties.add("id");
   }

   /* (non-Javadoc)
    * @see PSConverter#convert(Class, Object)
    */
   @Override
   public Object convert(Class type, Object value)
   {
      Object result = super.convert(type, value);
      if (isClientToServer(value))
      {
         com.percussion.webservices.security.data.PSCommunityVisibility orig = 
            (com.percussion.webservices.security.data.PSCommunityVisibility) value;
         
         PSCommunityVisibility dest = (PSCommunityVisibility) result;
         
         // convert id
         Long id = orig.getId();
         if (id != null)
            dest.setGUID(new PSDesignGuid(id));
      }
      else
      {
         PSCommunityVisibility orig = (PSCommunityVisibility) value;
         
         com.percussion.webservices.security.data.PSCommunityVisibility dest = 
            (com.percussion.webservices.security.data.PSCommunityVisibility) result;
         
         // convert id
         IPSGuid guid = orig.getGUID();
         if (guid != null)
            dest.setId(new PSDesignGuid(guid).getValue());
      }
      
      return result;
   }
}

