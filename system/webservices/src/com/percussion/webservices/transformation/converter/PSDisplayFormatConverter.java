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

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSDFColumns;
import com.percussion.cms.objectstore.PSDFMultiProperty;
import com.percussion.cms.objectstore.PSDisplayColumn;
import com.percussion.cms.objectstore.PSDisplayFormat;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.error.PSExceptionUtils;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.ui.data.CommunityRef;
import com.percussion.webservices.ui.data.PSDisplayFormatColumnsColumn;
import com.percussion.webservices.ui.data.PSDisplayFormatColumnsColumnRenderType;
import com.percussion.webservices.ui.data.PSDisplayFormatColumnsColumnSortOrder;
import com.percussion.webservices.ui.data.Property;
import org.apache.axis.types.UnsignedInt;
import org.apache.commons.beanutils.BeanUtilsBean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Converts objects between the classes
 * <code>com.percussion.i18n.PSLocale</code> and
 * <code>com.percussion.webservices.content.data.PSLocale</code>.
 */
public class PSDisplayFormatConverter extends PSConverter
{

   /* (non-Javadoc)
    * @see PSConverter#PSConvert(BeanUtilsUtil)
    */
   public PSDisplayFormatConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);
   }

   /* (non-Javadoc)
    * @see PSConverter#convert(Class, Object)
    */
   @Override
   public Object convert(Class type, Object value)
   {
      if (value == null)
         return null;

      if (isClientToServer(value))
      {
         com.percussion.webservices.ui.data.PSDisplayFormat source =
            (com.percussion.webservices.ui.data.PSDisplayFormat) value;

         PSDisplayFormat target;
         try
         {
            target = new PSDisplayFormat();
            PSDesignGuid guid = new PSDesignGuid(source.getId());
            String[] key = new String[]{String.valueOf(guid.longValue())};
            PSKey locator = PSDisplayFormat.createKey(key);
            locator.setPersisted(false);
            target.setLocator(locator);
            
            target.setInternalName(source.getName());
            target.setDisplayName(source.getLabel());
            target.setDescription(source.getDescription());
            
            PSDFColumns cols = getColumns(source.getColumns(), guid.longValue());
            target.setColumnList(cols);
            setProperties(target, source);
         }
         catch (Exception e)
         {
            log.error(PSExceptionUtils.getMessageForLog(e));
            throw new RuntimeException(e);
         }
         
         return target;
      }
      else // convert from objectstore to webservice
      {
         PSDisplayFormat source = (PSDisplayFormat) value;
         PSDisplayFormatColumnsColumn[] cols = 
            getColumns(source.getColumnContainer(), 
                  source.getSortedColumnName());
         CommunityRef[] communities = getCommunities(source);
         Property[] properties = getProperties(source);
         PSDesignGuid guid = new PSDesignGuid(source.getGUID());
         return
            new com.percussion.webservices.ui.data.PSDisplayFormat(
               guid.getValue(),
               source.getDescription(),
               cols, 
               communities,
               properties,
               source.getInternalName(),
               source.getDisplayName());

      }
   }
   /**
    * Gets the webservice communities from objectstore instance. 
    * @param source the display format object, assumed not <code>null</code>.
    * @return the communties, never <code>null</code>, may be empty.
    */
   private Property[] getProperties(PSDisplayFormat source)
   {
      List<Property> resultList = new ArrayList<>();

      // handle all community property
      if (source.doesPropertyHaveValue(PSDisplayFormat.PROP_COMMUNITY,
            PSDisplayFormat.PROP_COMMUNITY_ALL))
      {
         resultList.add(new Property(
               PSDisplayFormat.PROP_COMMUNITY,
               PSDisplayFormat.PROP_COMMUNITY_ALL));
      }
      
      // handle the rest of the properties, except sys_community
      Iterator props = source.getProperties();
      PSDFMultiProperty srcProp;
      Property tgtProp;
      
      // get all properties, except the actual community ids
      while (props.hasNext())
      {
         srcProp = (PSDFMultiProperty) props.next();

         if (!PSDisplayFormat.PROP_COMMUNITY.equals(srcProp.getName()))
         {
            String propName = srcProp.getName();
            if (srcProp.hasValues())
            {
               Iterator valueItr = srcProp.iterator();
               while (valueItr.hasNext())
               {
                  tgtProp = new Property(propName, (String) valueItr.next());
                  resultList.add(tgtProp);
               }
            }
            else
            {
               tgtProp = new Property(propName, null);
               resultList.add(tgtProp);
            }
         }
      }
      
      Property[] result = new Property[resultList.size()];
      resultList.toArray(result);
      
      return result;
   }
   
   /**
    * Gets the webservice communities from objectstore instance. 
    * @param source the display format object, assumed not <code>null</code>.
    * @return the communties, never <code>null</code>, may be empty.
    */
   private CommunityRef[] getCommunities(PSDisplayFormat source)
   {
      CommunityRef[] result = new CommunityRef[0];
      Map<IPSGuid, String> communities = source.getAllowedCommunities(); 
      if (communities != null)
      {
         result = new CommunityRef[communities.size()];
         int i=0;
         for (Map.Entry<IPSGuid, String> community : communities.entrySet())
         {
            PSDesignGuid guid = new PSDesignGuid(community.getKey());
            result[i++] = new CommunityRef(
                  guid.getValue(), community.getValue());
         }
      }
      else // get ids only if any
      {
         if (!source.doesPropertyHaveValue(PSDisplayFormat.PROP_COMMUNITY,
               PSDisplayFormat.PROP_COMMUNITY_ALL))
         {
            Iterator props = source.getProperties();
            PSDFMultiProperty prop;

            while (props.hasNext())
            {
               prop = (PSDFMultiProperty) props.next();

               if (PSDisplayFormat.PROP_COMMUNITY.equals(prop.getName()))
               {
                  result = new CommunityRef[prop.size()];
                  Iterator values = prop.iterator();
                  String value;
                  int i=0;
                  while (values.hasNext())
                  {
                     value = (String) values.next();
                     long id = Long.parseLong(value);
                     PSDesignGuid guid = new PSDesignGuid(new PSGuid(
                           PSTypeEnum.COMMUNITY_DEF, id));
                     result[i++] = new CommunityRef(guid.getValue(), "");
                  }
                  break;
               }
            }
         }
      }
      return result;
   }
   /**
    * Converts the columns from objectstore type to webservice type.
    * 
    * @param cols the to be converted column data, assumed not <code>null</code>.
    * @param sortColumn the name of the sort column, assumed not 
    *    <code>null</code>.
    * 
    * @return the converted columns, never <code>null</code>, may be empty.
    */
   private PSDisplayFormatColumnsColumn[] getColumns(
         PSDFColumns cols, String sortColumn)
   {
      PSDisplayFormatColumnsColumn[] tgtCols = new
         PSDisplayFormatColumnsColumn[cols.size()];
      PSDisplayFormatColumnsColumn tgtCol;
      
      for (int i=0; i<cols.size(); i++)
      {
         PSDisplayColumn col = (PSDisplayColumn)cols.get(i);
         
         String dataType;
         if (col.isNumberType()) 
            dataType = PSDisplayFormatColumnsColumnRenderType._number;
         else if (col.isImageType())
            dataType = PSDisplayFormatColumnsColumnRenderType._image;
         else if (col.isDateType())
            dataType = PSDisplayFormatColumnsColumnRenderType._date;
         else
            dataType = PSDisplayFormatColumnsColumnRenderType._text;
            
         PSDisplayFormatColumnsColumnRenderType renderType =
            PSDisplayFormatColumnsColumnRenderType.fromString(
                  dataType);
         String sortOrderString = col.isAscendingSort() ? 
               SORT_ORDER_ASCENDING : SORT_ORDER_DESCENDING;
         PSDisplayFormatColumnsColumnSortOrder sortOrder =
            PSDisplayFormatColumnsColumnSortOrder.fromString(
                  sortOrderString);
         UnsignedInt sequence = new UnsignedInt(String.valueOf(col
               .getPosition()));
         boolean isSortedColumn = col.getSource().equalsIgnoreCase(sortColumn);
         
         tgtCol = new PSDisplayFormatColumnsColumn(
               col.getSource(),
               col.getDisplayName(),
               col.getDescription(),
               col.isCategorized(),
               isSortedColumn,
               renderType,
               sortOrder,
               sequence,
               col.getWidth()
               );
         
         tgtCols[i] = tgtCol;
      }
      
      return tgtCols;
   }
   /**
    * Converts a list of WS columns to a list of objectstore columns
    * 
    * @param cols the WS columns, assume not <code>null</code>.
    * 
    * @return a list of objectstore columns, never <code>null</code>, but
    *    may be empty.
    *    
    * @throws PSCmsException if an error occurs during the convertion. 
    * @throws ClassNotFoundException if cannot find {@link PSDisplayColumn} 
    *    class. 
    */
   private PSDFColumns getColumns(
         PSDisplayFormatColumnsColumn[] cols, long displayId)
         throws PSCmsException, ClassNotFoundException 
   {
      PSDFColumns tgtCols = new PSDFColumns();
      if (cols == null)
         return tgtCols;
      
      PSDisplayColumn tgtCol;
      boolean isAscendingSort;
      int groupType;
      for (PSDisplayFormatColumnsColumn col : cols)
      {
         PSKey key = PSDisplayColumn.createKey(col.getName(), displayId, false);
         isAscendingSort = col.getSortOrder().getValue().equalsIgnoreCase(
                 SORT_ORDER_ASCENDING);
         groupType = col.isCategory() ? PSDisplayColumn.GROUPING_CATEGORY
               : PSDisplayColumn.GROUPING_FLAT;
         
         tgtCol = new PSDisplayColumn(key);
               
         tgtCol.setDisplayName(col.getLabel());
         tgtCol.setDescription(col.getDescription());
         tgtCol.setSortOrder(isAscendingSort);
         tgtCol.setPosition(col.getSequence().intValue());
         tgtCol.setWidth(col.getWidth());
         tgtCol.setGroupingType(groupType);

         String dataType = col.getRenderType().getValue();
         if (dataType.equalsIgnoreCase(PSDisplayFormatColumnsColumnRenderType._number))
            dataType = PSDisplayColumn.DATATYPE_NUMBER;
         else if (dataType.equalsIgnoreCase(PSDisplayFormatColumnsColumnRenderType._image))
            dataType = PSDisplayColumn.DATATYPE_IMAGE;
         else if (dataType.equalsIgnoreCase(PSDisplayFormatColumnsColumnRenderType._date))
            dataType = PSDisplayColumn.DATATYPE_DATE;
         else
            dataType = PSDisplayColumn.DATATYPE_TEXT;
         tgtCol.setRenderType(dataType);

         tgtCols.add(tgtCol);
      }
      return tgtCols;
   }
   
   /**
    * Converts the properties from webservice (client) object to objectstore
    * object.
    * @param target the objectstore object, assumed not <code>null</code>.
    * @param source the webservice object, assumed not <code>null</code>.
    */
   private void setProperties(PSDisplayFormat target,
         com.percussion.webservices.ui.data.PSDisplayFormat source)
   {
      
      boolean allowAllCommunities = false;
      // get non PROP_COMMUNITY properties
      for (Property prop : source.getProperties())
      {
         if (prop.getName().equalsIgnoreCase(PSDisplayFormat.PROP_COMMUNITY)
               && prop.getValue().equals(PSDisplayFormat.PROP_COMMUNITY_ALL))
         {
            allowAllCommunities = true;
         }
         else
         {
            target.setProperty(prop.getName(), prop.getValue());
         }
      }
      
      // get PROP_COMMUNITY properties
      if (allowAllCommunities)
      {
         target.addCommunity(PSDisplayFormat.PROP_COMMUNITY_ALL);
      }
      else // get a list of community id(s), throw away name(s)
      {
         for (CommunityRef community : source.getCommunities())
         {
            PSDesignGuid guid = new PSDesignGuid(community.getId());
            target.addCommunity(String.valueOf(guid.longValue()));
         }
      }
   }
   
   /**
    * The string value for ascending sort order used in WS.
    */
   private static final String SORT_ORDER_ASCENDING = "ascending";
   private static final String SORT_ORDER_DESCENDING = "descending";
}
