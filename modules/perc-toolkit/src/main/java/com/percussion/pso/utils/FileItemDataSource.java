/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.DataSource;
import org.apache.commons.fileupload.FileItem;

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
   public String getName()
   {     
      return item.getName(); 
   }
}
