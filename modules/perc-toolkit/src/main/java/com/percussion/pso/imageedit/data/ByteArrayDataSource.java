/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *  
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.imageedit.data;

import javax.activation.DataSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A simple in-memory datasource. 
 *
 * @author DavidBenua
 *
 */
public class ByteArrayDataSource implements DataSource
{
   
   ByteArrayOutputStream store; 
   String name; 
   String contentType; 
   
   /**
    * 
    */
   public ByteArrayDataSource()
   {
      store = new ByteArrayOutputStream();
   }
   public ByteArrayDataSource(String name, String contentType)
   {
      this();
      this.name =  name;
      this.contentType = contentType;
   }
   
   public ByteArrayDataSource(String name, String contentType, int size)
   {
      this(name, contentType);
      store = new ByteArrayOutputStream(size);
   }
   /**
    * @see DataSource#getContentType()
    */
   public String getContentType()
   {
      return contentType;
   }
   /**
    * @see DataSource#getInputStream()
    */
   public InputStream getInputStream() throws IOException
   {
      ByteArrayInputStream bis = new ByteArrayInputStream(store.toByteArray()); 
      return bis; 
   }
   /**
    * @see DataSource#getName()
    */
   public String getName()
   {
      return name;
   }
   /**
    * @see DataSource#getOutputStream()
    */
   public OutputStream getOutputStream() throws IOException
   {
      return store;
   }
   
   /**
    * Gets the contents of the data as a byte array. 
    * @return the current contents. Never <code>null</code>. May be <code>empty</code>
    */
   public byte[] getBytes()
   {
      return store.toByteArray();
   }
}
