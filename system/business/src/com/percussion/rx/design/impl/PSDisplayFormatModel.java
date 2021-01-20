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
package com.percussion.rx.design.impl;

import com.percussion.cms.objectstore.IPSDbComponent;
import com.percussion.cms.objectstore.PSComponentProcessorProxy;
import com.percussion.cms.objectstore.PSDisplayFormat;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.rx.design.PSDesignModelUtils;
import com.percussion.utils.guid.IPSGuid;

import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;

public class PSDisplayFormatModel extends PSDesignModel
{
   @Override
   public Object load(IPSGuid guid)
   {
      if (guid == null || !isValidGuid(guid))
         throw new IllegalArgumentException("guid is not valid for this model");
      
      PSDisplayFormat displayFormat = null;
      try
      {
         PSComponentProcessorProxy proxy = PSDesignModelUtils
               .getComponentProxy();
         String compType = PSDisplayFormat.getComponentType(
               PSDisplayFormat.class);

         Element[] elements = proxy.load(compType,
               new PSKey[] { PSDesignModelUtils
                     .getComponentKey(guid, compType) });
         if (elements.length > 0)
         {
            displayFormat = new PSDisplayFormat(elements[0]);
         }
      }
      catch (Exception e) 
      {
         throw new RuntimeException(e);
      }
   
      if (displayFormat == null)
      {
         String msg = "Failed to get the design object for guid {0}";
         Object[] args = { guid };
         throw new RuntimeException(MessageFormat.format(msg, args));
      }
      
      return displayFormat;
   }
   
   @Override
   public Object loadModifiable(IPSGuid guid)
   {
      return load(guid);
   }
   
   @Override
   public Object load(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");
      
      PSDisplayFormat displayFormat = null;
      try
      {
         PSComponentProcessorProxy proxy = 
            PSDesignModelUtils.getComponentProxy();
         Element[] elements = proxy.load(PSDisplayFormat.getComponentType(
            PSDisplayFormat.class), null);
         for (Element element : elements)
         {
            PSDisplayFormat df = new PSDisplayFormat(element);
            if (df.getInternalName().equals(name))
            {
               displayFormat = df;
               break;
            }
         }
      }
      catch (Exception e) 
      {
         throw new RuntimeException(e);
      }
   
      if (displayFormat == null)
      {
         String msg = "Failed to get the design object for name {0}";
         Object[] args = { name };
         throw new RuntimeException(MessageFormat.format(msg, args));
      }
      
      return displayFormat;
   }

   @Override
   public IPSGuid nameToGuid(String name)
   {
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name must not be null");
      }
      PSDisplayFormat df = (PSDisplayFormat) load(name);
      return df.getGUID();
   }
   
   @Override
   public void save(Object obj)
   {
      if (obj == null)
         throw new IllegalArgumentException("obj must not be null");

      if (!(obj instanceof PSDisplayFormat))
      {
         throw new RuntimeException("Invalid Object passed for save.");
      }
      try
      {
         PSComponentProcessorProxy proxy = PSDesignModelUtils
               .getComponentProxy();
         proxy.save(new IPSDbComponent[] { ((PSDisplayFormat) obj) });
      }
      catch (Exception e)
      {
         String msg = "Failed to save the display format design object ({0})";
         Object[] args = { ((PSDisplayFormat) obj).getName() };
         throw new RuntimeException(MessageFormat.format(msg, args), e);
      }
   }
   
   @Override
   public void delete(IPSGuid guid)
   {
      if (guid == null || !isValidGuid(guid))
         throw new IllegalArgumentException("guid is not valid for this model");
      PSDisplayFormat df = (PSDisplayFormat) load(guid);
      delete(df);
   }

   /**
    * Deletes the supplied display format object.
    * @param df assumed not <code>null</code>.
    */
   private void delete(PSDisplayFormat df)
   {
      try
      {
         PSComponentProcessorProxy proxy = 
            PSDesignModelUtils.getComponentProxy();
         PSKey[] dfs = {df.getLocator()};
         proxy.delete(df.getComponentType(),dfs);
      }
      catch (Exception e) 
      {
         throw new RuntimeException(e);
      }
   }
   
   @Override
   public void delete(String name)
   {
      PSDisplayFormat df = (PSDisplayFormat) load(name);
      delete(df);
   }
}
