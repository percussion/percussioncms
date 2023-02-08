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
package com.percussion.pso.utils;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.fileupload.FileItem;

import javax.activation.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A thin wrapper for a FileItem as a DataSource. 
 * 
 * @author DavidBenua
 * @see javax.activation.DataSource
 * @see org.apache.commons.fileupload.FileItem
 */
public class FileItemDataSource implements DataSource
{
   /**
    * the wrapped file item. 
    */
   private FileItem item = null; 
   
   /**
    * Sole Constructor. 
    * @param item the file item to be wrapped. 
    * Must not be <code>null</code>. 
    */
   public FileItemDataSource(FileItem item)
   {
      this.item = item; 
   }
   
   /**
    * Gets the file data as a stream.
    * @return the stream  
    * @see javax.activation.DataSource#getInputStream()
    */
   public InputStream getInputStream() throws IOException
   {
       return item.getInputStream(); 
   }
   /**
    * Gets a stream for writing the data.
    * @return the stream. 
    * @see javax.activation.DataSource#getOutputStream()
    */
   public OutputStream getOutputStream() throws IOException
   {
      return item.getOutputStream();       
   }
   /**
    * Gets the MIME content type of this file.
    * @return the content type.  
    * @see javax.activation.DataSource#getContentType()
    */
   public String getContentType()
   {
      return item.getContentType(); 
   }
   /**
    * Gets the file name. 
    * @return the file name. 
    * @see javax.activation.DataSource#getName()
    */
   @SuppressFBWarnings("FILE_UPLOAD_FILENAME")
   public String getName()
   {     
      return item.getName(); 
   }
}
