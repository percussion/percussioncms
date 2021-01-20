
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
       ByteArrayInputStream bis = new ByteArrayInputStream(this.store.toByteArray());
       return bis;
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
