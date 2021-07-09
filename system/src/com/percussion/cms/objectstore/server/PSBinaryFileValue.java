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

