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

import java.io.InputStream;

import org.apache.commons.lang.StringUtils;

/**
 * Base class for all service requests to create assets during bulk upload.
 */
public abstract class PSAbstractAssetRequest
{
   /**
    * Gets the type of asset this request will be used to create.  
    * 
    * @return the asset type, may be <code>null</code>.
    */
   public AssetType getType()
   {
      return type;
   }
   
   /**
    * @param type the asset type to set.  May not be <code>null</code>.
    */
   protected void setType(AssetType type)
   {
       if (type == null)
       {
           throw new IllegalArgumentException("type may not be null");
       }
       
       this.type = type;
   }

   /**
    * Gets the folder path (finder) under which the asset will be created.
    * 
    * @return the new asset folder path, may be <code>null</code>.
    */
   public String getFolderPath()
   {
       return folderPath;
   }

   /**
    * Sets the folder path (finder) under which the asset will be created.
    * 
    * @param folderPath the new asset folder path, may not be <code>null</code> or empty.
    */
   protected void setFolderPath(String folderPath)
   {
       if (StringUtils.isBlank(folderPath))
       {
           throw new IllegalArgumentException("folderPath may not be blank");
       }
       
       this.folderPath = folderPath;
   }
   
   /**
    * Gets the name of the file for which the binary asset will be created.
    * 
    * @return the file name, may be <code>null</code>.
    */
   public String getFileName()
   {
       return fileName;
   }

   /**
    * @param fileName may not be <code>null</code> or empty.
    */
   protected void setFileName(String fileName)
   {
       if (StringUtils.isBlank(fileName))
       {
           throw new IllegalArgumentException("fileName may not be blank");
       }

       this.fileName = fileName;
   }
   
   /**
    * Gets the contents of the file for which the binary asset will be created.
    * 
    * @return the file contents, may be <code>null</code>.
    */
   public InputStream getFileContents()
   {
       return fileContents;
   }

   /**
    * @param fileContents may not be <code>null</code>.
    */
   protected void setFileContents(InputStream fileContents)
   {
       this.fileContents = fileContents;
   }
   
   /**
    * Specifies the type of asset to be created by a request.
    */
   public enum AssetType
   {
       /**
        * Binary assets
        */
       FILE,
       
       IMAGE,
       
       FLASH,
       
       /**
        * Extracted assets
        */
       HTML,
       
       RICH_TEXT,
       
       SIMPLE_TEXT;
   }
   
   /**
    * @see #getFolderPath()
    * @see #setFolderPath(String)
    */
   private String folderPath;
   
   /**
    * @see #getType()
    * @see #setType(AssetType) 
    */
   private AssetType type;

   /**
    * @see #getFileName()
    * @see #setFileName(String)
    */
   private String fileName;
   
   /**
    * @see #getFileContents()
    * @see #setFileContents(InputStream)
    */
   private InputStream fileContents;
   
}
