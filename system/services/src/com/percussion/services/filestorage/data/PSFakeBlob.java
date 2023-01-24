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
package com.percussion.services.filestorage.data;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;

public class PSFakeBlob implements Blob
{
   InputStream stream = null;

   public PSFakeBlob(InputStream stream)
   {
      this.stream = stream;
   }

   public void free() throws SQLException
   {
      // TODO Auto-generated method stub

   }

   public InputStream getBinaryStream() throws SQLException
   {
      return stream;
   }

   public InputStream getBinaryStream(long arg0, long arg1) throws SQLException
   {
      // TODO Auto-generated method stub
      return null;
   }

   public byte[] getBytes(long arg0, int arg1) throws SQLException
   {
      // TODO Auto-generated method stub
      return null;
   }

   public long length() throws SQLException
   {
      // TODO Auto-generated method stub
      return 0;
   }

   public long position(byte[] arg0, long arg1) throws SQLException
   {
      // TODO Auto-generated method stub
      return 0;
   }

   public long position(Blob arg0, long arg1) throws SQLException
   {
      // TODO Auto-generated method stub
      return 0;
   }

   public OutputStream setBinaryStream(long arg0) throws SQLException
   {
      // TODO Auto-generated method stub
      return null;
   }

   public int setBytes(long arg0, byte[] arg1) throws SQLException
   {
      // TODO Auto-generated method stub
      return 0;
   }

   public int setBytes(long arg0, byte[] arg1, int arg2, int arg3)
         throws SQLException
   {
      // TODO Auto-generated method stub
      return 0;
   }

   public void truncate(long arg0) throws SQLException
   {
      // TODO Auto-generated method stub

   }
}
