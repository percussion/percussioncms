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
package com.percussion.cms.objectstore.server;

import com.percussion.util.PSPurgableTempFile;

import java.io.InputStream;

/**
 * A binary value that is created from a temporary file in order to support
 * uploads of large files.
 */
public class PSPurgableFileValue extends PSBinaryFileValue
{
   /**
    * Stores the supplied file. Use
    * {@link #getTempFile()} to get the purgable file.
    * 
    * @param file The file containing the binary data.
    * See {@link #getTempFile()}.
    */
   public PSPurgableFileValue(PSPurgableTempFile file)
   {
      m_tempFile = file;
   }
   
   @Override
   public byte[] getData()
   {
      throw new UnsupportedOperationException("getData() is not"
            + " implemented");
   }
   
   @Override   
   public void setData(byte[] content)
   {
      throw new UnsupportedOperationException("setData(byte[]) is not"
            + " implemented");
   }

   @Override
   public void setData(InputStream content)
   {
      throw new UnsupportedOperationException("setData(InputStream)"
            + " is not implemented");
   }
}

