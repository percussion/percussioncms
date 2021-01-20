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
package com.percussion.assetmanagement.data;

import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * It contains data for requesting a URL, which can be used for creating a
 * related asset or editing an existing asset. 
 *
 * @author YuBingChen
 */

@JsonRootName("AssetEditUrlRequest")
public class PSAssetEditUrlRequest
{
   /**
    * The page parent type
    */
    /*
     * TODO switch this to an enum (Adam Gent)
     */
   public static final String PAGE_PARENT = "page";
   public static final String TEMPLATE_PARENT = "template";
   
   /**
    * Gets the type of the parent. It can be either {@link #PAGE_PARENT} or 
    * {@link #TEMPLATE_PARENT}.
    * 
    * @return the parent type, should not be blank.
    */
   public String getType()
   {
      return type;
   }
   
   /**
    * Gets the parent ID of the asset.
    * 
    * @return the parent ID, must be an existing page or template.
    */
   public String getParentId()
   {
      return parentId;
   }
   
   /**
    * Gets the asset ID.
    * 
    * @return the asset ID. It may be <code>null</code> if requesting the URL
    * for creating an asset; otherwise requesting the URL for editing
    */
   public String getAssetId()
   {
      return assetId;
   }
   
   /**
    * Gets the widget instance ID
    * @return the widget ID.
    */
   public String getWidgetId()
   {
      return widgetId;
   }
   
   /**
    * Gets the widget definition of the asset.
    * 
    * @return the widgetDefinition, must be an existing asset.
    */
   public String getWidgetDefinition()
   {
       return widgetDefinition;
   }

   /**
    * @param type the type to set
    */
   public void setType(String type)
   {
       this.type = type;
   }

   /**
    * @param parentId the parentId to set
    */
   public void setParentId(String parentId)
   {
       this.parentId = parentId;
   }

   /**
    * @param assetId the assetId to set
    */
   public void setAssetId(String assetId)
   {
       this.assetId = assetId;
   }

   /**
    * @param widgetId the widgetId to set
    */
   public void setWidgetId(String widgetId)
   {
       this.widgetId = widgetId;
   }

   /**
    * @param widgetDefinition the widgetDefinition to set
    */
   public void setWidgetDefinition(String widgetDefinition)
   {
       this.widgetDefinition = widgetDefinition;
   }
   
   /**
    * The type of the parent. It can be either {@link #PAGE_PARENT} or 
    * {@link #TEMPLATE_PARENT}.
    */
   private String type;
   
   /**
    * The ID of the parent, which can be a page or template.
    */
   private String parentId;
   
   /**
    * The asset ID. It may be <code>null</code> if requesting an URL for 
    * creating the asset.
    */
   private String assetId;
   
   /**
    * The widget instance ID on a page or template.
    */
   private String widgetId;
   
   /**
    * The widget definition ID on a page or template.
    */
   private String widgetDefinition;
   
}
