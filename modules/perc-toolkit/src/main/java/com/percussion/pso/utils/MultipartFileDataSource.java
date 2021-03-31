/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
 /*  
 * @author DavidBenua
 */
package com.percussion.pso.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.DataSource;
import org.springframework.web.multipart.MultipartFile;

/**
 * A thin wrapper for a MultipartFile as a DataSource. 
 *
 * @author DavidBenua
 * @see javax.activation.DataSource
 * @see org.springframework.web.multipart.MultipartFile
 */
public class MultipartFileDataSource implements DataSource
{
   /**
    * the multipart file
    */
   private MultipartFile file = null; 
  
   /**
    * Sole Constructor
    * @param file the multipart file to wrap in this datasource. 
    */
   public MultipartFileDataSource(MultipartFile file)
   {
      this.file = file;
   }
   
   /*
    * @see javax.activation.DataSource#getInputStream()
    */
   public InputStream getInputStream() throws IOException
   {
       return file.getInputStream();  
   }
   /*
    * @see javax.activation.DataSource#getOutputStream()
    */
   public OutputStream getOutputStream() throws IOException
   {
      throw new IOException("OutputStreams not supported");  
   }
   /*
    * @see javax.activation.DataSource#getContentType()
    */
   public String getContentType()
   {
      return file.getContentType(); 
   }
   /*
    * @see javax.activation.DataSource#getName()
    */
   public String getName()
   {     
      return file.getName(); 
   }
}
