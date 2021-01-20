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

