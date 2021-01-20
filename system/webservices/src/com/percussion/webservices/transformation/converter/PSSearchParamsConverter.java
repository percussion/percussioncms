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

import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.search.objectstore.PSWSSearchField;
import com.percussion.search.objectstore.PSWSSearchParams;
import com.percussion.webservices.common.ConnectorTypes;
import com.percussion.webservices.common.OperatorTypes;
import com.percussion.webservices.content.PSSearchField;
import com.percussion.webservices.content.PSSearchParamsFolderFilter;
import com.percussion.webservices.content.PSSearchParamsTitle;
import com.percussion.webservices.content.PSSearchProperty;
import com.percussion.webservices.content.PSSearchResultField;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.lang.StringUtils;

/**
 * Converts objects between the classes 
 * <code>com.percussion.search.objectstore.PSWSSearchParams</code> and 
 * <code>com.percussion.webservices.content.PSSearchSearchParams</code>.
 */
public class PSSearchParamsConverter extends PSConverter
{
   /* (non-Javadoc)
    * @see PSConverter#PSConvert(BeanUtilsUtil)
    */
   public PSSearchParamsConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);
   }

   /* (non-Javadoc)
    * @see PSConverter#convert(Class, Object)
    */
   @SuppressWarnings("unchecked")
   @Override
   public Object convert(@SuppressWarnings("unused") Class type, Object value)
   {
      if (value == null)
         return null;
      
      if (isClientToServer(value))
      {
         com.percussion.webservices.content.PSSearchParams source = 
            (com.percussion.webservices.content.PSSearchParams) value;
         
         PSWSSearchParams target = new PSWSSearchParams();

         String contentTypeName = source.getContentType();
         if (StringUtils.isBlank(contentTypeName))
         {
            target.setContentTypeId(-1);
         }
         else
         {
            PSItemDefinition def = PSItemConverterUtils.getItemDefinition(
               contentTypeName); 
            target.setContentTypeId(def.getContentEditor().getContentType());
         }
         
         PSSearchParamsTitle sourceTitle = source.getTitle();
         if (sourceTitle != null)
         {
            OperatorTypes sourceOperator = sourceTitle.getOperator() == null ? OperatorTypes.equal
                  : sourceTitle.getOperator();
            Converter converter = getConverter(OperatorTypes.class);
            PSWSSearchField.PSOperatorEnum operator = 
               (PSWSSearchField.PSOperatorEnum) converter.convert(
                  PSWSSearchField.PSOperatorEnum.class, sourceOperator);
            ConnectorTypes sourceConnector = sourceTitle.getConnector() == null ? ConnectorTypes.and
                  : sourceTitle.getConnector();
            converter = getConverter(ConnectorTypes.class);
            PSWSSearchField.PSConnectorEnum connector = 
               (PSWSSearchField.PSConnectorEnum) converter.convert(
                  PSWSSearchField.PSConnectorEnum.class, sourceConnector);
            target.setTitle(sourceTitle.getValue(), operator.getOrdinal(), 
               connector.getOrdinal());
         }
         
         target.setFTSQuery(source.getFullTextQuery());
         
         Map<String, String> properties = new HashMap<String, String>();
         if (source.getProperties() != null)
         {
            for (PSSearchProperty property : source.getProperties())
               properties.put(property.getName(), property.get_value());
         }
         target.setProperties(properties);
         
         Collection<String> searchResults = new ArrayList<String>();
         if (source.getSearchResults() != null)
         {
            for (PSSearchResultField searchResult : source.getSearchResults())
               searchResults.add(searchResult.getName());
         }
         target.setResultFields(searchResults);
         
         List<PSWSSearchField> searchFields = new ArrayList<PSWSSearchField>();
         if (source.getParameter() != null)
         {
            for (PSSearchField field : source.getParameter())
            {
               Converter converter = getConverter(field.getClass());
               searchFields.add((PSWSSearchField) converter.convert(
                  PSWSSearchField.class, field));
            }
         }
         target.setSearchFields(searchFields);
         
         target.setSearchForFolders(source.isSearchForFolders());
         PSSearchParamsFolderFilter folderFilter = source.getFolderFilter();
         if (folderFilter != null)
            target.setFolderPathFilter(folderFilter.get_value(), 
               folderFilter.isIncludeSubFolders());
         
         return target;
      }
      else
      {
         PSWSSearchParams source = (PSWSSearchParams) value;

         com.percussion.webservices.content.PSSearchParams target = 
            new com.percussion.webservices.content.PSSearchParams();

         long contentTypeId = source.getContentTypeId();
         if (contentTypeId != -1)
         {
            PSItemDefinition def = PSItemConverterUtils.getItemDefinition(
               contentTypeId); 
            target.setContentType(def.getName());
         }
         
         PSWSSearchField sourceTitle = source.getTitle();
         if (sourceTitle != null)
         {
            PSSearchParamsTitle title = new PSSearchParamsTitle();
            title.setValue(sourceTitle.getValue());
            Converter converter = getConverter(
               PSWSSearchField.PSOperatorEnum.class);
            OperatorTypes operator = (OperatorTypes) converter.convert(
               OperatorTypes.class, sourceTitle.getOperatorEnum());
            title.setOperator(operator);
            converter = getConverter(PSWSSearchField.PSConnectorEnum.class);
            ConnectorTypes connector = (ConnectorTypes) converter.convert(
               ConnectorTypes.class, sourceTitle.getConnectorEnum());
            title.setConnector(connector);
            target.setTitle(title);
         }
         
         target.setFullTextQuery(source.getFTSQuery());
         
         Map<String, String> sourceProperties = source.getProperties();
         PSSearchProperty[] properties = 
            new PSSearchProperty[sourceProperties.size()];
         int index = 0;
         for (Entry<String, String> sourceProperty : sourceProperties.entrySet())
         {
            PSSearchProperty property = new PSSearchProperty();
            property.setName(sourceProperty.getKey());
            property.set_value(sourceProperty.getValue());
            
            properties[index++] = property;
         }
         target.setProperties(properties);
         
         Collection<String> sourceSearchResults = source.getResultFields();
         PSSearchResultField[] searchResults = 
        	 new PSSearchResultField[sourceSearchResults.size()];
         index = 0;
         for (String sourceSearchResult : sourceSearchResults)
         {
            PSSearchResultField searchResult = new PSSearchResultField();
            searchResult.setName(sourceSearchResult);
            
            searchResults[index++] = searchResult;
         }
         
         target.setSearchResults(searchResults);
         
         List<PSWSSearchField> sourceSearchFields = source.getSearchFields();
         PSSearchField[] searchFields = 
        	 new PSSearchField[sourceSearchFields.size()];
         index = 0;
         for (PSWSSearchField sourceSearchField : sourceSearchFields)
         {
             Converter converter = getConverter(sourceSearchField.getClass());
             searchFields[index++] = ((PSSearchField) converter.convert(
                PSSearchField.class, sourceSearchField));
         }
         target.setParameter(searchFields);
         
         target.setSearchForFolders(source.isSearchForFolders());
         
         PSSearchParamsFolderFilter folderFilter = 
        	 new PSSearchParamsFolderFilter();
         folderFilter.set_value(source.getFolderPathFilter());
         folderFilter.setIncludeSubFolders(source.isIncludeSubFolders());
         target.setFolderFilter(folderFilter);

         return target;
      }
   }
}
