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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
