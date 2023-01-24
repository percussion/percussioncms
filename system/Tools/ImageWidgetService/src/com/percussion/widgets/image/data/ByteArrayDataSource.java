
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

package com.percussion.widgets.image.data;
     
     import java.io.ByteArrayInputStream;
     import java.io.ByteArrayOutputStream;
     import java.io.IOException;
     import java.io.InputStream;
     import java.io.OutputStream;
     import javax.activation.DataSource;
     
     public class ByteArrayDataSource
       implements DataSource
     {
       ByteArrayOutputStream store;
       String name;
       String contentType;
     
       public ByteArrayDataSource()
       {
      this.store = new ByteArrayOutputStream();
       }
     
       public ByteArrayDataSource(String name, String contentType) {
      this();
      this.name = name;
       this.contentType = contentType;
       }
     
       public ByteArrayDataSource(String name, String contentType, int size)
       {
       this(name, contentType);
       this.store = new ByteArrayOutputStream(size);
       }
     
       public String getContentType()
       {
       return this.contentType;
       }
     
       public InputStream getInputStream()
         throws IOException
       {
           try(ByteArrayInputStream bis = new ByteArrayInputStream(this.store.toByteArray())) {
               return bis;
           }
       }
     
       public String getName()
       {
       return this.name;
       }
     
       public OutputStream getOutputStream()
         throws IOException
       {
       return this.store;
       }
     
       public byte[] getBytes()
       {
       return this.store.toByteArray();
       }
     }
