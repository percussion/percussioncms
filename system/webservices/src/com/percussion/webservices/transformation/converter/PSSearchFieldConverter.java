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

import com.percussion.search.objectstore.PSWSSearchField;
import com.percussion.webservices.common.ConnectorTypes;
import com.percussion.webservices.common.OperatorTypes;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.lang.StringUtils;

/**
 * Converts objects between the classes 
 * <code>com.percussion.search.objectstore.PSWSSearchField</code> and 
 * <code>com.percussion.webservices.content.PSSearchField</code>.
 */
public class PSSearchFieldConverter extends PSConverter
{
   /* (non-Javadoc)
    * @see PSConverter#PSConvert(BeanUtilsUtil)
    */
   public PSSearchFieldConverter(BeanUtilsBean beanUtils)
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
         com.percussion.webservices.content.PSSearchField source = 
            (com.percussion.webservices.content.PSSearchField) value;
         
         ConnectorTypes sourceConnector = (source.getConnector() == null) ? ConnectorTypes.and
               : source.getConnector();

         Converter converter = getConverter(sourceConnector.getClass());
         PSWSSearchField.PSConnectorEnum connector = 
            (PSWSSearchField.PSConnectorEnum) converter.convert(
               PSWSSearchField.PSConnectorEnum.class, sourceConnector);
         
         PSWSSearchField target = null;
         if (StringUtils.isBlank(source.getExternalOperator()))
         {
            OperatorTypes sourceOperator = (source.getOperator() == null) ? OperatorTypes.equal
                  : source.getOperator();

            converter = getConverter(sourceOperator.getClass());
            PSWSSearchField.PSOperatorEnum operator = 
               (PSWSSearchField.PSOperatorEnum) converter.convert(
                  PSWSSearchField.PSOperatorEnum.class, sourceOperator);

            target = new PSWSSearchField(source.getName(), 
               operator.getOrdinal(), source.getValue(), 
               connector.getOrdinal());
         }
         else
         {
            target = new PSWSSearchField(source.getName(), 
               source.getExternalOperator(), source.getValue(), 
               connector.getOrdinal());
         }
         
         return target;
      }
      else
      {
         PSWSSearchField source = (PSWSSearchField) value;

         Converter converter = getConverter(
            PSWSSearchField.PSOperatorEnum.class);
         OperatorTypes operator = (OperatorTypes) converter.convert(
            OperatorTypes.class, 
            PSWSSearchField.PSOperatorEnum.valueOf(source.getOperator()));

         converter = getConverter(PSWSSearchField.PSConnectorEnum.class);
         ConnectorTypes connector = (ConnectorTypes) converter.convert(
            ConnectorTypes.class, 
            PSWSSearchField.PSConnectorEnum.valueOf(source.getConnector()));

         com.percussion.webservices.content.PSSearchField target = 
            new com.percussion.webservices.content.PSSearchField();
         target.setName(source.getName());
         target.setValue(source.getValue());
         target.setOperator(operator);
         target.setExternalOperator(source.getExternalOperator());
         target.setConnector(connector);

         return target;
      }
   }
}
