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

