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

