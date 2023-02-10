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
