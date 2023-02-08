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

import com.percussion.cms.objectstore.PSBinaryValue;
import com.percussion.content.PSContentFactory;
import com.percussion.util.IOTools;
import com.percussion.util.PSPurgableTempFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A binary value that is created from a file in order to provide additional
 * information from the original file reference such as file name, and mime 
 * type (from the file extension if there is one).  This information is only 
 * provided during the save of the item if this class is used server-side, 
 * otherwise it will provide the same functionality as {@link PSBinaryValue}.
 */
public class PSBinaryFileValue extends PSBinaryValue
{
   /**
    * No-arg constructor used by derived classes
    */
   protected PSBinaryFileValue()
   {      
   }
   
   /**
    * Reads the data from the supplied file and also stores the data in a temp
    * file recording the file name and mime type info. Use
    * {@link #getTempFile()} instead of
    * {@link PSBinaryValue#getValue() getValue()} or
    * {@link PSBinaryValue#getValueAsString() getValueAsString()} to retrieve
    * this information in addition to the data.
    * 
    * @param file The file containing the binary data. The contents of the file
    * are copied to a purgable temp file that includes the original path and
    * mime type information. See {@link #getTempFile()}.
    * 
    * @throws FileNotFoundException If the supplied file does not exist.
    * @throws IOException If there is an error reading from or writing to a
    * file.
    */
   public PSBinaryFileValue(File file) throws FileNotFoundException,IOException
   {
      super(new FileInputStream(file));

      // create temp file including source path with normalized separator 
      m_tempFile = new PSPurgableTempFile("psx", ".bin", null, 
         file.getAbsolutePath().replace('\\', '/'), 
         PSContentFactory.guessMimeType(file), null);
      
      OutputStream out = null;
      try
      {
         out = new FileOutputStream(m_tempFile);
         IOTools.copyStream(new ByteArrayInputStream((byte[]) getValue()), out);
      }
      finally
      {
         if (out != null)
         {
            try
            {
               out.close();
            }
            catch (IOException e)
            {
               // ignore
            }
         }
      }
   }

   /**
    * Get the purgable temp file created during construction. 
    * 
    * @return The file, never <code>null</code>.
    */
   public PSPurgableTempFile getTempFile()
   {
      return m_tempFile;
   }
   
   /**
    * The temp file created during construction, never <code>null</code> or 
    * modified after that.    
    */
   protected PSPurgableTempFile m_tempFile;
}

