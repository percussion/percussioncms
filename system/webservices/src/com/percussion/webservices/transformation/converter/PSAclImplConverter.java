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

import com.percussion.services.security.IPSAclEntry;
import com.percussion.services.security.data.PSAclEntryImpl;
import com.percussion.services.security.data.PSAclImpl;
import com.percussion.webservices.transformation.impl.PSTransformerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.Converter;

/**
 * Convert between {@link com.percussion.services.security.data.PSAclImpl} and
 * {@link com.percussion.webservices.system.PSAclImpl}
 */
public class PSAclImplConverter extends PSConverter
{

   /**
    * See {@link PSConverter#PSConverter(BeanUtilsBean) super()}
    * @param beanUtils
    */
   public PSAclImplConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);
      m_specialProperties.add("entries");
      m_specialProperties.add("guid");
      m_specialProperties.add("permissions");
      m_specialProperties.add("version");
   }

   @Override
   public Object convert(Class type, Object value)
   {
      if (value == null)
         return null;
      
      Object result = super.convert(type, value);
      
      if (isClientToServer(value))
      {
         com.percussion.webservices.system.PSAclImpl src = 
            (com.percussion.webservices.system.PSAclImpl) value;
         PSAclImpl tgt = (PSAclImpl) result;
         
         Converter converter = PSTransformerFactory.getInstance().getConverter(
            com.percussion.webservices.system.PSAclEntryImpl.class);
         
         for (com.percussion.webservices.system.PSAclEntryImpl entry : 
            src.getEntries())
         {
            tgt.addEntry((PSAclEntryImpl) converter.convert(
               PSAclEntryImpl.class, entry));
         }
      }
      else
      {
         PSAclImpl src = (PSAclImpl) value;
         com.percussion.webservices.system.PSAclImpl tgt = 
            (com.percussion.webservices.system.PSAclImpl) result;
         
         Collection<IPSAclEntry> entrySet = src.getEntries();
         List<com.percussion.webservices.system.PSAclEntryImpl> entries =
            new ArrayList<com.percussion.webservices.system.
               PSAclEntryImpl>();
         
         Converter converter = PSTransformerFactory.getInstance().getConverter(
            PSAclEntryImpl.class);
         for (IPSAclEntry entry : entrySet)
         {
            entries.add((com.percussion.webservices.system.PSAclEntryImpl) 
               converter.convert(
                  com.percussion.webservices.system.PSAclEntryImpl.class, 
                  entry));
         }
         
         tgt.setEntries(entries.toArray(
            new com.percussion.webservices.system.
               PSAclEntryImpl[entries.size()]));
      }
      
      return result;
   }
}

