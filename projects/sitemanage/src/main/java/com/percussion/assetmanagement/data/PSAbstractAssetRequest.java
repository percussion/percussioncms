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

import org.apache.commons.lang.StringUtils;

import java.io.InputStream;

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

       this.fileName = fileName.replace("\\x20","-");
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
