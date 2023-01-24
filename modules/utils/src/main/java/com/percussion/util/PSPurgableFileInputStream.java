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

package com.percussion.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * This class is used to create a {@link java.io.FileInputStream} for a 
 * purgable temporary file which is deleted when the stream is closed.
 * 
 * @see PSPurgableTempFile
 */
public class PSPurgableFileInputStream extends FileInputStream
{
   /**
    * Construct an instance from a purgable temp file.
    *
    * @param tmpFile   the purgable temp file, it may not be <code>null</code>
    *
    * @throws FileNotFoundException if an exception throw from
    *    {@link java.io.FileInputStream#FileInputStream(File)}
    *
    * @see java.io.FileInputStream
    */
   public PSPurgableFileInputStream(PSPurgableTempFile tmpFile)
      throws FileNotFoundException
   {
      super(tmpFile);
      m_tmpFile = tmpFile;
   }

   /**
    * The same as {@link java.io.FileInputStream#close()}, 
    * except the file of this stream will be deleted afterwards
    */
   public void close()
      throws IOException
   {
      super.close();
      m_tmpFile.release();
   }
   
   /**
    * The purgable temp file. Initialized by constructor, never
    * <code>null</code> after that.
    */
   private PSPurgableTempFile m_tmpFile;
}

