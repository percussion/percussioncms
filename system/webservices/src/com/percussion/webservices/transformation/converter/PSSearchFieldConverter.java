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
