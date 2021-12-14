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
package com.percussion.content;

import com.percussion.util.PSCharSets;
import com.percussion.util.PSPurgableFileInputStream;
import com.percussion.util.PSPurgableTempFile;

import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

/**
 * Encapsulates the results from converting content using the 
 * {@link PSContentConverter}
 */
public class PSConversionResults
{
   /**
    * Construct this class from its member data
    * 
    * @param resultFile The temp file containing the results, may not be 
    * <code>null</code> and must reference a valid file.  Will be deleted after 
    * the reader returned by {@link #getContent()} is closed.
    * @param fileType The type of file that was converted, never 
    * <code>null</code> or empty. This is the description of the file type 
    * returned by the conversion implementation.
    * @param encoding The encoding used to write the file, never 
    * <code>null</code> or empty.  Either the IANA or Java encoding name
    * may be supplied.
    */
   public PSConversionResults(PSPurgableTempFile resultFile, String fileType, 
      String encoding)
   {
      if (resultFile == null || !resultFile.exists())
         throw new IllegalArgumentException(
            "resultFile may not be null and must exist");
      
      if (fileType == null || fileType.trim().length() == 0)
         throw new IllegalArgumentException(
            "fileType may not be null or empty");
      
      if (encoding == null || encoding.trim().length() == 0)
         throw new IllegalArgumentException(
            "encoding may not be null or empty");
   
      m_resultFile = resultFile;
      m_fileType = fileType;
      m_encoding = PSCharSets.getJavaName(encoding);
   }
   
   /**
    * Get a reader to the content of the conversion results.
    * 
    * @return The reader, never <code>null</code>.  Caller assumes ownership of
    *  the reader and is responsible for closing it.
    * 
    * @throws FileNotFoundException if the file supplied during construction
    * does not exist or cannot be opened.
    * @throws UnsupportedEncodingException if the encoding supplied during
    * construction is not supported.
    */
   public Reader getContent() 
      throws UnsupportedEncodingException, FileNotFoundException 
   {
      return new InputStreamReader(new PSPurgableFileInputStream(m_resultFile), 
         m_encoding);
   }
   
   /**
    * Get the type of file that was converted.
    * 
    * @return The file type supplied during construction, never 
    * <code>null</code> or empty.
    */
   public String getFileType()
   {
      return m_fileType; 
   }
   
   /**
    * Get the encoding used to write the results.
    * 
    * @return The encoding supplied during construction, never 
    * <code>null</code> or empty.
    */
   public String getEncoding()
   {
      return m_encoding;
   }
   
   /**
    * The temp file containing the conversion results, supplied during ctor, never
    * <code>null</code> or modified after that.
    */
   private PSPurgableTempFile m_resultFile;
   
   /**
    * The type of file that was converted, never <code>null</code> or empty or
    * modified after ctor.
    */
   private String m_fileType;
   
   /**
    * The Java name of the encoding of file that was converted, never 
    * <code>null</code> or empty or modified after ctor.
    */
   private String m_encoding;
}
