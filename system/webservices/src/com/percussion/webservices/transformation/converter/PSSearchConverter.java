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

import com.percussion.search.objectstore.PSWSSearchParams;
import com.percussion.search.objectstore.PSWSSearchRequest;
import com.percussion.webservices.content.PSSearchParams;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.Converter;

/**
 * Converts objects between the classes 
 * <code>com.percussion.search.objectstore.PSWSSearchRequest</code> and 
 * <code>com.percussion.webservices.content.PSSearch</code>.
 */
public class PSSearchConverter extends PSConverter
{
   /* (non-Javadoc)
    * @see PSConverter#PSConvert(BeanUtilsUtil)
    */
   public PSSearchConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);
   }

   /* (non-Javadoc)
    * @see PSConverter#convert(Class, Object)
    */
   @SuppressWarnings("unchecked")
   @Override
   public Object convert(Class type, Object value)
   {
      if (value == null)
         return null;
      
      if (isClientToServer(value))
      {
         com.percussion.webservices.content.PSSearch source = 
            (com.percussion.webservices.content.PSSearch) value;
         
         Converter converter = getConverter(PSSearchParams.class);
         PSWSSearchParams params = (PSWSSearchParams) converter.convert(
            PSWSSearchParams.class, source.getPSSearchParams());
         
         PSWSSearchRequest target = new PSWSSearchRequest(params);
         target.setCaseInsensitiveSearch(source.isUseDbCaseSensitivity());
         target.setUseExternalSearchEngine(source.isUseExternalSearchEngine());
         
         return target;
      }
      else
      {
         PSWSSearchRequest source = (PSWSSearchRequest) value;
         
         Converter converter = getConverter(PSWSSearchParams.class);
         PSSearchParams params = (PSSearchParams) converter.convert(
            PSSearchParams.class, source.getSearchParams());

         com.percussion.webservices.content.PSSearch target = 
            new com.percussion.webservices.content.PSSearch();
         target.setPSSearchParams(params);
         target.setUseDbCaseSensitivity(source.isCaseInsensitiveSearch());
         target.setUseExternalSearchEngine(source.useExternalSearchEngine());

         return target;
      }
   }
}
