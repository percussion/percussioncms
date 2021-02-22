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

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class to provide the content creating/editing criteria part of the page or
 * template. When content is created part of a page or template it becomes local
 * to the page or template and is treated as local content. If the content type
 * associated to the widget produces a resource, like images, files etc. then
 * the content becomes shared automatically. The name of the content is
 * auto-generated for the local content and for the shared content it is
 * provided by the user.
 * 
 * @author bjoginipally
 * @author adamgent
 * 
 */
@XmlRootElement(name = "ContentEditCriteria")
public class PSContentEditCriteria extends PSAssetEditor
{

   /*
    * See PSContentEditor#producesResource(boolean)
    */
   private boolean producesResource = false;

   private String contentName = "";

   /**
    * The parent folder path that the asset is located. This path does not include the name of the asset itself.
    */
   private String folderPath = "";
   
   private Boolean createSharedAsset;
   
   private Integer preferredEditorHeight;
   {
       setPreferredEditorHeight(DEFAULT_PREFERRED_EDITOR_HEIGHT);
   }
   private Integer preferredEditorWidth;
   {
       setPreferredEditorWidth(DEFAULT_PREFERRED_EDITOR_WIDTH);
   }
   
   /**
    * Gets the folder path that the asset is located. This path does not include the name of the asset itself.
    * @return the folder path. It can never be <code>null</code>, but may be empty for a local asset.
    */
   public String getFolderPath()
   {
       return folderPath;
   }
   
   /**
    * Sets the folder path that the asset is located.
    * @param folderPath the folder path, <code>null</code> value treated as empty.
    */
   public void setFolderPath(String folderPath)
   {
       if (folderPath == null) folderPath = "";
       this.folderPath = folderPath;
   }
   
   /**
    * The type of the content produced by the editor. The value should be filled
    * from the content editors producesResource attribute.
    * @return <code>true</code> if it does.
    * 
    */
   public boolean getProducesResource()
   {
      return producesResource;
   }

   public void setProducesResource(boolean producesResource)
   {
      this.producesResource = producesResource;
   }

   /**
    * The name of the content that gets created. Should be set to a unique name,
    * if it is local content otherwise blank.
    * @return never <code>null</code>, maybe blank. 
    */
   public String getContentName()
   {
      return contentName;
   }

   public void setContentName(String name)
   {
      this.contentName = name;
   }

   /**
    * Preferred editor height, this value needs to be set to the widget
    * definitions preferred height value.
    * @return never <code>null</code>.
    * 
    */
   public Integer getPreferredEditorHeight()
   {
      return preferredEditorHeight;
   }

   public void setPreferredEditorHeight(Integer preferredEditorHeight)
   {
      this.preferredEditorHeight = preferredEditorHeight;
   }

   /**
    * Preferred editor width, this value needs to be set to the widget
    * definitions preferred width value.
    * @return never <code>null</code>. 
    */
   public Integer getPreferredEditorWidth()
   {
      return preferredEditorWidth;
   }

   public void setPreferredEditorWidth(Integer preferredEditorWidth)
   {
      this.preferredEditorWidth = preferredEditorWidth;
   }
   
   /**
    * Creates shared asset flag, this value needs to be set in the widget
    * definition.
    * @return boolean. 
    */
   public Boolean getCreateSharedAsset()
   {
      return createSharedAsset;
   }

   public void setCreateSharedAsset(Boolean createSharedAsset)
   {
      this.createSharedAsset = createSharedAsset;
   }
   
   /**
    * Constant for default preferred height for the editor. 
    */
   public static int DEFAULT_PREFERRED_EDITOR_HEIGHT = 400;

   /**
    * Constant for default preferred width for the editor. 
    */
   public static int DEFAULT_PREFERRED_EDITOR_WIDTH = 800;
   
}
